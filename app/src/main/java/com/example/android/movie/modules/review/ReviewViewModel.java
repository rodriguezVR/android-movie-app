package com.example.android.movie.modules.review;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.movie.data.MovieRepository;
import com.example.android.movie.model.ReviewResponse;

/**
 * {@link ViewModel} for ReviewFragment
 */
public class ReviewViewModel extends ViewModel {
    private final MovieRepository mRepository;
    private final LiveData<ReviewResponse> mReviewResponse;

    public ReviewViewModel (MovieRepository repository, int movieId) {
        mRepository = repository;
        mReviewResponse = mRepository.getReviewResponse(movieId);
    }

    public LiveData<ReviewResponse> getReviewResponse() {
        return mReviewResponse;
    }
}
