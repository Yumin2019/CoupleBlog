package com.coupleblog.a100photo;

import static com.coupleblog.singleton.CB_AppFunc.application;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coupleblog.CB_PhotoEditorActivity;
import com.coupleblog.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class EmojiBSFragment extends BottomSheetDialogFragment {

    static ArrayList<String> emojisList = getEmojis(application);

    public EmojiBSFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        CB_PhotoEditorActivity.Companion.setMHasBSSheet(false);
    }

    private EmojiListener mEmojiListener;

    public interface EmojiListener {
        void onEmojiClick(String emojiUnicode);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.transparent_bottom_sheet_theme);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.photo_fragment_bottom_sticker_emoji_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).addBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 5);
        rvEmoji.setLayoutManager(gridLayoutManager);
        EmojiAdapter emojiAdapter = new EmojiAdapter();
        rvEmoji.setAdapter(emojiAdapter);
        rvEmoji.setHasFixedSize(true);
        rvEmoji.setDrawingCacheEnabled(true);
        rvEmoji.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvEmoji.setItemViewCacheSize(emojisList.size());
    }
    public void setEmojiListener(EmojiListener emojiListener) {
        mEmojiListener = emojiListener;
    }


    public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_row_emoji, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.txtEmoji.setText(emojisList.get(position));
        }

        @Override
        public int getItemCount() {
            return emojisList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtEmoji;

            ViewHolder(View itemView) {
                super(itemView);
                txtEmoji = itemView.findViewById(R.id.txtEmoji);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mEmojiListener != null) {
                            mEmojiListener.onEmojiClick(emojisList.get(getLayoutPosition()));
                        }
                        dismiss();
                    }
                });
            }
        }
    }

    /**
     * Provide the list of emoji in form of unicode string
     *
     * @param context context
     * @return list of emoji unicode
     */
    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }

    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }
}

