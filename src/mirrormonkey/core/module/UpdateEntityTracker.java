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
import java.util.HashSet;

import mirrormonkey.core.EntityRegistrationListener;
import mirrormonkey.core.UpdateAwareEntity;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Tracks local instances of entities that implement <tt>UpdateAwareEntity</tt>
 * to update them during the main loop.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <D>
 *            class used to store mutable data about entities
 */
public class UpdateEntityTracker<D extends DynamicEntityData> implements
		EntityRegistrationListener<D> {

	/**
	 * Contains all local instances of all visible entities where the local
	 * instance implements <tt>UpdateAwareEntity</tt>.
	 */
	private final Collection<D> activeEntities;

	/**
	 * Creates a new <tt>UpdateAwareEntityTracker</tt>.
	 */
	public UpdateEntityTracker() {
		activeEntities = new HashSet<D>();
	}

	public void entityRegistered(D data) {
		if (UpdateAwareEntity.class.isInstance(data.getLocalInstance())) {
			activeEntities.add(data);
		}
	}

	public void localInstanceChanged(D data, SyncEntity oldInstance,
			SyncEntity newInstance) {
		activeEntities.remove(data);
		if (UpdateAwareEntity.class.isInstance(newInstance)) {
			activeEntities.add(data);
		}
	}

	public void entityRemoved(D data) {
		activeEntities.remove(data);
	}

	/**
	 * Updates every local instance of every entity that implements
	 * <tt>UpdateAwareEntity</tt>.
	 * 
	 * @param tpf
	 *            time passed since last call, in seconds
	 */
	public void update(float tpf) {
		for (DynamicEntityData d : activeEntities) {
			((UpdateAwareEntity) d.getLocalInstance()).update(tpf);
		}
	}

}
