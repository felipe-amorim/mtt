package support;

import org.openqa.selenium.WebElement;

public class Actions {
    private boolean each = false;

    public void click(){
        if(!each) {
            Core.lastElements.get(0).click();
        }else {
            for (WebElement element:Core.lastElements) {
                element.click();
            }
            each = false;
        }
    }

    public void send(String text){
        if(!each) {
        Core.lastElements.get(0).sendKeys(text);
        }else {
            for (WebElement element:Core.lastElements) {
                element.sendKeys(text);
            }
            each = false;
        }
    }

    public Actions each(){
        each = true;
        return this;
    }


}
