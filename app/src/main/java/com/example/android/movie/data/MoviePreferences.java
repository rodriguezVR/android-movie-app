package com.example.android.movie.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.android.movie.R;

public class MoviePreferences {

    /**
     * Returns the sort criteria currently set in Preferences. The default sort criteria this method
     * will return is "popular".
     *
     * @param context Context used to get the SharedPreferences
     * @return Sort Criteria The current user has set in SharedPreferences. Will default to
     * "popular" if SharedPreferences have not been implemented yet.
     */
    public static String getPreferredSortCriteria(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortBy = context.getString(R.string.pref_sort_by_key);
        String defaultSortBy = context.getString(R.string.pref_sort_by_default);
        return prefs.getString(keyForSortBy, defaultSortBy);
    }

    public static void setPreferredSortCriteria(Context context, String sortBy) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String keyForSortBy = context.getString(R.string.pref_sort_by_key);

        editor.putString(keyForSortBy,sortBy);
        editor.apply();
    }

}
