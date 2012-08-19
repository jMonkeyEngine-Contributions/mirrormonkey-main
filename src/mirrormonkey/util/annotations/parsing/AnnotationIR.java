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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import mirrormonkey.util.annotations.control.AnnotationOverride;
import mirrormonkey.util.annotations.control.IRClass;

/**
 * Collects annotations from <tt>AnnotatedElements</tt>. Which annotation types
 * are collected can be configured.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class AnnotationIR {

	/**
	 * Keep track of the parser because it supplies presets.
	 */
	protected final AnnotationParser parser;

	/**
	 * Collect these annotation types.
	 */
	protected final Collection<Class<? extends Annotation>> collectTypes;

	/**
	 * Annotations for each type from <tt>collectTypes</tt> are stored here
	 */
	protected final Map<Class<?>, Annotation> collectedAnnotations;

	/**
	 * Annotations present on the currently parsed <tt>AnnotatedElement</tt> are
	 * stored here to enable carrying them when the <tt>AnnotationIR</tt>
	 * changes when parsing that member.
	 */
	public final Map<Class<?>, Annotation> currentCollection;

	/**
	 * Creates a new <tt>AnnotationIR</tt> that will look for presets in the
	 * specified <tt>AnnotationParser</tt>. After creating the
	 * <tt>AnnotationIR</tt>, the user still has to specify which annotation
	 * types it should collect.
	 * 
	 * @param parser
	 *            the <tt>AnnotationParser</tt> to get presets from
	 */
	public AnnotationIR(AnnotationParser parser) {
		this.parser = parser;
		collectTypes = new HashSet<Class<? extends Annotation>>();
		collectedAnnotations = new HashMap<Class<?>, Annotation>();
		currentCollection = new HashMap<Class<?>, Annotation>();
	}

	/**
	 * Creates a new <tt>AnnotationIR</tt> with parser, collected types and
	 * collected annotations from a previous one.
	 * 
	 * @param previous
	 *            <tt>AnnotationIR</tt> that is to be replaced by this one
	 */
	public AnnotationIR(AnnotationIR previous) {
		this(previous.parser);
		loadFromPrevious(previous);
	}

	/**
	 * Copies the state of a previous <tt>AnnotationIR</tt> and sets it as
	 * current state of this <tt>AnnotationIR</tt>.
	 * 
	 * @param previous
	 *            the <tt>AnnotationIR</tt> to copy the state of
	 */
	protected void loadFromPrevious(AnnotationIR previous) {
		collectTypes.addAll(previous.collectTypes);
		collectedAnnotations.putAll(previous.collectedAnnotations);
		currentCollection.putAll(previous.currentCollection);
	}

	/**
	 * Clears all collected annotations and the settings of which types to
	 * collect. Does <b>not</tt> clear the annotations from the currently parsed
	 * <tt>AnnotatedElement</tt>.
	 */
	protected void clear() {
		getCollectedAnnotations().clear();
		getCollectTypes().clear();
	}

	/**
	 * When encountering an annotation with type <tt>type</tt>, it will be
	 * collected.
	 * 
	 * @param type
	 *            type of annotation to start collecting
	 */
	public void addCollectType(Class<? extends Annotation> type) {
		getCollectTypes().add(type);
	}

	/**
	 * When encountering an annotation with type <tt>type</tt>, it will no
	 * longer be collected.
	 * 
	 * @param type
	 *            type of annotations to stop collecting. Will not remove
	 *            annotations of that type that were collected so far.
	 */
	public void removeCollectType(Class<?> type) {
		getCollectTypes().remove(type);
	}

	/**
	 * Removes the previously collected annotation with type <tt>type</tt> (if
	 * any).
	 * 
	 * @param type
	 *            type of annotations to remove. This will not stop this
	 *            <tt>AnnotationIR</tt> to collect future annotations with that
	 *            type.
	 */
	public void removeCollectedAnnotation(Class<?> type) {
		getCollectedAnnotations().remove(type);
	}

	/**
	 * Collects all annotations from an <tt>AnnotatedElement</tt>. Additionally,
	 * the <tt>AnnotatedElement</tt> is checked for preset annotations and, if
	 * any are found, the annotations of their preset membersById are also
	 * collected.
	 * 
	 * This method only collects annotations of types that have been previously
	 * added to <tt>collectTypes</tt>.
	 * 
	 * @param element
	 *            the <tt>AnnotatedElement</tt> to collect annotations from
	 * @param control
	 *            If this is set to <tt>true</tt>, then control annotations
	 *            which may change the IR will be regarded. Otherwise, they
	 *            won't.
	 * @return The <tt>AnnotationIR</tt> that is responsible for
	 *         <tt>element</tt> after parsing its control annotations. This may
	 *         be <tt>this</tt> or a newly instantiated <tt>AnnotationIR</tt>,
	 *         the latter if <tt>control</tt> is set to <tt>true</tt> and
	 *         <tt>element</tt> contained relevant control annotations.
	 */
	public AnnotationIR parseAnnotatedElement(AnnotatedElement element,
			boolean control) {
		currentCollection.clear();
		getParser().getPresets(element, currentCollection);
		AnnotationIR newIR = this;
		if (control) {
			newIR = parseControlAnnotations();
		}
		newIR.parseCollectAnnotations();
		return newIR;
	}

	/**
	 * Parses control annotations as defined in the package
	 * <tt>mirrormonkey.util.annotations.control</tt> and changes the IR
	 * accordingly.
	 * 
	 * @return The <tt>AnnotationIR</tt> that is responsible for the currently
	 *         parsed <tt>AnnotatedElement</tt> after parsing its control
	 *         annotations. This may be <tt>this</tt> or a newly instantiated
	 *         <tt>AnnotationIR</tt>, the latter if <tt>control</tt> is set to
	 *         <tt>true</tt> and <tt>element</tt> contained relevant control
	 *         annotations.
	 */
	protected AnnotationIR parseControlAnnotations() {
		AnnotationIR newIR = this;
		AnnotationOverride ao = (AnnotationOverride) currentCollection
				.get(AnnotationOverride.class);
		if (ao != null) {
			newIR = overrideToDefault();
		}

		IRClass irc = (IRClass) newIR.currentCollection.get(IRClass.class);
		if (irc != null) {
			if (!irc.value().equals(IRClass.NoChange.class)) {
				newIR = reflectiveConstr(irc.value());
			}
			newIR.parseIRClass(irc);
		}

		return newIR;
	}

	/**
	 * Collects annotations according to the currently set collected types from
	 * the currently parsed <tt>AnnotatedElement</tt> and adds them to this
	 * <tt>AnnotationIR</tt>.
	 */
	protected void parseCollectAnnotations() {
		for (Entry<Class<?>, Annotation> e : currentCollection.entrySet()) {
			if (getCollectTypes().contains(e.getKey())) {
				getCollectedAnnotations().put(e.getKey(), e.getValue());
			}
		}
	}

	/**
	 * Uses an IRClass to determine which annotation classes to start or stop
	 * collecting or to drop entirely.
	 * 
	 * @param irClass
	 *            control annotation that was discovered from the element that
	 *            this <tt>AnnotationIR</tt> keeps track of
	 */
	@SuppressWarnings("unchecked")
	private void parseIRClass(IRClass irClass) {
		for (Class<?> c : irClass.stopCollect()) {
			removeCollectType(c);
		}
		for (Class<?> c : irClass.removeCompletely()) {
			removeCollectedAnnotation(c);
		}
		for (Class<?> c : irClass.startCollect()) {
			addCollectType((Class<? extends Annotation>) c);
		}
	}

	/**
	 * Instantiates and returns a new <tt>AnnotationIR</tt> of class
	 * <tt>clazz</tt>.
	 * 
	 * @param clazz
	 *            class of the newly created <tt>AnnotationIR</tt>
	 * @return a newly created <tt>AnnotationIR</tt> of type <tt>clazz</tt>
	 */
	private AnnotationIR reflectiveConstr(Class<?> clazz) {
		Constructor<?> constr;
		if ((constr = findConstructor(false, clazz)) == null) {
			throw new IllegalArgumentException(
					"Could not find constructor for this class (" + getClass()
							+ ") in class " + clazz);
		}
		try {
			return (AnnotationIR) constr.newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the distance between two classes (<b>not</b> interfaces or
	 * classes and interfaces) in the class hierarchy.
	 * 
	 * @param from
	 *            a <tt>Class</tt> that is assignment compatible to <tt>to</tt>
	 * @param to
	 *            a <tt>Class</tt> that <tt>from</tt> is assignment compatible
	 *            from</tt>
	 * @return 0, if <tt>from</tt> equals </tt> to. In any other case, the
	 *         number of classes in between <tt>from</tt> and <tt>to</tt> in the
	 *         class hierarchy.
	 */
	private int dist(Class<?> from, Class<?> to) {
		int val = 0;
		Class<?> c = from;
		while (!c.equals(to)) {
			val++;
			c = c.getSuperclass();
		}
		return val;
	}

	/**
	 * Searches the constructor for <tt>irClass</tt> that is most appropriate
	 * for creating a new instance of <tt>irClass</tt> from this IR.
	 * 
	 * @param matchParentExactly
	 *            If set to <tt>true</tt>, only a constructor that exactly
	 *            matches the class of this <tt>AnnotationIR</tt> will be
	 *            returned. If set to <tt>false</tt>, the constructor with
	 *            parameter that is nearest to the class of this
	 *            <tt>AnnotationIR</tt> will be returned.
	 * @param irClass
	 *            the class to search a constructor of
	 * @return the constructor of <tt>irClass</tt> taking this
	 *         <tt>AnnotationIR</tt> as parameter.
	 */
	private Constructor<?> findConstructor(boolean matchParentExactly,
			Class<?> irClass) {
		int lastDist = Integer.MAX_VALUE;
		Constructor<?> constr = null;
		for (Constructor<?> cc : irClass.getDeclaredConstructors()) {
			Class<?>[] pTypes = cc.getParameterTypes();
			if (pTypes.length != 1) {
				continue;
			}
			if (!pTypes[0].isAssignableFrom(getClass())) {
				continue;
			}
			int curDist = dist(getClass(), pTypes[0]);
			if (curDist == 0) {
				return cc;
			}
			if (!matchParentExactly && curDist < lastDist) {
				constr = cc;
			}
		}
		return constr;
	}

	/**
	 * Returns a previously collected annotation of a specified type, if any
	 * annotation has been collected. If no annotation of that type has
	 * previously been collected, <tt>null</tt> is returned.
	 * 
	 * @param <T>
	 *            type of the annotation to return
	 * @param type
	 *            class of the collected annotation
	 * @return least recently collected annotation of type <tt>type</tt> or
	 *         <tt>null</tt> if no such annotation has been collected at all
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getCollectedAnnotation(Class<T> type) {
		return (T) getCollectedAnnotations().get(type);
	}

	/**
	 * Creates a new and clean <tt>AnnotationIR</tt> for the
	 * <tt>AnnotatedElement</tt> that this <tt>AnnotationIR</tt> is responsible
	 * for.
	 * 
	 * @return a new and clean <tt>AnnotationIR</tt> that will replace this
	 */
	public abstract AnnotationIR overrideToDefault();

	/**
	 * @return every collected annotation for this <tt>AnnotationIR</tt>
	 */
	public Map<Class<?>, Annotation> getCollectedAnnotations() {
		return collectedAnnotations;
	}

	/**
	 * @return every annotation type that is currently set to be collected by
	 *         this <tt>AnnotationIR</tt>
	 */
	public Collection<Class<? extends Annotation>> getCollectTypes() {
		return collectTypes;
	}

	/**
	 * @return the <tt>AnnotationParser</tt> that this <tt>AnnotationIR</tt> was
	 *         created by
	 */
	public AnnotationParser getParser() {
		return parser;
	}

}
