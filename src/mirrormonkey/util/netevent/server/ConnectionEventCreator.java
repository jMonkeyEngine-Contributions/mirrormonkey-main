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

package mirrormonkey.util.netevent.server;

import mirrormonkey.util.netevent.NetworkEvent;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;

/**
 * Listens for <tt>Server</tt>s for <tt>HostedConnection</tt>s that are
 * established or closed, generates fitting events for them and relays the
 * events to a <tt>ServerEventManager</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ConnectionEventCreator implements ConnectionListener {

	/**
	 * relay connection events to this manager
	 */
	protected ServerEventManager manager;

	/**
	 * Set the <tt>ServerEventManager</tt> to relay connection add and remove
	 * events to.
	 * 
	 * @param manager
	 *            the <tt>ServerEventManager</tt> to relay connection events to
	 */
	public void setManager(ServerEventManager manager) {
		if (this.manager != null) {
			throw new IllegalStateException();
		}
		this.manager = manager;
	}

	public void connectionAdded(Server server, HostedConnection conn) {
		manager.atomicAddEvent(new ConnectionAddedEvent(server, conn));
	}

	public void connectionRemoved(Server server, HostedConnection conn) {
		manager.atomicAddEvent(new ConnectionRemovedEvent(server, conn));
	}

	/**
	 * Encapsulates notifying all <tt>ConnectionListener</tt>s registered to
	 * <tt>manager</tt> that a connection was added.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class ConnectionAddedEvent implements NetworkEvent {

		/**
		 * server to which the connection was added
		 */
		private final Server server;

		/**
		 * connection that was added
		 */
		private final HostedConnection connection;

		/**
		 * Creates a new <tt>ConnectionAddedEvent</tt> for a specific
		 * <tt>Server</tt> and <tt>HostedConnection</tt>.
		 * 
		 * @param server
		 *            the server that the connection was added to
		 * @param connection
		 *            the connection that has been established
		 */
		public ConnectionAddedEvent(Server server, HostedConnection connection) {
			this.server = server;
			this.connection = connection;
		}

		public final void process() {
			for (ConnectionListener l : manager.getConnectionListeners()) {
				try {
					l.connectionAdded(server, connection);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Encapsulates notifying all <tt>ConnectionListener</tt>s registered to
	 * <tt>manager</tt> that a connection was closed.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class ConnectionRemovedEvent implements NetworkEvent {

		/**
		 * server that the closed connection was connected to
		 */
		public final Server server;

		/**
		 * connection that was closed
		 */
		public final HostedConnection connection;

		/**
		 * Creates a new <tt>ConnectionRemovedEvent</tt> for a specific
		 * <tt>Server</tt> and <tt>HostedConnection</tt>.
		 * 
		 * @param server
		 *            the server that contained the closed connection
		 * @param connection
		 *            the connection that was closed
		 */
		public ConnectionRemovedEvent(Server server, HostedConnection connection) {
			this.server = server;
			this.connection = connection;
		}

		public final void process() {
			for (ConnectionListener l : manager.getConnectionListeners()) {
				try {
					l.connectionRemoved(server, connection);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}
