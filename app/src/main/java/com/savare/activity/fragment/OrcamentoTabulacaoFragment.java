package com.savare.activity.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.savare.R;
import com.savare.activity.InicioActivity;
import com.savare.activity.ProdutoListaActivity;
import com.savare.activity.material.designer.InicioMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.adapter.OrcamentoTabulacaoFragmentAdapter;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.GPSTracker;
import com.savare.funcoes.LocalizacaoFuncoes;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
@Deprecated
public class OrcamentoTabulacaoFragment extends FragmentActivity {

	public static final String KEY_ID_ORCAMENTO = "ID_ORCAMENTO",
							   KEY_ATACADO_VAREJO = "AEACADO_VAREJO",
							   KEY_NOME_RAZAO = "NOME_RAZAO",
							   KEY_ID_PESSOA = "ID_PESSOA";
	ViewPager viewPagerOrcamentoMain;
	ActionBar actionBar;
	OrcamentoTabulacaoFragmentAdapter adapterOrcamentoTabulacao;
	private String idOrcamento,
				   atacadoVarejo = "0",
				   idPessoa,
				   razaoSocial,
				   tipoOrcamentoPedido;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_activity_orcamento_main);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		if (intentParametro != null) {
			// Seta o campo codigo consumo total com o que foi passado por parametro
			idOrcamento = intentParametro.getString(KEY_ID_ORCAMENTO);
			//textTotal.setText("Total");
			idPessoa = intentParametro.getString(KEY_ID_PESSOA);
			razaoSocial = intentParametro.getString(KEY_NOME_RAZAO);
			atacadoVarejo = intentParametro.getString(KEY_ATACADO_VAREJO);
			
			// Seta o titulo da action bar com a razao do cliente
			getActionBar().setTitle(intentParametro.getString(KEY_ID_PESSOA) + " - " + intentParametro.getString(KEY_NOME_RAZAO));
			
		} else {
			// Seta o titulo da action bar com a razao do cliente
			getActionBar().setTitle("Selecione um Cliente");
			
		}
		
		adapterOrcamentoTabulacao = new OrcamentoTabulacaoFragmentAdapter(getSupportFragmentManager());
		
		viewPagerOrcamentoMain = (ViewPager) findViewById(R.id.fragment_activity_orcamento_main_pagina);
		viewPagerOrcamentoMain.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(position);

				// Checa se a aba clicada eh da lista de produtos (orcamento)
				if (position == 0){
					// Pega a tela da aba de lista de produtos para manipular
					OrcamentoFragment fragemntOrcamento = (OrcamentoFragment) adapterOrcamentoTabulacao.getRegisteredFragments(position);
					// Checa se retornou a tela
					if (fragemntOrcamento != null){
						// Executa onResume da classe OrcamentoFragment
						fragemntOrcamento.onResume();
					}
				// Checa se a aba clicada eh a de desconto
				} else if (position == 1){
					// Pega a tela da aba de desconto para manipular
					OrcamentoDescontoFragment fragmentOrcamentoDesconto = (OrcamentoDescontoFragment) adapterOrcamentoTabulacao.getRegisteredFragments(position);
					// Checa se retornou a tela
					if (fragmentOrcamentoDesconto != null){
						// Executa onResume da classe OrcamentoDescontoFragment
						fragmentOrcamentoDesconto.onResume();
					}
				}
			}
		});
		viewPagerOrcamentoMain.setAdapter(adapterOrcamentoTabulacao);

		// Checa se tem algum numero de orcamento
		if (idOrcamento != null && idOrcamento.length() > 0){
			// Instancia a classe para pegar os dados de localizacao
			GPSTracker gps = new GPSTracker(OrcamentoTabulacaoFragment.this);

			if (gps.canGetLocation()){
				ContentValues dadosLocalizacao = new ContentValues();

				dadosLocalizacao.put("LATITUDE", gps.getLatitude());
				dadosLocalizacao.put("LONGITUDE", gps.getLongitude());
				dadosLocalizacao.put("ALTITUDE", gps.getAltitude());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Crica um formato de data e hora
				dadosLocalizacao.put("HORARIO_LOCALIZACAO", sdf.format(gps.getHorarioLocalizacao()));
				dadosLocalizacao.put("TIPO_LOCALIZACAO", gps.getTipoLocalizacao());
				dadosLocalizacao.put("PRECISAO", gps.getPrecisao());

				OrcamentoSql orcamentoSql = new OrcamentoSql(OrcamentoTabulacaoFragment.this);
				orcamentoSql.update(dadosLocalizacao, "(ID_AEAORCAM = " + idOrcamento + ") AND (LATITUDE IS NULL) AND (LONGITUDE IS NULL)");

			}
		}
		
		// Pega os parametros para passar para os fragmets
		Bundle parametros = new Bundle();
		parametros.putString(KEY_ID_ORCAMENTO, idOrcamento);
		parametros.putString(KEY_ATACADO_VAREJO, atacadoVarejo);
		parametros.putString(KEY_ID_PESSOA, idPessoa);
		parametros.putString(KEY_NOME_RAZAO, razaoSocial);
		
		// Inseri os parametro(dados do orcamento e cliente) no adapter
		adapterOrcamentoTabulacao.setParamentros(parametros);
		
		actionBar = getActionBar();
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener(){
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPagerOrcamentoMain.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}
        	
        };
        actionBar.addTab(actionBar.newTab().setText("Orçamento").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Desconto").setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Prazo").setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Observação").setTabListener(tabListener));
        
	} // Fim do onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Instancia a classe para manipulacao do orcamento
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoTabulacaoFragment.this);
		// Pega o status do orcamento
		this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(idOrcamento);
		
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				/*// Abre a tela inicial do sistema
				Intent intentInicio = new Intent(OrcamentoTabulacaoFragment.this, InicioMDActivity.class);
				// Tira a acitivity da pilha e inicia uma nova
				intentInicio.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intentInicio);*/

				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
