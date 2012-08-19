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

package mirrormonkey.framework.connection;

import mirrormonkey.framework.connection.messages.TimeSyncRequestMessage;

/**
 * One part of the time synchronization algorithm. This class is responsible for
 * determining when a timeout on a time sync message transfer has occurred,
 * adjusting the timeout and frequency with which messages are sent and sending
 * messages.
 * 
 * The other part of the time synchronization algorithm is implemented in
 * <tt>TimeSyncResponseListener</tt>, which is responsible for
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class TimeSync {

	/**
	 * As soon as the current delay between messages falls beyond this value, it
	 * will no longer be decreased when a message was successfully received.
	 */
	public static final long MAX_TIMEOUT = 2000000000;

	/**
	 * If the current timeout is above MAX_TIMEOUT and a response has been
	 * received for the last message within that time, the current timeout will
	 * be decreased by this value.
	 * 
	 * If no response has been received for the last message within the current
	 * timeout, then the current timeout will be increased by this value for the
	 * next message.
	 */
	public static final long TIMEOUT_DELTA = 500000000;

	/**
	 * Timeout between sending a <tt>TimeSyncRequestMessage</tt> and expecting a
	 * response can never go lower than this.
	 */
	public static final long MIN_TIMEOUT = 500000000;

	/**
	 * This <tt>TimeSync</tt> updates the remote time for this
	 * <tt>ConnectionInfo</tt>.
	 */
	protected final ConnectionInfo<?> connectionInfo;

	/**
	 * Timeout between sent messages in nanoseconds. If no response is received
	 * within this time, this time will be adjusted as long as there is.
	 * 
	 * If responses for sent messages are received within this time, time is
	 * lowered to measure more accurately.
	 */
	protected long timeout;

	/**
	 * Time returned by <tt>System.nanoTime</tt> when the last
	 * <tt>TimeSyncRequestMessage</tt> has bee sent. <b>This is not in any way
	 * related to <tt>SyncAppState.getSyncTime</tt>. Instead, it is used to
	 * measure the message travel times more accurately.</b>
	 */
	protected long sentLocalTime;

	/**
	 * Value of <tt>System.nanoTime</tt> at which the next
	 * <tt>TimeSyncRequestMessage</tt> will be sent and the current one times
	 * out.
	 */
	protected long timeoutTarget;

	/**
	 * Id of the <tt>TimeSyncRequestmessage</tt> that is currently awaited.
	 */
	protected int currentId;

	/**
	 * Tracks whether a response has been received for the current time sync
	 * request. This way, we know when to increase or decrease timeout between
	 * messages.
	 */
	protected boolean received;

	/**
	 * Creates a new <tt>TimeSync</tt> that will try to synchronize time with a
	 * <tt>SyncAppState</tt> connected via a given connection.
	 * 
	 * @param connectionInfo
	 *            info about the connection over which the <tt>SyncAppState</tt>
	 *            is connected.
	 */
	public TimeSync(ConnectionInfo<?> connectionInfo) {
		this.connectionInfo = connectionInfo;
		timeout = MIN_TIMEOUT;
		sentLocalTime = 0;
		timeoutTarget = System.nanoTime();
		currentId = 0;
		received = false;
	}

	/**
	 * Checks whether the current message has timed out and sends a new message
	 * if applicable.
	 */
	public void update() {
		if (System.nanoTime() >= timeoutTarget) {
			currentId++;
			connectionInfo.send(new TimeSyncRequestMessage(currentId));
			sentLocalTime = System.nanoTime();
			if (received) {
				if (timeout > MAX_TIMEOUT) {
					timeout -= TIMEOUT_DELTA;
				}
			} else {
				timeout += TIMEOUT_DELTA;
			}
			if (timeout < MIN_TIMEOUT) {
				timeout = MIN_TIMEOUT;
			}
			timeoutTarget = sentLocalTime + timeout;
			received = false;
		}
	}

	@Override
	public String toString() {
		return "[TimeSync]";
	}
}
