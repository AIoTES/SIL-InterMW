package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.model.Location;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.ID.PropertyID;
import eu.interiot.message.Message;
import eu.interiot.message.managers.ID.IDManagerGOIoTP;
import eu.interiot.message.managers.ID.IDManagerGOIoTPex;
import eu.interiot.message.managers.URI.URIManagerINTERMW;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.types.SoftwarePlatformPayload;
import eu.interiot.message.utils.intermw.IPSMBridgeConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */

public class RegisterPlatformReq {
    private static final PropertyID PLATFORM_TYPE_PROP = new PropertyID(URIManagerINTERMW.PREFIX.intermw + "platformType");
    private Platform platform;
    private Location location;

    public RegisterPlatformReq(Platform platform) {
        this.platform = platform;
    }

    public RegisterPlatformReq(Platform platform, Location location) {
        this.platform = platform;
        this.location = location;
    }

    public RegisterPlatformReq(Message message) throws InvalidMessageException {
        platform = new Platform();
        platform.setClientId(message.getMetadata().getClientId().orElse(null));
        SoftwarePlatformPayload payload = message.getPayloadAsGOIoTPPayload().asSoftwarePlatformPayload();
        Iterator<EntityID> iterator = payload.getSoftwarePlatforms().iterator();
        if (!iterator.hasNext()) {
            throw new InvalidMessageException("No platform found in the message payload.");
        }
        EntityID platformEntityId = iterator.next();
        platform.setPlatformId(platformEntityId.toString());
        platform.setName(payload.getHasName(platformEntityId).orElse(null));
        URL baseEndpoint = null;
        try {
            baseEndpoint = payload.getHasBaseEndpoint(platformEntityId).isPresent() ?
                    new URL(payload.getHasBaseEndpoint(platformEntityId).get()) : null;
        } catch (MalformedURLException e) {
            throw new InvalidMessageException("Invalid baseEndpoint: " + baseEndpoint);
        }
        platform.setBaseEndpoint(baseEndpoint);
        String locationId = payload.getHasLocation(platformEntityId).isPresent() ?
                payload.getHasLocation(platformEntityId).get().toString() : null;
        platform.setLocationId(locationId);
        String platformType = payload.getAllDataPropertyAssertionsForEntityAsStrings(platformEntityId, PLATFORM_TYPE_PROP).iterator().next();
        platform.setType(platformType);

        PropertyID usernamePropID = new PropertyID(URIManagerINTERMW.PREFIX.intermw + "username");
        PropertyID encryptionAlgorithmPropID = new PropertyID(URIManagerINTERMW.PREFIX.intermw + "encryptionAlgorithm");
        PropertyID encryptedPasswordPropID = new PropertyID(URIManagerINTERMW.PREFIX.intermw + "encryptedPassword");

        Set<EntityID> userIDs = payload.getAllObjectPropertyAssertionsForEntity(platformEntityId, IDManagerGOIoTP.PROPERTIES.OBJECT.hasUser);
        if (userIDs.size() > 0) {
            EntityID userId = userIDs.iterator().next();
            Set<EntityID> authDatas = payload.getAllObjectPropertyAssertionsForEntity(userId, IDManagerGOIoTPex.PROPERTIES.OBJECT.hasAuthenticationData);
            if (authDatas.size() > 0) {
                EntityID authData = authDatas.iterator().next();
                if (!payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, usernamePropID).isEmpty()) {
                    String username = payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, usernamePropID).iterator().next();
                    platform.setUsername(username);
                }
                if (!payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, encryptedPasswordPropID).isEmpty()) {
                    String encryptedPassword = payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, encryptedPasswordPropID).iterator().next();
                    platform.setEncryptedPassword(encryptedPassword);
                }
                if (!payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, encryptionAlgorithmPropID).isEmpty()) {
                    String encryptionAlgorithm = payload.getAllDataPropertyAssertionsForEntityAsStrings(authData, encryptionAlgorithmPropID).iterator().next();
                    platform.setEncryptionAlgorithm(encryptionAlgorithm);
                }
            }
        }

        // IPSM alignments data
        IPSMBridgeConfig ipsmConfig = IPSMBridgeConfig.getFromPayload(platformEntityId, payload);

        platform.setDownstreamInputAlignmentName(ipsmConfig.downstreamInputAlignmentName.isPresent() ?
                ipsmConfig.downstreamInputAlignmentName.get() : null);
        platform.setDownstreamInputAlignmentVersion(ipsmConfig.downstreamInputAlignmentVersion.isPresent() ?
                ipsmConfig.downstreamInputAlignmentVersion.get() : null);
        platform.setDownstreamOutputAlignmentName(ipsmConfig.downstreamOutputAlignmentName.isPresent() ?
                ipsmConfig.downstreamOutputAlignmentName.get() : null);
        platform.setDownstreamOutputAlignmentVersion(ipsmConfig.downstreamOutputAlignmentVersion.isPresent() ?
                ipsmConfig.downstreamOutputAlignmentVersion.get() : null);

        platform.setUpstreamInputAlignmentName(ipsmConfig.upstreamInputAlignmentName.isPresent() ?
                ipsmConfig.upstreamInputAlignmentName.get() : null);
        platform.setUpstreamInputAlignmentVersion(ipsmConfig.upstreamInputAlignmentVersion.isPresent() ?
                ipsmConfig.upstreamInputAlignmentVersion.get() : null);
        platform.setUpstreamOutputAlignmentName(ipsmConfig.upstreamOutputAlignmentName.isPresent() ?
                ipsmConfig.upstreamOutputAlignmentName.get() : null);
        platform.setUpstreamOutputAlignmentVersion(ipsmConfig.upstreamOutputAlignmentVersion.isPresent() ?
                ipsmConfig.upstreamOutputAlignmentVersion.get() : null);

        Set<Long> timeCreatedSet = payload.getAllDataPropertyAssertionsForEntityAsLongs(platformEntityId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "created"));
        platform.setTimeCreated(timeCreatedSet.iterator().next());

        // Location data
        Set<Double> locationLatSet = payload.getAllDataPropertyAssertionsForEntityAsDoubles(platformEntityId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationLatitude"));
        Set<Double> locationLongSet = payload.getAllDataPropertyAssertionsForEntityAsDoubles(platformEntityId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationLongitude"));
        Set<String> locationDescriptionSet = payload.getAllDataPropertyAssertionsForEntityAsStrings(platformEntityId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationDescription"));

        Double latitude = null;
        if (!locationLatSet.isEmpty()) {
            latitude = locationLatSet.iterator().next();
        }
        Double longitude = null;
        if (!locationLongSet.isEmpty()) {
            longitude = locationLongSet.iterator().next();
        }
        String locationDescription = null;
        if (!locationDescriptionSet.isEmpty()) {
            locationDescription = locationDescriptionSet.iterator().next();
        }

        this.location = new Location(
                locationId,
                locationDescription,
                latitude,
                longitude
        );
    }

    public Message toMessage() {
        SoftwarePlatformPayload payload = new SoftwarePlatformPayload();
        EntityID platformId = new EntityID(platform.getPlatformId());
        payload.createSoftwarePlatform(platformId);
        payload.setHasName(platformId, platform.getName());
        payload.setHasLocation(platformId, new EntityID(platform.getLocationId()));
        payload.setHasBaseEndpoint(platformId, platform.getBaseEndpoint() != null ? platform.getBaseEndpoint().toString() : null);
        payload.setDataPropertyAssertionForEntity(platformId, PLATFORM_TYPE_PROP, platform.getType());
        payload.setDataPropertyAssertionForEntity(platformId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "created"), platform.getTimeCreated());

        if (platform.getUsername() != null) {
            EntityID userId = new EntityID();
            EntityID authDataId = new EntityID();
            payload.addTypeToEntity(platformId, IDManagerGOIoTP.TYPES.Platform);
            payload.addObjectPropertyAssertionToEntity(platformId, IDManagerGOIoTP.PROPERTIES.OBJECT.hasUser, userId);
            payload.addTypeToEntity(userId, IDManagerGOIoTP.TYPES.User);
            payload.addTypeToEntity(authDataId, IDManagerGOIoTPex.TYPES.AuthenticationData);
            payload.addObjectPropertyAssertionToEntity(userId, IDManagerGOIoTPex.PROPERTIES.OBJECT.hasAuthenticationData, authDataId);
            payload.addDataPropertyAssertionToEntity(authDataId,
                    new PropertyID(URIManagerINTERMW.PREFIX.intermw + "username"), platform.getUsername());
            payload.addDataPropertyAssertionToEntity(authDataId,
                    new PropertyID(URIManagerINTERMW.PREFIX.intermw + "encryptedPassword"), platform.getEncryptedPassword());
            payload.addDataPropertyAssertionToEntity(authDataId,
                    new PropertyID(URIManagerINTERMW.PREFIX.intermw + "encryptionAlgorithm"), platform.getEncryptionAlgorithm());
        }

        if (location != null) {
            if (location.getLatitude() != null) {
                payload.setDataPropertyAssertionForEntity(platformId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationLatitude"), location.getLatitude());
            }
            if (location.getLongitude() != null) {
                payload.setDataPropertyAssertionForEntity(platformId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationLongitude"), location.getLongitude());
            }
            if (location.getDescription() != null) {
                payload.setDataPropertyAssertionForEntity(platformId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "locationDescription"), location.getDescription());
            }
        }

        Message message = new Message();
        message.getMetadata().setMessageType(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_REGISTER);
        message.getMetadata().setClientId(platform.getClientId());
        message.getMetadata().asPlatformMessageMetadata().addReceivingPlatformID(platformId);
        message.setPayload(payload);

        EntityID authDataId = new EntityID();
        payload.addTypeToEntity(authDataId, IDManagerGOIoTPex.TYPES.AuthenticationData);
        payload.getAllDataPropertyAssertionsForEntity(authDataId, new PropertyID(URIManagerINTERMW.PREFIX.intermw + "username"));

        // IPSM alignments data
        IPSMBridgeConfig bridgeConfig = new IPSMBridgeConfig(
                platform.getDownstreamInputAlignmentName(),
                platform.getDownstreamInputAlignmentVersion(),
                platform.getDownstreamOutputAlignmentName(),
                platform.getDownstreamOutputAlignmentVersion(),
                platform.getUpstreamInputAlignmentName(),
                platform.getUpstreamInputAlignmentVersion(),
                platform.getUpstreamOutputAlignmentName(),
                platform.getUpstreamOutputAlignmentVersion()
        );
        bridgeConfig.putIntoPayload(platformId, payload);

        return message;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
