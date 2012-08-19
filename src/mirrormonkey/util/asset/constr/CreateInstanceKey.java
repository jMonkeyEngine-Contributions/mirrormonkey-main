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

package mirrormonkey.util.asset.constr;

import com.jme3.asset.AssetKey;

/**
 * <tt>AssetKey</tt> that will just create new instances of a class by calling
 * an empty constructor when loading the asset.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class CreateInstanceKey extends AssetKey<Object> {

	/**
	 * Root path for the create instance asset system, used to bind the
	 * <tt>ConstructorLocator</tt>.
	 */
	public static final String CREATE_INSTANCE_ROOT_PATH = "ClassLoad";

	/**
	 * Virtual file name extension for the create instance asset system, used to
	 * bind the <tt>CreateInstanceLoader</tt>.
	 */
	public static final String LOADER_EXTENSION = "dynamicLoad";

	/**
	 * Creates a new <tt>CreateInstanceKey</tt> that will create a new instance
	 * of <tt>clazz</tt> by reflectively calling its empty constructor.
	 * 
	 * @param clazz
	 *            the class of the asset to instantiate
	 */
	public CreateInstanceKey(Class<?> clazz) {
		this(getClassString(clazz));
	}

	/**
	 * Creates a new <tt>CreateInstanceKey</tt> for a given create instance path
	 * (as created with <tt>getClassString</tt>)
	 * 
	 * @param name
	 *            name of the class of the asset that will be instantiated,
	 *            extended with <tt>CREATE_INSTANCE_ROOT_PATH</tt> and
	 *            <tt>LOADER_EXTENSION</tt>
	 */
	public CreateInstanceKey(String name) {
		super(name);
	}

	/**
	 * Wraps a class name into <tt>CREATE_INSTANCE_PATH</tt> and
	 * <tt>LOADER_EXTENSION</tt> so that <tt>ConstructorLocator</tt> and
	 * <tt>CreateInstanceLoader</tt> are found to instantiate the asset by
	 * jMonkeyEngine.
	 * 
	 * @param clazz
	 *            <tt>Class</tt> that should be instantiated to load the asset
	 * @return name of <tt>clazz</tt>, wrapped so that instances can be created
	 *         using the classes in this package
	 */
	public static final String getClassString(Class<?> clazz) {
		return CREATE_INSTANCE_ROOT_PATH + "/" + clazz.getName() + "."
				+ LOADER_EXTENSION;
	}

	/**
	 * Unwraps the class name for this <tt>CreateInstanceKey</tt> from
	 * <tt>CREATE_INSTANCE_ROOT_PATH</tt> and <tt>LOADER_EXTENSION</tt>.
	 * 
	 * @return the name of the class that a new instance is to be created of
	 */
	public String getClassName() {
		int begin = CREATE_INSTANCE_ROOT_PATH.length() + 1;
		int end = getName().length() - (LOADER_EXTENSION.length() + 1);
		return getName().substring(begin, end);
	}

}
