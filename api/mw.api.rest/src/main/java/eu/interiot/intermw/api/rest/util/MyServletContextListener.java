package eu.interiot.intermw.api.rest.util;

import eu.interiot.intermw.api.InterMWInitializer;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MyServletContextListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(MyServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            InterMWInitializer.initialize();
        } catch (Exception e) {
            logger.error("Failed to initialize InterMW: " + e.getMessage(), e);
            // how to stop/unload webapp?
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
