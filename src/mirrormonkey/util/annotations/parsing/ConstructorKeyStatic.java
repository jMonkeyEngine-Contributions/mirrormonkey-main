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
 * <tt>MemberKey</tt> that is used to map a <tt>ConstructorIR</tt> using static
 * binding.
 * 
 * When this key class is used for <tt>ConstructorIRs</tt>, then constructors
 * encountered further down in the class hierarchy will <b>not</b> override
 * other constructors, even if they have an equal list of parameter types. This
 * means that annotations will not be inherited between constructors with equal
 * parameter lists.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class ConstructorKeyStatic extends MemberKey {

	/**
	 * Statically bound constructors come after dynamically bound constructors
	 */
	public static final int STATIC_CONSTRUCTOR_ORDER = ConstructorKeyDynamic.DYNAMIC_CONSTR_ORDER + 10;

	/**
	 * The <tt>Class</tt> that declares the constructor mapped by this
	 * <tt>ConstructorKeyStatic</tt>.
	 */
	private final Class<?> declaringClass;

	/**
	 * Parameter types of the constructor mapped by this
	 * <tt>ConstructorKeyStatic</tt>.
	 */
	private final Class<?>[] paramTypes;

	/**
	 * Creates a new <tt>ConstructorKeyStatic</tt> matching the parameter types
	 * and declaring class of a given constructor.
	 * 
	 * @param c
	 *            this <tt>ConstructorKeyStatic</tt> will equal any other
	 *            <tt>ConstructorKeyStatic</tt> with equal declaring class and
	 *            parameter types
	 */
	public ConstructorKeyStatic(Constructor<?> c) {
		this(c.getDeclaringClass(), c.getParameterTypes());
	}

	/**
	 * Creates a new <tt>ConstructorKeyStatic</tt> that can be used to search
	 * for a constructor with given declaring class and parameter types in a
	 * class hierarchy.
	 * 
	 * @param declaringClass
	 *            the declaring class that this <tt>ConstructorKeyStatic</tt>
	 *            will match
	 * @param paramTypes
	 *            the parameter types that this <tt>ConstructorKeyStatic</tt>
	 *            will match
	 */
	public ConstructorKeyStatic(Class<?> declaringClass, Class<?>[] paramTypes) {
		this.declaringClass = declaringClass;
		this.paramTypes = paramTypes;
	}

	@Override
	public int getTypeOrder() {
		return STATIC_CONSTRUCTOR_ORDER;
	}

	@Override
	public int compareSameTypeOrder(MemberKey k) {
		final ConstructorKeyStatic o = (ConstructorKeyStatic) k;
		final int classes = compareClasses(declaringClass, o.declaringClass);
		return classes != 0 ? classes : compareParamTypes(paramTypes,
				o.paramTypes);
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new ConstructorKeyStatic(declaringClass, sig);
	}

}