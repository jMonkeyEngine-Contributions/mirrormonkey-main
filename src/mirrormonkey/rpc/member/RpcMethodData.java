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

package mirrormonkey.rpc.member;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.framework.member.MemberDataKey;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.framework.parameter.ValueInterpreter;
import mirrormonkey.framework.parameter.ValueUtil;
import mirrormonkey.rpc.annotations.RpcTarget;
import mirrormonkey.rpc.messages.RpcCallMessage;
import mirrormonkey.rpc.messages.RpcErrorMessage;
import mirrormonkey.rpc.messages.RpcResultMessage;
import mirrormonkey.rpc.module.CallRegistry;

import com.jme3.network.Message;
import com.jme3.network.MessageConnection;

/**
 * Contains immutable data about a method that can be called via RPC.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcMethodData implements StaticMemberData {

	/**
	 * Error message that will be returned to the caller if the caller-local
	 * class is excluded by the represented method's <tt>AllowRpcFrom</tt>
	 * annotation.
	 */
	public static final String DISALLOWED_INBOUND_ERROR = "Inbound calls are not allowed for this method.";

	/**
	 * Error message that will be returned to the caller if it receives a timed
	 * out invocation request.
	 */
	public static final String REMOTE_TIMEOUT_ERROR = "Call not invoked because it timed out.";

	/**
	 * Index of this <tt>RpcMethodData</tt> in the <tt>StaticEntityData's</tt>
	 * member array.
	 */
	protected final int id;

	/**
	 * The represented method that can be called via RPC.
	 */
	protected final Method method;

	/**
	 * Determine whether to use asset injection, entity injection or none of
	 * them when transmitting parameters.
	 */
	protected final ValueInterpreter[] paramAccessors;

	/**
	 * Avoids re-creating arrays in which interpreted / packed parameters are
	 * stored.
	 */
	protected final Object[] paramCache;

	/**
	 * Determines whether to use asset injection, entity injection or none of
	 * them when transmitting parameters.
	 */
	protected final ValueInterpreter resultAccessor;

	/**
	 * The <tt>MemberDataKey</tt> that is used to look up this
	 * <tt>RpcMethodData</tt> in the <tt>StaticEntityData's</tt> member map.
	 */
	protected final MemberDataKey memberKey;

	/**
	 * Determines whether the method should be invoked locally before an
	 * invocation request is sent.
	 * 
	 * At most one of <tt>localInvokeBefore</tt> or <tt>localInvokeAfter</tt>
	 * can be <tt>true</tt>.
	 */
	protected final boolean localInvokeBefore;

	/**
	 * Determines whether the method should be invoked locally after an
	 * invocation request is sent.
	 * 
	 * At most one of <tt>localInvokeBefore</tt> or <tt>localInvokeAfter</tt>
	 * can be <tt>true</tt>.
	 */
	protected final boolean localInvokeAfter;

	/**
	 * Determines whether invocation request messages, result messages and error
	 * messages should be reliable or not.
	 */
	protected final boolean reliable;

	/**
	 * Determines whether inbound RPCs will be invoked or an error message be
	 * returned.
	 */
	protected final boolean allowInbound;

	/**
	 * Determines how long to wait for incoming result or error messages for
	 * each connection before notifying <tt>RpcListeners</tt> that the call
	 * timed out, measured in nanoseconds.
	 */
	protected final long resultTimeout;

	/**
	 * Determines how long invocation requests will stay valid before timing
	 * out, measured in nanoseconds.
	 */
	protected final long callTimeout;

	/**
	 * Creates a new <tt>RpcMethodData</tt> that will represent a method that
	 * can be called via RPC.
	 * 
	 * @param id
	 *            index in the <tt>StaticEntityData's</tt> member array
	 * @param method
	 *            represented method
	 * @param paramAccessors
	 *            asset or entity injection for parameters
	 * @param resultAccessor
	 *            asset or entity injection for result
	 * @param memberKey
	 *            key in the <tt>StaticEntityData's</tt> member map
	 * @param localInvokeBefore
	 *            invoke this method before sending an invocation request
	 * @param localInvokeAfter
	 *            invoke this method after sending an invocation request
	 * @param reliable
	 *            use reliable or unreliable messages
	 * @param allowInbound
	 *            execute incoming invocation requests or return error message
	 * @param resultTimeout
	 *            time to wait for result messages, in nanoseconds
	 * @param callTimeout
	 *            time to wait before an invocation request times out, in
	 *            nanoseconds
	 */
	public RpcMethodData(int id, Method method,
			ValueInterpreter[] paramAccessors, ValueInterpreter resultAccessor,
			MemberDataKey memberKey, boolean localInvokeBefore,
			boolean localInvokeAfter, boolean reliable, boolean allowInbound,
			long resultTimeout, long callTimeout) {
		this.id = id;
		this.method = method;
		this.paramAccessors = paramAccessors;
		this.paramCache = new Object[paramAccessors.length];
		this.resultAccessor = resultAccessor;
		this.memberKey = memberKey;
		this.localInvokeBefore = localInvokeBefore;
		this.localInvokeAfter = localInvokeAfter;
		this.reliable = reliable;
		this.allowInbound = allowInbound;
		this.resultTimeout = resultTimeout;
		this.callTimeout = callTimeout;
	}

	/**
	 * Tries to execute an invocation request, if applicable.
	 * 
	 * @param source
	 *            the connection that sent the request
	 * @param entityData
	 *            mutable data about the entity on which the call should be
	 *            invoked
	 * @param message
	 *            invocation request containing call parameters
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for locally managing
	 *            synchronization
	 * @return A <tt>message</tt> that will contain the result of the
	 *         invocation. This can be an <tt>RpcResultMessage</tt> or
	 *         <tt>RpcErrorMessage</tt>, depending on if the invocation was
	 *         successful or if it threw an exception.
	 */
	public Message executeFromRemote(
			@SuppressWarnings("unused") MessageConnection source,
			DynamicEntityData entityData, RpcCallMessage message,
			SyncAppState<?> appState) {
		if (!allowInbound) {
			return message.callId != CallRegistry.NO_RESPONSE_EXPECTED ? new RpcErrorMessage(
					message.callId, DISALLOWED_INBOUND_ERROR, message.reliable)
					: null;
		}
		if (callTimeout != RpcTarget.NO_CALL_TIMEOUT
				&& ((message.estdInvocationTime + callTimeout) < appState
						.getSyncTime())) {
			return message.callId != CallRegistry.NO_RESPONSE_EXPECTED ? new RpcErrorMessage(
					message.callId, REMOTE_TIMEOUT_ERROR, message.reliable)
					: null;
		}
		try {
			int i = 0;
			for (ValueInterpreter a : paramAccessors) {
				paramCache[i] = a.extractData(message.parameters[i++]);
			}
			Object result;
			result = method.invoke(entityData.getLocalInstance(), paramCache);
			result = resultAccessor.packData(result);
			return message.callId != CallRegistry.NO_RESPONSE_EXPECTED ? new RpcResultMessage(
					message.callId, result, message.reliable) : null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return new RpcErrorMessage(message.callId, e.getCause(),
					message.reliable);
		} catch (Throwable e) {
			e.printStackTrace();
			return new RpcErrorMessage(message.callId, e, message.reliable);
		}
	}

	@Override
	public String toString() {
		String s = "[" + getClass().getName() + "@"
				+ System.identityHashCode(this) + ":";
		for (ValueInterpreter p : paramAccessors) {
			s += "\n  " + p;
		}
		return s;
	}

	/**
	 * Uses the <tt>ValueAccessor</tt> to extract a result from a result
	 * message, according to <tt>EntityInjection</tt> or <tt>AssetInjection</tt>
	 * annotations on the represented method.
	 * 
	 * @param result
	 *            packed result that was received from the called side
	 * @return unpacked result, with respect to entity injection or asset
	 *         injection
	 */
	public Object extractResult(Object result) {
		return resultAccessor.extractData(result);
	}

	public MemberDataKey getMemberKey() {
		return memberKey;
	}

	/**
	 * Uses <tt>ValueAccessors</tt> to pack an array of parameters to create an
	 * invocation request, according to their <tt>EntityInjection</tt> and
	 * <tt>AssetInjection</tt> annotations.
	 * 
	 * @param args
	 *            the arguments that should be packed
	 * @return array of packed parameters with respect to entity injection and
	 *         asset injection
	 */
	public Object[] packParams(Object[] args) {
		return ValueUtil.packData(paramAccessors, args);
	}

	public DynamicMemberData createDynamicData(DynamicEntityData entity) {
		return null;
	}

	public void setStaticEntityData(StaticEntityData staticData) {
	}

	/**
	 * @return time to wait for result or error messages, in nanoseconds
	 */
	public long getResultTimeout() {
		return resultTimeout;
	}

	/**
	 * @return the index used to fetch this <tt>RpcMethodData</tt> from the
	 *         <tt>StaticEntityData's</tt> member array
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return <tt>true</tt> if messages used to transmit invocation requests,
	 *         results or errors for the represented method should be reliable,
	 *         <tt>false</tt> otherwise
	 */
	public boolean isReliable() {
		return reliable;
	}

	/**
	 * @return The represented method that can be called from remote.
	 */
	public Method getMethod() {
		return method;
	}
}
