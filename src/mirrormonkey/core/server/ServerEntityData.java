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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import mirrormonkey.core.ConstructorNotFoundException;
import mirrormonkey.core.InstanceLifecycleListener;
import mirrormonkey.core.member.SearchCompatibleKey;
import mirrormonkey.core.member.SearchExactKey;
import mirrormonkey.core.member.SearchKey;
import mirrormonkey.core.member.SearchNamedKey;
import mirrormonkey.core.member.SearchNamedUnsafeKey;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.member.DynamicMemberData;

import com.jme3.network.MessageConnection;

/**
 * Server-side implementation of <tt>DynamicEntityData</tt>. In addition to its
 * superclass, this will keep track of the connections to which the represented
 * entity is visible and of the mapping stacks that describe how the entity is
 * visible to the connections.
 * 
 * For every connected class that is present on at least one mapping stack for
 * the represented entity, exactly one <tt>Interpretation</tt> will be created.
 * The interpretations keep track of dynamic data of fields and are shared
 * between mapping stacks for this particular entity.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ServerEntityData extends DynamicEntityData {

	/**
	 * Unmodifiable, empty collection so we can return it without runtime
	 * overhead when active connections are requested for a connected class that
	 * is not on any mapping stack.
	 */
	private static final Collection<ServerConnectionInfo> NO_CONNECTIONS = Collections
			.unmodifiableCollection(new LinkedList<ServerConnectionInfo>());

	/**
	 * If not specified otherwise by a method caller, then constructor calls
	 * will not override previous constructor calls.
	 */
	private static final boolean DEFAULT_OVERRIDE_OPTION = false;

	/**
	 * Core module that track entity and connection data.
	 */
	protected final ServerCoreModule module;

	/**
	 * Contains the mapping stack references for every connection to which the
	 * represented entity is visible.
	 */
	private final Map<MessageConnection, MappingStack> connectionToStack;

	/**
	 * Maps every connected class of the represented entity that is currently
	 * present on at least one mapping stack to an instance of
	 * <tt>Interpretation</tt>.
	 * 
	 * The <tt>Interpretation</tt> contains dynamic data for the connected class
	 * and is shared across multiple connections / mapping stacks.
	 */
	private final Map<Class<?>, Interpretation> connectedClassToDynamicData;

	/**
	 * Creates a new <tt>ServerEntityData</tt> with given core module, entity ID
	 * and server-local instance.
	 * 
	 * @param module
	 *            the <tt>ServerCoreModule</tt> that will manages the
	 *            represented entity
	 * @param id
	 *            the unique entity ID
	 * @param entity
	 *            the server-local reference of the represented entity
	 */
	protected ServerEntityData(ServerCoreModule module, int id,
			SyncEntity entity) {
		super(id, entity);
		this.module = module;
		connectionToStack = new HashMap<MessageConnection, MappingStack>();
		connectedClassToDynamicData = new HashMap<Class<?>, Interpretation>();
	}

	/*
	 * ========================================================================
	 * either external entry points themselves or called by delegators that are
	 * external entry points
	 * ========================================================================
	 */

	/**
	 * Checks whether this <tt>ServerEntityData</tt> is outdated and throws an
	 * <tt>IllegalStateException</tt> if it is.
	 * 
	 * A <tt>ServerEntityData</tt> is considered outdated if it is a dummy
	 * instance and there is a real <tt>ServerEntityData</tt> present that
	 * represents the same entity. It is also considered outdated if it is a
	 * real <tt>ServerEntityData</tt> and there is another real
	 * <tt>ServerEntityData</tt> that represents the same entity, although that
	 * should never happen due to invariants.
	 */
	protected final void assertNotOutdated() {
		module.getEntityProvider().assertNotOutdated(this);
	}

	/**
	 * Searches a constructor for a client-local entity class, invokes the
	 * constructor on the client side for a given client and manages internal
	 * data structures accordingly.
	 * 
	 * The client-local constructor will only be called if the top of the
	 * mapping stack changes for the represented entity and the given connection
	 * or if the given connected class is on top of the mapping stack and the
	 * override option is enabled, but the call will be recorded by the mapping
	 * stack in any case.
	 * 
	 * This is the main entry point for creating client-local instances and
	 * manages all internal data structures.
	 * 
	 * This method will register this <tt>ServerEntityData</tt> to the core
	 * module if it was not previously registered.
	 * 
	 * If a <tt>ConstructorNotFoundException</tt> or
	 * <tt>IllegalStateException</tt> is thrown by this method, then it will
	 * exit before any changes are performed, messages sent or listeners
	 * notified.
	 * 
	 * @param override
	 *            If this is set to <tt>true</tt> and a constructor is stored
	 *            for the given connection and connected class internally, then
	 *            the constructor found by this method will be used on the
	 *            client side and will be stored internally. If this is set to
	 *            <tt>false</tt>, then the constructor for <tt>key</tt> will
	 *            only be searched and stored internally if there is no
	 *            constructor present at the time.
	 * @param connectedClass
	 *            client-local entity class for which a reference should be
	 *            added
	 * @param key
	 *            Contains the connection for which mapping stack should be
	 *            changed, the parameters for the constructor and encapsulates
	 *            the search algorithm that should be used for dynamic
	 *            constructor binding.
	 * @return the <tt>MappingStackElement</tt> that is responsible for storing
	 *         the reference counter for the represented entity, the given
	 *         client and the given client-local class
	 * @throws ConstructorNotFoundException
	 *             if a constructor must be called, but a constructor for
	 *             <tt>key</tt> could not be found.
	 * @throws IllegalStateException
	 *             if this <tt>ServerEntityData</tt> is a dummy instance, but
	 *             another instance has been registered in the core module in
	 *             the time since this <tt>ServerEntityData</tt> was created
	 */
	public MappingStackElement callConstr(boolean override,
			Class<? extends SyncEntity> connectedClass, SearchKey key) {
		assertNotOutdated();
		MappingStack s = getStack(key.getConnection(), true);
		return s.addReference(override, connectedClass, key);
	}

	/**
	 * Will decrease the reference counter of the top-most mapping stack element
	 * for a given connection by one and manages all internal data structures
	 * accordingly.
	 * 
	 * If the reference counter of the top-most element reaches 0, then the
	 * element will be removed from the mapping stack. After that, there are two
	 * possibilities of what happens:
	 * 
	 * If the mapping stack for the given connection is empty after removing the
	 * element, then the represented entity will be made invisible to the
	 * connection. If it was the last connection to which the represented entity
	 * was visible and there are no <tt>InstanceLifecycleListeners</tt>
	 * registered for the entity, then this instance of
	 * <tt>ServerEntityData</tt> will be unregistered from the core module and
	 * turned into a dummy instance.
	 * 
	 * If the top-most element of the mapping stack changes by removing the
	 * element, but the mapping stack still contains other elements, then this
	 * method will try to replace the current client-local instance with an
	 * instance of the class associated with the new top-most element on the
	 * stack. For this, the last constructor that was called for that
	 * client-local class will be called again.
	 * 
	 * If a <tt>ConstructorNotFoundException</tt> or
	 * <tt>IllegalStateException</tt> is thrown by this method, then it will
	 * exit before any changes are performed, messages sent or listeners
	 * notified.
	 * 
	 * @param connection
	 *            the <tt>MessageConnection</tt> for which the mapping stack
	 *            should be changed
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been decreased
	 * @throws ConstructorNotFoundException
	 *             If this method would change the client-local instance for the
	 *             given connection, but the constructor that has been invoked
	 *             last for the new client-local class can no longer be invoked.
	 *             This can happen e.g. if entity injection is used on one of
	 *             the parameters, but the client-local class of one of the
	 *             injected parameters has changed so that it is no longer
	 *             assignment compatible to the declared parameter class.
	 * @throws IllegalStateException
	 *             If this <tt>ServerEntityData</tt> is a dummy instance,
	 *             nothing will be done. However, if it is a dummy instance and
	 *             there is another, real instance registered for the
	 *             represented entity, then an <tt>IllegalStateException</tt>
	 *             will be thrown.
	 */
	public MappingStackElement remove(MessageConnection connection) {
		assertNotOutdated();
		MappingStack s;
		if ((s = getStack(connection, false)) != null) {
			s.removeReference(s.getActiveData().getConnectedClass());
			return s.getTop();
		}
		return MappingStack.DUMMY;
	}

	/**
	 * Decreases the reference counter for a given connection and client-local
	 * class for that connection by one and manages internal data structures
	 * accordingly.
	 * 
	 * If the reference counter of the element reaches zero by performing this
	 * operation, then the element will be removed from the mapping stack. When
	 * that happens, there are three cases for what will happen:
	 * 
	 * If the mapping stack for the given connection is empty after removing the
	 * element, then the represented entity will be made invisible to the
	 * connection. If it was the last connection to which the represented entity
	 * was visible and there are no <tt>InstanceLifecycleListeners</tt>
	 * registered for the entity, then this instance of
	 * <tt>ServerEntityData</tt> will be unregistered from the core module and
	 * turned into a dummy instance.
	 * 
	 * If the element was the top-most element of the stack, but the stack still
	 * contains other elements, then this method will try to replace the current
	 * client-local instance with an instance of the class associated with the
	 * new top-most element on the stack. For this, the last constructor that
	 * was called for that client-local class will be called again.
	 * 
	 * If the element not the top-most element on the stack and the stack still
	 * contains other elements, then the element will simply be removed. This
	 * method will not have any further effects.
	 * 
	 * If a <tt>ConstructorNotFoundException</tt> or
	 * <tt>IllegalStateException</tt> is thrown by this method, then it will
	 * exit before any changes are performed, messages sent or listeners
	 * notified.
	 * 
	 * @param connection
	 *            the connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            the client-local class associated with the mapping stack
	 *            element that should be changed
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been decreased
	 * @throws ConstructorNotFoundException
	 *             If this method would change the client-local instance for the
	 *             given connection, but the constructor that has been invoked
	 *             last for the new client-local class can no longer be invoked.
	 *             This can happen e.g. if entity injection is used on one of
	 *             the parameters, but the client-local class of one of the
	 *             injected parameters has changed so that it is no longer
	 *             assignment compatible to the declared parameter class.
	 * @throws IllegalStateException
	 *             If this <tt>ServerEntityData</tt> is a dummy instance,
	 *             nothing will be done. However, if it is a dummy instance and
	 *             there is another, real instance registered for the
	 *             represented entity, then an <tt>IllegalStateException</tt>
	 *             will be thrown.
	 */
	public MappingStackElement remove(MessageConnection connection,
			Class<? extends SyncEntity> connectedClass) {
		assertNotOutdated();
		MappingStack s;
		if ((s = getStack(connection, false)) != null) {
			s.removeReference(connectedClass);
			return s.getTop();
		}
		return MappingStack.DUMMY;
	}

	@Override
	public void addInstanceLifecycleListener(InstanceLifecycleListener listener) {
		assertNotOutdated();
		super.addInstanceLifecycleListener(listener);
		checkRegister();
	}

	@Override
	public void removeInstanceLifecycleListener(
			InstanceLifecycleListener listener) {
		assertNotOutdated();
		super.removeInstanceLifecycleListener(listener);
		checkDestroy();
	}

	@Override
	public void destroy() {
		assertNotOutdated();
		while (!connectionToStack.isEmpty()) {
			connectionToStack.values().iterator().next().destroy();
		}
		connectionToStack.clear();
		listenerConfiguration.clear();
		unregister();
	}

	/**
	 * Fetches the mapping stack responsible for tracking how the represented
	 * entity is visible to a given connection.
	 * 
	 * @param connection
	 *            the connection
	 * @param create
	 *            If this is set to <tt>true</tt>, then a dummy mapping stack
	 *            will be created if the represented entity is not currently
	 *            visible to <tt>connection</tt>. If this is set to
	 *            <tt>false</tt> and the represented entity is not visible to
	 *            <tt>connection</tt>, then <tt>null</tt> will be returned.
	 * @return Will return <tt>null</tt> if the represented entity is not
	 *         visible to connection and <tt>create</tt> is set to false. Will
	 *         create and return a dummy mapping stack if the represented entity
	 *         is not visible to <tt>connection</tt> and <tt>create</tt> is set
	 *         to <tt>true</tt>. Will return the mapping stack that describes
	 *         how the represented entity is visible to <tt>connection</tt> in
	 *         other cases, no matter the value of <tt>create</tt>.
	 */
	public MappingStack getStack(MessageConnection connection, boolean create) {
		MappingStack s = connectionToStack.get(connection);
		if (s == null && create) {
			s = new MappingStack(this, module.getData(connection));
		}
		return s;
	}

	/**
	 * Fetches the <tt>Interpretation</tt> that contains dynamic data on the
	 * represented entity for a given connected class.
	 * 
	 * @param staticData
	 *            the <tt>StaticEntityData</tt> that describes the static
	 *            component of how the represented entity should be handled
	 * @param create
	 *            If this is set to <tt>true</tt> and no interpretation could be
	 *            found, then a new instance of <tt>Interpretation</tt> will be
	 *            created and returned. If this is set to <tt>false</tt> and no
	 *            interpretation is found, then <tt>null</tt> will be returned.
	 * @return the <tt>Interpretation</tt> containing the dynamic component of
	 *         how the represented entity should be handled, given a connected
	 *         class
	 */
	protected Interpretation getInterpretation(StaticEntityData staticData,
			boolean create) {
		Interpretation i = connectedClassToDynamicData.get(staticData
				.getConnectedClass());
		if (i == null && create) {
			i = new Interpretation(this, staticData);
		}
		return i;
	}

	/*
	 * ========================================================================
	 * framework-internal entry points
	 * ========================================================================
	 */

	/**
	 * Called by dummy mapping stack instances when they are turned from a dummy
	 * instance into a real instance. Registers this <tt>ServerEntityData</tt>
	 * to the core module if it was not previously registered. Registers the
	 * created stack to this <tt>ServerEntityData</tt>.
	 * 
	 * @param stack
	 *            the stack that has been turned from a dummy instance into a
	 *            real instance
	 */
	protected void stackCreationCallback(MappingStack stack) {
		boolean wasEmpty = connectionToStack.isEmpty();
		connectionToStack.put(stack.getConnectionInfo().getConnection(), stack);
		checkRegister();
		if (wasEmpty) {
			module.notifyEntityRegistration(this);
		}
	}

	/**
	 * Called by existing mapping stacks when they are turned into a dummy
	 * instance. Unregisters the calling mapping stack from this
	 * <tt>ServerEntityData</tt>. Unregisters this <tt>ServerEntityData</tt>
	 * from the core module if the removed mapping stack contained the last
	 * connection to which the represented entity was visible and if there are
	 * no <tt>InstanceLifecycleListeners</tt> registered specifically for the
	 * represented entity.
	 * 
	 * @param stack
	 *            the <tt>MappingStack</tt> wich is now empty
	 */
	protected void stackDestructionCallback(MappingStack stack) {
		boolean wasEmpty = connectionToStack.isEmpty();
		connectionToStack.remove(stack.getConnectionInfo().getConnection());
		checkDestroy();
		if (!wasEmpty && connectionToStack.isEmpty()) {
			module.notifyEntityRemoval(this);
		}
	}

	/**
	 * Registers an interpretation that previously was a dummy instance to this
	 * <tt>ServerEntityData</tt>.
	 * 
	 * @param i
	 *            the interpretation for which the first connection has just
	 *            been added
	 */
	protected void interpretationCreationCallback(Interpretation i) {
		connectedClassToDynamicData.put(i.getStaticData().getConnectedClass(),
				i);
	}

	/**
	 * Unregisters an interpretation from this <tt>ServerEntityData</tt> and
	 * turns the interpretation into a dummy instance.
	 * 
	 * @param i
	 *            the interpretation for which the last connection has just been
	 *            removed
	 */
	protected void interpretationDestructionCallback(Interpretation i) {
		connectedClassToDynamicData.remove(i.getStaticData()
				.getConnectedClass());
	}

	@Override
	public StaticEntityData getActiveStaticData(MessageConnection forConnection) {
		MappingStack m = connectionToStack.get(forConnection);
		return m == null ? null : m.getActiveData();
	}

	@Override
	public DynamicMemberData[] getMemberData(StaticEntityData staticData) {
		Interpretation i = getInterpretation(staticData, false);
		return i == null ? null : i.getDynamicData();
	}

	@Override
	public Collection<ServerConnectionInfo> getActiveConnections(
			StaticEntityData staticData) {
		Interpretation i = getInterpretation(staticData, false);
		return i == null ? NO_CONNECTIONS : i.getActiveConnections();
	}

	/*
	 * =======================================================================
	 * little helpers
	 * =======================================================================
	 */

	/**
	 * Turns this <tt>ServerEntityData</tt> from a dummy instance into a real
	 * instance and registers it to the core module.
	 * 
	 * Please note that despite its similar name, this method is <b>not</b>
	 * called solely when the represented entity's state is changed to
	 * registered. It also will not notify <tt>EntityRegistrationListeners</tt>
	 */
	private void register() {
		getLocalInstance().setData(this);
		module.getEntityProvider().registerData(this);
		setNotDummy(true);
	}

	/**
	 * Turns this <tt>ServerEntityData</tt> from a real instance into a dummy
	 * instance and unregisters it from the core module.
	 * 
	 * Please note that despite its similar name, this method is <b>not</b>
	 * always (and not necessarily) called when the represented entity's state
	 * is changed to unregistered. It also will not notify any
	 * <tt>EntityRegistrationListeners</tt>.
	 */
	private void unregister() {
		getLocalInstance().setData(null);
		module.getEntityProvider().removeData(this);
		setNotDummy(false);
	}

	/**
	 * Convenience method to check whether this <tt>ServerEntityData</tt> should
	 * be registered (if it previously wasn't).
	 */
	private void checkRegister() {
		if (!isNotDummy()
				&& (hasInstanceLifecycleListeners() || hasConnections())) {
			register();
		}
	}

	/**
	 * Convenience method to check whether this <tt>ServerEntityData</tt> should
	 * be unregistered (if it previously was registered).
	 */
	private void checkDestroy() {
		if (isNotDummy() && !hasInstanceLifecycleListeners() && !hasConnections()) {
			destroy();
		}
	}

	/**
	 * @return <tt>true</tt> if the represented entity is visible to at least
	 *         one client, <tt>false</tt> if it isn't.
	 */
	private boolean hasConnections() {
		return !connectionToStack.isEmpty();
	}

	/*
	 * =======================================================================
	 * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	 * All interesting code above this line. Rest is only convenience methods
	 * =======================================================================
	 */

	/*
	 * Exact class match for parameters
	 */

	/**
	 * Increases the reference counter for a given connection and uses the
	 * server-local class as client-local class. Uses exact matching algorithm
	 * to find the constructor and does not override any constructor that may be
	 * present on the responsible mapping stack element. Please see the
	 * documentation of <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callConstr(MessageConnection connection,
			Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, getLocalInstance()
				.getClass(), new SearchExactKey(connection, constrParams));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses exact matching algorithm to find the constructor and does not
	 * override any constructor that may be present on the responsible mapping
	 * stack element. Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callConstr(MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, connectedClass,
				new SearchExactKey(connection, constrParams));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses exact matching algorithm to find the constructor and may
	 * override any constructor that may be present on the responsible mapping
	 * stack element, depending on the <tt>override</tt> flag. Please see the
	 * documentation of <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param override
	 *            <tt>true</tt> if a constructor search key that is present on
	 *            the responsible mapping stack element should be overridden by
	 *            the given constructor invocation, <tt>false</tt> otherwise
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callConstr(boolean override,
			MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, Object... constrParams) {
		return callConstr(override, connectedClass, new SearchExactKey(
				connection, constrParams));
	}

	/*
	 * Search for constructor with @NamedConstructor
	 */

	/**
	 * Increases the reference counter for a given connection and uses the
	 * server-local class as client-local class. Uses name based matching
	 * algorithm to find the constructor and does not override any constructor
	 * that may be present on the responsible mapping stack element. Please see
	 * the documentation of <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callNamedConstr(MessageConnection connection,
			String constrName, Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, getLocalInstance()
				.getClass(), new SearchNamedKey(connection, constrParams,
				constrName));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses name based matching algorithm to find the constructor and
	 * does not override any constructor that may be present on the responsible
	 * mapping stack element. Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callNamedConstr(MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, String constrName,
			Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, connectedClass,
				new SearchNamedKey(connection, constrParams, constrName));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses name based matching algorithm to find the constructor and may
	 * override any constructor that may be present on the responsible mapping
	 * stack element, depending on the <tt>override</tt> flag. Please see the
	 * documentation of <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param override
	 *            <tt>true</tt> if a constructor search key that is present on
	 *            the responsible mapping stack element should be overridden by
	 *            the given constructor invocation, <tt>false</tt> otherwise
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callNamedConstr(boolean override,
			MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, String constrName,
			Object... constrParams) {
		return callConstr(override, connectedClass, new SearchNamedKey(
				connection, constrParams, constrName));
	}

	/*
	 * Search for a named constructor BUT DO NOT CHECK ON THE SERVER SIDE IF THE
	 * PARAMETERS ARE ASSIGNMENT-COMPATIBLE
	 */

	/**
	 * Increases the reference counter for a given connection and uses the
	 * server-local class as client-local class. Uses unsafe name based matching
	 * algorithm to find the constructor and does not override any constructor
	 * that may be present on the responsible mapping stack element. Please see
	 * the documentation of <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callUnsafeConstr(MessageConnection connection,
			String constrName, Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, getLocalInstance()
				.getClass(), new SearchNamedUnsafeKey(connection, constrParams,
				constrName));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses unsafe name based matching algorithm to find the constructor
	 * and does not override any constructor that may be present on the
	 * responsible mapping stack element. Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callUnsafeConstr(MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, String constrName,
			Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, connectedClass,
				new SearchNamedUnsafeKey(connection, constrParams, constrName));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses unsafe name based matching algorithm to find the constructor
	 * and may override any constructor that may be present on the responsible
	 * mapping stack element, depending on the <tt>override</tt> flag. Please
	 * see the documentation of <tt>callConstr(boolean, Class, SearchKey)</tt>
	 * and <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param override
	 *            <tt>true</tt> if a constructor search key that is present on
	 *            the responsible mapping stack element should be overridden by
	 *            the given constructor invocation, <tt>false</tt> otherwise
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrName
	 *            tries to invoke a constructor that has been bound to this name
	 *            using the <tt>NamedClientSideConstructor</tt> annotation
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callUnsafeConstr(boolean override,
			MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, String constrName,
			Object... constrParams) {
		return callConstr(override, connectedClass, new SearchNamedUnsafeKey(
				connection, constrParams, constrName));
	}

	/*
	 * Search for ANY constructor that can be called with the given parameters
	 * (IF THERE ARE MULTIPLE CONSTRUCTORS THAT DO, THE CONSTRUCTOR THAT WILL BE
	 * CALLED MAY BE DIFFERENT FOR EACH CALL)
	 */

	/**
	 * Increases the reference counter for a given connection and uses the
	 * server-local class as client-local class. Uses assignment compatible
	 * matching algorithm to find the constructor and does not override any
	 * constructor that may be present on the responsible mapping stack element.
	 * Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callAnyConstr(MessageConnection connection,
			Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, getLocalInstance()
				.getClass(), new SearchCompatibleKey(connection, constrParams));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses assignment compatible matching algorithm to find the
	 * constructor and does not override any constructor that may be present on
	 * the responsible mapping stack element. Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callAnyConstr(MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, Object... constrParams) {
		return callConstr(DEFAULT_OVERRIDE_OPTION, connectedClass,
				new SearchCompatibleKey(connection, constrParams));
	}

	/**
	 * Increases the reference counter for a given connection and client-local
	 * class. Uses assignment compatible matching algorithm to find the
	 * constructor and may override any constructor that may be present on the
	 * responsible mapping stack element, depending on the <tt>override</tt>
	 * flag. Please see the documentation of
	 * <tt>callConstr(boolean, Class, SearchKey)</tt> and
	 * <tt>SearchExactKey</tt> for further details.
	 * 
	 * @param override
	 *            <tt>true</tt> if a constructor search key that is present on
	 *            the responsible mapping stack element should be overridden by
	 *            the given constructor invocation, <tt>false</tt> otherwise
	 * @param connection
	 *            connection for which the mapping stack should be changed
	 * @param connectedClass
	 *            client-local class for which the reference counter should be
	 *            increased
	 * @param constrParams
	 *            parameters passed to the constructor if a new client-local
	 *            instance is created
	 * @return the <tt>MappingStackElement</tt> for which the reference counter
	 *         has been increased
	 */
	public MappingStackElement callAnyConstr(boolean override,
			MessageConnection connection,
			Class<? extends SyncEntity> connectedClass, Object... constrParams) {
		return callConstr(override, connectedClass, new SearchCompatibleKey(
				connection, constrParams));
	}

}
