/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge.model;

import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.commons.ErrorReporter;
import eu.interiot.message.Message;

/**
 * A {@link Bridge} instance deals with communication with IoT platforms at the
 * level of middleware
 * <p>
 * One implementation per platform type should be provided
 * <p>
 * The platform type supported by a {@link Bridge} instance is specified
 * by means of {@link eu.interiot.intermw.bridge.annotations.Bridge#platformType()}
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a> * @author
 * <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 */
public interface Bridge {

    void setPublisher(Publisher<Message> publisher);

    void setErrorReporter(ErrorReporter errorReporter);

    void process(Message message) throws BridgeException;
}
