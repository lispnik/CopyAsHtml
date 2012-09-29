package com.eekboom.copy_as_html;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;

public class CopyAsHtml implements ApplicationComponent {
    private static Logger _logger;
    private static CopyAsHtml _instance;

    public CopyAsHtml() {
    }

    public String getComponentName() {
        return "Copy as HTML";
    }

    public static CopyAsHtml getInstance() {
        return _instance;
    }

    public void initComponent() {
        _instance = this;
        _logger = Logger.getInstance(getClass().getName());
        _logger.info("CopyAsHtml.initComponent()");
    }

    public void disposeComponent() {
        _instance = null;
        _logger.info("CopyAsHtml.disposeComponent()");
        _logger = null;
    }

}
