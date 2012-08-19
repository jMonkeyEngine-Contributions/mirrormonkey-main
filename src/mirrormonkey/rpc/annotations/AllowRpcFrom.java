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

import mirrormonkey.util.annotations.hfilter.ClassFilter;

/**
 * Restricts possible connected classes that are allowed to invoke a method.
 * 
 * By default, server and client can perform arbitrary RPCs on every client for
 * every visible entity. Users can add this annotation to a number of methods in
 * the <tt>RpcSpecification</tt> or their implementations to evaluate the
 * connected class from which a call has been received against a list of
 * <tt>ClassFilters</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowRpcFrom {

	/**
	 * Whenever a call is received for an annotated method, the connected class
	 * is evaluated against this array of <tt>ClassFilters</tt>.
	 * 
	 * If the connected class evaluates to <tt>true</tt> in the list of
	 * <tt>ClassFilters</tt>, then the method will be invoked and the result
	 * returned. Otherwise, the method is not invoked and an error message will
	 * be sent to the invoking connection.
	 * 
	 * The mechanism to check whether a call is allowed only takes effect on the
	 * time needed to parse entity classes. Once a class hierarchy has been
	 * parsed for the first time, the data is cached until the VM is closed.
	 * <b>Using this annotation on methods does not add any runtime overhead on
	 * method invocations.</b>
	 * 
	 * @return an array of <tt>ClassFilters</tt> used to determine if a call is
	 *         actually going to be invoked
	 */
	public ClassFilter[] value();

}
