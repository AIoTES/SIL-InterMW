/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union�s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 *
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 *
 *
 * For more information, contact:
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.commons.model.enums;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enum of possible capabilities 
 * @author mllorente
 *
 */
@XmlRootElement(name = "PlatformCapabilityType")
public enum PlatformCapabilityType {
    CREATE,
    READ,
    PUBLISH,
    SUBSCRIBE,
    TEMPORAL_FILTER,
    ATTRIBUTE_FILTER,
    GEOSPATIAL_FILTER
}
