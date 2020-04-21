package com.savare.banco.local;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.funcoes.rotinas.async.UpgradeBancoDadosAsyncRotinas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConexaoBancoDeDados extends SQLiteOpenHelper {

	private Context context;

	//private static final Logger log = LoggerFactory.getLogger(ConexaoBancoDeDados.class);

	private static final String SQL_DIR = "sql";
	private static final String CREATEFILE = "create.sql";
	private static final String UPGRADEFILE_PREFIX = "upgrade-";
	private static final String UPGRADEFILE_SUFFIX = ".sql";

	//private static int VERSAO = 1;
	private static String NOME_BANCO = "DadosSavare.db";
	private static String PATH_BANCO = Environment.getExternalStorageDirectory() + "/SAVARE/BancoDeDados/";
	private SQLiteDatabase bancoSavare;
	private List<String> listaInsertPadrao = null;
	private MaterialDialog.Builder mProgressDialog;
	private MaterialDialog dialog;

	public ConexaoBancoDeDados(Context context, int versao) {
		//super(context, PATH_BANCO + NOME_BANCO, null, VERSAO);
		super(context, PATH_BANCO + NOME_BANCO, null, versao);
		new File(Environment.getExternalStorageDirectory().toString() + "/SAVARE/BancoDeDados/").mkdirs();
		this.context = context;
		//abrirBanco();
	}

	@Override
	public void onCreate(SQLiteDatabase bd) {
		Log.i("SAVARE", "Criando o Banco de Dados");
		try {
			//bd.execSQL(SQL_TABELAS[i]);
			execSqlFile(CREATEFILE, bd);

			criaListaInsertPadrao();

			if (listaInsertPadrao != null) {
				for (String insert : listaInsertPadrao) {
					bd.execSQL(insert);
				}
			}
		} catch (SQLException e) {
            new MaterialDialog.Builder(context)
                    .title("ConexaoBancoDeDados")
                    .content("Não foi possível criar as tabelas do banco de dados. \n" + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
		}
    } // Fim onCreate


	@Override
	public void onUpgrade(SQLiteDatabase bd, int versaoAntiga, int versaoNova) {
		Log.i("SAVARE", "upgrade database from " + versaoAntiga + " to " + versaoNova);
		try {
			/*for (String sqlFile : AssetUtils.list(SQL_DIR, this.context.getAssets())) {
				if (sqlFile.startsWith(UPGRADEFILE_PREFIX)) {
					int fileVersion = Integer.parseInt(sqlFile.substring(UPGRADEFILE_PREFIX.length(), sqlFile.length() - UPGRADEFILE_SUFFIX.length()));
					if ((fileVersion > versaoAntiga) && (fileVersion <= versaoNova)) {
						execSqlFile(sqlFile, bd);
					}
				}
			}*/
			//Verifica se eh uma nova versao
			if (versaoNova > versaoAntiga){
				// Executa uma classe pra atualizar o banco de dados
                UpgradeBancoDadosAsyncRotinas upgradeBancoDadosAsyncRotinas = new UpgradeBancoDadosAsyncRotinas(context, null, null);
                upgradeBancoDadosAsyncRotinas.execute();
            }
		} catch (Exception exception) {
            new MaterialDialog.Builder(context)
                    .title("ConexaoBancoDeDados")
                    .content("Falha na atualização do Banco de Dados. \n" + exception.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
		}
		//onCreate(bd);
	} // Fim onUpgrade


	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
		Log.i("SAVARE", "upgrade database from " + oldVersion + " to " + newVersion);
	}

	/**
	 * Funcao responsavel por abrir o banco de dados
	 *
	 * @return
	 */
	public SQLiteDatabase abrirBanco() {
		//Log.i("SAVARE", "ConexaoBancoDeDados - abrirBanco");

		String bancoDados = PATH_BANCO + NOME_BANCO;
		//File s = context.getExternalFilesDir(null);

		if (bancoSavare == null || !bancoSavare.isOpen()) {
			/*bancoSavare = SQLiteDatabase.openDatabase(bancoDados, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING|
																		SQLiteDatabase.NO_LOCALIZED_COLLATORS|
																		SQLiteDatabase.CREATE_IF_NECESSARY|
																		SQLiteDatabase.OPEN_READWRITE);*/

			bancoSavare = getWritableDatabase();
			//bancoSavare = SQLiteDatabase.openDatabase(bancoDados, null, SQLiteDatabase.OPEN_READWRITE);
		}
		return bancoSavare;
	}

	/**
	 * Funcao responsavel por fechar o banco de dados
	 */
	public void fechar() {

		if (bancoSavare != null && bancoSavare.isOpen()) {
			bancoSavare.close();
		}
	}

	public boolean existDataBase() {
		File dbFile = context.getDatabasePath(PATH_BANCO + NOME_BANCO);

		return dbFile.exists();
	}

	protected void execSqlFile(String sqlFile, SQLiteDatabase db){
	    try {
            Log.i("SAVARE", "Executar o SqlFile. - ConexaoBancoDeDados");
            for (String sqlInstruction : SqlParser.parseSqlFile(SQL_DIR + "/" + sqlFile, this.context.getAssets())) {
			/*if (sqlInstruction.contains("S INSERT_CFACLIFO_ANT B")){
				String s = sqlInstruction;
				int i = sqlInstruction.length();
			}*/
                // Executa a instrucao sql
                db.execSQL(sqlInstruction);
            }
        } catch (SQLException e){
            new MaterialDialog.Builder(context)
                    .title("ConexaoBancoDeDados")
                    .content("Não foi possível excutar o arquivo SQL. \n" + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        } catch (IOException e){
            new MaterialDialog.Builder(context)
                    .title("ConexaoBancoDeDados")
                    .content("Não foi possível abrir o arquivo SQL. \n" + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }
	}

	protected void criaListaInsertPadrao() {
		listaInsertPadrao = new ArrayList<String>();

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

		if (telephonyManager != null) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
			listaInsertPadrao.add("INSERT OR REPLACE INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'AEAORCAM', (DATETIME('NOW', 'localtime')));");
			listaInsertPadrao.add("INSERT OR REPLACE INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'AEAITORC', (DATETIME('NOW', 'localtime')));");
			listaInsertPadrao.add("INSERT OR REPLACE INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'RPAPARCE_BAIXA', (DATE('NOW', 'localtime')));");
		}
	}
}
