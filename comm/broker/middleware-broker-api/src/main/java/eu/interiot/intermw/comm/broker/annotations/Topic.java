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
package eu.interiot.intermw.comm.broker.annotations;

import eu.interiot.intermw.comm.broker.Service;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An {@link Annotation} for {@link eu.interiot.intermw.comm.broker.Topic}
 * instances
 *
 * This is used for creating instances of
 * {@link eu.interiot.intermw.comm.broker.Topic} by reflection
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

    /**
     * When using a Default {@link Topic} implementation, set this to
     * <code>true</code>
     *
     * Only a <default> {@link es.prodevelop.middleare.broker.Topic} should be
     * annotated in the classpath.
     *
     * @see Service#init(eu.interiot.intermw.comm.broker.Broker, String,
     *      Class)
     *
     *
     * @return
     */
    boolean isDefault() default false;

}
