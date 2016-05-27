package com.savare.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.custom.AlertDialogTextoCustom;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;
import com.savare.funcoes.rotinas.async.GerarPdfAsyncRotinas;
@Deprecated
public class OrcamentoActivity extends Activity {
	
	private TextView textCodigoOrcamento, textCodigoPessoa, textTotal, textAtacadoVarejo;
	private ListView listViewItemOrcamento;
	private List<ItemOrcamentoBeans> listaItemOrcamento;
	//private List<ItemOrcamentoBeans> listaItemOrcamentoSelecionado = new ArrayList<ItemOrcamentoBeans>();
	private List<Integer> listaItemOrcamentoSelecionado = new ArrayList<Integer>();
	private ItemUniversalAdapter adapterItemOrcamento;
	private String Observacao, 
				   idPessoa,
				   razaoSocial,
				   tipoOrcamentoPedido;
	private int totalItemSelecionado = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orcamento);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		recuperaCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			
			textCodigoOrcamento.setText(intentParametro.getString("ID_AEAORCAM"));
			textTotal.setText("Total");
			idPessoa = intentParametro.getString("ID_CFACLIFO");
			razaoSocial = intentParametro.getString("NOME_RAZAO");
			textAtacadoVarejo.setText(intentParametro.getString("ATAC_VAREJO"));
			
			// Seta o titulo da action bar com a raz�o do cliente
			getActionBar().setTitle(intentParametro.getString("ID_CFACLIFO") + " - " + intentParametro.getString("NOME_RAZAO"));
			
		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoActivity");
			mensagem.put("mensagem", "Não foi possível criar orçamento\n"
					   + "Favor, voltar e selecione novamente um cliente para criar um novo orçamento");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
			funcoes.menssagem(mensagem);
		}
		
		// Torna o listView em multiplas selecao
		listViewItemOrcamento.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		listViewItemOrcamento.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Checa se eh orcamento
				if(tipoOrcamentoPedido.equals("O")){
					
					ItemOrcamentoBeans itemOrcamento = (ItemOrcamentoBeans) parent.getItemAtPosition(position);
					
					ProdutoListaBeans produtoVenda = new ProdutoListaBeans();
					// Instancia a classe de rotinas de produtos
					ProdutoRotinas produtoRotinas = new ProdutoRotinas(OrcamentoActivity.this);
					// Pega os dados do produto
					produtoVenda = produtoRotinas.listaProduto("AEAPRODU.ID_AEAPRODU = " + itemOrcamento.getProduto().getIdProduto(), null, textCodigoOrcamento.getText().toString(), null, null).get(0);
					produtoVenda.setAtacadoVarejo(textAtacadoVarejo.getText().charAt(0));
					
					// Checa se nao esta vazio
					if( (produtoVenda != null) && (produtoVenda.getProduto() != null)){
						
						Bundle bundle = new Bundle();
						bundle.putParcelable("AEAPLOJA", produtoVenda);
						bundle.putParcelable("AEAORCAM", preencheDadosOrcamento());
						bundle.putInt("POSICAO", position);
						bundle.putLong("ID_AEAITORC", itemOrcamento.getIdItemOrcamento());
						bundle.putString("TELA_CHAMADA", "OrcamentoActivity");
						// Abre a tela de detalhes do produto
						Intent intent = new Intent(OrcamentoActivity.this, OrcamentoProdutoDetalhesActivity.class);
						intent.putExtras(bundle);
						
						startActivityForResult(intent, 1);
					} else {
						// Dados da mensagem
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 0);
						mensagem.put("tela", "OrcamentoActivity");
						mensagem.put("mensagem", "N�o foi poss�vel carregar os dados do produto. \n");
						// Instancia a classe  de funcoes para mostra a mensagem
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
						funcoes.menssagem(mensagem);
					}
				
				} else {
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
					// Cria uma variavem para inserir as propriedades da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 2);
					mensagem.put("tela", "ListaOrcamentoPedidoActivity");
					mensagem.put("mensagem", "N�o � um or�amento. \n");
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
					if (tipoOrcamentoPedido.equals("O")){
						
						AlertDialog.Builder builderConfirmacao = new AlertDialog.Builder(OrcamentoActivity.this);
				        builderConfirmacao.setMessage("Tem certeza que deseja excluir o(s) item(ns)?")
				               .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				                   public void onClick(DialogInterface dialog, int id) {
										
				                	   // Instancia a classe para manipular os produto no banco de dados
										ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(OrcamentoActivity.this);
										int totalDeletado = 0;
										for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
											// Deleta o item da lista de item original
											if((itemOrcamentoSql.delete("AEAITORC.ID_AEAITORC = " + listaItemOrcamento.get(listaItemOrcamentoSelecionado.get(i)).getIdItemOrcamento())) > 0){
												totalDeletado ++;
											}
										} // Fim for
										
										// Dados da mensagem
										final ContentValues mensagem = new ContentValues();
										mensagem.put("comando", 2);
										mensagem.put("tela", "OrcamentoActivity");
										
										// Verifica se foi deletado algum registro
										if(totalDeletado > 0){
											mensagem.put("mensagem", totalDeletado + " Deletado(s). \n");
											
											// Atualiza a lista de produtos
											onResume();
											
										}else {
											mensagem.put("mensagem", "N�O FOI POSS�VEL DELETAR OS ITENS SELECIONADOS. \n");
										}
										
										// Instancia a classe  de funcoes para mostra a mensagem
										FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
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
						
					}	else {
						
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
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
				if(listaItemOrcamentoSelecionado == null){
					listaItemOrcamentoSelecionado = new ArrayList<Integer>();
				}
				// Checa se o comando eh de selecao ou descelecao
				if(checked){
					// Incrementa o totalizador
					totalItemSelecionado = totalItemSelecionado + 1; 
					//listaItemOrcamentoSelecionado.add(listaItemOrcamento.get(position));
					listaItemOrcamentoSelecionado.add(position);
					// Mar o adapter para mudar a cor do fundo
					adapterItemOrcamento.getListaItemOrcamento().get(position).setTagSelectContext(true);
					adapterItemOrcamento.notifyDataSetChanged();
					
				}else {
					int i = 0;
					while(i < listaItemOrcamentoSelecionado.size()){
						
						// Checar se a posicao desmacada esta na lista
						if(listaItemOrcamentoSelecionado.get(i) == position){
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
				if(totalItemSelecionado > 1){
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " itens selecionados");
				} else {
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " item selecionado");
				}
			
			}
		});
		
		
	} // FIm onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
		// Pega todos os produtos do orcamento
		listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, textCodigoOrcamento.getText().toString(), null, null);
		
		adapterItemOrcamento = new ItemUniversalAdapter(OrcamentoActivity.this, ItemUniversalAdapter.ITEM_ORCAMENTO, listaItemOrcamento);
		
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
		
		this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.orcamento, menu);
        getMenuInflater().inflate(R.menu.orcamento, menu);
        
        // Configuracao associando item de pesquisa com a SearchView
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		
 		SearchView searchView = (SearchView) menu.findItem(R.id.menu_orcamento_pesquisa).getActionView();
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
 				// Chama a funcao para carregar a lista com todos os produtos
 				//criaListaDeProdutos(null, null, 1);
 				
 				OrcamentoActivity.this.adapterItemOrcamento.getFilter().filter(query);

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
 					
 					OrcamentoActivity.this.adapterItemOrcamento.getFilter().filter(newText);

 					// Seta o adapte com a nova lista, com ou sem o filtro
 					adapterItemOrcamento.setListaItemOrcamento(listaItemOrcamento);
 				}
 				return false;
 			} // Fim do onQueryTextChange
 			
 		}); // Fim do setOnQueryTextListener
        
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case android.R.id.home:
			
			// Abre a tela inicial do sistema
			Intent intentInicio = new Intent(OrcamentoActivity.this, InicioActivity.class);
			// Tira a acitivity da pilha e inicia uma nova
			intentInicio.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intentInicio);
			
			//finish();
			break;
		
		case R.id.menu_orcamento_adicionar:
			
			// Checa se eh um orcamento
			if (this.tipoOrcamentoPedido.equals("O")){
				// Abre a tela inicial do sistema
				Intent intent = new Intent(OrcamentoActivity.this, ProdutoListaActivity.class);
				intent.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
				intent.putExtra("ID_CFACLIFO", idPessoa);
				intent.putExtra("NOME_RAZAO", razaoSocial.replaceFirst("- ", ""));
				intent.putExtra("ATAC_VAREJO", textAtacadoVarejo.getText().toString());
				startActivity(intent);
			
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", "N�o � um or�amento. \n"
						   + "N�o pode ser inserido novos produtos.");
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			
			break;
			
		case R.id.menu_orcamento_enviar_email:
			
			try {
				//Cria novo um ProgressDialogo e exibe
				ProgressDialog progress = new ProgressDialog(OrcamentoActivity.this);
		        progress.setMessage("Aguarde, o PDF est� sendo Gerado...");
		        progress.setCancelable(false);
		        progress.show();
		        
				GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(OrcamentoActivity.this);
				// Envia a lista de produtos que pertence ao orcamento
				gerarPdfRotinas.setListaItensOrcamento(listaItemOrcamento);
				// Envia os dados do orcamento
				gerarPdfRotinas.setOrcamento(preencheDadosOrcamento());
				
				String retornoCaminho = gerarPdfRotinas.criaArquivoPdf();
				
				if(retornoCaminho.length() > 0){
					// Fecha a barra de progresso
					progress.dismiss();
					
					File arquivo = new File(retornoCaminho);
					
					PessoaRotinas pessoaRotinas = new PessoaRotinas(OrcamentoActivity.this);
					
					Intent dadosEmail = new Intent(Intent.ACTION_SEND);
					dadosEmail.setType("message/rfc822");
					dadosEmail.putExtra(Intent.EXTRA_EMAIL  , new String[]{pessoaRotinas.emailPessoa(idPessoa)});
					dadosEmail.putExtra(Intent.EXTRA_SUBJECT, "Or�amento/Pedido de N� " + textCodigoOrcamento.getText());
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
			
		case R.id.menu_orcamento_pesquisa:
			
			break;
			
		case R.id.menu_orcamento_atualizar:
			onResume();
			break;
			
		case R.id.menu_orcamento_observacao:
			
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
			// Pega a obs do banco de dados
			String obs = orcamentoRotinas.selectObservacaoOrcamento(textCodigoOrcamento.getText().toString());
			
			AlertDialogTextoCustom.Builder dialogoObservacao = new AlertDialogTextoCustom.Builder(OrcamentoActivity.this);
			
			LayoutInflater li = LayoutInflater.from(dialogoObservacao.getContext());
			View promptsView = li.inflate(R.layout.layout_dialog_texto, null);
			
			dialogoObservacao.setView(promptsView);
			
			final EditText editObservacao = (EditText) promptsView.findViewById(R.id.layout_dialog_texto_edit_texo);
			// Preenche o dialog com os dados do banco de dados
			editObservacao.setText(obs);
			// Altera o titulo da observacao
			dialogoObservacao.setTitle("Observa��o");
			dialogoObservacao.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Checa se eh um orcamento
					if (tipoOrcamentoPedido.equals("O")){
						// Preenche os dados para salvar no banco de dados
						ContentValues dadosObservacao = new ContentValues();
						dadosObservacao.put("OBS", editObservacao.getText().toString());
						
						OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
						// Inseri a obs no banco de dados
						orcamentoRotinas.updateOrcamento(dadosObservacao, textCodigoOrcamento.getText().toString());
					
					} else {
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
						// Cria uma variavem para inserir as propriedades da mensagem
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 2);
						mensagem.put("tela", "OrcamentoActivity");
						mensagem.put("mensagem", "N�o � um or�amento. \n"
								   + "N�o pode ser inserido/atualizado a observa��o.");
						// Executa a mensagem passando por parametro as propriedades
						funcoes.menssagem(mensagem);
					}
				}
			});
			dialogoObservacao.create();
			dialogoObservacao.show();
			break;
			
		case R.id.menu_orcamento_salvar:
			
			try{
				//ContentValues dadosOrcamento = new ContentValues();
				//dadosOrcamento.put("TIPO_ORCAMENTO", "ORCAMENTO");
				
				GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(OrcamentoActivity.this);
				// Seta(envia) os dados do orcamento
				gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
				// Seta(envia) a lista de produtos do orcamento
				gerarPdfSalvar.setListaItensOrcamento(listaItemOrcamento);
				
				gerarPdfSalvar.execute("");
				
				// Fecha a view
				finish();
				
			}catch (Exception e) {
				//Log.i("thread", e.getMessage());
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 0);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", "N�o Foi poss�vel salvar o orcamento em PDF.");
				mensagem.put("dados", e.toString());
				mensagem.put("usuario", funcoes.getValorXml("Usuario"));
				mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
				mensagem.put("email", funcoes.getValorXml("Email"));
				
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_rateio_preco:
			// Checa se existe produtos no orcamento
			if(listViewItemOrcamento.getCount() > 0){
				
				if(adapterItemOrcamento.getTipoItem() == adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO){
					adapterItemOrcamento.setTipoItem(adapterItemOrcamento.ITEM_ORCAMENTO);
					((BaseAdapter) listViewItemOrcamento.getAdapter()).notifyDataSetChanged();
					
					orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
					
					textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
					
				}else {
					adapterItemOrcamento.setTipoItem(adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO);
					((BaseAdapter) listViewItemOrcamento.getAdapter()).notifyDataSetChanged();
					
					// Variavel para armezenar o total da diferenca entro o preco vendido e o preco de tabela
					double totalDiferenca = 0;
					// Passa por toda a lista de itens
					for(int i = 0; i < listaItemOrcamento.size(); i++){
						totalDiferenca = totalDiferenca + (listaItemOrcamento.get(i).getValorBruto() - listaItemOrcamento.get(i).getValorLiquido());
					}
					// Instancia a classe de funcoes
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
					// Seta o campo com o total da diferenca
					textTotal.setText("Diferen�a: " + funcoes.arredondarValor(String.valueOf(totalDiferenca * (-1))));
				}
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", "N�o existe produtos na lista de orcamento. \n");
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_orcamento_plano_pagamento:
			// Instancia a classe de funcoes
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
			
			// Preenche o ContentValues com os dados da pessoa
			Intent orcamento = new Intent(OrcamentoActivity.this, OrcamentoPlanoPagamentoActivity.class);
			orcamento.putExtra("ID_CFACLIFO",  idPessoa);
			orcamento.putExtra("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
			orcamento.putExtra("ATAC_VAREJO", textAtacadoVarejo.getText().toString());
			orcamento.putExtra("NOME_RAZAO", razaoSocial);
			orcamento.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
			startActivity(orcamento);
			
			break;

		case R.id.menu_orcamento_transformar_pedido:
			
			// Checa se eh um orcamento
			if (tipoOrcamentoPedido.equals("O")){
			
				// Instancia a classe para manipular os orcamento no banco de dados
				OrcamentoSql orcamentoSql = new OrcamentoSql(OrcamentoActivity.this);
				int totalAtualizado = 0;
								
				ContentValues dadosPedido = new ContentValues();
				dadosPedido.put("STATUS", "P");
					
				totalAtualizado = totalAtualizado + orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText());
				
				// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				
				// Verifica se foi deletado algum registro
				if(totalAtualizado > 0){
					mensagem.put("mensagem", totalAtualizado + " Transformado(s) em Pedido(s). \n");
					
					this.tipoOrcamentoPedido = "P";
					
					GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(OrcamentoActivity.this);
					// Seta(envia) os dados do orcamento
					gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
					// Seta(envia) a lista de produtos do orcamento
					gerarPdfSalvar.setListaItensOrcamento(listaItemOrcamento);
					
					gerarPdfSalvar.execute("");
					
					// Fecha a view
					finish();
					
				}else {
					mensagem.put("mensagem", "N�O FOI POSS�VEL TRANSFORMAR O(S) OR�AMENTO(S) EM PEDIDO(S). \n");
				}
				
				// Instancia a classe  de funcoes para mostra a mensagem
				funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
				funcoes.menssagem(mensagem);
			
			} else {
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoActivity");
				mensagem.put("mensagem", " N�o � um or�amento. \n");
				
				funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
				funcoes.menssagem(mensagem);
			}
			break;
			
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	} // FIm onOptionsItemSelected
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
	        if(resultCode == RESULT_OK){
	        	
	        	onResume();
	        }
	    }
	}
	
	private void recuperaCamposTela(){
		textCodigoOrcamento = (TextView) findViewById(R.id.activity_orcamento_text_codigo_orcamento);
		textTotal = (TextView) findViewById(R.id.activity_orcamento_text_total);
		textAtacadoVarejo = (TextView) findViewById(R.id.activity_orcamento_text_atacado_varejo);
		listViewItemOrcamento = (ListView) findViewById(R.id.activity_orcamento_list_produto);
	}
	
	/**
	 * Funcao para retornar os dados do orcamento
	 * @return
	 */
	private OrcamentoBeans preencheDadosOrcamento(){
		OrcamentoBeans orcamento = new OrcamentoBeans();
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(OrcamentoActivity.this);
		
		orcamento.setIdOrcamento(Integer.valueOf(textCodigoOrcamento.getText().toString()));
		orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
		orcamento.setIdPessoa(Integer.valueOf(idPessoa));
		orcamento.setNomeRazao(razaoSocial);
		// Instancia a classe de rotinas do orcamento para manipular os dados com o banco
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
		// Pega a obs do banco de dados
		orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(textCodigoOrcamento.getText().toString()));
		// Pega o total do orcamento no banco de dados
		double total = Double.parseDouble(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()).replace(",", "").replace(".", "")) / 1000;
		// Insere o total do orcamento varaviavel orcamento
		orcamento.setTotalOrcamento(total);
		orcamento.setDataCadastro(orcamentoRotinas.dataCadastroOrcamento(textCodigoOrcamento.getText().toString()));
		
		return orcamento;
	}
	
	/**
	 * Atualiza o campo de total do orcamento.
	 */
	private void totalOrcamento(){
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoActivity.this);
		
		textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
	}
	
	
	

}
