package com.fajicskills.fsandroidutils.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.fajicskills.fsandroidutils.R;
import com.fajicskills.fsandroidutils.models.User;
import com.fajicskills.fsandroidutils.util.DisplayUtility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 7/8/16.
 */

public class AvatarView extends FrameLayout {

    // region Constants
    // endregion

    // region Member Variables
    private Transformation roundedTransformation;
    private float radius;
    // endregion

    // region Views
    TextView textView;
    ImageView imageView;
    // endregion

    // region Interfaces
    public interface UserInitials {
        String getInitials();
    }
    // endregion

    // region Constructors
    public AvatarView(Context context) {
        super(context);
        init(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    // endregion

    // region Helper Methods
    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.avatar_view, this, true);
            textView = (TextView)view.findViewById(R.id.initials_tv);
            imageView = (ImageView)view.findViewById(R.id.photo_iv);


            if(attrs != null){
                TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.AvatarView, 0, 0);
                try {
                    radius = a.getDimension(R.styleable.AvatarView_av_radius, 0);
                } finally {
                    a.recycle();
                }
            }

            roundedTransformation = new RoundedTransformationBuilder()
                    .cornerRadius(radius)
//                .borderWidthDp(1f)
//                .borderColor(ContextCompat.getColor(getContext(), R.color.grey_400))
                    .oval(false)
                    .build();

            // resize ImageView
            ViewGroup.LayoutParams imageViewLayoutParams = imageView.getLayoutParams();
            imageViewLayoutParams.width = (int)(radius*2);
            imageViewLayoutParams.height = (int)(radius*2);
            imageView.setLayoutParams(imageViewLayoutParams);

            // resize TextView
            ViewGroup.LayoutParams textViewLayoutParams = textView.getLayoutParams();
            textViewLayoutParams.width = (int)(radius*2);
            textViewLayoutParams.height = (int)(radius*2);
            textView.setLayoutParams(textViewLayoutParams);

            // resize TextView's fontSize
            int fontSize = (16*(int)radius*2)/ DisplayUtility.dp2px(getContext(), 40);
            textView.setTextSize(fontSize);
        }
    }

    public void bind(@Nullable User user) {
        if (user == null) {
            return;
        }

        String avatarUrl = user.getAvatarUrl();

        if (!TextUtils.isEmpty(avatarUrl)) {
            Picasso.with(getContext())
                    .load(avatarUrl)
                    .transform(roundedTransformation)
                    .resize((int)radius*2, (int)radius*2)
                    .centerCrop()
                    .into(imageView);
        } else {
            nullify();
        }

//        textView.setText(user.getInitials());
        String firstInital = "";
        String name = user.getName();
        if(!TextUtils.isEmpty(name))
            firstInital = String.valueOf(name.charAt(0)).toUpperCase().trim();
        textView.setText(firstInital);

        // Set up background
        StateListDrawable stateListDrawable = new StateListDrawable();

        GradientDrawable defaultDrawable = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.default_avatar_bg);
        defaultDrawable.setColor(DisplayUtility.getDefaultAvatarBackgroundColor(user, getContext()));
        if(isClickable()){
            GradientDrawable pressedDrawable = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.pressed_avatar_bg);
            pressedDrawable.setColor(DisplayUtility.getPressedAvatarBackgroundColor(user, getContext()));
            stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, pressedDrawable);
        }
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        textView.setBackground(stateListDrawable);
    }

    public void nullify() {
        imageView.setImageDrawable(null);
        textView.setText("");
    }
    // endregion
}
