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

package mirrormonkey.state.member;

/**
 * Utility class containing methods to work with getter and setter names.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class BeanUtil {

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private BeanUtil() {
	}

	/**
	 * Checks whether a given string is a setter name according to java
	 * convention.
	 * 
	 * @param name
	 *            name to check for setter quality
	 * @return <tt>true</tt>, if <tt>name</tt> is a conventional setter name,
	 *         <tt>false</tt> if it isn't
	 */
	public static final boolean isSetterName(String name) {
		return name.startsWith("set") && name.length() > 3;
	}

	/**
	 * Checks whether a given string is a getter name according to java
	 * convention.
	 * 
	 * @param name
	 *            name to check for getter quality
	 * @return <tt>true</tt>, if <tt>name</tt> is a conventional getter name,
	 *         <tt>false</tt> if it isn't
	 */
	public static final boolean isGetterName(String name) {
		return name.startsWith("get") && name.length() > 3;
	}

	/**
	 * Creates a conventional getter name for a field name.
	 * 
	 * @param name
	 *            name of a field
	 * @return conventional name that a getter reading a field with name
	 *         <tt>name</tt> would have per convention
	 */
	public static final String getterName(String name) {
		String firstChar = name.substring(0, 1);
		String rest = name.substring(1);
		return "get" + firstChar.toUpperCase() + rest;
	}

	/**
	 * Creates a conventional setter name for a field name.
	 * 
	 * @param name
	 *            name of a field
	 * @return conventional name that a setter writing a field with name
	 *         <tt>name</tt> would have per convention
	 */
	public static final String setterName(String name) {
		String firstChar = name.substring(0, 1);
		String rest = name.substring(1);
		return "set" + firstChar.toUpperCase() + rest;
	}

	/**
	 * Creates field name for a conventional setter or getter name.
	 * 
	 * @param name
	 *            name of a getter or setter
	 * @return name of the field that a getter or setter with name <tt>name</tt>
	 *         would access
	 */
	public static final String cutPrefix(String name) {
		String rest = name.substring(3);
		return rest.substring(0, 1).toLowerCase()
				+ rest.substring(1, rest.length());
	}

}
