package  com.fajicskills.fsandroidutils.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.fajicskills.fsandroidutils.util.CustomFontUtils;


public class CustomFontEditText extends AppCompatEditText {

    // region Constructors
    public CustomFontEditText(Context context) {
        super(context);
        init(context, null);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    // endregion

    // region Helper Methods
    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            CustomFontUtils.applyCustomFont(this, context, attrs);
        }
    }
    // endregion
}
