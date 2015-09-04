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

import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by Bruno Nogueira Silva on 04/09/2015.
 */
public class EnviarDadosJsonRotinas {

    public static final String TAG = EnviarDadosJsonRotinas.class.getSimpleName();
    public static final int TIPO_ARRAY = 0;
    public static final int TIPO_OBJECT = 1;
    private Context context;
    private HashMap<String, String> dados;
    private int tipoJson = -1;
    private String url = "http://www.parceiraodistribuidora.com.br/android-volley.php";
    private RequestQueue filaPedido;

    public EnviarDadosJsonRotinas(Context context, int tipoJson, HashMap<String, String> dados) {
        this.context = context;
        this.tipoJson = tipoJson;
        this.dados = dados;
        this.filaPedido = Volley.newRequestQueue(context);
    }

    public void enviarDados(){
        // Checa se foi passado algum dados por parametro
        if (dados != null & dados.size() > 0) {

            // Checa o tipo de JSon a ser enviado
            if (tipoJson == TIPO_ARRAY) {

                CustomJsonArrayRequest pedidoArray = new CustomJsonArrayRequest(
                        Request.Method.POST,
                        url,
                        dados,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
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
                pedidoArray.setRetryPolicy(new DefaultRetryPolicy(5000,
                                               DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                               DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                filaPedido.add(pedidoArray);
            }
        }
    }

    public void pararEnvioDados(){
        if (filaPedido != null) {
            filaPedido.cancelAll(TAG);
        }
    }
}
