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

package mirrormonkey.framework.connection.messages;

import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Sent to inform a connection of the local time after a
 * <tt>TimeSyncRequestMessage</tt> has been received.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class TimeSyncResponseMessage implements Message {

	/**
	 * ID of the <tt>TimeSyncRequestMessage</tt> to identify outdated responses.
	 */
	public int id;

	/**
	 * Time of <tt>SyncAppState.getSyncTime</tt> as the message was sent, in
	 * nanoseconds.
	 */
	public long localTime;

	/**
	 * Creates a new <tt>TimeSyncResponseMessage</tt>. Only to be used by
	 * <tt>SpiderMonkey</tt>.
	 * 
	 * @deprecated Only to be used by SpiderMonkey's serializers.
	 */
	@Deprecated
	public TimeSyncResponseMessage() {
	}

	/**
	 * Creates a new <tt>TimeSyncResponseMessage</tt> with given local time, as
	 * response for a <tt>TimeSyncRequestMessage</tt> with given ID.
	 * 
	 * @param id
	 *            the ID of the <tt>TimeSyncRequestMessage</tt> that this
	 *            <tt>TimeSyncResponseMessage</tt> responds to
	 * @param localTime
	 *            the local time returned by <tt>SyncAppState.getSyncTime</tt>,
	 *            measured in nanoseconds
	 */
	public TimeSyncResponseMessage(int id, long localTime) {
		this.id = id;
		this.localTime = localTime;
	}

	public Message setReliable(boolean reliable) {
		return this;
	}

	public boolean isReliable() {
		return false;
	}

}
