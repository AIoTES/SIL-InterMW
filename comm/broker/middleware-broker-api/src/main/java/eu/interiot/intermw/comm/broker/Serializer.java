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
 * An interface for serializing and deserializing messages
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class of this {@link Serializer}
 */
public interface Serializer<M> {

    public String serialize(M message) throws BrokerException;

    public M deserialize(String message, Class<M> type) throws BrokerException;

}
