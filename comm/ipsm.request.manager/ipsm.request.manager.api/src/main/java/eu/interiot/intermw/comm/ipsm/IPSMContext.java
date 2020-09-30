package eu.interiot.intermw.comm.ipsm;

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
 * Main entry point to get the {@link IPSMRequestManager} and {@link Configuration}
 * instances
 * <p>
 * It uses reflection to automatically search for classes annotated with
 * {@link eu.interiot.intermw.comm.ipsm.annotations.IPSMRequestManager} annotations
 * <p>
 * The {@link IPSMContext} class stores a map of {@link IPSMRequestManager} per
 * {@link Configuration}. Currently only one configuration is supported.
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@eu.interiot.intermw.commons.annotations.Context
public class IPSMContext {

    private final static Logger log = LoggerFactory.getLogger(IPSMContext.class);
    private final static String PROPERTIES_FILENAME = "intermw.properties";

    private static ContextHelper contextHelper;

    private static Map<Integer, IPSMRequestManager> ipsmRequestManagers = new HashMap<>();


    static {
        log.debug("Initializing IPSMRM context...");
        try {
            contextHelper = new ContextHelper(PROPERTIES_FILENAME);
            if (getIPSMRequestManager() == null) {
                throw new MiddlewareException("Cannot create IPSMRM instance.");
            }
            log.info("IPSMRM context has been initialized successfully.");

        } catch (Exception e) {
            log.error("Failed to initialize IPSMRM context: " + e.getMessage(), e);
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
     * Creates a new {@link IPSMRequestManager} instance with the default
     * {@link Configuration} instance
     *
     * @return A {@link IPSMRequestManager} instance
     * @throws ContextException Thrown when no Classes annotated with
     *                          {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                          or
     *                          {@link eu.interiot.intermw.comm.ipsm.annotations.IPSMRequestManager}
     *                          are found
     * @see #getConfiguration()
     */
    public static IPSMRequestManager getIPSMRequestManager() throws MiddlewareException {
        Configuration configuration = IPSMContext.getConfiguration();
        return IPSMContext.getIPSMRequestManager(configuration);
    }

    /**
     * Creates a new {@link IPSMRequestManager} instance given a {@link Configuration}
     *
     * @return A {@link IPSMRequestManager} instance
     * @throws ContextException Thrown when no Classes annotated with
     *                          {@link Configuration}
     *                          or
     *                          {@link eu.interiot.intermw.comm.ipsm.annotations.IPSMRequestManager}
     *                          are found
     * @see #getIPSMRequestManager() ()
     * @see #getConfiguration()
     */
    public static IPSMRequestManager getIPSMRequestManager(Configuration configuration) throws MiddlewareException {
        Integer configurationKey = configuration.hashCode();
        IPSMRequestManager ipsmRequestManager = ipsmRequestManagers.get(configurationKey);
        if (ipsmRequestManager == null) {
            ipsmRequestManager = IPSMContext.loadIPSMRequestManager(configuration);
            ipsmRequestManagers.put(configurationKey, ipsmRequestManager);
        }

        return ipsmRequestManager;
    }

    private static IPSMRequestManager loadIPSMRequestManager(Configuration configuration) throws MiddlewareException {
        Class<?> _Class = contextHelper.getFirstClassAnnotatedWith(
                eu.interiot.intermw.comm.ipsm.annotations.IPSMRequestManager.class);

        log.debug("Creating a new instance of " + _Class);
        IPSMRequestManager ipsmRequestManager;
        try {
            ipsmRequestManager = (IPSMRequestManager) _Class.getConstructor(Configuration.class).newInstance(configuration);

        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    _Class.getCanonicalName(), targetException.getMessage()), targetException);

        } catch (Exception e) {
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    _Class.getCanonicalName(), e.getMessage()), e);
        }

        return ipsmRequestManager;
    }

}
