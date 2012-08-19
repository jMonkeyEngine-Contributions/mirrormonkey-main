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

import com.jme3.network.MessageConnection;

/**
 * This <tt>SearchKey</tt> will match any constructor that was bound to a given
 * name. In contrast to <tt>SearchNamedKey</tt>, no checks will be performed on
 * parameter count and types, which makes this key faster. The drawback is that
 * if for some reason the constructor can not be invoked on the client side due
 * to a programming error, there will be no feedback to the server at all.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class SearchNamedUnsafeKey extends SearchKey {

	/**
	 * The name that the constructor must be bound to.
	 */
	private final String name;

	/**
	 * Creates a new <tt>SearchNamedUnsafeKey</tt> that can be used to search a
	 * constructor to invoke on the client side.
	 * 
	 * @param connection
	 *            the connection for which a constructor should be searched
	 * @param parameters
	 *            the parameters that should be passed to the constructor
	 * @param name
	 *            the name to which the constructor must be bound
	 */
	public SearchNamedUnsafeKey(MessageConnection connection,
			Object[] parameters, String name) {
		super(connection, parameters);
		this.name = name;
	}

	@Override
	public final boolean equals(Object o) {
		if (!o.getClass().equals(PresentNamedKey.class)) {
			return false;
		}
		return ((PresentNamedKey) o).name.equals(name);
	}

}
