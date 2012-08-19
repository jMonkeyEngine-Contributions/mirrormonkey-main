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

package mirrormonkey.util.annotations.hfilter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Iterator;

/**
 * <tt>ClassFilter</tt> contains means to define a list of class filters and to
 * evaluate classes against that filter list, checking whether they are included
 * or excluded by the filter list.
 * 
 * The list is parsed in a linear fashion. Every filter in the list may include
 * or exclude all classes, one specific class or every class that is assignment
 * compatible with a specific class (which means that instances need to be
 * cast-able to the class given).
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassFilter {

	/**
	 * Used to specify whether a <tt>ClassFilter</tt> matches a single specific
	 * class or every class that is assignment compatible with it.
	 */
	public static enum HierarchyType {

		/**
		 * Specifies that the filter matches exactly the classes listed in
		 * <tt>classes</tt>
		 */
		SPECIFIC,

		/**
		 * Specifies that the filter matches every class that is listed in
		 * <tt>classes</tt> and every class that is assignment compatible to at
		 * least one of them
		 */
		HIERARCHY
	}

	/**
	 * Used to specify whether classes that match a <tt>ClassFilter</tt> are
	 * explicitly included in or excluded from the list of included classes.
	 */
	public static enum SelectType {

		/**
		 * Specifies that every class that matches the filter should be included
		 * in the list of filtered classes until explicitly excluded by another
		 * filter from the list.
		 */
		INCLUDE,

		/**
		 * Specifies that every class that matches the filter should be excluded
		 * from the list of filtered classes until explicitly included by
		 * another filter in the list.
		 */
		EXCLUDE
	}

	/**
	 * Selects whether this <tt>ClassFilter</tt> is applied to all specified
	 * classes or all classes and all classes assignment compatible to at least
	 * one of them.
	 * 
	 * @return the <tt>HierarchyType</tt> of this <tt>ClassFilter</tt>
	 */
	public HierarchyType hierarchy() default HierarchyType.SPECIFIC;

	/**
	 * Selects whether this <tt>ClassFilter</tt> includes the matching classes
	 * into the list of selected classes or excludes matching classes from the
	 * list.
	 * 
	 * @return the <tt>SelectType</tt> of this <tt>ClassFilter</tt>
	 */
	public SelectType select() default SelectType.INCLUDE;

	/**
	 * Selects every class (or superclass) that this <tt>ClassFilter</tt>
	 * matches
	 * 
	 * @return an array of every class that this <tt>ClassFilter</tt> matches or
	 *         an empty class array if this <tt>ClassFilter</tt> matches every
	 *         class
	 */
	public Class<?>[] value() default {};

	/**
	 * Utility class to evaluate classes against lists of <tt>ClassFilters</tt>
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public static final class Eval {

		/**
		 * Private constructor to make it an utility class
		 */
		private Eval() {
		}

		/**
		 * Calculates whether a <tt>ClassFilter</tt> matches a <tt>Class</tt>
		 * according to the <tt>ClassFilter's</tt> parameters.
		 * 
		 * @param filter
		 *            the <tt>ClassFilter</tt> to evaluate <tt>toTest</tt>
		 *            against
		 * @param toTest
		 *            the <tt>Class</tt> to evaluate against <tt>filter</tt>
		 * @return <tt>true</tt> if, and only if, the parameters of
		 *         <tt>filter</tt> dictate that <tt>filter.select</tt> is
		 *         relevant to evaluate <tt>toTest</tt> against <tt>filter</tt>.
		 */
		public static final boolean matches(ClassFilter filter, Class<?> toTest) {
			if (filter.value().length == 0) {
				return true;
			}
			return filter.hierarchy().equals(HierarchyType.HIERARCHY) ? matchesHierarchy(
					filter, toTest) : matchesSpecific(filter, toTest);
		}

		/**
		 * Calculates whether <tt>toTest</tt> is assignment compatible with at
		 * least one class from <tt>filter.value</tt>.
		 * 
		 * @param filter
		 *            the <tt>ClassFilter</tt> to evaluate <tt>toTest</tt>
		 *            against
		 * @param toTest
		 *            the <tt>Class</tt> to evaluate against <tt>filter</tt>
		 * @return <tt>true</tt> if the method <tt>isAssignableFrom</tt> from at
		 *         least one <tt>Class</tt> from <tt>filter.value</tt> returns
		 *         <tt>true</tt> for parameter <tt>toTest</tt>, <tt>false</tt>
		 *         otherwise.
		 */
		private static final boolean matchesHierarchy(ClassFilter filter,
				Class<?> toTest) {
			for (Class<?> c : filter.value()) {
				if (c.isAssignableFrom(toTest)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Calculates whether <tt>toTest</tt> is equal to one class from
		 * <tt>filter.value</tt>.
		 * 
		 * @param filter
		 *            the <tt>ClassFilter</tt> to evaluate <tt>toTest</tt>
		 *            against
		 * @param toTest
		 *            the <tt>Class</tt> to evaluate against <tt>filter</tt>
		 * @return <tt>true</tt> if the method <tt>equals</tt> from at least one
		 *         <tt>Class</tt> from <tt>filter.value</tt> returns
		 *         <tt>true</tt> for parameter <tt>toTest</tt>, <tt>false</tt>
		 *         otherwise.
		 */
		private static final boolean matchesSpecific(ClassFilter filter,
				Class<?> toTest) {
			for (Class<?> c : filter.value()) {
				if (c.equals(toTest)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks a single class against a list of <tt>ClassFilters</tt>.
		 * Generally, <tt>filters</tt> is iterated and every filter overwrites
		 * settings of previous filters that match the same class.
		 * 
		 * @param filters
		 *            array of <tt>ClassFilters</tt> to check <tt>toTest</tt>
		 *            against
		 * @param toTest
		 *            class to check against the filters
		 * @return <tt>true</tt>, if the last filter in <tt>filters</tt> that
		 *         matches <tt>toTest</tt> is set to <tt>INCLUDE</tt>,
		 *         <tt>false</tt> otherwise or if no <tt>ClassFilter</tt>
		 *         matches <tt>toTest</tt>
		 */
		public static final boolean contains(ClassFilter[] filters,
				Class<?> toTest) {
			boolean state = false;
			for (ClassFilter f : filters) {
				if (matches(f, toTest)) {
					state = f.select().equals(SelectType.INCLUDE);
				}
			}
			return state;
		}

		/**
		 * Removes every class that is not explicitly included by a list of
		 * <tt>ClassFilters</tt> from a <tt>Collection</tt> of <tt>Classes</tt>.
		 * 
		 * @param filters
		 *            the filters to evaluate the elements of <tt>toFilter</tt>
		 *            against
		 * @param toFilter
		 *            the <tt>Collection</tt> to filter for explicit matches
		 *            against <tt>filters</tt>
		 */
		public static final void filter(ClassFilter[] filters,
				Collection<Class<?>> toFilter) {
			for (Iterator<Class<?>> i = toFilter.iterator(); i.hasNext();) {
				if (!contains(filters, i.next())) {
					i.remove();
				}
			}
		}
	}

}
