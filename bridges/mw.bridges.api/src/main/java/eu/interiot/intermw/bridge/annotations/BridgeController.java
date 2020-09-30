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
 * Classes annotated with the {@link BridgeController} annotation manage data
 * flow with {@link Bridge} instances
 *
 * A default implementation of
 * {@link eu.interiot.intermw.bridge.BridgeController} must exist in the
 * classpath but they can be customised for a specific platform type by
 * adding additional instances and annotating them with this annotation
 *
 * @see eu.interiot.intermw.bridge.BridgeController
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeController {

    /**
     * one or more platform types where the
     * {@link eu.interiot.intermw.bridge.BridgeController} will be used
     *
     * default is all platform types
     *
     * @return The list of {platform types that the
     *         {@link eu.interiot.intermw.bridge.BridgeController} instance is
     *         able to deal with If no platforms are indicated then the
     *         {@link eu.interiot.intermw.bridge.BridgeController} is considered
     *         to be able to deal with all the platform type available
     *
     */
    String[] platforms() default {};
}