package eu.interiot.intermw.api.rest.resource;

/**
 * Created by flavio_fuart on 27-Jun-17.
 */

import eu.interiot.intermw.api.InterMwApiImplExperimental;
import eu.interiot.intermw.api.exception.BadRequestException;
import eu.interiot.intermw.api.exception.ConflictException;
import eu.interiot.intermw.api.exception.NotFoundException;
import eu.interiot.intermw.api.model.*;
import eu.interiot.intermw.api.rest.model.MwAsyncResponse;
import eu.interiot.intermw.comm.arm.ResponseMessageParser;
import eu.interiot.intermw.commons.Context;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.*;
import eu.interiot.message.Message;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@SwaggerDefinition(
        info = @Info(
                description = "INTER-IoT Middleware Layer Interoperability Components",
                version = "2.3.0",
                title = "MW2MW",
                termsOfService = "http://www.inter-iot-project.eu/",
                contact = @Contact(
                        name = "Interiot contact",
                        email = "coordinator@inter-iot.eu",
                        url = "http://www.inter-iot-project.eu/"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        basePath = "/api",
        consumes = {"application/json"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        externalDocs = @ExternalDocs(value = "Project deliverables", url = "http://www.inter-iot-project.eu/deliverables")
)
@Path("/mw2mw")
@Api(tags = {"mw2mw"})
public class InterMwApiREST {

    private static final Logger logger = LoggerFactory.getLogger(InterMwApiREST.class);
    private InterMwApiImplExperimental interMwApi;
    private static final String CLIENT_ID_HEADER = "Client-ID";

    public InterMwApiREST(@HeaderParam(CLIENT_ID_HEADER) String authorization) throws MiddlewareException {
        interMwApi = new InterMwApiImplExperimental(Context.getConfiguration());
    }

    @Inject
    @javax.ws.rs.core.Context
    private SecurityContext securityContext;

    @GET
    @Path("/clients")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all clients",
            tags = {"Clients"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Client.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response listClients() throws MiddlewareException {

        List<Client> clients = interMwApi.listClients();
        return Response.ok(clients).build();
    }

    @POST
    @Path("/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Register a new client",
            tags = {"Clients"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success.", response = Client.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 409, message = "Client is already registered.")})
    public Response registerClient(RegisterClientInput input,
                                   @javax.ws.rs.core.Context UriInfo uriInfo) throws MiddlewareException, ConflictException, BadRequestException {

        Client client = convertRegisterClientInput(input);

        interMwApi.registerClient(client);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path("{clientId}");
        URI location = uriBuilder.build(client.getClientId());
        return Response.created(location).entity(client).build();
    }

    @GET
    @Path("/clients/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve specified client",
            tags = {"Clients"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Client.class),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified client does not exist.")})
    public Response getClient(@PathParam("clientId") String clientId) throws MiddlewareException, NotFoundException {

        Client client = interMwApi.getClient(clientId);
        if (client == null) {
            throw new NotFoundException("Client " + clientId + " does not exist.");
        }
        return Response.ok(client).build();
    }

    @PUT
    @Path("/clients/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update specified client",
            tags = {"Clients"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Client.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified client does not exist.")})
    public Response updateClient(@PathParam("clientId") String clientId,
                                 UpdateClientInput input) throws MiddlewareException, BadRequestException, NotFoundException {

        interMwApi.updateClient(clientId, input);

        Client updatedClient = interMwApi.getClient(clientId);
        return Response.ok(updatedClient).build();
    }

    @DELETE
    @Path("/clients/{clientId}")
    @ApiOperation(value = "Remove specified client",
            tags = {"Clients"})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Success."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified client does not exist.")})
    public Response removeClient(@PathParam("clientId") String clientId) throws MiddlewareException, NotFoundException {

        interMwApi.removeClient(clientId);
        return Response.noContent().build();
    }

    @GET
    @Path("/platform-types")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all supported platform types",
            notes = "List all platform types for which corresponding bridge is available and loaded by INTER-MW.",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response listPlatformTypes() throws MiddlewareException {

        String conversationId = interMwApi.listPlatformTypes(getClientId());
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @GET
    @Path("/platforms")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all platforms registered with InterIoT",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Platform.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response listPlatforms() throws MiddlewareException {

        List<Platform> platforms = interMwApi.listPlatforms();
        return Response.ok(platforms).build();
    }

    @GET
    @Path("/platforms/{platformId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve specified platform",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Platform.class),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified platform does not exist.")})
    public Response getPlatform(@PathParam("platformId") String platformId) throws MiddlewareException, NotFoundException {

        Platform platform = interMwApi.getPlatform(platformId);
        if (platform == null) {
            throw new NotFoundException("Platform " + platformId + " does not exist.");
        }
        return Response.ok(platform).build();
    }

    @POST
    @Path("/platforms")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Register a new platform instance",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 409, message = "Platform is already registered by given client.")})
    public Response registerPlatform(RegisterPlatformInput input)
            throws MiddlewareException, ConflictException, BadRequestException {

        Platform platform = new Platform();
        platform.setClientId(getClientId());
        platform.setEncryptedPassword(input.getEncryptedPassword());
        platform.setEncryptionAlgorithm(input.getEncryptionAlgorithm());
        platform.setLocationId(input.getLocation());
        platform.setName(input.getName());
        platform.setPlatformId(input.getPlatformId());
        platform.setType(input.getType());
        platform.setUsername(input.getUsername());

        platform.setDownstreamInputAlignmentName(input.getDownstreamInputAlignmentName());
        platform.setDownstreamInputAlignmentVersion(input.getDownstreamInputAlignmentVersion());
        platform.setDownstreamOutputAlignmentName(input.getDownstreamOutputAlignmentName());
        platform.setDownstreamOutputAlignmentVersion(input.getDownstreamOutputAlignmentVersion());

        platform.setUpstreamInputAlignmentName(input.getUpstreamInputAlignmentName());
        platform.setUpstreamInputAlignmentVersion(input.getUpstreamInputAlignmentVersion());
        platform.setUpstreamOutputAlignmentName(input.getUpstreamOutputAlignmentName());
        platform.setUpstreamOutputAlignmentVersion(input.getUpstreamOutputAlignmentVersion());

        try {
            platform.setBaseEndpoint(new URL(input.getBaseEndpoint()));
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid baseEndpoint: " + input.getBaseEndpoint());
        }

        Location location = null;
        if (input.getLocationLat() != null && input.getLocationLong() != null) {
            // additional location data
            location = new Location(input.getLocation(), input.getLocationDescription(), input.getLocationLat(), input.getLocationLong());
        }

        String conversationId = interMwApi.registerPlatform(platform, location);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @PUT
    @Path("/platforms/{platformId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update specified platform instance",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified platform does not exist.")})
    public Response updatePlatform(@PathParam("platformId") String platformId, UpdatePlatformInput input)
            throws MiddlewareException, BadRequestException, NotFoundException {

        String conversationId = interMwApi.updatePlatform(getClientId(), platformId, input);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @DELETE
    @Path("/platforms/{platformId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove specified platform instance",
            notes = "Removes specified platform from the registry and undeploys the bridge.",
            tags = {"Platforms"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Platform does not exist.")})
    public Response removePlatform(@PathParam("platformId") String platformId)
            throws MiddlewareException, NotFoundException, BadRequestException {

        String conversationId = interMwApi.removePlatform(getClientId(), platformId);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all devices registered with InterIoT according to the specified filter",
            tags = {"Devices"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = IoTDevice.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response listDevices(@QueryParam("platformId") String platformId) throws MiddlewareException {

        List<IoTDevice> devices = interMwApi.listDevices(getClientId(), platformId);
        return Response.ok(devices).build();
    }

    @POST
    @Path("/devices")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Register (start managing) devices",
            tags = {"Devices"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 409, message = "One or more devices are already registered.")})
    public Response platformCreateDevices(PlatformCreateDeviceInput input)
            throws MiddlewareException, BadRequestException, ConflictException {

        String conversationId = interMwApi.platformCreateDevices(getClientId(), input.getDevices());
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @PUT
    @Path("/devices/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update specified device",
            tags = {"Devices"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 404, message = "Device does not exist.")})
    public Response platformUpdateDevice(@PathParam("deviceId") String deviceId,
                                         IoTDevice device) throws MiddlewareException, BadRequestException {

        if (!deviceId.equals(device.getDeviceId())) {
            throw new BadRequestException("DeviceID in path and body doesn't match.");
        }
        String conversationId = interMwApi.platformUpdateDevice(getClientId(), device);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @DELETE
    @Path("/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete specified device",
            tags = {"Devices"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 404, message = "Device does not exist.")})
    public Response platformDeleteDevice(@PathParam("deviceId") String deviceId) throws MiddlewareException, BadRequestException {
        List<String> devices = new ArrayList<>();
        devices.add(deviceId);
        String conversationId = interMwApi.platformDeleteDevices(getClientId(), devices);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @POST
    @Path("/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Subscribe to specified devices",
            tags = {"Subscriptions"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 409, message = "Client is already subscribed to specified devices.")})
    public Response subscribe(SubscribeInput input)
            throws MiddlewareException, BadRequestException {

        String conversationId = interMwApi.subscribe(getClientId(), input.getDeviceIds());
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @GET
    @Path("/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List subscriptions",
            tags = {"Subscriptions"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success.", response = Subscription.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response listSubscriptions(@QueryParam("clientId") String clientId) throws MiddlewareException {

        List<Subscription> subscriptions = interMwApi.listSubscriptions(clientId);
        return Response.ok(subscriptions).build();
    }

    @DELETE
    @Path("/subscriptions/{conversationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Unsubscribe from specified conversation",
            tags = {"Subscriptions"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 404, message = "Specified conversation doesn't exist.")})
    public Response unsubscribe(@PathParam("conversationId") String cnversationId)
            throws MiddlewareException, NotFoundException, BadRequestException {

        String unsubscribeConversationId = interMwApi.unsubscribe(getClientId(), cnversationId);
        MwAsyncResponse response = new MwAsyncResponse(unsubscribeConversationId);
        return Response.accepted(response).build();
    }

    @POST
    @Path("/devices/{deviceId}/actuation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Send actuation message",
            notes = "Send actuation message to actuators",
            tags = {"Actuation"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request.")
    })
    public Response actuation(@PathParam("deviceId") String deviceId, ActuationInput input) throws MiddlewareException, NotFoundException {

        String conversationId = interMwApi.actuate(getClientId(), deviceId, input);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    @GET
    @Path("/devices/data")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Query sensors for data",
            notes = "Query all intermw sensors for data",
            tags = "Sensors")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request.")
    })
    public Response queryAllPlatformDeviceSensorData(@QueryParam("platformId") String platformId) throws MiddlewareException, InterruptedException, ExecutionException, IOException, TimeoutException {
        Message message = interMwApi.getAllSensorData(getClientId(), platformId);
        return Response.ok(message).build();
    }

    @GET
    @Path("/devices/data/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Query one device sensor data",
            notes = "Query one intermw sensor for data",
            tags = "Device Sensor")
    public Response queryDeviceSensorData(@QueryParam("platformId") String platformId, @PathParam("deviceId") String deviceId) throws InterruptedException, MiddlewareException, TimeoutException, ExecutionException {
        Message message = interMwApi.getSensorDataForDevices(getClientId(), platformId, Arrays.asList(deviceId));
        return Response.ok(message).build();
    }

    @POST
    @Path("/responses")
    @Produces({"application/ld+json", "application/json"})
    @ApiOperation(value = "Retrieve response messages concerning the client",
            notes = "Retrieves response messages concerning the client waiting in the queue, if any. " +
                    "Maximum number of messages returned is specified at client registration. Returns array of messages in JSON-LD format " +
                    "or empty array if none is available.",
            tags = {"Messages"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success."),
            @ApiResponse(code = 400, message = "Invalid request."),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response retrieveResponseMessages()
            throws MiddlewareException, BadRequestException {

        String clientId = getClientId();
        Client client = interMwApi.getClient(clientId);
        List<Message> messages = interMwApi.retrieveResponseMessages(clientId);

        String mediaType;
        switch (client.getResponseFormat()) {
            case JSON_LD:
                mediaType = "application/ld+json";
                break;
            case JSON:
                mediaType = MediaType.APPLICATION_JSON;
                break;
            default:
                throw new BadRequestException("Unsupported response format: " + client.getResponseFormat());
        }

        String responseEntity = ResponseMessageParser.convertMessages(client.getResponseFormat(), messages);
        return Response.ok(responseEntity, mediaType).build();
    }

    @POST
    @Path("/requests")
    @Consumes("application/ld+json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Send given JSON-LD message downstream towards the platform",
            notes = "A raw method for sending message in JSON-LD format downstream.",
            tags = {"Messages"})
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The request has been accepted for processing.", response = MwAsyncResponse.class),
            @ApiResponse(code = 400, message = "Invalid request (invalid JSON-LD message)."),
            @ApiResponse(code = 401, message = "Unauthorized.")})
    public Response sendMessage(String content) throws MiddlewareException, BadRequestException {

        String conversationId = interMwApi.sendMessage(getClientId(), content);
        MwAsyncResponse response = new MwAsyncResponse(conversationId);
        return Response.accepted(response).build();
    }

    private Client convertRegisterClientInput(RegisterClientInput input) throws BadRequestException {
        Client client = new Client();
        client.setClientId(input.getClientId());
        client.setReceivingCapacity(input.getReceivingCapacity());
        client.setResponseDelivery(input.getResponseDelivery());
        client.setResponseFormat(input.getResponseFormat());

        if (input.getResponseDelivery() == Client.ResponseDelivery.SERVER_PUSH && input.getCallbackUrl() == null) {
            throw new BadRequestException("CallbackUrl attribute must be specified when using SERVER_PUSH response delivery.");
        }
        if (input.getCallbackUrl() != null) {
            try {
                client.setCallbackUrl(new URL(input.getCallbackUrl()));
            } catch (MalformedURLException e) {
                throw new BadRequestException("Invalid callbackURL: " + input.getCallbackUrl());
            }
        }

        return client;
    }

    private String getClientId() {
        return securityContext.getUserPrincipal().getName();
    }
}
