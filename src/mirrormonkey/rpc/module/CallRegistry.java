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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import mirrormonkey.util.IdGenerator;

/**
 * Responsible for assigning a unique ID to and for tracking each outbound RPC
 * that expects a response.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class CallRegistry {

	/**
	 * ID for invocation requests that do not expect a response.
	 */
	public static final int NO_RESPONSE_EXPECTED = -1;

	/**
	 * Generates unique IDs for invocation requests that expect a response.
	 */
	protected final IdGenerator idGen;

	/**
	 * Contains data about expected responses, mapped by the call's ID.
	 */
	protected final Map<Integer, RegisteredCall> currentCalls;

	/**
	 * Contains all currently registered calls during update so they can remove
	 * themselves on timeout without causing a
	 * <tt>ConcurrentModificationException</tt>.
	 */
	protected final LinkedList<RegisteredCall> currentIteration;

	/**
	 * Creates a new <tt>CallRegistry</tt>.
	 */
	public CallRegistry() {
		idGen = new IdGenerator();
		idGen.reserve(NO_RESPONSE_EXPECTED);
		currentCalls = new LinkedHashMap<Integer, RegisteredCall>();
		currentIteration = new LinkedList<RegisteredCall>();
	}

	/**
	 * Generates a new ID for a given <tt>RegisteredCall</tt> and registers it
	 * in this <tt>CallRegistry</tt>.
	 * 
	 * <tt>RegisteredCalls</tt> call this method in their constructor and set
	 * their IDs to the returned value.
	 * 
	 * @param call
	 *            created call requesting an ID
	 * @return ID for <tt>call</tt> that is guaranteed to be unique for as long
	 *         as <tt>call</tt> is registered
	 */
	public synchronized int registerCall(RegisteredCall call) {
		int id = idGen.generateAndReserve();
		currentCalls.put(Integer.valueOf(id), call);
		return id;
	}

	/**
	 * Fetches a <tt>RegisteredCall</tt> for a given ID.
	 * 
	 * @param callId
	 *            the ID or the call
	 * @return an instance of <tt>RegisteredCall</tt> that contains data about
	 *         the invocation request with <tt>callId</tt> or <tt>null</tt> if
	 *         no such call is registered
	 */
	public synchronized RegisteredCall getRegisteredCall(int callId) {
		return currentCalls.get(Integer.valueOf(callId));
	}

	/**
	 * Checks this <tt>CallRegistry</tt> for calls that timed out, removes them
	 * and notifies <tt>RpcListeners</tt> of timeouts.
	 */
	public synchronized void update() {
		currentIteration.clear();
		currentIteration.addAll(currentCalls.values());
		for (RegisteredCall i : currentIteration) {
			i.update();
		}
	}

	/**
	 * Removes a given registered call from this <tt>CallRegistry</tt> if it is
	 * registered. Does nothing otherwise.
	 * 
	 * @param registeredCall
	 *            the <tt>RegisteredCall</tt> to remove
	 */
	protected void removeCall(RegisteredCall registeredCall) {
		currentCalls.remove(registeredCall.id);
	}

	/**
	 * @return the <tt>IdGenerator</tt> that generates unique IDs for invocation
	 *         requests that expect a response
	 */
	public IdGenerator getIdGen() {
		return idGen;
	}

}
