/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union<92>s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 *
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 *
 *
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.comm.broker.launcher;

import eu.interiot.intermw.comm.broker.exceptions.BrokerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Base class for launching standalone applications. It provides Properties
 * object after creation with the content of the configuration properties file.
 *
 * Subclasses have to provide the name of the application config file overriding
 * abstract methods.
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public abstract class AbstractLauncher {

    private String baseDir;
    private Properties appConfig;

    /**
     * Does the general set-up of the application
     *
     * @throws BrokerException
     *             Thrown when it has not been possible to launch the
     *             application. Usually due to a malformed or non-existent
     *             configuration file
     */
    public AbstractLauncher(String[] args) throws BrokerException {
        this.appConfig = getConfigProperties();
        this.registerShutdownHook();
    }

    /**
     * Every application has to set its configuration file name.
     *
     * @return
     */
    public abstract String getApplicationConfigFileName();

    /**
     * Base method to perform a cleanUp when exiting the application
     */
    public abstract void cleanUp();

    /**
     * Convenience method to get {@link File} from application configuration
     * properties object
     *
     * @param appConfigKey
     * @return
     */
    public File getConfigFile(String appConfigKey) {
        return new File(getApplicationBaseDir() + File.separator + appConfig.getProperty(appConfigKey));
    }

    /**
     * The application config file as a {@link Properties} object
     *
     * @return
     * @throws IOException
     */
    public Properties getConfigProperties() throws BrokerException {
        try {
            File config = new File(getApplicationBaseDir() + File.separator + getApplicationConfigFileName());
            FileInputStream fileConfigStream = new FileInputStream(config);

            Properties prop = new Properties();
            prop.load(fileConfigStream);

            fileConfigStream.close();

            return prop;
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * Returns a configuration property from the configuration file
     *
     * @param property
     *            The key of the property to retrieve
     * @return The value of the <code>property</code> param
     */
    public String getProperty(String property) {
        return this.appConfig.getProperty(property);
    }

    /**
     * The name of the log4j properties file of the application. Default value
     * is: log4j.properties
     *
     * @return
     */
    protected String getLog4JConfigFileName() {
        return "log4j.properties";
    }

    /**
     * The location of the log4j properties file
     *
     * @return
     * @throws BrokerException
     *             Thrown when the log4j properties file does not exist
     */
    protected String getLog4JPropertiesFileLocation() throws BrokerException {
        try {
            return getApplicationBaseDir() + File.separator + getLog4JConfigFileName();
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    private String getApplicationBaseDir() {
        String tempDir;
        if (baseDir == null) {
            tempDir = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources";

            baseDir = (new File(tempDir)).exists() ? tempDir : ".";
        }
        return baseDir;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanUp();
            }
        });
    }
}
