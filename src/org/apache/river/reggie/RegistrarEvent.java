/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.reggie;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.export.ServiceProxyAccessor;
import net.jini.io.MarshalledInstance;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;
import org.apache.river.api.io.AtomicSerial.ReadInput;
import org.apache.river.api.io.AtomicSerial.ReadObject;

/**
 * Concrete implementation class for abstract ServiceEvent.
 *
 * @author Sun Microsystems, Inc.
 *
 */
@AtomicSerial
class RegistrarEvent extends ServiceEvent {

    private static final long serialVersionUID = 2L;

    /**
     * The new state of the serviceItem, or null if the serviceItem has been
     * deleted from the lookup service.  This is either a ServiceItem,
     * an Item (to be converted to a ServiceItem when getServiceItem is called),
     * or a bootstrapProxy;
     *
     * @serial
     */
    private volatile Object serviceItem;
    /**
     * The service ID of the serviceItem that triggered the event.  This field is used
     * instead of the inherited serviceID field (which is set to null) and is
     * written directly as a 128-bit value in order to avoid potential codebase
     * annotation loss (see bug 4745728).
     */
    private transient ServiceID servID;

    @ReadInput
    private static ReadObject getRO(){
	return new RO();
    }
    
    private static GetArg check(GetArg arg) throws IOException {
	Object serviceItem = arg.get("serviceItem", null);
	if (serviceItem == null ||
	    serviceItem instanceof ServiceItem ||
	    serviceItem instanceof Item ||
	    serviceItem instanceof ServiceProxyAccessor && 
		Proxy.isProxyClass(serviceItem.getClass()))
	{
	    RO r = (RO) arg.getReader();
	    if (r.servID instanceof ServiceID) return arg;
	}
	throw new InvalidObjectException("Invariants weren't satisfied");
    }
    
    public RegistrarEvent(GetArg arg) throws IOException {
	super(check(arg));
	serviceItem = arg.get("serviceItem", null);
	servID = ((RO) arg.getReader()).servID;
    }

    /**
     * Simple constructor.
     *
     * @param source the ServiceRegistrar that generated the event
     * @param eventID the registration eventID
     * @param seqNo the sequence number of this event
     * @param handback the client handback
     * @param serviceID the serviceID of the serviceItem that triggered the event
     * @param transition the transition that triggered the event
     * @param item the new state of the serviceItem, or null if deleted
     */
    public RegistrarEvent(Object source,
			  long eventID,
			  long seqNo,
			  MarshalledObject handback,
			  ServiceID serviceID,
			  int transition,
			  Object item)
    {
	super(source, eventID, seqNo, handback, null, transition);
	this.serviceItem = item;
	servID = serviceID;
    }

    /**
     * Returns the new state of the serviceItem, or null if the serviceItem was deleted
     * from the lookup service.
     */
    public ServiceItem getServiceItem() {
	if (serviceItem instanceof ServiceItem){
	    return ((ServiceItem) serviceItem).clone();
	} else if (serviceItem instanceof Item) {
	    serviceItem = ((Item)serviceItem).get();
	} else if (serviceItem instanceof ServiceProxyAccessor){
	    try {
		serviceItem = ((ServiceProxyAccessor) serviceItem).getServiceProxy();
	    } catch (RemoteException ex) {
		serviceItem = null;
	    };
	}
	return (ServiceItem)serviceItem;
    }
    
    public Object getBootstrapProxy(){
	if (serviceItem instanceof Item){
	    return ((Item)serviceItem).bootstrapProxy;
	} else if (serviceItem instanceof ServiceItem){
	    return null;
	} else {
	    return serviceItem;
	}
    }

    // javadoc inherited from ServiceEvent
    public ServiceID getServiceID() {
	return servID;
    }

    /**
     * Writes the default serializable field value for this instance, followed
     * by the serviceItem's service ID encoded as specified by the
     * ServiceID.writeBytes method.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
	servID.writeBytes(out);
    }

    /**
     * Reads the default serializable field value for this instance, followed
     * by the serviceItem's service ID encoded as specified by the
     * ServiceID.writeBytes method. 
     */
    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	servID = new ServiceID(in);
    }
    
    private static class RO implements ReadObject {

	ServiceID servID;
	@Override
	public void read(ObjectInput in) throws IOException, ClassNotFoundException {
	    servID = new ServiceID(in);
}
	
    }
}
