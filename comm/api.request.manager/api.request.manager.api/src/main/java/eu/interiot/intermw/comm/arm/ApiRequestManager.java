package eu.interiot.intermw.comm.arm;

/**
 * API Request Manager Interface
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio fuart</a>
 */

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.ApiCallback;
import eu.interiot.intermw.commons.model.Client;
import eu.interiot.message.Message;

/**
 * API Request Manager
 */
public interface ApiRequestManager {

    /**
     * Main entry point for the Inter-IoT middlware.
     * <p>
     * Based on sequence diagrams: MW01, MW03, MW04, MW05, MW07
     *
     * @param message action to be perfomed
     * @return Returns a unique conversation (session) identifier that can be used by the client to
     * relate answers (callbacks) with the respective request
     * @throws ???
     */
    String process(Message message) throws MiddlewareException;

    //TODO maybe include numbers of sequence diagrams down under here

    /**
     * Register callback for the client
     *
     * @param clientId    object that represents the client application, with which to associate the callback
     * @param apiCallback ApiCallback function to send results.
     * @return Returns a unique conversation (session) identifier that can be used by the client to
     * relate answers (callbacks) with the respective request
     * @throws ???
     */
    void registerCallback(String clientId, ApiCallback<Message> apiCallback);

    void updateCallback(Client client) throws MiddlewareException;

    /**
     * Unregister callback for the client
     *
     * @param clientId object that represents the client application, from which to dissasociate the callback
     * @return Returns a unique conversation (session) identifier that can be used by the client to
     * relate answers (callbacks) with the respective request
     * @throws ???
     */
    void unregisterCallback(String clientId);

    /**
     * Retrieve a map containing QUERY message responses
     *
     * This map is necessary for simulating synchronized responses to clients
     * when retrieving results of QUERY requests.
     *
     * @param conversationId*/
    Message getQueryResponseMessage(String conversationId);

    /////////////////////////////////////////////////////////////////////////////

    /*
     *
     * MW01	MW2MW Subscription request to topic
     * MW01, http://tinyurl.com/mw01v01
     *
     * MW02	New info pushed to topic (with semantic mediation)
     * MW02, http://tinyurl.com/mw02v02
     *
     * MW03	Query
     * MW03, http://tinyurl.com/mw03v01
     *
     * MW04	Resource discovery
     * MW04, http://tinyurl.com/mw04v01
     *
     * MW05	Unsubscribe from topic
     * MW05, http://tinyurl.com/mw05v02
     *
     * MW06	Flow creation
     * MW06, http://tinyurl.com/mw06v01
     *
     * MW07	MW2MW sends information to device(s)
     * MW07, http://tinyurl.com/mw07v01
     *
     * MW08	New info pushed to topic (without semantic mediation)
     * MW08, http://tinyurl.com/mw08v01
     *
     */
}
