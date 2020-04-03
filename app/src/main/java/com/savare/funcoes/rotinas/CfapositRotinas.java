package com.savare.funcoes.rotinas;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.funcoesSql.PositivacaoSql;
import com.savare.beans.CfapositBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class CfapositRotinas extends Rotinas {

    public CfapositRotinas(Context context) {
        super(context);
    }

    public List<CfapositBeans> listaClientePositivacao(String where){
        List<CfapositBeans> listCfaposit = new ArrayList<>();
        try{
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT CFACLIFO.ID_CFACLIFO, CFACLIFO.NOME_RAZAO, CFACLIFO.NOME_FANTASIA, CFACLIFO.CODIGO_CLI, CFACLIFO.CPF_CNPJ, \n");
            sql.append("CFAPOSIT.ID_CFAPOSIT, CFAPOSIT.DATA_VISITA, CFAPOSIT.VALOR_VENDA, CFAPOSIT.STATUS FROM CFAPOSIT \n");
            sql.append("LEFT OUTER JOIN CFACLIFO CFACLIFO \n");
            sql.append("ON(CFAPOSIT.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n");
            if ( (where != null) && (!where.isEmpty())){
                sql.append("WHERE (").append(where).append(") ");
            }
            sql.append("ORDER BY CFAPOSIT.DATA_VISITA, CFACLIFO.NOME_RAZAO ");

            PositivacaoSql positivacaoSql = new PositivacaoSql(context);

            Cursor dados = positivacaoSql.sqlSelect(sql.toString());
            // Checa se voltou alguma coisa do banco de dados
            if (dados != null && dados.getCount() > 0){
                //Percorre por todos os registros recuperado do banco de dados
                while(dados.moveToNext()) {
                    PessoaBeans cliente = new PessoaBeans();
                    cliente.setIdPessoa(dados.getInt(dados.getColumnIndex("ID_CFACLIFO")));
                    cliente.setNomeRazao(dados.getString(dados.getColumnIndex("NOME_RAZAO")));
                    cliente.setNomeFantasia(dados.getString(dados.getColumnIndex("NOME_FANTASIA")));
                    cliente.setCodigoCliente(dados.getInt(dados.getColumnIndex("CODIGO_CLI")));
                    cliente.setCpfCnpj(dados.getString(dados.getColumnIndex("CPF_CNPJ")));

                    CfapositBeans cfapositBeans = new CfapositBeans();
                    cfapositBeans.setPessoaBeans(cliente);
                    cfapositBeans.setIdCfaposit(dados.getInt(dados.getColumnIndex("ID_CFAPOSIT")));
                    cfapositBeans.setDataVisita(dados.getString(dados.getColumnIndex("DATA_VISITA")));
                    cfapositBeans.setValor(dados.getDouble(dados.getColumnIndex("VALOR_VENDA")));
                    cfapositBeans.setStatus(dados.getString(dados.getColumnIndex("STATUS")));
                    listCfaposit.add(cfapositBeans);
                }
            }
        } catch (final Exception e){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("CfapositRotinas")
                            .content("Erro desconhecido: " + e.getMessage())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        }
        return listCfaposit;
    }
}
