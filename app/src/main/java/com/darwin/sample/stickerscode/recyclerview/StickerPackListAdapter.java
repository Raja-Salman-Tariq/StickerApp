/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.darwin.sample.stickerscode.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.darwin.sample.R;
import com.darwin.sample.stickerscode.activities.StickerPackDetailsActivity;
import com.darwin.sample.stickerscode.model.Sticker;
import com.darwin.sample.stickerscode.model.StickerPack;
import com.darwin.sample.stickerscode.utils.StickerPackLoader;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    private AppCompatActivity activity;

    public StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks, @NonNull OnAddButtonClickedListener onAddButtonClickedListener, @NonNull AppCompatActivity a) {
        this.stickerPacks = stickerPacks;
        this.onAddButtonClickedListener = onAddButtonClickedListener;
        this.activity = a;
    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.publisherView.getContext();
        viewHolder.publisherView.setText(pack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, pack.getTotalSize()));

        viewHolder.titleView.setText(pack.name);
        viewHolder.container.setOnClickListener(view -> {
            Log.d("AddStickerPackActivity", "addStickerPackToWhatsApp: sending stickor of "+pack.identifier+"; andd "+pack.name);
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
//            view.getContext().startActivity(intent);
            activity.startActivityForResult(intent, 999);
        });
        viewHolder.imageRowView.removeAllViews();

        Log.d("THEURI", "***************************onBindViewHolder: "+pack.custom);

        //if this sticker pack contains less stickers than the max, then take the smaller size.
        Uri picUri;
        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_packs_list_image_item, viewHolder.imageRowView, false);
            if (pack.custom==false) {
                Log.d("THEURI", "onBindViewHolder: "+pack.custom);
                picUri = StickerPackLoader.getStickerAssetUri(pack.identifier, pack.getStickers().get(i).imageFileName);
            }
            else {
                Sticker s = pack.getStickers().get(i);
                Log.d("StickerAdder", "*****: "+context.getExternalFilesDir(null)+File.separator+"custom_stickers"+File.separator+pack.identifier+File.separator+"stickers"+File.separator+s.imageFileName);
                File img = new File(context.getExternalFilesDir(null),"custom_stickers"+File.separator+pack.identifier+File.separator+"stickers"+File.separator+s.imageFileName);
                picUri = Uri.fromFile(img);
//                Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
//                rowImage.setBackground(new BitmapDrawable(context.getResources(), bitmap));
//                ImageView iv = (ImageView) activity.findViewById(R.id.test_iv);
//                Log.d("THEURI123", "onBindViewHolder:12345 "+bitmap.toString()+"  ::: "+iv);
//                iv.setImageURI(Uri.fromFile(img));
//                iv.setImageURI(s.uri);
//                Uri uri = Uri.fromFile(new File(s.path));
            }
            rowImage.setImageURI(picUri);
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = minMarginBetweenImages - lp.leftMargin - lp.rightMargin;
            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                rowImage.setLayoutParams(lp);
            }
            viewHolder.imageRowView.addView(rowImage);
        }
        setAddButtonAppearance(viewHolder.addButton, pack);
        viewHolder.animatedStickerPackIndicator.setVisibility(pack.animatedStickerPack ? View.VISIBLE : View.GONE);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton, null);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(v -> onAddButtonClickedListener.onAddButtonClicked(pack));
            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setImageRowSpec(int maxNumberOfStickersInARow, int minMarginBetweenImages) {
        this.minMarginBetweenImages = minMarginBetweenImages;
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
