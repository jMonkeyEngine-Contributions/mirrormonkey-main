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

package mirrormonkey.core;

import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.SyncEntity;

/**
 * Indicates that the implementing class is interested in entity registration
 * events. Such an event appears when the local instance of an entity becomes
 * registered or unregistered.
 * 
 * An entity is considered registered as long as there is a local instance
 * available. This means that its state can only change from registered to
 * unregistered and vice versa. Multiple succeeding calls with the same argument
 * to one method defined in this class are not possible without a call to the
 * other method in between.
 * 
 * If the method <tt>entityRegistered</tt> is called for an argument, then it is
 * guaranteed that that argument's <tt>getLocalInstance</tt> method will not
 * return <tt>null</tt> until after <tt>entityRemoved</tt> is called for the
 * same argument. However, it is not guaranteed that the argument's
 * <tt>getLocalInstance</tt> method will always yield the same result, as the
 * local instance may change without changing the registration state of an
 * entity.
 * 
 * After the method<tt>entityRemoved</tt> is called for an argument, then it is
 * guaranteed that that argument's <tt>getLocalInstance</tt> method will return
 * <tt>null</tt> until before the next class to <tt>entityRegistered</tt> for
 * that argument.
 * 
 * It is possible for <tt>EntityRegistrationListeners</tt> to arbitrarily modify
 * the list of registered listeners without causing a
 * <tt>ConcurrentModificationException</tt>. However, the modifications will not
 * take effect on the listeners called for the current entity registration
 * event.
 * 
 * <b>Please note that the reference of the <tt>DynamicEntityData</tt> object
 * for a given entity may change in between calls of <tt>entityRemoved</tt> and
 * <tt>entityRegistered</tt></b>. This means that to avoid memory leaks and an
 * overall illegal application state, you <b>must</b> discard the reference
 * passed to those methods.
 * 
 * @author Philipp Christian Loewner
 * 
 * @param <D>
 *            <tt>ClientEntityData</tt> or <tt>ServerEntityData</tt> if the
 *            default implementation of the core module is used.
 */
public interface EntityRegistrationListener<D extends DynamicEntityData> {

	/**
	 * Called whenever an entity becomes registered. Not called when the local
	 * instance of the entity changes.
	 * 
	 * @param data
	 *            The <tt>DynamicEntityData</tt> providing mutable data about
	 *            the entity. At the point of time that this method is called
	 *            (and up until <tt>entityRemoved</tt> is called for
	 *            <tt>data</tt>), it is guaranteed that the local instance
	 *            returned by <tt>data's</tt> <tt>getLocalInstance</tt> method
	 *            is never <tt>null</tt>.
	 */
	public void entityRegistered(D data);

	/**
	 * Called whenever the local instance of an entity changes to enable
	 * implementing classes to react to the local class of the entity.
	 * 
	 * This is only called in the clients, as the server-local instance never
	 * changes without changing the identity of the entity.
	 * 
	 * @param data
	 *            The <tt>DynamicEntityData</tt> providing mutable data about
	 *            the entity for which the local instance has changed.
	 * @param oldInstance
	 *            old local instance
	 * @param newInstance
	 *            new local instance
	 */
	public void localInstanceChanged(D data, SyncEntity oldInstance,
			SyncEntity newInstance);

	/**
	 * Called whenever an entity becomes unregistered. Not called when the local
	 * instance of the entity changes, but called before it is set to
	 * <tt>null</tt>.
	 * 
	 * @param data
	 *            The <tt>DynamicEntityData</tt> providing mutable data about
	 *            the entity. At the point of time that this method is called,
	 *            it is guaranteed that the local instance returned by
	 *            <tt>data's</tt> <tt>getLocalInstance</tt> method will not
	 *            return <tt>null</tt>.
	 */
	public void entityRemoved(D data);

}
