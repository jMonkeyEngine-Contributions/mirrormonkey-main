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

package mirrormonkey.state.member;

import mirrormonkey.core.InstanceInitializedEvent;
import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.core.InstanceRemovedEvent;
import mirrormonkey.core.InstanceReplacedEvent;
import mirrormonkey.core.client.ClientCoreModule;
import mirrormonkey.core.client.ClientEntityData;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Contains dynamic (instance-level) data about fields that use value tracking
 * as well as entity tracking.
 * 
 * This class ensures that as soon as an entity becomes visible or invisible
 * locally and value tracking is used, the value of the represented field will
 * remain consistent with entity visibility.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class EntityDynamicUpdateData extends TrackingDynamicUpdateData {

	/**
	 * Listens to lifecycle events for the instances of the entity that the
	 * represented field is set to.
	 */
	private final InstanceLifecycleListener valueChangeListener;

	/**
	 * Keeps data about entities.
	 */
	private final ClientCoreModule coreModule;

	/**
	 * Dynamic data about the entity that the represented field is currently set
	 * to.
	 */
	private ClientEntityData currentValue;

	/**
	 * Creates a new <tt>EntityDynamicUpdateData</tt> for a given field and
	 * entity.
	 * 
	 * @param fieldData
	 *            static data about the represented field
	 * @param entity
	 *            dynamic data about the entity
	 * @param coreModule
	 *            contains data about entities that the represented field can be
	 *            set to
	 */
	public EntityDynamicUpdateData(StaticUpdateMemberData fieldData,
			DynamicEntityData entity, ClientCoreModule coreModule) {
		super(fieldData, entity);
		valueChangeListener = new ValueChangeListener();
		entity.addInstanceLifecycleListener(new OwnEntityStateListener());
		this.coreModule = coreModule;
		currentValue = null;
	}

	@Override
	public void setFromRemote(Object value, long lastTime) {
		if (this.lastTime >= lastTime) {
			return;
		}
		this.lastTime = lastTime;
		int entityId = (Integer) value;
		ClientEntityData data = coreModule.getData(entityId);
		if (currentValue == data) {
			return;
		}
		if (currentValue != null) {
			currentValue.removeInstanceLifecycleListener(valueChangeListener);
		}
		if (entityId == EntityProvider.NULL_ID) {
			currentValue = null;
			write(null);
		} else {
			currentValue = data;
			currentValue.addInstanceLifecycleListener(valueChangeListener);
			write(data.getLocalInstance());
		}
	}

	/**
	 * Convenience method to set the represented field to a given entity.
	 * 
	 * @param value
	 *            new value for the represented field
	 */
	protected final void write(SyncEntity value) {
		fieldData.writeAccessor
				.writeValue(entityData.getLocalInstance(), value);
	}

	/**
	 * Listens for visibility of the entity that the represented field is set to
	 * and changes the value of the represented field accordingly.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private final class ValueChangeListener implements
			InstanceLifecycleListener {

		/**
		 * Creates a new <tt>ValueChangeListener</tt> for this
		 * <tt>EntityDynamicUpdateData</tt>.
		 */
		public ValueChangeListener() {
		}

		public int getSortingOrder() {
			return FRAMEWORK_MAX_SORTING_ORDER;
		}

		public void setSortingOrder(int sortingOrder) {
		}

		public void instanceInitialized(InstanceInitializedEvent e) {
			write(e.instance);
		}

		public void instanceReplaced(InstanceReplacedEvent e) {
		}

		public void instanceReplacing(InstanceReplacedEvent e) {
			write(e.newInstance);
		}

		public void instanceRemoved(InstanceRemovedEvent e) {
			write(null);
		}

	}

	/**
	 * Listens for visibility of the entity contains the represented field and
	 * cleans up data if that entity becomes invisible.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private final class OwnEntityStateListener implements
			InstanceLifecycleListener {

		/**
		 * Creates a new <tt>OwnEntityStateListener</tt> for this
		 * <tt>EntityDynamicUpdateData</tt>.
		 */
		public OwnEntityStateListener() {
		}

		/**
		 * Cleans up additional data that was registered by this
		 * <tt>EntityDynamicUpdateData</tt> to avoid memory leaks.
		 */
		@SuppressWarnings("synthetic-access")
		private void destroy() {
			if (currentValue != null) {
				currentValue.removeInstanceLifecycleListener(this);
			}
			entityData.removeInstanceLifecycleListener(this);
		}

		public int getSortingOrder() {
			return FRAMEWORK_MAX_SORTING_ORDER;
		}

		public void setSortingOrder(int sortingOrder) {
		}

		public void instanceInitialized(InstanceInitializedEvent e) {
		}

		public void instanceReplaced(InstanceReplacedEvent e) {
			destroy();
		}

		public void instanceReplacing(InstanceReplacedEvent e) {
		}

		public void instanceRemoved(InstanceRemovedEvent e) {
			destroy();
		}

	}

}
