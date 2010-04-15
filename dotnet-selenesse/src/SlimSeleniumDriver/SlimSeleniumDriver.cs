using System;
using System.Threading;
using fitSharp.Machine.Model;
using Selenium;

namespace Selenesse
{
    public class SlimSeleniumDriver : DomainAdapter
    {

        private const string KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE = "Couldn't access document.body";
        private static string timeoutSeconds = "30";
        private string timeoutMilliseconds = timeoutSeconds + "000";

        private readonly ISelenium _seleniumInstance;

        // constructor with the 4 arguments for the selenium server
        public SlimSeleniumDriver(string serverHost, int serverPort, string browserString, string browserURL)
        {
            _seleniumInstance = new DefaultSelenium(serverHost, serverPort, browserString, browserURL);
            _seleniumInstance.Start();
        }

        public object SystemUnderTest
        {
            get { return _seleniumInstance; }
        }

        public void open(string openURL)
        {
            _seleniumInstance.Open(openURL);
        }

        public void close()
        {
            _seleniumInstance.Close();
        }

        //Convenience methods
        public string getToday()
        {
            const string format = "dd/MM/yy";
            DateTime dt = Convert.ToDateTime(DateTime.Now);
            string str = dt.ToString(format);
            return str;
        }

        public void setTimeoutSeconds(string seconds)
        {
            timeoutSeconds = seconds;
            timeoutMilliseconds = timeoutSeconds + "000";
            _seleniumInstance.SetTimeout(timeoutMilliseconds);
        }

        public void pause(int milliseconds){
            //  throws InterruptedException 
		    Thread.Sleep(milliseconds);
	    }


        //Element interaction methods
        public bool click(String locator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Click(locator);
                return true;
            }
            return false;
        }

        public bool clickAt(String locator, String coordinates)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.ClickAt(locator, coordinates);
                return true;
            }
            return false;
        }

        public bool clickUpToTimes(String locator, int numberOfTimesToExecute)
        {
            int tries = 0;
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            while (_seleniumInstance.IsElementPresent(locator) && tries <= numberOfTimesToExecute)
            {
                try
                {
                    _seleniumInstance.Click(locator);
                }
                catch (SeleniumException e)
                {
                    if (e.Message.Contains("not found"))
                    {
                        return elementFound;
                    }
                    throw e;
                }
                tries++;
            }
            return elementFound;
        }

        public bool focus(String locator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Focus(locator);
                return true;
            }
            return false;
        }

        public bool makeChecked(String locator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Check(locator);
                return true;
            }
            return false;
        }

        public bool makeNotChecked(String locator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Uncheck(locator);
                return true;
            }
            return false;
        }

        public bool select(String selectLocator, String optionLocator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(selectLocator);
            if (elementFound && !optionIsAlreadySelected(selectLocator, optionLocator))
            {
                _seleniumInstance.Select(selectLocator, optionLocator);
            }
            return elementFound;
        }

        public bool type(String locator, String text)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Type(locator, text);
            }
            return elementFound;
        }

        //_AndWait methods
        public bool clickAndWait(String locator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(locator);
            if (elementFound)
            {
                _seleniumInstance.Click(locator);
                _seleniumInstance.WaitForPageToLoad(timeoutMilliseconds);
            }
            return elementFound;
        }

        public bool selectAndWait(String selectLocator, String optionLocator)
        {
            bool elementFound = _seleniumInstance.IsElementPresent(selectLocator);
            if (elementFound && !optionIsAlreadySelected(selectLocator, optionLocator))
            {
                _seleniumInstance.Select(selectLocator, optionLocator);
                _seleniumInstance.WaitForPageToLoad(timeoutMilliseconds);
            }
            return elementFound;
        }

        //waitFor_ methods
        public bool waitForElementPresent(String locator)
        {
            AbstractWait w = new WaitForElementToAppear(locator, _seleniumInstance);
            try
            {
                w.Wait("Cannot find element " + locator + " after " + timeoutSeconds + " seconds", int.Parse(timeoutMilliseconds));
            }
            catch (SeleniumException e)
            {
                if (isKnownSeleniumBug(e))
                {
                    waitForElementPresent(locator);
                }
                throw e;
            }
            return true;
        }

        public bool waitForTextPresent(String text)
        {
            AbstractWait x = new WaitForTextToAppear(text, _seleniumInstance);
            try
            {
                x.Wait("Cannot find text " + text + " after " + timeoutSeconds + " seconds", int.Parse(timeoutMilliseconds));
            }
            catch (SeleniumException e)
            {
                if (isKnownSeleniumBug(e))
                {
                    waitForTextPresent(text);
                }
                throw e;
            }
            return true;
        }
         
	    // Waiter classes
        //protected class WaitForElementToAppear : AbstractWait {
        //    protected string text;
        //    public  WaitForElementToAppear(String  text) {
        //        this.text= text; 
        //    }
        //    public bool until() {
        //        return _seleniumInstance.IsElementPresent(text);
        //    }
        //}
     
        //protected class WaitForTextToAppear : AbstractWait {
        //    protected string text;
        //    public  WaitForTextToAppear(String  text){
        //        this.text= text; 
        //    }
        //    public bool until(){
        //        return _seleniumInstance.IsTextPresent(text);
        //    }
        //}

        private bool optionIsAlreadySelected(String selectLocator, String optionLocator) {
            return (optionLocator.StartsWith("id=")
                    && _seleniumInstance.GetSelectedId(selectLocator).Equals(optionLocator.Replace("id=", ""))) ||
                   (optionLocator.StartsWith("label=")
                    && _seleniumInstance.GetSelectedLabel(selectLocator).Equals(optionLocator.Replace("label=", ""))) ||
                   (optionLocator.StartsWith("value=")
                    && _seleniumInstance.GetSelectedValue(selectLocator).Equals(optionLocator.Replace("value=", ""))) ||
                   (optionLocator.StartsWith("index=")
                    && _seleniumInstance.GetSelectedIndex(selectLocator).Equals(optionLocator.Replace("index=", ""))) ||
                   (_seleniumInstance.GetSelectedLabel(selectLocator).Equals(optionLocator));
        }

        private static bool isKnownSeleniumBug(SeleniumException exception)
        {
            return exception.Message.Contains(KNOWN_SELENIUM_BUG_EXCEPTION_MESSAGE);
        }

    }
}

