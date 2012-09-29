package com.eekboom.copy_as_html;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.components.ApplicationComponent;
import com.eekboom.xlayouts.XTableLayout;
import org.jdom.Element;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

public class Configuration implements ApplicationComponent, Configurable, JDOMExternalizable {
    public static String LINE_NO_ALWAYS = "always";
    public static String LINE_NO_NEVER = "never";
    public static String LINE_NO_FOLLOW = "follow";

    public static String FONT_SIZE_FOLLOW = "follow";
    public static String FONT_SIZE_FIXED = "fixed";
    public static String FONT_SIZE_UNSPECIFIED = "null";

    private static Icon NO_ICON = new Icon() {
        public int getIconHeight() {
            return 32;
        }

        public int getIconWidth() {
            return 32;
        }

        public void paintIcon(Component component, Graphics g, int x, int y) {
        }
    };
    private JComponent _panel;
    private JCheckBox _borderCheckBox;
    private JCheckBox _includeWarningHighlightsCheckBox;

    private JRadioButton _textPlainRadioButton;
    private JRadioButton _textHtmlRadioButton;
    private JRadioButton _textHtmlPlainRadioButton;

    private JRadioButton _includeLineNosFollowEditor;
    private JRadioButton _includeLineNosAlways;
    private JRadioButton _includeLineNosNever;

    private JRadioButton _lineNosStartAt1RadioButton;
    private JRadioButton _lineNosStartAtEditorRowRadioButton;

    private JCheckBox _unindentCheckBox;

    private JRadioButton _fontSizeFollowsEditorRadioButton;
    private JRadioButton _fontSizeFixedRadioButton;
    private JRadioButton _fontSizeUnspecifiedRadioButton;
    private JFormattedTextField _fontSizeField;

    private JCheckBox _includePaddingCheckBox;
    private JFormattedTextField _paddingField;

    private JCheckBox _tabsToSpacesCheckBox;
    private JFormattedTextField _tabsField;

    public boolean _addBorder = true;
    public boolean _includeWarningHighlights;
    public int _mimeTypes = ClipboardHelper.TYPE_HTML_AND_PLAIN;
    public boolean _lineNosStartAt1 = true;
    public String _lineNoType = LINE_NO_FOLLOW;

    public boolean _unindent;

    public boolean _includePadding = true;
    public int _padding = 4;

    public String _fontSizeType = FONT_SIZE_FOLLOW;
    public int _fontSize;
    
    public boolean _tabsToSpaces = true;
    public int _tabSize = 4;

    private static Configuration _instance;

    public Configuration() {
        _panel = new JPanel(new XTableLayout());

        createComponents();

        layoutComponents();
    }

    private void createComponents() {
        // Mime Type options
        _textPlainRadioButton = new JRadioButton("text/plain (Useful for pasting as HTML source)");
        _textHtmlRadioButton = new JRadioButton("text/html (Useful for pasting in Word, OpenOffice, ...)");
        _textHtmlPlainRadioButton = new JRadioButton("both");
        ButtonGroup buttonGroupMimeTypes = new ButtonGroup();
        buttonGroupMimeTypes.add(_textPlainRadioButton);
        buttonGroupMimeTypes.add(_textHtmlPlainRadioButton);
        buttonGroupMimeTypes.add(_textHtmlRadioButton);

        // Add Border
        _borderCheckBox = new JCheckBox("Add Border");
        _borderCheckBox.setToolTipText("Surrounds the exported text with a thin black border.");

        // Include Editor Warning Highlights
        _includeWarningHighlightsCheckBox = new JCheckBox("Include Editor Warning Highlights");
        _includeWarningHighlightsCheckBox.setToolTipText(
                "Also includes additional highlighting from warnings and errors,\nlike the light gray for unused symbol.");

        // unindent
        _unindentCheckBox = new JCheckBox("Un-indent (remove common leading white space)");

        // padding
        _includePaddingCheckBox = new JCheckBox("Add padding (px)");
        DecimalFormat paddingFormat = new DecimalFormat("#0");
        NumberFormatter paddingNumberFormatter = new NumberFormatter(paddingFormat);
        paddingNumberFormatter.setCommitsOnValidEdit(true);
        _paddingField = new JFormattedTextField(paddingNumberFormatter) {
            public Dimension getMaximumSize() {
                Dimension dimension = super.getMaximumSize();
                dimension.width = 40;
                return dimension;
            }
        };
        _paddingField.setHorizontalAlignment(JTextField.LEFT);
        _paddingField.setAlignmentX(0.0F);

        // tabsToSpaces
        _tabsToSpacesCheckBox = new JCheckBox("Convert tabs to spaces");
        DecimalFormat tabsFormat = new DecimalFormat("#0");
        NumberFormatter tabsNumberFormatter = new NumberFormatter(tabsFormat);
        tabsNumberFormatter.setCommitsOnValidEdit(true);
        _tabsField = new JFormattedTextField(tabsNumberFormatter) {
            public Dimension getMaximumSize() {
                Dimension dimension = super.getMaximumSize();
                dimension.width = 40;
                return dimension;
            }
        };
        _tabsField.setHorizontalAlignment(JTextField.LEFT);
        _tabsField.setAlignmentX(0.0F);

        // font options
        _fontSizeFollowsEditorRadioButton = new JRadioButton("Follow Editor Setting");
        _fontSizeFixedRadioButton = new JRadioButton("Fixed Size");
        _fontSizeUnspecifiedRadioButton = new JRadioButton("Don't include");
        DecimalFormat decimalFormat = new DecimalFormat("##0");
        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        numberFormatter.setCommitsOnValidEdit(true);
        _fontSizeField = new JFormattedTextField(numberFormatter) {
            public Dimension getMaximumSize() {
                Dimension dimension = super.getMaximumSize();
                dimension.width = 40;
                return dimension;
            }
        };
        _fontSizeField.setHorizontalAlignment(JTextField.LEFT);
        _fontSizeField.setAlignmentX(0.0F);
        ButtonGroup fontSizeGroup = new ButtonGroup();
        fontSizeGroup.add(_fontSizeFollowsEditorRadioButton);
        fontSizeGroup.add(_fontSizeFixedRadioButton);

        // Include Line Nos
        _includeLineNosFollowEditor = new JRadioButton("Follow Editor Setting");
        _includeLineNosAlways = new JRadioButton("Always");
        _includeLineNosNever = new JRadioButton("Never");
        ButtonGroup buttonGroupIncludeLineNos = new ButtonGroup();
        buttonGroupIncludeLineNos.add(_includeLineNosFollowEditor);
        buttonGroupIncludeLineNos.add(_includeLineNosAlways);
        buttonGroupIncludeLineNos.add(_includeLineNosNever);

        // First Line Number
        _lineNosStartAt1RadioButton = new JRadioButton("Start at 1");
        _lineNosStartAtEditorRowRadioButton = new JRadioButton("Use Editor Row");
        ButtonGroup buttonGroupLineNos = new ButtonGroup();
        buttonGroupLineNos.add(_lineNosStartAt1RadioButton);
        buttonGroupLineNos.add(_lineNosStartAtEditorRowRadioButton);
    }

    private void layoutComponents() {
        _panel.add(new JLabel("Copy as MIME Type"), "0 0");
        _panel.add(_textHtmlRadioButton, "1 0 2 1");
        _panel.add(_textPlainRadioButton, "1 1 2 1");
        _panel.add(_textHtmlPlainRadioButton, "1 2");

        _panel.add(_borderCheckBox, "0 3");
        _panel.add(_includeWarningHighlightsCheckBox, "0 4 2 1");
        _panel.add(_unindentCheckBox, "0 5 2 1");
        _panel.add(_includePaddingCheckBox, "0 6");
        _panel.add(_paddingField, "1 6");
        _panel.add(_tabsToSpacesCheckBox, "0 7");
        _panel.add(_tabsField, "1 7");

        _panel.add(new JLabel("Font Size"), "0 8");
        _panel.add(_fontSizeFollowsEditorRadioButton, "1 8");
        _panel.add(_fontSizeFixedRadioButton, "1 9");
        _panel.add(_fontSizeField, "2 9");
        _panel.add(_fontSizeUnspecifiedRadioButton, "1 10");

        _panel.add(new JLabel("Include Line Numbers"), "0 11");
        _panel.add(_includeLineNosFollowEditor, "1 11");
        _panel.add(_includeLineNosAlways, "1 12");
        _panel.add(_includeLineNosNever, "1 13");

        _panel.add(new JLabel("First Line Number"), "0 14");
        _panel.add(_lineNosStartAt1RadioButton, "1 14");
        _panel.add(_lineNosStartAtEditorRowRadioButton, "1 15");
    }

    public void initComponent() {
        _instance = this;
    }

    public void disposeComponent() {
    }

    public String getComponentName() {
        return "CopyAsHtmlConfiguration";
    }

    public static Configuration getInstance() {
        return _instance;
    }

    public boolean getAddBorder() {
        return _addBorder;
    }

    public boolean getLineNosStartAt1() {
        return _lineNosStartAt1;
    }

    public String getLineNoType() {
        return _lineNoType;
    }

    public boolean getIncludeWarningHighlights() {
        return _includeWarningHighlights;
    }

    public boolean isIncludePadding() {
        return _includePadding;
    }

    public int getPadding() {
        return _padding;
    }

    public boolean isTabsToSpaces() {
        return _tabsToSpaces;
    }

    public int getTabSize() {
        return _tabSize;
    }

    public boolean isUnindent() {
        return _unindent;
    }

    public int getMimeTypes() {
        return _mimeTypes;
    }

    public String getDisplayName() {
        return "Copy as HTML";
    }

    public Icon getIcon() {
        return NO_ICON;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return _panel;
    }

    public String getFontSizeType() {
        return _fontSizeType;
    }

    public int getFontSize() {
        return _fontSize;
    }

    public boolean isModified() {
        boolean borderChanged = _addBorder != _borderCheckBox.isSelected();
        boolean warningHighlightChanged = _includeWarningHighlights != _includeWarningHighlightsCheckBox.isSelected();
        boolean mimeTypesChanged = _mimeTypes != getMimeType();
        boolean unindentChanged = _unindent != _unindentCheckBox.isSelected();
        boolean lineNosStartChanged = _lineNosStartAt1 != _lineNosStartAt1RadioButton.isSelected();
        boolean includeLineNosChanged = (LINE_NO_ALWAYS.equals(_lineNoType) && !_includeLineNosAlways.isSelected()
                                         || LINE_NO_NEVER.equals(_lineNoType) && !_includeLineNosNever.isSelected()
                                         || (_lineNoType == null || LINE_NO_FOLLOW.equals(_lineNoType)) && !_includeLineNosFollowEditor.isSelected());
        boolean isFontModified = (FONT_SIZE_FOLLOW.equals(_fontSizeType) || _fontSizeType == null) && !_fontSizeFollowsEditorRadioButton.isSelected()
                                 || FONT_SIZE_FIXED.equals(_fontSizeType) && _fontSizeFixedRadioButton.isSelected()
                                 || FONT_SIZE_UNSPECIFIED.equals(_fontSizeType) && _fontSizeUnspecifiedRadioButton.isSelected()
                                 || _fontSize != ((Number) _fontSizeField.getValue()).intValue();
        boolean isPaddingModified = _includePadding != _includePaddingCheckBox.isSelected()
                                    || _padding != ((Number) _paddingField.getValue()).intValue();
        boolean isTabsModified = _tabsToSpaces != _tabsToSpacesCheckBox.isSelected()
                                    || _tabSize != ((Number) _tabsField.getValue()).intValue();

        boolean isModified = borderChanged || warningHighlightChanged || mimeTypesChanged || includeLineNosChanged || lineNosStartChanged
                             || isFontModified || unindentChanged || isPaddingModified || isTabsModified;

        return isModified;
    }

    private int getMimeType() {
        if (_textHtmlPlainRadioButton.isSelected()) {
            return ClipboardHelper.TYPE_HTML_AND_PLAIN;
        }
        if (_textPlainRadioButton.isSelected()) {
            return ClipboardHelper.TYPE_PLAIN;
        }
        if (_textHtmlRadioButton.isSelected()) {
            return ClipboardHelper.TYPE_HTML;
        }
        return ClipboardHelper.TYPE_HTML_AND_PLAIN;
    }

    public void apply() throws ConfigurationException {
        _addBorder = _borderCheckBox.isSelected();
        _includeWarningHighlights = _includeWarningHighlightsCheckBox.isSelected();
        _unindent = _unindentCheckBox.isSelected();
        _includePadding = _includePaddingCheckBox.isSelected();
        _padding = ((Number) _paddingField.getValue()).intValue();
        _tabsToSpaces = _tabsToSpacesCheckBox.isSelected();
        _tabSize = ((Number) _tabsField.getValue()).intValue();
        _mimeTypes = getMimeType();
        _lineNosStartAt1 = _lineNosStartAt1RadioButton.isSelected();
        if (_includeLineNosAlways.isSelected()) {
            _lineNoType = LINE_NO_ALWAYS;
        }
        else if (_includeLineNosNever.isSelected()) {
            _lineNoType = LINE_NO_NEVER;
        }
        else {
            _lineNoType = LINE_NO_FOLLOW;
        }
        if (_fontSizeFollowsEditorRadioButton.isSelected()) {
            _fontSizeType = FONT_SIZE_FOLLOW;
        }
        else if (_fontSizeFixedRadioButton.isSelected()) {
            _fontSizeType = FONT_SIZE_FIXED;
        }
        else {
            _fontSizeType = FONT_SIZE_UNSPECIFIED;
        }
        _fontSize = ((Number) _fontSizeField.getValue()).intValue();
    }

    public void reset() {
        _borderCheckBox.setSelected(_addBorder);
        _includeWarningHighlightsCheckBox.setSelected(_includeWarningHighlights);
        _unindentCheckBox.setSelected(_unindent);
        _includePaddingCheckBox.setSelected(_includePadding);
        _paddingField.setValue(Integer.valueOf(_padding));
        _tabsToSpacesCheckBox.setSelected(_tabsToSpaces);
        _tabsField.setValue(Integer.valueOf(_tabSize));

        switch (_mimeTypes) {
            case ClipboardHelper.TYPE_HTML_AND_PLAIN:
                _textHtmlPlainRadioButton.setSelected(true);
                break;
            case ClipboardHelper.TYPE_HTML:
                _textHtmlRadioButton.setSelected(true);
                break;
            case ClipboardHelper.TYPE_PLAIN:
                _textPlainRadioButton.setSelected(true);
                break;
            default:
                _textHtmlPlainRadioButton.setSelected(true);
        }

        _includeLineNosFollowEditor.setSelected(LINE_NO_FOLLOW.equals(_lineNoType));
        _includeLineNosAlways.setSelected(LINE_NO_ALWAYS.equals(_lineNoType));
        _includeLineNosNever.setSelected(LINE_NO_NEVER.equals(_lineNoType));

        _lineNosStartAt1RadioButton.setSelected(_lineNosStartAt1);
        _lineNosStartAtEditorRowRadioButton.setSelected(!_lineNosStartAt1);

        _fontSizeFollowsEditorRadioButton.setSelected(FONT_SIZE_FOLLOW.equals(_fontSizeType));
        _fontSizeFixedRadioButton.setSelected(FONT_SIZE_FIXED.equals(_fontSizeType));
        _fontSizeUnspecifiedRadioButton.setSelected(FONT_SIZE_UNSPECIFIED.equals(_fontSizeType));
        _fontSizeField.setValue(Integer.valueOf(_fontSize));

    }

    public void disposeUIResources() {
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}
