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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A default {@link Serializer} instance based on {@link Gson}
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class this {@link Serializer} understands
 */
/* TODO / for some reason it takes the non default serialser..... so I, Flavio, comment this out for the time being
@eu.interiot.intermw.comm.broker.annotations.Serializer

 */
public class DefaultSerializer<M> implements Serializer<M> {
    private final static Logger log = LoggerFactory.getLogger(JsonLDSerializer.class);

    private final Gson gson;

    /**
     * The constructor
     *
     * Creates a basic {@link Gson} instance
     *
     * FIXME Improve the {@link Gson} instance configuration
     */
    public DefaultSerializer() {
        log.debug("Created DefaultSerializer");
        gson = new GsonBuilder().create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(M message) {
        return gson.toJson(message);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M deserialize(String message, Class<M> type) {
        return gson.fromJson(message, type);
    }

}
