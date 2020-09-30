package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.payload.GOIoTPPayload;
import eu.interiot.message.payload.types.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Gasper Vrhovsek
 */
public class ObservationRes extends BaseRes {

    private String hasResult;
    private String featureOfInterest;
    private String observedProperty;
    private String phenomenonTime;
    private String resultTime;
    private String madeBysensor;
    private String hasLocation;

    private String ioTDeviceId;
    private List<Observation> observations;
    private List<Sensor> sensors;
    private List<FeatureOfInterest> featuresOfInterest;

    public ObservationRes() {
    }

    public ObservationRes(Message message) {
        super(message);
        GOIoTPPayload payloadAsGOIoTPPayload = message.getPayloadAsGOIoTPPayload();

        ObservationPayload observationPayload = payloadAsGOIoTPPayload.asObservationPayload();
        Set<EntityID> observationIds = observationPayload.getObservations();

        if (observationIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid message format, missing observations");
        } else if (observationIds.size() == 1) {
            // simple message
            EntityID observationId = observationIds.iterator().next();
            constructSimpleObservation(observationPayload, observationId);
        } else {
            // complex version, nested objects with local ids etc. look at example JSON-LD in https://docs2.inter-iot.eu/docs/intermw/latest/developer-guide/json-ld-messaging/#msg_observation
            constructComplexObservation(observationPayload, observationIds);
        }
    }

    private void constructComplexObservation(ObservationPayload observationPayload, Set<EntityID> observationIds) {

        observations = new ArrayList<>();
        sensors = new ArrayList<>();

        Iterator<EntityID> iterator = observationIds.iterator();
        while (iterator.hasNext()) {
            EntityID observationId = iterator.next();

            String resultTime = observationPayload.getResultTime(observationId).orElse(null);


            // RESULTS per observation
            EntityID hasResultId = observationPayload.getHasResult(observationId).orElse(null);
            ResultPayload resultPayload = observationPayload.asResultPayload();
            String hasResultValue = resultPayload.getHasResultValue(hasResultId).orElse(null);
            EntityID hasUnit = resultPayload.getHasUnit(hasResultId).orElse(null);
            Result result = new Result(hasResultValue, hasUnit);

            EntityID madeBySensor = observationPayload.getMadeBySensor(observationId).orElse(null);
            EntityID hasFeatureOfInterest = observationPayload.getHasFeatureOfInterest(observationId).orElse(null);
            EntityID sensorIsHostedBy = null;
            if (madeBySensor != null) {
                // SENSORS per observation
                SensorPayload sensorPayload = observationPayload.asSensorPayload();
                EntityID sensorHasLocation = sensorPayload.getHasLocation(madeBySensor).orElse(null);
                String sensorHasName = sensorPayload.getHasName(madeBySensor).orElse(null);
                sensorIsHostedBy = sensorPayload.getIsHostedBy(madeBySensor).orElse(null);
                sensors.add(new Sensor(sensorHasName, sensorHasLocation, sensorIsHostedBy));
            }

            observations.add(
                    new Observation(madeBySensor != null && madeBySensor.isUnique() ? madeBySensor :
                            sensorIsHostedBy,
                            resultTime,
                            hasFeatureOfInterest,
                            result));

            // DEVICE
            IoTDevicePayload ioTDevicePayload = observationPayload.asIoTDevicePayload();
            if (ioTDevicePayload.getIoTDevices().iterator().hasNext()) {
                EntityID iotDeviceId = ioTDevicePayload.getIoTDevices().iterator().next();
                this.ioTDeviceId = iotDeviceId != null ? iotDeviceId.toString() : null;
            }
        }

        FeatureOfInterestPayload featureOfInterestPayload = observationPayload.asFeatureOfInterestPayload();
        Set<EntityID> featureOfInterestIds = featureOfInterestPayload.getFeatureOfInterests();

        if (featureOfInterestIds != null && !featureOfInterestIds.isEmpty()) {

            featuresOfInterest = new ArrayList<>();

            for (EntityID featureOfInterest : featureOfInterestIds) {
                Set<EntityID> hasProperty = featureOfInterestPayload.getHasProperty(featureOfInterest);
                Set<EntityID> hasSample = featureOfInterestPayload.getHasSample(featureOfInterest);

                featuresOfInterest.add(new FeatureOfInterest(featureOfInterest.toString(), hasProperty, hasSample));
            }
        }
    }

    private void constructSimpleObservation(ObservationPayload observationPayload, EntityID observationId) {
        if (observationPayload.getHasLocation(observationId).isPresent()) {
            this.hasLocation = observationPayload.getHasLocation(observationId).get().toString();
        }
        if (observationPayload.getMadeBySensor(observationId).isPresent()) {
            this.madeBysensor = observationPayload.getMadeBySensor(observationId).get().toString();
        }
        if (observationPayload.getResultTime(observationId).isPresent()) {
            this.resultTime = observationPayload.getResultTime(observationId).orElse(null);
        }
        if (observationPayload.getHasResult(observationId).isPresent()) {
            this.hasResult = observationPayload.getHasResult(observationId).get().toString();
        }
        if (observationPayload.getHasFeatureOfInterest(observationId).isPresent()) {
            this.featureOfInterest = observationPayload.getHasFeatureOfInterest(observationId).get().toString();
        }
        if (observationPayload.getObservedProperty(observationId).isPresent()) {
            this.observedProperty = observationPayload.getObservedProperty(observationId).get().toString();
        }
        if (observationPayload.getPhenomenonTime(observationId).isPresent()) {
            this.phenomenonTime = observationPayload.getPhenomenonTime(observationId).get().toString();
        }
    }

    public String getHasResult() {
        return hasResult;
    }

    public String getFeatureOfInterest() {
        return featureOfInterest;
    }

    public String getObservedProperty() {
        return observedProperty;
    }

    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    public String getResultTime() {
        return resultTime;
    }

    public String getMadeBysensor() {
        return madeBysensor;
    }

    public String getHasLocation() {
        return hasLocation;
    }

    public String getIoTDeviceId() {
        return ioTDeviceId;
    }

    List<Observation> getObservations() {
        return observations;
    }

    List<Sensor> getSensors() {
        return sensors;
    }

    public List<FeatureOfInterest> getFeaturesOfInterest() {
        return featuresOfInterest;
    }

    static class Observation {
        private Result result;
        private String madeBySensor;
        private String resultTime;
        private String hasFeatureOfInterest;

        public Observation() {
        }

        Observation(EntityID madeBySensor, String resultTime, EntityID hasFeatureOfInterest, Result result) {
            this.madeBySensor = madeBySensor != null ? madeBySensor.toString() : null;
            this.resultTime = resultTime;
            this.hasFeatureOfInterest = hasFeatureOfInterest != null ? hasFeatureOfInterest.toString() : null;
            this.result = result;
        }

        public String getMadeBySensor() {
            return madeBySensor;
        }

        public String getResultTime() {
            return resultTime;
        }

        public String getHasFeatureOfInterest() {
            return hasFeatureOfInterest;
        }

        public Result getResult() {
            return result;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("result", result)
                    .append("madeBySensor", madeBySensor)
                    .append("resultTime", resultTime)
                    .toString();
        }
    }

    static class Result {
        private String hasResultValue;
        private String hasUnit;

        public Result() {
        }

        Result(String hasResultValue, EntityID hasUnit) {
            this.hasResultValue = hasResultValue;
            this.hasUnit = hasUnit != null ? hasUnit.toString() : null;
        }

        public String getHasResultValue() {
            return hasResultValue;
        }

        public String getHasUnit() {
            return hasUnit;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("hasResultValue", hasResultValue)
                    .append("hasUnit", hasUnit)
                    .toString();
        }
    }

    static class Sensor {
        private String hasName;
        private String hasLocation;
        private String hostedBy;

        public Sensor() {
        }

        public Sensor(String hasName, EntityID hasLocation, EntityID hostedBy) {
            this.hasName = hasName;
            this.hasLocation = hasLocation != null ? hasLocation.toString() : null;
            this.hostedBy = hostedBy != null ? hostedBy.toString() : null;
        }

        public String getHasName() {
            return hasName;
        }

        public String getHasLocation() {
            return hasLocation;
        }

        public String getHostedBy() {
            return hostedBy;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("hasName", hasName)
                    .append("hasLocation", hasLocation)
                    .append("hostedBy", hostedBy)
                    .toString();
        }
    }

    static class FeatureOfInterest {
        // TODO after alignments are finished on vmbrk01
        private String id;
        private Set<EntityID> hasProperty;
        private Set<EntityID> hasSample;

        public FeatureOfInterest() {
        }

        public FeatureOfInterest(String id, Set<EntityID> hasProperty, Set<EntityID> hasSample) {
            this.id = id;
            this.hasProperty = hasProperty;
            this.hasSample = hasSample;
        }

        public String getId() {
            return id;
        }

        public Set<EntityID> getHasProperty() {
            return hasProperty;
        }

        public Set<EntityID> getHasSample() {
            return hasSample;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("hasProperty", hasProperty)
                    .append("hasSample", hasSample)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("hasResult", hasResult)
                .append("featureOfInterest", featureOfInterest)
                .append("observedProperty", observedProperty)
                .append("phenomenonTime", phenomenonTime)
                .append("resultTime", resultTime)
                .append("madeBysensor", madeBysensor)
                .append("hasLocation", hasLocation)
                .append("ioTDeviceId", ioTDeviceId)
                .append("observations", observations)
                .append("sensors", sensors)
                .toString();
    }

    public static ObservationRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, ObservationRes.class);
    }
}
