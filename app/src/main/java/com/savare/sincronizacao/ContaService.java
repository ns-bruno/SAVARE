package com.savare.sincronizacao;

import android.accounts.Account;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;

/**
 * Created by Bruno Nogueira Silva on 17/09/2015.
 */
public class ContaService extends Service {

    private Autenticador autenticador;

    @Override
    public void onCreate() {
        autenticador = new Autenticador(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return autenticador.getIBinder();
    }


    public static Account GetAccount(Context context) {
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        return new Account(funcoes.getValorXml("Usuario"), context.getResources().getString(R.string.sync_account_type));
    }

}
