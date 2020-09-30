/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union<92>s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.comm.broker;

import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Serializer} instance based on the JsonLD message implementation
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 *
 * @param <M>
 *            The message class this {@link Serializer} understands
 */
@eu.interiot.intermw.comm.broker.annotations.Serializer
public class JsonLDSerializer<M> implements Serializer<M> {
    private final static Logger log = LoggerFactory.getLogger(JsonLDSerializer.class);

    /**
     * The constructor
     *
     *
     * FIXME Improve the instance configuration
     */
    public JsonLDSerializer() {
    }

    public boolean isDefault() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String serialize(M message) throws BrokerException {
        try {
            return ((Message) message).serializeToJSONLD();
        } catch (Exception e) {
            throw new BrokerException("Failed to serialize message to JSON-LD: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized M deserialize(String message, Class<M> type) throws BrokerException {
        try {
            return (M) new Message(message);
        } catch (Exception e) {
            throw new BrokerException("Failed to deserialize message from JSON-LD: " + e.getMessage(), e);
        }
    }
}
