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

import java.util.Arrays;

/**
 * Used to map constructors that can be called on the client side and use either
 * name based or unsafe name based parameter matching.
 * 
 * A <tt>PresentNamedKey</tt> will delegate any call to <tt>equals</tt> to the
 * call parameter if the call parameter is a <tt>SearchKey</tt>, so by
 * exchanging the class of the search key for constructor lookups, the matching
 * algorithm can be replaced.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class PresentNamedKey extends PresentKey {

	/**
	 * The name to which the represented constructor is bound.
	 */
	protected final String name;

	/**
	 * Creates a new <tt>PresentNamedKey</tt> that will be used to map a given
	 * constructor, bound to a given name.
	 * 
	 * @param data
	 *            the <tt>ConstructorData</tt> representing the constructor that
	 *            this <tt>PresentNamedKey</tt> will match against
	 * @param name
	 *            the name to which the constructor is bound
	 */
	public PresentNamedKey(ConstructorData data, String name) {
		super(data);
		this.name = name;
	}

	@Override
	public final boolean equals(Object o) {
		if (o.getClass().equals(PresentNamedKey.class)) {
			PresentNamedKey k = (PresentNamedKey) o;
			if (!k.name.equals(name) || k.parameterCount() != parameterCount()) {
				return false;
			}
			return Arrays.deepEquals(((PresentKey) o).data.getConstr()
					.getParameterTypes(), data.getConstr().getParameterTypes());
		} else if (SearchKey.class.isAssignableFrom(o.getClass())) {
			return ((SearchKey) o).equals(this);
		}
		return false;
	}
}
