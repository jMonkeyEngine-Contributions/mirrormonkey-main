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
 * Contains the result of an invocation.
 * 
 * <tt>RpcResultMessages</tt> are only created and transmitted if the caller
 * expects a response, the invocation was successful and the invocation did not
 * throw an exception.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class RpcResultMessage implements Message {

	/**
	 * The unique ID of the call message to identify which call returned.
	 */
	public int callId;

	/**
	 * The result that the call returned, packed according to the invoked
	 * method's <tt>EntityInjection</tt> and <tt>AssetInjection</tt>
	 * annotations.
	 */
	public Object result;

	/**
	 * Determines whether reliable or unreliable transfer should be used when
	 * sending the message.
	 */
	public boolean reliable;

	/**
	 * Empty constructor for SpiderMonkey's <tt>Serializer</tt>.
	 * 
	 * @deprecated only for serialization
	 */
	@Deprecated
	public RpcResultMessage() {
	}

	/**
	 * Creates a new <tt>RpcResultMessage</tt> for a given call and result.
	 * 
	 * @param callId
	 *            unique ID of the call that returned
	 * @param result
	 *            result that was returned by the call
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used,
	 *            <tt>false</tt> if unreliable transfer should be used
	 */
	public RpcResultMessage(int callId, Object result, boolean reliable) {
		this.callId = callId;
		this.result = result;
		this.reliable = reliable;
	}

	public boolean isReliable() {
		return reliable;
	}

	public Message setReliable(boolean reliable) {
		this.reliable = reliable;
		return this;
	}

	@Override
	public String toString() {
		return "[RpcResultMessage@" + System.identityHashCode(this)
				+ ": callId=" + callId + " result=" + result + " reliable="
				+ reliable + "]";
	}
}
