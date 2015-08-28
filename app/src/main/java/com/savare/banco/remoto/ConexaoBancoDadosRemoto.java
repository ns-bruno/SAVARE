package com.savare.banco.remoto;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.jdom2.Content;

/**
 * Created by Bruno Nogueira Silva on 27/08/2015.
 */
public class ConexaoBancoDadosRemoto {

    private Context context;
    private RequestQueue filaPedido;
    private static ConexaoBancoDadosRemoto instance;

    public ConexaoBancoDadosRemoto(Context context) {
        this.context = context;
        this.filaPedido = getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if( filaPedido == null ){
            filaPedido = Volley.newRequestQueue(context);
        }
        return(filaPedido);
    }

    public static ConexaoBancoDadosRemoto getInstance( Context c ){
        if( instance == null ){
            instance = new ConexaoBancoDadosRemoto( c.getApplicationContext() );
        }
        return( instance );
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
