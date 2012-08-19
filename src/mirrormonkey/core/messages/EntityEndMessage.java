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

package mirrormonkey.core.messages;

import mirrormonkey.framework.entity.DynamicEntityData;

import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 * This message is sent from server to client whenever an entity becomes
 * invisible to that client and the local instance should be destroyed.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class EntityEndMessage implements Message {

	/**
	 * The ID of the entity.
	 */
	public int entityId;

	/**
	 * Empty constructor for serializing.
	 * 
	 * @deprecated Only SpiderMonkey's <tt>Serializer</tt> should use this
	 *             constructor.
	 */
	@Deprecated
	public EntityEndMessage() {
	}

	/**
	 * Creates a new <tt>EntityEndMessage</tt> for the entity provided.
	 * 
	 * @param data
	 *            contains data about the entity that became invisible
	 */
	public EntityEndMessage(DynamicEntityData data) {
		this.entityId = data.getId();
	}

	public boolean isReliable() {
		return true;
	}

	public Message setReliable(boolean reliable) {
		return this;
	}

}
