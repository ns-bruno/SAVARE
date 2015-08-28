package com.savare.sincronizacao;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.savare.configuracao.AutenticadorConta;

/**
 * Created by Bruno Nogueira Silva on 28/08/2015.
 */
public class AutenticacaoServico extends Service {

    private AutenticadorConta autenticador;

    @Override
    public void onCreate() {
        autenticador = new AutenticadorConta(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return autenticador.getIBinder();
    }
}
