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

package mirrormonkey.core.member;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.logging.Logger;

import mirrormonkey.core.annotations.ClientSideConstructor;
import mirrormonkey.core.annotations.NamedClientSideConstructor;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.entity.StaticEntityDataIR;
import mirrormonkey.framework.member.MemberDataIR;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.util.annotations.parsing.ClassIR;
import mirrormonkey.util.annotations.parsing.ConstructorIR;
import mirrormonkey.util.annotations.parsing.MemberKey;

/**
 * Collects data about constructors that can be called to create client-local
 * instances.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ConstructorDataIR extends ConstructorIR implements MemberDataIR {

	/**
	 * Data about constructors goes here.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ConstructorDataIR.class.getName());

	/**
	 * Creates a new <tt>ConstructorDataIR</tt> for a constructor that did not
	 * have a previous IR.
	 * 
	 * @param cir
	 *            collects data about the class hierarchy
	 * @param key
	 *            used to bind this IR to match other members against it
	 * @param constr
	 *            constructor that had a <tt>ClientSideConstructor</tt>
	 *            annotation
	 */
	public ConstructorDataIR(ClassIR cir, MemberKey key, Constructor<?> constr) {
		super(cir, key, constr);
		this.addCollectType(ClientSideConstructor.class);
		this.addCollectType(NamedClientSideConstructor.class);
	}

	/**
	 * Creates a new <tt>ConstructorDataIR</tt> for a constructor that
	 * previously had an IR.
	 * 
	 * @param previous
	 *            the previous IR for the constructor
	 */
	public ConstructorDataIR(ConstructorIR previous) {
		super(previous);
		this.addCollectType(ClientSideConstructor.class);
		this.addCollectType(NamedClientSideConstructor.class);
	}

	public StaticMemberData extractData(int i,
			EntityProvider<?> entityProvider, StaticEntityDataIR localIR,
			StaticEntityDataIR connectedIR) {
		if (!getCollectedMember().getDeclaringClass().equals(
				localIR.getLocalClass())) {
			return null;
		}
		ConstructorData cd = new ConstructorData(i,
				entityProvider.getInterpreters(getParameterIRs()),
				getCollectedMember(),
				getCollectedAnnotation(NamedClientSideConstructor.class));
		LOGGER.info("Extracting constructor data.\n  IR class: " + getClass()
				+ "\n  ID: " + i + "\n  collected constructor:"
				+ cd.getConstr() + "\n  member key" + cd.getMemberKey()
				+ "\n  param interpreters: "
				+ Arrays.toString(cd.getInterpreters()));
		return cd;
	}
}
