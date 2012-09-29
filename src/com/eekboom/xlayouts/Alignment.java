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
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/** @noinspection SerializableHasSerializationMethods*/
abstract class Alignment implements Serializable {
    private static final long         serialVersionUID = 3112438897942635643L;

    /**
     * @serial an alignment is identified exclusively by its concrete class, i.e. either XAlignment or YAlignment
     *         and by this field, whose values are defined by the constants declared in those subclasses
     */
    private final           String    name;
    private final transient int       axis;
    private final transient boolean   usePreferredSize;
    private final transient boolean   useComponentAlignmentFactor;
    private final transient double    alignmentFactor;
    private final transient boolean   useComponentOrientation;

    protected Alignment(String name, int axis, boolean usePreferredSize, boolean useComponentAlignmentFactor,
              double alignmentFactor, boolean useComponentOrientation) {
        this.name                        = name;
        this.axis                        = axis;
        this.usePreferredSize            = usePreferredSize;
        this.useComponentAlignmentFactor = useComponentAlignmentFactor;
        this.alignmentFactor             = alignmentFactor;
        this.useComponentOrientation     = useComponentOrientation;
    }

    /**
     * Reads the position and size given in cellBounds (only the part refering to this alignment's axis),
     * and modifies it in place, so that the component is aligned as specified in the Alignment's constructor.
     * The desired size is either prefSize or maxSize (see constructor), but at most the size given in cellBounds.
     * @param cellBounds in:  container rectangle in which the component needs to be aligned
     *                   out: desired position and size of the given component
     * @param prefSize   the component's preferred size
     * @param maxSize    the component's maximum size
     * @param component  the component that should be aligned (its alignment factor is read, when this alignment uses it).
     * @param containerComponentOrientation the container's ComponentOrientation, used only when this alignment needs it.
     */
    void align(Rectangle cellBounds, int prefSize, int maxSize, Component component,
               ComponentOrientation containerComponentOrientation) {
        int    cellPos              = XLayouts.getPos(axis, cellBounds);
        int    cellSize             = XLayouts.getSize(axis, cellBounds);

        double alignmentFactor      = useComponentAlignmentFactor
                 ? XLayouts.getAlignmentFactor(axis, component) : this.alignmentFactor;
        int    desiredComponentSize = usePreferredSize ? prefSize : maxSize;

        if(useComponentOrientation) {
            alignmentFactor = adjustAlignmentFactorToComponentOrientation(axis, alignmentFactor, containerComponentOrientation);
        }

        int    pos;
        int    size;

        if(desiredComponentSize < cellSize) {
            size = desiredComponentSize;
            int space = cellSize - size;
            pos  = cellPos + (int) (alignmentFactor * space);
        }
        else {
            pos  = cellPos;
            size = cellSize;
        }

        XLayouts.setPos(cellBounds, axis, pos);
        XLayouts.setSize(cellBounds, axis, size);
    }

    private static double adjustAlignmentFactorToComponentOrientation(int axis, double alignmentFactor,
                                                               ComponentOrientation componentOrientation) {
        if(axis == Axis.X) {
            if(!componentOrientation.isLeftToRight()) {
                double adjustedAlignmentFactor = 1.0 - alignmentFactor;
                return adjustedAlignmentFactor;
            }
        }
        return alignmentFactor;
    }

    String getName() {
        return name;
    }

    static Alignment get(String name, Map nameToInstance) {
        Alignment alignment = (Alignment) nameToInstance.get(name);
        if(alignment == null) {
            String messageText = getErrorMessage(name, nameToInstance);
            throw new IllegalArgumentException(messageText);
        }
        return alignment;
    }

    private static String getErrorMessage(String name, Map nameToInstance) {
        StringBuffer buffer       = new StringBuffer("Alignment text must be one of ");
        String       separator    = "";
        Iterator     nameIterator = nameToInstance.keySet().iterator();
        while(nameIterator.hasNext()) {
            buffer.append(separator);
            separator = ", ";
            buffer.append(nameIterator.next());
        }
        buffer.append(" but was ");
        buffer.append(name);
        String messageText = new String(buffer);
        return messageText;
    }

    public String toString() {
        return name;
    }
}
