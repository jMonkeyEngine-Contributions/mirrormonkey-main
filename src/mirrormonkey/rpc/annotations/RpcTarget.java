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
 * Indicates that the annotated method can be invoked from remote.
 * 
 * The RPC module supports adding either <tt>EntityInjection</tt> or
 * <tt>AssetInjection</tt> to the method or its parameters. Doing so will enable
 * entity injection on the result, or the annotated parameters, respectively.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcTarget {

	/**
	 * Setting this as the value of <tt>callTimeout</tt> indicates that calls
	 * never time out.
	 */
	public static final long NO_CALL_TIMEOUT = Long.MAX_VALUE;

	/**
	 * Specifies the time that MirrorMonkey waits before notifying
	 * <tt>RpcListeners</tt> that would be notified of a result that no result
	 * has not yet been received.
	 * 
	 * @return Time indicating how long to wait before assuming that the
	 *         response or call messages have been lost somewhere along the way,
	 *         in nanoseconds. Please note that network latency is not
	 *         automatically included in any way.
	 */
	public long responseTimeout() default 1000000000L;

	/**
	 * Specifies the time that MirrorMonkey waits before a received call will no
	 * longer be invoked. If an invocation request is received and the call
	 * timeout is exceeded, then the requested method will not be invoked.
	 * Instead of returning the result of the invocation, a timeout error
	 * message is passed to the calling side.
	 * 
	 * @return Time indicating how long to wait before an invocation request
	 *         becomes invalid, in milliseconds. Please note that this feature
	 *         is not entirely reliable, as the time that a call times out is
	 *         based on estimating remote time for the calling side.
	 */
	public long callTimeout() default NO_CALL_TIMEOUT;

	/**
	 * Specifies whether invocation request and result messages should be
	 * reliable or not.
	 * 
	 * @return <tt>true</tt>, if messages for the annotated method should be
	 *         reliable, <tt>false</tt> otherwise
	 */
	public boolean reliable() default true;

}
