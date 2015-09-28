package com.savare.activity;

import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ImportarDadosTxtRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SincronizacaoActivity extends Activity {

	private ProgressBar progressReceberDados,
						progressReceberDadosEmpresa,
						progressReceberDadosClientes,
						progressReceberDadosProdutos,
						progressReceberDadosTitulos;
	private TextView textDataUltimoEnvio,
                     textDataUltimoRecebimento,
			 		 textReceberDados,
			 		 textReceberDadosEmpresa,
			 		 textReceberDadosClientes,
			 		 textReceberDadosProdutos,
			 		 textReceberDadosTitulos;
	private Button buttonReceberDados,
				   buttonReceberDadosEmpresa,
				   buttonReceberDadosClientes,
				   buttonReceberDadosProdutos,
				   buttonReceberDadosTitulos;
	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sincronizacao);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoActivity.this);
		
		funcoes.bloqueiaOrientacaoTela();
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		// 
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		recuperaCampos();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Instancia a classe de rotinas da tabela usuario
		UsuarioRotinas usuarioRotinas = new UsuarioRotinas(SincronizacaoActivity.this);

		// Pega a data do ultimo envio de pedido
		textDataUltimoEnvio.setText(usuarioRotinas.dataUltimoEnvio(funcoes.getValorXml("CodigoUsuario")));
        // Pega a data do ultimo recebimento de dados
        textDataUltimoRecebimento.setText(usuarioRotinas.dataUltimoRecebimento(funcoes.getValorXml("CodigoUsuario")));

		
		buttonReceberDados.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoActivity.this, progressReceberDados, textReceberDados);
				receberDadosFtpAsync.execute();
			}
		});
		
		buttonReceberDadosEmpresa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoActivity.this, progressReceberDadosEmpresa, textReceberDadosEmpresa);
				receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_S);
			}
		});
		
		buttonReceberDadosClientes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoActivity.this, progressReceberDadosClientes, textReceberDadosClientes);
				receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_C);
			}
		});
		
		buttonReceberDadosProdutos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoActivity.this, progressReceberDadosProdutos, textReceberDadosProdutos);
				receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_A);
			}
		});
		
		buttonReceberDadosTitulos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoActivity.this, progressReceberDadosTitulos, textReceberDadosTitulos);
				receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_R);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoActivity.this);
		funcoes.desbloqueiaOrientacaoTela();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.sincronizacao, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				finish();
				break;

			case  R.id.menu_sincronizacao_atualizar:
				/*HashMap<String, String> params = new HashMap<String, String>();
				params.put("email", "ns.bruno@gmail.com");
				params.put("pasword", "123");
				params.put("METODO", "OBJECT");
				params.put("dados", "Nenhum dados transmetidos");
				params.put("usuario", "Bruno Nogueira Silva");
				params.put("empresa", "Parceirão Distribuidora");

				EnviarDadosJsonRotinas enviarDadosJson = new EnviarDadosJsonRotinas(SincronizacaoActivity.this, EnviarDadosJsonRotinas.TIPO_OBJECT);
				enviarDadosJson.enviarDados(params);

				enviarDadosJson.enviarDados(null);*/

				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoActivity.this);
				funcoes.TriggerRefresh(SincronizacaoActivity.this);

				/*EnviarOrcamentoSyncAdapterRotinas enviarOrcamento = new EnviarOrcamentoSyncAdapterRotinas(SincronizacaoActivity.this, EnviarOrcamentoSyncAdapterRotinas.TELA_SEGUNDO_PLANO);
				enviarOrcamento.execute(); */
				/*try {
					EnviarOrcamentoSyncAdapterRotinas enviarOrcamento = new EnviarOrcamentoSyncAdapterRotinas(SincronizacaoActivity.this,
																											  EnviarOrcamentoSyncAdapterRotinas.TELA_SEGUNDO_PLANO);
					enviarOrcamento.execute();

				}catch (Exception e){
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoActivity.this);

					// Armazena as informacoes para para serem exibidas e enviadas
					ContentValues contentValues = new ContentValues();
					contentValues.put("comando", 0);
					contentValues.put("tela", "SincronizacaoActivity");
					contentValues.put("mensagem", e.getMessage());
					contentValues.put("dados", this.toString());
					// Pega os dados do usuario
					contentValues.put("usuario", funcoes.getValorXml("Usuario"));
					contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					contentValues.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(contentValues);
				}*/
				break;
			
		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected
	
	private void recuperaCampos(){
		textDataUltimoEnvio = (TextView) findViewById(R.id.activity_sincronizacao_text_data_ultimo_envio);
        textDataUltimoRecebimento = (TextView) findViewById(R.id.activity_sincronizacao_text_data_ultimo_recebimento);
		textReceberDados = (TextView) findViewById(R.id.activity_sincronizacao_textView_receber_dados);
		textReceberDadosEmpresa = (TextView) findViewById(R.id.activity_sincronizacao_textView_receber_dados_bloco_s);
		textReceberDadosClientes = (TextView) findViewById(R.id.activity_sincronizacao_textView_receber_dados_bloco_c);
		textReceberDadosProdutos = (TextView) findViewById(R.id.activity_sincronizacao_textView_receber_dados_bloco_a);
		textReceberDadosTitulos = (TextView) findViewById(R.id.activity_sincronizacao_textView_receber_dados_bloco_r);
		buttonReceberDados = (Button) findViewById(R.id.activity_sincronizacao_button_receber_dados);
		buttonReceberDadosEmpresa = (Button) findViewById(R.id.activity_sincronizacao_button_receber_dados_bloco_s);
		buttonReceberDadosClientes = (Button) findViewById(R.id.activity_sincronizacao_button_receber_dados_bloco_c);
		buttonReceberDadosProdutos = (Button) findViewById(R.id.activity_sincronizacao_button_receber_dados_bloco_a);
		buttonReceberDadosTitulos = (Button) findViewById(R.id.activity_sincronizacao_button_receber_dados_bloco_r);
		progressReceberDados = (ProgressBar) findViewById(R.id.activity_sincronizacao_progressBar_receber_dados);
		progressReceberDadosEmpresa = (ProgressBar) findViewById(R.id.activity_sincronizacao_progressBar_receber_dados_bloco_s);
		progressReceberDadosClientes = (ProgressBar) findViewById(R.id.activity_sincronizacao_progressBar_receber_dados_bloco_c);
		progressReceberDadosProdutos = (ProgressBar) findViewById(R.id.activity_sincronizacao_progressBar_receber_dados_bloco_a);
		progressReceberDadosTitulos = (ProgressBar) findViewById(R.id.activity_sincronizacao_progressBar_receber_dados_bloco_r);
	}
}
