package org.tdar.functional;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.tdar.functional.util.WebElementSelection;

public class SaveSearchResultsSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {
    
    private static final String url = "/search";
    private static final String collectionDescription = "newCollectionDescription";

    @Test
    public void testSaveSearchResults() {
    	enterSearchCriteriaAndSubmit();
    	clickSaveResultsLinkAndDisplayModal();
    	enterCollectionInformation();
    	submitModalForm();
    	//checkIfProgressBarIsVisible(); //Runs too fast to check.
    	checkIfSubmitButtonIsDisabled();
    	checkIfSuccessMessageIsDisplayed();
    }
    
    
    
    public void checkIfSuccessMessageIsDisplayed(){
    	  Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver())
    			    .withTimeout(30, TimeUnit.SECONDS)
    			    .pollingEvery(5, TimeUnit.SECONDS)
    			    .ignoring(NoSuchElementException.class);

		  Boolean completeMessage = wait.until(
			  new Function<WebDriver, Boolean>() {
    			    public Boolean apply(WebDriver driver) {
    			    	return driver.findElement(By.id("progress-complete-text")).isDisplayed();
    			    }
  			   });
		  
    	assertTrue("the success message is displayed", find(By.id("progress-complete-text")).isDisplayed());
    }
    
    public void submitModalForm(){
    	WebElementSelection submitButton = find(By.id("submitSaveSearchResultsBtn"));
    	assertTrue("The button is enabled", submitButton.isEnabled());
    	submitButton.click(); 
    }
    
    public void checkIfProgressBarIsVisible(){
    	assertTrue("The progress bar is being displayed", find(By.id("upload-progress")).isDisplayed());
    }
    
    public void checkIfSubmitButtonIsDisabled(){
    	WebElementSelection submitButton = find(By.id("submitSaveSearchResultsBtn"));
    	assertTrue("The button is disabled", !submitButton.isEnabled());
    }
    
    
    public void enterSearchCriteriaAndSubmit(){
    	//Navigate to search
    	gotoPage(url);
    	//Etner some text ("TEST");
    	//groups[0].allFields[0]
    	setFieldByName("groups[0].allFields[0]","TEST");

    	//Clicks the button 
    	//searchButtonTop click
    	find(By.id("searchButtonTop")).click();
    }
    
    public void clickSaveResultsLinkAndDisplayModal(){
    	assertTrue("The save results link displays", getText().contains("Save these results"));
    	find(By.id("saveSearchLink")).click();
    	sleep(500);
    	assertTrue("The modal window is displayed", find(By.id("modal")).isDisplayed());
   	}

    public void enterCollectionInformation(){
    	//Enter some text into the selectize text area.
    	find(By.id("collection-list-selectized")).val("collection-list-selectized").sendKeys(Keys.ENTER);
    	sleep(500);
    	WebElementSelection descriptionBox = find(By.id(collectionDescription));
    	assertTrue("The description box is displayed", descriptionBox.isDisplayed());
    	descriptionBox.val("New test collection");
    }
    
    public void sleep(Integer millis){
	  //this is wrong. 
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.debug("Couldn't sleep {}",e);
		}
    }

}
