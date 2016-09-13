package com.savare.activity;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.adapter.DescricaoSimplesAdapter;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ProdutoRotinas;
import com.savare.funcoes.rotinas.async.PesquisaListaProdutoAsyncRotinas;
@Deprecated
public class ProdutoListaActivity extends Activity implements OnNavigationListener {
	
	private TextView textCodigoOrcamento, textCodigoPessoa, textNomeRazao, textAtacadoVarejo, textProcessoPesquisa;
	private ListView listViewProduto;
	private ProgressBar progressoPesquisa;
	private ActionBar actionBar;
	private DescricaoSimplesAdapter adapterClasse;
	private List<ProdutoListaBeans> listaProdutos;
	private ItemUniversalAdapter adapterProduto;
	private long idItemOrcamento = 0;
	private ProdutoListaBeans produtoVendaClicado;
	public static final String KEY_TELA_PRODUTO_LISTA_ACTIVITY = "ProdutoListaActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_produto_lista);
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		// 
		actionBar.setDisplayHomeAsUpEnabled(true);
		//Tira o titulo da action bar
		actionBar.setDisplayShowTitleEnabled(false);
		// Ativa a navegacao suspensa Spinner
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        // Instancia a classe de rotinas de produtos
        ProdutoRotinas produtoRotinas = new ProdutoRotinas(ProdutoListaActivity.this);
        
        adapterClasse = new DescricaoSimplesAdapter(ProdutoListaActivity.this, produtoRotinas.listaClasse());
        
        actionBar.setListNavigationCallbacks(adapterClasse, this);
		
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			textCodigoOrcamento.setText(intentParametro.getString("ID_AEAORCAM"));
			textNomeRazao.setText(intentParametro.getString("NOME_RAZAO"));
			textCodigoPessoa.setText(intentParametro.getString("ID_CFACLIFO"));
			textAtacadoVarejo.setText(intentParametro.getString("ATAC_VAREJO"));

		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "ProdutoListaActivity");
			mensagem.put("mensagem", "Não foi possível criar a lista de produtos a partir do orçamento\n"
					   + "Favor, voltar e selecione novamente um cliente para criar um novo orçamento");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoListaActivity.this);
			funcoes.menssagem(mensagem);
		}
		/*// Verifica se o adapter esta preenchido
		if(adapterProduto != null){
			// Marca todos os produtos da lista que ja tem no orcamento
			marcaProdutoJaComprados();
		}*/
		
		listViewProduto.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Checa se a lista de produtos pertence a um orcamento
				if( (textCodigoOrcamento != null) && (textCodigoOrcamento.getText().length() > 0) ){
					//Pega os dados da pessoa que foi clicado
					ProdutoListaBeans produtoVenda = (ProdutoListaBeans) parent.getItemAtPosition(position);
					produtoVenda.setAtacadoVarejo(textAtacadoVarejo.getText().charAt(0));
					
					Bundle bundle = new Bundle();
					bundle.putParcelable("AEAORCAM", preencheDadosOrcamento());
					bundle.putString("ID_AEAPRODU", "" + produtoVenda.getProduto().getIdProduto());
					bundle.putString("ATAC_VARE", textAtacadoVarejo.getText().toString());
					//bundle.putString("TELA_CHAMADA", "ProdutoListaActivity");
					bundle.putInt("POSICAO", position);
					bundle.putLong("ID_AEAITORC", idItemOrcamento);
					bundle.putString("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
					bundle.putString("ID_CFACLIFO", textCodigoPessoa.getText().toString());
					bundle.putString("RAZAO_SOCIAL", textNomeRazao.getText().toString());

					// Abre a tela de detalhes do produto
					Intent intent = new Intent(ProdutoListaActivity.this, OrcamentoProdutoDetalhesActivity.class);
					intent.putExtras(bundle);
					
					startActivityForResult(intent, 1);
				
				} else {
					// Pega os dados do produto clicado
					produtoVendaClicado = (ProdutoListaBeans) parent.getItemAtPosition(position);
					produtoVendaClicado.setAtacadoVarejo(textAtacadoVarejo.getText().charAt(0));
					
					// Abre a tela de detalhes do produto
					Intent intent = new Intent(ProdutoListaActivity.this, ListaOrcamentoPedidoActivity.class);
					intent.putExtra("ORCAMENTO_PEDIDO", "O");
					intent.putExtra("RETORNA_VALOR", "T");
					// Abre a activity aquardando uma resposta
					startActivityForResult(intent, 1);
				}
			}
		});
		
	} // Fim onCreate
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//ProdutoAsyncRotinas produtoAsyncRotinas = new ProdutoAsyncRotinas(ProdutoListaActivity.this);
		//produtoAsyncRotinas.execute(new String[]{textCodigoOrcamento.getText().toString()});
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.produto_lista, menu);
		
		// Configuracao associando item de pesquisa com a SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_produto_lista_pesquisa).getActionView();
		searchView.setQueryHint("Pesquisar");
		
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setSubmitButtonEnabled(true);
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			/**
			 * Botao para submeter a pesquisa.
			 * So eh executado quando clicado no botao.
			 */
			@Override
			public boolean onQueryTextSubmit(String query) {
								
				String where = "( (AEAPRODU.DESCRICAO LIKE '%"+query+"%') OR "
							     + "(AEAPRODU.CODIGO_ESTRUTURAL LIKE '%"+query+"%') OR "
							     + "(AEAPRODU.DESCRICAO_AUXILIAR LIKE '%"+query+"%') OR "
							     + "(AEAPRODU.REFERENCIA LIKE '%"+query+"%') OR "
							     + "(AEAMARCA.DESCRICAO LIKE '%"+query+"%') )";

				PesquisaListaProdutoAsyncRotinas pesquisaProduto =
						new PesquisaListaProdutoAsyncRotinas(ProdutoListaActivity.this,
															 where,
															 null,
															 textCodigoOrcamento.getText().toString(),
															 textAtacadoVarejo.getText().toString(),
															 1,
															 listViewProduto,
															 progressoPesquisa,
															 textProcessoPesquisa);
				pesquisaProduto.execute();

				//criaListaDeProdutos(where, null, 1);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				// Fecha o teclado
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				// Fecha a barra de progresso
				return false;
			} // Fim do onQueryTextSubmit

			
			/**
			 * Pega todo o texto digitado
			 */
			@Override
			public boolean onQueryTextChange(String newText) {
				
				return false;
			} // Fim do onQueryTextChange
			
		}); // Fim do setOnQueryTextListener
		
		return super.onCreateOptionsMenu(menu);
	} // Fim do onCreateOptionsMenu
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			break;
			
		case R.id.menu_produto_lista_context_detalhes_produto:
			ProdutoListaBeans produtoLoja = (ProdutoListaBeans) listViewProduto.getItemAtPosition(info.position);
			
			Intent intent = new Intent(ProdutoListaActivity.this, ProdutoDetalhesActivity.class);
			intent.putExtra(ProdutoDetalhesActivity.KEY_ID_PRODUTO, String.valueOf(produtoLoja.getProduto().getIdProduto()));
			
			startActivity(intent);
			
			break;
			
		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected


	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		if(!adapterClasse.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Nenhuma Classe Selecionada")){
			
			this.listaProdutos = null;
			
			// Cria a variavel para montar a clausula where do sql
			String where = null;
			
			if((!adapterClasse.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Nenhum valor encontrado")) && 
					(!adapterClasse.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Todos os produtos"))){
				
				// Monta a clausula where do sql
				where = "AEACLASE.DESCRICAO = '" + adapterClasse.getLista().get(itemPosition).getTextoPrincipal() + "' ";
				
				// Verifica se a listagem de produto pertence a um orcamento
				if( (textCodigoOrcamento != null) && (textCodigoOrcamento.getText().length() > 0)){
					// Adiciona outro filtro
					where = where + "AND (AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText().toString() + ")) ";
				} else {
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoListaActivity.this);
					
					// Adiciona outro filtro
					where = where + "AND (AEAPLOJA.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") ";
				}
				// Cria a lista com os produtos de acordo com a classe selecionada
				PesquisaListaProdutoAsyncRotinas pesquisaProduto =
						new PesquisaListaProdutoAsyncRotinas(ProdutoListaActivity.this,
								where,
								null,
								textCodigoOrcamento.getText().toString(),
								textAtacadoVarejo.getText().toString(),
								0,
								listViewProduto,
								progressoPesquisa,
								textProcessoPesquisa);
				pesquisaProduto.execute();
				//criaListaDeProdutos(where, null, 0);
				
			} else if(adapterClasse.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Todos os produtos")) {
				// Verifica se a listagem de produto pertence a um orcamento
				if( (textCodigoOrcamento != null) && (textCodigoOrcamento.getText().length() > 0)){
					where = "AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText().toString() + ")";
				} else {
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoListaActivity.this);
					
					where = "AEAPLOJA.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa");
				}
				// Preenche a lista de PRODUTOS
				PesquisaListaProdutoAsyncRotinas pesquisaProduto =
						new PesquisaListaProdutoAsyncRotinas(ProdutoListaActivity.this,
								where,
								" GROUP BY AEAPLOJA.ID_AEAPLOJA ",
								textCodigoOrcamento.getText().toString(),
								textAtacadoVarejo.getText().toString(),
								0,
								listViewProduto,
								progressoPesquisa,
								textProcessoPesquisa);
				pesquisaProduto.execute();
				//criaListaDeProdutos(where, " GROUP BY AEAPLOJA.ID_AEAPLOJA ", 0);
			}
		}

		return false;
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
	        if(resultCode == RESULT_OK){
	        	// Pega a posicao da lista de produtos	
	        	int posicao = data.getExtras().getInt("POSICAO");
	        	
	        	// Informa que o produto esta em um orcamento
    			listaProdutos.get(posicao).setEstaNoOrcamento(data.getExtras().getChar("RESULTADO"));
    			this.idItemOrcamento = data.getExtras().getLong("ID_AEAITORC");
    			
    			((BaseAdapter) listViewProduto.getAdapter()).notifyDataSetChanged();
    			
	        } else if(resultCode == 100){
		    	
		    	OrcamentoBeans orcamento = new OrcamentoBeans(); 
				// Pega os dados do orcamento restornado a lista de orcamento
		    	orcamento = data.getParcelableExtra("AEAORCAM");
				
				if((orcamento != null) && (orcamento.getIdOrcamento() > 0)){
					
					textCodigoOrcamento.setText(""+orcamento.getIdOrcamento());
					textNomeRazao.setText(orcamento.getNomeRazao());
					textCodigoPessoa.setText("" + orcamento.getIdPessoa());
					textAtacadoVarejo.setText("" + orcamento.getTipoVenda());

					Intent dadosParametro = new Intent(ProdutoListaActivity.this, OrcamentoProdutoDetalhesActivity.class);
					// Pega os dados para enviar para outra tela
					dadosParametro.putExtra("ID_AEAPRODU", "" + produtoVendaClicado.getProduto().getIdProduto());
					dadosParametro.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
					dadosParametro.putExtra("ID_CFACLIFO", "" + orcamento.getIdPessoa());
					dadosParametro.putExtra("RAZAO_SOCIAL", orcamento.getNomeRazao());
					//dadosParametro.putExtra("POSICAO", position);
					dadosParametro.putExtra("ID_AEAITORC", "" + produtoVendaClicado.getProduto().getIdProduto());
					dadosParametro.putExtra("ATAC_VARE", textAtacadoVarejo.getText().toString());
					dadosParametro.putExtra(OrcamentoFragment.KEY_TELA_CHAMADA, KEY_TELA_PRODUTO_LISTA_ACTIVITY);

					/*Bundle bundle = new Bundle();
					bundle.putParcelable("AEAPLOJA", produtoVendaClicado);
					bundle.putParcelable("AEAORCAM", preencheDadosOrcamento());
					bundle.putString("TELA_CHAMADA", "ProdutoListaActivity");
					bundle.putLong("ID_AEAITORC", produtoVendaClicado.getProduto().getIdProduto());
					// Abre a tela de detalhes do produto
					Intent intent = new Intent(ProdutoListaActivity.this, OrcamentoProdutoDetalhesActivity.class);
					intent.putExtras(bundle);*/
					
					startActivityForResult(dadosParametro, 1);
				}
				
		    } else if(resultCode == 101){
		    	textCodigoOrcamento.setText("");
				textNomeRazao.setText("");
				textCodigoPessoa.setText("");
				//textAtacadoVarejo.setText("");
		    }
	    } 
	}
	
	
	private void recuperaCamposTela() {
		textCodigoOrcamento = (TextView) findViewById(R.id.activity_produto_lista_text_codigo_orcamento);
		textNomeRazao = (TextView) findViewById(R.id.activity_produto_lista_text_nome_razao);
		textCodigoPessoa = (TextView) findViewById(R.id.activity_produto_lista_text_codigo_pessoa);
		textAtacadoVarejo = (TextView) findViewById(R.id.activity_produto_lista_text_atacado_varejo);
		textProcessoPesquisa = (TextView) findViewById(R.id.activity_produto_lista_textView_processo_pesquisa);
		listViewProduto = (ListView) findViewById(R.id.activity_produto_lista_listView_produto);
		progressoPesquisa = (ProgressBar) findViewById(R.id.activity_produto_lista_progressBar_processo_pesquisa);
	}
	
	/**
	 * Funcao responsavel para preenche a lista com os produtos,
	 * de acorto com o tipo de filtro.
	 * @param where
	 * @param group
	 * @param tipo - 0 = Normal | 1 = Campo de Pesquisa(actionBar)
	 */
	/*private void criaListaDeProdutos(String where, String group, int tipo){

		// Cria variavel para armazenar where auxiliar
		String whereAux = "";
		
		// Verifica se o tipo eh normal
		if (tipo == 0) {
			// Instancia a classe de rotina
			ProdutoRotinas produtoRotinas = new ProdutoRotinas(ProdutoListaActivity.this);

			if( (textCodigoOrcamento != null) && (textCodigoOrcamento.getText().length() > 0) ){
				// Cria a lista de produto e verifica se os produto existe no orcamento
				listaProdutos = produtoRotinas.listaProduto(where, group, textCodigoOrcamento.getText().toString(), null, null);
			}else {
				// Cria a lista de produto sem verificar se o produto existe no orcamento
				listaProdutos = produtoRotinas.listaProduto(where, group, null, null, null);
			}
			
		// Lista todos os produtos, para o Campo de Pesquisa(actionBar)
		} else if(tipo == 1){
			
			// Verifica se a listagem de produto pertence a um orcamento
			if((textCodigoOrcamento != null) && (textCodigoOrcamento.getText().length() > 0)){
				
				whereAux += "(AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText().toString() + ")) ";

			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoListaActivity.this);
				
				whereAux += "(AEAPLOJA.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")";
			}
			
			if(where.length() > 0){
				whereAux += " AND " + where;
			}
			
			// Preenche a lista de PRODUTOS
			criaListaDeProdutos(whereAux, group, 0);
			
		} // Fim do if tipo 1
		
		// Instancia o adapter e o seu tipo(produto)
		adapterProduto = new ItemUniversalAdapter(ProdutoListaActivity.this, ItemUniversalAdapter.PRODUTO);
		// Seta a lista de produtos no adapter
		adapterProduto.setListaProduto(listaProdutos);
		// Informa o tipo da venda (atacado ou varejo)
		adapterProduto.setAtacadoVarejo(textAtacadoVarejo.getText().toString());
		
		// Seta o listView com o novo adapter que ja esta com a nova lista
		listViewProduto.setAdapter(adapterProduto);
		
	} //Fim criaListaDeProdutos*/

	
	/**
	 * Funcao para retornar os dados do orcamento
	 * @return
	 */
	private OrcamentoBeans preencheDadosOrcamento(){
		OrcamentoBeans orcamento = new OrcamentoBeans();
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoListaActivity.this);
		
		orcamento.setIdOrcamento(Integer.valueOf(textCodigoOrcamento.getText().toString()));
		orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
		orcamento.setIdPessoa(Integer.valueOf(textCodigoPessoa.getText().toString()));
		orcamento.setNomeRazao(textNomeRazao.getText().toString());
		orcamento.setTipoVenda(textAtacadoVarejo.getText().toString());
		
		return orcamento;
	}
	
} // Fim da classe
