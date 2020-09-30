package eu.interiot.intermw.commons.model.enums;

/**
 * Class of all possible actions, performable within middleware
 *
 * @author matevzmarkovic
 */
public enum Actions {
    /**
     * Subscribe to a specific filter.
     * Subscription (request) to device observations (with filter).
     */
    SUBSCRIBE,
    /**
     * Query for values.
     * Query (request) to device observations (with filter). Query differs from subscription because is a
     * ‘one-off’, while subscription is continuous
     */
    QUERY,
    /**
     * Unsubscribe from a topic.
     */
    UNSUBSCRIBE,
    /**
     * Update the device - actuation and other updates.
     */

    THING_UPDATE,
    /**
     * Register a platform.
     * This action tells to the middleware to create an instance of the platform bridge, create channels (broker topics),
     * and register the platform in the Mw services (platform registry).
     */
    REGISTER_PLATFORM,
    /**
     * Unregisters a platform.
     * This action tells to the middleware un-register a platform (remove it from the registry, destroy topics and
     * undeploy the bridge.
     */
    UNREGISTER_PLATFORM,
    /**
     * Sent to the error topic in case of error in the middleware
     */
    ERROR,
    /**
     * The catch-all action, that does absolutely nothing; handled by the DefaultHandler.
     */
    UNKNOWN

}