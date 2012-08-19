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

import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.StaticEntityData;

/**
 * Indicates that the implementing class stores immutable data about a member of
 * an entity class.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface StaticMemberData {

	/**
	 * @return the <tt>MemberDataKey</tt> that this <tt>StaticMemberData</tt>
	 *         should be registered by in the <tt>StaticEntityData</tt> that
	 *         owns it
	 */
	public MemberDataKey getMemberKey();

	/**
	 * Called when an entity is made visible to the first connection.
	 * 
	 * @param entity
	 *            the entity that became visible
	 * @return an instance of <tt>DynamicMemberData</tt> that will contain
	 *         dynamic data about the member represented by this
	 *         <tt>StaticMemberData</tt> for <tt>entity</tt> or <tt>null</tt> if
	 *         this <tt>StaticMemberData</tt> does not request any dynamic data
	 *         to be stored
	 */
	public DynamicMemberData createDynamicData(DynamicEntityData entity);

	/**
	 * Called when this <tt>StaticMemberData</tt> is added to a
	 * <tt>StaticEntityData</tt>.
	 * 
	 * @param staticData
	 *            the <tt>StaticEntityData</tt> that owns this
	 *            <tt>StaticMemberData</tt>
	 */
	public void setStaticEntityData(StaticEntityData staticData);

}
