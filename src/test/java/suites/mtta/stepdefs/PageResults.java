package suites.mtta.stepdefs;

import cucumber.api.java.en.Then;
import support.Core;

public class PageResults /*extends Core*/ {
    @Then("The results page should be displayed")
    public void theResultsPageShouldBeDisplayed() {
        //find("//div[@id=\"resultStats\"]").click();
    }
}
