package com.savare.activity.material.designer;

import android.content.ContentValues;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.beans.ServidoresBeans;
import com.savare.funcoes.rotinas.ServidoresRotinas;

/**
 * Created by Bruno on 16/11/2017.
 */

public class CadastroServidorMDActivity extends AppCompatActivity {

    private EditText editTextNome,
                     editTextIpServidor,
                     editTextPorta;
    private Toolbar toolbarCabecalho;
    private String idServidor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_servidor_md);

        recuperaCampo();

        Bundle parametro = getIntent().getExtras();
        // Checa se realmente foi passado dados por parametro
        if (parametro != null){
            idServidor = parametro.getString("ID_SERVIDORES");

            if ((idServidor != null) && (!idServidor.isEmpty())){
                ServidoresRotinas servidoresRotinas = new ServidoresRotinas(getApplicationContext());

                ServidoresBeans servidorBeans = servidoresRotinas.listaServidores("ID_SERVIDORES = " + idServidor, null, null).get(0);

                if ((servidorBeans != null)){
                    editTextNome.setText(servidorBeans.getNomeServidor());
                    editTextIpServidor.setText(servidorBeans.getIpServidor());
                    editTextPorta.setText(""+servidorBeans.getPorta());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cadastro_servidor_md, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ServidoresRotinas servidoresRotinas = new ServidoresRotinas(getApplicationContext());

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_cadastro_servidor_md_salvar:

                // Verifica se tem algum id gravado
                if ((idServidor != null) && (!idServidor.isEmpty()) && (Integer.valueOf(idServidor) > 0)){
                    //Atualiza os dados do servidor
                    if (servidoresRotinas.updateServidor(salvarDadosCampo(), idServidor) > 0 ){
                        finish();
                    }
                } else {
                    if (servidoresRotinas.insertServidor(salvarDadosCampo()) > 0) {
                        finish();
                    }
                }
                break;

            case R.id.menu_cadastro_servidor_md_deletar:
                // Verifica se tem algum id gravado
                if ((idServidor != null) && (!idServidor.isEmpty()) && (Integer.valueOf(idServidor) > 0)){
                    if (servidoresRotinas.deleteServidor(idServidor) > 0){
                        finish();
                    }
                }else {
                    new MaterialDialog.Builder(CadastroServidorMDActivity.this)
                            .title("CadastroServidorMDActivity")
                            .content(R.string.ip_servidor_webservice_nao_cadastrado)
                            .positiveText(R.string.button_ok)
                            .show();
                }

                break;

            default: break;
        }
        return true;
    }

    private void recuperaCampo(){
        editTextNome = (EditText) findViewById(R.id.activity_cadastro_servidor_md_editText_nome_servidor);
        editTextIpServidor = (EditText) findViewById(R.id.activity_cadastro_servidor_md_editText_ip_servidor);
        editTextPorta = (EditText) findViewById(R.id.activity_cadastro_servidor_md_editText_porta);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_cadastro_servidor_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.adicionar_servidor));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private ContentValues salvarDadosCampo(){
        ContentValues valores = new ContentValues();
        try{
            valores.put("NOME", editTextNome.getText().toString());
            valores.put("IP_SERVIDOR", editTextIpServidor.getText().toString());
            valores.put("PORTA", Integer.valueOf(editTextPorta.getText().toString()));

        }catch (Exception e){
            new MaterialDialog.Builder(CadastroServidorMDActivity.this)
                    .title("CadastroServidorMDActivity")
                    .content(e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }
        return valores;
    }
}
