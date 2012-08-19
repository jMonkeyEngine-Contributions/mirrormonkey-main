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

package mirrormonkey.core.server;

import java.util.HashMap;
import java.util.Map;

import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.framework.ServerSyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.Server;

/**
 * Contains data about connections on the server side.
 * 
 * In addition to <tt>ConnectionInfo</tt>, this class will track the entities
 * that are visible to the connection that it represents.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ServerConnectionInfo extends ConnectionInfo<ServerSyncAppState>
		implements ConnectionListener {

	/**
	 * The <tt>CoreModule</tt> responsible for storing a reference to this
	 * <tt>ServerConnectionInfo</tt> and data about the entities
	 */
	private final ServerCoreModule module;

	/**
	 * Maps entity IDs to <tt>MappingStack</tt> references that describe how an
	 * entity is visible to the connection represented by this
	 * <tt>ServerConnectionInfo</tt>.
	 */
	private final Map<Integer, MappingStack> entities;

	/**
	 * Creates a new <tt>ServerConnectionInfo</tt> that is owned by a given
	 * <tt>SyncAppState</tt> and represents a given <tt>MessageConnection</tt>
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param connection
	 *            the <tt>MessageConnection</tt> for which this
	 *            <tt>ServerConnectionInfo</tt> stores data
	 */
	public ServerConnectionInfo(ServerSyncAppState appState,
			MessageConnection connection) {
		super(appState, connection, false);
		module = appState.getModule(ServerCoreModule.class);
		entities = new HashMap<Integer, MappingStack>();
	}

	/**
	 * Called whenever the first <tt>InstanceLifecycleListener</tt> is added to
	 * this <tt>ServerConnectionInfo</tt> or the first entity becomes visible.
	 * Turns this <tt>ServerConnectionInfo</tt> from a dummy instance to the
	 * real instance representing <tt>connection</tt>.
	 * 
	 * Internal use only.
	 */
	protected void register() {
		module.register(this);
		appState.getEventManager().addConnectionListener(this);
		registered = true;
	}

	/**
	 * Makes every entity that is currently visible to the represented
	 * connection invisible to it and turns this <tt>ServerConnectionInfo</tt>
	 * into a dummy instance.
	 * 
	 * After calling this method, this <tt>ServerConnectionInfo</tt> will no
	 * longer be returned by the core module when calling <tt>getData</tt> with
	 * the represented connection as parameter.
	 */
	public void destroy() {
		assertNotOutdated();
		while (!entities.isEmpty()) {
			entities.values().iterator().next().destroy();
		}
		appState.getEventManager().removeConnectionListener(this);
		module.remove(connection);
		registered = false;
	}

	/**
	 * Called by a <tt>MappingStack</tt> when it is turned from a dummy instance
	 * into an existing instance. This happens when an entity becomes visible to
	 * the represented connection.
	 * 
	 * @param entity
	 *            the entity that is visible to the represented connection by
	 *            the mapping stack
	 * @param s
	 *            the stack that describes how the entity is visible
	 */
	protected void stackCreationCallback(ServerEntityData entity, MappingStack s) {
		entities.put(entity.getId(), s);
		checkRegister();
	}

	/**
	 * Called by a <tt>MappingStack</tt> when it is turned from an existing
	 * instance into a dummy instance. This happens when an entity becomes
	 * invisible to the represented connection.
	 * 
	 * @param entity
	 *            the entity that became invisible to the represented connection
	 * @param s
	 *            the stack that described how the entity was visible
	 */
	protected void stackDestructionCallback(ServerEntityData entity,
			@SuppressWarnings("unused") MappingStack s) {
		entities.remove(entity.getId());
		checkDestroy();
	}

	/**
	 * Checks whether this <tt>ServerConnectionInfo</tt> is outdated and throws
	 * an <tt>IllegalStateException</tt> if it is.
	 * 
	 * A <tt>ServerConnectionInfo</tt> is considered outdated if it is a dummy
	 * object and there is a real <tt>ServerConnectionInfo</tt> present that
	 * represents the same connection or if it is a real element and there is
	 * another real element that represents the same connection (although the
	 * latter should never happen).
	 */
	protected void assertNotOutdated() {
		if (!isRegistered()) {
			ServerConnectionInfo info = module.getData(connection);
			if (info.isRegistered()) {
				throw new IllegalStateException();
			}
		} else {
			ServerConnectionInfo info = module.getData(connection);
			if (info != this) {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void addInstanceLifecycleListener(InstanceLifecycleListener listener) {
		assertNotOutdated();
		super.addInstanceLifecycleListener(listener);
		checkRegister();
	}

	@Override
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener) {
		assertNotOutdated();
		super.removeInstanceLifecycleListener(listener);
		checkDestroy();
	}

	/**
	 * Checks whether the internal state of this <tt>ServerConnectionInfo</tt>
	 * has changed so that this instance was turned from a dummy instance into a
	 * real instance. If that is the case, then this instance will be registered
	 * in the core module.
	 */
	@SuppressWarnings("deprecation")
	private void checkRegister() {
		if (!registered
				&& (!getListenerConfiguration().isEmpty() || !entities
						.isEmpty())) {
			register();
		}
		if (!running && !entities.isEmpty()) {
			running = true;
		}
	}

	/**
	 * Checks whether the internal state of this <tt>ServerConnectionInfo</tt>
	 * has changed so that this instance was turned from a real instance into a
	 * dummy instance. If that is the case, then this instance will be removed
	 * from the core module.
	 */
	@SuppressWarnings("deprecation")
	private void checkDestroy() {
		if (running && entities.isEmpty()) {
			running = false;
		}
		if (registered && getListenerConfiguration().isEmpty()
				&& entities.isEmpty()) {
			destroy();
		}
	}

	public void connectionAdded(Server server, HostedConnection conn) {
	}

	public void connectionRemoved(Server server, HostedConnection conn) {
		if (conn.equals(connection)) {
			// prevent messages from being sent on destroy
			running = false;
			destroy();
		}
	}

}
