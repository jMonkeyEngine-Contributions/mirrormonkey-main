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

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.ServerSyncAppState;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.util.IdGenerator;

import com.jme3.network.MessageConnection;

/**
 * Server-Side implementation of <tt>CoreModule</tt>. In addition to
 * <tt>CoreModule</tt> it provides capabilities to generate unique entity IDs
 * and store <tt>ServerConnectionInfo</tt> instances representing all
 * connections that currently have entities visible to them.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ServerCoreModule extends
		CoreModule<ServerEntityData, ServerSyncAppState> {

	/**
	 * Used to generate unique IDs for entities.
	 */
	private final IdGenerator idGenerator;

	/**
	 * Maps connections to the <tt>ServerConnectionInfo</tt> references that
	 * represent them.
	 */
	private final Map<MessageConnection, ServerConnectionInfo> connectionData;

	/**
	 * Creates a new <tt>ServerCoreModule</tt> that will be managed by a given
	 * <tt>SyncAppState</tt>.
	 * 
	 * This constructor will be called reflectively by <tt>appState</tt>.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> that creates this
	 *            <tt>ServerCoreModule</tt>
	 */
	public ServerCoreModule(ServerSyncAppState appState) {
		super(appState, false);
		idGenerator = new IdGenerator();
		idGenerator.reserve(EntityProvider.NULL_ID);
		connectionData = new HashMap<MessageConnection, ServerConnectionInfo>();
	}

	/**
	 * Fetches the <tt>ServerEntityData</tt> representing a given entity. Will
	 * create and return a new dummy element if no element exists.
	 * 
	 * Dummy elements are registered and unregistered on demand. If no dummy
	 * element is created, the reference returned by <tt>entity.getData</tt>
	 * will be returned.
	 * 
	 * @param entity
	 *            the entity
	 * @return either the same reference that <tt>entity.getData</tt> would
	 *         return or a newly created <tt>ServerEntityData</tt> if
	 *         <tt>entity.getData</tt> would return <tt>null</tt>
	 */
	public ServerEntityData getData(SyncEntity entity) {
		ServerEntityData data = (ServerEntityData) entity.getData();
		if (data != null) {
			return data;
		}
		return new ServerEntityData(this, idGenerator.generateAndReserve(),
				entity);
	}

	@Override
	public ServerConnectionInfo getData(MessageConnection connection) {
		ServerConnectionInfo data = connectionData.get(connection);
		if (data != null) {
			return data;
		}
		return new ServerConnectionInfo(getAppState(), connection);
	}

	@Override
	public void update(float tpf) {
		for (ServerConnectionInfo i : connectionData.values()) {
			i.update();
		}
		super.update(tpf);
	}

	/**
	 * Removes the <tt>ServerConnectionInfo</tt> representing a given connection
	 * from this <tt>ServerCoreModule</tt> so that it will no longer be returned
	 * by the <tt>getData</tt> method.
	 * 
	 * Internal use only. Use getData(connection).destroy if you want to
	 * unregister a connection from MirrorMonkey.
	 * 
	 * @param connection
	 *            the connection for which the representing data should be
	 *            removed
	 */
	protected void remove(MessageConnection connection) {
		connectionData.remove(connection);
	}

	/**
	 * Adds a <tt>ServerConnectionInfo</tt> representing some connection so that
	 * it will be returned by the <tt>getData</tt> method.
	 * 
	 * Internal use only. Instances of <tt>ServerConnectionInfo</tt> are
	 * registered on demand.
	 * 
	 * @param info
	 *            the <tt>ServerConnectionInfo</tt> that was just turned from a
	 *            dummy instance into a real instance
	 */
	protected void register(ServerConnectionInfo info) {
		connectionData.put(info.getConnection(), info);
	}

	/**
	 * Checks whether there is an instance of <tt>ConnectionInfo</tt> present
	 * that represents a given connection.
	 * 
	 * @param connection
	 *            the connection to check
	 * @return <tt>true</tt>, if <tt>getData</tt> will return a real instance
	 *         for <tt>connection</tt>, <tt>false</tt> if it would create and
	 *         return a dummy instance.
	 */
	public boolean hasData(MessageConnection connection) {
		return connectionData.containsKey(connection);
	}
}
