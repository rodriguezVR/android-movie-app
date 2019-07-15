package com.example.android.movie.modules.main;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.android.movie.modules.GridSpacingItemDecoration;
import com.example.android.movie.R;
import com.example.android.movie.modules.detail.DetailActivity;
import com.example.android.movie.data.MovieEntry;
import com.example.android.movie.data.MoviePreferences;
import com.example.android.movie.databinding.ActivityMainBinding;
import com.example.android.movie.model.Movie;
import com.example.android.movie.utils.InjectorUtils;

import java.util.List;

import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;
import static com.example.android.movie.utils.Constant.GRID_INCLUDE_EDGE;
import static com.example.android.movie.utils.Constant.GRID_SPACING;
import static com.example.android.movie.utils.Constant.GRID_SPAN_COUNT;
import static com.example.android.movie.utils.Constant.LAYOUT_MANAGER_STATE;
import static com.example.android.movie.utils.Constant.REQUEST_CODE_DIALOG;

/**
 * The MainActivity displays the list of movies that appear as a grid of images
 */
public class MainActivity extends AppCompatActivity implements
        FavoriteAdapter.FavoriteAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MoviePagedListAdapter.MoviePagedListAdapterOnClickHandler {

    /**
     * Tag for a log message
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * MoviePagedListAdapter enables for data to be loaded in chunks
     */
    private MoviePagedListAdapter mMoviePagedListAdapter;

    /**
     * Exposes a list of favorite movies from a list of MovieEntry to a RecyclerView
     */
    private FavoriteAdapter mFavoriteAdapter;

    /**
     * String for the sort criteria("most popular and highest rated")
     */
    private String mSortCriteria;

    /**
     * String for searching online movies
     */
    private String mSearch;

    /**
     * Member variable for restoring list items positions on device rotation
     */
    private Parcelable mSavedLayoutState;

    /**
     * ViewModel for MainActivity
     */
    private MainActivityViewModel mMainViewModel;

    /**
     * This field is used for data binding
     */
    private ActivityMainBinding mMainBinding;

    /**
     * Used to call search with delay after text is changed.
     */
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mHandler = new Handler();

        initAdapter();

        mSortCriteria = MoviePreferences.getPreferredSortCriteria(this);

        if (savedInstanceState == null && !isOnline()) {
            mSortCriteria = getString(R.string.pref_sort_by_favorites);
            mMainBinding.radioFavorites.setChecked(true);
        }

        setupViewModel(mSortCriteria);
        updateUI();

        setRadioButton(mSortCriteria);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        setSwipeRefreshLayout();

        setColumnSpacing();

        if (savedInstanceState != null) {
            mSavedLayoutState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE);
            mMainBinding.rvMovie.getLayoutManager().onRestoreInstanceState(mSavedLayoutState);
        }

        mMainBinding.search.setText("");
        mSearch = "";
        mMainBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mHandler.removeCallbacksAndMessages(null);

                if (charSequence.length() > 1){
                    return;
                }

                if (charSequence.toString().isEmpty()) {
                    //viewVisibleAnimator(mMainBinding.radioGroup);
                    mMainBinding.radioGroup.setVisibility(View.VISIBLE);
                    if (!mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
                        scaleView(mMainBinding.radioGroup, 0, 1);
                    }
                } else if (!mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
                    //viewGoneAnimator(mMainBinding.radioGroup);
                    scaleView(mMainBinding.radioGroup,1,0);
                }
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                mSearch = editable.toString();
                if (!mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMainViewModel.setMoviePagedList(mSortCriteria, mSearch);
                            updateUI();
                        }
                    }, 500);
                } else {
                    if (mSearch.isEmpty()) {
                        updateUI();
                    } else {
                        mFavoriteAdapter.filter(editable.toString());
                    }

                }
            }
        });

        if (mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
            mMainBinding.search.setHint(getString(R.string.search_offline_placeholder));
        } else {
            mMainBinding.search.setHint(getString(R.string.search_online_placeholder));
        }
    }

    /**
     * Set the LayoutManager to the RecyclerView and create MoviePagedListAdapter and FavoriteAdapter
     */
    private void initAdapter() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
        mMainBinding.rvMovie.setLayoutManager(layoutManager);

        mMainBinding.rvMovie.setHasFixedSize(true);

        mMoviePagedListAdapter = new MoviePagedListAdapter(this);
        mFavoriteAdapter = new FavoriteAdapter(this, this);
    }

    /**
     * Get the MainActivityViewModel from the factory
     */
    private void setupViewModel(String sortCriteria) {
        MainViewModelFactory factory = InjectorUtils.provideMainActivityViewModelFactory(
                MainActivity.this, sortCriteria, "");
        mMainViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
    }

    /**
     * Update the UI depending on the sort criteria
     */
    private void updateUI() {
        mMainViewModel.setFavoriteMovies();

        if (mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
            mMainBinding.rvMovie.setAdapter(mFavoriteAdapter);
            observeFavoriteMovies();
        } else {
            mMainBinding.rvMovie.setAdapter(mMoviePagedListAdapter);
            observeMoviePagedList();
        }
    }

    /**
     * Update the MoviePagedList from LiveData in MainActivityViewModel
     */
    private void observeMoviePagedList() {
        mMainViewModel.getMoviePagedList().observe(this, new Observer<PagedList<Movie>>() {
            @Override
            public void onChanged(@Nullable PagedList<Movie> pagedList) {
                showMovieDataView();
                if (pagedList != null) {
                    mMoviePagedListAdapter.submitList(pagedList);
                    mMainBinding.rvMovie.getLayoutManager().onRestoreInstanceState(mSavedLayoutState);
                }

                if (!isOnline()) {
                    showMovieDataView();
                    showSnackbarOffline();
                }
            }
        });
    }

    /**
     * Update the list of MovieEntries from LiveData in MainActivityViewModel
     */
    private void observeFavoriteMovies() {
        mMainViewModel.getFavoriteMovies().observe(this, new Observer<List<MovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MovieEntry> movieEntries) {
                mFavoriteAdapter.setMovies(movieEntries);

                mMainBinding.rvMovie.getLayoutManager().onRestoreInstanceState(mSavedLayoutState);

                if (movieEntries == null || movieEntries.size() == 0) {
                    showEmptyView();
                } else if (!isOnline()) {
                    showMovieDataView();
                }
            }
        });
    }

    /**
     * When preferences have been changed, make a network request again.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_by_key))) {
            mSortCriteria = sharedPreferences.getString(key, getString(R.string.pref_sort_by_default));
        }

        mMainViewModel.setMoviePagedList(mSortCriteria, mSearch);
        updateUI();
        if (mSortCriteria.equals(getString(R.string.pref_sort_by_favorites))) {
            mMainBinding.search.setHint(getString(R.string.search_offline_placeholder));
        } else {
            mMainBinding.search.setHint(getString(R.string.search_online_placeholder));
        }
        if (mSearch == null || mSearch.isEmpty()) {
            return;
        }

        mSearch = "";
        mMainBinding.search.setText(mSearch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item clicks.
     *
     * @param movie The movie that was clicked
     */
    @Override
    public void onItemClick(Movie movie) {
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_MOVIE, movie);

        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(EXTRA_MOVIE, b);
        startActivity(intent);
    }

    @Override
    public void onFavItemClick(MovieEntry movieEntry) {
        int movieId = movieEntry.getMovieId();
        String originalTitle = movieEntry.getOriginalTitle();
        String title = movieEntry.getTitle();
        String posterPath = movieEntry.getPosterPath();
        String overview = movieEntry.getOverview();
        double voteAverage = movieEntry.getVoteAverage();
        String releaseDate = movieEntry.getReleaseDate();
        String backdropPath = movieEntry.getBackdropPath();

        Movie movie = new Movie(movieId, originalTitle, title, posterPath, overview,
                voteAverage, releaseDate, backdropPath);

        Bundle b = new Bundle();
        b.putParcelable(EXTRA_MOVIE, movie);

        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(EXTRA_MOVIE, b);
        startActivity(intent);
    }

    /**
     * Set the SwipeRefreshLayout triggered by a swipe gesture.
     */
    private void setSwipeRefreshLayout() {
        mMainBinding.swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));


        mMainBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            /**
             * Called when a swipe gesture triggers a refresh
             */
            @Override
            public void onRefresh() {
                showMovieDataView();
                if (!mSortCriteria.equals(getString(R.string.pref_sort_by_favorites)) || mSearch.isEmpty()) {
                    updateUI();
                } else {
                    mFavoriteAdapter.filter(mSearch);
                }


                hideRefresh();
            }
        });
    }

    /**
     * When online, show a snack bar message notifying updated
     *
     * @param isOnline True if connected to the network
     */
    private void showSnackbarRefresh(boolean isOnline) {
        if (isOnline) {
            Snackbar.make(mMainBinding.rvMovie, getString(R.string.snackbar_updated)
                    , Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if there is the network connectivity
     *
     * @return true if connected to the network
     */
    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Show a dialog when there is no internet connection
     *
     * @param isOnline true if connected to the network
     */
    private void showNetworkDialog(final boolean isOnline) {
        if (!isOnline) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert);
            builder.setIcon(R.drawable.ic_warning);
            builder.setTitle(getString(R.string.no_network_title));
            builder.setMessage(getString(R.string.no_network_message));
            builder.setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), REQUEST_CODE_DIALOG);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * This method will make the View for the movie data visible
     */
    private void showMovieDataView() {
        mMainBinding.tvEmpty.setVisibility(View.INVISIBLE);
        mMainBinding.rvMovie.setVisibility(View.VISIBLE);
    }

    /**
     * When there are no favorite movies, display an empty view
     */
    private void showEmptyView() {
        mMainBinding.tvEmpty.setVisibility(View.VISIBLE);
        mMainBinding.tvEmpty.setText(getString(R.string.message_empty_favorites));
        mMainBinding.tvEmpty.setTextColor(Color.WHITE);
    }

    /**
     * When offline, show a snackbar message
     */
    private void showSnackbarOffline() {
        Snackbar snackbar = Snackbar.make(
                mMainBinding.frameMain, R.string.snackbar_offline, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.WHITE);
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();
    }

    /**
     * Set column spacing to make each column have the same spacing.
     */
    private void setColumnSpacing() {
        GridSpacingItemDecoration decoration = new GridSpacingItemDecoration(
                GRID_SPAN_COUNT, GRID_SPACING, GRID_INCLUDE_EDGE);
        mMainBinding.rvMovie.addItemDecoration(decoration);
    }

    /**
     * Method for persisting data across Activity recreation
     * <p>
     * Reference: @see "https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll
     * -position-using-recyclerview-state"
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_STATE,
                mMainBinding.rvMovie.getLayoutManager().onSaveInstanceState());
    }

    /**
     * Hide refresh progress
     */
    private void hideRefresh() {
        mMainBinding.swipeRefresh.setRefreshing(false);
    }

    /**
     * Method called when category is selected
     *
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        String category = getString(R.string.pref_sort_by_default);
        if (view.getId() == mMainBinding.radioPopular.getId()) {
            category = getString(R.string.pref_sort_by_popular);
        } else if (view.getId() == mMainBinding.radioTopRated.getId()) {
            category = getString(R.string.pref_sort_by_top_rated);
        } else if (view.getId() == mMainBinding.radioUpcoming.getId()) {
            category = getString(R.string.pref_sort_by_upcoming);
        } else if (view.getId() == mMainBinding.radioFavorites.getId()) {
            category = getString(R.string.pref_sort_by_favorites);
        }

        MoviePreferences.setPreferredSortCriteria(this, category);
        mSortCriteria = category;
        updateUI();
    }

    /**
     * Method required to set radio button check.
     *
     * @param category string for checking radio button according
     */
    private void setRadioButton(String category) {
        if (category.equals(getString(R.string.pref_sort_by_popular))) {
            mMainBinding.radioPopular.setChecked(true);
        } else if (category.equals(getString(R.string.pref_sort_by_top_rated))) {
            mMainBinding.radioTopRated.setChecked(true);
        } else if (category.equals(getString(R.string.pref_sort_by_upcoming))) {
            mMainBinding.radioUpcoming.setChecked(true);
        } else if (category.equals(getString(R.string.pref_sort_by_favorites))) {
            mMainBinding.radioFavorites.setChecked(true);
        }
    }

    /**
     * Method for radio group scaling animation, when hiding / showing.
     *
     * @param v
     * @param startScale
     * @param endScale
     */
    public void scaleView(final View v, float startScale, final float endScale) {

        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        //anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (endScale == 0f) {
                    v.setVisibility(View.GONE);
                }
                v.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}

