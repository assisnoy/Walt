package com.walt.model;

public class DriverDistanceImpl implements DriverDistance{
    Driver driver;
    Long distance;

    public DriverDistanceImpl(Driver driver, Long totalDistance) {
        this.driver = driver;
        this.distance = totalDistance;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public Long getTotalDistance() {
        return distance;
    }
}
