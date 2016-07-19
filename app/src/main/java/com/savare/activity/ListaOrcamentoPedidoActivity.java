package com.savare.activity;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.savare.R;
import com.savare.activity.material.designer.ClienteListaMDActivity;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.adapter.DescricaoSimplesAdapter;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarOrcamentoFtpAsyncRotinas;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
@Deprecated
public class ListaOrcamentoPedidoActivity extends Activity implements OnNavigationListener {
	
	private ListView listViewListaOrcamentoPedido;
	private TextView textTipoOrcamentoPedido;
	private TextView textDataFinal;
	private TextView textDataInicial;
	private Dialog dialogPeriodo;
	private List<Integer> listaItemOrcamentoSelecionado = new ArrayList<Integer>();
	private ItemUniversalAdapter adapterListaOrcamentoPedido;
	private DescricaoSimplesAdapter adapterCidade;
	private String tipoOrcamentoPedido; // O = Orcamento, P = Pedido nao enviados, E = Excluido, N = Pedidos Enviados
	private String retornaValor = "F";
	private String cidade;
	private int totalItemSelecionado = 0;
	private ActionBar actionBar;
	private String tipoOrdem = null;
	double totalDiferenca;
	int anoInicialSelecinado = -1;
    int mesInicialSelecionado = -1;
    int diaInicialSelecionado = -1;
    int anoFinalSelecinado = -1;
    int mesFinalSelecionado = -1;
    int diaFinalSelecionado = -1;
    public static final String TIPO_ORCAMENTO = "O",
    						   TIPO_PEDIDO_NAO_ENVIADO = "P",
    						   TIPO_PEDIDO_ENVIADO = "N",
    						   TIPO_ORCAMENTO_EXCLUIDO = "E",
    						   TIPO_PEDIDO_RETORNADO_BLOQUEADO = "RB",
    						   TIPO_PEDIDO_RETORNADO_LIBERADO = "RL",
							   TIPO_PEDIDO_RETORNADO_EXCLUIDO = "RE",
    						   TIPO_PEDIDO_FATURADO = "F",
							   ITEM_NAO_CONFERIDO = "NC";
    public static final String KEY_TELA_LISTA_ORCAMENTO_PEDIDO = "ListaOrcamentoPedidosActivity",
    						   KEY_TELA_CHAMADA = "TELA_CHAMADA",
							   KEY_RETORNA_VALOR = "RETORNA_VALOR",
							   KEY_ORCAMENTO_PEDIDO = "ORCAMENTO_PEDIDO";
	public static final String TELA_LISTA_PRODUTOS = "LISTA_PRODUTOS";
    public static final int RETORNA_CLIENTE = 100;
    public static final int SOLICITA_CLIENTE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		setContentView(R.layout.activity_lista_orcamento_pedido);
		
		// Pega a actionBar da Activity(tela)
		actionBar = getActionBar();
		// Ativa a action bar com o simbolo de voltar 
		actionBar.setDisplayHomeAsUpEnabled(true);
		//Tira o titulo da action bar
		actionBar.setDisplayShowTitleEnabled(false);
		// Ativa a navegacao suspensa Spinner
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
		// Recupera os campos da tela para manipulalos
		recuperaCampos();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			
			this.tipoOrcamentoPedido = intentParametro.getString(KEY_ORCAMENTO_PEDIDO);
			this.retornaValor = intentParametro.getString(KEY_RETORNA_VALOR);
			
			if(this.retornaValor == null){
				this.retornaValor = "F";
			}
		
		} else {
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "ListaOrcamentoPedidoActivity");
			mensagem.put("mensagem", "Não foi possível carregar o tipo de lista\n");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
			funcoes.menssagem(mensagem);
		}
		
		// Carrega a lista das cidades
		carregarListaCidade();
		
		/*// Instancia a classe para manipular dados do orcamento
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
		
		adapterCidade = new DescricaoSimplesAdapter(ListaOrcamentoPedidoActivity.this, orcamentoRotinas.listaCidadeOrcamentoPedido(this.tipoOrcamentoPedido, null));
		
		// Preenche o spinner da action bar com as cidades
		actionBar.setListNavigationCallbacks(adapterCidade, this);
		// Posiciona o spinner na primeira posicao da lista
		actionBar.setSelectedNavigationItem(0);*/
		
		// Pega o clique do listListaOrcamentoPedido
		listViewListaOrcamentoPedido.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				// Checa se quem chamou essa tela eh para retornar 
				if( (retornaValor != null) && (retornaValor.equals(TELA_LISTA_PRODUTOS)) ){
					
					//Pega os dados da pessoa que foi clicado
					OrcamentoBeans orcamento = (OrcamentoBeans) parent.getItemAtPosition(position);
					
					Bundle bundle = new Bundle();
					bundle.putParcelable("AEAORCAM", orcamento);

					// Cria uma intent para returnar um valor para activity ProdutoLista
					Intent returnIntent = new Intent();
					returnIntent.putExtras(bundle);
										
					setResult(100, returnIntent);
					// Fecha a tela de detalhes de produto
					finish();
					
				} else {
					//Pega os dados da pessoa que foi clicado
					OrcamentoBeans orcamento = (OrcamentoBeans) parent.getItemAtPosition(position);

					/*// Cria uma intent para abrir uma nova activity
					Bundle bundle = new Bundle();
					bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO, String.valueOf(orcamento.getIdOrcamento()));
					bundle.putString(OrcamentoTabulacaoFragment.KEY_NOME_RAZAO, orcamento.getNomeRazao());
					bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_PESSOA, String.valueOf(orcamento.getIdPessoa()));
					bundle.putString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO, String.valueOf(orcamento.getTipoVenda()));

					Intent i = new Intent(ListaOrcamentoPedidoActivity.this, OrcamentoTabulacaoFragment.class);*/


					Bundle bundle = new Bundle();
					bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO, String.valueOf(orcamento.getIdOrcamento()));
					bundle.putString(OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO, orcamento.getNomeRazao());
					bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA, String.valueOf(orcamento.getIdPessoa()));
					bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO, String.valueOf(orcamento.getTipoVenda()));

					Intent i = new Intent(ListaOrcamentoPedidoActivity.this, OrcamentoTabFragmentMDActivity.class);

					i.putExtras(bundle);

					// Abre outra tela
					startActivity(i);
				}
			}
		});
		
		
		
		listViewListaOrcamentoPedido.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Passa por tota a lista de orcamento/pedido
				for (int i = 0; i < adapterListaOrcamentoPedido.getListaOrcamentoPediso().size(); i++) {
					// Mar o adapter para mudar a cor do fundo
					adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(i).setTagSelectContext(false);
				}
				adapterListaOrcamentoPedido.notifyDataSetChanged();
				listaItemOrcamentoSelecionado = null;
				totalItemSelecionado = 0;
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Checa se eh orcamento
				if(tipoOrcamentoPedido.equals("O")){
					// Cria a variavel para inflar o menu de contexto
					MenuInflater menuContext = mode.getMenuInflater();
					menuContext.inflate(R.menu.lista_orcamento_context, menu);
					
				// Checa se eh pedido
				} else if((tipoOrcamentoPedido.equals("P")) || (tipoOrcamentoPedido.equals("N"))){
					// Cria a variavel para inflar o menu de contexto
					MenuInflater menuContext = mode.getMenuInflater();
					menuContext.inflate(R.menu.lista_pedido_context, menu);
					
					// Checa se eh Lixeira(orcamento excluidos)
				} else if(tipoOrcamentoPedido.equals("E")){
					// Cria a variavel para inflar o menu de contexto
					MenuInflater menuContext = mode.getMenuInflater();
					menuContext.inflate(R.menu.lista_lixeira_context, menu);
				}
				return true;
			}
			
			
			
			@Override
			public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
				
				switch (item.getItemId()) {
				
				case R.id.menu_lista_orcamento_context_transformar_pedido:
					// Instancia a classe para manipular os orcamento no banco de dados
					OrcamentoSql orcamentoSql = new OrcamentoSql(ListaOrcamentoPedidoActivity.this);
					int totalAtualizado = 0;
					for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
						
						ContentValues dadosPedido = new ContentValues();
						dadosPedido.put("STATUS", "P");
						
						totalAtualizado = totalAtualizado + orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + 
																				adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
					}
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 2);
					mensagem.put("tela", "ListaOrcamentoPedidoActivity");
					
					// Verifica se foi deletado algum registro
					if(totalAtualizado > 0){
						mensagem.put("mensagem", totalAtualizado + " Transformado(s) em Pedido(s). \n");
						
						// Recarrega a lista de orcamento
						carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
						
						// Pega a posicao da lista
						//int i = actionBar.getSelectedNavigationIndex();
						//onNavigationItemSelected(i, adapterCidade.getItemId(i));
						//onResume();
						
					}else {
						mensagem.put("mensagem", "NÃO FOI POSSÍVEL TRANSFORMAR O(S) ORÇAMENTO(S) EM PEDIDO(S). \n");
					}
					// Esvazia a lista de selecionados
					//listaItemOrcamentoSelecionado = null;
					
					// Instancia a classe  de funcoes para mostra a mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
					funcoes.menssagem(mensagem);
					// Fecha o menu context
					mode.finish();
					
					break;
					
				case R.id.menu_lista_orcamento_context_deletar:
					
					// Checa se eh orcamento
					if(tipoOrcamentoPedido.equals("O")){
						
						AlertDialog.Builder builder = new AlertDialog.Builder(ListaOrcamentoPedidoActivity.this);
				        builder.setMessage("Tem certeza que deseja excluir o(s) pedido(s)?")
				               .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				                   public void onClick(DialogInterface dialog, int id) {

				                	// Instancia a classe para manipular os orcamento no banco de dados
										OrcamentoSql orcamentoSqlDelete = new OrcamentoSql(ListaOrcamentoPedidoActivity.this);
										int totalDeletado = 0;
										for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
											
											ContentValues dadosPedido = new ContentValues();
											dadosPedido.put("STATUS", "E");
											
											totalDeletado = totalDeletado + orcamentoSqlDelete.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + 
																								      adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento()); 
											// Deleta o item da lista de item original
											if(totalDeletado > 0){
												// Remove o item da lista pricipal
												adapterListaOrcamentoPedido.getListaOrcamentoPediso().remove(listaItemOrcamentoSelecionado.get(i));
												// Remove da lista do adapter
												adapterListaOrcamentoPedido.remove(listaItemOrcamentoSelecionado.get(i));
											}
										}
										// Dados da mensagem
										ContentValues mensagemDelete = new ContentValues();
										mensagemDelete.put("comando", 2);
										mensagemDelete.put("tela", "ListaOrcamentoPedidoActivity");
										
										// Verifica se foi deletado algum registro
										if(totalDeletado > 0){
											mensagemDelete.put("mensagem", totalDeletado + " Deletado(s). \n");
											
											// Pega a posicao da lista
											int i = actionBar.getSelectedNavigationIndex();
											onNavigationItemSelected(i, adapterCidade.getItemId(i));
											onResume();
										}else {
											mensagemDelete.put("mensagem", "NÃO FOI POSSÍVEL DELETAR O(S) OR�AMENTO(S) SELECIONADO(S). \n");
										}
										
										// Instancia a classe  de funcoes para mostra a mensagem
										FuncoesPersonalizadas funcoesDelete = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
										funcoesDelete.menssagem(mensagemDelete);
										
										// Fecha o menu context
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
				        builder.create();
						builder.show();
						
						
						
					}
					
					break;
					
				case R.id.menu_lista_lixeira_context_restaurar_orcamento:
					// Instancia a classe para manipular os orcamento no banco de dados
					OrcamentoSql orcamentoSqlLixeira = new OrcamentoSql(ListaOrcamentoPedidoActivity.this);
					int totalLixeira = 0;
					for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
						
						ContentValues dadosPedido = new ContentValues();
						dadosPedido.put("STATUS", "O");
						
						totalLixeira = totalLixeira + orcamentoSqlLixeira.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + 
																				adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
						// Deleta o item da lista de item original
						if(totalLixeira > 0){
							// Remove o item da lista pricipal
							adapterListaOrcamentoPedido.getListaOrcamentoPediso().remove(listaItemOrcamentoSelecionado.get(i));
							// Remove da lista do adapter
							adapterListaOrcamentoPedido.remove(listaItemOrcamentoSelecionado.get(i));
						}
					}
					// Dados da mensagem
					ContentValues mensagemLixeira = new ContentValues();
					mensagemLixeira.put("comando", 2);
					mensagemLixeira.put("tela", "ListaOrcamentoPedidoActivity");
					
					// Verifica se foi deletado algum registro
					if(totalLixeira > 0){
						mensagemLixeira.put("mensagem", totalLixeira + " Recuperado(s). \n");
						
						// Pega a posicao da lista
						int i = actionBar.getSelectedNavigationIndex();
						onNavigationItemSelected(i, adapterCidade.getItemId(i));
						onResume();
						
					}else {
						mensagemLixeira.put("mensagem", "NÃO FOI POSSÍVEL RECUPERAR O(S) OR�AMENTO(S) DELETADO(S). \n");
					}
					
					// Instancia a classe  de funcoes para mostra a mensagem
					FuncoesPersonalizadas funcoesLixeira = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
					funcoesLixeira.menssagem(mensagemLixeira);
					// Fecha o menu context
					mode.finish();
					
					break;
					
				case R.id.menu_lista_orcamento_context_enviar_email:
					
					enviarEmail();
					// Fecha o menu context
					mode.finish();
					break;
					

				case R.id.menu_lista_pedido_context_enviar_email:
					
					enviarEmail();
					// Fecha o menu context
					mode.finish();
					break;
					
				case R.id.menu_lista_pedido_context_enviar_pedido_nuvem:
					/*// Instancia a classe para manipular XML
					GerarXmlOrcamentoRotinas gerarXml = new GerarXmlOrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
					
					for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
						gerarXml.setIdOrcamento(""+adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
						String s = gerarXml.criarArquivoXml();
						s.length();
					}*/
					String[] idOrcamento = new String[listaItemOrcamentoSelecionado.size()];
					for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {
						idOrcamento[i] = String.valueOf(adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
					}
					
					EnviarOrcamentoFtpAsyncRotinas enviarOrcamento = new EnviarOrcamentoFtpAsyncRotinas(ListaOrcamentoPedidoActivity.this);
					enviarOrcamento.execute(idOrcamento);
					
					break;
					
				case R.id.menu_lista_lixeira_context_enviar_email:
					
					enviarEmail();
					// Fecha o menu context
					mode.finish();
					break;
					
				default:
					break;
				}
				return false;
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
					adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(position).setTagSelectContext(true);
					adapterListaOrcamentoPedido.notifyDataSetChanged();
					//listViewListaOrcamentoPedido.getChildAt(listViewListaOrcamentoPedido.getSelectedItemPosition()).setBackgroundColor(getResources().getColor(R.color.cinza_platina));
					
				}else {
					int i = 0;
					while(i < listaItemOrcamentoSelecionado.size()){
						
						// Checar se a posicao desmarcada esta na lista
						if(listaItemOrcamentoSelecionado.get(i) == position){
							// Remove a posicao da lista de selecao
							listaItemOrcamentoSelecionado.remove(i);
							// Diminui o total de itens selecionados
							totalItemSelecionado = totalItemSelecionado - 1;
							
							// Mar o adapter para mudar a cor do fundo
							adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(position).setTagSelectContext(false);
							adapterListaOrcamentoPedido.notifyDataSetChanged();
						}
						// Incrementa a variavel
						i++;
					}
				}
				// Checa se tem mais de um item selecionados
				if(totalItemSelecionado > 1){
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " selecionados");
				} else {
					// Muda o titulo do menu de contexto quando seleciona os itens
					mode.setTitle(totalItemSelecionado + " selecionado");
				}
			}
		});
		
	} // FIm onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Pega a palavra que eh para ser removida
		String remover = adapterCidade.getLista().get(actionBar.getSelectedNavigationIndex()).getTextoPrincipal().substring(0, 5);
		// Pega o nome da cidade
		cidade = adapterCidade.getLista().get(actionBar.getSelectedNavigationIndex()).getTextoPrincipal().replaceAll(remover, "");
		if(cidade.equals(" as Cidades")){
			cidade = null;
		}
		// Calcula o total da lista e exibe
		calculaTotal();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.lista_orcamento, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			
			finish();
			break;
			
		case R.id.menu_lista_orcamento_atualizar:
			
			carregarListaCidade();
			break;
			
		case R.id.menu_lista_orcamento_rateio_preco:
			// Checa se existe produtos no orcamento
			if(adapterListaOrcamentoPedido.getListaOrcamentoPediso().size() > 0){
				
				if(adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO){
					// Muda o tipo de listagem do adapter
					adapterListaOrcamentoPedido.setTipoItem(adapterListaOrcamentoPedido.LISTA_ORCAMENTO_PEDIDO);
					((BaseAdapter) listViewListaOrcamentoPedido.getAdapter()).notifyDataSetChanged();
					
				}else {
					
					// Muda o tipo de listagem do adapter
					adapterListaOrcamentoPedido.setTipoItem(adapterListaOrcamentoPedido.RATEIO_ORCAMENTO);
					((BaseAdapter) listViewListaOrcamentoPedido.getAdapter()).notifyDataSetChanged();
					
					// Cria uma vareavel para pegar a lista de orcamentos
					List<OrcamentoBeans> listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();
					// Pega a lista de orcamento
					listaOrcamentoPedido = adapterListaOrcamentoPedido.getListaOrcamentoPediso();
					
					// Variavel para armezenar o total da diferenca entro o preco vendido e o preco de tabela
					this.totalDiferenca = 0;
					// Passa por toda a lista de itens
					for(int i = 0; i < listaOrcamentoPedido.size(); i++){
						this.totalDiferenca = this.totalDiferenca + (listaOrcamentoPedido.get(i).getTotalOrcamentoBruto() - listaOrcamentoPedido.get(i).getTotalOrcamento());
					}
					
					
				}
				
			} else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "ListaOrcamentoPedidoActivity");
				mensagem.put("mensagem", "N�o existe produtos na lista de orcamento. \n");
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}
			
			onResume();
			break;
			
		case R.id.menu_lista_orcamento_filtrar_periodo:
			
			dialogPeriodo = new Dialog(ListaOrcamentoPedidoActivity.this);
			// Seta o layout customizado para o dialog
			dialogPeriodo.setContentView(R.layout.layout_dialog_periodo_data);
			// Seta o titulo do dialog
			dialogPeriodo.setTitle(getResources().getString(R.string.periodo));
			dialogPeriodo.setCancelable(true);

			// Associa o campo do dialog customizado
			textDataInicial = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_inicial);
			
			// Checa se ja existe alguma data selecionada
			if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
				textDataInicial.setText(diaInicialSelecionado + "/" + mesInicialSelecionado + "/" + anoInicialSelecinado);
			}
			
			// Desailita a edicao do campo
			textDataInicial.setFocusable(false);
			// Pega os clique do campo
			textDataInicial.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Process to get Current Date
		            final Calendar c = Calendar.getInstance();
		            
					if(anoInicialSelecinado < 0){
						anoInicialSelecinado = c.get(Calendar.YEAR);
					}
					if(mesInicialSelecionado < 0){
						mesInicialSelecionado = c.get(Calendar.MONTH);
					}
		            if(diaInicialSelecionado < 0){
		            	diaInicialSelecionado = c.get(Calendar.DAY_OF_MONTH);
		            }
		            
					// Launch Date Picker Dialog
		            DatePickerDialog dataDialog = new DatePickerDialog(ListaOrcamentoPedidoActivity.this, new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							// Preenche o campo com a data
							textDataInicial.setText(dayOfMonth+"/" + (monthOfYear + 1) + "/"+year);
							// Seta o calendario com a data selecionada
							//c.set(year, monthOfYear + 1, dayOfMonth);
							
							anoInicialSelecinado = year;
							mesInicialSelecionado = (monthOfYear + 1);
							diaInicialSelecionado = dayOfMonth;
						}
					}, anoInicialSelecinado, mesInicialSelecionado, diaInicialSelecionado);
		            dataDialog.show();
				}
			});
			
			textDataFinal = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_final);
			// Checa se ja existe alguma data selecionada
			if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
				textDataFinal.setText(diaFinalSelecionado + "/" + mesFinalSelecionado + "/" + anoFinalSelecinado);
			}
			textDataFinal.setFocusable(false);
			textDataFinal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Process to get Current Date
		            final Calendar c = Calendar.getInstance();
		            
		            if(anoFinalSelecinado < 0){
		            	anoFinalSelecinado = c.get(Calendar.YEAR);
		            }
		            if(mesFinalSelecionado < 0){
		            	mesFinalSelecionado = c.get(Calendar.MONTH);
		            }
		            if(diaFinalSelecionado < 0){
		            	diaFinalSelecionado = c.get(Calendar.DAY_OF_MONTH);
		            }
					// Launch Date Picker Dialog
		            DatePickerDialog dataDialog = new DatePickerDialog(ListaOrcamentoPedidoActivity.this, new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							textDataFinal.setText(dayOfMonth+"/"+ (monthOfYear + 1)+"/"+year);
							//c.set(year, monthOfYear, dayOfMonth);
							//dataFormatadaFinal.setCalendar(c);
							anoFinalSelecinado = year;
							mesFinalSelecionado = monthOfYear + 1;
							diaFinalSelecionado = dayOfMonth;
						}
					}, anoInicialSelecinado, mesInicialSelecionado, diaInicialSelecionado);
		            dataDialog.show();
				}
			});
			
			Button buttonCancelar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_cancelar);
			buttonCancelar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Fecha o dialog com seletor de data
					dialogPeriodo.dismiss();
					
					anoInicialSelecinado = -1;
					mesInicialSelecionado = -1;
					diaInicialSelecionado = -1;
					
					anoFinalSelecinado = -1;
					mesFinalSelecionado = -1;
					diaInicialSelecionado = -1;
					
					carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
				}
			});
			
			
			Button buttonFiltrar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_filtrar);
			buttonFiltrar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
					dialogPeriodo.dismiss();
				}
			});
			
			dialogPeriodo.show();
			
			break;
			
		case R.id.menu_lista_orcamento_ordem_decrescente:
			// Seta a ordem decrescente
			tipoOrdem = OrcamentoRotinas.ORDEM_DECRESCENTE;
			// Recarrega a lista de orcamento pedidos
			carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
			
			adapterListaOrcamentoPedido.notifyDataSetChanged();
			break;
			
		case R.id.menu_lista_orcamento_ordem_crescente:
			// Seta a ordem decrescente
			tipoOrdem = OrcamentoRotinas.ORDEM_CRESCENTE;
			// Recarrega a lista de orcamento pedidos
			carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
			
			break;
		
		case R.id.menu_lista_orcamento_somente_enviados:
			
			if(!tipoOrcamentoPedido.equalsIgnoreCase("O")){
				// Checa se nao esta na lista de pedidos enviados
				if(!tipoOrcamentoPedido.equalsIgnoreCase("N")){
					tipoOrcamentoPedido = "N";
					// Recarrega a lista de cidades
					carregarListaCidade();
					// Recarrega a lista
					carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
				
				} else {
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 1);
					mensagem.put("tela", "ListaOrcamentoPedidoActivity");
					mensagem.put("mensagem", "Voc� j� esta na lista de pedidos enviados.\n");
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
					funcoes.menssagem(mensagem);
				}
				
			} else {
				// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 1);
				mensagem.put("tela", "ListaOrcamentoPedidoActivity");
				mensagem.put("mensagem", "Filtro permitido apenas para a lista de pedidos.\n");
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_lista_orcamento_nao_enviados:
			
			if(!tipoOrcamentoPedido.equalsIgnoreCase("O")){
				// Checa se nao esta na lista de pedidos nao enviados
				if(!tipoOrcamentoPedido.equalsIgnoreCase("P")){
					tipoOrcamentoPedido = "P";
					// Recarrega a lista de cidades
					carregarListaCidade();
					// Recarrega a lista
					carregarListaOrcamentoPedido(actionBar.getSelectedNavigationIndex());
				
				} else {
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 1);
					mensagem.put("tela", "ListaOrcamentoPedidoActivity");
					mensagem.put("mensagem", "Você já esta na lista de pedidos NÃO enviados.\n");
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
					funcoes.menssagem(mensagem);
				}
			} else {
				// Dados da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 1);
				mensagem.put("tela", "ListaOrcamentoPedidoActivity");
				mensagem.put("mensagem", "Filtro permitido apenas para a lista de pedidos.\n");
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				funcoes.menssagem(mensagem);
			}
			break;
			
		case R.id.menu_lista_orcamento_novo_orcamento:
			// Abre a tela de detalhes do produto
			Intent intent = new Intent(ListaOrcamentoPedidoActivity.this, ClienteListaMDActivity.class);
			intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_LISTA_ORCAMENTO_PEDIDO);
			// Abre a activity aquardando uma resposta
			startActivityForResult(intent, SOLICITA_CLIENTE);
			break;
			
		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Checa se eh um retorno
		if(requestCode == SOLICITA_CLIENTE){
			// Checa se eh um retorno da tela de clientes
			if(resultCode == RETORNA_CLIENTE){
				
				final Intent dadosRetornado = data;
				
				// Cria um dialog para selecionar atacado ou varejo
				AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(ListaOrcamentoPedidoActivity.this);
				// Atributo(variavel) para escolher o tipo da venda
				final String[] opcao = {"Atacado", "Varejo"};
				// Preenche o dialogo com o titulo e as opcoes
				mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// Preenche o ContentValues com os dados da pessoa
						ContentValues dadosCliente = new ContentValues();
						dadosCliente.put("ID_CFACLIFO", dadosRetornado.getStringExtra("ID_CFACLIFO"));
						dadosCliente.put("ID_CFAESTAD", dadosRetornado.getStringExtra("ID_CFAESTAD"));
						dadosCliente.put("ID_CFACIDAD", dadosRetornado.getStringExtra("ID_CFACIDAD"));
						dadosCliente.put("ID_SMAEMPRE", dadosRetornado.getStringExtra("ID_SMAEMPRE"));
						dadosCliente.put("GUID", UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16));
						dadosCliente.put("ATAC_VAREJO", which);
						dadosCliente.put("PESSOA_CLIENTE", dadosRetornado.getStringExtra("PESSOA_CLIENTE"));
						dadosCliente.put("NOME_CLIENTE", dadosRetornado.getStringExtra("NOME_CLIENTE"));
						dadosCliente.put("IE_RG_CLIENTE", dadosRetornado.getStringExtra("IE_RG_CLIENTE"));
						dadosCliente.put("CPF_CGC_CLIENTE", dadosRetornado.getStringExtra("CPF_CGC_CLIENTE"));
						dadosCliente.put("ENDERECO_CLIENTE", dadosRetornado.getStringExtra("ENDERECO_CLIENTE"));
						dadosCliente.put("BAIRRO_CLIENTE", dadosRetornado.getStringExtra("BAIRRO_CLIENTE"));
						dadosCliente.put("CEP_CLIENTE", dadosRetornado.getStringExtra("CEP_CLIENTE"));
						/*dadosCliente.put("LATITUDE", localizacao.getLatitude());
						dadosCliente.put("LONGITUDE", localizacao.getLongitude());
						dadosCliente.put("ALTITUDE", localizacao.getAltitude());
						dadosCliente.put("HORARIO_LOCALIZACAO", localizacao.getHorarioLocalizacao());
						dadosCliente.put("TIPO_LOCALIZACAO", localizacao.getTipoLocalizacao());
						dadosCliente.put("PRECISAO", localizacao.getPrecisao());*/

						OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
						// Cria um novo orcamento no banco de dados
						long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);
						
						// Verifica se retornou algum numero
						if(numeroOracmento > 0){
							
							/*Bundle bundle = new Bundle();
							bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
							bundle.putString(OrcamentoTabulacaoFragment.KEY_NOME_RAZAO, dadosRetornado.getStringExtra("NOME_CLIENTE"));
							bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_PESSOA, dadosRetornado.getStringExtra("ID_CFACLIFO"));
							bundle.putString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO, String.valueOf(which));
							bundle.putString("AV", "0");
							
							Intent i = new Intent(ListaOrcamentoPedidoActivity.this, OrcamentoTabulacaoFragment.class);
							i.putExtras(bundle);*/

							Bundle bundle = new Bundle();
							bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
							bundle.putString(OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO, dadosRetornado.getStringExtra("NOME_CLIENTE"));
							bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA, dadosRetornado.getStringExtra("ID_CFACLIFO"));
							bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO, String.valueOf(which));
							bundle.putString("AV", "0");

							Intent i = new Intent(ListaOrcamentoPedidoActivity.this, OrcamentoTabFragmentMDActivity.class);
							i.putExtras(bundle);
							
							// Abre outra tela
							startActivity(i);
						}
					}});
				
				// Faz a mensagem (dialog) aparecer
				mensagemAtacadoVarejo.show();
				
				
				
				
			}
		}
	}
	
	private void recuperaCampos(){
		listViewListaOrcamentoPedido = (ListView) findViewById(R.id.activity_lista_orcamento_pedido_list_orcamento_pedido);
		listViewListaOrcamentoPedido.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		textTipoOrcamentoPedido = (TextView) findViewById(R.id.activity_lista_orcamento_pedido_text_tipo_orcamento_pedido);
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		carregarListaOrcamentoPedido(itemPosition);
		
		return false;
	} // Fim onNavigationItemSelected
	
	private void enviarEmail(){
		//Cria novo um ProgressDialogo e exibe
		ProgressDialog progress = new ProgressDialog(ListaOrcamentoPedidoActivity.this);
		progress.setIndeterminate(true);
		progress.setTitle("Enviar e-mail");
        progress.setMessage("Aguarde, todos os PDF est�o sendo gerados.\n"
        			      + " Levar� um pouquinho mais de tempo.");
        progress.setCancelable(true);
        
        progress.show();
        
        ArrayList<Uri> listaCaminho = new ArrayList<Uri>();;
        
        OrcamentoBeans orcamento = null;
        		
        for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
        	orcamento = new OrcamentoBeans();
        	
        	// Pega os dados do orcamento
        	orcamento = (OrcamentoBeans) listViewListaOrcamentoPedido.getItemAtPosition(listaItemOrcamentoSelecionado.get(i));
        	
        	// Instancia a classe para manipulas os dado do orcamento
        	OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
        	
        	orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(""+orcamento.getIdOrcamento()));
        	
        	// Instancia a classe responsavel por criar o pdf
        	GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(ListaOrcamentoPedidoActivity.this);
    		// Envia os dados do orcamento
    		gerarPdfRotinas.setOrcamento(orcamento);
    		// Envia a lista de produtos que pertence ao orcamento
    		gerarPdfRotinas.setListaItensOrcamento(orcamentoRotinas.listaItemOrcamentoResumida(null, ""+orcamento.getIdOrcamento(), null, null));
    		
    		// Cria o pdf e pega o caminho do arquivo
    		String retornoCaminho = gerarPdfRotinas.criaArquivoPdf();
    		
    		// Checa se existe algum caminho
    		if(retornoCaminho.length() > 0){
    			// Adiciona o caminha a uma lista
    			listaCaminho.add(Uri.fromFile(new File(retornoCaminho)));
    		}
    		
        } // Fim for
        
        // Fecha a barra de progresso	
		progress.dismiss();
        
        if(listaCaminho.size() > 0){
							
			PessoaRotinas pessoaRotinas = new PessoaRotinas(ListaOrcamentoPedidoActivity.this);
			
			Intent dadosEmail = new Intent(Intent.ACTION_SEND_MULTIPLE);
			//dadosEmail.setType("text/plai");
			dadosEmail.setType("message/rfc822");
			dadosEmail.putExtra(Intent.EXTRA_EMAIL  , new String[]{pessoaRotinas.emailPessoa(""+orcamento.getIdPessoa())});
			dadosEmail.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.orcamento_pedido_numero) + orcamento.getIdOrcamento());
			dadosEmail.putExtra(Intent.EXTRA_STREAM, listaCaminho);
			dadosEmail.putExtra(Intent.EXTRA_TEXT   , "E-Mail enviado pelo App SAVARE.");
			
			try {
			    startActivity(Intent.createChooser(dadosEmail, "Enviar e-mail..."));
			
			} catch (android.content.ActivityNotFoundException ex) {
			    //Toast.makeText(OrcamentoActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			}
		}
	} // Fim enviar email

	
	/**
	 * Funcao para calcular o total de varios orcamento.
	 * 
	 */
	private void calculaTotal(){
		// Instancia a classe para manipular dados do orcamento
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
		
		// Pega o periodo selecionado no menu
		String periodo = wherePeriodoData();
		
		// Checa se faz parte da lista de pedido
		if ((this.tipoOrcamentoPedido.equals(TIPO_PEDIDO_NAO_ENVIADO)) || (this.tipoOrcamentoPedido.equals(TIPO_PEDIDO_ENVIADO))){
			// Muda a cor do actionBar
			actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.laranja_escuro)));
			
			// Checa se esta visualizando rateio de preco
			if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
											    " Pedido(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) + 
											    " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
											    " | Dif.: " + funcoes.arredondarValor(totalDiferenca));
				
			} else {
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
					    					    " Pedido(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
			}
			
			// Checa se faz parte da lista de orcamento
		} else if(this.tipoOrcamentoPedido.equals(TIPO_ORCAMENTO)){
			// Muda a cor do actionBar
			actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.verde_escuro)));
			
			// Checa se esta visualizando rateio de preco
			if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
											    " Orçamento(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) +
											    " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
											    " | Dif.: " + funcoes.arredondarValor(totalDiferenca));
			} else {
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
					    					    " Orçamento(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
			}
			
			// Checa se faz parte da lista de orcamentos excluidos
		} else if (this.tipoOrcamentoPedido.equals(TIPO_ORCAMENTO_EXCLUIDO)){
			// Muda a cor do actionBar
			actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.vermelho_escuro)));
			
			// Checa se esta visualizando rateio de preco
			if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){
				
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoActivity.this);
				
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
											    " Excluido(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) + 
											    " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
											    " | Dif.: " + funcoes.arredondarValor(totalDiferenca));
				
			} else {
				textTipoOrcamentoPedido.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) + 
					    					    " Excluido(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
			}
		} // Fim if (this.tipoOrcamentoPedido.equals("E"))
	}

	private void carregarListaCidade(){
		// Instancia a classe para manipular dados do orcamento
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);

		ItemUniversalAdapter adapterCidadePadrao = new ItemUniversalAdapter(ListaOrcamentoPedidoActivity.this, ItemUniversalAdapter.CIDADE);
		adapterCidadePadrao.setListaCidade(orcamentoRotinas.listaCidadeOrcamentoPedido(this.tipoOrcamentoPedido, null));
		//adapterCidade = new DescricaoSimplesAdapter(ListaOrcamentoPedidoActivity.this, orcamentoRotinas.listaCidadeOrcamentoPedido(this.tipoOrcamentoPedido, null));
		
		// Preenche o spinner da action bar com as cidades
		actionBar.setListNavigationCallbacks(adapterCidadePadrao, this);
		// Posiciona o spinner na primeira posicao da lista
		actionBar.setSelectedNavigationItem(0);
	}


	/**
	 * Carrega a lista completa dos or�amentos/pedidos de acordo com o
	 * periodo selecionado e a cidade selecionada.
	 * 
	 * @param itemPosition
	 */
	private void carregarListaOrcamentoPedido(int itemPosition){
		// Instancia a classe de orcamento para manipular dados do orcamento
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoActivity.this);
		
		// Cria uma variavel para armazenar a lista de orcamento
		List<OrcamentoBeans> listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();
		
		// Checa se esta selecionado todos os orcamento/pedido
		if(adapterCidade.getLista().get(itemPosition).getTextoPrincipal().equalsIgnoreCase("Todas as Cidades")) {
			
			// Preenche a lista de pessoas
			listaOrcamentoPedido = orcamentoRotinas.listaOrcamentoPedido(new String[]{tipoOrcamentoPedido}, wherePeriodoData(), tipoOrdem);
		} else {
			// Pega a palavra que eh para ser removida
			String remover = adapterCidade.getLista().get(itemPosition).getTextoPrincipal().substring(0, 5);
			
			// Monta a clausula where do sql
			String where = "CFACIDAD.DESCRICAO = '" + adapterCidade.getLista().get(itemPosition).getTextoPrincipal().replaceAll(remover, "") + "'";
			
			String periodo = wherePeriodoData();
			
			if((periodo != null) && (periodo.length() > 0)){
				where += " AND " + periodo;
			}
			
			// Instancia a classe
			listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();
			// Preenche a lista de pessoas
			listaOrcamentoPedido = orcamentoRotinas.listaOrcamentoPedido(new String[]{tipoOrcamentoPedido}, where, tipoOrdem);
		}
		
		// Seta o adapter com a nova lista
		adapterListaOrcamentoPedido = new ItemUniversalAdapter(ListaOrcamentoPedidoActivity.this, ItemUniversalAdapter.LISTA_ORCAMENTO_PEDIDO);
		// Preenche o adapter com a lista de orcamento
		adapterListaOrcamentoPedido.setListaOrcamentoPedido(listaOrcamentoPedido);
		// Seta o listView com o novo adapter que ja esta com a nova lista
		listViewListaOrcamentoPedido.setAdapter(adapterListaOrcamentoPedido);
		
		onResume();
	}
	
	
	/**
	 * Pega o periodo que foi selecionado no menu.
	 * 
	 * @return
	 */
	private String wherePeriodoData(){
		String where = null;
		
		// Formata a data
		DateFormat dataFormatadaInicial = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dataFormatadaFinal = new SimpleDateFormat("yyyy-MM-dd");
		
		// Checa se existe data inicial para pesquisar
		if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
			Calendar c = Calendar.getInstance();
			
			c.set(anoInicialSelecinado, mesInicialSelecionado - 1, diaInicialSelecionado);
			
			// Preenche a dataFormatada com a data salva
			dataFormatadaFinal.setCalendar(c);
			
			where = " (DT_CAD >= '" + dataFormatadaInicial.format(c.getTime()) + " 00:00:00')";
		}
		
		// Checa se existe data final para pesquisar
		if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
			Calendar c = Calendar.getInstance();
			
			c.set(anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);
			
			dataFormatadaFinal.setCalendar(c);
			
			if((where != null) && (where.length() > 0)){
				where += " AND (DT_CAD <= '" + dataFormatadaFinal.format(c.getTime()) + " 23:59:59')";
			} else {
				where = " (DT_CAD <= '" + dataFormatadaFinal.format(c.getTime()) + " 23:59:59')";
			}
		}
		return where;
	}
	
	
	
}
