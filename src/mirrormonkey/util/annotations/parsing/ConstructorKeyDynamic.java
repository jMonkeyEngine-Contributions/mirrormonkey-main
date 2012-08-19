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

package mirrormonkey.util.annotations.parsing;

import java.lang.reflect.Constructor;

/**
 * <tt>MemberKey</tt> that is used to map a <tt>ConstructorIR</tt> utilizing
 * dynamic binding.
 * 
 * When this key class is used for <tt>ConstructorIRs</tt>, then constructors
 * encountered further down in the class hierarchy will override other
 * constructors with the parameter types that were previously encountered. Every
 * annotation that was collected on previous constructors will be inherited
 * between matching constructors.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class ConstructorKeyDynamic extends MemberKey {

	/**
	 * Constructors always come first when looking at the collected data.
	 */
	public static final int DYNAMIC_CONSTR_ORDER = 0;

	/**
	 * Parameter types of the constructor mapped by this
	 * <tt>ConstructorKeyDynamic</tt>.
	 */
	private final Class<?>[] paramTypes;

	/**
	 * Creates a new <tt>ConstructorKeyDynamic</tt> matching the parameter types
	 * of a given constructor.
	 * 
	 * @param c
	 *            this <tt>ConstructorKeyDynamic</tt> will equal any other
	 *            <tt>ConstructorKeyDynamic</tt> with this
	 *            <tt>Constructor's</tt> parameter types
	 */
	public ConstructorKeyDynamic(Constructor<?> c) {
		this(c.getParameterTypes());
	}

	/**
	 * Creates a new <tt>ConstructorKeyDynamic</tt> that can be used to search
	 * for a constructor with given parameter types in a class hierarchy.
	 * 
	 * @param paramTypes
	 *            the parameter types that this <tt>ConstructorKeyDynamic</tt>
	 *            will match
	 */
	public ConstructorKeyDynamic(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public final int getTypeOrder() {
		return DYNAMIC_CONSTR_ORDER;
	}

	@Override
	public final int compareSameTypeOrder(MemberKey k) {
		return compareParamTypes(paramTypes,
				((ConstructorKeyDynamic) k).paramTypes);
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new ConstructorKeyDynamic(sig);
	}

}
