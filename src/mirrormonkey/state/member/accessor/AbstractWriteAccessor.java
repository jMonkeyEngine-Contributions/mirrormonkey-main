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

package mirrormonkey.state.member.accessor;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mirrormonkey.framework.entity.SyncEntity;

/**
 * Common superclass for write access to a synchronized field or virtual field.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <M>
 *            type of the member that is used to write the field's value
 */
public abstract class AbstractWriteAccessor<M extends Member> implements
		ValueWriteAccessor {

	/**
	 * Warning messages go here.
	 */
	public static final Logger LOGGER = Logger
			.getLogger(AbstractWriteAccessor.class.getName());

	/**
	 * Contains null values for primitive synchronized fields.
	 */
	public static final Map<Class<?>, Object> PRIMITIVES_TO_NULL = new HashMap<Class<?>, Object>() {
		private static final long serialVersionUID = 1L;

		{
			put(boolean.class, false);
			put(byte.class, (byte) 0);
			put(short.class, (short) 0);
			put(char.class, (char) 0);
			put(int.class, 0);
			put(long.class, (long) 0);
			put(float.class, (float) 0);
			put(double.class, (double) 0);

		}
	};

	/**
	 * Maps primitive types to their corresponding object types.
	 */
	public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_REFS = new HashMap<Class<?>, Class<?>>() {
		private static final long serialVersionUID = 1L;

		{
			put(boolean.class, Boolean.class);
			put(byte.class, Byte.class);
			put(short.class, Short.class);
			put(char.class, Character.class);
			put(int.class, Integer.class);
			put(long.class, Long.class);
			put(float.class, Float.class);
			put(double.class, Double.class);
		}
	};

	/**
	 * Type of the field.
	 */
	protected final Class<?> type;

	/**
	 * Member to use when writing values.
	 */
	protected final M member;

	/**
	 * Value that must be written if <tt>null</tt> is received.
	 */
	protected final Object nullValue;

	/**
	 * Creates a new <tt>AbstractWriteAccessor</tt> for a given field type and
	 * member.
	 * 
	 * @param type
	 *            type of the field that will be written
	 * @param member
	 *            member that will be used to write values
	 */
	public AbstractWriteAccessor(Class<?> type, M member) {
		if (PRIMITIVES_TO_REFS.containsKey(type)) {
			this.type = PRIMITIVES_TO_REFS.get(type);
		} else {
			this.type = type;
		}
		this.member = member;
		nullValue = PRIMITIVES_TO_NULL.get(type);
	}

	/**
	 * Actually sets the value...
	 * 
	 * @param entity
	 *            ...on this entity
	 * @param value
	 *            ...to this
	 * @throws Throwable
	 *             sometimes
	 */
	public abstract void write(SyncEntity entity, Object value)
			throws Throwable;

	public final void writeValue(SyncEntity entity, Object value) {
		Object realValue = value;
		if (!member.getDeclaringClass().isInstance(entity)) {
			LOGGER.info("Received update message for " + entity
					+ " but have unavailable member " + member);
			return;
		}
		if (value != null && !type.isInstance(value)) {
			LOGGER.info("Tried to set value for\n  " + member + " to\n  "
					+ value + "\nfrom remote, but its type " + value.getClass()
					+ " was " + "not assignment compatible with my type "
					+ type);
			realValue = nullValue;
		}
		try {
			write(entity, realValue);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
