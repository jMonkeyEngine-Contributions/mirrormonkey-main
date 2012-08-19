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

package mirrormonkey.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that when transmitting the annotated element over the network,
 * asset injection should be used.
 * 
 * The runtime class of the annotated element must be either an <tt>Asset</tt>
 * or an <tt>AssetKey</tt>.
 * 
 * When transmitting the annotated element over the network, the sending side
 * will serialize and send the annotated element, if the annotated element is an
 * <tt>AssetKey</tt>. If the annotated element is an <tt>Asset</tt>, then its
 * <tt>getKey</tt> method will be called and the <tt>AssetKey</tt> returned by
 * that method will be transmitted over the network.
 * 
 * On the receiving side, the received key will be looked up in the local
 * <tt>AssetManager</tt> and inserted for the annotated element.
 * 
 * It is not possible to have both <tt>AssetInjection</tt> and
 * <tt>EntityInjection</tt> present on the same annotated element.
 * 
 * @author Philipp Christian Loewner
 * 
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
		ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AssetInjection {
}
