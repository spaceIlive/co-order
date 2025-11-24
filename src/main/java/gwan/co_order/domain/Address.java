package gwan.co_order.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Address {
    private String address;
    private double latitude;
    private double longitude;

    protected Address() {
    }

    public Address(String address, double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도 범위 초과");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도 범위 초과");
        }
    }
}

