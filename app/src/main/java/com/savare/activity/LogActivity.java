package com.savare.activity;

import java.util.List;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.LogBeans;
import com.savare.funcoes.rotinas.LogRotinas;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

public class LogActivity extends Activity implements OnNavigationListener{

	private ListView listViewLogs;
	private ItemUniversalAdapter adapterListaLogs;
	private ItemUniversalAdapter adapterTabela;
	private List<LogBeans> listaLog;
	private List<LogBeans> listaTabela;
	private ActionBar actionBar;
	private String idOrcamento;
	private String[] tabela;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		listViewLogs = (ListView) findViewById(R.id.activity_log_listView_logs);
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			
			idOrcamento = (intentParametro.getString("ID_AEAORCAM"));
			tabela = intentParametro.getStringArray("TABELA");
		}
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		
		// Seta o titulo da action bar com a raz�o do cliente
		actionBar.setTitle("Orçamento/Pedido " + idOrcamento);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		//Tira o titulo da action bar
		actionBar.setDisplayShowTitleEnabled(false);
		// Ativa a navegacao suspensa Spinner
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        LogRotinas logRotinas = new LogRotinas(LogActivity.this);
        
        adapterTabela = new ItemUniversalAdapter(LogActivity.this, ItemUniversalAdapter.TABELA_LOG);
        
        if(tabela != null){
        	
        	adapterTabela.setListaTabelaLog(logRotinas.listaTabela(null, tabela));
        } else {
        	adapterTabela.setListaTabelaLog(logRotinas.listaTabela(null, null));
        }
        
        // Preenche o spinner da action bar com as cidades
 		actionBar.setListNavigationCallbacks(adapterTabela, this);
     		
 		// Posiciona o spinner na primeira posicao da lista
 		actionBar.setSelectedNavigationItem(0);
 		
	} // Fim onCreate
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			//Intent intent = new Intent(LogActivity.this, InicioActivity.class);
			// Tira a acitivity da pilha e inicia uma nova
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			//startActivity(intent);
			finish();
			break;
			
		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected
	
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String where = "";
		
		if(adapterTabela.getListaTabelaLog().size() > 0){
			where = "LOG.TABELA = '" + adapterTabela.getListaTabelaLog().get(itemPosition).getTabela() + "'";
			
			if(idOrcamento != null){
				where += " AND LOG.ID_TABELA = " + idOrcamento;
			}
			
			LogRotinas logRotinas = new LogRotinas(LogActivity.this);
			
			listaLog = logRotinas.listaLogPorTabela(where);
			
			if(listaLog != null){
				adapterListaLogs = new ItemUniversalAdapter(LogActivity.this, ItemUniversalAdapter.LOG);
				adapterListaLogs.setListaLog(listaLog);
				
				listViewLogs.setAdapter(adapterListaLogs);
			}
		}
		return false;
	}
}
