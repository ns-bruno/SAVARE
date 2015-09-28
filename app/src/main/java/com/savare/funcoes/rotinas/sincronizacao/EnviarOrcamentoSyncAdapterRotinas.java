package com.savare.funcoes.rotinas.sincronizacao;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.EstoqueBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.OrcamentoProdutoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.beans.UnidadeVendaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.EnviarDadosJsonRotinas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 18/09/2015.
 */
public class EnviarOrcamentoSyncAdapterRotinas {

    public static final int TELA_SEGUNDO_PLANO = 0,
                            TELA_VISIVEL = 1;
    private static String TAG = "SAVARE";

    private Context context;

    public EnviarOrcamentoSyncAdapterRotinas(Context context) {
        Log.i(TAG, "construtor - EnviarOrcamentoSyncAdapterRotinas");

        this.context = context;
    }

    protected void preExecute(){
        Log.i(TAG, "preExecute - EnviarOrcamentoSyncAdapterRotinas");

    }

    public String execute(){
        Log.i(TAG, "execute - EnviarOrcamentoSyncAdapterRotinas");

        preExecute();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        String mensagem = "";
        // Inicializa a vareavel para salvar os dados do banco de dados
        List<OrcamentoProdutoBeans> listaOrcamentoComProdutos = new ArrayList<OrcamentoProdutoBeans>();;

        try {
            // Checa se tem internet
            if (funcoes.existeConexaoInternet()) {

                OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                Cursor dadosOrcamento = orcamentoSql.query("AEAORCAM.STATUS = 'P'");
                // Checa se retornou alguma coisa
                if (dadosOrcamento != null && dadosOrcamento.getCount() > 0){

                    // Passa por todos os registros recuperados
                    while (dadosOrcamento.moveToNext()){
                        // Pega os dados do orcamento
                        OrcamentoBeans orcamento = new OrcamentoBeans();
                        orcamento.setIdOrcamento(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_AEAORCAM")));
                        orcamento.setIdEmpresa(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_SMAEMPRE")));
                        orcamento.setIdPessoa(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFACLIFO")));
                        orcamento.setIdEstado(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFAESTAD")));
                        orcamento.setIdCidade(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFACIDAD")));
                        orcamento.setIdTipoDocumento(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFATPDOC")));
                        orcamento.setGuid(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("GUID")));
                        orcamento.setDataCadastro(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_CAD")));
                        orcamento.setDataAlteracao(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_ALT")));
                        orcamento.setTotalOrcamentoBruto(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_MERC_BRUTO")));
                        orcamento.setTotalDesconto(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_MERC_DESCONTO")));
                        orcamento.setTotalFrete(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_FRETE")));
                        orcamento.setTotalSeguro(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_SEGURO")));
                        orcamento.setTotalOutros(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_OUTROS")));
                        orcamento.setTotalEncargosFinanceiros(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_ENCARGOS_FINANCEIROS")));
                        orcamento.setTotalOrcamento(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("FC_VL_TOTAL")));
                        if (dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ATAC_VAREJO")) != null && dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ATAC_VAREJO")).length() > 0) {
                            orcamento.setTipoVenda(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ATAC_VAREJO")).charAt(0));
                        }
                        orcamento.setPessoaCliente(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("PESSOA_CLIENTE")));
                        orcamento.setNomeRazao(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("NOME_CLIENTE")));
                        orcamento.setRgIe(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("IE_RG_CLIENTE")));
                        orcamento.setCpfCnpj(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CPF_CGC_CLIENTE")));
                        orcamento.setEnderecoCliente(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ENDERECO_CLIENTE")));
                        orcamento.setBairroCliente(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("BAIRRO_CLIENTE")));
                        orcamento.setCepCliente(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CEP_CLIENTE")));
                        orcamento.setObservacao(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("OBS")));
                        orcamento.setStatus(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("STATUS")));
                        orcamento.setTipoEntrega(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_ENTREGA")));
                        orcamento.setLatitude(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("LATITUDE")));
                        orcamento.setLongitude(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("LONGITUDE")));
                        orcamento.setAltitude(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("ALTITUDE")));
                        orcamento.setHorarioLocalizacao(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("HORARIO_LOCALIZACAO")));
                        orcamento.setTipoLocalizacao(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_LOCALIZACAO")));
                        orcamento.setPrecisaoLocalizacao(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("PRECISAO")));

                        OrcamentoProdutoBeans orcamentoEProdutos = new OrcamentoProdutoBeans();

                        orcamentoEProdutos.setOrcamento(orcamento);

                        listaOrcamentoComProdutos.add(orcamentoEProdutos);
                    } // Fim while (dadosOrcamento.moveToNext())

                    // Passa por todos os orcamento para pegar todos os produtos de cada orcamento
                    for (int i = 0; i < listaOrcamentoComProdutos.size(); i++) {
                        ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                        Cursor dadosItens = itemOrcamentoSql.query("AEAITORC.ID_AEAORCAM = " + listaOrcamentoComProdutos.get(i).getOrcamento().getIdOrcamento(),
                                                                   "AEAITORC.SEQUENCIA");

                        // Checa se retornou algum registro do banco de dados
                        if (dadosItens != null && dadosItens.getCount() > 0){

                            // Cria uma vareavel para armazenar a lista de produtos do determinado orcamento
                            List<ItemOrcamentoBeans> listaItemOrcamento = new ArrayList<ItemOrcamentoBeans>();

                            // Passa por todos os registros
                            while (dadosItens.moveToNext()){

                                // Pega os dados do produtos
                                ItemOrcamentoBeans itemOrcamento = new ItemOrcamentoBeans();

                                itemOrcamento.setIdItemOrcamento(dadosItens.getInt(dadosItens.getColumnIndex("ID_AEAITORC")));
                                itemOrcamento.setGuid(dadosItens.getString(dadosItens.getColumnIndex("GUID")));
                                itemOrcamento.setDataCadastro(dadosItens.getString(dadosItens.getColumnIndex("DT_CAD")));
                                itemOrcamento.setDataAlteracao(dadosItens.getString(dadosItens.getColumnIndex("DT_ALT")));
                                itemOrcamento.setQuantidade(dadosItens.getDouble(dadosItens.getColumnIndex("QUANTIDADE")));
                                itemOrcamento.setValorTabela(dadosItens.getDouble(dadosItens.getColumnIndex("VL_TABELA")));
                                itemOrcamento.setValorBruto(dadosItens.getDouble(dadosItens.getColumnIndex("VL_BRUTO")));
                                itemOrcamento.setValorDesconto(dadosItens.getDouble(dadosItens.getColumnIndex("VL_DESCONTO")));
                                itemOrcamento.setValorLiquido(dadosItens.getDouble(dadosItens.getColumnIndex("FC_LIQUIDO")));
                                itemOrcamento.setComplemento(dadosItens.getString(dadosItens.getColumnIndex("COMPLEMENTO")));
                                itemOrcamento.setSequencialDesconto(dadosItens.getString(dadosItens.getColumnIndex("SEQ_DESCONTO")));

                                ProdutoBeans produto = new ProdutoBeans();
                                produto.setIdProduto(dadosItens.getInt(dadosItens.getColumnIndex("ID_AEAPRODU")));
                                itemOrcamento.setProduto(produto);

                                EstoqueBeans estoque = new EstoqueBeans();
                                estoque.setIdEstoque(dadosItens.getInt(dadosItens.getColumnIndex("ID_AEAESTOQ")));
                                itemOrcamento.setEstoqueVenda(estoque);

                                PlanoPagamentoBeans planoPagamento = new PlanoPagamentoBeans();
                                planoPagamento.setIdPlanoPagamento(dadosItens.getInt(dadosItens.getColumnIndex("ID_AEAPLPGT")));
                                itemOrcamento.setPlanoPagamento(planoPagamento);

                                UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
                                unidadeVenda.setIdUnidadeVenda(dadosItens.getInt(dadosItens.getColumnIndex("ID_AEAUNVEN")));
                                itemOrcamento.setUnidadeVenda(unidadeVenda);

                                PessoaBeans pessoaVendedor = new PessoaBeans();
                                pessoaVendedor.setIdPessoa(dadosItens.getInt(dadosItens.getColumnIndex("ID_CFACLIFO_VENDEDOR")));
                                itemOrcamento.setPessoaVendedor(pessoaVendedor);

                                listaItemOrcamento.add(itemOrcamento);
                            }
                            // Adiciona a lista de produtos junto com o orcamento
                            listaOrcamentoComProdutos.get(i).setListaProdutosOrcamento(listaItemOrcamento);
                        }
                    } // Fim do for
                }
            } else { // Fim checaInternet

                mensagem += context.getResources().getString(R.string.nao_existe_conexao_internet) + "\n";
            }
            // Checa se tem algum orcamento salvo na lista, para enviar via web
            if (listaOrcamentoComProdutos != null && listaOrcamentoComProdutos.size() > 0){

                // Instancia a classe encarregada por enviar os dados pela net
                final EnviarDadosJsonRotinas enviarDadosJson = new EnviarDadosJsonRotinas(context, EnviarDadosJsonRotinas.TIPO_OBJECT);

                // Passa por todos os orcamentos
                for (int i = 0; i < listaOrcamentoComProdutos.size(); i++) {

                    // Checa se este pedido tem itens(produtos)
                    if ((listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento() != null) && (listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().size() > 0)) {

                        // Passa por todos os produtos do orcamento
                        for (int j = 0; j < listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().size(); j++) {
                            // Checa se ja foi enviado
                            if (!listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).isTagEnviado()) {

                                final HashMap<String, String> paramItemOrcamento = new HashMap<String, String>();
                                paramItemOrcamento.put("tipoDados", EnviarDadosJsonRotinas.TIPO_DADOS_ITENS_PEDIDO);
                                paramItemOrcamento.put("sequencia", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getSeguencia());
                                paramItemOrcamento.put("idOrcamItem", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getIdItemOrcamento());
                                paramItemOrcamento.put("idEstoq", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getEstoqueVenda().getIdEstoque());
                                paramItemOrcamento.put("idProdu", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getProduto().getIdProduto());
                                paramItemOrcamento.put("idPlPgt", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getPlanoPagamento().getIdPlanoPagamento());
                                paramItemOrcamento.put("idUnVen", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getUnidadeVenda().getIdUnidadeVenda());
                                paramItemOrcamento.put("idClifoVendedorItem", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getPessoaVendedor().getIdPessoa());
                                paramItemOrcamento.put("guidItem", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getGuid());
                                paramItemOrcamento.put("dtCadItem", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getDataCadastro());
                                paramItemOrcamento.put("dtAltItem", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getDataAlteracao());
                                paramItemOrcamento.put("quantidade", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getQuantidade());
                                paramItemOrcamento.put("vlTabela", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getValorTabela());
                                paramItemOrcamento.put("vlBruto", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getValorBruto());
                                paramItemOrcamento.put("vlDeconto", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getValorDesconto());
                                paramItemOrcamento.put("totalLiquido", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getValorLiquido());
                                paramItemOrcamento.put("complemento", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getComplemento());
                                paramItemOrcamento.put("seqDesconto", "" + listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).getSequencialDesconto());

                                // Executa o processo de enviar dados em segundo plano
                                Runnable runnableItem = new Runnable() {
                                    @Override
                                    public void run() {
                                        enviarDadosJson.enviarDados(paramItemOrcamento);
                                    }
                                };
                                new Thread(runnableItem).start();

                                listaOrcamentoComProdutos.get(i).getListaProdutosOrcamento().get(j).setTagEnviado(true);
                            }
                            //paramItemOrcamento.clear();
                        }
                    }
                    // Checa se o orcamento ja foi enviado
                    if (!listaOrcamentoComProdutos.get(i).getOrcamento().isTagEnviado()) {

                        final HashMap<String, String> paramOrcamento = new HashMap<String, String>();
                        paramOrcamento.put("METODO", "OBJECT");
                        paramOrcamento.put("tipoDados", EnviarDadosJsonRotinas.TIPO_DADOS_PEDIDO);
                        paramOrcamento.put("idOrcam", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdOrcamento());
                        paramOrcamento.put("idEmpre", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdEmpresa());
                        paramOrcamento.put("idClifoVendedor", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdPessoaVendedor());
                        paramOrcamento.put("idClifo", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdPessoa());
                        paramOrcamento.put("idEstad", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdEstado());
                        paramOrcamento.put("idCidad", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdCidade());
                        paramOrcamento.put("idTpDoc", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getIdTipoDocumento());
                        paramOrcamento.put("guid", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getGuid());
                        paramOrcamento.put("dtCad", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getDataCadastro());
                        paramOrcamento.put("dtAlt", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getDataAlteracao());
                        paramOrcamento.put("vlMercBruto", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalOrcamentoBruto());
                        paramOrcamento.put("vlMercDesconto", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalDesconto());
                        paramOrcamento.put("vlFrete", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalFrete());
                        paramOrcamento.put("vlSeguro", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalSeguro());
                        paramOrcamento.put("vlOutros", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalOutros());
                        paramOrcamento.put("vlEncargosFinanceiros", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalEncargosFinanceiros());
                        paramOrcamento.put("vlTotal", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTotalOrcamento());
                        paramOrcamento.put("atacVarejo", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTipoVenda());
                        paramOrcamento.put("pessoaCliente", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getPessoaCliente());
                        paramOrcamento.put("nomeCliente", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getNomeRazao());
                        paramOrcamento.put("ieRg", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getRgIe());
                        paramOrcamento.put("cpfCGC", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getCpfCnpj());
                        paramOrcamento.put("enderecoCliente", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getEnderecoCliente());
                        paramOrcamento.put("bairroCliente", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getBairroCliente());
                        paramOrcamento.put("cepCliente", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getCepCliente());
                        paramOrcamento.put("obs", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getObservacao());
                        paramOrcamento.put("status", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getStatus());
                        paramOrcamento.put("tipoEntrega", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTipoEntrega());
                        paramOrcamento.put("latitude", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getLatitude());
                        paramOrcamento.put("longitude", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getLongitude());
                        paramOrcamento.put("altitude", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getAltitude());
                        paramOrcamento.put("horarioLocalizacao", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getHorarioLocalizacao());
                        paramOrcamento.put("tipoLocalizacao", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getTipoLocalizacao());
                        paramOrcamento.put("precisao", "" + listaOrcamentoComProdutos.get(i).getOrcamento().getPrecisaoLocalizacao());

                        // Executa o processo de enviar dados em segundo plano
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                enviarDadosJson.enviarDados(paramOrcamento);
                            }
                        };
                        new Thread(runnable).start();

                        // Marca o pedido como enviado
                        listaOrcamentoComProdutos.get(i).getOrcamento().setTagEnviado(true);

                        // Limba os parametro do orcamento
                        //paramOrcamento.clear();
                    }
                }
            }
        }catch (Exception e){
            // Armazena as informacoes para para serem exibidas e enviadas
            ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "EnviarOrcamentoSyncAdapterRotinas");
            contentValues.put("mensagem", e.getMessage());
            contentValues.put("dados", context.toString());
            // Pega os dados do usuario
            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            funcoes.menssagem(contentValues);
        } finally {
            postExecute();
        }
        return mensagem;
    }

    protected void postExecute(){
        Log.i(TAG, "postExecute - EnviarOrcamentoSyncAdapterRotinas");

    }
}
