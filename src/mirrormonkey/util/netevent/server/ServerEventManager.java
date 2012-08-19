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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import mirrormonkey.util.netevent.EventManager;
import mirrormonkey.util.netevent.message.MessageEventCreator;
import mirrormonkey.util.netevent.queue.EventQueue;
import mirrormonkey.util.netevent.queue.SimpleEventQueue;

import com.jme3.network.ConnectionListener;

/**
 * Stores information that will be used if MirrorMonkey is used together with
 * SpiderMonkey's <tt>Server</tt> and <tt>HostedConnection</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ServerEventManager extends EventManager {

	/**
	 * Contains all listeners that will be notified if a client connects or
	 * disconnects
	 */
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();

	/**
	 * On collecting <tt>ConnectionListeners</tt>, they will be added to this
	 * <tt>Collection</tt> and this <tt>Collection</tt> will be returned by the
	 * respective methods.
	 */
	private final Collection<ConnectionListener> iteratedConnectionListeners = new LinkedList<ConnectionListener>();

	/**
	 * <tt>ConnectionListener</tt> that relays events to this
	 * <tt>ServerEventManager</tt>
	 */
	private final ConnectionEventCreator connectionListener;

	/**
	 * Creates a new <tt>ServerEventManager</tt> with default event storage and
	 * processing policy, message listener and connection listener.
	 */
	public ServerEventManager() {
		this(new SimpleEventQueue(), new MessageEventCreator(),
				new ConnectionEventCreator());
	}

	/**
	 * Creates a new <tt>ServerEventManager</tt> with specific event storage and
	 * processing policy, message listener and connection listener.
	 * 
	 * @param events
	 *            event storage and processing policy
	 * @param messageListener
	 *            the <tt>MessageEventCreator</tt> that should relay messages to
	 *            this <tt>ServerEventManager</tt>
	 * @param connectionListener
	 *            the <tt>ConnectionEventCreator</tt> that should relay
	 *            connection add and remove events to this
	 *            <tt>ServerEventManager</tt>
	 * 
	 */
	public ServerEventManager(EventQueue events,
			MessageEventCreator messageListener,
			ConnectionEventCreator connectionListener) {
		super(events, messageListener);

		connectionListener.setManager(this);
		this.connectionListener = connectionListener;
	}

	/**
	 * Returns the <tt>ConnectionEventCreator</tt> that intercepts
	 * connectionAdded and connectionRemoved calls from the server and relays
	 * corresponding events to this <tt>ServerEventManager</tt>.
	 * 
	 * @return listener that relays connection events to this
	 *         <tt>ServerEventManager</tt>
	 */
	public ConnectionEventCreator getConnectionListener() {
		return connectionListener;
	}

	/**
	 * Returns the <tt>ConnectionListener</tt>s that will be notified if a
	 * client connects or disconnects.
	 * 
	 * @return all listenes that will be notified if connection to a client is
	 *         established or lost
	 */
	public Collection<ConnectionListener> getConnectionListeners() {
		iteratedConnectionListeners.clear();
		iteratedConnectionListeners.addAll(connectionListeners);
		return iteratedConnectionListeners;
	}

	/**
	 * Adds a <tt>ConnectionListener</tt> that will be notified if a client
	 * connects or disconnects
	 * 
	 * @param listener
	 *            the listener that will be notified of arriving and departing
	 *            clients
	 */
	public void addConnectionListener(ConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/**
	 * Removes a <tt>ConnectionListener</tt>, which will no longer be notified
	 * if a client connects or disconnects.
	 * 
	 * @param listener
	 *            the listener that should no longer be notified of arriving and
	 *            departing clients
	 */
	public void removeConnectionListener(ConnectionListener listener) {
		connectionListeners.remove(listener);
	}

}
