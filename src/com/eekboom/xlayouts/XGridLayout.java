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

/**
 * <p>XGridLayout lays out components in a grid of equally sized cells, multiple containers managed by the same
 * XGridLayout instance will share the same cell dimension.</p>
 * 
 * <p><b>Gaps</b><br/>
 * Horizontal and vertical gaps between the cells in the grid can be specified (in pixels). These gaps are not applied
 * to the outer sides of the grid.</p>
 * 
 * <p><b>Grid Insets</b><br/>
 * The grid insets specify how much empty space is added at the outer sides of the grid (between the last components
 * in the grid and the container's border).<br/>
 * (This could also be achieved by adding an empty border to the container itself, but that would be more complicated
 * if another border is already present and very hard to do if that other border is controlled by look and feel
 * classes.)</p>
 * 
 * <p><b>Minimum Size</b><br/>
 * If the container is shrunk below its minimum size, by default child components will not shrink any further, but the
 * trailing components will disappear (i.e. in a left-to-right container orientation the right-most and bottom-most
 * components will be given bounds outside of the container).
 * To achieve the same behaviour as java.awt.GridLayout and let component shrink down to zero size use
 * {@link #setShrinkBelowMinimumSize(Boolean)}.<br/>  
 * The minimum width is calculated as the column count multiplied by the largest minimum width of any child component 
 * (plus horizontal gaps between components and insets). 
 * Similarly the maximum height is calculated as the row count times the maximum of all childrens' maximum heights.</p>
 * 
 * <p><b>Maximum Size</b><br/>
 * XGridLayout reports a sensible maximum size for a container (unlike java.awt.GridLayout):<br/>
 * The maximum width is calculated as the column count multiplied by the largest maximum width of any child component
 * (plus horizontal gaps between components and insets). 
 * Similarly the maximum height is calculated as the row count times the maximum of all childrens' maximum heights.</p>
 * 
 * <p><b>Grid Alignment</b><br/>
 * If the container is wider than it's maximum width then the grid of cells will be horizontally aligned using the
 * <code>xGridAlignment</code>
 * (see {@link #isGridXAlignmentSet()}, {@link #setGridXAlignment(XAlignment)}, {@link #getDefaultGridXAlignment()},
 * {@link #setDefaultGridXAlignment(XAlignment)}).<br/>
 * Of course the <code>yAlignment</code> is used on the vertical axis.<br/>
 * The default alignments are <code>start</code> for both axes, so for left-to-right container orientation the grid
 * will be anchored at the top-left of the container.<br/>
 * To achieve the same behaviour as used by java.awt.GridLayout set the grid alignments to <code>fill</code>.</p>      
 *  
 * <p><b>Component Alignment</b><br/>
 * Each cell in the grid has the same size and by default each component is resized to completely fill its cell.  
 * However any other alignment may also be used to center the component or anchor it at any edge of its cell.</p>
 * 
 * <p><b>Slack Distribution</b><br/>
 * Some extra space, called slack exists if the container's width is not evenly divisible by the column count
 * (e.g. a layout with two columns in a container width of 85 pixels).
 * By default this slack is distributed between the columns, so that component widths can differ by one pixel (42 and
 * 43 pixels in this example). (Similarly slack can also exist in vertical direction).<br/>
 * If strict equal sizes are more important than smooth resize and alignment behaviour, then  slack distribution can
 * be switched off.</p>
 * 
 * <p><b>Default Values</b><br/>
 * A XGridLayout can be created without supplying values for settings like gaps, grid align, etc.
 * The layout will then use a default value for each setting. These default values are shared between all instances
 * of XGridLayout. Default values can be changed using static accessor methods of XGridLayout. The following settings
 * have these (initial) default values:
 * <table>
 *     <tr><td>GridXAlignment</td><td>{@link XAlignment#START}</td></tr>
 *     <tr><td>GridYAlignment</td><td>{@link YAlignment#START}</td></tr>
 *     <tr><td>ComponentXAlignment</td><td>{@link XAlignment#FILL}</td></tr>
 *     <tr><td>ComponentYAlignment</td><td>{@link YAlignment#FILL}</td></tr>
 *     <tr><td>GridInsets</td><td>new {@link java.awt.Insets}(0, 0, 0, 0)</td></tr>
 *     <tr><td>DistributeSlack</td><td>true</td></tr>
 *     <tr><td>ShrinkBelowMinimumSize</td><td>false</td></tr>
 * </table>
 *  
 * 
 * <p><b>Grid Groups</b><br/>
 * A grid group bundles a set of grid layout instances, so that all layouts in the group will use the same cell size
 * and the same gaps. You will seldom - if ever - need to use grid groups explicitly. Simply using the same grid
 * layout instance for multiple containers will achieve the same effect.<br/>
 * The only need to create a grid group explicitly is when several containers with different geometry (column and row
 * count) should use the same gaps and cell size.  
 * </p>
 * 
 * <br/>Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved. <br/><b>xlayouts</b> is published under a <a
 * href="doc-files/LICENSE.txt">BSD license</a>.
 *
 * @author Stephen Kelvin
 * @noinspection SerializableHasSerializationMethods
 */
public class XGridLayout implements LayoutManager2, Serializable {
    private static final long serialVersionUID              = 2910972816630607687L;

    private static XAlignment defaultGridXAlignment         = XAlignment.START;
    private static YAlignment defaultGridYAlignment         = YAlignment.START;

    private static XAlignment defaultComponentXAlignment    = XAlignment.FILL;
    private static YAlignment defaultComponentYAlignment    = YAlignment.FILL;
    
    private static Insets     defaultGridInsets             = new Insets(0, 0, 0, 0);

    private static boolean    defaultDistributeSlack        = true;
    private static boolean    defaultShrinkBelowMinimumSize;

    /**
     * @serial
     */ 
    private int               rowCount;

    /**
     * @serial
     */ 
    private int               columnCount;

    /**
     * @serial
     */ 
    private final XGridGroup  group;

    /**
     * @serial
     */ 
    private XAlignment        gridXAlign;

    /**
     * @serial
     */ 
    private YAlignment        gridYAlign;

    /**
     * @serial
     */ 
    private Insets            gridInsets;

    /**
     * @serial
     */ 
    private XAlignment        componentXAlign;

    /**
     * @serial
     */ 
    private YAlignment        componentYAlign;

    /**
     * @serial
     */ 
    private Boolean           shrinkBelowMinimumSize;

    /**
     * @serial
     */ 
    private Boolean           distributeSlack;
    
    /**
     * <p>Creates a XGridLayout that lays out components in a single row of equally sized cells, with default gaps
     * between cells.</p>
     * <p>Each component will be aligned in its cell using the default component alignment (which is <code>fill</code> by default).</p>
     * <p>If the container is larger than the maximum size of the grid (as determined by the largest maximum size
     * of any component times the column/rowCount) then the grid as a whole is aligned in the container as specified
     * by the default grid alignment.</p>
     * 
     * @see #setGaps(int, int)
     * @see #setDefaultGaps(int, int)
     * @see #setComponentAlignment(XAlignment, YAlignment)
     * @see #setDefaultComponentAlignment(XAlignment, YAlignment)
     * @see #setGridAlignment(XAlignment, YAlignment)
     * @see #setDefaultGridAlignment(XAlignment, YAlignment)
     */
    public XGridLayout() {
        this(1, 0, new XGridGroup());
    }

    /**
     * <p>Creates a XGridLayout that lays out components in a grid of equally sized cells with the specified number of
     * rows and columns and default gaps between components.</p>
     * <p>Usually exactly one of rowCount or columnCount should be zero: If rowCount is zero, then the actual rowCount
     * will be determined by the columnCount and the number of components in the container. If columnCount is zero, then
     * the actual columnCount will be determined by the rowCount and the number of components in the container.</p>
     * <p>If both rowCount and columnCount are non-zero, then the specified columnCount is ignored.</p>
     * <p>Each component will be aligned in its cell using the default component alignment (which is <code>fill</code> by default).</p> 
     * @param rowCount the number of rows in the grid. If zero the number of rows is determined by the number of
     *                 components in the container and the specified columnCount. rowCount and columnCount must not both
     *                 be zero.
     * @param columnCount the number of columns in the grid. If zero the number of columns is determined by the number
     *                    of components in the container and the specified columnCount. rowCount and columnCount must
     *                    not both be zero.
     * 
     * @see #setGaps(int, int)
     * @see #setDefaultGaps(int, int)
     * @see #setComponentAlignment(XAlignment, YAlignment)
     * @see #setDefaultComponentAlignment(XAlignment, YAlignment)
     * @see #setGridAlignment(XAlignment, YAlignment)
     * @see #setDefaultGridAlignment(XAlignment, YAlignment)
     */
    public XGridLayout(int rowCount, int columnCount) {
        this(rowCount, columnCount, new XGridGroup());
    }

    /**
     * <p>Creates a XGridLayout that lays out components in a grid of equally sized cells with the specified number of
     * rows and columns and the specified gaps between components.</p>
     * <p>Usually exactly one of rowCount or columnCount should be zero: If rowCount is zero, then the actual rowCount
     * will be determined by the columnCount and the number of components in the container. If columnCount is zero, then
     * the actual columnCount will be determined by the rowCount and the number of components in the container.</p>
     * <p>If both rowCount and columnCount are non-zero, then the specified columnCount is ignored.</p>
     * <p>Each component will be aligned in its cell using the default component alignment (which is <code>fill</code> by default).</p> 
     * @param rowCount the number of rows in the grid. If zero the number of rows is determined by the number of
     *                 components in the container and the specified columnCount. rowCount and columnCount must not both
     *                 be zero.
     * @param columnCount the number of columns in the grid. If zero the number of columns is determined by the number
     *                    of components in the container and the specified columnCount. rowCount and columnCount must
     *                    not both be zero.
     * @param xGap Horizontal space in pixels between cells in the grid (will not be applied to the outer sides of the
     *             grid). Must not be negativ.
     * @param yGap Vertical space in pixels between cells in the grid (will not be applied to the outer sides of the
     *             grid). Must not be negativ.
     * @see #setGaps(int, int)
     * @see #setDefaultGaps(int, int)
     * @see #setComponentAlignment(XAlignment, YAlignment)
     * @see #setDefaultComponentAlignment(XAlignment, YAlignment)
     * @see #setGridAlignment(XAlignment, YAlignment)
     * @see #setDefaultGridAlignment(XAlignment, YAlignment)
     */
    public XGridLayout(int rowCount, int columnCount, int xGap, int yGap) {
        this(rowCount, columnCount, new XGridGroup(xGap, yGap));
    }

    /**
     * <p>Creates a XGridLayout that lays out components in a grid of equally sized cells with the specified number of
     * rows and columns. All layout instances created with the same XGridGroup will use same cell sizes. The gaps
     * between cells are determined by the grid group.</p>
     * <p>Usually exactly one of rowCount or columnCount should be zero: If rowCount is zero, then the actual rowCount
     * will be determined by the columnCount and the number of components in the container. If columnCount is zero, then
     * the actual columnCount will be determined by the rowCount and the number of components in the container.</p>
     * <p>If both rowCount and columnCount are non-zero, then the specified columnCount is ignored.</p>
     * <p>Each component will be aligned in its cell using the default component alignment (which is <code>fill</code> by default).</p> 
     * @param rowCount the number of rows in the grid. If zero the number of rows is determined by the number of
     *                 components in the container and the specified columnCount. rowCount and columnCount must not both
     *                 be zero.
     * @param columnCount the number of columns in the grid. If zero the number of columns is determined by the number
     *                    of components in the container and the specified columnCount. rowCount and columnCount must
     *                    not both be zero.
     * @param gridGroup All layouts created with the same grid group will use the same cell size and same gaps.
     *                  Must not be null. Use {@link #XGridLayout(int, int, int, int)} if this layout is not part of a
     *                  grid group.
     * @see #setGaps(int, int)
     * @see #setDefaultGaps(int, int)
     * @see #setComponentAlignment(XAlignment, YAlignment)
     * @see #setDefaultComponentAlignment(XAlignment, YAlignment)
     * @see #setGridAlignment(XAlignment, YAlignment)
     * @see #setDefaultGridAlignment(XAlignment, YAlignment)
     */
    public XGridLayout(int rowCount, int columnCount, XGridGroup gridGroup) {
        if(rowCount == 0 && columnCount == 0) {
            throw new IllegalArgumentException("rowCount and columnCount must not both be zero");
        }
        if(rowCount < 0) {
            throw new IllegalArgumentException("rowCount must not be negative");
        }
        if(columnCount < 0) {
            throw new IllegalArgumentException("rowCount must not be negative");
        }
        if(gridGroup == null) {
            throw new IllegalArgumentException("gridGroup must not be null");
        }
        if(rowCount > 0 && columnCount > 0) {
            XLayouts.warn("XGridLayout constructor invocation with both row and column count positive. columnCount will be ignored.");
        }

        this.rowCount    = rowCount;
        this.columnCount = columnCount;
        group            = gridGroup;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @return true if a specific horizontal alignment is set for this layout instance or false if the default
     *         alignment is used
     * @see #setComponentXAlignment(XAlignment) 
     * @see #getDefaultComponentXAlignment() 
     * @see #setDefaultComponentXAlignment(XAlignment)  
     */ 
    public boolean isComponentXAlignmentSet() {
        return componentXAlign != null;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @return the current horizontal alignment of each component in its grid cell - will return the default component
     *         horizontal alignment if it was not explicitly specified. Will never return null. 
     * @see #setComponentXAlignment(XAlignment) 
     * @see #getDefaultComponentXAlignment() 
     * @see #setDefaultComponentXAlignment(XAlignment)  
     */ 
    public XAlignment getComponentXAlignment() {
        return componentXAlign != null ? componentXAlign : defaultComponentXAlignment;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @param componentXAlign the horizontal alignment of each component in its grid cell.
     * @return the layout itself
     */ 
    public XGridLayout setComponentXAlignment(XAlignment componentXAlign) {
        this.componentXAlign = componentXAlign;
        return this;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @return the current vertical alignment of each component in its grid cell, or null if this grid layout uses the
     *         default vertical component alignment
     * @see #setComponentYAlignment(YAlignment) 
     * @see #getDefaultComponentYAlignment() 
     * @see #setDefaultComponentYAlignment(YAlignment)  
     */ 
    public boolean isComponentYAlignmentSet() {
        return componentYAlign != null;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @return the current vertical alignment of each component in its grid cell - will return the default component
     *         vertical alignment if it was not explicitly specified. Will never return null. 
     * @see #setComponentYAlignment(YAlignment) 
     * @see #getDefaultComponentYAlignment() 
     * @see #setDefaultComponentYAlignment(YAlignment)  
     */ 
    public YAlignment getComponentYAlignment() {
        return componentYAlign != null ? componentYAlign : defaultComponentYAlignment;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @param componentYAlign the vertical alignment of each component in its grid cell.
     * @return the layout itself
     */ 
    public XGridLayout setComponentYAlignment(YAlignment componentYAlign) {
        this.componentYAlign = componentYAlign;
        return this;
    }

    /**
     * For a detailed description see the component alignment section in the {@link XGridLayout} documentation.
     * @param componentXAlign the horizontal alignment of each component in its grid cell or null if the default
     *                        alignment sould be used.
     * @param componentYAlign the vertical alignment of each component in its grid cell or null if the default
     *                        alignment sould be used.
     * @return the layout itself
     */ 
    public XGridLayout setComponentAlignment(XAlignment componentXAlign, YAlignment componentYAlign) {
        this.componentXAlign = componentXAlign;
        this.componentYAlign = componentYAlign;
        return this;
    }

    /**
     * @return the default gaps between the container's border and the child components on the outer side of the grid 
     */ 
    public static Insets getDefaultGridInsets() {
        return new Insets(defaultGridInsets.top,  defaultGridInsets.left, defaultGridInsets.bottom, defaultGridInsets.right);
    }

    /**
     * Sets the default gaps at the outer side of the grid (between the component on the outer sides of the grid and the
     * container's border). A specific grid layout instance may override this default.
     * Setting the default grid insets affects all grid layout instances that do not have grid insets set explicitly 
     * (even instances that were created previously).
     * @param defaultGridInsets insets to be used between the grid and the container's border. Must not be null.
     * @see #setGridInsets(java.awt.Insets)  
     */ 
    public static void setDefaultGridInsets(Insets defaultGridInsets) {
        if(defaultGridInsets == null) {
            throw new IllegalArgumentException("defaultGridInsets must not be null");
        }
        XGridLayout.defaultGridInsets = defaultGridInsets;
    }

    /**
     * Sets the default gaps at the outer side of the grid (between the component on the outer sides of the grid and the
     * container's border). A specific grid layout instance may override this default.
     * Setting the default grid insets affects all grid layout instances that do not have grid insets set explicitly 
     * (even instances that were created previously).
     * @param gap gap in pixels to be used between each side of the grid and the container's border.
     * @see #setGridInsets(int)   
     */ 
    public static void setDefaultGridInsets(int gap) {
        defaultGridInsets = new Insets(gap, gap, gap, gap);
    }

    /**
     * Sets the default gaps at the outer side of the grid (between the component on the outer sides of the grid and the
     * container's border). A specific grid layout instance may override this default.
     * Setting the default grid insets affects all grid layout instances that do not have grid insets set explicitly 
     * (even instances that were created previously).
     * @param top     gap in pixels to be used between the top side of the grid and the container's border.
     * @param left    gap in pixels to be used between the left side of the grid and the container's border.
     * @param bottom  gap in pixels to be used between the bottom side of the grid and the container's border.
     * @param right   gap in pixels to be used between the right side of the grid and the container's border.
     * @see #setGridInsets(int, int, int, int)   
     */
    public static void setDefaultGridInsets(int top, int left, int bottom, int right) {
        defaultGridInsets = new Insets(top, left, bottom, right);
    }

    /**
     * For a detailed description see the "Default Values" and the "Grid Insets" section in the {@link XGridLayout}
     * documentation.
     * @return true if specific grid insets have been set for this layout or false if the default insets are used. 
     *         specified for this grid layout instance or null if the default gaps are used
     * @see #getDefaultGridInsets()  
     */ 
    public boolean isGridInsetsSet() {
        return gridInsets != null;
    }

    /**
     * For a detailed description see the "Grid Insets" section in the {@link XGridLayout} documentation.
     * @return the actual gaps between the container's border and the child components on the outer side of the grid.
     *         Will return the default grid insets if no insets are specified explicitly for this grid layout instance.
     * @see #isGridInsetsSet()  
     * @see #getDefaultGridInsets()  
     */ 
    public Insets getGridInsets() {
        Insets insets = gridInsets != null ? gridInsets : defaultGridInsets;
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    /**
     * Sets the gaps at the outer side of the grid (between the component on the outer sides of the grid and the
     * container's border). 
     * For a detailed description see the "Grid Insets" section in the {@link XGridLayout} documentation.
     * @param gridInsets insets to be used between the grid and the container's border. May be null in which case the
     *                   default grid insets are used.
     * @return the layout itself
     * @see #setDefaultGridInsets(java.awt.Insets)  
     */ 
    public XGridLayout setGridInsets(Insets gridInsets) {
        this.gridInsets = gridInsets;
        return this;
    }

    /**
     * Sets the gaps at the outer sides of the grid (between the component on the outer sides of the grid and the
     * container's border). 
     * For a detailed description see the "Grid Insets" section in the {@link XGridLayout} documentation.
     * @param gap gap in pixels between the grid and the container's border.
     * @return the layout itself
     * @see #setGridInsets(java.awt.Insets)  
     * @see #setDefaultGridInsets(java.awt.Insets)  
     */ 
    public XGridLayout setGridInsets(int gap) {
        gridInsets = new Insets(gap, gap, gap, gap);
        return this;
    }

    /**
     * Sets the gaps at the outer side of the grid (between the component on the outer sides of the grid and the
     * container's border).
     * For a detailed description see the "Grid Insets" section in the {@link XGridLayout} documentation.
     * @param top     gap in pixels to be used between the top side of the grid and the container's border.
     * @param left    gap in pixels to be used between the left side of the grid and the container's border.
     * @param bottom  gap in pixels to be used between the bottom side of the grid and the container's border.
     * @param right   gap in pixels to be used between the right side of the grid and the container's border.
     * @return the layout itself
     * @see #setDefaultGridInsets(int, int, int, int)   
     */
    public XGridLayout setGridInsets(int top, int left, int bottom, int right) {
        gridInsets = new Insets(top, left, bottom, right);
        return this;
    }

    /**
     * For a detailed description see the "Gaps" and "Default Values" sections in the {@link XGridLayout} documentation.
     * @return the horizontal gap in pixels between cells in the grid for layouts that do not specify this gap explicitly
     */ 
    public static int getDefaultXGap() {
        return XGridGroup.getDefaultXGap();
    }

    /**
     * Sets the horizontal gap between cells in the grid for layouts that do not specify this gap explicitly.
     * The gaps will be set for all layout instances in the same group
     * For a detailed description see the "Gaps" and "Default Values" sections in the {@link XGridLayout} documentation.
     * @param defaultXGap the horizontal gap in pixels between cells in the layout. Will not be applied at the outer
     *                    sides of the grid.  
     * @see XGridGroup
     */ 
    public static void setDefaultXGap(int defaultXGap) {
        XGridGroup.setDefaultXGap(defaultXGap);
    }

    /**
     * For a detailed description see the "Gaps" and "Default Values" sections in the {@link XGridLayout} documentation.
     * @return the vertical gap in pixels between cells in the grid for layouts that do not specify this gap explicitly
     */ 
    public static int getDefaultYGap() {
        return XGridGroup.getDefaultYGap();
    }
    
    /**
     * Sets the vertical gap between cells in the grid for layouts that do not specify this gap explicitly
     * The gaps will be set for all layout instances in the same group
     * For a detailed description see the "Gaps" and "Default Values" sections in the {@link XGridLayout} documentation.
     * @param defaultYGap the vertical gap in pixels between cells in the layout. Will not be applied at the outer
     *                    sides of the grid.  
     * @see XGridGroup
     */ 
    public static void setDefaultYGap(int defaultYGap) {
        XGridGroup.setDefaultYGap(defaultYGap);
    }

    /**
     * Sets the gaps between cells in the grid for layouts that do not specify these gaps explicitly.
     * The gaps will not be applied at the outer sides of the grid.  
     * The gaps will be set for all layout instances in the same group.
     * For a detailed description see the "Gaps" and "Default Values" sections in the {@link XGridLayout} documentation.
     * @param defaultYGap the vertical gap in pixels between cells in the layout. 
     * @param defaultXGap the vertical gap in pixels between cells in the layout.
     * @see XGridGroup
     */ 
    public static void setDefaultGaps(int defaultXGap, int defaultYGap) {
        XGridGroup.setDefaultGaps(defaultXGap, defaultYGap);
    }
    
    public boolean isXGapSet() {
        return group.isXGapSet();
    }

    /**
     * @return the horizontal gap in pixels between cells in the grid for this layout
     * @see #getDefaultXGap() 
     */ 
    public int getXGap() {
        return group.getXGap();
    }

    /**
     * Sets the horizontal gap between cells in this layout.
     * @param xGap horizontal gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @return the layout itself
     * @see #setDefaultXGap(int) 
     */ 
    public XGridLayout setXGap(Integer xGap) {
        group.setXGap(xGap);
        return this;
    }

    /**
     * Sets the horizontal gap between cells in this layout.
     * @param xGap horizontal gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @return the layout itself
     * @see #setDefaultXGap(int) 
     */ 
    public XGridLayout setXGap(int xGap) {
        group.setXGap(xGap);
        return this;
    }

    /**
     * @return the vertical gap in pixels between cells in the grid for this layout
     * @see #getDefaultYGap() 
     */ 
    public int getYGap() {
        return group.getYGap();
    }

    public boolean isYGapSet() {
        return group.isYGapSet();
    }

    /**
     * Sets the vertical gap between cells in this layout.
     * @param yGap vertical gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @return the layout itself
     * @see #setDefaultYGap(int) 
     */ 
    public XGridLayout setYGap(Integer yGap) {
        group.setYGap(yGap);
        return this;
    }

    /**
     * Sets the vertical gap between cells in this layout.
     * @param yGap vertical gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @return the layout itself
     * @see #setDefaultYGap(int) 
     */ 
    public XGridLayout setYGap(int yGap) {
        group.setYGap(yGap);
        return this;
    }

    /**
     * Sets the gaps between cells in this layout.
     * @param xGap vertical gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @param yGap vertical gap in pixels between cells in the grid for this layout or null if the default gap should
     *             be used
     * @return the layout itself
     * @see #setDefaultGaps(int, int) 
     */ 
    public XGridLayout setGaps(Integer xGap, Integer yGap) {
        group.setGaps(xGap, yGap);
        return this;
    }

    /**
     * Sets the gaps between cells in this layout.
     * @param xGap vertical gap in pixels between cells in the grid for this layout
     * @param yGap vertical gap in pixels between cells in the grid for this layout
     * @return the layout itself
     * @see #setDefaultGaps(int, int) 
     */ 
    public XGridLayout setGaps(int xGap, int yGap) {
        group.setGaps(xGap, yGap);
        return this;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @return true if a specific horizontal grid alignment is set for this layout instance or false if the default
     *         alignment is used
     * @see #getGridXAlignment() 
     * @see #getDefaultGridXAlignment() 
     */ 
    public boolean isGridXAlignmentSet() {
        return gridXAlign != null;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @return the horizontal alignment of the grid that is used when the container is wider than the layout's maximum
     * width. Will never return null, but will return the default horizontal alignment if the alignment is not set
     *  explicitly.
     * @see #isGridXAlignmentSet() 
     * @see #getDefaultGridXAlignment() 
     */ 
    public XAlignment getGridXAlignment() {
        return gridXAlign != null ? gridXAlign : defaultGridXAlignment;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @param gridXAlign the horizontal alignment of the grid that is used when the container is wider than the
     *                   layout's maximum width or null if the default alignment should be used.
     * @see #setDefaultGridXAlignment(XAlignment) 
     */ 
    public void setGridXAlignment(XAlignment gridXAlign) {
        this.gridXAlign = gridXAlign;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @return true if a specific vertical grid alignment is set for this layout instance or false if the default
     *         alignment is used
     * @see #getGridYAlignment() 
     * @see #getDefaultGridYAlignment() 
     */ 
    public boolean isGridYAlignmentSet() {
        return gridYAlign != null;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @return the vertical alignment of the grid that is used when the container is taller than the layout's maximum
     * height. Will never return null, but will return the default vertical alignment if the alignment is not set
     * explicitly.
     * @see #isGridYAlignmentSet() 
     * @see #getDefaultGridYAlignment() 
     */ 
    public YAlignment getGridYAlignment() {
        return gridYAlign != null ? gridYAlign : defaultGridYAlignment;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @param gridYAlign the vertical alignment of the grid that is used when the container is taller than the
     *                   layout's maximum height or null if the default alignment should be used.
     * @see #setDefaultGridYAlignment(YAlignment) 
     */ 
    public void setGridYAlignment(YAlignment gridYAlign) {
        this.gridYAlign = gridYAlign;
    }

    /**
     * For a detailed description see the grid alignment section in the {@link XGridLayout} documentation.
     * @param gridXAlign the horizontal alignment of the grid that is used when the container is wider than the
     *                   layout's maximum height or null if the default alignment should be used.
     * @param gridYAlign the vertical alignment of the grid that is used when the container is taller than the
     *                   layout's maximum height or null if the default alignment should be used.
     * @see #setDefaultGridAlignment(XAlignment, YAlignment) 
     */ 
    public XGridLayout setGridAlignment(XAlignment gridXAlign, YAlignment gridYAlign) {
        this.gridYAlign = gridYAlign;
        this.gridXAlign = gridXAlign;
        return this;
    }

    
    public static boolean getDefaultDistributeSlack() {
        return defaultDistributeSlack;
    }

    public static void setDefaultDistributeSlack(boolean defaultDistributeSlack) {
        XGridLayout.defaultDistributeSlack = defaultDistributeSlack;
    }

    public static boolean getDefaultShrinkBelowMinimumSize() {
        return defaultShrinkBelowMinimumSize;
    }

    public static void setDefaultShrinkBelowMinimumSize(boolean defaultShrinkBelowMinimumSize) {
        XGridLayout.defaultShrinkBelowMinimumSize = defaultShrinkBelowMinimumSize;
    }

    public boolean isDistributeSlackSet() {
        return distributeSlack != null;
    }

    public boolean getDistributeSlack() {
        return distributeSlack != null ? distributeSlack.booleanValue() : defaultDistributeSlack;
    }

    /**
     * 
     * @param distributeSlack
     * @return the layout itself
     */ 
    public XGridLayout setDistributeSlack(Boolean distributeSlack) {
        this.distributeSlack = distributeSlack;
        return this;
    }

    public boolean isShrinkBelowMinimumSizeSet() {
        return shrinkBelowMinimumSize != null;
    }

    public boolean getShrinkBelowMinimumSize() {
        return shrinkBelowMinimumSize != null ? shrinkBelowMinimumSize.booleanValue() : defaultShrinkBelowMinimumSize;
    }

    /**
     * 
     * @param shrinkBelowMinimumSize
     * @return the layout itself
     */ 
    public XGridLayout setShrinkBelowMinimumSize(Boolean shrinkBelowMinimumSize) {
        this.shrinkBelowMinimumSize = shrinkBelowMinimumSize;
        return this;
    }

    public static XAlignment getDefaultGridXAlignment() {
        return defaultGridXAlignment;
    }

    public static void setDefaultGridXAlignment(XAlignment defaultGridXAlignment) {
        if(defaultGridXAlignment == null) {
            throw new IllegalArgumentException("defaultGridXAlignment must not be null");
        }
        XGridLayout.defaultGridXAlignment = defaultGridXAlignment;
    }

    public static YAlignment getDefaultGridYAlignment() {
        return defaultGridYAlignment;
    }

    public static void setDefaultGridYAlignment(YAlignment defaultGridYAlignment) {
        if(defaultGridYAlignment == null) {
            throw new IllegalArgumentException("defaultGridYAlignment must not be null");
        }
        XGridLayout.defaultGridYAlignment = defaultGridYAlignment;
    }

    public static void setDefaultGridAlignment(XAlignment defaultGridXAlignment, YAlignment defaultGridYAlignment) {
        setDefaultGridXAlignment(defaultGridXAlignment);
        setDefaultGridYAlignment(defaultGridYAlignment);
    }

    public static XAlignment getDefaultComponentXAlignment() {
        return defaultComponentXAlignment;
    }

    public static void setDefaultComponentXAlignment(XAlignment defaultComponentXAlignment) {
        if(defaultComponentXAlignment == null) {
            throw new IllegalArgumentException("defaultComponentXAlignment must not be null");
        }
        XGridLayout.defaultComponentXAlignment = defaultComponentXAlignment;
    }

    public static YAlignment getDefaultComponentYAlignment() {
        return defaultComponentYAlignment;
    }

    public static void setDefaultComponentYAlignment(YAlignment defaultComponentYAlignment) {
        if(defaultComponentYAlignment == null) {
            throw new IllegalArgumentException("defaultComponentYAlignment must not be null");
        }
        XGridLayout.defaultComponentYAlignment = defaultComponentYAlignment;
    }

    public static void setDefaultComponentAlignment(XAlignment defaultComponentXAlignment,
                                                    YAlignment defaultComponentYAlignment)
    {
        setDefaultComponentXAlignment(defaultComponentXAlignment);
        setDefaultComponentYAlignment(defaultComponentYAlignment);
    }

    public int getRowCount() {
        return rowCount;
    }

    /**
     * 
     * @param rowCount
     * @return the layout itself
     */ 
    public XGridLayout setRowCount(int rowCount) {
        this.rowCount = rowCount;
        return this;
    }

    public int getColumnCount() {
        return columnCount;
    }

    /**
     * @param columnCount
     * @return the layout itself
     */ 
    public XGridLayout setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        return this;
    }

    public void removeLayoutComponent(Component component) {
        group.checkContainer(component.getParent());
    }

    public void addLayoutComponent(String name, Component component) {
        group.checkContainer(component.getParent());
    }

    public void layoutContainer(Container container) {
        group.checkContainer(container);
        synchronized(container.getTreeLock()) {
            int    componentCount = container.getComponentCount();

            if(componentCount == 0) {
                return;
            }

            ComponentOrientation componentOrientation = container.getComponentOrientation();
            int    rowCount       = this.rowCount    != 0 ? this.rowCount : (componentCount + columnCount - 1) / columnCount;
            int    columnCount    = this.columnCount != 0 && this.rowCount == 0
                     ? this.columnCount : (componentCount + this.rowCount - 1) / this.rowCount;

            Insets     containerInsets        = container.getInsets();
            Insets     gridInsets             = getGridInsets();
            int        leftInsets             = containerInsets.left   + gridInsets.left;
            int        rightInsets            = containerInsets.right  + gridInsets.right;
            int        topInsets              = containerInsets.top    + gridInsets.top;
            int        bottomInsets           = containerInsets.bottom + gridInsets.bottom;
            int        xInsets                = leftInsets + rightInsets;
            int        yInsets                = bottomInsets + topInsets;
                                              
            int        containerWidth         = container.getWidth()  - xInsets;
            int        containerHeight        = container.getHeight() - yInsets;
            int[]      containerSizes         = {containerWidth, containerHeight};
            int[]      gridSizes              = new int[2];
            int[]      stripCounts            = {columnCount, rowCount};
            
            boolean    shrinkBelowMinimumSize = getShrinkBelowMinimumSize();

            
            for(int axis = 0; axis < 2; ++axis) {
                int stripCount        = stripCounts[axis];
                int containerSize     = containerSizes[axis];
                int preferredCellSize = group.getMaxPreferredSize(axis);
                int gap               = group.getGap(axis);
                int preferredGridSize = preferredCellSize * stripCount + (stripCount - 1) * gap;
                int gridSize          = preferredGridSize;
                if(containerSize < gridSize) {
                    int minimumGridSize = shrinkBelowMinimumSize ? 0 : group.getMaxMinimumSize(axis);
                    gridSize = Math.max(containerSize, minimumGridSize);
                }
                if(containerSize > gridSize) {
                    Alignment fillAlignment = XLayouts.getFillAlignment(axis);
                    boolean isFillAlignment = fillAlignment == getGridAlignment(axis);
                    
                    int maximumGridSize = isFillAlignment ? 11 : 11; // @todo
                }

                gridSizes[axis] = gridSize;
            }
                                              
            XAlignment gridXAlign             = getGridXAlignment();
            YAlignment gridYAlign             = getGridYAlignment();
                                              
            XAlignment componentXAlign        = getComponentXAlignment();
            YAlignment componentYAlign        = getComponentYAlignment();
                                              
            Dimension  minimumSize            = getLayoutSize(group.getMaxMinimumSize(), container, false);
            Dimension  preferredSize          = getLayoutSize(group.getMaxPreferredSize(), container, false);
            Dimension  maximumSize            = getLayoutSize(group.getMaxMaximumSize(), container, false);
                                              
            if(shrinkBelowMinimumSize) {
                minimumSize.width = Math.min(containerWidth, minimumSize.width);
                minimumSize.height = Math.min(containerHeight, minimumSize.height);
            }
            if(XAlignment.FILL == gridXAlign) {
                maximumSize.width = Math.max(containerWidth, maximumSize.width);
            }
            if(YAlignment.FILL == gridYAlign) {
                maximumSize.height = Math.max(containerHeight, maximumSize.height);
            }
            
            int     gridWidth = containerWidth; //preferredSize.width;
            gridWidth = Math.max(minimumSize.width, gridWidth);
            gridWidth = Math.min(maximumSize.width, gridWidth);
            
            int     gridHeight = containerHeight; //preferredSize.height;
            gridHeight = Math.max(minimumSize.height, gridHeight);
            gridHeight = Math.min(maximumSize.height, gridHeight);
            
            
            Rectangle  cellBounds             = new Rectangle(leftInsets, topInsets, gridWidth, gridHeight);

            gridXAlign.align(cellBounds, preferredSize.width, maximumSize.width, container, componentOrientation);
            gridYAlign.align(cellBounds, preferredSize.height, maximumSize.height, container, componentOrientation);

            int       startX                   = cellBounds.x;
            int       startY                   = cellBounds.y;
            int       groupWidth               = cellBounds.width;
            int       groupHeight              = cellBounds.height;

            int       xGap                     = group.getXGap();
            int       yGap                     = group.getYGap();
            int       netWidth                 = groupWidth  - (columnCount - 1) * xGap;
            int       netHeight                = groupHeight - (rowCount - 1)    * yGap;

            int       lastColumnWidth          = netWidth    / columnCount;
            int       lastRowHeight            = netHeight   / rowCount;

            double    remainingWidth           = netWidth;
            int       remainingComponentCountX = columnCount;

            Dimension maxMinimumSize           = group.getMaxMinimumSize();

            boolean   isLeftToRight            = container.getComponentOrientation().isLeftToRight();

            boolean   distributeSlack          = getDistributeSlack();

            for(int columnIndex = 0, x = startX;
            columnIndex < columnCount; --remainingComponentCountX, ++columnIndex, x += lastColumnWidth + xGap) {
                if(distributeSlack) {
                    lastColumnWidth = (int) Math.round(remainingWidth / remainingComponentCountX);
                    if(!shrinkBelowMinimumSize) {
                        lastColumnWidth = Math.max(lastColumnWidth, maxMinimumSize.width);
                    }
                    remainingWidth -= lastColumnWidth;
                }
                int    remainingComponentCountY = rowCount;
                double remainingHeight          = netHeight;
                for(int rowIndex = 0, y = startY;
                rowIndex < rowCount; --remainingComponentCountY, ++rowIndex, y += lastRowHeight + yGap) {
                    if(distributeSlack) {
                        lastRowHeight = (int) Math.round(remainingHeight / remainingComponentCountY);
                        if(!shrinkBelowMinimumSize) {
                            lastRowHeight = Math.max(lastRowHeight, maxMinimumSize.height);
                        }
                        remainingHeight -= lastRowHeight;
                    }
                    int componentIndex = rowIndex * columnCount
                                         + (isLeftToRight ? columnIndex : columnCount - columnIndex - 1);
                    if(componentIndex < componentCount) {
                        Component component              = container.getComponent(componentIndex);

                        Rectangle componentBounds        = new Rectangle(x, y, lastColumnWidth, lastRowHeight);
                        Dimension preferredComponentSize = component.getPreferredSize();
                        componentXAlign.align(componentBounds, preferredComponentSize.width, lastColumnWidth,
                                              component, componentOrientation);
                        componentYAlign.align(componentBounds, preferredComponentSize.height, lastRowHeight,
                                              component, componentOrientation);
                        component.setBounds(componentBounds);
                    }
                }
            }
        }
    }

    private Alignment getGridAlignment(int axis) {
        return axis == 0 ? (Alignment)getGridXAlignment() : getGridYAlignment();
    }
    
    public Dimension minimumLayoutSize(Container container) {
        group.checkContainer(container);
        Dimension maxMinimumSize   = group.getMaxMinimumSize();
        return getLayoutSize(maxMinimumSize, container, true);
    }

    public Dimension preferredLayoutSize(Container container) {
        group.checkContainer(container);

        Dimension maxPreferredSize  = group.getMaxPreferredSize();
        return getLayoutSize(maxPreferredSize, container, true);

    }

    public Dimension maximumLayoutSize(Container container) {
        group.checkContainer(container);

        Dimension maxMaximumSize  = group.getMaxMaximumSize();
        return getLayoutSize(maxMaximumSize, container, true);
    }

    private Dimension getLayoutSize(Dimension cellSize, Container container, boolean includeInsets) {
        int       cellWidth         = cellSize.width;
        int       cellHeight        = cellSize.height;

        int       ownComponentCount = container.getComponentCount();
        int       rowCount          = this.rowCount    != 0
                                       ? this.rowCount : (ownComponentCount + columnCount - 1) / columnCount;
        int       columnCount       = this.columnCount != 0 && this.rowCount == 0
                                       ? this.columnCount : (ownComponentCount + this.rowCount - 1) / this.rowCount;

        int       xGap              = group.getXGap();
        int       yGap              = group.getYGap();
        int       containerWidth    = columnCount * cellWidth  + (columnCount - 1) * xGap;
        int       containerHeight   = rowCount    * cellHeight + (rowCount - 1)    * yGap;
        
        if(includeInsets) {
            Insets    containerInsets   = container.getInsets();
            Insets    gridInsets        = getGridInsets();
            int       xInsets           = containerInsets.left + containerInsets.right  + gridInsets.left + gridInsets.right;
            int       yInsets           = containerInsets.top  + containerInsets.bottom + gridInsets.top  + gridInsets.bottom;

            containerWidth += xInsets;
            containerHeight += yInsets;
        }

        return new Dimension(containerWidth, containerHeight);
    }

    public void addLayoutComponent(Component component, Object constraints) {
        group.checkContainer(component.getParent());
    }

    public float getLayoutAlignmentX(Container container) {
        group.checkContainer(container);
        return 0.0f;
    }

    public float getLayoutAlignmentY(Container container) {
        group.checkContainer(container);
        return 0.0f;
    }

    public void invalidateLayout(Container container) {
        group.invalidateLayout(container);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XGridLayout@");
        buffer.append(Integer.toHexString(System.identityHashCode(this)));
        buffer.append("[columnCount=");
        buffer.append(columnCount);
        buffer.append(",rowCount=");
        buffer.append(rowCount);
        buffer.append(",group=");
        buffer.append(group);
        buffer.append(",componentXAlign=");
        buffer.append(componentXAlign);
        buffer.append(",componentYAlign=");
        buffer.append(componentYAlign);
        buffer.append(",gridXAlign=");
        buffer.append(gridXAlign);
        buffer.append(",gridYAlign=");
        buffer.append(gridYAlign);
        buffer.append(",distributeSlack=");
        buffer.append(distributeSlack);
        buffer.append(",shrinkBelowMinimumSize=");
        buffer.append(shrinkBelowMinimumSize);
        buffer.append(']');
        return new String(buffer);
    }
}
