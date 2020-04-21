package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.OrcamentoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;


/**
 * Created by Bruno on 10/10/2016.
 */

public class ChecaDadosEnviadosWebserviceAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String[] tabelaEnviarDados = null;
    private ProgressBar progressBarStatus = null;
    private TextView textStatus = null;
    private String[] idOrcamentoSelecionado = null;
    private List<String> idOrcamentoFaltaEnviar = null;

    public ChecaDadosEnviadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    public void setIdOrcamentoSelecionado(String[] idOrcamentoSelecionado) { this.idOrcamentoSelecionado = idOrcamentoSelecionado; }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

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
                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setVisibility(View.VISIBLE);
                            textStatus.setText(context.getResources().getText(R.string.vamos_checar_pedido_enviado));
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
                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()){

                    checaPedidoEnviado();
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
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressBarStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressBarStatus.setVisibility(View.INVISIBLE);
                    progressBarStatus.setIndeterminate(true);
                }
            });
        }

        if (idOrcamentoFaltaEnviar.size() > 0){
            EnviarDadosWebserviceAsyncRotinas enviarDadosWebserviceAsyncRotinas = new EnviarDadosWebserviceAsyncRotinas(context);
            enviarDadosWebserviceAsyncRotinas.setIdOrcamentoSelecionado(idOrcamentoFaltaEnviar.toArray(new String[0]));
            enviarDadosWebserviceAsyncRotinas.execute();
        }
    }

    private void checaPedidoEnviado(){
        try{
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
                listaTipo = new String[]{OrcamentoRotinas.PEDIDO_NAO_ENVIADO,
                                         OrcamentoRotinas.PEDIDO_ENVIADO,
                                         OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                                         OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO,
                                         OrcamentoRotinas.PEDIDO_RETORNADO_EXCLUIDO,
                                         OrcamentoRotinas.PEDIDO_FATURADO};
            } else {
                listaTipo = new String[]{OrcamentoRotinas.PEDIDO_ENVIADO};
            }
            List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();

            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

            // Busca todos os pedidos ja enviados
            listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, where, OrcamentoRotinas.ORDEM_CRESCENTE);

            // Checa se retornou alguma coisa
            if ((listaOrcamento != null) && (listaOrcamento.size() > 0)){

                if (progressBarStatus != null) {
                    final List<OrcamentoBeans> finalListaOrcamento = listaOrcamento;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //progressBarStatus.setVisibility(View.VISIBLE);
                            progressBarStatus.setIndeterminate(false);
                            progressBarStatus.setMax(finalListaOrcamento.size());
                        }
                    });
                }

                final int controle = 0;

                idOrcamentoFaltaEnviar = new ArrayList<String>();

                // Passa por todos os registros
                for (final OrcamentoBeans orcamento : listaOrcamento) {
                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setVisibility(View.VISIBLE);
                                textStatus.setText(context.getResources().getText(R.string.estamos_checando_pedido_numero) + " "+ orcamento.getIdOrcamento());
                            }
                        });
                    }
                    if (progressBarStatus != null) {
                        final List<OrcamentoBeans> finalListaOrcamento = listaOrcamento;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                //progressBarStatus.setVisibility(View.VISIBLE);
                                progressBarStatus.setProgress(controle);
                            }
                        });
                    }

                    PropertyInfo propertyGuidOrcam = new PropertyInfo();
                    propertyGuidOrcam.setName("guid");
                    propertyGuidOrcam.setValue(orcamento.getGuid());
                    propertyGuidOrcam.setType(orcamento.getGuid().getClass());

                    // Cria uma lista para salvar todas as propriedades
                    List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                    listaPropertyInfos.add(propertyGuidOrcam);

                    PropertyInfo propertyTotalOrcam = new PropertyInfo();
                    propertyTotalOrcam.setName("totalOrcamento");
                    propertyTotalOrcam.setValue(""+orcamento.getTotalOrcamento());
                    propertyTotalOrcam.setType(String.class);

                    listaPropertyInfos.add(propertyTotalOrcam);

                    List<String> listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoApenasID(null, ""+orcamento.getIdOrcamento());

                    if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)){
                        PropertyInfo propertyQuantidadeItemOrcam = new PropertyInfo();
                        propertyQuantidadeItemOrcam.setName("quantidadeItemOrcamento");
                        propertyQuantidadeItemOrcam.setValue(listaItemOrcamento.size());
                        propertyQuantidadeItemOrcam.setType(Integer.class);

                        listaPropertyInfos.add(propertyQuantidadeItemOrcam);
                    }

                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                    final Vector<SoapObject> listaRetornoWeb = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_CHECK_SEND_AEAORCAM, listaPropertyInfos);

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
                        // Passa por toda a lista
                        for (SoapObject objetoIndividual : listaRetornoWeb) {
                            final int finalControle = controle;

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null){
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados));
                                    }
                                });
                            }

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
                                    // Cria uma vareavel para salvar o status do pedido
                                    ContentValues dadosPedido = new ContentValues();

                                    String numero = null;

                                    if (objeto.hasProperty("extra")){

                                        String extra = objeto.getProperty("extra").toString();

                                        Scanner in = new Scanner(extra).useDelimiter("\\|");

                                        double valotTotal = Double.parseDouble(in.next());
                                        String situacao = in.next();
                                        numero = in.next();

                                        if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("1")){
                                            // Marca o status como retorno liberado
                                            dadosPedido.put("STATUS", "RL");

                                        } else if (situacao.equalsIgnoreCase("6")){
                                            // Marca o status como retorno como excluido ou bloqueado
                                            dadosPedido.put("STATUS", "RB");

                                        } else if (situacao.equalsIgnoreCase("5")){
                                            // Marca o status como retorno como excluido ou bloqueado
                                            dadosPedido.put("STATUS", "RE");

                                        } else if (situacao.equalsIgnoreCase("3") || situacao.equalsIgnoreCase("4")){
                                            // Marca o status como retorno como faturado
                                            dadosPedido.put("STATUS", "F");

                                        } else {
                                            // Marca o status como retorno como pedido bloqueado
                                            dadosPedido.put("STATUS", "RB");
                                        }

                                    }else {
                                        dadosPedido.put("STATUS", "RB");
                                    }

                                    ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                                    if (itemOrcamentoSql.update(dadosPedido, "AEAITORC.ID_AEAORCAM = " + orcamento.getIdOrcamento()) > 0){
                                        OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                        // Adiciona o numero do orcamento retornado do banco de dados
                                        dadosPedido.put("NUMERO", numero);

                                        orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + orcamento.getIdOrcamento());
                                    }
                                } else {
                                    idOrcamentoFaltaEnviar.add(""+orcamento.getIdOrcamento());
                                }
                            }
                        } // Fim do for SoapObject
                    }
                } // Fim do for orcamento
            }
        } catch (Exception e){
            // Cria uma notificacao para ser manipulado
            /*Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();*/

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                    .setContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage()))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            String name = "FileNotification";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel mChannel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_MIN);
                mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
                mChannel.enableLights(true);
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO, mBuilder.build());
        }
    }
}
