package com.savare.banco.funcoesSql;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.local.DatabaseManager;
import com.savare.funcoes.FuncoesPersonalizadas;

import java.util.List;

public class FuncoesSqlThread {

    private Context context;
    private SQLiteDatabase bancoDados;
    private DatabaseManager databaseManager;
    private String tabela;
    private static final String[] CONFLICT_VALUES = new String[] {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};

    public FuncoesSqlThread(Context context) {
        this.context = context;
        this.databaseManager = DatabaseManager.getInstance(context);
    }

    public FuncoesSqlThread(Context context, String tabela) {
        this.context = context;
        this.tabela = tabela;
        this.databaseManager = DatabaseManager.getInstance(context);
    }

    public Cursor sqlSelectThread(String sql){
        //Log.i("SAVARE", sql);
        Cursor cursor = null;
        bancoDados = databaseManager.openDatabase();
        try {
            cursor = bancoDados.rawQuery(sql, null);

            if(cursor.getCount() < 0){
                cursor = null;
            }

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
            DatabaseManager.getInstance(context).closeDatabase();
        }
        return cursor;
    }

    /**
     * Funcao para inserir no banco de dados. \n
     * Tem que ser passado por parametro os dados atraves de @values.
     * x
     * @param values - Dados que eh para ser inseridos
     * @return
     */
    public long insert(ContentValues values){
        bancoDados = databaseManager.openDatabase();
        long id = 0;

        try {
            bancoDados.beginTransaction();
            // Inseri os valores no banco de dados
            id = bancoDados.insertWithOnConflict(tabela, null, values, SQLiteDatabase.CONFLICT_NONE);
            //id = bancoDados.insert(tabela, null, values);

            if (id <= 0){
                new MaterialDialog.Builder(context)
                        .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                        .content(context.getResources().getString(R.string.nao_foi_possivel_cadastrar))
                        .positiveText(R.string.button_ok)
                        .show();
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            //funcoes.menssagem(contentValues);
            new MaterialDialog.Builder(context)
                    .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                    .content(context.getResources().getText(R.string.ip_servidor_webservice_nao_cadastrado) + "\n" + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
			/*((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					//funcoes.menssagem(contentValues);
					new MaterialDialog.Builder(context)
							.title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
							.content(context.getResources().getText(R.string.ip_servidor_webservice_nao_cadastrado) + "\n" + e.getMessage())
							.positiveText(R.string.button_ok)
							.show();
				}
			});*/
        } finally{
            bancoDados.setTransactionSuccessful();
            bancoDados.endTransaction();
            databaseManager.closeDatabase();
        }
        return id;
    } // Fim do insert


    /**
     * Funcao para inserir, mas caso o registro seja unico e j�
     * exista no banco de dados o registro ser� subistituido pelo atual. \n
     * Tem que ser passado por parametro os dados atraves de @values.
     *
     * @param values - Dados que eh para ser inseridos
     * @return
     */
    public long insertOrReplace(ContentValues values){
        bancoDados = databaseManager.openDatabase();
        long id = 0;

        try {
            // Inseri os valores no banco de dados
            id = bancoDados.insertWithOnConflict(tabela, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        } catch (final SQLException e) {

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally{
            databaseManager.closeDatabase();
        }
        return id;
    } // Fim do insert

    /**
     * Executa um insert no banco mais rapido devido passar no paramento
     * o sql puro e os dados do argumento que contem no sql.
     * Para gerar o sql e os argumento para usar a funcao(metodo) construirSqlStatement,
     * e para gerar os argumentos basta usar a funcao argumentoStatement.
     *
     * @param sql
     * @param bindArgs
     * @return
     */
    public long insertOrReplaceFast(String sql, String[] bindArgs){
        bancoDados = databaseManager.openDatabase();
        long id = 0;

        bancoDados.beginTransaction();

        try {
            SQLiteStatement statement = bancoDados.compileStatement(sql);

            if (bindArgs != null) {
                for (int i = bindArgs.length; i != 0; i--) {
                    if((bindArgs[i - 1] != null) && (bindArgs[i - 1].length() > 0)){
                        statement.bindString(i, bindArgs[i - 1]);
                    }
                }
            }

            //statement.bindAllArgsAsStrings(bindArgs);
            id = statement.executeInsert();

        } catch (final SQLException e) {
            Log.e("SAVARE", e.getMessage());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            Log.e("SAVARE", e.getMessage());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.getMessage()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {

            bancoDados.setTransactionSuccessful();
            bancoDados.endTransaction();

            databaseManager.closeDatabase();
        }
        return id;
    }


    public boolean insertList(List<ContentValues> listaValores){
        boolean totalSucesso = true;
        bancoDados = databaseManager.openDatabase();
        try {
            bancoDados.beginTransaction();

            for(ContentValues valores : listaValores) {
                //bancoDados.execSQL(construirInsert(valores, bancoDados.CONFLICT_REPLACE));
                if (bancoDados.insertWithOnConflict(tabela, null, valores, bancoDados.CONFLICT_REPLACE) <= 0){
                    totalSucesso = false;
                }
            }
        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(e.toString())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(e.toString())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {

            bancoDados.setTransactionSuccessful();
            bancoDados.endTransaction();

            databaseManager.closeDatabase();
        }
        return totalSucesso;
    }

    public int update(ContentValues dados, String where){
        int qtdAtualizado = 0;
        bancoDados = databaseManager.openDatabase();

        try {
            //qtdAtualizado = bancoDados.updateWithOnConflict(tabela, dados, where, null, 0);
            qtdAtualizado = bancoDados.update(tabela, dados, where, null);

            if (qtdAtualizado <= 0){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                                .content(context.getResources().getString(R.string.nao_foi_possivel_atualizar))
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        } finally{
            databaseManager.closeDatabase();
        }
        return qtdAtualizado;
    } // Fim do update

    public int updateFast(ContentValues dados, String where){
        int qtdAtualizado = 0;
        bancoDados = databaseManager.openDatabase();
        try {
            qtdAtualizado = bancoDados.updateWithOnConflict(tabela, dados, where, null, SQLiteDatabase.CONFLICT_NONE);

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        } finally{
            databaseManager.closeDatabase();
        }
        return qtdAtualizado;
    } // Fim do update


    /**
     * Funcao para recuperar os dados, ou seja, executa qualquer select.
     *
     * @param where - Condicoes para selecionar os dados
     * @return - Retorna um Cursor com os dados recuperados.
     */
    public Cursor query(String where){
        Cursor cursor = null;
        bancoDados = databaseManager.openDatabase();
        try {

            cursor = bancoDados.query(tabela, null, where, null, null, null, null);

            if(cursor.getCount() < 0){
                cursor = null;
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
            databaseManager.closeDatabase();
        }
        return cursor;
    } //Fim do query


    /**
     * Funcao para recuperar os dados, ou seja, o select, mas
     * com a possibilidade de ordernar o resultado do select.
     *
     * @param where - Condicoes para selecionar os dados
     * @param ordem - Colunas para ordenar
     * @return - Retorna um Cursor com os dados recuperados.
     */
    public Cursor query(String where, String ordem){
        //Log.i("SAVARE", "FuncoesSql - " + where + " - " + ordem);

        Cursor cursor = null;
        bancoDados = databaseManager.openDatabase();
        try {

            cursor = bancoDados.query(tabela, null, where, null, null, null, ordem);

            if(cursor.getCount() < 0){
                cursor = null;
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
            databaseManager.closeDatabase();
        }
        return cursor;
    } //Fim do query

    /**
     *  Executa os comando em SQL de INSERT, UPDATE e DELETE;
     *  NAO executa SELECT.
     *
     * @param sql
     */
    public void execSQL(String sql){
        Log.i("SAVARE", sql);

        bancoDados = databaseManager.openDatabase();
        try {
            bancoDados.execSQL(sql);

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
            databaseManager.closeDatabase();
        }

    } //Fim do execSQL


    /**
     * Funcao para executar um select qualquer.
     * @param sql - Passa o select que deseja ser executado.
     * @return - Retorna um cursor com os dados recuperados, ou um cursor nulo
     * caso nao exista valores.
     *
     */
    public Cursor sqlSelect(String sql){
        //Log.i("SAVARE", sql);
        Cursor cursor = null;
        bancoDados = databaseManager.openDatabase();
        try {
            cursor = bancoDados.rawQuery(sql, null);

            if(cursor.getCount() < 0){
                cursor = null;
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
            databaseManager.closeDatabase();
        }

        return cursor;
    } //Fim do sqlCustom

    /**
     * Funcao para deletar registro do banco de dados.
     * @param where
     * @return - Retorna a quantidade de registros(cadastros) deletados.
     */
    public int delete(String where){
        int quantidadeExcluido = 0;
        bancoDados = databaseManager.openDatabase();
        try {

            quantidadeExcluido = bancoDados.delete(tabela, where, null);

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        }catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        } finally {
            databaseManager.closeDatabase();
        }
        // Retorna a quantidade de registros excluidos
        return quantidadeExcluido;
    }

    public int getCountRows(String where){
        Cursor cursor = null;
        bancoDados = databaseManager.openDatabase();
        int qtdRows = 0;
        try {
            String select = "SELECT COUNT(*) AS QTDROWS FROM " + tabela;

            if ((where != null) && (!where.isEmpty())){
                select += " WHERE ( " + where + " );";
            }else {
                select += ";";
            }
            cursor = bancoDados.rawQuery(select, null);

            if((cursor != null) && (cursor.getCount() > 0)){
                cursor.moveToFirst();
                qtdRows = cursor.getInt(cursor.getColumnIndex("QTDROWS"));
            }

        } catch (final SQLException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("FuncoesSqlThread - " + (tabela != null ? tabela : ""))
                            .content(new FuncoesPersonalizadas(context).tratamentoErroBancoDados(e.toString()))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

        } finally {
           databaseManager.closeDatabase();
        }
        return qtdRows;
    }

    public String construirSqlStatement(ContentValues values){
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" OR REPLACE");
        sql.append(" INTO ");
        sql.append(tabela);
        sql.append('(');

        int size = (values != null && values.size() > 0) ? values.size() : 0;

        if (size > 0) {
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append(((i > 0) && (values.get(colName).toString().length() > 0)) ? "," : "");
                sql.append((values.get(colName).toString().length() > 0) ? colName : "");
                // Incrementa o controle
                if ((values.get(colName).toString().length() > 0)) {
                    i++;
                }
            }
            sql.append(')');
            sql.append(" VALUES (");
            i = 0;
            for (String colName : values.keySet()) {
                sql.append(((i > 0) && (values.get(colName).toString().length() > 0)) ? "," : "");
                sql.append((values.get(colName).toString().length() > 0) ? "?" : "");
                // Incrementa o controle
                if (values.get(colName).toString().length() > 0) {
                    i++;
                }
            }
        } else {
            sql.append(null + ") VALUES (NULL");
        }
        sql.append(')');
        return sql.toString();
    }

    public String construirInsert(ContentValues values, int conflictAlgorithm){
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(" INTO ");
        sql.append(tabela);
        sql.append('(');

        int size = (values != null && values.size() > 0) ? values.size() : 0;

        if (size > 0) {
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append(((i > 0) && (values.get(colName) != null) && (values.get(colName).toString().length() > 0)) ? ", " : "");
                sql.append(((values.get(colName) != null) && (values.get(colName).toString().length() > 0)) ? colName : "");
                // Incrementa o controle
                if ((values.get(colName) != null) && (values.get(colName).toString().length() > 0)) {
                    i++;
                }
            }
            sql.append(')');
            sql.append(" VALUES (");
            i = 0;
            for (String colName : values.keySet()) {
                sql.append(((i > 0) && (values.get(colName) != null) && (values.get(colName).toString().length() > 0)) ? ", " : "");
                //sql.append((values.get(colName).toString().length() > 0) ? "?" : "");


                if ((values.get(colName) != null) && (values.get(colName).toString().length() > 0)){
                    if (((values.get(colName).getClass().equals(String.class)) ||
                            (values.get(colName).getClass().equals(CharSequence.class))) &&
                            (!(((values.getAsString(colName).contains("SELECT")) && (values.getAsString(colName).contains("FROM"))) ||
                                    ((values.getAsString(colName).contains("select")) && (values.getAsString(colName).contains("from")))))){
                        sql.append("'" + values.getAsString(colName) + "'");
                    } else {
                        sql.append(values.getAsString(colName));
                    }
                }
                // Incrementa o controle
                if ((values.get(colName) != null) && (values.get(colName).toString().length() > 0)) {
                    i++;
                }
            }
        } else {
            sql.append(null + ") VALUES (NULL");
        }
        sql.append(')');
        return sql.toString();
    }

    public String construirUpdate(ContentValues values, String where, int conflictAlgorithm){
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(this.tabela);
        sql.append(" SET ");

        int i = 0;
        for (String colName : values.keySet()) {
            sql.append(((i > 0) && (values.get(colName) != null) && (values.get(colName).toString().length() > 0)) ? "," : "");
            sql.append(((values.get(colName) != null) && (values.get(colName).toString().length() > 0)) ? colName : "");
            // Checa se tem valores
            if ((values.get(colName) != null) && (values.get(colName).toString().length() > 0)){
                // Checa se eh uma string
                if (((values.get(colName).getClass().equals(String.class)) ||
                        (values.get(colName).getClass().equals(CharSequence.class))) &&
                        (!values.get(colName).equals("null"))){

                    sql.append(" = '" + values.getAsString(colName) + "'");

                } else {
                    sql.append(" = " +  values.get(colName));
                }
                i++;
            }
        }
        // Checa se o where passado por paramento nao esta vazia
        if ((where != null) && (!where.isEmpty())){
            sql.append(" WHERE (" + where + ")");
        }
        return sql.toString();
    }


    public String[] argumentoStatement(ContentValues values){
        String[] bindArgs = null;
        int size = 0;

        if(values != null && values.size() > 0){
            for (String colName : values.keySet()) {
                if(values.get(colName).toString().length() > 0){
                    size++;
                }
            }
        }
        //int size = (values != null && values.size() > 0) ? values.size() : 0;

        if (size > 0) {
            bindArgs = new String[size];
            int i = 0;
            for (String colName : values.keySet()) {
                // Checa se existe algum valor armazenado
                if(values.get(colName).toString().length() > 0){
                    bindArgs[i] = values.get(colName).toString();
                    i++;
                }

            }
        }
        return bindArgs;
    }
}
