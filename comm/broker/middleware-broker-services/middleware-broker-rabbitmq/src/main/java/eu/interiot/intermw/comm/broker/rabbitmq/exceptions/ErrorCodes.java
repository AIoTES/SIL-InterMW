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
package eu.interiot.intermw.comm.broker.rabbitmq.exceptions;

/**
 * An {@link Enum} for error codes related to the RabbitMQ implementation of the
 * broker API
 *
 * Reserved {@link ErrorCodes} for RabbitMQ go from 1000 to 1999
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public enum ErrorCodes {

    NO_CREDENTIALS(1000,
            "Either host or username or password are missing when connecting to RabbitMQ. Please check your configuration properties");

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
