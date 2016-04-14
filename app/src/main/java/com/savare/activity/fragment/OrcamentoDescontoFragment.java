package com.savare.activity.fragment;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

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
import android.widget.TextView;
@Deprecated
public class OrcamentoDescontoFragment extends Fragment {
	
	View viewOrcamentoDesconto;
	private TextView textCodigoOrcamento,
					 textTotalBruto,
					 textValorDesconto,
					 textAtacadoVarejo;
	private EditText editDescontoPercentual,
	 				 editTotalLiquido;
	//private String codigoPessoa,
	//   			   razaoSocial;
	private String tipoOrcamentoPedido;
	private double totalBruto = 0,
			   	   totalLiquidoAuxiliar = 0,
			   	   descontoPercentual = 0,
			   	   totalBrutoAuxiliar;
	private int idTipoDocumento,
				idPlanoPagamento;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewOrcamentoDesconto = inflater.inflate(R.layout.fragment_orcamento_desconto, container, false);
		
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle parametro = getArguments();

		if (parametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			textCodigoOrcamento.setText(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO));
			//codigoPessoa = parametro.getString("ID_CFACLIFO");
			//razaoSocial = parametro.getString("NOME_RAZAO");
			if(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("0")){
				textAtacadoVarejo.setText("Atacado");
			
			}else if (parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("1")){
				textAtacadoVarejo.setText("Varejo");
			}
			
			/*// Instancia a classe de rotinas
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			
			// Pega o total bruto do orcamento
			textTotalBruto.setText(orcamentoRotinas.totalOrcamentoBruto(textCodigoOrcamento.getText().toString()));
			
			// Pega o total liquido do orcamento
			editTotalLiquido.setText(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
			
			// Pega o percentual de desconto aplicado no total do orcamento
			editDescontoPercentual.setText(orcamentoRotinas.descontoPercentualOrcamento(textCodigoOrcamento.getText().toString()));
			
			// Move o cursor para o final do campo
			editDescontoPercentual.setSelection(editDescontoPercentual.getText().length());
			
			// Pega o valor total bruto do orcamento
			totalBruto = funcoes.desformatarValor(textTotalBruto.getText().toString()); // Double.parseDouble(textTotalBruto.getText().toString().replace(".", "").replace(",", "")) / 1000;
			totalBrutoAuxiliar = totalBruto;
			
			// Pega o percentual de desconto concedido
			descontoPercentualOrcamento = funcoes.desformatarValor(editDescontoPercentual.getText().toString()); // Double.parseDouble(editDescontoPercentual.getText().toString().replace(".", "").replace(",", "")) / 1000;
			
			// Pega o valor total liquido
			totalLiquidoAuxiliar = funcoes.desformatarValor(editTotalLiquido.getText().toString()); // Double.parseDouble(editTotalLiquido.getText().toString().replace(".", "").replace(",", "")) / 1000;*/
			
		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "Não foi carregar os dados do orçamento.\n");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
		}
		
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
							   + getActivity().getResources().getString(R.string.favor_entrar_contato_responsavel_ti));
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
				try {
					if(editTotalLiquido.isFocused()){
						// Calcula os totais de acordo com o digitado
						calculaTodosCampos(editTotalLiquido.getId());
					}
					
				} catch (Exception e) {
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoDescontoActivity");
					mensagem.put("mensagem", "Erro grave ao executar o addTextChangedListener do editTotalLiquido. \n"
										     + e.getMessage() +"\n"
										     + getActivity().getResources().getString(R.string.favor_entrar_contato_responsavel_ti));
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
		
		return viewOrcamentoDesconto;
	}

	
	@Override
	public void onResume() {
		super.onResume();

		// Instancia a classe de rotinas
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

		// Pega o valor total bruto do orcamento
		totalBruto = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoBruto(textCodigoOrcamento.getText().toString()));
		totalBrutoAuxiliar = totalBruto;

		// Mostra o total bruto na tela
		textTotalBruto.setText(funcoes.arredondarValor(totalBruto));

		// Pega o percentual de desconto aplicado no total do orcamento
		descontoPercentual = funcoes.desformatarValor(orcamentoRotinas.descontoPercentualOrcamento(textCodigoOrcamento.getText().toString()));

		// Mostra o percentual de desconto na tela
		editDescontoPercentual.setText(funcoes.arredondarValor(descontoPercentual));

		// Move o cursor para o final do campo
		editDescontoPercentual.setSelection(editDescontoPercentual.getText().length());

		// Pega o valor total liquido
		totalLiquidoAuxiliar = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));

		// Montra o total liquido na tela
		editTotalLiquido.setText(funcoes.arredondarValor(totalLiquidoAuxiliar));

		// Pega o status do orcamento
		this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());
	} // Fim onResume
	
	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//super.onCreateOptionsMenu(menu);
		inflater.inflate(R.menu.orcamento_desconto_fragment, menu);
		
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
				mensagem.put("tela", "OrcamentoDescontoFragment");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n" +
										 getActivity().getResources().getString(R.string.nao_possivel_inserir_alterar_orcamento));
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
		
		if(orcamentoRotinas.distribuiDescontoItemOrcamento(textCodigoOrcamento.getText().toString(), totalLiquidoAuxiliar, totalBrutoAuxiliar)){

			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "Desconto Distribuido com sucesso.");

			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
			// Executa o onResume (todos os campos de valores)
			onResume();
			
		} else {
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
			mensagem.put("mensagem", "Não foi possível salvar os desconto e o plano de pagamento");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
			
		}
	}

	
	/**
	 * Acossia os campos da view com as variaves criadas para manipula-las.
	 * 
	 */
	private void recuperaCamposTela() {
		textCodigoOrcamento = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_codigo_orcamento);
		textTotalBruto = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_total_bruto);
		textValorDesconto = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_valor_desconto);
		//textTotalLiquido = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_total_liquido);
		textAtacadoVarejo = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_text_atacado_varejo);
		editDescontoPercentual = (EditText) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_edit_desconto);
		editTotalLiquido = (EditText) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_plano_pagamento_edit_total_liquido);
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

			// Armazena o total liquido em uma vareavel auxiliar
			this.totalLiquidoAuxiliar = totalLiquido;

			// Seta o valor do campo total liquido com o valor calculao com o desconto
			editTotalLiquido.setText(funcoes.arredondarValor(totalLiquido));
			textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
		}
		
		// Checa se o campo que chomou esta funcao foi o campo editTotalLiquido
		if(campoChamada == editTotalLiquido.getId()){
			// Calcula o percentual de desconto utilizado
			percentualDesconto = (((totalLiquido / totalBrutoAuxiliar) * 100) - 100) * -1;

			this.totalLiquidoAuxiliar = totalLiquido;
			
			editDescontoPercentual.setText(funcoes.arredondarValor(percentualDesconto));
			textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
		}
		
	}

}
