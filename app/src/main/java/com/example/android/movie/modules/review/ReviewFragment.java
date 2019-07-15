package com.example.android.movie.modules.review;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.movie.R;
import com.example.android.movie.databinding.FragmentReviewBinding;
import com.example.android.movie.model.Movie;
import com.example.android.movie.model.Review;
import com.example.android.movie.model.ReviewResponse;
import com.example.android.movie.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;

public class ReviewFragment extends Fragment implements ReviewAdapter.ReviewAdapterOnClickHandler {

    /** Tag for a log message */
    private static final String TAG = ReviewFragment.class.getSimpleName();

    /** Member variable for the list of reviews */
    private List<Review> mReviews;

    /** This field is used for data binding */
    private FragmentReviewBinding mReviewBinding;

    /** Member variable for ReviewAdapter */
    private ReviewAdapter mReviewAdapter;

    /** Member variable for the Movie object */
    private Movie mMovie;

    /** ViewModel for ReviewFragment */
    private ReviewViewModel mReviewViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public ReviewFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_MOVIE)) {
                Bundle b = intent.getBundleExtra(EXTRA_MOVIE);
                mMovie = b.getParcelable(EXTRA_MOVIE);
            }
        }

        setupViewModel(this.getActivity());
    }

    /**
     * Every time the user data is updated, the onChanged callback will be invoked and update the UI
     */
    private void setupViewModel(Context context) {
        ReviewViewModelFactory factory = InjectorUtils.provideReviewViewModelFactory(context, mMovie.getId());
        mReviewViewModel = ViewModelProviders.of(this, factory).get(ReviewViewModel.class);

        mReviewViewModel.getReviewResponse().observe(this, new Observer<ReviewResponse>() {
            @Override
            public void onChanged(@Nullable ReviewResponse reviewResponse) {
                if (reviewResponse != null) {
                    mReviews = reviewResponse.getReviewResults();
                    reviewResponse.setReviewResults(mReviews);
                    if (!mReviews.isEmpty()) {
                        mReviewAdapter.addAll(mReviews);
                    } else {
                        showNoReviewsMessage();
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mReviewBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_review, container, false);
        View rootView = mReviewBinding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mReviewBinding.rvReview.setLayoutManager(layoutManager);
        mReviewBinding.rvReview.setHasFixedSize(true);

        mReviews = new ArrayList<>();

        mReviewAdapter = new ReviewAdapter(mReviews, this);
        mReviewBinding.rvReview.setAdapter(mReviewAdapter);

        showOfflineMessage(isOnline());

        return rootView;
    }

    /**
     * Handles RecyclerView item clicks to open a website that displays the user review.
     *
     * @param url The URL that displays the user review
     */
    @Override
    public void onItemClick(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * This method will make the message that says no reviews found visible and
     * hide the View for the review data
     */
    private void showNoReviewsMessage() {
        mReviewBinding.rvReview.setVisibility(View.INVISIBLE);
        mReviewBinding.tvNoReviews.setVisibility(View.VISIBLE);
    }

    /**
     * Make the offline message visible and hide the review View when offline
     *
     * @param isOnline True when connected to the network
     */
    private void showOfflineMessage(boolean isOnline) {
        if (isOnline) {
            mReviewBinding.tvOffline.setVisibility(View.INVISIBLE);
            mReviewBinding.rvReview.setVisibility(View.VISIBLE);
        } else {
            mReviewBinding.rvReview.setVisibility(View.INVISIBLE);
            mReviewBinding.tvOffline.setVisibility(View.VISIBLE);
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
