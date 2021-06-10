package xyz.cyanclay.poststudent.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import xyz.cyanclay.poststudent.R;

public class BorderTextView extends AppCompatTextView {

    Paint paint = new Paint();

    public BorderTextView(Context context) {
        super(context);
    }

    public BorderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(getResources().getColor(R.color.textBorder));

        int strokeWidth = 1;
        canvas.drawLine(0, 0, this.getWidth() - strokeWidth, 0, paint);
        canvas.drawLine(0, 0, 0, this.getHeight() - strokeWidth, paint);
        canvas.drawLine(this.getWidth() - strokeWidth, 0, this.getWidth() - strokeWidth, this.getHeight() - strokeWidth, paint);
        canvas.drawLine(0, this.getHeight() - strokeWidth, this.getWidth() - strokeWidth, this.getHeight() - strokeWidth, paint);

        super.onDraw(canvas);
    }
}
