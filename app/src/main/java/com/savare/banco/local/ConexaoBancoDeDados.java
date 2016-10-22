package com.savare.banco.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.savare.banco.AssetUtils;
import com.savare.beans.DispositivoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConexaoBancoDeDados extends SQLiteOpenHelper {

	private Context context;

	//private static final Logger log = LoggerFactory.getLogger(ConexaoBancoDeDados.class);

	private static final String SQL_DIR = "sql" ;
	private static final String CREATEFILE = "create.sql";
	private static final String UPGRADEFILE_PREFIX = "upgrade-";
	private static final String UPGRADEFILE_SUFFIX = ".sql";

	//private static int VERSAO = 1;
	private static String NOME_BANCO = "DadosSavare.db";
	private static String PATH_BANCO = Environment.getExternalStorageDirectory() + "/SAVARE/BancoDeDados/";
	private SQLiteDatabase bancoSavare;
	private List<String> listaInsertPadrao = null;

	public ConexaoBancoDeDados(Context context, int versao) {
		//super(context, PATH_BANCO + NOME_BANCO, null, VERSAO);
		super(context, PATH_BANCO + NOME_BANCO, null, versao);
		new File(Environment.getExternalStorageDirectory().toString() + "/SAVARE/BancoDeDados").mkdirs();
		this.context = context;
		//abrirBanco();
	}

	@Override
	public void onCreate(SQLiteDatabase bd) {
		Log.i("SAVARE", "create database");
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
			// Armazena as informacoes para para serem exibidas e enviadas
			ContentValues contentValues = new ContentValues();
			contentValues.put("comando", 0);
			contentValues.put("tela", "ConexaoBancoDeDados");
			contentValues.put("mensagem", "Não foi possível criar as tabelas do banco de dados. \n" + e.getMessage());
			contentValues.put("dados", e.toString());
			// Pega os dados do usuario
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas( this.context);
			contentValues.put("usuario", funcoes.getValorXml("Usuario"));
			contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
			contentValues.put("email", funcoes.getValorXml("Email"));

			funcoes.menssagem(contentValues);

		} catch (IOException e) {
			//e.printStackTrace();
			// Armazena as informacoes para para serem exibidas e enviadas
			ContentValues contentValues = new ContentValues();
			contentValues.put("comando", 0);
			contentValues.put("tela", "ConexaoBancoDeDados");
			contentValues.put("mensagem", "Erro ao tentar pegar o arquivo com as instruções SQL. \n" + e.getMessage());
			contentValues.put("dados", e.toString());
			// Pega os dados do usuario
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas( this.context);
			contentValues.put("usuario", funcoes.getValorXml("Usuario"));
			contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
			contentValues.put("email", funcoes.getValorXml("Email"));

			funcoes.menssagem(contentValues);
		}
	} // Fim onCreate

	
	@Override
	public void onUpgrade(SQLiteDatabase bd, int versaoAntiga, int versaoNova) {
		try {
			Log.i("SAVARE", "upgrade database from " + versaoAntiga + " to " + versaoNova);

			for( String sqlFile : AssetUtils.list(SQL_DIR, this.context.getAssets())) {
				if ( sqlFile.startsWith(UPGRADEFILE_PREFIX)) {
					int fileVersion = Integer.parseInt(sqlFile.substring( UPGRADEFILE_PREFIX.length(),  sqlFile.length() - UPGRADEFILE_SUFFIX.length()));
					if ( fileVersion > versaoAntiga && fileVersion <= versaoNova ) {
						execSqlFile( sqlFile, bd );
					}
				}
			}
		} catch( IOException exception ) {
			// Armazena as informacoes para para serem exibidas e enviadas
			ContentValues contentValues = new ContentValues();
			contentValues.put("comando", 0);
			contentValues.put("tela", "ConexaoBancoDeDados");
			contentValues.put("mensagem", "Falha na atualização do Banco de Dados. \n" + exception.getMessage());
			contentValues.put("dados", exception.toString());
			// Pega os dados do usuario
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas( this.context);
			contentValues.put("usuario", funcoes.getValorXml("Usuario"));
			contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
			contentValues.put("email", funcoes.getValorXml("Email"));

			funcoes.menssagem(contentValues);
		}
		//onCreate(bd);
	} // Fim onUpgrade

	
	
	/**
	 * Funcao responsavel por abrir o banco de dados
	 * 
	 * @return
	 */
	public SQLiteDatabase abrirBanco() {
		Log.i("SAVARE", "ConexaoBancoDeDados - abrirBanco");

		String bancoDados = PATH_BANCO + NOME_BANCO;
		//File s = context.getExternalFilesDir(null);

		if (bancoSavare == null || !bancoSavare.isOpen()) {
			bancoSavare = SQLiteDatabase.openDatabase(bancoDados, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING|
																		SQLiteDatabase.NO_LOCALIZED_COLLATORS|
																		SQLiteDatabase.CREATE_IF_NECESSARY|
																		SQLiteDatabase.OPEN_READWRITE);
			
			//bancoSavare = getWritableDatabase();
			//bancoSavare = SQLiteDatabase.openDatabase(bancoDados, null, 0);
			// bancoSavare = getWritableDatabase();
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

	/*private void importDatabase(String inputFileName) throws IOException {
		InputStream mInput = new FileInputStream(inputFileName);
		String outFileName = Environment.getExternalStorageDirectory().toString();
		OutputStream mOutput = new FileOutputStream(outFileName);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer)) > 0) {
			mOutput.write(mBuffer, 0, mLength);
		}
		mOutput.flush();
		mOutput.close();
		mInput.close();
	}*/


	protected void execSqlFile(String sqlFile, SQLiteDatabase db ) throws SQLException, IOException {
		//log.info("  exec sql file: {}" + sqlFile);
		Log.i("SAVARE", "Executar o SqlFile. - ConexaoBancoDeDados");
		for(String sqlInstruction : SqlParser.parseSqlFile(SQL_DIR + "/" + sqlFile, this.context.getAssets())) {
			/*if (sqlInstruction.contains("S INSERT_CFACLIFO_ANT B")){
				String s = sqlInstruction;
				int i = sqlInstruction.length();
			}*/
			// Executa a instrucao sql
			db.execSQL(sqlInstruction);
		}
	}

	protected void criaListaInsertPadrao(){
		listaInsertPadrao = new ArrayList<String>();

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

		if (telephonyManager != null) {
			listaInsertPadrao.add("INSERT OR ABORT INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'AEAORCAM', (DATETIME('NOW', 'localtime')));");
			listaInsertPadrao.add("INSERT OR ABORT INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'AEAITORC', (DATETIME('NOW', 'localtime')));");
			listaInsertPadrao.add("INSERT OR ABORT INTO ULTIMA_ATUALIZACAO_DISPOSITIVO (ID_DISPOSITIVO, TABELA, DATA_ULTIMA_ATUALIZACAO) VALUES ('" + telephonyManager.getDeviceId() + "', " + "'RPAPARCE_BAIXA', (DATE('NOW', 'localtime')));");
		}
	}
}
