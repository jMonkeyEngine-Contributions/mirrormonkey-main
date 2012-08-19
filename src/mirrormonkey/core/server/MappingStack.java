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

import mirrormonkey.core.ConstructorNotFoundException;
import mirrormonkey.core.InstanceInitializedEvent;
import mirrormonkey.core.InstanceRemovedEvent;
import mirrormonkey.core.InstanceReplacedEvent;
import mirrormonkey.core.member.ConstructorData;
import mirrormonkey.core.member.SearchKey;
import mirrormonkey.core.messages.EntityChangeMessage;
import mirrormonkey.core.messages.EntityEndMessage;
import mirrormonkey.core.messages.EntityInitMessage;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.parameter.AssetInterpreter;
import mirrormonkey.framework.parameter.ValueInterpreter;

/**
 * Represents how an entity is visible to a connection and provides an
 * abstraction layer so that users do not need to keep track of the different
 * client-local classes for which the entity is visible to a client.
 * 
 * This is basically a combination between a stack and a sorted doubly linked
 * list (not counting that you can use a linked list as a stack, too).
 * 
 * There can be at most one <tt>MappingStack</tt> per possible entity - client
 * combination. If there is no <tt>MappingStack</tt> for a combination, then the
 * entity is not visible to the client. If there is a <tt>MappingStack</tt> for
 * a combination, then the entity is visible to the client and the
 * <tt>MappingStack</tt> keeps track of all client-local classes.
 * 
 * For every client-local class for an entity, the <tt>MappingStack</tt>
 * contains one <tt>MappingStackElement</tt> that keeps track of the constructor
 * that was called and how many times it was called. Every constructor call for
 * that entity, that client and that client-local class will increase the
 * <tt>MappingStackElement's</tt> reference counter by one, every time a
 * client-local class is removed from that client and that entity, the reference
 * counter of the responsible <tt>MappingStackElement</tt> is decreased. If the
 * reference counter for a <tt>MappingStackElement</tt> reaches 0, then the
 * element will be removed from the <tt>MappingStack</tt>. If the number of
 * <tt>MappingStackElements</tt> in a <tt>MappingStack</tt> reaches 0, then the
 * <tt>MappingStack</tt> will be destroyed and the entity will become invisible
 * to the client.
 * 
 * <tt>MappingStackElements</tt> are ordered in the <tt>MappingStack</tt> so
 * that every client-local class is assignment compatible with every class below
 * it in the stack. If the user tries to insert an element into the stack that
 * is not assignment compatible with every client-local class below it or if a
 * client-local class that would be placed above it is not assignment compatible
 * to the new client-local class, then a <tt>ClassCastException</tt> will be
 * thrown.
 * 
 * The client-local class of an entity is always the class represented by the
 * top-most element on the mapping stack for that entity and that client.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class MappingStack {

	/**
	 * Dummy element to return if a remove operation destroys this
	 * <tt>MappingStack</tt>
	 */
	protected static final DummyElement DUMMY = new DummyElement(null);

	/**
	 * Represents the entity for which this <tt>MappingStack</tt> contains
	 * client-local classes.
	 */
	private final ServerEntityData entity;

	/**
	 * Contains data about the connection for which this <tt>MappingStack</tt>
	 * contains client-local classes.
	 */
	private final ServerConnectionInfo connection;

	/**
	 * Dummy element of the linked list.
	 */
	private final MappingStackElement dummy;

	/**
	 * Creates a new <tt>MappingStack</tt> that contains mapping information for
	 * a given entity and client.
	 * 
	 * @param entity
	 *            represents the entity being mapped to the client
	 * @param connection
	 *            represents the client
	 */
	public MappingStack(ServerEntityData entity, ServerConnectionInfo connection) {
		this.entity = entity;
		this.connection = connection;
		dummy = new DummyElement(this);
	}

	/*
	 * =========================================================================
	 * Accessor methods
	 * =========================================================================
	 */

	/**
	 * @return top-most element of this <tt>MappingStack</tt> or <tt>dummy</tt>
	 *         if this <tt>MappingStack</tt> is empty
	 */
	public MappingStackElement getTop() {
		return dummy.getPrev();
	}

	/**
	 * @return bottom element of this <tt>MappingStack</tt> or <tt>dummy</tt> if
	 *         this <tt>MappingStack</tt> is empty
	 */
	public MappingStackElement getBottom() {
		return dummy.getNext();
	}

	/**
	 * @return static context of the top-most element of this
	 *         <tt>MappingStack</tt> or <tt>null</tt> if this stack is empty
	 */
	public StaticEntityData getActiveData() {
		return getTop().getStaticData();
	}

	/**
	 * @return <tt>true</tt> if this <tt>MappingStack</tt> does contain only the
	 *         dummy element, <tt>false</tt> otherwise
	 */
	public boolean isEmpty() {
		return getTop() == dummy;
	}

	/**
	 * @return data about the client that this <tt>MappingStack</tt> belongs to
	 */
	public ServerConnectionInfo getConnectionInfo() {
		return connection;
	}

	/**
	 * When working with dummy elements that are created on-demand, we risk
	 * inconsistent state if we allow calls to random dummy elements.
	 * 
	 * This method is called to ensure that no state-changing calls are made on
	 * outdated dummy elements. It checks if there is newer data present for the
	 * entity, connection, or this <tt>MappingStack</tt> and throws an
	 * <tt>IllegalArgumentException</tt> in that case, so that inconsistent
	 * state does not occur.
	 */
	private void assertNotOutdated() {
		entity.assertNotOutdated();
		connection.assertNotOutdated();
		MappingStack present = entity.getStack(connection.getConnection(),
				false);
		if (present != null && present != this) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Makes the entity represented by <tt>entity</tt> invisible to
	 * <tt>connection</tt>, no matter what elements this stack contains.
	 * 
	 * In case this was the last <tt>MappingStack</tt> for <tt>entity</tt> or
	 * <tt>connection</tt>, the respective data elements will be discarded if no
	 * <tt>InstanceLifecycleListener</tt> is present for that specific data
	 * element.
	 */
	public void destroy() {
		assertNotOutdated();
		if (getTop().exists()) {
			destroy(getTop().getStaticData());
		} else {
			connection.stackDestructionCallback(entity, this);
			entity.stackDestructionCallback(this);
		}
	}

	/**
	 * Removes every element from this <tt>MappingStack</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param staticData
	 *            context info held by the current top-most element
	 */
	protected void destroy(StaticEntityData staticData) {
		notifyDestroy(staticData);
		while (getBottom().exists()) {
			getBottom().remove();
		}
		connection.stackDestructionCallback(entity, this);
		entity.stackDestructionCallback(this);
	}

	/**
	 * This is the main method for managing elements of this
	 * <tt>MappingStack</tt> whenever a constructor should be called on the
	 * client side.
	 * 
	 * Searches for the <tt>MappingStackElement</tt> responsible for the class
	 * <tt>connectedClass</tt> on this <tt>MappingStack</tt> and increases its
	 * reference counter by one.
	 * 
	 * If no element could be found for <tt>connectedClass</tt>, then a new
	 * element will be created and inserted into this <tt>MappingStack</tt>. If
	 * the element created does not fit into this <tt>MappingStack</tt>, then a
	 * <tt>ClassCastException</tt> will be thrown and the internal state will
	 * remain unchanged.
	 * 
	 * If the top-most element of this <tt>MappingStack</tt> is changed by this
	 * method, then <tt>InstanceLifecycleEvents</tt> will be dispatched
	 * accordingly and messages will be sent to the client.
	 * 
	 * Internal use only.
	 * 
	 * @param override
	 *            If set to <tt>true</tt> then this method will ignore if there
	 *            is already an element present for <tt>connectedClass</tt>. The
	 *            reference counter will be increased normally, but the
	 *            constructor for that <tt>MappingStackElement</tt> will be
	 *            overridden and the client-local instance will be re-created by
	 *            calling the new constructor on the client side. If set to
	 *            <tt>false</tt>, then a new client-local instance will only be
	 *            created if no element can be found for <tt>connectedClass</tt>
	 *            .
	 * @param connectedClass
	 *            client-local class
	 * @param key
	 *            <tt>SearchKey</tt> that will be used to find the constructor
	 *            that should be called and contains the constructor parameters
	 * @throws ClassCastException
	 *             if <tt>connectedClass</tt> is not assignment compatible from
	 *             any element that would be placed on top of it in this
	 *             <tt>MappingStack</tt> or if it not assignment compatible to
	 *             any present element that would be placed below it
	 * @return the <tt>MappingStackElement</tt> that was used to count the
	 *         reference
	 */
	protected MappingStackElement addReference(boolean override,
			Class<? extends SyncEntity> connectedClass, SearchKey key) {
		StaticEntityData staticData = entity.module.getEntityProvider()
				.getStaticData(entity.getLocalInstance().getClass(),
						connectedClass);
		Interpretation i = entity.getInterpretation(staticData, true);
		MappingStackElement e = findNextAssignable(connectedClass, true);

		if (connectedClass.equals(e.getConnectedClass())) {
			// e must exist, since getConnectedClass of dummy will return null
			ConstructorData cd = e.fetchConstrAndAddRef(override, key);
			if (override && e.isTop()) {
				notifyChange(cd.getStaticData(), cd, key);
			}
		} else {
			MappingStackElement prev = e;
			e = new ExistingElement(i);
			ConstructorData cd = e.fetchConstrAndAddRef(true, key);

			boolean wasEmpty = isEmpty();
			e.insertAfter(prev);
			if (wasEmpty) {
				connection.stackCreationCallback(entity, this);
				entity.stackCreationCallback(this);
				notifyInit(cd, key);
			} else if (e.isTop()) {
				notifyChange(prev.getStaticData(), cd, key);
			}
		}
		return e;
	}

	/**
	 * Sets the reference counter for a given client-local entity class to 0 and
	 * removes the associated element from this <tt>MappingStack</tt>.
	 * 
	 * Destroys this <tt>MappingStack</tt> if it its was the last element.
	 * 
	 * If this <tt>MappingStack</tt> is destroyed by this method or the top-most
	 * element changed, <tt>InstanceLifecycleEvents</tt> will be dispatched
	 * accordingly and a message will be sent to the client.
	 * 
	 * @param connectedClass
	 *            client-local class that should be removed
	 */
	public void drainReferences(Class<? extends SyncEntity> connectedClass) {
		assertNotOutdated();
		drainReferences(findExact(connectedClass));
	}

	/**
	 * Does the actual work for <tt>drainReferences</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param toDrain
	 *            element that should be removed
	 */
	private void drainReferences(MappingStackElement toDrain) {
		if (toDrain == null || !toDrain.exists()) {
			return;
		}
		while (toDrain.hasRefs()) {
			toDrain.removeRef();
		}
		if (!toDrain.getPrev().exists() && !toDrain.getNext().exists()) {
			destroy();
		} else if (toDrain.getNext().exists()) {
			toDrain.remove();
		} else {
			MappingStackElement prev = toDrain.getPrev();
			ConstructorData cd = prev.fetchConstr();
			if (cd == null) {
				throw new ConstructorNotFoundException(
						"Could not find constructor for fallback: "
								+ prev.getKey());
			}
			toDrain.remove();
			notifyChange(toDrain.getStaticData(), cd, prev.getKey());
		}
	}

	/**
	 * Decreases the reference counter for a given client-local class by 1 and
	 * removes the associated element from this <tt>MappingStack</tt> if it was
	 * the last reference.
	 * 
	 * If the element is removed and it was the last element of this
	 * <tt>MappingStack</tt>, then this stack will be destroyed.
	 * 
	 * If this <tt>MappingStack</tt> is destroyed or the top-most element is
	 * changed by this method, then <tt>InstanceLifecycleEvents</tt> will be
	 * dispatched accordingly and a message will be sent to the client.
	 * 
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            decreased
	 */
	public void removeReference(Class<? extends SyncEntity> connectedClass) {
		assertNotOutdated();
		removeReference(findExact(connectedClass));
	}

	/**
	 * Does the actual work for <tt>removeReference</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param toRemove
	 *            element for which the reference counter should be decreased by
	 *            one
	 */
	private void removeReference(MappingStackElement toRemove) {
		if (toRemove == null || !toRemove.exists()) {
			return;
		}
		toRemove.removeRef();
		if (!toRemove.hasRefs()) {
			if (!toRemove.getPrev().exists() && !toRemove.getNext().exists()) {
				destroy();
			} else if (toRemove.getNext().exists()) {
				toRemove.remove();
			} else {
				MappingStackElement prev = toRemove.getPrev();
				ConstructorData cd = prev.fetchConstr();
				if (cd == null) {
					throw new ConstructorNotFoundException(
							"Could not find constructor for fallback: "
									+ prev.getKey());
				}
				toRemove.remove();
				notifyChange(toRemove.getStaticData(), cd, prev.getKey());
			}
		}
	}

	/**
	 * Sends an <tt>EntityInitMessage</tt> to <tt>connection</tt> and dispatches
	 * an <tt>InstanceInitializedEvent</tt> to the listeners concerned.
	 * 
	 * @param cd
	 *            data about the constructor that should be called on the client
	 *            side
	 * @param key
	 *            contains the parameters that should be passed to the
	 *            constructor and was used to find it
	 */
	private void notifyInit(ConstructorData cd, SearchKey key) {
		connection.send(new EntityInitMessage(entity, cd, key.getParameters()));
		entity.module.notifyInitListeners(new InstanceInitializedEvent(entity
				.getLocalInstance(), entity, cd.getStaticData(), connection));
	}

	/**
	 * Sends an <tt>EntityChangeMessage</tt> to <tt>connection</tt> and
	 * dispatches an <tt>InstanceReplacedEvent</tt> to the listeners concerned.
	 * 
	 * @param oldStaticData
	 *            previous context in which the entity was visible to the client
	 * @param cd
	 *            constructor that should be called on the client side
	 * @param key
	 *            contains the parameters that should be passed to the
	 *            constructor and was used to find it
	 */
	private void notifyChange(StaticEntityData oldStaticData,
			ConstructorData cd, SearchKey key) {
		InstanceReplacedEvent e = new InstanceReplacedEvent(
				entity.getLocalInstance(), entity.getLocalInstance(), entity,
				oldStaticData, cd.getStaticData(), connection);
		entity.module.notifyReplacedListeners(e);
		connection
				.send(new EntityChangeMessage(entity, cd, key.getParameters()));
		entity.module.notifyReplacingListeners(e);
	}

	/**
	 * Sends an <tt>EntityEndMessage</tt> to <tt>connection</tt> and dispatches
	 * an <tt>InstanceRemovedEvent</tt> to the listeners concerned.
	 * 
	 * @param staticData
	 *            the context in which the entity was visible to the client
	 */
	private void notifyDestroy(StaticEntityData staticData) {
		entity.module.notifyEndListeners(new InstanceRemovedEvent(entity
				.getLocalInstance(), entity, staticData, connection));
		connection.send(new EntityEndMessage(entity));
	}

	/**
	 * Searches for an element on this <tt>MappingStack</tt> that is associated
	 * with a given class.
	 * 
	 * @param connectedClass
	 *            client-local class
	 * @return the element that tracks references and constructor for
	 *         <tt>connectedClass</tt> or the dummy element if no such element
	 *         could be found
	 */
	public MappingStackElement findExact(Class<?> connectedClass) {
		MappingStackElement current = dummy;
		while ((current = current.getPrev()).exists()) {
			if (connectedClass.equals(current.getStaticData()
					.getConnectedClass())) {
				return current;
			}
		}
		return current;
	}

	/**
	 * Searches for the highest element on this <tt>MappingStack</tt> where a
	 * given class is assignment compatible to the associated class.
	 * 
	 * @param connectedClass
	 *            the class that the element should be assignable from
	 * @param failOnIncompatible
	 *            set to <tt>true</tt>, if this method should check whether
	 *            <tt>connectedClass</tt> fits in this <tt>MappingStack</tt>
	 *            given its present state, <tt>false</tt> if incompatible
	 *            <tt>connectedClass</tt> should be ignored and the dummy
	 *            element returned
	 * @return the top-most element that tracks references and constructor for a
	 *         class that is assignable from <tt>connectedClass</tt> or
	 *         <tt>dummy</tt> if no such element could be found
	 */
	public MappingStackElement findNextAssignable(Class<?> connectedClass,
			boolean failOnIncompatible) {
		MappingStackElement current = dummy;
		while ((current = current.getPrev()).exists()) {
			Class<?> presentClass = current.getStaticData().getConnectedClass();
			if (presentClass.isAssignableFrom(connectedClass)) {
				return current;
			} else if (failOnIncompatible
					&& !connectedClass.isAssignableFrom(presentClass)) {
				throw new ClassCastException(connectedClass
						+ " is incompatible to " + presentClass
						+ ", which is present on stack " + this);
			}
		}
		return current;
	}

	/**
	 * An element on this <tt>MappingStack</tt>.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private final class ExistingElement extends MappingStackElement {

		/**
		 * Contains static and dynamic data about the entity, which is a
		 * singleton and shared between multiple stacks.
		 */
		private final Interpretation interpretation;

		/**
		 * Last key used to call a constructor for this element on the client
		 * side. Will be reused once this element becomes the top-most element
		 * of the stack.
		 */
		private SearchKey key;

		/**
		 * Number of constructor calls on this element that have not yet been
		 * removed.
		 */
		private int refCount;

		/**
		 * Creates a new <tt>ExistingElement</tt> that uses a given entity
		 * interpretation singleton.
		 * 
		 * @param interpretation
		 *            singleton instance containing data about the entity
		 */
		public ExistingElement(Interpretation interpretation) {
			this.interpretation = interpretation;
			key = null;
			refCount = 0;
		}

		@Override
		protected ConstructorData fetchConstr() {
			return interpretation.getStaticData().getData(key,
					ConstructorData.class);
		}

		@Override
		protected ConstructorData fetchConstrAndAddRef(boolean override,
				SearchKey key) {
			SearchKey searchFor = override ? key : this.key;
			ConstructorData cd = interpretation.getStaticData().getData(
					searchFor, ConstructorData.class);
			if (cd == null) {
				throw new ConstructorNotFoundException(searchFor.toString());
			}
			// TODO ugly
			ValueInterpreter[] interpreters = cd.getInterpreters();
			for (int i = 0; i < interpreters.length; i++) {
				if (key.getParameters()[i] == null
						&& AssetInterpreter.class.equals(interpreters[i]
								.getClass())) {
					throw new NullPointerException(
							"AssetKey may not be null during constructor call.");
				}
			}
			this.key = searchFor;
			refCount++;
			return cd;
		}

		@Override
		protected void removeRef() {
			if (--refCount < 0) {
				refCount = 0;
			}
		}

		@Override
		protected boolean hasRefs() {
			return refCount > 0;
		}

		@Override
		@SuppressWarnings("synthetic-access")
		protected void insertBetween(MappingStackElement prev,
				MappingStackElement next) {
			this.prev = prev;
			prev.next = this;
			this.next = next;
			next.prev = this;
			if (prev.exists()) {
				((ExistingElement) prev).interpretation.makePassive(connection);
			}
			if (next.exists()) {
				interpretation.add(connection);
			} else {
				interpretation.makeActive(connection);
			}
		}

		@Override
		@SuppressWarnings("synthetic-access")
		protected void remove() {
			prev.next = this.next;
			next.prev = this.prev;
			if (!next.exists() && prev.exists()) {
				((ExistingElement) prev).interpretation.makeActive(connection);
			}
			next = null;
			prev = null;
			interpretation.remove(connection);
		}

		@Override
		public Class<? extends SyncEntity> getConnectedClass() {
			return interpretation.getStaticData().getConnectedClass();
		}

		@Override
		public StaticEntityData getStaticData() {
			return interpretation.getStaticData();
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public SearchKey getKey() {
			return key;
		}

		@Override
		public boolean stackExists() {
			return !isEmpty();
		}
	}

	/**
	 * Dummy element.
	 * 
	 * @author Philipp Christian Loewner
	 * 
	 */
	private static final class DummyElement extends MappingStackElement {

		/**
		 * Stack for which this is the dummy element or <tt>null</tt> if this is
		 * the global dummy.
		 */
		private final MappingStack stack;

		/**
		 * Creates a new <tt>DummyElement</tt> for a given stack.
		 * 
		 * @param stack
		 *            stack for which this is the dummy element or <tt>null</tt>
		 *            if this is the global dummy
		 */
		public DummyElement(MappingStack stack) {
			this.stack = stack;
			next = this;
			prev = this;
		}

		@Override
		protected ConstructorData fetchConstr() {
			throw new IllegalStateException();
		}

		@Override
		protected ConstructorData fetchConstrAndAddRef(boolean override,
				SearchKey key) {
			throw new IllegalStateException();
		}

		@Override
		protected void removeRef() {
		}

		@Override
		protected boolean hasRefs() {
			return false;
		}

		@Override
		protected void insertBetween(MappingStackElement prev,
				MappingStackElement next) {
		}

		@Override
		protected void remove() {
		}

		@Override
		public Class<? extends SyncEntity> getConnectedClass() {
			return null;
		}

		@Override
		public StaticEntityData getStaticData() {
			return null;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public SearchKey getKey() {
			return null;
		}

		@Override
		public boolean stackExists() {
			return stack != null && !stack.isEmpty();
		}

	}

}
