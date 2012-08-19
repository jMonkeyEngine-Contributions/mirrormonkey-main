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

package mirrormonkey.rpc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is invoked locally on entities whenever a proxy
 * method is called.
 * 
 * <b>Please note that this annotation should only be added to method
 * declarations in interfaces extending <tt>RpcSpecification</tt>. Adding it to
 * methods in implementing classes to override the definition in the
 * specification is explicitly not supported.</b>
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvokeLocally {

	/**
	 * Defines if and how the method is invoked locally. Default annotation
	 * (which will be assumed if this annotation is not added to a method) is
	 * <tt>NONE</tt>.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	public static enum LocalInvokeMode {
		/**
		 * Methods will not be invoked on the calling side.
		 */
		NONE,

		/**
		 * Methods will be invoked on the calling side before an invocation
		 * request is sent to the client.
		 */
		BEFORE,

		/**
		 * Methods will be invoked on the calling side after an invocation
		 * request has been sent to the client.
		 */
		AFTER
	};

	/**
	 * Defines whether the annotated method should be invoked locally whenever a
	 * method is invoked on a proxy to which the declaring entity instance is
	 * added.
	 * 
	 * @return the <tt>LocalInvokeMode</tt> specifying if and how the annotated
	 *         method should be invoked locally
	 */
	public LocalInvokeMode value() default LocalInvokeMode.AFTER;

}
