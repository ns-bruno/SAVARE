package com.savare.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.ProdutoListaBeans;
import com.savare.provider.SearchableProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 23/12/2015.
 */
public class SearchableActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private List<ProdutoListaBeans> listaProdutos;
    private ListView listViewProduto;
    private ItemUniversalAdapter adapterListaProduto;
    private String atacadoVarejo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        toolbar = (Toolbar) findViewById(R.id.activity_searchable_tb_main);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null){
            listaProdutos = savedInstanceState.getParcelableArrayList("listaProduto");
            atacadoVarejo = savedInstanceState.getString("atacadoVarejo");

        } else {
            atacadoVarejo = "0";
        }

        listViewProduto = (ListView) findViewById(R.id.fragment_produto_lista_universal_md_listView_lista_produto);

        //Pegar a lista de produtos

        /*adapterListaProduto = new ItemUniversalAdapter(getApplicationContext(), ItemUniversalAdapter.PRODUTO);
        // Seta a lista de produtos no adapter
        adapterListaProduto.setListAeaploja(listaProdutos);
        // Informa o tipo da venda (atacado ou varejo)
        adapterListaProduto.setAtacadoVarejo(atacadoVarejo);

        if (adapterListaProduto != null) {
            listViewProduto.setAdapter(adapterListaProduto);
        }*/

        hendleSearch(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        hendleSearch(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("listaProduto", (ArrayList<ProdutoListaBeans>) listaProdutos);
        outState.putString("atacadoVarejo", atacadoVarejo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchable, menu);

        // Configuracao associando item de pesquisa com a SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //SearchView searchView = (SearchView) menu.findItem(R.id.menu_produto_lista_tab_md_fragment_search_pesquisar).getActionView();
        SearchView searchView;
        MenuItem itemMenuSearch = menu.findItem(R.id.menu_searchable_search_pesquisar);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
            searchView = (SearchView) itemMenuSearch.getActionView();
        }
        else{
            searchView = (SearchView) MenuItemCompat.getActionView(itemMenuSearch);
        }

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //searchView.setQueryHint(getResources().getString(R.string.pesquisar));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_searchable_search_action_delete:
                SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this, SearchableProvider.AUTHORITY, SearchableProvider.MODE);
                // Limpa o historico de palavras pesquisadas
                searchRecentSuggestions.clearHistory();

                Toast.makeText(this, "Cookies removidos", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
        return true;
    }

    public void hendleSearch( Intent intent ){
        if( Intent.ACTION_SEARCH.equalsIgnoreCase( intent.getAction() ) ){
            String q = intent.getStringExtra( SearchManager.QUERY );

            toolbar.setTitle(q);

            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this, SearchableProvider.AUTHORITY, SearchableProvider.MODE);
            searchRecentSuggestions.saveRecentQuery(q, null);
        }
    }
}
