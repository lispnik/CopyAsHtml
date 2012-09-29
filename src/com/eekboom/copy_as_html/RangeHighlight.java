package com.eekboom.copy_as_html;

import com.intellij.openapi.editor.markup.TextAttributes;

class RangeHighlight implements Comparable {
    private int _startOffset;
    private int _endOffset;
    private TextAttributes _textAttributes;

    public RangeHighlight(int startOffset, int endOffset, TextAttributes textAttributes) {
        _startOffset = startOffset;
        _endOffset = endOffset;
        _textAttributes = textAttributes;
    }

    public Object getTextAttributes() {
        return _textAttributes;
    }

    public int getStartOffset() {
        return _startOffset;
    }

    public int getEndOffset() {
        return _endOffset;
    }

    public int compareTo(Object o) {
        RangeHighlight rangeHighlight = (RangeHighlight) o;
        return _startOffset - rangeHighlight._startOffset;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RangeHighlight)) {
            return false;
        }
        RangeHighlight rangeHighlight = (RangeHighlight) o;
        return _startOffset == rangeHighlight._startOffset;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("range=").append(_startOffset).append("...").append(_endOffset);
        buffer.append(", foreground=").append(_textAttributes.getForegroundColor());
        buffer.append(", background=").append(_textAttributes.getBackgroundColor());
        buffer.append(", font style=").append(_textAttributes.getFontType());
        return buffer.toString();
    }

}
