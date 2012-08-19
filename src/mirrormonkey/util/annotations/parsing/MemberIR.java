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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * IR class that tracks annotations of a <tt>Member</tt> as well as the member
 * itself. Will work in conjunction with <tt>ClassIR</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class MemberIR extends AnnotationIR {

	/**
	 * The <tt>ClassIR</tt> that keeps track of the parsed class hierarchy. This
	 * <tt>MemberIR</tt> is mapped to (at least) one <tt>MemberKey</tt> in the
	 * <tt>ClassIR</tt>.
	 */
	protected ClassIR classIR;

	/**
	 * The <tt>MemberKey</tt> that this <tt>MemberIR</tt> is mapped to in
	 * <tt>classIR</tt>.
	 */
	protected MemberKey key;

	/**
	 * The last <tt>Member</tt> that matched <tt>key</tt> in the class
	 * hierarchy.
	 */
	protected Member collectedMember;

	/**
	 * Creates a new <tt>MemberIR</tt>. This constructor will be called when no
	 * IR was mapped to <tt>key</tt> in <tt>classIR</tt> and this
	 * <tt>MemberIR</tt> is created for the currently parsed member.
	 * 
	 * @param classIR
	 *            the <tt>ClassIR</tt> that keeps track of the parsed class
	 *            hierarchy
	 * @param key
	 *            the <tt>key</tt> that this <tt>MemberIR</tt> will be mapped to
	 *            in <tt>classIR</tt> when it is registered
	 */
	public MemberIR(ClassIR classIR, MemberKey key) {
		super(classIR.getParser());
		this.classIR = classIR;
		this.key = key;
	}

	/**
	 * Creates a new <tt>MemberIR</tt> and loads the state of a previously
	 * present <tt>MemberIR</tt> for the same member.
	 * 
	 * @param previous
	 *            the <tt>MemberIR</tt> that was previously present for the
	 *            member
	 */
	public MemberIR(MemberIR previous) {
		super(previous);
	}

	@Override
	public void loadFromPrevious(AnnotationIR prev) {
		super.loadFromPrevious(prev);
		MemberIR previous = (MemberIR) prev;
		classIR = previous.classIR;
		key = previous.key;
		collectedMember = previous.collectedMember;
	}

	/**
	 * @return the <tt>ClassIR</tt> that keeps track of the currently parsed
	 *         class hierarchy
	 */
	public ClassIR getClassIR() {
		return classIR;
	}

	/**
	 * @return the <tt>MemberKey</tt> that this <tt>MemberIR</tt> is mapped to
	 *         in the current <tt>ClassIR</tt>
	 */
	public MemberKey getKey() {
		return key;
	}

	/**
	 * Maps this <tt>MemberIR</tt> to <tt>key</tt> in <tt>classIR</tt>.
	 * Subclasses may override this method if they wish to be mapped to multiple
	 * <tt>MemberKeys</tt>.
	 */
	public void register() {
		classIR.getMemberIRs().put(key, this);
	}

	/**
	 * @return the last <tt>Member</tt> that matched <tt>key</tt> in the
	 *         currently parsed class hierarchy
	 */
	public Member getCollectedMember() {
		return collectedMember;
	}

	/**
	 * Called whenever <tt>member</tt> matched any key that this
	 * <tt>MemberIR</tt> was mapped to in <tt>classIR</tt>.
	 * 
	 * The default implementation collects the member and its annotations and
	 * manages this IR according to the control annotations found on
	 * <tt>member</tt>.
	 * 
	 * @param member
	 *            the <tt>Member</tt> that matched one of the
	 *            <tt>MemberKeys</tt> that this <tt>MemberIR</tt> is mapped to
	 *            in <tt>classIR</tt>
	 */
	protected void update(Member member) {
		collectedMember = member;
		MemberIR newIR = (MemberIR) super.parseAnnotatedElement(
				(AnnotatedElement) member, true);
		if (newIR != this) {
			newIR.register();
		}
	}

	/**
	 * Called by subclasses to ensure that a member that should be parsed by
	 * them really is the member type that they responsible for.
	 * 
	 * @param collectedMember
	 *            the <tt>Member</tt> that should be parsed by a
	 *            <tt>MemberIR</tt>
	 * @param clazz
	 *            the <tt>Class</tt> that is expected for
	 *            <tt>collectedMember</tt>
	 * @throws IllegalArgumentException
	 *             if <tt>collectedMember.getClass</tt> does not return a class
	 *             equals to <tt>clazz</tt>
	 */
	protected static final void checkMemberClass(Member collectedMember,
			Class<?> clazz) {
		if (!collectedMember.getClass().equals(clazz)) {
			throw new IllegalArgumentException(
					"Annotation used on wrong member type.\n  clazz:" + clazz
							+ "\ncollectedmember:" + collectedMember);
		}
	}

}
