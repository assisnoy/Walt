package com.walt;

import com.walt.dao.*;
import com.walt.exceptions.CustomerDoesntExistException;
import com.walt.exceptions.DifferentCityException;
import com.walt.exceptions.NoAvailableDriverException;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    RestaurantRepository restaurantRepository;
    @Autowired
    CityRepository cityRepository;

    // ************************************* createOrderAndAssignDriver ***************************************
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws NoAvailableDriverException, CustomerDoesntExistException, DifferentCityException {
        if (customer == null || customer.getName() == null || customerRepository.findByName(customer.getName()) == null){
            throw new CustomerDoesntExistException();
        }
        if (!customer.getCity().getName().equals(restaurant.getCity().getName())){
            throw new DifferentCityException();
        }
        Driver driver = findDriver(restaurant.getCity(), deliveryTime);
        if (driver == null){
            throw new NoAvailableDriverException();
        }
        Delivery delivery = new Delivery(driver, restaurant, customer, deliveryTime);
        delivery.setDistance(getRandomDistance());
        deliveryRepository.save(delivery);
        return delivery;
    }

    private Driver findDriver(City city, Date deliveryTime) {
        List<Driver> availableDrivers = findAvailableDrivers(city, deliveryTime);
        return findLeastBusyDriver(availableDrivers);
    }

    private List<Driver> findAvailableDrivers(City city, Date deliveryTime) {
        List <Driver> availableDrivers = new java.util.ArrayList<>(Collections.emptyList());
        List<Driver> driversInCity = driverRepository.findAllDriversByCity(city);
        for (Driver driver: driversInCity){
            if (deliveryRepository.findFirstByDriverAndDeliveryTime(driver, deliveryTime) == null){
                availableDrivers.add(driver);
            }
        }
        return availableDrivers;
    }

    private Driver findLeastBusyDriver(List<Driver> availableDrivers) {
        if (availableDrivers.isEmpty()){
            return null;
        }
        Driver leastBusyDriver = availableDrivers.get(0);
        if (availableDrivers.size() == 1){
            return leastBusyDriver;
        }
        // if there is more than one available driver in the city, choosing the one who's the least busy
        int leastBusyDriverAmountOfDeliveries = Integer.MAX_VALUE;
        for (Driver driver: availableDrivers){
            int amountOfDeliveries = getDriverAmountOfDeliveries(driver);
            if (amountOfDeliveries < leastBusyDriverAmountOfDeliveries){
                leastBusyDriver = driver;
                leastBusyDriverAmountOfDeliveries = amountOfDeliveries;
            }
        }
        return leastBusyDriver;
    }

    private int getDriverAmountOfDeliveries(Driver driver) {
        return deliveryRepository.findAllDeliveriesByDriver(driver).size();
    }

    private double getRandomDistance() {
        // distance is a random number between 0-20 Km
        return Math.random() * 21;
    }

    // ************************************* getDriverRankReport ***************************************
    @Override
    public List<DriverDistance> getDriverRankReport() {
        List<DriverDistance> driverDistanceList = new java.util.ArrayList<>(Collections.emptyList());
        List<Driver> drivers = (List<Driver>) driverRepository.findAll();
        List<Delivery> deliveries;
        double totalDistance;
        for (Driver driver: drivers){
            deliveries = deliveryRepository.findAllDeliveriesByDriver(driver);
            totalDistance = 0;
            for (Delivery delivery: deliveries){
                totalDistance+=delivery.getDistance();
            }
            DriverDistance driverDistance = new DriverDistanceImpl(driver, (long) totalDistance);
            driverDistanceList.add(driverDistance);
        }
        return driverDistanceList;
    }

    // ************************************* getDriverRankReportByCity ***************************************
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        List<DriverDistance> driverDistanceList = new java.util.ArrayList<>(Collections.emptyList());
        List<Driver> drivers = driverRepository.findAllDriversByCity(city);
        List<Delivery> deliveries;
        double totalDistance;
        for (Driver driver: drivers){
            deliveries = deliveryRepository.findAllDeliveriesByDriver_CityAndDriver(city, driver);
            totalDistance = 0;
            for (Delivery delivery: deliveries){
                totalDistance+=delivery.getDistance();
            }
            DriverDistance driverDistance = new DriverDistanceImpl(driver, (long) totalDistance);
            driverDistanceList.add(driverDistance);
        }
        return driverDistanceList;
    }
}
