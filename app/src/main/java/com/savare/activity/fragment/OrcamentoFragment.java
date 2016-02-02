package com.savare.activity.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.savare.R;
import com.savare.activity.ClienteListaActivity;
import com.savare.activity.LogActivity;
import com.savare.activity.OrcamentoProdutoDetalhesActivity;
import com.savare.activity.material.designer.ProdutoListaMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.GerarPdfAsyncRotinas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class OrcamentoFragment extends Fragment {
	
	private TextView textCodigoOrcamento, textTotal, textAtacadoVarejo;
	private ListView listViewItemOrcamento;
	private List<ItemOrcamentoBeans> listaItemOrcamento;
	//private List<ItemOrcamentoBeans> listaItemOrcamentoSelecionado = new ArrayList<ItemOrcamentoBeans>();
	private List<Integer> listaItemOrcamentoSelecionado = new ArrayList<Integer>();
	private ItemUniversalAdapter adapterItemOrcamento;
	private String Observacao, 
				   idPessoa,
				   idOrcamento,
				   razaoSocial,
				   tipoOrcamentoPedido;
	private int totalItemSelecionado = 0;
	public static final int SOLICITA_CLIENTE = 2,
							RETORNA_CLIENTE = 100,
							ERRO_RETORNA_CLIENTE = 101;
	public static final String KEY_TELA_ORCAMENTO_FRAGMENTO = "ORCAMENTO_FRAGMENT",
							   KEY_TELA_CHAMADA = "TELA_CHAMADA",
							   KEY_ID_ORCAMENTO = "ID_ORCAMENTO";
	View viewOrcamento;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewOrcamento = inflater.inflate(R.layout.fragment_orcamento, container, false); 
		
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle parametro = getArguments();
		
		if(parametro != null){
			textCodigoOrcamento.setText(""+parametro.getString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO));
			textAtacadoVarejo.setText(""+parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO));
			idPessoa = parametro.getString(""+OrcamentoTabulacaoFragment.KEY_ID_PESSOA);
			idOrcamento = parametro.getString(""+OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO);
			razaoSocial = parametro.getString(""+OrcamentoTabulacaoFragment.KEY_NOME_RAZAO);
		}
		
		// Torna o listView em multiplas selecao
		listViewItemOrcamento.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		listViewItemOrcamento.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Checa se eh orcamento
				if (tipoOrcamentoPedido.equals("O")) {

					ItemOrcamentoBeans itemOrcamento = (ItemOrcamentoBeans) parent.getItemAtPosition(position);

					if ((itemOrcamento != null) && (itemOrcamento.getProduto() != null)){
						// Abre a tela de detalhes do produto
						Intent intent = new Intent(getActivity(), OrcamentoProdutoDetalhesActivity.class);

						intent.putExtra("ID_AEAPRODU", ""+itemOrcamento.getProduto().getIdProduto());
						intent.putExtra("ID_AEAORCAM", idOrcamento);
						intent.putExtra("ID_CFACLIFO", idPessoa);
						intent.putExtra("RAZAO_SOCIAL", razaoSocial);
						intent.putExtra("POSICAO", position);
						intent.putExtra("ID_AEAITORC", itemOrcamento.getIdItemOrcamento());
						intent.putExtra("ATAC_VARE", textAtacadoVarejo.getText().toString());
						intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_ORCAMENTO_FRAGMENTO);

						startActivityForResult(intent, 1);
					} else {
						// Dados da mensagem
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 0);
						mensagem.put("tela", "OrcamentoFragment");
						mensagem.put("mensagem", "Não foi possível carregar os dados do produto. \n");
						// Instancia a classe  de funcoes para mostra a mensagem
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
						funcoes.menssagem(mensagem);
					}

					/*ProdutoListaBeans produtoVenda = new ProdutoListaBeans();
					// Instancia a classe de rotinas de produtos
					ProdutoRotinas produtoRotinas = new ProdutoRotinas(getActivity());
					// Pega os dados do produto
					produtoVenda = produtoRotinas.listaProduto("AEAPRODU.ID_AEAPRODU = " + itemOrcamento.getProduto().getIdProduto(), null, textCodigoOrcamento.getText().toString()).get(0);
					produtoVenda.setAtacadoVarejo(textAtacadoVarejo.getText().charAt(0));

					// Checa se nao esta vazio
					if ((produtoVenda != null) && (produtoVenda.getProduto() != null)) {

						Bundle bundle = new Bundle();
						bundle.putParcelable("AEAPLOJA", produtoVenda);
						bundle.putParcelable("AEAORCAM", preencheDadosOrcamento());
						bundle.putInt("POSICAO", position);
						bundle.putLong("ID_AEAITORC", itemOrcamento.getIdItemOrcamento());
						bundle.putString("TELA_CHAMADA", "OrcamentoActivity");
						// Abre a tela de detalhes do produto
						Intent intent = new Intent(getActivity(), OrcamentoProdutoDetalhesActivity.class);
						intent.putExtras(bundle);

						startActivityForResult(intent, 1);
					} else {
						// Dados da mensagem
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 0);
						mensagem.put("tela", "OrcamentoActivity");
						mensagem.put("mensagem", "Não foi possível carregar os dados do produto. \n");
						// Instancia a classe  de funcoes para mostra a mensagem
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
						funcoes.menssagem(mensagem);
					}*/

				} else {
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
					// Cria uma variavem para inserir as propriedades da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 2);
					mensagem.put("tela", "OrcamentoFragment");
					mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");
					// Executa a mensagem passando por parametro as propriedades
					funcoes.menssagem(mensagem);
				}
			} // Fim setOnItemClickListener
		}); //listViewItemOrcamento.setOnItemClickListener

		
		listViewItemOrcamento.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

				// Passa por todos os itens da lista
				for (int i = 0; i < adapterItemOrcamento.getListaItemOrcamento().size(); i++) {
					// Mar o adapter para mudar a cor do fundo
					adapterItemOrcamento.getListaItemOrcamento().get(i).setTagSelectContext(false);
				}

				adapterItemOrcamento.notifyDataSetChanged();
				listaItemOrcamentoSelecionado = null;
				totalItemSelecionado = 0;
			}


			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Cria a variavel para inflar o menu de contexto
				MenuInflater menuContext = mode.getMenuInflater();
				menuContext.inflate(R.menu.orcamento_context, menu);

				return true;
			}


			@Override
			public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

					case R.id.menu_orcamento_context_deletar:
						// Checa se eh um orcamento
						if (tipoOrcamentoPedido.equals("O")) {

							AlertDialog.Builder builderConfirmacao = new AlertDialog.Builder(getActivity());
							builderConfirmacao.setMessage("Tem certeza que deseja excluir o(s) item(ns)?")
									.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {

											// Instancia a classe para manipular os produto no banco de dados
											ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(getActivity());
											int totalDeletado = 0;
											for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {
												// Deleta o item da lista de item original
												if ((itemOrcamentoSql.delete("AEAITORC.ID_AEAITORC = " + listaItemOrcamento.get(listaItemOrcamentoSelecionado.get(i)).getIdItemOrcamento())) > 0) {
													totalDeletado++;
												}
											} // Fim for

											// Dados da mensagem
											final ContentValues mensagem = new ContentValues();
											mensagem.put("comando", 2);
											mensagem.put("tela", "OrcamentoFragment");

											// Verifica se foi deletado algum registro
											if (totalDeletado > 0) {
												mensagem.put("mensagem", totalDeletado + " Deletado(s). \n");

												// Atualiza a lista de produtos
												onResume();

											} else {
												mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_conseguimos_deletar_itens) + "\n");
											}

											// Instancia a classe  de funcoes para mostra a mensagem
											FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
											funcoes.menssagem(mensagem);

											mode.finish();

										}
									})
									.setNegativeButton("N�o", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											// Fecha o menu context
											mode.finish();
										}
									});
							// Create the AlertDialog object and return it
							builderConfirmacao.create();
							builderConfirmacao.show();

							//return true;

						} else {

							FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
							// Cria uma variavem para inserir as propriedades da mensagem
							ContentValues mensagem = new ContentValues();
							mensagem.put("comando", 2);
							mensagem.put("tela", "OrcamentoActivity");
							mensagem.put("mensagem", "N�o � um or�amento. \n"
									+ "N�o pode ser deletado.");
							// Executa a mensagem passando por parametro as propriedades
							funcoes.menssagem(mensagem);
						}


						break;


					default:
						//return false;
				} // Fim switch
				return true;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				// Checa se a lista de selecionado eh nula
				if (listaItemOrcamentoSelecionado == null) {
					listaItemOrcamentoSelecionado = new ArrayList<Integer>();
				}
				// Checa se o comando eh de selecao ou descelecao
				if (checked) {
					// Incrementa o totalizador
					totalItemSelecionado = totalItemSelecionado + 1;
					//listaItemOrcamentoSelecionado.add(listaItemOrcamento.get(position));
					listaItemOrcamentoSelecionado.add(position);
					// Mar o adapter para mudar a cor do fundo
					adapterItemOrcamento.getListaItemOrcamento().get(position).setTagSelectContext(true);
					adapterItemOrcamento.notifyDataSetChanged();

				} else {
					int i = 0;
					while (i < listaItemOrcamentoSelecionado.size()) {

						// Checar se a posicao desmacada esta na lista
						if (listaItemOrcamentoSelecionado.get(i) == position) {
							// Remove a posicao da lista de selecao
							listaItemOrcamentoSelecionado.remove(i);
							// Diminui o total de itens selecionados
							totalItemSelecionado = totalItemSelecionado - 1;
							// Mar o adapter para mudar a cor do fundo
							adapterItemOrcamento.getListaItemOrcamento().get(position).setTagSelectContext(false);
							adapterItemOrcamento.notifyDataSetChanged();
						}
						// Incrementa a variavel
						i++;
					}
				}
				// Checa se tem mais de um item selecionados
				if (totalItemSelecionado > 1) {
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " itens selecionados");
				} else {
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " item selecionado");
				}

			}
		});
		
		// Ativa a opcao de menus para este fragment
		setHasOptionsMenu(true);

		return viewOrcamento;
	} // Fim do onCreate
	
	@Override
	public void onResume() {
		super.onResume();
		
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
		// Pega todos os produtos do orcamento
		listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, textCodigoOrcamento.getText().toString());
		
		adapterItemOrcamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.ITEM_ORCAMENTO, listaItemOrcamento);
		
		// Verifica se existe algum dado na lista
		if(listaItemOrcamento.size() > 0){
			// Preenche o list com os itens no layout(adapter) personalizado
			listViewItemOrcamento.setAdapter(adapterItemOrcamento);
			// Calcula o total dos pedidos listados
			textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
			
		} else {
			// Passa por todos da lista do adapter
			for (int i = 0; i < adapterItemOrcamento.getCount(); i++) {
				// Remove todos do adapter se existir
				adapterItemOrcamento.remove(i);
				//adapterItemOrcamento.notifyDataSetChanged();
			}
		}
		// Pega os status do orcamento, para checar se eh um orcamento ou pedido
		this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());
		
		
	} // Fim do onResume
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//super.onCreateOptionsMenu(menu, inflater);
		//MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.orcamento, menu);
        inflater.inflate(R.menu.orcamento_fragment, menu);
        
        // Configuracao associando item de pesquisa com a SearchView
 		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
 		
 		SearchView searchView = (SearchView) menu.findItem(R.id.menu_orcamento_fragment_pesquisa).getActionView();
 		searchView.setQueryHint("Pesquisar");
 		
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
 		searchView.setSubmitButtonEnabled(true);

 		searchView.setOnQueryTextListener(new OnQueryTextListener() {

 			/**
 			 * Botao para submeter a pesquisa.
 			 * So eh executado quando clicado no botao.
 			 */
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				// Chama a funcao para carregar a lista com todos os produtos
 				//criaListaDeProdutos(null, null, 1);
 				
 				adapterItemOrcamento.getFilter().filter(query);

 				// Seta o adapte com a nova lista, com ou sem o filtro
 				adapterItemOrcamento.setListaItemOrcamento(listaItemOrcamento);

 				return false;
 			} // Fim do onQueryTextSubmit

 			
 			/**
 			 * Pega todo o texto digitado
 			 */
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				// Checa se nao existe caracter no campo de pesquisa
 				if(newText.length() <= 0){
 					
 					adapterItemOrcamento.getFilter().filter(newText);

 					// Seta o adapte com a nova lista, com ou sem o filtro
 					adapterItemOrcamento.setListaItemOrcamento(listaItemOrcamento);
 				}
 				return false;
 			} // Fim do onQueryTextChange
 			
 		}); // Fim do setOnQueryTextListener
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_orcamento_fragment_adicionar:
			
			// Checa se eh um orcamento
			if (this.tipoOrcamentoPedido.equals("O")){
				// Abre a tela inicial do sistema
				/*Intent intent = new Intent(getActivity(), ProdutoListaActivity.class);
				intent.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
				intent.putExtra("ID_CFACLIFO", idPessoa);
				intent.putExtra("NOME_RAZAO", razaoSocial.replaceFirst("- ", ""));
				intent.putExtra("ATAC_VAREJO", textAtacadoVarejo.getText().toString());
				startActivity(intent);*/

				Intent intentOrcamento = new Intent(getContext(), ProdutoListaMDActivity.class);
				intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, textCodigoOrcamento.getText().toString());
				intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ID_CLIENTE, idPessoa);
				intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, textAtacadoVarejo.getText().toString());
				intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_NOME_RAZAO, razaoSocial.replaceFirst("- ", ""));
				startActivity(intentOrcamento);

			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n"
									   + getActivity().getResources().getString(R.string.nao_pode_ser_inserido_novos_produtos));
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			
			break;
			
		case R.id.menu_orcamento_fragment_enviar_email:
			
			try {
				//Cria novo um ProgressDialogo e exibe
				ProgressDialog progress = new ProgressDialog(getActivity());
		        progress.setMessage("Aguarde, o PDF está sendo Gerado...");
		        progress.setCancelable(false);
		        progress.show();
		        
				GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(getActivity());
				// Envia a lista de produtos que pertence ao orcamento
				gerarPdfRotinas.setListaItensOrcamento(listaItemOrcamento);
				// Envia os dados do orcamento
				gerarPdfRotinas.setOrcamento(preencheDadosOrcamento());
				
				String retornoCaminho = gerarPdfRotinas.criaArquivoPdf();
				
				if(retornoCaminho.length() > 0){
					// Fecha a barra de progresso
					progress.dismiss();
					
					File arquivo = new File(retornoCaminho);
					
					PessoaRotinas pessoaRotinas = new PessoaRotinas(getActivity());
					
					Intent dadosEmail = new Intent(Intent.ACTION_SEND);
					//dadosEmail.setType("message/rfc822");
					dadosEmail.setType("text/plain");
					dadosEmail.putExtra(Intent.EXTRA_EMAIL  , new String[]{pessoaRotinas.emailPessoa(idPessoa)});
					dadosEmail.putExtra(Intent.EXTRA_SUBJECT, "Orçamento/Pedido # " + textCodigoOrcamento.getText());
					dadosEmail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+arquivo));
					dadosEmail.putExtra(Intent.EXTRA_TEXT   , "E-Mail enviado pelo App SAVARE.");
					
					try {
					    startActivity(Intent.createChooser(dadosEmail, "Enviar e-mail..."));
					
					} catch (android.content.ActivityNotFoundException ex) {
					    //Toast.makeText(OrcamentoActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
					}
				} else {
					progress.dismiss();
				}
				
				
			} catch (Exception e) {
				
			}
			
			break;
			
		case R.id.menu_orcamento_fragment_pesquisa:
			
			break;
			
		case R.id.menu_orcamento_fragment_atualizar:
			onResume();
			break;
			
		case R.id.menu_orcamento_fragment_salvar:
			
			try{
				//ContentValues dadosOrcamento = new ContentValues();
				//dadosOrcamento.put("TIPO_ORCAMENTO", "ORCAMENTO");
				
				GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(getActivity());
				// Seta(envia) os dados do orcamento
				gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
				// Seta(envia) a lista de produtos do orcamento
				gerarPdfSalvar.setListaItensOrcamento(listaItemOrcamento);
				
				gerarPdfSalvar.execute("");
				
				// Fecha a view
				//finish();
				
			}catch (Exception e) {
				//Log.i("thread", e.getMessage());
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 0);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_foi_possivel_salvar_orcamento_pdf));
				mensagem.put("dados", e.toString());
				mensagem.put("usuario", funcoes.getValorXml("Usuario"));
				mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
				mensagem.put("email", funcoes.getValorXml("Email"));
				
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_fragment_rateio_preco:
			// Checa se existe produtos no orcamento
			if(listViewItemOrcamento.getCount() > 0){
				
				if(adapterItemOrcamento.getTipoItem() == adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO){
					adapterItemOrcamento.setTipoItem(adapterItemOrcamento.ITEM_ORCAMENTO);
					((BaseAdapter) listViewItemOrcamento.getAdapter()).notifyDataSetChanged();

					OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
					
					textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
					
				}else {
					adapterItemOrcamento.setTipoItem(adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO);
					((BaseAdapter) listViewItemOrcamento.getAdapter()).notifyDataSetChanged();
					
					// Variavel para armezenar o total da diferenca entro o preco vendido e o preco de tabela
					double totalDiferenca = 0;
					// Passa por toda a lista de itens
					for(int i = 0; i < listaItemOrcamento.size(); i++){
						totalDiferenca = totalDiferenca + (listaItemOrcamento.get(i).getValorTabela() - listaItemOrcamento.get(i).getValorLiquido());
					}
					// Instancia a classe de funcoes
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
					// Seta o campo com o total da diferenca
					textTotal.setText("Diferença: " + funcoes.arredondarValor(String.valueOf(totalDiferenca * (-1))));
				}
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoFragment");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_existe_produto_lista_orcamento) + "\n");
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_fragment_transformar_pedido:
			// Instancia a classe de funcoes
			FuncoesPersonalizadas funcoes;
			
			// Checa se eh um orcamento
			if (tipoOrcamentoPedido.equals("O")){
			
				// Instancia a classe para manipular os orcamento no banco de dados
				OrcamentoSql orcamentoSql = new OrcamentoSql(getActivity());
				int totalAtualizado = 0;
								
				ContentValues dadosPedido = new ContentValues();
				dadosPedido.put("STATUS", "P");
					
				totalAtualizado = totalAtualizado + orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText());
				
				// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoFragment");
				
				// Verifica se foi deletado algum registro
				if(totalAtualizado > 0){
					mensagem.put("mensagem", totalAtualizado + " Transformado(s) em Pedido(s). \n");
					
					this.tipoOrcamentoPedido = "P";
					
					GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(getActivity());
					// Seta(envia) os dados do orcamento
					gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
					// Seta(envia) a lista de produtos do orcamento
					gerarPdfSalvar.setListaItensOrcamento(listaItemOrcamento);
					
					gerarPdfSalvar.execute("");
					
					// Fecha a view
					//finish();
					
				}else {
					mensagem.put("mensagem", "NÃO CONSEGUIMOS TRANSFORMAR O(S) ORÇAMENTO(S) EM PEDIDO(S). \n");
				}
				
				// Instancia a classe  de funcoes para mostra a mensagem
				funcoes = new FuncoesPersonalizadas(getActivity());
				funcoes.menssagem(mensagem);
			
			} else {
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");
				
				funcoes = new FuncoesPersonalizadas(getActivity());
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_fragment_trocar_cliente:
			// Checa se eh um orcamento
			if (tipoOrcamentoPedido.equals("O")){
				// Abre a tela de detalhes do produto
				Intent intent = new Intent(getActivity(), ClienteListaActivity.class);
				intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_ORCAMENTO_FRAGMENTO);
				intent.putExtra(KEY_ID_ORCAMENTO, textCodigoOrcamento.getText().toString());
				// Abre a activity aquardando uma resposta
				startActivityForResult(intent, SOLICITA_CLIENTE);
			
			} else {
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoFragment");
				mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");
				
				funcoes = new FuncoesPersonalizadas(getActivity());
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_fragment_visualizar_logs:
			
			// Abre a tela inicial do sistema
			Intent intent = new Intent(getActivity(), LogActivity.class);
			intent.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
			intent.putExtra("TABELA", new String[]{"AEAORCAM", "AEAITORC"});
			startActivity(intent);
			
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
	        if(resultCode == Activity.RESULT_OK){
	        	// Executa apenas a parte do onResume
	        	onResume();
	        }
	        // Checa se eh um retorno da tela de cliente
	    } else if(requestCode == SOLICITA_CLIENTE){
	    	
	    	if(resultCode == RETORNA_CLIENTE){
	    		getActivity().getActionBar().setTitle(data.getStringExtra("ID_CFACLIFO") + " - " + data.getStringExtra("NOME_CLIENTE"));
				razaoSocial = data.getStringExtra("NOME_CLIENTE");
	    	
	    	} else if(requestCode == ERRO_RETORNA_CLIENTE){
	    		// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 0);
				mensagem.put("tela", "OrcamentoFragment");
				mensagem.put("mensagem", "Não conseguimos trocar o cliente deste orçamento. \n");
				// Instancia a classe  de funcoes para mostra a mensagem
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
				funcoes.menssagem(mensagem);
	    	}
	    }
	}
	
	private void recuperaCamposTela(){
		textCodigoOrcamento = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_text_codigo_orcamento);
		textTotal = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_text_total);
		textAtacadoVarejo = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_text_atacado_varejo);
		listViewItemOrcamento = (ListView) viewOrcamento.findViewById(R.id.fragment_orcamento_list_produto);
	}
	
	/**
	 * Funcao para retornar os dados do orcamento
	 * @return
	 */
	protected OrcamentoBeans preencheDadosOrcamento(){
		OrcamentoBeans orcamento = new OrcamentoBeans();
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
		
		orcamento.setIdOrcamento(Integer.valueOf(textCodigoOrcamento.getText().toString()));
		orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
		orcamento.setIdPessoa(Integer.valueOf(idPessoa));
		orcamento.setNomeRazao(razaoSocial);
		// Instancia a classe de rotinas do orcamento para manipular os dados com o banco
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
		// Pega a obs do banco de dados
		orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(textCodigoOrcamento.getText().toString()));
		// Pega o total do orcamento no banco de dados
		double total = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
		// Insere o total do orcamento varaviavel orcamento
		orcamento.setTotalOrcamento(total);
		orcamento.setDataCadastro(orcamentoRotinas.dataCadastroOrcamento(textCodigoOrcamento.getText().toString()));
		
		return orcamento;
	}
	
	/**
	 * Atualiza o campo de total do orcamento.
	 */
	private void totalOrcamento(){
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
		
		textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
	}
}
