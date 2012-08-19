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
 * <tt>MemberKey</tt> that is used to map a <tt>MethodIR</tt> utilizing dynamic
 * binding.
 * 
 * When this key class is used for <tt>MethodIRs</tt>, then methods encountered
 * further down in the class hierarchy will override other methods with equal
 * name and parameter types. Every annotations that was collected on previous
 * methods will be inherited between matching methods.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class MethodKeyDynamic extends MemberKey {

	/**
	 * Dynamically bound methods come after fields and before statically bound
	 * methods.
	 */
	public static final int DYNAMIC_METHOD_ORDER = FieldKeyStatic.STATIC_FIELD_ORDER + 10;

	/**
	 * Name of the method mapped by this <tt>MethodKeyDynamic</tt>.
	 */
	private final String name;

	/**
	 * Parameter types of the method mapped by this <tt>MethodKeyDynamic</tt>.
	 */
	private final Class<?>[] paramTypes;

	/**
	 * Return type of the method mapped by this <tt>MethodKeyDynamic</tt>. This
	 * is <b>not</b> used for comparison.
	 */
	private final Class<?> returnType;

	/**
	 * Creates a new <tt>MethodKeyDynamic</tt> matching the name and parameter
	 * types of a given method.
	 * 
	 * @param method
	 *            this <tt>MethodKeyDynamic</tt> will equal any other
	 *            <tt>MethodKeyDynamic</tt> with this <tt>Method's</tt> name and
	 *            parameter types
	 */
	public MethodKeyDynamic(Method method) {
		this(method.getName(), method.getParameterTypes(), method
				.getReturnType());
	}

	/**
	 * Creates a new <tt>MethodKeyDynamic</tt> that can be used to search for a
	 * method with given parameter types in a class hierarchy.
	 * 
	 * @param name
	 *            the name that this <tt>MethodKeyDynamic</tt> will match
	 * @param paramTypes
	 *            the parameter types that this <tt>MethodKeyDynamic</tt> will
	 *            match
	 * @param returnType
	 *            the return type of the method mapped by this
	 *            <tt>MethodKeyDynamic</tt> (if any method exists). This is
	 *            <b>not</b> used for any comparison.
	 */
	public MethodKeyDynamic(String name, Class<?>[] paramTypes,
			Class<?> returnType) {
		this.name = name;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	@Override
	public int getTypeOrder() {
		return DYNAMIC_METHOD_ORDER;
	}

	@Override
	public int compareSameTypeOrder(MemberKey k) {
		MethodKeyDynamic o = (MethodKeyDynamic) k;
		return compareSignature(name, paramTypes, o.name, o.paramTypes);
	}

	@Override
	public String toString() {
		String s = "[MethodKeyDynamic name=" + name + "; paramTypes:";
		for (Class<?> i : paramTypes) {
			s += ", " + i;
		}
		s += "; returnType (unchecked) : " + returnType;
		s += "]";
		return s;
	}

	/**
	 * @return the name that this <tt>MethodKeyDynamic</tt> will match
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parameter types that this <tt>MethodKeyDynamic</tt> will
	 *         match
	 */
	public Class<?>[] getParameterTypes() {
		return paramTypes;
	}

	/**
	 * @return the return type of the method that this <tt>MethodKeyDynamic</tt>
	 *         maps in the <tt>ClassIR</tt> for the currently parsed hierarchy
	 */
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new MethodKeyDynamic(name, sig, returnType);
	}
}