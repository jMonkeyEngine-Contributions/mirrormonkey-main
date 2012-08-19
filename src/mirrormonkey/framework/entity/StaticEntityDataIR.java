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

package mirrormonkey.framework.entity;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import mirrormonkey.core.annotations.NotifyLifecycleListeners;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.SyncAppState;
import mirrormonkey.framework.member.MemberDataIR;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.util.annotations.hfilter.ClassFilter;
import mirrormonkey.util.annotations.parsing.AnnotationParser;
import mirrormonkey.util.annotations.parsing.ClassIR;
import mirrormonkey.util.annotations.parsing.MemberIR;

/**
 * Responsible for keeping data about an entity class hierarchy that is
 * currently being parsed. As soon as parsing is finished, resulting
 * <tt>StaticEntityData</tt> can be extracted.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StaticEntityDataIR extends ClassIR {

	/**
	 * Logging
	 */
	private static final Logger LOGGER = Logger
			.getLogger(StaticEntityDataIR.class.getName());

	/**
	 * The <tt>SyncAppState</tt> for which the class hierarchy is parsed.
	 */
	private SyncAppState<?> appState;

	/**
	 * The class of the client-local entity.
	 */
	private Class<? extends SyncEntity> localClass;

	/**
	 * The connected entity class.
	 */
	private Class<? extends SyncEntity> connectedClass;

	/**
	 * Creates a new <tt>StaticEntityDataIR</tt> with a callback reference to
	 * the <tt>AnnotationParser</tt> that created it.
	 * 
	 * @param parser
	 *            the <tt>AnnotationParser</tt> responsible for parsing the
	 *            entity class hierarchy
	 */
	public StaticEntityDataIR(AnnotationParser parser) {
		super(parser);
		addCollectType(NotifyLifecycleListeners.class);
	}

	/**
	 * Called after the hierarchy of the local and connected entity class have
	 * been parsed to extract immutable data about how to handle entity
	 * instances with parsed local and connected class.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> for which the data is created
	 * @param connectedIR
	 *            the <tt>StaticEntityDataIR</tt> that resulted from parsing the
	 *            class hierarchy of the connected entity class (in contrast to
	 *            this <tt>StaticEntityDataIR</tt>, which resulted from parsing
	 *            the local entity class hierarchy)
	 * @param entityProvider
	 *            the <tt>EntityProvider</tt> to which the extracted data will
	 *            be registered
	 * @param collectLocalConstructors
	 *            <tt>true</tt>, if to determine which constructors are
	 *            callable, the local hierarchy should be regarded (client
	 *            side), <tt>false</tt> if the constructors from the connected
	 *            class should be regarded (server side)
	 * @return an instance of <tt>StaticEntityData</tt> that contains
	 *         information about how to handle entity instance if their local
	 *         and connected class correspond to the parsed classes
	 */
	@SuppressWarnings("unchecked")
	public StaticEntityData extractData(SyncAppState<?> appState,
			StaticEntityDataIR connectedIR, EntityProvider<?> entityProvider,
			boolean collectLocalConstructors) {
		LOGGER.info("BEGIN parsing class hierarchy.");
		this.appState = appState;
		connectedIR.appState = appState;

		this.localClass = (Class<? extends SyncEntity>) topDownParsedClasses
				.peekLast();
		this.connectedClass = (Class<? extends SyncEntity>) connectedIR.topDownParsedClasses
				.peekLast();
		connectedIR.localClass = connectedClass;
		connectedIR.connectedClass = localClass;

		StaticMemberData smd = null;
		List<StaticMemberData> sortedMembers = new LinkedList<StaticMemberData>();
		Set<MemberIR> hashedIRs = new HashSet<MemberIR>();
		StaticEntityDataIR constrIR = collectLocalConstructors ? this
				: connectedIR;
		for (MemberIR ir : constrIR.getMemberIRs().values()) {
			if (!ir.getCollectedMember().getClass().equals(Constructor.class)) {
				// Break instead of continue since members are sorted,
				// Constructors come first
				break;
			}
			if (MemberDataIR.class.isAssignableFrom(ir.getClass())
					&& (smd = ((MemberDataIR) ir).extractData(
							sortedMembers.size(), entityProvider, constrIR,
							null)) != null && !hashedIRs.contains(ir)) {
				sortedMembers.add(smd);
				hashedIRs.add(ir);
			}
		}

		for (MemberIR ir : getMemberIRs().values()) {
			if (ir.getCollectedMember().getClass().equals(Constructor.class)) {
				continue;
			}
			if (MemberDataIR.class.isAssignableFrom(ir.getClass())
					&& (smd = ((MemberDataIR) ir).extractData(
							sortedMembers.size(), entityProvider, this,
							connectedIR)) != null && !hashedIRs.contains(ir)) {
				sortedMembers.add(smd);
				hashedIRs.add(ir);
			}
		}

		NotifyLifecycleListeners nll = getCollectedAnnotation(NotifyLifecycleListeners.class);
		List<Class<?>> parsed = extractNotifyClasses(nll, this);
		parsed.addAll(extractNotifyClasses(nll, connectedIR));

		StaticEntityData d = new StaticEntityData(
				localClass,
				connectedClass,
				sortedMembers.toArray(new StaticMemberData[sortedMembers.size()]),
				parsed.toArray(new Class<?>[parsed.size()]));

		LOGGER.info("Parsed entity class hierarchy with the following result:\n  IR class: "
				+ getClass()
				+ "\n  local class: "
				+ localClass
				+ d.getLocalClass()
				+ "\n  connected class: "
				+ d.getConnectedClass()
				+ "\n  notify classes: "
				+ Arrays.toString(d.getNotifyClasses()));

		return d;
	}

	/**
	 * Extracts a list of all classes for which class bound instance lifecycle
	 * listeners should be notified when a lifecycle events occurs for an entity
	 * with local and connected class corresponding to the classes parsed for a
	 * given <tt>StaticEntityDataIR</tt>
	 * 
	 * @param nll
	 *            the annotation containing the class filters against which the
	 *            classes from the hierarchy should be evaluated
	 * @param ir
	 *            the <tt>StaticEntityDataIR</tt> that resulted from parsing the
	 *            local entity class hierarchy
	 * @return a <tt>List</tt> containing every class from the parsed local
	 *         class hierarchy (including interfaces and superinterfaces) that
	 *         evaluates to true against the given <tt>ClassFilters</tt> for the
	 *         local <tt>NotifyLifecycleListeners</tt> annotation
	 */
	private static final List<Class<?>> extractNotifyClasses(
			NotifyLifecycleListeners nll, StaticEntityDataIR ir) {
		List<Class<?>> parsed = new LinkedList<Class<?>>();
		if (nll != null) {
			parsed.addAll(ir.topDownParsedClasses);
			parsed.addAll(ir.topDownParsedInterfaces);
			ClassFilter.Eval.filter(nll.value(), parsed);
		}
		return parsed;
	}

	/**
	 * @return the <tt>SyncAppState</tt> that requested parsing the class
	 *         hierarchy
	 */
	public SyncAppState<?> getAppState() {
		return appState;
	}

	/**
	 * @return the local entity class
	 */
	public Class<? extends SyncEntity> getLocalClass() {
		return localClass;
	}

	/**
	 * @return the connected entity class
	 */
	public Class<? extends SyncEntity> getConnectedClass() {
		return connectedClass;
	}

}
