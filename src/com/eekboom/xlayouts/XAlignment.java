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

import java.util.HashMap;
import java.util.Map;

/**
 * <code>XAlignment</code> specifies how a component or group of components is aligned horizontally.<br/>
 * E.g. an alignment of <code>XAlignment.END</code> for a component, that participates in a <code>XTableLayout</code>,
 * positions the component at the trailing edge of its cell (i.e. right for left-to-right component orientation).<br/>
 * <br/>
 * Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved. <br/>
 * <b>xlayouts</b> is published under a <a href="doc-files/LICENSE.txt">BSD license</a>.
 *
 * @author Stephen Kelvin
 */
public final class XAlignment extends Alignment {
    private static final long      serialVersionUID = -8920299688483011355L;

    private static final Map       nameToInstance   = new HashMap();

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and anchors it at the
     * leading edge of its column (left for left-to-right component orientation)
     */
    public static final XAlignment START            = new XAlignment("start", true, true, false, 0.0);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and anchors it at the
     * trailing edge of its column (right for left-to-right component orientation)
     */
    public static final XAlignment END              = new XAlignment("end", true, true, false, 1.0);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and makes it left aligned
     * within its column
     */
    public static final XAlignment LEFT             = new XAlignment("left", false, true, false, 0.0);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and centers it within its
     * column
     */
    public static final XAlignment CENTER           = new XAlignment("center", false, true, false, 0.5);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and makes it right aligned
     * within its column
     */
    public static final XAlignment RIGHT            = new XAlignment("right", false, true, false, 1.0);

    /**
     * Makes the component fill its column(s) horizontally (if the component's maximum width prevents this, the
     * component gets its maximum width and is aligned according to the value returned from {@link
     * java.awt.Component#getAlignmentX()}).
     */
    public static final XAlignment FILL             = new XAlignment("fill", false, false, true, Double.NaN);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and aligns it according to the
     * value returned from {@link java.awt.Component#getAlignmentX()}).
     */
    public static final XAlignment ALIGN            = new XAlignment("align", true, true, true, Double.NaN);

    /**
     * If the component is expandable (i.e. its minimum, preferred, maximum widths differ) it behaves like {@link #FILL}
     * else like {@link #ALIGN}.
     */
    public static final XAlignment AUTO             = new XAlignment("auto", true, true, true, Double.NaN);

    private XAlignment(String name, boolean useComponentOrientation, boolean usePreferredSize,
                       boolean useComponentAlignmentFactor, double alignmentFactor) {
        super(name, Axis.X, usePreferredSize, useComponentAlignmentFactor, alignmentFactor, useComponentOrientation);

        nameToInstance.put(name, this);
    }

    /**
     * Used to create constraints from their textual representation
     * @param name the name as specified in the constraint,
     *        i.e. either "left", "center", "right", "fill", "align" or "auto"
     * @return the XAlignment instance corresponding to the name
     * @throws IllegalArgumentException if no XAlignment with the given name exists
     */
    static XAlignment get(String name) {
        return (XAlignment) get(name, nameToInstance);
    }

    private Object readResolve() {
        String name = getName();
        XAlignment xAlignment = get(name);
        return xAlignment;
    }
}
