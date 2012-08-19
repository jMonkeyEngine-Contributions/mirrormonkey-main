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

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.module.MirrorMonkeyModule;
import mirrormonkey.rpc.RpcSpecification;
import mirrormonkey.rpc.annotations.RpcAnnotationPresets;
import mirrormonkey.rpc.messages.RpcCallMessage;
import mirrormonkey.rpc.messages.RpcErrorMessage;
import mirrormonkey.rpc.messages.RpcResultMessage;

import com.jme3.network.serializing.Serializer;

/**
 * Glues the different classes of the RPC module together.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcModule extends MirrorMonkeyModule<SyncAppState<?>> {

	/**
	 * Classes of all messages that this module uses.
	 */
	public static final Class<?>[] RPC_MESSAGES = { RpcCallMessage.class,
			RpcResultMessage.class, RpcErrorMessage.class };

	/**
	 * Listens for incoming invocation requests, determines if they are valid
	 * and invokes them if they are.
	 */
	protected final RpcCallListener callListener;

	/**
	 * Listens for incoming result messages, determines if they are valid and
	 * notifies RPC listeners if they are.
	 */
	protected final RpcResultListener responseListener;

	/**
	 * Listens for incoming error messages, determines if they are valid and
	 * notifies RPC listeners if they are.
	 */
	protected final RpcErrorListener errorListener;

	/**
	 * Responsible for generating unique IDs for invocation requests and keeping
	 * track of call messages for which no result has been received yet.
	 */
	protected final CallRegistry callRegistry;

	/**
	 * Supplies data about connections.
	 */
	protected final CoreModule<?, ?> coreModule;

	/**
	 * Creates a new <tt>RpcModule</tt> using a given <tt>SyncAppState</tt> and
	 * the default call registry implementation.
	 * 
	 * @param appState
	 *            the local <tt>SyncAppState</tt> responsible for
	 *            synchronization
	 */
	public RpcModule(SyncAppState<?> appState) {
		this(appState, new CallRegistry());
	}

	/**
	 * Creates a new <tt>RpcModule</tt> using a given <tt>SyncAppState</tt> and
	 * <tt>CallRegistry</tt>.
	 * 
	 * @param appState
	 *            local <tt>SyncAppState</tt> responsible for synchronization
	 * @param callRegistry
	 *            the <tt>CallRegistry</tt> keeping track of invocation requests
	 *            that expect result messages to be returned
	 */
	public RpcModule(SyncAppState<?> appState, CallRegistry callRegistry) {
		this(appState, new RpcCallListener(appState), new RpcResultListener(
				callRegistry), new RpcErrorListener(callRegistry), callRegistry);
	}

	/**
	 * Creates a new <tt>RpcModule</tt> using a given <tt>SyncAppState</tt>,
	 * <tt>CallRegistry</tt> and message listeners.
	 * 
	 * @param appState
	 *            local <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param callListener
	 *            responsible for invoking inbound invocation requests
	 * @param responseListener
	 *            responsible for notifying RPC listeners of inbound response
	 *            messages
	 * @param errorListener
	 *            responsible for notifying RPC listeners of inbound error
	 *            messages
	 * @param callRegistry
	 *            responsible for keeping track of invocation requests that
	 *            expect result messages to be returned
	 */
	public RpcModule(SyncAppState<?> appState, RpcCallListener callListener,
			RpcResultListener responseListener, RpcErrorListener errorListener,
			CallRegistry callRegistry) {
		super(appState);

		CoreModule<?, ?> coreModule = appState.getModule(CoreModule.class);

		coreModule.getEntityProvider().parsePresetClass(
				RpcAnnotationPresets.class);

		for (Class<?> i : RPC_MESSAGES) {
			Serializer.registerClass(i);
		}

		this.callListener = callListener;
		this.responseListener = responseListener;
		this.errorListener = errorListener;
		this.callRegistry = callRegistry;
		this.coreModule = coreModule;

		appState.getEventManager().addMessageListener(callListener,
				RpcCallMessage.class);
		appState.getEventManager().addMessageListener(responseListener,
				RpcResultMessage.class);
		appState.getEventManager().addMessageListener(errorListener,
				RpcErrorMessage.class);
	}

	/**
	 * Instantiates and returns a new <tt>EntityProxy</tt> that can be used to
	 * relay RPCs to methods defined in an <tt>RpcSpecification</tt> to a
	 * dynamic amount of entities and connections.
	 * 
	 * @param spec
	 *            The <tt>RpcSpecification</tt>. The returned proxy will
	 *            implement this interface and relay local calls to its methods
	 *            to the entities and connections that were added to it.
	 * @return an implementation of <tt>EntityProxy</tt> that will intercept
	 *         calls to methods defined in <tt>spec</tt> and relay them to added
	 *         entities and connections
	 */
	public <T extends RpcSpecification> EntityProxy<T> createRpcProxy(
			Class<T> spec) {
		return new RpcInvocationHandler<T>(this, spec, coreModule);
	}

	@Override
	public void update(float tpf) {
		callRegistry.update();
	}

	/**
	 * Internally, this is only used for testing and debugging.
	 * 
	 * @return the <tt>CallRegistry</tt> keeping track of invocation requests
	 *         that expect a response from clients, but have neither returned
	 *         nor timed out
	 */
	public CallRegistry getCallRegistry() {
		return callRegistry;
	}

}
