package eu.interiot.intermw.commons.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: gasper
 * On: 20.6.2018
 */
@ApiModel(value = "ActuationResult", description = "Object containing instructions for a single actuation. It contains" +
        "information about the value and units to send to which actuatableProperty with optional result time.")
public class ActuationResult implements Serializable {
    private String actuatableProperty;
    private String value;
    private String unit;
    private Date resultTime;

    public ActuationResult(String actuatableProperty, String value, String unit, Date resultTime) {
        this.actuatableProperty = actuatableProperty;
        this.value = value;
        this.unit = unit;
        this.resultTime = resultTime;
    }

    public ActuationResult(String actuatableProperty, String value, String unit) {
        this(actuatableProperty, value, unit, null);
    }

    public ActuationResult() {
    }

    @ApiModelProperty(value = "Actuatable property ID, example: http://inter-iot.eu/light/L01/brightness", dataType = "String", required = true)
    public String getActuatableProperty() {
        return actuatableProperty;
    }

    public void setActuatableProperty(String actuatableProperty) {
        this.actuatableProperty = actuatableProperty;
    }

    @ApiModelProperty(value = "Actuation value. This value together with unit defines the actuation. Example: 10", dataType = "String", required = true)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ApiModelProperty(value = "Actuation unit. Has to be subclass of sweet_units:Unit. See https://docs.inter-iot.eu/ontology/1.1/#sweet_units-Unit", dataType = "String", required = true)
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @ApiModelProperty(value = "Result time. Suggests when the actuation result should take time.", dataType = "Date", required = false)
    public Date getResultTime() {
        return resultTime;
    }

    public void setResultTime(Date resultTime) {
        this.resultTime = resultTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ActuationResult that = (ActuationResult) o;

        return new EqualsBuilder()
                .append(actuatableProperty, that.actuatableProperty)
                .append(value, that.value)
                .append(unit, that.unit)
                .append(resultTime, that.resultTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(actuatableProperty)
                .append(value)
                .append(unit)
                .append(resultTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("actuatableProperty", actuatableProperty)
                .append("value", value)
                .append("unit", unit)
                .append("resultTime", resultTime)
                .toString();
    }
}
