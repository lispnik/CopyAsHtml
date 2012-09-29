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

import javax.swing.*;
import java.awt.*;

/**
 * Stores each component's constraints, minimum, maximum and preferred sizes, to avoid re-query at each layout
 */
class ComponentInfo {
    private static ClassMap _clazz2DefaultsMap = new ClassMap();

    static {
        _clazz2DefaultsMap.put(JLabel.class, new Defaults(XAlignment.START, YAlignment.CENTER, 0.0, 0.0));
        _clazz2DefaultsMap.put(AbstractButton.class, new Defaults(XAlignment.START, YAlignment.CENTER, 0.0, 0.0));
        _clazz2DefaultsMap.put(JTextField.class, new Defaults(XAlignment.FILL, YAlignment.CENTER, 1.0, 0.0));
        _clazz2DefaultsMap.put(JComboBox.class, new Defaults(XAlignment.FILL, YAlignment.CENTER, 1.0, 0.0));
    }

    final Component         component;

    final XTableConstraints constraints;

    final int[]             position         = new int[2];
    final int[]             size             = new int[2];
    final Alignment[]       alignment        = new Alignment[2];
    final double[]          weight           = new double[2];
    final int[]             minimumSize      = new int[2];
    final int[]             preferredSize    = new int[2];
    final int[]             maximumSize      = new int[2];


    /**
     * sum of the weight of all strips this cell spans, probably different from constraints' weight
     */
    final double[]          weightTotal      = new double[2];

    /**
     * amount of gap included in cell's size, can be non-zero only if component spans multiple strips
     */
    final int[]             includedGapsSize = new int[2];

    ComponentInfo(Component component, XTableConstraints constraints) {
        this.component   = component;
        this.constraints = constraints;

        // Must not call refresh() here: When a serialized component is read, it is too early. The component's UI has
        // not been initialized and a StackOverflowError occurs when JScrollBar.getMinimumSize() is called
        // (delegating to getPreferredSize() and back to getMinimumSize()).
        // refresh();
    }

    public void refresh() {
        Dimension minimumDimension   = component.getMinimumSize();
        Dimension preferredDimension = component.getPreferredSize();
        Dimension maximumDimension   = component.getMaximumSize();
        
        Defaults defaults = (Defaults) _clazz2DefaultsMap.get(component.getClass());
        
        for(int axis = 0; axis < 2; ++axis) {
            int       axisMinimumSize   = XLayouts.getSize(axis, minimumDimension);
            int       axisPreferredSize = XLayouts.getSize(axis, preferredDimension);
            int       axisMaximumSize   = XLayouts.getSize(axis, maximumDimension);

            Alignment axisAlignment     = constraints.getAlignment(axis);
            double    axisWeight        = constraints.getWeight(axis);
            
            if(defaults != null) {
                if(axisAlignment == XLayouts.getAutoAlignment(axis)) {
                    axisAlignment = defaults.alignment[axis];
                }
                if(axisWeight == XTableConstraints.AUTO_WEIGHT) {
                    axisWeight = defaults.weight[axis];
                }
            }
            else {
                boolean sizesDiffer       = axisMinimumSize != axisPreferredSize || axisPreferredSize != axisMaximumSize;
    
                if(axisAlignment == XLayouts.getAutoAlignment(axis)) {
                    if(sizesDiffer) {
                        axisAlignment = XLayouts.getFillAlignment(axis);
                    }
                    else {
                        axisAlignment = XLayouts.getAlignAlignment(axis);
                    }
                }
    
                if(axisWeight == XTableConstraints.AUTO_WEIGHT) {
                    axisWeight = sizesDiffer ? 1.0 : 0.0;
                }
            }

            minimumSize[axis]   = axisMinimumSize;
            preferredSize[axis] = axisPreferredSize;
            maximumSize[axis]   = axisMaximumSize;

            alignment[axis] = axisAlignment;
            weight[axis] = axisWeight;
        }
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ComponentInfo@");
        buffer.append(Integer.toHexString(System.identityHashCode(this)));
        buffer.append("component=");
        buffer.append(component.getClass().getName());
        buffer.append("(name=");
        buffer.append(component.getName());
        buffer.append("), constraints=");
        buffer.append(constraints);
        return buffer.toString();
    }

    private static class Defaults {
        private Alignment[] alignment = new Alignment[2];
        private double[]    weight    = new double[2];

        private Defaults(XAlignment xAlignment,  YAlignment yAlignment, double xWeight, double yWeight) {
            alignment = new Alignment[] {xAlignment,  yAlignment};
            weight = new double[] {xWeight, yWeight};
        }
    }
}
