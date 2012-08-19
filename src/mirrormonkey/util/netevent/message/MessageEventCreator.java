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

package mirrormonkey.util.netevent.message;

import mirrormonkey.util.netevent.EventManager;
import mirrormonkey.util.netevent.NetworkEvent;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens for <tt>Messages</tt> arriving on a connection, generates fitting
 * events and relays the events to a <tt>EventManager</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class MessageEventCreator implements MessageListener<MessageConnection> {

	/**
	 * relay message events to this manager
	 */
	protected EventManager manager;

	/**
	 * Sets the <tt>EventManager</tt> that this <tt>MessageEventCreator</tt>
	 * should relay message events to.
	 * 
	 * @param manager
	 *            the manager that should receive inbound message events
	 */
	public void setManager(EventManager manager) {
		if (this.manager != null) {
			throw new IllegalStateException();
		}
		this.manager = manager;
	}

	public void messageReceived(MessageConnection source, Message m) {
		manager.atomicAddEvent(new MessageEvent(source, m));
	}

	/**
	 * Encapsulates notifying all <tt>MessageListener</tt>s after a message has
	 * been received.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public final class MessageEvent implements NetworkEvent {

		/**
		 * connection that sent the message
		 */
		private final MessageConnection connection;

		/**
		 * message that was received
		 */
		private final Message message;

		/**
		 * Creates a new <tt>MessageEvent</tt> for an inbound <tt>Message</tt>.
		 * 
		 * @param connection
		 *            the connection that sent the message
		 * @param message
		 *            the message that was received
		 */
		public MessageEvent(MessageConnection connection, Message message) {
			this.connection = connection;
			this.message = message;
		}

		public final void process() {
			for (MessageListener<MessageConnection> l : manager
					.getGenericMessageListeners()) {
				notifyListener(l);
			}
			for (MessageListener<MessageConnection> l : manager
					.getListenersFor(message.getClass())) {
				notifyListener(l);
			}
		}

		/**
		 * Notifies a listener that a message has been received and catches all
		 * exceptions it may generate.
		 * 
		 * @param l
		 *            the listener that should be notified
		 */
		private final void notifyListener(MessageListener<MessageConnection> l) {
			try {
				l.messageReceived(connection, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
