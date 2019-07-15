package com.example.android.movie.modules.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.android.movie.AppExecutors;
import com.example.android.movie.R;
import com.example.android.movie.data.MovieDatabase;
import com.example.android.movie.data.MovieEntry;
import com.example.android.movie.databinding.ActivityDetailBinding;
import com.example.android.movie.modules.info.InformationFragment;
import com.example.android.movie.modules.trailer.TrailerFragment;
import com.example.android.movie.model.Genre;
import com.example.android.movie.model.Movie;
import com.example.android.movie.model.MovieDetails;
import com.example.android.movie.model.Video;
import com.example.android.movie.utils.FormatUtils;
import com.example.android.movie.utils.InjectorUtils;
import com.example.android.movie.modules.main.FavViewModel;
import com.example.android.movie.modules.main.FavViewModelFactory;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.android.movie.utils.Constant.BACKDROP_FILE_SIZE;
import static com.example.android.movie.utils.Constant.CAST;
import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;
import static com.example.android.movie.utils.Constant.IMAGE_BASE_URL;
import static com.example.android.movie.utils.Constant.RELEASE_YEAR_BEGIN_INDEX;
import static com.example.android.movie.utils.Constant.RELEASE_YEAR_END_INDEX;
import static com.example.android.movie.utils.Constant.RESULTS_GENRE;
import static com.example.android.movie.utils.Constant.RESULTS_RELEASE_YEAR;
import static com.example.android.movie.utils.Constant.RESULTS_RUNTIME;
import static com.example.android.movie.utils.Constant.SHARE_INTENT_TYPE_TEXT;
import static com.example.android.movie.utils.Constant.SHARE_URL;
import static com.example.android.movie.utils.Constant.YOUTUBE_BASE_URL;

/**
 * This activity is responsible for displaying the details for a selected movie.
 */
public class DetailActivity extends AppCompatActivity implements
        InformationFragment.OnInfoSelectedListener, TrailerFragment.OnTrailerSelectedListener,
        InformationFragment.OnViewAllSelectedListener {

    /** Tag for logging */
    public static final String TAG = DetailActivity.class.getSimpleName();

    /** ViewModel for Favorites */
    private FavViewModel mFavViewModel;

    /** True when the movie is in favorites collection, otherwise false */
    private boolean mIsInFavorites;

    /** Member variable for the MovieDatabase*/
    private MovieDatabase mDb;

    /** Member variable for the MovieEntry */
    private MovieEntry mMovieEntry;

    /** Movie object */
    private Movie mMovie;

    /** This field is used for data binding */
    private ActivityDetailBinding mDetailBinding;

    /** The first trailer's YouTube URL */
    private String mFirstVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_MOVIE)) {
                Bundle b = intent.getBundleExtra(EXTRA_MOVIE);
                mMovie = b.getParcelable(EXTRA_MOVIE);
            }
        }

        mDb = MovieDatabase.getInstance(getApplicationContext());
        mIsInFavorites = isInFavoritesCollection();

        setupUI();

        if (savedInstanceState != null) {
            mDetailBinding.pbDetailLoadingIndicator.setVisibility(View.GONE);

            String resultRuntime = savedInstanceState.getString(RESULTS_RUNTIME);
            String resultReleaseYear = savedInstanceState.getString(RESULTS_RELEASE_YEAR);
            String resultGenre = savedInstanceState.getString(RESULTS_GENRE);

            mDetailBinding.tvRuntime.setText(resultRuntime);
            mDetailBinding.tvReleaseYear.setText(resultReleaseYear);
            mDetailBinding.tvGenre.setText(resultGenre);
        }
    }

    /**
     *  This method is called from onCreate to setup the UI
     */
    private void setupUI() {
        showUpButton();

        mDetailBinding.tabLayout.setupWithViewPager(mDetailBinding.contentDetail.viewpager);
        mDetailBinding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        DetailPagerAdapter pagerAdapter = new DetailPagerAdapter(
                this, getSupportFragmentManager());
        mDetailBinding.contentDetail.viewpager.setAdapter(pagerAdapter);

        setCollapsingToolbarTitle();
        loadBackdropImage();
        setTitle();

        showLoading(isOnline());
        if (!isOnline()) {
            loadMovieDetailData();
        }
    }

    /**
     * This method is called when the fab button is clicked.
     * If the movie is not in the favorites collection, insert the movie data into the database.
     * Otherwise, delete the movie data from the database
     */
    public void onFavoriteClick(View view) {
        mMovieEntry = getMovieEntry();

        if (!mIsInFavorites) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().insertMovie(mMovieEntry);
                }
            });

            showSnackbarAdded();
        } else {
            mMovieEntry = mFavViewModel.getMovieEntry().getValue();
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().deleteMovie(mMovieEntry);
                }
            });

            showSnackbarRemoved();
        }
    }

    /**
     * Returns a MovieEntry
     */
    private MovieEntry getMovieEntry() {
        String runtime = mDetailBinding.tvRuntime.getText().toString();
        String releaseYear = mDetailBinding.tvReleaseYear.getText().toString();
        String genre = mDetailBinding.tvGenre.getText().toString();

        mMovieEntry = new MovieEntry(mMovie.getId(), mMovie.getOriginalTitle(), mMovie.getTitle(),
                mMovie.getPosterPath(), mMovie.getOverview(), mMovie.getVoteAverage(),
                mMovie.getReleaseDate(), mMovie.getBackdropPath(), new Date(),
                runtime, releaseYear, genre);

        return mMovieEntry;
    }

    /**
     * When offline, display runtime, release year, and genre of the movie.
     */
    private void loadMovieDetailData() {
        FavViewModelFactory factory = InjectorUtils.provideFavViewModelFactory(
                DetailActivity.this, mMovie.getId());
        mFavViewModel = ViewModelProviders.of(this, factory).get(FavViewModel.class);

        mFavViewModel.getMovieEntry().observe(this, new Observer<MovieEntry>() {
            @Override
            public void onChanged(@Nullable MovieEntry movieEntry) {
                if (movieEntry != null) {
                    mDetailBinding.tvRuntime.setText(movieEntry.getRuntime());
                    mDetailBinding.tvReleaseYear.setText(movieEntry.getReleaseYear());
                    mDetailBinding.tvGenre.setText(movieEntry.getGenre());
                }
            }
        });
    }

    /**
     * Return true and set a favoriteFab image to full heart image if the movie is in favorites collection.
     * Otherwise return false and set favoriteFab image to border heart image.
     */
    private boolean isInFavoritesCollection() {
        FavViewModelFactory factory = InjectorUtils.provideFavViewModelFactory(
                DetailActivity.this, mMovie.getId());
        mFavViewModel = ViewModelProviders.of(this, factory).get(FavViewModel.class);

        mFavViewModel.getMovieEntry().observe(this, new Observer<MovieEntry>() {
            @Override
            public void onChanged(@Nullable MovieEntry movieEntry) {
                if (mFavViewModel.getMovieEntry().getValue() == null) {
                    mDetailBinding.fab.setImageResource(R.drawable.favorite_border);
                    mIsInFavorites = false;
                } else {
                    mDetailBinding.fab.setImageResource(R.drawable.favorite);
                    mIsInFavorites = true;
                }
            }
        });
        return mIsInFavorites;
    }

    /**
     * Show a snackbar message when a movie added to MovieDatabase
     *
     * Reference: @see "https://stackoverflow.com/questions/34020891/how-to-change-background-color-of-the-snackbar"
     */
    private void showSnackbarAdded() {
        Snackbar snackbar = Snackbar.make(
                mDetailBinding.coordinator, R.string.snackbar_added, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.WHITE);
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();
    }

    /**
     * Show a snackbar message when a movie removed from MovieDatbase
     */
    private void showSnackbarRemoved() {
        Snackbar snackbar = Snackbar.make(
                mDetailBinding.coordinator, R.string.snackbar_removed, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.WHITE);
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();
    }

    /**
     * Define the behavior for onTrailerSelected
     */
    @Override
    public void onTrailerSelected(final Video video) {
        mDetailBinding.ivPlayCircle.setVisibility(View.VISIBLE);

        String firstVideoKey = video.getKey();
        mFirstVideoUrl = YOUTUBE_BASE_URL + firstVideoKey;

        mDetailBinding.ivPlayCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTrailer(mFirstVideoUrl);
            }
        });
    }

    /**
     * Use Intent to open a YouTube link in either the native app or a web browser of choice
     *
     * @param videoUrl The first trailer's YouTube URL
     */
    private void launchTrailer(String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Show an up button in Collapsing Toolbar
     */
    private void showUpButton() {
        setSupportActionBar(mDetailBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Display the backdrop image
     */
    private void loadBackdropImage() {
        String backdropPath = mMovie.getBackdropPath();
        String backdrop = IMAGE_BASE_URL + BACKDROP_FILE_SIZE + backdropPath;
        Picasso.with(this)
                .load(backdrop)
                .error(R.drawable.photo)
                .into(mDetailBinding.ivBackdrop);
    }

    /**
     * The {@link Movie} object contains information, such as ID, original title, title, poster path,
     * vote average, release date, and backdrop path. Get the title from the {@link Movie} and
     * set the title to the TextViews
     */
    private void setTitle() {
        String title = mMovie.getTitle();
        mDetailBinding.tvDetailTitle.setText(title);
    }

    /**
     * Get the release date from the {@link Movie} and display the release year. This method is
     * called as soon as the loading indicator is gone.
     */
    private void showReleaseYear() {
        String releaseDate = mMovie.getReleaseDate();
        String releaseYear = releaseDate.substring(RELEASE_YEAR_BEGIN_INDEX, RELEASE_YEAR_END_INDEX);
        mDetailBinding.tvReleaseYear.setText(releaseYear);
    }

    /**
     * Show the title in the app bar when a CollapsingToolbarLayout is fully collapsed, otherwise hide the title.
     *
     * Reference: @see "https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed"
     */
    private void setCollapsingToolbarTitle() {
        mDetailBinding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mDetailBinding.collapsingToolbarLayout.setTitle(mMovie.getTitle());
                    isShow = true;
                } else if (isShow) {
                    mDetailBinding.collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Define the behavior for onInformationSelected
     * @param movieDetails The movie details contains information, such as budget, genre, runtime,
     *                    revenue, status, vote count, credits.
     */
    @Override
    public void onInformationSelected(MovieDetails movieDetails) {
        mDetailBinding.pbDetailLoadingIndicator.setVisibility(View.GONE);

        showReleaseYear();

        int runtime = movieDetails.getRuntime();
        mDetailBinding.tvRuntime.setText(FormatUtils.formatTime(this, runtime));

        List<Genre> genres = movieDetails.getGenres();
        List<String> genresStrList = new ArrayList<>();

        for (int i = 0; i < genres.size(); i++) {
            Genre genre = genres.get(i);
            String genreName = genre.getGenreName();
            genresStrList.add(genreName);
        }
        String genreStr = TextUtils.join(getString(R.string.delimiter_comma), genresStrList);
        mDetailBinding.tvGenre.setText(genreStr);
    }

    /**
     * When the arrow icon in the app bar is clicked, finishes DetailActivity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Uses the ShareCompat Intent builder to create our share intent for sharing.
     * Return the newly created intent.
     *
     * @return The Intent to use to start our share.
     */
    private Intent createShareIntent() {
        String shareText = getString(R.string.check_out) + mMovie.getTitle()
                + getString(R.string.new_line) + SHARE_URL + mMovie.getId();
        if (mFirstVideoUrl != null) {
            shareText += getString(R.string.new_line) + getString(R.string.youtube_trailer)
                    + mFirstVideoUrl;
        }

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType(SHARE_INTENT_TYPE_TEXT)
                .setText(shareText)
                .setChooserTitle(getString(R.string.chooser_title))
                .createChooserIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    /**
     * Persist the runtime, release year, and genre of the movie by saving the data in onSaveInstanceState.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String resultRuntime = mDetailBinding.tvRuntime.getText().toString();
        outState.putString(RESULTS_RUNTIME, resultRuntime);

        String resultReleaseYear = mDetailBinding.tvReleaseYear.getText().toString();
        outState.putString(RESULTS_RELEASE_YEAR, resultReleaseYear);

        String resultGenre = mDetailBinding.tvGenre.getText().toString();
        outState.putString(RESULTS_GENRE, resultGenre);
    }

    /**
     * Switch to CastFragment in a ViewPager when "VIEW ALL" TextView is clicked in the DetailActivity
     */
    @Override
    public void onViewAllSelected() {
        mDetailBinding.contentDetail.viewpager.setCurrentItem(CAST);
    }

    /**
     * Check if there is the network connectivity
     *
     * @return true if connected to the network
     */
    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * When online, show loading indicator, otherwise hide loading indicator.
     *
     * @param isOnline true if connected to the network
     */
    private void showLoading(boolean isOnline) {
        if (!isOnline) {
            mDetailBinding.pbDetailLoadingIndicator.setVisibility(View.GONE);
        } else {
            mDetailBinding.pbDetailLoadingIndicator.setVisibility(View.VISIBLE);
        }
    }
}
