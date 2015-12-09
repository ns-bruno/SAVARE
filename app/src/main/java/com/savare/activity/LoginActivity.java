package com.savare.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.savare.R;
import com.savare.activity.material.designer.InicioMDActivity;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class LoginActivity extends Activity {
	
	// Variaveis globais
	private TextView textUsuario,
					 textCodigoUsuario;
	private EditText editSenha;
	private Button buttonEntrar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		recuperarDadosTela();

		buttonEntrar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Rotinas rotinas = new Rotinas(LoginActivity.this);
				
				// Checa se existe o codigo do usuario e o nome do usuario
				if(rotinas.checaUsuario(textCodigoUsuario.getText().toString(), textUsuario.getText().toString())){
					
					// Checa se a senha esta certa
					try {
						if(rotinas.checaUsuarioESenha(textCodigoUsuario.getText().toString(), textUsuario.getText().toString(), editSenha.getText().toString()) == true){
							// Abre a tela inicial do sistema
							//Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
							Intent intent = new Intent(LoginActivity.this, InicioMDActivity.class);
							// Tira a acitivity da pilha e inicia uma nova
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
							
						// Mostra uma mensagem caso a senha esteja errada
						} else {
							ContentValues mensagem = new ContentValues();
							mensagem.put("comando", 1);
							mensagem.put("tela", "LoginActivitys");
							mensagem.put("mensagem", "Senha incorreta");
							
							FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);
							funcoes.menssagem(mensagem);
						}
					} catch (Exception e) {
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 1);
						mensagem.put("tela", "LoginActivitys");
						mensagem.put("mensagem", "Senha incorreta \n" + e.getMessage());
						mensagem.put("dados", e.toString());
						
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);
						funcoes.menssagem(mensagem);
					}
					
				// Mostra uma mensagem caso usuario esteja errado	
				} else {
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 1);
					mensagem.put("tela", "LoginActivitys");
					mensagem.put("mensagem", "Usuário não existe");
					mensagem.put("dados", textCodigoUsuario.getText().toString() + textUsuario.getText().toString());
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);
					funcoes.menssagem(mensagem);
				}
			}
		});
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	/**
	 * Executa toda vez que a activity eh aberta
	 */
	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (!camposObrigatorioPreenchido()) {
				// Abre a tela inicial do sistema
				Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
				startActivity(intent);
				return;
			}

			// Instancia a classe de funcoes personalizadas
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);
			// Funca o codigo do usuario no xml
			String codigoUsuario = funcoes.getValorXml("CodigoUsuario");

			// Instancia a classe de rotinas
			Rotinas rotinas = new Rotinas(LoginActivity.this);

			// Verfifica se existe algum usuario cadastrado, ou
			if ((rotinas.existeUsuario() == false) || (codigoUsuario.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
				// Abre a tela inicial do sistema
				Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
				startActivity(intent);

			} else {
				textCodigoUsuario.setText(codigoUsuario);
				UsuarioSQL usuarioSQL = new UsuarioSQL(LoginActivity.this);
				// Pega os dados do usuario(vendedor)
				Cursor dadosUsuario = usuarioSQL.query("id_usua = " + codigoUsuario);

				if ((dadosUsuario != null) && (dadosUsuario.getCount() > 0)) {
					// Move para o primeiro registro
					dadosUsuario.moveToFirst();
					// Preenche os campos com os dados do usuario(vendedor)
					textUsuario.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA")));
				}
			}
			// Cria o alarme se nao existir para enviar e receber dados
			funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, true);

		}catch (Exception e){
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);

			// Armazena as informacoes para para serem exibidas e enviadas
			ContentValues contentValues = new ContentValues();
			contentValues.put("comando", 0);
			contentValues.put("tela", "LoginActivity");
			contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.getMessage()));
			contentValues.put("dados", LoginActivity.this.toString());
			// Pega os dados do usuario

			contentValues.put("usuario", funcoes.getValorXml("Usuario"));
			contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
			contentValues.put("email", funcoes.getValorXml("Email"));

			funcoes.menssagem(contentValues);
		}
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.login, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case R.id.menu_login_cadastro_usuario:
			// Cria uma intent para salvar o local que eh para ser aberto
			Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
			intent.putExtra("RECADASTRAR", true);
			startActivity(intent);
			
			break;
			
			

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Recupera os dados da tela da view
	 * @return 
	 */
	private void recuperarDadosTela(){
		textUsuario = (TextView) findViewById(R.id.activity_login_text_usuario);
		textCodigoUsuario = (TextView) findViewById(R.id.activity_login_text_codigo_usuario);
		editSenha = (EditText) findViewById(R.id.activity_login_edit_senha);
		buttonEntrar = (Button) findViewById(R.id.activity_login_button_entrar);
	}
	
	private boolean camposObrigatorioPreenchido(){
		boolean retorno = false;

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginActivity.this);
		// Checa se tem algum campo obrigatorio vazio
		if ((funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
				(funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
				(funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
				(funcoes.getValorXml("ChaveEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
				(funcoes.getValorXml("ModoConexao").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
			retorno = false;

		} else {
			retorno = true;
		}
		return retorno;
	}
	
}
