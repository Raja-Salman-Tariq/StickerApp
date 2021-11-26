/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.darwin.sample.stickerscode.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.darwin.sample.BuildConfig;
import com.darwin.sample.R;
import com.darwin.sample.ViolaSampleActivity;
import com.darwin.sample.stickerscode.WhitelistCheck;
import com.darwin.sample.stickerscode.model.Sticker;
import com.darwin.sample.stickerscode.model.StickerContentProvider;
import com.darwin.sample.stickerscode.model.StickerPack;
import com.darwin.sample.stickerscode.recyclerview.StickerPackListAdapter;
import com.darwin.sample.stickerscode.recyclerview.StickerPackListItemViewHolder;
import com.darwin.sample.utils.ActivityResultHandler;
import com.darwin.sample.utils.StickerAndPackHandler;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class StickerPackListActivity extends AddStickerPackActivity {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private HashSet<StickerPack> stickerPackList;


    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("testo01", ": " + result.getData().getData());
                StickerPack newPack = result.getData().getParcelableExtra("pack");
//                if (newPack!=null)
//                    stickerPackList.add(newPack);
//                new StickerAndPackHandler().add(this, result.getData().getData(), this);

                ArrayList<String> identifiers = new ArrayList();

                for (StickerPack s : stickerPackList)
                    identifiers.add(s.identifier);

//                for (StickerPack s : new StickerAndPackHandler().get(this)) {
//                    if (!identifiers.contains(s)) {
//                        stickerPackList.add(s);
//                    }
//                }

                if (newPack!=null)
                    stickerPackList.add(newPack);

//                getContentResolver().insert(StickerContentProvider.);
                Snackbar snack = Snackbar.make(
                        getWindow().getDecorView().findViewById(android.R.id.content),
                        "Remember to add more stickers to this pack !",
                        Snackbar.LENGTH_LONG
                );
                snack.setAction("CLOSE", null);
                snack.show();
            }
    );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==999 && resultCode==RESULT_OK){
            StickerPack pack = data.getParcelableExtra("pack");
            stickerPackList.remove(pack);
//            Log.d("outerSticker", "removal of "+pack.identifier+" was "+stickerPackList.remove(pack));
            stickerPackList.add(pack);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);
        packRecyclerView = findViewById(R.id.sticker_pack_list);
        stickerPackList = new HashSet(getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA));
        showStickerPackList(new ArrayList(stickerPackList));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, stickerPackList.size()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[0]));
        for (StickerPack s : stickerPackList)
            if (s.identifier.contains("ack"))
                Log.d("outerSticker", "onResume: sz = "+s.getStickers().size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.action_info).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add){
            StickerPackListActivity myThis = this;
            Snackbar snack = Snackbar.make(
                    getWindow().getDecorView().findViewById(android.R.id.content),
                    "You're creating a custom sticker pack. Make sure to add at least 3 stickers " +
                            "from the menu after selecting this sticker pack else the pack will be deleted.",
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                    );
            snack.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGetContent.launch(new Intent(myThis, ViolaSampleActivity.class));
                }
            });

            TextView tv = (TextView) snack.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setMaxLines(5);

            snack.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList, onAddButtonClickedListener, this);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }


    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(pack.identifier, pack.name);


    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);
            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
            allStickerPacksListAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }


    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }
}
