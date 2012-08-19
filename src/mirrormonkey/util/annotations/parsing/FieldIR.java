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

import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * Default IR class to collect information about fields.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class FieldIR extends MemberIR {

	/**
	 * Creates a new <tt>FieldIR</tt>. Called when a field matching a field key
	 * was first encountered and no IR was found for that key.
	 * 
	 * @param classIR
	 *            IR responsible for collecting data about the class hierarchy
	 *            as a whole
	 * @param key
	 *            the key that this <tt>FieldIR</tt> will be mapped by in
	 *            <tt>classIR</tt>
	 * @param field
	 *            field that this <tt>FieldIR</tt> should collect data about
	 */
	public FieldIR(ClassIR classIR, MemberKey key,
			@SuppressWarnings("unused") Field field) {
		super(classIR, key);
	}

	/**
	 * Creates a new <tt>FieldIR</tt> for a field that already had a
	 * <tt>FieldIR</tt> and copies the state of that previous IR.
	 * 
	 * @param previous
	 *            the IR that previously collected data about the matching field
	 */
	public FieldIR(MemberIR previous) {
		super(previous);
		checkMemberClass(collectedMember, Field.class);
	}

	@Override
	public void update(Member m) {
		checkMemberClass(m, Field.class);
		super.update(m);
	}

	@Override
	public Field getCollectedMember() {
		return (Field) collectedMember;
	}

	@Override
	public AnnotationIR overrideToDefault() {
		clear();
		return new FieldIR(this);
	}

}
