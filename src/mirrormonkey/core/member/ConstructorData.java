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

import mirrormonkey.core.annotations.NamedClientSideConstructor;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.framework.member.DynamicMemberData;
import mirrormonkey.framework.member.MemberDataKey;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.framework.parameter.ValueInterpreter;
import mirrormonkey.framework.parameter.ValueUtil;

/**
 * Contains immutable data about a constructor that can be called on the client
 * side.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ConstructorData implements StaticMemberData {

	/**
	 * The <tt>PresentKey</tt> used to match searches against the represented
	 * constructor.
	 */
	private final MemberDataKey key;

	/**
	 * The unique ID of this constructor, which is its index in the id bound
	 * <tt>StaticMemberData</tt> array in <tt>StaticEntityData</tt>
	 */
	private final int id;

	/**
	 * The <tt>ValueInterpreters</tt> used to pack and unpack constructor for
	 * transmitting them over the network.
	 */
	private final ValueInterpreter[] interpreters;

	/**
	 * The represented constructor.
	 */
	private final Constructor<?> constr;

	/**
	 * The static context for which this <tt>ConstructorData</tt> represents a
	 * valid constructor.
	 */
	private StaticEntityData staticData;

	/**
	 * Creates a new <tt>ConstructorData</tt> that will store the given data.
	 * 
	 * @param id
	 *            unique member ID of this constructor in its static context
	 * @param interpreters
	 *            used to pack and unpack parameters for network transfer
	 * @param constr
	 *            represented constructor
	 * @param nameAnnot
	 *            collected <tt>NamedClientSideConstructor</tt> or <tt>null</tt>
	 *            if none was collected
	 */
	public ConstructorData(int id, ValueInterpreter[] interpreters,
			Constructor<?> constr, NamedClientSideConstructor nameAnnot) {
		if (nameAnnot == null) {
			key = new PresentKey(this);
		} else {
			key = new PresentNamedKey(this, nameAnnot.value());
		}
		this.id = id;
		this.interpreters = interpreters;
		this.constr = constr;
	}

	/**
	 * Reflectively creates a new local entity instance by invoking the
	 * represented constructor for a given array of parameters.
	 * 
	 * @param constrParams
	 *            parameters that should be passed to the constructor invocation
	 * @return the newly created local entity instance
	 */
	public SyncEntity newEntity(Object[] constrParams) {
		try {
			return (SyncEntity) getConstr().newInstance(
					ValueUtil.extractData(getInterpreters(), constrParams));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return ConstructorData.class.hashCode()
				* constr.getDeclaringClass().hashCode() + id;
	}

	@Override
	public boolean equals(Object o) {
		if (!ConstructorData.class.isInstance(o)) {
			return false;
		}
		ConstructorData cd = (ConstructorData) o;
		return cd.id == id;
	}

	public MemberDataKey getMemberKey() {
		return key;
	}

	/**
	 * @return fully-qualified name of the connected class
	 */
	public String getConnectedClassName() {
		return staticData.getConnectedClass().getName();
	}

	/**
	 * @return unique ID of this <tt>ConstructorData</tt> in its valid static
	 *         context
	 */
	public int getId() {
		return id;
	}

	public DynamicMemberData createDynamicData(DynamicEntityData entity) {
		return null;
	}

	public void setStaticEntityData(StaticEntityData staticData) {
		this.staticData = staticData;
	}

	/**
	 * @return static context in which the represented constructor can be called
	 */
	public StaticEntityData getStaticData() {
		return staticData;
	}

	/**
	 * @return fully-qualified name of the local class
	 */
	public String getLocalClassName() {
		return staticData.getLocalClass().getName();
	}

	@Override
	public String toString() {
		return "[" + super.toString() + ": static=" + staticData
				+ "\n  constructor=" + getConstr() + "]\n";
	}

	/**
	 * @return the represented constructor
	 */
	public Constructor<?> getConstr() {
		return constr;
	}

	/**
	 * @return the <tt>ValueInterpreters</tt> used to pack and unpack parameters
	 *         when transmitting them over the network
	 */
	public ValueInterpreter[] getInterpreters() {
		return interpreters;
	}

}
