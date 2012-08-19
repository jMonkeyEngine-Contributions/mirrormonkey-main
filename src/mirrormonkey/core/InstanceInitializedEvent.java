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
 * Event that is dispatched whenever an entity is made visible to a connection
 * and a client-local instance is created.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class InstanceInitializedEvent implements InstanceLifecycleEvent {

	/**
	 * The local instance of the entity.
	 */
	public final SyncEntity instance;

	/**
	 * The <tt>DynamicEntityData</tt> instance holding mutable data about the
	 * entity.
	 */
	public final DynamicEntityData dynamicData;

	/**
	 * The <tt>StaticEntityData</tt> instance providing information about the
	 * context (local and connected class) in which the instance became visible.
	 */
	public final StaticEntityData staticData;

	/**
	 * Information about the connection that the entity became visible to.
	 */
	public final ConnectionInfo<?> connection;

	/**
	 * Creates a new <tt>InstanceInitializedEvent</tt> to be dispatched with the
	 * data provided.
	 * 
	 * @param instance
	 *            local instance of the entity
	 * @param dynamicData
	 *            mutable data about the entity
	 * @param staticData
	 *            immutable data about how the entity must be handled in this
	 *            context
	 * @param connection
	 *            data about the connection that the entity became visible to
	 */
	public InstanceInitializedEvent(SyncEntity instance,
			DynamicEntityData dynamicData, StaticEntityData staticData,
			ConnectionInfo<?> connection) {
		this.instance = instance;
		this.dynamicData = dynamicData;
		this.staticData = staticData;
		this.connection = connection;
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!InstanceInitializedEvent.class.isInstance(o)) {
			return false;
		}
		InstanceInitializedEvent e = (InstanceInitializedEvent) o;
		return instance == e.instance && dynamicData == e.dynamicData
				&& staticData == e.staticData && connection == e.connection;
	}

}