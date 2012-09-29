package com.eekboom.copy_as_html;

import java.awt.*;
import java.text.Format;

public class CodeStyle {
    private static final Format _colorFormat = new ColorFormat();

    private Color _foregroundColor;
    private Color _backgroundColor;
    private boolean _bold;
    private boolean _italic;
    private Color _underlineColor;
    private Color _strikeThroughColor;
    private Color _boxColor;

    public CodeStyle(Color foregroundColor, Color backgroundColor, boolean bold, boolean italic, Color underlineColor,
                     Color strikeThroughColor, Color boxColor) {
        _foregroundColor = foregroundColor;
        _backgroundColor = backgroundColor;
        _bold = bold;
        _italic = italic;
        _underlineColor = underlineColor;
        _strikeThroughColor = strikeThroughColor;
        _boxColor = boxColor;
    }

    public Color getForegroundColor() {
        return _foregroundColor;
    }

    public Color getBackgroundColor() {
        return _backgroundColor;
    }

    public boolean isBold() {
        return _bold;
    }

    public boolean isItalic() {
        return _italic;
    }

    public Color getUnderlineColor() {
        return _underlineColor;
    }

    public Color getStrikeThroughColor() {
        return _strikeThroughColor;
    }

    public Color getBoxColor() {
        return _boxColor;
    }

    public String startHtml() {
        StringBuffer buffer = new StringBuffer();

        if(_boxColor != null) {
            buffer.append("<span style=\"border-style:solid; border-width:0.01mm; border-color:");
            buffer.append(_colorFormat.format(_boxColor));
            buffer.append("\">");
        }

        if(_underlineColor != null) {
            buffer.append("<span style=\"text-decoration:underline;color:");
            buffer.append(_colorFormat.format(_underlineColor));
            buffer.append("\">");
        }

        if(_strikeThroughColor != null) {
            buffer.append("<span style=\"text-decoration:line-through;color:");
            buffer.append(_colorFormat.format(_strikeThroughColor));
            buffer.append("\">");
        }

        if(hasBasicTextStyle()) {
            buffer.append("<span style=\"");

            if(_foregroundColor != null) {
                buffer.append("color:");
                buffer.append(_colorFormat.format(_foregroundColor));
                buffer.append(";");
            }

            if(_backgroundColor != null) {
                buffer.append("background-color:");
                buffer.append(_colorFormat.format(_backgroundColor));
                buffer.append(";");
            }

            if(_bold) {
                buffer.append("font-weight:bold;");
            }

            if(_italic) {
                buffer.append("font-style:italic;");
            }

            buffer.append("\">");
        }

        return new String(buffer);
    }

    private boolean hasBasicTextStyle() {
        return _foregroundColor != null | _backgroundColor != null | _bold | _italic;
    }

    public String endHtml() {
        StringBuffer buffer = new StringBuffer();
        if(hasBasicTextStyle()) {
            buffer.append("</span>");
        }

        if(_strikeThroughColor != null) {
            buffer.append("</span>");
        }
        if(_underlineColor != null) {
            buffer.append("</span>");
        }
        if(_boxColor != null) {
            buffer.append("</span>");
        }
        return new String(buffer);
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        final CodeStyle codeStyle = (CodeStyle) o;

        if(_bold != codeStyle._bold) {
            return false;
        }
        if(_italic != codeStyle._italic) {
            return false;
        }
        if(_backgroundColor != null ? !_backgroundColor.equals(codeStyle._backgroundColor) :
           codeStyle._backgroundColor != null) {
            return false;
        }
        if(_boxColor != null ? !_boxColor.equals(codeStyle._boxColor) : codeStyle._boxColor != null) {
            return false;
        }
        if(_foregroundColor != null ? !_foregroundColor.equals(codeStyle._foregroundColor) :
           codeStyle._foregroundColor != null) {
            return false;
        }
        if(_strikeThroughColor != null ? !_strikeThroughColor.equals(codeStyle._strikeThroughColor) :
           codeStyle._strikeThroughColor != null) {
            return false;
        }
        if(_underlineColor != null ? !_underlineColor.equals(codeStyle._underlineColor) :
           codeStyle._underlineColor != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (_foregroundColor != null ? _foregroundColor.hashCode() : 0);
        result = 29 * result + (_backgroundColor != null ? _backgroundColor.hashCode() : 0);
        result = 29 * result + (_bold ? 1 : 0);
        result = 29 * result + (_italic ? 1 : 0);
        result = 29 * result + (_underlineColor != null ? _underlineColor.hashCode() : 0);
        result = 29 * result + (_strikeThroughColor != null ? _strikeThroughColor.hashCode() : 0);
        result = 29 * result + (_boxColor != null ? _boxColor.hashCode() : 0);
        return result;
    }
}
