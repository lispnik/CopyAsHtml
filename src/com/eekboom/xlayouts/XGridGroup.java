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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p><code>XGridGroup</code> can be used to constrain multiple <code>XGridLayout</code>s to use the same size for its components - even if 
 * these layouts have different characteristics (row and column count specifications).</p> 
 * 
 * <p>You rarely need to use this class explicitly: 
 * If you want multiple grids with the characteristics (e.g. each grid is a single row with variable number of columns) to use the same
 * component size it is much easier to use a single XGridLayout  instance for all containers.</p>
 *   
 * <br/>Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved.
 * <br/><b>xlayouts</b> is published under a <a href="doc-files/LICENSE.txt">BSD license</a>.
 * 
 * @author Stephen Kelvin
 */
public class XGridGroup implements Serializable {
    private static final long   serialVersionUID = 933605105870038813L;

    // Basically XGridGroup is just a collection of containers all managed by XGridLayouts where all components in the containers should
    // be equally sized.
    // Its implementation is a little more complicated because:
    // * It holds weak references to containers only.
    //   Else if a specific container is no longer used, its garbage collection would be hindered by other containers in the same group that
    //   are still used.
    // * It makes sure that reference identity semantics holds even if equals() and hashcode() have been overriden in a container's class.
    //   XGridGroup.add() is called more than once for each container but must never add a container if it is already present.
    //   (A component subclass should probably never override either equals() or hashcode() as its stands for a visual representation
    //    and is by definition different from any other component instance. Still better to safeguard.)
    
    private static int          instanceCount;

    private static int[]        defaultGaps = {4, 4}; 

    /**
     * @serial
     */ 
    private Integer[]           gaps = new Integer[2];

    /**
     * @serial
     */ 
    private String              name;

    private transient Set       containers;

    private transient int[]     maxMinimumSize;
    private transient int[]     maxPreferredSize;
    private transient int[]     maxMaximumSize;

    private transient boolean   dirty;

    private boolean             invalidating;

    public XGridGroup() {
        this(XGridLayout.getDefaultXGap(), XGridLayout.getDefaultYGap());
    }

    public XGridGroup(int xGap, int yGap) {
        this(xGap, yGap, "XGridGroup-" + instanceCount);
    }

    public XGridGroup(int xGap, int yGap, String name) {
        this(new Integer(xGap), new Integer(yGap), name);
    }

    public XGridGroup(Integer xGap, Integer yGap, String name) {
        if(xGap != null && xGap.intValue() < 0) {
            throw new IllegalArgumentException("xGap must not be negativ: " + xGap);
        }
        if(yGap != null && yGap.intValue() < 0) {
            throw new IllegalArgumentException("xGap must not be negativ: " + yGap);
        }

        gaps[Axis.X] = xGap;
        gaps[Axis.Y] = yGap;
        this.name = name;
        
        initializeTransientFields();

        ++instanceCount;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        Set containerSet = getAllContainers();
        Object[] containers = containerSet.toArray();
        stream.writeObject(containers);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializeTransientFields();
        Object[] containers = (Object[]) stream.readObject();
        for(int containerIndex = 0; containerIndex < containers.length; containerIndex++) {
            Container container = (Container) containers[containerIndex];
            add(container);
        }
    }

    private void initializeTransientFields() {
        dirty       = true;
        containers  = new HashSet();
        containers       = new HashSet();

        maxMinimumSize   = new int[2];
        maxPreferredSize = new int[2];
        maxMaximumSize   = new int[2];
    }

    private void add(Container container) {
        containers.add(new WeakIdentityHashReference(container));
    }

    private void remove(Container container) {
        containers.remove(new WeakIdentityHashReference(container));
    }

    public boolean isXGapSet() {
        return gaps[Axis.X] != null;
    }

    int getGap(int axis) {
        return gaps[axis] != null ? gaps[axis].intValue() : defaultGaps[axis];
    }

    public int getXGap() {
        return gaps[Axis.X] != null ? gaps[Axis.X].intValue() : defaultGaps[Axis.X];
    }

    public void setXGap(Integer xGap) {
        gaps[Axis.X] = xGap;
    }

    public void setXGap(int xGap) {
        gaps[Axis.X] = new Integer(xGap);
    }

    public boolean isYGapSet() {
        return gaps[Axis.Y] != null;
    }

    public int getYGap() {
        return gaps[Axis.Y] != null ? gaps[Axis.Y].intValue() : defaultGaps[Axis.Y];
    }

    public void setYGap(Integer yGap) {
        gaps[Axis.Y] = yGap;
    }

    public void setYGap(int yGap) {
        gaps[Axis.Y] = new Integer(yGap);
    }

    public XGridGroup setGaps(Integer xGap, Integer yGap) {
        gaps[Axis.X] = xGap;
        gaps[Axis.Y] = yGap;
        return this;
    }

    public XGridGroup setGaps(int xGap, int yGap) {
        gaps[Axis.X] = new Integer(xGap);
        gaps[Axis.Y] = new Integer(yGap);
        return this;
    }

    public static int getDefaultXGap() {
        return defaultGaps[Axis.X];
    }

    public static void setDefaultXGap(int defaultXGap) {
        defaultGaps[Axis.X] = defaultXGap;
    }

    public static int getDefaultYGap() {
        return defaultGaps[Axis.Y];
    }

    public static void setDefaultYGap(int defaultYGap) {
        defaultGaps[Axis.Y] = defaultYGap;
    }

    public static void setDefaultGaps(int defaultXGap, int defaultYGap) {
        defaultGaps[Axis.X] = defaultXGap;
        defaultGaps[Axis.Y] = defaultYGap;
    }

    public String getName() {
        return name;
    }

    public XGridGroup setName(String name) {
        this.name = name;
        return this;
    }

    Set getAllContainers () {
        Set resultSet = new HashSet();
        for(Iterator iterator = containers.iterator(); iterator.hasNext();) {
            WeakReference weakReference = (WeakReference) iterator.next();
            Object        container     = weakReference.get();
            if(container == null) {
                iterator.remove();
            }
            else {
                resultSet.add(container);
            }
        }
        return resultSet;
    }

    void setDirty() {
        dirty = true;
        for(int axis = 0; axis < 2 ; ++axis) {
            maxMinimumSize  [axis] = 0;
            maxPreferredSize[axis] = 0;
            maxMaximumSize  [axis] = 0;
        }
    }

    Dimension getMaxMinimumSize() {
        if(dirty) {
            computeComponentSizes();
        }
        return new Dimension(maxMinimumSize[Axis.X], maxMinimumSize[Axis.Y]);
    }
    
    int getMaxMinimumSize(int axis) {
        if(dirty) {
            computeComponentSizes();
        }
        return maxMinimumSize[axis];
    }

    Dimension getMaxPreferredSize() {
        if(dirty) {
            computeComponentSizes();
        }
        return new Dimension(maxPreferredSize[Axis.X], maxPreferredSize[Axis.Y]);
    }
    
    int getMaxPreferredSize(int axis) {
        if(dirty) {
            computeComponentSizes();
        }
        return maxPreferredSize[axis];
    }

    Dimension getMaxMaximumSize() {
        if(dirty) {
            computeComponentSizes();
        }
        return new Dimension(maxMaximumSize[Axis.X], maxMaximumSize[Axis.Y]);
    }
    
    int getMaxMaximumSize(int axis) {
        if(dirty) {
            computeComponentSizes();
        }
        return maxMaximumSize[axis];
    }

    private void computeComponentSizes() {
        for(Iterator iterator = containers.iterator(); iterator.hasNext();) {
            WeakIdentityHashReference weakContainerReference = (WeakIdentityHashReference) iterator.next();
            Container                 container              = (Container)                 weakContainerReference.get();
            if(container == null) {
                iterator.remove();
            }
            else {
                if(container.isVisible()) {
                    int componentCount = container.getComponentCount();
                    for(int componentIndex = 0; componentIndex < componentCount; ++componentIndex) {
                        Component component       = container.getComponent(componentIndex);
                        
                        if(component.isVisible()) {
                            Dimension minimumSize   = component.getMinimumSize();
                            maxMinimumSize[Axis.X] = Math.max(maxMinimumSize[Axis.X], minimumSize.width);
                            maxMinimumSize[Axis.Y] = Math.max(maxMinimumSize[Axis.Y], minimumSize.height);

                            Dimension preferredSize   = component.getPreferredSize();
                            maxPreferredSize[Axis.X] = Math.max(maxPreferredSize[Axis.X], preferredSize.width);
                            maxPreferredSize[Axis.Y] = Math.max(maxPreferredSize[Axis.Y], preferredSize.height);

                            Dimension maximumSize   = component.getMaximumSize();
                            maxMaximumSize[Axis.X] = Math.max(maxMaximumSize[Axis.X], maximumSize.width);
                            maxMaximumSize[Axis.Y] = Math.max(maxMaximumSize[Axis.Y], maximumSize.height);
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return name;
    }

    void invalidateLayout(Container container) {
        checkContainer(container);
        setDirty();
        invalidateOtherContainers(container);
    }

    private void invalidateOtherContainers(Container container) {
        if(!invalidating) {
            invalidating = true;

            try {
                for(Iterator iterator = containers.iterator(); iterator.hasNext();) {
                    WeakIdentityHashReference weakContainerReference = (WeakIdentityHashReference) iterator.next();
                    Container                 otherContainer         = (Container)                 weakContainerReference.get();
                    if(container == null) {
                        iterator.remove();
                    }
                    else {
                        if(otherContainer != container) {
                            otherContainer.invalidate();
                        }
                    }
                }
            }
            finally {
                invalidating = false;
            }
        }
    }

    void checkContainer(Container container) {
        LayoutManager layout        = container.getLayout();
        boolean       isXGridLayout = layout instanceof XGridLayout;
        if(isXGridLayout) {
            add(container);
        }
        else {
            remove(container);
        }
    }

    private static class WeakIdentityHashReference extends WeakReference {
        private WeakIdentityHashReference(Object referent) {
            super(referent);
        }

        public boolean equals(Object obj) {
            if(!(obj instanceof WeakReference)) {
                return false;
            }
            WeakReference other = (WeakReference) obj;
            return get() == other.get();
        }

        public int hashCode() {
            return System.identityHashCode(get());
        }
    }
}
