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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import mirrormonkey.framework.annotations.AssetInjection;
import mirrormonkey.framework.annotations.EntityInjection;
import mirrormonkey.rpc.annotations.RpcBindOverride;
import mirrormonkey.util.annotations.parsing.MemberKey;
import mirrormonkey.util.annotations.parsing.MethodIR;
import mirrormonkey.util.annotations.parsing.MethodKeyDynamic;

/**
 * Used instead of <tt>RpcMethodDataIR</tt> if the <tt>RpcBindOverride</tt>
 * annotation is found on a method.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class BindOverrideMethodDataIR extends RpcMethodDataIR {

	/**
	 * Parameter types that the method is bound to (instead of its declared
	 * parameter types).
	 */
	private Class<?>[] boundParamClasses;

	/**
	 * The key that the method that will be actually invoked is bound to.
	 */
	private MemberKey realMethodKey;

	/**
	 * Creates a new <tt>BindOverrideMethodDataIR</tt> for a method that
	 * previously had a default IR.
	 * 
	 * @param previous
	 *            previous IR
	 */
	public BindOverrideMethodDataIR(MethodIR previous) {
		super(previous);
	}

	/**
	 * Creates a new <tt>BindOverrideMethodDataIR</tt> for a method that
	 * previously had an <tt>RpcMethodDataIR</tt>.
	 * 
	 * @param previous
	 *            previous IR
	 */
	public BindOverrideMethodDataIR(RpcMethodDataIR previous) {
		super(previous);
	}

	@Override
	public RpcMethodKey createKey() {
		return new RpcMethodKey(getCollectedMember().getName(),
				boundParamClasses);
	}

	@Override
	public void register() {
		Method m = getCollectedMember();
		realMethodKey = new MethodKeyDynamic(m);
		this.clear();
		RpcBindOverride rbo = (RpcBindOverride) currentCollection
				.get(RpcBindOverride.class);
		boundParamClasses = rbo.value();
		MethodKeyDynamic overriddenKey = new MethodKeyDynamic(m.getName(),
				rbo.value(), null);
		MethodIR ir = (MethodIR) classIR.getMemberIRs().get(overriddenKey);
		if (ir == null) {
			throw new IllegalArgumentException(
					"IR could not be found for key: " + overriddenKey);
		} else if (ir == this) {
			throw new IllegalStateException();
		} else if (BindOverrideMethodDataIR.class.isInstance(ir)
				&& !Arrays.deepEquals(m.getParameterTypes(), ir
						.getCollectedMember().getParameterTypes())) {
			throw new IllegalStateException("Ambiguous override: " + m);
		}

		for (int i = 0; i < m.getParameterTypes().length; i++) {
			Class<?> insertAs = m.getParameterTypes()[i];
			Class<?> insertFrom = overriddenKey.getParameterTypes()[i];
			if (ir.getParameterIRs()[i]
					.getCollectedAnnotation(AssetInjection.class) == null
					&& ir.getParameterIRs()[i]
							.getCollectedAnnotation(EntityInjection.class) == null
					&& !insertAs.isAssignableFrom(insertFrom)) {
				throw new IllegalArgumentException("Illegal override: method "
						+ m + ", parameter " + i + "; " + insertAs
						+ " is not assignable from " + insertFrom);
			}
		}

		if (ir.getCollectedAnnotation(AssetInjection.class) == null
				&& ir.getCollectedAnnotation(EntityInjection.class) == null
				&& !ir.getCollectedMember().getReturnType()
						.isAssignableFrom(getCollectedMember().getReturnType())) {
			throw new IllegalArgumentException("Illegal override: " + m
					+ " overridden return type " + ir.getCollectedMember()
					+ " incompatible with new return type.");
		}

		loadFromPrevious(ir);
		collectedMember = m;

		classIR.getMemberIRs().remove(key);
		classIR.getMemberIRs().remove(realMethodKey);
		classIR.getMemberIRs().put(key, this);
		classIR.getMemberIRs().put(realMethodKey, this);
	}

	@Override
	public void update(Member m) {
		// This is pretty hacky, but at least it should work in the context of
		// the test cases. More complicated using code may lead to unexpected
		// behavior.
		Method oldMethod = getCollectedMember();
		super.update(m);
		if (Arrays.deepEquals(boundParamClasses,
				((Method) m).getParameterTypes())) {
			collectedMember = oldMethod;
		}
	}
}
