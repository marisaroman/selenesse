//Released under the terms of the GNU GPL v2
//SeleNesse is maintained by Marisa Seal and Chris McMahon
//Portions of SeleNesse based on code originally written by Gojko Adzic http://gojko.net

package selenesse;

import com.thoughtworks.selenium.*;
import fitnesse.slim.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class SlimSeleniumDriver {
	private static final String KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE = "Couldn't access document.body";
	private static final String FORWARD_SLASH = "/";
	private String timeoutSeconds = "30";
	private String timeoutMilliseconds = timeoutSeconds + "000";
	
	@SystemUnderTest
	public Selenium seleniumInstance;
	
	public SlimSeleniumDriver(String host, int port, String browser, String baseURL) {
		seleniumInstance = new DefaultSelenium(host, port, browser, baseURL);
		seleniumInstance.start();
	}
	
	public String getCookies() {
		return seleniumInstance.getCookie();
	}
	
	//HTTP Requests

	/**
	 * Makes a GET request to the given path using the cookies currently set on the {@link Selenium}
	 * instance.
	 * 
	 * @param path
	 * @return the response
	 */
	public String get(String path) throws Exception
	{
		return makeRequest("GET", path);
	}

	/**
	 * Makes a PUT request to the given path using the cookies currently set on the {@link Selenium}
	 * instance.
	 * 
	 * @param path
	 * @return the response
	 */
	public String put(String path) throws Exception
	{
		return makeRequest("PUT", path);
	}
	
	/**
	 * Makes a DELETE request to the given path using the cookies currently set on the
	 * {@link Selenium} instance.
	 * 
	 * @param path
	 * @return the response
	 */
	public String delete(String path) throws Exception
	{
		return makeRequest("DELETE", path);
	}

	/**
	 * Makes a simple file POST request to the given path using the cookies currently set on the
	 * {@link Selenium} instance.
	 * 
	 * @param path
	 * @param mediaType
	 * @param filename
	 * @return the response
	 */
	public String postFile(String path, String mediaType, String filename) throws Exception
	{
		String url = getFormattedURL(getBaseURL(), path);
		return HttpUtils.postSimpleFile(url, getCookies(), mediaType, filename);
	}
	
	// Makes a simple http request with the given request method.
	private String makeRequest(String requestMethod, String path) throws Exception {
		String url = getFormattedURL(getBaseURL(), path);
		return HttpUtils.makeRequest(requestMethod, url, getCookies());
	}

	public String getBaseURL() throws MalformedURLException {
		URL url = new URL(seleniumInstance.getLocation());
		return url.getProtocol() + "://" + url.getHost();
	}
	
	private String getFormattedURL(String baseURL, String path) {
		if (!path.startsWith(FORWARD_SLASH)) {
			path = FORWARD_SLASH + path;
		}
		return baseURL + path;
	}
	
	//Convenience methods
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
	public boolean waitForEditable(String locator) {
		Wait w = new WaitForElementToBeEditable(locator);
		try {
			w.wait("Element " + locator + " not editable after " + timeoutSeconds + " seconds", Long.parseLong(timeoutMilliseconds));
		}
		catch (SeleniumException e) {
			if (isKnownSeleniumBug(e)) {
				waitForEditable(locator);
			}
			else {
				throw e;
			}
		}
		return true;
	}
	
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
	
	public boolean waitForElementNotPresent(String locator) {
		Wait w = new WaitForElementToDisappear(locator);
		try {
			w.wait("Element " +locator+ " still present after "+timeoutSeconds+" seconds", Long.parseLong(timeoutMilliseconds));
		}
		catch (SeleniumException e) {
			if (isKnownSeleniumBug(e)) {
				waitForElementNotPresent(locator);
			}
			else {
				throw e;
			}
		}
		return true;
	}
	
	public boolean waitForVisible(String locator) {
		Wait x=new WaitForElementToBeVisible(locator);
		try {
			x.wait("Element " + locator + " not visible after " + timeoutSeconds + " seconds", Long.parseLong(timeoutMilliseconds));
		}
		catch (SeleniumException e) {
        	if (isKnownSeleniumBug(e)) {
        		waitForVisible(locator);
        	}
        	else {
				throw e;
			}
        }
        return true;
	}
	
	public boolean waitForNotVisible(String locator) {
		Wait x=new WaitForElementToBeInvisible(locator);
		try {
			x.wait("Element " + locator + " still visible after " + timeoutSeconds + " seconds", Long.parseLong(timeoutMilliseconds));
		}
		catch (SeleniumException e) {
        	if (isKnownSeleniumBug(e)) {
        		waitForNotVisible(locator);
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
	
	public boolean waitForTextNotPresent(String text){
		Wait x=new WaitForTextToDisappear(text);
		try {
        	x.wait("Text " +text+ " still present after "+timeoutSeconds+" seconds", Long.parseLong(timeoutMilliseconds));
        }
        catch (SeleniumException e) {
        	if (isKnownSeleniumBug(e)) {
        		waitForTextNotPresent(text);
        	}
        	else {
				throw e;
			}
        }
        return true;
	}
	
	public boolean waitForSelectedLabel(String selectLocator, String label) {
		Wait x=new WaitForLabelToBeSelected(selectLocator, label);
		boolean elementFound = seleniumInstance.isElementPresent(selectLocator); 
		if (elementFound) {
			try {
				x.wait("Option with label " +label+ " not selected in " + selectLocator + " after " + timeoutSeconds 
						+ " seconds", Long.parseLong(timeoutMilliseconds));
			}
			catch (SeleniumException e) {
	        	if (isKnownSeleniumBug(e)) {
	        		waitForSelectedLabel(selectLocator, label);
	        	}
	        	else {
					throw e;
				}
	        }
		}
        return elementFound;
	}
	
	//Waiter classes
	protected class WaitForElementToBeEditable extends Wait {
		protected String locator;
		public WaitForElementToBeEditable(String locator) {
			this.locator = locator;
		}
		public boolean until() {
			return seleniumInstance.isEditable(locator);
		}
	}
	protected class WaitForElementToAppear extends Wait {
		protected String locator;
	   	public  WaitForElementToAppear(String locator) {
	   		this.locator= locator; 
	   	}
	   	public boolean until() {
	   		return seleniumInstance.isElementPresent(locator);
	   	}
    }
	
	protected class WaitForElementToBeVisible extends Wait {
		protected String locator;
		public WaitForElementToBeVisible(String locator) {
			this.locator = locator;
		}
		public boolean until() {
			return (seleniumInstance.isElementPresent(locator) &&
					seleniumInstance.isVisible(locator));
		}
	}
	
	protected class WaitForElementToBeInvisible extends Wait {
		protected String locator;
		public WaitForElementToBeInvisible(String locator) {
			this.locator = locator;
		}
		public boolean until() {
			return !seleniumInstance.isVisible(locator);
		}
	}
	
	protected class WaitForElementToDisappear extends Wait {
		protected String locator;
	   	public  WaitForElementToDisappear(String locator) {
	   		this.locator= locator; 
	   	}
	   	public boolean until() {
	   		return !seleniumInstance.isElementPresent(locator);
	   	}
	}
	
	protected class WaitForTextToAppear extends Wait{
		protected String text;
	   	public  WaitForTextToAppear(String text){
	   		this.text= text; 
	   	}
	   	public boolean until(){
	   		return seleniumInstance.isTextPresent(text);
	   	}
    }
	
	protected class WaitForTextToDisappear extends Wait{
		protected String text;
		public WaitForTextToDisappear(String text){
			this.text = text;
		}
		public boolean until(){
			return !seleniumInstance.isTextPresent(text);
		}
	}
	
	protected class WaitForLabelToBeSelected extends Wait {
		protected String locator;
		protected String label;
		public WaitForLabelToBeSelected (String locator, String label) {
			this.locator = locator;
			this.label = label;
		}
		public boolean until() {
			return seleniumInstance.getSelectedLabel(locator).equals(label);
		}
	}
	
	private boolean optionIsAlreadySelected(String selectLocator, String optionLocator) {
		if (seleniumInstance.isSomethingSelected(selectLocator)) {
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
		return false;
	}
	
	private boolean isKnownSeleniumBug(SeleniumException exception) {
		return exception.getMessage().contains(KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE);
	}
}


