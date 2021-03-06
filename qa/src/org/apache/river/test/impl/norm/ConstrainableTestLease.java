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
package org.apache.river.test.impl.norm;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.rmi.RemoteException;
import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.security.proxytrust.ProxyTrust;
import net.jini.security.proxytrust.ProxyTrustIterator;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 * Lease class use by renwal service tests when they don't want to use
 * <code>LocalLease</code>.
 */
@AtomicSerial
public class ConstrainableTestLease extends TestLease implements RemoteMethodControl {

    /**
     * Owner of lease
     */
    final private LeaseBackEnd home;

    /**
     * Create a new lease.
     * @param home     <code>LeaeBackEnd</code> object that will be used to
     *                 communicate renew and cancel requests to the granter
     *                 of the lease.
     * @param id       An <code>int</code> that <code>home</code> can use
     *                 to identify the leased resource.
     * @param expiration The initial expiration of the lease.
     */
    public ConstrainableTestLease(int id, LeaseBackEnd home, long expiration) {
	super(id, home, expiration);
	this.home = home;
    }
    
    public ConstrainableTestLease(GetArg arg) throws IOException{
	super(check(arg));
	home = arg.get("home", null, LeaseBackEnd.class);
    }
    
    private static GetArg check(GetArg arg) throws IOException{
	LeaseBackEnd home = arg.get("home", null, LeaseBackEnd.class);
	if (!(home instanceof RemoteMethodControl)) throw new 
	    InvalidObjectException("LeaseBackEnd not an instance of RemoteMethodControl");
	if (!(home instanceof ProxyTrust)) throw new InvalidObjectException(
	    "LeaseBackEnd not an instance of ProxyTrust");
	return arg;
    }

    // purposefully inherit doc comment from supertype
    public String toString() {
	return "ConstrainableTestLease: " + super.toString();
    }

    protected class IteratorImpl implements ProxyTrustIterator {
	private boolean hasNextFlag = true;
	private ProxyTrust proxy;

	IteratorImpl(ProxyTrust proxy) {
	    this.proxy = proxy;
	}

	public boolean hasNext() {
	    return hasNextFlag;
	}

	public Object next() throws RemoteException {
	    hasNextFlag = false;
	    return proxy;
	}

	public void setException(RemoteException e) {
	}
    }

    protected ProxyTrustIterator getProxyTrustIterator() {
	return new IteratorImpl(home);
    }

    public RemoteMethodControl setConstraints(MethodConstraints constraints) {
	((RemoteMethodControl) home).setConstraints(constraints);
	return this;
    }

    public MethodConstraints getConstraints() {
	return ((RemoteMethodControl) home).getConstraints();
    }
}



       
