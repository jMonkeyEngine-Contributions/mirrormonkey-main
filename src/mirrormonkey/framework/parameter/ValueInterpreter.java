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
 * Encapsulates a way that a family of values is handles when sending them over
 * the network.
 * 
 * In contrast to SpiderMonkey's <tt>Serializer</tt>, packing and extracting
 * data is done synchronized with the main loop and not depending on the object
 * class.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface ValueInterpreter {

	/**
	 * Interprets a received value.
	 * 
	 * @param object
	 *            the object that was received
	 * @return the interpretation of the received object, as interpreted by this
	 *         <tt>ValueInterpreter</tt>
	 */
	public Object extractData(Object object);

	/**
	 * Packs a value so that it may be sent over the network.
	 * 
	 * @param object
	 *            the value to pack
	 * @return packed form of <tt>object</tt>
	 */
	public Object packData(Object object);

	/**
	 * Checks whether an object would be assignment compatible with a given
	 * class if it was transmitted over the network and extracted using this
	 * <tt>ValueInterpreter</tt>.
	 * 
	 * @param expectedClass
	 *            the class that the value returned by <tt>extractData</tt> on
	 *            the other side of the connection should be assignment
	 *            compatible to
	 * @param connection
	 *            the <tt>MessageConnection</tt> that <tt>value</tt> would be
	 *            sent over
	 * @param value
	 *            the object that would be packed and sent using this
	 *            <tt>ValueInterpreter</tt>
	 * @return <tt>true</tt> if when sending the object returned by the method
	 *         <tt>packData</tt> for <tt>value</tt> via <tt>connection</tt>, the
	 *         method <tt>extractData</tt> would return a value that would be
	 *         assignment compatible with <tt>expectedClass</tt> on the other
	 *         side of <tt>connection</tt>, <tt>false</tt> otherwise.
	 */
	public boolean isAssignableFrom(Class<?> expectedClass,
			MessageConnection connection, Object value);

	/**
	 * Checks whether an object would have a given class if it was transmitted
	 * over the network and extracted using this <tt>ValueInterpreter</tt>.
	 * 
	 * @param expectedClass
	 *            the class that the value returned by <tt>extractData</tt> on
	 *            the other side of the connection should be an instance of
	 * @param connection
	 *            the <tt>MessageConnection</tt> that <tt>value</tt> would be
	 *            sent over
	 * @param value
	 *            the object that would be packed and sent using this
	 *            <tt>ValueInterpreter</tt>
	 * 
	 * @return <tt>true</tt>, if when sending the object returned by the method
	 *         <tt>packData</tt> for <tt>value</tt> via <tt>connection</tt>, the
	 *         method <tt>extractData</tt> would return a value that would be an
	 *         instance of exactly <tt>expectedClass</tt> on the other side of
	 *         <tt>connection</tt>, <tt>false</tt> otherwise
	 */
	public boolean isExactMatch(Class<?> expectedClass,
			MessageConnection connection, Object value);
}