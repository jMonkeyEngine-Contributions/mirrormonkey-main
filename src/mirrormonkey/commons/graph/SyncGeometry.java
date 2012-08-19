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

package mirrormonkey.commons.graph;

import mirrormonkey.core.annotations.NotifyLifecycleListeners;
import mirrormonkey.framework.entity.DynamicEntityData;
import mirrormonkey.framework.entity.SyncEntity;
import mirrormonkey.util.annotations.hfilter.ClassFilter;
import mirrormonkey.util.annotations.hfilter.ClassFilter.HierarchyType;

import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

@NotifyLifecycleListeners(@ClassFilter(hierarchy = HierarchyType.HIERARCHY, value = { Spatial.class }))
public class SyncGeometry extends Geometry implements SyncEntity {

	private DynamicEntityData data;

	protected SyncGeometry(String name, Vector3f localTranslation) {
		super(name);
		setLocalTranslation(localTranslation);
	}

	protected SyncGeometry(String name, Mesh mesh, Vector3f localTranslation) {
		super(name, mesh);
		setLocalTranslation(localTranslation);
	}

	public SyncGeometry(String name, Mesh mesh, Vector3f localTranslation,
			MaterialDef matDef) {
		this(name, mesh, localTranslation, new Material(
				matDef.getAssetManager(), matDef.getAssetName()));
	}

	public SyncGeometry(String name, Mesh mesh, Vector3f localTranslation,
			Material mat) {
		this(name, mesh, localTranslation);
		mat.setColor("Color", ColorRGBA.Blue);
		setMaterial(mat);
	}

	public void setData(DynamicEntityData data) {
		this.data = data;
	}

	public DynamicEntityData getData() {
		return data;
	}

}
