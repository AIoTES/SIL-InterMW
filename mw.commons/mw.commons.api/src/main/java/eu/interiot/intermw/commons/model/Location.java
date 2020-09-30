package eu.interiot.intermw.commons.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Gasper Vrhovsek
 */
public class Location {
    private String locationId;
    private String description;
    private Double latitude;
    private Double longitude;

    public Location() {
    }

    public Location(String locationId) {
        this.locationId = locationId;
    }

    public Location(String locationId, Double latitude, Double longitude) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String locationId, String description, Double latitude, Double longitude) {
        this.locationId = locationId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getDescription() {
        return description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("locationId", locationId)
                .append("description", description)
                .append("latitude", latitude)
                .append("longitude", longitude)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        return new EqualsBuilder()
                .append(latitude, location.latitude)
                .append(longitude, location.longitude)
                .append(locationId, location.locationId)
                .append(description, location.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(locationId)
                .append(description)
                .append(latitude)
                .append(longitude)
                .toHashCode();
    }
}
