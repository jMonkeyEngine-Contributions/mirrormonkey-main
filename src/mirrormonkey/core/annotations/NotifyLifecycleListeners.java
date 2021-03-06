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

package mirrormonkey.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mirrormonkey.util.annotations.hfilter.ClassFilter;

/**
 * Specifies for which classes class bound <tt>InstanceLifecycleListeners</tt>
 * should be notified for instances of the annotated class.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyLifecycleListeners {

	/**
	 * @return Every class in the local and connected class hierarchy will be
	 *         evaluated against this <tt>ClassFilters</tt>. If it is included,
	 *         <tt>InstanceLifecycleListeners</tt> bound to that class will be
	 *         notified on instance lifecycle events. If not, they will not be
	 *         notified.
	 */
	public ClassFilter[] value() default {};

}
