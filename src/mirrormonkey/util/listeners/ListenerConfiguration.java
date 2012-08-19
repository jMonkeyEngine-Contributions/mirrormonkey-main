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

package mirrormonkey.util.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Some subsystems provide listener interfaces that the core and / or framework
 * subsystem must not know so that the subsystems remain loosely coupled.
 * 
 * For these cases, a <tt>ListenerConfiguration</tt> object is provided by all
 * the objects that listeners can be registered to instead of the default
 * add*Listener methods. Please note that providing a
 * <tt>ListenerConfiguration</tt> does not necessarily mean that all interfaces
 * are collected from the objects.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class ListenerConfiguration {

	/**
	 * Added listeners, mapped by their listener interface classes. To which
	 * classes the listeners are mapped can be influenced by the
	 * <tt>MapListener</tt> annotation.
	 * 
	 * This can be <tt>null</tt> if there is no listener added at all.
	 */
	private Map<Class<?>, List<ManagedListener>> listenerCollections;

	/**
	 * Returns a <tt>Collection</tt> of every <tt>Class</tt> representing any
	 * interface implemented by <tt>clazz</tt> or superinterface thereof that is
	 * annotated with <tt>MapListener</tt>.
	 * 
	 * @param clazz
	 *            the interface hierarchy implemented by this <tt>Class</tt> is
	 *            parsed
	 * @return a <tt>Collection</tt> of every <tt>Class</tt> representing an
	 *         interface in the interface hierarchy implemented by
	 *         <tt>clazz</tt> that is annotated with <tt>MapListener</tt>
	 */
	private final List<Class<?>> recursiveListenerScan(final Class<?> clazz) {
		final List<Class<?>> cc = new LinkedList<Class<?>>();
		for (Class<?> i = clazz; !i.equals(Object.class); i = i.getSuperclass()) {
			recursiveListenerScan(i, cc);
		}
		return cc;
	}

	/**
	 * Adds any <tt>Class</tt> representing the interface <tt>iface</tt> or any
	 * superinterface thereof to <tt>classes</tt> if and only if that
	 * <tt>Class</tt> is annotated with <tt>MapListener</tt>
	 * 
	 * @param iface
	 *            a <tt>Class</tt> representing an interface to scan
	 * @param classes
	 *            <tt>Collection</tt> of every <tt>Class</tt> in
	 *            <tt>iface's</tt> inheritance hierarchy that is annotated with
	 *            <tt>MapListener</tt>
	 */
	private final void recursiveListenerScan(final Class<?> iface,
			final Collection<Class<?>> classes) {
		for (Class<?> i : iface.getInterfaces()) {
			recursiveListenerScan(i, classes);
		}
		if (iface.isAnnotationPresent(MapListener.class)) {
			classes.add(iface);
		}
	}

	/**
	 * Adds a <tt>ManagedListener</tt> to this <tt>ListenerConfiguration</tt>.
	 * 
	 * This is this abstraction layer's method of calling
	 * <tt>object.add{listener.getClass()}Listener(listener);</tt> whereas
	 * object is the object that specifies this <tt>ListenerConfiguration</tt>.
	 * 
	 * @param listener
	 *            the <tt>ManagedListener</tt> to add to this
	 *            <tt>ListenerConfiguration</tt>
	 */
	public final void addListener(ManagedListener listener) {
		for (Class<?> cc : recursiveListenerScan(listener.getClass())) {
			addListener(cc, listener);
		}
	}

	/**
	 * Adds a <tt>ManagedListener</tt> listener to this
	 * <tt>ListenerConfiguration</tt>.
	 * 
	 * Calling this method is pretty much the same as
	 * <tt>object.add{iface.getName()}Listener(listener);</tt> whereas object is
	 * the object that specifies this <tt>ListenerConfiguration</tt>.
	 * 
	 * @param iface
	 *            the listener interface class that <tt>listener</tt> should be
	 *            mapped to
	 * @param listener
	 *            the listener to add
	 */
	private final void addListener(Class<?> iface, ManagedListener listener) {
		if (listenerCollections == null) {
			listenerCollections = new HashMap<Class<?>, List<ManagedListener>>();
		}
		List<ManagedListener> listeners = listenerCollections.get(iface);
		if (listeners == null) {
			listeners = new LinkedList<ManagedListener>();
			listenerCollections.put(iface, listeners);
		}
		listeners.add(listener);
	}

	/**
	 * Removes a <tt>ManagedListener</tt> from this
	 * <tt>ListenerConfiguration</tt> .
	 * 
	 * This is this abstraction layer's method of calling
	 * <tt>object.remove{listener.getClass()}Listener(listener);</tt> whereas
	 * object is the object that specifies this <tt>ListenerConfiguration</tt>.
	 * 
	 * @param listener
	 *            the <tt>ManagedListener</tt> to remove from this
	 *            <tt>ListenerConfiguration</tt>
	 */
	public final void removeListener(ManagedListener listener) {
		for (Class<?> c : recursiveListenerScan(listener.getClass())) {
			removeListener(c, listener);
		}
	}

	/**
	 * Removes a <tt>ManagedListener</tt> listener from this
	 * <tt>ListenerConfiguration</tt>.
	 * 
	 * Calling this method is pretty much the same as
	 * <tt>object.remove{iface.getName()}Listener(listener);</tt> whereas object
	 * is the object that specifies this <tt>ListenerConfiguration</tt>.
	 * 
	 * @param iface
	 *            the listener interface class that <tt>listener</tt> should no
	 *            longer be mapped to
	 * @param listener
	 *            the listener to remove
	 */
	private final void removeListener(Class<?> iface, ManagedListener listener) {
		if (listenerCollections == null) {
			return;
		}
		List<ManagedListener> listeners = listenerCollections.get(iface);
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			listenerCollections.remove(iface);
			if (listenerCollections.isEmpty()) {
				listenerCollections = null;
			}
		}
	}

	/**
	 * Adds every added <tt>ManagedListener</tt> to <tt>listeners</tt> that was
	 * registered to <tt>listenerClass</tt>.
	 * 
	 * @param listenerClass
	 *            listener class to search for
	 * @param listeners
	 *            <tt>Collection</tt> to add the listeners to
	 */
	@SuppressWarnings("unchecked")
	public final <T extends ManagedListener> void getListeners(
			Class<T> listenerClass, Collection<T> listeners) {
		if (listenerCollections == null) {
			return;
		}
		List<T> l = (List<T>) listenerCollections.get(listenerClass);
		if (l != null) {
			listeners.addAll(l);
		}
	}

	/**
	 * @return <tt>true</tt> if this <tt>ListenerConfiguration</tt> does not
	 *         contain any <tt>ManagedListeners</tt>, <tt>false</tt> otherwise
	 */
	public boolean isEmpty() {
		return listenerCollections == null || listenerCollections.isEmpty();
	}

	/**
	 * Removes all <tt>ManagedListeners</tt> from this
	 * <tt>ListenerConfiguration</tt>.
	 */
	public void clear() {
		listenerCollections = null;
	}

}
