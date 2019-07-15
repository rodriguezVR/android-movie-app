package com.example.android.movie.modules.info;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.movie.R;
import com.example.android.movie.databinding.FragmentInfoBinding;
import com.example.android.movie.model.Cast;
import com.example.android.movie.model.Credits;
import com.example.android.movie.model.Crew;
import com.example.android.movie.model.Movie;
import com.example.android.movie.model.MovieDetails;
import com.example.android.movie.utils.FormatUtils;
import com.example.android.movie.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;

/**
 * The InformationFragment displays information for the selected movie.
 */
public class InformationFragment extends Fragment {

    /** This field is used for data binding */
    private FragmentInfoBinding mInfoBinding;

    /** Define a new interface OnInfoSelectedListener that triggers a Callback in the host activity.
     *  The callback is a method named onInformationSelected(MovieDetails movieDetails) that contains
     *  information about the MovieDetails */
    OnInfoSelectedListener mCallback;

    public interface OnInfoSelectedListener {
        void onInformationSelected(MovieDetails movieDetails);
    }

    /**
     * Define a new interface OnViewAllSelectedListener that triggers a Callback in the host activity.
     * The callback is a method named onViewAllSelected() that is triggered when the user clicks
     * "VIEW ALL" TextView
     */
    OnViewAllSelectedListener mViewAllCallback;

    /** OnViewAllSelectedListener interface, calls a method in the host activity named onViewAllSelected */
    public interface OnViewAllSelectedListener {
        void onViewAllSelected();
    }

    /** Tag for logging */
    public static final String TAG = InformationFragment.class.getSimpleName();

    /** Member variable for the Movie object */
    private Movie mMovie;

    /** ViewModel for InformationFragment */
    private InfoViewModel mInfoViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public InformationFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMovie = getMovieData();

        setupViewModel(this.getActivity(), mMovie.getId());
        loadDetails();
    }

    /**
     * Every time the user data is updated, the onChanged callback will be invoked and update the UI
     */
    private void setupViewModel(Context context, int movieId) {
        InfoViewModelFactory factory = InjectorUtils.provideInfoViewModelFactory(context, movieId);
        mInfoViewModel = ViewModelProviders.of(this, factory).get(InfoViewModel.class);

        mInfoViewModel.getMovieDetails().observe(this, new Observer<MovieDetails>() {
            @Override
            public void onChanged(@Nullable MovieDetails movieDetails) {
                if (movieDetails != null) {
                    // Trigger the callback onInformationSelected
                    mCallback.onInformationSelected(movieDetails);

                    // Display vote count, budget, revenue, status of the movie
                    loadMovieDetailInfo(movieDetails);

                    // Display cast and crew of the movie
                    loadCastCrew(movieDetails);
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInfoBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_info, container, false);
        View rootView = mInfoBinding.getRoot();

        mInfoBinding.tvViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger the callback onViewAllSelected
                mViewAllCallback.onViewAllSelected();
            }
        });

        return rootView;
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
     * Display cast and crew of the movie
     */
    private void loadCastCrew(MovieDetails movieDetails) {
        Credits credits = movieDetails.getCredits();
        List<Cast> castList = credits.getCast();
        List<String> castStrList = new ArrayList<>();
        for (int i = 0; i < castList.size(); i++) {
            Cast cast = castList.get(i);
            String castName = cast.getName();
            castStrList.add(castName);
        }

        Activity activity = getActivity();
        if (activity != null) {
            String castStr = TextUtils.join(getString(R.string.delimiter_comma), castStrList);
            mInfoBinding.tvCast.setText(castStr);

            List<Crew> crewList = credits.getCrew();
            for (int i = 0; i < crewList.size(); i++) {
                Crew crew = crewList.get(i);
                if (crew.getJob().equals(getString(R.string.director))) {
                    mInfoBinding.tvDirector.setText(crew.getName());
                    break;
                }
            }
        }
    }

    /**
     * Display vote count, budget, revenue, status of the movie
     */
    private void loadMovieDetailInfo(MovieDetails movieDetails) {
        int voteCount = movieDetails.getVoteCount();
        long budget = movieDetails.getBudget();
        long revenue = movieDetails.getRevenue();
        String status = movieDetails.getStatus();

        mInfoBinding.tvVoteCount.setText(FormatUtils.formatNumber(voteCount));
        mInfoBinding.tvBudget.setText(FormatUtils.formatCurrency(budget));
        mInfoBinding.tvRevenue.setText(FormatUtils.formatCurrency(revenue));
        mInfoBinding.tvStatus.setText(status);
    }

    /**
     * Get the detail information from the Movie object, then set them to the TextView to display the
     * overview, vote average, release date of the movie.
     */
    private void loadDetails() {
        mInfoBinding.tvOverview.setText(mMovie.getOverview());
        mInfoBinding.tvVoteAverage.setText(String.valueOf(mMovie.getVoteAverage()));
        mInfoBinding.tvOriginalTitle.setText(mMovie.getOriginalTitle());
        mInfoBinding.tvReleaseDate.setText(FormatUtils.formatDate(mMovie.getReleaseDate()));
    }

    /**
     * Override onAttach to make sure that the container activity has implemented the callback
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnInfoSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnInfoSelectedListener");
        }

        try {
            mViewAllCallback = (OnViewAllSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnViewAllSelectedListener");
        }
    }
}
