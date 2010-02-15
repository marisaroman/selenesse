package selenesse;

import com.thoughtworks.selenium.*;
import fitnesse

public class SlimSeleniumDriver {
	private String timeoutSeconds = "30";
	private String timeoutMilliseconds = timeoutSeconds + "000";
	
	@SystemUnderTest
	public Selenium seleniumInstance;
	
	public SlimSeleniumDriver(String host, int port, String browser, String baseURL) {
		seleniumInstance = new DefaultSelenium(host, port, browser, baseURL);
		seleniumInstance.start();
	}
	
	public void setTimeoutSeconds(String seconds) {
		timeoutSeconds = seconds;
		timeoutMilliseconds = timeoutSeconds + "000";
	}
	
	public boolean click(String locator) {
		boolean elementFound = seleniumInstance.isElementPresent(locator);
		if (elementFound) {
			seleniumInstance.click(locator);
		}
		return elementFound;
	}
}


