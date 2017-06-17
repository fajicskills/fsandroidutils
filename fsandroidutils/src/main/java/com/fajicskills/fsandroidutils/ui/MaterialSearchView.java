package  com.fajicskills.fsandroidutils.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fajicskills.fsandroidutils.R;
import com.fajicskills.fsandroidutils.adapters.SuggestionsAdapter;
import com.fajicskills.fsandroidutils.bus.RxBus;
import com.fajicskills.fsandroidutils.bus.events.FilterClickedEvent;
import com.fajicskills.fsandroidutils.bus.events.HideSearchSuggestionsEvent;
import com.fajicskills.fsandroidutils.bus.events.LeftDrawableClickedEvent;
import com.fajicskills.fsandroidutils.bus.events.SearchPerformedEvent;
import com.fajicskills.fsandroidutils.bus.events.ShowSearchSuggestionsEvent;
import com.fajicskills.fsandroidutils.util.DisplayUtility;
import com.fajicskills.fsandroidutils.util.FontCache;
import com.fajicskills.fsandroidutils.util.TrestleUtility;


import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class MaterialSearchView extends FrameLayout implements
        SuggestionsAdapter.OnItemClickListener,
        SuggestionsAdapter.OnItemLongClickListener,
        SuggestionsAdapter.OnSearchSuggestionCompleteClickListener {

    // region Constants
    public static final int REQUEST_VOICE = 1001;
    public static final int MENU = 0;
    public static final int BACK = 1;
    public static final int SEARCH = 2;
    public static final int AVATAR = 3;
    // endregion

    // region Member Variables
    private boolean areSearchSuggestionsVisible;
    private DividerItemDecoration dividerItemDecoration;
    private int leftDrawableType;
    private String hintText;
    private int marginTop;
    private int marginBottom;
    private int marginLeft;
    private int marginRight;
    private String voicePrompt;
    private Typeface font;
    private SuggestionsAdapter suggestionsAdapter ;
    private boolean isSearchEditTextFocused = false;


    EditText searchEditText;

    ImageView microphoneImageView;

    ImageView clearImageView;

    ImageView filterImageView;
    CardView cardView;
    ImageView leftDrawableImageView;

    CircleImageView leftDrawableRoundedImageView;

    View dividerView;

    FrameLayout backgroundCoverFrameLayout;

    RecyclerView recyclerView;
    // endregion

    // region Listeners

    public void backgroundCoverFrameLayoutClicked() {
        if (areSearchSuggestionsVisible) {
            hideSearchSuggestions();
        }
    }

    public void microphoneImageViewClicked() {
        if (isVoiceAvailable()) {
            hideSearchSuggestions();

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt);
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            Bundle bundle = new Bundle();
            bundle.putString(RecognizerIntent.EXTRA_PROMPT, voicePrompt);
            bundle.putString(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            bundle.putInt(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtras(bundle);

            ((Activity) ((ContextWrapper) microphoneImageView.getContext()).getBaseContext()).startActivityForResult(intent, REQUEST_VOICE);
        } else {
            Toast.makeText(getContext(), "Voice Search is unavailable", Toast.LENGTH_SHORT).show();
        }
    }


    public void leftDrawableImageViewClicked() {
        if (areSearchSuggestionsVisible) {
            hideSearchSuggestions();
        } else {
            LeftDrawableClickedEvent.Type type = null;
            switch (leftDrawableType) {
                case 0:
                    type = LeftDrawableClickedEvent.Type.MENU;
                    break;
                case 1:
                    type = LeftDrawableClickedEvent.Type.BACK;
                    break;
                case 2:
                    type = LeftDrawableClickedEvent.Type.SEARCH;
                    searchEditText.requestFocus();
                default:
                    break;
            }

            RxBus.getInstance().send(new LeftDrawableClickedEvent(type));
        }
    }

    public void clearImageViewClicked() {
        setQuery("");
    }

    public void onSearchEditTextTextChanged(CharSequence text) {
        if (text.length() > 0) {
            microphoneImageView.setVisibility(View.GONE);
            clearImageView.setVisibility(View.VISIBLE);
        } else {
            clearImageView.setVisibility(View.GONE);
            microphoneImageView.setVisibility(View.VISIBLE);
        }

        if (isSearchEditTextFocused) {
            suggestionsAdapter.setCurrentQuery(text.toString());
            RxBus.getInstance().send(new ShowSearchSuggestionsEvent(text.toString()));
        }

        filterImageView.setVisibility(View.GONE);
    }

    public void searchEditTextClicked() {
        searchEditText.requestFocus();
    }


    public void onSearchEditTextFocusChanged(boolean focused) {
        isSearchEditTextFocused = focused;

        if (isSearchEditTextFocused) {
            if (!areSearchSuggestionsVisible) {
                showSearchSuggestions();
            }
            DisplayUtility.showKeyboard(getContext(), searchEditText);
        } else {
            DisplayUtility.hideKeyboard(getContext(), searchEditText);
        }
    }

    private OnClickListener filterImageViewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            RxBus.getInstance().send(new FilterClickedEvent());
        }
    };
    // endregion

    // region Constructors
    public MaterialSearchView(Context context) {
        super(context);
        init(null);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    // endregion

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event != null) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (areSearchSuggestionsVisible) {
                    hideSearchSuggestions();
                }
            }
        }

        return true;
    }

    // region SuggestionsAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        TextView suggestionTextView = (TextView) view.findViewById(R.id.suggestion_tv);
        String suggestion = suggestionTextView.getText().toString();

        hideSearchSuggestions();
        RxBus.getInstance().send(new SearchPerformedEvent(suggestion));
    }
    // endregion

    // region SuggestionsAdapter.OnItemLongClickListener Methods

    @Override
    public void onItemLongClick(int position, View view) {
        TextView suggestionTextView = (TextView) view.findViewById(R.id.suggestion_tv);
        final String suggestion = suggestionTextView.getText().toString();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        alertDialogBuilder.setMessage(TrestleUtility.getFormattedText("Remove from search history?", font));

        alertDialogBuilder.setPositiveButton(
                getContext().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //RealmUtility.deleteQuery(suggestion);
                        RxBus.getInstance().send(new ShowSearchSuggestionsEvent(getQuery()));
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton(
                getContext().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.show();

        Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
        btnPositive.setTypeface(font);

        Button btnNegative = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
        btnNegative.setTypeface(font);

    }
    // endregion

    // region SuggestionsAdapter.OnSearchSuggestionCompleteClickListener Methods
    @Override
    public void onSearchSuggestionCompleteClickListener(int position, TextView textView) {
        searchEditText.setText(textView.getText().toString());
        int textLength = searchEditText.getText().length();
        searchEditText.setSelection(textLength, textLength);
    }
    // endregion

    // region Helper Methods
    private void init(AttributeSet attrs) {
        new SuggestionsAdapter(getContext());
        if (!isInEditMode()) {
            View view = LayoutInflater.from(getContext()).inflate((R.layout.material_search_view), this, true);
            ButterKnife.bind(this);
            searchEditText = (EditText)view.findViewById(R.id.search_et);
            microphoneImageView = (ImageView)view.findViewById(R.id.microphone_iv);
            clearImageView = (ImageView)view.findViewById(R.id.clear_iv);
            filterImageView = (ImageView)view.findViewById(R.id.filter_iv);
            cardView = (CardView)view.findViewById(R.id.cv);
            leftDrawableImageView = (ImageView)view.findViewById(R.id.left_drawable_iv);
            leftDrawableRoundedImageView = (CircleImageView)view.findViewById(R.id.left_drawable_riv);
            dividerView = view.findViewById(R.id.divider_v);
            backgroundCoverFrameLayout = (FrameLayout)view.findViewById(R.id.bg_cover_fl);
            recyclerView = (RecyclerView)view.findViewById(R.id.rv);

            backgroundCoverFrameLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    backgroundCoverFrameLayoutClicked();
                }
            });

            microphoneImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    microphoneImageViewClicked();
                }
            });
            leftDrawableImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    leftDrawableImageViewClicked();
                }
            });
            clearImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearImageViewClicked();
                }
            });

            searchEditText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchEditTextClicked();
                }
            });

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence text, int start, int before, int count) {
                    onSearchEditTextTextChanged(text);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    onSearchEditTextFocusChanged(hasFocus);
                }
            });

            font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());

            if (attrs != null) {
                TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
                try {
                    leftDrawableType = a.getInteger(R.styleable.MaterialSearchView_leftDrawableType, 1);
                    hintText = a.getString(R.styleable.MaterialSearchView_hintText);
                    voicePrompt = a.getString(R.styleable.MaterialSearchView_voicePrompt);
                    marginTop = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginTop, 0);
                    marginBottom = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginBottom, 0);
                    marginLeft = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginLeft, 0);
                    marginRight = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginRight, 0);
                } finally {
                    a.recycle();
                }
            }

            setUpLeftDrawable(false);
            setUpCardView();
            setUpHintText();
            setUpListeners();
        }
    }

    private void setUpCardView() {
        LayoutParams params = new LayoutParams(
                cardView.getLayoutParams());
        params.topMargin = marginTop;
        params.bottomMargin = marginBottom;
        params.leftMargin = marginLeft;
        params.rightMargin = marginRight;

        cardView.setLayoutParams(params);
    }

    private void setUpListeners() {
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSearchSuggestions();
                    RxBus.getInstance().send(new SearchPerformedEvent(getQuery()));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setUpLeftDrawable(boolean showingSearchSuggestions) {
        if (showingSearchSuggestions) {
            leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_dark));
            leftDrawableRoundedImageView.setVisibility(View.GONE);
            leftDrawableImageView.setVisibility(View.VISIBLE);
        } else {
            switch (leftDrawableType) {
                case MENU:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_dark));
                    break;
                case BACK:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_dark));
                    break;
                case SEARCH:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_search));
                    break;
                case AVATAR:
                    break;
                default:
                    break;
            }

            if (leftDrawableType == AVATAR) {
                leftDrawableImageView.setVisibility(View.GONE);
                leftDrawableRoundedImageView.setVisibility(View.VISIBLE);
            } else {
                leftDrawableRoundedImageView.setVisibility(View.GONE);
                leftDrawableImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setLeftDrawableType(int type) {
        leftDrawableType = type;
    }

    private void setUpHintText() {
        if (searchEditText != null)
            searchEditText.setHint(hintText);
    }

    private void showSearchSuggestions() {
        RxBus.getInstance().send(new ShowSearchSuggestionsEvent(getQuery()));

        suggestionsAdapter.setOnItemClickListener(this);
        suggestionsAdapter.setOnItemLongClickListener(this);
        suggestionsAdapter.setOnSearchSuggestionCompleteClickListener(this);

        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(getContext());
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
//        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setAdapter(suggestionsAdapter);

        if (!(suggestionsAdapter.isEmpty())) {
            recyclerView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        }

        backgroundCoverFrameLayout.setVisibility(View.VISIBLE);

        setUpLeftDrawable(true);

        areSearchSuggestionsVisible = true;
    }

    private void hideSearchSuggestions() {
        dividerView.setVisibility(View.GONE);
        backgroundCoverFrameLayout.setVisibility(View.GONE);

        setUpLeftDrawable(false);

        recyclerView.setVisibility(View.GONE);
        recyclerView.removeItemDecoration(dividerItemDecoration);

        searchEditText.clearFocus();
        areSearchSuggestionsVisible = false;

        RxBus.getInstance().send(new HideSearchSuggestionsEvent());
    }

    private boolean isVoiceAvailable() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities.size() != 0);
    }

    public void setQuery(String query) {
        searchEditText.setText(query);
        suggestionsAdapter.setCurrentQuery(query);
        if (!TextUtils.isEmpty(query))
            filterImageView.setVisibility(View.VISIBLE);
    }

    public String getQuery() {
        return searchEditText.getText().toString();
    }

    public void setHint(String hint) {
        searchEditText.setHint(hint);
    }

    public void addSuggestions(List<String> suggestions) {
        suggestionsAdapter.clear();
        suggestionsAdapter.addAll(suggestions);

        if (!(suggestionsAdapter.isEmpty())) {
            recyclerView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        }

        backgroundCoverFrameLayout.setVisibility(View.VISIBLE);
    }

    public void enableFilter() {
        filterImageView.setOnClickListener(filterImageViewOnClickListener);
        filterImageView.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ripple));
        filterImageView.setImageResource(R.drawable.ic_filter_list_active);
    }

    public void disableFilter() {
        filterImageView.setOnClickListener(null);
        filterImageView.setBackgroundDrawable(null);
        filterImageView.setImageResource(R.drawable.ic_filter_list_inactive);
    }

//    public void setAvatar(Retailer retailer){
////        mLeftDrawableRoundedImageView.bind(retailer);
//        mLeftDrawableImageView.setVisibility(View.GONE);
//        mLeftDrawableRoundedImageView.setVisibility(View.VISIBLE);
//    }
    // endregion
}
