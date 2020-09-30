package eu.interiot.intermw.commons.interfaces;

import java.util.List;
import java.util.Properties;


/**
 * An interface to retrieve configuration elements for the Communication and Control { @link Middleware}
 * entities
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
public interface Configuration {
    public static final String SCAN_PACKAGES = "scan-packages";

    /**
     * A list of strings representing the packages where the implementations of
     * {APIs are
     *
     * @return
     */
    List<String> getScanPackages();

    /**
     * The whole {@link Properties} object so that any class uses what it needs
     *
     * @return
     */
    Properties getProperties();

    /**
     * Checks if given configuration property is set.
     *
     * @param key The property key
     * @return true if given configuration property is set
     */
    boolean contains(String key);

    /**
     * A configuration property given a <key>
     *
     * @param key The property key
     * @return The property value
     */
    String getProperty(String key);

    /**
     * From the whole {@link Properties} object it takes the ones that starts
     * with <prefix>
     *
     * @param prefix The prefix to filter the {@link Properties} object
     * @return A subset of the {@link Properties} object
     */
    Properties getPropertiesWithPrefix(String prefix);

    Properties getPropertiesWithPrefix(String prefix, boolean removePrefix);

    String getParliamentUrl();

    String getBrokerType();

    String getRabbitmqHostname();

    int getRabbitmqPort();

    String getRabbitmqUsername();

    String getRabbitmqPassword();

    Properties getBridgeCommonProperties();

    String getBridgeCallbackUrl();

    String getIPSMApiBaseUrl();

    int getClientReceivingCapacityDefault();

    int getQueryResponseTimeout();
}
