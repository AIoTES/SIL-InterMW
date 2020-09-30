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
package eu.interiot.intermw.comm.broker;

import eu.interiot.intermw.comm.broker.exceptions.BrokerException;

import java.util.List;

/**
 * FIXME DON'T USE ROUTING AT THE MOMENT
 *
 * An interface for providing routing capabilities to messages published from a
 * {@link Topic}
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class that is going to be routed
 *
 */
public interface Routing<M> {

    /**
     * Creates the routing for publishing
     *
     * Usually it is a combination of the properties of the message that is
     * going to be published
     *
     * In some cases it has to receive a <customParams> Object that depends on
     * the message bus implementation
     *
     * @param topic
     *            The {@link Topic}
     * @param customParams
     *            An arbitrary {@link Object} that can be send depending on the
     *            message bus implementation
     * @throws BrokerException
     * @return
     */
    public List<String> getPublishingRoutings(Topic<M> topic, Object customParams) throws BrokerException;

    /**
     * Creates the routing for subscribing
     *
     * This is used to filter the messages that arrive to a {@link Subscriber}
     *
     * @param topic
     *            The {@link Topic}
     * @throws BrokerException
     * @return
     */
    public List<String> getSubscribingRoutings(Topic<M> topic) throws BrokerException;

}
