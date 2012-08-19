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

import mirrormonkey.util.listeners.ManagedListener;
import mirrormonkey.util.listeners.MapListener;

/**
 * Indicates that the implementing class is interested in instance lifecycle
 * events. An instance lifecycle event occurs whenever an entity becomes visible
 * to a client, the client-local instance changes or an entity becomes invisible
 * to the client.
 * 
 * As <tt>InstanceLifecycleListeners</tt> can be registered on multiple
 * elements, they allow to specify a sorting order. No matter what elements
 * listeners are collected from, calls to their methods are always sorted in
 * ascending order of the values returned by their <tt>getSortingOrder</tt>
 * methods.
 * 
 * It is possible for <tt>InstanceLifecycleListeners</tt> to arbitrarily
 * register and unregister <tt>InstanceLifecycleListeners</tt> without causing a
 * <tt>ConcurrentModificationException</tt>. However, the modifications do not
 * take effect on the listeners called for the current entity lifecycle event.
 * This does not apply for unregistering an <tt>InstanceLifecycleListener</tt>
 * during the execution of <tt>entityReplacing</tt>, in which case the removed
 * listener's <tt>instanceReplacing</tt> method will not be called for the same
 * instance change.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@MapListener
public interface InstanceLifecycleListener extends ManagedListener {

	/**
	 * Maximum sorting order used by the framework.
	 */
	public static final int FRAMEWORK_MAX_SORTING_ORDER = 0;

	/**
	 * Every userspace implementation must return a value greater than or equal
	 * to this for its <tt>getSortingOrder</tt> method.
	 */
	public static final int USER_MIN_SORTING_ORDER = FRAMEWORK_MAX_SORTING_ORDER + 1;

	/**
	 * Called whenever an entity becomes visible to a client.
	 * 
	 * @param event
	 *            provides information about the entity that just became
	 *            visible, the connection it became visible to and the context
	 *            in which it became visible
	 */
	public void instanceInitialized(InstanceInitializedEvent event);

	/**
	 * Called whenever the client-local instance of an entity changed for a
	 * connection, before the client-local instance is actually changed.
	 * 
	 * In contrast to <tt>instanceReplacing</tt>, this method is called on the
	 * listeners collected for the old entity instance and local / connected
	 * class. After that, <tt>instanceReplacing</tt> will be called with the
	 * same <tt>InstanceReplacedEvent</tt>, but the called listeners are the
	 * ones registered for the new instance and local / connected class.
	 * 
	 * @param event
	 *            provides information about the entity of which a client-local
	 *            instance was changed as well as the connection for which the
	 *            entity changed and the new and old context in which the entity
	 *            was / is visible
	 */
	public void instanceReplaced(InstanceReplacedEvent event);

	/**
	 * Called whenever the client-local instance of an entity changed for a
	 * connection, after the client-local instance is actually changed.
	 * 
	 * In contrast to <tt>instanceReplaced</tt>, this method is called on the
	 * listeners collected for the new entity instance and new local / connected
	 * class.
	 * 
	 * @param event
	 *            provides information about the entity of which a client-local
	 *            instance was changed as well as the connection for which the
	 *            entity changed and the new and old context in which the entity
	 *            was / is visible
	 */
	public void instanceReplacing(InstanceReplacedEvent event);

	/**
	 * Called whenever an entity becomes invisible to a client.
	 * 
	 * @param event
	 *            provides information about the entity that just became
	 *            invisible, the connection that it became invisible to and the
	 *            context in which it was previously visible.
	 */
	public void instanceRemoved(InstanceRemovedEvent event);

	/**
	 * Called before instance lifecycle events are dispatched to
	 * <tt>InstanceLifecycleListeners</tt> to determine in which order the
	 * dispatching methods should be called on the listeners.
	 * 
	 * Calls on listeners are made in ascending order of the values they return
	 * for this method. For two listeners returning the same value for this
	 * method, the order between them can not be forseen.
	 * 
	 * @return The sorting order of this <tt>InstanceLifecycleListener</tt>.
	 *         Userspace implementations should return a value greater than or
	 *         equal to <tt>USER_MIN_SORTING_ORDER</tt>.
	 */
	public int getSortingOrder();

	/**
	 * 
	 * Implementing classes need not guarantee that the value returned by
	 * <tt>getSortingOrder</tt> is consistent with the value passed to this
	 * method. It is only provided as convenience for the users, in case they
	 * like to implement such behavior.
	 * 
	 * @param sortingOrder
	 *            the new sorting order for this
	 *            <tt>InstanceLifecycleListener</tt>
	 */
	public void setSortingOrder(int sortingOrder);

}
