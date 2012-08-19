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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import mirrormonkey.util.netevent.EventManager;
import mirrormonkey.util.netevent.message.MessageEventCreator;
import mirrormonkey.util.netevent.queue.EventQueue;
import mirrormonkey.util.netevent.queue.SimpleEventQueue;

import com.jme3.network.ClientStateListener;

/**
 * Stores information that will be used if MirrorMonkey is used together with
 * SpiderMonkey's <tt>Client</tt>.
 * 
 * In particular, this class provides methods to register
 * <tt>ClientStateListener</tt>s and process their events additionally to
 * <tt>MessageListener</tt>s.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientEventManager extends EventManager {

	/**
	 * Contains all listeners that will be notified if the client connects or
	 * disconnects
	 */
	private final Set<ClientStateListener> clientStateListeners = new HashSet<ClientStateListener>();

	/**
	 * On collecting <tt>ClientStateListeners</tt>, they will be added to this
	 * <tt>Collection</tt> and returned by the respective getStateListen
	 * methods.
	 */
	private final Collection<ClientStateListener> iteratedClientStateListeners = new LinkedList<ClientStateListener>();

	/**
	 * <tt>ClientStateListener</tt> that listens to events from the
	 * <tt>Client</tt> and relays the corresponding events to this
	 * <tt>ClientEventManager</tt>
	 */
	private final ClientStateEventCreator clientStateListener;

	/**
	 * Creates a new <tt>ClientEventManager</tt> with default event storage and
	 * processing policy.
	 */
	public ClientEventManager() {
		this(new SimpleEventQueue(), new MessageEventCreator(),
				new ClientStateEventCreator());
	}

	/**
	 * Creates a new <tt>ClientEventManager</tt> with specified event storage
	 * and processing policy.
	 * 
	 * @param events
	 *            event storage and processing policy
	 * @param messageListener
	 *            <tt>MessageEventCreator</tt> that will listen for messages and
	 *            supply this <tt>ClientEventManager</tt> with matching events
	 *            in a synchronous fashion
	 * @param clientStateListener
	 *            <tt>ClientStateEventCreator</tt> that will listen for client
	 *            state events and supply this <tt>ClientEventManager</tt> with
	 *            matching events in a synchronous fashion
	 */
	public ClientEventManager(EventQueue events,
			MessageEventCreator messageListener,
			ClientStateEventCreator clientStateListener) {
		super(events, messageListener);

		clientStateListener.setClientEventManager(this);
		this.clientStateListener = clientStateListener;
	}

	/**
	 * Returns the listener that intercepts client state events and relays them
	 * to this <tt>ClientEventManager</tt>.
	 * 
	 * @return listener that relays <tt>NetworkEvent</tt>s to this
	 *         <tt>ClientEventManager</tt> whenever the client connects to or
	 *         disconnects from a server
	 */
	public ClientStateEventCreator getClientStateListener() {
		return clientStateListener;
	}

	/**
	 * Returns the <tt>ClientStateListener</tt>s that will be notified if the
	 * state of the client changes.
	 * 
	 * @return all listeners that will be notified if connection to the server
	 *         is established or lost
	 */
	public Collection<ClientStateListener> getClientStateListeners() {
		iteratedClientStateListeners.clear();
		iteratedClientStateListeners.addAll(clientStateListeners);
		return iteratedClientStateListeners;
	}

	/**
	 * Adds a <tt>ClientStateListener</tt> that will be notified if connection
	 * to the server is established or lost.
	 * 
	 * @param listener
	 *            the listener that will be notified of client state changes
	 */
	public void addClientStateListener(ClientStateListener listener) {
		clientStateListeners.add(listener);
	}

	/**
	 * Removes a <tt>ClientStateListener</tt>, which will no longer be notified
	 * if connection to the server is established or lost.
	 * 
	 * @param listener
	 *            the listener that should no longer be notified of client state
	 *            changes
	 */
	public void removeClientStateListener(ClientStateListener listener) {
		clientStateListeners.remove(listener);
	}

}
