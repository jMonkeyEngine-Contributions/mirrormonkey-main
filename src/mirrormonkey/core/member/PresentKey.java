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

import mirrormonkey.framework.member.MemberDataKey;

/**
 * Used to map constructors that can be called on the client side and use either
 * exact or assignment compatible parameter matching.
 * 
 * A <tt>PresentKey</tt> will delegate any call to <tt>equals</tt> to the call
 * parameter if the call parameter is a <tt>SerarchKey</tt>, so by exchanging
 * the class of the search key for constructor lookups, the matching algorithm
 * can be replaced.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class PresentKey implements MemberDataKey {

	/**
	 * The value that this <tt>PresentKey</tt> is bound to in the static member
	 * data map in <tt>StaticEntityData</tt>.
	 */
	protected final ConstructorData data;

	/**
	 * Creates a new <tt>PresentKey</tt> that will be used to match constructor
	 * lookups against a given <tt>ConstructorData</tt>.
	 * 
	 * @param data
	 *            the <tt>ConstructorData</tt> instance representing the
	 *            constructor that this <tt>PresentKey</tt> matches against
	 */
	public PresentKey(ConstructorData data) {
		this.data = data;
	}

	/**
	 * @return the number of parameters that the represented constructor accepts
	 */
	public int parameterCount() {
		return data.getInterpreters().length;
	}

	@Override
	public final int hashCode() {
		return parameterCount() * PresentKey.class.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass().equals(PresentKey.class)) {
			return Arrays.deepEquals(((PresentKey) o).data.getConstr()
					.getParameterTypes(), data.getConstr().getParameterTypes());
		} else if (SearchKey.class.isAssignableFrom(o.getClass())) {
			return ((SearchKey) o).equals(this);
		}
		return false;
	}
}