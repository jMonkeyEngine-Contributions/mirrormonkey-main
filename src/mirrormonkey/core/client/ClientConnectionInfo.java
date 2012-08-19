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

import mirrormonkey.framework.ClientSyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageConnection;

/**
 * Client-side implementation of <tt>ConnectionInfo</tt>.
 * 
 * Listens to <tt>ClientStateEvents</tt> to unregister itself once the
 * connection is closed.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientConnectionInfo extends ConnectionInfo<ClientSyncAppState>
		implements ClientStateListener {

	/**
	 * Creates a new <tt>ClientConnectionInfo</tt> for a given
	 * <tt>ClientSyncAppState</tt> and <tt>MessageConnection</tt>.
	 * 
	 * @param appState
	 *            the local <tt>SyncAppState</tt>
	 * @param connection
	 *            the client
	 */
	public ClientConnectionInfo(ClientSyncAppState appState,
			MessageConnection connection) {
		super(appState, connection, true);
		appState.getEventManager().addClientStateListener(this);
	}

	public void clientConnected(Client c) {
		// setRunning(true);
	}

	public void clientDisconnected(Client c, DisconnectInfo info) {
		setRunning(false);
	}

	@Override
	public void send(Message m) {
		if (((Client) getConnection()).isConnected()) {
			super.send(m);
		}
	}

	/**
	 * Called whenever the underlying client changes state
	 * 
	 * @param b
	 *            <tt>true</tt> if the underlying client startet up,
	 *            <tt>false</tt> if it stopped
	 */
	public void setRunning(boolean b) {
		running = b;
	}

}
