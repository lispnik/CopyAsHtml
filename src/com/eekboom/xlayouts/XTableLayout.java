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
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>A flexible layout manager that arranges its components in a grid of variable sized rows and columns.
 * It is very similar in concept to GridBagLayout, but is much easier to use, and respects
 * components' maximum sizes.</p>
 * <p>A <b>Strip</b> refers to either a column or a row in the table - in general rows and columns are treated exactly similar.</p>
 * <p><b>Component Constraints</b><br/> are specified when a component is added and describe its position and resizing behaviour.
 * Constraints can either be specified using XTableConstraint instances or more conveniently in text form:<br/>
 * <b>x y width height xAlign yAlign xWeight yWeight</b>
 * <ul>
 * <li><b>Area: x, y, width, height</b> A component's area in the table is specified as column and row
 * indices plus its width (in columns) and height (in rows).</li>
 * <li><b>Alignment</b> Each component is aligned in its area, i.e. either anchored at any edge, centered,
 * or expanded to fill the area.</li>
 * <li><b>Weight</b> A component's weight influences how much the component's area will expand/shrink when the container
 * size changes.</li>
 * </p>
 *
 *
 */
class A {
}


/**
 * A very flexible layout manager that assigns each component to one or more cells of a grid of columns and rows.
 * Column widths and row heights depend on specified weights.<br/>
 * So <code>XTableLayout</code>'s features are very similar to <code>GridBagLayout</code>, but it is easier to use and
 * avoids some of the oddities of <code>GridBagLayout</code>:
 * <ul>
 * <li> Minimum and maximum sizes are respected: A component never gets smaller than its minimum or greater than
 * its maximum size.
 * <li> The layout reports sensible minimum and maximum sizes for its container.
 * <li> Gaps between columns and rows can be specified (but different gaps per component are not supported).
 * In contrast <code>GridBagLayout</code> allows you to specify insets per component so that gaps are around
 * components rather than between components, making separate configuration of the grid's outer insets difficult.
 * <li> Constraints can be specified using a string format and use default values for missing data
 * <li> All layout operations (e.g. adding a component) are fail-fast: An <code>IllegalArgumentException</code> is thrown
 * whenever illegal constraints or null arguments are supplied.
 * <li> When the container gets too small the right- and bottom-most will be invisible (<code>GridBagLayout</code> in
 * contrast will position components on top of each other and even make the top- and left-most component's larger
 * again when the container gets very small. Hey, in Java 5.0 this peculiar behaviour of GridBagLayout has finally been fixed!)
 * <li> The layout can be configured to report the container's minimum size as the preferred size. This can make sense if the
 * container is contained in a scroll pane and the scroll pane itself participates in a different layout:
 * When the scroll pane gets smaller the container also gets smaller until it reaches its minimum size, then scroll
 * bars will appear. When the scroll pane is larger, it will always resize the container to its own size.
 * <li> A single XTableLayout instance can only handle a single container instance. It is an error to use the same layout
 * object with different containers.
 * </ul>
 * <p/>
 * Each component has associated constraints that describe its position and resizing behaviour. Constraints are specified
 * when the component is added to its container and cannot be changed later.
 * An instance of {@link XTableConstraints} can be used, but it is much more convenient to use a string to specify the constraints:<br/>
 * <b>"x y width height xAlign yAlign xWeight yWeight"</b><br/><br/>
 * <dt><b>x, y</b>
 * <dd>Integer values that specify the component's start position in the grid. The cell at the top-left cell has
 * position x = 0, y = 0.<br/>
 * The special value "." means the same x (resp. y) position as the last component added.
 * "+" specifies the next x (resp. y) position after the last component added.
 * (So if "+" is used to specify the x position of a new component and the last component was added to x = 3
 * and had a width = 2, the new component will get x = 5.) When no component has been added so far then both
 * "+" and "." result in 0.
 * </dd><br/><br/>
 * <p/>
 * <dt><b>width, height</b>
 * <dd>The component will span <code>width</code> columns and <code>height</code> rows.
 * The default for both width and height is 1.</dd><br/><br/>
 * <p/>
 * <dt><b>xAlign</b>
 * <dd>One of "left", "center", "right" or "fill". If the component's column is wider than the component, then this
 * value is used to position the component within the column.<br/>
 * If "fill" is specified, but the column is wider than the component's maximum width, then the component is
 * positioned according to its horizontal alignment as returned from {@link java.awt.Component#getAlignmentX}.
 * (On the other hand the column will never get smaller than the component's minimum width.)<br/>
 * The default value is "left".</dd><br/><br/>
 * <p/>
 * <dt><b>yAlign</b>
 * <dd>One of "top", "center", "bottom" or "fill". If the component's row is higher than the component, then this
 * value is used to position the component within the row.<br/>
 * If "fill" is specified, but the row is higher than the component's maximum height, then the component is
 * positioned according to its vertical alignment as returned from {@link java.awt.Component#getAlignmentY}.
 * (On the other hand the row will never get smaller than the component's minimum height.)<br/>
 * The default value is "top" (but if xAlign is specified then yAlign must be specified explicitly, too).</dd><br/><br/>
 * <p/>
 * <dt><b>xWeight, yWeight</b>
 * <dd>Double values that specify the column's (resp. row's) weight.<br/>
 * When the container is wider than it's preferred width the extra width is distributed to each column proportionally
 * to the column's weight divided by the total weight.<br/>
 * The same algorithm is used if the container is smaller than it's preferred width to substract from each column's
 * preferred width.<br/><br/>
 * Basically a column's weight is the maximum of all xWeights specified for components in that column.
 * (Column weights are first calculated using non-spanning components only.
 * The xWeight of a component that spans multiple columns is then distributed to all of the columns proportionally
 * to each columns weight divided by the total weight of the columns that are spanned.)
 * If a component's xWeight is 0 then the component will always get (at least) its preferred width. If no other
 * component in the column specifies a positiv xWeight then this column will never resize. (If there is another
 * component with a positiv xWeight the column can get larger than the preferred component width and the component
 * is aligned using it's xAlign value.<br/>
 * The default value for both weights is 0.</dd><br/><br/>
 * <p/>
 * If a value is not specified (but the default is used), then all following values must not be specified also, i.e.
 * the format is:<br/>"x y [width height [xAlign yAlign [xWeight yWeight]]]"<br/>
 * <p/>
 * <br/>
 * <b>Example:</b><br/>
 * The container has three rows and two columns.
 * The first row contains a label that should never resize, but always get its preferred size. It is followed by a text
 * field that should expand horizontally only.<br/>
 * The second row contains a long label that spans both columns and also always gets its preferred size.
 * The third row contains a text area that also spans both columns, but expands both horizontally and vertically:<br/>
 * <table>
 * <tr><td nowrap valign="top"><code>panel.add(fieldLabel, "0 0");</code></td><td>top-left, use defaults for other values (i.e. width, height = 1, align to left and top, weights = 0)</td></tr>
 * <tr><td nowrap valign="top"><code>panel.add(textField, "+ . 1 1 fill top 1 0");<code></td><td>next x, same y, width and height = 1, fill horizontally, align to top, column weight = 1, row weight = 0</td></tr>
 * <tr><td nowrap valign="top"><code>panel.add(areaLabel, "0 + 2 1");</code></td><td>x = 0, next y, width = 2, height = 1 (align left and top, weights = 0) </td></tr>
 * <tr><td nowrap valign="top"><code>panel.add(textArea, "0 + 2 1 fill fill 1 1");</code></td><td>x = 0, next y, weidth = 2, height = 1, fill both horizontally and vertically, weights = 1</td></tr>
 * </table>
 * <br/>
 * <p/>
 * <br/>Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved.
 * <br/><b>xlayouts</b> is published under a <a href="doc-files/LICENSE.txt">BSD license</a>.
 *
 * @author Stephen Kelvin
 * @see XTableConstraints
 * @see java.awt.GridBagLayout
 */
public class XTableLayout implements LayoutManager2, Serializable {
    private static final long           serialVersionUID             = 3476468974678686896L;

    private static final XAlignment     DEFAULT_TABLE_X_ALIGN        = XAlignment.START;
    private static final YAlignment     DEFAULT_TABLE_Y_ALIGN        = YAlignment.START;

    private static int                  defaultXGap                  = 4;
    private static int                  defaultYGap                  = 4;

    private static Insets               defaultTableInsets           = new Insets(0, 0, 0, 0);

    private transient Map               container2SingleTableLayout;

    /**
     * gap in pixels between strips, will not be applied to the outer sides of the container
     * @serial
     */
    private int[]                       gap                          = new int[2];

    /**
     * if true the layout manager will report the container's minimum size as its preferred size,
     * useful when the container is in a scoll pane
     * @serial
     */
    private boolean                     reportMinimumSizeAsPreferred;

    /**
     * @serial
     */
    private XAlignment                  tableXAlign;
    /**
     * @serial
     */
    private YAlignment                  tableYAlign;

    private Insets                      tableInsets                  = defaultTableInsets;

    /**
     * maps a component to its info object
     */
    private transient Map               componentKey2ComponentInfo;

    /**
     * Construct a table layout that has default gaps between rows or columns, see {@link #setDefaultGaps(int, int)}
     * and returns the 'real' preferred size
     */
    public XTableLayout() {
        this(defaultXGap, defaultYGap, false, DEFAULT_TABLE_X_ALIGN, DEFAULT_TABLE_Y_ALIGN);
    }

    public XTableLayout(XAlignment tableXAlign, YAlignment tableYAlign) {
        this(defaultXGap, defaultYGap, false, tableXAlign, tableYAlign);
    }

    /**
     * Construct a table layout that has default gaps between rows or columns, see {@link #setDefaultGaps(int, int)}.
     *
     * @param reportMinimumSizeAsPreferred Should the layout use components' minimum sizes to compute its containers
     *                                     preferred size. This can make sense if the container is embedded in a
     *                                     scroll pane.
     */
    public XTableLayout(boolean reportMinimumSizeAsPreferred) {
        this(defaultXGap, defaultYGap, reportMinimumSizeAsPreferred, DEFAULT_TABLE_X_ALIGN, DEFAULT_TABLE_Y_ALIGN);
    }

    public XTableLayout(boolean reportMinimumSizeAsPreferred, XAlignment tableXAlign, YAlignment tableYAlign) {
        this(defaultXGap, defaultYGap, reportMinimumSizeAsPreferred, tableXAlign, tableYAlign);
    }

    /**
     * Construct a table layout that has the given gaps between rows and columns and returns the 'real' preferred size.
     *
     * @param xGap pixels between columns, is <b>not</b> applied to the left and right outer sides. Must not be negativ.
     * @param yGap pixels between rows, is <b>not</b> applied to the top and bottom outer sides. Must not be negativ.
     */
    public XTableLayout(int xGap, int yGap) {
        this(xGap, yGap, false, DEFAULT_TABLE_X_ALIGN, DEFAULT_TABLE_Y_ALIGN);
    }

    public XTableLayout(int xGap, int yGap, XAlignment tableXAlign, YAlignment tableYAlign) {
        this(xGap, yGap, false, tableXAlign, tableYAlign);
    }

    /**
     * Construct a table layout that has the given gaps between rows and columns.
     *
     * @param xGap                         pixels between columns, is <b>not</b> applied to the left and right outer sides. Must not be negativ.
     * @param yGap                         pixels between rows, is <b>not</b> applied to the top and bottom outer sides. Must not be negativ.
     * @param reportMinimumSizeAsPreferred Should the layout use components' minimum sizes to compute its containers
     *                                     preferred size. This can make sense if the container is embedded in a
     *                                     scroll pane.
     */
    public XTableLayout(int xGap, int yGap, boolean reportMinimumSizeAsPreferred) {
        this(xGap, yGap, reportMinimumSizeAsPreferred, DEFAULT_TABLE_X_ALIGN, DEFAULT_TABLE_Y_ALIGN);
    }

    public XTableLayout(int xGap, int yGap, boolean reportMinimumSizeAsPreferred, XAlignment tableXAlign,
                        YAlignment tableYAlign) {
        if(xGap < 0) {
            throw new IllegalArgumentException("xGap must not be negativ: " + xGap);
        }
        if(yGap < 0) {
            throw new IllegalArgumentException("xGap must not be negativ: " + yGap);
        }

        gap[Axis.X]                       = xGap;
        gap[Axis.Y]                       = yGap;
        this.reportMinimumSizeAsPreferred = reportMinimumSizeAsPreferred;
        this.tableXAlign                  = tableXAlign;
        this.tableYAlign                  = tableYAlign;

        initializeTransientFields();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        int componentCount = componentKey2ComponentInfo.size();
        Component[] components = new Component[componentCount];
        XTableConstraints[] constraints = new XTableConstraints[componentCount];
        Collection componentInfos = componentKey2ComponentInfo.values();
        int index = 0;
        Iterator iterator = componentInfos.iterator();
        while(iterator.hasNext()) {
            ComponentInfo componentInfo = (ComponentInfo) iterator.next();
            components[index] = componentInfo.component;
            constraints[index] = componentInfo.constraints;
            ++index;
        }

        stream.writeObject(components);
        stream.writeObject(constraints);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        initializeTransientFields();
        stream.defaultReadObject();

        Component[] components = (Component[]) stream.readObject();
        XTableConstraints[] constraints = (XTableConstraints[]) stream.readObject();

        for(int i = 0; i < components.length; i++) {
            Component component = components[i];
            XTableConstraints constraint = constraints[i];
            ComponentInfo componentInfo = new ComponentInfo(component, constraint);
            componentKey2ComponentInfo.put(component, componentInfo);
        }
    }

    private void initializeTransientFields() {
        container2SingleTableLayout = new HashMap();
        componentKey2ComponentInfo = new HashMap();
    }

    public static void setDefaultGaps(int defaultXGap, int defaultYGap) {
        XTableLayout.defaultXGap = defaultXGap;
        XTableLayout.defaultYGap = defaultYGap;
    }

    public static int getDefaultXGap() {
        return defaultXGap;
    }

    public static int getDefaultYGap() {
        return defaultYGap;
    }

    public static void setDefaultTableInsets(Insets defaultTableInsets) {
        XTableLayout.defaultTableInsets = defaultTableInsets;
    }

    public static void setDefaultTableInsets(int gap) {
        defaultTableInsets = new Insets(gap, gap, gap, gap);
    }

    public static void setDefaultTableInsets(int top, int left, int bottom, int right) {
        defaultTableInsets = new Insets(top, left, bottom, right);
    }

    public static Insets getDefaultTableInsets() {
        return new Insets(defaultTableInsets.top,  defaultTableInsets.left, defaultTableInsets.bottom, defaultTableInsets.right);
    }

    public Insets getTableInsets() {
        return new Insets(tableInsets.top, tableInsets.left, tableInsets.bottom, tableInsets.right);
    }

    public XTableLayout setTableInsets(Insets tableInsets) {
        this.tableInsets = tableInsets;
        return this;
    }

    public XTableLayout setTableInsets(int gap) {
        tableInsets = new Insets(gap, gap, gap, gap);
        return this;
    }

    public XTableLayout setTableInsets(int top, int left, int bottom, int right) {
        tableInsets = new Insets(top, left, bottom, right);
        return this;
    }

    //
    //
    // LayoutManager(2) methods
    //
    //

    /**
     * Convenience method for adding a component.
     * The XTableConstraints(String) constructor is called with the given text
     * and the component is added to the layout with the resulting constraints object.
     *
     * @param constraintText the string that describes the constraints to be associated with the component
     * @param comp           the component to be added
     * @see XTableConstraints#XTableConstraints(String)
     */
    public void addLayoutComponent(String constraintText, Component comp) {
        XTableConstraints constraints = new XTableConstraints(constraintText);
        addLayoutComponent(comp, constraints);
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param component the component to be removed
     */
    public void removeLayoutComponent(Component component) {
        Container parent = component.getParent();
        XSingleTableLayout singleTableLayout = getSingleTableLayout(parent);
        singleTableLayout.removeLayoutComponent(component);
        int componentCount = parent.getComponentCount();
        if(componentCount == 0) {
            container2SingleTableLayout.remove(parent);
        }
    }

    /**
     * Calculates the preferred size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out
     */
    public Dimension preferredLayoutSize(Container parent) {
        XSingleTableLayout singleTableLayout = getSingleTableLayout(parent);
        Dimension preferredSize = singleTableLayout
                .preferredLayoutSize(parent, reportMinimumSizeAsPreferred, tableInsets, gap);
        return preferredSize;
    }

    private XSingleTableLayout getSingleTableLayout(Container parent) {
        XSingleTableLayout singleTableLayout = (XSingleTableLayout) container2SingleTableLayout.get(parent);
        if(singleTableLayout == null) {
            singleTableLayout = new XSingleTableLayout(componentKey2ComponentInfo);
            container2SingleTableLayout.put(parent, singleTableLayout);
        }
        return singleTableLayout;
    }

    /**
     * Calculates the minimum size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out
     */
    public Dimension minimumLayoutSize(Container parent) {
        return getSingleTableLayout(parent).minimumLayoutSize(parent, tableInsets, gap);
    }

    /**
     * Calculates the maximum size for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out
     */
    public Dimension maximumLayoutSize(Container parent) {
        return getSingleTableLayout(parent).maximumLayoutSize(parent, tableInsets, gap);
    }

    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     */
    public void layoutContainer(Container parent) {
        getSingleTableLayout(parent).layoutContainer(parent, tableInsets, reportMinimumSizeAsPreferred, tableXAlign, tableYAlign, gap);
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     *
     * @param component        the component to be added
     * @param constraintObject where/how the component is added to the layout.
     */
    public void addLayoutComponent(Component component, Object constraintObject) {
        // check arguments
        if(component == null) {
            throw new IllegalArgumentException("component cannot be null.");
        }
        if(constraintObject == null) {
            constraintObject = ".";
        }
        XTableConstraints constraints;
        if(constraintObject instanceof String) {
            constraints = new XTableConstraints((String) constraintObject);
        }
        else if(constraintObject instanceof XTableConstraints) {
            constraints = (XTableConstraints) constraintObject;
        }
        else {
            throw new IllegalArgumentException("Constraints for XTableLayout must be of class XTableConstraints, but are " +
                                               constraintObject.getClass().getName());
        }
        getSingleTableLayout(component.getParent()).addLayoutComponent(component, constraints);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container parent) {
        getSingleTableLayout(parent).invalidateLayout();
    }

    //
    //
    // Helper methods
    //
    //

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XTableLayout@");
        buffer.append(Integer.toHexString(System.identityHashCode(this)));
        buffer.append("[xGap=");
        buffer.append(gap[Axis.X]);
        buffer.append(",yGap=");
        buffer.append(gap[Axis.Y]);
        buffer.append(",reportMinimumSizeAsPreferred=");
        buffer.append(reportMinimumSizeAsPreferred);
        buffer.append(']');
        return new String(buffer);
    }
}
