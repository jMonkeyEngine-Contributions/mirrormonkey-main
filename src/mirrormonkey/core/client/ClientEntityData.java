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

package mirrormonkey.core.client;

import java.util.Collection;

import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.core.InstanceRemovedEvent;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.member.DynamicMemberData;

import com.jme3.network.MessageConnection;

/**
 * Stores mutable data about entities that is client-specific.
 * 
 * Basically, this stores the same information as the server-specific version of
 * <tt>DynamicEntityData</tt>, only that there is only one possible connection
 * and one possible static context at any given time, so the complicated mapping
 * system that is present in the server can be completely omitted.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientEntityData extends DynamicEntityData {

	/**
	 * The static context in which the entity represented by this
	 * <tt>ClientEntityData</tt> is visible to this client. Contains information
	 * about how to handle the members of the local instance.
	 * 
	 * This will be <tt>null</tt> if the entity is not visible to this client.
	 */
	private StaticEntityData staticData;

	/**
	 * Contains mutable data about the interesting members of the local instance
	 * of the entity represented by this <tt>ClientEntityData</tt>.
	 * 
	 * This will be <tt>null</tt> if the entity is not visible to this client.
	 */
	private DynamicMemberData[] dynamicMembers;

	/**
	 * Contains information about entities and connections. We keep track of it
	 * because we may want to register / unregister this
	 * <tt>ClientEntityData</tt> to / from it.
	 */
	private final ClientCoreModule module;

	/**
	 * Creates a new <tt>ClientEntityData</tt> that will represent an entity
	 * with a given ID, local instance and responsible core module.
	 * 
	 * @param id
	 *            the ID of the entity
	 * @param entity
	 *            the local instance of the entity (or <tt>null</tt> if no local
	 *            instance exists)
	 * @param coreModule
	 *            the core module responsible for keeping data
	 */
	public ClientEntityData(int id, SyncEntity entity,
			ClientCoreModule coreModule) {
		super(id, entity);
		this.module = coreModule;
	}

	/**
	 * Called whenever the static context in which the entity represented by
	 * this <tt>ClientEntityData</tt> changes and sets the new context.
	 * 
	 * @param staticData
	 *            the new context under which the entity is visible or
	 *            <tt>null</tt> if it becomes invisible
	 */
	public void setStaticData(StaticEntityData staticData) {
		if (this.staticData == staticData) {
			return;
		}
		if (this.staticData != null) {
			// this.staticData.removeEntity(this);
		}
		if (staticData != null) {
			// staticData.addEntity(this);
			dynamicMembers = staticData.createDynamicMemberData(this);
		} else {
			dynamicMembers = null;
		}
		this.staticData = staticData;
	}

	@Override
	public void destroy() {
		if (isNotDummy()) {
			if (staticData != null) {
				// staticData.removeEntity(this);
			}
			module.getEntityProvider().removeData(this);
			if (getLocalInstance() != null) {
				getLocalInstance().setData(null);
			}
			setNotDummy(false);
		}
	}

	@Override
	public void addInstanceLifecycleListener(InstanceLifecycleListener listener) {
		module.getEntityProvider().assertNotOutdated(this);
		if (!isNotDummy()) {
			module.getEntityProvider().registerData(this);
			setNotDummy(true);
		}
		super.addInstanceLifecycleListener(listener);
	}

	@Override
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener) {
		module.getEntityProvider().assertNotOutdated(this);
		if (isNotDummy() && !isEntityPresent()) {
			destroy();
			setNotDummy(false);
		}
		super.removeInstanceLifecycleListener(listener);
	}

	@Override
	public final StaticEntityData getActiveStaticData(
			MessageConnection forConnection) {
		return staticData;
	}

	@Override
	public final DynamicMemberData[] getMemberData(StaticEntityData staticData) {
		return dynamicMembers;
	}

	@Override
	public final Collection<ConnectionInfo<?>> getActiveConnections(
			StaticEntityData staticData) {
		return module.getConnectionInfoAsCollection();
	}

	/**
	 * Called whenever the entity represented by this <tt>ClientEntityData</tt>
	 * becomes visible to this client.
	 * 
	 * Registers this <tt>ClientEntityData</tt> to the core module if it was a
	 * dummy instance before and notifies <tt>InstanceLifecycleListeners</tt>.
	 * 
	 * @param reference
	 *            the client-local instance
	 * @param staticData
	 *            static context in which the entity becomes visible
	 */
	protected void referenceArrived(SyncEntity reference,
			StaticEntityData staticData) {
		setLocalInstance(reference);
		reference.setData(this);
		setStaticData(staticData);
		if (!isNotDummy()) {
			module.getEntityProvider().registerData(this);
			setNotDummy(true);
		}
		module.notifyEntityRegistration(this);
	}

	/**
	 * Called whenever the entity represented by this <tt>ClientEntityData</tt>
	 * becomes invisible to this client.
	 * 
	 * Unregisters this <tt>ClientEntityData</tt> from the core module if there
	 * are no <tt>InstanceLifecycleListeners</tt> registered and notifies
	 * <tt>InstanceLifecycleListeners</tt>.
	 * 
	 */
	public void referenceDeparted() {
		module.notifyEndListeners(new InstanceRemovedEvent(getLocalInstance(),
				this, staticData, module.getData(module.getAppState()
						.getClient())));
		module.notifyEntityRemoval(this);
		// this if should never be necessary for normal programs, but is needed
		// by the test applications to clear after each test case
		if (getLocalInstance() != null) {
			getLocalInstance().setData(null);
		}
		setLocalInstance(null);
		setStaticData(null);
		if (isNotDummy() && !hasInstanceLifecycleListeners()) {
			destroy();
			setNotDummy(false);
		}
	}

	/**
	 * Called whenever the client-local instance of the entity represented by
	 * this <tt>ClientEntityData</tt> changes.
	 * 
	 * Due to spaghetti code, this does not change the static context or notify
	 * listeners. That is done by the <tt>EntityChangeListener</tt> that calls
	 * this method.
	 * 
	 * @param newReference
	 *            the new local instance of the entity
	 */
	protected void newReferenceArrived(SyncEntity newReference) {
		setLocalInstance(newReference);
		newReference.setData(this);
	}

}
