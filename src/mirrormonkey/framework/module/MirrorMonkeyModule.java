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

package mirrormonkey.framework.module;

import mirrormonkey.framework.SyncAppState;

/**
 * Abstract base class for every module.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <A>
 *            Class of the <tt>SyncAppState</tt> that instantiates and manages
 *            the module
 */
public abstract class MirrorMonkeyModule<A extends SyncAppState<?>> {

	/**
	 * The <tt>SyncAppState</tt> responsible for managing this
	 * <tt>MirrorMonkeyModule</tt>
	 */
	private final A appState;

	/**
	 * Creates a new <tt>MirrorMonkeyModule</tt> using a given
	 * <tt>SyncAppState</tt>.
	 * 
	 * @param appState
	 *            most likely the <tt>SyncAppState</tt> calling this constructor
	 */
	public MirrorMonkeyModule(A appState) {
		this.appState = appState;
	}

	/**
	 * @return the <tt>SyncAppState</tt> responsible for managing this
	 *         <tt>MirrorMonkeyModule</tt>
	 */
	public A getAppState() {
		return appState;
	}

	/**
	 * Modules may define one annotation preset class which is automatically
	 * parsed once the module was instantiated. Additional annotation preset
	 * classes must be parsed manually.
	 * 
	 * @return the annotation preset class for this <tt>MirrorMonkeyModule</tt>
	 */
	public Class<?> getPresetClass() {
		return Object.class;
	}

	/**
	 * The responsible <tt>AppState</tt> delegates update calls to all
	 * registered modules by calling this method.
	 * 
	 * @param tpf
	 *            time in seconds since the last call of this method
	 */
	public void update(@SuppressWarnings("unused") float tpf) {
	}

}
