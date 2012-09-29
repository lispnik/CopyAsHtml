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

import java.awt.*;

class XLayouts {
    private static XLogger logger = new GenericLogger();

    private XLayouts() {
    }

    public static void setLogger(XLogger logger) {
        XLayouts.logger = logger;
    }

    public static XLogger getLogger() {
        return logger;
    }

    static void warn(String message) {
        logger.warn(message);
    }

    static boolean isMacOs() {
        String  mrjVersion = System.getProperty("mrj.version");
        boolean isMacOs    = mrjVersion != null;
        return isMacOs;
    }

    static Alignment getFillAlignment(int axis) {
        if(axis == Axis.X) {
            return XAlignment.FILL;
        }
        else if(axis == Axis.Y) {
            return YAlignment.FILL;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static Alignment getAutoAlignment(int axis) {
        if(axis == Axis.X) {
            return XAlignment.AUTO;
        }
        else if(axis == Axis.Y) {
            return YAlignment.AUTO;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static Alignment getAlignAlignment(int axis) {
        if(axis == Axis.X) {
            return XAlignment.ALIGN;
        }
        else if(axis == Axis.Y) {
            return YAlignment.ALIGN;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int getSize(int axis, Dimension dimension) {
        if(axis == Axis.X) {
            return dimension.width;
        }
        else if(axis == Axis.Y) {
            return dimension.height;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int getSize(int axis, Rectangle rectangle) {
        if(axis == Axis.X) {
            return rectangle.width;
        }
        else if(axis == Axis.Y) {
            return rectangle.height;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int getPos(int axis, Rectangle rectangle) {
        if(axis == Axis.X) {
            return rectangle.x;
        }
        else if(axis == Axis.Y) {
            return rectangle.y;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static float getAlignmentFactor(int axis, Component component) {
        if(axis == Axis.X) {
            return component.getAlignmentX();
        }
        else if(axis == Axis.Y) {
            return component.getAlignmentY();
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static void setPos(Rectangle rectangle, int axis, int pos) {
        if(axis == Axis.X) {
            rectangle.x = pos;
        }
        else if(axis == Axis.Y) {
            rectangle.y = pos;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static void setSize(Rectangle rectangle, int axis, int size) {
        if(axis == Axis.X) {
            rectangle.width = size;
        }
        else if(axis == Axis.Y) {
            rectangle.height = size;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int getStart(int axis, Insets insets) {
        if(axis == Axis.X) {
            return insets.left;
        }
        else if(axis == Axis.Y) {
            return insets.top;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int getEnd(int axis, Insets insets) {
        if(axis == Axis.X) {
            return insets.right;
        }
        else if(axis == Axis.Y) {
            return insets.bottom;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static int get(int axis, Insets insets) {
        if(axis == Axis.X) {
            return insets.left + insets.right;
        }
        else if(axis == Axis.Y) {
            return insets.top + insets.bottom;
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
