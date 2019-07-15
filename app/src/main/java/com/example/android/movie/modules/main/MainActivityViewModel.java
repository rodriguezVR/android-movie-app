package com.example.android.movie.modules.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.example.android.movie.data.MovieDataSourceFactory;
import com.example.android.movie.data.MovieEntry;
import com.example.android.movie.data.MovieRepository;
import com.example.android.movie.model.Movie;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.android.movie.utils.Constant.INITIAL_LOAD_SIZE_HINT;
import static com.example.android.movie.utils.Constant.NUMBER_OF_FIXED_THREADS_FIVE;
import static com.example.android.movie.utils.Constant.PAGE_SIZE;
import static com.example.android.movie.utils.Constant.PREFETCH_DISTANCE;

/**
 * {@link ViewModel} for MainActivity
 */
public class MainActivityViewModel extends ViewModel {

    private final MovieRepository mRepository;

    private LiveData<PagedList<Movie>> mMoviePagedList;
    private LiveData<List<MovieEntry>> mFavoriteMovies;


    public MainActivityViewModel(MovieRepository repository, String sortCriteria, String search) {
        mRepository = repository;
        init(sortCriteria,"");
    }

    /**
     * Initialize the paged list
     */
    private void init(String sortCriteria, String search) {
        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_FIXED_THREADS_FIVE);

        MovieDataSourceFactory movieDataFactory = new MovieDataSourceFactory(sortCriteria,search);

        PagedList.Config config = (new PagedList.Config.Builder())
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(INITIAL_LOAD_SIZE_HINT)
                .setPageSize(PAGE_SIZE)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .build();

        mMoviePagedList = new LivePagedListBuilder<>(movieDataFactory, config)
                .setFetchExecutor(executor)
                .build();
    }

    /**
     * Returns LiveData of PagedList of movie
     */
    public LiveData<PagedList<Movie>> getMoviePagedList() {
        return mMoviePagedList;
    }

    /**
     * Set the LiveData of PagedList of movie to clear the old list and reload
     *
     * @param sortCriteria The sort order of the movies by popular, top rated,
     *                     upcoming, and favorites
     */
    public void setMoviePagedList(String sortCriteria, String search) {
        init(sortCriteria,search);
    }

    /**
     * Returns LiveData of the List of MovieEntries
     */
    public LiveData<List<MovieEntry>> getFavoriteMovies() {
        return mFavoriteMovies;
    }

    /**
     *  Set a new value for the list of MovieEntries
     */
    public void setFavoriteMovies() {
        mFavoriteMovies = mRepository.getFavoriteMovies();
    }
}
