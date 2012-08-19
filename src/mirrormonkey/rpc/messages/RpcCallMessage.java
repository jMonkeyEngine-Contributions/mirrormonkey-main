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

package mirrormonkey.rpc.messages;

import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Contains an invocation request that is transmitted over the network.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class RpcCallMessage implements Message {

	/**
	 * ID of the entity for which a method should be invoked.
	 */
	public int entityId;

	/**
	 * Index of the <tt>RpcMethodData</tt> in its <tt>StaticEntityData's</tt>
	 * member array.
	 */
	public int methodId;

	/**
	 * Unique ID of the invocation request or <tt>NO_RESPONSE_EXPECTED</tt>.
	 * Response messages will contain the same id to identify which call has
	 * returned.
	 */
	public int callId;

	/**
	 * Parameters for the call, in packed form according to the
	 * <tt>EntityInjection</tt> and <tt>AssetInjection</tt> annotations.
	 */
	public Object[] parameters;

	/**
	 * Determines whether to use reliable or unreliable transfer.
	 */
	public boolean reliable;

	/**
	 * Time on the called side, as estimated by the calling side at the point of
	 * time that the call request was created.
	 */
	public long estdInvocationTime;

	/**
	 * Empty constructor for SpiderMonkey's <tt>Serializer</tt>.
	 * 
	 * @deprecated only for deserialization puropse
	 */
	@Deprecated
	public RpcCallMessage() {
	}

	/**
	 * Creates a new <tt>RpcCallMessage</tt> that will contain a given
	 * invocation request.
	 * 
	 * @param entityId
	 *            id of the entity for which a method should be invoked
	 * @param methodId
	 *            index of the method in the current <tt>StaticEntityData's</tt>
	 *            member array
	 * @param callId
	 *            id to identify the call for response messages, if any
	 * @param parameters
	 *            parameters for the call in packed form
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used,
	 *            <tt>false</tt> if unreliable transfer should be used
	 * @param estdInvocationTime
	 *            time on the called side, as estimated on the calling side on
	 *            message creation
	 */
	public RpcCallMessage(int entityId, int methodId, int callId,
			Object[] parameters, boolean reliable, long estdInvocationTime) {
		this.entityId = entityId;
		this.methodId = methodId;
		this.callId = callId;
		this.parameters = parameters;
		this.reliable = reliable;
		this.estdInvocationTime = estdInvocationTime;
	}

	public boolean isReliable() {
		return reliable;
	}

	public Message setReliable(boolean reliable) {
		this.reliable = reliable;
		return this;
	}

}
