package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.VersionUtils;

public class UpgradeBancoDadosAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private String[] tabelasUpgrade = null;
    private Context context;
    private MaterialDialog.Builder mProgressDialog;
    private MaterialDialog dialog;
    private OnTaskCompleted listenerTaskCompleted;
    private StringBuilder sql = new StringBuilder();
    Cursor dadosTable = null;
    // Cria uma notificacao para ser manipulado
    NotificationManager notificationManager;
    NotificationCompat.BigTextStyle bigTextStyle;
    NotificationCompat.Builder mBuilder;

    public UpgradeBancoDadosAsyncRotinas(Context context, String[] tabelasUpgrade, OnTaskCompleted listenerTaskCompleted) {
        this.tabelasUpgrade = tabelasUpgrade;
        this.context = context;
        this.listenerTaskCompleted = listenerTaskCompleted;
    }

    public UpgradeBancoDadosAsyncRotinas(Context context, String[] tabelasUpgrade) {
        this.tabelasUpgrade = tabelasUpgrade;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new MaterialDialog.Builder(context)
            .title(context.getResources().getString(R.string.upgrade_banco_dados))
            .content(context.getResources().getString(R.string.aguarde_atualizando_banco_dados))
            .cancelable(false)
            .progress(true, 0)
            .progressIndeterminateStyle(true);

        dialog = mProgressDialog.build();
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                dialog.show();
            }
        });

        String name = "FileUpgrade";

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel mChannel = new NotificationChannel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL, name, NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Create a BigTextStyle object.
        bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_atualizando_banco_dados));
        //bigTextStyle.setBigContentTitle("Happy Christmas Detail Info.");

        mBuilder = new NotificationCompat.Builder(context, ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentTitle(context.getResources().getString(R.string.upgrade_banco_dados))
                //.setContentText(mActivity.getResources().getString(R.string.app_name))
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setSound(null, 0)
                .setVibrate(new long[0])
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
            SQLiteDatabase bancoDados = conexaoBancoDeDados.abrirBanco();

            if(bancoDados != null){

                bigTextStyle.bigText(context.getResources().getString(R.string.verificando_tem_backup_tabelas))
                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                // SQL para verificar se tem algum tabela temporaria criada, se conter eh pra restaurar ela
                sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME NOT IN('sqlite_sequence', 'android_metadata')) AND (NAME LIKE '%TEMP%')");

                dadosTable = bancoDados.rawQuery(sql.toString(), null);

                if ( (dadosTable != null) && (dadosTable.getCount() > 0)){
                    // Executa a restauracao do banco de dados
                    restaurarBackup(conexaoBancoDeDados, bancoDados);
                    return null;
                }
                dadosTable = null;

                bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados))
                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                sql.setLength(0);
                sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME NOT IN('sqlite_sequence', 'android_metadata', 'ULTIMA_ATUALIZACAO_DISPOSITIVO')) AND (NAME NOT LIKE '%TEMP%')");

                if (tabelasUpgrade != null && tabelasUpgrade.length > 0){
                    sql.append(" AND (NAME IN(");

                    for (int i =0; i < tabelasUpgrade.length; i++) {
                        if (i < tabelasUpgrade.length - 1) {
                            sql.append("'").append(tabelasUpgrade[i]).append("', ");
                        } else {
                            sql.append("'").append(tabelasUpgrade[i]).append("');");
                        }
                    }
                } else {
                    sql.append(";");
                }

                dadosTable = bancoDados.rawQuery(sql.toString(), null);

                if ((dadosTable != null) && (dadosTable.getCount() > 0)){
                    //Passa por todos os dados
                    while (dadosTable.moveToNext()){
                        final String table = dadosTable.getString(dadosTable.getColumnIndex("name"));
                        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS TEMP_" + table + " AS SELECT * FROM " + table + ";";

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressDialog.content(context.getResources().getString(R.string.salvando_dados) + " - " + table);
                            }
                        });

                        bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados) + " - " + table)
                                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, true);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                        bancoDados.execSQL(sqlCreateTable);
                        bancoDados.execSQL("DROP TABLE IF EXISTS " + table);
                    }
                } else {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(context)
                                    .title("UpgradeBancoDadosAsyncRotinas")
                                    .content(context.getResources().getString(R.string.nao_conseguimor_pegar_table))
                                    .positiveText(R.string.button_ok)
                                    .show();
                        }
                    });
                }
                dadosTable = null;

                restaurarBackup(conexaoBancoDeDados, bancoDados);

            } else {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("UpgradeBancoDadosAsyncRotinas")
                                .content(context.getResources().getString(R.string.nao_conseguimor_abrir_banco_dados))
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    mProgressDialog.content(context.getResources().getString(R.string.finalizando));
                }
            });

            bigTextStyle.bigText(context.getResources().getString(R.string.finalizando))
                    .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, true);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        } catch (final Exception e){
            // Atualiza a notificacao
            bigTextStyle.bigText(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("UpgradeBancoDadosAsyncRotinas")
                            .content(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage())
                            .positiveText(R.string.button_ok)
                            .show();
                    dialog.dismiss();
                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Checa se a interface de retorno do asynctask eh diferente de nula
        if (listenerTaskCompleted != null) {
            listenerTaskCompleted.onTaskCompleted();
        }

        bigTextStyle.bigText(context.getResources().getString(R.string.terminamos_upgrade_banco_dados))
                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, false);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                dialog.dismiss();
            }
        });
    }

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }

    private void restaurarBackup(ConexaoBancoDeDados conexaoBancoDeDados, SQLiteDatabase bancoDados){
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog.content(context.getResources().getString(R.string.criando_tabelas));
            }
        });

        bigTextStyle.bigText(context.getResources().getString(R.string.criando_tabelas))
                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Cria todas as tabelas executando o create.sql
        conexaoBancoDeDados.onCreate(bancoDados);

        sql.setLength(0);
        sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME LIKE '%TEMP%');");

        dadosTable = bancoDados.rawQuery(sql.toString(), null);

        if ((dadosTable != null) && (dadosTable.getCount() > 0)){
            //Passa por todos os dados
            while (dadosTable.moveToNext()){
                String table = dadosTable.getString(dadosTable.getColumnIndex("name")).replace("TEMP_", "");
                final String tableTemp = dadosTable.getString(dadosTable.getColumnIndex("name"));

                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.content(context.getResources().getString(R.string.salvando_dados) + " - " + tableTemp);
                    }
                });

                bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados) + " - " + tableTemp)
                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                StringBuilder sqlInsert = new StringBuilder();
                sqlInsert.append("INSERT INTO ").append(table).append(" (");

                StringBuilder sqlSelectTemp = new StringBuilder();
                sqlSelectTemp.append("SELECT ");

                Cursor columns = bancoDados.rawQuery("PRAGMA TABLE_INFO(" + table + ");", null);
                Cursor columnsTemp = bancoDados.rawQuery("PRAGMA TABLE_INFO(" + tableTemp + ");", null);

                if ((columns != null) && (columns.getCount() > 0) && (columnsTemp != null) && (columnsTemp.getCount() > 0)){
                    // Passa por todas as columans
                    for (int i = 0; i < columnsTemp.getCount(); i++) {
                        columns.moveToNext();
                        columnsTemp.moveToNext();

                        if (i < (columns.getCount() - 1) ) {
                            sqlInsert.append(columns.getString(columns.getColumnIndex("name"))).append(", ");
                        } else {
                            sqlInsert.append(columns.getString(columns.getColumnIndex("name"))).append(") ");
                        }

                        if (i < (columnsTemp.getCount() - 1) ) {
                            sqlSelectTemp.append(columnsTemp.getString(columnsTemp.getColumnIndex("name"))).append(", ");
                        } else {
                            sqlSelectTemp.append(columnsTemp.getString(columnsTemp.getColumnIndex("name"))).append(" FROM ").append(tableTemp).append(";");
                        }
                    }
                    bancoDados.execSQL(sqlInsert.append(sqlSelectTemp.toString()).toString());
                    bancoDados.execSQL("DROP TABLE IF EXISTS " + tableTemp);
                }
            }
        } else {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("UpgradeBancoDadosAsyncRotinas")
                            .content(context.getResources().getString(R.string.nao_conseguimor_pegar_table))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        }
    }
}
