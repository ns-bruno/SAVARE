package com.savare.beans.json;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Faturamento on 11/09/2015.
 */
public class CustomJsonStringResquest extends StringRequest {

    public CustomJsonStringResquest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }
}
