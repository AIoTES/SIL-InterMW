package eu.interiot.intermw.api;

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class InterMWInitializer {
    private static final Logger logger = LoggerFactory.getLogger(InterMWInitializer.class);
    private static boolean isInitialized = false;

    private InterMWInitializer() {
    }

    public static void initialize() throws MiddlewareException {
        if (isInitialized) {
            return;
        }

        logger.debug("InterMW is initializing...");

        Reflections reflections = new Reflections("eu.interiot.intermw");
        Set<Class<?>> contextClasses = reflections
                .getTypesAnnotatedWith(eu.interiot.intermw.commons.annotations.Context.class);

        ClassLoader classLoader = InterMWInitializer.class.getClassLoader();

        for (Class<?> contextClass : contextClasses) {
            logger.info("Loading context " + contextClass.getName());
            try {
                Class loadedContextClass = classLoader.loadClass(contextClass.getName());
                if (loadedContextClass == null)
                    logger.error("Class not loaded");
                Class.forName(contextClass.getName());
            } catch (ClassNotFoundException e) {
                logger.error("Class not found", e);
            }
        }
        isInitialized = true;
        logger.debug("InterMW has been initialized successfully.");
    }
}
