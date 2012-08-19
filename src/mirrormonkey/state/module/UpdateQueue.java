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

package mirrormonkey.state.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;

import mirrormonkey.state.member.DynamicUpdateData;

/**
 * Keeps track of which sets should be updated when.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class UpdateQueue {

	/**
	 * The <tt>StateModule</tt> managing state synchronization.
	 */
	protected final StateModule module;

	/**
	 * Contains and schedules update sets.
	 */
	private final PriorityQueue<UpdateSet> dataForUpdate;

	/**
	 * Keeps track of dynamic data that has been updated in a frame.
	 * 
	 * If we didn't do that, then value tracked fields will prematurely reset
	 * their tracked values, causing a lot of omitted updates despite changed
	 * values.
	 */
	private final Collection<DynamicUpdateData> updated;

	/**
	 * Creates a new <tt>UpdateQueue</tt> that will schedule updates for a given
	 * <tt>StateModule</tt>.
	 * 
	 * @param module
	 *            local instance of <tt>StateModule</tt> responsible for entity
	 *            state synchronization
	 */
	public UpdateQueue(StateModule module) {
		this.module = module;
		dataForUpdate = new PriorityQueue<UpdateSet>();
		updated = new HashSet<DynamicUpdateData>();
	}

	/**
	 * Schedules an update set for regular updates.
	 * 
	 * @param h
	 *            set to schedule
	 */
	protected void addSet(UpdateSet h) {
		dataForUpdate.add(h);
	}

	/**
	 * Removes an update set so no regular updates are performed on it any more.
	 * 
	 * @param h
	 *            set to remove
	 */
	protected void removeSet(UpdateSet h) {
		dataForUpdate.remove(h);
	}

	/**
	 * Checks for scheduled update sets for a given local sync time and updates
	 * the schedules sets.
	 * 
	 * @param newTime
	 *            current local sync time
	 */
	public void update(long newTime) {
		updated.clear();
		if (dataForUpdate.isEmpty()) {
			return;
		}
		while (dataForUpdate.peek().isDue(newTime)) {
			UpdateSet h = dataForUpdate.poll();
			h.performUpdate(newTime, updated);
			addSet(h);
		}
		for (DynamicUpdateData d : updated) {
			d.reset();
		}
	}

	/**
	 * @return local sync time for which the next update set is scheduled
	 */
	public long nextUpdate() {
		return dataForUpdate.peek().getDue();
	}

}
