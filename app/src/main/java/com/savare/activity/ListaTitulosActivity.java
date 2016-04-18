package com.savare.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnQueryTextListener;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.adapter.ListaTitulosExpandableAdapter;
import com.savare.adapter.PessoaAdapter;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.TitulosListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ParcelaRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
@Deprecated
public class ListaTitulosActivity extends Activity {
	
	private TextView textTotalAReceber,
					 textTotalCredito;
	private TextView textDataFinal;
	private TextView textDataInicial;
	private Dialog dialogPeriodo;
	private ExpandableListView expandagleListaTitulos;
	private List<TitulosListaBeans> listaTitulos;
	private ListaTitulosExpandableAdapter adapterListaTitulos;
	private String idPessoa,
				   whereFiltroAuxiliar;
	private char tipoListagem = '0'; // 0 = Titulos em Aberto, 1 = Titulos Baixado, 2 = Titulos em Aberto Vencidos 
	private char pagarReceber = '0'; // 0 = Receber, 1 = Pagar
	int anoInicialSelecinado = -1;
    int mesInicialSelecionado = -1;
    int diaInicialSelecionado = -1;
    int anoFinalSelecinado = -1;
    int mesFinalSelecionado = -1;
    int diaFinalSelecionado = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_titulos);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Lista de Titulos");
		
		recuperaCampos();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			idPessoa = intentParametro.getString("ID_CFACLIFO");
			
		} else {
			idPessoa = "";
		}
		
		ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosActivity.this);
		
		listaTitulos = new ArrayList<TitulosListaBeans>();
		listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, null);
		
		adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosActivity.this, listaTitulos);
		expandagleListaTitulos.setAdapter(adapterListaTitulos);
		
	}  // fim onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosActivity.this);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaTitulosActivity.this);
		
		textTotalAReceber.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '0', whereFiltroAuxiliar)));
		textTotalCredito.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '1', whereFiltroAuxiliar)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.lista_titulos, menu);
		
		if((idPessoa != null) && (idPessoa.length() < 0)){
			menu.getItem(R.menu.lista_titulos).setVisible(false);
		}
		// Configuracao associando item de pesquisa com a SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_lista_titulos_search_pesquisar).getActionView();
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
						     + "CFAENDER.BAIRRO LIKE '%" + query + "%' OR "
						     + "CFASTATU.DESCRICAO LIKE '%" + query + "%' ";
				// Pega a where e salva
				whereFiltroAuxiliar = where;
				
				ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosActivity.this);
				
				listaTitulos = new ArrayList<TitulosListaBeans>();
				listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, where);
				
				// Atualiza o adapter com uma nova lista
				adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosActivity.this, listaTitulos);
				expandagleListaTitulos.setAdapter(adapterListaTitulos);
				
				onResume();
				
				return false;
			} // Fim do onQueryTextSubmit

			/**
			 * Pega todo o texto digitado
			 */
			@Override
			public boolean onQueryTextChange(String newText) {

				if(newText.length() <= 0){
					whereFiltroAuxiliar = "";
				}
				return false;
			} // Fim do onQueryTextChange
		}); // Fim do setOnQueryTextListener
		
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosActivity.this);

		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			break;
			
		case R.id.menu_lista_titulos_vencidos:
			
			listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '2', pagarReceber, whereFiltroAuxiliar);
			adapterListaTitulos.setListaPessoasParent(listaTitulos);
			expandagleListaTitulos.setAdapter(adapterListaTitulos);
			onResume();
			break;
			
		case R.id.menu_lista_titulos_aberto:
			
			listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '0', pagarReceber, whereFiltroAuxiliar);
			adapterListaTitulos.setListaPessoasParent(listaTitulos);
			expandagleListaTitulos.setAdapter(adapterListaTitulos);
			onResume();
			break;
			
		case R.id.menu_lista_titulos_baixados:
			
			listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '1', '0', whereFiltroAuxiliar);
			adapterListaTitulos.setListaPessoasParent(listaTitulos);
			expandagleListaTitulos.setAdapter(adapterListaTitulos);
			onResume();
			break;
			
		case R.id.menu_lista_titulos_credito:
			
			listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '0', '1', whereFiltroAuxiliar);
			adapterListaTitulos.setListaPessoasParent(listaTitulos);
			expandagleListaTitulos.setAdapter(adapterListaTitulos);
			onResume();
			break;
			
		case R.id.menu_lista_titulos_filtrar_periodo:

			dialogPeriodo = new Dialog(ListaTitulosActivity.this);
			// Seta o layout customizado para o dialog
			dialogPeriodo.setContentView(R.layout.layout_dialog_periodo_data);
			// Seta o titulo do dialog
			dialogPeriodo.setTitle("Período");
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
		            DatePickerDialog dataDialog = new DatePickerDialog(ListaTitulosActivity.this, new OnDateSetListener() {
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
		            DatePickerDialog dataDialog = new DatePickerDialog(ListaTitulosActivity.this, new OnDateSetListener() {
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
					
					carregarListaTitulosPeriodo();
				}
			});
			
			
			Button buttonFiltrar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_filtrar);
			buttonFiltrar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					carregarListaTitulosPeriodo();
					dialogPeriodo.dismiss();
				}
			});
			
			dialogPeriodo.show();
			break;
		default:
				break;
		}
		return true;
	} // Fim
	
	
	private void recuperaCampos(){
		textTotalAReceber = (TextView) findViewById(R.id.activity_lista_titulos_text_total_a_receber);
		textTotalCredito = (TextView) findViewById(R.id.activity_lista_titulos_text_credito);
		expandagleListaTitulos = (ExpandableListView) findViewById(R.id.activity_lista_titulos_expandable_lista_titulos);
	} // Fim do recuperaCampos
	
	
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
			dataFormatadaInicial.setCalendar(c);
			
			where = " (DT_VENCIMENTO >= '" + dataFormatadaInicial.format(c.getTime()) + "')";
		}
		
		// Checa se existe data final para pesquisar
		if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
			Calendar c = Calendar.getInstance();
			
			c.set(anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);
			
			dataFormatadaFinal.setCalendar(c);
			
			if((where != null) && (where.length() > 0)){
				where += " AND (DT_VENCIMENTO <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
			} else {
				where = " (DT_VENCIMENTO <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
			}
		}
		return where;
	}
	
	
	/**
	 * Carrega a lista completa dos or�amentos/pedidos de acordo com o
	 * periodo selecionado e a cidade selecionada.
	 * 
	 * @param itemPosition
	 */
	private void carregarListaTitulosPeriodo(){
		
		String wherePeriodo = wherePeriodoData();
		
		ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosActivity.this);
		
		listaTitulos = new ArrayList<TitulosListaBeans>();
		listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, wherePeriodo);
		// Seta o adapter com uma nova lista
		adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosActivity.this, listaTitulos);
		expandagleListaTitulos.setAdapter(adapterListaTitulos);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaTitulosActivity.this);
		// Calcula os campos totais
		textTotalAReceber.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '0', wherePeriodo)));
		textTotalCredito.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '1', wherePeriodo)));
	}
	
}
