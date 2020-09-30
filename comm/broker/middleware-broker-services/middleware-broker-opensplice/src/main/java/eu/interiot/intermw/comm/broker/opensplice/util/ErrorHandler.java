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
package eu.interiot.intermw.comm.broker.opensplice.util;

import DDS.RETCODE_NO_DATA;
import DDS.RETCODE_OK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    public static final int NR_ERROR_CODES = 13;
    private static Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    /* Array to hold the names for all ReturnCodes. */
    public static String[] RetCodeName = new String[NR_ERROR_CODES];

    static {
        RetCodeName[0] = new String("DDS_RETCODE_OK");
        RetCodeName[1] = new String("DDS_RETCODE_ERROR");
        RetCodeName[2] = new String("DDS_RETCODE_UNSUPPORTED");
        RetCodeName[3] = new String("DDS_BUS_NO_AVAILABLE OR DDS_RETCODE_BAD_PARAMETER");
        RetCodeName[4] = new String("DDS_RETCODE_PRECONDITION_NOT_MET");
        RetCodeName[5] = new String("DDS_RETCODE_OUT_OF_RESOURCES");
        RetCodeName[6] = new String("DDS_RETCODE_NOT_ENABLED");
        RetCodeName[7] = new String("DDS_RETCODE_IMMUTABLE_POLICY");
        RetCodeName[8] = new String("DDS_RETCODE_INCONSISTENT_POLICY");
        RetCodeName[9] = new String("DDS_RETCODE_ALREADY_DELETED");
        RetCodeName[10] = new String("DDS_RETCODE_TIMEOUT");
        RetCodeName[11] = new String("DDS_RETCODE_NO_DATA");
        RetCodeName[12] = new String("DDS_RETCODE_ILLEGAL_OPERATION");
    }

    /**
     * Returns the name of an error code.
     **/
    public static String getErrorName(int status) {
        return RetCodeName[status];
    }

    /**
     * Check the return status for errors. If there is an error, then terminate.
     **/
    public static void checkStatus(int status, String info) {

        if (status != RETCODE_OK.value && status != RETCODE_NO_DATA.value) {
            String cause = "[OpenSplice] Error in " + info + ": " + getErrorName(status);
            log.info(cause);
            throw new RuntimeException(cause);
        }
    }

    /**
     * Check whether a valid handle has been returned. If not, then terminate.
     **/
    public static void checkHandle(Object handle, String info) {
        if (handle == null) {
            String cause = "[OpenSplice] Error in " + info + ": Creation failed: invalid handle";
            log.info(cause);
            throw new RuntimeException(cause);
        }
    }

}
