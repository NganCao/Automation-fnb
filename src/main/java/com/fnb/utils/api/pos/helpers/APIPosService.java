package com.fnb.utils.api.pos.helpers;

import com.fnb.utils.api.JsonAPIReader;
import com.fnb.utils.api.admin.helpers.APIAminService;
import com.fnb.utils.helpers.Helper;
import com.fnb.utils.api.JsonAPIReader.ConfigAPIObject;
import com.fnb.utils.api.pos.helpers.JsonAPIPosReader.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class APIPosService {
    private WebDriver driver;
    private String platform;
    private static String url;
    private static String apiURL;
    private static String userName;
    private static String password;
    private static String storeId;
    private static String accountId;
    private static String theme;
    private static String path = "src/main/java/com/fnb/utils/api/";
    private static String typeAPI = "pos";
    public static ConfigAPIObject configAPIObject;
    public static JsonAPIReader jsonAPIReader;
    public static JsonAPIPosReader jsonAPIPosReader;
    public static APIAminService apiAminService;
    private static Helper helper;

    public APIPosService(WebDriver driver, String platform, String theme, String url) {
        this.driver = driver;
        this.platform = platform;
        this.theme = theme;
        this.url = url;
        helper = new Helper(driver);
        getApiURL();
        getStoreConfigJson();
    }

    public static String getFilePath(String fileName) {
        return typeAPI + "/jsonSchema/" + theme + "/" + fileName;
    }

    /**
     * run to prepare data from apiConfig file
     */
    private void getApiURL() {
        configAPIObject = jsonAPIReader.apiReader(typeAPI);
        apiURL = configAPIObject.getUrl();
        userName = configAPIObject.getUserName();
        password = configAPIObject.getPassword();
        storeId = apiAminService.storeId;
        accountId = apiAminService.accountId;
    }

    /**
     * branch/get-branches-by-customer-address?lat=0&lng=0&isNotSelectCustomerAddress=true
     *
     * @param lat
     * @param lng
     */
    public static List<Branch> getBranchesByCustomerAddress(Double lat, Double lng) {
        Boolean isNotSelectCustomerAddress = false;
        if (lat == 0 || lng == 0) {
            isNotSelectCustomerAddress = true;
        }
        String apiPath = "branch/get-branches-by-customer-address";
        RequestSpecification request = given();
        request.baseUri(apiURL)
                .basePath(apiPath)
                .header("X-STORE-ID", storeId)
                .accept("application/json");
        request.queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("isNotSelectCustomerAddress", isNotSelectCustomerAddress);
        Response response = request.when().get();
        String fileName = "getBranchSchema.json";
        String filePath = typeAPI + "/jsonSchema/" + theme + "/" + fileName; //pos/jsonSchema/theme1/getBranchSchema.json
        helper.writeFile(path + filePath, response.asPrettyString());
        return jsonAPIPosReader.getAllBranches(filePath);
    }

    public static String getGoogleApiKey() {
        String apiPath = "store/get-google-api-key-by-store-id";
        RequestSpecification request = given();
        request.baseUri(apiURL)
                .basePath(apiPath)
                .header("X-STORE-ID", storeId)
                .accept("application/json");
        Response response = request.when().get();
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.get("googleApiKey").toString();
    }

    /**
     * Get long and lat with address by google map api
     * https://developers.google.com/maps/documentation/geocoding/requests-geocoding
     *
     * @param address
     * @return
     */
    public static Map<String, Double> getLatLongByAddress(String address) {
        Map<String, Double> location = new HashMap<>();
        String googleApiKey = getGoogleApiKey();
        String baseURL = "https://maps.googleapis.com/maps/api/geocode/json";
        Response response = given()
                .queryParam("address", address)
                .queryParam("key", googleApiKey)
                .get(baseURL);
        JsonPathConfig jsonConfig = new JsonPathConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
        BigDecimal lat = JsonPath.with(response.asString()).using(jsonConfig).get("results[0].geometry.location.lat");
        BigDecimal lng = JsonPath.with(response.asString()).using(jsonConfig).get("results[0].geometry.location.lng");
        location.put("lat", lat.doubleValue());
        location.put("lng", lng.doubleValue());
        return location;
    }

    /**
     * store.config.json api
     * {{store URL}}/store.config.json
     */
    private static void getStoreConfigJson() {
        String apiPath = "store.config.json";
        RequestSpecification request = given();
        request.baseUri(url)
                .basePath(apiPath)
                .accept("application/json");
        Response response = request.when().get();
        String fileName = "storeConfig.json";
        helper.writeFile(path + "/" + getFilePath(fileName), response.asPrettyString());
    }

    public static List<MenuItem> getMenuHeaderList() {
        String fileName = "storeConfig.json";
        String filePath = typeAPI + "/jsonSchema/" + theme + "/" + fileName;
        return jsonAPIPosReader.getMenuHeaderList(filePath);
    }
}
