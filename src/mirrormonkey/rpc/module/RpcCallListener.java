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

package mirrormonkey.rpc.module;

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.rpc.member.RpcMethodData;
import mirrormonkey.rpc.messages.RpcCallMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens to inbound <tt>RpcCallMessages</tt>, invokes the requests that they
 * contain and, if applicable, responds with an according
 * <tt>RpcResponseMessage</tt> or <tt>RpcErrorMessage</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcCallListener implements MessageListener<MessageConnection> {

	/**
	 * Local <tt>SyncAppState</tt> responsible for managing synchronization.
	 */
	private final SyncAppState<?> appState;

	/**
	 * The <tt>EntityProvider</tt> storing information about locally known
	 * entities.
	 */
	private final EntityProvider<?> entityProvider;

	/**
	 * Creates a new <tt>RpcCallListener</tt> that will invoke incoming
	 * requests.
	 * 
	 * @param appState
	 *            local <tt>SyncAppState</tt>
	 */
	public RpcCallListener(SyncAppState<?> appState) {
		this.appState = appState;
		this.entityProvider = appState.getModule(CoreModule.class)
				.getEntityProvider();
	}

	public void messageReceived(MessageConnection source, Message message) {
		RpcCallMessage castMessage = (RpcCallMessage) message;
		DynamicEntityData data = entityProvider.getData(castMessage.entityId);
		Message response;
		if ((response = data.getActiveStaticData(source)
				.getData(castMessage.methodId, RpcMethodData.class)
				.executeFromRemote(source, data, castMessage, appState)) != null) {
			source.send(response);
		}
	}

}
