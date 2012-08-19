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

package mirrormonkey.util.annotations.parsing;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Default IR class to collect information about methods when parsing a class
 * hierarchy.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class MethodIR extends MemberIR {

	/**
	 * Information about the parameters declared by the constructor represented
	 * by this <tt>ConstructorIR</tt> and their declared annotations.
	 */
	private ParameterIR[] parameterIRs;

	/**
	 * Creates a new <tt>MethodIR</tt>. Called when a method matching a method
	 * key was first encountered and no IR was found for that key.
	 * 
	 * @param classIR
	 *            IR responsible for collecting data about the class hierarchy
	 *            as a whole
	 * @param key
	 *            the key that this <tt>MethodIR</tt> will be mapped to in
	 *            <tt>classIR</tt>
	 * @param method
	 *            method that this <tt>MethodIR</tt> should collect data about
	 */
	public MethodIR(ClassIR classIR, MemberKey key, Method method) {
		super(classIR, key);
		parameterIRs = new ParameterIR[method.getParameterTypes().length];
		try {
			for (int i = 0; i < parameterIRs.length; i++) {
				parameterIRs[i] = (ParameterIR) getParser().getIRConstr(
						Parameter.class).newInstance(classIR.getParser());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new <tt>MethodIR</tt> for a method that already had a
	 * <tt>MethodIR</tt> and copies the state of the previous IR.
	 * 
	 * @param previous
	 *            the IR that previously collected data about the matching
	 *            method
	 */
	public MethodIR(MethodIR previous) {
		super(previous);
	}

	@Override
	public void loadFromPrevious(AnnotationIR prev) {
		super.loadFromPrevious(prev);
		MethodIR previous = (MethodIR) prev;
		parameterIRs = previous.parameterIRs;
	}

	@Override
	public void update(Member member) {
		checkMemberClass(member, Method.class);
		Method m = (Method) member;
		for (int i = 0; i < parameterIRs.length; i++) {
			parameterIRs[i] = parameterIRs[i]
					.update(new Parameter(m.getParameterAnnotations()[i], m
							.getParameterTypes()[i], i));
		}
		super.update(m);
	}

	/**
	 * @return IRs responsible to collect data about the parameters declared by
	 *         the method that this <tt>MethodIR</tt> represents
	 */
	public ParameterIR[] getParameterIRs() {
		return parameterIRs;
	}

	@Override
	public Method getCollectedMember() {
		return (Method) collectedMember;
	}

	@Override
	public AnnotationIR overrideToDefault() {
		clear();
		for (int i = 0; i < parameterIRs.length; i++) {
			parameterIRs[i] = (ParameterIR) parameterIRs[i].overrideToDefault();
		}
		return new MethodIR(this);
	}

}
