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
package eu.interiot.intermw.comm.broker.activemq.exceptions;

import eu.interiot.intermw.comm.broker.Service;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;

/**
 * A subclass of {@link BrokerException} to warn about the lack of
 * credentials needed to initialize a {@link Service} against ActiveMQ
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class MissingCredentialsException extends BrokerException {

    private static final long serialVersionUID = 8454174089716483698L;

    public MissingCredentialsException() {
        super(ErrorCodes.NO_CREDENTIALS.errorCode, ErrorCodes.NO_CREDENTIALS.errorDescription);
    }

}
