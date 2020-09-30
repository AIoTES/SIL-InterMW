package eu.interiot.intermw.services.registry;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.*;
import eu.interiot.intermw.commons.model.Client;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.model.PlatformStatistics;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import eu.interiot.intermw.commons.model.extractors.IoTDeviceExtractor;
import eu.interiot.intermw.commons.responses.DeviceRegistryInitializeRes;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.payload.types.IoTDevicePayload;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphExtract;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TripleBoundary;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Gasper Vrhovsek
 */
public class ParliamentRegistryExperimental extends ParliamentRegistry {
    private final static Logger logger = LoggerFactory.getLogger(ParliamentRegistryExperimental.class);

    private static final String PREFIXES = "PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#>\n" +
            "PREFIX fn: <http://www.w3.org/2005/xpath-functions#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX par: <http://parliament.semwebcentral.org/parliament#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX time: <http://www.w3.org/2006/time#>\n" +
            "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX iiotex: <http://inter-iot.eu/GOIoTPex#>\n" +
            "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>\n" +
            "PREFIX iiot: <http://inter-iot.eu/GOIoTP#>\n" +
            "PREFIX InterIoT: <http://inter-iot.eu/>\n" +
            "PREFIX ssn: <http://www.w3.org/ns/ssn/>\n" +
            "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
            "PREFIX mdw: <http://interiot.eu/ont/middleware.owl#>\n" +
            "PREFIX clients: <http://interiot.eu/clients#>";


    //    private static final String CLIENT_PREFIX = "clients:";
    private static final String CLIENT_PREFIX = "http://inter-iot.eu/clients#";
    private static final String CLIENTS_GRAPH = "http://inter-iot.eu/clients";
    private static final String PLATFORM_GRAPH = "http://inter-iot.eu/platforms";
    private static final String DEVICE_GRAPH = "http://interiot.eu/devices";
    private static final String MIDDLEWARE_NAMESPACE = "http://interiot.eu/ont/middleware.owl#";

    public ParliamentRegistryExperimental(Configuration conf) {
        super(conf);
    }

    @Override
    public void registerClient(Client client) throws MiddlewareException {
        logger.debug("Registering client {}...", client.getClientId());
        // Insert client id
        // TODO combine both insertions into same template file!
        String clientRefInsert = getRefInsert(CLIENTS_GRAPH, getClientGraphId(client.getClientId()));
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", clientRefInsert);
        }

        // Insert client
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-create.rq");
        pss.setIri("?graph", getClientGraphId(client.getClientId()));
        pss.setIri("?clientId", getClientGraphId(client.getClientId()));
        pss.setLiteral("?callbackUrl", client.getCallbackUrl() != null ? client.getCallbackUrl().toString() : "");
        pss.setLiteral("?receivingCapacity", client.getReceivingCapacity() != null ? client.getReceivingCapacity() : 0);
        pss.setLiteral("?responseDelivery", client.getResponseDelivery() != null ? client.getResponseDelivery().name() : "");
        pss.setLiteral("?responseFormat", client.getResponseFormat().name());

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(clientRefInsert);
            conn.update(updateRequest);
        }
    }

    @Override
    public boolean isClientRegistered(String clientId) throws MiddlewareException {
        logger.debug("Checking if client {} exists...", clientId);

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-exists.rq");
        pss.setIri("?clientId", getClientGraphId(clientId));
        pss.setIri("?clientGraph", getClientGraphId(clientId));

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            return queryExecution.execAsk();
        }
    }

    @Override
    public Client getClientById(String clientId) throws MiddlewareException {
        logger.debug("Retrieving client {}...", clientId);
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-getById-construct.rq");
        pss.setIri("?graph", getClientGraphId(clientId));

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Client client = new Client();
        client.setClientId(clientId);
        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            Model clientModel = conn.queryConstruct(query);
            StmtIterator stmtIterator = clientModel.listStatements();

            while (stmtIterator.hasNext()) {
                Statement next = stmtIterator.next();
                String predicate = next.getPredicate().getLocalName();
                RDFNode object = next.getObject();

                if (predicate.equals("callbackUrl")) {
                    if (!object.asLiteral().getString().isEmpty()) {
                        client.setCallbackUrl(new URL(object.asLiteral().getString()));
                    }
                } else if (predicate.equals("receivingCapacity")) {
                    client.setReceivingCapacity(object.asLiteral().getInt());
                } else if (predicate.equals("responseDelivery")) {
                    client.setResponseDelivery(Client.ResponseDelivery.valueOf(object.asLiteral().getString()));
                } else if (predicate.equals("responseFormat")) {
                    client.setResponseFormat(Client.ResponseFormat.valueOf(object.asLiteral().getString()));
                }
            }
        } catch (MalformedURLException e) {
            throw new MiddlewareException("Failed to retrieve client", e);
        }
        return client;
    }

    @Override
    public List<Client> listClients() throws MiddlewareException {
        List<Client> clients = new ArrayList<>();
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-getAll.rq");
        Query query = pss.asQuery();

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            while (resultSet.hasNext()) {
                // looping through data!
                // ?clientId ?callbackUrl ?receivingCapacity ?responseDelivery ?responseFormat
                QuerySolution next = resultSet.next();
                RDFNode clientId = next.get("client");
                RDFNode callbackUrl = next.get("callbackUrl");
                RDFNode receivingCapacity = next.get("receivingCapacity");
                RDFNode responseDelivery = next.get("responseDelivery");
                RDFNode responseFormat = next.get("responseFormat");

                Client client = new Client();
                client.setClientId(clientId.toString().split(CLIENTS_GRAPH + "#")[1]);
                if (!callbackUrl.asLiteral().getString().isEmpty()) {
                    client.setCallbackUrl(new URL(callbackUrl.asLiteral().getString()));
                }
                client.setReceivingCapacity(receivingCapacity.asLiteral().getInt());
                client.setResponseDelivery(Client.ResponseDelivery.valueOf(responseDelivery.asLiteral().getString()));
                client.setResponseFormat(Client.ResponseFormat.valueOf(responseFormat.asLiteral().getString()));

                clients.add(client);
            }
        } catch (MalformedURLException e) {
            logger.error("Could not list clients", e);
            throw new MiddlewareException(e);
        }
        return clients;
    }

    @Override
    public void removeClient(String clientId) throws MiddlewareException {
        logger.debug("Removing client {}...", clientId);

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-remove.rq");
        pss.setIri("?clientId", getClientGraphId(clientId));
        pss.setIri("?clientIDGraph", getClientGraphId(clientId));
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

    @Override
    public void updateClient(Client client) throws MiddlewareException {
        logger.debug("Registering client {}...", client.getClientId());

        // Insert client
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/client-update.rq");
        pss.setIri("?graph", getClientGraphId(client.getClientId()));
        pss.setIri("?clientId", getClientGraphId(client.getClientId()));
        pss.setLiteral("?callbackUrl", client.getCallbackUrl() != null ? client.getCallbackUrl().toString() : "");
        pss.setLiteral("?receivingCapacity", client.getReceivingCapacity() != null ? client.getReceivingCapacity() : 0);
        pss.setLiteral("?responseDelivery", client.getResponseDelivery() != null ? client.getResponseDelivery().name() : "");
        pss.setLiteral("?responseFormat", client.getResponseFormat().name());

        update(pss);
    }

    @Override
    public void registerPlatform(Platform platform) throws MiddlewareException {
        logger.debug("Registering platform {}...", platform.getPlatformId());

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/platform-register.rq");
        pss.setIri("?platformGraphId", platform.getPlatformId());
        pss.setIri("?platformId", platform.getPlatformId());
        pss.setLiteral("?name", platform.getName());
        pss.setLiteral("?type", platform.getType());
        pss.setLiteral("?baseEndpoint", platform.getBaseEndpoint().toString());
        pss.setIri("?clientId", CLIENT_PREFIX + platform.getClientId());
        pss.setLiteral("?timeCreated", platform.getTimeCreated());

        pss.setLiteral("?encryptedPassword", platform.getEncryptedPassword() != null ? platform.getEncryptedPassword() : "");
        pss.setLiteral("?encryptionAlgorithm", platform.getEncryptionAlgorithm() != null ? platform.getEncryptionAlgorithm() : "");
        pss.setLiteral("?location", platform.getLocationId() != null ? platform.getLocationId() : "");
        pss.setLiteral("?username", platform.getUsername() != null ? platform.getUsername() : "");

        // alignments
        pss.setLiteral("?downstreamInputAlignmentName", platform.getDownstreamInputAlignmentName());
        pss.setLiteral("?downstreamInputAlignmentVersion", platform.getDownstreamInputAlignmentVersion());
        pss.setLiteral("?downstreamOutputAlignmentName", platform.getDownstreamOutputAlignmentName());
        pss.setLiteral("?downstreamOutputAlignmentVersion", platform.getDownstreamOutputAlignmentVersion());
        pss.setLiteral("?upstreamInputAlignmentName", platform.getUpstreamInputAlignmentName());
        pss.setLiteral("?upstreamInputAlignmentVersion", platform.getUpstreamInputAlignmentVersion());
        pss.setLiteral("?upstreamOutputAlignmentName", platform.getUpstreamOutputAlignmentName());
        pss.setLiteral("?upstreamOutputAlignmentVersion", platform.getUpstreamOutputAlignmentVersion());

        update(pss);
    }

    @Override
    public List<Platform> listPlatforms() throws MiddlewareException {
        URL url = Resources.getResource("experimental/platforms-getAll.rq");
        String selectAllPlatforms;
        try {
            selectAllPlatforms = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }

        try (RDFConnection conn = connect()) {
            Model allPlatformsModel = conn.queryConstruct(selectAllPlatforms);

            StmtIterator stmtIterator = allPlatformsModel.listStatements();

            List<String> platformIds = new ArrayList<>();
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                platformIds.add(statement.getObject().toString());
            }

            List<Platform> platformList = new ArrayList<>();
            if (!platformIds.isEmpty()) {
                String platformsQuery = getPlatformsQuery(platformIds);

                logger.debug("SPARQL construct:\n{}", platformsQuery);
                Model platforms = conn.queryConstruct(platformsQuery);
                try {
                    platformList = extractPlatformList(platforms);
                } catch (MalformedURLException e) {
                    throw new MiddlewareException("Failed to extract platform list from jena model", e);
                }
            }

            return setPlatformStatistics(platformList);
        }
    }

    @Override
    public Platform getPlatformById(String platformId) throws MiddlewareException {
        logger.debug("Retrieving platform {}...", platformId);
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/platform-getById.rq");
        pss.setIri("?platformGraphId", platformId);

        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            Model platformModel = conn.queryConstruct(query);
            List<Platform> platformList = extractPlatformList(platformModel);
            if (platformList.isEmpty()) {
                return null;
            } else if (platformList.size() == 1) {
                platformList = setPlatformStatistics(platformList);
                return platformList.get(0);
            } else {
                throw new MiddlewareException("Multiple platforms found for a single platform ID");
            }
        } catch (MalformedURLException e) {
            throw new MiddlewareException("Failed to extract platform list from jena model", e);
        }
    }

    @Override
    public void removePlatform(String platformId) throws MiddlewareException {
        logger.debug("Removing platform {}...", platformId);

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/platform-remove.rq");
        pss.setIri("?platformId", platformId);
        pss.setIri("?platformGraphId", platformId);

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

    @Override
    public void updatePlatform(Platform platform) throws MiddlewareException {
        logger.debug("Registering platform {}...", platform.getPlatformId());

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/platform-update.rq");
        pss.setIri("?platformGraphId", platform.getPlatformId());
        pss.setIri("?platformId", platform.getPlatformId());
        pss.setLiteral("?name", platform.getName());
        pss.setLiteral("?type", platform.getType());
        pss.setLiteral("?baseEndpoint", platform.getBaseEndpoint().toString());
        pss.setIri("?clientId", CLIENT_PREFIX + platform.getClientId());
        pss.setLiteral("?timeCreated", platform.getTimeCreated());

        if (platform.getLocationId() != null) {
            pss.setLiteral("?location", platform.getLocationId());
        } else {
            pss.setParam("?location", (Node) null);
        }
        if (platform.getUsername() != null) {
            pss.setLiteral("?username", platform.getUsername());
        } else {
            pss.setParam("?username", (Node) null);
        }
        if (platform.getEncryptedPassword() != null) {
            pss.setLiteral("?encryptedPassword", platform.getEncryptedPassword());
        } else {
            pss.setParam("?encryptedPassword", (Node) null);
        }
        if (platform.getEncryptionAlgorithm() != null) {
            pss.setLiteral("?encryptionAlgorithm", platform.getEncryptionAlgorithm());
        } else {
            pss.setParam("?encryptionAlgorithm", (Node) null);
        }

        // Alignments
        pss.setLiteral("?downstreamInputAlignmentName", platform.getDownstreamInputAlignmentName());
        pss.setLiteral("?downstreamInputAlignmentVersion", platform.getDownstreamInputAlignmentVersion());
        pss.setLiteral("?downstreamOutputAlignmentName", platform.getDownstreamOutputAlignmentName());
        pss.setLiteral("?downstreamOutputAlignmentVersion", platform.getDownstreamOutputAlignmentVersion());
        pss.setLiteral("?upstreamInputAlignmentName", platform.getUpstreamInputAlignmentName());
        pss.setLiteral("?upstreamInputAlignmentVersion", platform.getUpstreamInputAlignmentVersion());
        pss.setLiteral("?upstreamOutputAlignmentName", platform.getUpstreamOutputAlignmentName());
        pss.setLiteral("?upstreamOutputAlignmentVersion", platform.getUpstreamOutputAlignmentVersion());

        pss.setLiteral("?downstreamInputAlignmentName", platform.getDownstreamInputAlignmentName());
        pss.setLiteral("?downstreamInputAlignmentVersion", platform.getDownstreamInputAlignmentVersion());
        pss.setLiteral("?downstreamOutputAlignmentName", platform.getDownstreamOutputAlignmentName());
        pss.setLiteral("?downstreamOutputAlignmentVersion", platform.getDownstreamOutputAlignmentVersion());
        pss.setLiteral("?upstreamInputAlignmentName", platform.getUpstreamInputAlignmentName());
        pss.setLiteral("?upstreamInputAlignmentVersion", platform.getUpstreamInputAlignmentVersion());
        pss.setLiteral("?upstreamOutputAlignmentName", platform.getUpstreamOutputAlignmentName());
        pss.setLiteral("?upstreamOutputAlignmentVersion", platform.getUpstreamOutputAlignmentVersion());


        update(pss);
    }

    @Override
    public void registerDevices(List<IoTDevice> devices) throws MiddlewareException {
        logger.debug("Registering devices...");

        List<UpdateRequest> updateRequests = new ArrayList<>();
        for (IoTDevice ioTDevice : devices) {

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{type}", getDeviceTypeQueryClause(ioTDevice));

            ParameterizedSparqlString pss = getPSSfromTemplate("experimental/device-create.rq", replacements);

            pss.setIri("?deviceId", ioTDevice.getDeviceId());
            pss.setLiteral("?name", ioTDevice.getName());
            pss.setIri("?hostedBy", ioTDevice.getHostedBy());
            pss.setIri("?location", ioTDevice.getLocation());

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

    @Override
    public void updateDevices(List<IoTDevice> devices) throws MiddlewareException {
        logger.debug("Updating devices...");

        List<UpdateRequest> updateRequests = new ArrayList<>();
        for (IoTDevice ioTDevice : devices) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{type}", getDeviceTypeQueryClause(ioTDevice));
            ParameterizedSparqlString pss = getPSSfromTemplate("experimental/device-update.rq", replacements);
            pss.setIri("?deviceId", ioTDevice.getDeviceId());
            pss.setLiteral("?name", ioTDevice.getName());
            pss.setIri("?hostedBy", ioTDevice.getHostedBy());
            pss.setIri("?location", ioTDevice.getLocation());

            UpdateRequest updateRequest = pss.asUpdate();
            updateRequests.add(updateRequest);
        }

        try (RDFConnection conn = connect()) {
            logger.trace("SPARQL queries :");
            for (UpdateRequest updateRequest : updateRequests) {
                if (logger.isTraceEnabled()) {
                    logger.trace(updateRequest.getOperations().get(0).toString());
                }
                conn.update(updateRequest);
            }
        }
    }

    public void registerLocation(Location location) throws MiddlewareException {
        String locReplacement = "";
        if (location.getLatitude() != null && location.getLongitude() != null) {
             locReplacement = "mdw:hasLatitude ?latitude;\n" +
                    "          mdw:hasLongitude ?longitude;";
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{lat_long}", locReplacement);

        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/location-register.rq", replacements);
        pss.setIri("?locationId", location.getLocationId());
        pss.setIri("?locationGraphId", location.getLocationId());
        pss.setLiteral("?description", location.getDescription() != null ? location.getDescription() : "");
        // optionals
        if (locReplacement.length() > 0) {
            pss.setLiteral("?latitude", location.getLatitude());
            pss.setLiteral("?longitude", location.getLongitude());
        }

//        System.out.println("registerLocation sparql = " + pss.toString());

        update(pss);
    }

    public Location getLocation(String locationId) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/location-getById.rq");
        pss.setIri("?locationGraphId", locationId);

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

//            resultSet.getRowNumber();
            while (resultSet.hasNext()) {

                QuerySolution next = resultSet.next();

                RDFNode locationId1 = next.get("locationId");
                RDFNode description = next.get("description");
                RDFNode latitude = next.get("latitude");
                RDFNode longitude = next.get("longitude");

                return new Location(
                        locationId1.toString(),
                        description.toString(),
                        latitude != null ? latitude.asLiteral().getDouble() : null,
                        longitude != null ? longitude.asLiteral().getDouble() : null
                );


            }
        }
        return null;
    }

    public List<Location> getAllLocations() throws MiddlewareException {
        List<Location> result = new ArrayList<>();
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/location-getAll.rq");

        Query query = pss.asQuery();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution next = resultSet.next();

                RDFNode locationId = next.get("locationId");
                RDFNode description = next.get("description");
                RDFNode latitude = next.get("latitude");
                RDFNode longitude = next.get("longitude");

                result.add(
                        new Location(
                                locationId.asResource().toString(),
                                description.asLiteral().getString(),
                                latitude.asLiteral().getDouble(),
                                longitude.asLiteral().getDouble()));

            }
        }
        return result;
    }

    public void removeLocation(String locationId) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/location-removeById.rq");
        pss.setIri("?locationId", locationId);
        update(pss);
    }

    public void updateLocation(Location locationUpdate) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/location-update.rq");
        pss.setIri("?locationGraphId", locationUpdate.getLocationId());
        pss.setIri("?locationId", locationUpdate.getLocationId());


        pss.setLiteral("?description", locationUpdate.getDescription() != null ? locationUpdate.getDescription() : "");
        // optionals
        pss.setLiteral("?latitude", locationUpdate.getLatitude());
        pss.setLiteral("?longitude", locationUpdate.getLongitude());

        update(pss);
    }

    public void registerDevices(String platformId, Message message) throws MiddlewareException {
        // validate we have IoTDevices
        IoTDevicePayload ioTDevicePayload = message.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
        Set<EntityID> ioTDevices = ioTDevicePayload.getIoTDevices();

        if (ioTDevices == null || ioTDevices.isEmpty()) {
            logger.warn("Trying to register devices with empty device list, no devices will be registered");
            return;
        }

        try (RDFConnection conn = connect()) {
            String updateString = buildDeviceDiscoverySparqlInsert(platformId, message);
            if (logger.isTraceEnabled()) {
                logger.trace("Register devices insert: " + updateString);
            }
            conn.update(updateString);
        }

    }

    public List<String> getDeviceIds(String platformId) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/device-getAllPlatformDeviceIds.rq");

        if (StringUtils.isNotBlank(platformId)) {
            pss.setIri("?platformId", platformId);
        }

        Query query = pss.asQuery();

        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        List<String> results = new ArrayList<>();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(query);
            ResultSet resultSet = queryExecution.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution next = resultSet.next();

                Resource deviceIdRes = next.getResource("deviceId");
                results.add(deviceIdRes.toString());
            }
        }
        return results;
    }

    @Override
    public List<IoTDevice> getDevices(List<String> deviceIds) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSDeviceByIds(deviceIds);

        try (RDFConnection conn = connect()) {
            Model model = conn.queryConstruct(pss.asQuery());


            Message message = new Message();
            message.setPayload(new IoTDevicePayload(model));

            DeviceRegistryInitializeRes res = new DeviceRegistryInitializeRes(message);

            IoTDeviceExtractor.fromIoTDevicePayload(new IoTDevicePayload(model));

            return res.getIoTDevices();
        }
    }

    @Override
    public List<IoTDevice> getDevicesByType(IoTDeviceType type, String platformId) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/deviceId-getByType.rq");
        pss.setIri("?sosaDeviceType", type.getDeviceTypeUri());
        pss.setIri("?platformId", platformId);
        Query sensorIdsQuery = pss.asQuery();


        String sparql = pss.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", sparql);
        }

        List<String> sensorIds = new ArrayList<>();
        try (RDFConnection conn = connect()) {
            QueryExecution queryExecution = conn.query(sensorIdsQuery);
            ResultSet resultSet = queryExecution.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution next = resultSet.next();

                Resource deviceIdRes = next.getResource("deviceId");
                sensorIds.add(deviceIdRes.toString());
            }

            ParameterizedSparqlString pssDeviceByIds = getPSSDeviceByIds(sensorIds);
            Model model = conn.queryConstruct(pssDeviceByIds.asQuery());
            return IoTDeviceExtractor.fromIoTDevicePayload(new IoTDevicePayload(model));
        }
    }

    @Override
    public void removeDevices(List<String> deviceIds) throws MiddlewareException {
        for (String deviceId : deviceIds) {
            logger.debug("Removing device {}...", deviceId);

            ParameterizedSparqlString pss = getPSSfromTemplate("experimental/device-delete.rq");
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

    private List<Platform> setPlatformStatistics(List<Platform> platforms) throws MiddlewareException {
        ParameterizedSparqlString pssCountDevices = getPSSfromTemplate("experimental/platforms-countDevices.rq");
        ParameterizedSparqlString pssCountSubscribedDevices = getPSSfromTemplate("experimental/platforms-countSubscribedDevices.rq");
        ParameterizedSparqlString pssCountSubscriptions = getPSSfromTemplate("experimental/platforms-countSubscriptions.rq");
        HashMap<String, Integer> devicesPerPlatform = new HashMap<>();
        HashMap<String, Integer> subscriptionsPerPlatform = new HashMap<>();
        HashMap<String, Integer> subscribedDevicesPerPlatform = new HashMap<>();
        List<Platform> setPlatforms = new ArrayList<>();

        try (RDFConnection conn = connect()) {
            Consumer<QuerySolution> consumerDevicesPerPlatform = querySolution -> {
                if (querySolution.contains("platformId") && querySolution.contains("devices")) {
                    devicesPerPlatform.put(querySolution.get("platformId").toString(), querySolution.get("devices").asLiteral().getInt());
                }
            };
            conn.querySelect(pssCountDevices.asQuery(), consumerDevicesPerPlatform);
            Consumer<QuerySolution> consumerSubscribedDevicesPerPlatform = querySolution -> {
                if (querySolution.contains("platformId") && querySolution.contains("subscribedDevices")) {
                    subscribedDevicesPerPlatform.put(querySolution.get("platformId").toString(), querySolution.get("subscribedDevices").asLiteral().getInt());
                }
            };
            conn.querySelect(pssCountSubscribedDevices.asQuery(), consumerSubscribedDevicesPerPlatform);
            Consumer<QuerySolution> consumerSubscriptionsPerPlatform = querySolution -> {
                if (querySolution.contains("platformId") && querySolution.contains("subscriptions")) {
                    subscriptionsPerPlatform.put(querySolution.get("platformId").toString(), querySolution.get("subscriptions").asLiteral().getInt());
                }
            };
            conn.querySelect(pssCountSubscriptions.asQuery(), consumerSubscriptionsPerPlatform);

        } catch (Exception e) {
            logger.error("Error occurred while listing devices: {} ", e);
        }

        for (Platform platform : platforms) {
            String platformId = platform.getPlatformId();
            PlatformStatistics platformStatistics = new PlatformStatistics();
            platformStatistics.setDeviceCount(devicesPerPlatform.getOrDefault(platformId, 0));
            platformStatistics.setSubscribedDeviceCount(subscribedDevicesPerPlatform.getOrDefault(platformId, 0));
            platformStatistics.setSubscriptionCount(subscriptionsPerPlatform.getOrDefault(platformId, 0));
            platform.setPlatformStatistics(platformStatistics);
            setPlatforms.add(platform);
        }
        return setPlatforms;
    }

    private RDFConnection connect() {
        return RDFConnectionFactory.connect(getConf().getParliamentUrl(), "sparql", "sparql", "sparql");
    }

    private void update(ParameterizedSparqlString pss) {
        if (logger.isTraceEnabled()) {
            logger.trace("SPARQL query:\n{}", pss.toString());
        }
        UpdateRequest updateRequest = pss.asUpdate();
        try (RDFConnection conn = connect()) {
            conn.update(updateRequest);
        }
    }

    private String getClientGraphId(String clientId) {
        return CLIENTS_GRAPH + "#" + clientId;
    }

    private List<Platform> extractPlatformList(Model platforms) throws MalformedURLException {
        Map<String, Platform> platformMap = new HashMap<>();
        StmtIterator stmtIterator = platforms.listStatements();

        while (stmtIterator.hasNext()) {
            Statement next = stmtIterator.next();

            String platformId = next.getSubject().toString();
            Property predicate = next.getPredicate();

            RDFNode object = next.getObject();

            if (!platformMap.containsKey(platformId)) {
                platformMap.put(platformId, new Platform());
            }

            Platform platform = platformMap.get(platformId);
            platform.setPlatformId(platformId);

            if (predicate.getNameSpace().equals(MIDDLEWARE_NAMESPACE)) {
                String localName = predicate.getLocalName();
                switch (localName) {
                    case "name":
                        platform.setName(object.asLiteral().getString());
                        break;
                    case "type":
                        platform.setType(object.asLiteral().getString());
                        break;
                    case "baseEndpoint":
                        if (!object.asLiteral().getString().isEmpty()) {
                            platform.setBaseEndpoint(new URL(object.asLiteral().getString()));
                        }
                        break;
                    case "location":
                        platform.setLocationId(object.asLiteral().getString());
                        break;
                    case "clientId":
                        platform.setClientId(object.asResource().toString());
                        break;
                    case "username":
                        platform.setUsername(object.asLiteral().getString());
                        break;
                    case "timeCreated":
                        platform.setTimeCreated(object.asLiteral().getLong());
                        break;
                    case "encryptedPassword":
                        platform.setEncryptedPassword(object.asLiteral().getString());
                        break;
                    case "encryptionAlgorithm":
                        platform.setEncryptionAlgorithm(object.asLiteral().getString());
                        break;
                    case "downstreamInputAlignmentName":
                        platform.setDownstreamInputAlignmentName(object.asLiteral().getString());
                        break;
                    case "downstreamInputAlignmentVersion":
                        platform.setDownstreamInputAlignmentVersion(object.asLiteral().getString());
                        break;
                    case "downstreamOutputAlignmentName":
                        platform.setDownstreamOutputAlignmentName(object.asLiteral().getString());
                        break;
                    case "downstreamOutputAlignmentVersion":
                        platform.setDownstreamOutputAlignmentVersion(object.asLiteral().getString());
                        break;
                    case "upstreamInputAlignmentName":
                        platform.setUpstreamInputAlignmentName(object.asLiteral().getString());
                        break;
                    case "upstreamInputAlignmentVersion":
                        platform.setUpstreamInputAlignmentVersion(object.asLiteral().getString());
                        break;
                    case "upstreamOutputAlignmentName":
                        platform.setUpstreamOutputAlignmentName(object.asLiteral().getString());
                        break;
                    case "upstreamOutputAlignmentVersion":
                        platform.setUpstreamOutputAlignmentVersion(object.asLiteral().getString());
                        break;
                }
            }
        }
        return new ArrayList<>(platformMap.values());
    }

    // RDF graph insert

    private String getPlatformsQuery(List<String> platformIds) {
        // TODO template in rq file

        StringBuilder platformGraphs = new StringBuilder();

        Iterator<String> iterator = platformIds.iterator();
        while (iterator.hasNext()) {
            String platformId = iterator.next();
            platformGraphs.append("{ GRAPH <").append(platformId).append("> { ?s ?p ?o } }");
            if (iterator.hasNext()) {
                platformGraphs.append(" UNION ");
            }
        }

        return "PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#>\n" +
                "PREFIX fn: <http://www.w3.org/2005/xpath-functions#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX par: <http://parliament.semwebcentral.org/parliament#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX iiotex: <http://inter-iot.eu/GOIoTPex#>\n" +
                "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX iiot: <http://inter-iot.eu/GOIoTP#>\n" +
                "PREFIX InterIoT: <http://inter-iot.eu/>\n" +
                "PREFIX ssn: <http://www.w3.org/ns/ssn/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "\n" +
                "\n" +
                "CONSTRUCT {?s ?p ?o}\n{" +
                platformGraphs.toString() +
                "}";

    }

    private String buildSparqlRemovePlatformId(String platformId) {
        return "DELETE WHERE {\n" +
                "    GRAPH <" + PLATFORM_GRAPH + "> {\n" +
                "         <" + platformId + "> ?p ?o\n" +
                "    }\n" +
                "};";
    }

    private String buildDeviceDiscoverySparqlInsert(String platformId, Message message) {
        StringWriter sw = new StringWriter();

        Map<String, String> nsPrefixMap = message.getPrefixes();

        for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                sw.append("PREFIX ").append(entry.getKey()).append(": <").append(entry.getValue()).append(">").append("\n");
            }
        }

        GraphExtract graphExtract = new GraphExtract(TripleBoundary.stopNowhere);
        ResIterator subjectIterator = message.getPayload().getJenaModel().listSubjects();

        while (subjectIterator.hasNext()) {
            // This iterates through all subjects in the device discovery graph response
            Resource next = subjectIterator.next();
            if (!next.isAnon()) {
                Graph extract = graphExtract.extract(next.asNode(), message.getPayload().getJenaModel().getGraph());

                Model extractModel = ModelFactory.createModelForGraph(extract);
                sw.append("INSERT DATA\n").append("{\n").append("\tGRAPH <").append(next.getURI()).append("> {\n");
                RDFDataMgr.write(sw, extractModel, Lang.TTL);
                sw.append("\t}\n};\n\n");

                sw.append("INSERT DATA \n").append("{\n")
                        .append("\tGRAPH <").append("http://inter-iot.eu/devices").append("> {\n")
                        .append("<").append(next.getURI()).append(">").append(" sosa:isHostedBy ").append(" <").append(platformId).append(">;\n")
                        .append("}")
                        .append("};\n\n");
            }
        }
        return sw.toString();
    }

    private String getRefInsert(String graph, String refId) {
        StringWriter sw = new StringWriter();
        sw.append(PREFIXES).append("\n\n");
        sw.append("INSERT DATA\n{")
                .append("GRAPH <").append(graph).append(">").append("{\n")
                .append("<").append(refId).append(">").append(" rdf:type <").append(refId).append(">\n")
                .append("}")
                .append("}");

        return sw.toString();
    }

    private ParameterizedSparqlString getPSSfromTemplate(String templateResourceName) throws MiddlewareException {
        return getPSSfromTemplate(templateResourceName, null);
    }

    private ParameterizedSparqlString getPSSfromTemplate(String templateResourceName, @Nullable Map<String, String> replacements) throws MiddlewareException {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        URL url = Resources.getResource(templateResourceName);
        String template = null;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }

        if (replacements != null) {
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                template = template.replace(replacement.getKey(), replacement.getValue());
            }
        }

        pss.setCommandText(template);
        return pss;
    }

    private ParameterizedSparqlString getPSSDeviceByIds(List<String> deviceIds) throws MiddlewareException {
        ParameterizedSparqlString pss = getPSSfromTemplate("experimental/device-getByIds.rq");

        StringBuilder sb = new StringBuilder();

        Iterator<String> platformDeviceGraphIteratior = deviceIds.iterator();

        while (platformDeviceGraphIteratior.hasNext()) {
            String deviceGraphId = platformDeviceGraphIteratior.next();
            sb.append("{ GRAPH <").append(deviceGraphId).append("> { ?a ?b ?c } }");
            if (platformDeviceGraphIteratior.hasNext()) {
                sb.append(" UNION ");
            }
        }

        String commandText = pss.getCommandText();
        String deviceGraphUnion = sb.toString();
        pss.setCommandText(commandText.replace("{device_graph_union}", deviceGraphUnion));
        return pss;
    }
}
