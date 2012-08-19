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
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Indicates that the implementing entity needs to be notified before and after
 * its state is read to be transmitted to a connected instance by MirrorMonkey.
 * 
 * Whenever local state of instances of classes implementing this interface is
 * read, the corresponding methods defined in this interface will be called.
 * 
 * To enable this feature, users must implement this interface and use
 * <tt>UpdateSetId</tt> on at least one field or virtual field in the
 * implementing entity class. Fields that contain the same set ID in their
 * <tt>UpdateSetId</tt> annotation must be updated using the same frequency and
 * reliability.
 * 
 * If above conditions are met, then fields that share the same update set ID
 * are always read and transmitted together in one message. This means that if
 * unreliable transfer is used, all fields in one set are always updated
 * together or not at all. Every time before and after fields for a specific
 * update set ID are read, the corresponding methods defined in this interface
 * are called.
 * 
 * Please note that no checks are performed on whether multiple updates for the
 * same set are read in the same frame. If a set is read multiple times for the
 * same local instance, then methods defined in this interface will be called
 * multiple times for that instance. More precisely, if a set is read for one
 * connected class but multiple connected instances, then the methods defined in
 * this interface will be called once. For every other connected class (no
 * matter if it is used by only one connection ore more), the methods defined in
 * this interface will be called again one time with according parameters.
 * 
 * If fields are updated but do not define an update set ID, then the methods
 * defined in this interface will not be called for them.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface OutboundAwareEntity extends SyncEntity {

	/**
	 * Indicates that an update set is going to be read.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param set
	 *            ID of the update set that is going to be read
	 * @param connectedClass
	 *            connected class on every connection to which the update will
	 *            be sent
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer will be used,
	 *            <tt>false</tt> otherwise
	 */
	public void beforeOutbound(SyncAppState<?> appState, int set,
			Class<?> connectedClass, boolean reliable);

	/**
	 * Indicates that an update set was just read and transmitted.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> responsible for managing
	 *            synchronization
	 * @param set
	 *            ID of the update set that is going to be read
	 * @param connectedClass
	 *            connected class on every connection to which the update will
	 *            be sent
	 * @param reliable
	 *            <tt>true</tt> if reliable transfer will be used,
	 *            <tt>false</tt> otherwise
	 */
	public void afterOutbound(SyncAppState<?> appState, int set,
			Class<?> connectedClass, boolean reliable);

}
