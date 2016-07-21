package com.savare.funcoes.rotinas.async;

import android.content.Context;
import android.os.AsyncTask;

import com.savare.funcoes.FuncoesPersonalizadas;

/**
 * Created by Faturamento on 29/06/2016.
 */
public class ReceberDadosWebserviceAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private Context context;

    public ReceberDadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("RecebendoDados", "S");

        if (funcoes.existeConexaoInternet()){

        }

        return null;
    }
}
