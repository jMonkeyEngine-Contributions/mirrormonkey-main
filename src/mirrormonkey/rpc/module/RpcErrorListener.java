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

import mirrormonkey.rpc.messages.RpcErrorMessage;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;
import com.jme3.network.MessageListener;

/**
 * Listens to inbound <tt>RpcErrorListeners</tt> and notifies listeners for
 * those messages.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcErrorListener implements MessageListener<MessageConnection> {

	/**
	 * The RPC module's <tt>CallRegistry</tt> to which are registered.
	 */
	private final CallRegistry callRegistry;

	/**
	 * Creates a new <tt>RpcErrorListener</tt> that will fetch call data from a
	 * given <tt>CallRegistry</tt>.
	 * 
	 * @param callRegistry
	 *            the call registry that stores data about invocation requests
	 */
	public RpcErrorListener(CallRegistry callRegistry) {
		this.callRegistry = callRegistry;
	}

	public void messageReceived(MessageConnection source, Message message) {
		RpcErrorMessage castMessage = (RpcErrorMessage) message;
		RegisteredCall call = callRegistry
				.getRegisteredCall(castMessage.callId);
		// Can be null due to timeout
		if (call != null) {
			call.errorArrived(source, castMessage);
		}
	}

}
