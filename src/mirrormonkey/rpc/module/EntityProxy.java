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

import mirrormonkey.rpc.RpcListener;
import mirrormonkey.rpc.RpcSpecification;

import com.jme3.network.MessageConnection;

/**
 * Implemented by every proxy that handles RPC.
 * 
 * Provides methods that control on which entities and connections RPCs are
 * performed and which listeners are notified of returning calls.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <T>
 *            Specification implemented by this proxy.
 */
public interface EntityProxy<T extends RpcSpecification> {

	/**
	 * Users can perform RPCs to the reference returned by this method. The RPCs
	 * will be routed to entities and connections according to this
	 * <tt>EntityProxy</tt>.
	 * 
	 * @return this <tt>EntityProxy</tt>, type-safely cast to <tt>T</tt>
	 */
	public T getCallTarget();

	/**
	 * Adds a connection to this <tt>EntityProxy</tt>.
	 * 
	 * @param connection
	 *            The connection to add. After this method returns, every call
	 *            to an RPC target method of this proxy will be relayed to every
	 *            entity and every connection currently present in this proxy.
	 */
	public void addTargetConnection(MessageConnection connection);

	/**
	 * Removes a target connection from this <tt>EntityProxy</tt>. After this
	 * method has returned, method calls on RPC target methods of this
	 * <tt>EntityProxy</tt> will no longer be sent to the connection.
	 * 
	 * @param connection
	 *            the <tt>MessageConnection</tt> that will no longer be
	 *            requested to invoke RPCs
	 */
	public void removeTargetConnection(MessageConnection connection);

	/**
	 * Adds a target entity to this <tt>EntityProxy</tt>. The entity must
	 * implement the specification type parameter of this <tt>EntityProxy</tt>.
	 * 
	 * Every call on the RPC target methods implemented by this
	 * <tt>EntityProxy</tt> will be sent to every currently added connection and
	 * every currently added entity.
	 * 
	 * @param entity
	 *            the local instance of the entity for which invocation requests
	 *            should be broadcasted by this <tt>RpcProxy</tt>
	 */
	public void addTargetEntity(T entity);

	/**
	 * Removes a target entity from this <tt>EntityProxy</tt> or does nothing if
	 * the entity is not currently added.
	 * 
	 * @param entity
	 *            local instance of the entity for which invocation requests
	 *            should no longer be broadcasted by this <tt>RpcProxy</tt>.
	 */
	public void removeTargetEntity(T entity);

	/**
	 * Adds an <tt>RpcListener</tt> that will be notified for every returning
	 * call on every entity and every connection.
	 * 
	 * Please note that adding or removing listeners does take effect
	 * immediately. This means that if there are invocation requests that have
	 * been sent but did not yet return, then listeners that have been added in
	 * the meantime will be notified of returning calls, even if they were not
	 * added at the point of time that the invocation request was sent.
	 * 
	 * This does not apply to a situation where there are no rpc listeners
	 * present when the request is generated, in which case no listeners will be
	 * notified in any case.
	 * 
	 * @param listener
	 *            the <tt>RpcListener</tt> that will be notified of timeouts,
	 *            errors and results of every invocation performed on this
	 *            <tt>EntityProxy</tt>
	 */
	public void addRpcListener(RpcListener listener);

	/**
	 * Removes an <tt>RpcListener</tt> from this <tt>EntityProxy</tt>.
	 * 
	 * Please note that adding or removing listeners does take effect
	 * immediately. This means that if there are invocation requests that have
	 * been sent but did not yet return, then listeners that have been removed
	 * in the meantime will not be notified of returning calls, even if they
	 * were added at the point of time that the invocation request was sent.
	 * 
	 * @param listener
	 *            the <tt>RpcListener</tt> that will no longer be notified of
	 *            timeouts, errors and results of invocations performed on this
	 *            <tt>EntityProxy</tt>
	 */
	public void removeRpcListener(RpcListener listener);

}
