/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.commons.exceptions;

/**
 * The action is legal but not supported in this version of Middleware.
 * @author mllorente
 *
 */
public class UnsupportedActionException extends ActionException {

    private static final long serialVersionUID = -4207137088784523806L;

    public UnsupportedActionException(String cause) {
        super(ErrorCode.UNSUPPORTED_ACTION_EXCEPTION.getErrorCode(), cause);
    }

}
