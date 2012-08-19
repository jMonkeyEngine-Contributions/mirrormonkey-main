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
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.messages.TimeSyncRequestMessage;
import mirrormonkey.framework.connection.messages.TimeSyncResponseMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Waits for <tt>TimeSyncRequestMessages</tt> received from connections using
 * MirrorMonkey. For every incoming message, a <tt>TimeSyncResponseMessage</tt>
 * will be sent to the source connection.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class TimeSyncRequestListener implements
		MessageListener<MessageConnection> {

	/**
	 * Local <tt>SyncAppState</tt> that is responsible for keeping the local
	 * time.
	 */
	private final SyncAppState<?> appState;

	/**
	 * Core module responsible for keeping <tt>ConnectionInfo</tt> instances for
	 * source connections.
	 */
	private final CoreModule<?, ?> module;

	/**
	 * Creates a new <tt>TimeSyncRequestListener</tt> that allows connections to
	 * synchronize time with the given <tt>SyncAppState</tt>.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> that connections should synchronize
	 *            time with
	 * @param module
	 *            the <tt>CoreModule</tt> responsible for keeping data about
	 *            possible source connections
	 */
	public TimeSyncRequestListener(SyncAppState<?> appState,
			CoreModule<?, ?> module) {
		this.appState = appState;
		this.module = module;
	}

	public void messageReceived(MessageConnection source, Message m) {
		TimeSyncRequestMessage r = (TimeSyncRequestMessage) m;
		module.getData(source).send(
				new TimeSyncResponseMessage(r.id, appState.getSyncTime()));
	}

}
