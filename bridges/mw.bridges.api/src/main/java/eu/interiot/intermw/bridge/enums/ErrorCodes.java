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
package eu.interiot.intermw.bridge.enums;

/**
 * An {@link Enum} for error codes related to the bridge API
 *
 * Reserved {@link ErrorCodes} for ActiveMQ go from 20000 to 20999
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public enum ErrorCodes {

    NO_BRIDGE_FACTORY(20000,
            "MwFactory implementation not found. Please make sure a class annotated with the MwFactory annotation exists in the classpath");

    /**
     * The unique ID for the error code
     */
    public Integer errorCode;

    /**
     * A description for the error code
     */
    public String errorDescription;

    private ErrorCodes(Integer errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
}
