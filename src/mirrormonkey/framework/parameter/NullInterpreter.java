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

package mirrormonkey.framework.parameter;

import com.jme3.network.MessageConnection;

/**
 * Values are not packed or unpacked at all. Basically, this delegates
 * serializing completely to SpiderMonkey's <tt>Serializer</tt>.
 * 
 * The methods to check classes returned by <tt>extractData</tt> when
 * transmitting objects using this <tt>NullInterpreter</tt> will check exactly
 * against the local class of the transmitted value.
 * 
 * They will explicitly return <tt>true</tt>, if <tt>value</tt> is <tt>null</tt>
 * .
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class NullInterpreter implements ValueInterpreter {

	public Object extractData(Object object) {
		return object;
	}

	public Object packData(Object object) {
		return object;
	}

	public boolean isAssignableFrom(Class<?> connectedParameterClass,
			MessageConnection connection, Object parameter) {
		return parameter == null
				|| connectedParameterClass.isInstance(parameter);
	}

	public boolean isExactMatch(Class<?> connectedParameterClass,
			MessageConnection connection, Object parameter) {
		return parameter == null
				|| connectedParameterClass.equals(parameter.getClass());
	}

}
