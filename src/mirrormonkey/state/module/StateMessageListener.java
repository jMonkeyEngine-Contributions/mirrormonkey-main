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

package mirrormonkey.state.module;

import java.util.logging.Logger;

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.state.InboundAwareEntity;
import mirrormonkey.state.annotations.UpdateSetId;
import mirrormonkey.state.member.DynamicUpdateData;
import mirrormonkey.state.messages.UpdateMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens for incoming <tt>UpdateMessages</tt>, decides if they are valid and
 * performs the updates that they contain if they are.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StateMessageListener implements MessageListener<MessageConnection> {

	/**
	 * Warning messages go here.
	 */
	private static final Logger LOGGER = Logger
			.getLogger("StateMessageListener");

	/**
	 * Manages synchronization.
	 */
	private final SyncAppState<?> appState;

	/**
	 * Manages information about visible entities.
	 */
	private final CoreModule<?, ?> coreModule;

	/**
	 * Creates a new <tt>StateMessageListener</tt>.
	 * 
	 * @param appState
	 *            local <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 */
	public StateMessageListener(SyncAppState<?> appState) {
		this.appState = appState;
		coreModule = appState.getModule(CoreModule.class);
	}

	public void messageReceived(MessageConnection source, Message m) {
		UpdateMessage message = (UpdateMessage) m;
		ConnectionInfo<?> connectionInfo = coreModule.getData(source);
		DynamicEntityData data = coreModule.getEntityProvider().getData(
				message.entityId);
		if (data == null) {
			LOGGER.info("Received update for entity " + message.entityId
					+ " which is no longer registered.");
			return;
		}
		StaticEntityData sed = data.getActiveStaticData(source);
		if (sed == null) {
			LOGGER.info("Received update for entity " + message.entityId
					+ " which is no longer visible in that static context.");
		}

		DynamicMemberData[] dmd = data.getMemberData(sed);
		InboundAwareEntity iae = null;
		long lastTimestamp = 0;
		if (message.setId != UpdateSetId.NO_SET
				&& InboundAwareEntity.class.isInstance(data.getLocalInstance())
				&& message.fieldIds.length > 0) {
			lastTimestamp = ((DynamicUpdateData) dmd[message.fieldIds[0]])
					.getLastTime();
			if (message.localTime < lastTimestamp) {
				return;
			}
			iae = (InboundAwareEntity) data.getLocalInstance();
			iae.beforeInbound(appState, connectionInfo, message.setId,
					sed.getConnectedClass(), m.isReliable(), lastTimestamp,
					message.localTime);
		}
		for (int i = 0; i < message.fieldIds.length; i++) {
			int fieldId = message.fieldIds[i];
			((DynamicUpdateData) dmd[fieldId]).setFromRemote(
					message.fieldValues[i], message.localTime);
		}
		if (iae != null) {
			iae.afterInbound(appState, connectionInfo, message.setId,
					sed.getConnectedClass(), m.isReliable(), lastTimestamp,
					message.localTime);
		}
	}
}
