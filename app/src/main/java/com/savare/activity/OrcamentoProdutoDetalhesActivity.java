package com.savare.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.adapter.DescricaoDuplaAdapter;
import com.savare.adapter.DescricaoSimplesAdapter;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.DescricaoSimplesBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.EstoqueRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;

public class OrcamentoProdutoDetalhesActivity extends Activity {
	
	private TextView textDescricaoProduto, 
					 textUltimoPreco, 
					 textEstoque,
					 textDescontoMaximo,
					 textSequencial,
					 textCodigoUnico;
	private Spinner spinnerEmbalagem,
					spinnerPlanoPagamentoPreco,
					spinnerEstoque;
	private EditText editQuantidade,
					 editUnitarioLiquidoVenda,
					 editDesconto,
					 editValorDesconto,
					 editTotal,
					 editObservacao;

	private ActionBar actionBar;
	private ItemUniversalAdapter adapterEmbalagem;
	private ItemUniversalAdapter adapterPlanoPagamentoPreco;
	private ItemUniversalAdapter adapterEstoque;
	private double valorUnitarioTabela,
				   valorUnitarioVenda,
				   valorUnitarioVendaAux;
	private List<PlanoPagamentoBeans> listaPlanoPagamentoPreco;
	private OrcamentoBeans orcamento;
	private ProdutoListaBeans produto;
	private long idItemOrcamento = 0;
	private String telaChamada = "",
				   idProduto,
				   idOrcamento,
				   idPessoa,
				   razaoSocial;
	private boolean telaCarregada = false;

	//TODO: Metodo onCreate da Activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orcamento_produto_detalhes);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
		funcoes.bloqueiaOrientacaoTela();
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		
		// Ativa a action bar com o simbolo de voltar
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		recuperaCampos();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		try{
			// Pega os dados passado por paramentro
			idProduto = getIntent().getExtras().getString("ID_AEAPRODU");
			idOrcamento = getIntent().getExtras().getString("ID_AEAORCAM");
			idPessoa = getIntent().getExtras().getString("ID_CFACLIFO");
			telaChamada = getIntent().getExtras().getString(OrcamentoFragment.KEY_TELA_CHAMADA);
			razaoSocial = getIntent().getExtras().getString("RAZAO_SOCIAL");
			/*//Pega os dados do produto da outra activity
			telaChamada = getIntent().getExtras().getString("TELA_CHAMADA");
			orcamento = (OrcamentoBeans) getIntent().getParcelableExtra("AEAORCAM");
			idProduto = getIntent().getExtras().getString("ID_AEAPRODU");*/

			// Checa se foi passado algum id de produto
			if (idProduto != null){
				ProdutoRotinas produtoRotinas = new ProdutoRotinas(OrcamentoProdutoDetalhesActivity.this);
				// Checa se passou algum numero de orcamento
				if (idOrcamento != null) {

					produto = produtoRotinas.listaProduto("AEAPRODU.ID_AEAPRODU = " + idProduto, null, idOrcamento).get(0);

					orcamento = new OrcamentoBeans();
					// Pega os dados do orcamento
					orcamento = preencheDadosOrcamento();

				} else {
					// Pega lista sem associar com um orcamento
					produto = produtoRotinas.listaProduto("AEAPRODU.ID_AEAPRODU = " + idProduto, null, null).get(0);
				}
			}
			// Pega se a venda eh no atacado ou varejo
			produto.setAtacadoVarejo(getIntent().getExtras().getString("ATAC_VARE").charAt(0));

			// Checa se as variaveis nao estao vazias
			if ((produto != null) && (produto.getProduto() != null) && (orcamento != null)) {
				
				carregarDadosDoProduto(produto, orcamento);
				
				// Cria uma variavel para armazenar a lista de embalagens
				/*List<DescricaoSimplesBeans> listaEmbalagem = new ArrayList<DescricaoSimplesBeans>();
				// Cria um laco para passar por todas as embalagens
				for (int i = 0; i < produto.getProduto().getListaEmbalagem().size(); i++) {
					
					DescricaoSimplesBeans descricaoEmbalagem = new DescricaoSimplesBeans();
					descricaoEmbalagem.setTextoPrincipal(produto.getProduto().getListaEmbalagem().get(i).getDescricaoEmbalagem() + " | " + 
														 produto.getProduto().getListaEmbalagem().get(i).getUnidadeVendaEmbalagem().getSiglaUnidadeVenda());
					// Adiciona a embalagem na lista
					listaEmbalagem.add(descricaoEmbalagem);
				}*/
				// Preenche o adapter de embalagem com uma lista
				adapterEmbalagem = new ItemUniversalAdapter(OrcamentoProdutoDetalhesActivity.this, ItemUniversalAdapter.EMBALAGEM);
				adapterEmbalagem.setListaEmbalagem(produto.getProduto().getListaEmbalagem());
				
				// Preenche o spinner com o adapter
				spinnerEmbalagem.setAdapter(adapterEmbalagem);
				
			} else {
				// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 1);
				mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
				mensagem.put("mensagem", "Não foi possível carregar os detalhes do produto\n"
						   + "Favor, voltar e selecione novamente um produto");
				
				funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
				funcoes.menssagem(mensagem);
			}
		} catch(Exception e){
			funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 0);
			mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
			mensagem.put("mensagem", "Erro ao pegar os dados do produto da outra tela (activity). \n" 
					   + "Erro: " + e.getMessage() + "\n"
					   + "Favor, voltar e selecione novamente um produto");
			mensagem.put("dados", getIntent().getParcelableExtra("AEAPLOJA").toString());
			mensagem.put("usuario", funcoes.getValorXml("Usuario"));
			mensagem.put("usuario", funcoes.getValorXml("ChaveEmpresa"));
			mensagem.put("usuario", funcoes.getValorXml("Email"));
			
			funcoes.menssagem(mensagem);
		} // Fim do catch
		

		editQuantidade.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					if(editQuantidade.isFocused()){
						
						calculaTodosCampos(editQuantidade.getId());
						
					}
				}catch(Exception e){
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Erro grave no campo Quantidade (addTextChangedListener editQuantidade). \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com a T.I.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));

					
					funcoes.menssagem(mensagem);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		}); // Fim do editQuantidade
		
		
		editUnitarioLiquidoVenda.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editUnitarioLiquidoVenda.setText("");
				return false;
			}
		});
		editUnitarioLiquidoVenda.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					if(editUnitarioLiquidoVenda.isFocused()){
						
						calculaTodosCampos(editUnitarioLiquidoVenda.getId());
						
					}
				}catch(Exception e){
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Erro grave no campo Unitário (addTextChangedListener editUnitarioLiquido). \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com a T.I.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));

					
					funcoes.menssagem(mensagem);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		}); // Fim do editUnitarioLiquidoVenda
		
		
		editDesconto.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editDesconto.setText("");
				return false;
			}
		});
		editDesconto.addTextChangedListener( new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					if(editDesconto.isFocused()){
						
						calculaTodosCampos(editDesconto.getId());
						
					}
				}catch(Exception e){
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Erro no campo desconto (addTextChangedListener editDesconto). \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com a TI.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));

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
		
		
		editValorDesconto.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editValorDesconto.setText("");
				return false;
			}
		});
		
		editValorDesconto.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if(editValorDesconto.isFocused()){
						calculaTodosCampos(editValorDesconto.getId());
					}
				} catch (Exception e) {
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Erro no campo valor de desconto (addTextChangedListener editValorDesconto). \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com a TI.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(mensagem);
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		editTotal.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editTotal.setText("");
				return false;
			}
		});
		editTotal.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					if(editTotal.isFocused()){
						
						calculaTodosCampos(editTotal.getId());
						
					}
				}catch(Exception e){
					// Instancia a classe da mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Erro grave no campo Total (addTextChangedListener editTotal). \n"
							   + e.getMessage() +"\n"
							   + "Favor, entrar em contato com a TI.");
					mensagem.put("dados", e.getMessage());
					mensagem.put("usuario", funcoes.getValorXml("Usuario"));
					mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					mensagem.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(mensagem);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		}); // Fim editTotal
		
		
		spinnerPlanoPagamentoPreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Muda os valores dos campos de acordo com o selecionado
				valorUnitarioVendaAux = adapterPlanoPagamentoPreco.getListaPlanoPagamento().get(position).getPrecoProduto();
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
				
				if((produto.getEstaNoOrcamento() != '1') || (telaCarregada == true)){
					editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
				}
				calculaTodosCampos(spinnerPlanoPagamentoPreco.getId());
				telaCarregada = true;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
	} // Fim onCreate
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Carrega os dados do estoque
		carregarDadosEstoque(""+produto.getProduto().getIdProduto());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.orcamento_produto_detalhes, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			
			if ((telaChamada != null) && (telaChamada.equalsIgnoreCase("ProdutoListaActivity"))){
				setResult(101);
			} else {
				setResult(RESULT_CANCELED);
			}
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
			funcoes.desbloqueiaOrientacaoTela();
			
			finish();
			break;
			
		case R.id.menu_orcamento_produto_detalhes_salvar:
			//Testar aqui
			// Checa se os campos estao vazios
			if((editQuantidade != null) && (!editQuantidade.getText().equals(""))){
				funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
				
				// Checa os dados informado
				if(validarDados()){
					// Calcula os valores necessario para salvar no banco de dados
					double quantidade = funcoes.desformatarValor(editQuantidade.getText().toString()), //Double.parseDouble(editQuantidade.getText().toString()),
						   vlCusto = (this.produto.getCustoCompleto() * quantidade),
						   vlBruto = (this.valorUnitarioVendaAux * quantidade),
						   totalDigitadoLiquido = funcoes.desformatarValor(editTotal.getText().toString()),
						   vlDesconto = vlBruto - totalDigitadoLiquido,
						   fcCustoUn = vlCusto / quantidade, 
						   fcBrutoUn = vlBruto / quantidade, 
						   fcDescontoUn = vlDesconto / quantidade, 
						   fcLiquido = totalDigitadoLiquido;

					//Pega os dados do produto
					ContentValues produto = new ContentValues();
					produto.put("ID_AEAORCAM", this.orcamento.getIdOrcamento());
					produto.put("ID_AEAESTOQ", adapterEstoque.getListaEstoque().get(spinnerEstoque.getSelectedItemPosition()).getIdEstoque());
					produto.put("ID_AEAPLPGT", this.listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()).getIdPlanoPagamento());
					produto.put("ID_AEAUNVEN", this.produto.getProduto().getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getUnidadeVendaEmbalagem().getIdUnidadeVenda());
					produto.put("ID_CFACLIFO_VENDEDOR", funcoes.getValorXml("CodigoUsuario"));
					produto.put("ID_AEAPRODU", this.produto.getProduto().getIdProduto());
					produto.put("QUANTIDADE", quantidade);
					produto.put("VL_CUSTO", vlCusto);
					produto.put("VL_BRUTO", vlBruto);
					if(this.orcamento.getTipoVenda() == '0'){
						produto.put("VL_TABELA", this.produto.getValorTabelaAtacado() * quantidade);
						produto.put("VL_TABELA_UN", this.produto.getValorTabelaAtacado());
					} else {
						produto.put("VL_TABELA", this.produto.getValorTabelaVarejo() * quantidade);
						produto.put("VL_TABELA_UN", this.produto.getValorTabelaVarejo());
					}
					produto.put("VL_DESCONTO", vlDesconto);
					produto.put("FC_DESCONTO_UN", (vlDesconto / quantidade));
					produto.put("FC_CUSTO_UN", fcCustoUn);
					produto.put("FC_BRUTO_UN", fcBrutoUn);
					produto.put("FC_DESCONTO_UN", fcDescontoUn);
					produto.put("FC_LIQUIDO", fcLiquido);
					produto.put("FC_LIQUIDO_UN", (fcLiquido / quantidade));
					produto.put("COMPLEMENTO", editObservacao.getText().toString());
					produto.put("TIPO_PRODUTO", String.valueOf(this.produto.getProduto().getTipoProduto()));
					// Instancia classe para manipular o orcamento
					OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoProdutoDetalhesActivity.this);
					
		
					// Verifica se o produto ja esta no orcamento
					if(this.produto.getEstaNoOrcamento() == '1'){
						
						// Verifica se atualizou com sucesso
						if(orcamentoRotinas.updateItemOrcamento(produto, String.valueOf(this.idItemOrcamento)) > 0){
							funcoes.desbloqueiaOrientacaoTela();
							// Fecha a tela de detalhes de produto
							finish();
						}
					// Envia os dados do produto para inserir no banco de dados
					} else{
						// Salva a proxima sequencia do item
						produto.put("SEQUENCIA", orcamentoRotinas.proximoSequencial(String.valueOf(this.orcamento.getIdOrcamento())));
						produto.put("GUID", orcamentoRotinas.gerarGuid());
						
						if((this.idItemOrcamento = orcamentoRotinas.insertItemOrcamento(produto)) > 0){
							// Cria uma intent para returnar um valor para activity ProdutoLista
							Intent returnIntent = new Intent();
							returnIntent.putExtra("RESULTADO", '1');
							// Pega a posicao do deste produto na lista de produtos
							returnIntent.putExtra("POSICAO", getIntent().getExtras().getInt("POSICAO"));
							returnIntent.putExtra("ID_AEAITORC", this.idItemOrcamento);
							
							// Checa se se quem chemou foi a tela de lista de de orçamento sem associacao de orcamento
							if ( (telaChamada != null) && (telaChamada.equalsIgnoreCase("ProdutoListaActivity")) ){
								setResult(101, returnIntent);
							} else {
								setResult(RESULT_OK, returnIntent);
							}
							
							funcoes.desbloqueiaOrientacaoTela();
							// Fecha a tela de detalhes de produto
							finish();
						}
					}
				} else {
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 2);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Verifique se os campos obrigatorios estão preenchidos.\n");
					
					funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					funcoes.menssagem(mensagem);
				}
			}
			break;

		default:
			break;
		}
		return true;
	} // Fim do onOptionsItemSelected
	
	
	/**
	 * Acossia a variaveis com os campos para poder manipular os campos
	 * na view (activity).
	 */
	private void recuperaCampos(){
		textDescricaoProduto = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_descricao_produto);
		textUltimoPreco = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_ultimo_preco);
		textEstoque = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_estoque);
		textDescontoMaximo = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_desconto_maximo);
		textSequencial = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_sequencial);
		textCodigoUnico = (TextView) findViewById(R.id.activity_orcamento_produto_detalhes_text_codigo_unico);
		spinnerEmbalagem = (Spinner) findViewById(R.id.activity_orcamento_produto_detalhes_spinner_embalagem);
		spinnerPlanoPagamentoPreco = (Spinner) findViewById(R.id.activity_orcamento_produto_detalhes_spinner_unitario_tabela);
		spinnerEstoque = (Spinner) findViewById(R.id.activity_orcamento_produto_detalhes_spinner_estoque);
		editQuantidade = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_quantidade);
		editUnitarioLiquidoVenda = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_unitario_liquido);
		editDesconto = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_desconto);
		editValorDesconto = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_valor_desconto);
		editTotal = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_total);
		editObservacao = (EditText) findViewById(R.id.activity_orcamento_produto_detalhes_edit_observacao);
	} // Fim recuperaCampos
	
	
	/**
	 * Funcao para carregar os dados do produto em seu devidos campos.
	 * 
	 * @param produtoVenda
	 */
	private void carregarDadosDoProduto(ProdutoListaBeans produtoVenda, OrcamentoBeans orcamento){
		// Preenche o titulo da action bar
		this.actionBar.setTitle(orcamento.getIdOrcamento() + " - " + orcamento.getNomeRazao());
		// Preenche o campos da descricao do produtos
		textDescricaoProduto.setText(produtoVenda.getProduto().getCodigoEstrutural() + " - " + produtoVenda.getProduto().getDescricaoProduto() + " - " + produtoVenda.getProduto().getDescricaoMarca());
		// Instanci classe de funcoes
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
		
		textEstoque.setText(funcoes.arredondarValor(String.valueOf(produtoVenda.getEstoqueFisico())));
		
		// Verifica se o estoque eh menor que zero
		if(produtoVenda.getEstoqueFisico() < 1){
			textEstoque.setTextColor(getResources().getColor(R.color.vermelho_escuro));
		}
		
		// Verifica se a venda eh do atacado
		if(String.valueOf(produtoVenda.getAtacadoVarejo()).equals("0")){
			
			// Armazena o valor final do produto de acordo com o plano de pagamento selecionado
			valorUnitarioVendaAux = carregarDadosPlanoPagamento(produtoVenda.getValorUnitarioAtacado(), produtoVenda.getValorPromocaoAtacado(), '0');


			// Preence o campo com o valor do produto
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
			
			// Verifica se tem preco de promocao
			if(produtoVenda.getValorPromocaoAtacado() > 0){
				// Muda a cor do fundo para destacar que eh promocao
				spinnerPlanoPagamentoPreco.setBackgroundColor(getResources().getColor(R.color.amarelo)); 
			}
			
			// Verifica se a venda eh do varejo
		}else if(String.valueOf(produtoVenda.getAtacadoVarejo()).equals("1")){
			
			// Armazena o valor final do produto de acordo com o plano de pagamento selecionado
			valorUnitarioVendaAux = carregarDadosPlanoPagamento(produtoVenda.getValorUnitarioVarejo(), produtoVenda.getValorPromocaoVarejo(), '1');
			
			// Preence o campo com o valor do produto
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
			
			// Verifica se tem preco de promocao
			if(produtoVenda.getValorPromocaoVarejo() > 0){
				// Muda a cor do fundo para destacar que eh promocao
				spinnerPlanoPagamentoPreco.setBackgroundColor(getResources().getColor(R.color.amarelo));
			} 
		} // Fim do if do varejo
		
		// Verfica se o produto ja esta no orcamento
		if(produtoVenda.getEstaNoOrcamento() == '1'){
			// Instancia a classe de rotinas
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoProdutoDetalhesActivity.this);
			ItemOrcamentoBeans itemOrcamentoBeans = new ItemOrcamentoBeans();

			// Pega os dados de um determinado produto no orcamento usando o idProduto e o idOrcamento 
			itemOrcamentoBeans = orcamentoRotinas.selectItemOrcamento(String.valueOf(orcamento.getIdOrcamento()), String.valueOf(produto.getProduto().getIdProduto()));
			// Preenche o campo com a quantidade que foi comprado
			editQuantidade.setText(funcoes.arredondarValor(itemOrcamentoBeans.getQuantidade()));
			// Move o cursor para o final do campo
			editQuantidade.setSelection(editQuantidade.length());
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade()));
			editTotal.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorLiquido()));
			editObservacao.setText(itemOrcamentoBeans.getComplemento());
			editDesconto.setText(funcoes.arredondarValor(((((itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getValorBruto())*100)-100)* -1)));
			editValorDesconto.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorDesconto()));
			textSequencial.setText(""+itemOrcamentoBeans.getSeguencia());
			textCodigoUnico.setText(itemOrcamentoBeans.getGuid());
			this.idItemOrcamento = itemOrcamentoBeans.getIdItemOrcamento();
			//this.valorUnitarioVendaAux = Double.valueOf(funcoes.arredondarValor(String.valueOf((itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade()))).replace(".", "").replace(",", "."));
			this.valorUnitarioVendaAux = (itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade());
		} else {
			textSequencial.setText("");
			textCodigoUnico.setText("");
		}
		
	} // Fim do carregarDadosDoProduto
	
	
	/**
	 * Funcao para carregar os dados do plano de pagamento.
	 * Tras uma lista de plano de pagamento com o valor do produto, 
	 * com juros e/ou com desconto.
	 * 
	 * @param preco
	 * @param precoPromocao
	 * @param atacadoVarejo
	 * @return
	 */
	private double carregarDadosPlanoPagamento(double preco, double precoPromocao, char atacadoVarejo){
		// Instancia a rotinas para buscar os dados
		PlanoPagamentoRotinas planoRotinas = new PlanoPagamentoRotinas(OrcamentoProdutoDetalhesActivity.this);
		
		// Instancia a lista
		this.listaPlanoPagamentoPreco = new ArrayList<PlanoPagamentoBeans>();
		// Recebe os dados do banco
		this.listaPlanoPagamentoPreco = planoRotinas.listaPlanoPagamento("ATIVO = '1'", "DESCRICAO, CODIGO", String.valueOf(atacadoVarejo));
		
		double precoVenda = 0;
		// Checa se retornou alguma coisa para lista de pagamentos
		if((this.listaPlanoPagamentoPreco != null) && (this.listaPlanoPagamentoPreco.size() > 0)){
			// Passa por todos os planos
			for (int i = 0; i < this.listaPlanoPagamentoPreco.size(); i++) {
				
				// Checa se eh uma venda para o atacado
				if(atacadoVarejo == '0'){
					
					// Checa se tem promocao
					if(precoPromocao > 0){
						
						// Checa se aplica desconto para produtos de promocao
						if(this.listaPlanoPagamentoPreco.get(i).getDescontoPromocao() == '1'){
							// Aplica o desconto no preco ja com o juros
							precoVenda = (precoPromocao + (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getJurosAtacado() / 100))) - 
										 (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getDescontoAtacado() / 100));
						
						} else {
							// Aplica apenas o juros em cima do preco da promocao
							precoVenda = (precoPromocao + (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getJurosAtacado() / 100)));
						}
					
					} else {
						// Aplica o desconto no preco ja com o juros
						precoVenda = (preco + (preco * (this.listaPlanoPagamentoPreco.get(i).getJurosAtacado() / 100))) - 
									 (preco * (this.listaPlanoPagamentoPreco.get(i).getDescontoAtacado() / 100));
					}
					
					// Checa se eh uma venda para o varejo
				} else if(atacadoVarejo == '1'){
					// Aplica o juros no preco
					precoVenda = preco + (preco * (this.listaPlanoPagamentoPreco.get(i).getJurosVarejo() / 100));
					
					// Checa se tem promocao
					if(precoPromocao > 0){
						
						// Checa se aplica desconto para produtos de promocao
						if(this.listaPlanoPagamentoPreco.get(i).getDescontoPromocao() == '1'){
							// Aplica o desconto no preco ja com o juros
							precoVenda = (precoPromocao + (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getJurosVarejo() / 100))) - 
										 (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getDescontoVarejo() / 100));
						} else {
							// Aplica apenas o juros em cima do preco da promocao
							precoVenda = (precoPromocao + (precoPromocao * (this.listaPlanoPagamentoPreco.get(i).getJurosVarejo() / 100)));
						}
					
					} else {
						// Aplica o desconto no preco ja com o juros
						precoVenda = (preco + (preco * (this.listaPlanoPagamentoPreco.get(i).getJurosVarejo() / 100))) - 
									 (preco * (this.listaPlanoPagamentoPreco.get(i).getDescontoVarejo() / 100));
					}
				}
				
				this.listaPlanoPagamentoPreco.get(i).setPrecoProduto(precoVenda);
			}
			
			this.adapterPlanoPagamentoPreco = new ItemUniversalAdapter(OrcamentoProdutoDetalhesActivity.this, ItemUniversalAdapter.PLANO_PAGAMENTO_ORCAMENTO);
			this.adapterPlanoPagamentoPreco.setListaPlanoPagamento(this.listaPlanoPagamentoPreco);
			
			spinnerPlanoPagamentoPreco.setAdapter(adapterPlanoPagamentoPreco);
			
			spinnerPlanoPagamentoPreco.setSelection(planoRotinas.posicaoPlanoPagamentoLista(listaPlanoPagamentoPreco, String.valueOf(this.orcamento.getIdOrcamento())));
		}
		
		return precoVenda;
	} // Fim carregarDadosPlanoPagamento

	
	private void carregarDadosEstoque(String idProduto){
		// Instancia a classe de rotinas do estoque
		EstoqueRotinas estoqueRotinas = new EstoqueRotinas(OrcamentoProdutoDetalhesActivity.this);
		
		adapterEstoque = new ItemUniversalAdapter(OrcamentoProdutoDetalhesActivity.this, ItemUniversalAdapter.ESTOQUE);
		// Inseri uma lista dentro do adapter
		adapterEstoque.setListaEstoque(estoqueRotinas.listaEstoqueProduto(idProduto, null));
		// Inseri o adapter dentro do spinner
		spinnerEstoque.setAdapter(adapterEstoque);
	}
	
	/**
	 * Valida os dados preenchidos nos campos.
	 * 
	 * @return
	 */
	private boolean validarDados(){
		boolean dadosValidos = true;
		// Instancia as rotinas de produtos
		ProdutoRotinas produtoRotinas = new ProdutoRotinas(OrcamentoProdutoDetalhesActivity.this);
		// Pega o id da embalagem
		int idEmbalagem = produto.getProduto().getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getIdEmbalagem(); 
		// Pega a quantidade de casas decimais no cadastro do produto
		int casasDecimais = produtoRotinas.casasDecimaisProduto(""+idEmbalagem, ""+produto.getProduto().getIdProduto());
		
		// Checa se existe ponto
		if(editQuantidade.getText().toString().indexOf(".") > 0){
			// Pega as casas decimais da quantidade digitada
			String cdAux = editQuantidade.getText().toString().substring(editQuantidade.getText().toString().indexOf(".") + 1);
			// Converte o valor pego apos a virgula
			int decimal = Integer.parseInt(cdAux);
			
			if(decimal > 0){
				// Checa se a quantidade de casas decimais esta liberado
				if(cdAux.length() > casasDecimais){
					// Retorna falso para informar que os dados digitados nao sao validos
					dadosValidos = false;
					
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 1);
					mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
					mensagem.put("mensagem", "Quatidade de digitos após a virgula permitido é igual a " + casasDecimais 
							   + "\n Favor, voltar e digites uma nova quantidade.");
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
					funcoes.menssagem(mensagem);
				}
			}
		}

		if ((adapterEstoque.getListaEstoque() == null) || (adapterEstoque.getListaEstoque().size() <= 0)){
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
			mensagem.put("mensagem", "Não tem estoque selecionado."
					+ "\n Favor, entrar em contato com o administrador de TI da empresa para que possa enviar os dados corretos do produto.");

			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
			funcoes.menssagem(mensagem);

			dadosValidos = false;
		}

		if ((listaPlanoPagamentoPreco == null) || (listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()) == null)){
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
			mensagem.put("mensagem", "Não tem plano de pagamento selecionado."
					+ "\n Favor, entrar em contato com o administrador de TI da empresa para que possa enviar os dados corretos do produto.");

			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
			funcoes.menssagem(mensagem);

			dadosValidos = false;
		}
		return dadosValidos;
	} // Fim validarDados
	
	
	/**
	 * Calcula os campos de valores da tela de detalhes de produtos,
	 * para salvar no pedidos.
	 * 
	 * @param campoChamada
	 */
	private void calculaTodosCampos(int campoChamada){
		double quantidade = 0,
			   //valorUnitarioBruto = 0,
			   valorUnitarioLiquido = 0,
			   percentualDesconto = 0,
			   totalLiquido = 0,
			   valorDesconto = 0;
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);
		
		// Checa se tem quantidade digitada
		if((editQuantidade != null) && (editQuantidade.getText().length() > 0)){
			quantidade = funcoes.desformatarValor(editQuantidade.getText().toString());
		}
		
		// Checa se tem unitario digitado
		if((editUnitarioLiquidoVenda != null) && (editUnitarioLiquidoVenda.getText().length() > 0)){
			valorUnitarioLiquido = funcoes.desformatarValor(editUnitarioLiquidoVenda.getText().toString());
		}
		
		// Checa se tem percentual de desconto digitado
		if((editDesconto != null) && (editDesconto.getText().length() > 0)){
			percentualDesconto = funcoes.desformatarValor(editDesconto.getText().toString());
		}
		
		// Checa se tem algum  total digitado
		if((editTotal != null) && (editTotal.getText().length() > 0)){
			totalLiquido = funcoes.desformatarValor(editTotal.getText().toString());
		}
		
		if((editValorDesconto != null) && (editValorDesconto.getText().length() > 0)){
			valorDesconto = funcoes.desformatarValor(editValorDesconto.getText().toString());
		}
		
		// Checa se o campo que esta chamando esta funcao eh o campo quantidade
		if(campoChamada == editQuantidade.getId()){
			valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
			totalLiquido = (valorUnitarioLiquido * quantidade);
			valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
			
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
			editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
			editValorDesconto.setText(funcoes.arredondarValor(valorDesconto));
			editTotal.setText(funcoes.arredondarValor(totalLiquido));
		}
		
		// Checa se o campo que esta chamando esta funcao eh o campo quantidade ou desconto
		if(campoChamada == editDesconto.getId()){
			valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
			totalLiquido = (valorUnitarioLiquido * quantidade);
			valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);

			editQuantidade.setText(funcoes.arredondarValor(quantidade));
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
			editTotal.setText(funcoes.arredondarValor(totalLiquido));
			editValorDesconto.setText(funcoes.arredondarValor(valorDesconto));
		}
		
		if(campoChamada == editValorDesconto.getId()){
			valorUnitarioLiquido = ((this.valorUnitarioVendaAux * quantidade) - valorDesconto) / quantidade;
			totalLiquido = (valorUnitarioLiquido * quantidade);
			percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
			
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
			editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
			editTotal.setText(funcoes.arredondarValor(totalLiquido));
		}
		
		// Checa se o campo que esta chamando esta funcao eh o campo unitario liquido
		if(campoChamada == editUnitarioLiquidoVenda.getId()){
			percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
			totalLiquido = (valorUnitarioLiquido * quantidade);
			valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
			
			// Seta os campos com os novos valores
			editQuantidade.setText(funcoes.arredondarValor(quantidade));
			editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
			editValorDesconto.setText(funcoes.arredondarValor(valorDesconto));
			editTotal.setText(funcoes.arredondarValor(totalLiquido));
		}
		
		// Checa se o campo que esta chamando esta funcao eh o campo total
		if(campoChamada == editTotal.getId()){
			valorUnitarioLiquido = (totalLiquido / quantidade);
			percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
			valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
			
			// Seta os campos com os novos valores
			editQuantidade.setText(funcoes.arredondarValor(quantidade));
			editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
			editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
			editValorDesconto.setText(funcoes.arredondarValor(valorDesconto));
		}
		
		if(campoChamada == spinnerPlanoPagamentoPreco.getId()){
			if(quantidade > 0){
				if(percentualDesconto > 0){
					valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
				}
				totalLiquido = (valorUnitarioLiquido * quantidade);
			}
			editTotal.setText(funcoes.arredondarValor(totalLiquido));
		}
		
	} // Fim calculaTodosCampos

	protected OrcamentoBeans preencheDadosOrcamento(){
		OrcamentoBeans orcamento = new OrcamentoBeans();
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoProdutoDetalhesActivity.this);

		orcamento.setIdOrcamento(Integer.valueOf(idOrcamento));
		orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
		orcamento.setIdPessoa(Integer.valueOf(idPessoa));
		orcamento.setNomeRazao(razaoSocial);
		// Instancia a classe de rotinas do orcamento para manipular os dados com o banco
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoProdutoDetalhesActivity.this);
		// Pega a obs do banco de dados
		orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(idOrcamento));
		// Pega o total do orcamento no banco de dados
		double total = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(idOrcamento));
		// Insere o total do orcamento varaviavel orcamento
		orcamento.setTotalOrcamento(total);
		orcamento.setDataCadastro(orcamentoRotinas.dataCadastroOrcamento(idOrcamento));

		return orcamento;
	}
	
	
}
