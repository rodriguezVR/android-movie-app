package com.example.android.movie.data;

import com.example.android.movie.model.MovieDetails;
import com.example.android.movie.model.MovieResponse;
import com.example.android.movie.model.ReviewResponse;
import com.example.android.movie.model.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The movie api interface. Retrofit turns HTTP API into a Java interface.
 */
public interface TheMovieApi {

    @GET("search/movie")
    Call<MovieResponse> getMoviesBySearch(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page,
            @Query("query") String search
    );

    @GET("movie/{sort_criteria}")
    Call<MovieResponse> getMovies(
            @Path("sort_criteria") String sortCriteria,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/{id}")
    Call<MovieDetails> getDetails(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("append_to_response") String credits
    );

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getReviews(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/{id}/videos")
    Call<VideoResponse> getVideos(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

}
