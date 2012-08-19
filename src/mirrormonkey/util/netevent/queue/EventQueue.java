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

package mirrormonkey.util.netevent.queue;

import mirrormonkey.util.netevent.NetworkEvent;

/**
 * Defines a policy for event storage and processing.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface EventQueue {

	/**
	 * Adds a new <tt>NetworkEvent</tt> to the queue to be processed later
	 * on.
	 * 
	 * @param event
	 *            the event that should be added to the queue
	 */
	public void add(NetworkEvent event);

	/**
	 * Returns the current number of unprocessed events.
	 * 
	 * @return the number of events currently in the queue
	 */
	public int getSize();

	/**
	 * Processes a number of enqueued events.
	 * 
	 * Standard implementations must guarantee that:
	 * <ul>
	 * <li>Events are processed in order of their receipt.</li>
	 * <li>Events are consumed after they have been processed.</li>
	 * </ul>
	 * 
	 * In most cases, implementations will simply notify a number of listeners
	 * and then exit. Any behavior additional to that would normally have to be
	 * implemented within its own listener class.
	 */
	public void processEvents();

}
