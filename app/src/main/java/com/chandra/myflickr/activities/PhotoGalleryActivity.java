package com.chandra.myflickr.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.adapters.PhotosAdapter;
import com.chandra.myflickr.events.FlickrPhotoCommentEvent;
import com.chandra.myflickr.events.FlickrPhotoEvent;
import com.chandra.myflickr.managers.CacheManager;
import com.chandra.myflickr.managers.FlickrLoginManager;
import com.chandra.myflickr.misc.QueryPreferences;
import com.chandra.myflickr.models.FlickrPhoto;
import com.chandra.myflickr.services.UserPhotoService;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;

public class PhotoGalleryActivity extends BaseActivity implements
                                                       PhotosAdapter.OnPhotoItemClickListener,
                                                       SwipeRefreshLayout.OnRefreshListener {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryActivity.class.getSimpleName());

    @BindView(R.id.photo_gallery_recycler_view)
    RecyclerView mPhotoRecyclerView;

    @BindView(R.id.no_data_view)
    TextView mNoDataTextView;

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout mRefreshLayout;

    SearchView mSearchView;

    protected Type mListType;
    protected String mCacheKey;

    protected ArrayList<FlickrPhoto> mDataArray;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private static boolean isSearchExpanded = false;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, PhotoGalleryActivity.class);
        return intent;
    }

    @Override
    protected void setLayoutResource() {
        setContentView(R.layout.activity_photogallery);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            if (!FlickrLoginManager.hasLogin()) {
                goToLoginActivity(true);
                return;
            }

        initialiseData();
        initialiseUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //----------------------------------------------------------------------------------------------
    // Setup
    //----------------------------------------------------------------------------------------------

    protected void initialiseData() {
        if (mListType == null)
            mListType = new TypeToken<ArrayList<FlickrPhoto>>() {
            }.getType();
        if (mCacheKey == null || mCacheKey.trim().length() <= 0)
            mCacheKey = this.getClass().getSimpleName();

        this.mDataArray = restoreCacheList();

        //pull data from server
        getDataFromServer(false, null);
    }

    protected void initialiseUI() {
        //Swipe refresh
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1,
                R.color.swipe_color_2,
                R.color.swipe_color_3,
                R.color.swipe_color_4);

        //Recycler View
        mPhotoRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mPhotoRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PhotosAdapter(this, mDataArray);
        mPhotoRecyclerView.setAdapter(mAdapter);

        //NoDataView
        refreshNoDataView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(queryTextListener);
        mSearchView.setQueryHint("Enter space seperated tags");

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isSearchExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isSearchExpanded = false;
                getDataFromServer(false, null);
                return true;
            }
        });

        mSearchView.clearFocus();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                goToLoginActivity(false);
                return true;
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(PhotoGalleryActivity.this, null);
                isSearchExpanded = false;
                finish();
                startActivity(getIntent());
                getDataFromServer(false, null);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    //----------------------------------------------------------------------------------------------
    // Event
    //----------------------------------------------------------------------------------------------

    @Override
    public void onRefresh() {
        if (isSearchExpanded) {
            EditText et = (EditText) findViewById(R.id.search_src_text);
            mSearchView.clearFocus();

            String query = et.getText().toString();
            if (query == null) {
                query = QueryPreferences.getStoredQuery(this);
            }
            String[] tags = query.split(" ");
            getDataFromServer(true, tags);
        } else {
            getDataFromServer(true, null);
        }
    }

    @Override
    public void onPhotoItemClicked(int position) {
        Intent intent = PhotoViewerActivity.newInstance(this, mDataArray, position);
        startActivity(intent);
    }

    protected void goToLoginActivity(boolean hasOverride) {
        Intent intent = LoginActivity.newInstance(this, !hasOverride);
        startActivity(intent);

        if (hasOverride) {
            overridePendingTransition(0, 0);
        }
        finish();
    }

    protected void refreshNoDataView() {
        boolean isShowing = (mDataArray == null || mDataArray.size() <= 0);
        mNoDataTextView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
        mPhotoRecyclerView.setVisibility(isShowing ? View.GONE : View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------
    // Data Photo Section
    //----------------------------------------------------------------------------------------------

    protected void getDataFromServer(boolean isSwipeRefresh, String[] tags) {
        if (!isSwipeRefresh) {
            showLoadingDialog();
        }

        Intent intent = new Intent(this, UserPhotoService.class);
        intent.setAction(UserPhotoService.ACTION_GET_PHOTOS);
        if (tags != null) {
            intent.putExtra(UserPhotoService.PHOTOS_BY_TAG, tags);
        }
        startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotosDownloadedEvent(FlickrPhotoEvent event) {
        dismissLoadingDialog();
        if (event == null)
            return;

        mDataArray = event.getDataArray();
        if (mDataArray != null) {
            logger.debug("get user photo done: " + mDataArray.size());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null && mAdapter instanceof PhotosAdapter)
                    ((PhotosAdapter) mAdapter).updateDataArray(mDataArray);
                mRefreshLayout.setRefreshing(false);
                refreshNoDataView();
            }
        });

        saveCacheList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentAddedEvent(FlickrPhotoCommentEvent event) {
        if (event == null)
            return;

        String commentId = event.getCommentId();
        final int position = event.getPosition();
        if (commentId == null) {
            logger.debug("Comment not added");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FlickrPhoto photo = mDataArray.get(position);
                mDataArray.get(position).setCommentSum(photo.getCommentSum() + 1);
                //TODO : This doesn't update counter automatically
                //((PhotosAdapter) mAdapter).updateDataArray(mDataArray);
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    protected ArrayList<FlickrPhoto> restoreCacheList() {
        String cacheKey = this.mCacheKey;
        if (cacheKey == null || cacheKey.length() <= 0) {
            cacheKey = this.getClass().getSimpleName();
        }

        return CacheManager.getListCacheData(cacheKey, mListType);
    }

    protected void saveCacheList() {
        String cacheKey = this.mCacheKey;
        if (cacheKey == null || cacheKey.length() <= 0) {
            cacheKey = this.getClass().getSimpleName();
        }

        CacheManager.saveListCacheData(cacheKey, mDataArray);
    }

    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            QueryPreferences.setStoredQuery(PhotoGalleryActivity.this, query);
            String[] tags = query.split(" ");
            logger.debug(Arrays.toString(tags));
            getDataFromServer(false, tags);
            mSearchView.clearFocus();
            return true;
        }
    };

}
