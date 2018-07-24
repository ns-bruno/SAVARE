package com.savare.banco.storedProcedure;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.funcoes.VersionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Bruno Nogueira Silva on 06/09/2016.
 */
public class StoredProcedure {

    protected ConexaoBancoDeDados conexaoBanco;
    protected SQLiteDatabase bancoDados;
    protected Context context;
    protected ProgressBar progressBarStatus = null;
    protected TextView textStatus = null;

    public StoredProcedure(Context context, ProgressBar progressBarStatus, TextView textStatus) {
        super();
        this.context = context;
        this.progressBarStatus = progressBarStatus;
        this.textStatus = textStatus;
        try {
            //int vAtual =  VersionUtils.getVersionCode(context);
            conexaoBanco = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
        } catch (PackageManager.NameNotFoundException e) {
            new MaterialDialog.Builder(context)
                    .title("StoredProcedure")
                    .content(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
