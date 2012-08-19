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

package mirrormonkey.framework.parameter;

import com.jme3.network.MessageConnection;

/**
 * Utility class containing convenience methods that perform multiple calls to
 * the classes in this package on arrays of values.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class ValueUtil {

	/**
	 * Private constructor because this is a utility class.
	 */
	private ValueUtil() {
	}

	/**
	 * Extracts an array of values using an array of <tt>ValueInterpreters</tt>
	 * of equal length. Each value will be extracted by the
	 * <tt>ValueInterpreter</tt> with equal index in the respective arrays.
	 * 
	 * @param interpreters
	 *            the array of <tt>ValueInterpreters</tt> to use when extracting
	 *            the values
	 * @param data
	 *            the array of values to extract
	 * @return an array of equal length containing the result of
	 *         <tt>interpreters[i].extractData(data[i])</tt> on each position
	 *         <tt>i</tt>
	 */
	public static final Object[] extractData(ValueInterpreter[] interpreters,
			Object[] data) {
		Object[] extracted = new Object[data.length];
		for (int i = 0; i < interpreters.length; i++) {
			extracted[i] = interpreters[i].extractData(data[i]);
		}
		return extracted;
	}

	/**
	 * Packs an array of values using an array of <tt>ValueInterpreters</tt> of
	 * equal length. Each value will be packed by the <tt>ValueInterpreter</tt>
	 * with equal index in the respective arrays.
	 * 
	 * @param interpreters
	 *            the array of <tt>ValueInterpreters</tt> to use when packing
	 *            the values
	 * @param data
	 *            the array of values to pack
	 * @return an array of equal length containing the result of
	 *         <tt>interpreters[i].packData(data[i])</tt> on each position
	 *         <tt>i</tt>
	 */
	public static final Object[] packData(ValueInterpreter[] interpreters,
			Object[] data) {
		Object[] packed = new Object[data.length];
		for (int i = 0; i < interpreters.length; i++) {
			packed[i] = interpreters[i].packData(data[i]);
		}
		return packed;
	}

	/**
	 * Calls <tt>isAssignableFrom</tt> for an array of values,
	 * <tt>ValueInterpreters</tt> and expected classes.
	 * 
	 * @param connection
	 *            connection to pass to calls
	 * @param expectedClasses
	 *            array of expected class parameter for every call
	 * @param parameters
	 *            array of values for every call
	 * @param interpreters
	 *            array of <tt>ValueInterpreters</tt> for every call
	 * @return <tt>true</tt> if every <tt>ValueInterpreter</tt> returns
	 *         <tt>true</tt> for a call of <tt>isAssignableFrom</tt> for the
	 *         value with equal index in its respective array, <tt>false</tt>
	 *         otherwise
	 */
	public static final boolean assignableFrom(MessageConnection connection,
			Class<?>[] expectedClasses, Object[] parameters,
			ValueInterpreter[] interpreters) {
		if (interpreters.length != parameters.length) {
			return false;
		}
		for (int i = 0; i < interpreters.length; i++) {
			if (!interpreters[i].isAssignableFrom(expectedClasses[i],
					connection, parameters[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Calls <tt>isExactMatch</tt> for an array of values,
	 * <tt>ValueInterpreters</tt> and expected classes.
	 * 
	 * @param connection
	 *            connection to pass to all calls
	 * @param expectedClasses
	 *            array of expected class parameter for every call
	 * @param parameters
	 *            array of values for every call
	 * @param interpreters
	 *            array of <tt>ValueInterpreters</tt> for every call
	 * @return <tt>true</tt> if every <tt>ValueInterpreter</tt> returns
	 *         <tt>true</tt> for a call of <tt>isExactMatch</tt> for the value
	 *         with equal index in its respective array, <tt>false</tt>
	 *         otherwise
	 */
	public static final boolean exactMatch(MessageConnection connection,
			Class<?>[] expectedClasses, Object[] parameters,
			ValueInterpreter[] interpreters) {
		if (interpreters.length != parameters.length) {
			return false;
		}
		for (int i = 0; i < interpreters.length; i++) {
			if (!interpreters[i].isExactMatch(expectedClasses[i], connection,
					parameters[i])) {
				return false;
			}
		}
		return true;
	}
}
