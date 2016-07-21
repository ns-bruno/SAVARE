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
public class SavareAutenticadorService extends Service {

    private SavareAutenticador savareAutenticador;

    @Override
    public void onCreate() {
        savareAutenticador = new SavareAutenticador(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return savareAutenticador.getIBinder();
    }


    public static Account GetAccount(Context context) {
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        return new Account(funcoes.getValorXml("Usuario"), context.getResources().getString(R.string.sync_account_type));
    }

}
