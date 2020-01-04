package suites.mtta.stepdefs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class PageMain {
    private WebDriver driver = null;

    private void createChrome() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", System.getProperty("user.dir")+"/src/test/java/resources/downloads/");
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("disable-infobars");
        options.addArguments("--incognito");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--allow-insecure-localhost");
        //options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Given("The script navigates to {string}")
    public void theScriptNavigatesTo(String arg0) {
        createChrome();
        driver.get(arg0);
        driver.findElement(By.xpath("//input[@type=\"text\"]")).sendKeys("Thread number");
        //driver.findElement(By.xpath("//input[@type=\"text\"]")).sendKeys(Keys.ENTER);
        driver.quit();
    }

    @When("The script types its thread number")
    public void theScriptTypesItsThreadNumber() {
        //driver.findElement(By.xpath("//input[@type=\"text\"]")).sendKeys("Thread number");
        //driver.findElement(By.xpath("//input[@type=\"text\"]")).sendKeys(Keys.ENTER);
    }
}
