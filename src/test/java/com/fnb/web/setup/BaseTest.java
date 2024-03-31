package com.fnb.web.setup;

import com.fnb.utils.helpers.HelperListener;
import com.fnb.web.admin.pages.PagesAdminSetup;
import com.fnb.web.pos.pages.PagesPosSetup;
import com.fnb.web.store.PagesStoreSetup;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import java.awt.*;
import java.io.IOException;

public class BaseTest {
    public static ThreadLocal<Setup> setup = new ThreadLocal<>();
    public static ThreadLocal<PagesAdminSetup> adminPage = new ThreadLocal<>();
    public static ThreadLocal<PagesPosSetup> posPage = new ThreadLocal<>();
    public static ThreadLocal<PagesStoreSetup> storePage = new ThreadLocal<>();

    public WebDriver getDriver() {
        return getSetup().driver;
    }

    public Setup getSetup() {
        return setup.get();
    }

    public PagesAdminSetup adminPage() {
        return adminPage.get();
    }

    public PagesPosSetup posPage() {
        return posPage.get();
    }

    public PagesStoreSetup storePage() {
        return storePage.get();
    }

    @BeforeSuite
    public void beforeSuite() {
    }

    @Parameters({"platform", "theme"})
    @BeforeClass
    public void initTestSetup(String platform, @Optional("theme1") String theme) throws AWTException, IOException {
        setup.set(new Setup());
        getSetup().setupDriver(platform, theme);
        switch (platform) {
            case "pos":
                posPage.set(getSetup().navigateToPOSPage());
                HelperListener.setDriver(getDriver(), platform, theme);
                break;
            case "admin":
                adminPage.set(getSetup().navigateToAdminPage());
                posPage.set(getSetup().navigateToPOSPage());
                HelperListener.setDriver(getDriver(), platform, theme);
                break;
            case "store":
                storePage.set(getSetup().navigateToStorePage());
                HelperListener.setDriver(getDriver(), platform, theme);
                break;
        }
    }

    @AfterClass
    public void endSession() {
        getSetup().tearDownDriver();
    }

    @AfterTest
    public void tearDown() throws Exception {

    }
}
