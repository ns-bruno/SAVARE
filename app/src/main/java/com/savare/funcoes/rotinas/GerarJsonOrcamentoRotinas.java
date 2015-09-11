package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.jdom2.Element;
import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Created by Faturamento on 08/09/2015.
 */
public class GerarJsonOrcamentoRotinas {

    private Context context;
    private String idOrcamento;

    public GerarJsonOrcamentoRotinas(Context context, String idOrcamento) {
        this.context = context;
        this.idOrcamento = idOrcamento;
    }

    public String criarJson(){
        try {
            OrcamentoSql orcamentoSql = new OrcamentoSql(context);

            Cursor dadosOrcamento = orcamentoSql.query("AEAORCAM.ID_AEAORCAM = " + idOrcamento);



            // Cria o arquivo para gerar o json
            JSONStringer orcamentoJson = new JSONStringer();

            if ((dadosOrcamento != null) && (dadosOrcamento.getCount() > 0)) {
                // Move o cursor para o primeiro registro
                dadosOrcamento.moveToFirst();

                orcamentoJson
                        .object().key("orcamentoProc")
                            .object().key("dadosOrcamento")
                                .object().key("identificacaoOrcamento")
                                    .object().key("idOrcam").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_AEAORCAM")))
                                    .object().key("idEmpre").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_SMAEMPRE")))
                                    .object().key("idClifo").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFACLIFO")))
                                    .object().key("idEstad").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFAESTAD")))
                                    .object().key("idCidad").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFACIDAD")))
                                    .object().key("idTpDoc").value(dadosOrcamento.getInt(dadosOrcamento.getColumnIndex("ID_CFATPDOC")))
                                    .object().key("guid").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("GUID")))
                                    .object().key("dtCad").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_CAD")))
                                    .object().key("dtAlt").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_ALT")))
                                    .object().key("vlMercBruto").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_MERC_BRUTO")))
                                    .object().key("vlMercDesconto").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_MERC_DESCONTO")))
                                    .object().key("vlFrete").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_FRETE")))
                                    .object().key("vlSeguro").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_SEGURO")))
                                    .object().key("vlOutros").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_OUTROS")))
                                    .object().key("vlEncargosFinanceiros").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("VL_ENCARGOS_FINANCEIROS")))
                                    .object().key("vlTotal").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("FC_VL_TOTAL")))
                                    .object().key("atacVarejo").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ATAC_VAREJO")))
                                    .object().key("pessoaCliente").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("PESSOA_CLIENTE")))
                                    .object().key("nomeCliente").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("NOME_CLIENTE")))
                                    .object().key("ieRg").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("IE_RG_CLIENTE")))
                                    .object().key("cpfCGC").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CPF_CGC_CLIENTE")))
                                    .object().key("enderecoCliente").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ENDERECO_CLIENTE")))
                                    .object().key("bairroCliente").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("BAIRRO_CLIENTE")))
                                    .object().key("cepCliente").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CEP_CLIENTE")))
                                    .object().key("obs").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("OBS")))
                                    .object().key("obs").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("OBS")))
                                    .object().key("status").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("STATUS")))
                                    .object().key("tipoEntrega").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_ENTREGA")))
                                .endObject()

                                .object().key("localPedido")
                                    .object().key("latitude").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("LATITUDE")))
                                    .object().key("longitude").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("LONGITUDE")))
                                    .object().key("altitude").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("ALTITUDE")))
                                    .object().key("horarioLocalizacao").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("HORARIO_LOCALIZACAO")))
                                    .object().key("tipoLocalizacao").value(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_LOCALIZACAO")))
                                    .object().key("precisao").value(dadosOrcamento.getDouble(dadosOrcamento.getColumnIndex("PRECISAO")))
                                .endObject();
                // Instancia a classe
                ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
                // Pega a lista de produtos do determinado pedido/orcamento
                Cursor listaItemOrcamento = itemOrcamentoSql.query("AEAITORC.ID_AEAORCAM = " + idOrcamento, "AEAITORC.SEQUENCIA, AEAITORC.DT_CAD");

                // Verifica se retornou algum registro do banco
                if((listaItemOrcamento != null) && (listaItemOrcamento.getCount() > 0)){
                    // Move o cursor para o primeiro registro
                    listaItemOrcamento.moveToFirst();
                    // Passa por todos os registro
                    for (int i = 0; i < listaItemOrcamento.getCount(); i++) {
                        orcamentoJson
                                .object().key("itemOrcamento")
                                    .object().key("sequencia").value(i + 1)
                                    .object().key("idOrcamItem").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_AEAORCAM")))
                                    .object().key("idEstoq").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_AEAESTOQ")))
                                    .object().key("idProdu").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_AEAPRODU")))
                                    .object().key("idPlPgt").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_AEAPLPGT")))
                                    .object().key("idUnVen").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_AEAUNVEN")))
                                    .object().key("idClifoVendedorItem").value(listaItemOrcamento.getInt(listaItemOrcamento.getColumnIndex("ID_CFACLIFO_VENDEDOR")))
                                    .object().key("guidItem").value(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("GUID")))
                                    .object().key("dtCadItem").value(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("DT_CAD")))
                                    .object().key("quantidade").value(listaItemOrcamento.getDouble(listaItemOrcamento.getColumnIndex("QUANTIDADE")))
                                    .object().key("vlTabela").value(listaItemOrcamento.getDouble(listaItemOrcamento.getColumnIndex("VL_TABELA")))
                                    .object().key("vlBruto").value(listaItemOrcamento.getDouble(listaItemOrcamento.getColumnIndex("VL_BRUTO")))
                                    .object().key("vlDeconto").value(listaItemOrcamento.getDouble(listaItemOrcamento.getColumnIndex("VL_DESCONTO")))
                                    .object().key("totalLiquido").value(listaItemOrcamento.getDouble(listaItemOrcamento.getColumnIndex("FC_LIQUIDO")))
                                    .object().key("complemento").value(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("COMPLEMENTO")))
                                    .object().key("seqDesconto").value(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("SEQ_DESCONTO")))
                                .endObject(); // Fim do key "itemOrcamento"
                    } // fim for
                }
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                orcamentoJson
                            .endObject() // Fim do key "dadosOrcamento"

                            .object().key("identificacaoEmpresa")
                                .object().key("chaveEmpresa").value(funcoes.getValorXml("ChaveEmpresa"))
                                .object().key("codigoEmpresa").value(funcoes.getValorXml("CodigoEmpresa"))
                                .object().key("codigoUsuario").value(funcoes.getValorXml("CodigoUsuario"))
                            .endObject() // Fim do key "identificacaoEmpresa"
                        .endObject(); // Fim do key "orcamentoProc"
            }
            return orcamentoJson.toString();
        } catch (Exception error){
            return null;
        }
    } // Fim criarJson
}
