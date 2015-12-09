package com.savare.activity;

import java.util.List;
import java.util.UUID;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;

import com.savare.R;
import com.savare.activity.fragment.ClienteCadastroFragment;
import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.adapter.DescricaoSimplesAdapter;
import com.savare.adapter.PessoaAdapter;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;

public class ClienteListaActivity extends Activity implements OnNavigationListener{
	
	private ListView listViewPessoa;
	private PessoaRotinas pessoaRotinas;
	private PessoaAdapter adapterPessoa;
	private List<PessoaBeans> listaPessoas;
	private DescricaoSimplesAdapter adapterCidade;
	private ActionBar actionBar;
	private String telaChamou,
				   idOrcamento;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_cliente_lista);
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			
			this.telaChamou = intentParametro.getString(ListaOrcamentoPedidoActivity.KEY_TELA_CHAMADA);
			
			if(telaChamou.equals(OrcamentoFragment.KEY_TELA_ORCAMENTO_FRAGMENTO)){
				idOrcamento = intentParametro.getString(OrcamentoFragment.KEY_ID_ORCAMENTO);
			}
		}
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		// 
		actionBar.setDisplayHomeAsUpEnabled(true);
		//Tira o titulo da action bar
		actionBar.setDisplayShowTitleEnabled(false);
		// Ativa a navegacao suspensa Spinner
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
		listViewPessoa = (ListView) findViewById(R.id.activity_cliente_lista_list_pessoa);

		// Instancia a classe
		pessoaRotinas = new PessoaRotinas(ClienteListaActivity.this);
		
		adapterCidade = new DescricaoSimplesAdapter(ClienteListaActivity.this, pessoaRotinas.listaCidadePessoa("cliente"));
		
		// Preenche o spinner da action bar com as cidades
		actionBar.setListNavigationCallbacks(adapterCidade, this);
		
		// Posiciona o spinner na primeira posicao da lista
		actionBar.setSelectedNavigationItem(0);

		// Pega o clique do listViewPessoa
		listViewPessoa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if((telaChamou != null) && (telaChamou.equals(ListaOrcamentoPedidoActivity.KEY_TELA_LISTA_ORCAMENTO_PEDIDO))){
					
					PessoaBeans pessoa = new PessoaBeans();
					pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaActivity.this);
					
					// Cria uma intent para returnar um valor para activity ProdutoLista
					Intent returnIntent = new Intent();
					returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
					returnIntent.putExtra("ID_CFAESTAD", String.valueOf(pessoa.getEstadoPessoa().getCodigoEstado()));
					returnIntent.putExtra("ID_CFACIDAD", String.valueOf(pessoa.getCidadePessoa().getIdCidade()));
					returnIntent.putExtra("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
					returnIntent.putExtra("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
					returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
					returnIntent.putExtra("IE_RG_CLIENTE", pessoa.getIeRg());
					returnIntent.putExtra("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
					returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
					returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
					returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
					returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
					returnIntent.putExtra("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
					returnIntent.putExtra("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
					returnIntent.putExtra("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
					if (pessoa.isCadastroNovo()){
						returnIntent.putExtra("CADASTRO_NOVO", "S");
					}
					setResult(ListaOrcamentoPedidoActivity.RETORNA_CLIENTE, returnIntent);
					// Fecha a tela de detalhes de produto
					finish();
					
				} else if((telaChamou != null) && (telaChamou.equals(OrcamentoFragment.KEY_TELA_ORCAMENTO_FRAGMENTO))){
					
					PessoaBeans pessoa = new PessoaBeans();
					pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaActivity.this);
					// Preenche o ContentValues com os dados da pessoa
					ContentValues dadosCliente = new ContentValues();
					dadosCliente.put("ID_CFACLIFO", pessoa.getIdPessoa());
					dadosCliente.put("ID_CFAESTAD", pessoa.getEstadoPessoa().getCodigoEstado());
					dadosCliente.put("ID_CFACIDAD", pessoa.getCidadePessoa().getIdCidade());
					dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
					dadosCliente.put("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
					dadosCliente.put("NOME_CLIENTE", pessoa.getNomeRazao());
					dadosCliente.put("IE_RG_CLIENTE", pessoa.getIeRg());
					dadosCliente.put("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
					dadosCliente.put("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
					dadosCliente.put("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
					dadosCliente.put("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
					if (pessoa.isCadastroNovo()){
						dadosCliente.put("CADASTRO_NOVO", "S");
					}
					OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ClienteListaActivity.this);
					// Atualiza o cliente do orcamento
					int qtdAlterado = orcamentoRotinas.updateOrcamento(dadosCliente, idOrcamento);
					
					// Checa se atualizou algum orcamento
					if(qtdAlterado > 0){
						
						Intent returnIntent = new Intent();
						returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
						returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
						returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
						returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
						returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
						returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
						
						setResult(OrcamentoFragment.RETORNA_CLIENTE, returnIntent);
						// Fecha a tela de detalhes de produto
						finish();
					
					} else {
						setResult(OrcamentoFragment.ERRO_RETORNA_CLIENTE);
						// Fecha a tela de detalhes de produto
						finish();
					}
					
				} else {
					//Pega os dados da pessoa que foi clicado
					PessoaBeans pessoa = (PessoaBeans) parent.getItemAtPosition(position);
					
					// Abre a tela inicial do sistema
					Intent intent = new Intent(ClienteListaActivity.this, ClienteDetalhesActivity.class);
					intent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
					intent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
					intent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
					intent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
					intent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
					if (pessoa.isCadastroNovo()){
						intent.putExtra("CADASTRO_NOVO", "S");
					}
					startActivity(intent);
				}
			}
		});
	} // Fim do onCreate
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.cliente_lista, menu);
		
		// Configuração associando item de pesquisa com a SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_cliente_lista_pesquisa).getActionView();
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

				String where = "CFACLIFO.NOME_RAZAO LIKE '%" + query + "%' OR "
						     + "CFACLIFO.NOME_FANTASIA LIKE '%" + query + "%' OR "
						     + "CFACLIFO.CPF_CNPJ LIKE '%" + query + "%' OR "
						     + "CFACIDAD.DESCRICAO LIKE '%" + query + "%' OR "
						     + "CFAENDER.BAIRRO LIKE '%" + query + "%' OR "
						     + "CFASTATU.DESCRICAO LIKE '%" + query + "%' ";
				
				// Cria a lista com as pessoas de acordo com a cidade selecionada
				listaPessoas = pessoaRotinas.listaPessoaResumido(where, "cliente");
				// Seta o adapter com a nova lista
				adapterPessoa = new PessoaAdapter(ClienteListaActivity.this, listaPessoas, 0);
				// Seta o listView com o novo adapter que ja esta com a nova lista
				listViewPessoa.setAdapter(adapterPessoa);

				return false;
			} // Fim do onQueryTextSubmit

			/**
			 * Pega todo o texto digitado
			 */
			@Override
			public boolean onQueryTextChange(String newText) {

				
				return false;
			} // Fim do onQueryTextChange
			
		
			//OnQueryTextListener
			
		}); // Fim do setOnQueryTextListener
		
				
		return super.onCreateOptionsMenu(menu);
	} // Fim do onCreateOptionsMenu
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			//Intent intent = new Intent(ClienteListaActivity.this, InicioActivity.class);
			// Tira a acitivity da pilha e inicia uma nova
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			//startActivity(intent);
			finish();
			break;
			
		case R.id.menu_cliente_novo_cliente:
			// Abre a tela inicial do sistema
			Intent intentNovo = new Intent(ClienteListaActivity.this, ClienteCadastroFragment.class);
			startActivity(intentNovo);
			break;

		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected
	

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Cria a variavel para montar a clausula where do sql
		String where = null;
		
		if((!adapterCidade.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Nenhum valor encontrado")) && 
				(!adapterCidade.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Todas as Cidades"))){
			
			// Monta a clausula where do sql
			where = "CFACIDAD.DESCRICAO = '" + adapterCidade.getLista().get(itemPosition).getTextoPrincipal().replace("'", "%") + "'";
			// Cria a lista com as pessoas de acordo com a cidade selecionada
			listaPessoas = pessoaRotinas.listaPessoaResumido(where, "cliente");
			
		} else if(adapterCidade.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Todas as Cidades")) {
			// Preenche a lista de pessoas
			listaPessoas = pessoaRotinas.listaPessoaResumido(null, "cliente");
		}
		if(listaPessoas != null){
			// Seta o adapter com a nova lista
			adapterPessoa = new PessoaAdapter(ClienteListaActivity.this, listaPessoas, 0);
			// Seta o listView com o novo adapter que ja esta com a nova lista
			listViewPessoa.setAdapter(adapterPessoa);
		}

		return false;
	} // Fim onNavigationItemSelected

}
