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

package mirrormonkey.core.module;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mirrormonkey.core.EntityRegistrationListener;
import mirrormonkey.core.InstanceInitializedEvent;
import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.core.InstanceRemovedEvent;
import mirrormonkey.core.InstanceReplacedEvent;
import mirrormonkey.core.annotations.CoreModulePresets;
import mirrormonkey.core.messages.EntityChangeMessage;
import mirrormonkey.core.messages.EntityEndMessage;
import mirrormonkey.core.messages.EntityInitMessage;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.connection.TimeSyncRequestListener;
import mirrormonkey.framework.connection.TimeSyncResponseListener;
import mirrormonkey.framework.connection.messages.TimeSyncRequestMessage;
import mirrormonkey.framework.connection.messages.TimeSyncResponseMessage;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.module.MirrorMonkeyModule;
import mirrormonkey.util.listeners.ListenerConfiguration;

import com.jme3.network.MessageConnection;
import com.jme3.network.serializing.Serializer;

/**
 * Common ancestor of the core modules. Provides capabilities that are needed on
 * both server and client.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <D>
 *            class that is locally used to store mutable data about entities
 * @param <A>
 *            local implementation of <tt>SyncAppState</tt>
 */
public abstract class CoreModule<D extends DynamicEntityData, A extends SyncAppState<?>>
		extends MirrorMonkeyModule<A> {

	/**
	 * Contains classes of all messages used by the core module.
	 */
	private static final Class<?>[] LIFECYCLE_MESSAGES = {
			EntityInitMessage.class, EntityEndMessage.class,
			EntityChangeMessage.class };

	/**
	 * Contains global <tt>InstanceLifecycleListeners</tt>, which will be
	 * notified for every entity lifecycle event.
	 */
	private final ListenerConfiguration listenerConfiguration;

	/**
	 * Contains class bound <tt>InstanceLifecycleListeners</tt>, which will be
	 * notified for specific entity lifecycle events where bound listeners are
	 * specified by the client-local and server-local entity class hierarchy.
	 */
	private final Map<Class<?>, ListenerConfiguration> specificListeners;

	/**
	 * Contains data about all locally known entities.
	 */
	private final EntityProvider<D> entityProvider;

	/**
	 * Responds to incoming <tt>TimeSyncRequestMessages</tt> to let connected
	 * core modules estimated local time of this core module.
	 */
	private final TimeSyncRequestListener syncRequestListener;

	/**
	 * Responds to incoming <tt>TimeSyncResponseMessages</tt> to estimate local
	 * time of connected core modules.
	 */
	private final TimeSyncResponseListener syncResponseListener;

	/**
	 * Collect <tt>InstanceLifecycleListeners</tt> into this set to avoid
	 * creating a new one every time listeners should be notified.
	 */
	private final SortedSet<InstanceLifecycleListener> collectListenersListSingleton;

	/**
	 * Same content as <tt>collectListenersListSingleton</tt>, but unmodifiable.
	 */
	private final SortedSet<InstanceLifecycleListener> returnListenersListSingleton;

	/**
	 * Contains listeners that will be notified when an entity is registered or
	 * unregistered.
	 */
	private final Set<EntityRegistrationListener<D>> entityRegListeners;

	/**
	 * Tracks registered <tt>UpdateAwareEntities</tt> so we can update them
	 * during our own update method.
	 */
	private final UpdateEntityTracker<D> updateTracker;

	/**
	 * Creates a new <tt>CoreModule</tt> with given <tt>SyncAppState</tt>
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> creating this <tt>CoreModule</tt>
	 * @param collectLocalConstructors
	 *            <tt>true</tt> if constructors of the local class should be
	 *            considered, <tt>false</tt> for constructors of the connected
	 *            class
	 */
	public CoreModule(A appState, boolean collectLocalConstructors) {
		super(appState);

		listenerConfiguration = new ListenerConfiguration();

		entityProvider = new EntityProvider<D>(appState,
				collectLocalConstructors);
		entityProvider.parsePresetClass(CoreModulePresets.class);

		for (Class<?> i : LIFECYCLE_MESSAGES) {
			Serializer.registerClass(i);
		}

		Serializer.registerClass(TimeSyncRequestMessage.class);
		Serializer.registerClass(TimeSyncResponseMessage.class);

		specificListeners = new HashMap<Class<?>, ListenerConfiguration>();

		syncRequestListener = new TimeSyncRequestListener(appState, this);
		appState.getEventManager().addMessageListener(syncRequestListener,
				TimeSyncRequestMessage.class);

		syncResponseListener = new TimeSyncResponseListener(this);
		appState.getEventManager().addMessageListener(syncResponseListener,
				TimeSyncResponseMessage.class);

		collectListenersListSingleton = new TreeSet<InstanceLifecycleListener>(
				new ListenerOrderComparator());
		returnListenersListSingleton = Collections
				.unmodifiableSortedSet(collectListenersListSingleton);

		entityRegListeners = new HashSet<EntityRegistrationListener<D>>();

		updateTracker = new UpdateEntityTracker<D>();
		addEntityRegistrationListener(updateTracker);
	}

	@Override
	public void update(float tpf) {
		updateTracker.update(tpf);
	}

	/**
	 * Adds a new <tt>EntityRegistrationListener</tt> that will be notified
	 * whenever entities become registered or unregistered.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addEntityRegistrationListener(
			EntityRegistrationListener<D> listener) {
		entityRegListeners.add(listener);
	}

	/**
	 * Removes a <tt>EntityRegistrationListener</tt>.
	 * 
	 * @param listener
	 *            the <tt>EntityRegistrationListener</tt> that will no longer be
	 *            notified if entities become registered or unregistered
	 */
	public void removeEntityRegistrationListener(
			EntityRegistrationListener<D> listener) {
		entityRegListeners.remove(listener);
	}

	/**
	 * Dispatches an entity registration event to all registered
	 * <tt>EntityRegistrationListers</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param data
	 *            <tt>DynamicEntityData</tt> representing the entity that was
	 *            just registered
	 */
	public void notifyEntityRegistration(D data) {
		final List<EntityRegistrationListener<D>> entityRegListeners = new LinkedList<EntityRegistrationListener<D>>();
		entityRegListeners.addAll(this.entityRegListeners);
		for (EntityRegistrationListener<D> i : entityRegListeners) {
			i.entityRegistered(data);
		}
	}

	/**
	 * Dispatches an entity removal event to all registered
	 * <tt>EntityRegistrationListeners</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param data
	 *            the <tt>DynamicEntityData</tt> representing the entity that
	 *            was just unregistered
	 */
	public void notifyEntityRemoval(D data) {
		final List<EntityRegistrationListener<D>> entityRegListeners = new LinkedList<EntityRegistrationListener<D>>();
		entityRegListeners.addAll(this.entityRegListeners);
		for (EntityRegistrationListener<D> i : entityRegListeners) {
			i.entityRemoved(data);
		}
	}

	/**
	 * Fetches the <tt>ListenerConfiguration</tt> that stores class bound
	 * listeners for a specific class.
	 * 
	 * @param notifyClass
	 *            the class that the fetched listeners are bound to
	 * @return the <tt>ListenerConfiguration</tt> storing listeners bound to
	 *         <tt>notifyClass</tt> or <tt>null</tt> if there are no
	 *         <tt>InstanceLifecycleListeners</tt> bound to <tt>notifyClass</tt>
	 */
	public ListenerConfiguration getListenerConfiguration(Class<?> notifyClass) {
		return specificListeners.get(notifyClass);
	}

	/**
	 * Adds an <tt>InstanceLifecycleListener</tt> and binds it to a number of
	 * classes.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> that will from now on
	 *            be notified when instance lifecycle events bound to
	 *            <tt>notifyClasses</tt> occur
	 * @param notifyClasses
	 *            the classes that <tt>listener</tt> will be bound to
	 */
	public void addInstanceLifecycleListener(
			InstanceLifecycleListener listener, Class<?>... notifyClasses) {
		for (Class<?> i : notifyClasses) {
			ListenerConfiguration previous = specificListeners.get(i);
			if (previous == null) {
				previous = new ListenerConfiguration();
				specificListeners.put(i, previous);
			}
			previous.addListener(listener);
		}
	}

	/**
	 * Removes an <tt>InstanceLifecycleListener</tt> bound to a number of
	 * specific classes.
	 * 
	 * @param listener
	 *            the <tt>InstanceLifecycleListener</tt> that will no longer be
	 *            notified when instance lifecycle events bound to
	 *            <tt>notifyClasses</tt> occur
	 * @param notifyClasses
	 *            the classes that <tt>listener</tt> will be unbound from
	 */
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener, Class<?>... notifyClasses) {
		for (Class<?> i : notifyClasses) {
			ListenerConfiguration previous = specificListeners.get(i);
			if (previous != null) {
				previous.removeListener(listener);
				if (previous.isEmpty()) {
					specificListeners.remove(i);
				}
			}
		}
	}

	/**
	 * Collects <tt>InstanceLifecycleListeners</tt> that must be notified for an
	 * instance lifecycle event on a given entity, context and connection.
	 * 
	 * @param entityData
	 *            the <tt>DynamicEntityData</tt> representing an entity for
	 *            which an instance lifecycle event has occurred
	 * @param staticEntityData
	 *            the <tt>StaticEntityData</tt> containing data about the
	 *            context in which the instance lifecycle event occurred
	 * @param info
	 *            the <tt>ConnectionInfo</tt> containing data about the
	 *            connection for which the instance lifecycle event occurred
	 * @return a reference to <tt>returnListenersListSingleton</tt>, which is
	 *         filled with listeners to notify by this method
	 */
	protected Collection<InstanceLifecycleListener> collectListeners(
			DynamicEntityData entityData, StaticEntityData staticEntityData,
			ConnectionInfo<?> info) {
		collectListenersListSingleton.clear();
		getListenerConfiguration().getListeners(
				InstanceLifecycleListener.class, collectListenersListSingleton);
		info.collectInstanceLifecycleListeners(collectListenersListSingleton);
		entityData
				.collectInstanceLifecycleListeners(collectListenersListSingleton);
		if (staticEntityData != null) {
			for (Class<?> c : staticEntityData.getNotifyClasses()) {
				ListenerConfiguration lc = specificListeners.get(c);
				if (lc != null) {
					lc.getListeners(InstanceLifecycleListener.class,
							collectListenersListSingleton);
				}
			}
		}
		return returnListenersListSingleton;
	}

	/**
	 * Collects <tt>InstanceLifecycleListeners</tt> that must be notified for a
	 * given <tt>InstanceInitializedEvent</tt> and dispatches the event to them.
	 * 
	 * @param e
	 *            the <tt>InstanceLifecycleEvent</tt> that occurred
	 */
	public void notifyInitListeners(InstanceInitializedEvent e) {
		for (InstanceLifecycleListener i : collectListeners(e.dynamicData,
				e.staticData, e.connection)) {
			i.instanceInitialized(e);
		}
	}

	/**
	 * Collects <tt>InstanceLifecycleListeners</tt> and
	 * <tt>EntityRegistrationListeners</tt> that must be notified for a given
	 * <tt>InstanceReplacedEvent</tt> and dispatches the event to them. Will
	 * dispatch the event to the <tt>instanceReplaced</tt> method of the
	 * <tt>InstanceLifecycleListeners</tt> listening to the old context and to
	 * the <tt>localInstanceChanged</tt> method of the concerned
	 * <tt>EntityRegistrationListeners</tt>.
	 * 
	 * The other <tt>notify*Listeners</tt> do not notify
	 * <tt>EntityRegistrationListeners</tt> since when to do that is dependent
	 * on whether we run server or client code.
	 * 
	 * @param e
	 *            the <tt>InstanceReplacedEvent</tt> that occurred
	 */
	@SuppressWarnings("unchecked")
	public void notifyReplacedListeners(InstanceReplacedEvent e) {
		if (e.isReferenceChanged()) {
			final List<EntityRegistrationListener<D>> entityRegListeners = new LinkedList<EntityRegistrationListener<D>>();
			entityRegListeners.addAll(this.entityRegListeners);
			for (EntityRegistrationListener<D> l : entityRegListeners) {
				l.localInstanceChanged((D) e.dynamicData, e.oldInstance,
						e.newInstance);
			}
		}
		// e.dynamicData in next line was newEntityData
		for (InstanceLifecycleListener i : collectListeners(e.dynamicData,
				e.oldStaticData, e.connection)) {
			i.instanceReplaced(e);
		}
	}

	/**
	 * Collects <tt>InstanceLifecycleListeners</tt> that must be notified for a
	 * given <tt>InstanceReplacedEvent</tt> and dispatches the event to their
	 * <tt>instanceReplacing</tt> method. Will notify listeners that listen to
	 * the new context.
	 * 
	 * @param e
	 *            the <tt>InstanceReplacedEvent</tt> that occurred
	 */
	public void notifyReplacingListeners(InstanceReplacedEvent e) {
		for (InstanceLifecycleListener i : collectListeners(e.dynamicData,
				e.newStaticData, e.connection)) {
			i.instanceReplacing(e);
		}
	}

	/**
	 * Collects <tt>InstanceLifecycleListeners</tt> that must be notified for a
	 * given <tt>InstanceRemovedEvent</tt> and dispatches the event to their
	 * <tt>instanceRemoved</tt> method.
	 * 
	 * @param e
	 *            the <tt>InstanceRemovedEvent</tt> that occurred
	 */
	public void notifyEndListeners(InstanceRemovedEvent e) {
		for (InstanceLifecycleListener i : collectListeners(e.dynamicData,
				e.staticData, e.connection)) {
			i.instanceRemoved(e);
		}
	}

	/**
	 * @return the <tt>EntityProvider</tt> containing the
	 *         <tt>DynamicEntityData</tt> and <tt>StaticEntityData</tt>
	 *         instances storing data about the entities known locally
	 */
	public EntityProvider<D> getEntityProvider() {
		return entityProvider;
	}

	/**
	 * Fetches the <tt>ConnectionInfo</tt> storing data about a given
	 * <tt>MessageConnection</tt> (may create a dummy element if that data
	 * doesn't exist yet).
	 * 
	 * @param connection
	 *            the <tt>MessageConnection</tt> to collect data for
	 * @return the <tt>ConnectionInfo</tt> storing data about
	 *         <tt>connection</tt>
	 */
	public abstract ConnectionInfo<?> getData(MessageConnection connection);

	/**
	 * @return the <tt>ListenerConfiguration</tt> containing <b>global</b>
	 *         <tt>InstanceLifecycleListeners</tt>
	 */
	public ListenerConfiguration getListenerConfiguration() {
		return listenerConfiguration;
	}

	/**
	 * Sorts <tt>InstanceLifecycleListeners</tt> in ascending order according to
	 * the values returned by their <tt>getSortingOrder</tt> methods.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private static final class ListenerOrderComparator implements
			Comparator<InstanceLifecycleListener> {

		/**
		 * Creates a new <tt>ListenerOrderComparator</tt>.
		 */
		public ListenerOrderComparator() {
		}

		public int compare(InstanceLifecycleListener o1,
				InstanceLifecycleListener o2) {
			if (o1.getSortingOrder() != o2.getSortingOrder()) {
				return o1.getSortingOrder() - o2.getSortingOrder();
			}
			return System.identityHashCode(o1) - System.identityHashCode(o2);
		}

	}
}