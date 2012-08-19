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

import java.lang.reflect.Method;

import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.rpc.RpcListener;
import mirrormonkey.rpc.member.RpcMethodData;
import mirrormonkey.rpc.messages.RpcErrorMessage;
import mirrormonkey.rpc.messages.RpcResultMessage;

import com.jme3.network.MessageConnection;

/**
 * Contains data about an invocation request that was created for one entity and
 * one connection and expects a result.
 * 
 * For a single call performed on an <tt>EntityProxy</tt>, there may be multiple
 * instances of <tt>RegisteredCall</tt> created and registered, one for each
 * connection and entity that the <tt>EntityProxy</tt> contains.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RegisteredCall {

	/**
	 * The <tt>CallRegistry</tt> keeping track of this registered call.
	 */
	protected final CallRegistry registry;

	/**
	 * The unique ID of this <tt>RegisteredCall</tt> that is used to identify
	 * response messages.
	 */
	protected final int id;

	/**
	 * Static data about the method that has been invoked.
	 */
	protected final RpcMethodData methodData;

	/**
	 * The <tt>MessageConnection</tt> that the invocation request has been sent
	 * to.
	 */
	protected final MessageConnection expectResultFrom;

	/**
	 * The <tt>RpcInvocationHandler</tt> that generated the invocation request.
	 */
	protected final RpcInvocationHandler<?> handler;

	/**
	 * The value of <tt>System.nanoTime</tt> at which this
	 * <tt>RegisteredCall</tt> times out.
	 */
	protected final long timeoutAt;

	/**
	 * The entity on which the call has been invoked.
	 */
	protected final SyncEntity entity;

	/**
	 * Creates a new <tt>RegisteredCall</tt> for a given invocation request.
	 * 
	 * @param module
	 *            the module generating the invocation request
	 * @param methodData
	 *            static data about the invoked method
	 * @param expectResultFrom
	 *            connection to which the request has been sent
	 * @param handler
	 *            the invocation handler generating the request
	 * @param timeout
	 *            amount of nanoseconds after which the call should time out and
	 *            listeners should be notified
	 * @param entity
	 *            local instance of the entity on which a call was invoked
	 */
	public RegisteredCall(RpcModule module, RpcMethodData methodData,
			MessageConnection expectResultFrom,
			RpcInvocationHandler<?> handler, long timeout, SyncEntity entity) {
		registry = module.callRegistry;
		if (handler.listeners.isEmpty()) {
			id = CallRegistry.NO_RESPONSE_EXPECTED;
		} else {
			id = registry.registerCall(this);
		}

		this.methodData = methodData;
		this.expectResultFrom = expectResultFrom;
		this.handler = handler;
		this.timeoutAt = System.nanoTime() + timeout;
		this.entity = entity;
	}

	/**
	 * Notifies RPC listeners that a result arrived and unregisters this
	 * <tt>RegisteredCall</tt> from the call registry (or throws a
	 * <tt>RuntimeException</tt> if some other connection tried to inject a
	 * result).
	 * 
	 * @param source
	 *            connection that returned a result for the call
	 * @param castMessage
	 *            the result message received from <tt>source</tt>
	 */
	public void resultArrived(MessageConnection source,
			RpcResultMessage castMessage) {
		if (!expectResultFrom.equals(source)) {
			throw new RuntimeException();
		}
		try {
			Object extractedResult = methodData
					.extractResult(castMessage.result);
			for (RpcListener i : handler.listeners) {
				i.resultArrived(this, extractedResult);
			}
		} catch (IllegalStateException e) {
			for (RpcListener i : handler.listeners) {
				i.errorArrived(this, e.getMessage());
			}
		}
		registry.removeCall(this);
	}

	/**
	 * Notifies RPC listeners that an error arrived and unregisters this
	 * <tt>RegisteredCall</tt> from the call registry (or throws a
	 * <tt>RuntimeException</tt> if some other connection tried to inject a
	 * result).
	 * 
	 * @param source
	 *            connection that returned an error for the call
	 * @param castMessage
	 *            the error message received from <tt>source</tt>
	 */
	public void errorArrived(MessageConnection source,
			RpcErrorMessage castMessage) {
		if (!expectResultFrom.equals(source)) {
			throw new RuntimeException();
		}
		for (RpcListener i : handler.listeners) {
			i.errorArrived(this, castMessage.message);
		}
		registry.removeCall(this);
	}

	/**
	 * Checks whether this <tt>RegisteredCall</tt> has timed out. Notifies RPC
	 * listeners and unregisters it from the call registry if it has, does
	 * nothing if it hasn't.
	 */
	public void update() {
		if (System.nanoTime() >= timeoutAt) {
			for (RpcListener i : handler.listeners) {
				i.timedOut(this);
			}
			registry.removeCall(this);
		}
	}

	/**
	 * @return connection that the invocation request was sent to
	 */
	public MessageConnection getConnection() {
		return expectResultFrom;
	}

	/**
	 * @return local instance of the entity that the call was invoked on
	 */
	public SyncEntity getEntity() {
		return entity;
	}

	/**
	 * @return reflective descriptor of the method thas has been invoked
	 */
	public Method getMethod() {
		return methodData.getMethod();
	}

	/**
	 * @return unique ID of this call
	 */
	public int getId() {
		return id;
	}

}
