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

package mirrormonkey.rpc.member;

import java.util.Arrays;

import mirrormonkey.framework.member.MemberDataKey;

/**
 * Used to map and look up data about an RPC method in <tt>StaticEntityData</tt>
 * by method signature.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcMethodKey implements MemberDataKey {

	/**
	 * Method name to look up.
	 */
	protected final String name;

	/**
	 * Parameter types of the method to look up.
	 */
	protected final Class<?>[] argTypes;

	/**
	 * Creates a new <tt>RpcMethodKey</tt> that can be used to store or look up
	 * an instance of <tt>RpcMethodData</tt> in <tt>StaticEntityData</tt>.
	 * 
	 * @param name
	 *            name of the method to look up
	 * @param argTypes
	 *            parameter types of the method to look up
	 */
	public RpcMethodKey(String name, Class<?>[] argTypes) {
		this.name = name;
		this.argTypes = argTypes;
	}

	@Override
	public int hashCode() {
		return 13 * name.hashCode() + argTypes.length;
	}

	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(RpcMethodKey.class)) {
			return false;
		}
		RpcMethodKey other = (RpcMethodKey) o;
		return name.equals(other.name)
				&& Arrays.deepEquals(argTypes, other.argTypes);
	}

	@Override
	public String toString() {
		String s = "[RpcMethodKey name=" + name + " parms={";
		for (Class<?> c : argTypes) {
			s += c.getName();
			s += " ";
		}
		s += "}]";
		return s;
	}
}