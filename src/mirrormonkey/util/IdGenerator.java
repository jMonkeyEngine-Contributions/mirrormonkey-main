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

package mirrormonkey.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * Generates <tt>int</tt> values that are guaranteed to be unique.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class IdGenerator {

	/**
	 * If no first ID is supplied to the constructor, this is used as first ID.
	 */
	public static final int DEFAULT_FIRST_ID = 1;

	/**
	 * Contains every ID that is currently in use.
	 */
	private final Collection<Integer> reservedIds;

	/**
	 * Next ID to be generated (if not in use).
	 */
	private int nextId;

	/**
	 * Creates a new <tt>IdGenerator</tt> that will use
	 * <tt>DEFAULT_FIRST_ID</tt> as first returned ID.
	 */
	public IdGenerator() {
		this(DEFAULT_FIRST_ID);
	}

	/**
	 * Creates a new <tt>IdGenerator</tt>. The first generated ID will be equal
	 * to the supplied parameter.
	 * 
	 * @param firstId
	 *            first id to be returned by <tt>generateAndReserve</tt>
	 */
	public IdGenerator(int firstId) {
		nextId = firstId;
		reservedIds = new HashSet<Integer>();
	}

	/**
	 * Sets the next ID that will be returned by the method
	 * <tt>generateAndReserve</tt>.
	 * 
	 * @param nextId
	 *            the next value that will be returned and reserved by
	 *            <tt>generateAndReserve</tt>
	 * @throws IllegalStateException
	 *             if <tt>nextId</tt> is already reserved
	 */
	public void setNextId(int nextId) throws IllegalStateException {
		if (reservedIds.contains(nextId)) {
			throw new IllegalStateException("Id " + nextId + " already taken.");
		}
		this.nextId = nextId;
	}

	/**
	 * Returns and reserves the next free ID. Next in this case means the next
	 * <tt>int</tt> greater than the value previously returned by this method
	 * that is still free or the smallest free ID if an overflow occurs when
	 * searching for the next free ID.
	 * 
	 * @return the next free ID
	 * @throws IllegalStateException
	 *             if there are no free IDs left
	 */
	public int generateAndReserve() {
		int guardian = nextId;
		while (reservedIds.contains(nextId)) {
			if (++nextId == guardian) {
				throw new IllegalStateException("All IDs taken");
			}
		}
		reserve(nextId);
		return nextId;
	}

	/**
	 * Indicates that an ID is no longer used, freeing it for future calls of
	 * <tt>generateAndReserve</tt>.
	 * 
	 * @param id
	 *            the ID that is no longer in use
	 */
	public void release(int id) {
		reservedIds.remove(id);
	}

	/**
	 * Explicitly reserves a single ID. This ID will no longer be considered
	 * when searching for free IDs until it is released.
	 * 
	 * @param id
	 *            the ID to reserve
	 * @throws IllegalStateException
	 *             if <tt>id</tt> is already reserved
	 */
	public void reserve(int id) {
		if (reservedIds.contains(id)) {
			throw new IllegalStateException("ID " + id + " already taken.");
		}
		reservedIds.add(id);
	}

}
