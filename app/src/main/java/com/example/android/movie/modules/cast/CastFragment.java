package com.example.android.movie.modules.cast;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.movie.R;
import com.example.android.movie.databinding.FragmentCastBinding;
import com.example.android.movie.model.Cast;
import com.example.android.movie.model.Credits;
import com.example.android.movie.model.Movie;
import com.example.android.movie.model.MovieDetails;
import com.example.android.movie.utils.InjectorUtils;
import com.example.android.movie.modules.info.InfoViewModel;
import com.example.android.movie.modules.info.InfoViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;

/**
 * The CastFragment displays all of the cast members for the selected movie.
 */
public class CastFragment extends Fragment {

    /** Tag for a log message */
    public static final String TAG = CastFragment.class.getSimpleName();

    /** Member variable for the list of casts */
    private List<Cast> mCastList;

    /** Member variable for CastAdapter */
    private CastAdapter mCastAdapter;

    /** This field is used for data binding */
    private FragmentCastBinding mCastBinding;

    /** Member variable for the Movie object */
    private Movie mMovie;

    /**
     *  ViewModel for InformationFragment.
     *  MovieDetails data contains the cast data of the movie, and get casts data from the getDetails
     *  method in the InfoViewModel
     */
    private InfoViewModel mInfoViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public CastFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCastBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_cast, container, false);
        View rootView = mCastBinding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCastBinding.rvCast.setLayoutManager(layoutManager);
        mCastBinding.rvCast.setHasFixedSize(true);

        mCastList = new ArrayList<>();

        mCastAdapter = new CastAdapter(mCastList);
        mCastBinding.rvCast.setAdapter(mCastAdapter);

        showOfflineMessage(isOnline());

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMovie = getMovieData();
        setupViewModel(this.getActivity(), mMovie.getId());
    }

    private void setupViewModel(Context context, int movieId) {
        InfoViewModelFactory factory = InjectorUtils.provideInfoViewModelFactory(context, movieId);
        mInfoViewModel = ViewModelProviders.of(this, factory).get(InfoViewModel.class);

        mInfoViewModel.getMovieDetails().observe(this, new Observer<MovieDetails>() {
            @Override
            public void onChanged(@Nullable MovieDetails movieDetails) {
                if (movieDetails != null) {
                    // Display cast of the movie
                    loadCast(movieDetails);
                }
            }
        });
    }

    /**
     * Gets movie data from the MainActivity.
     */
    private Movie getMovieData() {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_MOVIE)) {
                Bundle b = intent.getBundleExtra(EXTRA_MOVIE);
                mMovie = b.getParcelable(EXTRA_MOVIE);
            }
        }
        return mMovie;
    }

    /**
     * Display the cast of the movie
     */
    private void loadCast(MovieDetails movieDetails) {
        Credits credits = movieDetails.getCredits();
        mCastList = credits.getCast();
        credits.setCast(mCastList);
        mCastAdapter.addAll(mCastList);
    }


    /**
     * Make the offline message visible and hide the cast View when offline
     *
     * @param isOnline True when connected to the network
     */
    private void showOfflineMessage(boolean isOnline) {
        if (isOnline) {
            mCastBinding.tvOffline.setVisibility(View.INVISIBLE);
            mCastBinding.rvCast.setVisibility(View.VISIBLE);
        } else {
            mCastBinding.rvCast.setVisibility(View.INVISIBLE);
            mCastBinding.tvOffline.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check if there is the network connectivity
     *
     * @return true if connected to the network
     */
    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
