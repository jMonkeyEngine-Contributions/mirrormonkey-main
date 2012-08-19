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

import java.lang.reflect.Field;

/**
 * <tt>MemberKey</tt> that is used to map a <tt>FieldIR</tt> using static
 * binding.
 * 
 * When this key class is used for <tt>FieldIRs</tt>, then field encountered
 * further down in the class hierarchy will <b>not</b> override other fields,
 * even if the have equal type and name. This means that annotations will not be
 * inherited between fields.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class FieldKeyStatic extends MemberKey {

	/**
	 * Statically bound fields come after statically bound fields, but before
	 * methods.
	 */
	public static final int STATIC_FIELD_ORDER = FieldKeyDynamic.DYNAMIC_FIELD_ORDER + 10;

	/**
	 * The <tt>Class</tt> that declares the field mapped by this
	 * <tt>FieldKeyStatic</tt>.
	 */
	private final Class<?> declaringClass;

	/**
	 * The name of the field mapped by this <tt>FieldKeyStatic</tt>.
	 */
	private final String name;

	/**
	 * The type of the field mapped by this <tt>FieldKeyStatic</tt>.
	 */
	private final Class<?> type;

	/**
	 * Creates a new <tt>FieldKeyStatic</tt> matching the name, type and
	 * declaring class of a given field.
	 * 
	 * @param field
	 *            this <tt>FieldKeyStatic</tt> will equals any other
	 *            <tt>FieldKeyStatic</tt> with equal name, type and declaring
	 *            class
	 */
	public FieldKeyStatic(Field field) {
		this(field.getDeclaringClass(), field.getName(), field.getType());
	}

	/**
	 * Creates a new <tt>ConstructorKeyDynamic</tt> that can be used to search
	 * for a field with given name, type and declaring class in a class
	 * hierarchy.
	 * 
	 * @param declaringClass
	 *            the declaring class that this <tt>FieldKeyStatic</tt> will
	 *            match
	 * @param name
	 *            the name that this <tt>FieldKeyStatic</tt> will match
	 * @param type
	 *            the type that this <tt>FieldKeyStatic</tt> will match
	 */
	public FieldKeyStatic(Class<?> declaringClass, String name, Class<?> type) {
		this.declaringClass = declaringClass;
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return "[FieldKeyStatic: name=" + name + ", declaringClass="
				+ declaringClass + ", type=" + type + "]";
	}

	/**
	 * @return the <tt>Class</tt> that declares the field mapped by this
	 *         <tt>FieldKeyStatic</tt>
	 */
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * @return the name of the field mapped by this <tt>FieldKeyStatic</tt>
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type of the field mapped by this <tt>FieldKeyStatic</tt>
	 */
	public Class<?> getType() {
		return type;
	}

	@Override
	public int getTypeOrder() {
		return STATIC_FIELD_ORDER;
	}

	@Override
	public int compareSameTypeOrder(MemberKey k) {
		FieldKeyStatic o = (FieldKeyStatic) k;
		int v;
		if ((v = name.compareTo(o.name)) != 0) {
			return v;
		}
		if ((v = compareClasses(declaringClass, o.declaringClass)) != 0) {
			return v;
		}
		return compareClasses(type, o.type);
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new FieldKeyStatic(declaringClass, name, sig[0]);
	}

}