package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    public long getmSelectedItemId() {
        return mSelectedItemId;
    }

    public void setmSelectedItemId(long mSelectedItemId) {
        this.mSelectedItemId = mSelectedItemId;
    }

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private Toolbar mToolbar;
    int ALL_ARTICLES_LOADER = 0;
    int CURRENT_ARTICLE_LOADER = 1;

    private int mMutedColor = 0xFF333333;
    String TAG = ArticleDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        // mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        getLoaderManager().initLoader(ALL_ARTICLES_LOADER, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
               // Log.v(TAG, "onPageScrolled" + position + "position offset" + positionOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.v(TAG, "onPageScrollStateChanged" + state);
                //TODO check this aimation
//                mUpButton.animate()
//                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
//                        .setDuration(300);
//
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(TAG, "onPageSelected" + position);
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                //reset shared element transaction name
                if (position == 0) {
                    Log.v(TAG, "first fragment");
                    ((ArticleDetailFragment) mPagerAdapter.getItem(position + 1)).setImageTransitionName(null);

                } else if (position == mPagerAdapter.getCount()) {
                    Log.v(TAG, "last fragment");
                    ((ArticleDetailFragment) mPagerAdapter.getItem(position - 1)).setImageTransitionName(null);
                } else {
                    Log.v(TAG, "middle fragment");
                    ((ArticleDetailFragment) mPagerAdapter.getItem(position - 1)).setImageTransitionName(null);
                    ((ArticleDetailFragment) mPagerAdapter.getItem(position + 1)).setImageTransitionName(null);
                }

                ((ArticleDetailFragment) mPagerAdapter.getItem(position)).setImageTransitionName(getString(R.string.transition_name_of_pic));
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                getLoaderManager().initLoader(CURRENT_ARTICLE_LOADER, null, ArticleDetailActivity.this);
                updateUpButtonPosition();

            }
        });
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
                getLoaderManager().initLoader(CURRENT_ARTICLE_LOADER, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Loader<Cursor> returnLoader = null;
        if (i == ALL_ARTICLES_LOADER) {
            returnLoader = ArticleLoader.newAllArticlesInstance(this);
        } else if (i == CURRENT_ARTICLE_LOADER) {
            returnLoader = ArticleLoader.newInstanceForItemId(this, mSelectedItemId);
        }
        return returnLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == ALL_ARTICLES_LOADER) {
            mCursor = cursor;
            mPagerAdapter.notifyDataSetChanged();

            // Select the start ID
            if (mStartId > 0) {
                mCursor.moveToFirst();
                // TODO: optimize
                while (!mCursor.isAfterLast()) {
                    if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                        final int position = mCursor.getPosition();
                        mPager.setCurrentItem(position, false);
                        break;
                    }
                    mCursor.moveToNext();
                }
                mStartId = 0;
            }
        } else {
            if (mCursor != null) {
                ImageLoaderHelper.getInstance(this).getImageLoader()
                        .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
                                    Palette p = Palette.generate(bitmap, 12);

                                    mMutedColor = p.getDarkMutedColor(ContextCompat.getColor(ArticleDetailActivity.this, R.color.theme_primary));

                                    setTaskBarColored(ArticleDetailActivity.this, mMutedColor);

                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });
            }
        }
    }

    public void setTaskBarColored(Activity activity, int color) {
        Log.v(TAG, "setTaskBarColored");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


            window.setStatusBarColor(color);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (cursorLoader.getId() == ALL_ARTICLES_LOADER) {
            mCursor = null;
            mPagerAdapter.notifyDataSetChanged();
        }
    }


    private void updateUpButtonPosition() {
//        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
//        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                // mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
