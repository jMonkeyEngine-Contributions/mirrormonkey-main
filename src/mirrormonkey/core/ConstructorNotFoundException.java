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

package mirrormonkey.core;

/**
 * Thrown on the server side when a call should be delegated to a client to
 * create a client-local instance of an entity, but the class that should be
 * instantiated does not support instantiation for the arguments provided.
 * 
 * As constructor call delegation uses varargs, the first thing you want to do
 * when you get this exception is check whether the correct version of
 * <tt>callConstr</tt> is called. If it is, check if the client-local entity
 * class provides a suiting constructor for the arguments you provided and if
 * that constructor is annotated with <tt>ClientSideConstructor</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ConstructorNotFoundException extends RuntimeException {

	/**
	 * Generated SVUID.
	 */
	private static final long serialVersionUID = 7516398692460687312L;

	/**
	 * Creates a new <tt>ConstructorNotFoundException</tt> with a given
	 * description of why it occured.
	 * 
	 * @param reason
	 *            String containing the key that was used to try and find the
	 *            constructor.
	 */
	public ConstructorNotFoundException(String reason) {
		super(reason);
	}

}
