package xyz.cyanclay.buptallinone.network.spanner.handler;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.nightwhistler.htmlspanner.spans.FontFamilySpan;

import org.htmlcleaner.TagNode;

public class StrongHandler extends TagNodeHandler {
    @Override
    public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end) {
        FontFamilySpan originalSpan = getFontFamilySpan(builder, start, end);

        FontFamilySpan boldSpan;

        if (originalSpan != null) {
            boldSpan = new FontFamilySpan(originalSpan.getFontFamily());
            boldSpan.setItalic(originalSpan.isItalic());
        } else {
            boldSpan = new FontFamilySpan(getSpanner().getDefaultFont());
        }

        boldSpan.setBold(true);

        builder.setSpan(boldSpan, start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
