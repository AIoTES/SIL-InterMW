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

import eu.interiot.intermw.comm.commons.exceptions.CommException;

/**
 * Base Broker Exception class
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class BrokerException extends CommException {

    private static final long serialVersionUID = -1779923843342749755L;

    public BrokerException(Integer code, String cause, Throwable e) {
        super(code, cause, e);
    }

    public BrokerException(Integer code, String cause) {
        super(code, cause);
    }

    public BrokerException(Integer code, Throwable e) {
        super(code, e);
    }

    public BrokerException(String cause, Throwable e) {
        super(cause, e);
    }

    public BrokerException(String cause) {
        super(cause);
    }

    public BrokerException(Throwable exception) {
        super(exception);
    }
}
