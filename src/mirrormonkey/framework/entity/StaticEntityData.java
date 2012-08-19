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

package mirrormonkey.framework.entity;

import java.util.HashMap;
import java.util.Map;

import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.framework.member.MemberDataKey;
import mirrormonkey.framework.member.StaticMemberData;

/**
 * Responsible for keeping data that is common to all entities where the
 * top-most element of the mapping stack has the same local and connected class.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class StaticEntityData {

	/**
	 * Local entity class for which this <tt>StaticEntityData</tt> was created.
	 */
	private final Class<? extends SyncEntity> localClass;

	/**
	 * Connected entity class for which this <tt>StaticEntityData</tt> was
	 * created.
	 */
	private final Class<? extends SyncEntity> connectedClass;

	/**
	 * Contains all classes in the class hierarchy of local and connected entity
	 * class for which bound listeners should be notified on instance lifecycle
	 * events.
	 */
	private final Class<?>[] notifyClasses;

	/**
	 * For every member, there can be one interested module. When parsing the
	 * local and connected class hierarchy, an instance of
	 * <tt>StaticMemberData</tt> is created for every member that has a
	 * corresponding member in the other class hierarchy.
	 * 
	 * The annotation parsing is deterministic in respect to the IDs that a
	 * member will receive when exchanging local and connected class.
	 * 
	 * Contains the same instances of <tt>StaticMemberData</tt> as
	 * <tt>membersByKey</tt>, only mapped for different use cases.
	 */
	private final StaticMemberData[] membersById;

	/**
	 * Contains the same instances of <tt>StaticMemberData</tt> as
	 * <tt>membersById</tt>, but they are mapped by instances of
	 * <tt>MemberDataKey</tt> instead of their IDs.
	 */
	private final Map<MemberDataKey, StaticMemberData> membersByKey;

	/**
	 * Creates a new <tt>StaticEntityData</tt>. Called by
	 * <tt>StaticEntityDataIR</tt> to extract collected data.
	 * 
	 * @param localClass
	 *            local entity class
	 * @param connectedClass
	 *            connected entity class
	 * @param members
	 *            static data about every member that has a module interested in
	 *            it
	 * @param notifyClasses
	 *            whenever an instance lifecycle event occurs, every
	 *            <tt>InstanceLifecycleListener</tt> that is bound to one of
	 *            these classes will be notified
	 */
	public StaticEntityData(Class<? extends SyncEntity> localClass,
			Class<? extends SyncEntity> connectedClass,
			StaticMemberData[] members, Class<?>[] notifyClasses) {
		this.localClass = localClass;
		this.connectedClass = connectedClass;
		this.membersById = members;
		membersByKey = new HashMap<MemberDataKey, StaticMemberData>();
		for (StaticMemberData data : members) {
			data.setStaticEntityData(this);
			membersByKey.put(data.getMemberKey(), data);
		}
		this.notifyClasses = notifyClasses;
	}

	/**
	 * Extracts mutable data about members, if necessary. Called whenever an
	 * entity becomes visible to the first connection in context of this
	 * <tt>StaticEntityData</tt>.
	 * 
	 * @param entity
	 *            the entity for which dynamic data should be created
	 * @return an array of <tt>DynamicMemberData</tt> that contains an instance
	 *         of <tt>DynamicMemberData</tt> on index <tt>i</tt> if the
	 *         <tt>StaticMemberData</tt> with ID <tt>i</tt> requests that
	 *         dynamic data be stored and <tt>null</tt> on all indices
	 *         <tt>j</tt> where j are the IDs of the instances of
	 *         <tt>StaticMemberData</tt> that do not request that dynamic data
	 *         be stored
	 */
	public final DynamicMemberData[] createDynamicMemberData(
			DynamicEntityData entity) {
		DynamicMemberData[] dynamicData = new DynamicMemberData[membersById.length];
		int id = 0;
		for (StaticMemberData i : membersById) {
			dynamicData[id++] = i.createDynamicData(entity);
		}
		return dynamicData;
	}

	/**
	 * @return immutable data on how to handle members of entities in this
	 *         context
	 */
	public final StaticMemberData[] getMembersById() {
		return membersById;
	}

	/**
	 * Fetches the instance of <tt>StaticMemberData</tt> that is mapped by a
	 * given ID. Expects that instance to be of class <tt>castTo</tt> and will
	 * downcast it to that class, then return it.
	 * 
	 * @param id
	 *            the ID of the <tt>StaticMemberData</tt> to fetch
	 * @param castTo
	 *            expected class of the <tt>StaticMemberData</tt> to fetch
	 * @return the instance of <tt>StaticMemberData</tt> with ID <tt>id</tt>,
	 *         downcast to <tt>castTo</tt>
	 */
	@SuppressWarnings("unchecked")
	public final <T extends StaticMemberData> T getData(int id,
			@SuppressWarnings("unused") Class<T> castTo) {
		return (T) membersById[id];
	}

	/**
	 * Fetches the instance of <tt>StaticMemberData</tt> that is mapped by a
	 * given <tt>MemberDataKey</tt>. Expects that instance to be assignment
	 * compatible with a given class and casts it down to that class.
	 * 
	 * @param key
	 *            the <tt>MemberDataKey</tt> to look up
	 * @param castTo
	 *            the class to cast the found <tt>StaticMemberData</tt> to
	 * @return the <tt>StaticMemberData</tt> mapped by <tt>key</tt> or
	 *         <tt>null</tt> if none could be found
	 */
	@SuppressWarnings("unchecked")
	public final <T extends StaticMemberData> T getData(MemberDataKey key,
			@SuppressWarnings("unused") Class<T> castTo) {
		return (T) membersByKey.get(key);
	}

	/**
	 * @return the local entity class
	 */
	public final Class<? extends SyncEntity> getLocalClass() {
		return localClass;
	}

	/**
	 * @return the connected entity class
	 */
	public final Class<? extends SyncEntity> getConnectedClass() {
		return connectedClass;
	}

	@Override
	public final String toString() {
		return "[" + super.toString() + ": local=" + localClass.getName()
				+ ", connected=" + connectedClass.getName() + "]";
	}

	/**
	 * @return an array of all classes for which bound
	 *         <tt>InstanceLifecycleListeners</tt> should be notified
	 */
	public final Class<?>[] getNotifyClasses() {
		return notifyClasses;
	}

}
