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

package mirrormonkey.state;

import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Indicates that the implementing entity needs to be notified before and after
 * its state is set from remote by MirrorMonkey.
 * 
 * Whenever local state of instances of classes implementing this interface is
 * set, the corresponding methods defined in this interface will be called.
 * 
 * To enable this feature, users must implement this interface and use
 * <tt>UpdateSetId</tt> on at least one field or virtual field in the
 * implementing entity class. Fields that contain the same set ID in their
 * <tt>UpdateSetId</tt> annotation must be updated using the same frequency and
 * reliability.
 * 
 * If these conditions are met, then fields that share the same update set id
 * are always read and transmitted together in one message. This means that if
 * unreliable transfer is used, all fields in one set are always updated
 * together, or not at all. Every time before and after fields for a specific
 * set id are set, the corresponding methods defined in this interface are
 * called.
 * 
 * Please note that no checks are performed on whether multiple updates are
 * performed for the same set in the same frame. If multiple messages for the
 * same set are received and processed during the same frame, then the methods
 * defined in this interface will be called multiple times.
 * 
 * If fields are updated that do not define an update set ID, then the methods
 * defined in this interface will not be called for them..
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface InboundAwareEntity extends SyncEntity {

	/**
	 * Indicates that an update set is going to be written.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param connection
	 *            data about the connection from which the update was received
	 * @param set
	 *            ID of the update set for which a new state was received
	 * @param connectedClass
	 *            connected entity class (local class in <tt>connection</tt>)
	 * @param reliable
	 *            <tt>true</tt> if the received message used reliable transfer,
	 *            <tt>false</tt> otherwise
	 * @param lastTimestamp
	 *            local time for the connected instance at the time that the set
	 *            was read the last time
	 * @param newTimestamp
	 *            local time for the connected instance at the time that the set
	 *            was read this time
	 */
	public void beforeInbound(SyncAppState<?> appState,
			ConnectionInfo<?> connection, int set, Class<?> connectedClass,
			boolean reliable, long lastTimestamp, long newTimestamp);

	/**
	 * Indicates that an update set was just written.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param connection
	 *            data about the connection from which the update was received
	 * @param set
	 *            ID of the update set for which a new state was just written
	 * @param connectedClass
	 *            connection entity class (local class in <tt>connection</tt>)
	 * @param reliable
	 *            <tt>true</tt> if the received message used reliable transfer,
	 *            <tt>false</tt> otherwise
	 * @param lastTimestamp
	 *            local time for the connected instance at the time that the set
	 *            was read the last time
	 * @param newTimestamp
	 *            local time for the connected instance at the time that the set
	 *            was read this time
	 */
	public void afterInbound(SyncAppState<?> appState,
			ConnectionInfo<?> connection, int set, Class<?> connectedClass,
			boolean reliable, long lastTimestamp, long newTimestamp);

}
