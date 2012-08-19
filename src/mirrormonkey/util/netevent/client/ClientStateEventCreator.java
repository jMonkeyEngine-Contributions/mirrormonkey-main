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

package mirrormonkey.util.netevent.client;

import mirrormonkey.util.netevent.NetworkEvent;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;

/**
 * Listens for <tt>Client</tt>s to establish or close connections to the server,
 * generates fitting events and relays the events to a
 * <tt>ClientEventManager</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientStateEventCreator implements ClientStateListener {

	/**
	 * relay client state events to this manager
	 */
	protected ClientEventManager manager;

	/**
	 * Sets the <tt>ClientEventManager</tt> that this
	 * <tt>ClientStateEventCreator</tt> should relay client state events to.
	 * 
	 * @param manager
	 *            the manager that should receive inbound client state events
	 */
	public void setClientEventManager(ClientEventManager manager) {
		if (this.manager != null) {
			throw new IllegalStateException();
		}
		this.manager = manager;
	}

	public void clientConnected(Client client) {
		manager.atomicAddEvent(new ClientConnectedEvent(client));
	}

	public void clientDisconnected(Client client, DisconnectInfo info) {
		manager.atomicAddEvent(new ClientDisconnectedEvent(client, info));
	}

	/**
	 * Encapsulates notifying all <tt>ClientStateListener</tt>s after a
	 * connection to the server has been established.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class ClientConnectedEvent implements NetworkEvent {

		/**
		 * client that established the connection to the server
		 */
		private final Client client;

		/**
		 * Creates a new <tt>ClientConnectedEvent</tt> for a specified
		 * <tt>client</tt>.
		 * 
		 * @param client
		 *            the client that has successfully connected to a server
		 */
		public ClientConnectedEvent(Client client) {
			this.client = client;
		}

		public final void process() {
			for (ClientStateListener l : manager.getClientStateListeners()) {
				try {
					l.clientConnected(client);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Encapsulates notifying all <tt>ClientStateListener</tt>s after a
	 * connection to a server has been closed.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class ClientDisconnectedEvent implements NetworkEvent {

		/**
		 * client that closed its connection to the server
		 */
		private final Client client;

		/**
		 * disconnect info as passed to <tt>clientDisconnected</tt>
		 */
		private final DisconnectInfo info;

		/**
		 * Creates a new <tt>ClientDisconnectedEvent</tt> for a specific client,
		 * providing specific info about why the connection was closed.
		 * 
		 * @param client
		 *            the client that has been disconnected from its server
		 * @param info
		 *            an object containing information as to why the client has
		 *            been disconnected
		 */
		public ClientDisconnectedEvent(Client client, DisconnectInfo info) {
			this.client = client;
			this.info = info;
		}

		public final void process() {
			for (ClientStateListener l : manager.getClientStateListeners()) {
				try {
					l.clientDisconnected(client, info);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
