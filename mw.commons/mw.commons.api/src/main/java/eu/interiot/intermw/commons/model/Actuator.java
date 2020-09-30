package eu.interiot.intermw.commons.model;

/**
 * Author: gasper
 * On: 5.6.2018
 */
public class Actuator {

    private String actuatorId;

    private String implementsProcedure;
    private String isHostedBy;
    private String hasSubsystem;
    private String madeActuation;
    private String forProperty;
    private String hasDeployment;

    public Actuator() {
    }

    public Actuator(String actuatorId) {
        this.actuatorId = actuatorId;
    }

    public String getActuatorId() {
        return actuatorId;
    }

    public void setActuatorId(String actuatorId) {
        this.actuatorId = actuatorId;
    }

    public String getImplementsProcedure() {
        return implementsProcedure;
    }

    public void setImplementsProcedure(String implementsProcedure) {
        this.implementsProcedure = implementsProcedure;
    }

    public String getIsHostedBy() {
        return isHostedBy;
    }

    public void setIsHostedBy(String isHostedBy) {
        this.isHostedBy = isHostedBy;
    }

    public String getHasSubsystem() {
        return hasSubsystem;
    }

    public void setHasSubsystem(String hasSubsystem) {
        this.hasSubsystem = hasSubsystem;
    }

    public String getMadeActuation() {
        return madeActuation;
    }

    public void setMadeActuation(String madeActuation) {
        this.madeActuation = madeActuation;
    }

    public String getForProperty() {
        return forProperty;
    }

    public void setForProperty(String forProperty) {
        this.forProperty = forProperty;
    }

    public String getHasDeployment() {
        return hasDeployment;
    }

    public void setHasDeployment(String hasDeployment) {
        this.hasDeployment = hasDeployment;
    }
}
