package com.savare.beans.json;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bruno Nogueira Silva on 11/09/2015.
 */
public class CustomJsonStringResquest extends StringRequest {

    private Map<String, String> parametros;
    private Response.Listener<String> listener;
    private Context context;

    public CustomJsonStringResquest(int method, String url, Map<String, String> params, Response.Listener<String> listener, Response.ErrorListener errorListener, Context context) {
        super(method, url, listener, errorListener);
        this.parametros = params;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // Cria a vareavel para pegar o cabecalho
        HashMap<String, String> cabecalho = new HashMap<String, String>();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Adiciona os dados do cabecalho
        cabecalho.put("METODO", "STRING");
        cabecalho.put("CHAVE_USUA", funcoes.getValorXml("ChaveEmpresa"));
        cabecalho.put("USUARIO_USUA", funcoes.getValorXml("Usuario"));
        cabecalho.put("ID_USUA", funcoes.getValorXml("CodigoUsuario"));
        cabecalho.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));

        return cabecalho; //!= null ? cabecalho : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parametros;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String resposta = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(resposta, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Priority getPriority() {
        return(Priority.IMMEDIATE);
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
}
