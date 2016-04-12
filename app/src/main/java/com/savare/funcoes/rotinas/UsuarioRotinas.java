package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.StatusBeans;
import com.savare.beans.UsuarioBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Bruno Nogueira Silva on 02/07/2015.
 */
public class UsuarioRotinas extends Rotinas {

    public UsuarioRotinas(Context context) {
        super(context);
    }

    /**
     * Atualiza a data e hora do recebimento de dados.
     * Retorna verdadei ou falso, indicando se consiguiu atualizar a data de recebimento.
     *
     * @param dataHora - yyyy/MM/dd HH:mm:ss
     * @return
     */
    public boolean atualizaDataHoraRecebimento(String dataHora){
        boolean retorno = false;

        ContentValues dados = new ContentValues();

        if(dataHora == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Calendar cal = Calendar.getInstance().getTime();

            dados.put("DT_ULTIMO_RECEBIMENTO", dateFormat.format(Calendar.getInstance().getTime()));
        } else {
            dados.put("DT_ULTIMO_RECEBIMENTO", dataHora);
        }

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        UsuarioSQL usuarioSQL = new UsuarioSQL(context);

        if(usuarioSQL.update(dados, "ID_USUA = " + funcoes.getValorXml("CodigoUsuario")) > 0){
            retorno = true;
            funcoes.setValorXml("DataUltimoRecebimento", dados.get("DT_ULTIMO_RECEBIMENTO").toString());
        }
        return retorno;
    } // Fim atualizaDataHoraRecebimento


    /**
     * Pega a data do ultimo envio de pedidos.
     * Essa data esta relacionada com a data de evnio automático.
     *
     * @param idUsuario
     * @return
     */
    public String dataUltimoEnvio(String idUsuario){
        String dtUltimoEnvio = "";

        try {
            String sql = "SELECT DT_ULTIMO_ENVIO FROM USUARIO_USUA WHERE ID_USUA = " + idUsuario;

            UsuarioSQL usuarioSQL = new UsuarioSQL(context);

            Cursor cursor = usuarioSQL.sqlSelect(sql);

            // Checa se retornou um registro
            if( (cursor != null) && (cursor.getCount() > 0)){
                // Move para o primeiro registro
                cursor.moveToFirst();

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                dtUltimoEnvio = funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_ULTIMO_ENVIO")));
            }

        } catch (Exception e) {
            dtUltimoEnvio = e.getMessage();
        }

        return dtUltimoEnvio;
    } // Fim dataUltimoEnvio


    public String dataUltimoRecebimento(String idUsuario){
        String dtUltimoRecebimento = "";

        try {
            String sql = "SELECT DT_ULTIMO_RECEBIMENTO FROM USUARIO_USUA WHERE ID_USUA = " + idUsuario;

            UsuarioSQL usuarioSQL = new UsuarioSQL(context);

            Cursor cursor = usuarioSQL.sqlSelect(sql);

            // Checa se retornou um registro
            if( (cursor != null) && (cursor.getCount() > 0)){
                // Move para o primeiro registro
                cursor.moveToFirst();

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                dtUltimoRecebimento = funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_ULTIMO_RECEBIMENTO")));
            }

        } catch (Exception e) {
            dtUltimoRecebimento = e.getMessage();
        }

        return dtUltimoRecebimento;
    } // Fim dataUltimoEnvio


    public UsuarioBeans usuarioCompleto(String where){
        // Cria uma vareavel para salvar os dados do usuario
        UsuarioBeans usuario = new UsuarioBeans();

        UsuarioSQL usuarioSQL = new UsuarioSQL(context);
        // Executa o sql para pegar os dados do usuario
        Cursor dadosUsuario = usuarioSQL.query(where);

        if (dadosUsuario != null && dadosUsuario.getCount() > 0){
            dadosUsuario.moveToFirst();

            usuario.setIdUsuario(dadosUsuario.getInt(dadosUsuario.getColumnIndex("ID_USUA")));
            usuario.setIdEmpresa(dadosUsuario.getInt(dadosUsuario.getColumnIndex("ID_SMAEMPRE")));
            usuario.setChave(dadosUsuario.getString(dadosUsuario.getColumnIndex("CHAVE_USUA")));
            usuario.setNomeUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("NOME_USUA")));
            usuario.setLoginUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA")));
            usuario.setSenhaUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_USUA")));
            usuario.setEmail(dadosUsuario.getString(dadosUsuario.getColumnIndex("EMAIL_USUA")));
            usuario.setEmpresaUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("EMPRESA_USUA")));
            usuario.setVendeAtacadoUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("VENDE_ATACADO_USUA")).charAt(0));
            usuario.setVendeVarejoUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("VENDE_VAREJO_USUA")).charAt(0));
            if ((dadosUsuario.getString(dadosUsuario.getColumnIndex("ATIVO_USUA")) != null) && (dadosUsuario.getString(dadosUsuario.getColumnIndex("ATIVO_USUA")).length() > 0)) {
                usuario.setAtivoUsuario(dadosUsuario.getString(dadosUsuario.getColumnIndex("ATIVO_USUA")).charAt(0));
            }
            usuario.setIpServidor(dadosUsuario.getString(dadosUsuario.getColumnIndex("IP_SERVIDOR_USUA")));
            usuario.setUsuarioServidor(dadosUsuario.getString(dadosUsuario.getColumnIndex("USUARIO_SERVIDOR_USUA")));
            usuario.setSenhaServidor(dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_SERVIDOR_USUA")));
            usuario.setPastaServidor(dadosUsuario.getString(dadosUsuario.getColumnIndex("PASTA_SERVIDOR_USUA")));
            usuario.setDataUltimoRecebimento(dadosUsuario.getString(dadosUsuario.getColumnIndex("DT_ULTIMO_RECEBIMENTO")));
            usuario.setDataUltimoEnvio(dadosUsuario.getString(dadosUsuario.getColumnIndex("DT_ULTIMO_ENVIO")));
            usuario.setValorCreditoAtacado(dadosUsuario.getDouble(dadosUsuario.getColumnIndex("VALOR_CREDITO_ATACADO")));
            usuario.setValorCreditoVarejo(dadosUsuario.getDouble(dadosUsuario.getColumnIndex("VALOR_CREDITO_VAREJO")));
            usuario.setPercentualCreditoAtacado(dadosUsuario.getDouble(dadosUsuario.getColumnIndex("PERCENTUAL_CREDITO_ATACADO")));
            usuario.setPercentualCreditoVarejo(dadosUsuario.getDouble(dadosUsuario.getColumnIndex("PERCENTUAL_CREDITO_VAREJO")));
            usuario.setModoConexao(dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO")));
        }

        return usuario;
    }

    public String modoConexao(){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        try {
            if (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                UsuarioSQL usuarioSQL = new UsuarioSQL(context);
                // Executa o sql para pegar os dados do usuario
                Cursor dadosUsuario = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));

                if (dadosUsuario != null && dadosUsuario.getCount() > 0){
                    // Move para o primeiro
                    dadosUsuario.moveToFirst();

                    return dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO"));
                }
            }
        } catch (Exception e){

        }
        return "";
    }

    public int quantidadeHorasUltimoEnvio(){
        int qtdHoras = -1;
        // Instancia a classe para manipular a tabela no banco de dados
        UsuarioSQL usuarioSQL = new UsuarioSQL(context);

        String sql = ("SELECT (((STRFTIME('%s','now') - STRFTIME('%s',USUARIO_USUA.DT_ULTIMO_ENVIO))/60)/60) AS HORAS FROM USUARIO_USUA");

        Cursor cursor = usuarioSQL.sqlSelect(sql);

        // Checa se retornou algum registro
        if( (cursor != null) && (cursor.getCount() > 0) ){
            // Move para o primeiro registro
            cursor.moveToFirst();
            // Pega o valor salvo no cursor
            qtdHoras = cursor.getInt(cursor.getColumnIndex("HORAS"));
        }
        return qtdHoras;
    }
}
