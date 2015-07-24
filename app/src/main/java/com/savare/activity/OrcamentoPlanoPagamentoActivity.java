package com.savare.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.TipoDocumentoRotinas;

public class OrcamentoPlanoPagamentoActivity extends Activity {
	
	private TextView textCodigoOrcamento,
					 textTotalBruto,
					 textTotalLiquido,
					 textAtacadoVarejo;
	private EditText editDescontoPercentual,
					 editTotalLiquido;
	private Spinner spinnerTipoDocumento,
					spinnerPlanoPagamento;
	private String codigoPessoa,
				   razaoSocial,
				   tipoOrcamentoPedido;
	private double totalBruto = 0,
				   totalLiquido = 0,
				   descontoPercentual = 0,
				   totalBrutoAuxiliar;
	private int idTipoDocumento,
				idPlanoPagamento;
	private ItemUniversalAdapter adapterTipoDocumento,
								 adapterPlanoPagamento;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_orcamento_plano_pagamento);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Carrega os campos do xml
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			textCodigoOrcamento.setText(intentParametro.getString("ID_AEAORCAM"));
			codigoPessoa = intentParametro.getString("ID_CFACLIFO");
			razaoSocial = intentParametro.getString("NOME_RAZAO");
			textAtacadoVarejo.setText(intentParametro.getString("ATAC_VAREJO"));
			
			// Seta o titulo da action bar com a raz�o do cliente
			getActionBar().setTitle(intentParametro.getString("ID_CFACLIFO") + " - " + intentParametro.getString("NOME_RAZAO"));
			
			// Instancia a classe de rotinas
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoPlanoPagamentoActivity.this);
			// Pega o total bruto do orcamento
			textTotalBruto.setText(orcamentoRotinas.totalOrcamentoBruto(textCodigoOrcamento.getText().toString()));
			// Pega o total liquido do orcamento
			editTotalLiquido.setText(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
			// Pega o percentual de desconto aplicado no total do orcamento
			editDescontoPercentual.setText(orcamentoRotinas.descontoPercentual(textCodigoOrcamento.getText().toString()));
			// Move o cursor para o final do campo
			editDescontoPercentual.setSelection(editDescontoPercentual.getText().length());
			
			// Pega o valor total bruto do orcamento
			totalBruto = Double.parseDouble(textTotalBruto.getText().toString().replace(".", "").replace(",", "")) / 1000;
			totalBrutoAuxiliar = totalBruto;
			// Pega o percentual de desconto concedido
			descontoPercentual = Double.parseDouble(editDescontoPercentual.getText().toString().replace(".", "").replace(",", "")) / 1000;
			// Pega o valor total liquido
			totalLiquido = Double.parseDouble(editTotalLiquido.getText().toString().replace(".", "").replace(",", "")) / 1000;
			
			
			
		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "Não foi carregar os dados do orçamento.\n");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
			funcoes.menssagem(mensagem);
		}
		
		// Intancia a classe de rotinas do tipo de documento
		TipoDocumentoRotinas tipoDocumentoRotinas = new TipoDocumentoRotinas(OrcamentoPlanoPagamentoActivity.this);
		// Intancia a classe do adapter
		adapterTipoDocumento = new ItemUniversalAdapter(OrcamentoPlanoPagamentoActivity.this, 3);
		// Preenche o adapter com uma lista
		adapterTipoDocumento.setListaTipoDocumento(tipoDocumentoRotinas.listaTipoDocumento(null));
		// Preenche o spinner com um adapter personalizado
		spinnerTipoDocumento.setAdapter(adapterTipoDocumento);
		
		//Instancia a classe de rotina do plano de pagamento
		PlanoPagamentoRotinas planoPagamentoRotinas = new PlanoPagamentoRotinas(OrcamentoPlanoPagamentoActivity.this);
		// Intancia a classe do adapter
		adapterPlanoPagamento = new ItemUniversalAdapter(OrcamentoPlanoPagamentoActivity.this, 4);
		// Preenche o adapter com uma lista
		adapterPlanoPagamento.setListaPlanoPagamento(planoPagamentoRotinas.listaPlanoPagamento(null, "DESCRICAO", textAtacadoVarejo.getText().toString()));
		// Preenche o spinner com um adapter personalizado
		spinnerPlanoPagamento.setAdapter(adapterPlanoPagamento);
		// Posiciona o spinner no plano de pagamento
		spinnerPlanoPagamento.setSelection(planoPagamentoRotinas.posicaoPlanoPagamentoLista(adapterPlanoPagamento.getListaPlanoPagamento(), textCodigoOrcamento.getText().toString()));
		
		// Executa quando existe foco no campo
		editDescontoPercentual.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editDescontoPercentual.setText("");
				return false;
			}
		});
		// Executa toda vez que eh digitado alguma coisa no campo
		editDescontoPercentual.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					// Checa se o campo esta com foco
					if(editDescontoPercentual.isFocused()){
						// Checa se foi digitado
						if((editDescontoPercentual.getText().length() <= 0) || (editDescontoPercentual.getText().equals(""))){
							s = "0";
						}
						// Tira os ponto e a virgula
						s = s.toString().replace(".", "").replace(",", "");
						// Pega o desconto digitado
						descontoPercentual = Double.parseDouble(s.toString());
						// Calcula o total liquido
						totalLiquido = totalBrutoAuxiliar - (totalBrutoAuxiliar * (descontoPercentual / 100));
						
						// Instancia a classe para pegar funcoes personalizadas
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
						// Seta o valor do campo total liquido com o valor calculao com o desconto
						editTotalLiquido.setText(funcoes.arredondarValor(totalLiquido));
						
					}
				} catch (Exception e) {
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
					mensagem.put("mensagem", "Erro grave ao executar o addTextChangedListener do editDescontoPercentual. \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com o respons�vel de T.I.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));
					// Montra a mensagem
					funcoes.menssagem(mensagem);
				} // Fim catch
			} // onTextChanged
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		}); // addTextChangedListener
		
		
		editTotalLiquido.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editTotalLiquido.setText("");
				return false;
			}
		});
		editTotalLiquido.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				// Instancia a classe da mensagem
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
				
				try {
					if(editTotalLiquido.isFocused()){
						// Checa se foi digitado
						if((editTotalLiquido.getText().length() <= 0) || (editTotalLiquido.getText().equals(""))){
							s = "0";
						}
						// Formata o valor digitado
						s = funcoes.arredondarValor(s.toString());
						
						// Tira os ponto e a virgula
						s = s.toString().replace(".", "").replace(",", "");
						
						s = String.valueOf(Double.parseDouble(s.toString()) / 1000);
						
						totalLiquido = Double.parseDouble(s.toString());
						// Calcula o percentual de desconto utilizado
						descontoPercentual = (((totalLiquido / totalBrutoAuxiliar) * 100) - 100) * -1;
						
						editDescontoPercentual.setText(funcoes.arredondarValor(descontoPercentual));
					}
					
				} catch (Exception e) {
					
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
					mensagem.put("mensagem", "Erro grave ao executar o addTextChangedListener do editTotalLiquido. \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com o respons�vel de T.I.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));
					// Montra a mensagem
					funcoes.menssagem(mensagem);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		
	} // Fim onCreate
	
	
	@Override
	protected void onResume() {
		super.onResume();

		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoPlanoPagamentoActivity.this);

		// Pega o id do tipo do documento do orcamento
		idTipoDocumento = orcamentoRotinas.idTipoDocumentoOrcamento(textCodigoOrcamento.getText().toString());
		
		// Pega o id do plano de pagamento
		idPlanoPagamento = orcamentoRotinas.idPlanoPagamentoOrcamento(textCodigoOrcamento.getText().toString());
		
		// Checa se o orcamento tem algum tipo de documento salvo
		if(idTipoDocumento > 0){
			// Passa pela lista de tipo de documento
			for(int i = 0; i < adapterTipoDocumento.getListaTipoDocumento().size(); i++){
				// Checa se o tipo de documento da lista eh o mesmo do orcamento
				if(adapterTipoDocumento.getListaTipoDocumento().get(i).getIdTipoDocumento() == idTipoDocumento){
					// Posiciona a lista no documento que ja tem no orcamento
					spinnerTipoDocumento.setSelection(i);
					i = adapterTipoDocumento.getListaTipoDocumento().size();
				}
			}
		}
		
		
		// Checa se o orcamento tem algum tipo de documento salvo
		if(idPlanoPagamento > 0){
			// Passa pela lista de tipo de documento
			for(int i = 0; i < adapterPlanoPagamento.getListaPlanoPagamento().size(); i++){
				// Checa se o tipo de documento da lista eh o mesmo do orcamento
				if(adapterPlanoPagamento.getListaPlanoPagamento().get(i).getIdPlanoPagamento() == idPlanoPagamento){
					// Posiciona a lista no documento que ja tem no orcamento
					spinnerPlanoPagamento.setSelection(i);
					i = adapterPlanoPagamento.getListaPlanoPagamento().size();
				}
			}
		}
		// Pega o status do orcamento
		this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());
		
	} // Fim onResume
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.orcamento_plano_pagamento, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			
			//salvarDesconto();
			finish();
			break;
			
		case R.id.menu_orcamento_plano_pagamento_salvar:
			// Checa se eh um orcamento
			if (this.tipoOrcamentoPedido.equals("O")){
				
				salvarDesconto();
			
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", "N�o � um or�amento. \n"
						   + "N�o pode ser inserido/alterado plano de pagamento.");
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			
			break;
		
		default:
			break;
		}
		return true;
	} // Fim do onOptionsItemSelected
	
	
	private void salvarDesconto(){
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoPlanoPagamentoActivity.this);
		
		if(orcamentoRotinas.distribuiDescontoItemOrcamento(textCodigoOrcamento.getText().toString(), (totalBrutoAuxiliar - totalLiquido))){
			
			ContentValues valoresOrcamento = new ContentValues();
			valoresOrcamento.put("ID_CFATPDOC", adapterTipoDocumento.getListaTipoDocumento().get(spinnerTipoDocumento.getSelectedItemPosition()).getIdTipoDocumento());
			// Salva o tipo de documento no orcamento
			orcamentoRotinas.updateOrcamento(valoresOrcamento, textCodigoOrcamento.getText().toString());
			
			ContentValues valoresItemOrcamento = new ContentValues();
			valoresItemOrcamento.put("ID_AEAPLPGT", adapterPlanoPagamento.getListaPlanoPagamento().get(spinnerTipoDocumento.getSelectedItemPosition()).getIdPlanoPagamento());
			// Salva o plano de pagamento nos itens do orcamento
			orcamentoRotinas.updatePlanoPagamentoItemOrcamento(valoresItemOrcamento, textCodigoOrcamento.getText().toString());
			
			finish();
			
		} else {
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "N�o foi poss�vel salvar os desconto e o plano de pagamento");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoPlanoPagamentoActivity.this);
			funcoes.menssagem(mensagem);
			
			finish();
		}
	}

	
	/**
	 * 
	 */
	private void recuperaCamposTela() {
		textCodigoOrcamento = (TextView) findViewById(R.id.activity_orcamento_plano_pagamento_text_codigo_orcamento);
		textTotalBruto = (TextView) findViewById(R.id.activity_orcamento_plano_pagamento_text_total_bruto);
		textTotalLiquido = (TextView) findViewById(R.id.activity_orcamento_plano_pagamento_text_total_liquido);
		textAtacadoVarejo = (TextView) findViewById(R.id.activity_orcamento_plano_pagamento_text_atacado_varejo);
		editDescontoPercentual = (EditText) findViewById(R.id.activity_orcamento_plano_pagamento_edit_desconto);
		editTotalLiquido = (EditText) findViewById(R.id.activity_orcamento_plano_pagamento_edit_total_liquido);
		spinnerTipoDocumento = (Spinner) findViewById(R.id.activity_orcamento_plano_pagamento_spinner_tipo_documento);
		spinnerPlanoPagamento = (Spinner) findViewById(R.id.activity_orcamento_plano_pagamento_spinner_plano_pagamento);
	}

}
