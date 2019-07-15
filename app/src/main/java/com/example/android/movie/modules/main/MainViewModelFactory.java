package com.example.android.movie.modules.main;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.movie.data.MovieRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link MovieRepository} and String sortCriteria
 */
public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final MovieRepository mRepository;
    private final String mSortCriteria;
    private final String mSearch;

    public MainViewModelFactory(MovieRepository repository, String sortCriteria, String search) {
        mRepository = repository;
        mSortCriteria = sortCriteria;
        mSearch = search;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainActivityViewModel(mRepository, mSortCriteria, mSearch);
    }
}
