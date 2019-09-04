package com.savare.funcoes.rotinas.async;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.ServidoresBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.CriticaOrcamentoRotina;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.ServidoresRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;


/**
 * Created by Bruno on 14/09/2016.
 */
public class EnviarDadosWebserviceAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String[] tabelaEnviarDados = null;
    private ProgressBar progressBarStatus = null;
    private TextView textStatus = null;
    private Calendar calendario;
    private String[] idOrcamentoSelecionado = null;
    private String idPessoaTemporario = null;
    private ServidoresBeans servidorAtivo;
    private OnTaskCompleted listenerTaskCompleted;
    // Cria uma notificacao para ser manipulado
    NotificationManager notificationManager;
    NotificationCompat.BigTextStyle bigTextStyle;
    NotificationCompat.Builder mBuilder;

    public EnviarDadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    public EnviarDadosWebserviceAsyncRotinas(OnTaskCompleted listenerTaskCompleted, Context context) {
        this.listenerTaskCompleted = listenerTaskCompleted;
        this.context = context;
    }

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }

    public void setTabelaEnviarDados(String[] tabelaEnviarDados) {
        this.tabelaEnviarDados = tabelaEnviarDados;
    }

    public void setProgressBarStatus(ProgressBar progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    public void setTextStatus(TextView textStatus) {
        this.textStatus = textStatus;
    }

    public void setIdOrcamentoSelecionado(String[] idOrcamentoSelecionado) {
        this.idOrcamentoSelecionado = idOrcamentoSelecionado;
    }

    public String getIdPessoaTemporario() {
        return idPessoaTemporario;
    }

    public void setIdPessoaTemporario(String idPessoaTemporario) {
        this.idPessoaTemporario = idPessoaTemporario;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Inicializa a data
        calendario = Calendar.getInstance();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getText(R.string.aguarde_estamos_checando_se_existe_internet));
                }
            });
        }
        if (progressBarStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressBarStatus.setVisibility(View.VISIBLE);
                    progressBarStatus.setIndeterminate(true);
                }
            });
        }
        /*mLoad = PugNotification.with(context).load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .title(R.string.enviar_dados_nuvem)
                .flags(Notification.DEFAULT_LIGHTS);

        mLoad.bigTextStyle("Aguarde, vamor enviar os dados. Primeiro vamos ver se tem internet...");
        mLoad.progress().value(0, 0, true).build();*/

        String name = "FileDownload";

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Create a BigTextStyle object.
        bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_vamos_enviar_dados));
        //bigTextStyle.setBigContentTitle("Happy Christmas Detail Info.");

        mBuilder = new NotificationCompat.Builder(context, ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                //.setContentText(mActivity.getResources().getString(R.string.app_name))
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setSound(null, 0)
                .setVibrate(new long[0])
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());
    }

    @Override
    protected Void doInBackground(Void... params) {
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("EnviandoDados", "S");

        if (funcoes.existeConexaoInternet()) {
            bigTextStyle.bigText(context.getResources().getString(R.string.estamos_checando_webservice_online));
            mBuilder.setStyle(bigTextStyle);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

            try {
                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()) {
                    // Envia os orcamento AEAORCAM
                    if (((tabelaEnviarDados != null) && (tabelaEnviarDados.length > 0) &&
                            ((Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_INSERT_AEAORCAM)) || (Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_INSERT_AEAITORC)))) ||
                            (tabelaEnviarDados == null)) {

                        // Envia os dados
                        enviaPedido();
                    }
                    // Envia os orcamento CFACLIFO
                    if (((tabelaEnviarDados != null) && (tabelaEnviarDados.length > 0) &&
                            ((Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO)) || (Arrays.asList(tabelaEnviarDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO)))) ) {

                        // Envia os dados
                        enviaCadastroCliente();
                    }
                }
            } catch (final Exception e) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("EnviarDadosWebServiceAsyncRotinas")
                                .content(context.getResources().getString(R.string.erro_inesperado) + "\n" + e.getMessage())
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
            }
        } else {
            // Checa se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText("Não existe conexão com a internet.");


                    }
                });
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("EnviarDadosWebServiceAsyncRotinas")
                            .content(context.getResources().getString(R.string.nao_existe_conexao_internet))
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml(funcoes.TAG_ENVIANDO_DADOS, "N");

        // Cria uma notificacao para ser manipulado
        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                    .bigText(context.getResources().getString(R.string.terminamos_enviar_dados));
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, false);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());
        notificationManager.cancel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS);

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.terminamos_enviar_dados));
                }
            });
        }
        if (progressBarStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressBarStatus.setIndeterminate(true);
                    progressBarStatus.setVisibility(View.INVISIBLE);
                }
            });
        }
        // Checa se a interface de retorno do asynctask eh diferente de nula
        if (listenerTaskCompleted != null) {
            listenerTaskCompleted.onTaskCompleted();
        }
    }

    private void enviaPedido() {
        JsonObject statuRetorno = null;
        int totalPedidoEnviado = 0;
        try {
            ServidoresRotinas servidoresRotinas = new ServidoresRotinas(context);

            List<ServidoresBeans> listaServidores = servidoresRotinas.listaServidores(null, "ID_SERVIDORES ASC", null);
            // Verifica se retornou alguma lista de servidores
            if ( (listaServidores!= null) && (listaServidores.size() > 0)) {

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                // Passa por todos os servidores para verficar qual eh o primeiro que esta online
                for (final ServidoresBeans servidor : listaServidores) {
                    bigTextStyle.bigText(context.getResources().getString(R.string.estamos_checando_webservice_online) + " - " + servidor.getNomeServidor());
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                //final String nomeServidor = servidor.getNomeServidor();
                                textStatus.setText(context.getResources().getText(R.string.estamos_checando_webservice_online) + " - " + servidor.getNomeServidor());
                            }
                        });
                    }
                    if (progressBarStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setVisibility(View.VISIBLE);
                                progressBarStatus.setIndeterminate(true);
                            }
                        });
                    }
                    // Pinga o IP da lista
                    if (funcoes.pingHost(servidor.getIpServidor(), servidor.getPorta())){
                        servidorAtivo = new ServidoresBeans();
                        servidorAtivo = servidor;
                        break;
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(context)
                                        .title("EnviarDadosWebServiceAsyncRotinas")
                                        .content(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor())
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
//                        new MaterialDialog.Builder(context)
//                                .title("EnviarDadosWebserviceAsyncRotinas")
//                                .content(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor())
//                                .positiveText(R.string.button_ok)
//                                .show();

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor());
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor());
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } // Fim for listaServidores
                // Verifica se tem algum servidor ativo
                if (servidorAtivo == null){
                    new MaterialDialog.Builder(context)
                            .title("EnviarDadosWebserviceAsyncRotinas")
                            .content(context.getResources().getString(R.string.aparentemente_servidor_webservice_offline))
                            .positiveText(R.string.button_ok)
                            .show();
                } else {
                    // Indica que essa notificacao eh do tipo progress
                    bigTextStyle.bigText(context.getResources().getString(R.string.estamos_conectanto_servidor_webservice) + " - " + servidorAtivo.getNomeServidor());
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.listando_pedidos_para_enviar));
                            }
                        });
                    }
                    if (progressBarStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setIndeterminate(true);
                            }
                        });
                    }

                    List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();

                    final OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

                    String where = null;
                    if ((idOrcamentoSelecionado != null) && (idOrcamentoSelecionado.length > 0)) {
                        // Adiciona a coluna a ser filtrada na tabela
                        where = "AEAORCAM.ID_AEAORCAM IN(";

                        int controle = 0;
                        // Passa por todos os id's de orcamentos
                        for (String id : idOrcamentoSelecionado) {
                            controle++;
                            where += id;
                            // Checa se eh o ultimo da lista de tipos de pesquisa
                            if (controle < idOrcamentoSelecionado.length) {
                                where += ", ";
                            } else {
                                where += ")";
                            }
                        }
                    }
                    String[] listaTipo = null;
                    if (where != null) {
                        listaTipo = new String[]{OrcamentoRotinas.PEDIDO_NAO_ENVIADO,
                                OrcamentoRotinas.PEDIDO_ENVIADO,
                                OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                                OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO,
                                OrcamentoRotinas.PEDIDO_RETORNADO_EXCLUIDO,
                                OrcamentoRotinas.PEDIDO_FATURADO};
                    } else {
                        listaTipo = new String[]{OrcamentoRotinas.PEDIDO_NAO_ENVIADO};
                    }

                    // Indica que essa notificacao eh do tipo progress
                    bigTextStyle.bigText(context.getResources().getString(R.string.listando_pedidos_para_enviar));
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                    // Busca todos os pedidos nao enviados
                    listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, where, OrcamentoRotinas.ORDEM_CRESCENTE);

                    // Checa se retornou alguma coisa
                    if ((listaOrcamento != null) && (listaOrcamento.size() > 0)) {

                        // Passa por todos os registros
                        for (final OrcamentoBeans orcamento : listaOrcamento) {

                            // Busta todos os itens do orcamento
                            List<ItemOrcamentoBeans> listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, "" + orcamento.getIdOrcamento(), null, null);

                            orcamento.setListaItemOrcamento(listaItemOrcamento);

                            // Indica que essa notificacao eh do tipo progress
                            bigTextStyle.bigText(context.getResources().getString(R.string.enviando_pedidos_confirmando) + " Nº " + orcamento.getIdOrcamento());
                            mBuilder.setStyle(bigTextStyle);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.enviando_pedidos_confirmando) + " Nº " + orcamento.getIdOrcamento());
                                    }
                                });
                            }
                            if (progressBarStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressBarStatus.setIndeterminate(true);
                                    }
                                });
                            }
                            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                            // Instancia gson para gera json
                            JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo,null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_AEAORCAM, WSSisinfoWebservice.METODO_POST, new Gson().toJson(orcamento, orcamento.getClass()), null), JsonObject.class);

                            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                                // Indica que essa notificacao eh do tipo progress
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {
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
                                    bigTextStyle.bigText(context.getResources().getString(R.string.salvando_critica_retorno));
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.salvando_critica_retorno));
                                            }
                                        });
                                    }
                                    if (progressBarStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                progressBarStatus.setIndeterminate(true);
                                            }
                                        });
                                    }

                                    final ContentValues dadosCritica = new ContentValues();
                                    dadosCritica.put("ID_AEAORCAM", orcamento.getIdOrcamento());
                                    dadosCritica.put("CODIGO_RETORNO_WEBSERVICE", ((statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() : -1));
                                    dadosCritica.put("RETORNO_WEBSERVICE", ((statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() : "") +
                                            (statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO) ? " \n " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "")));

                                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

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

                                            bigTextStyle.bigText(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                            mBuilder.setStyle(bigTextStyle);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

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
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + context.hashCode())
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.enviar_dados_nuvem)
                                            .bigTextStyle("Código Retorno: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() + "\n" : "Sem código.\n")
                                                    + "Mensagem: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() + "\n" : "Não conseguimos enviar o pedido Nº " + orcamento.getIdOrcamento() + "\n")
                                                    + "Extra: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "\n"))
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                                .bigText("Código Retorno: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() + "\n" : "Sem código.\n")
                                                        + "Mensagem: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() + "\n" : "Não conseguimos enviar o pedido Nº " + orcamento.getIdOrcamento() + "\n")
                                                        + "Extra: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "\n"));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());
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
                            /*Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS)
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.enviar_dados_nuvem)
                                    .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice))
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();*/

                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                        }
                    }
                    if (totalPedidoEnviado == listaOrcamento.size()) {

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.pedidos_enviados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.pedidos_enviados));
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }

                    } else if (totalPedidoEnviado < listaOrcamento.size()) {

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.nem_todos_pedidos_foram_enviados_tente_novamente));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nem_todos_pedidos_foram_enviados_tente_novamente));
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }

            } else {
                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados))
                        .bigText(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados));
                        }
                    });
                }
                if (progressBarStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.GONE);
                        }
                    });
                }
            }
        } catch (Exception e) {

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                    .bigText(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void enviaCadastroCliente() {
        JsonObject statuRetorno = null;
        int totalPedidoEnviado = 0;
        try {
            ServidoresRotinas servidoresRotinas = new ServidoresRotinas(context);

            List<ServidoresBeans> listaServidores = servidoresRotinas.listaServidores(null, "ID_SERVIDORES ASC", null);
            // Verifica se retornou alguma lista de servidores
            if ( (listaServidores!= null) && (listaServidores.size() > 0)) {

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                // Passa por todos os servidores para verficar qual eh o primeiro que esta online
                for (final ServidoresBeans servidor : listaServidores) {
                    bigTextStyle.bigText(context.getResources().getString(R.string.estamos_checando_webservice_online) + " - " + servidor.getNomeServidor());
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                //final String nomeServidor = servidor.getNomeServidor();
                                textStatus.setText(context.getResources().getText(R.string.estamos_checando_webservice_online) + " - " + servidor.getNomeServidor());
                            }
                        });
                    }
                    if (progressBarStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setVisibility(View.VISIBLE);
                                progressBarStatus.setIndeterminate(true);
                            }
                        });
                    }
                    // Pinga o IP da lista
                    if (funcoes.pingHost(servidor.getIpServidor(), servidor.getPorta())){
                        servidorAtivo = new ServidoresBeans();
                        servidorAtivo = servidor;
                        break;
                    } else {
                        new MaterialDialog.Builder(context)
                                .title("EnviarDadosWebserviceAsyncRotinas")
                                .content(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor())
                                .positiveText(R.string.button_ok)
                                .show();

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor());
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor());
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } // Fim for listaServidores
                // Verifica se tem algum servidor ativo
                if (servidorAtivo == null){
                    new MaterialDialog.Builder(context)
                            .title("EnviarDadosWebserviceAsyncRotinas")
                            .content(context.getResources().getString(R.string.aparentemente_servidor_webservice_offline))
                            .positiveText(R.string.button_ok)
                            .show();
                } else {
                    // Indica que essa notificacao eh do tipo progress
                    bigTextStyle.bigText(context.getResources().getString(R.string.listando_clientes_novos));
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.listando_clientes_novos));
                            }
                        });
                    }
                    if (progressBarStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setIndeterminate(true);
                            }
                        });
                    }
                    String where = "";
                    // Checa se foi passado algum parametro
                    if (idPessoaTemporario != null){
                        // Especifica uma pessoa
                        where += " (CFACLIFO.ID_CFACLIFO = " + idPessoaTemporario + ")";
                    } else {
                        // Pega todos os cadastro temporarios
                        where += " (CFACLIFO.ID_CFACLIFO < 0) AND (CFACLIFO.STATUS_CADASTRO_NOVO = 'N') ";
                    }

                    PessoaRotinas pessoaRotinas = new PessoaRotinas(context);

                    // Pega a lista de pessoa a serem enviadas os dados
                    List<PessoaBeans> listaPessoasCadastro = pessoaRotinas.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
                    // Checa se retornou alguma lista
                    if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0){
                        // Passa por todos os cadastro
                        for (final PessoaBeans pessoa : listaPessoasCadastro){
                            // Indica que essa notificacao eh do tipo progress
                            bigTextStyle.bigText(context.getResources().getString(R.string.enviando_cliente_novo) + " Nº " + pessoa.getIdPessoa());
                            mBuilder.setStyle(bigTextStyle);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.enviando_cliente_novo) + " Nº " + pessoa.getIdPessoa());
                                    }
                                });
                            }
                            if (progressBarStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressBarStatus.setIndeterminate(true);
                                    }
                                });
                            }
                            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                            // Instancia gson para gera json
                            JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo,null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO, WSSisinfoWebservice.METODO_POST, new Gson().toJson(pessoa, pessoa.getClass()), null), JsonObject.class);

                            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                                // Indica que essa notificacao eh do tipo progress
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {
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
                                    bigTextStyle.bigText(context.getResources().getString(R.string.salvando_critica_retorno));
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.salvando_critica_retorno));
                                            }
                                        });
                                    }
                                    if (progressBarStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                progressBarStatus.setIndeterminate(true);
                                            }
                                        });
                                    }
                                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                                        PessoaSql pessoaSql = new PessoaSql(context);

                                        ContentValues atualizaPessoa = new ContentValues();
                                        // Marca que o cadastro foi enviado
                                        atualizaPessoa.put("STATUS_CADASTRO_NOVO", "E");

                                        // Marca o novo cadastro como enviado
                                        if ((pessoaSql.update(atualizaPessoa, "CFACLIFO.ID_CFACLIFO = " + pessoa.getIdPessoa())) > 0){

                                            bigTextStyle.bigText(context.getResources().getString(R.string.marcando_cliente_novo_enviado_sucesso));
                                            mBuilder.setStyle(bigTextStyle);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.marcando_cliente_novo_enviado_sucesso));
                                                    }
                                                });
                                            }
                                        }
                                        totalPedidoEnviado++;

                                        inserirUltimaAtualizacao("CFACLIFO_ENVIAR");
                                    } else {
                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                                .bigText("Código Retorno: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() + "\n" : "Sem código.\n")
                                                        + "Mensagem: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() + "\n" : "Não conseguimos enviar o cliente novo Nº " + pessoa.getIdPessoa() + "\n")
                                                        + "Extra: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "\n"));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                                        new MaterialDialog.Builder(context)
                                                .title("EnviarDadosWebserviceAsyncRotinas")
                                                .content("Código Retorno: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() + "\n" : "Sem código.\n")
                                                        + "Mensagem: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() + "\n" : "Não conseguimos enviar o cliente novo Nº " + pessoa.getIdPessoa() + "\n")
                                                        + "Extra: " + ((statuRetorno != null && statuRetorno.has(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO)) ? statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_EXTRA_RETORNO).getAsString() : "\n"))
                                                .positiveText(R.string.button_ok)
                                                .show();
                                    }
                                }
                            } else {

                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                        }
                    } else {
                        new MaterialDialog.Builder(context)
                                .title("EnviarDadosWebserviceAsyncRotinas")
                                .content(context.getResources().getString(R.string.nao_achamos_clientes_novos))
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                    if ( (listaPessoasCadastro.size() > 0) && (totalPedidoEnviado == listaPessoasCadastro.size())) {

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.cliente_novo_enviados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.cliente_novo_enviados));
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }

                    } else if (totalPedidoEnviado < listaPessoasCadastro.size()) {

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                                .bigText(context.getResources().getString(R.string.nem_todos_clientes_novos_foram_enviados_tente_novamente));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nem_todos_clientes_novos_foram_enviados_tente_novamente));
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                    if (totalPedidoEnviado > 0){
                        ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(context);
                        receberDados.setTabelaRecebeDados(new String[]{WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO,
                                                                       WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM,
                                                                       WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM});
                        receberDados.setProgressBarStatus(progressBarStatus);
                        receberDados.setTextStatus(textStatus);
                        receberDados.execute();
                    }
                }

            } else {
                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados))
                        .bigText(context.getResources().getString(R.string.nem_todos_clientes_novos_foram_enviados_tente_novamente));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS + new Random().nextInt(100), mBuilder.build());

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.nem_todos_pedidos_foram_enviados_tente_novamente));
                        }
                    });
                }
                if (progressBarStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.GONE);
                        }
                    });
                }
            }
        } catch (Exception e) {

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                    .bigText(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void enviaItemPedido(List<OrcamentoBeans> listaOrcamento) {
        try {
            bigTextStyle.bigText(context.getResources().getString(R.string.vamos_enviar_item_pedido));
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, true);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

            // Passa por todos os orcamentos para enviar os itens
            for (final OrcamentoBeans orcamento : listaOrcamento) {

                // Cria uma vareavel para pegar todos os itens do orcamento
                List<ItemOrcamentoBeans> listaItemOrcamento = new ArrayList<ItemOrcamentoBeans>();

                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

                // Busta todos os itens do orcamento
                listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, "" + orcamento.getIdOrcamento(), null, null);

                // Checa se retornou alguma coisa
                if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)) {

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

                    for (int i = 0; i < listaItemOrcamento.size(); i++) {
                        //for (final ItemOrcamentoBeans itemOrc : listaItemOrcamento) {

                        final ItemOrcamentoBeans itemOrc = listaItemOrcamento.get(i);

                        // Cria uma lista para salvar todas as propriedades
                        List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                        PropertyInfo propertyOrcamento = new PropertyInfo();
                        propertyOrcamento.setName("itemOrcamento");
                        propertyOrcamento.setValue(itemOrc);
                        propertyOrcamento.setType(itemOrc.getClass());

                        listaPropertyInfos.add(propertyOrcamento);

                        bigTextStyle.bigText(context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento() + "\n" +
                                context.getResources().getString(R.string.enviando_item_pedido) + " Nº " + itemOrc.getSequencia() + "/" + listaItemOrcamento.size());
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(listaItemOrcamento.size(), itemOrc.getSequencia(), false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            final int finalListaItemOrcamento = listaItemOrcamento.size();
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.enviando_pedidos) + " Nº " + orcamento.getIdOrcamento() + "\n" +
                                            context.getResources().getString(R.string.enviando_item_pedido) + " Nº " + itemOrc.getSequencia() + "/" + finalListaItemOrcamento);
                                }
                            });
                        }
                        if (progressBarStatus != null) {
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

                            bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            mBuilder.setStyle(bigTextStyle)
                                    .setProgress(0, 0, true);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                    }
                                });
                            }
                            if (progressBarStatus != null) {
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

                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaRetornoWeb.size());
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setProgress(finalControle);
                                        }
                                    });
                                }
                                controle++;

                                SoapObject objeto;

                                if (objetoIndividual.hasProperty("return")) {
                                    objeto = (SoapObject) objetoIndividual.getProperty("return");
                                } else {
                                    objeto = objetoIndividual;
                                }
                                // Checa se voltou algum codigo de retorno
                                if (objeto.hasProperty("codigoRetorno")) {
                                    // Checa se o retorno foi o numero 100 (inserido com sucesso)
                                    if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 100) {
                                        // Incrementa o total de enviado com sucesso
                                        totalEnviadoSucesso++;

                                        bigTextStyle.bigText(context.getResources().getString(R.string.marcando_item_enviado_sucesso));
                                        mBuilder.setStyle(bigTextStyle);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
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

                                        if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAITORC = " + itemOrc.getIdItemOrcamento()) <= 0) {
                                            todosSucesso = false;
                                        }
                                    }
                                }
                            } // Fim do for SoapObject
                        }
                    }
                    // Checa se foram todos enviados com sucesso
                    if (totalEnviadoSucesso == listaItemOrcamento.size()) {

                        // Cria uma lista para salvar todas as propriedades
                        List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                        PropertyInfo propertyOrcamento = new PropertyInfo();
                        propertyOrcamento.setName("orcamento");
                        propertyOrcamento.setValue(orcamento);
                        propertyOrcamento.setType(orcamento.getClass());

                        listaPropertyInfos.add(propertyOrcamento);

                        WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                        bigTextStyle.bigText(context.getResources().getString(R.string.vamos_checar_pedido_enviado));
                        mBuilder.setStyle(bigTextStyle);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.vamos_checar_pedido_enviado));
                                }
                            });
                        }

                        if (progressBarStatus != null) {

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
                                if (objeto.hasProperty("codigoRetorno")) {

                                    // Checa se o retorno foi o numero 101 (atualizado com sucesso)
                                    if (Integer.parseInt(objeto.getProperty("codigoRetorno").toString()) == 101) {

                                        inserirUltimaAtualizacao("AEAITORC_ENVIAR");
                                        inserirUltimaAtualizacao("AEAORCAM_ENVIAR");

                                        // Cria uma vareavel para salvar o status do pedido
                                        ContentValues dadosPedido = new ContentValues();
                                        dadosPedido.put("STATUS", "RB");

                                        ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                        if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAORCAM = " + orcamento.getIdOrcamento()) > 0) {

                                            OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                            orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());

                                            bigTextStyle.bigText(context.getResources().getString(R.string.marcando_pedido_enviado_sucesso));
                                            mBuilder.setStyle(bigTextStyle);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_ENVIAR_DADOS, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
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
        } catch (Exception e) {
            // Cria uma notificacao para ser manipulado
            /*Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();*/

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.enviar_dados_nuvem))
                    .bigText(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void inserirUltimaAtualizacao(String tabela) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String dataInicioAtualizacao = sdf.format(calendario.getTime());

        Calendar dtAlt = Calendar.getInstance();

        ContentValues dataAtualizacao = new ContentValues();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            dataAtualizacao.put("ID_DISPOSITIVO", telephonyManager.getDeviceId());
        }
        dataAtualizacao.put("TABELA", tabela);
        dataAtualizacao.put("DT_ALT", sdf.format(dtAlt.getTime()));
        dataAtualizacao.put("DATA_ULTIMA_ATUALIZACAO", dataInicioAtualizacao);

        UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);

        ultimaAtualizacaoSql.insertOrReplace(dataAtualizacao);
    }
}
