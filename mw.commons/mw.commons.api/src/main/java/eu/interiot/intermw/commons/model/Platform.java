package eu.interiot.intermw.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URL;

public class Platform {

    private String platformId;
    private String type;
    private URL baseEndpoint;
    private String locationId;
    private String name;
    private String clientId;
    private String username;
    private long timeCreated;
    private PlatformStatistics platformStatistics;
    @JsonIgnore
    private String encryptedPassword;
    @JsonIgnore
    private String encryptionAlgorithm;

    private String downstreamInputAlignmentName;
    private String downstreamInputAlignmentVersion;
    private String downstreamOutputAlignmentName;
    private String downstreamOutputAlignmentVersion;

    private String upstreamInputAlignmentName;
    private String upstreamInputAlignmentVersion;
    private String upstreamOutputAlignmentName;
    private String upstreamOutputAlignmentVersion;

    private Location location;

    public Platform() {
        this.timeCreated = System.currentTimeMillis();
    }

    public Platform(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URL getBaseEndpoint() {
        return baseEndpoint;
    }

    public void setBaseEndpoint(URL baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getDownstreamInputAlignmentName() {
        return downstreamInputAlignmentName;
    }

    public void setDownstreamInputAlignmentName(String downstreamInputAlignmentName) {
        this.downstreamInputAlignmentName = downstreamInputAlignmentName;
    }

    public String getDownstreamInputAlignmentVersion() {
        return downstreamInputAlignmentVersion;
    }

    public void setDownstreamInputAlignmentVersion(String downstreamInputAlignmentVersion) {
        this.downstreamInputAlignmentVersion = downstreamInputAlignmentVersion;
    }

    public String getDownstreamOutputAlignmentName() {
        return downstreamOutputAlignmentName;
    }

    public void setDownstreamOutputAlignmentName(String downstreamOutputAlignmentName) {
        this.downstreamOutputAlignmentName = downstreamOutputAlignmentName;
    }

    public String getDownstreamOutputAlignmentVersion() {
        return downstreamOutputAlignmentVersion;
    }

    public void setDownstreamOutputAlignmentVersion(String downstreamOutputAlignmentVersion) {
        this.downstreamOutputAlignmentVersion = downstreamOutputAlignmentVersion;
    }

    public String getUpstreamInputAlignmentName() {
        return upstreamInputAlignmentName;
    }

    public void setUpstreamInputAlignmentName(String upstreamInputAlignmentName) {
        this.upstreamInputAlignmentName = upstreamInputAlignmentName;
    }

    public String getUpstreamInputAlignmentVersion() {
        return upstreamInputAlignmentVersion;
    }

    public void setUpstreamInputAlignmentVersion(String upstreamInputAlignmentVersion) {
        this.upstreamInputAlignmentVersion = upstreamInputAlignmentVersion;
    }

    public String getUpstreamOutputAlignmentName() {
        return upstreamOutputAlignmentName;
    }

    public void setUpstreamOutputAlignmentName(String upstreamOutputAlignmentName) {
        this.upstreamOutputAlignmentName = upstreamOutputAlignmentName;
    }

    public String getUpstreamOutputAlignmentVersion() {
        return upstreamOutputAlignmentVersion;
    }

    public void setUpstreamOutputAlignmentVersion(String upstreamOutputAlignmentVersion) {
        this.upstreamOutputAlignmentVersion = upstreamOutputAlignmentVersion;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public PlatformStatistics getPlatformStatistics() {
        return platformStatistics;
    }

    public void setPlatformStatistics(PlatformStatistics platformStatistics) {
        this.platformStatistics = platformStatistics;
    }
}
