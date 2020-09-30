package eu.interiot.intermw.commons;

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@eu.interiot.intermw.commons.annotations.Context
public class Context {

    private final static Logger log = LoggerFactory.getLogger(Context.class);
    private final static String PROPERTIES_FILENAME = "intermw.properties";

    private static ContextHelper contextHelper;

    static {
        contextHelper = new ContextHelper(PROPERTIES_FILENAME);
    }

    /**
     * Returns the default {@link Configuration} instance from the
     * {@link ContextHelper}
     *
     * @return A {@link Configuration} instance
     * @throws MiddlewareException Thrown when no Class annotated with
     *                             {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                             is found
     */
    public static Configuration getConfiguration() throws MiddlewareException {
        return contextHelper.getConfiguration();
    }
}
