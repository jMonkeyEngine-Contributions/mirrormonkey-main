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
 * Default IR class used for constructor and method parameters.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ParameterIR extends AnnotationIR {

	/**
	 * The parameter of the last constructor or method that was collected
	 * matching the memberKeys of the owning <tt>MethodIR</tt> or
	 * <tt>ConstructorIR</tt>.
	 */
	private Parameter collectedParameter;

	/**
	 * Creates a new <tt>ParameterIR</tt> for a parameter of a method or
	 * constructor that was just encountered for the first time.
	 * 
	 * @param parser
	 *            the <tt>AnnotationParser</tt> responsible for parsing the
	 *            class hierarchy
	 */
	public ParameterIR(AnnotationParser parser) {
		super(parser);
	}

	/**
	 * Creates a new <tt>ParameterIR</tt> for a parameter that was previously
	 * encountered. Keeps the state of the <tt>ParameterIR</tt> that was
	 * previously responsible for that parameter.
	 * 
	 * @param previous
	 *            <tt>ParameterIR</tt> that was previously responsible for the
	 *            parameter
	 */
	public ParameterIR(ParameterIR previous) {
		super(previous);
		collectedParameter = previous.collectedParameter;
	}

	/**
	 * Collects a <tt>Parameter</tt> that was encountered when parsing a method
	 * or constructor and manages the IR responsible for that parameter
	 * according to the control annotations that the parameter declares.
	 * 
	 * @param p
	 *            the <tt>Parameter</tt> to parse
	 * @return the IR responsible for <tt>p</tt> with respect to its declared
	 *         control annotations
	 */
	public ParameterIR update(Parameter p) {
		collectedParameter = p;
		return (ParameterIR) parseAnnotatedElement(p, true);
	}

	/**
	 * @return the last <tt>Parameter</tt> that was collected using the
	 *         <tt>update</tt> method
	 */
	public Parameter getCollectedParameter() {
		return collectedParameter;
	}

	@Override
	public AnnotationIR overrideToDefault() {
		clear();
		return new ParameterIR(this);
	}

}
