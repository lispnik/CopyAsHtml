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

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Describes the position and resizing behaviour of a single component in a <code>XTableLayout</code>.<br/> An instance
 * of this class is created internally by XTableLayout whenever a string constraint is used when a component is added to
 * the container. <code>XTableConstraints</code> objects can be created explicitly to specify the constraints when
 * adding to a container, but it's much more convenient to stick with the string mechanism.<br/><br/>
 * <p/>
 * <br/>Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved.
 * <br/><b>xlayouts</b> is published under a <a href="doc-files/LICENSE.txt">BSD license</a>.
 *
 * @author Stephen Kelvin
 * @see com.eekboom.xlayouts.XTableLayout
 */
public class XTableConstraints implements Serializable {
    private static final long serialVersionUID = -1892500102024551014L;

    /**
     * x or y value to specify that the component should be placed in same the column or row as the component added
     * previously
     */
    public static final int SAME       = Integer.MIN_VALUE + 11;
    /**
     * x or y value to specify that the component should be placed in the column or row following the component added
     * previously
     */
    public static final int NEXT       = Integer.MIN_VALUE + 12;


    /**
     * width or height value to request that the size of the component will be calculated automatically
     */
    public static final int AUTO_SIZE  = Integer.MIN_VALUE + 13;

    /**
     * column index at which the associated component starts
     */
    public final int        x;
    /**
     * row index at which the associated component starts
     */
    public final int        y;
    /**
     * number of columns that the associated component spans
     */
    public final int        width;
    /**
     * number of rows that the associated component spans
     */
    public final int        height;
    /**
     * horizontal alignment of the associated component within its column
     */
    public final XAlignment xAlignment;
    /**
     * vertical alignment of the associated component within its row
     */
    public final YAlignment yAlignment;
    /**
     * specifies how much the component's column(s) should expand in relation to all columns
     */
    public final double     xWeight;
    /**
     * specifies how much the component's row(s) should expand in relation to all rows
     */
    public final double     yWeight;
    static final double AUTO_WEIGHT = -1.0;

    /**
     * Creates a new constraints object using a string specification. Defaults for missing values are width = AUTO_SIZE,
     * height = AUTO_SIZE, xAlign = AUTO, yAlign = AUTO, xWeight = AUTO, yWeight = AUTO.
     *
     * @param text constraints in the form "x y [width height] [xAlign yAlign [xWeigth yWeigth]]"
     * @see #XTableConstraints(int, int, int, int, XAlignment, YAlignment, double, double)
     */
    public XTableConstraints(String text) throws IllegalArgumentException {
        StringTokenizer tokenizer  = new StringTokenizer(text, ", ");
        int             tokenCount = tokenizer.countTokens();
        if(tokenCount < 1 || tokenCount > 8) {
            throw new IllegalArgumentException("XTableConstraints are invalid: " + text);
        }
        String[] tokens = new String[tokenCount];
        int startOfNonIntTokens = tokenCount;
        int i = 0;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            tokens[i] = token;
            if(!".".equals(token) && !"+".equals(token) && !isInt(token)) {
                startOfNonIntTokens = Math.min(startOfNonIntTokens, i);
            }
            ++i;
        }



        boolean oddBoundsTokenCount = startOfNonIntTokens % 2 != 0;
        int nextTokenIndex = 0;
        
        // x, y
        String xText;
        String yText;
        if(startOfNonIntTokens == 0) {
            xText = "+";
            yText = ".";

        }
        else {
            if(oddBoundsTokenCount) {
                String pos = tokens[nextTokenIndex++];
                if(".".equals(pos)) {
                    xText = "+";
                    yText = ".";
                }
                else if("+".equals(pos)) {
                    xText = "0";
                    yText = "+";
                }
                else {
                    throw new IllegalArgumentException("XTableConstraints are invalid: " + text);
                }
            }
            else {
                xText = tokens[nextTokenIndex++];
                yText = tokens[nextTokenIndex++];
            }
        }
        if(".".equals(xText)) {
            x = SAME;
        }
        else if("+".equals(xText)) {
            x = NEXT;
        }
        else {
            x = Integer.parseInt(xText);
        }
        if(".".equals(yText)) {
            y = SAME;
        }
        else if("+".equals(yText)) {
            y = NEXT;
        }
        else {
            y = Integer.parseInt(yText);
        }

        if(x < 0 && x != SAME && x != NEXT) {
            throw new IllegalArgumentException("x must not be negative: " + x);
        }
        if(y < 0 && y != SAME && y != NEXT) {
            throw new IllegalArgumentException("y must not be negative: " + y);
        }
        
        // width, height
        if(nextTokenIndex < startOfNonIntTokens) {
            width  = Integer.parseInt(tokens[nextTokenIndex++]);
            height = Integer.parseInt(tokens[nextTokenIndex++]);
        }
        else {
            width  = AUTO_SIZE;
            height = AUTO_SIZE;
        }
        if(width != AUTO_SIZE && width <= 0) {
            throw new IllegalArgumentException("width must be positiv: " + width);
        }
        if(height != AUTO_SIZE && height <= 0) {
            throw new IllegalArgumentException("height must be positiv: " + height);
        }
        
        // alignment
        if(nextTokenIndex < tokenCount) {
            String xAlignText = tokens[nextTokenIndex++];
            xAlignment = XAlignment.get(xAlignText);

            String yAlignText = tokens[nextTokenIndex++];
            yAlignment = YAlignment.get(yAlignText);
        }
        else {
            xAlignment = XAlignment.AUTO;
            yAlignment = YAlignment.AUTO;
        }
        
        // weights
        if(nextTokenIndex < tokenCount) {
            xWeight = Double.parseDouble(tokens[nextTokenIndex++]);
            yWeight = Double.parseDouble(tokens[nextTokenIndex++]);

            if(xWeight < 0.0) {
                throw new IllegalArgumentException("xWeight must not be negative: " + xWeight);
            }
            if(yWeight < 0.0) {
                throw new IllegalArgumentException("yWeight must not be negative: " + yWeight);
            }
        }
        else {
            xWeight = AUTO_WEIGHT;
            yWeight = AUTO_WEIGHT;
        }
    }

    private static boolean isInt(String text) {
        try {
            Integer.parseInt(text);
            return true;
        }
        catch(NumberFormatException ignore) {
            return false;
        }
    }

    /**
     * Creates a new constraints object using the given position and defaults for other values (widht = AUTO_SIZE,
     * height = AUTO_SIZE, xAlign = AUTO, yAlign = AUTO, xWeight = 0, yWeight = 0).
     *
     * @param x column index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param y row index where the component should start, must be non-negative integer value or SAME or NEXT
     */
    public XTableConstraints(int x, int y) {
        this(x, y, AUTO_SIZE, AUTO_SIZE, XAlignment.AUTO, YAlignment.AUTO, AUTO_WEIGHT, AUTO_WEIGHT, false);
    }

    /**
     * Creates a new constraints object using the given position and size and defaults for other values (xAlign = AUTO,
     * yAlign = AUTO, xWeight = 0, yWeight = 0).
     *
     * @param x      column index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param y      row index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param width  number of columns that the component spans, must be positiv
     * @param height number of rows that the component spans, must be positiv
     */
    public XTableConstraints(int x, int y, int width, int height) {
        this(x, y, width, height, XAlignment.AUTO, YAlignment.AUTO, AUTO_WEIGHT, AUTO_WEIGHT, false);
    }

    /**
     * Creates a new constraints object using the given position, and alignment, width and height of AUTO_SIZE and auto weights.
     *
     * @param x          column index where the component should start, must be non-negative integer value or SAME or
     *                   NEXT
     * @param y          row index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param xAlignment horizontal alignment of the component in its grid cell, see constants defined in {@link
     *                   XAlignment}, must not be null
     * @param yAlignment vertical alignment of the component in its grid cell, see constants defined in {@link
     *                   YAlignment}, must not be null
     */
    public XTableConstraints(int x, int y, XAlignment xAlignment, YAlignment yAlignment) {
        this(x, y, AUTO_SIZE, AUTO_SIZE, xAlignment, yAlignment, AUTO_WEIGHT, AUTO_WEIGHT, false);
    }

    /**
     * Creates a new constraints object using the given position, size and alignment and auto weights.
     *
     * @param x          column index where the component should start, must be non-negative integer value or SAME or
     *                   NEXT
     * @param y          row index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param width      number of columns that the component spans, must be positiv
     * @param height     number of rows that the component spans, must be positiv
     * @param xAlignment horizontal alignment of the component in its grid cell, see constants defined in {@link
     *                   XAlignment}, must not be null
     * @param yAlignment vertical alignment of the component in its grid cell, see constants defined in {@link
     *                   YAlignment}, must not be null
     */
    public XTableConstraints(int x, int y, int width, int height, XAlignment xAlignment, YAlignment yAlignment) {
        this(x, y, width, height, xAlignment, yAlignment, AUTO_WEIGHT, AUTO_WEIGHT, false);
    }

    /**
     * Creates a new constraints object using the given position, size, alignment and weights.
     *
     * @param x          column index where the component should start, must be non-negative integer value or SAME or
     *                   NEXT
     * @param y          row index where the component should start, must be non-negative integer value or SAME or NEXT
     * @param width      number of columns that the component spans, must be positiv
     * @param height     number of rows that the component spans, must be positiv
     * @param xAlignment horizontal alignment of the component in its grid cell, see constants defined in {@link
     *                   XAlignment}, must not be null
     * @param yAlignment vertical alignment of the component in its grid cell, see constants defined in {@link
     *                   YAlignment}, must not be null
     * @param xWeight    specifies how much the component's column(s) should expand in relation to all columns, must not be negative
     * @param yWeight    specifies how much the component's row(s) should expand in relation to all rows, must not be negative
     */
    public XTableConstraints(int x, int y, int width, int height, XAlignment xAlignment,
                             YAlignment yAlignment, double xWeight, double yWeight) {
        this(x, y, width, height, xAlignment, yAlignment, xWeight, yWeight, true);
    }
    
    private XTableConstraints(int x, int y, int width, int height,
                              XAlignment xAlignment, YAlignment yAlignment, double xWeight, double yWeight,
                              boolean checkWeight)
    {
        if(x < 0 && x != SAME && x != NEXT) {
            throw new IllegalArgumentException("x must not be negative: " + x);
        }
        if(y < 0 && y != SAME && y != NEXT) {
            throw new IllegalArgumentException("y must not be negative: " + y);
        }
        if(width <= 0 && width != AUTO_SIZE) {
            throw new IllegalArgumentException("width must either be XTableConstraints.AUTO_SIZE or a positiv integer: " + width);
        }
        if(height <= 0 && height != AUTO_SIZE) {
            throw new IllegalArgumentException("height must either be XTableConstraints.AUTO_SIZE or a positiv integer: " + height);
        }
        if(xAlignment == null) {
            throw new IllegalArgumentException("XAlignment must not be null");
        }
        if(yAlignment == null) {
            throw new IllegalArgumentException("YAlignment must not be null");
        }

        if(checkWeight) {
            if(xWeight < 0.0) {
                throw new IllegalArgumentException("xWeight must not be negative: " + xWeight);
            }
            if(yWeight < 0.0) {
                throw new IllegalArgumentException("yWeight must not be negative: " + yWeight);
            }
        }

        this.x          = x;
        this.y          = y;
        this.width      = width;
        this.height     = height;
        this.xAlignment = xAlignment;
        this.yAlignment = yAlignment;
        this.xWeight    = xWeight;
        this.yWeight    = yWeight;
    }

    int getPos(int axis) {
        if(axis == Axis.X) {
            return x;
        }
        else if(axis == Axis.Y) {
            return y;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    int getSize(int axis) {
        if(axis == Axis.X) {
            return width;
        }
        else if(axis == Axis.Y) {
            return height;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    Alignment getAlignment(int axis) {
        if(axis == Axis.X) {
            return xAlignment;
        }
        else if(axis == Axis.Y) {
            return yAlignment;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    double getWeight(int axis) {
        if(axis == Axis.X) {
            return xWeight;
        }
        else if(axis == Axis.Y) {
            return yWeight;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constraints object are strictly value based, so this will return true if all attributes are equal.
     *
     * @param other the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the other argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
        if(!(other instanceof XTableConstraints)) {
            return false;
        }

        XTableConstraints xTableConstraints = (XTableConstraints) other;

        if(x != xTableConstraints.x) {
            return false;
        }
        if(y != xTableConstraints.y) {
            return false;
        }
        if(width != xTableConstraints.width) {
            return false;
        }
        if(height != xTableConstraints.height) {
            return false;
        }
        if(xAlignment != xTableConstraints.xAlignment) {
            return false;
        }
        if(yAlignment != xTableConstraints.yAlignment) {
            return false;
        }
        if(xWeight != xTableConstraints.xWeight) {
            return false;
        }
        if(yWeight != xTableConstraints.yWeight) {
            return false;
        }

        return true;
    }

    /**
     * Overridden to ensure that constraints objects that are <code>equal()</code> have the same hash code.
     *
     * @return a hash code value for this object.
     * @see #equals(java.lang.Object)
     * @see java.util.Hashtable
     */
    public int hashCode() {
        int result = x;
        result = 29 * result + y;
        result = 29 * result + width;
        result = 29 * result + height;
        result = 29 * result + xAlignment.hashCode();
        result = 29 * result + yAlignment.hashCode();

        long xWeightTemp = Double.doubleToLongBits(xWeight);
        result = 29 * result + (int) (xWeightTemp ^ xWeightTemp >>> 32);
        long yWeightTemp = Double.doubleToLongBits(yWeight);
        result = 29 * result + (int) (yWeightTemp ^ yWeightTemp >>> 32);
        return result;
    }

    /**
     * @return a human readable description of this constraints' attribute values. The result can <b>not</b> be used to
     *         create a new instance with {@link #XTableConstraints(String)}!
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XTableConstraints@");
        buffer.append(Integer.toHexString(System.identityHashCode(this)));
        buffer.append("[x=");
        buffer.append(getIndexText(x));
        buffer.append(",y=");
        buffer.append(getIndexText(y));
        buffer.append(",width=");
        buffer.append(width);
        buffer.append(",height=");
        buffer.append(height);
        buffer.append(",xAlign=");
        buffer.append(xAlignment);
        buffer.append(",yAlign=");
        buffer.append(yAlignment);
        buffer.append(']');
        return new String(buffer);
    }

    private static String getIndexText(int pos) {
        switch(pos) {
            case SAME:
                return ".";
            case NEXT:
                return "+";
            default:
                return Integer.toString(pos);
        }
    }

    /**
     * return a constraint object that has any relative position attributes (NEXT, SAME) replaced by absolute positions
     * using the given base position
     *
     * @param lastX      base column index to which NEXT or SAME x values are resolved
     * @param lastY      base row index to which NEXT or SAME x values are resolved
     * @param lastWidth  increment to add to lastX for NEXT value
     * @param lastHeight increment to add to lastY for NEXT value
     */
    XTableConstraints resolve(int lastX, int lastY, int lastWidth, int lastHeight) {
        if(   x != SAME && x != NEXT && y != SAME && y != NEXT) {
            return this;
        }

        int        x               = this.x == SAME ? lastX : this.x == NEXT ? lastX + lastWidth  : this.x;
        int        y               = this.y == SAME ? lastY : this.y == NEXT ? lastY + lastHeight : this.y;

        XTableConstraints absolute = new XTableConstraints(x, y, width, height, xAlignment, yAlignment, xWeight,
                                                           yWeight, false);

        return absolute;
    }
}
