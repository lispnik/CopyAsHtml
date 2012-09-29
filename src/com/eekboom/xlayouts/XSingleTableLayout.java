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
import java.util.Arrays;
import java.util.Map;

class XSingleTableLayout {
    private final XInfoTable infoTable;

    /**
     * total amount of gap betwen strips for the current layout. Can be different from gap[axis] * (_stripCount[axis] -
     * 1), because there will be no gap when a column is empty or contains spanning components only.
     */
    private final int[]      gapTotal                = new int[2];

    /**
     * (absolute) weight of each row/column
     */
    private double[][]       stripWeights            = new double[2][0];

    /**
     * the sum of all row/column weights. Used to calculate each strip's relative weight
     */
    private final double[]   totalWeight             = new double[2];

    /**
     * each strip's minimum size, will be the maximum of all minimum sizes of components in that strip. a component that
     * spans multiple strips contributes to each strip's minimum size proportionally to the weight of the strip in
     * respect to the sum of the weights of all spanned strips.
     */
    private int[][]          minimumStripSizes       = new int[2][0];

    /**
     * each strip's preferred size, will be the maximum of all preferred size of components in that strip. a component
     * that spans multiple strips contributes to each strip's preferred size proportionally to the weight of the strip
     * in respect to the sum of the weights of all spanned columns.
     */
    private int[][]          preferredStripSizes     = new int[2][0];

    /**
     * Each strip's maximum width, will be the minimum of all maximum sizes of components in that strip, but never
     * smaller than the largest minimum size of any component. So minimum sizes take precedence: If component A has a
     * minimum size of 200 and the only other component B has a maximum size of 100 then the strip will have size 200
     * and B will be aligned according to its constraints. A component that spans multiple strips contributes to each
     * strip's maximum size proportionally to the weight of the strip in respect to the sum of the weights of all
     * spanned strips.
     */
    private int[][]          maximumStripSizes       = new int[2][0];

    /**
     * stripHasGap[axis][i] means there should be a gap of gap[axis] pixels in front of strip i. There won't be a gap if
     * no component ends in that strip.
     */
    private boolean[][]      stripHasGap             = new boolean[2][0];

    /**
     * sum of all minimum strip sizes
     */
    private final int[]      minimumStripSizeTotal   = new int[2];

    /**
     * sum of all preferred strip sizes
     */
    private final int[]      preferredStripSizeTotal = new int[2];

    /**
     * sum of all maximum strip sizes
     */
    private final long[]     maximumStripSizeTotal   = new long[2];

    /**
     * flag to indicate that weights and gaps have to be recalculated before layout. Set whenever a component is added
     * or removed.
     */
    private boolean          dirtyWeightsAndGaps     = true;
    /**
     * flag to indicate that minimum and preferred column widths and row heights have to be recalculated before layout.
     * Set whenever a component is added or removed or when <code>invalidate()</code> is called.
     */
    private boolean          dirtySizesComponents    = true;
    /**
     * flag to indicate that every component's minimum, preferred and maximum size has to be re-queried before layout.
     * Set whenever <code>invalidate()</code> is called.
     */
    private boolean          dirtySizesRowsColumns   = true;

    XSingleTableLayout(Map componentKey2ComponentInfo) {
        infoTable = new XInfoTable(componentKey2ComponentInfo);
    }

    /**
     * Convenience method for adding a component. The XTableConstraints(String) constructor is called with the given
     * text and the component is added to the layout with the resulting constraints object.
     *
     * @param constraintText the string that describes the constraints to be associated with the component
     * @param comp           the component to be added
     * @see XTableConstraints#XTableConstraints(String)
     */
    void addLayoutComponent(String constraintText, Component comp) {
        XTableConstraints constraints = new XTableConstraints(constraintText);
        addLayoutComponent(comp, constraints);
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param component the component to be removed
     */
    void removeLayoutComponent(Component component) {
        infoTable.remove(component);

        setDirtyWeightsAndGaps();
    }

    /**
     * Calculates the preferred size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out, must always be the same container
     */
    Dimension preferredLayoutSize(Container parent, boolean reportMinimumSizeAsPreferred, Insets tableInsets, int[] gap) {
        if(reportMinimumSizeAsPreferred) {
            return minimumLayoutSize(parent, tableInsets, gap);
        }

        refreshSizes(parent, gap);

        Insets insets = parent.getInsets();
        int    preferredWidth = getPreferredLayoutSize(insets, Axis.X, tableInsets);
        int    preferredHeight = getPreferredLayoutSize(insets, Axis.Y, tableInsets);

        return new Dimension(preferredWidth, preferredHeight);
    }

    private int getPreferredLayoutSize(Insets insets, int axis, Insets tableInsets) {
        return preferredStripSizeTotal[axis] + gapTotal[axis] + XLayouts.get(axis, insets) + XLayouts.get(axis,
                                                                                                          tableInsets);
    }

    /**
     * Calculates the minimum size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out, must always be the same container
     */
    Dimension minimumLayoutSize(Container parent, Insets tableInsets, int[] gap) {
        refreshSizes(parent, gap);

        Insets insets = parent.getInsets();
        int    minimumWidth = getMinimumLayoutSize(insets, Axis.X, tableInsets);
        int    minimumHeight = getMinimumLayoutSize(insets, Axis.Y, tableInsets);

        return new Dimension(minimumWidth, minimumHeight);
    }

    private int getMinimumLayoutSize(Insets insets, int axis, Insets tableInsets) {
        return minimumStripSizeTotal[axis] + gapTotal[axis] + XLayouts.get(axis, insets) + XLayouts.get(axis,
                                                                                                        tableInsets);
    }

    /**
     * Calculates the maximum size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out, must always be the same container
     */
    Dimension maximumLayoutSize(Container parent, Insets tableInsets, int[] gap) {
        refreshSizes(parent, gap);

        Insets insets = parent.getInsets();
        int    maximumWidth = getMaximumLayoutSize(insets, Axis.X, tableInsets);
        int    maximumHeight = getMaximumLayoutSize(insets, Axis.Y, tableInsets);

        return new Dimension(maximumWidth, maximumHeight);
    }

    private int getMaximumLayoutSize(Insets insets, int axis, Insets tableInsets) {
        long maximumWidthLong = maximumStripSizeTotal[axis] + gapTotal[axis] + XLayouts.get(axis, insets) + XLayouts
                .get(axis, tableInsets);

        int  maximumWidth = (int) Math.min(Integer.MAX_VALUE, maximumWidthLong);
        return maximumWidth;
    }

    /**
     * Lays out the specified container.
     *
     * @param container the container to be laid out, must always be the same container
     */
    void layoutContainer(Container container, Insets tableInsets, boolean reportMinimumSizeAsPreferred, XAlignment tableXAlign, YAlignment tableYAlign, int[] gap) {
        if(container.getComponentCount() == 0) {
            return;
        }

        // make sure the currently cached minimum and preferred widths and rows are up-to-date
        refreshSizes(container, gap);

        ComponentOrientation componentOrientation = container.getComponentOrientation();
        Insets    insets = container.getInsets();

        int       leftInsets = insets.left + tableInsets.left;
        int       topInsets = insets.top + tableInsets.top;
        int       rightInsets = insets.right + tableInsets.right;
        int       bottomInsets = insets.bottom + tableInsets.bottom;

        int       insetsX = leftInsets + rightInsets;
        int       insetsY = topInsets + bottomInsets;

        int       nettoWidth = container.getWidth() - insetsX;
        int       nettoHeight = container.getHeight() - insetsY;

        int[]     containerSize = new int[]{nettoWidth, nettoHeight};

        Rectangle tableBounds = new Rectangle(leftInsets, topInsets, nettoWidth, nettoHeight);

        int[][]   stripSizes = new int[2][];

        // calculate strip sizes
        for(int axis = 0; axis < 2; ++axis) {
            if(containerSize[axis] <= minimumStripSizeTotal[axis] + gapTotal[axis]) {
                stripSizes[axis] = minimumStripSizes[axis];
            }
            else if(containerSize[axis] >= maximumStripSizeTotal[axis] + gapTotal[axis]) {
                stripSizes[axis] = maximumStripSizes[axis];

                Dimension preferredSize = preferredLayoutSize(container, reportMinimumSizeAsPreferred, tableInsets, gap);
                tableXAlign.align(tableBounds, preferredSize.width, nettoWidth, container, componentOrientation);
                tableYAlign.align(tableBounds, preferredSize.height, nettoHeight, container, componentOrientation);
            }
            else {
                int   extraSize = containerSize[axis] - preferredStripSizeTotal[axis] - gapTotal[axis];
                stripSizes[axis] = calculateStripSizesFromExtraSize(container, axis, extraSize);
            }
        }

        // reshape components
        ReshapeIterator reshapeIterator = new ReshapeIterator(componentOrientation, tableBounds, stripSizes, gap);
        infoTable.iterate(container, reshapeIterator);
    }

    private int[] calculateStripSizesFromExtraSize(Container container, int axis, int extraSize) {
        int stripCount = infoTable.getStripCount(container, axis);
        int[] newStripSizes = new int[stripCount];
        boolean[] stripDone = new boolean[stripCount];
        double  totalWeight = this.totalWeight[axis];

        double slack = 0.0;
        int newExtraSize = extraSize;
        double newTotalWeight = 0.0;
        boolean first = true;
        boolean recalc;
        do {
            recalc = false;
            for(int stripIndex = 0; stripIndex < stripCount; ++stripIndex) {
                if(!stripDone[stripIndex]) {
                    double stripWeight = stripWeights[axis][stripIndex];
                    int preferredStripSize = first ? preferredStripSizes[axis][stripIndex] : newStripSizes[stripIndex];
                    if(stripWeight == 0.0) {
                        // make strips that do not change size get their preferred size
                        newStripSizes[stripIndex] = preferredStripSize;
                        stripDone[stripIndex] = true;
                    }
                    else {
                        double extraStripSize = extraSize * (stripWeight / totalWeight);
                        double scaledSizeExact = preferredStripSize + extraStripSize + slack;
                        int    scaledSize = (int) Math.round(scaledSizeExact);
                        slack = scaledSizeExact - scaledSize;
                        int minimumStripSize = minimumStripSizes[axis][stripIndex];
                        int maximumStripSize = maximumStripSizes[axis][stripIndex];
                        if(scaledSize < minimumStripSize) {
                            stripDone[stripIndex] = true;
                            newStripSizes[stripIndex] = minimumStripSize;
                            newExtraSize -= minimumStripSize - preferredStripSize;
                            recalc = true;
                        }
                        else if(scaledSize > maximumStripSize) {
                            stripDone[stripIndex] = true;
                            newStripSizes[stripIndex] = maximumStripSize;
                            newExtraSize -= maximumStripSize - preferredStripSize;
                            recalc = true;
                        }
                        else {
                            newStripSizes[stripIndex] = scaledSize;
                            newExtraSize -= scaledSize - preferredStripSize;

                            newTotalWeight += stripWeights[axis][stripIndex];
                        }
                    }
                }
            }
            extraSize = newExtraSize;
            totalWeight = newTotalWeight;
            newTotalWeight = 0.0;
            first = false;
        }
        while(recalc);

        return newStripSizes;
    }

    /**
     * Adds the specified component to the layout, using the specified constraint object.
     *
     * @param component   the component to be added
     * @param constraints where/how the component is added to the layout.
     */
    void addLayoutComponent(Component component, XTableConstraints constraints) {
        // may throw an exception, be sure to call before changing state (e.g. lastPos)
        infoTable.add(component, constraints);

        setDirtyWeightsAndGaps();
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
     */
    void invalidateLayout() {
        setDirtySizes();
    }

    //
    //
    // Helper methods
    //
    //

    /**
     * Called whenever a component is added or removed
     */
    private void setDirtyWeightsAndGaps() {
        dirtyWeightsAndGaps = true;
        dirtySizesRowsColumns = true;
    }

    /**
     *
     */
    private void setDirtySizes() {
        dirtySizesComponents = true;
        dirtySizesRowsColumns = true;
    }

    /**
     * Ensure that the current strip sizes and gaps are up-to-date.
     */
    private void refreshWeightsAndGaps(Container container, final int[] gap) {
        if(!dirtyWeightsAndGaps) {
            return;
        }

        final int[][]       stripExpansionKind     = new int[2][];
        final boolean[][]   componentStartsInStrip = new boolean[2][];
        final boolean[][]   componentEndsInStrip   = new boolean[2][];

        // Clear weights and gaps.
        for(int axis = 0; axis < 2; ++axis) {
            // These are allocated here, but filled in refreshSizes()
            int stripCount = infoTable.getStripCount(container, axis);

            minimumStripSizes  [axis] = new int[stripCount];
            preferredStripSizes[axis] = new int[stripCount];
            maximumStripSizes  [axis] = new int[stripCount];

            // These are calculated here
            totalWeight[axis]  = 0.0;
            stripWeights[axis] = new double[stripCount];

            stripHasGap[axis] = new boolean[stripCount];
            gapTotal[axis]    = 0;

            stripExpansionKind    [axis] = new int[stripCount];
            componentStartsInStrip[axis] = new boolean[stripCount + 1];
            componentEndsInStrip  [axis] = new boolean[stripCount + 1];
        }

        //noinspection TooBroadScope
        final int preferExpand = 0;
        final int preferFixed  = 1;
        final int mustExpand =  2;

        // first pass:
        // * calculate strip expansion kind
        // * check which strips are followed by a gap
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                if(dirtySizesComponents) {
                    info.refresh();
                }
                for(int axis = 0; axis < 2; ++axis) {
                    int     cellStartPos        = info.position[axis];
                    int     cellEndPos          = cellStartPos + info.size[axis];
                    boolean isSpanningComponent = cellEndPos - cellStartPos > 1;
                    double  componentWeight     = info.weight[axis];
                    boolean isExpanding         = componentWeight > 0.0;

                    int expansionKind;
                    if(!isExpanding) {
                        expansionKind = preferFixed;
                    }
                    else {
                        expansionKind = isSpanningComponent ? preferExpand : mustExpand;
                    }

                    for(int stripIndex = cellStartPos; stripIndex < cellEndPos; ++stripIndex) {
                        stripExpansionKind[axis][cellStartPos]
                        = Math.max(expansionKind, stripExpansionKind[axis][cellStartPos]);
                    }

                    componentStartsInStrip[axis][cellStartPos] = true;
                    componentEndsInStrip  [axis][cellEndPos]   = true;
                }
            }
        });

        for(int axis = 0; axis < 2; ++axis) {
            int     stripCount                 = infoTable.getStripCount(container, axis);
            boolean needGapBeforeNextComponent = false;
            for(int stripIndex = 0; stripIndex < stripCount; ++stripIndex) {
                needGapBeforeNextComponent |= componentEndsInStrip[axis][stripIndex];
                if(needGapBeforeNextComponent) {
                    boolean starts = componentStartsInStrip[axis][stripIndex];
                    if(starts) {
                        stripHasGap[axis][stripIndex] = true;
                        needGapBeforeNextComponent = false;
                    }
                }
            }
        }

        final ComponentOrientation componentOrientation = container.getComponentOrientation();

        // second pass: correct expansion kind for components with non-zero weight that span only PREFER_FIXED strips
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                for(int axis = 0; axis < 2; ++axis) {
                    int     cellStartPos         = info.position[axis];
                    int     cellEndPos           = cellStartPos + info.size[axis];
                    boolean isSpanningComponent  = cellEndPos - cellStartPos > 1;
                    double  componentWeight      = info.weight[axis];
                    if(isSpanningComponent && componentWeight > 0) {
                        boolean anyStripIsExpandable = false;
                        for(int stripIndex = cellStartPos; stripIndex < cellEndPos; ++stripIndex) {
                            int expansionKind = stripExpansionKind[axis][stripIndex];
                            if(expansionKind != preferFixed) {
                                anyStripIsExpandable = true;
                                break;
                            }
                        }
                        if(!anyStripIsExpandable) {
                            // prefer trailing strip to be expandable
                            int expandableStripIndex = componentOrientation.isLeftToRight() ? cellEndPos - 1
                                                       : cellStartPos;
                            stripExpansionKind[axis][expandableStripIndex] = mustExpand;
                        }
                    }
                }
            }
        });

        // third pass: distribute weights
        // Each component's weight will be distributed equally among the strips it spans that either MUST_EXPANDmustExpand or PREFER_TO_EXPAND.
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                for(int axis = 0; axis < 2; ++axis) {
                    int cellStartPos        = info.position[axis];
                    int cellEndPos          = cellStartPos + info.size[axis];
                    int expandingStripCount = 0;
                    for(int cellPos = cellStartPos; cellPos < cellEndPos; ++cellPos) {
                        if(cellPos > cellStartPos && stripHasGap[axis][cellPos]) {
                            info.includedGapsSize[axis] += gap[axis];
                        }
                        if(stripExpansionKind[axis][cellPos] != preferFixed) {
                            ++expandingStripCount;
                        }
                    }
                    double componentWeight = info.weight[axis];
                    if(componentWeight != 0.0) {
                        double fraction             = 1.0 / expandingStripCount;
                        double componentStripWeight = fraction * componentWeight;
                        for(int cellPos = cellStartPos; cellPos < cellEndPos; ++cellPos) {
                            if(stripExpansionKind[axis][cellPos] != preferFixed) {
                                double currentStripWeight = stripWeights[axis][cellPos];
                                stripWeights[axis][cellPos] = Math.max(componentStripWeight, currentStripWeight);
                            }
                        }
                    }
                }
            }
        });

        // sum up
        for(int axis = 0; axis < 2; ++axis) {
            for(int pos = 0; pos < infoTable.getStripCount(container, axis); ++pos) {
                gapTotal   [axis] += stripHasGap [axis][pos] ? gap[axis] : 0;
                totalWeight[axis] += stripWeights[axis][pos];
            }
        }

        // fourth pass:
        // * only now I can calculate the sums of strip weights for each component
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                for(int axis = 0; axis < 2; ++axis) {
                    double weight = 0.0;
                    int    cellStartPos = info.position[axis];
                    int    cellEndPos   = cellStartPos + info.size[axis];
                    for(int cellPos     = cellStartPos; cellPos < cellEndPos; ++cellPos) {
                        weight += stripWeights[axis][cellPos];
                    }
                    info.weightTotal[axis] = weight;
                }
            }
        });

        dirtyWeightsAndGaps = false;
    }

    /**
     * Updates minimum, preferred and maximum strip sizes and total minimum, preferred and maximum sizes.
     */
    private void refreshSizes(Container container, final int[] gap) {
        if(!dirtySizesRowsColumns) {
            return;
        }

        refreshWeightsAndGaps(container, gap);

        for(int axis = 0; axis < 2; ++axis) {
            minimumStripSizeTotal[axis] = 0;
            preferredStripSizeTotal[axis] = 0;
            maximumStripSizeTotal[axis] = 0;

            Arrays.fill(minimumStripSizes[axis], 0);
            Arrays.fill(preferredStripSizes[axis], 0);
            Arrays.fill(maximumStripSizes[axis], -1);
        }

        // calculate minimum, preferred and maximum strip size

        // first pass:
        // calculate strip size for non-expanding strips using non-expanding, non-spanning components only
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                if(dirtySizesComponents) {
                    info.refresh();
                }
                for(int axis = 0; axis < 2; ++axis) {
                    double  weight   = info.weight[axis];
                    int     startPos = info.position[axis];
                    int     gridSize = info.size[axis];
                    int     endPos   = startPos + gridSize;

                    if(weight == 0.0) {
                        int    size = info.preferredSize[axis];
                        double slack = 0.0;

                        double stripFactor = 1.0 / gridSize;
                        for(int pos = startPos; pos < endPos; ++pos) {

                            double stripSizeExact = size * stripFactor + slack;
                            int    stripSize = (int) Math.round(stripSizeExact);
                            slack = stripSizeExact - stripSize;

                            int maxSize = Math.max(preferredStripSizes[axis][pos], stripSize);
                            minimumStripSizes[axis][pos] = maxSize;
                            preferredStripSizes[axis][pos] = maxSize;
                        }
                    }
                }
            }
        });

        // calculate minimum, preferred and maximum strip size
        // second pass: adjust for spanning components
        infoTable.iterate(container, new XInfoTable.TableIterator() {
            public void handle(ComponentInfo info) {
                for(int axis = 0; axis < 2; ++axis) {
                    double  weight   = info.weight[axis];
                    int     gridSize = info.size[axis];
                    int     startPos = info.position[axis];
                    int     endPos   = startPos + gridSize;

                    if(weight > 0.0) {
                        double minimumComponentSize   = info.minimumSize[axis];
                        double preferredComponentSize = info.preferredSize[axis];
                        double maximumComponentSize   = info.maximumSize[axis];

                        int gapsAndFixedStripsTotalSize = 0;
                        for(int pos = startPos; pos < endPos; ++pos) {
                            if(stripWeights[axis][pos] == 0.0) {
                                gapsAndFixedStripsTotalSize += preferredStripSizes[axis][pos];
                            }
                            if(pos > startPos && stripHasGap[axis][pos]) {
                                gapsAndFixedStripsTotalSize += gap[axis];
                            }
                        }
                        minimumComponentSize   = Math.max(0.0, minimumComponentSize   - gapsAndFixedStripsTotalSize);
                        preferredComponentSize = Math.max(0.0, preferredComponentSize - gapsAndFixedStripsTotalSize);
                        maximumComponentSize   = Math.max(0.0, maximumComponentSize   - gapsAndFixedStripsTotalSize);
                        double totalWeight = info.weightTotal[axis];

                        double minimumSizeSlack       = 0.0; // carries the delta introduced by rounding errors so far
                        double preferredSizeSlack     = 0.0;
                        double maximumSizeSlack       = 0.0;
                        for(int pos = startPos; pos < endPos; ++pos) {
                            if(totalWeight == 0.0 || stripWeights[axis][pos] > 0.0) {
                                double stripFactor = totalWeight == 0.0 ? 1.0 / gridSize : stripWeights[axis][pos]
                                                                                           / totalWeight;

                                double minimumStripSizeExact = minimumComponentSize * stripFactor + minimumSizeSlack;
                                int    minimumStripSize = (int) Math.round(minimumStripSizeExact);
                                minimumSizeSlack = minimumStripSizeExact - minimumStripSize;

                                double preferredStripSizeExact = preferredComponentSize * stripFactor
                                                                 + preferredSizeSlack;
                                int    preferredStripSize = (int) Math.round(preferredStripSizeExact);
                                preferredSizeSlack = preferredStripSizeExact - preferredStripSize;

                                double maximumStripSizeExact = maximumComponentSize * stripFactor + maximumSizeSlack;
                                int    maximumStripSize = (int) Math.round(maximumStripSizeExact);
                                maximumSizeSlack = maximumStripSizeExact - maximumStripSize;

                                minimumStripSizes[axis][pos] = Math.max(minimumStripSizes[axis][pos], minimumStripSize);
                                preferredStripSizes[axis][pos]
                                = Math.max(preferredStripSizes[axis][pos], preferredStripSize);
                                Alignment alignment = info.alignment[axis];
                                if(info.weight[axis] > 0 && alignment == XLayouts.getFillAlignment(axis)) {
                                    int currentMaximumSize = maximumStripSizes[axis][pos];
                                    if(currentMaximumSize == -1) {
                                        currentMaximumSize = Integer.MAX_VALUE;
                                    }
                                    maximumStripSizes[axis][pos] = Math.max(minimumStripSizes[axis][pos],
                                                                            Math.min(currentMaximumSize,
                                                                                     maximumStripSize));
                                }
                            }
                        }
                    }
                }
            }
        });

        for(int axis = 0; axis < 2; ++axis) {
            for(int stripIndex = 0; stripIndex < infoTable.getStripCount(container, axis); ++stripIndex) {
                if(maximumStripSizes[axis][stripIndex] == -1) {
                    maximumStripSizes[axis][stripIndex] = preferredStripSizes[axis][stripIndex];
                }

                minimumStripSizeTotal  [axis] += minimumStripSizes[axis][stripIndex];
                preferredStripSizeTotal[axis] += preferredStripSizes[axis][stripIndex];
                maximumStripSizeTotal  [axis] += maximumStripSizes[axis][stripIndex];
            }
        }

        dirtySizesComponents = false;
        dirtySizesRowsColumns = false;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XSingleTableLayout@");
        buffer.append(Integer.toHexString(System.identityHashCode(this)));
        return new String(buffer);
    }

    private class ReshapeIterator implements XInfoTable.TableIterator {
        private ComponentOrientation componentOrientation;
        private Rectangle            tableBounds;
        private int[][] stripSizes;
        private int[] gap;

        private int yRow;
        private int xColumn;

        private int lastX;
        private int lastY;

        private ReshapeIterator(ComponentOrientation componentOrientation, Rectangle tableBounds, int[][] stripSizes, int[] gap) {
            this.componentOrientation = componentOrientation;
            this.tableBounds = tableBounds;
            //noinspection AssignmentToCollectionOrArrayFieldFromParameter
            this.stripSizes = stripSizes;

            this.gap = gap;

            yRow = tableBounds.y;
            if(componentOrientation.isLeftToRight()) {
                xColumn = tableBounds.x;
            }
            else {
                xColumn = tableBounds.x + tableBounds.width;
            }
        }

        public void handle(ComponentInfo info) {
            int x = info.position[Axis.X];
            int y = info.position[Axis.Y];

            if(componentOrientation.isLeftToRight()) {
                if(lastX > x) {
                    xColumn = tableBounds.x;
                    lastX = 0;
                }
                while(lastX < x) {
                    xColumn += stripSizes[Axis.X][lastX];
                    ++lastX;
                    if(stripHasGap[Axis.X][lastX]) {
                        xColumn += gap[Axis.X];
                    }
                }
            }
            else {
                int endX = x + info.size[Axis.X];
                if(lastX > endX) {
                    xColumn = tableBounds.x + tableBounds.width;
                    lastX = 0;
                }
                while(lastX < endX) {
                    xColumn -= stripSizes[Axis.X][lastX];
                    if(stripHasGap[Axis.X][lastX]) {
                        xColumn -= gap[Axis.X];
                    }
                    ++lastX;
                }
            }
            while(lastY < y) {
                yRow += stripSizes[Axis.Y][lastY];
                ++lastY;
                if(stripHasGap[Axis.Y][lastY]) {
                    yRow += gap[Axis.Y];
                }
            }

            int               cellWidth = info.includedGapsSize[Axis.X];
            for(int xCell = x; xCell < x + info.size[Axis.X]; ++xCell) {
                cellWidth += stripSizes[Axis.X][xCell];
            }
            int cellHeight = info.includedGapsSize[Axis.Y];
            for(int yCell = y; yCell < y + info.size[Axis.Y]; ++yCell) {
                cellHeight += stripSizes[Axis.Y][yCell];
            }

            Component  component = info.component;
            Rectangle  rectangle = new Rectangle(xColumn, yRow, cellWidth, cellHeight);
            for(int axis = 0; axis < 2; ++axis) {
                Alignment alignment = info.alignment[axis];
                int       max = info.maximumSize[axis];
                int       pref = info.preferredSize[axis];

                alignment.align(rectangle, pref, max, component, componentOrientation);
            }

            component.setBounds(rectangle);
        }
    }
}

