/* Copyright 2013 Stephen Stacha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.innavace.ds.listener;

import com.innavace.ds.config.Configuration;
import com.innavace.ds.config.ConfigurationHandler;
import com.innavace.ds.config.Connection;
import com.innavace.ds.config.ConnectionHandler;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * User: sstacha
 * Date: Mar 5, 2013
 * Basic context listener to initialize and destroy connections and configurations properly
 */
public class ApplicationListener implements ServletContextListener {
    public static Logger log = Logger.getLogger(ApplicationListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // initialize connections and configurations since they are used in many places
        System.out.println(" ");
        System.out.println("DATA SERVICES APPLICATION STARTUP");
        System.out.println("---------------------------------");
        System.out.println(" ");
        log.info("INITIALIZING HANDLERS...");
        try {
            // initialize our connections
            log.debug("initializing connection handler...");
            ConnectionHandler.init();
            // initialize our user configurations & sysreg
            log.debug("initializing configuration handler...");
            ConfigurationHandler.init();
        }
        catch (Exception ex) {
            log.error("APPLICATION LISTENER EXCEPTION: ", ex);
        }
        log.debug("all handlers initialized...");

        // now that they are initialized lets print them out as a status
        try {
            log.info("caching initialized handler data...");
            log.info("CONNECTIONS");
            Collection<Connection> connections = ConnectionHandler.connectionsMap.values();
            for (Connection connection : connections)
                log.info("    " + connection);
            log.debug("loaded connections to memory...");
            log.info("CONFIGURATIONS");
            Collection<Configuration> configurations = ConfigurationHandler.getConfigurations();
            for (Configuration configuration : configurations)
                log.info("    " + configuration);
            log.debug("loaded configurations to memory...");
            log.info("SYSTEM REGISTRY");
            Set<Map.Entry<String, String>> entries = ConfigurationHandler.getRegistryEntries();
            for (Map.Entry<String, String> entry : entries)
                log.info("    " + entry.getKey() + "\t: " + entry.getValue());
            log.debug("loaded registry entries to memory...");
            log.info(" ");
            log.info("data services listener is up");
            log.info("-------------------------------------\n");
        }
        catch (Exception ex) {
            log.error("\nAPPLICATION LISTENER EXCEPTION: ", ex);
        }
        System.out.println("DATA SERVICES APPLICATION STARTUP COMPLETE");
        System.out.println("------------------------------------------");
        System.out.println(" ");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(" ");
        System.out.println("DATA SERVICES APPLICATION SHUTTING DOWN");
        System.out.println("---------------------------------------");
        System.out.println(" ");
        log.debug("destroying handlers");
        ConnectionHandler.destroy();
        log.info(" ");
        log.info("data services listener shut down");
        log.info("-------------------------------------\n");
        System.out.println("DATA SERVICES APPLICATION SHUTDOWN COMPLETE");
        System.out.println("-------------------------------------------");
        System.out.println(" ");
    }
}
