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

/**
 * A {@link Service} for subscribing to messages published by a
 * {@link Publisher}
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @param <M>
 *            The messages class this {@link Service} works with
 */
public interface Subscriber<M> extends Service<M> {

    /**
     * Subscribes to the messages of a {@link Topic}
     *
     * @param listener
     *            A {@link Listener} instance to handle the messages as they
     *            arrive
     * @throws BrokerException
     */
    public void subscribe(Listener<M> listener) throws BrokerException;

    /**
     * Subscribes to a the messages of a {@link Topic} creating a specific
     * message queue
     *
     * This API method depends on the support of the specific message bus
     * implementation. For some implementations, the <queue> parameter could
     * have no effect
     *
     * @param listener
     *            A {@link Listener} instance to handle the messages as they
     *            arrive
     * @param queue
     *            The {@link Queue} configuration. This parameter depends on the
     *            support of the specific message bus implementation
     * @throws BrokerException
     */
    public void subscribe(Listener<M> listener, Queue queue) throws BrokerException;
}
