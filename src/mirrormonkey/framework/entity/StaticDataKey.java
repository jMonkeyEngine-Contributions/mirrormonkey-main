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
 * Immutable key class to map <tt>StaticEntityData</tt> instances to local and
 * connected class of an entity in the <tt>EntityProvider</tt>.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public final class StaticDataKey {

	/**
	 * Local entity class to fetch the <tt>StaticEntityData</tt> instance for.
	 */
	public final Class<? extends SyncEntity> localClass;

	/**
	 * Connected entity class to fetch the <tt>StaticEntityData</tt> instance
	 * for.
	 */
	public final Class<? extends SyncEntity> connectedClass;

	/**
	 * Creates a new <tt>StaticDataKey</tt> that can be used to look up
	 * <tt>StaticEntityData</tt> for given local and connected entity class.
	 * 
	 * @param localClass
	 *            local entity class to look up
	 * @param connectedClass
	 *            connected entity class to look up
	 */
	public StaticDataKey(Class<? extends SyncEntity> localClass,
			Class<? extends SyncEntity> connectedClass) {
		this.localClass = localClass;
		this.connectedClass = connectedClass;
	}

	@Override
	public final int hashCode() {
		return 13 * localClass.hashCode() + connectedClass.hashCode();
	}

	@Override
	public final boolean equals(Object o) {
		if (!o.getClass().equals(StaticDataKey.class)) {
			return false;
		}
		StaticDataKey cdk = (StaticDataKey) o;
		return cdk.localClass.equals(localClass)
				&& cdk.connectedClass.equals(connectedClass);
	}

	@Override
	public final String toString() {
		return "StaticDataKey[" + localClass.getName() + " : "
				+ connectedClass.getName() + "]";
	}

}