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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.annotations.AssetInjection;
import mirrormonkey.framework.annotations.EntityInjection;
import mirrormonkey.framework.entity.StaticEntityDataIR;
import mirrormonkey.framework.member.MemberDataIR;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.rpc.annotations.AllowRpcFrom;
import mirrormonkey.rpc.annotations.InvokeLocally;
import mirrormonkey.rpc.annotations.InvokeLocally.LocalInvokeMode;
import mirrormonkey.rpc.annotations.RpcBindOverride;
import mirrormonkey.rpc.annotations.RpcTarget;
import mirrormonkey.util.annotations.hfilter.ClassFilter;
import mirrormonkey.util.annotations.parsing.ClassIR;
import mirrormonkey.util.annotations.parsing.MemberKey;
import mirrormonkey.util.annotations.parsing.MethodIR;

/**
 * IR class used to collect and extract data about methods that can be called
 * from remote.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class RpcMethodDataIR extends MethodIR implements MemberDataIR {

	/**
	 * Info about parsed data goes here.
	 */
	private static final Logger LOGGER = Logger.getLogger(RpcMethodDataIR.class
			.getName());

	/**
	 * Creates a new <tt>RpcMethodDataIR</tt> for a method that did not
	 * previously have an IR.
	 * 
	 * @param classIR
	 *            the IR responsible for keeping track of the class hierarchy
	 * @param key
	 *            the key that this <tt>RpcMethodDataIR</tt> will be bound to
	 * @param m
	 *            the method for which this <tt>RpcMethodDataIR</tt> is created
	 */
	public RpcMethodDataIR(ClassIR classIR, MemberKey key, Method m) {
		super(classIR, key, m);
		addCollectTypes();
	}

	/**
	 * Creates a new <tt>RpcMethodDataIR</tt> for a method that previously had
	 * an IR.
	 * 
	 * @param previous
	 *            the previous IR for the method
	 */
	public RpcMethodDataIR(MethodIR previous) {
		super(previous);
		addCollectTypes();
	}

	/**
	 * Convenience method to add annotation types from the RPC module.
	 */
	private void addCollectTypes() {
		addCollectType(RpcTarget.class);
		addCollectType(AllowRpcFrom.class);
		addCollectType(InvokeLocally.class);
		addCollectType(EntityInjection.class);
		addCollectType(AssetInjection.class);
		addCollectType(RpcBindOverride.class);
	}

	/**
	 * Creates the <tt>RpcMethodKey</tt> that will be used to bind the resulting
	 * <tt>RpcMethodData</tt> in its <tt>StaticEntityData</tt>.
	 * 
	 * @return the <tt>RpcMethodKey</tt> for which the resulting
	 *         <tt>StaticEntityData</tt> will be returned
	 */
	public RpcMethodKey createKey() {
		return new RpcMethodKey(getCollectedMember().getName(),
				getCollectedMember().getParameterTypes());
	}

	public StaticMemberData extractData(int id,
			EntityProvider<?> entityProvider, StaticEntityDataIR localIR,
			StaticEntityDataIR connectedIR) {
		RpcTarget annotation = getCollectedAnnotation(RpcTarget.class);
		AllowRpcFrom receiveFrom = getCollectedAnnotation(AllowRpcFrom.class);

		if (annotation == null) {
			throw new IllegalStateException(id + " :" + getClass() + "; key: "
					+ key + "; RpcTarget not found.");
		} else if (receiveFrom == null) {
			throw new IllegalStateException(getClass() + "; key: " + key
					+ "; AllowRpcFrom not found.");
		}
		boolean allowInbound = ClassFilter.Eval.contains(receiveFrom.value(),
				localIR.getConnectedClass());

		InvokeLocally local = getCollectedAnnotation(InvokeLocally.class);
		LocalInvokeMode localInvokeMode = local == null ? mirrormonkey.rpc.annotations.InvokeLocally.LocalInvokeMode.NONE
				: local.value();

		RpcMethodData d = new RpcMethodData(id, getCollectedMember(),
				entityProvider.getInterpreters(getParameterIRs()),
				entityProvider.getInterpreter(this, getCollectedMember()
						.getReturnType()), createKey(),
				localInvokeMode.equals(LocalInvokeMode.BEFORE),
				localInvokeMode.equals(LocalInvokeMode.AFTER),
				annotation.reliable(), allowInbound,
				annotation.responseTimeout(), annotation.callTimeout());
		LOGGER.info("Extracting RPC method data:" + "\n  IR class: "
				+ getClass() + "\n  ID: " + id + "\n  local method: "
				+ d.method + "\n  parameter interpreters: "
				+ Arrays.toString(d.paramAccessors)
				+ "\n  result interpreter: " + d.resultAccessor
				+ "\n  reliable: " + d.reliable + "\n  result timeout: "
				+ d.resultTimeout + "\n  call timeout: " + d.callTimeout
				+ "\n  allow inbound: " + d.allowInbound + "\n  member key: "
				+ d.memberKey + "\n  local invoke after: " + d.localInvokeAfter
				+ "\n  local invoke before: " + d.localInvokeBefore);

		return d;
	}
}
