package eu.interiot.intermw.commons.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: gasper
 * On: 28.5.2018
 */
public class Actuation {

    private String clientId;
    private String platformId;

    private String madeByActuator;
    private String madeByActuatorLocalId;

    private String deviceId;

    private Set<ActuationResult> actuationResults;

    public Actuation() {
        this.actuationResults = new HashSet<>();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getMadeByActuator() {
        return madeByActuator;
    }

    public void setMadeByActuator(String madeByActuator) {
        this.madeByActuator = madeByActuator;
    }

    public String getMadeByActuatorLocalId() {
        return madeByActuatorLocalId;
    }

    public void setMadeByActuatorLocalId(String madeByActuatorLocalId) {
        this.madeByActuatorLocalId = madeByActuatorLocalId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Set<ActuationResult> getActuationResults() {
        return actuationResults;
    }

    public void setActuationResults(Set<ActuationResult> actuationResults) {
        this.actuationResults = actuationResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Actuation actuation = (Actuation) o;

        return new EqualsBuilder()
                .append(clientId, actuation.clientId)
                .append(platformId, actuation.platformId)
                .append(madeByActuator, actuation.madeByActuator)
                .append(madeByActuatorLocalId, actuation.madeByActuatorLocalId)
                .append(deviceId, actuation.deviceId)
                .append(actuationResults, actuation.actuationResults)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clientId)
                .append(platformId)
                .append(madeByActuator)
                .append(madeByActuatorLocalId)
                .append(deviceId)
                .append(actuationResults)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientId", clientId)
                .append("platformId", platformId)
                .append("madeByActuator", madeByActuator)
                .append("madeByActuatorLocalId", madeByActuatorLocalId)
                .append("deviceId", deviceId)
                .append("actuationResults", actuationResults)
                .toString();
    }
}
