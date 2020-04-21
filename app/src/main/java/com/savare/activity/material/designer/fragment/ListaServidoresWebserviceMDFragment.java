package com.savare.activity.material.designer.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.activity.material.designer.CadastroServidorMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.ServidoresBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ServidoresRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno on 18/11/2017.
 */

public class ListaServidoresWebserviceMDFragment extends SlideFragment {

    private TextView textViewCnpjEmpresa;
    private ListView listViewListaServidores;
    private FloatingActionMenu menuFloatingButton;
    private FloatingActionButton itemMenuNovoServidor;
    private int mPreviousVisibleItem;
    private ItemUniversalAdapter adapterServidores;

    public static ListaServidoresWebserviceMDFragment newInstance(){
        return new ListaServidoresWebserviceMDFragment();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_lista_servidores_webservice_md, container, false);

        recuperaCampo(root);

        if (!new FuncoesPersonalizadas(getContext()).getValorXml("CnpjEmpresa").equals(FuncoesPersonalizadas.NAO_ENCONTRADO)){
            textViewCnpjEmpresa.setText(new FuncoesPersonalizadas(getContext()).getValorXml("CnpjEmpresa"));
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

                Intent intent = new Intent(getContext(), CadastroServidorMDActivity.class);

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
                Intent intent = new Intent(getContext(), CadastroServidorMDActivity.class);

                startActivity(intent);
            }
        });

        return root;
    } // Fim onCreateView

    @Override
    public void onResume() {
        super.onResume();

        carregarListaServidores();
    }

    private void recuperaCampo(View root){
        menuFloatingButton = (FloatingActionMenu) root.findViewById(R.id.fragment_lista_servidores_webservice_md_menu_float);
        itemMenuNovoServidor = (FloatingActionButton) root.findViewById(R.id.fragment_lista_servidores_webservice_md_menu_float_novo_servidor);
        listViewListaServidores = (ListView) root.findViewById(R.id.fragment_lista_servidores_webservice_md_list_servidores);
        textViewCnpjEmpresa = (TextView) root.findViewById(R.id.fragment_lista_servidores_webservice_md_textView_cnpj_empresa);
    }

    private void carregarListaServidores(){
        ServidoresRotinas servidoresRotinas = new ServidoresRotinas(getActivity());

        List<ServidoresBeans> listaServidores = new ArrayList<ServidoresBeans>();

        listaServidores = servidoresRotinas.listaServidores(null, "ID_SERVIDORES ASC", null);

        if ((listaServidores != null) && (listaServidores.size() > 0)){
            listViewListaServidores.setVisibility(View.VISIBLE);

            adapterServidores = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.SERVIDORES);
            adapterServidores.setListaServidores(listaServidores);

            listViewListaServidores.setAdapter(adapterServidores);
        } else {
            listViewListaServidores.setVisibility(View.INVISIBLE);
        }
    }
}
