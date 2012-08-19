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

import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.member.DynamicMemberData;

/**
 * Contains dynamic (instance-level) data about a simple synchronized field that
 * does not use value tracking.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class DynamicUpdateData implements DynamicMemberData {

	/**
	 * Contains static data about the represented field.
	 */
	protected final StaticUpdateMemberData fieldData;

	/**
	 * Contains dynamic data about the local entity instance.
	 */
	protected final DynamicEntityData entityData;

	/**
	 * Timestamp on the latest update message that was received for this field.
	 * Used to determine if a newer unreliable update message for a field was
	 * transmitted faster than another update message despite being older.
	 */
	protected long lastTime;

	/**
	 * Creates a new <tt>DynamicUpdateData</tt> for a given field and entity.
	 * 
	 * @param fieldData
	 *            static data about the field that this
	 *            <tt>DynamicEntityData</tt> should represent
	 * @param entityData
	 *            dynamic data about the entity instance that contains the field
	 */
	public DynamicUpdateData(StaticUpdateMemberData fieldData,
			DynamicEntityData entityData) {
		this.fieldData = fieldData;
		this.entityData = entityData;
	}

	/**
	 * @return timestamp of the last update message that was accepted
	 */
	public long getLastTime() {
		return lastTime;
	}

	/**
	 * @return index of this <tt>DynamicUpdateData</tt> in the static entity
	 *         data's member array
	 */
	public int getId() {
		return fieldData.id;
	}

	/**
	 * @return <tt>true</tt> if the value tracking feature determined that the
	 *         value of this field has changed since the last update message was
	 *         sent, <tt>false</tt> otherwise
	 */
	public boolean isChanged() {
		return true;
	}

	@Override
	public String toString() {
		return "[DynamicUpdateData static=" + fieldData + "]";
	}

	/**
	 * Updates the local value to a new value that was received from remote.
	 * Ignores the new value and does nothing if a never value for the
	 * represented field has already been received in a prior message.
	 * 
	 * @param value
	 *            new value of the represented field in packed form according to
	 *            entity injection and asset injection
	 * @param lastTime
	 *            timestamp of the message that contained the new value
	 */
	public void setFromRemote(Object value, long lastTime) {
		if (this.lastTime >= lastTime) {
			return;
		}
		this.lastTime = lastTime;
		fieldData.writeAccessor.writeValue(entityData.getLocalInstance(),
				fieldData.parameterInterpreter.extractData(value));
	}

	/**
	 * @return the current value of the field, in packed form according to
	 *         entity injection and asset injection
	 */
	public Object getValue() {
		return fieldData.parameterInterpreter.packData(fieldData.readAccessor
				.readValue(entityData.getLocalInstance()));
	}

	/**
	 * Notifies the value tracking feature that an update message for this field
	 * has just been sent.
	 */
	public void reset() {
	}
}
