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
 * <code>YAlignment</code> specifies how a component or group of components is aligned vertically.<br/>
 * E.g. an alignment of <code>YAlignment.START</code> for a component, that participates in a <code>XTableLayout</code>,
 * positions the component at the top edge of its cell.<br/>
 * <br/>
 * Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved. <br/>
 * <b>xlayouts</b> is published under a <a href="doc-files/LICENSE.txt">BSD license</a>.
 *
 * @author Stephen Kelvin
 */
public final class YAlignment extends Alignment {
    private static final long      serialVersionUID = -5125448205895376149L;

    private static final Map       nameToInstance   = new HashMap();

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and anchors it at the
     * leading edge of its row (top for left-to-right component orientation)
     */
    public static final YAlignment START            = new YAlignment("start", true, true, false, 0.0);

    /**
     * Gives the components its preferred width (but never more than its column(s) width) and anchors it at the
     * trailing edge of its column (right for left-to-right component orientation)
     */
    public static final YAlignment END              = new YAlignment("end", true, true, false, 1.0);

    /**
     * Gives the components its preferred height (but never more than its row(s) height) and makes it top aligned within its row 
     */
    public static final YAlignment TOP              = new YAlignment("top", false, true, false, 0.0);

    /**
     * Gives the components its preferred height (but never more than its row(s) height) and centers it within its row
     */
    public static final YAlignment CENTER           = new YAlignment("center", false, true, false, 0.5);

    /**
     * Gives the components its preferred height (but never more than its row(s) height) and makes it bottom aligned within its row 
     */
    public static final YAlignment BOTTOM           = new YAlignment("bottom", false, true, false, 1.0);

    /**
     * Makes the component fill its row(s) vertically (if the component's maximum height prevents this, the component gets its maximum
     * height and is aligned according to the value returned from {@link java.awt.Component#getAlignmentY()}).
     */
    public static final YAlignment FILL             = new YAlignment("fill", false, false, true, Double.NaN);

    /**
     * Gives the components its preferred height (but never more than its row(s) height) and aligns it according to the value 
     * returned from {@link java.awt.Component#getAlignmentY()}).
     */
    public static final YAlignment ALIGN            = new YAlignment("align", true, true, true, Double.NaN);

    /**
     * If the component is expandable (i.e. its minimum, preferred, maximum heights differ) it behaves like {@link #FILL} else like 
     * {@link #ALIGN}.
     */
    public static final YAlignment AUTO             = new YAlignment("auto", true, true, true, Double.NaN);

    private YAlignment(String name, boolean useComponentOrientation, boolean usePreferredSize,
                       boolean useComponentAlignmentFactor, double alignmentFactor) {
        super(name, Axis.Y, usePreferredSize, useComponentAlignmentFactor, alignmentFactor, useComponentOrientation);

        nameToInstance.put(name, this);
    }

    /**
     * Used to create constraints from their textual representation
     * @param name the name as specified in the constraint,
     *        i.e. either "top", "center", "bottom", "fill", "align" or "auto"
     * @return the YAlignment instance corresponding to the name, or null if no such alignment exists
     */
    static YAlignment get(String name) {
        return (YAlignment) get(name, nameToInstance);
    }

    private Object readResolve() {
        String name = getName();
        YAlignment yAlignment = get(name);
        return yAlignment;
    }
}
