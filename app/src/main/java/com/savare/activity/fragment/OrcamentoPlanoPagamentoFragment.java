package com.savare.activity.fragment;

import com.savare.R;
import com.savare.activity.OrcamentoPlanoPagamentoActivity;
import com.savare.activity.OrcamentoProdutoDetalhesActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.TipoDocumentoRotinas;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class OrcamentoPlanoPagamentoFragment extends Fragment {
	
	View viewOrcamentoPlanoPgto;
	private TextView textCodigoOrcamento,
					 textTotalBruto,
					 textValorDesconto,
					 textAtacadoVarejo;
	private EditText editDescontoPercentual,
	 				 editTotalLiquido;
	private Spinner spinnerTipoDocumento,
					spinnerPlanoPagamento;
	//private String codigoPessoa,
	//   			   razaoSocial;
	private String tipoOrcamentoPedido;
	private double totalBruto = 0,
			   	   totalLiquido = 0,
			   	   descontoPercentual = 0,
			   	   totalBrutoAuxiliar;
	private int idTipoDocumento,
				idPlanoPagamento;
	private ItemUniversalAdapter adapterTipoDocumento,
								 adapterPlanoPagamento;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewOrcamentoPlanoPgto = inflater.inflate(R.layout.fragment_orcamento_plano_pagamento, container, false);
		
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle parametro = getArguments();
		
		if (parametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			textCodigoOrcamento.setText(parametro.getString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO));
			//codigoPessoa = parametro.getString("ID_CFACLIFO");
			//razaoSocial = parametro.getString("NOME_RAZAO");
			if(parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO).equals("0")){
				textAtacadoVarejo.setText("Atacado");
			
			}else if (parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO).equals("1")){
				textAtacadoVarejo.setText("Varejo");
			}
			
			// Instancia a classe de rotinas
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			
			// Pega o total bruto do orcamento
			textTotalBruto.setText(orcamentoRotinas.totalOrcamentoBruto(textCodigoOrcamento.getText().toString()));
			
			// Pega o total liquido do orcamento
			editTotalLiquido.setText(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
			
			// Pega o percentual de desconto aplicado no total do orcamento
			editDescontoPercentual.setText(orcamentoRotinas.descontoPercentual(textCodigoOrcamento.getText().toString()));
			
			// Move o cursor para o final do campo
			editDescontoPercentual.setSelection(editDescontoPercentual.getText().length());
			
			// Pega o valor total bruto do orcamento
			totalBruto = funcoes.desformatarValor(textTotalBruto.getText().toString()); // Double.parseDouble(textTotalBruto.getText().toString().replace(".", "").replace(",", "")) / 1000;
			totalBrutoAuxiliar = totalBruto;
			
			// Pega o percentual de desconto concedido
			descontoPercentual = funcoes.desformatarValor(editDescontoPercentual.getText().toString()); // Double.parseDouble(editDescontoPercentual.getText().toString().replace(".", "").replace(",", "")) / 1000;
			
			// Pega o valor total liquido
			totalLiquido = funcoes.desformatarValor(editTotalLiquido.getText().toString()); // Double.parseDouble(editTotalLiquido.getText().toString().replace(".", "").replace(",", "")) / 1000;
			
		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "Não foi carregar os dados do orçamento.\n");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
		}
		
		// Intancia a classe de rotinas do tipo de documento
		TipoDocumentoRotinas tipoDocumentoRotinas = new TipoDocumentoRotinas(getActivity());
		// Intancia a classe do adapter
		adapterTipoDocumento = new ItemUniversalAdapter(getActivity(), 3);
		// Preenche o adapter com uma lista
		adapterTipoDocumento.setListaTipoDocumento(tipoDocumentoRotinas.listaTipoDocumento(null));
		// Preenche o spinner com um adapter personalizado
		spinnerTipoDocumento.setAdapter(adapterTipoDocumento);
		
		
		//Instancia a classe de rotina do plano de pagamento
		PlanoPagamentoRotinas planoPagamentoRotinas = new PlanoPagamentoRotinas(getActivity());
		// Intancia a classe do adapter
		adapterPlanoPagamento = new ItemUniversalAdapter(getActivity(), 4);
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
						
						calculaTodosCampos(editDescontoPercentual.getId());
						
					}
				} catch (Exception e) {
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
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
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
				
				try {
					if(editTotalLiquido.isFocused()){
						
						calculaTodosCampos(editTotalLiquido.getId());
						
						/*// Checa se foi digitado
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
						
						editDescontoPercentual.setText(funcoes.arredondarValor(descontoPercentual));*/
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
		// Ativa a opcao de menus para este fragment
		setHasOptionsMenu(true);
		
		return viewOrcamentoPlanoPgto;
	}

	
	@Override
	public void onResume() {
		super.onResume();
		
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//super.onCreateOptionsMenu(menu);
		inflater.inflate(R.menu.orcamento_plano_pagamento_fragment, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		case R.id.menu_orcamento_plano_pagamento_fragment_salvar:
			// Checa se eh um orcamento
			if (this.tipoOrcamentoPedido.equals("O")){
				
				salvarDesconto();
			
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
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
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
		
		if(orcamentoRotinas.distribuiDescontoItemOrcamento(textCodigoOrcamento.getText().toString(), (totalBrutoAuxiliar - totalLiquido))){
			
			ContentValues valoresOrcamento = new ContentValues();
			valoresOrcamento.put("ID_CFATPDOC", adapterTipoDocumento.getListaTipoDocumento().get(spinnerTipoDocumento.getSelectedItemPosition()).getIdTipoDocumento());
			// Salva o tipo de documento no orcamento
			orcamentoRotinas.updateOrcamento(valoresOrcamento, textCodigoOrcamento.getText().toString());
			
			ContentValues valoresItemOrcamento = new ContentValues();
			valoresItemOrcamento.put("ID_AEAPLPGT", adapterPlanoPagamento.getListaPlanoPagamento().get(spinnerTipoDocumento.getSelectedItemPosition()).getIdPlanoPagamento());
			// Salva o plano de pagamento nos itens do orcamento
			orcamentoRotinas.updatePlanoPagamentoItemOrcamento(valoresItemOrcamento, textCodigoOrcamento.getText().toString());
			
		} else {
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "N�o foi poss�vel salvar os desconto e o plano de pagamento");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
			
		}
	}

	
	/**
	 * Acossia os campos da view com as variaves criadas para manipula-las.
	 * 
	 */
	private void recuperaCamposTela() {
		textCodigoOrcamento = (TextView) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_codigo_orcamento);
		textTotalBruto = (TextView) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_total_bruto);
		textValorDesconto = (TextView) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_valor_desconto);
		//textTotalLiquido = (TextView) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_total_liquido);
		textAtacadoVarejo = (TextView) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_atacado_varejo);
		editDescontoPercentual = (EditText) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_edit_desconto);
		editTotalLiquido = (EditText) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_edit_total_liquido);
		spinnerTipoDocumento = (Spinner) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_spinner_tipo_documento);
		spinnerPlanoPagamento = (Spinner) viewOrcamentoPlanoPgto.findViewById(R.id.fragment_orcamento_plano_pagamento_spinner_plano_pagamento);
	}
	
	
	private void calculaTodosCampos(int campoChamada){
		double percentualDesconto = 0,
			   totalLiquido = 0;
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
		
		// Checa se tem algum valor no percentual de desconto
		if((editDescontoPercentual != null) && (editDescontoPercentual.getText().length() > 0)){
			percentualDesconto = funcoes.desformatarValor(editDescontoPercentual.getText().toString());
		}
		
		// Checa se tem algum valor no campo de total liquido
		if((editTotalLiquido != null) && (editTotalLiquido.getText().length() > 0)){
			totalLiquido = funcoes.desformatarValor(editDescontoPercentual.getText().toString());
		}
		
		// Checa se o campo que chamou esta funcao foi o campo editDescontoPercentual
		if(campoChamada == editDescontoPercentual.getId()){
			// Calcula o total liquido
			totalLiquido = totalBrutoAuxiliar - (totalBrutoAuxiliar * (percentualDesconto / 100));
			
			// Seta o valor do campo total liquido com o valor calculao com o desconto
			editTotalLiquido.setText(funcoes.arredondarValor(totalLiquido));
			textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
		}
		
		// Checa se o campo que chomou esta funcao foi o campo editTotalLiquido
		if(campoChamada == editTotalLiquido.getId()){
			// Calcula o percentual de desconto utilizado
			percentualDesconto = (((totalLiquido / totalBrutoAuxiliar) * 100) - 100) * -1;
			
			editDescontoPercentual.setText(funcoes.arredondarValor(percentualDesconto));
			textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
		}
		
	}

}
