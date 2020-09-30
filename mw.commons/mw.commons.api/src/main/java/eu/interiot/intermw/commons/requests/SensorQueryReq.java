package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;

import java.util.List;

import static eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;

/**
 * Author: gasper
 * On: 17.10.2018
 */
public class SensorQueryReq {
    private final String clientId;
    private final String platformId;
    private List<IoTDevice> sensors;

    public SensorQueryReq(List<IoTDevice> sensors, String clientId, String platformId) {
        this.sensors = sensors;
        this.clientId = clientId;
        this.platformId = platformId;
    }

    public Message toMessage() {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(MessageTypesEnum.QUERY);
        metadata.setClientId(clientId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));

        IoTDevicePayload payload = new IoTDevicePayload();
        for (IoTDevice sensor : sensors) {
            payload.createIoTDevice(new EntityID(sensor.getDeviceId()));
        }

        message.setPayload(payload);
        return message;
    }

    public List<IoTDevice> getSensors() {
        return sensors;
    }

    public void setSensors(List<IoTDevice> sensors) {
        this.sensors = sensors;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPlatformId() {
        return platformId;
    }
}
