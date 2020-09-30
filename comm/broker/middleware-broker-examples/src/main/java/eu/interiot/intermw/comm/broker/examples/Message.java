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
package eu.interiot.intermw.comm.broker.examples;

import org.apache.commons.lang3.StringUtils;

/**
 * This is an example of a class of the domain of the client that wants to make
 * use of the Middlware broker
 *
 * It is just a regular class, nothing special here
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class Message {

    private String text = StringUtils.EMPTY;

    public Message(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
