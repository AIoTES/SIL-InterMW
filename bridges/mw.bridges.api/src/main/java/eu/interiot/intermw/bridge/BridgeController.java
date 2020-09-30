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
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge;

import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.bridge.model.Bridge;
import eu.interiot.intermw.comm.broker.annotations.Broker;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.requests.RegisterPlatformReq;

/**
 * This interface deals with connections to the IoT {@link RegisterPlatformReq ) subscriptions and
 * the {@link Broker} subscriptions to receive information from the platform
 *
 * Internally contains an instance of {@link Bridge} that is used to make the
 * calls to the methods of the IoT platform
 *
 * Instances of this interface must be annotated with the
 * {@link eu.interiot.intermw.bridge.annotations.BridgeController} annotation to
 * be automatically loaded by the {@link BridgeContext }
 *
 * @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
public interface BridgeController {

    /**
     * This method prepares {@link Broker} subscriptions to the list of
     * {@link eu.interiot.intermw.commons.model.enums.Actions} and makes the necessary calls to the {@link Bridge}
     *
     * @throws BridgeException
     * @throws BrokerException
     */
    public void setUpBrokerListeners() throws BridgeException, BrokerException;

    /**
     * Frees resources: Closes connections of the Bridge to the IoT platform,
     * removes subscribers, publishers, etc.
     *
     * @throws BridgeException
     * @throws BrokerException
     */
    public void destroy() throws BridgeException, BrokerException;

    /**
     * The {@link Bridge} attached to this instance of the
     * {@link BridgeController}
     *
     * @return A {@link Bridge} instance
     */
    public Bridge getBridge();
}
