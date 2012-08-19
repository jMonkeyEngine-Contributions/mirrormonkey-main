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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.rpc.RpcListener;
import mirrormonkey.rpc.RpcSpecification;
import mirrormonkey.rpc.annotations.InvokeLocally;
import mirrormonkey.rpc.annotations.InvokeLocally.LocalInvokeMode;
import mirrormonkey.rpc.annotations.RpcTarget;
import mirrormonkey.rpc.member.RpcMethodData;
import mirrormonkey.rpc.member.RpcMethodKey;
import mirrormonkey.rpc.messages.RpcCallMessage;

import com.jme3.network.MessageConnection;

/**
 * Implementation of <tt>EntityProxy</tt> that will be passed to java's proxy
 * factory.
 * 
 * Implements all the methods from <tt>EntityProxy</tt> and delegates reflective
 * invocations to them.
 * 
 * Reflective invocations of methods defined in the RPC specification that this
 * invocation handler was created for will be intercepted and relayed to all
 * added entities and all added connections, according to the annotations
 * present on the intercepted methods.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <T>
 *            Class of the RPC specification implemented by all added entities
 */
public class RpcInvocationHandler<T extends RpcSpecification> implements
		InvocationHandler, EntityProxy<T> {

	/**
	 * Warning and error messages that are not severe enough for an exception go
	 * here.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(RpcInvocationHandler.class.getName());

	/**
	 * RPC specification for which this invocation handler was created.
	 */
	protected final Class<T> proxyRpcSpec;

	/**
	 * The proxy for which this invocation handler was created.
	 */
	protected final EntityProxy<T> proxy;

	/**
	 * The proxy for which this invocation handler was created, casted up to the
	 * RPC specification.
	 */
	protected final T callTarget;

	/**
	 * Core module supplies data about estimated remote time, which is needed
	 * for call timeout.
	 */
	protected final CoreModule<?, ?> coreModule;

	/**
	 * The <tt>RpcModule</tt> to which calls are registered if result messages
	 * are expected.
	 */
	protected final RpcModule module;

	/**
	 * All connections that intercepted RPCs should be relied to.
	 */
	protected final Collection<MessageConnection> connections;

	/**
	 * All entities that intercepted RPCs should be relied to.
	 */
	protected final Collection<T> targetEntities;

	/**
	 * All <tt>RpcListeners</tt> that should be notified when result messages
	 * are received.
	 */
	protected final Collection<RpcListener> listeners;

	/**
	 * Creates a new <tt>RpcInvocationHandler</tt> for a given proxy
	 * specification.
	 * 
	 * @param module
	 *            module responsible for registering calls
	 * @param proxyRpcSpec
	 *            RPC specification for which this invocation handler is created
	 * @param coreModule
	 *            supplies information about the connections that we send
	 *            messages to
	 */
	@SuppressWarnings("unchecked")
	public RpcInvocationHandler(RpcModule module, Class<T> proxyRpcSpec,
			CoreModule<?, ?> coreModule) {
		this.proxyRpcSpec = proxyRpcSpec;
		proxy = (EntityProxy<T>) Proxy.newProxyInstance(
				proxyRpcSpec.getClassLoader(), new Class<?>[] { proxyRpcSpec,
						EntityProxy.class }, this);
		callTarget = (T) proxy;
		this.module = module;
		connections = new LinkedList<MessageConnection>();
		listeners = new LinkedList<RpcListener>();
		targetEntities = new LinkedList<T>();
		this.coreModule = coreModule;
	}

	public T getCallTarget() {
		return callTarget;
	}

	public void addTargetConnection(MessageConnection connection) {
		connections.add(connection);
	}

	public void removeTargetConnection(MessageConnection connection) {
		connections.remove(connection);
	}

	public void addRpcListener(RpcListener listener) {
		listeners.add(listener);
	}

	public void removeRpcListener(RpcListener listener) {
		listeners.remove(listener);
	}

	public void addTargetEntity(T entity) {
		targetEntities.add(entity);
	}

	public void removeTargetEntity(T entity) {
		targetEntities.remove(entity);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getDeclaringClass().equals(EntityProxy.class)
				|| method.getDeclaringClass().equals(Object.class)) {
			return method.invoke(this, args);
		} else if (method.getDeclaringClass().equals(proxyRpcSpec)) {
			Object[] rpArgs = args == null ? new Object[0] : args;
			Object localResult = null;
			for (T i : targetEntities) {
				localResult = invokeOnEntity(i, method, rpArgs);
			}
			if (targetEntities.size() == 1) {
				return localResult;
			}
			return null;
		} else {
			throw new IllegalArgumentException("Method not found: " + method);
		}
	}

	/**
	 * Intercepts a call to a method defined in the RPC specification that this
	 * invocation handler was created for. Called for every added entity once a
	 * call is intercepted.
	 * 
	 * Creates an invocation request for every connection and sends it.
	 * Registers calls to the RPC module if a result is expected.
	 * 
	 * @param entity
	 *            entity to create invocation requests for
	 * @param method
	 *            RPC method that should be invoked
	 * @param args
	 *            parameters passed to the call, unpacked form
	 * @return <tt>null</tt> if the invocation is not performed locally, the
	 *         result of the local invocation if it is performed locally
	 * @throws Throwable
	 *             if it occurs
	 */
	public Object invokeOnEntity(SyncEntity entity, Method method, Object[] args)
			throws Throwable {
		if (!method.isAnnotationPresent(RpcTarget.class)) {
			return method.invoke(entity, args);
		}

		DynamicEntityData entityData = entity.getData();
		boolean localBefore = false;

		InvokeLocally il = method.getAnnotation(InvokeLocally.class);
		LocalInvokeMode localMode = il == null ? LocalInvokeMode.NONE : il
				.value();
		Object invocationResult = null;
		if (localMode.equals(LocalInvokeMode.BEFORE)) {
			invocationResult = method.invoke(entity, args);
			localBefore = true;
		}
		for (MessageConnection i : connections) {
			StaticEntityData staticData = entityData.getActiveStaticData(i);
			if (staticData == null) {
				LOGGER.warning("Tried to relay RPC to connection " + i
						+ "; entity: " + entity
						+ "; static entity data not found.");
				continue;

			}

			RpcMethodData methodData = staticData.getData(new RpcMethodKey(
					method.getName(), method.getParameterTypes()),
					RpcMethodData.class);

			if (methodData == null) {
				LOGGER.warning("Tried to relay RPC to connection: " + i
						+ "; entity: " + entity + "; method data not found.");
				/*
				 * for (RpcListener l : listeners) { l.errorArrived(null, i,
				 * RpcErrorMessage.PARAM_CLASS_CAST_EXCEPTION); }
				 * coreModule.getAppState
				 * ().getEventManager().getMessageListener() .messageReceived(i,
				 * new RpcErrorMessage());
				 */
				continue;
			}

			RegisteredCall rc = new RegisteredCall(module, methodData, i, this,
					methodData.getResultTimeout(),
					entityData.getLocalInstance());
			RpcCallMessage message = new RpcCallMessage(entityData.getId(),
					methodData.getId(), rc.getId(),
					methodData.packParams(args), methodData.isReliable(),
					coreModule.getData(i).getEstimatedRemoteTime());

			i.send(message);

		}
		if (localMode.equals(LocalInvokeMode.AFTER) && !localBefore) {
			invocationResult = method.invoke(entityData.getLocalInstance(),
					args);
		}
		return invocationResult;
	}
}
