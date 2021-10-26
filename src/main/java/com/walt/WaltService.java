package com.walt;

import com.walt.exceptions.CustomerDoesntExistException;
import com.walt.exceptions.DifferentCityException;
import com.walt.exceptions.NoAvailableDriverException;
import com.walt.model.*;

import java.util.Date;
import java.util.List;

public  interface WaltService{

    Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws NoAvailableDriverException, CustomerDoesntExistException, DifferentCityException;

    List<DriverDistance> getDriverRankReport();

    List<DriverDistance> getDriverRankReportByCity(City city);
}

