package xyz.cyanclay.buptallinone.network.spanner.handler;

import android.text.SpannableStringBuilder;

import net.nightwhistler.htmlspanner.TagNodeHandler;

import org.htmlcleaner.TagNode;

import java.util.HashMap;
import java.util.Map;

public abstract class StyledHandler extends TagNodeHandler {

    @Override
    public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end) {
        Map<String, String> attrs = node.getAttributes();

        Map<String, String> styles = new HashMap<>();
        String style = attrs.get("style");
        if (style != null) {
            String[] settings = style.split(";");
            for (String setting : settings) {
                String[] queries = setting.split(":");
                if (queries.length == 2)
                    styles.put(queries[0].trim(), queries[1].trim());
            }
        }

        buildStyle(styles, builder, start, end);
    }

    protected abstract void buildStyle(Map<String, String> styles,
                                       SpannableStringBuilder builder, int start, int end);
}
