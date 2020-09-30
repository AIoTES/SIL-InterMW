package eu.interiot.intermw.comm.arm;

import eu.interiot.intermw.commons.ContextHelper;
import eu.interiot.intermw.commons.exceptions.ContextException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


/**
 * Main entry point to get the {@link ApiRequestManager} and {@link Configuration}
 * instances
 * <p>
 * It uses reflection to automatically search for classes annotated with
 * {@link eu.interiot.intermw.comm.arm.annotations.ApiRequestManager} annotations
 * <p>
 * The {@link ARMContext} class stores a map of {@link ApiRequestManager} per
 * {@link Configuration}. Currently only one configuration is supported.
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@eu.interiot.intermw.commons.annotations.Context
public class ARMContext {

    private final static Logger log = LoggerFactory.getLogger(ARMContext.class);
    private final static String PROPERTIES_FILENAME = "intermw.properties";

    private static ContextHelper contextHelper;

    private static Map<Integer, ApiRequestManager> apiRequestManagers = new HashMap<>();


    static {
        log.debug("Initializing ARM context...");
        try {
            contextHelper = new ContextHelper(PROPERTIES_FILENAME);
            if (getApiRequestManager() == null) {
                throw new MiddlewareException("Cannot create ARM instance.");
            }
            log.info("ARM context has been initialized successfully.");

        } catch (Exception e) {
            log.error("Failed to initialize ARM context: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the default {@link Configuration} instance from the {@link ContextHelper}
     *
     * @return A {@link Configuration} instance
     * @throws ContextException Thrown when no Class annotated with
     *                          {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                          is found
     */
    public static Configuration getConfiguration() throws MiddlewareException {
        return contextHelper.getConfiguration();
    }

    /**
     * Creates a new {@link ApiRequestManager} instance with the default
     * {@link Configuration} instance
     *
     * @return A {@link ApiRequestManager} instance
     * @throws ContextException Thrown when no Classes annotated with
     *                          {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                          or
     *                          {@link eu.interiot.intermw.comm.arm.annotations.ApiRequestManager}
     *                          are found
     * @see #getConfiguration()
     */
    public static ApiRequestManager getApiRequestManager() throws MiddlewareException {
        Configuration configuration = ARMContext.getConfiguration();
        return ARMContext.getApiRequestManager(configuration);
    }

    /**
     * Creates a new {@link ApiRequestManager} instance given a {@link Configuration}
     *
     * @return A {@link ApiRequestManager} instance
     * @throws ContextException Thrown when no Classes annotated with
     *                          {@link Configuration}
     *                          or
     *                          {@link eu.interiot.intermw.comm.arm.annotations.ApiRequestManager}
     *                          are found
     * @see #getApiRequestManager() ()
     * @see #getConfiguration()
     */
    public static ApiRequestManager getApiRequestManager(Configuration configuration) throws MiddlewareException {
        Integer configurationKey = configuration.hashCode();
        ApiRequestManager apiRequestManager = apiRequestManagers.get(configurationKey);
        if (apiRequestManager == null) {
            apiRequestManager = ARMContext.loadApiRequestManager(configuration);
            apiRequestManagers.put(configurationKey, apiRequestManager);
        }

        return apiRequestManager;
    }

    private static ApiRequestManager loadApiRequestManager(Configuration configuration) throws MiddlewareException {
        Class<?> _Class = contextHelper.getFirstClassAnnotatedWith(
                eu.interiot.intermw.comm.arm.annotations.ApiRequestManager.class);

        log.debug("Creating a new instance of " + _Class);
        ApiRequestManager apiRequestManager;
        try {
            apiRequestManager = (ApiRequestManager) _Class.getConstructor(Configuration.class).newInstance(configuration);

        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    _Class.getCanonicalName(), targetException.getMessage()), targetException);

        } catch (Exception e) {
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    _Class.getCanonicalName(), e.getMessage()), e);
        }

        return apiRequestManager;
    }


}
