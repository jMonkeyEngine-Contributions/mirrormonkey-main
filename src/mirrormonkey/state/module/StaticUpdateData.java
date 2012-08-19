/*
 * Copyright (c) 2011, 2012, Philipp Christian Loewner
 * All rights reserved.
 * 
 * Disclaimer:
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * This software uses parts of:
 * jMonkeyEngine
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 * 
 * For jMonkeyEngine, the same disclaimer as printed above applies.
 */

package mirrormonkey.state.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.state.annotations.UpdateSetId;
import mirrormonkey.state.member.StaticUpdateMemberData;

/**
 * Contains data on how to synchronize the state of two entity instances in a
 * specific static context.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StaticUpdateData {

	/**
	 * Manages overall synchronization.
	 */
	protected final SyncAppState<?> appState;

	/**
	 * Data about the static context in which synchronized entities are visible.
	 */
	protected final StaticEntityData staticData;

	/**
	 * This is reference-equal to the local <tt>StateModule's</tt> updateQueue.
	 */
	private final UpdateQueue queue;

	/**
	 * Data about the different update sets that need to be synchronized in the
	 * given context.
	 */
	private final UpdateSet[] sets;

	/**
	 * Entities that are currently synchronized in the given context.
	 */
	private final Map<Integer, EntityReg> activeEntities;

	/**
	 * Creates a new <tt>StaticUpdateData</tt> that will determine how to update
	 * instances of entities in a given context and will schedule updates in a
	 * given <tt>UpdateQueue</tt>.
	 * 
	 * @param appState
	 *            responsible for managing overall synchronization
	 * @param staticData
	 *            static context in which synchronized entities are visible
	 * @param queue
	 *            manages updates for every <tt>StaticUpdateData</tt>
	 */
	public StaticUpdateData(SyncAppState<?> appState,
			StaticEntityData staticData, UpdateQueue queue) {
		this.appState = appState;
		this.staticData = staticData;
		this.queue = queue;
		sets = extractSets(this, queue.module.lastTime);
		activeEntities = new HashMap<Integer, EntityReg>();
	}

	/**
	 * Starts synchronizing an entity with a connection in the represented
	 * static context.
	 * 
	 * @param entity
	 *            dynamic data about the entity that should be synchronized
	 * @param connection
	 *            data about the connection that the entity should be
	 *            synchronized to
	 */
	@SuppressWarnings("synthetic-access")
	public void add(DynamicEntityData entity,
			@SuppressWarnings("unused") ConnectionInfo<?> connection) {
		if (activeEntities.isEmpty()) {
			for (UpdateSet h : sets) {
				queue.addSet(h);
			}
		}

		EntityReg reg = activeEntities.get(entity.getId());
		if (reg == null) {
			reg = new EntityReg(entity);
			activeEntities.put(entity.getId(), reg);
		}
		reg.count++;
	}

	/**
	 * Stops synchronizing an entity with a connection in the represented static
	 * context.
	 * 
	 * @param entityId
	 *            dynamic data about the entity that should no longer be
	 *            synchronized in the represented static context
	 * @param connection
	 *            data about the connection that the entity should no longer be
	 *            synchronized to in the represented static context
	 */
	@SuppressWarnings("synthetic-access")
	public void remove(Integer entityId,
			@SuppressWarnings("unused") ConnectionInfo<?> connection) {
		EntityReg reg = activeEntities.get(entityId);
		if (--reg.count == 0) {
			activeEntities.remove(entityId);
		}

		if (activeEntities.isEmpty()) {
			for (UpdateSet h : sets) {
				queue.removeSet(h);
			}
		}
	}

	/**
	 * @return a collection containing all entities that are currently
	 *         synchronized to at least one client in the represented static
	 *         context
	 */
	protected final Collection<EntityReg> getActiveEntities() {
		return activeEntities.values();
	}

	/**
	 * Keeps track of local entity instances that must be synchronized to at
	 * least one connection in this context.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class EntityReg {

		/**
		 * Data about the entity.
		 */
		public final DynamicEntityData entity;

		/**
		 * Dynamic member data of the entity.
		 */
		public final DynamicMemberData[] dynamicData;

		/**
		 * Number of clients that the entity must be synchronized to in this
		 * context.
		 */
		private int count;

		/**
		 * Creates a new <tt>EntityReg</tt> for a given entity.
		 * 
		 * @param entity
		 *            dynamic data about the entity
		 */
		public EntityReg(DynamicEntityData entity) {
			this.entity = entity;
			this.dynamicData = entity.getMemberData(staticData);
			count = 0;
		}

	}

	/**
	 * IR class used to gather data about update sets when a specific static
	 * context is used for the first time.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private static final class TempUpdateSet {

		/**
		 * Update set ID.
		 */
		private final int id;

		/**
		 * Determines whether reliable transfer should be used for the set.
		 */
		private final boolean reliable;

		/**
		 * Time between updates, in nanoseconds.
		 */
		private final long freq;

		/**
		 * Members that declare this update set's ID.
		 */
		private final Collection<StaticUpdateMemberData> members;

		/**
		 * Creates a new <tt>TempUpdateSet</tt>.
		 * 
		 * @param id
		 *            update set ID
		 * @param reliable
		 *            <tt>true</tt> if reliable transfer should be used for this
		 *            set, <tt>false</tt> if unreliable transfer should be used
		 * @param freq
		 *            time between updates, in nanoseconds
		 */
		public TempUpdateSet(int id, boolean reliable, long freq) {
			this.id = id;
			this.reliable = reliable;
			this.freq = freq;
			members = new LinkedList<StaticUpdateMemberData>();
		}

		/**
		 * Adds a member to the represented update set.
		 * 
		 * @param memberData
		 *            static data about the member
		 */
		public final void addMember(StaticUpdateMemberData memberData) {
			if (id != UpdateSetId.NO_SET
					&& (memberData.reliable != reliable || memberData.freq != freq)) {
				throw new IllegalStateException(memberData
						+ " does not fit into this set. Requires reliable="
						+ reliable + " and frequency=" + freq);
			}
			members.add(memberData);
		}

		/**
		 * Creates a new <tt>UpdateSet</tt> for the gathered data.
		 * 
		 * @param staticData
		 *            data about the static state synchronization context
		 * @param currentTime
		 *            local sync time on the current frame
		 * @return a new instance of <tt>UpdateSet</tt> that corresponds to the
		 *         data that this <tt>TempUpdateSet</tt> gathered
		 */
		public final UpdateSet extractSet(StaticUpdateData staticData,
				long currentTime) {
			return new UpdateSet(
					staticData,
					reliable,
					id,
					members.toArray(new StaticUpdateMemberData[members.size()]),
					freq, currentTime);
		}
	}

	/**
	 * Examines a new static context in which an entity can be visible for its
	 * update sets.
	 * 
	 * @param sd
	 *            data about the static context
	 * @param time
	 *            current local sync time
	 * @return array containing all found sync sets
	 */
	private static final UpdateSet[] extractSets(StaticUpdateData sd, long time) {
		final Map<Integer, TempUpdateSet> tempSets = new HashMap<Integer, TempUpdateSet>();

		for (StaticMemberData smd : sd.staticData.getMembersById()) {
			if (!StaticUpdateMemberData.class.isInstance(smd)) {
				continue;
			}
			StaticUpdateMemberData sud = (StaticUpdateMemberData) smd;
			if (sud.readAccessor == null) {
				continue;
			}
			TempUpdateSet tus = tempSets.get(sud.set);
			if (tus == null) {
				tus = new TempUpdateSet(sud.set, sud.reliable, sud.freq);
				tempSets.put(sud.set, tus);
			}
			tus.addMember(sud);
		}

		UpdateSet[] val = new UpdateSet[tempSets.values().size()];
		int i = 0;
		for (TempUpdateSet tus : tempSets.values()) {
			val[i++] = tus.extractSet(sd, time);
		}
		return val;
	}
}
