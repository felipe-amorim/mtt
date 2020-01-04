package support;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class Core {
    private WebDriver driver = null;
    private String lastXpath = "";
    static List<WebElement> lastElements = null;


    private void innerFind(){
        lastElements = driver.findElements(By.xpath(lastXpath));
    }

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
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    protected void navigate (String url){
        if(driver==null){
            createChrome();
        }
        driver.get(url);
    }

    protected Actions find(String xpath){
        lastXpath = xpath;
        innerFind();
        return new Actions();
    }

    public List<WebElement> getAllElements(String xpath){
        lastXpath = xpath;
        innerFind();
        return Core.lastElements;
    }

    public WebElement getElement(String xpath){
        lastXpath = xpath;
        innerFind();
        return Core.lastElements.get(0);
    }


}
