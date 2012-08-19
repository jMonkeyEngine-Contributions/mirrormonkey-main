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
 * Contains either the message of an exception thrown by an invocation or the
 * message of an internal error.
 * 
 * <tt>RpcErrorMessages</tt> are only created and transmitted if the caller
 * expects a response. They are used instead of <tt>RpcResultMessages</tt> if
 * the method invocation throws an exception or an internal error occurs.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class RpcErrorMessage implements Message {

	/**
	 * The unique ID of the call message to identify which call caused an error.
	 */
	public int callId;

	/**
	 * The error message that the call produced. This can be either the reason
	 * string if an exception was thrown or an internal error message.
	 */
	public String message;

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
	public RpcErrorMessage() {
	}

	/**
	 * Creates a new <tt>RpcErrorMessage</tt> for a given call and result.
	 * 
	 * @param callId
	 *            unique ID of the call that returned
	 * @param e
	 *            thrown exception
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used,
	 *            <tt>false</tt> if unreliable transfer should be used
	 */
	public RpcErrorMessage(int callId, Throwable e, boolean reliable) {
		this(callId, e.getMessage(), reliable);
	}

	/**
	 * 
	 * Creates a new <tt>RpcErrorMessage</tt> for a given call and result.
	 * 
	 * @param callId
	 *            unique ID of the call that returned
	 * @param message
	 *            error message
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used,
	 *            <tt>false</tt> if unreliable transfer should be used
	 */
	public RpcErrorMessage(int callId, String message, boolean reliable) {
		this.callId = callId;
		this.message = message;
		this.reliable = reliable;
	}

	public boolean isReliable() {
		return reliable;
	}

	public Message setReliable(boolean reliable) {
		this.reliable = reliable;
		return this;
	}

}
