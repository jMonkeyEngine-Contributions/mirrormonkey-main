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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It is possible to create preset annotations with this subsystem. Preset
 * annotations are annotations that will imply any other number of annotations
 * when used on an <tt>AnnotatedElement</tt>.
 * 
 * In order to use preset annotations, users have to define a simple class that
 * will be called the preset class. In this class, users may declare fields,
 * methods and internal classes, one for each preset that they want to create
 * and annotate them with the annotations that the preset should imply.<br>
 * 
 * Then, users have to create different annotation types for each preset. For
 * each annotation type, this annotation has to be added to the field, method or
 * internal class of the preset class that is annotated with the annotations
 * that the preset annotation type should imply.<br>
 * 
 * Before being able to use the preset annotations, the preset class must be
 * registered in the <tt>AnnotationParser</tt> by calling its
 * <tt>parsePresetClass</tt> method.<br>
 * 
 * After all these steps are performed, whenever an <tt>AnnotatedElement</tt> is
 * encountered that is annotated with a preset annotation, the annotations of
 * the <tt>AnnotatedElement</tt> that was linked to the preset annotation's type
 * by this annotation are parsed in addition to every other annotation on the
 * <tt>AnnotatedElement</tt> concerned.<br>
 * 
 * Please note that this subsystem does <b>not</b> check the <tt>Target</tt>
 * annotation on the annotations on the fields, methods or internal classes
 * against the type of the <tt>AnnotatedElement</tt> annotated with the preset
 * annotation, so it is possible to introduce illegal annotations to
 * <tt>AnnotatedElement</tt>s if not careful.<br>
 * 
 * It is not possible to use one preset annotation class with multiple fields,
 * methods or internal classes of preset classes. It is, however, possible to
 * link two different preset annotation classes to the same field, method or
 * internal class in the preset class. It is also possible to use multiple
 * preset annotation classes on one <tt>AnnotatedElement</tt>.<br>
 * 
 * It is possible to collect the preset annotation itself in the corresponding
 * IRs if they are set to listen to the preset class.<br>
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
		ElementType.CONSTRUCTOR })
public @interface DefinePreset {

	/**
	 * Returns the associated preset annotation type. A preset annotation can be
	 * any annotation. Whenever a preset annotation type that was previously
	 * defined with <tt>DefinePreset</tt> is encountered when parsing a member,
	 * or class, the member or class annotated with the <tt>DefinePreset</tt>
	 * annotation with matching <tt>value</tt> will be parsed additionally.
	 * During that process, <tt>AnnotationOverride</tt> and <tt>IRClass</tt>
	 * annotations will be parsed as if they were present on the member or class
	 * annotated with the preset annotation.<br>
	 * 
	 * @return The preset annotation class that will be bound to the annotated
	 *         member or class
	 */
	public Class<? extends Annotation> value();

}
