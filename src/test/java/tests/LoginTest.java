package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.LoginPage;

public class LoginTest extends BaseTest {
	
	@Test
    public void testValidLogin() {
        LoginPage login = new LoginPage(driver);
        login.login("user@example.com", "password123");

        // Example assertion — replace with your actual logic
        Assert.assertTrue(driver.getTitle().contains("Dashboard"));
    }
}
