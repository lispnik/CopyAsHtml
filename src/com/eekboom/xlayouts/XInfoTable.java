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
import java.util.*;
import java.util.List;
import java.io.Serializable;

/**
 * <code>XInfoTable</code> provides a way to iterate over ComponentInfos of a single container in a fixed, 'row-by-row'
 * ordering: Each component info in the first row is visited in order of ascending x position, then each row is visited
 * in ascending y position.
 */
class XInfoTable {
    /**
     * Maps components to ComponentInfo objects.
     * Beware: This map is shared between multiple XSingleTableLayout instances belonging to the same XTableLayout.
     */
    private final Map component2ComponentInfo;
    private final int[]         size              = new int[2];
    private final IntStreak[]   stripSize         = new IntStreak[2];
    private final Set           componentInfos    = new TreeSet(new ComponentInfoComparator());

    private boolean             isDirty           = true;

    private List                rows              = new ArrayList();

    /**
     * Create an info table for use with a single XTableSingleLayout
     * @param component2ComponentInfo containes
     */
    XInfoTable(Map component2ComponentInfo) {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        this.component2ComponentInfo = component2ComponentInfo;
        stripSize[Axis.X] = new IntStreak();
        stripSize[Axis.Y] = new IntStreak();
    }

    int getStripCount(Container container, int axis) {
        ensureClean(container);
        return size[axis];
    }

    void add(Component component, XTableConstraints constraints) {
        ComponentInfo info = new ComponentInfo(component, constraints);

        component2ComponentInfo.put(component, info);

        setDirty();
    }

    void remove(Component component) {
        ComponentInfo info = (ComponentInfo) component2ComponentInfo.remove(component);
        if(info == null) {
            throw new IllegalArgumentException("component was never added to layout: " + component);
        }

        setDirty();
    }

    void iterate(Container parent, TableIterator tableIterator) {
        ensureClean(parent);
        for(Iterator iterator = componentInfos.iterator(); iterator.hasNext();) {
            ComponentInfo info = (ComponentInfo) iterator.next();
            tableIterator.handle(info);
        }
    }

    void set(int stripAxis, int stripIndex, int componentIndex, ComponentInfo  info) {
        int x;
        int y;
        if(stripAxis == Axis.X) {
            x = stripIndex;
            y = componentIndex;
        }
        else {
            x = componentIndex;
            y = stripIndex;
        }
        set(x, y, info);
    }

    ComponentInfo get(int stripAxis, int stripIndex, int componentIndex, boolean returnSpannedComponent) {
        int x;
        int y;
        if(stripAxis == Axis.X) {
            x = stripIndex;
            y = componentIndex;
        }
        else {
            x = componentIndex;
            y = stripIndex;
        }
        return get(x, y, returnSpannedComponent);
    }

    ComponentInfo get(int x, int y, boolean returnSpannedComponent) {
        if(y >= rows.size()) {
            return null;
        }
        List row = (List) rows.get(y);
        if(x >= row.size()) {
            return null;
        }
        ComponentInfo info = (ComponentInfo) row.get(x);
        return info;
    }

    static interface TableIterator {
        void handle(ComponentInfo info);
    }

    private void setDirty() {
        if(!isDirty) {
            isDirty = true;
            componentInfos.clear();
            rows.clear();
        }
    }

    private void ensureClean(Container container) {
        if(isDirty) {
            isDirty = false;
            recalculatePositions(container);
            //recalculateSizes();
        }
    }

    private void set(int x, int y, ComponentInfo info) {
        ensureSize(rows, y);
        List row = (List) rows.get(y);
        if(row == null) {
            row = new ArrayList();
            rows.set(y, row);
        }
        ensureSize(row, x);
        row.set(x, info);
    }

    private static void ensureSize(List list, int index) {
        int oldSize = list.size();
        int addCount = index - oldSize;
        for(int i = 0; i <= addCount; ++i) {
            list.add(null);
        }
    }

    private void recalculateSizes() {
        for(int axis = 1; axis >= 0; --axis) {
            int stripCount = size[axis];
            int maxComponentCount = size[Axis.other(axis)];
            for(int stripIndex = 0; stripIndex < stripCount; ++stripIndex) {
                expandStrip(axis, stripIndex, maxComponentCount);
            }
        }
    }

    private void expandStrip(int stripAxis, int stripIndex, int toSize) {
        int expandAxis = Axis.other(stripAxis);
        int size = stripSize[stripAxis].get(stripIndex);
        boolean hasExpanded = true;
        while(hasExpanded && size < toSize) {
            hasExpanded = false;
            int index = size - 1;
            while(index >= 0 && size < toSize) {
                ComponentInfo info = get(stripAxis, stripIndex, index, true);
                if(info == null) {
                    --index;
                    continue;
                }
                index = info.position[expandAxis];
                if(canExpand(expandAxis, info)) {
                    expand(expandAxis, info);
                    hasExpanded = true;
                    int endPos = info.position[expandAxis] + info.size[expandAxis];
                    stripSize[stripAxis].maximize(stripIndex, endPos);
                    stripSize[expandAxis].maximize(endPos - 1, stripIndex + 1);
                    size = stripSize[stripAxis].get(stripIndex);
                }
                --index;
            }
        }
    }

    private void expand(int axis, ComponentInfo info) {
        int start      = info.position[axis];
        int size       = info.size[axis];

        int otherAxis  = Axis.other(axis);
        int otherStart = info.position[otherAxis];
        int otherSize  = info.size[otherAxis];
        int otherEnd   = otherStart + otherSize;

        for(int otherIndex = otherStart; otherIndex < otherEnd; ++otherIndex) {
            ComponentInfo otherInfo = get(axis, otherIndex, start + size, true);
            if(otherInfo != null) {
                move(axis, info);
            }
            set(otherAxis, otherIndex, start + size, info);
        }

        info.size[axis]++;
    }

    private boolean canExpand(int axis, ComponentInfo info) {
        int size       = info.constraints.getSize(axis);
        if(size != XTableConstraints.AUTO_SIZE) {
            return false;
        }

        int start       = info.position[axis];
        int currentSize = info.size[axis];

        int otherAxis   = Axis.other(axis);
        int otherStart  = info.position[otherAxis];
        int otherSize   = info.size[otherAxis];
        int otherEnd    = otherStart + otherSize;

        for(int otherIndex = otherStart; otherIndex < otherEnd; ++otherIndex) {
            ComponentInfo otherInfo = get(axis, otherIndex, start + currentSize, true);
            if(otherInfo != null) {
                return canMove(axis, info);
            }
        }
        return true;
    }

    private boolean canMove(int axis, ComponentInfo info) {
        int pos        = info.constraints.getPos(axis);
        if(pos != XTableConstraints.SAME && pos != XTableConstraints.NEXT) {
            return false;
        }

        int start      = info.position[axis];
        int size       = info.position[axis];

        int otherAxis  = Axis.other(axis);
        int otherStart = info.position[otherAxis];
        int otherSize  = info.size[otherAxis];
        int otherEnd   = otherStart + otherSize;

        for(int otherIndex = otherStart; otherIndex < otherEnd; ++otherIndex) {
            ComponentInfo otherInfo = get(axis, otherIndex, start + size, true);
            if(otherInfo != null) {
                return canMove(axis, info); // todo a) what the purpose of canMove() b) should "info" be "otherInfo"? c) idea complains about tail recursion
            }
        }
        return true;
    }

    private void move(int axis, ComponentInfo info) {
        int start      = info.position[axis];
        int size       = info.position[axis];

        int otherAxis  = Axis.other(axis);
        int otherStart = info.position[otherAxis];
        int otherSize  = info.size[otherAxis];
        int otherEnd   = otherStart + otherSize;

        for(int otherIndex = otherStart; otherIndex < otherEnd; ++otherIndex) {
            ComponentInfo otherInfo = get(axis, otherIndex, start + size, true);
            if(otherInfo != null) {
                move(axis, info);
            }
        }
        // @todo
        //set(otherAxis, otherIndex, start + size, info);
        info.position[axis]++;
    }

    private void recalculatePositions(Container parent) {
        Component[] components = parent.getComponents();
        size[Axis.X] = 0;
        size[Axis.Y] = 0;
        int[] lastPos = new int[2];
        int[] lastSize = new int[2];
        for(int i = 0; i < components.length; i++) {
            Component component = components[i];
            ComponentInfo info = (ComponentInfo) component2ComponentInfo.get(component);
            XTableConstraints constraints = info.constraints;
            for(int axis = 0; axis < 2; ++axis) {
                int pos = constraints.getPos(axis);
                if(pos == XTableConstraints.SAME) {
                    pos = lastPos[axis];
                }
                else if(pos == XTableConstraints.NEXT) {
                    pos = lastPos[axis] + lastSize[axis];
                }
                info.position[axis] = pos;
                int     size        = constraints.getSize(axis);
                boolean isAutoSize  = size == XTableConstraints.AUTO_SIZE;
                int     nonAutoSize = isAutoSize ? 1 : size;

                info.size[axis] = nonAutoSize;
                lastPos  [axis] = pos;
                lastSize [axis] = nonAutoSize;
                this.size[axis] = Math.max(this.size[axis], pos + nonAutoSize);
            }

            int startX = info.position[Axis.X];
            int endX   = startX + info.size[Axis.X];
            int startY = info.position[Axis.Y];
            int endY   = startY + info.size[Axis.Y];

            for(int x = startX; x < endX; ++x) {
                for(int y = startY; y < endY; ++y) {
                    set(x, y, info);
                }
            }
            for(int x = startX; x < endX; ++x) {
                stripSize[Axis.X].maximize(x, endY);
            }
            for(int y = startY; y < endY; ++y) {
                stripSize[Axis.Y].maximize(y, endX);
            }
        }

        for(int i = 0; i < components.length; i++) {
            Component component = components[i];
            ComponentInfo info = (ComponentInfo) component2ComponentInfo.get(component);
            componentInfos.add(info);
        }
    }

    private static class ComponentInfoComparator implements Comparator, Serializable {
        private ComponentInfoComparator() {
        }

        public int compare(Object o1, Object o2) {
            ComponentInfo componentInfo1 = (ComponentInfo) o1;
            ComponentInfo componentInfo2 = (ComponentInfo) o2;
            for(int axis = 1; axis >= 0; --axis) {
                int pos1 = componentInfo1.position[axis];
                int pos2 = componentInfo2.position[axis];
                if(pos1 < pos2) {
                    return -1;
                }
                if(pos1 > pos2) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
