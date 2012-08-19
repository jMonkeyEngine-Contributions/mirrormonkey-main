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

import mirrormonkey.core.client.ClientCoreModule;
import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.framework.member.MemberDataKey;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.framework.parameter.IdentityAwareInterpreter;
import mirrormonkey.framework.parameter.ValueInterpreter;
import mirrormonkey.state.member.accessor.ValueReadAccessor;
import mirrormonkey.state.member.accessor.ValueWriteAccessor;

/**
 * Contains static (context-level) data about a synchronized field or virtual
 * field.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StaticUpdateMemberData implements StaticMemberData {

	/**
	 * Module that keeps track of entity data. Will be set to <tt>null</tt> on
	 * the server side.
	 */
	public final ClientCoreModule clientCore;

	/**
	 * Index of this <tt>StaticUpdateMemberData</tt> in its
	 * <tt>StaticEntityData's</tt> member array.
	 */
	public final int id;

	/**
	 * Key that can be used to find this <tt>StaticUpdateMemberData</tt> in its
	 * <tt>StaticEntityData's</tt> member map.
	 */
	public final MemberDataKey key;

	/**
	 * Determines whether to use reliable or unreliable messages when updating
	 * the value of the represented field.
	 */
	public final boolean reliable;

	/**
	 * Determines whether to use the value tracking feature on the represented
	 * field.
	 */
	public final boolean track;

	/**
	 * Determines whether to used the entity injection feature on the
	 * represented field.
	 */
	public final boolean entity;

	/**
	 * Time between updates of the represented field, in nanoseconds.
	 */
	public final long freq;

	/**
	 * Set ID for the represented field (or <tt>NO_SET</tt> if the represented
	 * field does not belong to a particular update set).
	 */
	public final int set;

	/**
	 * Abstract description of how to read values from this field (or
	 * <tt>null</tt> if values don't need to be read).
	 */
	public final ValueReadAccessor readAccessor;

	/**
	 * Abstract description of how to write values for this field (or
	 * <tt>null</tt> if values don't need to be written).
	 */
	public final ValueWriteAccessor writeAccessor;

	/**
	 * Determines whether to use entity injection, asset injection or none of
	 * both.
	 */
	public final ValueInterpreter parameterInterpreter;

	/**
	 * Name of the represented field.
	 */
	public final String name;

	/**
	 * Creates a new <tt>StaticUpdateMemberData</tt> that contains given data.
	 * 
	 * @param id
	 *            index in <tt>StaticEntityData's</tt> member array
	 * @param key
	 *            key in <tt>StaticEntityData's</tt> member map
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used for updates,
	 *            <tt>false</tt> if unreliable transfer should be used
	 * @param track
	 *            <tt>true</tt> if value tracking should be used, <tt>false</tt>
	 *            otherwise
	 * @param freq
	 *            time between updates, in nanoseconds
	 * @param set
	 *            set id
	 * @param readAccessor
	 *            used to read field values
	 * @param writeAccessor
	 *            used to write field values
	 * @param parameterInterpreter
	 *            used to pack / unpack field values
	 * @param name
	 *            name of the field
	 * @param coreModule
	 *            keeps track of entity data
	 */
	public StaticUpdateMemberData(int id, MemberDataKey key, boolean reliable,
			boolean track, long freq, int set, ValueReadAccessor readAccessor,
			ValueWriteAccessor writeAccessor,
			ValueInterpreter parameterInterpreter, String name,
			CoreModule<?, ?> coreModule) {
		this.id = id;
		this.key = key;
		this.reliable = reliable;
		this.track = track;
		this.freq = freq;
		this.set = set;
		this.readAccessor = readAccessor;
		this.writeAccessor = writeAccessor;
		this.parameterInterpreter = parameterInterpreter;
		this.name = name;
		entity = IdentityAwareInterpreter.class
				.isInstance(parameterInterpreter);
		if (ClientCoreModule.class.isInstance(coreModule)) {
			clientCore = (ClientCoreModule) coreModule;
		} else {
			clientCore = null;
		}
	}

	@Override
	public String toString() {
		return "[StaticUpdateMemberData name=" + name + "]";
	}

	public MemberDataKey getMemberKey() {
		return key;
	}

	public DynamicMemberData createDynamicData(DynamicEntityData entity) {
		if (track) {
			if (this.entity && clientCore != null) {
				return new EntityDynamicUpdateData(this, entity, clientCore);
			}
			return new TrackingDynamicUpdateData(this, entity);
		}
		return new DynamicUpdateData(this, entity);
	}

	public void setStaticEntityData(StaticEntityData staticData) {
	}

}
