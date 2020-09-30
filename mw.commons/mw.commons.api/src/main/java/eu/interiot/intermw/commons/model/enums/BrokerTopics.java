/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
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

/**
 * A {@link Enum} to have the topic names for the bridges communications
 *
 * Topic names for communication among different components. They reflect flows
 * defined in the architecture diagram. The first segment of the name is the
 * publishing component, the second is the listening component
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public enum BrokerTopics {

    /**
     * Global error topic
     */
    ERROR("error"),
    /**
     * PRM-RD Platform request manager to Services
     *
     */
    PRM_SRM("prm_srm"),
    /**
     * PRM-RD Platform request manager to Services
     *
     */
    SRM_PRM("srm_prm"),
    /**
     * PRM-ARM Platform request manager to API Request Manager
     *
     */
    PRM_ARM("prm_arm"),
    /**
     * IPSMRM-Bx IPSMRM to Bridge X. To this topic name the platform ID is appended
     * to create a unique topic.
     */
    IPSMRM_BRIDGE("ipsmrm_bridge"),
    /**
     * Bx-IPSMRM Bridge X to IPSMRM To this topic name the platform ID is appended
     * to create a unique topic.
     */
    BRIDGE_IPSMRM("bridge_ipsmrm"),
    /**
     * ARM-PRM API Request Manager to Platform request manager
     *
     */
    ARM_PRM("arm_prm"),

    /**
     * IPSMRM-PRM IPSM Request Manager to Platform request manager
     *
     */
    IPSMRM_PRM("ipsmrm_prm"),

    /**
     * PRM-IPSMRM Platform request manager to IPSM Request Manager
     *
     */
    PRM_IPSMRM("prm_ipsmrm"),

    /**
     * BridgeControllerEmulator topic Listen for messages from the outer
     * universe
     *
     */
    BRIDGE_EMULATOR("bridge_controller_emulator_topic"),

    /**
     * Topic for client callback messages (REST)
     */
    REST_API("rest_api"),

    /**
     * Examples topic Listen for messages from the outer universe
     *
     */
    DEFAULT("default");

    private final String topicName;

    public String getTopicName() {
        return topicName;
    }

    private BrokerTopics(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName(String platformId) {
        return topicName + "_" + platformId.replaceAll("[:/#]+", "_");
    }
}
