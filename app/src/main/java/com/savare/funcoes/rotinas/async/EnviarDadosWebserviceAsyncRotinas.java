package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by Bruno on 14/09/2016.
 */
public class EnviarDadosWebserviceAsyncRotinas  extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String[] tabelaEnviarDados = null;
    private ProgressBar progressBarStatus = null;
    private TextView textStatus = null;
    private Calendar calendario;
    private String[] idOrcamentoSelecionado = null;

    public EnviarDadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    public void setTabelaEnviarDados(String[] tabelaEnviarDados) {this.tabelaEnviarDados = tabelaEnviarDados; }

    public void setProgressBarStatus(ProgressBar progressBarStatus) { this.progressBarStatus = progressBarStatus; }

    public void setTextStatus(TextView textStatus) { this.textStatus = textStatus; }

    public void setIdOrcamentoSelecionado(String[] idOrcamentoSelecionado) { this.idOrcamentoSelecionado = idOrcamentoSelecionado; }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Inicializa a data
        calendario = Calendar.getInstance();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getText(R.string.aguarde_estamos_checando_se_existe_internet));
                }
            });
        }
        if (progressBarStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressBarStatus.setVisibility(View.VISIBLE);
                    progressBarStatus.setIndeterminate(true);
                }
            });
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("EnviandoDados", "S");

        if (funcoes.existeConexaoInternet()) {
            try {
                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()){
                    // Envia os orcamento AEAORCAM
                    if (((tabelaEnviarDados != null) && (tabelaEnviarDados.length > 0) &&
                            ((Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_INSERT_AEAORCAM)) || (Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_INSERT_AEAITORC))) ) ||
                            (tabelaEnviarDados == null) ){

                        // Envia os dados
                        enviaPedido();
                    }
                }
            } catch (Exception e) {
                final ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 2);
                mensagem.put("tela", "EnviarDadosWebServiceAsyncRotinas");
                mensagem.put("mensagem", e.getMessage());

                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        funcoes.menssagem(mensagem);
                    }
                });
            }
        }else {
            // Checa se o texto de status foi passado pro parametro
            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText("Não existe conexão com a internet.");


                    }
                });
            }
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 2);
            mensagem.put("tela", "EnviarDadosWebServiceAsyncRotinas");
            mensagem.put("mensagem", (context.getResources().getString((R.string.nao_existe_conexao_internet))));

            funcoes.menssagem(mensagem);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("EnviandoDados", "N");

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.conseguimos_enviar_pedido));
                }
            });
        }
        if (progressBarStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressBarStatus.setIndeterminate(true);
                    progressBarStatus.setVisibility(View.INVISIBLE);
                }
            });
        }

        ChecaDadosEnviadosWebserviceAsyncRotinas checaDadosEnviadosWebservice = new ChecaDadosEnviadosWebserviceAsyncRotinas(context);
        if (idOrcamentoSelecionado != null && idOrcamentoSelecionado.length > 0){
            checaDadosEnviadosWebservice.setIdOrcamentoSelecionado(idOrcamentoSelecionado);
        }
        checaDadosEnviadosWebservice.execute();
    }

    private void enviaPedido(){
        try {
            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText(context.getResources().getString(R.string.listando_pedidos_para_enviar));
                    }
                });
            }
            if (progressBarStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressBarStatus.setIndeterminate(true);
                    }
                });
            }

            List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();

            final OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

            String where = null;
            if ((idOrcamentoSelecionado != null) && (idOrcamentoSelecionado.length > 0)){
                // Adiciona a coluna a ser filtrada na tabela
                where = "AEAORCAM.ID_AEAORCAM IN(";

                int controle = 0;
                // Passa por todos os id's de orcamentos
                for (String id : idOrcamentoSelecionado) {
                    controle ++;
                    where += id;
                    // Checa se eh o ultimo da lista de tipos de pesquisa
                    if(controle < idOrcamentoSelecionado.length){
                        where += ", ";
                    } else {
                        where += ")";
                    }
                }
            }
            String[] listaTipo = null;
            if (where != null){
                listaTipo = new String[]{   OrcamentoRotinas.PEDIDO_NAO_ENVIADO,
                                            OrcamentoRotinas.PEDIDO_ENVIADO,
                                            OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                                            OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO,
                                            OrcamentoRotinas.PEDIDO_RETORNADO_EXCLUIDO,
                                            OrcamentoRotinas.PEDIDO_FATURADO};
            } else {
                listaTipo = new String[]{OrcamentoRotinas.PEDIDO_NAO_ENVIADO};
            }
            // Busca todos os pedidos nao enviados
            listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, where, OrcamentoRotinas.ORDEM_CRESCENTE);

            // Checa se retornou alguma coisa
            if ((listaOrcamento != null) && (listaOrcamento.size() > 0)){

                // Passa por todos os registros
                for (final OrcamentoBeans orcamento : listaOrcamento) {

                    PropertyInfo propertyOrcamento = new PropertyInfo();
                    propertyOrcamento.setName("orcamento");
                    propertyOrcamento.setValue(orcamento);
                    propertyOrcamento.setType(orcamento.getClass());

                    // Cria uma lista para salvar todas as propriedades
                    List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                    listaPropertyInfos.add(propertyOrcamento);

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento());
                            }
                        });
                    }
                    if (progressBarStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setIndeterminate(true);
                            }
                        });
                    }
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                    // Envia o orcamento para o webservice
                    final Vector<SoapObject> listaRetornoWeb = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_INSERT_AEAORCAM, listaPropertyInfos);

                    // Checa se retornou alguma coisa
                    if ((listaRetornoWeb != null) && (listaRetornoWeb.size() > 0)) {

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                }
                            });
                        }
                        if (progressBarStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setIndeterminate(false);
                                    progressBarStatus.setMax(listaRetornoWeb.size());
                                }
                            });
                        }
                        int controle = 0;

                        // Passa por toda a lista
                        for (SoapObject objetoIndividual : listaRetornoWeb) {
                            final int finalControle = controle;

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null){
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.enviando_pedidos) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                                    }
                                });
                            }
                            if (progressBarStatus != null){

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressBarStatus.setProgress(finalControle);
                                    }
                                });
                            }
                            controle ++;

                            SoapObject objeto;

                            if (objetoIndividual.hasProperty("return")) {
                                objeto = (SoapObject) objetoIndividual.getProperty("return");
                            } else {
                                objeto = objetoIndividual;
                            }
                            // Checa se voltou algum codigo de retorno
                            if (objeto.hasProperty("codigoRetorno")){

                                // Checa se o retorno foi o numero 100 (inserido com sucesso)
                                if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 100){
                                    // Cria uma vareavel para salvar o status do pedido
                                    ContentValues dadosPedido = new ContentValues();
                                    dadosPedido.put("STATUS", "N");
                                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);
                                    orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());

                                } else {
                                    listaOrcamento.remove(listaOrcamento.indexOf(orcamento));
                                }
                            }
                        } // Fim do for
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if ((listaOrcamento != null) && (listaOrcamento.size() > 0)) {

                            inserirUltimaAtualizacao("AEAORCAM_ENVIAR");
                            // Checa se tem algum orcamento na lista
                            if (listaOrcamento.size() > 0){
                                enviaItemPedido(listaOrcamento);
                            }
                        }
                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                }
                            });
                        }
                        if (progressBarStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setIndeterminate(true);
                                }
                            });
                        }
                    }
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void enviaItemPedido(List<OrcamentoBeans> listaOrcamento){
        try {
            // Passa por todos os orcamentos para enviar os itens
            for (OrcamentoBeans orcamento : listaOrcamento) {

                // Cria uma vareavel para pegar todos os itens do orcamento
                List<ItemOrcamentoBeans> listaItemOrcamento = new ArrayList<ItemOrcamentoBeans>();

                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

                // Busta todos os itens do orcamento
                listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, ""+orcamento.getIdOrcamento(), null, null);

                // Checa se retornou alguma coisa
                if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)){

                    // Passa por todos os registros
                    for (int i = 0; i < listaItemOrcamento.size(); i++) {
                        // Salva os ids mais importantes dentro da lista
                        listaItemOrcamento.get(i).setIdEstoqueTemp(listaItemOrcamento.get(i).getEstoqueVenda().getIdEstoque());
                        listaItemOrcamento.get(i).setIdVendedorTemp(listaItemOrcamento.get(i).getPessoaVendedor().getIdPessoa());
                        listaItemOrcamento.get(i).setIdUnidadeTemp(listaItemOrcamento.get(i).getUnidadeVenda().getIdUnidadeVenda());
                        listaItemOrcamento.get(i).setIdPlanoPagamentoTemp(listaItemOrcamento.get(i).getUnidadeVenda().getIdUnidadeVenda());
                        listaItemOrcamento.get(i).setOrcamento(orcamento);
                    }
                    // Vareavel para saber o total de enviados com sucesso
                    int totalEnviadoSucesso = 0;

                    for (int i = 0; i < listaItemOrcamento.size(); i ++){
                    //for (final ItemOrcamentoBeans itemOrc : listaItemOrcamento) {

                        final ItemOrcamentoBeans itemOrc = listaItemOrcamento.get(i);

                        // Cria uma lista para salvar todas as propriedades
                        List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                        PropertyInfo propertyOrcamento = new PropertyInfo();
                        propertyOrcamento.setName("itemOrcamento");
                        propertyOrcamento.setValue(itemOrc);
                        propertyOrcamento.setType(itemOrc.getClass());

                        listaPropertyInfos.add(propertyOrcamento);

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.enviando_item_pedido) + " Nº " + itemOrc.getIdItemOrcamento());
                                }
                            });
                        }
                        if (progressBarStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setIndeterminate(true);
                                }
                            });
                        }

                        WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                        final Vector<SoapObject> listaRetornoWeb = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_INSERT_AEAITORC, listaPropertyInfos);

                        // Checa se retornou alguma coisa
                        if ((listaRetornoWeb != null) && (listaRetornoWeb.size() > 0)) {
                            // Vareavel para saber se todos os dados foram inseridos com sucesso
                            boolean todosSucesso = true;

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null){
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                    }
                                });
                            }
                            if (progressBarStatus != null){
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressBarStatus.setIndeterminate(false);
                                        progressBarStatus.setMax(listaRetornoWeb.size());
                                    }
                                });
                            }
                            int controle = 0;

                            // Passa por toda a lista
                            for (SoapObject objetoIndividual : listaRetornoWeb) {
                                final int finalControle = controle;

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null){
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                                        }
                                    });
                                }
                                if (progressBarStatus != null){

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setProgress(finalControle);
                                        }
                                    });
                                }
                                controle ++;

                                SoapObject objeto;

                                if (objetoIndividual.hasProperty("return")) {
                                    objeto = (SoapObject) objetoIndividual.getProperty("return");
                                } else {
                                    objeto = objetoIndividual;
                                }
                                // Checa se voltou algum codigo de retorno
                                if (objeto.hasProperty("codigoRetorno")){
                                    // Checa se o retorno foi o numero 100 (inserido com sucesso)
                                    if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 100){
                                        // Incrementa o total de enviado com sucesso
                                        totalEnviadoSucesso ++;

                                        // Cria uma vareavel para salvar o status do pedido
                                        ContentValues dadosPedido = new ContentValues();
                                        dadosPedido.put("STATUS", "N");

                                        ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                        if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAITORC = " + itemOrc.getIdItemOrcamento()) <= 0){
                                            todosSucesso = false;
                                        }
                                    }
                                }
                            } // Fim do for SoapObject
                        }
                    }
                    // Checa se foram todos enviados com sucesso
                    if (totalEnviadoSucesso == listaItemOrcamento.size()){

                        // Cria uma lista para salvar todas as propriedades
                        List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                        PropertyInfo propertyOrcamento = new PropertyInfo();
                        propertyOrcamento.setName("orcamento");
                        propertyOrcamento.setValue(orcamento);
                        propertyOrcamento.setType(orcamento.getClass());

                        listaPropertyInfos.add(propertyOrcamento);

                        WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                        final Vector<SoapObject> listaRetornoWeb = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_UPDATE_STATUS_AEAORCAM, listaPropertyInfos);
                        // Checa se retornou alguma coisa
                        if ((listaRetornoWeb != null) && (listaRetornoWeb.size() > 0)) {
                            // Passa por toda a lista
                            for (SoapObject objetoIndividual : listaRetornoWeb) {

                                SoapObject objeto;

                                if (objetoIndividual.hasProperty("return")) {
                                    objeto = (SoapObject) objetoIndividual.getProperty("return");
                                } else {
                                    objeto = objetoIndividual;
                                }
                                // Checa se voltou algum codigo de retorno
                                if (objeto.hasProperty("codigoRetorno")){

                                    // Checa se o retorno foi o numero 100 (inserido com sucesso)
                                    if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 101){

                                        inserirUltimaAtualizacao("AEAITORC_ENVIAR");

                                        // Cria uma vareavel para salvar o status do pedido
                                        ContentValues dadosPedido = new ContentValues();
                                        dadosPedido.put("STATUS", "RB");

                                        ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                        if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAORCAM = " + orcamento.getIdOrcamento()) > 0){
                                            OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                            orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());
                                        }
                                    }
                                }
                            } // Fim do for SoapObject
                        }
                    }
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }




    private void inserirUltimaAtualizacao(String tabela){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String dataInicioAtualizacao = sdf.format(calendario.getTime());

        Calendar dtAlt = Calendar.getInstance();

        ContentValues dataAtualizacao = new ContentValues();
        dataAtualizacao.put("ID_DISPOSITIVO", telephonyManager.getDeviceId());
        dataAtualizacao.put("TABELA", tabela);
        dataAtualizacao.put("DT_ALT", sdf.format(dtAlt.getTime()));
        dataAtualizacao.put("DATA_ULTIMA_ATUALIZACAO", dataInicioAtualizacao);

        UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);

        ultimaAtualizacaoSql.insertOrReplace(dataAtualizacao);
    }
}
