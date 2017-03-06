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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.CriticaOrcamentoRotina;
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
    private OnTaskCompleted listenerTaskCompleted;
    // Cria uma notificacao para ser manipulado
    Load mLoad;

    public EnviarDadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    public EnviarDadosWebserviceAsyncRotinas(OnTaskCompleted listenerTaskCompleted, Context context) {
        this.listenerTaskCompleted = listenerTaskCompleted;
        this.context = context;
    }

    public interface OnTaskCompleted{
        void onTaskCompleted();
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
        mLoad = PugNotification.with(context).load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .title(R.string.enviar_pedido_nuvem)
                .flags(Notification.DEFAULT_LIGHTS);

        mLoad.bigTextStyle("Aguarde, vamor enviar os dados. Primeiro vamos ver se tem internet...");
        mLoad.progress().value(0, 0, true).build();
    }

    @Override
    protected Void doInBackground(Void... params) {
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("EnviandoDados", "S");

        if (funcoes.existeConexaoInternet()) {

            mLoad.bigTextStyle(R.string.estamos_checando_webservice_online);
            mLoad.progress().value(0, 0, true).build();

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

        // Checa se a interface de retorno do asynctask eh diferente de nula
        if (listenerTaskCompleted != null) {
            listenerTaskCompleted.onTaskCompleted();
        }

        // Cria uma notificacao para ser manipulado
        PugNotification.with(context)
                .load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                .title(R.string.enviar_pedido_nuvem)
                //.message(context.getResources().getString(R.string.terminamos_enviar_dados))
                .bigTextStyle(context.getResources().getString(R.string.terminamos_enviar_dados))
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .flags(Notification.DEFAULT_LIGHTS)
                .simple()
                .build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.terminamos_enviar_dados));
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
        /*ChecaDadosEnviadosWebserviceAsyncRotinas checaDadosEnviadosWebservice = new ChecaDadosEnviadosWebserviceAsyncRotinas(context);
        if (idOrcamentoSelecionado != null && idOrcamentoSelecionado.length > 0){
            checaDadosEnviadosWebservice.setIdOrcamentoSelecionado(idOrcamentoSelecionado);
        }
        checaDadosEnviadosWebservice.execute();*/
    }

    private void enviaPedido(){
        JsonObject statuRetorno = null;
        int totalPedidoEnviado = 0;
        try {
            // Indica que essa notificacao eh do tipo progress
            mLoad.bigTextStyle(context.getResources().getString(R.string.estamos_conectanto_servidor_nuvem));
            mLoad.progress().value(0, 0, true).build();

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

            // Indica que essa notificacao eh do tipo progress
            mLoad.bigTextStyle(context.getResources().getString(R.string.listando_pedidos_para_enviar));
            mLoad.progress().value(0, 0, true).build();

            // Busca todos os pedidos nao enviados
            listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, where, OrcamentoRotinas.ORDEM_CRESCENTE);

            // Checa se retornou alguma coisa
            if ((listaOrcamento != null) && (listaOrcamento.size() > 0)){

                // Passa por todos os registros
                for (final OrcamentoBeans orcamento : listaOrcamento) {

                    // Busta todos os itens do orcamento
                    List<ItemOrcamentoBeans> listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, ""+orcamento.getIdOrcamento(), null, null);

                    orcamento.setListaItemOrcamento(listaItemOrcamento);

                    // Indica que essa notificacao eh do tipo progress
                    mLoad.bigTextStyle(context.getResources().getString(R.string.enviando_pedidos_confirmando) + " Nº " + orcamento.getIdOrcamento());
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.enviando_pedidos_confirmando) + " Nº " + orcamento.getIdOrcamento());
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
                    // Instancia gson para gera json
                    Gson gson = new Gson();
                    String tempJsonRetorno = webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_INSERT_AEAORCAM, WSSisinfoWebservice.METODO_POST, gson.toJson(orcamento, orcamento.getClass()));
                    JsonObject retornoWebservice = gson.fromJson(tempJsonRetorno, JsonObject.class);

                    if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))){
                        // Indica que essa notificacao eh do tipo progress
                        mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mLoad.progress().value(0, 0, true).build();

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
                                    progressBarStatus.setIndeterminate(true);
                                }
                            });
                        }
                        // Pega o objeto de status retornado do webservice
                        statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                        if (statuRetorno != null) {
                            // Indica que essa notificacao eh do tipo progress
                            mLoad.bigTextStyle(context.getResources().getString(R.string.salvando_critica_retorno));
                            mLoad.progress().value(0, 0, true).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null){
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.salvando_critica_retorno));
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

                            final ContentValues dadosCritica = new ContentValues();
                            dadosCritica.put("ID_AEAORCAM", orcamento.getIdOrcamento());
                            dadosCritica.put("CODIGO_RETORNO_WEBSERVICE", ((statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() : -1));
                            dadosCritica.put("RETORNO_WEBSERVICE", ((statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() : "") + " \n " +
                                                                    (statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "")));

                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 100) {

                                dadosCritica.put("STATUS", OrcamentoRotinas.PEDIDO_ENVIADO);

                                totalPedidoEnviado++;

                                inserirUltimaAtualizacao("AEAITORC_ENVIAR");
                                inserirUltimaAtualizacao("AEAORCAM_ENVIAR");

                                // Cria uma vareavel para salvar o status do pedido
                                ContentValues dadosPedido = new ContentValues();
                                dadosPedido.put("STATUS", "RB");

                                ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAORCAM = " + orcamento.getIdOrcamento()) > 0) {
                                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                    orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());

                                    mLoad.bigTextStyle(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                    mLoad.progress().value(0, 0, true).build();

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                            }
                                        });
                                    }
                                }
                            } else {
                                dadosCritica.put("STATUS", OrcamentoRotinas.PEDIDO_ERRO_ENVIAR);

                                listaOrcamento.remove(listaOrcamento.indexOf(orcamento));

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + context.hashCode())
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.enviar_pedido_nuvem)
                                        .bigTextStyle("Código Retorno: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() + "\n" : "Sem código.\n")
                                                + "Mensagem: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() + "\n" : "Não conseguimos enviar o pedido Nº " + orcamento.getIdOrcamento() + "\n")
                                                + "Extra: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "\n"))
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                            final CriticaOrcamentoRotina criticaOrcamentoRotina = new CriticaOrcamentoRotina(context);

                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    criticaOrcamentoRotina.insertCriticaOrcamento(dadosCritica);
                                }
                            });
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.enviar_pedido_nuvem)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice))
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                                }
                            });
                        }
                        if (progressBarStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }


                    /*PropertyInfo propertyOrcamento = new PropertyInfo();
                    propertyOrcamento.setName("orcamento");
                    propertyOrcamento.setValue(orcamento);
                    propertyOrcamento.setType(orcamento.getClass());

                    // Cria uma lista para salvar todas as propriedades
                    List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                    listaPropertyInfos.add(propertyOrcamento);

                    // Indica que essa notificacao eh do tipo progress
                    mLoad.bigTextStyle(context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento());
                    mLoad.progress().value(0, 0, true).build();

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

                        mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mLoad.progress().value(0, 0, true).build();

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

                            mLoad.bigTextStyle(context.getResources().getString(R.string.enviando_pedidos) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                            mLoad.progress().value(0, 0, true).build();

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
                                // Checa se o retorno foi o numero 100 (inserido com sucesso)
                                if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) != 100){
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
                                aqui
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
                    }*/
                }
            }
            if (totalPedidoEnviado == listaOrcamento.size()){
                // Cria uma notificacao para ser manipulado
                Load mLoad = PugNotification.with(context).load()
                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(R.mipmap.ic_launcher)
                        .title(R.string.enviar_pedido_nuvem)
                        .message(R.string.pedidos_enviados)
                        .flags(Notification.DEFAULT_SOUND);
                mLoad.simple().build();

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.pedidos_enviados));
                        }
                    });
                }
                if (progressBarStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.GONE);
                        }
                    });
                }

            } else if (totalPedidoEnviado < listaOrcamento.size()){
                // Cria uma notificacao para ser manipulado
                Load mLoad = PugNotification.with(context).load()
                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(R.mipmap.ic_launcher)
                        .title(R.string.enviar_pedido_nuvem)
                        .message(R.string.nem_todos_pedidos_foram_enviados_tente_novamente)
                        .flags(Notification.DEFAULT_SOUND);
                mLoad.simple().build();

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.nem_todos_pedidos_foram_enviados_tente_novamente));
                        }
                    });
                }
                if (progressBarStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void enviaItemPedido(List<OrcamentoBeans> listaOrcamento){
        try {
            mLoad.bigTextStyle(context.getResources().getString(R.string.vamos_enviar_item_pedido));
            mLoad.progress().value(0, 0, true).build();

            // Passa por todos os orcamentos para enviar os itens
            for (final OrcamentoBeans orcamento : listaOrcamento) {

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
                        listaItemOrcamento.get(i).setIdPlanoPagamentoTemp(listaItemOrcamento.get(i).getPlanoPagamento().getIdPlanoPagamento());
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

                        mLoad.bigTextStyle( context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento() + "\n" +
                                            context.getResources().getString(R.string.enviando_item_pedido) + " Nº " + itemOrc.getSequencia() + "/" + listaItemOrcamento.size());
                        mLoad.progress().value(itemOrc.getSequencia(), listaItemOrcamento.size(), false).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            final int finalListaItemOrcamento = listaItemOrcamento.size();
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText( context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento() + "\n" +
                                                        context.getResources().getString(R.string.enviando_item_pedido) + " Nº " + itemOrc.getSequencia() + "/" + finalListaItemOrcamento);
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

                            mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            mLoad.progress().value(0, 0, true).build();

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

                                mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                                mLoad.progress().value(0, 0, true).build();

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

                                        mLoad.bigTextStyle(context.getResources().getString(R.string.marcando_item_enviado_sucesso));
                                        mLoad.progress().value(0, 0, true).build();

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null){
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.marcando_item_enviado_sucesso));
                                                }
                                            });
                                        }
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

                        mLoad.bigTextStyle(context.getResources().getString(R.string.vamos_checar_pedido_enviado));
                        mLoad.progress().value(0, 0, true).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.vamos_checar_pedido_enviado));
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

                                    // Checa se o retorno foi o numero 101 (atualizado com sucesso)
                                    if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 101){

                                        inserirUltimaAtualizacao("AEAITORC_ENVIAR");
                                        inserirUltimaAtualizacao("AEAORCAM_ENVIAR");

                                        // Cria uma vareavel para salvar o status do pedido
                                        ContentValues dadosPedido = new ContentValues();
                                        dadosPedido.put("STATUS", "RB");

                                        ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                        if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAORCAM = " + orcamento.getIdOrcamento()) > 0){

                                            OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                            orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());

                                            mLoad.bigTextStyle(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                            mLoad.progress().value(0, 0, true).build();

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null){
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                                    }
                                                });
                                            }
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
