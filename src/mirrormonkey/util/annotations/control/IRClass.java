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

package mirrormonkey.util.annotations.control;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Controls which class is used for intermediate representation of data
 * belonging to the annotated class or member and which annotations to collect.<br>
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IRClass {

	/**
	 * Indicates that the IR class that is used to store intermediate data for
	 * the annotated class or member should not be changed. Please note that
	 * this option is not available on the first encountered (top-most) member
	 * in a class hierarchy as member IRs will not be created by default. Using
	 * this for <tt>value</tt> on a member that did not previously match a
	 * <tt>MemberDataIR</tt> will lead to an exception.
	 */
	public static final class NoChange {
	}

	/**
	 * Type of the IR
	 * 
	 * @return type of the IR
	 */
	public Class<?> value() default NoChange.class;

	/**
	 * Annotations of these classes will be collected when parsing the annotated
	 * element and every matching element further down the class hierarchy. They
	 * will be collected until the classes are specified in the
	 * <tt>stopCollect</tt> parameter in another <tt>IRClass</tt> in a matching
	 * element.
	 * 
	 * @return Classes of all annotations that should be collected from now on.
	 *         These annotations will be collected from every matching element
	 *         further down in the class hierarchy, including this one.
	 */
	public Class<?>[] startCollect() default {};

	/**
	 * Annotations of these classes will no longer be collected when parsing the
	 * annotated element and every matching element further down the class
	 * hierarchy.
	 * 
	 * @return Classes of annotations that should no longer be collected.
	 *         Annotations that have been collected so far will not be removed.
	 *         For this, their classes will also have to be added to
	 *         <tt>removeCompletely</tt>.
	 */
	public Class<?>[] stopCollect() default {};

	/**
	 * Selectively discards specific annotations without resetting everything
	 * (as <tt>AnnotationOverride</tt> would do).
	 * 
	 * @return Classes of previously collected annotations that should be
	 *         discarded. Annotations of these classes will still be collected.
	 *         To change this, their classes will also have to be added to
	 *         <tt>stopCollect</tt>.
	 */
	public Class<?>[] removeCompletely() default {};

}
