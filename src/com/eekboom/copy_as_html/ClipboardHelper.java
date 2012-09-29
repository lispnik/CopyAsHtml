package com.eekboom.copy_as_html;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ClipboardHelper {
    public static final int TYPE_PLAIN = 1;
    public static final int TYPE_HTML = 2;
    public static final int TYPE_HTML_AND_PLAIN = 3;

    private static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    private static final String WINDOWS_HTML_HEADER = "Version:0.9" + WINDOWS_LINE_SEPARATOR +
                                                      "StartHTML:00000000000" + WINDOWS_LINE_SEPARATOR +
                                                      "EndHTML:00000000000" + WINDOWS_LINE_SEPARATOR +
                                                      "StartFragment:00000000000" + WINDOWS_LINE_SEPARATOR +
                                                      "EndFragment:00000000000" + WINDOWS_LINE_SEPARATOR;

    private static Method _wClipboardPublishMethod;
    private static Class _wClipboardClass;
    private static Method _wClipboardOpenClipboardMethod;
    private static Method _wClipboardCloseClipboardMethod;
    private static long WINDOWS_CF_UNICODETEXT = 13;
    private static long WINDOWS_CF_HTML;

    static {
        try {
            _wClipboardClass = Class.forName("sun.awt.windows.WClipboard");
            Class wDataTransfererClass = Class.forName("sun.awt.windows.WDataTransferer");
            Field htmlFormatField = wDataTransfererClass.getDeclaredField("CF_HTML");
            WINDOWS_CF_HTML = htmlFormatField.getLong(null);
            Class sunClipboardClass = Class.forName("sun.awt.datatransfer.SunClipboard");
            Class[] publishParameterTypes = new Class[]{Long.TYPE, byte[].class};
            _wClipboardPublishMethod = _wClipboardClass.getDeclaredMethod("publishClipboardData", publishParameterTypes);
            _wClipboardPublishMethod.setAccessible(true);
            _wClipboardOpenClipboardMethod = _wClipboardClass.getDeclaredMethod("openClipboard", new Class[]{sunClipboardClass});
            _wClipboardCloseClipboardMethod = _wClipboardClass.getDeclaredMethod("closeClipboard", new Class[0]);
        }
        catch(ClassNotFoundException e) {
            // fine, probably not windows, or Sun changed implementation, fall back to default copy behaviour
            _wClipboardClass = null;
        }
        catch(NoSuchMethodException e) {
            _wClipboardClass = null;
        }
        catch(NoSuchFieldException e) {
            _wClipboardClass = null;
        }
        catch(IllegalAccessException e) {
            _wClipboardClass = null;
        }
    }

    /**
     * It's surprisingly difficult to copy HTML to the clipboard (on Windows), see http://www.peterbuettner.de.
     * @param type one of TYPE_HTML_AND_PLAIN, TYPE_HTML or TYPE_PLAIN
     */
    public static void publish(Clipboard clipboard, int type, String text) {
        if(_wClipboardClass != null && _wClipboardClass.isAssignableFrom(clipboard.getClass())) {
            try {
                _wClipboardOpenClipboardMethod.invoke(clipboard, new Object[]{clipboard});
                try {
                    switch(type) {
                        case TYPE_HTML_AND_PLAIN:
                            {
                                byte[] unicodeData = convertToWindowsUnicodeClipboardFormat(text);
                                setData(clipboard, WINDOWS_CF_UNICODETEXT, unicodeData);
                            }
                            // fall through
                        case TYPE_HTML:
                            {
                                byte[] htmlData = convertToWindowsHtmlClipboardFormat(text);
                                setData(clipboard, WINDOWS_CF_HTML, htmlData);
                            }
                            break;
                        case TYPE_PLAIN:
                            {
                                byte[] unicodeData = convertToWindowsUnicodeClipboardFormat(text);
                                setData(clipboard, WINDOWS_CF_UNICODETEXT, unicodeData);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("wrong type");
                    }
                    return;
                }
                finally {
                    _wClipboardCloseClipboardMethod.invoke(clipboard, new Object[0]);
                }
            }
            catch(UnsupportedEncodingException e) {
            }
            catch(IllegalAccessException e) {
            }
            catch(InvocationTargetException e) {
            }
        }
        clipboard.setContents(new HtmlTransferable(type, text), null);
    }

    private static void setData(Clipboard clipboard, long format, byte[] data) throws IllegalAccessException, InvocationTargetException {
        _wClipboardPublishMethod.invoke(clipboard, new Object[]{new Long(format), data});
    }

    private static byte[] convertToWindowsUnicodeClipboardFormat(String text) throws UnsupportedEncodingException {
        byte[] charDataBytes = text.getBytes("utf-16le");
        byte[] data = new byte[charDataBytes.length + 1];
        System.arraycopy(charDataBytes, 0, data, 0, charDataBytes.length);
        return data;
    }

    /**
     * surrounds the html with this envelope, ready for windows clipboard (jdk support can be made better)
     * <pre>
     * Version:0.9
     * StartHTML:00000000000
     * EndHTML:00000000000
     * StartFragment:00000000000
     * EndFragment:00000000000
     * &lt;!--StartFragment--&gt;
     * ...
     * &lt;!-- EndFragment-- &gt;
     * </pre>
     * We have to return a byte array 'cause in Windows the html needs to be utf-8
     * encoded. And because we have to calculate char-offsets, we encode it here.
     *
     * @param html
     * @return byte[]
     */
    private static byte[] convertToWindowsHtmlClipboardFormat(String html) throws UnsupportedEncodingException {
        html = "<!--StartFragment-->" + html + "<!--EndFragment-->\r\n\0";

        byte[] bHtml = html.getBytes("UTF-8");// encode first 'cause it may grow

        int headerLen = WINDOWS_HTML_HEADER.length();
        int htmlLen = bHtml.length;

        StringBuffer buf = new StringBuffer(WINDOWS_HTML_HEADER);
        setValue(buf, "StartHTML", headerLen - 1);
        setValue(buf, "EndHTML", headerLen + htmlLen - 1);
        setValue(buf, "StartFragment", headerLen - 1);
        setValue(buf, "EndFragment", headerLen + htmlLen - 1);
        byte[] bHeader = buf.toString().getBytes("UTF-8");// should stay the same (no nonASCII chars in header)

        byte result[] = new byte[headerLen + htmlLen];
        System.arraycopy(bHeader, 0, result, 0, bHeader.length);
        System.arraycopy(bHtml, 0, result, bHeader.length, bHtml.length);

        return result;
    }

    /**
         * Replaces name+":00000000000" with name+":xxxxxxxxxxx" where xxx... is the '0' padded value.
         * Value can't be to long, since maxint can be displayed with 11 digits. If value is below zero
         * there is enough place (10 for the digits 1 for sign).<br>
         * If the search is not found nothing is done.
         *
         * @param src
         * @param name
         * @param value
         */
    private static void setValue(StringBuffer src, String name, int value) {
        String search = name + ":00000000000";
        int pos = src.indexOf(search);
        if(pos == -1) {
            return;// not found, do nothing
        }

        boolean belowZero = value < 0;
        if(belowZero) {
            value = -value;
        }

        src.replace(pos + search.length() - (value + "").length(), pos + search.length(), value + "");
        if(belowZero) {
            src.setCharAt(pos + name.length() + 1, '-'); // +1 'cause of ':' in "SearchMe:"
        }
    }

    public static class HtmlTransferable implements Transferable {
        private static DataFlavor HTML_DATA_FLAVOR;
        private DataFlavor[] _dataFlavors;
        private final String _text;

        static {
            try {
                HTML_DATA_FLAVOR = new DataFlavor("text/html;charset=UTF-8;class=java.lang.String");
            }
            catch(ClassNotFoundException e) {
                throw new RuntimeException("Cannot create data flavor for mime type text/html");
            }
        }

        public HtmlTransferable(int type, String text) {
            _text = text;
            switch(type) {
                case TYPE_PLAIN:
                    _dataFlavors = new DataFlavor[]{DataFlavor.stringFlavor};
                    break;
                case TYPE_HTML:
                    _dataFlavors = new DataFlavor[]{HTML_DATA_FLAVOR};
                    break;
                case TYPE_HTML_AND_PLAIN:
                    _dataFlavors = new DataFlavor[]{HTML_DATA_FLAVOR, DataFlavor.stringFlavor};
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) _dataFlavors.clone();
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for(int i = 0; i < _dataFlavors.length; i++) {
                if(flavor.equals(_dataFlavors[i])) {
                    return true;
                }
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return _text;
        }
    }
}

