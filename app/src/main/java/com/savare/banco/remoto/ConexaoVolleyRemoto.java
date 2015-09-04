package com.savare.banco.remoto;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Bruno Nogueira Silva on 27/08/2015.
 */
public class ConexaoVolleyRemoto {

    private Context context;
    private RequestQueue filaPedido;
    private static ConexaoVolleyRemoto instance;

    public static final String TAG = ConexaoVolleyRemoto.class.getSimpleName();

    public ConexaoVolleyRemoto(Context context) {
        this.context = context;
        this.filaPedido = getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if( filaPedido == null ){
            filaPedido = Volley.newRequestQueue(context);
        }
        return(filaPedido);
    }

    public static ConexaoVolleyRemoto getInstance( Context c ){
        if( instance == null ){
            instance = new ConexaoVolleyRemoto( c.getApplicationContext() );
        }
        return( instance );
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (filaPedido != null) {
            filaPedido.cancelAll(tag);
        }
    }
}
