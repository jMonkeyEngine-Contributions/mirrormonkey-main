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

package mirrormonkey.core.client;

import mirrormonkey.core.InstanceInitializedEvent;
import mirrormonkey.core.member.ConstructorData;
import mirrormonkey.core.messages.EntityInitMessage;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens to <tt>EntityInitMessages</tt> from the connected server, initializes
 * entities accordingly and notifies <tt>InstanceLifecycleListeners</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class EntityInitListener implements MessageListener<MessageConnection> {

	/**
	 * Contains information about entities and connections.
	 */
	private final ClientCoreModule module;

	/**
	 * Creates a new <tt>EntityInitListener</tt> that will update entity data
	 * stored in a given core module.
	 * 
	 * @param module
	 *            the <tt>CoreModule</tt> for which data should be updated
	 *            according to received <tt>EntityInitMessages</tt>
	 */
	public EntityInitListener(ClientCoreModule module) {
		this.module = module;
	}

	public void messageReceived(MessageConnection source, Message message) {
		try {
			EntityInitMessage castMessage = (EntityInitMessage) message;
			@SuppressWarnings("unchecked")
			Class<? extends SyncEntity> entityClass = (Class<? extends SyncEntity>) Class
					.forName(castMessage.className);
			@SuppressWarnings("unchecked")
			Class<? extends SyncEntity> connectedClass = (Class<? extends SyncEntity>) Class
					.forName(castMessage.connectedClassName);
			StaticEntityData staticData = module.getEntityProvider()
					.getStaticData(entityClass, connectedClass);

			ConstructorData constr = staticData.getData(castMessage.constrId,
					ConstructorData.class);
			SyncEntity reference = constr.newEntity(castMessage.packedParams);

			ClientEntityData data = module.getData(castMessage.entityId);
			data.referenceArrived(reference, staticData);
			if (!module.getData(source).isRunning()) {
				module.getData(source).setRunning(true);
			}

			module.notifyInitListeners(new InstanceInitializedEvent(data
					.getLocalInstance(), data,
					data.getActiveStaticData(source), module.getData(source)));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
