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

package mirrormonkey.framework.entity;

/**
 * Indicates that instances of implementing classes are local instances of an
 * entity.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface SyncEntity {

	/**
	 * Sets the <tt>DynamicEntityData</tt> that keeps information about the
	 * entity that this <tt>SyncEntity</tt> is a local instance of.
	 * 
	 * It is imperative that implementations always return <tt>data</tt> on
	 * every call of <tt>getData</tt> or <tt>null</tt> if this method has not
	 * yet been called.
	 * 
	 * @param data
	 *            the <tt>DynamicEntityData</tt> responsible for keeping data
	 *            about the entity that this <tt>DynamicEntityData</tt> is a
	 *            local instance of
	 */
	public void setData(DynamicEntityData data);

	/**
	 * Fetches the <tt>DynamicEntityData</tt> instance that keeps track of
	 * framework internal information about the entity that this
	 * <tt>SyncEntity</tt> is a local instance of.
	 * 
	 * It is imperative that this method always return the parameter of the last
	 * call to <tt>setData</tt> or <tt>null</tt> if that method has not been
	 * called yet.
	 * 
	 * @return the last instance of <tt>DynamicEntityData</tt> that was passed
	 *         to <tt>setData</tt> or <tt>null</tt> if that method has not yet
	 *         been called
	 */
	public DynamicEntityData getData();

}
