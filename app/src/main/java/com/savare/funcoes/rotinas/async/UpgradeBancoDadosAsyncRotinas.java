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
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;

public class UpgradeBancoDadosAsyncRotinas extends AsyncTask<Void, Void, Void> {

    public static Integer APENAS_TABELA_TEMP = 0;
    private String[] tabelasUpgrade = null;
    private Context context;
    private MaterialDialog.Builder mProgressDialog;
    private MaterialDialog dialog;
    private StringBuilder sql = new StringBuilder();
    private Cursor dadosTable = null;
    private Integer checkUpgradeComplete;
    // Cria uma notificacao para ser manipulado
    NotificationManager notificationManager;
    NotificationCompat.BigTextStyle bigTextStyle;
    NotificationCompat.Builder mBuilder;

    /**
     *
     * @param context
     * @param tabelasUpgrade
     * @param checkUpgradeComplete APENAS_TABELA_TEMP = APENAS VERIFICA SE TEM ALGUMA TABELA EM TEMP
     */
    public UpgradeBancoDadosAsyncRotinas(Context context, String[] tabelasUpgrade, Integer checkUpgradeComplete) {
        this.tabelasUpgrade = tabelasUpgrade;
        this.context = context;
        this.checkUpgradeComplete = checkUpgradeComplete;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if ( (checkUpgradeComplete != null) && (checkUpgradeComplete.equals(APENAS_TABELA_TEMP))) {

        } else {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

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
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
            SQLiteDatabase bancoDados = conexaoBancoDeDados.abrirBanco();

            if(bancoDados != null){
                // Verifica se eh para apenas chegar se o upgrade foi completado com sucesso
                if ( (checkUpgradeComplete != null) && (checkUpgradeComplete.equals(APENAS_TABELA_TEMP)) ) {
                    if (notificationManager != null) {
                        bigTextStyle.bigText(context.getResources().getString(R.string.verificando_tem_backup_tabelas))
                                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, true);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
                    }
                    // SQL para verificar se tem algum tabela temporaria criada, se conter eh pra restaurar ela
                    sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME NOT IN('sqlite_sequence', 'android_metadata')) AND (NAME LIKE '%TEMP%')");

                    dadosTable = bancoDados.rawQuery(sql.toString(), null);

                    if ((dadosTable != null) && (dadosTable.getCount() > 0)) {

                        while (dadosTable.moveToNext()){
                            bancoDados.execSQL("DROP TABLE IF EXISTS " + dadosTable.getString(dadosTable.getColumnIndex("name")).replace("TEMP_", ""));
                        }
                        // Executa a restauracao do banco de dados
                        restaurarBackup(conexaoBancoDeDados, bancoDados);
                        return null;
                    }
                } else {
                    if (notificationManager != null) {
                        bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados))
                                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, true);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
                    }
                    sql.setLength(0);
                    sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME NOT IN('sqlite_sequence', 'android_metadata', 'ULTIMA_ATUALIZACAO_DISPOSITIVO')) AND (NAME NOT LIKE '%TEMP%')");

                    if (tabelasUpgrade != null && tabelasUpgrade.length > 0) {
                        sql.append(" AND (NAME IN(");

                        for (int i = 0; i < tabelasUpgrade.length; i++) {
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

                    if ((dadosTable != null) && (dadosTable.getCount() > 0)) {
                        //Passa por todos os dados
                        while (dadosTable.moveToNext()) {
                            final String table = dadosTable.getString(dadosTable.getColumnIndex("name"));
                            String sqlCreateTable = "CREATE TABLE IF NOT EXISTS TEMP_" + table + " AS SELECT * FROM " + table + ";";

                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    mProgressDialog.content(context.getResources().getString(R.string.salvando_dados) + " - " + table);
                                }
                            });
                            if (notificationManager != null) {
                                bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados) + " - " + table)
                                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, true);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
                            }
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
                }
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
            if (dialog != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.content(context.getResources().getString(R.string.finalizando));
                    }
                });
            }
            if (notificationManager != null) {
                bigTextStyle.bigText(context.getResources().getString(R.string.finalizando))
                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
            }
        } catch (final Exception e){
            if (notificationManager != null) {
                // Atualiza a notificacao
                bigTextStyle.bigText(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage());
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
            }
            if (dialog != null) {
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
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Zera as dadas de sincronizacao para receber todos os dados novamente.
        UltimaAtualizacaoRotinas atualizacaoRotinas = new UltimaAtualizacaoRotinas(context);
        atualizacaoRotinas.apagarDatasSincronizacao();

        if (notificationManager != null) {
            bigTextStyle.bigText(context.getResources().getString(R.string.terminamos_upgrade_banco_dados))
                    .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
            notificationManager.cancel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR);
        }
        if (dialog != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    dialog.dismiss();
                }
            });
        }
    }

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }

    private void restaurarBackup(final ConexaoBancoDeDados conexaoBancoDeDados, final SQLiteDatabase bancoDados){
        try {
            if (mProgressDialog != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.content(context.getResources().getString(R.string.criando_tabelas));
                    }
                });
            }
            if (notificationManager != null) {
                bigTextStyle.bigText(context.getResources().getString(R.string.criando_tabelas))
                        .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
            }
            conexaoBancoDeDados.onCreate(bancoDados);

            /** Cria todas as tabelas executando o create.sql
            Runnable runConexaoBanco = new Runnable() {
                @Override
                public void run() {
                    conexaoBancoDeDados.onCreate(bancoDados);
                }
            };
            synchronized (runConexaoBanco) {
                ((Activity) context).runOnUiThread(runConexaoBanco);
                runConexaoBanco.wait();
            }*/

            sql.setLength(0);
            sql.append("SELECT NAME FROM SQLITE_MASTER WHERE (TYPE = 'table') AND (NAME LIKE '%TEMP%');");

            dadosTable = bancoDados.rawQuery(sql.toString(), null);

            if ((dadosTable != null) && (dadosTable.getCount() > 0)) {
                //Passa por todos os dados
                while (dadosTable.moveToNext()) {
                    String table = dadosTable.getString(dadosTable.getColumnIndex("name")).replace("TEMP_", "");
                    final String tableTemp = dadosTable.getString(dadosTable.getColumnIndex("name"));

                    if (dialog != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressDialog.content(context.getResources().getString(R.string.salvando_dados) + " - " + tableTemp);
                            }
                        });
                    }
                    if (notificationManager != null) {
                        bigTextStyle.bigText(context.getResources().getString(R.string.salvando_dados) + " - " + tableTemp)
                                .setBigContentTitle(context.getResources().getString(R.string.upgrade_banco_dados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, true);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
                    }
                    StringBuilder sqlInsert = new StringBuilder();
                    sqlInsert.append("INSERT INTO ").append(table).append(" (");

                    StringBuilder sqlSelectTemp = new StringBuilder();
                    sqlSelectTemp.append("SELECT ");

                    //Cursor columns = bancoDados.rawQuery("PRAGMA TABLE_INFO(" + table + ");", null);
                    Cursor columnsTemp = bancoDados.rawQuery("PRAGMA TABLE_INFO(" + tableTemp + ");", null);

                    if ((columnsTemp != null) && (columnsTemp.getCount() > 0)) {
                        // Passa por todas as columans
                        for (int i = 0; i < columnsTemp.getCount(); i++) {
                            //columns.moveToNext();
                            columnsTemp.moveToNext();
                            if (i < (columnsTemp.getCount() - 1)) {
                                sqlInsert.append(columnsTemp.getString(columnsTemp.getColumnIndex("name"))).append(", ");
                                sqlSelectTemp.append(columnsTemp.getString(columnsTemp.getColumnIndex("name"))).append(", ");
                            } else {
                                sqlInsert.append(columnsTemp.getString(columnsTemp.getColumnIndex("name"))).append(") ");
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
        } catch (final Exception e){
            if (notificationManager != null) {
                notificationManager.cancel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR);
            }

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("UpgradeBancoDadosAsyncRotinas")
                            .content(e.getMessage())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        }
    }
}
