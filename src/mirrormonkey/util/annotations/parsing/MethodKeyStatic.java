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

import java.lang.reflect.Method;

/**
 * <tt>MemberKey</tt> that is used to map a <tt>MethodIR</tt> using static
 * binding.
 * 
 * When this key class is used for <tt>MethodIR</tt>, then methods encountered
 * further down in the class hierarchy will <b>not</b> override other methods,
 * even if they have equals name and parameter list. This means that annotations
 * will not be inherited between methods with equals name and parameter lists.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class MethodKeyStatic extends MemberKey {

	/**
	 * Statically bound methods come after dynamically bound methods
	 */
	public static final int STATIC_METHOD_ORDER = MethodKeyDynamic.DYNAMIC_METHOD_ORDER + 10;

	/**
	 * The <tt>Class</tt> that declares the method mapped by this
	 * <tt>MethodKeyStatic</tt>.
	 */
	private final Class<?> declaringClass;

	/**
	 * The name of the method mapped by this <tt>MethodKeyStatic</tt>.
	 */
	private final String name;

	/**
	 * Parameter types of the method mapped by this <tt>MethodKeyStatic</tt>.
	 */
	private final Class<?>[] paramTypes;

	/**
	 * Return type of the method mapped by this <tt>MethodKeyStatic</tt>. This
	 * is <b>not</b> used for comparison with other keys.
	 */
	private final Class<?> returnType;

	/**
	 * Creates a new <tt>MethodKeyStatic</tt> matching the name, parameter types
	 * and declaring class of a given method.
	 * 
	 * @param m
	 *            this <tt>MethodKeyStatic</tt> will equal any other
	 *            <tt>MethodKeyStatic</tt> with equal name, parameter types and
	 *            declaring class
	 */
	public MethodKeyStatic(Method m) {
		this(m.getDeclaringClass(), m.getName(), m.getParameterTypes(), m
				.getReturnType());
	}

	/**
	 * Creates a new <tt>MethodKeyStatic</tt> that can be used to search for a
	 * constructor with given name, parameter types and declaring class in a
	 * class hierarchy.
	 * 
	 * @param declaringClass
	 *            the declaring class that this <tt>MethodKeyStatic</tt> should
	 *            match
	 * @param name
	 *            the name that this <tt>MethodKeyStatic</tt> should match
	 * @param paramTypes
	 *            the parameter types that this <tt>MethodKeyStatic</tt> should
	 *            match
	 * @param returnType
	 *            the return type of the method that this
	 *            <tt>MethodKeyStatic</tt> was created for. This is <b>not</b>
	 *            used for comparison between keys.
	 */
	public MethodKeyStatic(Class<?> declaringClass, String name,
			Class<?>[] paramTypes, Class<?> returnType) {
		this.declaringClass = declaringClass;
		this.name = name;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	@Override
	public int getTypeOrder() {
		return STATIC_METHOD_ORDER;
	}

	@Override
	public int compareSameTypeOrder(MemberKey k) {
		MethodKeyStatic o = (MethodKeyStatic) k;
		int v;
		if ((v = compareSignature(name, paramTypes, o.name, o.paramTypes)) != 0) {
			return v;
		}
		return compareClasses(declaringClass, o.declaringClass);
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new MethodKeyStatic(declaringClass, name, sig, returnType);
	}

}
