package com.poolstats.billiardsscraper.common.service.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.htmlunit.ScriptException;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.JavaScriptErrorListener;

/**
 * A no-op JavaScript error listener that silently suppresses all HtmlUnit JS errors.
 * Prevents ad-script errors (e.g. Google AdSense) from polluting the logs.
 */
public class SilentJavaScriptErrorListener implements JavaScriptErrorListener {

    @Override
    public void scriptException(HtmlPage page, ScriptException scriptException) {
        // suppress
    }

    @Override
    public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
        // suppress
    }

    @Override
    public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
        // suppress
    }

    @Override
    public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
        // suppress
    }

    @Override
    public void warn(String message, String sourceName, int line, String lineSource, int linePosition) {
        // suppress
    }
}

