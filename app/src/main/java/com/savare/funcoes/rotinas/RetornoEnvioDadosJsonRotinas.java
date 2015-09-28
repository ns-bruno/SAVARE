package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;

import com.android.volley.VolleyError;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Bruno Nogueira Silva on 19/09/2015.
 */
public class RetornoEnvioDadosJsonRotinas {

    private int tipoJson = -1;
    private Context context;
    private JSONArray responseArray;
    private JSONObject responseObject;
    private String responseString;
    private VolleyError responseError;

    public RetornoEnvioDadosJsonRotinas(Context context, int tipoJson, JSONArray responseArray, JSONObject responseObject, String responseString, VolleyError responseError) {
        this.context = context;
        this.tipoJson = tipoJson;
        this.responseArray = responseArray;
        this.responseObject = responseObject;
        this.responseString = responseString;
        this.responseError = responseError;
        execute();
    }

    private void execute() {
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            if (responseError != null) {
                // Armazena as informacoes para para serem exibidas e enviadas
                ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "RetornoEnvioDadosJsonRotinas");
                contentValues.put("mensagem", responseError.toString().replace("com.android.volley.", ""));
                // Mostra a mensagem
                funcoes.menssagem(contentValues);

            } else {
                // Checa se o tipo de envio foi por array
                if (tipoJson == EnviarDadosJsonRotinas.TIPO_ARRAY) {
                    // Checa se a resposta nao esta vazia
                    if (responseArray != null) {

                    }

                    // Checa se o tipo de envio foi por object
                } else if (tipoJson == EnviarDadosJsonRotinas.TIPO_OBJECT) {
                    // Checa se a resposta nao esta vazia
                    if (responseObject != null) {
                        // Pega todas as chaves do object
                        Iterator<String> listaChaves = responseObject.keys();

                        // Passa por todas a chaves
                        while (listaChaves.hasNext()){
                            String chave = listaChaves.next();
                            processarResposta(chave, responseObject.getString(chave));
                        }
                    }
                    // Checa se o tipo de envio foi por string
                } else if (tipoJson == EnviarDadosJsonRotinas.TIPO_STRING) {
                    // Checa se a resposta nao esta vazia
                    if (responseString != null) {

                    }
                }
            }
            }catch(Exception e){
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                // Armazena as informacoes para para serem exibidas e enviadas
                ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "RetornoEnvioDadosJsonRotinas");
                contentValues.put("mensagem", e.getMessage());
                contentValues.put("dados", context.toString());
                // Pega os dados do usuario
                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                contentValues.put("email", funcoes.getValorXml("Email"));

                funcoes.menssagem(contentValues);
            }

        } // Fim execute()

    private void processarResposta(String chave, String guid){

        switch (chave){
            case "AEAITORC.GUID" :
                ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                ContentValues dadosUp = new ContentValues();
                dadosUp.put("STATUS", "N");

                itemOrcamentoSql.update(dadosUp, "AEAITORC.GUID = " + guid);

                break;

            case "AEAORCAM.GUID" :
                OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                ContentValues dadosRetorno = new ContentValues();
                dadosRetorno.put("STATUS", "N");

                orcamentoSql.update(dadosRetorno, "AEAORCAM.GUID = " + guid);

                break;
            default:
                break;
        }
    }
}
