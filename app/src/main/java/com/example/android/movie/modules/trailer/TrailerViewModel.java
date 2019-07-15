package com.example.android.movie.modules.trailer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.movie.data.MovieRepository;
import com.example.android.movie.model.VideoResponse;

/**
 * {@link ViewModel} for TrailerFragment
 */
public class TrailerViewModel extends ViewModel {

    private final MovieRepository mRepository;
    private final LiveData<VideoResponse> mVideoResponse;

    public TrailerViewModel (MovieRepository repository, int movieId) {
        mRepository = repository;
        mVideoResponse = mRepository.getVideoResponse(movieId);
    }

    public LiveData<VideoResponse> getVideoResponse() {
        return mVideoResponse;
    }
}
