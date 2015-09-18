package com.savare.funcoes.rotinas;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.savare.beans.json.CustomJsonArrayRequest;
import com.savare.beans.json.CustomJsonObjectRequest;
import com.savare.beans.json.CustomJsonStringResquest;
import com.savare.configuracao.ServicosWeb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Bruno Nogueira Silva on 04/09/2015.
 */
public class EnviarDadosJsonRotinas {

    public static final String TAG = EnviarDadosJsonRotinas.class.getSimpleName();
    public static final int TIPO_ARRAY = 0;
    public static final int TIPO_OBJECT = 1;
    public static final int TIPO_STRING = 2;
    private Context context;
    private HashMap<String, String> dados;
    private int tipoJson = -1;
    private RequestQueue filaPedido;
    private boolean sucesso = false;

    public EnviarDadosJsonRotinas(Context context, int tipoJson, HashMap<String, String> dados) {
        this.context = context;
        this.tipoJson = tipoJson;
        this.dados = dados;
        this.filaPedido = Volley.newRequestQueue(context);
    }

    public EnviarDadosJsonRotinas(Context context, int tipoJson) {
        this.context = context;
        this.tipoJson = tipoJson;
        this.filaPedido = Volley.newRequestQueue(context);
    }

    public boolean enviarDados(HashMap<String, String> dados){

        // Checa se foi passado algum dados por parametro
        if (dados != null && dados.size() > 0) {

            // Checa o tipo de JSon a ser enviado
            if (tipoJson == TIPO_ARRAY) {

                CustomJsonArrayRequest pedidoArray = new CustomJsonArrayRequest(
                        Request.Method.POST,
                        ServicosWeb.URL_ENVIAR_PEDIDO,
                        dados,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                sucesso = true;
                                Log.i("SAVARE", "Sucesso: " + response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("SAVARE", "Erro: " + error.toString());
                            }
                        },
                        context
                );

                pedidoArray.setTag(TAG);
                pedidoArray.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                filaPedido.add(pedidoArray);

            } else if (tipoJson == TIPO_OBJECT){

                CustomJsonObjectRequest pedidoObject = new CustomJsonObjectRequest(
                        Request.Method.POST,
                        ServicosWeb.URL_ENVIAR_PEDIDO,
                        dados,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                sucesso = true;
                                Log.i("SAVARE", "Sucesso: " + response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("SAVARE", "Erro: " + error.toString());
                            }
                        },
                        context
                );

                pedidoObject.setTag(TAG);
                pedidoObject.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                filaPedido.add(pedidoObject);

            } else if (tipoJson == TIPO_STRING){

                CustomJsonStringResquest pedidoArray = new CustomJsonStringResquest(
                        Request.Method.POST,
                        ServicosWeb.URL_ENVIAR_PEDIDO,
                        dados,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                sucesso = true;
                                Log.i("SAVARE", "Sucesso: " + response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("SAVARE", "Erro: " + error.toString());
                            }
                        },
                        context
                );

                pedidoArray.setTag(TAG);
                pedidoArray.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                filaPedido.add(pedidoArray);
            }
            sucesso = false;
        }
        return sucesso;
    }

    public void pararEnvioDados(){
        if (filaPedido != null) {
            filaPedido.cancelAll(TAG);
        }
    }
}
