package api;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WebsiteHTTPTest extends BaseTest {

    @Test(description = "TC01: Homepage is reachable")
    public void homepageReturns200() {
        given().spec(requestSpec)
                .when().get("/")
                .then().spec(okSpec)
                .contentType(containsString("text/html"));
    }

    @Test(description = "TC02: Homepage has expected title")
    public void homepageHasTitle() {
        given().spec(requestSpec)
                .when().get("/")
                .then().statusCode(200)
                .body(containsString("Swag Labs"));
    }

    @Test(description = "TC04: HTTP redirects to HTTPS")
    public void httpRedirectsToHttps() {
        given().redirects().follow(false)
                .when().get("http://www.saucedemo.com/")
                .then().statusCode(anyOf(is(301), is(302), is(307), is(308)))
                .header("Location", startsWith("https://"));
    }

    @Test(description = "TC05: Key headers present")
    public void headersPresent() {
        Response res = given().spec(requestSpec).get("/");
        Assert.assertNotNull(res.getHeader("Content-Type"));
        Assert.assertTrue(res.getTime() < 3000, "Slow response: " + res.getTime());
    }

    @Test(description = "TC06: Static JS bundle loads")
    public void staticBundleLoads() {
        String html = given().spec(requestSpec).get("/").asString();
        Matcher m = Pattern.compile("<script[^>]+src=\"([^\"]+\\.js)\"").matcher(html);
        Assert.assertTrue(m.find(), "No script tag found");

        String jsPath = m.group(1);
        given().spec(requestSpec)
                .when().get(jsPath)
                .then().statusCode(200)
                .contentType(containsString("javascript"));
    }

    @Test(description = "TC07: Cookie-based 'login' is client-side only")
    public void inventoryPageServedRegardlessOfCookie() {
        // The server returns the same SPA shell; redirect-to-login logic is in JS.
        int withoutCookie = given().spec(requestSpec)
                .get("/inventory.html").statusCode();
        int withCookie = given().spec(requestSpec)
                .cookie("session-username", "standard_user")
                .get("/inventory.html").statusCode();

        System.out.println("No cookie: " + withoutCookie + " | With cookie: " + withCookie);
        Assert.assertEquals(withoutCookie, withCookie,
                "Server does not enforce session; auth is client-side");
    }

    @Test(description = "TC08: Unknown path behaviour")
    public void unknownPath() {
        int status = given().spec(requestSpec).get("/does-not-exist").statusCode();
        System.out.println("Unknown path status: " + status);
        Assert.assertTrue(status == 200 || status == 404);
    }
}
