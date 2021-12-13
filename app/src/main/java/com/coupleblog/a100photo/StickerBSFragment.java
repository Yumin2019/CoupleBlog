package com.coupleblog.a100photo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coupleblog.R;
import com.coupleblog.singleton.CB_AppFunc;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StickerBSFragment extends BottomSheetDialogFragment {

    static int itemSize = 0;
    static BitmapFactory.Options options = new BitmapFactory.Options();
    static int[] stickerList = new int[]{
            R.drawable.android,
            R.drawable.arch_1,
            R.drawable.arch_2,
            R.drawable.arch_3,
            R.drawable.arch_4,
            R.drawable.ballon_1,
            R.drawable.ballon_2,
            R.drawable.bear_1,
            R.drawable.bear_2,
            R.drawable.bed_1,
            R.drawable.bed_2,
            R.drawable.bed_3,
            R.drawable.bed_4,
            R.drawable.bed_5,
            R.drawable.bed_6,
            R.drawable.bed_7,
            R.drawable.bikini_1,
            R.drawable.bikini_2,
            R.drawable.bird_1,
            R.drawable.bird_2,
            R.drawable.bird_3,
            R.drawable.bird_4,
            R.drawable.bird_5,
            R.drawable.bird_7,
            R.drawable.bouquet_1,
            R.drawable.bouquet_2,
            R.drawable.bouquet_3,
            R.drawable.bouquet_4,
            R.drawable.bride_1,
            R.drawable.bride_2,
            R.drawable.bride_3,
            R.drawable.cake_1,
            R.drawable.cake_2,
            R.drawable.cake_3,
            R.drawable.cake_4,
            R.drawable.cake_5,
            R.drawable.cake_6,
            R.drawable.camera_1,
            R.drawable.camera_2,
            R.drawable.candle,
            R.drawable.christmas_1,
            R.drawable.christmas_2,
            R.drawable.christmas_3,
            R.drawable.christmas_4,
            R.drawable.clover,
            R.drawable.condom_1,
            R.drawable.condom_2,
            R.drawable.abstinence,
            R.drawable.costume_1,
            R.drawable.costume_2,
            R.drawable.costume_3,
            R.drawable.costume_4,
            R.drawable.costume_5,
            R.drawable.costume_6,
            R.drawable.costume_7,
            R.drawable.couple_1,
            R.drawable.couple_2,
            R.drawable.couple_3,
            R.drawable.couple_5,
            R.drawable.couple_6,
            R.drawable.couple_7,
            R.drawable.couple_8,
            R.drawable.couple_blog,
            R.drawable.diamond_1,
            R.drawable.diamond_2,
            R.drawable.diamond_3,
            R.drawable.dinner_1,
            R.drawable.dinner_2,
            R.drawable.dinner_3,
            R.drawable.dress_1,
            R.drawable.dress_2,
            R.drawable.dress_3,
            R.drawable.dress_4,
            R.drawable.dress_5,
            R.drawable.dress_6,
            R.drawable.dress_7,
            R.drawable.drink_1,
            R.drawable.drink_2,
            R.drawable.drink_3,
            R.drawable.family,
            R.drawable.flower_1,
            R.drawable.flower_10,
            R.drawable.flower_11,
            R.drawable.flower_2,
            R.drawable.flower_3,
            R.drawable.flower_4,
            R.drawable.flower_5,
            R.drawable.flower_6,
            R.drawable.flower_7,
            R.drawable.flower_8,
            R.drawable.flower_9,
            R.drawable.gender,
            R.drawable.gift_1,
            R.drawable.gift_10,
            R.drawable.gift_11,
            R.drawable.gift_2,
            R.drawable.gift_3,
            R.drawable.gift_4,
            R.drawable.gift_5,
            R.drawable.gift_6,
            R.drawable.gift_7,
            R.drawable.gift_8,
            R.drawable.gift_9,
            R.drawable.girl,
            R.drawable.groom_1,
            R.drawable.groom_2,
            R.drawable.groom_3,
            R.drawable.hand,
            R.drawable.heart_1,
            R.drawable.heart_2,
            R.drawable.heart_3,
            R.drawable.heart_4,
            R.drawable.heart_5,
            R.drawable.heart_6,
            R.drawable.heart_7,
            R.drawable.heart_8,
            R.drawable.heels_1,
            R.drawable.heels_2,
            R.drawable.house_1,
            R.drawable.house_2,
            R.drawable.ice_cream_1,
            R.drawable.ice_cream_2,
            R.drawable.just_married,
            R.drawable.key_1,
            R.drawable.key_2,
            R.drawable.key_3,
            R.drawable.kiss_1,
            R.drawable.kiss_2,
            R.drawable.kiss_3,
            R.drawable.letter_1,
            R.drawable.letter_2,
            R.drawable.letter_3,
            R.drawable.lipstick,
            R.drawable.money_1,
            R.drawable.money_2,
            R.drawable.music_1,
            R.drawable.music_2,
            R.drawable.necklace_1,
            R.drawable.necklace_2,
            R.drawable.necklace_3,
            R.drawable.necklace_4,
            R.drawable.necklace_5,
            R.drawable.necklace_6,
            R.drawable.pregnant,
            R.drawable.ring_1,
            R.drawable.ring_2,
            R.drawable.ring_3,
            R.drawable.ring_4,
            R.drawable.ring_5,
            R.drawable.ring_6,
            R.drawable.ring_7,
            R.drawable.ring_8,
            R.drawable.ring_9,
            R.drawable.rose_1,
            R.drawable.rose_2,
            R.drawable.rose_3,
            R.drawable.suit,
            R.drawable.sweet_1,
            R.drawable.sweet_2,
            R.drawable.sweet_3,
            R.drawable.sweet_4,
            R.drawable.sweet_5,
            R.drawable.ticket,
            R.drawable.time,
            R.drawable.travel_1,
            R.drawable.travel_2,
            R.drawable.travel_3,
            R.drawable.video_1,
            R.drawable.video_2,
            R.drawable.video_3,
            R.drawable.video_4,
            R.drawable.wine_1,
            R.drawable.wine_2,
            R.drawable.wine_3,
            R.drawable.wine_4,
            R.drawable.wine_5,
    };

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public StickerBSFragment() {
        // Required empty public constructor
    }

    private StickerListener mStickerListener;

    public void setStickerListener(StickerListener stickerListener) {
        mStickerListener = stickerListener;
    }

    public interface StickerListener {
        void onStickerClick(Bitmap bitmap);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.transparent_bottom_sheet_theme);
        itemSize = (int) CB_AppFunc.Companion.convertDpToPixel(50.0f);

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), stickerList[0], options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, itemSize, itemSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
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


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.photo_fragment_bottom_sticker_emoji_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).addBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvEmoji.setLayoutManager(gridLayoutManager);
        StickerAdapter stickerAdapter = new StickerAdapter();
        rvEmoji.setAdapter(stickerAdapter);
        rvEmoji.setHasFixedSize(true);
        rvEmoji.setDrawingCacheEnabled(true);
        rvEmoji.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvEmoji.setItemViewCacheSize(stickerList.length);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_row_sticker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.imgSticker.setImageBitmap(BitmapFactory.decodeResource(getResources(), stickerList[position], options));
        }

        @Override
        public int getItemCount() {
            return stickerList.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgSticker;

            ViewHolder(View itemView) {
                super(itemView);
                imgSticker = itemView.findViewById(R.id.imgSticker);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mStickerListener != null) {
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), stickerList[getLayoutPosition()]);
                            mStickerListener.onStickerClick(Bitmap.createScaledBitmap(bitmap, 256, 256, true));
                        }
                        dismiss();
                    }
                });
            }
        }
    }

    private String convertEmoji(String emoji) {
        String returnedEmoji = "";
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt);
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}