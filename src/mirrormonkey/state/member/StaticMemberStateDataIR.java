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

package mirrormonkey.state.member;

import static mirrormonkey.state.member.BeanUtil.cutPrefix;
import static mirrormonkey.state.member.BeanUtil.getterName;
import static mirrormonkey.state.member.BeanUtil.isGetterName;
import static mirrormonkey.state.member.BeanUtil.isSetterName;
import static mirrormonkey.state.member.BeanUtil.setterName;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.logging.Logger;

import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.EntityProvider;
import mirrormonkey.framework.annotations.AssetInjection;
import mirrormonkey.framework.annotations.EntityInjection;
import mirrormonkey.framework.entity.StaticEntityDataIR;
import mirrormonkey.framework.member.MemberDataIR;
import mirrormonkey.framework.member.StaticMemberData;
import mirrormonkey.framework.parameter.ValueInterpreter;
import mirrormonkey.state.annotations.BindFieldFrom;
import mirrormonkey.state.annotations.BindFieldType;
import mirrormonkey.state.annotations.DominantState;
import mirrormonkey.state.annotations.RelayState;
import mirrormonkey.state.annotations.TrackValue;
import mirrormonkey.state.annotations.UpdateSetId;
import mirrormonkey.state.annotations.UpdateState;
import mirrormonkey.state.member.accessor.FieldReadAccessor;
import mirrormonkey.state.member.accessor.FieldWriteAccessor;
import mirrormonkey.state.member.accessor.GetterReadAccessor;
import mirrormonkey.state.member.accessor.SetterWriteAccessor;
import mirrormonkey.state.member.accessor.ValueReadAccessor;
import mirrormonkey.state.member.accessor.ValueWriteAccessor;
import mirrormonkey.util.annotations.hfilter.ClassFilter;
import mirrormonkey.util.annotations.parsing.AnnotationIR;
import mirrormonkey.util.annotations.parsing.ClassIR;
import mirrormonkey.util.annotations.parsing.FieldIR;
import mirrormonkey.util.annotations.parsing.FieldKeyStatic;
import mirrormonkey.util.annotations.parsing.MemberIR;
import mirrormonkey.util.annotations.parsing.MethodIR;
import mirrormonkey.util.annotations.parsing.MethodKeyDynamic;

/**
 * Used to track data about synchronized fields or virtual fields while parsing
 * entity class hierarchies.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class StaticMemberStateDataIR extends MemberIR implements MemberDataIR {

	/**
	 * Info about created data goes here.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(StaticMemberStateDataIR.class.getName());

	/**
	 * Used to override to default annotation (if <tt>AnnotationOverride</tt> is
	 * used on the tracked field).
	 */
	protected Member lastMemberForReset;

	/**
	 * Name of the tracked field.
	 */
	protected String name;

	/**
	 * Type of the tracked field.
	 */
	protected Class<?> type;

	/**
	 * Key for real field, if any.
	 */
	protected FieldKeyStatic fieldKey;

	/**
	 * Real field, if any.
	 */
	protected Field field;

	/**
	 * Key for virtual field getter, if any.
	 */
	protected MethodKeyDynamic getterKey;

	/**
	 * Getter for virtual field, if any.
	 */
	protected Method getter;

	/**
	 * Key for virtual field setter, if any.
	 */
	protected MethodKeyDynamic setterKey;

	/**
	 * Setter for real field, if any.
	 */
	protected Method setter;

	/**
	 * Creates a new <tt>StaticMemberStateDataIR</tt> for a real field that
	 * previously had the default IR.
	 * 
	 * @param fieldIR
	 *            default IR that tracked the field
	 */
	public StaticMemberStateDataIR(FieldIR fieldIR) {
		super(fieldIR);
		addCollectTypes();

		initFromKey((FieldKeyStatic) fieldIR.getKey());
	}

	/**
	 * Creates a new <tt>StaticMemberStateDataIR</tt> for a virtual field that
	 * previously had the default IR.
	 * 
	 * @param methodIR
	 *            default IR that tracked the getter or setter for the virtual
	 *            field
	 */
	public StaticMemberStateDataIR(MethodIR methodIR) {
		super(methodIR);
		addCollectTypes();

		initFromKey((MethodKeyDynamic) methodIR.getKey());
	}

	/**
	 * Creates a new <tt>StaticMemberStateDataIR</tt> for a real field that did
	 * not previously have an IR.
	 * 
	 * @param classIR
	 *            tracks data about the entity class hierarchy
	 * @param key
	 *            key for the real field
	 */
	public StaticMemberStateDataIR(ClassIR classIR, FieldKeyStatic key) {
		super(classIR, key);
		addCollectTypes();

		initFromKey(key);
	}

	/**
	 * Creates a new <tt>StaticMemberStateDataIR</tt> for a virtual field that
	 * did not previously have an IR.
	 * 
	 * @param classIR
	 *            tracks data about the entity class hierarchy
	 * @param key
	 *            key for the getter or setter for the virtual field
	 */
	public StaticMemberStateDataIR(ClassIR classIR, MethodKeyDynamic key) {
		super(classIR, key);
		addCollectTypes();

		initFromKey(key);
	}

	/**
	 * Initializes for tracking a real field.
	 * 
	 * @param key
	 *            key for the real field
	 */
	protected void initFromKey(FieldKeyStatic key) {
		name = key.getName();
		fieldKey = key;
		type = key.getType();
	}

	/**
	 * Initializes for tracking a virtual field.
	 * 
	 * @param key
	 *            key for the getter or setter for the virtual field
	 */
	protected void initFromKey(MethodKeyDynamic key) {
		if (isSetterName(key.getName())) {
			if (key.getParameterTypes().length != 1
					|| !key.getReturnType().equals(void.class)) {
				throw new IllegalArgumentException("Not really a setter: "
						+ key);
			}
			setterKey = key;
			type = key.getParameterTypes()[0];
		} else if (isGetterName(key.getName())) {
			if (key.getParameterTypes().length != 0) {
				throw new IllegalArgumentException("Not really a getter: "
						+ key);
			}
			getterKey = key;
			type = key.getReturnType();
		} else {
			throw new IllegalArgumentException(
					"Name does not indicate getter or setter: " + key);
		}
		name = cutPrefix(key.getName());
	}

	/**
	 * Convenience method to collect every annotation used in the state module.
	 */
	protected void addCollectTypes() {
		addCollectType(BindFieldFrom.class);
		addCollectType(BindFieldType.class);
		addCollectType(DominantState.class);
		addCollectType(RelayState.class);
		addCollectType(TrackValue.class);
		addCollectType(AssetInjection.class);
		addCollectType(EntityInjection.class);
		addCollectType(UpdateState.class);
		addCollectType(UpdateSetId.class);
	}

	@Override
	public AnnotationIR parseAnnotatedElement(AnnotatedElement element,
			boolean b) {
		AnnotationIR newIR = super.parseAnnotatedElement(element, true);
		if (newIR != this) {
			throw new IllegalStateException("You can not change this IR.");
		}
		return this;
	}

	@Override
	protected void parseCollectAnnotations() {
		super.parseCollectAnnotations();

		BindFieldFrom bff = (BindFieldFrom) currentCollection
				.get(BindFieldFrom.class);
		BindFieldType bft = (BindFieldType) currentCollection
				.get(BindFieldType.class);
		if (bff != null) {
			bindFieldFrom(bff);
		}
		if (bft != null) {
			bindFieldType(bft);
		}
	}

	@Override
	public void register() {
		Member member = getCollectedMember();
		if (Field.class.isInstance(member)) {
			initFromField((Field) member);
		} else if (Method.class.isInstance(member)) {
			Method m = (Method) member;
			if (isGetterName(m.getName())) {
				initFromGetter(m);
			} else if (isSetterName(m.getName())) {
				initFromSetter(m);
			} else {
				throw new IllegalArgumentException(member
						+ " is neither a getter nor a setter.");
			}
		} else {
			throw new IllegalArgumentException(member
					+ " is neither field nor method; name: " + name);
		}
	}

	@Override
	protected void update(Member member) {
		lastMemberForReset = member;
		super.update(member);
		collectMember(member);
	}

	/**
	 * Collects a member, depending on its type.
	 * 
	 * @param member
	 *            member that is currently parsed
	 */
	protected void collectMember(Member member) {
		if (Field.class.isInstance(member)) {
			field = (Field) member;
		} else if (Method.class.isInstance(member)) {
			Method m = (Method) member;
			if (isGetterName(m.getName())) {
				getter = m;
			} else if (isSetterName(m.getName())) {
				setter = m;
			}
		} else {
			throw new IllegalArgumentException(member
					+ " is neither field nor method.");
		}
	}

	/**
	 * Called when the <tt>BindFieldFrom</tt> annotation is used on the tracked
	 * virtual field.
	 * 
	 * Takes care of registering this IR for the real field.
	 * 
	 * @param bff
	 *            annotation that was found
	 */
	protected void bindFieldFrom(BindFieldFrom bff) {
		if (fieldKey != null) {
			classIR.getMemberIRs().remove(fieldKey);
			field = null;
		}
		fieldKey = new FieldKeyStatic(bff.value(), name, type);
		MemberIR ir = classIR.getMemberIRs().get(fieldKey);
		if (ir != null) {
			carryAnnotations(ir);
			field = (Field) ir.getCollectedMember();
		}
		classIR.getMemberIRs().put(fieldKey, this);
	}

	/**
	 * Called when the <tt>BindFieldType</tt> annotation is used on the tracked
	 * field.
	 * 
	 * Disposes of old IRs for the tracked fields and registers this IR instead
	 * of them.
	 * 
	 * @param bft
	 *            annotation that was found
	 */
	protected void bindFieldType(BindFieldType bft) {
		type = bft.value();
		if (fieldKey != null) {
			classIR.getMemberIRs().remove(fieldKey);
			fieldKey = new FieldKeyStatic(fieldKey.getDeclaringClass(),
					fieldKey.getName(), type);
			MemberIR ir = classIR.getMemberIRs().get(fieldKey);
			if (ir != null) {
				carryAnnotations(ir);
				field = (Field) ir.getCollectedMember();
			}
			classIR.getMemberIRs().put(fieldKey, this);
		}

		if (getterKey != null) {
			classIR.getMemberIRs().remove(getterKey);
		}
		getterKey = new MethodKeyDynamic(getterName(name), new Class<?>[0],
				type);
		MemberIR ir = classIR.getMemberIRs().get(getterKey);
		if (ir != null) {
			carryAnnotations(ir);
			getter = (Method) ir.getCollectedMember();
		}
		classIR.getMemberIRs().put(getterKey, this);

		if (setterKey != null) {
			classIR.getMemberIRs().remove(setterKey);
		}
		setterKey = new MethodKeyDynamic(setterName(name),
				new Class<?>[] { type }, void.class);
		ir = classIR.getMemberIRs().get(setterKey);
		if (ir != null) {
			carryAnnotations(ir);
			setter = (Method) ir.getCollectedMember();
		}
		classIR.getMemberIRs().put(setterKey, this);
	}

	/**
	 * Creates keys and registers this IR.
	 */
	protected void createDynamicKeysAndRegister() {
		if (fieldKey != null) {
			classIR.getMemberIRs().put(fieldKey, this);
		}

		getterKey = new MethodKeyDynamic(getterName(name), new Class<?>[0],
				type);
		MemberIR ir = classIR.getMemberIRs().get(getterKey);
		if (ir != null && ir != this) {
			carryAnnotations(ir);
			getter = (Method) ir.getCollectedMember();
		}
		classIR.getMemberIRs().put(getterKey, this);

		setterKey = new MethodKeyDynamic(setterName(name),
				new Class<?>[] { type }, void.class);
		ir = classIR.getMemberIRs().get(setterKey);
		if (ir != null && ir != this) {
			carryAnnotations(ir);
			setter = (Method) ir.getCollectedMember();
		}
		classIR.getMemberIRs().put(setterKey, this);
	}

	/**
	 * Collects annotations from a previous IR.
	 * 
	 * @param ir
	 *            previous IR
	 */
	protected void carryAnnotations(MemberIR ir) {
		collectTypes.addAll(ir.getCollectTypes());
		for (Entry<Class<?>, Annotation> e : ir.getCollectedAnnotations()
				.entrySet()) {
			if (!collectedAnnotations.containsKey(e.getKey())) {
				collectedAnnotations.put(e.getKey(), e.getValue());
			}
		}
		currentCollection.putAll(ir.currentCollection);
	}

	/**
	 * Initializes this IR from a field.
	 * 
	 * @param f
	 *            the field
	 */
	protected void initFromField(Field f) {
		if (fieldKey != null) {
			classIR.getMemberIRs().put(fieldKey, this);
		}

		field = f;
	}

	/**
	 * Initializes this IR from a getter.
	 * 
	 * @param m
	 *            the getter
	 */
	protected void initFromGetter(Method m) {
		createDynamicKeysAndRegister();
		getter = m;
	}

	/**
	 * Initializes this IR from a setter.
	 * 
	 * @param m
	 *            the setter
	 */
	protected void initFromSetter(Method m) {
		createDynamicKeysAndRegister();
		setter = m;
	}

	public StaticMemberData extractData(int id,
			EntityProvider<?> entityProvider, StaticEntityDataIR localIR,
			StaticEntityDataIR connectedIR) {
		DominantState ds = getCollectedAnnotation(DominantState.class);
		RelayState rs = getCollectedAnnotation(RelayState.class);
		UpdateState us = getCollectedAnnotation(UpdateState.class);
		TrackValue tv = getCollectedAnnotation(TrackValue.class);
		UpdateSetId usi = getCollectedAnnotation(UpdateSetId.class);

		String filters = "";
		for (ClassFilter cf : ds.value()) {
			filters = filters + "\n    filter: " + cf;
			for (Class<?> c : cf.value()) {
				filters = filters + "\n      filter class: " + c;
			}
		}

		LOGGER.info("Extracting state member data: " + "\n  IR class: "
				+ getClass() + "\n  ID: " + id + "\n  collected member: "
				+ getCollectedMember() + "\n  field key: " + fieldKey
				+ "\n  field: " + field + "\n  getter key: " + getterKey
				+ "\n  getter: " + getter + "\n  setter key: " + setterKey
				+ "\n  setter: " + setter + "\n  tracking: " + tv.value()
				+ "\n  reliable: " + us.reliable() + "\n  filters: " + filters);

		StaticMemberStateDataIR cir = null;
		if (fieldKey != null) {
			MemberIR ir = connectedIR.getMemberIRs().get(fieldKey);
			if (ir != null && StaticMemberStateDataIR.class.isInstance(ir)) {
				cir = (StaticMemberStateDataIR) ir;
			}
			if (field == null) {
				throw new IllegalStateException(fieldKey
						+ ": Field was bound, but not found.");
			}
		}
		if (getterKey != null) {
			MemberIR ir = connectedIR.getMemberIRs().get(getterKey);
			if (ir != null && StaticMemberStateDataIR.class.isInstance(ir)) {
				if (cir != null && ir != cir) {
					throw new IllegalStateException(
							"Ambiguous target IR;\ncir: " + cir + "\nforeign: "
									+ ir);
				}
				cir = (StaticMemberStateDataIR) ir;
			}
		}
		if (setterKey != null) {
			MemberIR ir = connectedIR.getMemberIRs().get(setterKey);
			if (ir != null && StaticMemberStateDataIR.class.isInstance(ir)) {
				if (cir != null && ir != cir) {
					throw new IllegalStateException(
							"Ambiguous target IR;\ncir: " + cir + "\nforeign: "
									+ ir);
				}
				cir = (StaticMemberStateDataIR) ir;
			}
		}
		if (cir == null) {
			Logger.getLogger(getClass().getName()).warning(
					this + " no connected IR found. Omitting...");
			return null;
		}

		ValueReadAccessor readAccessor = null;
		ValueWriteAccessor writeAccessor = null;

		boolean otherDominant = ClassFilter.Eval.contains(ds.value(),
				localIR.getConnectedClass());
		boolean otherRelay = ClassFilter.Eval.contains(rs.value(),
				localIR.getConnectedClass());

		if (ClassFilter.Eval.contains(ds.value(), localIR.getLocalClass())) {
			if (otherDominant) {
				throw new IllegalStateException(this + "; local class: "
						+ localIR.getLocalClass()
						+ ": Ambiguous dominant state.");
			}
			readAccessor = requireReadAccessor();
		} else if (ClassFilter.Eval.contains(rs.value(),
				localIR.getLocalClass())) {
			if (otherRelay) {
				throw new IllegalStateException(this
						+ ": Ambiguous relay state.\n  local class: "
						+ localIR.getLocalClass() + "\n  connected class: "
						+ localIR.getConnectedClass());
			}
			if (otherDominant) {
				writeAccessor = requireWriteAccessor();
			} else {
				readAccessor = requireReadAccessor();

			}
		} else if (otherRelay || otherDominant) {
			writeAccessor = requireWriteAccessor();
		} else {
			LOGGER.info("Local class: " + localIR.getLocalClass()
					+ "; connected class: " + localIR.getConnectedClass()
					+ "; field " + name + ": Ambiguous dominated state.");
			return null;
		}

		ValueInterpreter parameterInterpreter = entityProvider.getInterpreter(
				this, type);

		return new StaticUpdateMemberData(id, new StateUpdateKey(),
				us.reliable(), tv.value(), us.value(), usi.value(),
				readAccessor, writeAccessor, parameterInterpreter, name,
				localIR.getAppState().getModule(CoreModule.class));
	}

	/**
	 * Creates a <tt>ValueWriteAccessor</tt> depending on whether a real or
	 * virtual field is used. Throws an <tt>IllegalStateException</tt> if write
	 * access is not available locally.
	 * 
	 * @return <tt>ValueWriteAccessor</tt> that will be locally used to write
	 *         the value of the tracked synchronized field
	 */
	protected ValueWriteAccessor requireWriteAccessor() {
		if (setter != null) {
			return new SetterWriteAccessor(setter);
		} else if (field != null) {
			return new FieldWriteAccessor(field);
		} else {
			throw new IllegalStateException(this
					+ " requires write access but has none.");
		}
	}

	/**
	 * Creates a <tt>ValueReadAccessor</tt> depending on whether a real or
	 * virtual field is used. Throws an <tt>IllegalStateException</tt> if read
	 * access to the tracked synchronized field is not locally available.
	 * 
	 * @return <tt>ValueReadAccessor</tt> that will be locally used to read the
	 *         value of the tracked synchronized field
	 */
	protected ValueReadAccessor requireReadAccessor() {
		if (getter != null) {
			return new GetterReadAccessor(getter);
		} else if (field != null) {
			return new FieldReadAccessor(field);
		} else {
			throw new IllegalStateException(this
					+ " requires read access but has none.");
		}
	}

	@Override
	public String toString() {
		return "[" + getClass().getName() + ":\n  class ir: "
				+ getClassIR().toString() + "\n  collected member: "
				+ getCollectedMember() + "]";
	}

	@Override
	public AnnotationIR overrideToDefault() {
		clear();
		FieldIR fir = null;
		if (fieldKey != null && field != null) {
			classIR.getMemberIRs().remove(fieldKey);
			fir = new FieldIR(classIR, fieldKey, field);
			fir.update(field);
			classIR.getMemberIRs().put(fieldKey, fir);
		}

		MethodIR gir = null;
		if (getterKey != null && getter != null) {
			classIR.getMemberIRs().remove(getterKey);
			gir = new MethodIR(classIR, getterKey, getter);
			gir.update(getter);
			classIR.getMemberIRs().put(getterKey, gir);
		}

		MethodIR sir = null;
		if (setterKey != null && setter != null) {
			classIR.getMemberIRs().remove(setterKey);
			sir = new MethodIR(classIR, setterKey, setter);
			sir.update(setter);
			classIR.getMemberIRs().put(setterKey, sir);
		}

		if (Field.class.isInstance(lastMemberForReset)) {
			return fir;
		} else if (Method.class.isInstance(lastMemberForReset)) {
			if (isGetterName(((Method) lastMemberForReset).getName())) {
				if (gir == null) {
					throw new IllegalStateException();
				}
				return gir;
			} else if (isSetterName(((Method) lastMemberForReset).getName())) {
				if (sir == null) {
					throw new IllegalStateException();
				}
				return sir;
			}
		}
		throw new IllegalStateException("last member " + lastMemberForReset);
	}
}
