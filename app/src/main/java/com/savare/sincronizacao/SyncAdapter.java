package com.savare.sincronizacao;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.sincronizacao.EnviarOrcamentoSyncAdapterRotinas;

/**
 * Created by Bruno Nogueira Silva on 17/09/2015.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final ContentResolver mContentResolver;
    private static final String TAG = "SAVARE";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.i(TAG, "### onPerformSync - SyncAdapter");

        try {
            EnviarOrcamentoSyncAdapterRotinas enviarOrcamento = new EnviarOrcamentoSyncAdapterRotinas(getContext());
            enviarOrcamento.execute();

        }catch (Exception e){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

            // Armazena as informacoes para para serem exibidas e enviadas
            ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "SyncAdapter");
            contentValues.put("mensagem", e.getMessage());
            contentValues.put("dados", getContext().toString());
            // Pega os dados do usuario
            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            funcoes.menssagem(contentValues);
        }
    }
}
