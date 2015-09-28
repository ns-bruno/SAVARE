package com.savare.beans.json;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bruno Nogueira Silva on 10/09/2015.
 */
public class CustomJsonObjectRequest extends Request<JSONObject> {

    public static String TAG = "SAVARE";
    private Response.Listener<JSONObject> listener;
    private Map<String, String> params;
    private Context context;


    public CustomJsonObjectRequest(int method,
                                  String url,
                                  Map<String, String> params,
                                  Response.Listener<JSONObject> reponseListener,
                                  Response.ErrorListener errorListener,
                                  Context context) {

        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
        this.context = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // Cria a vareavel para pegar o cabecalho
        HashMap<String, String> cabecalho = new HashMap<String, String>();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Adiciona os dados do cabecalho
        cabecalho.put("METODO", "OBJECT");
        cabecalho.put("CHAVE_USUA", funcoes.getValorXml("ChaveEmpresa"));
        cabecalho.put("USUARIO_USUA", funcoes.getValorXml("Usuario"));
        cabecalho.put("ID_USUA", funcoes.getValorXml("CodigoUsuario"));
        cabecalho.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
        return cabecalho != null ? cabecalho : super.getHeaders();
    }

    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
        return params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            Log.i(TAG, "CustomJsonObjectResquest - Resposta: \n" + response.toString());

            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Erro. CustomJsonObjectRequest: \n");
            return Response.error(new ParseError(e));

        } catch (JSONException je) {
            Log.e(TAG, "Erro. CustomJsonObjectRequest: \n");
            return Response.error(new ParseError(je));
        }
    }

    public Priority getPriority(){
        return(Priority.IMMEDIATE);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        listener.onResponse(response);
    }
}
