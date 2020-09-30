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

import eu.interiot.intermw.comm.broker.abstracts.AbstractTopic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.exceptions.ErrorCodes;

/**
 * A default {@link Topic} implementation
 *
 * When using this {@link Topic} there is no need to subclass the {@link Topic}
 * interface
 *
 * This is the preferred way of working with the {@link Broker}
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class this {@link Topic} is tied to
 *
 * @see Broker#createPublisher(String, Class)
 * @see Broker#createSubscriber(String, Class)
 * @see ErrorCodes#DEFAULT_TOPIC_EXCEPTION
 */
@eu.interiot.intermw.comm.broker.annotations.Topic(isDefault = true)
public class DefaultTopic<M> extends AbstractTopic<M> {

    /**
     * The constructor
     *
     * @param exchangeName
     *            The name of the exchange
     * @param broker
     *            The {@link Broker} instance
     * @param messageClass
     *            The message class this {@link Topic} is tied to
     * @throws BrokerException
     */
    public DefaultTopic(String exchangeName, Broker broker, Class<M> messageClass) throws BrokerException {
        super(exchangeName, broker, messageClass);
    }

    // public DefaultTopic(String exchangeName, Broker broker,
    // Routing<M> routing, Class<M> messageClass)
    // throws BrokerException {
    // super(exchangeName, broker, routing, messageClass);
    // }
}
