package com.savare.activity.material.designer;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.activity.material.designer.fragment.ClienteCadastroTelefoneMDFragment;
import com.savare.adapter.ClienteCadastroFragmentAdapter;

import java.util.Random;

public class ClienteCadastroTabFragmentMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cliente_cadastro_tab_md);

        recuperarCampos();


        //AuxiliarRotinas auxiliarRotinas = new AuxiliarRotinas(ClienteCadastroTabFragmentMDActivity.this);
        // Pega um id negativo temporario para fazer o cadastro do cliente
        //int idClifo = auxiliarRotinas.getIdClienteTemporario(null).getIdClienteTemporario();

        int idClifo = new Random().nextInt(500) * -1;

        // Cria a variavel para salvar o id temporario
        Bundle parametros = new Bundle();
        parametros.putString(ClienteCadastroTelefoneMDFragment.KEY_ID_PESSOA, ""+idClifo);
        parametros.putString("CADASTRO_NOVO", "S");

        ClienteCadastroFragmentAdapter clienteCadastroFragmentAdapter = new ClienteCadastroFragmentAdapter(getSupportFragmentManager(), getApplicationContext(), parametros);

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_cliente_cadastro_tab_md_pager);
        // Seta o adapter dentro da viewPager
        viewPager.setAdapter(clienteCadastroFragmentAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_cliente_cadastro_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void recuperarCampos(){
        toolbarCabecalho = (Toolbar) findViewById(R.id.fragment_cliente_cadastro_tab_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.novo_cliente));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
