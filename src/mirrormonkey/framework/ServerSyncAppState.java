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

package mirrormonkey.framework;

import mirrormonkey.util.netevent.server.ServerEventManager;

import com.jme3.network.Server;

/**
 * Responsible for managing modules, listeners and connection listeners on the
 * server side.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ServerSyncAppState extends SyncAppState<ServerEventManager> {

	/**
	 * The <tt>Server</tt> that clients using MirrorMonkey will connect to.
	 */
	private final Server server;

	/**
	 * Creates a new <tt>ServerSyncAppState</tt> using the default
	 * <tt>ServerEventManager</tt> and a given <tt>Server</tt>.
	 * 
	 * @param server
	 *            the <tt>Server</tt> that clients using MirrorMonkey will
	 *            connecte to
	 */
	public ServerSyncAppState(Server server) {
		this(new ServerEventManager(), server);
	}

	/**
	 * Creates a new <tt>ServerSyncAppState</tt> using the given
	 * <tt>ServerEventManager</tt> and <tt>Server</tt>.
	 * 
	 * @param eventManager
	 *            the <tt>ServerEventManager</tt> that will be used to dispatch
	 *            <tt>Messages</tt> and connection events
	 * @param server
	 *            the <tt>Server</tt> that clients using MirrorMonkey will
	 *            connect to
	 */
	public ServerSyncAppState(ServerEventManager eventManager, Server server) {
		super(eventManager);
		server.addMessageListener(eventManager.getMessageListener());
		server.addConnectionListener(eventManager.getConnectionListener());
		this.server = server;
	}

	/**
	 * @return the <tt>Server</tt> that clients using MirrorMonkey will connect
	 *         to
	 */
	public Server getServer() {
		return server;
	}

}
