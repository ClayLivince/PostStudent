package xyz.cyanclay.buptallinone.network.spanner.handler;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.Map;

public class SpanHandler extends StyledHandler {

    @Override
    protected void buildStyle(Map<String, String> styles, SpannableStringBuilder builder, int start, int end) {
        String fontSize = styles.get("font-size");
        String fontFamily = styles.get("font-family");
        String color = styles.get("color");

        if (fontSize != null) {
            AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(Integer.parseInt(
                    fontSize.substring(0, fontSize.length() - 2).trim()), true);
            builder.setSpan(sizeSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (fontFamily != null) {
            //FontFamilySpan fontSpan = new FontFamilySpan();
        }

        if (color != null) {
            ForegroundColorSpan colorSpan;
            int mColor;
            try {
                if (color.contains("rgb")) {
                    color = color.replace("rgb(", "");
                    color = color.replace(")", "");
                    String[] colorInts = color.split(",");
                    mColor = Color.rgb(Integer.parseInt(colorInts[0].trim()),
                            Integer.parseInt(colorInts[1].trim()),
                            Integer.parseInt(colorInts[2].trim()));
                } else mColor = Color.parseColor(color.trim());
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                mColor = Color.BLACK;
                Log.e("Color", "Unexpected color ::" + color);

            }

            colorSpan = new ForegroundColorSpan(mColor);
            builder.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
    }
}
