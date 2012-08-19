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

package mirrormonkey.rpc;

import mirrormonkey.rpc.module.RegisteredCall;

/**
 * Indicates that a class is interested in results returned by remote calls.
 * Methods in this class will be called when the instance of an implementing
 * class is registered in an <tt>RpcProxy</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface RpcListener {

	/**
	 * Called when a remote call has returned and the result was received
	 * locally.
	 * 
	 * @param registeredCall
	 *            contains information about the call that returned
	 * @param result
	 *            the result that was received, with respect to the method's
	 *            <tt>EntityInjection</tt> and <tt>AssetInjection</tt>
	 *            annotations
	 */
	public void resultArrived(RegisteredCall registeredCall, Object result);

	/**
	 * Called when a remote call has thrown an exception or could not be
	 * executed for other reasons.
	 * 
	 * @param registeredCall
	 *            contains information about the call that failed
	 * @param message
	 *            The Message of the error that occurred. This can be the reason
	 *            string for a thrown exception or a predefined error string in
	 *            case of an internal error.
	 */
	public void errorArrived(RegisteredCall registeredCall, String message);

	/**
	 * Called when the timeout limit for a call was reached. Please note that
	 * this is called once for every connection that has not yet returned an
	 * expected result.
	 * 
	 * @param registeredCall
	 *            contains information about the call that failed
	 */
	public void timedOut(RegisteredCall registeredCall);

}
