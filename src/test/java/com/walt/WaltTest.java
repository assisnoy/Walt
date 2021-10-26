package com.walt;

import com.walt.dao.*;
import com.walt.exceptions.CustomerDoesntExistException;
import com.walt.exceptions.DifferentCityException;
import com.walt.exceptions.NoAvailableDriverException;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");
        City eilat = new City("Eilat");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);
        cityRepository.save(eilat);

        createDrivers(jerusalem, tlv, bash, haifa);
        createCustomers(jerusalem, tlv, haifa, eilat);
        createRestaurant(jerusalem, tlv, haifa, eilat);
    }

    private void createRestaurant(City jerusalem, City tlv, City haifa, City eilat) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", haifa, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", haifa, "chinese restaurant");
        Restaurant mexican = new Restaurant("mexican", tlv, "mexican restaurant ");
        Restaurant buffet = new Restaurant("buffet", eilat, "buffet restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican, buffet));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa, City eilat) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer jane = new Customer("Jane", haifa, "Doe");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");
        Customer adele = new Customer("Adele", eilat, "Haktovet");
        Customer katy = new Customer("Katy", tlv, "Hazikukim");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, jane, rachmaninoff, bach, adele, katy));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);
        Driver dan = new Driver("Dan", tlv);
        Driver avigdor = new Driver("Avigdor", tlv);
        Driver eliezer = new Driver("Eliezer", tlv);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata, dan, avigdor, eliezer));
    }

    // *********************************************** basic tests *************************************************
    @Test
    public void testBasics(){
        Assertions.assertEquals(((List<City>) cityRepository.findAll()).size(),5);
        Assertions.assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    // ************************************* createOrderAndAssignDriver tests ***************************************
    @Test
    public void nullCustomer(){
        Restaurant restaurant = ((List<Restaurant>) restaurantRepository.findAll()).get(0);
        Assertions.assertThrows(CustomerDoesntExistException.class,
                ()->{waltService.createOrderAndAssignDriver(null, restaurant, new Date());} );
    }

    @Test
    public void emptyCustomerDoesntExist(){
        Customer customer = new Customer();
        Restaurant restaurant = ((List<Restaurant>) restaurantRepository.findAll()).get(0);
        Assertions.assertThrows(CustomerDoesntExistException.class,
                ()->{waltService.createOrderAndAssignDriver(customer, restaurant, new Date());} );
    }

    @Test
    public void customerDoesntExist(){
        Restaurant restaurant = ((List<Restaurant>) restaurantRepository.findAll()).get(0);
        City city = restaurant.getCity();
        Customer customer = new Customer("Noy Asis", city, "address");
        Assertions.assertThrows(CustomerDoesntExistException.class,
                ()->{waltService.createOrderAndAssignDriver(customer, restaurant, new Date());} );
    }

    @Test
    public void differentCities(){
        Customer customer = customerRepository.findByName("Chopin"); // lives in haifa
        Restaurant restaurant = ((List<Restaurant>) restaurantRepository.findAll()).get(0); // in jerusalem or tlv
        Assertions.assertThrows(DifferentCityException.class,
                ()->{waltService.createOrderAndAssignDriver(customer, restaurant, new Date());} );
    }

    @Test
    public void noDriverInCity(){
        Customer customer = customerRepository.findByName("Adele"); // lives in eilat
        Restaurant restaurant = restaurantRepository.findByName("buffet"); // in eilat
        Assertions.assertThrows(NoAvailableDriverException.class,
                ()->{waltService.createOrderAndAssignDriver(customer, restaurant, new Date());} );
    }

    @Test
    public void multipleAvailableDrivers() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        Customer customer = customerRepository.findByName("Bach"); // lives in tlv
        Restaurant restaurant = restaurantRepository.findByName("cafe"); // in tlv
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, new Date());
        Assertions.assertEquals(customer, delivery.getCustomer());
        Assertions.assertEquals(restaurant, delivery.getRestaurant());
        Assertions.assertEquals(customer.getCity().getId(), delivery.getDriver().getCity().getId());
    }

    @Test
    public void noAvailableDriverInCity() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        City city = new City("city");
        Customer customer = new Customer("Noy Asis", city, "address");
        Customer customer2 = new Customer("customer2", city, "address");
        Restaurant restaurant = new Restaurant("resturant", city, "address");
        Driver driver = new Driver("driver", city);
        cityRepository.save(city);
        customerRepository.saveAll(Lists.newArrayList(customer, customer2));
        restaurantRepository.save(restaurant);
        driverRepository.save(driver);

        Date date = new Date();
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);
        Assertions.assertEquals(customer, delivery.getCustomer());
        Assertions.assertEquals(restaurant, delivery.getRestaurant());
        Assertions.assertEquals(customer.getCity().getId(), delivery.getDriver().getCity().getId());
        // there are two deliveries in the same the city at the same time and there's only one driver in that city
        Assertions.assertThrows(NoAvailableDriverException.class,
                ()->{waltService.createOrderAndAssignDriver(customer2, restaurant, date);} );
    }

    // ************************************* getDriverRankReport Tests ***************************************
    @Test
    public void getSpecificDriverRankReport() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        City ashkelon = new City("Ashkelon");
        Customer beyonce = new Customer("Beyonce", ashkelon, "to the left");
        Restaurant hagril = new Restaurant("Hagril", ashkelon, "al");
        Driver jayZ = new Driver("jayZ", ashkelon);
        cityRepository.save(ashkelon);
        customerRepository.save(beyonce);
        restaurantRepository.save(hagril);
        driverRepository.save(jayZ);
        Delivery delivery = waltService.createOrderAndAssignDriver(beyonce, hagril, new Date());
        List<DriverDistance> driverDistanceList = waltService.getDriverRankReport();
        for (DriverDistance driverDistance: driverDistanceList){
            if (driverDistance.getDriver().getId().equals(delivery.getDriver().getId())){
                Assertions.assertEquals((long) delivery.getDistance(), driverDistance.getTotalDistance());
            }
        }
    }

    @Test
    public void getAllDriversRankReport() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        Date date = new Date();
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Mozart"), restaurantRepository.findByName("meat"), date);
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Chopin"), restaurantRepository.findByName("vegan"), date);
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Jane"), restaurantRepository.findByName("chinese"), date);

        List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();
        long deliveriesTotalDistance = 0;
        for (Delivery delivery: deliveries){
            deliveriesTotalDistance += delivery.getDistance();
        }

        List<DriverDistance> driverDistanceList = waltService.getDriverRankReport();
        long driversTotalDistance = 0;
        for (DriverDistance driverDistance: driverDistanceList){
            driversTotalDistance += driverDistance.getTotalDistance();
        }

        Assertions.assertEquals(deliveriesTotalDistance, driversTotalDistance);
    }

    // ************************************* getDriverRankReportByCity Tests ***************************************
    @Test
    public void getSpecificDriverRankReportByCity() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        City nyc = new City("NYC");
        Customer rihanna = new Customer("Rihanna", nyc, "to the left");
        Restaurant hamakom = new Restaurant("Hamakom", nyc, "sham");
        Driver drake = new Driver("drake", nyc);
        cityRepository.save(nyc);
        customerRepository.save(rihanna);
        restaurantRepository.save(hamakom);
        driverRepository.save(drake);
        Delivery delivery = waltService.createOrderAndAssignDriver(rihanna, hamakom, new Date());
        List<DriverDistance> driverDistanceList = waltService.getDriverRankReportByCity(nyc);
        for (DriverDistance driverDistance: driverDistanceList){
            if (driverDistance.getDriver().getId().equals(delivery.getDriver().getId())){
                Assertions.assertEquals((long) delivery.getDistance(), driverDistance.getTotalDistance());
            }
        }
    }

    @Test
    public void getAllDriversRankReportByCity() throws DifferentCityException, NoAvailableDriverException, CustomerDoesntExistException {
        Date date = new Date();
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"), restaurantRepository.findByName("cafe"), date);
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Rachmaninoff"), restaurantRepository.findByName("cafe"), date);
        waltService.createOrderAndAssignDriver(customerRepository.findByName("Katy"), restaurantRepository.findByName("cafe"), date);

        List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();
        long deliveriesTotalDistance = 0;
        for (Delivery delivery: deliveries){
            deliveriesTotalDistance += delivery.getDistance();
        }

        List<DriverDistance> driverDistanceList = waltService.getDriverRankReportByCity(cityRepository.findByName("Tel-Aviv"));
        long driversTotalDistance = 0;
        for (DriverDistance driverDistance: driverDistanceList){
            driversTotalDistance += driverDistance.getTotalDistance();
        }

        Assertions.assertEquals(deliveriesTotalDistance, driversTotalDistance);
    }

}
