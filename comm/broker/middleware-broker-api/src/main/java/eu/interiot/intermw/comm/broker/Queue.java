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

import java.util.Map;

/**
 * A template for adding configuration options when creating concrete queues for
 * different message brokers
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public interface Queue {

    /**
     * The name of the queue
     *
     * @return
     */
    public String getQueueName();

    /**
     * Specifies if a queue lives beyond its {@link Publisher} and
     * {@link Subscriber}
     *
     * See the concrete details of durability of the message broker you are
     * using
     *
     * @return
     */
    public boolean isDurable();

    /**
     * See the concrete details of durability of the message broker you are
     * using
     *
     * @return
     */
    public boolean isExclusive();

    /**
     * See the concrete details of durability of the message broker you are
     * using
     *
     * @return
     */
    public boolean autoDelete();

    /**
     * Open map of arguments (this is quite coupled with RabbitMQ)
     *
     * @return
     */
    public Map<String, Object> getArguments();
}
