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

/**
 * Contains dynamic (instance-level) data about fields that use value tracking.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class TrackingDynamicUpdateData extends DynamicUpdateData {

	/**
	 * Creates a new <tt>TrackingDynamicUpdateData</tt> for a given field and
	 * entity.
	 * 
	 * @param fieldData
	 *            static data about the represented field
	 * @param entity
	 *            dynamic data about the entity containing the represented field
	 */
	public TrackingDynamicUpdateData(StaticUpdateMemberData fieldData,
			DynamicEntityData entity) {
		super(fieldData, entity);
	}

	/**
	 * Last value for which an update message has been sent, in packed form
	 * according to entity injection and asset injection.
	 */
	public Object lastValue;

	@Override
	public boolean isChanged() {
		Object currentValue = getValue();
		if (lastValue == null) {
			if (currentValue == null) {
				return false;
			}
			return true;
		}
		return !lastValue.equals(currentValue);
	}

	@Override
	public void reset() {
		lastValue = getValue();
	}

}
