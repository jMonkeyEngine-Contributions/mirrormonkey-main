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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import mirrormonkey.util.annotations.control.DefinePreset;

/**
 * Main entry point to the <tt>mirrormonkey.util.annotations</tt> package.
 * Responsible for parsing class hierarchies for annotations and returning
 * temporary data structures (IRs) containing information about the class
 * hierarchy. The user can then extract runtime data from the IRs.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class AnnotationParser {

	/**
	 * Preset annotation types are mapped to the <tt>AnnotatedElement</tt> that
	 * are linked to them in this field.
	 */
	private final Map<Class<?>, AnnotatedElement> presets;

	/**
	 * Contains the key classes that will be used to register members in the
	 * <tt>ClassIR</tt> when parsing the hierarchy. Key classes are used to
	 * influence whether annotations are inherited and whether the members are
	 * bound statically or dynamically.
	 */
	private final Map<Class<? extends Member>, Set<Constructor<? extends MemberKey>>> memberKeyClasses;

	/**
	 * Contains the IR classes that are instantiated for a specific member once
	 * it is encountered for the first time.
	 */
	private final Map<Class<? extends AnnotatedElement>, Constructor<? extends AnnotationIR>> defaultIRClasses;

	/**
	 * Creates a new <tt>AnnotationParser</tt>
	 */
	public AnnotationParser() {
		presets = new HashMap<Class<?>, AnnotatedElement>();
		memberKeyClasses = new HashMap<Class<? extends Member>, Set<Constructor<? extends MemberKey>>>();
		defaultIRClasses = new HashMap<Class<? extends AnnotatedElement>, Constructor<? extends AnnotationIR>>();
		memberKeyClasses.put(Constructor.class,
				new LinkedHashSet<Constructor<? extends MemberKey>>());
		memberKeyClasses.put(Field.class,
				new LinkedHashSet<Constructor<? extends MemberKey>>());
		memberKeyClasses.put(Method.class,
				new LinkedHashSet<Constructor<? extends MemberKey>>());
		setClassIRClass(ClassIR.class);
		setMemberIRClass(Constructor.class, ConstructorIR.class);
		setMemberIRClass(Field.class, FieldIR.class);
		setMemberIRClass(Method.class, MethodIR.class);
		setParameterIRClass(ParameterIR.class);
	}

	/**
	 * Creates and returns the <tt>MemberKeys</tt> that a specific
	 * <tt>Member</tt> will be mapped to.
	 * 
	 * @param member
	 *            the member to create keys for
	 * @return array of newly created <tt>MemberKeys</tt> for <tt>member</tt>,
	 *         as specified by <tt>memberKeyClasses</tt>.
	 */
	public MemberKey[] createKeys(Member member) {
		try {
			Collection<Constructor<? extends MemberKey>> cc = memberKeyClasses
					.get(member.getClass());
			MemberKey[] v = new MemberKey[cc.size()];
			int i = 0;
			for (Constructor<? extends MemberKey> c : cc) {
				v[i++] = c.newInstance(member);
			}
			return v;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds a key class for a specific member class, so that a key of that class
	 * will be created and returned by <tt>createKeys</tt>.
	 * 
	 * @param memberClass
	 *            the class of the member to add the key class for
	 * @param keyClass
	 *            the key class to add for <tt>memberClass</tt>
	 */
	public void addKeyClass(Class<? extends Member> memberClass,
			Class<? extends MemberKey> keyClass) {
		try {
			memberKeyClasses.get(memberClass).add(
					keyClass.getDeclaredConstructor(memberClass));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Removes a key class for a specific member class, so that a key of that
	 * class will no longer be created and returned by <tt>createKeys</tt>.
	 * 
	 * @param memberClass
	 *            the member class to removed the mapped key class from
	 * @param keyClass
	 *            the key class to remove from the member class
	 */
	public void removeKeyClass(Class<? extends Member> memberClass,
			Class<? extends MemberKey> keyClass) {
		try {
			memberKeyClasses.get(memberClass).remove(
					keyClass.getDeclaredConstructor(memberClass));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Returns a constructor to create an IR of the default class registered for
	 * <tt>elementClass</tt>. Called when a member of <tt>elementClass</tt> has
	 * been encountered for the first time to create its default IR.
	 * 
	 * @param elementClass
	 *            class of the element to return a constructor for
	 * @return a <tt>Constructor</tt> that can be called to create the default
	 *         IR for an <tt>AnnotatedElement</tt> of class
	 *         <tt>elementClass</tt>
	 */
	public Constructor<? extends AnnotationIR> getIRConstr(Class<?> elementClass) {
		return defaultIRClasses.get(elementClass);
	}

	/**
	 * @param irClass
	 *            default IR that is created when starting to parse a class
	 */
	public void setClassIRClass(Class<? extends ClassIR> irClass) {
		try {
			defaultIRClasses.put(Class.class,
					irClass.getDeclaredConstructor(AnnotationParser.class));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @param irClass
	 *            default IR class that is used for method and constructor
	 *            parameters
	 */
	public void setParameterIRClass(Class<? extends ParameterIR> irClass) {
		try {
			defaultIRClasses.put(Parameter.class,
					irClass.getDeclaredConstructor(AnnotationParser.class));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @param memberClass
	 *            class of the members that should use <tt>irClass</tt> as
	 *            default IR class
	 * @param irClass
	 *            the IR class that should be used for default IRs for
	 *            <tt>memberTypes</tt>
	 */
	@SuppressWarnings("unchecked")
	public void setMemberIRClass(Class<? extends Member> memberClass,
			Class<? extends MemberIR> irClass) {
		try {
			defaultIRClasses.put(
					(Class<? extends AnnotatedElement>) memberClass, irClass
							.getDeclaredConstructor(ClassIR.class,
									MemberKey.class, memberClass));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Parses a class that contains preset annotation definitions. Every preset
	 * definition in <tt>clazz</tt> will be considered in future class hierarchy
	 * parsing. If <tt>clazz</tt> defines a preset annotation type that was also
	 * defined by a previously parsed preset class, the previous preset
	 * definition will be overridden.
	 * 
	 * @param clazz
	 *            the class containing the annotation preset type
	 */
	public void parsePresetClass(Class<?> clazz) {
		for (AnnotatedElement i : clazz.getDeclaredClasses()) {
			parsePresetElement(i);
		}
		for (AnnotatedElement i : clazz.getDeclaredConstructors()) {
			parsePresetElement(i);
		}
		for (AnnotatedElement i : clazz.getDeclaredMethods()) {
			parsePresetElement(i);
		}
		for (AnnotatedElement i : clazz.getDeclaredFields()) {
			parsePresetElement(i);
		}
	}

	/**
	 * Searches for a <tt>DefinePreset</tt> annotation on <tt>element</tt> and
	 * sets <tt>element</tt> as preset element for the preset anotation type
	 * defined by that annotation.
	 * 
	 * @param element
	 *            potential preset element
	 */
	private void parsePresetElement(AnnotatedElement element) {
		DefinePreset dp = element.getAnnotation(DefinePreset.class);
		if (dp != null) {
			presets.put(dp.value(), element);
		}
	}

	/**
	 * Recursively scans an <tt>AnnotatedElement</tt> for preset annotations.
	 * Adds every annotation defined by found preset members to <tt>putInto</tt>
	 * .
	 * 
	 * @param element
	 *            The <tt>AnnotatedElement</tt> to parse. This can be a preset
	 *            element that was found when searching for presets on a
	 *            previous element or an element that was encountered when
	 *            parsing a class hierarchy.
	 * @param putInto
	 *            <tt>Map</tt> to put every found annotation and preset
	 *            annotation into
	 */
	protected void getPresets(AnnotatedElement element,
			Map<Class<?>, Annotation> putInto) {
		for (Annotation i : element.getDeclaredAnnotations()) {
			boolean containedBefore = putInto.containsKey(i.annotationType());
			AnnotatedElement preset = presets.get(i.annotationType());
			if (!containedBefore && preset != null) {
				getPresets(preset, putInto);
			}
			putInto.put(i.annotationType(), i);
		}
		for (Annotation i : element.getDeclaredAnnotations()) {
			putInto.put(i.annotationType(), i);
		}
	}

	/**
	 * Parses a class and every superclass, interface and superinterface upwards
	 * in its hierarchy. Returns a <tt>ClassIR</tt> that runtime information
	 * about the class can be extracted from.
	 * 
	 * @param clazz
	 *            the class that should be parsed
	 * @return <tt>ClassIR</tt> containing runtime information about the class
	 *         hierarchy of <tt>clazz</tt>
	 */
	public ClassIR parseClass(Class<?> clazz) {
		try {
			ClassIR ir = (ClassIR) getIRConstr(Class.class).newInstance(this);

			Deque<Class<?>> implementationHierarchy = new LinkedList<Class<?>>();
			Deque<Class<?>> interfaces = new LinkedList<Class<?>>();
			for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
				implementationHierarchy.add(c);
				extractInterfaces(c.getInterfaces(), interfaces);
			}

			while (!interfaces.isEmpty()) {
				ir = ir.parseInterface(interfaces.pollLast());
			}

			while (!implementationHierarchy.isEmpty()) {
				ir = ir.parseClass(implementationHierarchy.pollLast());
			}

			return ir.cleanup();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e2) {
			throw new RuntimeException(e2);
		}
	}

	/**
	 * Recursively fetches every interface implemented by <tt>clazz</tt> and
	 * their superinterfaces and adds them to <tt>accumulator</tt>.
	 * 
	 * @param clazz
	 *            type or interface type to parse
	 * @param accumulator
	 *            will contain every interface or superinterface implemented or
	 *            extended by <tt>clazz</tt> for the first call of the recursion
	 *            after the recursion is done
	 */
	private void extractInterfaces(Class<?>[] clazz, Deque<Class<?>> accumulator) {
		for (Class<?> i : clazz) {
			extractInterfaces(i.getInterfaces(), accumulator);
			accumulator.add(i);
		}
	}

	/**
	 * Returns the number of keys that would be returned by <tt>createKeys</tt>
	 * for a member of class <tt>forClass</tt>. Used mainly for testing and
	 * debugging.
	 * 
	 * @param forClass
	 *            member class to count created keys for
	 * @return number of <tt>MemberKeys</tt> that are registered for member
	 *         class <tt>forClass</tt>
	 */
	public int getMemberClassTrackingCount(Class<? extends Member> forClass) {
		return memberKeyClasses.get(forClass).size();
	}

}
