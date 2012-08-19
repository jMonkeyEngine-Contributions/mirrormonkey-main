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

package mirrormonkey.state.messages;

import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Contains data about updated field values for a given update set.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class UpdateMessage implements Message {

	/**
	 * Timestamp of the side creating the message so we can filter out updates
	 * for which a newer value was already received.
	 */
	public long localTime;

	/**
	 * Determines whether to use reliable or unreliable transfer for this
	 * <tt>UpdateMessage</tt>.
	 */
	public boolean reliable;

	/**
	 * ID of the entity for which this <tt>UpdateMessage</tt> contains field
	 * values.
	 */
	public int entityId;

	/**
	 * ID of the set for which this <tt>UpdateMessage</tt> contains field values
	 * (or <tt>NO_SET</tt> if it contains values for fields that do not define a
	 * set id).
	 */
	public int setId;

	/**
	 * Index of the fields for which this <tt>UpdateMessage</tt> contains values
	 * in the current <tt>StaticEntityData's</tt> member array.
	 */
	public int[] fieldIds;

	/**
	 * Values of the different fields.
	 */
	public Object[] fieldValues;

	/**
	 * Empty constructor for SpiderMonkey's <tt>Serializer</tt>.
	 * 
	 * @deprecated only for serialization
	 */
	@Deprecated
	public UpdateMessage() {
	}

	/**
	 * Creates a new <tt>UpdateMessage</tt> for a given update set.
	 * 
	 * @param localTime
	 *            local timestamp
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer should be used for this
	 *            <tt>UpdateMessage</tt>, <tt>false</tt> if unreliable transfer
	 *            should be used
	 * @param entityId
	 *            ID of the entity for which this <tt>UpdateMessage</tt>
	 *            contains a part of the internal state
	 * @param setId
	 *            ID of the update set that this <tt>UpdateMessage</tt> contains
	 * @param fieldIds
	 *            IDs of the fields for which this message contains values
	 * @param fieldValues
	 *            values of the fields, in packed form according to entity
	 *            injection and asset injection
	 */
	public UpdateMessage(long localTime, boolean reliable, int entityId,
			int setId, int[] fieldIds, Object[] fieldValues) {
		this.localTime = localTime;
		this.reliable = reliable;
		this.entityId = entityId;
		this.setId = setId;
		this.fieldIds = fieldIds;
		this.fieldValues = fieldValues;
	}

	public Message setReliable(boolean f) {
		this.reliable = f;
		return this;
	}

	public boolean isReliable() {
		return reliable;
	}
}
