package com.example.android.movie.modules.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.movie.data.MovieEntry;
import com.example.android.movie.data.MovieRepository;

/**
 * {@link ViewModel} for Favorites
 */
public class FavViewModel extends ViewModel {

    private final MovieRepository mRepository;
    private LiveData<MovieEntry> mMovieEntry;

    public FavViewModel(MovieRepository repository, int movieId) {
        mRepository = repository;
        mMovieEntry = mRepository.getFavoriteMovieByMovieId(movieId);
    }

    public LiveData<MovieEntry> getMovieEntry() {
        return mMovieEntry;
    }
}
