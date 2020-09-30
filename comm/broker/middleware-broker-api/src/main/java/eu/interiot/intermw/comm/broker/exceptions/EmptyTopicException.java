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

import eu.interiot.intermw.comm.broker.Topic;

/**
 * Thrown when a {@link Topic} instance does not have a message instance
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class EmptyTopicException extends BrokerException {

    private static final long serialVersionUID = 5688152010252685149L;

    /**
     * The constructor
     *
     * @see ErrorCodes#EMPTY_TOPIC
     */
    public EmptyTopicException() {
        super(ErrorCodes.EMPTY_TOPIC.errorCode, ErrorCodes.EMPTY_TOPIC.errorDescription);
    }
}
