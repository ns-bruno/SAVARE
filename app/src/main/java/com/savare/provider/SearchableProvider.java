package com.savare.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Bruno Nogueira Silva on 12/28/15.
 */
public class SearchableProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.savare.provider.SearchableProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public SearchableProvider(){
        setupSuggestions( AUTHORITY, MODE );
    }
}
