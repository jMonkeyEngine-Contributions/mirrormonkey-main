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
import java.util.LinkedList;

import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.state.OutboundAwareEntity;
import mirrormonkey.state.annotations.UpdateSetId;
import mirrormonkey.state.member.DynamicUpdateData;
import mirrormonkey.state.member.StaticUpdateMemberData;
import mirrormonkey.state.messages.UpdateMessage;
import mirrormonkey.state.module.StaticUpdateData.EntityReg;

/**
 * Contains data about one particular update set.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class UpdateSet implements Comparable<UpdateSet> {

	/**
	 * Data about the static synchronization context that contains the
	 * represented update set.
	 */
	private final StaticUpdateData staticData;

	/**
	 * Determines whether reliable transfer should be used for the represented
	 * update set.
	 */
	private final boolean reliable;

	/**
	 * ID of the represented update set.
	 */
	private final int setId;

	/**
	 * Static data about the members that the represented set contains.
	 */
	private final StaticUpdateMemberData[] members;

	/**
	 * Time between updates, in nanoseconds.
	 */
	private final long freq;

	/**
	 * Contains dynamic data for updated fields during calls to
	 * <tt>performUpdate</tt>.
	 */
	private final Collection<DynamicUpdateData> cachedUpdates;

	/**
	 * Local sync time at which the next update will be performed.
	 */
	private long due;

	/**
	 * Creates a new <tt>UpdateSet</tt>.
	 * 
	 * @param staticData
	 *            static state sync context
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used to send
	 *            update messages, <tt>false</tt> if unreliable transfer should
	 *            be used
	 * @param setId
	 *            ID of the set
	 * @param members
	 *            these will be updated
	 * @param freq
	 *            time between updates in nanoseconds
	 * @param currentTime
	 *            local sync time in the current frame
	 */
	public UpdateSet(StaticUpdateData staticData, boolean reliable, int setId,
			StaticUpdateMemberData[] members, long freq, long currentTime) {
		this.staticData = staticData;
		this.reliable = reliable;
		this.setId = setId;
		this.members = members;
		this.freq = freq;
		this.due = currentTime;
		cachedUpdates = new LinkedList<DynamicUpdateData>();
	}

	/**
	 * Checks whether an update must be performed this frame.
	 * 
	 * @param time
	 *            local sync time of the current frame
	 * @return <tt>true</tt> if the represented update set must be updated this
	 *         frame, <tt>false</tt> if it must not be updated yet
	 */
	public boolean isDue(long time) {
		return time >= due;
	}

	/**
	 * Called after an update to reset the internal update timer.
	 * 
	 * @param newTime
	 *            local sync time of the current frame
	 */
	protected final void reset(long newTime) {
		while (due <= newTime) {
			due += freq;
		}
	}

	/**
	 * Reads the value for all fields of all local entity instances contained in
	 * the represented update set and creates and distributes update messages
	 * for them.
	 * 
	 * @param newTime
	 *            local sync time of the current frame
	 * @param updated
	 *            updated fields will be added to this collection so we can
	 *            perform collective reset on value tracking
	 */
	public void performUpdate(long newTime,
			Collection<DynamicUpdateData> updated) {
		for (EntityReg e : staticData.getActiveEntities()) {
			cachedUpdates.clear();
			for (StaticUpdateMemberData sumd : members) {
				DynamicUpdateData dud = (DynamicUpdateData) e.dynamicData[sumd.id];
				if (dud.isChanged()) {
					cachedUpdates.add(dud);
				}
			}
			if (!cachedUpdates.isEmpty()) {
				OutboundAwareEntity oae = null;
				if (OutboundAwareEntity.class.isInstance(e.entity
						.getLocalInstance()) && setId != UpdateSetId.NO_SET) {
					oae = ((OutboundAwareEntity) e.entity.getLocalInstance());
					oae.beforeOutbound(staticData.appState, setId,
							staticData.staticData.getConnectedClass(), reliable);
				}
				UpdateMessage um = new UpdateMessage(
						staticData.appState.getSyncTime(), reliable,
						e.entity.getId(), setId, new int[cachedUpdates.size()],
						new Object[cachedUpdates.size()]);
				int i = 0;
				for (DynamicUpdateData dud : cachedUpdates) {
					um.fieldIds[i] = dud.getId();
					um.fieldValues[i] = dud.getValue();
					updated.add(dud);
					i++;
				}
				for (ConnectionInfo<?> ci : e.entity
						.getActiveConnections(staticData.staticData)) {
					ci.send(um);
				}
				if (oae != null) {
					oae.afterOutbound(staticData.appState, setId,
							staticData.staticData.getConnectedClass(), reliable);
				}
			}
		}
		reset(newTime);
	}

	public int compareTo(UpdateSet o) {
		return ((Long) due).compareTo(o.due);
	}

	/**
	 * @return local sync time at which the next update will be performed for
	 *         the represented update set
	 */
	public long getDue() {
		return due;
	}

}
