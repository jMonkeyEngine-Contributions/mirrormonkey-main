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

package mirrormonkey.framework.parameter;

import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;

import com.jme3.network.MessageConnection;

/**
 * Assumes that values are entities and transmits their IDs over the network.
 * 
 * The methods to check classes returned by <tt>extractData</tt> when
 * transmitting object using this <tt>IdentityAwareInterpreter</tt> will check
 * against the connected class on top of the mapping stack for the value and the
 * given connection.
 * 
 * They will explicitly return <tt>true<tt>, if <tt>value</tt> is <tt>null</tt>
 * or if it is not visible to <tt>connection</tt> and <tt>false</tt> if
 * <tt>value</tt> is not assignment compatible to <tt>SyncEntity</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class IdentityAwareInterpreter implements ValueInterpreter {

	/**
	 * The <tt>EntityProvider</tt> used to extract local entity instances for
	 * their IDs.
	 */
	private final EntityProvider<?> entityProvider;

	/**
	 * Creates a new <tt>IdentityAwareInterpreter</tt> that will use a given
	 * <tt>EntityProvider</tt> to fetch local instances for incoming entity IDs.
	 * 
	 * @param entityProvider
	 *            the <tt>EntityProvider</tt> to look up local entity instances
	 */
	public IdentityAwareInterpreter(EntityProvider<?> entityProvider) {
		this.entityProvider = entityProvider;
	}

	public Object extractData(Object object) {
		DynamicEntityData data = entityProvider.getData((Integer) object);
		return data == null ? null : data.getLocalInstance();
	}

	public Object packData(Object object) {
		SyncEntity s = (SyncEntity) object;
		DynamicEntityData d = s == null ? null : s.getData();
		return d == null ? EntityProvider.NULL_ID : d.getId();
	}

	public boolean isAssignableFrom(Class<?> expectedClass,
			MessageConnection connection, Object parameter) {
		if (parameter == null) {
			return true;
		}
		if (!SyncEntity.class.isInstance(parameter)) {
			return false;
		}
		DynamicEntityData d = ((SyncEntity) parameter).getData();
		if (d == null) {
			return true;
		}
		StaticEntityData sd = d.getActiveStaticData(connection);
		return sd == null
				|| expectedClass.isAssignableFrom(sd.getConnectedClass());
	}

	public boolean isExactMatch(Class<?> expectedClass,
			MessageConnection connection, Object parameter) {
		if (parameter == null) {
			return true;
		}
		if (!SyncEntity.class.isInstance(parameter)) {
			return false;
		}
		DynamicEntityData d = ((SyncEntity) parameter).getData();
		if (d == null) {
			return true;
		}
		StaticEntityData sd = d.getActiveStaticData(connection);
		return sd == null || expectedClass.equals(sd.getConnectedClass());
	}

}
