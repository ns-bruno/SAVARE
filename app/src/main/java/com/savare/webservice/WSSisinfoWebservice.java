package com.savare.webservice;

import android.content.Context;

import com.savare.beans.ConexaoFIrebirdBeans;
import com.savare.funcoes.FuncoesPersonalizadas;

/**
 * Created by Bruno Nogueira Silva on 23/06/2016.
 */
public class WSSisinfoWebservice {

    private Context context;
    private ConexaoFIrebirdBeans conexaoFIrebirdBeans;

    public WSSisinfoWebservice(Context context) {
        this.context = context;

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        conexaoFIrebirdBeans = new ConexaoFIrebirdBeans();
        conexaoFIrebirdBeans.setIPServidor(((!funcoes.getValorXml("IPServidor").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("IPServidor") : "localhost"));
        conexaoFIrebirdBeans.setLocalBanco(((!funcoes.getValorXml("CaminhoBancoDados").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("CaminhoBancoDados") : "C:\\si.fir"));
        conexaoFIrebirdBeans.setPorta((!funcoes.getValorXml("PortaBancoDados").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("PortaBancoDados") : "3050");
        conexaoFIrebirdBeans.setUsuario((!funcoes.getValorXml("UsuarioServidor").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("UsuarioServidor") : "");
        conexaoFIrebirdBeans.setSenha((!funcoes.getValorXml("SenhaServidor").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("SenhaServidor") : "");
        conexaoFIrebirdBeans.setCertificado((!funcoes.getValorXml("ChaveEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? funcoes.getValorXml("ChaveEmpresa") : "");
    }
}
