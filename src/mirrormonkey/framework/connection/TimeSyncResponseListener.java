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

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.connection.messages.TimeSyncResponseMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Waits for incoming <tt>TimeSyncResponseMessages</tt>. Estimates travel time
 * of messages and time of remote <tt>SyncAppState</tt> and writes it to the
 * <tt>ConnectionInfo</tt> concerned.
 * 
 * The time synchronization algorithm is implemented in this class and
 * <tt>TimeSync</tt>.
 * 
 * @author Philipp Christian Lowener
 * 
 */
public class TimeSyncResponseListener implements
		MessageListener<MessageConnection> {

	/**
	 * Responsible for keeping data about connections.
	 */
	private final CoreModule<?, ?> coreModule;

	/**
	 * Creates a new <tt>TimeSyncResponseListener</tt> that can listen to
	 * <tt>TimeSyncResponseMessages</tt> from connections registered to a given
	 * <tt>CoreModule</tt>.
	 * 
	 * @param coreModule
	 *            the <tt>CoreModule</tt> responsible for keeping data about
	 *            connections
	 */
	public TimeSyncResponseListener(CoreModule<?, ?> coreModule) {
		this.coreModule = coreModule;
	}

	public void messageReceived(MessageConnection source, Message m) {
		TimeSyncResponseMessage r = (TimeSyncResponseMessage) m;
		ConnectionInfo<?> info = null;
		if ((info = coreModule.getData(source)) != null) {
			TimeSync ts = info.sync;

			if (r.id != ts.currentId) {
				return;
			}
			ts.received = true;
			long travelTime = System.nanoTime() - ts.sentLocalTime;
			info.estimatedLatency = travelTime / 2;
			info.estimatedRemoteTime += (r.localTime - info.estimatedRemoteTime);
			info.estimatedRemoteTime += info.estimatedLatency;
			info.estimatedArrivalTime = info.estimatedRemoteTime
					+ info.estimatedLatency;
		}
	}
}
