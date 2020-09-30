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
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Classes annotated with the {@link Bridge} annotation are clients of an IoT
 * platform and are responsible of connecting with the platform and provide
 * results in the inter-iot model
 *
 * @see eu.interiot.intermw.bridge.model.Bridge
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Bridge {

    /**
     * A {@link eu.interiot.intermw.bridge.model.Bridge} instance has to be
     * annotated with a {@link #platformType()}. That means that that
     * {@link eu.interiot.intermw.bridge.model.Bridge} instance will be used to
     * deal with the platform type indicated in the annotation
     *
     * @return The platform type
     */
    String platformType();
}