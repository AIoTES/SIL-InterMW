package eu.interiot.intermw.comm.prm;

/**
 * Platform Request Manager Interface
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio fuart</a>
 */
public interface PlatformRequestManager {

    /**
     * Push IPSM-translated message upstream towards the user
     *
     * Based on sequence diagrams: MW02, MW03, MW04, MW08
     * @param message
     * 		represents IIOTHS{M1,Og}, where M is data and O is a ontology

     * @throws ???
     */
    //void publish(Message message) throws MiddlewareException;


    /////////////////////////////////////////////////////////////////////////////

    /*
     *
     * MW01        MW2MW Subscription request to topic
     * MW01, http://tinyurl.com/mw01v01
     *
     */

    //protected? ok?

    /*
     *
     * MW02        New info pushed to topic (with semantic mediation)
     * MW02, http://tinyurl.com/mw02v02
     *
     */

    /*
     *
     * MW03        Query
     * MW03, http://tinyurl.com/mw03v01
     *
     */

    /*
     *
     * MW04        Resource discovery
     * MW04, http://tinyurl.com/mw04v01
     *
     */

    /*
     *
     * MW05        Unsubscribe from topic
     * MW05, http://tinyurl.com/mw05v02
     *
     */

    /*
     *
     * MW06        Flow creation
     * MW06, http://tinyurl.com/mw06v01
     *
     */

    //
    // protected createFlow(uniqueFlowID, F3.TO)

    /*
     *
     * MW07        MW2MW sends information to device(s)
     * MW07, http://tinyurl.com/mw07v01
     *
     */

    /*
     *
     * MW08        New info pushed to topic (without semantic mediation)
     * MW08, http://tinyurl.com/mw08v01
     *
     */
}
