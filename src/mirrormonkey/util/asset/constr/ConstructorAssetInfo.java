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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

/**
 * <tt>AssetInfo</tt> that will invoke an empty constructor to load an asset.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class ConstructorAssetInfo extends AssetInfo {

	/**
	 * Constructor to invoke to load the asset.
	 */
	private final Constructor<?> constructor;

	/**
	 * Creates a new <tt>ConstructorAssetInfo</tt> that will invoke an empty
	 * constructor to load an asset.
	 * 
	 * @param manger
	 *            the <tt>AssetManager</tt> that is used to load assets
	 * @param key
	 *            the <tt>AssetKey</tt> that is used to find the class and
	 *            constructor
	 * @param constructor
	 *            the constructor to invoke when creating an asset instance
	 */
	public ConstructorAssetInfo(AssetManager manger, AssetKey<?> key,
			Constructor<?> constructor) {
		super(manger, key);
		this.constructor = constructor;
	}

	/**
	 * Creates a new asset instance by calling the given constructor.
	 * 
	 * @return new asset instance
	 * @throws IllegalArgumentException
	 *             if <tt>newInstance</tt> throws it
	 * @throws InstantiationException
	 *             if <tt>newInstance</tt> throws it
	 * @throws IllegalAccessException
	 *             if <tt>newInstance</tt> throws it
	 * @throws InvocationTargetException
	 *             if <tt>newInstance</tt> throws it
	 */
	public Object invoke() throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		return constructor.newInstance();
	}

	@Override
	public InputStream openStream() {
		return null;
	}

}
