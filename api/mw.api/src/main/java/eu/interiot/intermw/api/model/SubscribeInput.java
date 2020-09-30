package eu.interiot.intermw.api.model;

import java.util.ArrayList;
import java.util.List;

public class SubscribeInput {
    private List<String> deviceIds;

    public SubscribeInput() {
        deviceIds = new ArrayList<>();
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public void addIoTDevice(String deviceId) {
        this.deviceIds.add(deviceId);
    }
}
