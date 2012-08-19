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

package mirrormonkey.framework.member;

import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.entity.StaticEntityDataIR;

/**
 * Indicates that the implementing class is an IR class that keeps track of a
 * member while parsing it. After parsing is done, immutable data on how to
 * handle the member can be extracted.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface MemberDataIR {

	/**
	 * Extracts immutable data on how to handle the member that was collected by
	 * this <tt>MemberDataIR</tt>.
	 * 
	 * @param id
	 *            unique ID of the member
	 * @param entityProvider
	 *            the <tt>EntityProvider</tt> that triggered parsing the class
	 *            hierarchy
	 * @param localIR
	 *            the <tt>StaticEntityDataIR</tt> that resulted when parsing the
	 *            local class hierarchy
	 * @param connectedIR
	 *            the <tt>StaticEntityDataIR</tt> that resulted when parsing the
	 *            connected class hierarchy
	 * @return an instance of <tt>StaticMemberData</tt> if this member is
	 *         somehow relevant to a module or <tt>null</tt> if it is not
	 */
	public StaticMemberData extractData(int id,
			EntityProvider<?> entityProvider, StaticEntityDataIR localIR,
			StaticEntityDataIR connectedIR);

}
