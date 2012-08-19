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

package mirrormonkey.state;

/**
 * Indicates that unless stated otherwise by the user by an explicit annotation
 * with <tt>DominantState</tt> or <tt>RelayState</tt>, the value of every
 * synchronized field or synchronized virtual field of instances of implementing
 * classes or their subclasses dominates the value of connected fields or
 * virtual fields of all connected classes.
 * 
 * More precisely, the above states the following: If a field or virtual field
 * in an implementing class is not annotated with either <tt>DominantState</tt>
 * or <tt>RelayState</tt>, then it will be considered a dominant field or
 * virtual field.
 * 
 * When synchronizing the value of fields over the network, a dominant field or
 * virtual field of a local instance will never receive updates from remote
 * instances of the same entity. Instead, every connected entity instance that
 * contains the same synchronized field or an equal virtual synchronized field
 * will receive updates from the local entity instance.
 * 
 * As a consequence, the value of dominant fields will be distributed over the
 * network for every entity.
 * 
 * More information about dominant state, relay state and dominated state is
 * available in the documentation.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public interface DominantEntity {

}
