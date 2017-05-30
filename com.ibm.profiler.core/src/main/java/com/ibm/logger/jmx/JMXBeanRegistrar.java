/*
 * Copyright 2017 Steve McDuff
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.logger.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.ibm.commerce.cache.LoggingHelper;

public final class JMXBeanRegistrar<JMXBean> {

    /**
     * Interface for creating a bean if it is not already registered.
     * 
     * @param <JMXBean> The type of the bean being registered.
     */
    public interface JMXBeanProvider<JMXBean> {

        /**
         * Provide the bean to register.
         * 
         * @return A non-null bean to register.
         */
        public JMXBean provide();

    }

    private final Map<ObjectName, JMXBean> map = new ConcurrentHashMap<ObjectName, JMXBean>( 128 );

    private final MBeanServer server;

    private static final Logger LOGGER = Logger.getLogger( JMXBeanRegistrar.class.getName() );

    public JMXBeanRegistrar() {
        this( ManagementFactory.getPlatformMBeanServer() );
    }

    /**
     * Constructor.
     * 
     * @param server A non-null {@link MBeanServer} to register beans to.
     * @throws IllegalArgumentException Thrown if any parameter is null.
     */
    public JMXBeanRegistrar( MBeanServer server ) {
        if ( server == null ) {
            throw new IllegalArgumentException( "The server cannot be null" );
        }
        this.server = server;
    }

    /**
     * Destroy the registrar by unregistering all registered beans.
     * 
     * @throws MBeanRegistrationException Thrown if the destruction fails.
     */
    public void destroy() throws MBeanRegistrationException {
        synchronized ( map ) {
            for ( ObjectName name : map.keySet() ) {
                try {
                    server.unregisterMBean( name );
                } catch ( InstanceNotFoundException e ) {
                    LoggingHelper.logUnexpectedException( LOGGER, JMXBeanRegistrar.class.getName(), "destroy", e );
                }
            }
            map.clear();
        }

    }

    /**
     * Retrieve the bean if one has already been registered under this name. If not, register the bean provided by the
     * bean provider.
     * 
     * @param name A non-null {@link ObjectName} to associate this bean with
     * @param jmxBeanProvider A JMXBean provider that provides the bean for registration.
     * @return A non-null JMXBean representing the bean that was registered during this operation or previously under
     *         the same name.
     * @throws MBeanRegistrationException Thrown if the registration fails.
     */
    public JMXBean retrieveOrRegister( ObjectName name, JMXBeanProvider<JMXBean> jmxBeanProvider ) throws MBeanRegistrationException {
        if ( name == null ) {
            throw new IllegalArgumentException( "The name cannot be null" );
        }
        if ( jmxBeanProvider == null ) {
            throw new IllegalArgumentException( "The jmxBeanProvider cannot be null" );
        }

        JMXBean result = null;
        synchronized ( map ) {
            result = map.get( name );
            if ( result == null ) {
                try {
                    result = jmxBeanProvider.provide();
                    server.registerMBean( result, name );
                    map.put( name, result );
                } catch ( InstanceAlreadyExistsException e ) {
                    // Generally, this should not be possible
                    throw new IllegalStateException( e );
                } catch ( NotCompliantMBeanException e ) {
                    // Indicates a programming error
                    throw new IllegalStateException( e );
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "JMXBeanRegistrar [map=" + map + ", server=" + server + "]";
    }
    
    /**
     * Get all the registered beans and their object name.
     * @return The registered beans.
     */
    public Map<ObjectName,JMXBean> getAllRegisteredBeans() {
    	HashMap<ObjectName, JMXBean> result = null;
    	synchronized(map) {
    		result = new HashMap<ObjectName, JMXBean>(map);
    	}
		return result;
    }

    /**
     * Unregister a bean by name.
     * @param key The bean name.
     */
	public void unregisterBean(ObjectName key) {
		synchronized(map) { 
			JMXBean remove = map.remove(key);
			if( remove != null ) {
                try {
                    server.unregisterMBean( key );
                } catch ( Exception e ) {
                    LoggingHelper.logUnexpectedException( LOGGER, JMXBeanRegistrar.class.getName(), "destroy", e );
                }
			}
		}
		
	}

}
