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

package mirrormonkey.state.annotations;

import mirrormonkey.state.DominantEntity;
import mirrormonkey.state.RelayEntity;
import mirrormonkey.state.member.StaticMemberStateDataIR;
import mirrormonkey.util.annotations.control.DefinePreset;
import mirrormonkey.util.annotations.control.IRClass;
import mirrormonkey.util.annotations.hfilter.ClassFilter;
import mirrormonkey.util.annotations.hfilter.ClassFilter.HierarchyType;

/**
 * Contains annotation presets for the state module.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StateAnnotationPresets {

	/**
	 * Default fields.
	 */
	@DefinePreset(UpdateState.class)
	@IRClass(StaticMemberStateDataIR.class)
	@TrackValue(false)
	@UpdateSetId(UpdateSetId.NO_SET)
	@RelayState(@ClassFilter(hierarchy = HierarchyType.HIERARCHY, value = RelayEntity.class))
	@DominantState(@ClassFilter(hierarchy = HierarchyType.HIERARCHY, value = DominantEntity.class))
	public Object field;

	/**
	 * Ensure that the right IR is used for virtual fields, even if only the
	 * corresponding real field is annotated with <tt>UpdateState</tt>.
	 */
	@DefinePreset(BindFieldFrom.class)
	@IRClass(StaticMemberStateDataIR.class)
	public Object field2;

	/**
	 * Ensure that the right IR is used for virtual fields, even if only the
	 * corresponding real field is annotated with <tt>UpdateState</tt>.
	 */
	@DefinePreset(BindFieldType.class)
	@IRClass(StaticMemberStateDataIR.class)
	public Object field3;

}
