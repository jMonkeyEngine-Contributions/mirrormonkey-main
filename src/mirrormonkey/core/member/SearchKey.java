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

package mirrormonkey.core.member;

import mirrormonkey.framework.member.MemberDataKey;

import com.jme3.network.MessageConnection;

/**
 * Encapsulates an algorithm that tries to match a set of constructor parameters
 * against a present constructor that can be called on the client side.
 * 
 * Used to determine which constructor is called when creating a client-local
 * instance of an entity.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class SearchKey implements MemberDataKey {

	/**
	 * The connection for which a constructor should be searched. This must be
	 * specified to enable matching client-local classes for entity injection.
	 */
	private final MessageConnection connection;

	/**
	 * The parameters that should be passed to the constructor invocation.
	 */
	private final Object[] parameters;

	/**
	 * Creates a new <tt>SearchKey</tt> that can be used to match present
	 * constructors against a set of parameters.
	 * 
	 * @param connection
	 *            the connection for which a local constructor should be
	 *            searched
	 * @param parameters
	 *            the parameters that should be passed to the constructor
	 */
	public SearchKey(MessageConnection connection, Object[] parameters) {
		this.connection = connection;
		this.parameters = parameters;
	}

	@Override
	public int hashCode() {
		return parameters.length * PresentKey.class.hashCode();
	}

	/**
	 * @return the parameters that should be passed to the constructor when
	 *         invoking it
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * @return the connection for which a constructor should be searched
	 */
	public MessageConnection getConnection() {
		return connection;
	}

}
