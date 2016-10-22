package com.savare.banco.storedProcedure;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.funcoes.VersionUtils;

/**
 * Created by Bruno Nogueira Silva on 06/09/2016.
 */
public class StoredProcedure {

    protected ConexaoBancoDeDados conexaoBanco;
    protected SQLiteDatabase bancoDados;
    protected Context context;

    public StoredProcedure(Context context) {
        super();
        this.context = context;
        try {
            //int vAtual =  VersionUtils.getVersionCode(context);
            conexaoBanco = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}
