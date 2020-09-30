package eu.interiot.intermw.commons.responses;

import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.extractors.IoTDeviceExtractor;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.GOIoTPPayload;
import eu.interiot.message.payload.types.IoTDevicePayload;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * @author Gasper Vrhovsek
 */
public class DeviceRemoveRes {

    private IoTDevice device;

    public DeviceRemoveRes() {
    }

    public DeviceRemoveRes(Message message) {
        GOIoTPPayload payload = message.getPayloadAsGOIoTPPayload();

        IoTDevicePayload ioTDevicePayload = payload.asIoTDevicePayload();
        List<IoTDevice> ioTDevices = IoTDeviceExtractor.fromIoTDevicePayload(ioTDevicePayload);

        if (ioTDevices.isEmpty()) {
            throw new IllegalArgumentException(URIManagerMessageMetadata.MessageTypesEnum.DEVICE_REMOVE.name() +
                    " message must contain one IoTDevice");
        }
        if (ioTDevices.size() > 1) {
            throw new IllegalArgumentException(URIManagerMessageMetadata.MessageTypesEnum.DEVICE_REMOVE.name() +
                    " message can only contain one IoTDevice");
        }

        device = ioTDevices.get(0);

    }

    public IoTDevice getDevice() {
        return device;
    }

    public void setDevice(IoTDevice device) {
        this.device = device;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof DeviceRemoveRes)) return false;

        DeviceRemoveRes that = (DeviceRemoveRes) o;

        return new EqualsBuilder()
                .append(device, that.device)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(device)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("device", device)
                .toString();
    }
}
