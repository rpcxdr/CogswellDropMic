package io.cogswell.example.dropmic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities.
 */
public class Utils {

    /**
     * Display a modal dialog linked to the specified activity.  If the handler
     * is not null, it will be called when the user closes the pop-up.  If the message
     * is null, only the stack trace will appear.
     * @param activity the activity that the modal dialog with be linked to.
     * @param title the title
     * @param message the message, or null if there is no message.
     * @param throwable the throwable.
     * @param handleAlertComplete the callback to be called when the dialog is closed.  If null, no callback will be called.
     */
    public static void alert(final Activity activity, final String title, final String message, Throwable throwable, Runnable handleAlertComplete) {
        String errMsg = (message==null?"":message+"\n")+(throwable==null?"":formatStackTrace(throwable));
        alert(activity,title,errMsg,handleAlertComplete);
    }
    /**
     * Display a modal dialog linked to the specified activity.  If the handler
     * is not null, it will be called when the user closes the pop-up.
     * @param activity the activity that the modal dialog with be linked to.
     * @param title the title
     * @param message the message.  The text will be scrollable if it is too long.
     * @param handleAlertComplete the callback to be called when the dialog is closed.  If null, no callback will be called.
     */
    public static void alert(final Activity activity, final String title, final String message, Runnable handleAlertComplete) {
        final Runnable handleAlertCompleteFinal = handleAlertComplete;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    new AlertDialog.Builder(activity)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        //.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // We need to implement this method for the cancel button to appear.
                                            if (handleAlertCompleteFinal != null) {
                                                handleAlertCompleteFinal.run();
                                            }
                                        }
                                    }
                            )
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } catch (Exception e) {
                    Log.e("Utils", "Failed to dispaly alert:\nTitle:"+title+"\nMessage: "+message+"\nAlertDialog Error: "+formatStackTrace(e));
                }
            }
        });
    }

    /**
     * Formats the specified stack trace as a string.
     * @param t
     * @return
     */
    public static String formatStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String stackTrace = sw.toString();

        return stackTrace;
    }

    /**
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     *
     * @param st
     *            A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    public static String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case '/':
                        ch = '/';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
