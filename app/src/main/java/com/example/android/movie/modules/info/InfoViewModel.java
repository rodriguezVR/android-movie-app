package com.example.android.movie.modules.info;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.movie.data.MovieRepository;
import com.example.android.movie.model.MovieDetails;

/**
 * {@link ViewModel} for InformationFragment
 */
public class InfoViewModel extends ViewModel {

    private final MovieRepository mRepository;
    private final LiveData<MovieDetails> mMovieDetails;

    public InfoViewModel (MovieRepository repository, int movieId) {
        mRepository = repository;
        mMovieDetails = mRepository.getMovieDetails(movieId);
    }

    public LiveData<MovieDetails> getMovieDetails() {
        return mMovieDetails;
    }
}
