package com.savare.activity;

import java.util.ArrayList;
import java.util.List;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.funcoes.rotinas.ProdutoRotinas;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

@Deprecated
public class ProdutoDetalhesActivity extends Activity {
	
	private ListView listViewDetalhesProduto;
	private ActionBar actionBar;
	private String idProduto;
	private ItemUniversalAdapter adapterListaDetalhesProduto;
	public static final String KEY_ID_PRODUTO = "ID_PRODUTO";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_produto_detalhes);
		
		// Ativa a action bar com o simbolo de voltar
		actionBar = getActionBar();
		
		// Ativa a action bar com o simbolo de voltar
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		listViewDetalhesProduto = (ListView) findViewById(R.id.activity_produto_detalhes_listView_detalhes_produtos);
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		try {
			// Pega os parametros passado de outra tela
			Bundle intentParametro = getIntent().getExtras();
			
			if (intentParametro != null) {
				// Seta o campo codigo consumo total com o que foi passado por parametro
				idProduto = intentParametro.getString(KEY_ID_PRODUTO);
			}
		} catch (Exception e) {
			
		}
		
		
	} // Fim onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		ProdutoRotinas produtoRotinas = new ProdutoRotinas(ProdutoDetalhesActivity.this);
		
		List<DescricaoDublaBeans> listaDetalhes = new ArrayList<DescricaoDublaBeans>();
		
		listaDetalhes = produtoRotinas.listaDetalhesProduto(idProduto);
		
		adapterListaDetalhesProduto = new ItemUniversalAdapter(ProdutoDetalhesActivity.this, ItemUniversalAdapter.DETALHES_PRODUTOS);
		adapterListaDetalhesProduto.setListaDetalhesProduto(listaDetalhes);
		
		listViewDetalhesProduto.setAdapter(adapterListaDetalhesProduto);
		// Muda a cor do divider do list
		listViewDetalhesProduto.setDivider(getResources().getDrawable(R.color.azul_escuro));
		// Muda a altura do divider do list
		listViewDetalhesProduto.setDividerHeight(5);
		
		ProdutoBeans produto = produtoRotinas.detalhesProduto(idProduto);
		
		actionBar.setTitle(produto.getDescricaoProduto());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	} // FIm onOptionsItemSelected
	
	

}
