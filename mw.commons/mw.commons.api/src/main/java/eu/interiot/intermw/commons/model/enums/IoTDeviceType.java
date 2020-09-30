package eu.interiot.intermw.commons.model.enums;

/**
 * @author Gasper Vrhovsek
 */
public enum IoTDeviceType {
    DEVICE("http://inter-iot.eu/GOIoTP#IoTDevice"),
    SENSOR("http://www.w3.org/ns.sosa/Sensor"),
    ACTUATOR("http://www.w3.org/ns.sosa/Actuator");

    private final String deviceTypeUri;

    IoTDeviceType(String deviceTypeUri) {
        this.deviceTypeUri = deviceTypeUri;
    }

    public String getDeviceTypeUri() {
        return this.deviceTypeUri;
    }

    public static IoTDeviceType fromDeviceTypeUri(String uri) {

        for(IoTDeviceType type : IoTDeviceType.values()) {
            if (type.getDeviceTypeUri().equals(uri)) {
                return type;
            }
        }

        return null;
//        throw new IllegalArgumentException("No enum constant for value " + uri);
    }

    @Override
    public String toString() {
        return this.deviceTypeUri;
    }
}
