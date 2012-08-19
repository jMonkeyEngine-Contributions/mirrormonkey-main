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

import mirrormonkey.core.messages.EntityEndMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens to <tt>EntityEndMessages</tt> from the connected server, destroys
 * entities accordingly and notifies <tt>InstanceLifecycleListeners</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class EntityEndListener implements MessageListener<MessageConnection> {

	/**
	 * Contains informaition about entities and connections.
	 */
	private final ClientCoreModule module;

	/**
	 * Creates a new <tt>EntityEndListener</tt> that will update entity data
	 * stored in a given core module.
	 * 
	 * @param module
	 *            the <tt>CoreModule</tt> for which data should be updated
	 *            according to received <tt>EntityEndMessages</tt>
	 */
	public EntityEndListener(ClientCoreModule module) {
		this.module = module;
	}

	public void messageReceived(MessageConnection source, Message message) {
		EntityEndMessage castMessage = (EntityEndMessage) message;
		ClientEntityData data = module.getData(castMessage.entityId);
		data.referenceDeparted();
		if (module.getData(source).isRunning()
				&& !module.getEntityProvider().hasEntities()) {
			module.getData(source).setRunning(false);
		}
	}

}
