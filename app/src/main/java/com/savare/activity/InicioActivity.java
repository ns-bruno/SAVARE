package com.savare.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savare.R;
import com.savare.activity.fragment.ClienteListaFragment;
import com.savare.activity.fragment.OrcamentoTabulacaoFragment;
import com.savare.activity.fragment.ResumoFragment;
import com.savare.adapter.CustomDrawerAdapter;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.DrawerItem;
import com.savare.beans.TitulosListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

public class InicioActivity extends Activity {

	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<DrawerItem> dataList;
    private CustomDrawerAdapter adapter;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inicio);
		
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.activity_inicio_left_drawer);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(InicioActivity.this);
        UsuarioSQL usuarioSQL = new UsuarioSQL(InicioActivity.this);
        // Pega os dados do usuario no banco de dados
        Cursor dados = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));
        dados.moveToFirst();
        
        // Add Drawer Item to dataList
        dataList.add(new DrawerItem(dados.getString(dados.getColumnIndex("NOME_USUA"))));
        dataList.add(new DrawerItem("Resumo", R.drawable.ic_action_view_as_grid));
        dataList.add(new DrawerItem("Clientes", R.drawable.ic_action_person));
        dataList.add(new DrawerItem("Orçamentos", R.drawable.ic_action_view_as_list));
        dataList.add(new DrawerItem("Produtos", R.drawable.ic_action_box_produtct));
        dataList.add(new DrawerItem("Títulos", R.drawable.ic_action_coins_pay));
        dataList.add(new DrawerItem("Pedidos à Enviar", R.drawable.ic_action_order));
        dataList.add(new DrawerItem("Pedidos Enviados", R.drawable.ic_action_order));
        dataList.add(new DrawerItem("Lixeira", R.drawable.ic_action_discard));
        dataList.add(new DrawerItem("Sincronização", R.drawable.ic_action_cloud));
        dataList.add(new DrawerItem("Configurações", R.drawable.ic_action_settings));
        dataList.add(new DrawerItem("Logs", R.drawable.ic_sim_alert));
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        adapter = new CustomDrawerAdapter(this, R.layout.drawer_listview_item, dataList);
        
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
     // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            
        	public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
	} // Fim do onCreate
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inicio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
		return true;
        
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	Fragment fragment = null;
        
		switch (position) {

		case 0:

			break;

		case 1:
			fragment = new ResumoFragment();
			break;

		case 2:
        	// Abre a tela inicial do sistema
			Intent intent = new Intent(InicioActivity.this, ClienteListaActivity.class);
			startActivity(intent);
            break;
            
		case 3:
			// Abre a tela Lista de Orcamento
			Intent intentListaOrcamentoPedido = new Intent(InicioActivity.this, ListaOrcamentoPedidoActivity.class);
			// Salva um valor para transferir para outrao Activity(Tela)
			intentListaOrcamentoPedido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.ORCAMENTO);
			// Abre outra tela
			startActivity(intentListaOrcamentoPedido);
			break;
			
		case 4:
			// Tela de lista de produtos
			// Cria um dialog para selecionar atacado ou varejo
			AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(InicioActivity.this);
			// Atributo(variavel) para escolher o tipo da venda
			final String[] opcao = {"Atacado", "Varejo"};
			// Preenche o dialogo com o titulo e as opcoes
			mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// Cria uma intent para abrir uma nova activity
					Intent intentOrcamento = new Intent(InicioActivity.this, ProdutoListaActivity.class);
					intentOrcamento.putExtra("ATAC_VAREJO", String.valueOf(which).toString());
					startActivity(intentOrcamento);
				
				}});
			
			// Faz a mensagem (dialog) aparecer
			mensagemAtacadoVarejo.show();
			break;
		
		case 5:
			// Tela de Lista de Titulos
			Intent intentListaTitulos = new Intent(InicioActivity.this, ListaTitulosActivity.class);
			// Abre outra tela
			startActivity(intentListaTitulos);
			break;
		
		case 6:
			// Tela de Lista de Pedidos nao enviados
			Intent intentListaPedido = new Intent(InicioActivity.this, ListaOrcamentoPedidoActivity.class);
			// Salva um valor para transferir para outrao Activity(Tela)
			intentListaPedido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.PEDIDO_NAO_ENVIADO);
			// Abre outra tela
			startActivity(intentListaPedido);
			break;
			
		case 7:
			// Tela de Lista de Pedidos nao enviados
			Intent intentListaPedidoEnviado = new Intent(InicioActivity.this, ListaOrcamentoPedidoActivity.class);
			// Salva um valor para transferir para outrao Activity(Tela)
			intentListaPedidoEnviado.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.PEDIDO_ENVIADO);
			// Abre outra tela
			startActivity(intentListaPedidoEnviado);
			break;
			
		case 8:
			// Tela de orcamentos excluidos(lixeira)
			Intent intentListaExcluido = new Intent(InicioActivity.this, ListaOrcamentoPedidoActivity.class);
			// Salva um valor para transferir para outrao Activity(Tela)
			intentListaExcluido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.EXCLUIDO);
			// Abre outra tela
			startActivity(intentListaExcluido);
			break;
			
		case 9:
			// Tela de sincronização
			Intent intentListaSincronizacao = new Intent(InicioActivity.this, SincronizacaoActivity.class);
			// Abre outra tela
			startActivity(intentListaSincronizacao);
			break;
			
		case 10:
			// Tela de sincronização
			Intent intentListaConfiguracoes = new Intent(InicioActivity.this, ConfiguracoesActivity.class);
			// Abre outra tela
			startActivity(intentListaConfiguracoes);
			break;
			
		case 11:
			// Tela de sincronização
			Intent intentLogs = new Intent(InicioActivity.this, LogActivity.class);
			// Abre outra tela
			startActivity(intentLogs);
			break;
              
        default:
              break;
        } // Fim do switch

        //FragmentManager fragmentManager = getFragmentManager();
        
        //fragmentManager.beginTransaction().replace(R.id.activity_inicio_content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
