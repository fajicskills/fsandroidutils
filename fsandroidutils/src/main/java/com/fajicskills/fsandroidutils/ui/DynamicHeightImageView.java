package  com.fajicskills.fsandroidutils.ui;


import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An {@link ImageView} layout that maintains a consistent width to height aspect ratio.
 */
public class DynamicHeightImageView extends AppCompatImageView {

    private double heightRatio;

    public DynamicHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightImageView(Context context) {
        super(context);
    }

    public void setHeightRatio(double ratio) {
        if (ratio != heightRatio) {
            heightRatio = ratio;
            requestLayout();
        }
    }

    public double getHeightRatio() {
        return heightRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (heightRatio > 0.0) {
            // set the image views size
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * heightRatio);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
