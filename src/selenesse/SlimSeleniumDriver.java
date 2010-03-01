//Released under the terms of the GNU GPL v2
//SeleNesse is maintained by Marisa Seal and Chris McMahon
//Portions of SeleNesse based on code originally written by Gojko Adzic http://gojko.net

package selenesse;

import com.thoughtworks.selenium.*;
import fitnesse.slim.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SlimSeleniumDriver {
	private static final String KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE = "Couldn't access document.body";
	private String timeoutSeconds = "30";
	private String timeoutMilliseconds = timeoutSeconds + "000";
	
	@SystemUnderTest
	public Selenium seleniumInstance;
	
	public SlimSeleniumDriver(String host, int port, String browser, String baseURL) {
		seleniumInstance = new DefaultSelenium(host, port, browser, baseURL);
		seleniumInstance.start();
	}
	
	//Convenience methods
	public String getToday() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        Date date = new Date();
        return dateFormat.format(date);
	}
	
	public void setTimeoutSeconds(String seconds) {
		timeoutSeconds = seconds;
		timeoutMilliseconds = timeoutSeconds + "000";
		seleniumInstance.setTimeout(timeoutMilliseconds);
	}
	
	public void pause(int milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}
	
	//Element interaction methods
	public boolean click(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.click(locator);
			return true;
		}
		return false;
	}
	
	public boolean clickAt(String locator, String coordinates) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.clickAt(locator, coordinates);
			return true;
		}
		return false;
	}
	
	public boolean clickUpToTimes(String locator, int numberOfTimesToExecute) {
		int tries = 0;
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		while (seleniumInstance.isElementPresent(locator) && tries <= numberOfTimesToExecute) {
			try {
				seleniumInstance.click(locator);
			}
			catch (SeleniumException e) {
				if (e.getMessage().contains("not found")) {
					return elementFound;
				}
				throw e;
			}
			tries++;
		}
		return elementFound;
	}
	
	public boolean focus(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.focus(locator);
			return true;
		}
		return false;
	}
	
	public boolean makeChecked(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.check(locator);
			return true;
		}
		return false;
	}
	
	public boolean makeNotChecked(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.uncheck(locator);
			return true;
		}
		return false;
	}
	
	public boolean select(String selectLocator, String optionLocator) {
		boolean elementFound = seleniumInstance.isElementPresent(selectLocator);
		if (elementFound && !optionIsAlreadySelected(selectLocator, optionLocator)) {
			seleniumInstance.select(selectLocator, optionLocator);
		}
		return elementFound;
	}
	
	public boolean type(String locator, String text) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.type(locator, text);
		}
		return elementFound;
	}
	
	//_AndWait methods
	public boolean clickAndWait(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.click(locator);
			seleniumInstance.waitForPageToLoad(timeoutMilliseconds);
		}
		return elementFound;
	}
	
	public boolean selectAndWait(String selectLocator, String optionLocator) {
		boolean elementFound = seleniumInstance.isElementPresent(selectLocator);
		if (elementFound && !optionIsAlreadySelected(selectLocator, optionLocator)) {
				seleniumInstance.select(selectLocator, optionLocator);
				seleniumInstance.waitForPageToLoad(timeoutMilliseconds);
		}
		return elementFound;
	}
	
	//waitFor_ methods
	public boolean waitForElementPresent(String locator) {
		Wait w = new WaitForElementToAppear(locator);
		try {
			w.wait("Cannot find element " +locator+ " after "+timeoutSeconds+" seconds", Long.parseLong(timeoutMilliseconds));
		}
		catch (SeleniumException e) {
			if (isKnownSeleniumBug(e)) {
				waitForElementPresent(locator);
			}
			else {
				throw e;
			}
		}
		return true;
	}
	
	public boolean waitForTextPresent(String text){
        Wait x=new WaitForTextToAppear(text);
        try {
        	x.wait("Cannot find text " +text+ " after "+timeoutSeconds+" seconds", Long.parseLong(timeoutMilliseconds));
        }
        catch (SeleniumException e) {
        	if (isKnownSeleniumBug(e)) {
        		waitForTextPresent(text);
        	}
        	else {
				throw e;
			}
        }
        return true;
	}
	
	//Waiter classes
	protected class WaitForElementToAppear extends Wait {
		protected String text;
	   	public  WaitForElementToAppear(String  text) {
	   		this.text= text; 
	   	}
	   	public boolean until() {
	   		return seleniumInstance.isElementPresent(text);
	   	}
    }
	
	protected class WaitForTextToAppear extends Wait{
		protected String text;
	   	public  WaitForTextToAppear(String  text){
	   		this.text= text; 
	   	}
	   	public boolean until(){
	   		return seleniumInstance.isTextPresent(text);
	   	}
    }
	
	private boolean optionIsAlreadySelected(String selectLocator, String optionLocator) {
		if ((optionLocator.startsWith("id=") 
				&& seleniumInstance.getSelectedId(selectLocator).equals(optionLocator.replace("id=", ""))) ||
			(optionLocator.startsWith("label=") 
				&& seleniumInstance.getSelectedLabel(selectLocator).equals(optionLocator.replace("label=", ""))) ||
			(optionLocator.startsWith("value=")
				&& seleniumInstance.getSelectedValue(selectLocator).equals(optionLocator.replace("value=", ""))) ||
			(optionLocator.startsWith("index=")
				&& seleniumInstance.getSelectedIndex(selectLocator).equals(optionLocator.replace("index=", ""))) ||
			(seleniumInstance.getSelectedLabel(selectLocator).equals(optionLocator))) {
			return true;
		}
		return false;
	}
	
	private boolean isKnownSeleniumBug(SeleniumException exception) {
		return exception.getMessage().contains(KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE);
	}
}


