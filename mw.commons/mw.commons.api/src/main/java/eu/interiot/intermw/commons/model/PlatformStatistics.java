package eu.interiot.intermw.commons.model;

public class PlatformStatistics {

    private int deviceCount;
    private int subscribedDeviceCount;
    private int subscriptionCount;

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getSubscribedDeviceCount() {
        return subscribedDeviceCount;
    }

    public void setSubscribedDeviceCount(int subscribedDeviceCount) {
        this.subscribedDeviceCount = subscribedDeviceCount;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }

}
