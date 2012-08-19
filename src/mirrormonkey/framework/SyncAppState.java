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

package mirrormonkey.framework;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import mirrormonkey.framework.module.MirrorMonkeyModule;
import mirrormonkey.util.asset.AssetExtensionCache;
import mirrormonkey.util.netevent.EventManager;
import mirrormonkey.util.serializer.ColorRGBASerializer;
import mirrormonkey.util.serializer.QuaternionSerializer;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.RenderManager;
import com.jme3.system.NanoTimer;
import com.jme3.system.Timer;

/**
 * Responsible for managing modules, message listeners and the local kind of
 * connection listeners.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <M>
 *            class of the <tt>EventManager</tt> to use - will most likely be
 *            <tt>ClientEventManager</tt> or <tt>ServerEventManager</tt>
 */
public abstract class SyncAppState<M extends EventManager> implements AppState {

	/**
	 * Will dispatch messages and connection events.
	 */
	private final M eventManager;

	/**
	 * Contains loaded modules, mapped by their classes.
	 * 
	 * If a module is is not a direct subclass of <tt>MirrorMonkeyModule</tt>,
	 * then it will be mapped by every class in its class hierarchy up to (and
	 * excluding) <tt>MirrorMonkeyModule</tt>.
	 */
	private final Map<Class<?>, MirrorMonkeyModule<?>> modules;

	/**
	 * Define own <tt>Timer</tt> to... keep track of time.
	 */
	private final Timer timer;

	/**
	 * Manages asset loader and and asset locator classes that are cached while
	 * no <tt>AssetManager</tt> is available yet.
	 */
	private final AssetExtensionCache assetExtensionCache;

	/**
	 * The main application, once it becomes available.
	 */
	private Application application;

	/**
	 * True while this <tt>SyncAppState</tt> is initialized (as in, an
	 * <tt>Application</tt> is present).
	 */
	private boolean initialized;

	/**
	 * True while this <tt>SyncAppState</tt> updates the application state and
	 * dispatches incoming message and connection events on call of the
	 * <tt>update</tt> method.
	 */
	private boolean enabled;

	/**
	 * Creates a new <tt>SyncAppState</tt> with given <tt>EventManager</tt>.
	 * 
	 * @param eventManager
	 *            the event manager that will be used to dispach message and
	 *            connection events
	 */
	public SyncAppState(M eventManager) {
		Serializer.registerClass(Quaternion.class, new QuaternionSerializer());
		Serializer.registerClass(ColorRGBA.class, new ColorRGBASerializer());
		this.eventManager = eventManager;
		modules = new HashMap<Class<?>, MirrorMonkeyModule<?>>();
		timer = new NanoTimer();
		assetExtensionCache = new AssetExtensionCache();
		initialized = false;
		enabled = true;
	}

	/**
	 * Returns a reference to the loaded module for a specific module class.
	 * 
	 * If no such reference is found, the method tries to instantiate the
	 * module, register it and return the reference. The module is instantiated
	 * by reflectively calling a one-parameter constructor with parameter type
	 * compatible to <tt>SyncAppState</tt>. The constructor that will require
	 * the lowest amount of upcasts to insert this <tt>SyncAppState</tt> as
	 * parameter will be called with this <tt>SyncAppState</tt> as parameter.
	 * 
	 * Modules will be mapped for further calls of this method to the same
	 * module class or any class that it extends, up to and excluding
	 * <tt>MirrorMonkeyModule</tt>.
	 * 
	 * @param moduleClass
	 *            the class of the module to load or fetch
	 * @return the module instance registered for class <tt>moduleClass</tt>
	 * @throws IllegalStateException
	 *             (wrapped in <tt>RuntimeException</tt>) if a new instance must
	 *             be created, but there is already a module registered for
	 *             <tt>moduleClass</tt> or one of the classes it extends up to
	 *             and excluding <tt>MirrorMonkeyModule</tt>
	 * @throws NoSuchMethodException
	 *             (wrapped in <tt>RuntimeException</tt>) if a new instance must
	 *             be created, but no single-argument constructor where the
	 *             argument type is assignable from the dynamic class of this
	 *             <tt>SyncAppState</tt> could be found
	 */
	@SuppressWarnings({ "javadoc", "unchecked" })
	public <T extends MirrorMonkeyModule<?>> T getModule(Class<T> moduleClass) {
		try {
			T module = (T) modules.get(moduleClass);
			if (module == null) {
				ConstrSearch: for (Class<?> c = getClass(); !c
						.equals(Object.class); c = c.getSuperclass()) {
					for (Constructor<?> constr : moduleClass.getConstructors()) {
						if (constr.getParameterTypes().length == 1
								&& constr.getParameterTypes()[0].equals(c)) {
							module = (T) constr.newInstance(this);
							break ConstrSearch;
						}
					}

				}
				if (module == null) {
					throw new NoSuchMethodException(
							"Could not find constructor for module class: "
									+ moduleClass + "\nmodules present: "
									+ modules);
				}
				for (Class<?> i = moduleClass; !i
						.equals(MirrorMonkeyModule.class); i = i
						.getSuperclass()) {
					if (modules.containsKey(i)) {
						throw new IllegalStateException("Module class " + i
								+ " already registered. Module: "
								+ modules.get(i));
					}
					modules.put(i, module);
				}
			}
			return module;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update(float tpf) {
		eventManager.processEvents();
		for (MirrorMonkeyModule<?> i : modules.values()) {
			i.update(tpf);
		}
		timer.update();
	}

	/**
	 * @return the <tt>Application</tt> that this <tt>SyncAppState</tt> is
	 *         initialized for, or <tt>null</tt> if this <tt>SyncAppState</tt>
	 *         is not initialized
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * @return the <tt>AssetExtensionCache</tt> responsible for caching asset
	 *         loader and asset locator class while no <tt>AssetManager</tt> is
	 *         available
	 */
	public AssetExtensionCache getAssetCache() {
		return assetExtensionCache;
	}

	/**
	 * @return the current time, as denoted by this <tt>SyncAppState's</tt>
	 *         timer
	 */
	public final long getSyncTime() {
		return timer.getTime();
	}

	/**
	 * @return the <tt>EventManager</tt> responsible to dispatch messages and
	 *         connection events
	 */
	public M getEventManager() {
		return eventManager;
	}

	public void cleanup() {
		assetExtensionCache.setApplication(null);
		initialized = false;
	}

	public void initialize(AppStateManager arg0, Application arg1) {
		application = arg1;
		initialized = true;
		assetExtensionCache.setApplication(arg1);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void postRender() {
	}

	public void render(RenderManager arg0) {
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void stateAttached(AppStateManager arg0) {
	}

	public void stateDetached(AppStateManager arg0) {
	}

}
