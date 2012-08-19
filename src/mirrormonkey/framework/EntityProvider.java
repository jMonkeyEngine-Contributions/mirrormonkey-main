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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mirrormonkey.framework.annotations.AssetInjection;
import mirrormonkey.framework.annotations.EntityInjection;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticDataKey;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.StaticEntityDataIR;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.parameter.AssetInterpreter;
import mirrormonkey.framework.parameter.CustomParameterIR;
import mirrormonkey.framework.parameter.IdentityAwareInterpreter;
import mirrormonkey.framework.parameter.NullInterpreter;
import mirrormonkey.framework.parameter.ValueInterpreter;
import mirrormonkey.util.annotations.parsing.AnnotationParser;
import mirrormonkey.util.annotations.parsing.ConstructorKeyDynamic;
import mirrormonkey.util.annotations.parsing.FieldKeyStatic;
import mirrormonkey.util.annotations.parsing.MemberIR;
import mirrormonkey.util.annotations.parsing.MethodKeyDynamic;
import mirrormonkey.util.annotations.parsing.ParameterIR;

import com.jme3.asset.Asset;
import com.jme3.asset.AssetKey;

/**
 * Responsible of keeping track of data about mappings between local and
 * connected entity classes (<tt>StaticEntityData</tt>) and derived, mutable
 * data for every entity instance (<tt>DynamicEntityData</tt>). Manages the
 * abstraction layer that differentiates between the presence or absence of the
 * annotations <tt>AssetInjection</tt> and <tt>EntityInjection</tt>.
 * Implementations and definitions of that abstraction layer can be found in the
 * package <tt>mirrormonkey.framework.parameter</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <D>
 *            different core modules may have different local classes to keep
 *            track of mutable entity instance data
 */
public class EntityProvider<D extends DynamicEntityData> {

	/**
	 * Error message of the <tt>IllegalArgumentException</tt> that will be
	 * thrown by <tt>assertNotOutdated</tt> if its argument is outdated.
	 */
	private static final String OUTDATED_DATA_ERROR = "Outdated entity data. Please make sure to always fetch a fresh "
			+ "instance of DynamicEntityData from the core module by calling its getData method.";

	/**
	 * Used to identify <tt>null</tt> values for entity injection over the
	 * network.
	 */
	public static final Integer NULL_ID = -1;

	/**
	 * Contains data about mappings between local entity classes and connected
	 * entity classes. That data is immutable.
	 * 
	 * Once static data has been added to this map, it will never be removed
	 * because re-parsing the classes is quite slow.
	 */
	private final Map<StaticDataKey, StaticEntityData> staticData;

	/**
	 * Contains mutable data about specific entity instances.
	 * 
	 * In contrast to <tt>staticData</tt>, data will be dynamically registered
	 * and unregistered from and to this map as needed.
	 */
	private final Map<Integer, D> dynamicData;

	/**
	 * Used to parse class hierarchies and create data for <tt>staticData</tt>.
	 */
	private final AnnotationParser parser;

	/**
	 * Singleton that will not pack or unpack method parameters, return values
	 * etc.; will be used when neither <tt>EntityInjection</tt> nor
	 * <tt>AssetInjection</tt> is used on the element in question.
	 */
	private final ValueInterpreter serializingInterpreter;

	/**
	 * Singleton that will pack method parameters, return values etc. to entity
	 * IDs and inject entities for IDs received from remote; will be used when
	 * <tt>EntityInjection</tt> is used on the element in question.
	 */
	private final ValueInterpreter identityInterpreter;

	/**
	 * Singleton that will pack method parameters, return value etc. to
	 * <tt>AssetKeys</tt> using their <tt>getKey</tt> method and inject assets
	 * when keys are received from remote; will be used when
	 * <tt>AssetInjection</tt> is used on the element in question.
	 */
	private final AssetInterpreter assetInterpreter;

	/**
	 * The <tt>SyncAppState</tt> that manages local synchronization.
	 */
	private final SyncAppState<?> appState;

	/**
	 * This will be <tt>true</tt> if local constructors should be collected for
	 * the creation of <tt>StaticEntityData</tt> instances (basically on the
	 * client side) and <tt>false</tt> when constructors of connected classes
	 * should be used (basically on the server side).
	 * 
	 * Having this field here instead of tracking that stuff in the core module
	 * is breaking loose coupling, but it's also much easier.
	 */
	private final boolean collectLocalConstructors;

	/**
	 * Creates a new <tt>EntityProvider</tt> using the default
	 * <tt>AnnotationParser</tt> and a given <tt>SyncAppState</tt>.
	 * 
	 * @param appState
	 *            the local <tt>SyncAppState</tt> that will manage this
	 *            <tt>EntityProvider</tt>
	 * @param collectLocalConstructors
	 *            <tt>true</tt>, if local class constructors should be
	 *            considered when creating new instances of
	 *            <tt>StaticEntityData</tt>, <tt>false</tt> if connected class
	 *            constructors should be considered. Will be <tt>true</tt> on
	 *            the client side, <tt>false</tt> on the server side.
	 */
	public EntityProvider(SyncAppState<?> appState,
			boolean collectLocalConstructors) {
		this(new AnnotationParser(), appState, collectLocalConstructors);
	}

	/**
	 * Creates a new <tt>EntityProvider</tt> using a given
	 * <tt>AnnotationParser</tt> and <tt>SyncAppState</tt>.
	 * 
	 * @param parser
	 *            the <tt>AnnotationParser</tt> that will be used to create new
	 *            instances of <tt>StaticEntityData</tt>
	 * @param appState
	 *            the local <tt>SyncAppState</tt> that will manage this
	 *            <tt>EntityProvider</tt>
	 * @param collectLocalConstructors
	 *            <tt>true</tt>, if local class constructors should be
	 *            considered when creating new instances of
	 *            <tt>StaticEntityData</tt>, <tt>false</tt> if connected class
	 *            constructors should be considered. Will be <tt>true</tt> on
	 *            the client side, <tt>false</tt> on the server side.
	 */
	public EntityProvider(AnnotationParser parser, SyncAppState<?> appState,
			boolean collectLocalConstructors) {
		this.appState = appState;
		staticData = new HashMap<StaticDataKey, StaticEntityData>();
		dynamicData = new HashMap<Integer, D>();
		this.parser = parser;
		parser.addKeyClass(Constructor.class, ConstructorKeyDynamic.class);
		parser.addKeyClass(Method.class, MethodKeyDynamic.class);
		parser.addKeyClass(Field.class, FieldKeyStatic.class);
		parser.setClassIRClass(StaticEntityDataIR.class);
		parser.setParameterIRClass(CustomParameterIR.class);
		serializingInterpreter = new NullInterpreter();
		identityInterpreter = new IdentityAwareInterpreter(this);
		assetInterpreter = new AssetInterpreter(appState);
		this.collectLocalConstructors = collectLocalConstructors;
	}

	/**
	 * Fetches the <tt>StaticEntityData</tt> that contains a description of how
	 * to handle an entity based on its local and connected class. If no such
	 * data is present, <tt>localClass</tt> and <tt>connectedClass</tt> will be
	 * parsed to extract data and it will be present from then on.
	 * 
	 * @param localClass
	 *            local entity class
	 * @param connectedClass
	 *            connected entity class
	 * @return information on how to handle an entity if its local class is
	 *         <tt>localClass</tt> and its connected class is
	 *         <tt>connectedClass</tt>
	 */
	public StaticEntityData getStaticData(
			Class<? extends SyncEntity> localClass,
			Class<? extends SyncEntity> connectedClass) {
		return getStaticData(localClass, connectedClass, true);
	}

	/**
	 * Fetches the <tt>StaticEntityData</tt> that contains a description of how
	 * to handle an entity based on its local and connected class. If no such
	 * data is present and <tt>add</tt> is <tt>false</tt>, then <tt>null</tt>
	 * will be returned. If <tt>add</tt> is <tt>false</tt> and no data can be
	 * found, it will be created and available from then on.
	 * 
	 * @param localClass
	 *            local entity class
	 * @param connectedClass
	 *            connected entity class
	 * @param add
	 *            <tt>true</tt> if data should be created and added if none is
	 *            found
	 * @return either information on how to handle an entity if its local class
	 *         is <tt>localClass</tt> and its connected class is
	 *         <tt>connectedClass</tt> or <tt>null</tt> if no such information
	 *         is present and <tt>add</tt> was set to <tt>false</tt>
	 */
	public StaticEntityData getStaticData(
			Class<? extends SyncEntity> localClass,
			Class<? extends SyncEntity> connectedClass, boolean add) {
		return getStaticData(new StaticDataKey(localClass, connectedClass), add);
	}

	/**
	 * Fetches the <tt>StaticEntityData</tt> that contains a description of how
	 * to handle an entity based on its local and connected class, as
	 * represented by a <tt>StaticDataKey</tt>. If no such data is present and
	 * <tt>add</tt> is set to <tt>false</tt>, then <tt>null</tt> will be
	 * returned. If <tt>add</tt> is set to <tt>false</tt> and no data can be
	 * found, it will be created and available from then on.
	 * 
	 * @param key
	 *            the <tt>StaticDataKey</tt> representing the local and
	 *            connected entity classes
	 * @param add
	 *            <tt>true if data should be created and added if none is found
	 * @return either information on how to handle an entity if its local class
	 *         is <tt>localClass</tt> and its connected class is
	 *         <tt>connectedClass</tt> or <tt>null</tt> if no such information
	 *         is present and <tt>add</tt> was set to <tt>false</tt>
	 */
	public StaticEntityData getStaticData(StaticDataKey key, boolean add) {
		StaticEntityData data = staticData.get(key);
		if (data == null) {
			StaticEntityDataIR ir = (StaticEntityDataIR) parser
					.parseClass(key.localClass);
			StaticEntityDataIR connectedIR = (StaticEntityDataIR) parser
					.parseClass(key.connectedClass);
			data = ir.extractData(appState, connectedIR, this,
					collectLocalConstructors);
			if (add) {
				addStaticData(key, data);
			}
		}
		return data;
	}

	/**
	 * Adds information on how to handle an entity with local and connected
	 * class as represented by <tt>key</tt> to this <tt>EntityProvider</tt>.
	 * 
	 * @param key
	 *            the <tt>StaticDataKey</tt> representing a mapping between a
	 *            local and connected entity class
	 * @param data
	 *            information on how to handle entities that have that mapping
	 *            on top of their mapping stack
	 */
	public void addStaticData(StaticDataKey key, StaticEntityData data) {
		staticData.put(key, data);
	}

	/**
	 * Parses a preset class so that the preset annotation types defined therein
	 * will be regarded in the future when creating instances of
	 * <tt>StaticEntityData</tt> with the parser.
	 * 
	 * @param clazz
	 *            the class containing preset annotation definitions with the
	 *            <tt>DefinePreset</tt> annotation
	 */
	public void parsePresetClass(Class<?> clazz) {
		parser.parsePresetClass(clazz);
	}

	@Override
	public String toString() {
		return "[EntityProvider dynamicData=" + dynamicData.toString() + "]";
	}

	/**
	 * Registers dynamic (mutable) data about an entity that will be returned by
	 * <tt>getData</tt> for the entity's IR from then on.
	 * 
	 * Is called by the core modules when the top of the mapping stack for an
	 * entity has changed.
	 * 
	 * @param data
	 *            the instance of <tt>DynamicEntityData</tt> that contains
	 *            dynamic data about an entity and a link to its immutable
	 *            <tt>StaticEntityData</tt>.
	 */
	public void registerData(D data) {
		dynamicData.put(Integer.valueOf(data.getId()), data);
	}

	/**
	 * Returns dynamic (mutable) data registered for an entity with specific ID
	 * or <tt>null</tt> if no data is present for that ID. Please note that this
	 * may return actual data even if the entity is not registered, as data such
	 * as added <tt>InstanceLifecycleListeners</tt> can be present even then.
	 * 
	 * @param entityId
	 *            the ID of the entity
	 * @return dynamic data about the entity with id <tt>entityId</tt> or
	 *         <tt>null</tt> if no such data is found
	 */
	public D getData(int entityId) {
		return dynamicData.get(Integer.valueOf(entityId));
	}

	/**
	 * Removes dynamic (mutable) data registered for an entity so that it will
	 * no longer be returned by <tt>getData</tt>.
	 * 
	 * @param data
	 *            the dynamic data to remove
	 */
	public void removeData(DynamicEntityData data) {
		removeData(data.getId());
	}

	/**
	 * Removes dynamic (mutable) data registered for an entity with a specific
	 * ID so that it will no longer be returned by <tt>getData</tt>.
	 * 
	 * @param entityId
	 *            the ID of the entity to remove data of
	 */
	public void removeData(int entityId) {
		dynamicData.remove(Integer.valueOf(entityId));
	}

	/**
	 * Fetches the <tt>ParameterInterpreter</tt> singleton for a member,
	 * according to its declared <tt>AssetInjection</tt> or
	 * <tt>EntityInjection</tt> annotations or lack thereof.
	 * 
	 * Throws an <tt>IllegalArgumentException</tt> if the annotations present
	 * can not possibly work on the member, but note that since e.g. things like
	 * unassignable local and connected entity classes are possible, an
	 * <tt>IllegalArgumentException</tt> will not always be thrown.
	 * 
	 * @param ir
	 *            the <tt>MemberIR</tt> that was acquired for the member when
	 *            parsing the entity class hierarchy
	 * @param type
	 *            the type of the member (type of fields or return types of
	 *            methods)
	 * @return the <tt>ParameterInterpreter</tt> that handles interpreting the
	 *         member for network communication
	 * @throws IllegalArgumentException
	 *             if there is no possible way for the present
	 *             <tt>EntityInjection</tt> and / or <tt>AssetInjection</tt>
	 *             annotations to work with the member
	 */
	public ValueInterpreter getInterpreter(MemberIR ir, Class<?> type) {
		EntityInjection ia = ir.getCollectedAnnotation(EntityInjection.class);
		AssetInjection ai = ir.getCollectedAnnotation(AssetInjection.class);
		if (ia == null && ai == null) {
			return serializingInterpreter;
		} else if (ia != null && ai != null) {
			throw new IllegalArgumentException(
					"You can not declare both EntityInjection and AssetInjection "
							+ " on member " + ir);
		} else if (ia != null) {
			return identityInterpreter;
		} else if (Asset.class.isAssignableFrom(type)
				|| assetInterpreter.isRegisteredAssetLike(type)) {
			return assetInterpreter;
		} else {
			throw new IllegalArgumentException(
					ir
							+ " declares AssetInjection but its type is neither "
							+ "assignable to Asset nor registered to the assetInterpreter.");
		}
	}

	/**
	 * Fetches the <tt>ParameterInterpreter</tt> singleton for a method
	 * parameter, according to its declared <tt>AssetInjection</tt> or
	 * <tt>EntityInjection</tt> annotations or lack thereof.
	 * 
	 * Throws an <tt>IllegalArgumentException</tt> if the annotations present
	 * can not possibly work on the parameter, but note that since e.g. things
	 * like unassignable local and connected entity classes are possible, an
	 * <tt>IllegalArgumentException</tt> will not always be thrown.
	 * 
	 * @param ir
	 *            the <tt>ParameterIR</tt> that was acquired for the parameter
	 *            when parsing the entity class hierarchy
	 * @return the <tt>ParameterInterpreter</tt> that handles interpreting the
	 *         parameter for network communication
	 * @throws IllegalArgumentException
	 *             if there is no possible way for the present
	 *             <tt>EntityInjection</tt> and / or <tt>AssetInjection</tt>
	 *             annotations to work with the parameter
	 */
	public ValueInterpreter getInterpreter(ParameterIR ir) {
		EntityInjection ia = ir.getCollectedAnnotation(EntityInjection.class);
		AssetInjection ai = ir.getCollectedAnnotation(AssetInjection.class);
		if (ia == null && ai == null) {
			return serializingInterpreter;
		} else if (ia != null && ai != null) {
			throw new IllegalArgumentException(
					"You can not declare both EntityInjection and AssetInjection "
							+ " on parameter "
							+ ir.getCollectedParameter().getParameterId());
		} else if (ia != null) {
			return identityInterpreter;
		} else if (Asset.class.isAssignableFrom(ir.getCollectedParameter()
				.getParameterType())
				|| AssetKey.class.isAssignableFrom(ir.getCollectedParameter()
						.getParameterType())
				|| assetInterpreter.isRegisteredAssetLike(ir
						.getCollectedParameter().getParameterType())) {
			return assetInterpreter;
		} else {
			throw new IllegalArgumentException(ir
					+ " declares @AssetInjection on parameter "
					+ ir.getCollectedParameter().getParameterId()
					+ " but that parameter is neither an Asset nor registered "
					+ "to the AssetInterpreter");
		}
	}

	/**
	 * Convenience method that fetches the <tt>ParameterInterpreter</tt>
	 * singletons for a whole list of parameters.
	 * 
	 * @param paramIRs
	 *            the <tt>ParameterIRs</tt> that were acquired for a method when
	 *            parsing the entity class hierarchy
	 * @return the <tt>ParameterInterpreter</tt> singletons responsible for
	 *         handling the parameters for network communication, as determined
	 *         by calls to <tt>getInterpreter(ParameterIR)</tt> for every
	 *         <tt>ParameterIR</tt> in <tt>paramIRs</tt>
	 */
	public ValueInterpreter[] getInterpreters(ParameterIR[] paramIRs) {
		ValueInterpreter[] interpreters = new ValueInterpreter[paramIRs.length];
		int i = 0;
		for (ParameterIR ir : paramIRs) {
			interpreters[i++] = getInterpreter(ir);
		}
		return interpreters;
	}

	/**
	 * Returns regularly if the given <tt>DynamicEntityData</tt> is not outdated
	 * and throws an <tt>IllegalStateException</tt> otherwise.
	 * 
	 * A <tt>DynamicEntityData</tt> is considered outdated if its entity is
	 * registered, but the checked data is not the <tt>DynamicEntityData</tt>
	 * that is currently used to keep track of the entity's dynamic data.
	 * 
	 * @param entityData
	 *            the <tt>DynamicEntityData</tt> to be checked
	 * @throws IllegalStateException
	 *             if the entity represented by <tt>entityData</tt> is
	 *             registered, but <tt>entityData</tt> is not its actual
	 *             <tt>DynamicEntityData</tt> instance.
	 */
	public void assertNotOutdated(D entityData) {
		if (!entityData.isNotDummy()) {
			if (entityData.isEntityPresent()) {
				if (entityData.getLocalInstance().getData() != null
						&& entityData.getLocalInstance().getData() != entityData) {
					throw new IllegalStateException(OUTDATED_DATA_ERROR);
				}
			} else if (dynamicData.get(entityData.getId()) != null) {
				throw new IllegalStateException(OUTDATED_DATA_ERROR);
			}
		} else {
			if (entityData.isEntityPresent()) {
				if (entityData.getLocalInstance().getData() != entityData) {
					throw new IllegalStateException(OUTDATED_DATA_ERROR);
				}
			} else if (dynamicData.get(entityData.getId()) != entityData) {
				throw new IllegalStateException(OUTDATED_DATA_ERROR);
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if, and only if, there is no dynamic data for any
	 * entity at the moment.
	 * 
	 * @return <tt>true</tt> if there is no dynamic data present for any entity
	 *         at the moment, <tt>false</tt> otherwise
	 */
	public boolean hasEntities() {
		return !dynamicData.isEmpty();
	}

	/**
	 * Returns every dynamic entity data that is present at the moment. Please
	 * note that for an instance of <tt>DynamicEntityData</tt> to be present, it
	 * is not necessarily needed for its entity to be registered.
	 * 
	 * @return a <tt>collection</tt> containing every instance of
	 *         <tt>DynamicEntityData</tt> that is currently present
	 */
	public Collection<D> getEntities() {
		return dynamicData.values();
	}

	/**
	 * Returns the <tt>AssetInterpreter</tt> singleton used by this
	 * <tt>EntityProvider</tt>.
	 * 
	 * This is used (and needed) because in contrast to the other
	 * <tt>ParameterInterpreter</tt> singletons the <tt>AssetInterpreter</tt>
	 * needs additional configuration.
	 * 
	 * @return the <tt>AssetInterpreter</tt> singleton that is used to manage
	 *         values of methods, fields and parameters for network
	 *         communication
	 */
	public AssetInterpreter getAssetInterpreter() {
		return assetInterpreter;
	}
}
