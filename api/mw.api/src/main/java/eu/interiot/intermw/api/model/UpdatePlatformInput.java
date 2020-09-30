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
package eu.interiot.intermw.api.model;

public class UpdatePlatformInput {

    private String baseEndpoint;
    private String location;
    private String name;
    private String username;
    private String encryptedPassword;
    private String encryptionAlgorithm;

    private String downstreamInputAlignmentName;
    private String downstreamInputAlignmentVersion;
    private String downstreamOutputAlignmentName;
    private String downstreamOutputAlignmentVersion;

    private String upstreamInputAlignmentName;
    private String upstreamInputAlignmentVersion;
    private String upstreamOutputAlignmentName;
    private String upstreamOutputAlignmentVersion;


    public UpdatePlatformInput() {
    }

    public String getBaseEndpoint() {
        return baseEndpoint;
    }

    public void setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
