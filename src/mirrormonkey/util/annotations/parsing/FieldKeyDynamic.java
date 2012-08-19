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
 * <tt>MemberKey</tt> that is used to map a <tt>FieldIR</tt> utilizing dynamic
 * binding.
 * 
 * When this key class is used for <tt>FieldIRs</tt>, then fields encountered
 * further down in the class hierarchy will override other fields with equals
 * type and name that were previously encountered. Every annotation that was
 * previously collected will be inherited between matching fields.
 * 
 * Please note that using this key class for fields will not disable overridden
 * fields at runtime and all assignments in your code will still use java's
 * default static binding.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class FieldKeyDynamic extends MemberKey {

	/**
	 * Dynamically bound fields come after constructors and before statically
	 * bound fields.
	 */
	public static final int DYNAMIC_FIELD_ORDER = ConstructorKeyStatic.STATIC_CONSTRUCTOR_ORDER + 10;

	/**
	 * Name of the field mapped by this <tt>FieldKeyDynamic</tt>.
	 */
	private final String name;

	/**
	 * Type of the field mapped by this <tt>FieldKeyDynamic</tt>.
	 */
	private final Class<?> type;

	/**
	 * Creates a new <tt>FieldKeyDynamic</tt> matching the name and type of a
	 * given field.
	 * 
	 * @param f
	 *            this <tt>FieldKeyDynamic</tt> will equals any other
	 *            <tt>FieldKeyDynamic</tt> with this <tt>Field's</tt> type and
	 *            name
	 */
	public FieldKeyDynamic(Field f) {
		this(f.getName(), f.getType());
	}

	/**
	 * Creates a new <tt>FieldKeyDynamic</tt> that can be used to search for a
	 * field with given name and type in the class hierarchy.
	 * 
	 * @param name
	 *            the name that this <tt>FieldKeyDynamic</tt> will match
	 * @param type
	 *            the type that this <tt>FieldKeyDynamic</tt> will match
	 */
	public FieldKeyDynamic(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public int getTypeOrder() {
		return DYNAMIC_FIELD_ORDER;
	}

	@Override
	public int compareSameTypeOrder(MemberKey k) {
		FieldKeyDynamic o = (FieldKeyDynamic) k;
		int v = name.compareTo(o.name);
		return v != 0 ? v : compareClasses(type, o.type);
	}

	@Override
	public MemberKey copyForSignature(Class<?>[] sig) {
		return new FieldKeyDynamic(name, sig[0]);
	}

}
