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

package mirrormonkey.core;

import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Event that is dispatched whenever the client-local of an already visible
 * entity changes. This happens whenever the top-most element of the mapping
 * stack changes on the server-side or when a client-local constructor is called
 * with the override option enabled.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class InstanceReplacedEvent implements InstanceLifecycleEvent {

	/**
	 * The old local instance of the entity.
	 */
	public final SyncEntity oldInstance;

	/**
	 * The new local instance of the entity.
	 * 
	 * On the server side, this will always be the same as <tt>oldEntity</tt>.
	 */
	public final SyncEntity newInstance;

	/**
	 * Contains mutable information about the entity.
	 */
	public final DynamicEntityData dynamicData;

	/**
	 * Contains immutable information about the context (local and connected
	 * class) in which the entity was visible before.
	 */
	public final StaticEntityData oldStaticData;

	/**
	 * Contains immutable information about the context (local and connected
	 * class) in which the entity is going to be visible from now on.
	 */
	public final StaticEntityData newStaticData;

	/**
	 * Contains information about the connection for which the client-local
	 * instance has changed.
	 */
	public final ConnectionInfo<?> connection;

	/**
	 * Creates a new <tt>InstanceReplacedEvent</tt> to be dispatched with the
	 * data provided.
	 * 
	 * @param oldInstance
	 *            old local instance of the entity
	 * @param newInstance
	 *            new local instance of the entity
	 * @param dynamicData
	 *            mutable data about the entity
	 * @param oldStaticData
	 *            immutable data about how the entity had to be handled before
	 *            the instance changed
	 * @param newStaticData
	 *            immutable data about how the entity has to be handled from now
	 *            on
	 * @param connection
	 *            data about the connection for which the client-local instance
	 *            changed
	 */
	public InstanceReplacedEvent(SyncEntity oldInstance, SyncEntity newInstance,
			DynamicEntityData dynamicData, StaticEntityData oldStaticData,
			StaticEntityData newStaticData, ConnectionInfo<?> connection) {
		this.oldInstance = oldInstance;
		this.newInstance = newInstance;
		this.dynamicData = dynamicData;
		this.oldStaticData = oldStaticData;
		this.newStaticData = newStaticData;
		this.connection = connection;
	}

	/**
	 * @return <tt>true</tt> if the old reference is the same as the new
	 *         reference, <tt>false</tt> otherwise
	 */
	public boolean isReferenceChanged() {
		return oldInstance != newInstance;
	}

	@Override
	public int hashCode() {
		return oldInstance.hashCode() * newInstance.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!InstanceReplacedEvent.class.isInstance(o)) {
			return false;
		}
		InstanceReplacedEvent e = (InstanceReplacedEvent) o;
		return oldInstance == e.oldInstance && newInstance == e.newInstance
				&& dynamicData == e.dynamicData
				&& oldStaticData == e.oldStaticData
				&& newStaticData == e.newStaticData
				&& connection == e.connection;
	}
}