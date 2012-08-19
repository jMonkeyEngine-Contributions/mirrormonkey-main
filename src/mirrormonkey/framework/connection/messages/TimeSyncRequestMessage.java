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
 * Sent to indicate that a <tt>TimeSyncResponseMessage</tt> with same ID is
 * awaited.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Serializable
public class TimeSyncRequestMessage implements Message {

	/**
	 * ID used to identify if a received <tt>TimeSyncResponseMessage</tt> is
	 * outdated or not.
	 */
	public int id;

	/**
	 * Empty constructor for SpiderMonkey.
	 * 
	 * @deprecated Only to be used by SpiderMonkey's serializers.
	 */
	@Deprecated
	public TimeSyncRequestMessage() {
	}

	/**
	 * Creates a new <tt>TimeSyncRequestMessage</tt> with a given ID.
	 * 
	 * @param id
	 *            id that will be expected for the accompanying
	 *            <tt>TimeSyncResponseMessage</tt>
	 */
	public TimeSyncRequestMessage(int id) {
		this.id = id;
	}

	public Message setReliable(boolean reliable) {
		return this;
	}

	public boolean isReliable() {
		return false;
	}

}
