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

package mirrormonkey.util.asset;

import java.util.Collection;
import java.util.LinkedList;

import com.jme3.app.Application;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;

/**
 * Since MirrorMonkey's extension to jMonkeyEngine's asset system require asset
 * loaders and asset locators to be registered before the <tt>Application's</tt>
 * <tt>AssetManager</tt> is initialized, this class provides means to cache
 * those asset loaders and asset locators and add them once the
 * <tt>AssetManager</tt> becomes known.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class AssetExtensionCache {

	/**
	 * Contains all <tt>AssetLoaders</tt> and <tt>AssetLocators</tt> that were
	 * registered to MirrorMonkey's asset extension system.
	 */
	private final Collection<CachedAssetExtension> cachedExtensions;

	/**
	 * Contains the currently initialized <tt>Application</tt> running
	 * MirrorMonkey's <tt>AppState</tt>.
	 */
	private Application application;

	/**
	 * Creates a new <tt>AssetExtensionCache</tt> with empty cache and no
	 * application.
	 */
	public AssetExtensionCache() {
		cachedExtensions = new LinkedList<CachedAssetExtension>();
		application = null;
	}

	/**
	 * Sets the currently active application to <tt>application</tt> and add all
	 * cached asset extensions to <tt>application's </tt> <tt>AssetManager</tt>.
	 * 
	 * @param application
	 *            the <tt>Application</tt> being initialized
	 */
	public void setApplication(Application application) {
		if (this.application == application) {
			return;
		}
		if (application != null) {
			for (CachedAssetExtension i : cachedExtensions) {
				i.register(application.getAssetManager());
			}
		}
		this.application = application;
	}

	/**
	 * Adds an <tt>AssetLoader</tt> class to the cache and registers it to the
	 * current <tt>Application's</tt> <tt>AssetManager</tt>, if one exists.
	 * 
	 * If the <tt>AssetManager</tt> does not exist yet, the <tt>AssetLoader</tt>
	 * class will be registered once one becomes available.
	 * 
	 * @param loaderClass
	 *            the class of the loader to cache
	 * @param loaderExtension
	 *            the extension to which the loader will be bound
	 */
	public void cacheAssetLoader(Class<? extends AssetLoader> loaderClass,
			String loaderExtension) {
		CachedAssetLoader cal = new CachedAssetLoader(loaderClass,
				loaderExtension);
		cachedExtensions.add(cal);
		if (application != null) {
			cal.register(application.getAssetManager());
		}
	}

	/**
	 * Adds an <tt>AssetLocator</tt> class to the cache and registers it to the
	 * current <tt>Application's</tt> <tt>AssetManager</tt>, if one exists.
	 * 
	 * If the <tt>AssetManager</tt> does not exist yet, the
	 * <tt>AssetLocator</tt> class will be registered once one becomes
	 * available.
	 * 
	 * @param createInstanceRootPath
	 *            the root path to which to bind <tt>locatorClass</tt>
	 * @param locatorClass
	 *            the <tt>AssetLocator</tt> class to cache
	 */
	public void cacheAssetLocator(String createInstanceRootPath,
			Class<? extends AssetLocator> locatorClass) {
		CachedAssetLocator cal = new CachedAssetLocator(locatorClass,
				createInstanceRootPath);
		cachedExtensions.add(cal);
		if (application != null) {
			cal.register(application.getAssetManager());
		}
	}

	/**
	 * Denotes cached asset extensions.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	protected static interface CachedAssetExtension {

		/**
		 * Registers the asset extension to <tt>manager</tt>. Called once
		 * <tt>manager</tt> becomes available to the <tt>AppState</tt>.
		 * 
		 * @param manager
		 *            the <tt>AssetManager</tt> to register this
		 *            <tt>CachedAssetExtension</tt> to
		 */
		public void register(AssetManager manager);

	}

	/**
	 * Represents a cached registration of an <tt>AssetLoader</tt>.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	protected static final class CachedAssetLoader implements
			CachedAssetExtension {

		/**
		 * Class of the cached <tt>AssetLoader</tt>.
		 */
		public final Class<? extends AssetLoader> clazz;

		/**
		 * Filename extension to which <tt>clazz</tt> will be bound
		 */
		public final String extension;

		/**
		 * Creates a new <tt>CachedAssetLoader</tt> that will register a
		 * <tt>AssetLoader</tt> class to an <tt>AssetManager</tt>.
		 * 
		 * @param clazz
		 *            the asset loader class to register
		 * @param extension
		 *            the filename extension to which <tt>clazz</tt> will be
		 *            bound
		 */
		public CachedAssetLoader(Class<? extends AssetLoader> clazz,
				String extension) {
			this.clazz = clazz;
			this.extension = extension;
		}

		public void register(AssetManager manager) {
			manager.registerLoader(clazz, extension);
		}

	}

	/**
	 * Represents a cached registration of on <tt>AssetLocator</tt>.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	protected static final class CachedAssetLocator implements
			CachedAssetExtension {

		/**
		 * Class of the cached <tt>AssetLocator</tt>.
		 */
		public final Class<? extends AssetLocator> clazz;

		/**
		 * Root path to which <tt>clazz</tt> will be registered.
		 */
		public final String rootPath;

		/**
		 * Creates a new <tt>CachedAssetLocator</tt> that will register a
		 * <tt>AssetLocator</tt> to an <tt>AssetManager</tt>.
		 * 
		 * @param clazz
		 *            the asset locator class to register
		 * @param rootPath
		 *            the root patch to which <tt>clazz</tt> will be bound
		 */
		public CachedAssetLocator(Class<? extends AssetLocator> clazz,
				String rootPath) {
			this.clazz = clazz;
			this.rootPath = rootPath;
		}

		public void register(AssetManager manager) {
			manager.registerLocator(rootPath, clazz);
		}

	}

}
