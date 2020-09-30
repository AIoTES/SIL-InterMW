package eu.interiot.intermw.api.model;

import eu.interiot.intermw.commons.model.ActuationResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Gasper Vrhovsek
 */
@ApiModel(value = "ActuationInput", description = "Actuation POST request payload.")
public class ActuationInput implements Serializable {
    private String actuatorId;
    private String actuatorLocalId;
    private Set<ActuationResult> actuationResultSet;

    @ApiModelProperty(value = "Actuator ID, example: http://inter-iot.eu/light/L01", dataType = "String", required = true)
    public String getActuatorId() {
        return actuatorId;
    }

    public void setActuatorId(String actuatorId) {
        this.actuatorId = actuatorId;
    }

    @ApiModelProperty(value = "Actuator local ID. This is not an unique identifier, but rather an identifier inside of a platform example: lights", dataType = "String", required = false)
    public String getActuatorLocalId() {
        return actuatorLocalId;
    }

    public void setActuatorLocalId(String actuatorLocalId) {
        this.actuatorLocalId = actuatorLocalId;
    }

    @ApiModelProperty(value = "Actuation results, consist of actuation value and actuation unit", required = true)
    public Set<ActuationResult> getActuationResultSet() {
        return actuationResultSet;
    }

    public void setActuationResultSet(Set<ActuationResult> actuationResultSet) {
        this.actuationResultSet = actuationResultSet;
    }
}
