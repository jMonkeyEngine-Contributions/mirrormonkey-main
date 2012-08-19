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

package mirrormonkey.framework.connection;

import java.util.Collection;

import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.util.listeners.ListenerConfiguration;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.system.NanoTimer;

/**
 * Contains information about a connection, the listeners registered to it and
 * tries to synchronize the time with the connected client or server.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <A>
 *            Class of the <tt>SyncAppState</tt> controlling MirrorMonkey
 * 
 */
public class ConnectionInfo<A extends SyncAppState<?>> {

	/**
	 * Keeps track of the registered listeners.
	 */
	private final ListenerConfiguration listenerConfiguration;

	/**
	 * The <tt>SyncAppState</tt> controlling MirrorMonkey.
	 */
	protected final A appState;

	/**
	 * Underlying connection.
	 */
	protected final MessageConnection connection;

	/**
	 * Is set to <tt>true</tt> as long as this <tt>ConnectionInfo</tt> is
	 * registered in <tt>appState</tt>.
	 */
	protected boolean registered;

	/**
	 * Is set to <tt>true</tt> exactly if the underlying <tt>connection</tt> is
	 * running. While this is <tt>false</tt>, messages passed to <tt>send</tt>
	 * will not be enqueued for sending.
	 */
	protected boolean running;

	/**
	 * Responsible to handle synchronizing the time over network.
	 */
	protected final TimeSync sync;

	/**
	 * Time on the remote <tt>SyncAppState</tt>, as estimated by <tt>sync</tt>.
	 */
	protected long estimatedRemoteTime;

	/**
	 * Time needed for a message sent to the connected <tt>SyncAppState</tt> to
	 * reach its destination, as estimated by <tt>sync</tt>.
	 */
	protected long estimatedLatency;

	/**
	 * Time on the remote <tt>SyncAppState</tt> on arrival of a message sent
	 * right now, as estimated by <tt>sync</tt>.
	 */
	protected long estimatedArrivalTime;

	/**
	 * Calculates a more precise time per frame value than the <tt>float</tt>
	 * available when updating. Used to update <tt>estimatedRemoteTime</tt>.
	 */
	private final NanoTimer t;

	/**
	 * Creates a new <tt>ConnectionInfo</tt> that will be registered to the
	 * given <tt>SyncAppState</tt> and will use the given
	 * <tt>MessageConnection</tt> to transmit messages.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> that controls MirrorMonkey
	 * @param connection
	 *            the underlying <tt>MessageConnection</tt>
	 * @param registered
	 *            initial value indicating whether this <tt>ConnectionInfo</tt>
	 *            is registered from the start
	 */
	public ConnectionInfo(A appState, MessageConnection connection,
			boolean registered) {
		if (connection == null) {
			throw new NullPointerException();
		}
		sync = new TimeSync(this);
		listenerConfiguration = new ListenerConfiguration();
		this.appState = appState;
		this.connection = connection;
		this.registered = registered;
		t = new NanoTimer();
		running = false;
	}

	/**
	 * Adds an <tt>InstanceLifecycleListener</tt> that will be notified whenever
	 * the top of the mapping stack for any entity visible to the underlying
	 * connection changes.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> to be notified from now
	 *            on
	 */
	public void addInstanceLifecycleListener(InstanceLifecycleListener listener) {
		listenerConfiguration.addListener(listener);
	}

	/**
	 * Removes an <tt>InstanceLifecycleListener</tt> that will no longer be
	 * notified whenever the top of the mapping stack for any entity visible to
	 * the underlying connection changes.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> that will no longer be
	 *            notified
	 */
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener) {
		listenerConfiguration.removeListener(listener);
	}

	/**
	 * Adds all added <tt>InstanceLifecycleListeners</tt> to a
	 * <tt>Collection</tt>. Called when listeners are collected for a lifecycle
	 * event on the underlying connection.
	 * 
	 * @param l
	 *            the <tt>Collection</tt> to add all registered listeners to
	 */
	public void collectInstanceLifecycleListeners(
			Collection<InstanceLifecycleListener> l) {
		listenerConfiguration.getListeners(InstanceLifecycleListener.class, l);
	}

	/**
	 * @return the underlying connection
	 */
	public MessageConnection getConnection() {
		return connection;
	}

	/**
	 * @return Estimated value that the connected <tt>SyncAppState</tt> would
	 *         return for its <tt>getSyncTime</tt> method if it were called
	 *         right now. Time is measured in nanoseconds.
	 */
	public long getEstimatedRemoteTime() {
		return estimatedRemoteTime;
	}

	/**
	 * @return Estimated time that an (unreliable) message sent right now would
	 *         need to reach the connected <tt>SyncAppState</tt>. Time is
	 *         measured in nanoseconds.
	 */
	public long getEstimatedLatency() {
		return estimatedLatency;
	}

	/**
	 * @return Estimated value that the connected <tt>SyncAppState</tt> would
	 *         return for its <tt>getSyncTime</tt> method at the point of time
	 *         that an (unreliable) message sent right now would reach it. Time
	 *         is measured in nanoseconds.
	 */
	public long getEstimatedArrivalTime() {
		return estimatedArrivalTime;
	}

	/**
	 * @return <tt>true</tt> if this <tt>ConnectionInfo</tt> is currently
	 *         registered in the <tt>SyncAppState</tt>, false otherwise
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * @return <tt>true</tt> if the underlying connection of this
	 *         <tt>ConnectionInfo</tt> is currently running, false otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sends the given <tt>Message</tt> over the network if the underlying
	 * connection is running. Does nothing otherwise.
	 * 
	 * @param message
	 *            the <tt>Message</tt> to enqueue in the connection for sending
	 */
	public void send(Message message) {
		if (!running) {
			return;
		}
		connection.send(message);
	}

	/**
	 * Updates <tt>estimatedRemoteTime</tt> and handles timeout or sending of
	 * time sync messages.
	 */
	public void update() {
		t.update();
		estimatedRemoteTime += t.getTime();
		t.reset();
		sync.update();
	}

	@Override
	public String toString() {
		return "[ConnectionInfo connection=" + connection + ", registered="
				+ registered + ", running=" + running + "]";
	}

	/**
	 * @return the <tt>ListenerConfiguration</tt> responsible for storing added
	 *         <tt>InstanceLifecycleListeners</tt>
	 * @deprecated Internal use only. Please use
	 *             <tt>addInstanceLifecycleListener</tt> and
	 *             <tt>removeInstanceLifecycleListener</tt> instead.
	 */
	@Deprecated()
	protected ListenerConfiguration getListenerConfiguration() {
		return listenerConfiguration;
	}
}
