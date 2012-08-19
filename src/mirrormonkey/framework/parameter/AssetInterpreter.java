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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import mirrormonkey.framework.SyncAppState;
import mirrormonkey.util.asset.BitmapFontKey;
import mirrormonkey.util.asset.MaterialDefKey;
import mirrormonkey.util.asset.constr.ConstructorLocator;
import mirrormonkey.util.asset.constr.CreateInstanceKey;
import mirrormonkey.util.asset.constr.CreateInstanceLoader;
import mirrormonkey.util.serializer.AssetKeySerializer;

import com.jme3.asset.Asset;
import com.jme3.asset.AssetKey;
import com.jme3.asset.MaterialKey;
import com.jme3.asset.ModelKey;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.MaterialList;
import com.jme3.network.MessageConnection;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.matext.OgreMaterialKey;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Texture3D;
import com.jme3.texture.TextureArray;
import com.jme3.texture.TextureCubeMap;

/**
 * Assumes packed values to be <tt>Assets</tt> or <tt>AssetKeys</tt>. Transmits
 * the <tt>AssetKeys</tt> themselves or fetches the <tt>AssetKeys</tt> of values
 * if they are <tt>Assets</tt>.
 * 
 * The methods to check classes of the values returned by <tt>extractData</tt>
 * when transmitting are a bit more complicated than the other value interpreter
 * classes. Generally, this <tt>AssetInterpreter</tt> keeps a map of which asset
 * classes will be returned by <tt>extractData</tt> for which key classes. This
 * map must contain the same values on both sides of the connection, or
 * unexpected runtime exceptions may occur.
 * 
 * For specifics on how to handle this class, please look up custom asset
 * injection in the advanced documentation.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class AssetInterpreter implements ValueInterpreter {

	/**
	 * AppState that this <tt>AssetInterpreter</tt> was created for
	 */
	private final SyncAppState<?> appState;

	/**
	 * The default <tt>Serializer</tt>, which will serialize <tt>AssetKeys</tt>
	 * using their names.
	 */
	private final AssetKeySerializer serializer;

	/**
	 * Keeps track of which asset classes can be extracted for which asset key
	 * classes.
	 * 
	 * Used for <tt>isAssignableFrom</tt> and <tt>isExactMatch</tt>.
	 */
	private final Map<Class<?>, Set<Class<?>>> assetKeyClassesToAssetClasses;

	/**
	 * Creates a new <tt>AssetInterpreter</tt> that will use a given
	 * <tt>SyncAppState</tt> to look up <tt>Assets</tt>.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> to use when looking up
	 *            <tt>Assets</tt>
	 */
	public AssetInterpreter(SyncAppState<?> appState) {
		this.appState = appState;
		assetKeyClassesToAssetClasses = new HashMap<Class<?>, Set<Class<?>>>();
		serializer = new AssetKeySerializer();
		addAssetClass(TextureKey.class, Texture.class);
		addAssetClass(TextureKey.class, Texture2D.class);
		addAssetClass(TextureKey.class, Texture3D.class);
		addAssetClass(TextureKey.class, TextureArray.class);
		addAssetClass(TextureKey.class, TextureCubeMap.class);
		// addAssetClass(AudioKey.class, AndroidAudioData.class);
		addAssetClass(AudioKey.class, AudioBuffer.class);
		addAssetClass(AudioKey.class, AudioData.class);
		addAssetClass(MaterialKey.class, Material.class);
		addAssetClass(CreateInstanceKey.class, Mesh.class);
		// addAssetClass(AudioKey.class, AudioStream.class);
		addAssetClass(MaterialDefKey.class, MaterialDef.class);
		addAssetClass(ModelKey.class, Spatial.class);
		addAssetClass(BitmapFontKey.class, BitmapFont.class);
		addAssetClass(OgreMaterialKey.class, MaterialList.class);

		appState.getAssetCache().cacheAssetLoader(CreateInstanceLoader.class,
				CreateInstanceKey.LOADER_EXTENSION);
		appState.getAssetCache().cacheAssetLocator(
				CreateInstanceKey.CREATE_INSTANCE_ROOT_PATH,
				ConstructorLocator.class);
	}

	/**
	 * Indicates that additionally to the already known asset key - asset
	 * combinations, another asset class can be result of an asset lookup for an
	 * asset key class and uses the default <tt>AssetKeySerializer</tt> to
	 * transmit that <tt>AssetKey</tt> over the network.
	 * 
	 * Please note that this method must be called on the server and every
	 * client for every asset key class - asset class combination that should be
	 * accessible using asset injection.
	 * 
	 * @param assetKeyClass
	 *            the asset key class that can be looked up
	 * @param assetClass
	 *            a class that could be the result of an asset lookup for
	 *            <tt>assetKeyClass</tt>
	 */
	public void addAssetClass(Class<?> assetKeyClass, Class<?> assetClass) {
		addAssetClass(assetKeyClass, assetClass, serializer);
	}

	/**
	 * Indicates that additionally to the already known asset key - asset
	 * combinations, another asset class can be result of an asset lookup for an
	 * asset key class and uses the given <tt>Serializer</tt> to transmit that
	 * <tt>AssetKey</tt> over the network.
	 * 
	 * Please note that calls to this method are not automatically synchronized
	 * with connected clients. In order to use this correctly, the method has to
	 * be called on the server and all clients with the same parameters.
	 * 
	 * @param assetKeyClass
	 *            the asset key class that can be looked up
	 * @param assetClass
	 *            a class that could be the result of an asset lookup for
	 *            <tt>assetKeyClass</tt>
	 * @param serializer
	 *            the <tt>Serializer</tt> to use when transmitting instances of
	 *            <tt>assetKeyClass</tt> over the network
	 */
	public void addAssetClass(Class<?> assetKeyClass, Class<?> assetClass,
			Serializer serializer) {
		Set<Class<?>> assetClasses = assetKeyClassesToAssetClasses
				.get(assetKeyClass);
		if (assetClasses == null) {
			assetClasses = new LinkedHashSet<Class<?>>();
			assetKeyClassesToAssetClasses.put(assetKeyClass, assetClasses);
		}
		Serializer.registerClass(assetKeyClass, serializer);
		assetClasses.add(assetClass);
	}

	/**
	 * Indicates that for a given asset key class, a specific asset class can no
	 * longer be the result of a lookup in the local <tt>AssetManager</tt>.
	 * 
	 * Please note that calls to this method are not automatically synchronized
	 * with connected clients. In order to use this correctly, the method has to
	 * be called on the server and all clients with the same parameters.
	 * 
	 * @param assetKeyClass
	 *            the asset key class
	 * @param assetClass
	 *            asset lookups for <tt>AssetKeyClass</tt> can no longer return
	 *            a value that is instance of this class
	 */
	public void removeAssetClass(Class<?> assetKeyClass, Class<?> assetClass) {
		Set<Class<?>> assetClasses = assetKeyClassesToAssetClasses
				.get(assetKeyClass);
		if (assetClasses == null) {
			return;
		}
		assetClasses.remove(assetClass);
		if (assetClasses.isEmpty()) {
			assetKeyClassesToAssetClasses.remove(assetKeyClass);
		}
	}

	public Object extractData(Object object) {
		if (object == null) {
			return null;
		}
		AssetKey<?> k = (AssetKey<?>) object;
		Object o = appState.getApplication().getAssetManager().loadAsset(k);
		if (Asset.class.isInstance(o)) {
			((Asset) o).setKey(k);
		}
		return o;
	}

	public Object packData(Object object) {
		// TODO core requires this, but for rpc, null is allowed
		/*
		 * if (object == null) { throw new
		 * NullPointerException("AssetKey null-reference supplied" +
		 * " for constructor"); }
		 */
		if (object == null) {
			return null;
		} else if (AssetKey.class.isInstance(object)) {
			return object;
		} else if (Asset.class.isInstance(object)) {
			return ((Asset) object).getKey();
		} else {
			throw new IllegalArgumentException("Argument " + object
					+ " is neither null, nor Asset, nor AssetKey"
					+ " and can not be used with @AssetInjection in"
					+ " this context.");
		}
	}

	public boolean isAssignableFrom(Class<?> expectedClass,
			MessageConnection connection, Object parameter) {
		if (parameter == null || !AssetKey.class.isInstance(parameter)) {
			return false;
		}
		Set<Class<?>> s = assetKeyClassesToAssetClasses.get(parameter
				.getClass());
		if (s == null) {
			return false;
		}
		for (Class<?> i : s) {
			if (expectedClass.isAssignableFrom(i)) {
				return true;
			}
		}
		return false;
	}

	public boolean isExactMatch(Class<?> expectedClass,
			MessageConnection connection, Object parameter) {
		if (parameter == null || !AssetKey.class.isInstance(parameter)) {
			return false;
		}
		Set<Class<?>> s = assetKeyClassesToAssetClasses.get(parameter
				.getClass());
		if (s == null) {
			return false;
		}
		return s.contains(expectedClass);
	}

	/**
	 * Checks whether there is a registered asset key class - asset class
	 * mapping where a given asset class can be result of an asset lookup.
	 * 
	 * @param parameterClass
	 *            the <tt>parameterClass</tt> that could be the result of an
	 *            asset lookup
	 * @return <tt>true</tt>, if <tt>parameterClass</tt> could be the class of
	 *         the result for an asset lookup of any registered key class,
	 *         <tt>false</tt> otherwise
	 */
	public boolean isRegisteredAssetLike(Class<?> parameterClass) {
		for (Set<Class<?>> i : assetKeyClassesToAssetClasses.values()) {
			if (i.contains(parameterClass)) {
				return true;
			}
		}
		return false;
	}

}
