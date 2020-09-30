package eu.interiot.intermw.commons.interfaces;

/**
 * Created by flavio_fuart on 20-Dec-16.
 */


//TODO Do we want exposing the Message or we do somthing simpler?

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.Client;

import java.io.IOException;

/**
 * This is the callback for the MW API
 *
 * @param <M> The message class
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
public interface ApiCallback<M> {

    /**
     * Handle asynchronous responses from the middleware
     *
     * @param message The message received
     */
    void handle(M message) throws IOException, MiddlewareException;

    void update(Client client) throws Exception;

    void stop();
}