package com.savare.activity.material.designer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.activity.fragment.OrcamentoTabulacaoFragment;
import com.savare.adapter.ClienteDetalhesFragmentAdapter;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarCadastroClienteFtpAsyncRotinas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bruno Nogueira Silva on 30/01/2016.
 */
public class ClienteDetalhesMDActivity extends AppCompatActivity{

    private Toolbar toolbarCabecalho;
    private FloatingActionButton itemMenuNovoOrcamento, itemMenuTitulosCliente, itemMenuEnviarDados;

    private String codigoCli,
            codigoFun,
            codigoUsu,
            codigoTra,
            idCliente;
    private String clienteNovo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_cliente_detalhes_tab_md);

        recuperaCampos();

        Bundle intentParametro = getIntent().getExtras();
        // Checa se foi passado algum parametro
        if (intentParametro != null) {
            // Pega os codigo internos da pessoa
            codigoCli = intentParametro.getString("CODIGO_CLI");
            codigoFun = intentParametro.getString("CODIGO_FUN");
            codigoTra = intentParametro.getString("CODIGO_TRA");
            codigoUsu = intentParametro.getString("CODIGO_USU");
            // Pega o id do cliente
            idCliente = intentParametro.getString("ID_CFACLIFO");
            clienteNovo = intentParametro.getString("CADASTRO_NOVO");

            if ((idCliente != null) && (Integer.parseInt(idCliente) > 0)){
                itemMenuEnviarDados.setVisibility(View.GONE);
            }
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_cliente_detalhes_tab_md_pager);

        ClienteDetalhesFragmentAdapter clienteDetalhesAdapter = new ClienteDetalhesFragmentAdapter(getSupportFragmentManager(), getApplicationContext(), intentParametro);

        viewPager.setAdapter(clienteDetalhesAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_cliente_detalhes_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);

        itemMenuNovoOrcamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instancia a classe de funcoes sql para pessoa
                PessoaRotinas pessoaRotinas = new PessoaRotinas(getApplicationContext());
                // Pega os dados de uma pessoa especifica
                final PessoaBeans pessoa = pessoaRotinas.listaPessoaResumido("CODIGO_CLI = " + codigoCli, PessoaRotinas.KEY_TIPO_CLIENTE).get(0);

                // Cria um dialog para selecionar atacado ou varejo
                AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(v.getContext());
                // Atributo(variavel) para escolher o tipo da venda
                final String[] opcao = {"Atacado", "Varejo"};
                // Preenche o dialogo com o titulo e as opcoes
                mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            // Instancia a classe de funcoes
                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

                            // Preenche o ContentValues com os dados da pessoa
                            ContentValues dadosCliente = new ContentValues();
                            dadosCliente.put("ID_CFACLIFO", pessoa.getIdPessoa());
                            dadosCliente.put("ID_CFAESTAD", pessoa.getEstadoPessoa().getCodigoEstado());
                            dadosCliente.put("ID_CFACIDAD", pessoa.getCidadePessoa().getIdCidade());
                            dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                            dadosCliente.put("GUID", UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16));
                            dadosCliente.put("ATAC_VAREJO", which);
                            dadosCliente.put("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
                            dadosCliente.put("NOME_CLIENTE", pessoa.getNomeRazao());
                            dadosCliente.put("IE_RG_CLIENTE", pessoa.getIeRg());
                            dadosCliente.put("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
                            dadosCliente.put("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
                            dadosCliente.put("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
                            dadosCliente.put("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
					/*dadosCliente.put("LATITUDE", localizacao.getLatitude());
					dadosCliente.put("LONGITUDE", localizacao.getLongitude());
					dadosCliente.put("ALTITUDE", localizacao.getAltitude());
					dadosCliente.put("HORARIO_LOCALIZACAO", localizacao.getHorarioLocalizacao());
					dadosCliente.put("TIPO_LOCALIZACAO", localizacao.getTipoLocalizacao());
					dadosCliente.put("PRECISAO", localizacao.getPrecisao());*/

                            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getApplicationContext());

                            // Cria um novo orcamento no banco de dados
                            long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);

                            // Verifica se retornou algum numero
                            if(numeroOracmento > 0){

                                Bundle bundle = new Bundle();
                                bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
                                bundle.putString(OrcamentoTabulacaoFragment.KEY_NOME_RAZAO, pessoa.getNomeRazao());
                                bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_PESSOA, String.valueOf(pessoa.getIdPessoa()));
                                bundle.putString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO, String.valueOf(which));
                                bundle.putString("AV", "0");

                                Intent i = new Intent(getApplicationContext(), OrcamentoTabulacaoFragment.class);
                                i.putExtras(bundle);

                                // Abre outra tela
                                startActivity(i);

                            } else {
                                // Dados da mensagem
                                ContentValues mensagem = new ContentValues();
                                mensagem.put("comando", 2);
                                mensagem.put("tela", "CleinteDetalhesActivity");
                                mensagem.put("mensagem", "Não foi possível criar orçamento");

                                //funcoes = new FuncoesPersonalizadas(getApplicationContext());
                                funcoes.menssagem(mensagem);
                            }
                        }catch(Exception e){
                            // Dados da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 1);
                            mensagem.put("tela", "CleinteDetalhesActivity");
                            mensagem.put("mensagem", "Não conseguimos pegar todos os dados da pessoa.");

                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());
                            funcoes.menssagem(mensagem);
                        }

                    }});
                // Faz a mensagem (dialog) aparecer
                mensagemAtacadoVarejo.show();
            }
        });

        itemMenuTitulosCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria uma intent para abrir uma nova activity
                Intent intentTitulos = new Intent(getApplicationContext(), ListaTitulosMDActivity.class);
                intentTitulos.putExtra("ID_CFACLIFO", idCliente);

                startActivity(intentTitulos);
            }
        });

        itemMenuEnviarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String where = " (CFACLIFO.ID_CFACLIFO == " + idCliente + ")";

                List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();

                PessoaRotinas pessoaRotinasCad = new PessoaRotinas(getApplicationContext());

                // Pega a lista de pessoas a serem enviadas os dados
                listaPessoasCadastro = pessoaRotinasCad.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
                // Checa se retornou alguma lista
                if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0) {
                    EnviarCadastroClienteFtpAsyncRotinas enviarCadastro = new EnviarCadastroClienteFtpAsyncRotinas(getApplicationContext(), EnviarCadastroClienteFtpAsyncRotinas.TELA_CLIENTE_DETALHES);
                    // Executa o envio do cadastro em segundo plano
                    enviarCadastro.execute(listaPessoasCadastro);
                }
            }
        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void recuperaCampos(){
        // Recupera o campo para manipular
        toolbarCabecalho = (Toolbar) findViewById(R.id.fragment_cliente_detalhes_tab_md_toolbar_cabecalho);
        setSupportActionBar(toolbarCabecalho);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemMenuNovoOrcamento = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_novo_orcamento);
        itemMenuTitulosCliente = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_titulos_cliente);
        itemMenuEnviarDados = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_enviar_dados_cliente);
    }
}
