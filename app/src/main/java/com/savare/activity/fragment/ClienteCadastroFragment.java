package com.savare.activity.fragment;

import com.savare.R;
import com.savare.activity.InicioActivity;
import com.savare.adapter.ClienteCadastroFragmentAdapter;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.AuxiliarRotinas;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

public class ClienteCadastroFragment extends FragmentActivity {
	
	public static final int DADOS_CLIENTE = 0,
							TELEFONE = 1,
							TOTAL_ABAS = 2;
	private ClienteCadastroFragmentAdapter adapterClienteCadastro;
	private ViewPager viewPagerClienteCadastro;
	private ActionBar actionBar;

	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_cliente_cadastro);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		adapterClienteCadastro = new ClienteCadastroFragmentAdapter(getSupportFragmentManager());

		AuxiliarRotinas auxiliarRotinas = new AuxiliarRotinas(ClienteCadastroFragment.this);
		// Pega um id negativo temporario para fazer o cadastro do cliente
		int idClifo = auxiliarRotinas.getIdClienteTemporario(null).getIdClienteTemporario();

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

		// Cria a variavel para salvar o id temporario
		Bundle clienteBundle = new Bundle();
		clienteBundle.putString(ClienteCadastroTelefoneFragment.KEY_ID_PESSOA, ""+idClifo);
		clienteBundle.putString("CADASTRO_NOVO", "S");
		// Passa por parametro o id temporario
		adapterClienteCadastro.setParamentros(clienteBundle);
		
		viewPagerClienteCadastro = (ViewPager) findViewById(R.id.fragment_cliente_cadastro);
		viewPagerClienteCadastro.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(position);
			}
		});

		viewPagerClienteCadastro.setAdapter(adapterClienteCadastro);
		
		actionBar = getActionBar();
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener(){
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPagerClienteCadastro.setCurrentItem(tab.getPosition());
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
        actionBar.addTab(actionBar.newTab().setText("Dados").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Telefone").setTabListener(tabListener));
	} // Fim onCreate
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				/*// Abre a tela inicial do sistema
				Intent intentInicio = new Intent(ClienteCadastroFragment.this, InicioActivity.class);
				// Tira a acitivity da pilha e inicia uma nova
				intentInicio.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intentInicio);*/
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
