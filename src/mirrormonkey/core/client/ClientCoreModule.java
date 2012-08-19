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

package mirrormonkey.core.client;

import java.util.ArrayList;
import java.util.Collection;

import mirrormonkey.core.messages.EntityChangeMessage;
import mirrormonkey.core.messages.EntityEndMessage;
import mirrormonkey.core.messages.EntityInitMessage;
import mirrormonkey.core.module.CoreModule;
import mirrormonkey.framework.ClientSyncAppState;
import mirrormonkey.framework.connection.ConnectionInfo;

import com.jme3.network.MessageConnection;

/**
 * Client-Side implementation of <tt>CoreModule</tt>. In contrast to
 * <tt>ServerCoreModule</tt>, this is rather passive and listens to incoming
 * entity lifecycle messages to create, change and destroy entity instances.
 * 
 * @author Philipp Christian Loewner
 * 
 */
public class ClientCoreModule extends
		CoreModule<ClientEntityData, ClientSyncAppState> {

	/**
	 * Responsible for listening to <tt>EntityInitMessages</tt> and creating
	 * local instances.
	 */
	private final EntityInitListener initListener;

	/**
	 * Responsible for listening to <tt>EntityEndMessages</tt> and destroying
	 * local instances.
	 */
	private final EntityEndListener endListener;

	/**
	 * Responsible for listening to <tt>EntityChangeMessages</tt> and changing
	 * local instances.
	 */
	private final EntityChangeListener changeListener;

	/**
	 * Containing data about the underlying client, which is connected to a
	 * server using MirrorMonkey.
	 */
	private final ClientConnectionInfo connectionInfo;

	/**
	 * As there is only one connection on the client side, this will always
	 * contain exactly <tt>connectionInfo</tt>.
	 * 
	 * This is stored as singleton because <tt>ClientEntityData</tt> must be
	 * able to return a list of all active connections.
	 */
	private final Collection<ConnectionInfo<?>> connectionInfoAsCollection;

	/**
	 * Creates a new <tt>ClientCoreModule</tt> for a given <tt>SyncAppState</tt>
	 * . This constructor is called by <tt>SyncAppState.getModule</tt> and
	 * should not be called from userspace directly.
	 * 
	 * @param appState
	 *            the <tt>SyncAppState</tt> creating this
	 *            <tt>ClientCoreModule</tt>
	 */
	public ClientCoreModule(ClientSyncAppState appState) {
		super(appState, true);

		initListener = new EntityInitListener(this);
		appState.getEventManager().addMessageListener(initListener,
				EntityInitMessage.class);

		endListener = new EntityEndListener(this);
		appState.getEventManager().addMessageListener(endListener,
				EntityEndMessage.class);

		changeListener = new EntityChangeListener(this);
		appState.getEventManager().addMessageListener(changeListener,
				EntityChangeMessage.class);

		connectionInfo = new ClientConnectionInfo(appState,
				appState.getClient());

		connectionInfoAsCollection = new ArrayList<ConnectionInfo<?>>(1);
		connectionInfoAsCollection.add(connectionInfo);
	}

	@Override
	public void update(float tpf) {
		connectionInfo.update();
		super.update(tpf);
	}

	@Override
	public ClientConnectionInfo getData(MessageConnection forConnection) {
		return connectionInfo.getConnection().equals(forConnection) ? connectionInfo
				: null;
	}

	/**
	 * @return the collection singleton containing <tt>connectionInfo</tt> as
	 *         only element
	 */
	public Collection<ConnectionInfo<?>> getConnectionInfoAsCollection() {
		return connectionInfoAsCollection;
	}

	/**
	 * Returns an instance of <tt>ClientEntityData</tt> that contains mutable
	 * data about an entity with a given ID.
	 * 
	 * If there is no instance of <tt>ClientEntityData</tt> present for that
	 * particular entity, then a dummy instance will be created and returned.
	 * 
	 * If an <tt>InstanceLifecycleListener</tt> is added to a dummy instance,
	 * then the dummy instance will be registered in this <tt>CoreModule</tt>
	 * and the dummy instance will become a real instance. From then on, that
	 * instance will be returned by <tt>getData</tt> for that ID.
	 * 
	 * If the last <tt>InstanceLifecycleListener</tt> is removed from an
	 * instance and there is no local entity present, the instance will be
	 * turned into a dummy instance and will no longer be returned by this
	 * method.
	 * 
	 * If an entity becomes visible to this client, then this method will be
	 * called to acquire the real or a dummy instance. In case of a dummy
	 * instance, the newly created dummy instance will be registered to this
	 * <tt>ClientCoreModule</tt> and will be returned by this method as long as
	 * there is at least one <tt>InstanceLifecycleListener</tt> directly
	 * registered to it or the entity is visible to this client.
	 * 
	 * If an entity becomes invisible to this client and there are no
	 * <tt>InstanceLifecycleListeners</tt> listening to that entity
	 * specifically, then the instance of <tt>ClientEntityData</tt> representing
	 * that entity will be turned into a dummy instance and no longer be
	 * returned by this method.
	 * 
	 * Calls that would register or unregister a dummy instance when there is a
	 * real instance present will result in an <tt>IllegalStateException</tt>.
	 * 
	 * @param id
	 *            ID of the entity
	 * @return real or dummy instance representing the entity with the given ID
	 */
	public ClientEntityData getData(int id) {
		ClientEntityData data = getEntityProvider().getData(id);
		if (data != null) {
			return data;
		}
		return new ClientEntityData(id, null, this);
	}

}
