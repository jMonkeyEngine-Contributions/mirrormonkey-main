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

package mirrormonkey.util.netevent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Semaphore;

import mirrormonkey.util.netevent.message.MessageEventCreator;
import mirrormonkey.util.netevent.queue.EventQueue;
import mirrormonkey.util.netevent.queue.SimpleEventQueue;

import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Centralized data storage class which glues together the parts of this
 * package.
 * 
 * In order to use this class, message listeners have to be added to an instance
 * of this class instead of the connections they want to listen to. Then, this
 * instance has to be added to one or more listeners specified for the requested
 * event types. The listeners in turn have to be added to the connections that
 * the user initially wanted to listen to.
 * 
 * There are multiple flavors available for listeners, some of them filtering
 * incoming events after certain criteria. They are all located in
 * <tt>mirrormonkey.util.netevent</tt> and subpackages, and are named
 * *EventCreator.
 * 
 * There are also multiple flavors for event storage and processing which can be
 * changed by passing an <tt>EventQueue</tt> to the constructor.
 * 
 * Please note that in general, the listeners that will be notified if a
 * specific event occurs only reflects the listener collections at the point of
 * time that the message is processed, not at the point of time that the message
 * was actually received.
 * 
 * Please note that users must consume the collections of listeners returned by
 * <tt>getGenericMessageListeners</tt> and <tt>getListenersFor</tt> before
 * calling those methods or refrain from further iterating those collections, as
 * no new collections will be created for each call.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class EventManager {

	/**
	 * If there are no message listeners listening to a particular message
	 * class, we want to return an empty collection instead of null.
	 */
	private static final Collection<MessageListener<MessageConnection>> EMPTY_COLLECTION = Collections
			.unmodifiableCollection(new LinkedList<MessageListener<MessageConnection>>());

	/**
	 * Listeners that will be notified of messages of any messages, regardless
	 * of their class
	 */
	private final Set<MessageListener<MessageConnection>> genericMessageListeners = new HashSet<MessageListener<MessageConnection>>();

	/**
	 * Listeners that are only notified of certain message classes
	 */
	private final HashMap<Class<?>, Set<MessageListener<MessageConnection>>> specificMessageListeners = new HashMap<Class<?>, Set<MessageListener<MessageConnection>>>();

	/**
	 * On collecting <tt>MessageListeners</tt>, they will be added to this
	 * <tt>Collection</tt> and it will be returned by the respective methods.
	 */
	private final Collection<MessageListener<MessageConnection>> iteratedMessageListeners = new LinkedList<MessageListener<MessageConnection>>();

	/**
	 * Stores events and processes them; this class delegates calls after making
	 * them thread-safe
	 */
	private final EventQueue events;

	/**
	 * Synchronizes calls that depend on the current event queue
	 */
	private final Semaphore eventState = new Semaphore(1);

	/**
	 * Listens to inbound messages and relays events encapsulating the messages
	 * to this class
	 */
	private final MessageEventCreator messageListener;

	/**
	 * Creates a new <tt>EventManager</tt> with default event storage and
	 * processing policy and message listener.
	 */
	public EventManager() {
		this(new SimpleEventQueue(), new MessageEventCreator());
	}

	/**
	 * Creates a new <tt>EventManager</tt> with custom event storage and
	 * processing policy and message listener
	 * 
	 * @param events
	 *            event storage and processing policy
	 * @param messageListener
	 *            <tt>MessageEventCreator</tt> that listens for inbound messages
	 *            and relays them to this <tt>EventManager</tt>
	 */
	public EventManager(EventQueue events, MessageEventCreator messageListener) {
		this.events = events;

		messageListener.setManager(this);
		this.messageListener = messageListener;
	}

	/**
	 * Returns the listener that relays messages to this <tt>EventManager</tt>.
	 * 
	 * @return the listener that intercepts messages and relays them to this
	 *         <tt>EventManager</tt>
	 */
	public MessageEventCreator getMessageListener() {
		return messageListener;
	}

	/**
	 * Locks the event queue calls it to start processing inbound events.
	 */
	public void processEvents() {
		lock();
		events.processEvents();
		release();
	}

	/**
	 * Adds an event to the inbound event queue without locking it before. This
	 * method should be called exactly if the caller accessed the event queue
	 * before and secured its call with <tt>lock</tt>
	 * 
	 * @param event
	 *            the event to be added to the inbound queue
	 */
	public void addEvent(NetworkEvent event) {
		events.add(event);
	}

	/**
	 * Locks the inbound event queue and adds an event to it.
	 * 
	 * @param event
	 *            the event that should be added to the inbound queue
	 */
	public void atomicAddEvent(NetworkEvent event) {
		lock();
		events.add(event);
		release();
	}

	/**
	 * Locks the inbound event queue for thread-safe access from both main
	 * update and any other thread. The queue can be unlocked by calling
	 * <tt>release</tt>.
	 */
	public void lock() {
		eventState.acquireUninterruptibly();
	}

	/**
	 * Unlocks the inbound event queue after a call to <tt>lock</tt>.
	 */
	public void release() {
		eventState.release();
	}

	/**
	 * Returns a <tt>Collection</tt> of all <tt>MessageListeners</tt> that will
	 * be notified of every inbound messages regardless of its class.
	 * 
	 * @return all <tt>MessageListeners</tt> that will be notified of every
	 *         incoming message
	 */
	public Collection<MessageListener<MessageConnection>> getGenericMessageListeners() {
		iteratedMessageListeners.clear();
		iteratedMessageListeners.addAll(genericMessageListeners);
		return iteratedMessageListeners;
	}

	/**
	 * Adds a <tt>MessageListener</tt> that will be notified for every inbound
	 * message regardless its class or does nothing if that listener is already
	 * added.
	 * 
	 * @param listener
	 *            the listener that will be notified in the future
	 */
	public void addMessageListener(MessageListener<MessageConnection> listener) {
		genericMessageListeners.add(listener);
	}

	/**
	 * Removes a <tt>MessageListener</tt>. The <tt>MessageListener</tt> will no
	 * longer be notified of any incoming message.
	 * 
	 * @param listener
	 *            the listener that should no longer be notified
	 */
	public void removeMessageListener(
			MessageListener<MessageConnection> listener) {
		genericMessageListeners.remove(listener);
	}

	/**
	 * Gets every listener that has been registered for a specific message
	 * class. This does not include listeners that are generally registered for
	 * every class using <tt>addMessageListener(MessageListener)</tt>.
	 * 
	 * @param messageClass
	 *            message class that the listeners are registered for
	 * @return a <tt>Collection</tt> of all <tt>MessageListener</tt>s that are
	 *         currently registered for <tt>messageClass</tt>
	 */
	public Collection<MessageListener<MessageConnection>> getListenersFor(
			Class<?> messageClass) {
		Collection<MessageListener<MessageConnection>> listeners = specificMessageListeners
				.get(messageClass);
		if (listeners != null) {
			iteratedMessageListeners.clear();
			iteratedMessageListeners.addAll(listeners);
			return iteratedMessageListeners;
		}
		return EMPTY_COLLECTION;
	}

	/**
	 * Adds a <tt>MessageListener</tt> that will only be notified if a message
	 * arrives that is instance of a specific message class.
	 * 
	 * Please note that while it is possible to call this method multiple times
	 * for a specific listener and class without the listener being called
	 * multiple times for that class, it is not possible to mix up
	 * class-specific and general listening for a single listener instance.
	 * 
	 * @param listener
	 *            the listener that will be notified of the specified message
	 *            classes in the future
	 * @param listenTo
	 *            array of message classes that the listener will be notified
	 *            for
	 */
	public void addMessageListener(MessageListener<MessageConnection> listener,
			Class<?>... listenTo) {
		for (Class<?> c : listenTo) {
			Set<MessageListener<MessageConnection>> cc = specificMessageListeners
					.get(c);
			if (cc == null) {
				cc = new HashSet<MessageListener<MessageConnection>>();
				specificMessageListeners.put(c, cc);
			}
			cc.add(listener);
		}
	}

	/**
	 * Please note that while it is possible to remove a listener only for some
	 * of the classes it is registered for, it is not possible to mix this
	 * method with <tt>addMessageListener(MessageListener)</tt>.
	 * 
	 * @param listener
	 *            the listener that should no longer be notified of the
	 *            specified message classes
	 * @param removeFrom
	 *            the message classes that <tt>listener</tt> should no longer be
	 *            notified for
	 */
	public void removeMessageListener(
			MessageListener<MessageConnection> listener, Class<?>... removeFrom) {
		for (Class<?> c : removeFrom) {
			Collection<MessageListener<MessageConnection>> listeners = specificMessageListeners
					.get(c);
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty()) {
					specificMessageListeners.remove(c);
				}
			}
		}
	}

	/**
	 * Returns the number of message classes that have at least one
	 * <tt>MessageListener</tt> registered for them.
	 * 
	 * @return the number of message classes that are currently listened to
	 */
	public int getMessageClassCount() {
		return specificMessageListeners.size();
	}

}
