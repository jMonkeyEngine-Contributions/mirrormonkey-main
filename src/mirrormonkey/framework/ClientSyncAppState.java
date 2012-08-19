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

import mirrormonkey.util.netevent.client.ClientEventManager;

import com.jme3.network.Client;

/**
 * Responsible for managing modules, message listeners and client state
 * listeners on the client side.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientSyncAppState extends SyncAppState<ClientEventManager> {

	/**
	 * The <tt>Client</tt> that is connected to a server using MirrorMonkey.
	 */
	private final Client client;

	/**
	 * Creates a new <tt>ClientSyncAppState</tt> using the default
	 * <tt>ClientEventManager</tt> and a given <tt>Client</tt>.
	 * 
	 * @param client
	 *            the <tt>Client</tt> that is connected to a server using
	 *            MirrorMonkey
	 */
	public ClientSyncAppState(Client client) {
		this(new ClientEventManager(), client);
	}

	/**
	 * Creates a new <tt>ClientSyncAppState</tt> using a given network event
	 * manager and client.
	 * 
	 * @param eventManager
	 *            the <tt>ClientEventManager</tt> that will be used to dispatch
	 *            <tt>Messages</tt> and client state events
	 * @param client
	 *            the <tt>Client</tt> that is connected to a server using
	 *            MirrorMonkey
	 */
	public ClientSyncAppState(ClientEventManager eventManager, Client client) {
		super(eventManager);
		client.addMessageListener(eventManager.getMessageListener());
		client.addClientStateListener(eventManager.getClientStateListener());
		this.client = client;
	}

	/**
	 * @return the <tt>Client</tt> that is connected to a server using
	 *         MirrorMonkey
	 */
	public Client getClient() {
		return client;
	}
}
