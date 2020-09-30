/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union�s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.api;

import eu.interiot.intermw.api.exception.BadRequestException;
import eu.interiot.intermw.api.exception.ConflictException;
import eu.interiot.intermw.api.exception.NotFoundException;
import eu.interiot.intermw.api.model.ActuationInput;
import eu.interiot.intermw.api.model.UpdateClientInput;
import eu.interiot.intermw.api.model.UpdatePlatformInput;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.ApiCallback;
import eu.interiot.intermw.commons.model.*;
import eu.interiot.message.Message;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * General API for the Intermw module. It provides functionalities to let IoT
 * Open Platforms interoperate.
 *
 * @author aromeu
 */
public interface InterMwApi {

    Client getClient(String clientId) throws MiddlewareException;

    List<Client> listClients() throws MiddlewareException;

    void registerClient(Client client) throws MiddlewareException, ConflictException, BadRequestException;

    void registerClient(Client client, ApiCallback<Message> apiCallback) throws MiddlewareException, ConflictException, BadRequestException;

    void updateClient(String clientId, UpdateClientInput input) throws MiddlewareException, NotFoundException, BadRequestException;

    void removeClient(String clientId) throws MiddlewareException, NotFoundException;

    Message retrieveResponseMessage(String clientId, long timeoutMillis) throws MiddlewareException;

    List<Message> retrieveResponseMessages(String clientId) throws MiddlewareException;

    String listPlatformTypes(String clientId) throws MiddlewareException;

    List<Platform> listPlatforms() throws MiddlewareException;

    String registerPlatform(Platform platform, Location location) throws MiddlewareException, ConflictException, BadRequestException;

    Platform getPlatform(String platformId) throws MiddlewareException;

    String updatePlatform(Platform platform) throws MiddlewareException, BadRequestException, NotFoundException;

    String updatePlatform(String clientId, String platformId, UpdatePlatformInput input) throws MiddlewareException, BadRequestException, NotFoundException;

    String removePlatform(String clientId, String platformId) throws MiddlewareException, NotFoundException, BadRequestException;

    String platformCreateDevices(String clientId, List<IoTDevice> devices) throws MiddlewareException, BadRequestException, ConflictException;

    String platformUpdateDevice(String clientId, IoTDevice device) throws MiddlewareException, BadRequestException;

    String platformUpdateDevices(String clientId, List<IoTDevice> devices) throws MiddlewareException, BadRequestException;

    String platformDeleteDevices(String clientId, List<String> deviceIds) throws MiddlewareException, NotFoundException, BadRequestException;

    List<IoTDevice> listDevices(String clientId, String platformId) throws MiddlewareException;

    String syncDevices(String clientId, String platformId) throws MiddlewareException;

    String subscribe(String clientId, List<String> deviceIds) throws MiddlewareException, BadRequestException;

    List<Subscription> listSubscriptions() throws MiddlewareException;

    List<Subscription> listSubscriptions(String clientId) throws MiddlewareException;

    String unsubscribe(String clientId, String subscriptionId) throws MiddlewareException, NotFoundException, BadRequestException;

    String actuate(String clientId, String actuatorId, ActuationInput input) throws MiddlewareException, NotFoundException;

    Message getAllSensorData(String clientId, String platformId) throws MiddlewareException, ExecutionException, InterruptedException, IOException, TimeoutException;

    Message getSensorDataForDevices(String clientId, String platformId, @Nullable List<String> deviceIds) throws MiddlewareException, InterruptedException, ExecutionException, TimeoutException;

    String sendMessage(String clientId, String content) throws BadRequestException, MiddlewareException;
}
