using System;
using System.Collections.Generic;
using System.Text;
using Selenium;

namespace Selenesse
{

    /**************************************************/
    /* waiter subclasses */
    class WaitForTextToAppear : AbstractWait
    {
        private String text;
        private ISelenium instance;

        public WaitForTextToAppear(String text, ISelenium selenium)
        {
            this.text = text;
            this.instance = selenium;
        }
        public override bool Until()
        {
            return instance.IsTextPresent(text);
        }
    }
    class WaitForChecked : AbstractWait
    {
        private String locator;
        private bool ischecked;
        private ISelenium instance;
        public WaitForChecked(String locator, bool ischecked, ISelenium selenium)
        {
            this.locator = locator;
            this.ischecked = ischecked;
            this.instance = selenium;
        }
        public override bool Until()
        {
            if (!instance.IsElementPresent(locator)) return false;
            return instance.IsChecked(locator) == ischecked;
        }
    }
    class WaitForElementToAppear : AbstractWait
    {
        private String text;
        private ISelenium instance;
        public WaitForElementToAppear(String text, ISelenium selenium)
        {
            this.text = text;
            this.instance = selenium;
        }
        public override bool Until()
        {
            return instance.IsElementPresent(text);
        }
    }
    class WaitForElementToDisappear : AbstractWait
    {
        private String text;
        private ISelenium instance;
        public WaitForElementToDisappear(String text, ISelenium selenium)
        {
            this.text = text;
            this.instance = selenium;
        }
        public override bool Until()
        {
            return (!instance.IsElementPresent(text));
        }
    }

    class WaitForFieldValue : AbstractWait
    {
        private String text;
        private String elementLocator;
        private ISelenium instance;
        public WaitForFieldValue(String element, String value, ISelenium selenium)
        {
            this.text = value;
            this.elementLocator = element;
            this.instance = selenium;
        }
        public override bool Until()
        {
            String value = instance.GetValue(elementLocator);
            if (value == null) return false;
            return value.IndexOf(text) >= 0;
        }
    }
    /**************************************************/
}