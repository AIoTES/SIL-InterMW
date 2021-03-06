/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 *
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 *
 *
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge.exceptions;

import eu.interiot.intermw.bridge.enums.ErrorCodes;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;

/**
 * {@link MiddlewareException} subclass for Bridge API exceptions
 *
 * @see ErrorCodes
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 *
 */
public class BridgeException extends MiddlewareException {

    private static final long serialVersionUID = -3406866625239308498L;

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(Throwable exception) {
        super(exception);
    }

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(String cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(Integer code, Throwable e) {
        super(code, e);
    }

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(String cause, Throwable e) {
        super(cause, e);
    }

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(Integer code, String cause) {
        super(code, cause);
    }

    /**
     * {@inheritDoc}
     *
     */
    public BridgeException(Integer code, String cause, Throwable e) {
        super(code, cause, e);
    }
}