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
import java.util.LinkedHashSet;
import java.util.Set;

import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.member.DynamicMemberData;

/**
 * Contains data about an entity in a specific context, where a context is the
 * local and one of the connected classes.
 * 
 * Instances of this class are created when an entity becomes visible to the
 * first client with a given client-local class. They keep track of connections
 * where that entity is visible with the same client-local class and is
 * destroyed once no client uses the client-local class any more.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class Interpretation {

	/**
	 * Contains data about the entity that this <tt>Interpretation</tt> belongs
	 * to.
	 */
	private final ServerEntityData entity;

	/**
	 * Contains data about the static context in which the entity is visible.
	 */
	private final StaticEntityData staticData;

	/**
	 * Contains mutable data that different modules want to store about members
	 * of the local instance.
	 */
	private final DynamicMemberData[] dynamicData;

	/**
	 * Contains data about all connections that this <tt>Interpretation</tt> is
	 * visible to, even if the context of this <tt>Interpretation</tt> is not on
	 * top of the mapping stack for <tt>entity</tt> and those connections.
	 */
	private final Set<ServerConnectionInfo> allConnections;

	/**
	 * Contains the same elements as <tt>allConnections</tt> to be able to
	 * return them without risking inconsistent state.
	 */
	private final Set<ServerConnectionInfo> allConnectionsUmod;

	/**
	 * Contains data about all connections that this <tt>Interpretation</tt> is
	 * visible to and where <tt>staticData</tt> is on top of the mapping stack.
	 */
	private final Set<ServerConnectionInfo> activeConnections;

	/**
	 * Contains the same elements as <tt>activeConnections</tt> to be able to
	 * return them without risking inconsistent state.
	 */
	private final Set<ServerConnectionInfo> activeConnectionsUmod;

	/**
	 * Creates a new <tt>Interpretation</tt> that will store information about a
	 * given entity in a given context.
	 * 
	 * @param entity
	 *            the <tt>ServerEntityData</tt> representing the entity that
	 *            this <tt>Interpretation</tt> contains data about
	 * @param staticData
	 *            the <tt>StaticEntityData</tt> containing data about the
	 *            context in which <tt>entity</tt> is to be interpreted
	 */
	public Interpretation(ServerEntityData entity, StaticEntityData staticData) {
		this.entity = entity;
		this.staticData = staticData;
		dynamicData = staticData.createDynamicMemberData(entity);
		allConnections = new LinkedHashSet<ServerConnectionInfo>();
		allConnectionsUmod = Collections.unmodifiableSet(allConnections);
		activeConnections = new LinkedHashSet<ServerConnectionInfo>();
		activeConnectionsUmod = Collections.unmodifiableSet(activeConnections);
	}

	/**
	 * @return the <tt>StaticEntityData</tt> telling us how to interpret the
	 *         entity
	 */
	public StaticEntityData getStaticData() {
		return staticData;
	}

	/**
	 * @return array of mutable data that modules want to keep about the
	 *         interpreted entity instance (if any)
	 */
	public DynamicMemberData[] getDynamicData() {
		return dynamicData;
	}

	/**
	 * @return collection of connections that this <tt>Interpretation</tt> is
	 *         visible to where <tt>staticData</tt> is on top of the mapping
	 *         stack
	 */
	public Collection<ServerConnectionInfo> getActiveConnections() {
		return activeConnectionsUmod;
	}

	/**
	 * @return collection of all connections that this <tt>Interpretation</tt>
	 *         is visible to where <tt>staticData</tt> is not necessarily on top
	 *         of the mapping stack
	 */
	public Collection<ServerConnectionInfo> getAllConnections() {
		return allConnectionsUmod;
	}

	/**
	 * @return <tt>true</tt>, if this <tt>Interpretation</tt> is not visible to
	 *         any clients, <tt>false</tt> if it is
	 */
	public boolean isEmpty() {
		return allConnections.isEmpty();
	}

	/**
	 * @return <tt>true</tt>, if this <tt>Interpretation</tt> may be visible to
	 *         some clients, but it is not on top of the mapping stack for any,
	 *         <tt>false</tt> if it is on top of the mapping stack for at least
	 *         one client
	 */
	public boolean isPassive() {
		return activeConnections.isEmpty();
	}

	/**
	 * Called when <tt>staticData</tt> happens to be on top of the mapping stack
	 * for a connection and the entity represented by <tt>entity</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param info
	 *            data about the connection
	 */
	protected void makeActive(ServerConnectionInfo info) {
		add(info);
		activeConnections.add(info);
	}

	/**
	 * Called when the entity represented by <tt>entity</tt> is still visible to
	 * a connection, but <tt>staticData</tt> is no longer on top of the mapping
	 * stack.
	 * 
	 * Internal use only.
	 * 
	 * @param info
	 *            data about the connection
	 */
	protected void makePassive(ServerConnectionInfo info) {
		activeConnections.remove(info);
	}

	/**
	 * Called when the entity represented by <tt>entity</tt> became visible to a
	 * connection, but <tt>staticData</tt> is not located on top of the mapping
	 * stack.
	 * 
	 * Internal use only.
	 * 
	 * @param info
	 *            data about the connection
	 */
	protected void add(ServerConnectionInfo info) {
		boolean wasEmpty = isEmpty();
		allConnections.add(info);
		if (wasEmpty) {
			entity.interpretationCreationCallback(this);
		}
	}

	/**
	 * Called when the entity represented by <tt>entity</tt> is no longer
	 * visible to a connection in the context <tt>staticData</tt>.
	 * 
	 * Internal use only.
	 * 
	 * @param info
	 *            data about the connection
	 */
	protected void remove(ServerConnectionInfo info) {
		boolean wasEmpty = isEmpty();
		makePassive(info);
		allConnections.remove(info);
		if (!wasEmpty && isEmpty()) {
			entity.interpretationDestructionCallback(this);
		}
	}

}
