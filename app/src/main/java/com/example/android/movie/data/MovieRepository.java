package com.example.android.movie.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.android.movie.AppExecutors;
import com.example.android.movie.model.MovieDetails;
import com.example.android.movie.model.ReviewResponse;
import com.example.android.movie.model.VideoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.movie.utils.Constant.API_KEY;
import static com.example.android.movie.utils.Constant.CREDITS;
import static com.example.android.movie.utils.Constant.LANGUAGE;
import static com.example.android.movie.utils.Constant.PAGE;

/**
 * MovieRepository is responsible for handling data operations in Movie. Acts as a mediator
 * between {@link TheMovieApi} and {@link MovieDao}
 */
public class MovieRepository {

    private static final String TAG = MovieRepository.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static MovieRepository sInstance;
    private final MovieDao mMovieDao;
    private final TheMovieApi mTheMovieApi;
    private final AppExecutors mExecutors;

    private MovieRepository(MovieDao movieDao,
                            TheMovieApi theMovieApi,
                            AppExecutors executors) {
        mMovieDao = movieDao;
        mTheMovieApi = theMovieApi;
        mExecutors = executors;
    }

    public synchronized static MovieRepository getInstance(
            MovieDao movieDao, TheMovieApi theMovieApi, AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Making new repository");
                sInstance = new MovieRepository(movieDao, theMovieApi, executors);
            }
        }
        return sInstance;
    }

    /**
     * Make a network request by calling enqueue and provide a LiveData object of MovieDetails for ViewModel
     *
     * @param movieId The ID of the movie
     */
    public LiveData<MovieDetails> getMovieDetails(int movieId) {
        final MutableLiveData<MovieDetails> movieDetailsData = new MutableLiveData<>();

        mTheMovieApi.getDetails(movieId, API_KEY, LANGUAGE, CREDITS)
                .enqueue(new Callback<MovieDetails>() {
                    @Override
                    public void onResponse(Call<MovieDetails> call, Response<MovieDetails> response) {
                        if (response.isSuccessful()) {
                            MovieDetails movieDetails = response.body();
                            movieDetailsData.setValue(movieDetails);
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieDetails> call, Throwable t) {
                        movieDetailsData.setValue(null);
                        Log.e(TAG, "Failed getting MovieDetails: " + t.getMessage());
                    }
                });
        return movieDetailsData;
    }

    /**
     * Make a network request by calling enqueue and provide a LiveData object of ReviewResponse for ViewModel
     *
     * @param movieId The ID of the movie
     */
    public LiveData<ReviewResponse> getReviewResponse(int movieId) {
        final MutableLiveData<ReviewResponse> reviewResponseData = new MutableLiveData<>();

        mTheMovieApi.getReviews(movieId, API_KEY, LANGUAGE, PAGE)
                .enqueue(new Callback<ReviewResponse>() {
                    /**
                     * Invoked for a received HTTP response.
                     */
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        if (response.isSuccessful()) {
                            ReviewResponse reviewResponse = response.body();
                            reviewResponseData.setValue(reviewResponse);
                        }
                    }

                    /**
                     * Invoked when a network exception occurred talking to the server or when an unexpected exception
                     * occurred creating the request or processing the response.
                     */
                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
                        reviewResponseData.setValue(null);
                        Log.e(TAG, "Failed getting ReviewResponse: " + t.getMessage());
                    }
                });
        return reviewResponseData;
    }

    /**
     * Make a network request by calling enqueue and provide a LiveData object of VideoResponse for ViewModel
     *
     * @param movieId The ID of the movie
     */
    public LiveData<VideoResponse> getVideoResponse(int movieId) {
        final MutableLiveData<VideoResponse> videoResponseData = new MutableLiveData<>();

        mTheMovieApi.getVideos(movieId, API_KEY, LANGUAGE)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        if (response.isSuccessful()) {
                            VideoResponse videoResponse = response.body();
                            if (videoResponse != null) {
                                videoResponseData.setValue(videoResponse);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        videoResponseData.setValue(null);
                        Log.e(TAG, "Failed getting VideoResponse: " + t.getMessage());
                    }
                });
        return videoResponseData;
    }

    /**
     * Return a LiveData of the list of MovieEntries directly from the database
     */
    public LiveData<List<MovieEntry>> getFavoriteMovies() {
        return mMovieDao.loadAllMovies();
    }

    /**
     * Returns a LiveData of MovieEntry directly from the database
     *
     * @param movieId The movie ID
     */
    public LiveData<MovieEntry> getFavoriteMovieByMovieId(int movieId) {
        return mMovieDao.loadMovieByMovieId(movieId);
    }
}
