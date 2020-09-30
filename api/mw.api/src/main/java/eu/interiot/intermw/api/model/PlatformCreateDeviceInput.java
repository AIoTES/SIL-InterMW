package eu.interiot.intermw.api.model;

import eu.interiot.intermw.commons.model.IoTDevice;

import java.util.ArrayList;
import java.util.List;

public class PlatformCreateDeviceInput {
    private List<IoTDevice> devices;

    public PlatformCreateDeviceInput() {
        devices = new ArrayList<>();
    }

    public PlatformCreateDeviceInput(List<IoTDevice> devices) {
        this.devices = devices;
    }

    public List<IoTDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<IoTDevice> devices) {
        this.devices = devices;
    }

    public void addDevice(IoTDevice device) {
        this.devices.add(device);
    }
}
