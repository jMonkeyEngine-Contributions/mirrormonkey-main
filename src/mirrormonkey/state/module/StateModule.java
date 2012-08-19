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

package mirrormonkey.state.module;

import java.util.HashMap;
import java.util.Map;

import mirrormonkey.core.InstanceInitializedEvent;
import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.core.InstanceRemovedEvent;
import mirrormonkey.core.InstanceReplacedEvent;
import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.StaticDataKey;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.module.MirrorMonkeyModule;
import mirrormonkey.state.annotations.StateAnnotationPresets;
import mirrormonkey.state.messages.UpdateMessage;

import com.jme3.network.serializing.Serializer;

/**
 * Glues the different classes of the state module together.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StateModule extends MirrorMonkeyModule<SyncAppState<?>> implements
		InstanceLifecycleListener {

	/**
	 * Contains data about when to update which fields.
	 */
	protected final Map<StaticDataKey, StaticUpdateData> staticData;

	/**
	 * Contains data about when to update which fields.
	 */
	protected final UpdateQueue queue;

	/**
	 * Contains the last sync time at which this module has been updated.
	 */
	protected long lastTime;

	/**
	 * Creates a new <tt>StateModule</tt> for a given <tt>SyncAppState</tt>.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> calling this constructor
	 */
	@SuppressWarnings("unchecked")
	public StateModule(SyncAppState<?> appState) {
		super(appState);

		Serializer.registerClass(UpdateMessage.class);
		appState.getModule(CoreModule.class).getEntityProvider()
				.parsePresetClass(StateAnnotationPresets.class);

		staticData = new HashMap<StaticDataKey, StaticUpdateData>();

		queue = new UpdateQueue(this);
		appState.getEventManager().addMessageListener(
				new StateMessageListener(appState), UpdateMessage.class);
		appState.getModule(CoreModule.class).getListenerConfiguration()
				.addListener(this);
		lastTime = appState.getSyncTime();
	}

	/**
	 * Gets an instance of <tt>StaticUpdateData</tt> associated with an instance
	 * of <tt>StaticEntityData</tt>.
	 * 
	 * @param sd
	 *            instance of <tt>StaticEntityData</tt>
	 * @param create
	 *            <tt>true</tt> if a new instance of <tt>StaticUpdateData</tt>
	 *            should be created if none was found, <tt>false</tt> otherwise.
	 * @return the instance of <tt>StaticUpdateData</tt> associated with
	 *         <tt>sd</tt> or <tt>null</tt> if there is no such instance and
	 *         <tt>create</tt> was set to <tt>false</tt>
	 */
	private StaticUpdateData get(StaticEntityData sd, boolean create) {
		StaticDataKey key = new StaticDataKey(sd.getLocalClass(),
				sd.getConnectedClass());
		StaticUpdateData sud = staticData.get(key);
		if (sud == null && create) {
			sud = new StaticUpdateData(getAppState(), sd, queue);
			staticData.put(key, sud);
		}
		return sud;
	}

	/**
	 * Starts updating an entity for a given connection.
	 * 
	 * @param entity
	 *            local instance of the entity for which update messages should
	 *            be created
	 * @param info
	 *            data about the connection that update messages should be sent
	 *            to
	 */
	protected void add(SyncEntity entity, ConnectionInfo<?> info) {
		StaticEntityData sd = entity.getData().getActiveStaticData(
				info.getConnection());
		get(sd, true).add(entity.getData(), info);
	}

	/**
	 * Stops updating an entity for a given connection.
	 * 
	 * @param entity
	 *            local instance of the entity that should no longer be
	 *            synchronized with the connection
	 * @param info
	 *            data about the connection for which <tt>entity</tt> should no
	 *            longer be synchronized with its connected instance
	 */
	private void remove(SyncEntity entity, ConnectionInfo<?> info) {
		remove(entity.getData().getId(), info, entity.getData()
				.getActiveStaticData(info.getConnection()));
	}

	/**
	 * Stops synchronizing an entity with a given connection.
	 * 
	 * @param entityId
	 *            unique ID of the entity
	 * @param info
	 *            data about the connection that the entity should no longer be
	 *            synchronized with
	 * @param staticData
	 *            context in which the entity was synchronized
	 */
	protected void remove(Integer entityId, ConnectionInfo<?> info,
			StaticEntityData staticData) {
		StaticUpdateData d = get(staticData, false);
		if (d != null) {
			get(staticData, false).remove(entityId, info);
		}
	}

	@Override
	public void update(float tpf) {
		update(getAppState().getSyncTime());
	}

	/**
	 * Checks if any update messages should be sent this frame.
	 * 
	 * @param newTime
	 *            current local sync time
	 */
	public void update(long newTime) {
		queue.update(newTime);
		lastTime = newTime;
	}

	public void instanceInitialized(InstanceInitializedEvent e) {
		add(e.instance, e.connection);
	}

	public void instanceReplaced(InstanceReplacedEvent e) {
		remove(e.dynamicData.getId(), e.connection, e.oldStaticData);
	}

	public void instanceReplacing(InstanceReplacedEvent e) {
		add(e.newInstance, e.connection);
	}

	public void instanceRemoved(InstanceRemovedEvent e) {
		remove(e.instance, e.connection);
	}

	public int getSortingOrder() {
		return FRAMEWORK_MAX_SORTING_ORDER;
	}

	public void setSortingOrder(int sortingOrder) {
	}
}
