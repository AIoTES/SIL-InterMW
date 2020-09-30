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
package eu.interiot.intermw.comm.broker.exceptions;

/**
 * An {@link Enum} for error codes used in {@link BrokerException} instances
 * and subclasses
 *
 * Reserved {@link ErrorCodes} for the broker API go from 1 to 999
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public enum ErrorCodes {

    NO_CONFIG(1, "No broker configuration registered"),
    NO_CONFIG_PROPERTY(2, "A mandatory configuration property is missing: "),
    EMPTY_TOPIC(3, "A topic has been created with an empty message. This is not allowed. Please review your usage of the Broker API"),
    CONTEXT_EXCEPTION(4, "There is an error while creating the context. Please review your client configuration. Error description: "),
    DEFAULT_TOPIC_EXCEPTION(5, "No default Topic instance found. Review your Topic annotations and check that the property isDefault is set to true to at least one Topic class "),
    CANNOT_CREATE_TOPIC(6, "Cannot create Topic");

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
