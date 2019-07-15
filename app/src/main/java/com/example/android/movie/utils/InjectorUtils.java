package com.example.android.movie.utils;

import android.content.Context;

import com.example.android.movie.AppExecutors;
import com.example.android.movie.data.MovieDatabase;
import com.example.android.movie.data.MovieRepository;
import com.example.android.movie.data.TheMovieApi;
import com.example.android.movie.modules.main.FavViewModelFactory;
import com.example.android.movie.modules.info.InfoViewModelFactory;
import com.example.android.movie.modules.main.MainViewModelFactory;
import com.example.android.movie.modules.review.ReviewViewModelFactory;
import com.example.android.movie.modules.trailer.TrailerViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for Movie
 */
public class InjectorUtils {

    public static MovieRepository provideRepository(Context context) {
        MovieDatabase database = MovieDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        TheMovieApi theMovieApi = Controller.getClient().create(TheMovieApi.class);
        return MovieRepository.getInstance(database.movieDao(), theMovieApi, executors);
    }

    public static MainViewModelFactory provideMainActivityViewModelFactory(Context context, String sortCriteria, String search) {
        MovieRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository, sortCriteria, search);
    }

    public static InfoViewModelFactory provideInfoViewModelFactory(Context context, int movieId) {
        MovieRepository repository = provideRepository(context.getApplicationContext());
        return new InfoViewModelFactory(repository, movieId);
    }

    public static ReviewViewModelFactory provideReviewViewModelFactory(Context context, int movieId) {
        MovieRepository repository = provideRepository(context.getApplicationContext());
        return new ReviewViewModelFactory(repository, movieId);
    }

    public static TrailerViewModelFactory provideTrailerViewModelFactory(Context context, int movieId) {
        MovieRepository repository = provideRepository(context.getApplicationContext());
        return new TrailerViewModelFactory(repository, movieId);
    }

    public static FavViewModelFactory provideFavViewModelFactory(Context context, int movieId) {
        MovieRepository repository = provideRepository(context.getApplicationContext());
        return new FavViewModelFactory(repository, movieId);
    }
}
