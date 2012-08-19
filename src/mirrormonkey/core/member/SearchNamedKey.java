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

import mirrormonkey.framework.parameter.ValueUtil;

import com.jme3.network.MessageConnection;

/**
 * This <tt>SearchKey</tt> will match any constructor that has been bound to a
 * given name and for which the types of the parameters (as injected on the
 * client side) are assignment compatible to the declared parameter types in the
 * constructor.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class SearchNamedKey extends SearchKey {

	/**
	 * The name to which the constructor must be bound.
	 */
	private final String name;

	/**
	 * Creates a new <tt>SearchNamedKey</tt> that can be used to search a
	 * constructor to invoke on the client side.
	 * 
	 * @param connection
	 *            the connection for which a constructor should be searched
	 * @param parameters
	 *            the parameters that should be passed to the constructor
	 * @param name
	 *            the name to which the constructor must be bound
	 */
	public SearchNamedKey(MessageConnection connection, Object[] parameters,
			String name) {
		super(connection, parameters);
		this.name = name;
	}

	@Override
	public final boolean equals(Object o) {
		if (!o.getClass().equals(PresentNamedKey.class)) {
			return false;
		}
		PresentNamedKey other = (PresentNamedKey) o;
		return other.name.equals(name)
				&& ValueUtil.assignableFrom(getConnection(), other.data
						.getConstr().getParameterTypes(), getParameters(),
						other.data.getInterpreters());

	}
}
