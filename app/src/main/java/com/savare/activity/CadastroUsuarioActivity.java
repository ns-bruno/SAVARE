package com.savare.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.savare.R;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.funcoes.FuncoesPersonalizadas;

public class CadastroUsuarioActivity extends Activity {
	
	private static String TAG = "SAVARE";
	private EditText editChaveEmpresa,
					 editCodigoEmpresa,
					 editNomeCompleto,
					 editNomeLogin,
					 editSenha,
					 editEmail,
					 editIpServidor,
					 editSenhaServidor,
					 editUsuarioServidor,
					 editCodigoVendedor,
					 editPastaServidor;
	private RadioGroup radioGroupModoConexao;
	private RadioButton radioButtonAtivo,
						radioButtonPassivo;
	private FuncoesPersonalizadas funcoes;
	private ContentValues mensagem;
	private boolean recadastrar = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro_usuario);
		
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			recadastrar = intentParametro.getBoolean("RECADASTRAR");
		}
		recuperarCampoTela();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if(recadastrar){
			UsuarioSQL usuarioSQL = new UsuarioSQL(CadastroUsuarioActivity.this);
			// Pega os dados do usuário no banco de dados
			Cursor dadosUsuario = usuarioSQL.query(null);
			// Move para o primeiro registro
			dadosUsuario.moveToFirst();
			// Checa se existe algum registro
			if( (dadosUsuario != null) && (dadosUsuario.getCount() > 0) ){
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(CadastroUsuarioActivity.this);
				
				editChaveEmpresa.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("CHAVE_USUA")));
				//editCodigoEmpresa.setText(funcoes.getValorXml("CodigoEmpresa"));
				editCodigoEmpresa.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("ID_SMAEMPRE")));
				editNomeCompleto.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("NOME_USUA")));
				editNomeLogin.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA")));
				editSenha.setText(funcoes.descriptografaSenha(dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_USUA"))));
				editEmail.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("EMAIL_USUA")));
				editIpServidor.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("IP_SERVIDOR_USUA")));
				editSenhaServidor.setText(funcoes.descriptografaSenha(dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_SERVIDOR_USUA"))));
				editUsuarioServidor.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("USUARIO_SERVIDOR_USUA")));
				editCodigoVendedor.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("ID_USUA")));
				editPastaServidor.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("PASTA_SERVIDOR_USUA")));

				if (dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO")).equalsIgnoreCase("A")){
					radioGroupModoConexao.check(R.id.activity_cadastro_radioButton_modo_ativo);

				} else if (dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO")).equalsIgnoreCase("P")){
					radioGroupModoConexao.check(R.id.activity_cadastro_radioButton_modo_passivo);

				} else if (dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO")).equalsIgnoreCase("S")){
					radioGroupModoConexao.check(R.id.activity_cadastro_radioButton_segundo_plano);
				}
			}
		} /*else {
			Log.i(TAG, "delete - CadastrousuarioActivity");
			UsuarioSQL usuarioSQL = new UsuarioSQL(CadastroUsuarioActivity.this);
			usuarioSQL.delete(null);
		}*/
	}
	
	/**
	 * Metodo responsavel para inflar(aparecer) o menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//Infla (faz aparecer) o menus
		getMenuInflater().inflate(R.menu.cadastro_usuario, menu);
		
		return true;
	}
	
	/**
	 * Metodo ouvite esperando a selecao do menu.
	 * Vai executar alguma coisa de acorto com o menu clicado.
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		
		int id = item.getItemId();
		
		if (id == R.id.menu_cadastro_usuario_salvar) {
			
			UsuarioSQL usuarioSQL = new UsuarioSQL(CadastroUsuarioActivity.this);

			if (recadastrar){
				if (usuarioSQL.update(salvarDadosCampo(), "ID_USUA = " + editCodigoVendedor.getText().toString()) > 0){
					Log.i(TAG, "#update - salvarDadosCampo - CadastroUsuarioActivity");

					salvarDadosXml();
				}
			} else {
				if (usuarioSQL.insert(salvarDadosCampo()) > 0) {
					Log.i(TAG, "#insert - salvarDadosCampo - CadastroUsuarioActivity");

					salvarDadosXml();
				}
			}
			
		} else if (id == R.id.menu_cadastro_usuario_cancelar) {
			// Fecha a view(tela)
			finish();
		}
		return true;
	}
	
	/**
	 * Recupera os dados da tela da view
	 * @return 
	 */
	private void recuperarCampoTela(){
		
		editChaveEmpresa = (EditText) findViewById(R.id.cadastro_usuario_edit_chave);
		editCodigoEmpresa = (EditText) findViewById(R.id.cadastro_usuario_edit_codigo_empresa);
		editNomeCompleto = (EditText) findViewById(R.id.cadastro_usuario_edit_nome);
		editNomeLogin = (EditText) findViewById(R.id.cadastro_usuario_edit_login);
		editSenha = (EditText) findViewById(R.id.cadastro_usuario_edit_senha);
		editEmail = (EditText) findViewById(R.id.cadastro_usuario_edit_email);
		editIpServidor = (EditText) findViewById(R.id.cadastro_usuario_edit_ip_servidor);
		editSenhaServidor = (EditText) findViewById(R.id.cadastro_usuario_edit_senha_servidor);
		editUsuarioServidor = (EditText) findViewById(R.id.cadastro_usuario_edit_usuario_servidor);
		editCodigoVendedor = (EditText) findViewById(R.id.cadastro_usuario_edit_codigo_vendedor);
		editPastaServidor = (EditText) findViewById(R.id.cadastro_usuario_edit_pasta_servidor);
		radioGroupModoConexao = (RadioGroup) findViewById(R.id.activity_cadastro_usuario_radio_group_modo_conexao);
	}
	
	private ContentValues salvarDadosCampo(){
		ContentValues valores = new ContentValues();
		
		try{
			this.funcoes = new FuncoesPersonalizadas(CadastroUsuarioActivity.this);
			
			valores.put("chave_usua", editChaveEmpresa.getText().toString());
			valores.put("nome_usua", editNomeCompleto.getText().toString());
			valores.put("login_usua", editNomeLogin.getText().toString());
			valores.put("senha_usua", this.funcoes.criptografaSenha(editSenha.getText().toString()));
			valores.put("email_usua", editEmail.getText().toString());
			valores.put("ip_servidor_usua", editIpServidor.getText().toString());
			valores.put("senha_servidor_usua", this.funcoes.criptografaSenha(editSenhaServidor.getText().toString()));
			valores.put("usuario_servidor_usua", editUsuarioServidor.getText().toString());
			valores.put("id_usua", editCodigoVendedor.getText().toString());
			valores.put("id_smaempre", editCodigoEmpresa.getText().toString());
			valores.put("pasta_servidor_usua", editPastaServidor.getText().toString());
			if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_modo_ativo) {
				valores.put("modo_conexao", "A");
			} else if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_modo_passivo) {
				valores.put("modo_conexao", "P");
			} else if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_segundo_plano){
				valores.put("modo_conexao", "S");
			}

		} catch (Exception e){
			// Preencho com os dados da mensagem que Ã© para ser usada
			this.mensagem.put("comando", 0);
			this.mensagem.put("tela", "CadastroUsuarioActivity");
			this.mensagem.put("mensagem", e.getMessage().toString());
			this.mensagem.put("dados", valores.toString());
			this.mensagem.put("usuario", this.funcoes.getValorXml("Usuario"));
			this.mensagem.put("empresa", this.funcoes.getValorXml("Empresa"));
			this.mensagem.put("email", this.funcoes.getValorXml("Email"));
			
			// Executa a mensagem passando os dados por parametro
			this.funcoes.menssagem(this.mensagem);
		
		}
		
		return valores;
	}

	private void salvarDadosXml(){
		funcoes = new FuncoesPersonalizadas(CadastroUsuarioActivity.this);
		funcoes.setValorXml("CodigoUsuario", editCodigoVendedor.getText().toString());
		funcoes.setValorXml("Usuario", editNomeLogin.getText().toString());
		funcoes.setValorXml("ChaveEmpresa", editChaveEmpresa.getText().toString());
		funcoes.setValorXml("CodigoEmpresa", editCodigoEmpresa.getText().toString());
		funcoes.setValorXml("Email", editEmail.getText().toString());
		funcoes.setValorXml("EnviarAutomatico", "S");
		funcoes.setValorXml("ReceberAutomatico", "S");
		funcoes.setValorXml("ImagemProduto", "N");
		if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_modo_ativo) {
			funcoes.setValorXml("ModoConexao", "A");
		} else if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_modo_passivo) {
			funcoes.setValorXml("ModoConexao", "P");
		} else if (radioGroupModoConexao.getCheckedRadioButtonId() == R.id.activity_cadastro_radioButton_segundo_plano){
			funcoes.setValorXml("ModoConexao", "S");
		}


		finish();
	}
}
