using System;
using System.Collections.Generic;
using System.Text;

namespace Selenesse
{
    abstract class AbstractWait
    {
        protected int pollingFrequency = 100;
        public void Wait(String message, int timeoutMillis)
        {
            DateTime start = DateTime.Now;
            while (timeoutMillis > DateTime.Now.Subtract(start).TotalMilliseconds)
            {
                if (Until()) return;
                System.Threading.Thread.Sleep(pollingFrequency);
            }
            throw new ApplicationException(message);
        }
        public abstract bool Until();
    }
}