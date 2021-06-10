package xyz.cyanclay.poststudent.network.spanner.handler;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

import net.nightwhistler.htmlspanner.spans.AlignNormalSpan;
import net.nightwhistler.htmlspanner.spans.AlignOppositeSpan;
import net.nightwhistler.htmlspanner.spans.CenterSpan;

import java.util.Map;

import xyz.cyanclay.poststudent.util.Utils;

public class ParagraphHandler extends StyledHandler {
    private Context context;

    public ParagraphHandler(Context context) {
        this.context = context;
    }

    protected void buildStyle(Map<String, String> styles, SpannableStringBuilder builder, int start, int end) {
        String textIndent = styles.get("text-indent");
        String textAlign = styles.get("text-align");

        if (textIndent != null) {
            try {
                LeadingMarginSpan.Standard marginSpan = new
                        LeadingMarginSpan.Standard(Utils.dip2px(context, Float.parseFloat(
                        textIndent.substring(0, textIndent.length() - 2).trim())), 0);
                builder.setSpan(marginSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                Log.e("Unrecognized textIndent", textIndent);
            }
        }

        if (textAlign != null) {
            textAlign = textAlign.trim();
            AlignmentSpan span = null;
            if ("right".equalsIgnoreCase(textAlign)) {
                span = new AlignOppositeSpan();
            } else if ("center".equalsIgnoreCase(textAlign)) {
                span = new CenterSpan();
            } else if ("left".equalsIgnoreCase(textAlign)) {
                span = new AlignNormalSpan();
            }

            if (span != null) {
                builder.setSpan(span, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        appendNewLine(builder);
        appendNewLine(builder);
    }
}
