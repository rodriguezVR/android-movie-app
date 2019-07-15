package com.example.android.movie.modules.main;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.movie.AppExecutors;
import com.example.android.movie.R;
import com.example.android.movie.data.MovieDatabase;
import com.example.android.movie.data.MovieEntry;
import com.example.android.movie.databinding.FavListItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movie.utils.Constant.DELETE;
import static com.example.android.movie.utils.Constant.DELETE_GROUP_ID;
import static com.example.android.movie.utils.Constant.DELETE_ORDER;
import static com.example.android.movie.utils.Constant.IMAGE_BASE_URL;
import static com.example.android.movie.utils.Constant.IMAGE_FILE_SIZE;

/**
 * Exposes a list of favorite movies from a list of {@link MovieEntry} to a {@link RecyclerView}
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    /** Member variable for the list of MovieEntries that holds movie data */
    private List<MovieEntry> mMovieEntries;

    /** Context we use to utility methods, app resources and layout inflaters */
    private Context mContext;

    /** An on-click handler that we've defined to make it easy for a Activity to interface with
     * our RecyclerView
     */
    private final FavoriteAdapterOnClickHandler mOnClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface FavoriteAdapterOnClickHandler {
        void onFavItemClick(MovieEntry movieEntry);
    }

    /**
     * Constructor for the FavoriteAdapter
     */
    public FavoriteAdapter(Context context, FavoriteAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOnClickHandler = onClickHandler;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView
     *
     * @return A new FavoriteViewHolder that holds the FavListItemBinding
     */
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        FavListItemBinding favItemBinding = DataBindingUtil
                .inflate(layoutInflater, R.layout.fav_list_item, parent, false);
        return new FavoriteViewHolder(favItemBinding);
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     *
     * @param holder  The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        MovieEntry movieEntry = mMovieEntries.get(position);
        holder.bind(movieEntry);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mMovieEntries == null) return 0;
        return mMovieEntries.size();
    }

    /**
     * When data changes, updates the list of movieEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMovies(List<MovieEntry> movieEntries) {
        mMovieEntries = movieEntries;
        notifyDataSetChanged();
    }

    /**
     * Returns the list of MovieEntries
     */
    public List<MovieEntry> getMovies() {
        return mMovieEntries;
    }


    /**
     * Set Movies which match search criteria
     */

    public void filter(String search) {
        List<MovieEntry> filteredList = new ArrayList<>();

        for (MovieEntry movie: mMovieEntries){
            if (movie.getTitle().toLowerCase().contains(search.toLowerCase()) || movie.getGenre().toLowerCase().contains(search.toLowerCase()) || movie.getOriginalTitle().toLowerCase().contains(search.toLowerCase())){
                filteredList.add(movie);
            }
        }

        setMovies(filteredList);

    }
    /**
     * Cache of the children views for favorite movie list item.
     */
    public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        /** This field is used for data binding */
        FavListItemBinding mFavItemBinding;

        /**
         * Constructor for FavoriteViewHolder
         *
         * @param favItemBinding Used to access the layout's variables and views
         */
        public FavoriteViewHolder(FavListItemBinding favItemBinding) {
            super(favItemBinding.getRoot());

            mFavItemBinding = favItemBinding;
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(MovieEntry movieEntry) {
            String thumbnail = IMAGE_BASE_URL + IMAGE_FILE_SIZE + movieEntry.getPosterPath();

            Picasso.with(itemView.getContext())
                    .load(thumbnail)
                    .into(mFavItemBinding.ivThumbnail);

            mFavItemBinding.tvTitle.setText(movieEntry.getTitle());
        }

        /**
         * Called whenever a user clicks on an movie in the list
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MovieEntry movieEntry = mMovieEntries.get(adapterPosition);
            mOnClickHandler.onFavItemClick(movieEntry);
        }

        /**
         * When the user performs a long-click on a favorite movie item, a floating menu appears.
         *
         * Reference @see "https://stackoverflow.com/questions/36958800/recyclerview-getmenuinfo-always-null"
         * "https://stackoverflow.com/questions/37601346/create-options-menu-for-recyclerview-item"
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int adapterPosition = getAdapterPosition();
            MenuItem item = menu.add(DELETE_GROUP_ID, adapterPosition, DELETE_ORDER, v.getContext().getString(R.string.action_delete));
            item.setOnMenuItemClickListener(this);
        }

        /**
         * This gets called when a menu item is clicked.
         */
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getTitle().toString()) {
                case DELETE:
                    int adapterPosition = item.getItemId();
                    MovieEntry movieEntry = mMovieEntries.get(adapterPosition);
                    delete(movieEntry);
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Delete a favorite movie when the user clicks "Delete" menu option.
         */
        private void delete(final MovieEntry movieEntry) {
            final MovieDatabase db = MovieDatabase.getInstance(mContext);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    db.movieDao().deleteMovie(movieEntry);
                }
            });
        }
    }
}
