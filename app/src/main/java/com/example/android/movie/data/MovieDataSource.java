package com.example.android.movie.data;

import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.movie.model.Movie;
import com.example.android.movie.model.MovieResponse;
import com.example.android.movie.utils.Constant;
import com.example.android.movie.utils.Controller;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.movie.utils.Constant.NEXT_PAGE_KEY_TWO;
import static com.example.android.movie.utils.Constant.PREVIOUS_PAGE_KEY_ONE;
import static com.example.android.movie.utils.Constant.RESPONSE_CODE_API_STATUS;

/**
 * The MovieDataSource is the base class for loading snapshots of movie data into a given PagedList,
 * which is backed by the network. Since the TMDb API includes a key with each page load, extend
 * from PageKeyedDataSource.
 *
 */
public class MovieDataSource extends PageKeyedDataSource<Integer, Movie> {

    /** Tag for logging */
    private static final String TAG = MovieDataSource.class.getSimpleName();

    /** Member variable for TheMovieApi interface */
    private TheMovieApi mTheMovieApi;

    /** String for the sort order of the movies */
    private String mSortCriteria;

    /** String for searching online the movies */
    private String mSearch;

    public MovieDataSource(String sortCriteria, String search) {
        mTheMovieApi = Controller.getClient().create(TheMovieApi.class);
        mSortCriteria = sortCriteria;
        mSearch = search;
    }

    /**
     * This method is called first to initialize a PageList with data.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params,
                            @NonNull final LoadInitialCallback<Integer, Movie> callback) {

        if (mSearch == null || mSearch.isEmpty()) {
            mTheMovieApi.getMovies(mSortCriteria, Constant.API_KEY, Constant.LANGUAGE, Constant.PAGE_ONE)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful()) {
                                callback.onResult(response.body().getMovieResults(),
                                        PREVIOUS_PAGE_KEY_ONE, NEXT_PAGE_KEY_TWO);

                            } else if (response.code() == RESPONSE_CODE_API_STATUS) {
                                Log.e(TAG, "Invalid Api key. Response code: " + response.code());
                            } else {
                                Log.e(TAG, "Response Code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            Log.e(TAG, "Failed initializing a PageList: " + t.getMessage());
                        }
                    });
        }else {
            mTheMovieApi.getMoviesBySearch(Constant.API_KEY, Constant.LANGUAGE, Constant.PAGE_ONE, mSearch)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful()) {
                                callback.onResult(response.body().getMovieResults(),
                                        PREVIOUS_PAGE_KEY_ONE, NEXT_PAGE_KEY_TWO);

                            } else if (response.code() == RESPONSE_CODE_API_STATUS) {
                                Log.e(TAG, "Invalid Api key. Response code: " + response.code());
                            } else {
                                Log.e(TAG, "Response Code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            Log.e(TAG, "Failed initializing a PageList: " + t.getMessage());
                        }
                    });
        }
    }

    /**
     * Prepend page with the key specified by LoadParams.key
     */
    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params,
                           @NonNull LoadCallback<Integer, Movie> callback) {

    }

    /**
     * Append page with the key specified by LoadParams.key
     */
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params,
                          @NonNull final LoadCallback<Integer, Movie> callback) {

        final int currentPage = params.key;

        if (mSearch == null || mSearch.isEmpty()) {
            mTheMovieApi.getMovies(mSortCriteria, Constant.API_KEY, Constant.LANGUAGE, currentPage)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful()) {
                                int nextKey = currentPage + 1;
                                callback.onResult(response.body().getMovieResults(), nextKey);
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            Log.e(TAG, "Failed appending page: " + t.getMessage());
                        }
                    });

        }else {
            mTheMovieApi.getMoviesBySearch(Constant.API_KEY, Constant.LANGUAGE, currentPage,mSearch)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful()) {
                                int nextKey = currentPage + 1;
                                callback.onResult(response.body().getMovieResults(), nextKey);
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            Log.e(TAG, "Failed appending page: " + t.getMessage());
                        }
                    });
        }

    }
}
