package eu.interiot.intermw.comm.control;

import eu.interiot.intermw.commons.exceptions.MiddlewareException;

/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - XLAB d.o.o.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */

public interface ControlComponent {
    /**
     * This interface deals with common funcionalities of Comm components (broker subscriptions)
     *
     */

    /**
     * Frees resources: removes subscribers, publishers, etc.
     *
     * @throws MiddlewareException
     */
    public void destroy() throws MiddlewareException;


}
