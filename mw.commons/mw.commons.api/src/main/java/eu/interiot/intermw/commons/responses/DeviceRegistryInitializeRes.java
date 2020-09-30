package eu.interiot.intermw.commons.responses;

import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.extractors.IoTDeviceExtractor;
import eu.interiot.message.Message;
import eu.interiot.message.payload.GOIoTPPayload;
import eu.interiot.message.payload.types.IoTDevicePayload;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * @author Gasper Vrhovsek
 */
public class DeviceRegistryInitializeRes extends BaseRes {

    private List<IoTDevice> ioTDevices;

    public DeviceRegistryInitializeRes() {
    }

    public DeviceRegistryInitializeRes(Message message) {
        GOIoTPPayload payloadAsGOIoTPPayload = message.getPayloadAsGOIoTPPayload();

        IoTDevicePayload ioTDevicePayload = payloadAsGOIoTPPayload.asIoTDevicePayload();
        ioTDevices = IoTDeviceExtractor.fromIoTDevicePayload(ioTDevicePayload);
    }

    public List<IoTDevice> getIoTDevices() {
        return ioTDevices;
    }

    public void setIoTDevices(List<IoTDevice> ioTDevices) {
        this.ioTDevices = ioTDevices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof DeviceRegistryInitializeRes)) return false;

        DeviceRegistryInitializeRes that = (DeviceRegistryInitializeRes) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(ioTDevices, that.ioTDevices)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(ioTDevices)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ioTDevices", ioTDevices)
                .toString();
    }
}
