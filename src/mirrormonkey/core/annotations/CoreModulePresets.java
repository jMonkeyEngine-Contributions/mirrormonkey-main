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

import mirrormonkey.core.member.ConstructorDataIR;
import mirrormonkey.util.annotations.control.DefinePreset;
import mirrormonkey.util.annotations.control.IRClass;

/**
 * Contains preset annotations for the core module.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class CoreModulePresets {

	/**
	 * Choose the right IR class for constructors with
	 * <tt>ClientSideConstructor</tt>.
	 */
	@DefinePreset(ClientSideConstructor.class)
	@IRClass(ConstructorDataIR.class)
	public CoreModulePresets() {
	}

	/**
	 * Ensure that <tt>ClientSideConstructor</tt> is present wherever
	 * <tt>NamedClientSideConstructor</tt> is present.
	 * 
	 * @param o
	 *            not important; this constructor is never called
	 */
	@DefinePreset(NamedClientSideConstructor.class)
	@ClientSideConstructor
	public CoreModulePresets(@SuppressWarnings("unused") Object o) {
	}

}
