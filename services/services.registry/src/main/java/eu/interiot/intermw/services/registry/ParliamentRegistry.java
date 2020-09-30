package eu.interiot.intermw.services.registry;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.*;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ParliamentRegistry {
    private final static Logger logger = LoggerFactory.getLogger(ParliamentRegistry.class);
    private static final String SUBSCRIPTION_PREFIX = "subscriptions:";
    private static final String CLIENT_PREFIX = "clients:";

    private Configuration conf;

    public ParliamentRegistry(Configuration conf) {
        this.conf = conf;
    }

    private RDFConnection connect() {
        return RDFConnectionFactory.connect(conf.getParliamentUrl(), "sparql", "sparql", "sparql");
    }

    public void registerClient(Client client) throws MiddlewareException {
        logger.debug("Registering client {}...", client.getClientId());
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-create.rq");
        String sparql;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?clientId", CLIENT_PREFIX + client.getClientId());
        pss.setLiteral("?callbackUrl", client.getCallbackUrl() != null ? client.getCallbackUrl().toString() : "");
        pss.setLiteral("?receivingCapacity", client.getReceivingCapacity() != null ? client.getReceivingCapacity() : 0);
        pss.setLiteral("?responseDelivery", client.getResponseDelivery() != null ? client.getResponseDelivery().name() : "");
        pss.setLiteral("?responseFormat", client.getResponseFormat().name());

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
        }
    }

    public void updateClient(Client client) throws MiddlewareException {
        logger.debug("Updating client {}...", client.getClientId());
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-update.rq");
        String sparql;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?clientId", CLIENT_PREFIX + client.getClientId());
        pss.setLiteral("?callbackUrl", client.getCallbackUrl() != null ? client.getCallbackUrl().toString() : "");
        pss.setLiteral("?receivingCapacity", client.getReceivingCapacity() != null ? client.getReceivingCapacity() : 0);
        pss.setLiteral("?responseDelivery", client.getResponseDelivery() != null ? client.getResponseDelivery().name() : "");
        pss.setLiteral("?responseFormat", client.getResponseFormat().name());

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
        }
    }

    public void removeClient(String clientId) throws MiddlewareException {
        logger.debug("Removing client {}...", clientId);

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-remove.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?clientId", CLIENT_PREFIX + clientId);
        UpdateRequest updateRequest = pss.asUpdate();

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
            logger.debug("Client {} has been removed.", clientId);
        }
    }

    public Client getClientById(String clientId) throws MiddlewareException {
        logger.debug("Retrieving client {}...", clientId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-getById.rq");
        String sparql = null;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?clientId", CLIENT_PREFIX + clientId);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            if (!resultSet.hasNext()) {
                return null;
            } else {
                QuerySolution qs = resultSet.next();
                Client client = new Client();
                client.setClientId(clientId);
                String callbackUrl = qs.getLiteral("callbackUrl").getString();
                if (!Strings.isNullOrEmpty(callbackUrl)) {
                    try {
                        client.setCallbackUrl(new URL(qs.getLiteral("callbackUrl").getString()));
                    } catch (MalformedURLException e) {
                        throw new MiddlewareException("Invalid callbackUrl stored in the Parliament: " + qs.getLiteral("callbackUrl"));
                    }
                }
                client.setReceivingCapacity(qs.getLiteral("receivingCapacity").getInt() == 0 ? null :
                        qs.getLiteral("receivingCapacity").getInt());
                client.setResponseDelivery(qs.getLiteral("responseDelivery").getString().isEmpty() ? null :
                        Client.ResponseDelivery.valueOf(qs.getLiteral("responseDelivery").getString()));
                client.setResponseFormat(Client.ResponseFormat.valueOf(
                        qs.getLiteral("responseFormat").getString()));
                return client;
            }
        }
    }

    public List<Client> listClients() throws MiddlewareException {
        logger.debug("Retrieving list of clients...");
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-getById.rq");
        String sparql;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            List<Client> clients = new ArrayList<>();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                Client client = new Client();
                String clientId = qs.getResource("clientId").getURI().substring(CLIENT_PREFIX.length());
                client.setClientId(clientId);
                String callbackUrl = qs.getLiteral("callbackUrl").getString();
                if (!Strings.isNullOrEmpty(callbackUrl)) {
                    try {
                        client.setCallbackUrl(new URL(qs.getLiteral("callbackUrl").getString()));
                    } catch (MalformedURLException e) {
                        throw new MiddlewareException("Invalid callbackUrl stored in the Parliament: " + qs.getLiteral("callbackUrl"));
                    }
                }
                client.setReceivingCapacity(qs.getLiteral("receivingCapacity").getInt() == 0 ? null :
                        qs.getLiteral("receivingCapacity").getInt());
                client.setResponseDelivery(qs.getLiteral("responseDelivery").getString().isEmpty() ? null :
                        Client.ResponseDelivery.valueOf(qs.getLiteral("responseDelivery").getString()));
                client.setResponseFormat(Client.ResponseFormat.valueOf(
                        qs.getLiteral("responseFormat").getString()));
                clients.add(client);
            }
            return clients;
        }
    }

    public boolean isClientRegistered(String clientId) throws MiddlewareException {
        logger.debug("Checking if client {} exists...", clientId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("client-exists.rq");
        String sparql = null;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?clientId", CLIENT_PREFIX + clientId);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            return queryExecution.execAsk();
        }
    }

    public void registerPlatform(Platform platform) throws MiddlewareException {
        logger.debug("Registering platform {}...", platform.getPlatformId());
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("platform-register.rq");
        String sparql = null;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?platformId", platform.getPlatformId());
        pss.setLiteral("?name", platform.getName());
        pss.setLiteral("?type", platform.getType());
        pss.setLiteral("?baseEndpoint", platform.getBaseEndpoint().toString());
        pss.setIri("?clientId", CLIENT_PREFIX + platform.getClientId());
        pss.setLiteral("?location", platform.getLocationId() != null ? platform.getLocationId() : "");
        pss.setLiteral("?username", platform.getUsername() != null ? platform.getUsername() : "");
        pss.setLiteral("?encryptedPassword", platform.getEncryptedPassword() != null ? platform.getEncryptedPassword() : "");
        pss.setLiteral("?encryptionAlgorithm", platform.getEncryptionAlgorithm() != null ? platform.getEncryptionAlgorithm() : "");

        // alignments
        pss.setLiteral("?downstreamInputAlignmentName", platform.getDownstreamInputAlignmentName());
        pss.setLiteral("?downstreamInputAlignmentVersion", platform.getDownstreamInputAlignmentVersion());
        pss.setLiteral("?downstreamOutputAlignmentName", platform.getDownstreamOutputAlignmentName());
        pss.setLiteral("?downstreamOutputAlignmentVersion", platform.getDownstreamOutputAlignmentVersion());
        pss.setLiteral("?upstreamInputAlignmentName", platform.getUpstreamInputAlignmentName());
        pss.setLiteral("?upstreamInputAlignmentVersion", platform.getUpstreamInputAlignmentVersion());
        pss.setLiteral("?upstreamOutputAlignmentName", platform.getUpstreamOutputAlignmentName());
        pss.setLiteral("?upstreamOutputAlignmentVersion", platform.getUpstreamOutputAlignmentVersion());

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
        }
    }

    public void updatePlatform(Platform platform) throws MiddlewareException {
        logger.debug("Updating platform {}...", platform.getClientId());
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("platform-update.rq");
        String sparql;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?platformId", platform.getPlatformId());
        pss.setLiteral("?name", platform.getName());
        pss.setLiteral("?type", platform.getType());
        pss.setLiteral("?baseEndpoint", platform.getBaseEndpoint().toString());
        pss.setIri("?clientId", CLIENT_PREFIX + platform.getClientId());
        pss.setLiteral("?location", platform.getLocationId() != null ? platform.getLocationId() : "");
        pss.setLiteral("?username", platform.getUsername() != null ? platform.getUsername() : "");
        pss.setLiteral("?encryptedPassword", platform.getEncryptedPassword() != null ? platform.getEncryptedPassword() : "");
        pss.setLiteral("?encryptionAlgorithm", platform.getEncryptionAlgorithm() != null ? platform.getEncryptionAlgorithm() : "");

        // alignments
        pss.setLiteral("?downstreamInputAlignmentName", platform.getDownstreamInputAlignmentName());
        pss.setLiteral("?downstreamInputAlignmentVersion", platform.getDownstreamInputAlignmentVersion());
        pss.setLiteral("?downstreamOutputAlignmentName", platform.getDownstreamOutputAlignmentName());
        pss.setLiteral("?downstreamOutputAlignmentVersion", platform.getDownstreamOutputAlignmentVersion());
        pss.setLiteral("?upstreamInputAlignmentName", platform.getUpstreamInputAlignmentName());
        pss.setLiteral("?upstreamInputAlignmentVersion", platform.getUpstreamInputAlignmentVersion());
        pss.setLiteral("?upstreamOutputAlignmentName", platform.getUpstreamOutputAlignmentName());
        pss.setLiteral("?upstreamOutputAlignmentVersion", platform.getUpstreamOutputAlignmentVersion());

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
        }
    }

    public void removePlatform(String platformId) throws MiddlewareException {
        logger.debug("Removing platform {}...", platformId);

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("platform-remove.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?platformId", platformId);
        UpdateRequest updateRequest = pss.asUpdate();

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
            logger.debug("Platform {} has been removed.", platformId);
        }
    }

    public List<Platform> listPlatforms() throws MiddlewareException {
        logger.debug("Retrieving platforms list...");
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("platform-list.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            List<Platform> platforms = new ArrayList<>();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                Platform platform = new Platform();
                platform.setPlatformId(qs.getResource("platformId").getURI());
                platform.setName(qs.getLiteral("name").getString());
                platform.setType(qs.getLiteral("type").getString());
                try {
                    platform.setBaseEndpoint(new URL(qs.getLiteral("baseEndpoint").getString()));
                } catch (MalformedURLException e) {
                    throw new MiddlewareException("Invalid platform baseEndpoint: " + qs.getLiteral("baseEndpoint"));
                }
                platform.setLocationId(qs.contains("location") ? qs.getLiteral("location").getString() : null);
                String clientId = qs.getResource("clientId").getURI().substring(CLIENT_PREFIX.length());
                platform.setClientId(clientId);
                platform.setUsername(qs.contains("username") ? qs.getLiteral("username").getString() : null);
                platform.setEncryptedPassword(qs.contains("encryptedPassword") ? qs.getLiteral("encryptedPassword").getString() : null);
                platform.setEncryptionAlgorithm(qs.contains("encryptionAlgorithm") ? qs.getLiteral("encryptionAlgorithm").getString() : null);

                platforms.add(platform);
            }
            return platforms;
        }
    }

    public Platform getPlatformById(String platformId) throws MiddlewareException {
        logger.debug("Retrieving platform {}...", platformId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("platform-getById.rq");
        String sparql = null;
        try {
            sparql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(sparql);
        pss.setIri("?platformId", platformId);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            if (!resultSet.hasNext()) {
                return null;
            } else {
                QuerySolution qs = resultSet.next();
                Platform platform = new Platform();
                platform.setPlatformId(platformId);
                platform.setName(qs.getLiteral("name").getString());
                platform.setType(qs.getLiteral("type").getString());
                String clientId = qs.getResource("clientId").getURI().substring(CLIENT_PREFIX.length());
                platform.setClientId(clientId);

                String baseEndpoint = qs.getLiteral("baseEndpoint").getString();
                try {
                    platform.setBaseEndpoint(new URL(baseEndpoint));
                } catch (MalformedURLException e) {
                    throw new MiddlewareException("Invalid baseEndpoint: " + baseEndpoint);
                }

                platform.setLocationId(qs.contains("location") ? qs.getLiteral("location").getString() : null);
                platform.setUsername(qs.contains("username") ? qs.getLiteral("username").getString() : null);
                platform.setEncryptedPassword(qs.contains("encryptedPassword") ? qs.getLiteral("encryptedPassword").getString() : null);
                platform.setEncryptionAlgorithm(qs.contains("encryptionAlgorithm") ? qs.getLiteral("encryptionAlgorithm").getString() : null);

                //alignments
                platform.setDownstreamInputAlignmentName(qs.getLiteral("downstreamInputAlignmentName").getString());
                platform.setDownstreamInputAlignmentVersion(qs.getLiteral("downstreamInputAlignmentVersion").getString());
                platform.setDownstreamOutputAlignmentName(qs.getLiteral("downstreamOutputAlignmentName").getString());
                platform.setDownstreamOutputAlignmentVersion(qs.getLiteral("downstreamOutputAlignmentVersion").getString());
                platform.setUpstreamInputAlignmentName(qs.getLiteral("upstreamInputAlignmentName").getString());
                platform.setUpstreamInputAlignmentVersion(qs.getLiteral("upstreamInputAlignmentVersion").getString());
                platform.setUpstreamOutputAlignmentName(qs.getLiteral("upstreamOutputAlignmentName").getString());
                platform.setUpstreamOutputAlignmentVersion(qs.getLiteral("upstreamOutputAlignmentVersion").getString());

                return platform;
            }
        }
    }

    public void updateDevicesWithQueryBuilder(List<UpdateRequest> updateRequests) {
        try (RDFConnection conn = connect()) {
            for (UpdateRequest update : updateRequests) {
                logger.trace("SPARQL query:");
                logger.trace(update.getOperations().get(0).toString());
                conn.update(update);
            }
        }
    }

    public void updateDeviceWithQueryBuilder(UpdateRequest updateRequest) {
        updateDevicesWithQueryBuilder(Collections.singletonList(updateRequest));
    }

    public void registerDevicesWithQueryBuilder(List<UpdateRequest> insertRequests) {
        try (RDFConnection conn = connect()) {
            for (UpdateRequest request : insertRequests) {
                logger.trace("SPARQL queries:");
                logger.trace(request.getOperations().get(0).toString());
                conn.update(request);
            }
        }
    }

    public void registerDeviceWithQueryBuilder(UpdateRequest insert) {
        registerDevicesWithQueryBuilder(Collections.singletonList(insert));
    }

    public ArrayList<IoTDevice> getDevicesWithQueryBuilder(ParameterizedSparqlString parameterizedSparqlString) {
        String sparql = parameterizedSparqlString.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            Map<String, IoTDevice> devicesPerId = getAndGroupIoTDevices(resultSet);
            return new ArrayList<>(devicesPerId.values());
        }
    }

    public void registerDevices(List<IoTDevice> devices) throws MiddlewareException {
        logger.debug("Registering devices...");

        List<UpdateRequest> updateRequests = new ArrayList<>();
        for (IoTDevice ioTDevice : devices) {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            URL url = Resources.getResource("device-add.rq");
            String template = null;
            try {
                template = Resources.toString(url, Charsets.UTF_8);
            } catch (IOException e) {
                throw new MiddlewareException("Failed to load SPARQL query.", e);
            }

            // because type is not always just one, we must prepare the query by switching the placeholder
            template = template.replace("{type}", getDeviceTypeQueryClause(ioTDevice));

            pss.setCommandText(template);
            pss.setIri("?deviceId", ioTDevice.getDeviceId());
            pss.setLiteral("?name", ioTDevice.getName());
            pss.setIri("?hostedBy", ioTDevice.getHostedBy());
            pss.setLiteral("?location", ioTDevice.getLocation());
            UpdateRequest updateRequest = pss.asUpdate();
            updateRequests.add(updateRequest);
        }

        try (RDFConnection conn = connect()) {
            logger.trace("SPARQL queries:");
            for (UpdateRequest updateRequest1 : updateRequests) {
                if (logger.isTraceEnabled()) {
                    logger.trace(updateRequest1.getOperations().get(0).toString());
                }

                conn.update(updateRequest1);
            }
        }
    }

    public void updateDevices(List<IoTDevice> devices) throws MiddlewareException {
        logger.debug("Updating devices...");

        List<UpdateRequest> updateRequests = new ArrayList<>();
        for (IoTDevice ioTDevice : devices) {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            URL url = Resources.getResource("device-update.rq");
            String template = null;
            try {
                template = Resources.toString(url, Charsets.UTF_8);
            } catch (IOException e) {
                throw new MiddlewareException("Failed to load SPARQL query.", e);
            }

            // because type is not always just one, we must prepare the query by switching the placeholder
            template = template.replace("{type}", getDeviceTypeQueryClause(ioTDevice));

            pss.setCommandText(template);
            pss.setIri("?deviceId", ioTDevice.getDeviceId());
            pss.setLiteral("?name", ioTDevice.getName());
            pss.setIri("?hostedBy", ioTDevice.getHostedBy());
            pss.setLiteral("?location", ioTDevice.getLocation());
            UpdateRequest updateRequest = pss.asUpdate();
            updateRequests.add(updateRequest);
        }

        try (RDFConnection conn = connect()) {
            logger.trace("SPARQL queries :");
            for (UpdateRequest updateRequest1 : updateRequests) {
                if (logger.isTraceEnabled()) {
                    logger.trace(updateRequest1.getOperations().get(0).toString());
                }

                conn.update(updateRequest1);
            }
        }
    }

    public void registerLocation(Location location) throws MiddlewareException {
        throw new NotImplementedException("Register location is not implemented in ParliamentRegistry, try usin ParliamentRegistryExperimental");
    }

    public Location getLocation(String locationId) throws MiddlewareException {
        throw new NotImplementedException("Get location is not implemented in ParliamentRegistry, try usin ParliamentRegistryExperimental");
    }

    public List<Location> getAllLocations() throws MiddlewareException {
        throw new NotImplementedException("Get all locations is not implemented in ParliamentRegistry, try usin ParliamentRegistryExperimental");
    }

    protected String getDeviceTypeQueryClause(IoTDevice ioTDevice) {
        StringBuilder sb = new StringBuilder();
        EnumSet<IoTDeviceType> deviceTypes = ioTDevice.getDeviceTypes();
        for (IoTDeviceType deviceType : deviceTypes) {
            sb.append(String.format("rdf:type <%s>;", deviceType.getDeviceTypeUri()));
        }
        return sb.toString();
    }

    public void removeDevices(List<String> deviceIds) throws MiddlewareException {
        for (String deviceId : deviceIds) {
            logger.debug("Removing device {}...", deviceId);

            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            URL url = Resources.getResource("device-delete.rq");
            String template = null;
            try {
                template = Resources.toString(url, Charsets.UTF_8);
            } catch (IOException e) {
                throw new MiddlewareException("Failed to load SPARQL query.", e);
            }
            pss.setCommandText(template);
            pss.setIri("?deviceId", deviceId);
            UpdateRequest updateRequest = pss.asUpdate();

            String sparql = pss.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SPARQL query:\n{}", sparql);
            }

            try (RDFConnection conn = connect()) {
                conn.update(updateRequest);
                logger.debug("Device {} has been removed successfully.", deviceId);
            }
        }
    }

    public List<IoTDevice> getDevices(List<String> deviceIds) throws MiddlewareException {
        logger.debug("Retrieving devices {}...", deviceIds);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("device-getSelected.rq");
        String template;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }

        List<String> deviceIris = new ArrayList<>();
        for (String deviceId : deviceIds) {
            deviceIris.add("<" + deviceId + ">");
        }
        String deviceIrisString = StringUtils.join(deviceIris, ",");
        template = template.replace("%DEVICE_ID_LIST%", deviceIrisString);

        pss.setCommandText(template);

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            Map<String, IoTDevice> devicesPerId = getAndGroupIoTDevices(resultSet);
            return new ArrayList<>(devicesPerId.values());
        }
    }

    public List<IoTDevice> getDevicesByType(IoTDeviceType type, String platformId) throws MiddlewareException {
        throw new UnsupportedOperationException("getSensorDevices is not implemented. Try using ParliamentRegistryExperimental");
    }

    public void subscribe(Subscription subscription) throws MiddlewareException {
        logger.debug("Storing subscription {}...", subscription.getConversationId());

        if (subscription.getDeviceIds().isEmpty()) {
            throw new MiddlewareException("A subscription must have at least one device.");
        }

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-add.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?conversationId", SUBSCRIPTION_PREFIX + subscription.getConversationId());
        pss.setIri("?clientId", CLIENT_PREFIX + subscription.getClientId());
        pss.setLiteral("deviceId", "{DEVICE_IDS}");

        String sparql = pss.toString();
        List<String> deviceIris = new ArrayList<>();
        for (String deviceId : subscription.getDeviceIds()) {
            deviceIris.add("<" + deviceId + ">");
        }

        sparql = sparql.replace("\"{DEVICE_IDS}\"", String.join(",", deviceIris));
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.add(sparql);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
            logger.debug("Subscription {} has been stored successfully.", subscription.getConversationId());
        }
    }

    public Subscription getSubscriptionById(String conversationId) throws MiddlewareException {
        logger.debug("Retrieving subscription with ID {}...", conversationId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-getById.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?conversationId", SUBSCRIPTION_PREFIX + conversationId);

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            if (!resultSet.hasNext()) {
                return null;
            } else {
                Subscription subscription = new Subscription();
                subscription.setConversationId(conversationId);
                while (resultSet.hasNext()) {
                    QuerySolution qs = resultSet.next();
                    if (subscription.getClientId() == null) {
                        String clientId = qs.getResource("clientId").getURI().substring(CLIENT_PREFIX.length());
                        subscription.setClientId(clientId);
                    }
                    String deviceId = qs.getResource("deviceId").getURI();
                    subscription.addDeviceId(deviceId);
                }
                return subscription;
            }
        }
    }

    public Subscription findSubscription(String clientId, String conversationId) throws MiddlewareException {
        logger.debug("Retrieving subscription with clientId={}, conversationId={}", clientId, conversationId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-findByClientIdConvId.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?clientId", CLIENT_PREFIX + clientId);
        pss.setIri("conversationId", SUBSCRIPTION_PREFIX + conversationId);

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            if (!resultSet.hasNext()) {
                return null;
            } else {
                Subscription subscription = new Subscription();
                subscription.setConversationId(conversationId);
                subscription.setClientId(clientId);
                while (resultSet.hasNext()) {
                    QuerySolution qs = resultSet.next();
                    String deviceId = qs.getResource("deviceId").getURI();
                    subscription.addDeviceId(deviceId);
                }
                return subscription;
            }
        }
    }

    public List<Subscription> listSubcriptions() throws MiddlewareException {
        return listSubcriptions(null);
    }

    public List<Subscription> listSubcriptions(String clientId) throws MiddlewareException {
        logger.debug("Retrieving subscriptions...");
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-getById.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        if (clientId != null) {
            pss.setIri("?clientId", CLIENT_PREFIX + clientId);
        }

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();

            Map<String, Subscription> subscriptionsMap = new HashMap<>();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                String conversationId = qs.getResource("conversationId").getURI().substring(SUBSCRIPTION_PREFIX.length());
                if (clientId == null) {
                    clientId = qs.getResource("clientId").getURI().substring(CLIENT_PREFIX.length());
                }
                String deviceId = qs.getResource("deviceId").getURI();

                Subscription subscription;
                if (!subscriptionsMap.containsKey(conversationId)) {
                    subscription = new Subscription();
                    subscription.setConversationId(conversationId);
                    subscription.setClientId(clientId);
                    subscriptionsMap.put(conversationId, subscription);

                } else {
                    subscription = subscriptionsMap.get(conversationId);
                }

                subscription.addDeviceId(deviceId);
            }

            return new ArrayList<>(subscriptionsMap.values());
        }
    }

    public boolean isClientSubscribed(String clientId, List<IoTDevice> devices) throws MiddlewareException {
        logger.debug("Checking if client {} is subscribed to specified devices...", clientId);
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-isClientSubscribed.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?clientId", CLIENT_PREFIX + clientId);
        pss.setLiteral("deviceId", "{DEVICE_IDS}");

        String sparql = pss.toString();
        List<String> deviceIris = new ArrayList<>();
        for (IoTDevice ioTDevice : devices) {
            deviceIris.add("<" + ioTDevice.getDeviceId() + ">");
        }

        sparql = sparql.replace("\"{DEVICE_IDS}\"", String.join(",", deviceIris));

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sparql);
            ResultSet resultSet = queryExecution.execSelect();
            return resultSet.hasNext();
        }
    }

    public void deleteSubscription(String conversationId) throws MiddlewareException {
        logger.debug("Deleting subscription {}...", conversationId);

        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource("subscription-remove.rq");
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        pss.setCommandText(template);
        pss.setIri("?conversationId", SUBSCRIPTION_PREFIX + conversationId);
        UpdateRequest updateRequest = pss.asUpdate();

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
            logger.debug("Subscription {} has been deleted.", conversationId);
        }
    }

    private Map<String, IoTDevice> getAndGroupIoTDevices(ResultSet resultSet) {
        Map<String, IoTDevice> devicesPerId = new HashMap<>();
        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String deviceId = qs.getResource("deviceId").getURI();
            IoTDeviceType deviceType = IoTDeviceType.fromDeviceTypeUri(qs.getLiteral("type").getString());

            if (devicesPerId.containsKey(deviceId)) {
                devicesPerId.get(deviceId).addDeviceType(deviceType);
                if (qs.getResource("hosts") != null) {
                    devicesPerId.get(deviceId).addHosts(qs.getResource("hosts").getURI());
                }
                if (qs.getResource("isForProperty") != null) {
                    devicesPerId.get(deviceId).addForProperty(qs.getResource("isForProperty").getURI());
                }
                if (qs.getResource("observes") != null) {
                    devicesPerId.get(deviceId).addObserves(qs.getResource("observes").getURI());
                }
            } else {
                IoTDevice ioTDevice = new IoTDevice(deviceId);
                ioTDevice.setHostedBy(qs.getResource("hostedBy").getURI());
                ioTDevice.setName(qs.getLiteral("name").getString());
                ioTDevice.setDeviceTypes(EnumSet.of(deviceType));

                // optional values
                // sets
                if (qs.getResource("hosts") != null) {
                    ArrayList<String> hosts = new ArrayList<>();
                    hosts.add(qs.getResource("hosts").getURI());
                    ioTDevice.setHosts(hosts);
                }
                if (qs.getResource("isForProperty") != null) {
                    Set<String> isForProperty = new HashSet<>();
                    isForProperty.add(qs.getResource("isForProperty").getURI());
                    ioTDevice.setForProperty(isForProperty);
                }
                if (qs.getResource("observes") != null) {
                    Set<String> observes = new HashSet<>();
                    observes.add(qs.getResource("observes").getURI());
                    ioTDevice.setObserves(observes);
                }

                // single values
                ioTDevice.setLocation(qs.getLiteral("location") != null ?
                        qs.getLiteral("location").getString() : null);
                ioTDevice.setMadeActuation(qs.getResource("madeActuation") != null ?
                        qs.getResource("madeActuation").getURI() : null);
                ioTDevice.setImplementsProcedure(qs.getResource("implementsProcedure") != null ?
                        qs.getResource("implementsProcedure").getURI() : null);
                ioTDevice.setDetects(qs.getResource("detects") != null ?
                        qs.getResource("detects").getURI() : null);
                ioTDevice.setMadeObservation(qs.getResource("madeObservation") != null
                        ? qs.getResource("madeObservation").getURI() : null);

                devicesPerId.put(deviceId, ioTDevice);
            }
        }
        return devicesPerId;
    }

    public Configuration getConf() {
        return conf;
    }
}
