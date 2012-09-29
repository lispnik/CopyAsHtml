package com.eekboom.copy_as_html;

import java.text.Format;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.awt.*;

public class ColorFormat extends Format {
    public Object parseObject(String source, ParsePosition pos) {
        return null;
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        Color color = (Color) obj;
        toAppendTo.append('#');
        appendColor(toAppendTo, color.getRed());
        appendColor(toAppendTo, color.getGreen());
        appendColor(toAppendTo, color.getBlue());
        return toAppendTo;
    }

    private void appendColor(StringBuffer buffer, int value) {
        String hex = Integer.toHexString(value);
        if (hex.length() < 2) {
            buffer.append('0');
        }
        buffer.append(hex);
    }
}
