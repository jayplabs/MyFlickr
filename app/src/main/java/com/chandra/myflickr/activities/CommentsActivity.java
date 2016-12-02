package com.chandra.myflickr.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.adapters.CommentsRecyclerAdapter;
import com.chandra.myflickr.events.CommentsDownloadedEvent;
import com.chandra.myflickr.events.FlickrPhotoCommentEvent;
import com.chandra.myflickr.managers.FlickrLoginManager;
import com.chandra.myflickr.models.FlickrPhoto;
import com.chandra.myflickr.models.PhotoComment;
import com.chandra.myflickr.services.UserPhotoService;
import com.chandra.myflickr.utils.StringUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import butterknife.BindView;

public class CommentsActivity extends BaseActivity {

    private static final Logger logger = LoggerFactory.getLogger(CommentsActivity.class.getSimpleName());

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    @BindView(R.id.toolbarImage)
    SimpleDraweeView mToolbarImage;

    @BindView(R.id.commentsRecyclerView)
    RecyclerView mCommentsRecyclerView;

    @BindView(R.id.noCommentsTextView)
    TextView mNoCommentsTextView;

    private int mPosition;
    private FlickrPhoto mPhoto;

    protected ArrayList<PhotoComment> mCommentsArray;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private static final String EXTRA_POSITION = "com.chandra.myflickr.activities.POSITION";
    private static final String EXTRA_PHOTO_OBJECT = "com.chandra.myflickr.activities.PHOTO_OBJECT";

    public static Intent newInstance(Context context, FlickrPhoto photo, int position) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_PHOTO_OBJECT, photo);
        return intent;
    }

    @Override
    protected void setLayoutResource() {
        setContentView(R.layout.activity_comments);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            finish();

        mPhoto = (FlickrPhoto) bundle.getSerializable(EXTRA_PHOTO_OBJECT);
        mPosition = bundle.getInt(EXTRA_POSITION);

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
        //pull comments from server
        downloadComments();
    }

    protected void initialiseUI() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        // Update toolbar
        updateToolbar();

        mLayoutManager = new LinearLayoutManager(this);
        mCommentsRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CommentsRecyclerAdapter(this, mCommentsArray);
        mCommentsRecyclerView.setAdapter(mAdapter);

        mFloatingActionButton.setOnClickListener(listener);

        //NoDataView
        refreshNoDataView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    protected void refreshNoDataView() {
        boolean isShowing = (mCommentsArray == null || mCommentsArray.size() <= 0);
        mNoCommentsTextView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
        mCommentsRecyclerView.setVisibility(isShowing ? View.GONE : View.VISIBLE);
    }

    private void updateToolbar() {
        mToolbar.setTitle(mPhoto.getName());
        mCollapsingToolbarLayout.setTitle(mPhoto.getName());
        loadImage(mPhoto.getUrl());
    }

    private void loadImage(String imageUrl) {
        mToolbarImage.setImageURI(imageUrl);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
            LayoutInflater inflater = LayoutInflater.from(CommentsActivity.this);
            View view = inflater.inflate(R.layout.comment_dialog, null);
            final EditText comment = (EditText) view.findViewById(R.id.commentData);
            builder.setView(view)
                   .setCancelable(false)
                   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           onCommentAdded(comment.getText().toString(), mPhoto, mPosition);
                       }
                   })
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           dialog.cancel();
                       }
                   }).create().show();
        }
    };

    protected void onCommentAdded(String comment, FlickrPhoto photo, int position) {
        Intent intent = new Intent(this, UserPhotoService.class);
        intent.setAction(UserPhotoService.ACTION_ADD_COMMENT);
        intent.putExtra(UserPhotoService.PHOTO_POSITION, String.valueOf(position));
        intent.putExtra(UserPhotoService.PHOTO_ID, photo.getPhotoId());
        intent.putExtra(UserPhotoService.PHOTO_COMMENT_AUTHOR, FlickrLoginManager.getUserName());
        intent.putExtra(UserPhotoService.PHOTO_COMMENT, comment);
        startService(intent);
    }

    protected void downloadComments() {
        Intent intent = new Intent(this, UserPhotoService.class);
        intent.setAction(UserPhotoService.ACTION_GET_COMMENTS);
        intent.putExtra(UserPhotoService.PHOTO_ID, mPhoto.getPhotoId());
        startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentsDownloadedEvent(CommentsDownloadedEvent event) {
        if (event == null)
            return;

        mCommentsArray = event.getCommentsArray();
        if (mCommentsArray != null) {
            logger.debug("Downloaded Comments count : " + mCommentsArray.size());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null && mAdapter instanceof CommentsRecyclerAdapter)
                    ((CommentsRecyclerAdapter)mAdapter).updateCommentsArray(mCommentsArray);
                refreshNoDataView();
            }
        });
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onCommentAddedEvent(FlickrPhotoCommentEvent event) {
        if (event == null)
            return;

        final String author = event.getAuthorName();
        if (StringUtils.isNull(author))
            return;
        final String comment = event.getCommentData();
        if (StringUtils.isNull(comment))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null && mAdapter instanceof CommentsRecyclerAdapter) {
                    ((CommentsRecyclerAdapter)mAdapter).addComment(new PhotoComment(comment, author));
                    mAdapter.notifyDataSetChanged();
                }
                refreshNoDataView();
            }
        });
    }

}
