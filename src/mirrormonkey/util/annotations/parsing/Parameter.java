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

/**
 * Simple immutable class containing runtime information about a method or
 * constructor parameter similar to the classes in <tt>java.lang.reflect</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class Parameter implements AnnotatedElement {

	/**
	 * Every annotation that the parameter declares.
	 */
	private final Annotation[] annotations;

	/**
	 * The type of the method or constructor parameter represented by this
	 * <tt>Parameter</tt> instance.
	 */
	private final Class<?> parameterType;

	/**
	 * The position of the parameter represented by this <tt>Parameter</tt>
	 * instance in the parameter list of the declaring method or constructor
	 */
	private final int parameterId;

	/**
	 * Creates a new <tt>Parameter</tt> instance representing a parameter with
	 * the given runtime information.
	 * 
	 * @param annotations
	 *            all annotations present on the parameter represented by this
	 *            <tt>Parameter</tt> instance
	 * @param parameterType
	 *            the type of the parameter represented by this
	 *            <tt>Parameter</tt> instance
	 * @param parameterId
	 *            the position of the parameter represented by this
	 *            <tt>Parameter</tt> instance in the parameter list of the
	 *            declaring method or constructor
	 */
	public Parameter(Annotation[] annotations, Class<?> parameterType,
			int parameterId) {
		this.annotations = annotations;
		this.parameterType = parameterType;
		this.parameterId = parameterId;
	}

	public final boolean isAnnotationPresent(
			Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	@SuppressWarnings("unchecked")
	public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (Annotation i : annotations) {
			if (i.annotationType().equals(annotationClass)) {
				return (T) i;
			}
		}
		return null;
	}

	public final Annotation[] getAnnotations() {
		return annotations;
	}

	public final Annotation[] getDeclaredAnnotations() {
		return annotations;
	}

	/**
	 * @return the type of the parameter represented by this <tt>Parameter</tt>
	 *         instance
	 */
	public final Class<?> getParameterType() {
		return parameterType;
	}

	/**
	 * @return the position of the parameter represented by this
	 *         <tt>Parameter</tt> instance in the parameter list of the
	 *         declaring method or constructor
	 */
	public final int getParameterId() {
		return parameterId;
	}

}
