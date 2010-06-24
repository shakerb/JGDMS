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

package org.apache.river.imp.security.policy.util;

import java.lang.ref.WeakReference;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

/**
 * The PermissionGrantBuilder creates Dynamic PermissionGrant's based on
 * information provided by the user.
 * 
 * Single Thread use only.
 * @author Peter Firmstone.
 */
public class PermissionGrantBuilder {
    /**
     * Implied Context of Grant
     */ 
    public static final int CLASSLOADER = 0;
    public static final int CODESOURCE = 1;
    public static final int PROTECTIONDOMAIN = 2;
    public static final int CODESOURCE_CERTS = 3;
    // Store CodeSource
    private CodeSource cs;
    private Certificate[] certs;
    private WeakReference<ProtectionDomain> domain;    
    // Array of principals 
    private Principal[] principals;
    // Permissions collection
    private Permission[] permissions;   
    private int context;
    
    public PermissionGrantBuilder(){
        reset();
    }
    
    /**
     * Resets builder back to initial state, ready to recieve new information
     * for building a new PermissionGrant.
     */
    public void reset(){
        cs = null;
        certs = null;
        domain = null;
        principals = null;
        permissions = null;
        context = CODESOURCE;
    }
    
    /**
     * This method set's the Context of the applied Grant, that is if a 
     * Permission is to be granted directly to a PermissionDomain
     * @param context
     * @return
     */
    public PermissionGrantBuilder context(int context){
        if ( context < 0 ){
            throw new IllegalStateException("context must be >= 0");
        }
        if ( context > 3 ){
            throw new IllegalStateException("context must be <= 3");
        }
        this.context = context;
        return this;
    }
    
    public PermissionGrantBuilder codeSource(CodeSource cs){
        this.cs = cs;
        return this;
    }
    
    public PermissionGrantBuilder clazz(Class cl){        
        domain = new WeakReference<ProtectionDomain>(cl.getProtectionDomain());
        return this;
    }
    
    public PermissionGrantBuilder certificates(Certificate[] certs){
        this.certs = certs;
        return this;
    }
    
    public PermissionGrantBuilder principals(Principal[] pals){
        // don't worry about being protective here.
        this.principals = pals;
        return this;
    }
    
    public PermissionGrantBuilder permissions(Permission[] permissions){
        this.permissions = permissions;
        return this;
    }
    
    public PermissionGrant build(){
        switch (context){
            case CLASSLOADER:
                return new ClassLoaderGrant(domain, principals, permissions);
            case CODESOURCE:
                return new CodeSourceGrant(cs, principals, permissions);
            case CODESOURCE_CERTS:
                return new CertificateGrant(certs, principals, permissions);
            case PROTECTIONDOMAIN:
                return new ProtectionDomainGrant(domain, principals, permissions);
            default:
                return null;
        }
    }
}