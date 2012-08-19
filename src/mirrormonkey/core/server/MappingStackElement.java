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

package mirrormonkey.core.server;

import mirrormonkey.core.member.ConstructorData;
import mirrormonkey.core.member.SearchKey;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Represents an element on a <tt>MappingStack</tt>.
 * 
 * A <tt>MappingStackElement</tt> is associated with exactly one client-local
 * class and keeps track of the last constructor that was called on the client
 * side and how many times it was called.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class MappingStackElement {

	/**
	 * Next element in the linked list. The connected class represented by this
	 * element is a direct or indirect superclass of the connected class
	 * represented by the next element.
	 */
	protected MappingStackElement next;

	/**
	 * Previous element in the linked list. The connected class represented by
	 * this element is a direct or indirect subclass of the connected class
	 * represented by the previous element.
	 */
	protected MappingStackElement prev;

	/**
	 * @return <tt>true</tt>, if this the connected class represented by this
	 *         <tt>MappingStackElement</tt> is on the mapping stack,
	 *         <tt>false</tt> if this is a dummy element
	 */
	public abstract boolean exists();

	/**
	 * @return the connected class that this <tt>MappingStackElement</tt>
	 *         represents on the mapping stack
	 */
	public abstract Class<? extends SyncEntity> getConnectedClass();

	/**
	 * @return the <tt>SearchKey</tt> that was last used to find a constructor
	 *         to call on the client side, instantiating a client-local instance
	 *         with class <tt>connectedClass</tt>
	 */
	public abstract SearchKey getKey();

	/**
	 * @return next higher element on the mapping stack
	 */
	public MappingStackElement getNext() {
		return next;
	}

	/**
	 * @return next lower element on the mapping stack
	 */
	public MappingStackElement getPrev() {
		return prev;
	}

	/**
	 * @return reference to the <tt>StaticEntityData</tt> describing how an
	 *         entity must be handled for a connection if this element is on top
	 *         of the stack
	 */
	public abstract StaticEntityData getStaticData();

	/**
	 * @return <tt>true</tt> if this element is on top of the stack (even if it
	 *         is a dummy element), <tt>false</tt> otherwise
	 */
	public boolean isTop() {
		return !next.exists();
	}

	/**
	 * @return <tt>true</tt> if the mapping stack that returned this element has
	 *         at least one non-dummy element, <tt>false</tt> otherwise
	 */
	public abstract boolean stackExists();

	/**
	 * Internal use only.
	 * 
	 * @return fetches the constructor for the internally stored constructor key
	 */
	protected abstract ConstructorData fetchConstr();

	/**
	 * Increases the reference counter for this element by one and returns the
	 * constructor for either a given or the internally stored constructor key.
	 * 
	 * Internal use only.
	 * 
	 * @param override
	 *            If this is set to <tt>true</tt>, then the constructor stored
	 *            internally will be ignored and <tt>key</tt> will be used.
	 *            Before this method returns, <tt>key</tt> is stored internally.
	 *            If this is set to <tt>false</tt> and a key is stored
	 *            internally, then the parameter <tt>key</tt> will be ignored.
	 *            If no key is stored internally, the parameter <tt>key</tt>
	 *            will be used and stored.
	 * @param key
	 *            key for which a constructor should be found
	 * @return the <tt>ConstructorData</tt> found for either <tt>key</tt> or the
	 *         key stored internally, depending on whether there is a key stored
	 *         internally and if <tt>override</tt> is set to <tt>true</tt> or
	 *         <tt>false</tt>
	 */
	protected abstract ConstructorData fetchConstrAndAddRef(boolean override,
			SearchKey key);

	/**
	 * @return <tt>true</tt> if this element's reference counter is greater than
	 *         0, <tt>false</tt> if it is equal to 0
	 */
	protected abstract boolean hasRefs();

	/**
	 * Inserts this element into the mapping stack between a given element and
	 * its next.
	 * 
	 * Internal use only.
	 * 
	 * @param prev
	 *            this <tt>MappingStackElement</tt> is inserted after prev
	 */
	protected void insertAfter(MappingStackElement prev) {
		insertBetween(prev, prev.next);
	}

	/**
	 * Inserts this element into the mapping stack between two other elements.
	 * 
	 * Internal use only.
	 * 
	 * @param prev
	 *            this <tt>MappingStackElement</tt> is inserted after
	 *            <tt>prev</tt>
	 * @param next
	 *            this <tt>MappingStackElement</tt> is inserted before
	 *            <tt>next</tt>
	 */
	protected abstract void insertBetween(MappingStackElement prev,
			MappingStackElement next);

	/**
	 * Removes this element from the mapping stack, no matter the value of the
	 * reference counter.
	 * 
	 * Internal use only.
	 */
	protected abstract void remove();

	/**
	 * Decreases the reference counter of this <tt>MappingStackElement</tt> by
	 * one. Removes this element from the stack if the counter reached zero by
	 * that operation.
	 * 
	 * Internal use only.
	 */
	protected abstract void removeRef();

}
