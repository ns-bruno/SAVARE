package com.savare.activity.material.designer;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.ServidoresBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ServidoresRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno on 14/11/2017.
 */

public class ListaServidoresWebserviceMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private ListView listViewListaServidores;
    private TextView textStatus;
    private TextView textViewCnpjEmpresa;
    private FloatingActionMenu menuFloatingButton;
    private FloatingActionButton itemMenuNovoServidor;
    private ProgressBar progressBarStatus;
    private int mPreviousVisibleItem;
    private ItemUniversalAdapter adapterServidores;

    public static ListaServidoresWebserviceMDActivity newInstance() {
        return new ListaServidoresWebserviceMDActivity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lista_servidores_webservice_md);

        recuperaCampo();

        if (!new FuncoesPersonalizadas(this).getValorXml(FuncoesPersonalizadas.TAG_CNPJ_EMPRESA).equals(FuncoesPersonalizadas.NAO_ENCONTRADO)){
            textViewCnpjEmpresa.setText(new FuncoesPersonalizadas(this).getValorXml(FuncoesPersonalizadas.TAG_CNPJ_EMPRESA));
        } else {
            textViewCnpjEmpresa.setVisibility(View.INVISIBLE);
        }

        listViewListaServidores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                menuFloatingButton.close(true);

                ServidoresBeans servidoresBeans = (ServidoresBeans) adapterView.getItemAtPosition(i);

                Bundle bundle = new Bundle();
                bundle.putString("ID_SERVIDORES", String.valueOf(servidoresBeans.getIdServidores()));

                Intent intent = new Intent(ListaServidoresWebserviceMDActivity.this, CadastroServidorMDActivity.class);

                intent.putExtras(bundle);
                // Abre outra tela
                startActivity(intent);
            }
        });

        listViewListaServidores.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Funcao para ocultar o float button quando rolar a lista de orcamento/pedido
                if (firstVisibleItem > mPreviousVisibleItem) {
                    menuFloatingButton.hideMenu(true);
                } else if (firstVisibleItem < mPreviousVisibleItem) {
                    menuFloatingButton.showMenu(true);
                }
                mPreviousVisibleItem = firstVisibleItem;
            }
        });

        itemMenuNovoServidor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Fecha o float buttom menu
                menuFloatingButton.close(true);

                // Abre a tela de detalhes cadastro de servidores
                Intent intent = new Intent(ListaServidoresWebserviceMDActivity.this, CadastroServidorMDActivity.class);

                startActivity(intent);
            }
        });

    } // Fim onCreate

    @Override
    protected void onResume() {
        super.onResume();

        carregarListaServidores();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recuperaCampo(){
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_lista_servidores_webservice_toolbar_cabecalho);
        listViewListaServidores = (ListView) findViewById(R.id.activity_lista_servidores_webservice_md_list_servidores);
        textStatus = (TextView) findViewById(R.id.activity_lista_servidores_webservice_md_text_status);
        textViewCnpjEmpresa = (TextView) findViewById(R.id.activity_lista_servidores_webservice_md_textView_cnpj_empresa);
        menuFloatingButton = (FloatingActionMenu) findViewById(R.id.activity_lista_servidores_webservice_md_menu_float);
        itemMenuNovoServidor = (FloatingActionButton) findViewById(R.id.activity_lista_servidores_webservice_md_menu_float_novo_servidor);
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_lista_servidores_webservice_md_progressBar_status);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.lista_servidor));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void carregarListaServidores(){
        ServidoresRotinas servidoresRotinas = new ServidoresRotinas(ListaServidoresWebserviceMDActivity.this);

        List<ServidoresBeans> listaServidores = new ArrayList<ServidoresBeans>();

        listaServidores = servidoresRotinas.listaServidores(null, "ID_SERVIDORES ASC", null);

        if ((listaServidores != null) && (listaServidores.size() > 0)){
            adapterServidores = new ItemUniversalAdapter(ListaServidoresWebserviceMDActivity.this, ItemUniversalAdapter.SERVIDORES);
            adapterServidores.setListaServidores(listaServidores);

            listViewListaServidores.setAdapter(adapterServidores);
        }
    }
}
