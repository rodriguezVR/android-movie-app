package com.example.android.movie.modules.trailer;

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
import com.example.android.movie.databinding.FragmentTrailerBinding;
import com.example.android.movie.model.Movie;
import com.example.android.movie.model.Video;
import com.example.android.movie.model.VideoResponse;
import com.example.android.movie.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movie.utils.Constant.EXTRA_MOVIE;

public class TrailerFragment extends Fragment implements TrailerAdapter.TrailerAdapterOnClickHandler {

    /** Define a new interface OnTrailerSelectedListener that triggers a Callback in the host activity.
     *  The callback is a method named onTrailerSelected(Video video) that contains the first trailer
     */
    TrailerFragment.OnTrailerSelectedListener mCallback;

    public interface OnTrailerSelectedListener {
        void onTrailerSelected(Video video);
    }

    /** Tag for a log message */
    private static final String TAG = TrailerFragment.class.getSimpleName();

    /** Member variable for the list of trailers */
    private List<Video> mVideos;

    /** Member variable for TrailerAdapter */
    private TrailerAdapter mTrailerAdapter;

    private Movie mMovie;

    /** This field is used for data binding */
    private FragmentTrailerBinding mTrailerBinding;

    /** ViewModel for TrailerFragment */
    private TrailerViewModel mTrailerViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public TrailerFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMovie = getMovieData();

        setupViewModel(this.getActivity(), mMovie.getId());
    }

    /**
     * Every time the user data is updated, the onChanged callback will be invoked and update the UI
     */
    private void setupViewModel(Context context, int movieId) {
        TrailerViewModelFactory factory = InjectorUtils.provideTrailerViewModelFactory(context, movieId);
        mTrailerViewModel = ViewModelProviders.of(this, factory).get(TrailerViewModel.class);

        mTrailerViewModel.getVideoResponse().observe(this, new Observer<VideoResponse>() {
            @Override
            public void onChanged(@Nullable VideoResponse videoResponse) {
                if (videoResponse != null) {
                    mVideos = videoResponse.getVideoResults();
                    videoResponse.setVideoResults(mVideos);

                    if (!mVideos.isEmpty()) {
                        mCallback.onTrailerSelected(mVideos.get(0));

                        mTrailerAdapter.addAll(mVideos);
                    } else {
                        showNoTrailersMessage();
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTrailerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_trailer, container, false);
        View rootView = mTrailerBinding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mTrailerBinding.rvTrailer.setLayoutManager(layoutManager);
        mTrailerBinding.rvTrailer.setHasFixedSize(true);

        mVideos = new ArrayList<>();

        mTrailerAdapter = new TrailerAdapter(mVideos, this);
        mTrailerBinding.rvTrailer.setAdapter(mTrailerAdapter);
        showOfflineMessage(isOnline());

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
     * Override onAttach to make sure that the container activity has implemented the callback
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnTrailerSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTrailerSelectedListener");
        }
    }

    /**
     * When a movie trailer selected, use an Intent to open a YouTube link
     *
     * @param videoUrl YouTube video url to display a trailer video
     */
    @Override
    public void onItemClick(String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * This method will make the message that says no trailers found visible and
     * hide the View for the trailers data
     */
    private void showNoTrailersMessage() {
        mTrailerBinding.rvTrailer.setVisibility(View.INVISIBLE);
        mTrailerBinding.tvNoTrailers.setVisibility(View.VISIBLE);
    }

    /**
     * Make the offline message visible and hide the trailer View when offline
     *
     * @param isOnline True when connected to the network
     */
    private void showOfflineMessage(boolean isOnline) {
        if (isOnline) {
            mTrailerBinding.tvOffline.setVisibility(View.INVISIBLE);
            mTrailerBinding.rvTrailer.setVisibility(View.VISIBLE);
        } else {
            mTrailerBinding.rvTrailer.setVisibility(View.INVISIBLE);
            mTrailerBinding.tvOffline.setVisibility(View.VISIBLE);
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
