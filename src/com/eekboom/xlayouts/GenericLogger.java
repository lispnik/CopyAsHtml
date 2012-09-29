/*
 * Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list
 *   of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * - Neither the name of the copyright holder nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eekboom.xlayouts;

public class GenericLogger implements XLogger {
    private XLogger logger;

    /** @noinspection UseOfSystemOutOrSystemErr*/
    public GenericLogger() {
        String[] loggerClassNames = {
            "com.eekboom.xlayouts.JavaLoggingLogger",
            "com.eekboom.xlayouts.SystemErrLogger"
        };

        for(int i = 0; logger == null && i < loggerClassNames.length; i++) {
            String loggerClassName = loggerClassNames[i];
            createLogger(loggerClassName);
        }
        if(logger == null) {
            System.err.println("[xlayouts] No suitable logger found");
        }
    }

    /** @noinspection ErrorNotRethrown*/
    private void createLogger(String className) {
        try {
            Class loggerClazz = Class.forName(className);
            logger = (XLogger) loggerClazz.newInstance();
        }
        catch(IllegalAccessException ignore) {
            // fine, try another logger
        }
        catch(InstantiationException ignore) {
            // fine, try another logger
        }
        catch(ClassNotFoundException ignore) {
            // fine, try another logger
        }
        catch(NoClassDefFoundError ignore) {
            // fine, try another logger
        }
    }

    public void warn(String message) {
        if(logger != null) {
            logger.warn(message);
        }
    }
}
