package com.example.android.movie.data;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;

import com.example.android.movie.model.Movie;

/**
 * The MovieDataSourceFactory is responsible for creating a DataSource.
 */
public class MovieDataSourceFactory extends DataSource.Factory<Integer, Movie> {

    private MutableLiveData<MovieDataSource> mPostLiveData;
    private MovieDataSource mMovieDataSource;
    private String mSortBy;
    private String mSearch;

    public MovieDataSourceFactory(String sortBy, String search) {
        mPostLiveData = new MutableLiveData<>();
        mSortBy = sortBy;
        mSearch = search;
    }

    @Override
    public DataSource<Integer, Movie> create() {
        mMovieDataSource = new MovieDataSource(mSortBy,mSearch);

        // Keep reference to the data source with a MutableLiveData reference
        mPostLiveData = new MutableLiveData<>();
        mPostLiveData.postValue(mMovieDataSource);

        return mMovieDataSource;
    }

    public MutableLiveData<MovieDataSource> getPostLiveData() {
        return mPostLiveData;
    }
}
