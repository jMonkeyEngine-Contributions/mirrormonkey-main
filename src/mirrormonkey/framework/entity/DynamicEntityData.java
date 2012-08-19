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

import java.util.Collection;
import java.util.SortedSet;

import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.util.listeners.ListenerConfiguration;

import com.jme3.network.MessageConnection;

/**
 * Contains mutable data for a specific entity, like the current local instance
 * (if any), connections to which the entity is visible and
 * <tt>EntityInstanceListeners</tt> listening specifically to this entity.
 * 
 * If an entity is present, information about the top of the mapping stack
 * (immutable data) is available for every connection.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class DynamicEntityData {

	/**
	 * ID of the entity to identify it over the network.
	 */
	private final int id;

	/**
	 * Contains <tt>InstanceLifecycleListeners</tt> that listen to this entity
	 * specifically.
	 */
	protected final ListenerConfiguration listenerConfiguration = new ListenerConfiguration();

	/**
	 * Set to <tt>true</tt> as long as this <tt>DynamicEntityData</tt> is set as
	 * data for the entity. This is not to be confused with a registered entity
	 * (and thus, not named registered), as that term is used for entities that
	 * are visible to at least one connection. In contrast, this will be
	 * <tt>true</tt> as long as the entity is visible to at least one connection
	 * or has at least one <tt>InstanceLifecycleListener</tt> that listens to
	 * instance lifecycle events on this entity specifically.
	 */
	private boolean notDummy;

	/**
	 * Reference to the current local instance of the entity, if any.
	 */
	private SyncEntity localInstance;

	/**
	 * Creates a new <tt>DynamicEntityData</tt> for the entity with a given ID
	 * and local instance. The local instance may be <tt>null</tt>.
	 * 
	 * @param id
	 *            the ID of the entity
	 * @param localInstance
	 *            the local instance of the entity, or <tt>null</tt> if the
	 *            entity does not currently have a local instance
	 */
	public DynamicEntityData(int id, SyncEntity localInstance) {
		this.id = id;
		this.localInstance = localInstance;
		notDummy = false;
	}

	/**
	 * @return the current local instance of the entity or <tt>null</tt> if the
	 *         entity does not have a local instance
	 */
	public SyncEntity getLocalInstance() {
		return localInstance;
	}

	/**
	 * Sets the new local instance for the entity represented by this
	 * <tt>DynamicEntityData</tt>. If the local instance was not <tt>null</tt>
	 * before, then its <tt>DynamicEntityData</tt> reference will be set to
	 * <tt>null</tt>.
	 * 
	 * Due to spaghetti coding, this does not set the <tt>DynamicEntityData</tt>
	 * reference of <tt>instance</tt> to this <tt>DynamicEntityData</tt>.
	 * 
	 * @param instance
	 *            the new local instance of the entity represented by this
	 *            <tt>DynamicEntityData</tt>
	 */
	protected void setLocalInstance(SyncEntity instance) {
		if (this.localInstance == instance) {
			return;
		}
		if (this.localInstance != null) {
			this.localInstance.setData(null);
		}
		this.localInstance = instance;
	}

	/**
	 * @return the ID of the entity represented by this
	 *         <tt>DynamicEntityData</tt>
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns <tt>true</tt> if, and only if, <tt>CoreModule.getData</tt> would
	 * not return <tt>null</tt> for the entity represented by this
	 * <tt>DynamicEntityData</tt>. In that case, the reference returned by the
	 * method would be this <tt>DynamicEntityData</tt>.
	 * 
	 * @return <tt>true</tt>, if the entity represented by this
	 *         <tt>DynamicEntityData</tt> is registered or has at least one
	 *         <tt>InstanceLifecycleListener</tt> listening to events on this
	 *         entity specifically, <tt>false</tt> otherwise.
	 */
	public boolean isNotDummy() {
		return notDummy;
	}

	/**
	 * Called by the framework with <tt>true</tt> as soon as the entity
	 * represented by this <tt>DynamicEntityData</tt> becomes registered or an
	 * <tt>InstanceLifecycleListener</tt> is added to it.
	 * 
	 * Called with <tt>false</tt> when the entity represented by this
	 * <tt>DynamicEntityData</tt> is not registered any more and the last
	 * <tt>InstanceLifecycleListener</tt> is removed.
	 * 
	 * Interal use only.
	 * 
	 * @param notDummy
	 *            <tt>true</tt> if, and only if, the entity represented by this
	 *            <tt>DynamicEntityData</tt> is transformed from a dummy
	 *            instance to a registered instance, <tt>false</tt> if it is
	 *            changed from a real instance to a dummy instance.
	 */
	protected void setNotDummy(boolean notDummy) {
		this.notDummy = notDummy;
	}

	/**
	 * @return <tt>true</tt> if the entity represented by this
	 *         <tt>DynamicEntityData</tt> has a local instance, <tt>false</tt>
	 *         otherwise
	 */
	public boolean isEntityPresent() {
		return localInstance != null;
	}

	/**
	 * @return <tt>true</tt>, if the entity represented by this
	 *         <tt>DynamicEntityData</tt> has
	 *         <tt>InstanceLifecycleListeners</tt> listening to the entity
	 *         specifically
	 */
	public boolean hasInstanceLifecycleListeners() {
		return !listenerConfiguration.isEmpty();
	}

	/**
	 * Semantically different server and client side implementations at the
	 * moment.
	 * 
	 * Call this on the server side to completely destroy everything that is
	 * known about an entity: Registered <tt>InstanceLifecycleListeners</tt> and
	 * the complete <tt>MappingStack</tt> for every connection. The entity
	 * represented by this <tt>DynamicEntityData</tt> will be made invisible to
	 * every client and every <tt>InstanceLifecycleListener</tt> will be
	 * removed. This <tt>DynamicEntityData</tt> will be turned into a dummy
	 * instance and will no longer be returned by <tt>SyncEntity.getData</tt>
	 * and <tt>CoreModule.getData</tt> for the entity represented by it.
	 * 
	 * On the client side, internal use only. You can (and should) not influence
	 * data kept about entities on the client side.
	 */
	protected abstract void destroy();

	/**
	 * Adds an <tt>InstanceLifecycleListener</tt> that will listen to lifecycle
	 * events on the entity represented by this <tt>DynamicEntityData</tt>
	 * specifically.
	 * 
	 * Subclasses must ensure that if this is a dummy instance before this call,
	 * then it must be turned into a real instance. If this is a dummy instance
	 * and there is already another, real instance present for the entity, then
	 * an <tt>IllegalStateException</tt> should be thrown.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> to notify of instance
	 *            lifecycle events for the entity represented by this
	 *            <tt>DynamicEntityData</tt>
	 */
	public void addInstanceLifecycleListener(InstanceLifecycleListener listener) {
		listenerConfiguration.addListener(listener);
	}

	/**
	 * Removes an <tt>InstanceLifecycleListener</tt> that will no longer be
	 * notified of lifecycle events specifically for the entity represented by
	 * this <tt>DynamicEntityData</tt>.
	 * 
	 * Subclasses must ensure that if the entity represented by this
	 * <tt>DynamicEntityData</tt> is not registered and <tt>listener</tt> was
	 * the last <tt>InstanceLifecycleListener</tt> notified for lifecycle events
	 * specifically of that entity, then this <tt>DynamicEntityData</tt> must be
	 * turned into a dummy instance after the call.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> that should no longer
	 *            be notified of instance lifecycle events for the entity
	 *            represented by this <tt>DynamicEntityData</tt>
	 */
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener) {
		listenerConfiguration.removeListener(listener);
	}

	/**
	 * Adds every <tt>InstanceLifecycleListener</tt> that listens to lifecycle
	 * events for the entity represented by this <tt>DynamicEntityData</tt>
	 * specifically to a set.
	 * 
	 * Used by the framework to determine all listeners to notify when a
	 * lifecycle event occurs.
	 * 
	 * @param listeners
	 *            the <tt>SortedSet</tt> to add all
	 *            <tt>InstanceLifecycleListeners</tt> for the entity represented
	 *            by this <tt>DynamicEntityData</tt> to
	 */
	public void collectInstanceLifecycleListeners(
			SortedSet<InstanceLifecycleListener> listeners) {
		listenerConfiguration.getListeners(InstanceLifecycleListener.class,
				listeners);
	}

	/**
	 * Fetches the <tt>StaticEntityData</tt> that describes the mapping from
	 * local class to connected class that is currently located on top of the
	 * mapping stack for the entity represented by this
	 * <tt>DynamicEntityData</tt> and a given connection.
	 * 
	 * @param forConnection
	 *            the <tt>MessageConnection</tt> for which to fetch the top of
	 *            the mapping stack
	 * @return the <tt>StaticEntityData</tt> containing immutable data about how
	 *         the entity represented by this <tt>DynamicEntityData</tt> is to
	 *         be interpreted in the context of <tt>forConnection</tt> or
	 *         <tt>null</tt> if no such data is available (because the entity
	 *         represented by this <tt>DynamicEntityData</tt> is not visible to
	 *         <tt>forConnection</tt>)
	 */
	public abstract StaticEntityData getActiveStaticData(
			MessageConnection forConnection);

	/**
	 * Fetches mutable data about the relevant members in a specific context.
	 * 
	 * @param staticData
	 *            the context
	 * @return an array of <tt>DynamicMemberData</tt> containing the instances
	 *         of <tt>DynamicMemberData</tt> that keep mutable data about the
	 *         members of the entity represented by this
	 *         <tt>DynamicEntityData</tt> in the context of <tt>staticData</tt>
	 */
	public abstract DynamicMemberData[] getMemberData(
			StaticEntityData staticData);

	/**
	 * Fetches all connections to which the entity represented by this
	 * <tt>DynamicEntityData</tt> is visible in a certain context.
	 * 
	 * @param staticData
	 *            the context
	 * @return a <tt>Collection</tt> of the data about every connection that the
	 *         entity represented by this <tt>DynamicEntityData</tt> is visible
	 *         to and where <tt>staticData</tt> is on top of the mapping stack
	 */
	public abstract Collection<? extends ConnectionInfo<?>> getActiveConnections(
			StaticEntityData staticData);

}
