package com.example.application.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.WebBrowser;

public class BrowserUtil {
    public static String getBrowserIconURL(UI ui) {
        WebBrowser browser = ui.getSession().getBrowser();
        String browserApplication = browser.getBrowserApplication();

        if (browserApplication.contains("OPR")) return "icons/browser/opera.png";
        else if (browserApplication.contains("Edg")) return "icons/browser/microsoft.png";
        else if (browser.isFirefox()) return "icons/browser/firefox.png";
        else if (browser.isChrome()) return "icons/browser/chrome.png";
        else if (browser.isSafari()) return "icons/browser/safari.png" ;

        return null;
    }

    public static String getBrowserName(UI ui) {
        WebBrowser browser = ui.getSession().getBrowser();
        String browserApplication = browser.getBrowserApplication();

        if (browserApplication.contains("OPR")) return "Opera";
        else if (browserApplication.contains("Edg")) return "Edge";
        else if (browser.isFirefox()) return "Firefox";
        else if (browser.isChrome()) return "Chrome";
        else if (browser.isSafari()) return "Safari" ;

        return null;
    }
}
