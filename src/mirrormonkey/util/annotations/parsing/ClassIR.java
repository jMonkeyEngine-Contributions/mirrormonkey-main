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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Collects data about types in a class hierarchy.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClassIR extends AnnotationIR {

	/**
	 * Contains every class that was encountered when parsing the hierarchy.
	 */
	protected final Deque<Class<?>> topDownParsedClasses;

	/**
	 * Contains every interface and their superinterfaces encountered when
	 * parsing the hierarchy.
	 */
	protected final Deque<Class<?>> topDownParsedInterfaces;

	/**
	 * Contains information about every encountered <tt>Member</tt> from the
	 * hierarchy, mapped by their <tt>MemberKeys</tt> as defined in the
	 * responsible <tt>AnnotationParser</tt>. A <tt>MemberIR</tt> can be mapped
	 * by multiple keys.
	 * 
	 * All <tt>MemberIRs</tt> are sorted by the order implied by their
	 * respective <tt>MemberKeys</tt>.
	 */
	private final SortedMap<MemberKey, MemberIR> memberIRs;

	/**
	 * Creates a new <tt>ClassIR</tt>, most likely before the top-most class in
	 * a hierarchy is parsed.
	 * 
	 * @param parser
	 *            <tt>AnnotationParser</tt> that is responsible for parsing the
	 *            hierarchy
	 */
	protected ClassIR(AnnotationParser parser) {
		super(parser);
		topDownParsedClasses = new LinkedList<Class<?>>();
		topDownParsedInterfaces = new LinkedList<Class<?>>();
		memberIRs = new TreeMap<MemberKey, MemberIR>();
	}

	/**
	 * Called when a member of the class hierarchy requests a different
	 * <tt>ClassIR</tt> to be created for the hierarchy.
	 * 
	 * @param previous
	 *            <tt>ClassIR</tt> that was previously used to track information
	 *            about the parsed class hierarchy.
	 */
	protected ClassIR(ClassIR previous) {
		super(previous);
		topDownParsedClasses = previous.topDownParsedClasses;
		topDownParsedInterfaces = previous.topDownParsedInterfaces;
		memberIRs = previous.getMemberIRs();
	}

	/**
	 * Parses an interface from the parsed class hierarchy. Changes this
	 * <tt>IR</tt> if necessary and parses every method declared by the
	 * interface.
	 * 
	 * @param interfaceType
	 *            interface type to parse
	 * @return <tt>ClassIR</tt> that is responsible for the parsed class
	 *         hierarchy with respect to the control annotations declared on
	 *         <tt>interfaceType</tt>
	 */
	protected ClassIR parseInterface(Class<?> interfaceType) {
		topDownParsedInterfaces.addLast(interfaceType);
		ClassIR newIR = (ClassIR) super.parseAnnotatedElement(interfaceType,
				true);
		for (Method m : interfaceType.getDeclaredMethods()) {
			newIR.parseInterfaceMethod(m);
		}
		return newIR;
	}

	/**
	 * Parses a class from the parsed class hierarchy. Change this <tt>IR</tt>
	 * if necessary and parses every member declared in the class.
	 * 
	 * @param clazz
	 *            class to parse
	 * @return <tt>ClassIR</tt> that is responsible for the parsed class
	 *         hierarchy with respect to the control annotations declared on
	 *         <tt>interfaceType</tt>
	 */
	protected ClassIR parseClass(Class<?> clazz) {
		topDownParsedClasses.addLast(clazz);
		ClassIR newIR = (ClassIR) super.parseAnnotatedElement(clazz, true);
		for (Constructor<?> i : clazz.getDeclaredConstructors()) {
			newIR.parseConstructor(i);
		}
		for (Field i : clazz.getDeclaredFields()) {
			newIR.parseField(i);
		}
		for (Method m : clazz.getDeclaredMethods()) {
			newIR.parseMethod(m);
		}
		return newIR;
	}

	/**
	 * Default parsing algorithm for any <tt>Member</tt> encountered in any
	 * class or interface in the parsed class hierarchy.
	 * 
	 * Calls the parser to create default keys for <tt>member</tt>, looks up the
	 * <tt>MemberIR</tt> responsible for the member by every key or creates a
	 * new one if no IR previously existed. Parses and collects control
	 * annotations on the member and collects regular annotations.
	 * 
	 * @param member
	 *            the <tt>Member</tt> to parse
	 */
	protected void parseMember(Member member) {
		try {
			MemberKey[] keys = getParser().createKeys(member);
			for (MemberKey key : keys) {
				MemberIR ir = getMemberIRs().get(key);
				if (ir == null) {
					ir = (MemberIR) getParser().getIRConstr(member.getClass())
							.newInstance(this, key, member);
					ir.register();
				}
				ir.update(member);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parses a method that was declared by an interface in the class hierarchy.
	 * 
	 * @param m
	 *            <tt>Method</tt> to parse
	 */
	protected void parseInterfaceMethod(Method m) {
		parseMember(m);
	}

	/**
	 * Parses a constructor that was declared by a class in the class hierarchy.
	 * 
	 * @param c
	 *            <tt>Constructor</tt> to parse
	 */
	protected void parseConstructor(Constructor<?> c) {
		parseMember(c);
	}

	/**
	 * Parses a field that was declared by a class in the class hierarchy.
	 * 
	 * @param f
	 *            <tt>Field</tt> to parse
	 */
	protected void parseField(Field f) {
		parseMember(f);
	}

	/**
	 * Parses a method that was declared by a class in the class hierarchy.
	 * 
	 * @param m
	 *            <tt>Method</tt> to parse
	 */
	protected void parseMethod(Method m) {
		parseMember(m);
	}

	/**
	 * Called by the <tt>AnnotationParser</tt> after the whole class hierarchy
	 * has been parsed and before this <tt>ClassIR</tt> is returned.
	 * 
	 * @return the <tt>ClassIR</tt> that is responsible the class hierarchy
	 */
	public ClassIR cleanup() {
		return this;
	}

	@Override
	public AnnotationIR overrideToDefault() {
		clear();
		return new ClassIR(this);
	}

	/**
	 * @return A <tt>SortedMap</tt> containing all members that were so far
	 *         encountered when parsing the class hierarchy. A <tt>MemberIR</tt>
	 *         can be mapped by multiple keys.
	 */
	public SortedMap<MemberKey, MemberIR> getMemberIRs() {
		return memberIRs;
	}

}
