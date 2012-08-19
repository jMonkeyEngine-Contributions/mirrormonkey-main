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

/**
 * Used to look up the <tt>MemberIR</tt> for the currently parsed
 * <tt>Member</tt> in a <tt>ClassIR</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public abstract class MemberKey implements Comparable<MemberKey> {

	public int compareTo(MemberKey k) {
		int thisOrder = getTypeOrder();
		int otherOrder = k.getTypeOrder();
		int i = thisOrder < otherOrder ? -1 : (thisOrder != otherOrder ? 1
				: compareSameTypeOrder(k));
		return i;
	}

	/*
	 * @Override public int hashCode() { return super.hashCode(); }
	 * 
	 * @Override public boolean equals(Object o) { return
	 * MemberKey.class.isAssignableFrom(o.getClass()) && compareTo((MemberKey)
	 * o) == 0; }
	 */

	/**
	 * Creates a new <tt>MemberKey</tt> that will match the same members as this
	 * one, only that instead of the old parameter list (in case of method and
	 * constructor keys) or type (in case of field keys) will the parameter list
	 * <tt>sig</tt> or the type <tt>sig[0]</tt> will be used.
	 * 
	 * @param sig
	 *            type or parameter list that the newly created
	 *            <tt>MemberKey</tt> will match
	 * @return new <tt>MemberKey</tt> with same class that will match
	 *         <tt>sig</tt> instead
	 */
	public abstract MemberKey copyForSignature(Class<?>[] sig);

	/**
	 * To ensure deterministic ordering of members, every inheriting class must
	 * return a unique <tt>int</tt> here.
	 * 
	 * @return <tt>int</tt> that will be used as the first level ordering to
	 *         compare different <tt>MemberKeys</tt>.
	 */
	public abstract int getTypeOrder();

	/**
	 * Compares this <tt>MemberKey</tt> to another <tt>MemberKey</tt> that
	 * returned the same int for a call of <tt>getTypeOrder</tt>. As long as the
	 * values returned by the <tt>getTypeOrder</tt> of every <tt>MemberKey</tt>
	 * type are unique for that type, users can be sure that <tt>k.getClass</tt>
	 * will equals <tt>this.getClass</tt>.
	 * 
	 * @param k
	 *            the <tt>MemberKey</tt> to compare this <tt>MemberKey</tt> to
	 * @return same as <tt>compareTo</tt>
	 */
	public abstract int compareSameTypeOrder(MemberKey k);

	/**
	 * Convenience method to compare the signature of two methods (or name and
	 * type of two fields).
	 * 
	 * @param name1
	 *            name of the first method or field
	 * @param params1
	 *            parameter list of the first method or array with length 1 and
	 *            first value type of the first field
	 * @param name2
	 *            name of the second method or field
	 * @param params2
	 *            parameter list of the first method or array with length 1 and
	 *            first value type of the second field
	 * @return same as <tt>compareTo</tt> but for signatures
	 */
	public static final int compareSignature(String name1, Class<?>[] params1,
			String name2, Class<?>[] params2) {
		int v;
		if ((v = name1.compareTo(name2)) != 0) {
			return v;
		}
		return compareParamTypes(params1, params2);
	}

	/**
	 * Convenience method to copare the parameter list of two methods (or types
	 * of fields).
	 * 
	 * @param first
	 *            parameter list of the first method or array with length 1 and
	 *            first value set to the type of the first field
	 * @param second
	 *            parameter list of the second method or array with length 1 and
	 *            first value set to the type of the second field
	 * @return same as <tt>compareTo</tt> but for parameter lists
	 */
	public static final int compareParamTypes(Class<?>[] first,
			Class<?>[] second) {
		int v;
		if ((v = ((Integer) first.length).compareTo(second.length)) != 0) {
			return v;
		}
		int i = 0;
		while (i < first.length
				&& (v = compareClasses(first[i], second[i])) == 0) {
			i++;
		}
		return v;
	}

	/**
	 * Convenience method to compare two classes by their names.
	 * 
	 * @param first
	 *            first class to compare
	 * @param second
	 *            second class to compare
	 * @return same as comparing <tt>first.getName</tt> to
	 *         <tt>second.getName</tt>, obviously
	 */
	public static final int compareClasses(Class<?> first, Class<?> second) {
		return first.getName().compareTo(second.getName());
	}

}