package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.savare.R;
import com.savare.banco.funcoesSql.AreasSql;
import com.savare.banco.funcoesSql.CartaoSql;
import com.savare.banco.funcoesSql.CidadeSql;
import com.savare.banco.funcoesSql.ClasseSql;
import com.savare.banco.funcoesSql.CobrancaSql;
import com.savare.banco.funcoesSql.CotacaoSql;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.EnderecoSql;
import com.savare.banco.funcoesSql.EstadoSql;
import com.savare.banco.funcoesSql.EstoqueSql;
import com.savare.banco.funcoesSql.FatorSql;
import com.savare.banco.funcoesSql.FotosSql;
import com.savare.banco.funcoesSql.GradeSql;
import com.savare.banco.funcoesSql.ItemExcaoPromocaoSql;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.ItemPromocaoSql;
import com.savare.banco.funcoesSql.LancamentoParcelaSql;
import com.savare.banco.funcoesSql.LocacaoSql;
import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.ParametrosLocalSql;
import com.savare.banco.funcoesSql.ParametrosSql;
import com.savare.banco.funcoesSql.ParcelaSql;
import com.savare.banco.funcoesSql.PercentualSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.PlanoPagamentoSql;
import com.savare.banco.funcoesSql.PortadorBancoSql;
import com.savare.banco.funcoesSql.PrecoSql;
import com.savare.banco.funcoesSql.ProdutoLojaSql;
import com.savare.banco.funcoesSql.ProdutoRecomendadoSql;
import com.savare.banco.funcoesSql.ProdutoSql;
import com.savare.banco.funcoesSql.ProfissaoSql;
import com.savare.banco.funcoesSql.RamoAtividadeSql;
import com.savare.banco.funcoesSql.SituacaoTributariaSql;
import com.savare.banco.funcoesSql.StatusSql;
import com.savare.banco.funcoesSql.TabelaItemPromocaoSql;
import com.savare.banco.funcoesSql.TabelaPromocaoSql;
import com.savare.banco.funcoesSql.TipoClienteSql;
import com.savare.banco.funcoesSql.TipoDocumentoSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.ServidoresBeans;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.configuracao.ServicosWeb;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ServidoresRotinas;
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;


/**
 * Created by Bruno Nogueira Silva on 29/06/2016.
 */
public class ReceberDadosWebserviceAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String[] tabelaRecebeDados = null;
    private ProgressBar progressBarStatus = null;
    private TextView textStatus = null;
    private TextView textStatusErro = null;
    private OnTaskCompleted listenerTaskCompleted;
    private Calendar calendario;
    private List<String> listaGuidOrcamento = null;
    private ServidoresBeans servidorAtivo;
    // Cria uma notificacao para ser manipulado
    NotificationManager notificationManager;
    NotificationCompat.BigTextStyle bigTextStyle;
    NotificationCompat.Builder mBuilder;

    public ReceberDadosWebserviceAsyncRotinas(Context context) {
        this.context = context;
    }

    public ReceberDadosWebserviceAsyncRotinas(OnTaskCompleted listenerTaskCompleted, Context context) {
        this.context = context;
        this.listenerTaskCompleted = listenerTaskCompleted;
    }

    public ReceberDadosWebserviceAsyncRotinas(OnTaskCompleted listenerTaskCompleted, Context context, String[] tabelaRecebeDados) {
        this.context = context;
        this.tabelaRecebeDados = tabelaRecebeDados;
        this.listenerTaskCompleted = listenerTaskCompleted;
    }

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }

    public String[] getTabelaRecebeDados() {
        return tabelaRecebeDados;
    }

    public void setTabelaRecebeDados(String[] tabelaRecebeDados) {
        this.tabelaRecebeDados = tabelaRecebeDados;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Inicializa a data
        calendario = Calendar.getInstance();
        String name = "FileDownload";

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel mChannel = new NotificationChannel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL, name, NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Create a BigTextStyle object.
        bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_estamos_pegando_lista_servidores));
        //bigTextStyle.setBigContentTitle("Happy Christmas Detail Info.");

        mBuilder = new NotificationCompat.Builder(context, ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                //.setContentText(mActivity.getResources().getString(R.string.app_name))
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setSound(null, 0)
                .setVibrate(new long[0])
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getString(R.string.aguarde_estamos_pegando_lista_servidores));
                }
            });
        }
        if (textStatusErro != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatusErro.setText("");
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
    }

    @Override
    protected Void doInBackground(Void... params) {

        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml(funcoes.TAG_RECEBENDO_DADOS, "S");
        try {
            ServidoresRotinas servidoresRotinas = new ServidoresRotinas(context);

            List<ServidoresBeans> listaServidores = servidoresRotinas.listaServidores(null, "ID_SERVIDORES ASC", null);
            // Verifica se retornou alguma lista de servidores
            if ( (listaServidores!= null) && (listaServidores.size() > 0)){


                // Passa por todos os servidores para verficar qual eh o primeiro que esta online
                for (final ServidoresBeans servidor : listaServidores) {
                    bigTextStyle.bigText(context.getResources().getString(R.string.estamos_checando_webservice_online) + " - " + servidor.getNomeServidor());
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

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
                        if (textStatusErro != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +context.getResources().getText(R.string.servidor_webservice_offline) + " - " + servidor.getNomeServidor());
                                }
                            });
                        }
                    }
                } // Fim for listaServidores
                // Verifica se tem algum servidor ativo
                if (servidorAtivo == null){
                    new MaterialDialog.Builder(context)
                            .title("ReceberDadosWebserviceAsyncRotinas")
                            .content(context.getResources().getString(R.string.aparentemente_servidor_webservice_offline))
                            .positiveText(R.string.button_ok)
                            .show();
                    return null;
                } else {
                    if (textStatusErro != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatusErro.append("\n*" +context.getResources().getText(R.string.estamos_conectanto_servidor_webservice) + " - " + servidorAtivo.getNomeServidor());
                            }
                        });
                    }
                }
                bigTextStyle.bigText(context.getResources().getString(R.string.checando_versao_savare));
                mBuilder.setStyle(bigTextStyle);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setVisibility(View.VISIBLE);
                            textStatus.setText(context.getResources().getText(R.string.servidor_online));
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
                // Verifica se eh a primeira vez que esta abrindo o app
                if ((funcoes.getValorXml(funcoes.TAG_ABRIU_PRIMEIRA_VEZ).equalsIgnoreCase("S")) ||
                        (funcoes.getValorXml(funcoes.TAG_ABRIU_PRIMEIRA_VEZ).equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                    // Checa se tem internet
                    if (funcoes.existeConexaoInternet()){
                        // Cadastra o dispositivo no Webservice central (admin)
                        if (cadastrarDispositivoWebserviceCentral()){
                            // Cadastro o dispositivo no Webservice local da empresa
                            cadastrarDispositivo();
                        } else {
                            return null;
                        }
                    } else {
                        if (textStatusErro != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +
                                            context.getResources().getText(R.string.nao_existe_conexao_internet) + "\n" +
                                            context.getResources().getText(R.string.para_cadastrar_dispositivo_precisa_internet));
                                }
                            });
                        }
                        return null;
                    }
                } else {
                    // Checa se a versao do savere eh compativel com o webservice
                    if ((checarEmpresaAdmin()) && (checaFuncionarioAtivo())) {

                        importaDadosEmpresa();

                        if (funcoes.checaVersao()) {
                            if (textStatusErro != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatusErro.append("\n*" +context.getResources().getText(R.string.versao_savare_valida));
                                        textStatusErro.append("\n*" +context.getResources().getText(R.string.recebendo_dados));
                                    }
                                });
                            }
                            // Recebe os dados da tabela
                            /*if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))) ||
                                    (tabelaRecebeDados == null)) {

                                if (checaFuncionarioAtivo() == false) {
                                    return null;
                                }
                            }*/
                            // Recebe os dados da tabela CFAAREAS
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS))) ||
                                    (tabelaRecebeDados == null)) {

                                importarDadosArea();
                            }

                            // Recebe os dados da tabela CFAATIVI
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados da empresa
                                importarDadosAtividade();
                            }

                            // Recebe os dados da tabela CFASTATU
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosStatus();
                            }


                            // Recebe os dados da tabela CFATPDOC
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosTipoDocumento();
                            }

                            // Recebe os dados da tabela CFACCRED
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosCartaoCredito();
                            }

                            // Recebe os dados da tabela CFAPORTA
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosPortador();
                            }

                            // Recebe os dados da tabela CFAPROFI
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosProfissao();
                            }

                            // Recebe os dados da tabela CFATPCLI
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosTipoCliente();
                            }

                            // Recebe os dados da tabela CFATPCOB
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosTipoCobranca();
                            }

                            // Recebe os dados da tabela CFAESTAD
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAESTAD))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosEstado();
                            }

                            // Recebe os dados da tabela CFACIDAD
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACIDAD))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosCidade();
                            }

                            // Recebe os dados da tabela CFACLIFO
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosClifo();
                                importarDadosRemoveClifo();
                            }

                            // Recebe os dados da tabela CFACOTAC
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && ((Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACOTAC)) ||
                                    (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACOTAC)))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosCotacao();
                            }

                            // Recebe os dados da tabela CFAENDER
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && ((Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER)) ||
                                    (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM)))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosEndereco();
                            }

                            // Recebe os dados da tabela CFAPARAM
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosParametros();
                            }

                            // Recebe os dados da tabela CFAFOTOS
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosFotos();
                            }

                            // Recebe os dados da tabela AEAPLPGT
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosPlanoPagamento();
                            }

                            // Recebe os dados da tabela AEACLASE
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosClasseProdutos();
                            }

                            // Recebe os dados da tabela AEAUNVEN
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosUnidadeVenda();
                            }

                            // Recebe os dados da tabela AEAUNVEN
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosGrade();
                            }

                            // Recebe os dados da tabela AEAMARCA
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosMarca();
                            }

                            // Recebe os dados da tabela AEACODST
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosCodigoSituacaoTributaria();
                            }

                            // Recebe os dados da tabela AEAPRODU
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosProduto();
                            }

                            // Recebe os dados da tabela AEAPRECO
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosPreco();
                            }

                            // Recebe os dados da tabela AEAEMBAL
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosEmbalagem();
                            }

                            // Recebe os dados da tabela AEAPLOJA
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosProdutosPorLoja();
                            }

                            // Recebe os dados da tabela AEALOCES
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosLocalEstoque();
                            }

                            // Recebe os dados da tabela AEAESTOQ
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosEstoque();
                            }

                            // Recebe os dados da tabela AEAORCAM
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosOrcamento();
                            }

                            // Recebe os dados da tabela AEAITORC
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAITORC))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                //importarDadosItemOrcamento();
                            }

                            // Recebe os dados da tabela AEAPERCE
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosPercentual();
                            }

                            // Recebe os dados da tabela AEAFATOR
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosFator();
                            }

                            // Recebe os dados da tabela AEAPRREC
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosProdutoRecomendado();
                            }

                            // Recebe os dados da tabela AEATBPRO
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEATBPRO))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosTabelaPromocao();
                            }

                            // Recebe os dados da tabela AEAITTBP
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAITTBP))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosItemPromocao();
                            }

                            // Recebe os dados da tabela AEAEXTBP
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEXTBP))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosExcaoPromocao();
                            }

                            // Recebe os dados da tabela AEAEMTBP
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMTBP))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosTabelaItemPromocao();
                            }

                            // Recebe os dados da tabela RPALCPAR
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPALCPAR))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosLancamentoParcela();
                            }

                            // Recebe os dados da tabela RPAPARCE
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE))) ||
                                    (tabelaRecebeDados == null)) {

                                // Importa os dados
                                importarDadosParcela();
                            }

                            // Verifica se tem uma nova atualizacao e ja faz download pra atualizar
                            if ((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAVERPR)) ) {

                                // Importa os dados
                                importarNovaVersao();
                            }
                        } else {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    new MaterialDialog.Builder(context)
                                            .title("ReceberDadosWebserviceAsyncRotinas")
                                            .content(context.getResources().getString(R.string.nao_conseguimos_validar_versao))
                                            .positiveText(R.string.button_ok)
                                            .show();
                                }
                            });
                            if (textStatusErro != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatusErro.append("\n*" +context.getResources().getText(R.string.nao_conseguimos_validar_versao));
                                    }
                                });
                            }
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    new MaterialDialog.Builder(context)
                                            .title("ReceberDadosWebserviceAsyncRotinas")
                                            .content(context.getResources().getString(R.string.nao_conseguimos_validar_versao))
                                            .positiveText(R.string.button_ok)
                                            .show();
                                }
                            });
                        }
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(context)
                                        .title("ReceberDadosWebserviceAsyncRotinas")
                                        .content(context.getResources().getString(R.string.empresa_usuario_inativo))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                        if (textStatusErro != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +context.getResources().getText(R.string.empresa_usuario_inativo));
                                }
                            });
                        }
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(context)
                                        .title("ReceberDadosWebserviceAsyncRotinas")
                                        .content(context.getResources().getString(R.string.empresa_usuario_inativo))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                    }
                }
            } else {
                // Atualiza a notificacao
                bigTextStyle.bigText(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados));
                        }
                    });
                }
                if (textStatusErro != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +context.getResources().getString(R.string.nao_achamos_servidores_cadastrados));
                        }
                    });
                }
                if (progressBarStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("ReceberDadosWebserviceAsyncRotinas")
                                .content(context.getResources().getString(R.string.nao_achamos_servidores_cadastrados))
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
            }
        } catch (final Exception e) {
            // Atualiza a notificacao
            bigTextStyle.bigText(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.msg_error) + e.getMessage());
                    }
                });
            }
            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.append("\n" +context.getResources().getString(R.string.msg_error) + e.getMessage());
                    }
                });
            }
            if (progressBarStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressBarStatus.setVisibility(View.INVISIBLE);
                    }
                });
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("ReceberDadosWebserviceAsyncRotinas")
                            .content(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
            return null;
        }
        return null;
    } // Fim Background


    @Override
    protected void onPostExecute(Void params) {
        super.onPostExecute(params);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        // Marca que a aplicacao esta recebendo dados
        funcoes.setValorXml("RecebendoDados", "N");

        // Checa se a interface de retorno do asynctask eh diferente de nula
        if (listenerTaskCompleted != null) {
            listenerTaskCompleted.onTaskCompleted();
        }

        bigTextStyle.bigText(context.getResources().getString(R.string.terminamos_atualizacao))
                .setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos));
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, false);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.terminamos));
                }
            });
        }
        // Checo se o texto de status foi passado pro parametro
        if (textStatusErro != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatusErro.append("\n*" +context.getResources().getString(R.string.terminamos));
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
    }

    private void salvarDadosXml(ContentValues usuario) {
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        ContentValues parametros = new ContentValues();

        if (usuario.containsKey("CODIGO_FUN")) {
            funcoes.setValorXml(funcoes.TAG_CODIGO_USUARIO, usuario.getAsString("CODIGO_FUN"));
            parametros.put("NOME_PARAM", funcoes.TAG_CODIGO_USUARIO);
            parametros.put("VALOR_PARAM", funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO));
        }
        if (usuario.containsKey("ID_SMAEMPRE")) {
            funcoes.setValorXml(funcoes.TAG_CODIGO_EMPRESA, usuario.getAsString("ID_SMAEMPRE"));
            parametros.put("NOME_PARAM", funcoes.TAG_CODIGO_EMPRESA);
            parametros.put("VALOR_PARAM", funcoes.getValorXml(funcoes.TAG_CODIGO_EMPRESA));
        }
        if (usuario.containsKey("GUID")) {
            funcoes.setValorXml(funcoes.TAG_CHAVE_FUNCIONARIO, usuario.getAsString("GUID"));
            parametros.put("NOME_PARAM", funcoes.TAG_CHAVE_FUNCIONARIO);
            parametros.put("VALOR_PARAM", funcoes.getValorXml(funcoes.TAG_CHAVE_FUNCIONARIO));
        }
        if (usuario.containsKey("EMAIL")) {
            funcoes.setValorXml("Email", usuario.getAsString("EMAIL"));
        }
        if (funcoes.getValorXml("EnviarAutomatico").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
            funcoes.setValorXml("EnviarAutomatico", "N");
        }
        if (funcoes.getValorXml("ReceberAutomatico").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
            funcoes.setValorXml("ReceberAutomatico", "N");
        }
        if (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
            funcoes.setValorXml("ImagemProduto", "N");
        }
        if (funcoes.getValorXml("AbriuAppPriveiraVez").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
            funcoes.setValorXml("AbriuAppPriveiraVez", "S");
        }
        // Adiciona os parametros no banco de dados
        ParametrosLocalSql parametrosLocalSql = new ParametrosLocalSql(context);
        parametrosLocalSql.insert(parametros);
    }

    public void setProgressBarStatus(ProgressBar progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    public void setTextStatus(TextView textStatus) {
        this.textStatus = textStatus;
    }

    public void setTextStatusErro(TextView textStatusErro) {
        this.textStatusErro = textStatusErro;
    }

    public List<String> getListaGuidOrcamento() {
        return listaGuidOrcamento;
    }

    public void setListaGuidOrcamento(List<String> listaGuidOrcamento) {
        this.listaGuidOrcamento = listaGuidOrcamento;
    }


    private boolean checaFuncionarioAtivo() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Funcionário");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Funcionário");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String ultimaData = null, uuidDispositivo = null;

            // Cria uma variavel para salvar todos os paramentros para ser passado na url
            String parametrosWebservice = "";
            uuidDispositivo = funcoes.getValorXml("UuidDispositivo");

            if ((uuidDispositivo != null) && (!uuidDispositivo.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                // Pega quando foi a ultima data que recebeu dados
                ultimaData = pegaUltimaDataAtualizacao("CFACLIFO_FUN");

                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (ID_CFACLIFO = (SELECT SMADISPO.ID_CFACLIFO_FUNC FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + uuidDispositivo + "') )";
                } else {
                    parametrosWebservice += "&where= (ID_CFACLIFO = (SELECT SMADISPO.ID_CFACLIFO_FUNC FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + uuidDispositivo + "') )";
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                        // Atualiza a notificacao
                        bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mBuilder.setStyle(bigTextStyle);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                }
                            });
                        }
                        // Checa se retornou alguma coisa
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                            final JsonArray listaUsuarioRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                            boolean todosSucesso = true;

                            // Atualiza a notificacao
                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_usuario));
                            mBuilder.setStyle(bigTextStyle);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_usuario));
                                    }
                                });
                            }
                            if (progressBarStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressBarStatus.setIndeterminate(true);
                                        //progressBarStatus.setMax(listaUsuarioRetorno.size());
                                    }
                                });
                            }

                            List<ContentValues> listaDadosUsuario = new ArrayList<ContentValues>();
                            for (int i = 0; i < listaUsuarioRetorno.size(); i++) {

                                final JsonObject usuarioRetorno = listaUsuarioRetorno.get(i).getAsJsonObject();

                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Funcionário: " + usuarioRetorno.get("nomeRazao").toString());
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Funcionário: " + usuarioRetorno.get("nomeRazao").toString());
                                        }
                                    });
                                }
                                if (textStatusErro != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatusErro.append("\n*" +
                                                    context.getResources().getString(R.string.recebendo_dados_usuario) +
                                                    " - Funcionário: " + usuarioRetorno.get("nomeRazao").toString());
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
                                final ContentValues dadosUsuario = new ContentValues();
                                dadosUsuario.put("ID_CFACLIFO", usuarioRetorno.get("idCfaclifo").getAsInt());
                                dadosUsuario.put("ID_CFAPROFI", (usuarioRetorno.has("idCfaprofi")) ? usuarioRetorno.get("idCfaprofi").getAsInt() : null);
                                dadosUsuario.put("ID_CFAATIVI", (usuarioRetorno.has("idCfaativi")) ? usuarioRetorno.get("idCfaativi").getAsInt() : null);
                                dadosUsuario.put("ID_CFAAREAS", (usuarioRetorno.has("idCfaareas")) ? usuarioRetorno.get("idCfaareas").getAsInt() : null);
                                dadosUsuario.put("ID_CFATPCLI", (usuarioRetorno.has("idCfatpcli")) ? usuarioRetorno.get("idCfatpcli").getAsInt() : null);
                                dadosUsuario.put("ID_CFASTATU", (usuarioRetorno.has("idCfastatu")) ? usuarioRetorno.get("idCfastatu").getAsInt() : null);
                                dadosUsuario.put("ID_SMAEMPRE", (usuarioRetorno.has("idSmaempre")) ? usuarioRetorno.get("idSmaempre").getAsInt() : null);
                                dadosUsuario.put("DT_ALT", (usuarioRetorno.has("dtAlt")) ? usuarioRetorno.get("dtAlt").getAsString() : null);
                                dadosUsuario.put("GUID", usuarioRetorno.get("guid").getAsString());
                                dadosUsuario.put("CPF_CNPJ", (usuarioRetorno.has("cpfCgc")) ? usuarioRetorno.get("cpfCgc").getAsString() : null);
                                dadosUsuario.put("IE_RG", (usuarioRetorno.has("ieRg")) ? usuarioRetorno.get("ieRg").getAsString() : null);
                                dadosUsuario.put("NOME_RAZAO", (usuarioRetorno.has("nomeRazao")) ? usuarioRetorno.get("nomeRazao").getAsString() : null);
                                dadosUsuario.put("NOME_FANTASIA", (usuarioRetorno.has("nomeFantasia")) ? usuarioRetorno.get("nomeFantasia").getAsString() : "");
                                dadosUsuario.put("DT_NASCIMENTO", (usuarioRetorno.has("dtNascimento")) ? usuarioRetorno.get("dtNascimento").getAsString() : "");
                                dadosUsuario.put("CODIGO_CLI", (usuarioRetorno.has("codigoCli")) ? usuarioRetorno.get("codigoCli").getAsInt() : null);
                                dadosUsuario.put("CODIGO_FUN", (usuarioRetorno.has("codigoFun")) ? usuarioRetorno.get("codigoFun").getAsInt() : null);
                                dadosUsuario.put("CODIGO_USU", (usuarioRetorno.has("codigoUsu")) ? usuarioRetorno.get("codigoUsu").getAsInt() : null);
                                dadosUsuario.put("CODIGO_TRA", (usuarioRetorno.has("codigoTra")) ? usuarioRetorno.get("codigoTra").getAsInt() : null);
                                dadosUsuario.put("CLIENTE", (usuarioRetorno.has("cliente")) ? usuarioRetorno.get("cliente").getAsString() : "");
                                dadosUsuario.put("FUNCIONARIO", (usuarioRetorno.has("funcionario")) ? usuarioRetorno.get("funcionario").getAsString() : "");
                                dadosUsuario.put("USUARIO", (usuarioRetorno.has("usuario")) ? usuarioRetorno.get("usuario").getAsString() : "");
                                dadosUsuario.put("TRANSPORTADORA", (usuarioRetorno.has("transportadora")) ? usuarioRetorno.get("transportadora").getAsString() : "");
                                dadosUsuario.put("SEXO", (usuarioRetorno.has("sexo")) ? usuarioRetorno.get("sexo").getAsString() : "");
                                dadosUsuario.put("INSC_JUNTA", (usuarioRetorno.has("inscJunta")) ? usuarioRetorno.get("inscJunta").getAsString() : "");
                                dadosUsuario.put("INSC_SUFRAMA", (usuarioRetorno.has("inscSuframa")) ? usuarioRetorno.get("inscSuframa").getAsString() : "");
                                dadosUsuario.put("INSC_MUNICIPAL", (usuarioRetorno.has("inscMunicipal")) ? usuarioRetorno.get("inscMunicipal").getAsString() : "");
                                dadosUsuario.put("INSC_PRODUTOR", (usuarioRetorno.has("inscProdutor")) ? usuarioRetorno.get("inscProdutor").getAsString() : "");
                                dadosUsuario.put("ATIVO", (usuarioRetorno.has("ativo")) ? usuarioRetorno.get("ativo").getAsString() : null);

                                salvarDadosXml(dadosUsuario);

                                listaDadosUsuario.add(dadosUsuario);

                                // Checa se o usuario esta ativo
                                if (usuarioRetorno.get("ativo").toString().equalsIgnoreCase("0")) {

                                    final ContentValues contentValues = new ContentValues();
                                    contentValues.put("comando", 0);
                                    contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                    contentValues.put("mensagem", "O funcionário dessa chave esta inativo, não podemos baixar os dados dele. Entre em contato com a empresa ou com o suporte SAVARE.");
                                    contentValues.put("dados", "");


                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            funcoes.menssagem(contentValues);
                                        }
                                    });
                                    return false;
                                }
                            } // Fim for listaUsuarioRetorno

                            final PessoaSql pessoaSql = new PessoaSql(context);

                            todosSucesso = pessoaSql.insertList(listaDadosUsuario);

                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("CFACLIFO_FUN");
                            } else {
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                                        .bigText(context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario));
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                                // Checa se o texto de status foi passado pro parametro
                                if (textStatus != null) {

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario));
                                        }
                                    });
                                }

                                if (textStatusErro != null) {
                                    final JsonObject finalStatuRetorno = statuRetorno;
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatusErro.append("\n*" +
                                                    context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario));
                                        }
                                    });
                                }
                            }

                            // Atualiza a notificacao
                            bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                            mBuilder.setStyle(bigTextStyle)
                                    .setProgress(0, 0, true);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                        }
                    } else {
                        bigTextStyle.bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_usuario));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                        if (textStatusErro != null) {
                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +
                                            context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + " \n" +
                                            finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + " \n" +
                                            finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                }
                            });
                        }
                    }
                } else {
                    bigTextStyle.bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados));
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                    if (textStatusErro != null) {
                        final JsonObject finalStatuRetorno = statuRetorno;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatusErro.append("\n*" +
                                        context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                            }
                        });
                    }
                }
            } else {
                bigTextStyle.bigText(context.getResources().getString(R.string.nao_achamos_chave_dispositivo))
                        .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                if (textStatusErro != null) {
                    final JsonObject finalStatuRetorno = statuRetorno;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +
                                    context.getResources().getString(R.string.nao_achamos_chave_dispositivo));
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Cria uma notificacao
            /*PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEmpresa- " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();*/

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                        .bigText("ImportaDadosEmpresa- " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
        return true;
    }

    private void cadastrarDispositivo() {
        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Vamos cadastrar o Dispositivo");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Vamos cadastrar o Dispositivo");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String cnpjEmpresa = funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA);
            String parametrosWebservice = "";
            JsonObject statuRetorno = null;

            if ((cnpjEmpresa != null) && (!cnpjEmpresa.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                String abriuAppPrimeiraVez = funcoes.getValorXml("AbriuAppPriveiraVez");

                if (((abriuAppPrimeiraVez != null) && (abriuAppPrimeiraVez.equalsIgnoreCase("S"))) || (abriuAppPrimeiraVez.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {

                    parametrosWebservice += "&cnpjUrl=" + cnpjEmpresa;
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                    JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMADISPO, WSSisinfoWebservice.METODO_POST, null, parametrosWebservice), JsonObject.class);

                    if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                        statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                        // Verifica se retornou com sucesso
                        if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                            funcoes.setValorXml("AbriuAppPriveiraVez", "N");

                            // Atualiza a notificacao
                            bigTextStyle.bigText(context.getResources().getString(R.string.dispositivo_registrado));
                            mBuilder.setStyle(bigTextStyle);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.dispositivo_registrado));
                                    }
                                });
                            }
                            if (textStatusErro != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatusErro.append("\n*" +context.getResources().getString(R.string.dispositivo_registrado));
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

                        } else {
                            // Cria uma notificacao para ser manipulado
                            /*Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.recebendo_dados_usuario)
                                    .bigTextStyle(context.getResources().getString(
                                            R.string.nao_chegou_dados_servidor_empresa) + "\n" +
                                            "Codigo: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n"+
                                            "Mensagem: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO))
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();*/

                            bigTextStyle.bigText(context.getResources().getString(
                                    R.string.nao_chegou_dados_servidor_empresa) + "\n" +
                                    "Codigo: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n"+
                                    "Mensagem: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO))
                                    .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_usuario));
                            mBuilder.setStyle(bigTextStyle)
                                    .setProgress(0, 0, false);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                            if (textStatusErro != null) {
                                final JsonObject finalStatuRetorno = statuRetorno;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatusErro.append("\n*" +
                                                context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                                "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n"+
                                                "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                    }
                                });
                            }
                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    new MaterialDialog.Builder(context)
                                            .title("ReceberDadosWebserviceAsyncRotinas")
                                            .content(context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                                    "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) +
                                                    "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO))
                                            .positiveText(R.string.button_ok)
                                            .show();
                                }
                            });
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        bigTextStyle.bigText(context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                    "Codigo: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n" +
                                    "Mensagem: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO))
                                    .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                        if (textStatus != null) {
                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                            "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) +
                                            "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                }
                            });
                        }

                    }
                }
            } else {
                // Cria uma notificacao para ser manipulado
                bigTextStyle.bigText(context.getResources().getString(R.string.nao_achamos_cnpj))
                            .setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_usuario));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                if (textStatusErro != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +context.getResources().getString(R.string.nao_achamos_cnpj) );
                        }
                    });
                }

                //SuperToast.create(context, context.getResources().getString(R.string.nao_achamos_cnpj), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                SuperActivityToast.create(context, context.getResources().getString(R.string.nao_achamos_cnpj), Style.DURATION_LONG)
                        .setTextColor(Color.WHITE)
                        .setColor(Color.RED)
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();
            }

        } catch (final Exception e) {
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                        .bigText("CadastrarDispositivo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText(context.getResources().getString(R.string.msg_error) + e.getMessage());
                    }
                });
            }

            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append(context.getResources().getString(R.string.msg_error) + e.getMessage());
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
        }
    }

    /**
     * Cadastra o dispositivo no Webservice central {@link com.savare.configuracao.ServicosWeb}.IP_SERVIDOR_WEBSERVICE.
     * E o Webservice central retorna se pode ou nao cadastrar um novo dispositivo.
     */
    private boolean cadastrarDispositivoWebserviceCentral() {
        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Vamos cadastrar o Dispositivo no Webservice Central");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Vamos cadastrar o Dispositivo no Webservice Central");
                }
            });
        }
        if (textStatusErro != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatusErro.append("\n*" + "Vamos cadastrar o Dispositivo no Webservice Central");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String cnpjEmpresa = funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA);
            String parametrosWebservice = "";
            JsonObject statuRetorno = null;

            if ((cnpjEmpresa != null) && (!cnpjEmpresa.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                //String abriuAppPrimeiraVez = funcoes.getValorXml("AbriuAppPriveiraVez");

                parametrosWebservice += "&cnpjUrl=" + cnpjEmpresa;
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                ServidoresBeans servidorCentral = new ServidoresBeans();
                servidorCentral.setIpServidor(ServicosWeb.IP_SERVIDOR_WEBSERVICE);
                servidorCentral.setPorta(Integer.parseInt(ServicosWeb.PORTA_JSON));

                JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorCentral, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMADISPO, WSSisinfoWebservice.METODO_POST, null, parametrosWebservice), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                        // Atualiza a notificacao
                        bigTextStyle.bigText(context.getResources().getString(R.string.dispositivo_registrado));
                        mBuilder.setStyle(bigTextStyle);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.dispositivo_registrado));
                                }
                            });
                        }
                        if (textStatusErro != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +context.getResources().getString(R.string.dispositivo_registrado));
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
                        return true;
                    } else {
                        // Cria uma notificacao para ser manipulado
                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                    .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" +
                                    "Codigo: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n"+
                                    "Mensagem: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                        if (textStatusErro != null) {
                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +
                                            context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                            "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n"+
                                            "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                }
                            });
                        }
                        final JsonObject finalStatuRetorno = statuRetorno;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(context)
                                        .title("ReceberDadosWebserviceAsyncRotinas")
                                        .content(context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                                "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n" +
                                                "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                    "Codigo: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n" +
                                    "Mensagem: " + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                    if (textStatus != null) {
                        final JsonObject finalStatuRetorno = statuRetorno;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatusErro.append("\n*" +context.getResources().getString(R.string.nao_conseguimos_registrar_dispositivo) + "\n" +
                                        "Codigo: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) +
                                        "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                            }
                        });
                    }

                }
            } else {
                // Cria uma notificacao para ser manipulado
                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                        .bigText(context.getResources().getString(R.string.nao_achamos_cnpj));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.append(context.getResources().getString(R.string.nao_achamos_cnpj) );
                        }
                    });
                }

                if (textStatusErro != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +context.getResources().getString(R.string.nao_achamos_cnpj) );
                        }
                    });
                }

                //SuperToast.create(context, context.getResources().getString(R.string.nao_achamos_cnpj), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
            }

        } catch (final Exception e) {
            // Cria uma notificacao
//            PugNotification.with(context)
//                    .load()
//                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
//                    .title(R.string.msg_error)
//                    .bigTextStyle("CadastrarDispositivo - " + e.getMessage())
//                    .smallIcon(R.mipmap.ic_launcher)
//                    .largeIcon(R.mipmap.ic_launcher)
//                    .flags(Notification.DEFAULT_ALL)
//                    .simple()
//                    .build();

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                        .bigText("CadastrarDispositivo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText(context.getResources().getString(R.string.msg_error) + e.getMessage());
                    }
                });
            }

            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText("\n*" + context.getResources().getString(R.string.msg_error) + e.getMessage());
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
        }
        return false;
    }


    private boolean checarEmpresaAdmin() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Dados de licença da empresa");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Dados de licença da empresa");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String uuidDispositivo = funcoes.getValorXml("UuidDispositivo");
            String ultimaData = null, parametrosWebservice = "";

            if ((uuidDispositivo != null) && (!uuidDispositivo.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                // Pega quando foi a ultima data que recebeu dados
                ultimaData = pegaUltimaDataAtualizacao("CFACLIFO_ADMIN");

                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND ( CFACLIFO.ID_CFACLIFO = ( SELECT SMADISPO.ID_CFACLIFO FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + uuidDispositivo + "') )";
                } else {
                    parametrosWebservice += "&where= ( CFACLIFO.ID_CFACLIFO = ( SELECT SMADISPO.ID_CFACLIFO FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + uuidDispositivo + "') )";
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO_ADMIN, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    // Pega o status que retornou do webservice
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        // Atualiza a notificacao
                        bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mBuilder.setStyle(bigTextStyle);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                }
                            });
                        }
                        // Checa se retornou alguma coisa
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                            final JsonArray listaUsuarioRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

                            if (listaUsuarioRetorno.size() > 0) {
                                boolean todosSucesso = true;

                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_licenca));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_licenca));
                                        }
                                    });
                                }
                                if (progressBarStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setIndeterminate(true);
                                            //progressBarStatus.setMax(listaUsuarioRetorno.size());
                                        }
                                    });
                                }

                                //List<ContentValues> listaDadosUsuario = new ArrayList<ContentValues>();
                                ContentValues dadosServidor = new ContentValues();
                                UsuarioSQL usuarioSQL = new UsuarioSQL(context);

                                for (int i = 0; i < listaUsuarioRetorno.size(); i++) {
                                    final JsonObject usuarioRetorno = listaUsuarioRetorno.get(i).getAsJsonObject();

                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.achamos_alguma_coisa) + " - Empresa: " + usuarioRetorno.get("nomeRazao").toString());
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.achamos_alguma_coisa) + " - Empresa: " + usuarioRetorno.get("nomeRazao").toString());
                                            }
                                        });
                                    }
                                    if (textStatusErro != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatusErro.append("\n*" +context.getResources().getString(R.string.achamos_alguma_coisa) + " - Empresa: " + usuarioRetorno.get("nomeRazao").toString());
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
                                    // Checa se o usuario esta ativo
                                    if (usuarioRetorno.get("ativo").toString().equalsIgnoreCase("0")) {
                                        // Marca que a empresa esta inativa
                                        funcoes.setValorXml("EmpresaAtiva", "0");

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                new MaterialDialog.Builder(context)
                                                        .title("ReceberDadosWebserviceAsyncRotinas")
                                                        .content("Empresa está inativa, não podemos baixar os dados dele. Entre em contato com a sua empresa ou com o suporte SAVARE.")
                                                        .positiveText(R.string.button_ok)
                                                        .show();
                                            }
                                        });
                                        return false;
                                    } else {
                                        funcoes.setValorXml("EmpresaAtiva", "1");
                                    }
                                }
                            }
                            inserirUltimaAtualizacao("CFACLIFO_ADMIN");
                        }
                    } else if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // Marca que a empresa esta inativa
                        funcoes.setValorXml("EmpresaAtiva", "0");

                        // Atualiza a notificacao
                        bigTextStyle.bigText(context.getResources().getString(R.string.nao_autorizado));
                        mBuilder.setStyle(bigTextStyle);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());


                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nao_autorizado));
                                }
                            });
                        }
                        if (textStatusErro != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +"Empresa " + context.getResources().getString(R.string.nao_autorizado));
                                }
                            });
                        }
                        // Verifica se retornou alguma mensagem
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                            final JsonObject mensagem = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(mensagem.get("mensagemRetorno").getAsString());
                                    }
                                });
                            }
                            if (textStatusErro != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatusErro.append("\n*" +mensagem.get("mensagemRetorno").getAsString());
                                    }
                                });
                            }
                            // Cria uma notificacao para ser manipulado
                            /*Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.recebendo_dados_usuario)
                                    .bigTextStyle(mensagem.get("mensagemRetorno").getAsString())
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();*/

                            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_usuario))
                                    .bigText(mensagem.get("mensagemRetorno").getAsString());
                            mBuilder.setStyle(bigTextStyle)
                                    .setProgress(0, 0, false);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                        }
                        return false;
                    } else {
                        // Cria uma notificacao para ser manipulado
                        /*Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.msg_error)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();*/

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa));
                                }
                            });
                        }
                        if (textStatusErro != null) {
                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatusErro.append("\n*" +
                                            context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" +
                                            "Não sebemos se a empresa esta autorizada ou ativa." + "\n" +
                                            finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                }
                            });
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    /*Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.msg_error)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();*/

                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO));
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + " Dados de licença da empresa");
                            }
                        });
                    }
                    if (textStatusErro != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatusErro.append("\n*" +
                                                context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) +
                                                " Servidor webservice não retornou os dados de licença da empresa");
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            // Cria uma notificacao
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosFuncionario- " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
        return true;
    }


    private void importaDadosEmpresa() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Empresa");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Empresa");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("SMAEMPRE");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Verifica se conseguiu pegar o guid do usuario
            if (funcoes.getValorXml(funcoes.TAG_CHAVE_FUNCIONARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)){
                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                        .bigText("ImportarDadosEmpresa - " + context.getResources().getString(R.string.nao_tem_numero_identificacao_funcionario));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, false);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

                if (textStatusErro != null) {
                    final JsonObject finalStatuRetorno = statuRetorno;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +"ImportarDadosEmpresa- " + context.getResources().getString(R.string.nao_tem_numero_identificacao_funcionario));
                        }
                    });
                }
            } else {
                if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml(funcoes.TAG_CHAVE_FUNCIONARIO) + "'))";
                } else {
                    parametrosWebservice += "&where= (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml(funcoes.TAG_CHAVE_FUNCIONARIO) + "'))";
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                //JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        boolean todosSucesso = true;

                        JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                        if ((pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0)) {
                            final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                            int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                            for (int ia = pageNumber; ia < totalPages; ia++) {

                                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                                // Verifica se retornou com sucesso
                                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                            }
                                        });
                                    }
                                    // Checa se retornou alguma coisa
                                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                        final JsonArray listaEmpresaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                        // Checa se retornou algum dados na lista
                                        if (listaEmpresaRetorno.size() > 0) {
                                            final List<ContentValues> listaDadosEmpresa = new ArrayList<ContentValues>();

                                            for (int i = 0; i < listaEmpresaRetorno.size(); i++) {

                                                // Atualiza a notificacao
                                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_empresa) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEmpresaRetorno.size());
                                                mBuilder.setStyle(bigTextStyle)
                                                        .setProgress(listaEmpresaRetorno.size(), i, false);
                                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                                // Checo se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    final int finalI = i;
                                                    final int finalPageNumber = pageNumber + 1;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_empresa) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI + "/" + listaEmpresaRetorno.size());
                                                        }
                                                    });
                                                }
                                                if (progressBarStatus != null) {
                                                    final int finalI1 = i;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            progressBarStatus.setProgress(finalI1);
                                                        }
                                                    });
                                                }
                                                JsonObject empresaRetorno = listaEmpresaRetorno.get(i).getAsJsonObject();
                                                ContentValues dadosEmpresa = new ContentValues();

                                                // Inseri os valores
                                                dadosEmpresa.put("ID_SMAEMPRE", empresaRetorno.get("idSmaempre").getAsInt());
                                                if (empresaRetorno.has("idAeaplpgtVare")) {
                                                    dadosEmpresa.put("ID_AEAPLPGT_VARE", empresaRetorno.get("idAeaplpgtVare").getAsInt());
                                                }
                                                if ((empresaRetorno.has("idAeaplpgtAtac")) && (empresaRetorno.get("idAeaplpgtAtac").getAsInt() > 0)) {
                                                    dadosEmpresa.put("ID_AEAPLPGT_ATAC", empresaRetorno.get("idAeaplpgtAtac").getAsInt());
                                                }
                                                dadosEmpresa.put("ID_AEAPLPGT_VARE_VISTA", (empresaRetorno.has("idAeaplpgtVareVista") ? empresaRetorno.get("idAeaplpgtVareVista").getAsInt() : null));
                                                dadosEmpresa.put("ID_AEAPLPGT_ATAC_VISTA", (empresaRetorno.has("idAeaplpgtAtacVista") ? empresaRetorno.get("idAeaplpgtAtacVista").getAsInt() : null));
                                                if ((empresaRetorno.has("idCfastatu")) && (empresaRetorno.get("idCfastatu").getAsInt() > 0)) {
                                                    dadosEmpresa.put("ID_CFASTATU", empresaRetorno.get("idCfastatu").getAsInt());
                                                }
                                                if ((empresaRetorno.has("idCfaativi")) && (empresaRetorno.get("idCfaativi").getAsInt() > 0)) {
                                                    dadosEmpresa.put("ID_CFAATIVI", empresaRetorno.get("idCfaativi").getAsInt());
                                                }
                                                if ((empresaRetorno.has("guid")) && (empresaRetorno.get("guid").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("GUID", empresaRetorno.get("guid").getAsString());
                                                }
                                                if ((empresaRetorno.has("usCad")) && (empresaRetorno.get("usCad").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("US_CAD", empresaRetorno.get("usCad").getAsString());
                                                }
                                                if ((empresaRetorno.has("codigo")) && (empresaRetorno.get("codigo").getAsInt() > 0)) {
                                                    dadosEmpresa.put("CODIGO", empresaRetorno.get("codigo").getAsInt());
                                                }
                                                dadosEmpresa.put("DT_ALT", empresaRetorno.get("dtAlt").getAsString());
                                                dadosEmpresa.put("NOME_RAZAO", empresaRetorno.get("nomeRazao").getAsString());
                                                dadosEmpresa.put("NOME_FANTASIA", empresaRetorno.has("nomeFantasia") ? empresaRetorno.get("nomeFantasia").getAsString() : "");
                                                dadosEmpresa.put("CPF_CGC", empresaRetorno.has("cpfCgc") ? empresaRetorno.get("cpfCgc").getAsString() : "");
                                                if ((empresaRetorno.has("curvaAFis"))) {
                                                    dadosEmpresa.put("CURVA_A_FIS", empresaRetorno.get("curvaAFis").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("curvaBFis"))) {
                                                    dadosEmpresa.put("CURVA_B_FIS", empresaRetorno.get("curvaBFis").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("curvaCFis"))) {
                                                    dadosEmpresa.put("CURVA_C_FIS", empresaRetorno.get("curvaCFis").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("curvaAFin"))) {
                                                    dadosEmpresa.put("CURVA_A_FIN", empresaRetorno.get("curvaAFin").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("curvaBFin"))) {
                                                    dadosEmpresa.put("CURVA_B_FIN", empresaRetorno.get("curvaBFin").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("curvaCFin"))) {
                                                    dadosEmpresa.put("CURVA_C_FIN", empresaRetorno.get("curvaCFin").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("atacVarejo")) && (empresaRetorno.get("atacVarejo").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("ATAC_VAREJO", empresaRetorno.get("atacVarejo").getAsString());
                                                }
                                                if ((empresaRetorno.has("vendeAtacVare")) && (empresaRetorno.get("vendeAtacVare").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("VENDE_ATAC_VARE", empresaRetorno.get("vendeAtacVare").getAsString());
                                                }
                                                dadosEmpresa.put("ORC_SEM_ESTOQUE", empresaRetorno.get("orcSemEstoque").getAsString());
                                                if ((empresaRetorno.has("pedSemEstoque")) && (empresaRetorno.get("pedSemEstoque").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("PED_SEM_ESTOQUE", empresaRetorno.get("pedSemEstoque").getAsString());
                                                }
                                                if ((empresaRetorno.has("centavos")) && (empresaRetorno.get("centavos").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("CENTAVOS", empresaRetorno.get("centavos").getAsString());
                                                }
                                                if ((empresaRetorno.has("valMarkupAtac1"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_ATAC1", empresaRetorno.get("valMarkupAtac1").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("valMarkupAtac2"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_ATAC2", empresaRetorno.get("valMarkupAtac2").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("valMarkupAtac3"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_ATAC3", empresaRetorno.get("valMarkupAtac3").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("valMarkupVare1"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_VARE1", empresaRetorno.get("valMarkupVare1").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("valMarkupVare2"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_VARE2", empresaRetorno.get("valMarkupVare2").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("valMarkupVare3"))) {
                                                    dadosEmpresa.put("VAL_MARKUP_VARE3", empresaRetorno.get("valMarkupVare3").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupAtac1"))) {
                                                    dadosEmpresa.put("MARKUP_ATAC1", empresaRetorno.get("markupAtac1").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupAtac2"))) {
                                                    dadosEmpresa.put("MARKUP_ATAC2", empresaRetorno.get("markupAtac2").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupAtac3"))) {
                                                    dadosEmpresa.put("MARKUP_ATAC3", empresaRetorno.get("markupAtac3").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupVare1"))) {
                                                    dadosEmpresa.put("MARKUP_VARE1", empresaRetorno.get("markupVare1").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupVare2"))) {
                                                    dadosEmpresa.put("MARKUP_VARE2", empresaRetorno.get("markupVare2").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("markupVare3"))) {
                                                    dadosEmpresa.put("MARKUP_VARE3", empresaRetorno.get("markupVare3").getAsDouble());
                                                }
                                                dadosEmpresa.put("DIAS_ATRAZO", empresaRetorno.get("diasAtrazo").getAsInt());
                                                dadosEmpresa.put("SEM_MOVIMENTO", empresaRetorno.get("semMovimento").getAsInt());
                                                dadosEmpresa.put("JUROS_DIARIO", empresaRetorno.get("jurosDiario").getAsDouble());
                                                if ((empresaRetorno.has("capitaliza")) && (empresaRetorno.get("capitaliza").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("CAPITALIZA", empresaRetorno.get("capitaliza").getAsString());
                                                }
                                                if ((empresaRetorno.has("indexa")) && (empresaRetorno.get("indexa").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("INDEXA", empresaRetorno.get("indexa").getAsString());
                                                }
                                                if ((empresaRetorno.has("indiceValor")) && (empresaRetorno.get("indiceValor").getAsString().length() > 0)) {
                                                    dadosEmpresa.put("INDICE_VALOR", empresaRetorno.get("indiceValor").getAsString());
                                                }
                                                dadosEmpresa.put("VENDE_BLOQUEADO_ORC", empresaRetorno.has("vendeBloqueadoOrc") ? empresaRetorno.get("vendeBloqueadoOrc").getAsString() : "");
                                                dadosEmpresa.put("VENDE_BLOQUEADO_PED", empresaRetorno.has("vendeBloqueadoPed") ? empresaRetorno.get("vendeBloqueadoPed").getAsString() : "");
                                                dadosEmpresa.put("VALIDADE_FICHA_CLIENTE", empresaRetorno.get("validadeFichaCliente").getAsInt());
                                                if ((empresaRetorno.has("percJrVencParcMaior"))) {
                                                    dadosEmpresa.put("PERC_JR_VENC_PARC_MAIOR", empresaRetorno.get("percJrVencParcMaior").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("percJurosDiaRefat"))) {
                                                    dadosEmpresa.put("PERC_JUROS_DIA_REFAT", empresaRetorno.get("percJurosDiaRefat").getAsDouble());
                                                }
                                                if ((empresaRetorno.has("percMulta"))) {
                                                    dadosEmpresa.put("PERC_MULTA", empresaRetorno.get("percMulta").getAsDouble());
                                                }
                                                ;
                                                dadosEmpresa.put("VL_MIN_PRAZO_VAREJO", empresaRetorno.get("vlMinPrazoVarejo").getAsDouble());
                                                dadosEmpresa.put("VL_MIN_PRAZO_ATACADO", empresaRetorno.get("vlMinPrazoAtacado").getAsDouble());
                                                dadosEmpresa.put("VL_MIN_VISTA_VAREJO", empresaRetorno.get("vlMinVistaVarejo").getAsDouble());
                                                dadosEmpresa.put("VL_MIN_VISTA_ATACADO", empresaRetorno.get("vlMinVistaAtacado").getAsDouble());
                                                dadosEmpresa.put("MULTIPLOS_PLANOS", empresaRetorno.has("multiplosPlanos") ? empresaRetorno.get("multiplosPlanos").getAsString() : "");
                                                dadosEmpresa.put("QTD_DIAS_DESTACA_PRODUTO", empresaRetorno.get("qtdDiasDestacaProduto").getAsInt());
                                                dadosEmpresa.put("QTD_CASAS_DECIMAIS", empresaRetorno.has("qtdCasasDecimais") ? empresaRetorno.get("qtdCasasDecimais").getAsInt() : 3);
                                                dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_ATACADO", empresaRetorno.has("fechaVendaCredNegAtac") ? empresaRetorno.get("fechaVendaCredNegAtac").getAsString() : "");
                                                dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_VAREJO", empresaRetorno.has("fechaVendaCredNegVare") ? empresaRetorno.get("fechaVendaCredNegVare").getAsString() : "");
                                                dadosEmpresa.put("TIPO_ACUMULO_CREDITO_ATACADO", empresaRetorno.has("titpoAcumuloCredAtac") ? empresaRetorno.get("titpoAcumuloCredAtac").getAsString() : "");
                                                dadosEmpresa.put("TIPO_ACUMULO_CREDITO_VAREJO", empresaRetorno.has("titpoAcumuloCredVare") ? empresaRetorno.get("titpoAcumuloCredVare").getAsString() : "");
                                                dadosEmpresa.put("PERIODO_CREDITO_ATACADO", empresaRetorno.has("periodoCrcedAtac") ? empresaRetorno.get("periodoCrcedAtac").getAsString() : "");
                                                dadosEmpresa.put("PERIODO_CREDITO_VAREJO", empresaRetorno.has("periodoCrcedVare") ? empresaRetorno.get("periodoCrcedVare").getAsString() : "");
                                                dadosEmpresa.put("VERSAO_SAVARE", empresaRetorno.has("versaoSavare") ? empresaRetorno.get("versaoSavare").getAsString() : "");

                                                salvarDadosXml(dadosEmpresa);
                                                listaDadosEmpresa.add(dadosEmpresa);
                                            }
                                            EmpresaSql empresaSql = new EmpresaSql(context);

                                            todosSucesso = empresaSql.insertList(listaDadosEmpresa);
                                        }
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, true);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                    } else {
                                        final JsonObject finalRetornoWebservice = retornoWebservice;
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatusErro.append("\n*" +
                                                        context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" +
                                                        finalRetornoWebservice.toString() + "\n" +
                                                        "O retorno do webservice chegou vazio com os dados da empresa.");
                                            }
                                        });
                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_empresa))
                                                .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                    }
                                    // Incrementa o total de paginas
                                    pageNumber++;
                                    if (pageNumber < totalPages) {
                                        retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                    }
                                } else {
                                    todosSucesso = false;

                                    if (textStatusErro != null) {
                                        final JsonObject finalStatuRetorno = statuRetorno;
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatusErro.append("\n*" +
                                                        context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + "\n" +
                                                        "Código: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO) + "\n" +
                                                        "Mensagem: " + finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO));
                                            }
                                        });
                                    }
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                            }// Fim do for ia (page)
                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("SMAEMPRE");
                            }
                        }
                    } else {
                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                    }
                }
            }
        } catch (final Exception e) {
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                        .bigText("ImportarDadosEmpresa - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

            if (textStatusErro != null) {
                final JsonObject finalStatuRetorno = statuRetorno;
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" +"ImportarDadosEmpresa- " + e.getMessage());
                    }
                });
            }
        }
    }

    private void importarDadosArea() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Área de Atuação");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Área de Atuação");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAAREAS");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaAreasRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaAreasRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_areas));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaAreasRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_areas));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaAreasRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosAreas = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaAreasRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_areas) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + "/" + listaAreasRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaAreasRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_areas) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaAreasRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject areasRetorno = listaAreasRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosAreas = new ContentValues();

                                            dadosAreas.put("ID_CFAAREAS", areasRetorno.get("idCfaareas").getAsInt());
                                            dadosAreas.put("DT_ALT", areasRetorno.get("dtAlt").getAsString());
                                            dadosAreas.put("CODIGO", areasRetorno.get("codigo").getAsInt());
                                            dadosAreas.put("DESCRICAO", areasRetorno.get("descricao").getAsString());
                                            dadosAreas.put("DESC_ATAC_VISTA", areasRetorno.get("descAtacVista").getAsDouble());
                                            dadosAreas.put("DESC_ATAC_PRAZO", areasRetorno.get("descAtacPrazo").getAsDouble());
                                            dadosAreas.put("DESC_VARE_VISTA", areasRetorno.get("descVareVista").getAsDouble());
                                            dadosAreas.put("DESC_VARE_PRAZO", areasRetorno.get("descVarePrazo").getAsDouble());
                                            dadosAreas.put("DESC_SERV_VISTA", areasRetorno.get("descServVista").getAsDouble());
                                            dadosAreas.put("DESC_SERV_PRAZO", areasRetorno.get("descServPrazo").getAsDouble());
                                            if ((areasRetorno.has("descPromocao"))) {
                                                dadosAreas.put("DESC_PROMOCAO", areasRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosAreas.add(dadosAreas);
                                        }
                                        AreasSql areasSql = new AreasSql(context);

                                        todosSucesso = areasSql.insertList(listaDadosAreas);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.msg_error)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                /*Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.msg_error)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();*/

                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + retornoWebservice.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAAREAS");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    /*Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.msg_error)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();*/

                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Área : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosArea - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosAtividade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Ramo de Atividade");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Ramo de Atividade");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAATIVI");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaAtividadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaAtividadeRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_atividade));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaAtividadeRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_atividade));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaAtividadeRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosAtividade = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaAtividadeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_atividade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaAtividadeRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaAtividadeRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_atividade) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaAtividadeRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject atividadeRetorno = listaAtividadeRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosAtividade = new ContentValues();

                                            dadosAtividade.put("ID_CFAATIVI", atividadeRetorno.get("idCfaativi").getAsInt());
                                            dadosAtividade.put("DT_ALT", atividadeRetorno.get("dtAlt").getAsString());
                                            dadosAtividade.put("CODIGO", atividadeRetorno.get("codigo").getAsInt());
                                            dadosAtividade.put("DESCRICAO", atividadeRetorno.get("descricao").getAsString());
                                            dadosAtividade.put("DESC_ATAC_VISTA", atividadeRetorno.get("descAtacVista").getAsDouble());
                                            dadosAtividade.put("DESC_ATAC_PRAZO", atividadeRetorno.get("descAtacPrazo").getAsDouble());
                                            dadosAtividade.put("DESC_VARE_VISTA", atividadeRetorno.get("descVareVista").getAsDouble());
                                            dadosAtividade.put("DESC_VARE_PRAZO", atividadeRetorno.get("descVarePrazo").getAsDouble());
                                            dadosAtividade.put("DESC_SERV_VISTA", atividadeRetorno.get("descServVista").getAsDouble());
                                            dadosAtividade.put("DESC_SERV_PRAZO", atividadeRetorno.get("descServPrazo").getAsDouble());
                                            if ((atividadeRetorno.has("descPromocao"))) {
                                                dadosAtividade.put("DESC_PROMOCAO", atividadeRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosAtividade.add(dadosAtividade);
                                        } // FIm do for
                                        RamoAtividadeSql ramoAtividadeSql = new RamoAtividadeSql(context);
                                        todosSucesso = ramoAtividadeSql.insertList(listaDadosAtividade);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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

                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAATIVI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Atividade : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosAtividades - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosStatus() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Status");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Status");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFASTATU");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaStatusRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaStatusRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_status));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaStatusRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_status));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaStatusRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosStatus = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaStatusRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_status) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaStatusRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaStatusRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_status) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaStatusRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject statusObsRetorno = listaStatusRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosStatus = new ContentValues();

                                            dadosStatus.put("ID_CFASTATU", statusObsRetorno.get("idCfastatu").getAsInt());
                                            dadosStatus.put("DT_ALT", statusObsRetorno.get("dtAlt").getAsString());
                                            dadosStatus.put("CODIGO", statusObsRetorno.get("codigo").getAsInt());
                                            dadosStatus.put("DESCRICAO", statusObsRetorno.get("descricao").getAsString());
                                            if ((statusObsRetorno.has("mensagem"))) {
                                                dadosStatus.put("MENSAGEM", statusObsRetorno.get("mensagem").getAsString());
                                            }
                                            if ((statusObsRetorno.has("bloqueia"))) {
                                                dadosStatus.put("BLOQUEIA", statusObsRetorno.get("bloqueia").getAsString());
                                            }
                                            if ((statusObsRetorno.has("parcelaEmAberto"))) {
                                                dadosStatus.put("PARCELA_EM_ABERTO", statusObsRetorno.get("parcelaEmAberto").getAsString());
                                            }
                                            if ((statusObsRetorno.has("vistaPrazo"))) {
                                                dadosStatus.put("VISTA_PRAZO", statusObsRetorno.get("vistaPrazo").getAsString());
                                            }
                                            dadosStatus.put("DESC_ATAC_VISTA", statusObsRetorno.get("descAtacVista").getAsDouble());
                                            dadosStatus.put("DESC_ATAC_PRAZO", statusObsRetorno.get("descAtacPrazo").getAsDouble());
                                            dadosStatus.put("DESC_VARE_VISTA", statusObsRetorno.get("descVareVista").getAsDouble());
                                            dadosStatus.put("DESC_VARE_PRAZO", statusObsRetorno.get("descVarePrazo").getAsDouble());
                                            dadosStatus.put("DESC_SERV_VISTA", statusObsRetorno.get("descServVista").getAsDouble());
                                            dadosStatus.put("DESC_SERV_PRAZO", statusObsRetorno.get("descServPrazo").getAsDouble());
                                            if ((statusObsRetorno.has("descPromocao"))) {
                                                dadosStatus.put("DESC_PROMOCAO", statusObsRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosStatus.add(dadosStatus);
                                        } // Fim for
                                        StatusSql statusSql = new StatusSql(context);

                                        todosSucesso = statusSql.insertList(listaDadosStatus);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFASTATU");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Status : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosStatus - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosTipoDocumento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Tipo Documento");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Tipo Documento");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFATPDOC");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoDocumentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoDocumentoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_documento));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaTipoDocumentoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_documento));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaTipoDocumentoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosDocumento = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaTipoDocumentoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoDocumentoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaTipoDocumentoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaTipoDocumentoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject tipoDocumentoRetorno = listaTipoDocumentoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosDocumento = new ContentValues();

                                            dadosDocumento.put("ID_CFATPDOC", tipoDocumentoRetorno.get("idCfatpdoc").getAsInt());
                                            dadosDocumento.put("ID_SMAEMPRE", tipoDocumentoRetorno.get("idSmaempre").getAsInt());
                                            dadosDocumento.put("DT_ALT", tipoDocumentoRetorno.get("dtAlt").getAsString());
                                            dadosDocumento.put("CODIGO", tipoDocumentoRetorno.get("codigo").getAsInt());
                                            if (tipoDocumentoRetorno.has("descricao")) {
                                                dadosDocumento.put("DESCRICAO", tipoDocumentoRetorno.get("descricao").getAsString());
                                            }
                                            if (tipoDocumentoRetorno.has("sigla")) {
                                                dadosDocumento.put("SIGLA", tipoDocumentoRetorno.get("sigla").getAsString());
                                            }
                                            if (tipoDocumentoRetorno.has("tipo")) {
                                                dadosDocumento.put("TIPO", tipoDocumentoRetorno.get("tipo").getAsString());
                                            }
                                            listaDadosDocumento.add(dadosDocumento);
                                        } // FIm do for
                                        TipoDocumentoSql tipoDocumentoSql = new TipoDocumentoSql(context);

                                        todosSucesso = tipoDocumentoSql.insertList(listaDadosDocumento);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                            } else {
                                // Cria uma notificacao para ser manipulado
                                /*Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.msg_error)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();*/

                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                            // Incrementa o total de paginas
                            pageNumber++;
                            if (pageNumber < totalPages) {
                                retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPDOC");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Tipo de Documento : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosTipoDocumento - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosCartaoCredito() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Cartão Credito");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Cartão Credito");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACCRED");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaCartaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaCartaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cartao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaCartaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cartao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaCartaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosCartao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaCartaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cartao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaCartaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaCartaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cartao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaCartaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject cartaoRetorno = listaCartaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosCartao = new ContentValues();

                                            dadosCartao.put("ID_CFACCRED", cartaoRetorno.get("idCfaccred").getAsInt());
                                            if (cartaoRetorno.has("idCbaplctaTaxa") && cartaoRetorno.get("idCbaplctaTaxa").getAsInt() > 0) {
                                                dadosCartao.put("ID_CBAPLCTA_TAXA", cartaoRetorno.get("idCbaplctaTaxa").getAsInt());
                                            }
                                            if (cartaoRetorno.has("idCfaporta") && cartaoRetorno.get("idCfaporta").getAsInt() > 0) {
                                                dadosCartao.put("ID_CFAPORTA", cartaoRetorno.get("idCfaporta").getAsInt());
                                            }
                                            dadosCartao.put("GUID", cartaoRetorno.get("guid").getAsString());
                                            dadosCartao.put("US_CAD", cartaoRetorno.get("usCad").getAsString());
                                            dadosCartao.put("DT_CAD", cartaoRetorno.get("dtCad").getAsString());
                                            dadosCartao.put("DT_ALT", cartaoRetorno.get("dtAlt").getAsString());
                                            dadosCartao.put("CODIGO", cartaoRetorno.get("codigo").getAsInt());
                                            dadosCartao.put("DESCRICAO", cartaoRetorno.get("descricao").getAsString());
                                            if (cartaoRetorno.has("parcelaFim1") && cartaoRetorno.get("parcelaFim1").getAsInt() > 0) {
                                                dadosCartao.put("PARCELA_FIM1", cartaoRetorno.get("parcelaFim1").getAsInt());
                                            }
                                            if (cartaoRetorno.has("parcelaFim2") && cartaoRetorno.get("parcelaFim2").getAsInt() > 0) {
                                                dadosCartao.put("PARCELA_FIM2", cartaoRetorno.get("parcelaFim2").getAsInt());
                                            }
                                            if (cartaoRetorno.has("parcelaFim3") && cartaoRetorno.get("parcelaFim3").getAsInt() > 0) {
                                                dadosCartao.put("PARCELA_FIM3", cartaoRetorno.get("parcelaFim3").getAsInt());
                                            }
                                            if (cartaoRetorno.has("taxa1")) {
                                                dadosCartao.put("TAXA1", cartaoRetorno.get("taxa1").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxa2")) {
                                                dadosCartao.put("TAXA2", cartaoRetorno.get("taxa2").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxa3")) {
                                                dadosCartao.put("TAXA3", cartaoRetorno.get("taxa3").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxaDeb")) {
                                                dadosCartao.put("TAXA_DEB", cartaoRetorno.get("taxaDeb").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("diasDeb")) {
                                                dadosCartao.put("DIAS_DEB", cartaoRetorno.get("diasDeb").getAsInt());
                                            }
                                            if (cartaoRetorno.has("diasCre")) {
                                                dadosCartao.put("DIAS_CRE", cartaoRetorno.get("diasCre").getAsInt());
                                            }
                                            if (cartaoRetorno.has("antecipa")) {
                                                dadosCartao.put("ANTECIPA", cartaoRetorno.get("antecipa").getAsString());
                                            }
                                            if (cartaoRetorno.has("tarifaPorTransacao")) {
                                                dadosCartao.put("TARIFA_POR_TRANSACAO", cartaoRetorno.get("tarifaPorTransacao").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxaIntermediacao")) {
                                                dadosCartao.put("TAXA_INTERMEDIACAO", cartaoRetorno.get("taxaIntermediacao").getAsDouble());
                                            }

                                            listaDadosCartao.add(dadosCartao);
                                        }
                                        CartaoSql cartaoSql = new CartaoSql(context);

                                        todosSucesso = cartaoSql.insertList(listaDadosCartao);


                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACCRED");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Cartão de Crédito : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosCartaoCredito - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        }
    }

    private void importarDadosPortador() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Portador (Banco)");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Portador (Banco)");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAPORTA");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());


                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPortadorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPortadorRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_portador));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaPortadorRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_portador));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaPortadorRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosPortador = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaPortadorRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_portador) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPortadorRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaPortadorRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_portador) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPortadorRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject portadorRetorno = listaPortadorRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosPortador = new ContentValues();

                                            dadosPortador.put("ID_CFAPORTA", portadorRetorno.get("idCfaporta").getAsInt());
                                            dadosPortador.put("DT_ALT", portadorRetorno.get("dtAlt").getAsString());
                                            dadosPortador.put("CODIGO", portadorRetorno.get("codigo").getAsInt());
                                            if (portadorRetorno.has("dg")) {
                                                dadosPortador.put("DG", portadorRetorno.get("dg").getAsInt());
                                            }
                                            if (portadorRetorno.has("descricao")) {
                                                dadosPortador.put("DESCRICAO", portadorRetorno.get("descricao").getAsString());
                                            }
                                            if (portadorRetorno.has("sigla")) {
                                                dadosPortador.put("SIGLA", portadorRetorno.get("sigla").getAsString());
                                            }
                                            if (portadorRetorno.has("tipo")) {
                                                dadosPortador.put("TIPO", portadorRetorno.get("tipo").getAsString());
                                            }
                                            listaDadosPortador.add(dadosPortador);
                                        }
                                        PortadorBancoSql portadorBancoSql = new PortadorBancoSql(context);

                                        todosSucesso = portadorBancoSql.insertList(listaDadosPortador);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPORTA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Portador : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosPortador - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosProfissao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Profissão");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Profissão");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAPROFI");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProfissaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProfissaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_profisao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaProfissaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_profisao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaProfissaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosProfissao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaProfissaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_profisao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProfissaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaProfissaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_profisao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaProfissaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject profissaoRetorno = listaProfissaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosProfissao = new ContentValues();

                                            dadosProfissao.put("ID_CFAPROFI", profissaoRetorno.get("idCfaprofi").getAsInt());
                                            dadosProfissao.put("DT_ALT", profissaoRetorno.get("dtAlt").getAsString());
                                            dadosProfissao.put("CODIGO", profissaoRetorno.get("codigo").getAsInt());
                                            if (profissaoRetorno.has("cbo")) {
                                                dadosProfissao.put("CBO", profissaoRetorno.get("cbo").getAsInt());
                                            }
                                            dadosProfissao.put("DESCRICAO", profissaoRetorno.get("descricao").getAsString());
                                            dadosProfissao.put("DESC_ATAC_VISTA", profissaoRetorno.get("descAtacVista").getAsDouble());
                                            dadosProfissao.put("DESC_ATAC_PRAZO", profissaoRetorno.get("descAtacPrazo").getAsDouble());
                                            dadosProfissao.put("DESC_VARE_PRAZO", profissaoRetorno.get("descVarePrazo").getAsDouble());
                                            dadosProfissao.put("DESC_VARE_VISTA", profissaoRetorno.get("descVareVista").getAsDouble());
                                            dadosProfissao.put("DESC_SERV_VISTA", profissaoRetorno.get("descServVista").getAsDouble());
                                            dadosProfissao.put("DESC_SERV_PRAZO", profissaoRetorno.get("descServPrazo").getAsDouble());
                                            if (profissaoRetorno.has("descPromocao")) {
                                                dadosProfissao.put("DESC_PROMOCAO", profissaoRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosProfissao.add(dadosProfissao);
                                        } // Fim for
                                        ProfissaoSql profissaoSql = new ProfissaoSql(context);

                                        todosSucesso = profissaoSql.insertList(listaDadosProfissao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPROFI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    /*Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();*/

                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Profissão : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosProfissao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosTipoCliente() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Tipo Cliente");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());


        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Tipo Cliente");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFATPCLI");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoClienteRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaTipoClienteRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaTipoClienteRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosTipoCliente = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaTipoClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoClienteRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaTipoClienteRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaTipoClienteRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject tipoClienteRetorno = listaTipoClienteRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosTipoCliente = new ContentValues();

                                            dadosTipoCliente.put("ID_CFATPCLI", tipoClienteRetorno.get("idCfatpcli").getAsInt());
                                            dadosTipoCliente.put("DT_ALT", tipoClienteRetorno.get("dtAlt").getAsString());
                                            dadosTipoCliente.put("CODIGO", tipoClienteRetorno.get("codigo").getAsInt());
                                            dadosTipoCliente.put("DESCRICAO", tipoClienteRetorno.get("descricao").getAsString());
                                            dadosTipoCliente.put("DESC_ATAC_VISTA", tipoClienteRetorno.get("descAtacVista").getAsDouble());
                                            dadosTipoCliente.put("DESC_ATAC_PRAZO", tipoClienteRetorno.get("descAtacPrazo").getAsDouble());
                                            dadosTipoCliente.put("DESC_VARE_PRAZO", tipoClienteRetorno.get("descVarePrazo").getAsDouble());
                                            dadosTipoCliente.put("DESC_VARE_VISTA", tipoClienteRetorno.get("descVareVista").getAsDouble());
                                            dadosTipoCliente.put("DESC_SERV_VISTA", tipoClienteRetorno.get("descServVista").getAsDouble());
                                            dadosTipoCliente.put("DESC_SERV_PRAZO", tipoClienteRetorno.get("descServPrazo").getAsDouble());
                                            if (tipoClienteRetorno.has("descPromocao")) {
                                                dadosTipoCliente.put("DESC_PROMOCAO", tipoClienteRetorno.get("descPromocao").getAsString());
                                            }
                                            if (tipoClienteRetorno.has("vendeAtacVarejo")) {
                                                dadosTipoCliente.put("VENDE_ATAC_VAREJO", tipoClienteRetorno.get("vendeAtacVarejo").getAsString());
                                            }
                                            listaDadosTipoCliente.add(dadosTipoCliente);
                                        }
                                        TipoClienteSql tipoClienteSql = new TipoClienteSql(context);

                                        todosSucesso = tipoClienteSql.insertList(listaDadosTipoCliente);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCLI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Tipo Cliente : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosTipoCliente - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosTipoCobranca() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Tipo de Cobrança");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Tipo de Cobrança");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFATPCOB");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoCobrancaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoCobrancaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaTipoCobrancaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaTipoCobrancaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosTipoCobranca = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaTipoCobrancaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoCobrancaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaTipoCobrancaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaTipoCobrancaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject tipoCobrancaRetorno = listaTipoCobrancaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosTipoCobranca = new ContentValues();

                                            dadosTipoCobranca.put("ID_CFATPCOB", tipoCobrancaRetorno.get("idCfatpcob").getAsInt());
                                            dadosTipoCobranca.put("DT_ALT", tipoCobrancaRetorno.get("dtAlt").getAsString());
                                            dadosTipoCobranca.put("CODIGO", tipoCobrancaRetorno.get("codigo").getAsInt());
                                            dadosTipoCobranca.put("DESCRICAO", tipoCobrancaRetorno.get("descricao").getAsString());
                                            dadosTipoCobranca.put("SIGLA", tipoCobrancaRetorno.get("sigla").getAsString());

                                            listaDadosTipoCobranca.add(dadosTipoCobranca);
                                        }
                                        CobrancaSql cobrancaSql = new CobrancaSql(context);

                                        todosSucesso = cobrancaSql.insertList(listaDadosTipoCobranca);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCOB");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - TIpo Cobrança : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosTipoCobranca - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosEstado() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Estado");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Estado");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAESTAD");
            String ultimaDataParam = pegaUltimaDataAtualizacao("CFAPARAM");
            String ultimaDataEnder = pegaUltimaDataAtualizacao("CFAENDER");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraEstadoPorFuncionario =
                    "(CFAESTAD.ID_CFAESTAD IN " +
                            "(SELECT DISTINCT CFAENDER.ID_CFAESTAD FROM CFAENDER WHERE CFAENDER.ID_CFACLIFO IN (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                            "AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")))))";

            Gson gson = new Gson();
            String whereDatas = "";

            /*if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                whereDatas += "(DT_ALT >= '" + ultimaData + "') ";
            }
            // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA PARAM
            if ((ultimaDataParam != null) && (!ultimaDataParam.isEmpty())) {
                if ((!whereDatas.isEmpty()) && (whereDatas.length() > 5)){
                    whereDatas += " OR ";
                }
                whereDatas += "((SELECT CFAPARAM.DT_ALT FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                        " AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE \n" +
                        " CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))) >= '" + ultimaDataParam + "')";
            }
            // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA ENDERECO DA LISTA DE CLIENTES DO VENDEDOR
            if ((ultimaDataEnder != null) && (!ultimaDataEnder.isEmpty())) {
                if ((!whereDatas.isEmpty()) && (whereDatas.length() > 5)){
                    whereDatas += " OR ";
                }
                whereDatas += "( (SELECT CFAENDER.DT_ALT FROM CFAENDER WHERE \n" +
                        " CFAENDER.ID_CFACLIFO IN (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                        " AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE \n" +
                        " CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")))) >= '" + ultimaDataEnder + "')";
            }*/

            if ((whereDatas != null) && (!whereDatas.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraEstadoPorFuncionario;
            } else {
                parametrosWebservice += "&where= " + filtraEstadoPorFuncionario;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEstadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEstadoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_estado));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaEstadoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estado));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaEstadoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosEstado = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaEstadoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_estado) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEstadoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaEstadoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estado) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaEstadoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject estadoRetorno = listaEstadoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosEstado = new ContentValues();

                                            dadosEstado.put("ID_CFAESTAD", estadoRetorno.get("idCfaestad").getAsInt());
                                            dadosEstado.put("DT_ALT", estadoRetorno.get("dtAlt").getAsString());
                                            dadosEstado.put("DESCRICAO", estadoRetorno.get("descricao").getAsString());
                                            dadosEstado.put("UF", estadoRetorno.get("uf").getAsString());
                                            if (estadoRetorno.has("icmsSai")) {
                                                dadosEstado.put("ICMS_SAI", estadoRetorno.get("icmsSai").getAsDouble());
                                            }
                                            if (estadoRetorno.has("ipiSai")) {
                                                dadosEstado.put("IPI_SAI", estadoRetorno.get("ipiSai").getAsDouble());
                                            }
                                            if (estadoRetorno.has("tipoIpiSai")) {
                                                dadosEstado.put("TIPO_IPI_SAI", estadoRetorno.get("tipoIpiSai").getAsString());
                                            }
                                            listaDadosEstado.add(dadosEstado);
                                        }
                                        EstadoSql estadoSql = new EstadoSql(context);

                                        todosSucesso = estadoSql.insertList(listaDadosEstado);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAESTAD");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Estado : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosEstado - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosCidade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Cidade");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Cidade");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACIDAD");
            String ultimaDataParam = pegaUltimaDataAtualizacao("CFAPARAM");
            String ultimaDataEnder = pegaUltimaDataAtualizacao("CFAENDER");
            String ultimaDataEstad = pegaUltimaDataAtualizacao("CFAESTAD");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraCidadePorFuncionario =
                    "(CFACIDAD.ID_CFAESTAD IN " +
                            "(SELECT DISTINCT CFAENDER.ID_CFAESTAD FROM CFAENDER WHERE CFAENDER.ID_CFACLIFO IN (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                            "AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")))))";

            String sqlQuery =
                    "SELECT  DISTINCT CFACIDAD.ID_CFACIDAD, CFACIDAD.ID_CFAESTAD, CFACIDAD.GUID, CFACIDAD.US_CAD, \n" +
                            "CFACIDAD.DT_CAD, CFACIDAD.DT_ALT, CFACIDAD.CT_INTEG, CFACIDAD.DESCRICAO, CFACIDAD.CEP, \n" +
                            "CFACIDAD.DDD, CFACIDAD.COD_IBGE FROM CFAENDER \n" +
                            "LEFT OUTER JOIN CFACIDAD ON (CFAENDER.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) \n" +
                            "LEFT OUTER JOIN CFAESTAD ON (CFAENDER.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) \n" +
                            "LEFT OUTER JOIN CFACLIFO ON (CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n" +
                            "LEFT OUTER JOIN CFAPARAM ON (CFACLIFO.ID_CFACLIFO = CFAPARAM.ID_CFACLIFO) \n" +
                            "WHERE \n" +
                            "CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CFACLIFO_VEND_CIDADE.ID_CFACLIFO FROM CFACLIFO CFACLIFO_VEND_CIDADE WHERE CFACLIFO_VEND_CIDADE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")";
            Gson gson = new Gson();
            String whereDatas = "";

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                whereDatas += "(CFACIDAD.DT_ALT >= '" + ultimaData + "') ";

                // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA PARAM
                if ((ultimaDataParam != null) && (!ultimaDataParam.isEmpty())) {
                    if ((!whereDatas.isEmpty()) && (whereDatas.length() > 5)) {
                        whereDatas += " OR ";
                    }
                    whereDatas += "(CFAPARAM.DT_ALT >= '" + ultimaDataParam + "')";
                }
                // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA ENDERECO DA LISTA DE CLIENTES DO VENDEDOR
                if ((ultimaDataEnder != null) && (!ultimaDataEnder.isEmpty())) {
                    if ((!whereDatas.isEmpty()) && (whereDatas.length() > 5)) {
                        whereDatas += " OR ";
                    }
                    whereDatas += "( CFAENDER.DT_ALT >= '" + ultimaDataEnder + "')";
                }
                // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA ESTADO
                if ((ultimaDataEstad != null) && (!ultimaDataEstad.isEmpty())) {
                    if ((!whereDatas.isEmpty()) && (whereDatas.length() > 5)) {
                        whereDatas += " OR ";
                    }
                    whereDatas += "( CFAESTAD.DT_ALT >= '" + ultimaDataEstad + "')";
                }
            }

            if ((whereDatas != null) && (!whereDatas.isEmpty())) {

                parametrosWebservice += "&sqlQuery= " + sqlQuery + " AND ( " + whereDatas + " ) ";
            } else {
                parametrosWebservice += "&sqlQuery= " + sqlQuery;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaCidadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaCidadeRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cidade));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaCidadeRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cidade));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaCidadeRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosStatus = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaCidadeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cidade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaCidadeRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaCidadeRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cidade) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaCidadeRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject cidadeRetorno = listaCidadeRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosCidade = new ContentValues();

                                            dadosCidade.put("ID_CFACIDAD", cidadeRetorno.get("idCfacidad").getAsInt());
                                            dadosCidade.put("ID_CFAESTAD", cidadeRetorno.get("idCfaestad").getAsInt());
                                            dadosCidade.put("DT_ALT", cidadeRetorno.get("dtAlt").getAsString());
                                            dadosCidade.put("COD_IBGE", (cidadeRetorno.has("codIbge")) ? cidadeRetorno.get("codIbge").getAsInt() : null);
                                            dadosCidade.put("DESCRICAO", cidadeRetorno.get("descricao").getAsString());

                                            listaDadosStatus.add(dadosCidade);
                                        }
                                        CidadeSql cidadeSql = new CidadeSql(context);

                                        todosSucesso = cidadeSql.insertList(listaDadosStatus);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACIDAD");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Cidade : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosCidade - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosClifo() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Cliente e Fornecedor");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Cliente e Fornecedor");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACLIFO");
            String ultimaDataParam = pegaUltimaDataAtualizacao("CFAPARAM");

            String filtraClientePorParametro =
                    "SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") AND \n" +
                            "(CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))  \n";

            // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA PARAM
            if ((ultimaDataParam != null) && (!ultimaDataParam.isEmpty())) {

                filtraClientePorParametro += " AND (CFAPARAM.DT_ALT >= '" + ultimaDataParam + "')";
            }

            // Cria uma variavel para salvar todos os paramentros em json
            String filtraClientePorVendedor =
                    "((CFACLIFO.ID_CFACLIFO IN \n" +
                            "(" + filtraClientePorParametro + " ) " +
                            ") \n" +
                            "OR ( (CFACLIFO.NOME_RAZAO LIKE '%CONSUM%FIN%') " + ((ultimaData != null && !ultimaData.isEmpty()) ? " AND (CFACLIFO.DT_ALT >= '" + ultimaData + "')" : "") + " ))";

            String parametrosWebservice = "";
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraClientePorVendedor;
            } else {
                parametrosWebservice += "&where= " + filtraClientePorVendedor;
            }
            Gson gson = new Gson();
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClienteRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cliente));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaClienteRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaClienteRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosClifo = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cliente) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClienteRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaClienteRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaClienteRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject clienteRetorno = listaClienteRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosClifo = new ContentValues();

                                            dadosClifo.put("ID_CFACLIFO", clienteRetorno.get("idCfaclifo").getAsInt());
                                            dadosClifo.put("ID_SMAEMPRE", clienteRetorno.get("idSmaempre").getAsInt());
                                            dadosClifo.put("CPF_CNPJ", clienteRetorno.has("cpfCgc") ? clienteRetorno.get("cpfCgc").getAsString() : "");
                                            dadosClifo.put("DT_ALT", clienteRetorno.get("dtAlt").getAsString());
                                            if (clienteRetorno.has("ieRg")) {
                                                dadosClifo.put("IE_RG", clienteRetorno.get("ieRg").getAsString());
                                            }
                                            dadosClifo.put("NOME_RAZAO", clienteRetorno.get("nomeRazao").getAsString());
                                            if (clienteRetorno.has("nomeFantasia")) {
                                                dadosClifo.put("NOME_FANTASIA", clienteRetorno.get("nomeFantasia").getAsString());
                                            }
                                            if (clienteRetorno.has("dtNascimento")) {
                                                dadosClifo.put("DT_NASCIMENTO", clienteRetorno.get("dtNascimento").getAsString());
                                            }
                                            if (clienteRetorno.has("codigoCli")) {
                                                dadosClifo.put("CODIGO_CLI", clienteRetorno.get("codigoCli").getAsInt());
                                            }
                                            if (clienteRetorno.has("codigoFun")) {
                                                dadosClifo.put("CODIGO_FUN", clienteRetorno.get("codigoFun").getAsInt());
                                            }
                                            if (clienteRetorno.has("codigoUsu")) {
                                                dadosClifo.put("CODIGO_USU", clienteRetorno.get("codigoUsu").getAsInt());
                                            }
                                            if (clienteRetorno.has("codigoTra")) {
                                                dadosClifo.put("CODIGO_TRA", clienteRetorno.get("codigoTra").getAsInt());
                                            }
                                            if (clienteRetorno.has("cliente")) {
                                                dadosClifo.put("CLIENTE", clienteRetorno.get("cliente").getAsString());
                                            }
                                            if (clienteRetorno.has("funcionario")) {
                                                dadosClifo.put("FUNCIONARIO", clienteRetorno.get("funcionario").getAsString());
                                            }
                                            if (clienteRetorno.has("usuario")) {
                                                dadosClifo.put("USUARIO", clienteRetorno.get("usuario").getAsString());
                                            }
                                            if (clienteRetorno.has("transportadora")) {
                                                dadosClifo.put("TRANSPORTADORA", clienteRetorno.get("transportadora").getAsString());
                                            }
                                            if (clienteRetorno.has("sexo")) {
                                                dadosClifo.put("SEXO", clienteRetorno.get("sexo").getAsString());
                                            }
                                            if (clienteRetorno.has("inscSuframa")) {
                                                dadosClifo.put("INSC_SUFRAMA", clienteRetorno.get("inscSuframa").getAsString());
                                            }
                                            if (clienteRetorno.has("inscJunta")) {
                                                dadosClifo.put("INSC_JUNTA", clienteRetorno.get("inscJunta").getAsString());
                                            }
                                            if (clienteRetorno.has("inscMunicipal")) {
                                                dadosClifo.put("INSC_MUNICIPAL", clienteRetorno.get("inscMunicipal").getAsString());
                                            }
                                            if (clienteRetorno.has("inscProdutor")) {
                                                dadosClifo.put("INSC_PRODUTOR", clienteRetorno.get("inscProdutor").getAsString());
                                            }
                                            if (clienteRetorno.has("rendaMesGiro")) {
                                                dadosClifo.put("RENDA_MES_GIRO", clienteRetorno.get("rendaMesGiro").getAsDouble());
                                            }
                                            if (clienteRetorno.has("capitalSocial")) {
                                                dadosClifo.put("CAPITAL_SOCIAL", clienteRetorno.get("capitalSocial").getAsDouble());
                                            }
                                            if (clienteRetorno.has("estMercadorias")) {
                                                dadosClifo.put("EST_MERCADORIAS", clienteRetorno.get("estMercadorias").getAsDouble());
                                            }
                                            if (clienteRetorno.has("estMatPrima")) {
                                                dadosClifo.put("EST_MAT_PRIMA", clienteRetorno.get("estMatPrima").getAsDouble());
                                            }
                                            if (clienteRetorno.has("movtoVendas")) {
                                                dadosClifo.put("MOVTO_VENDAS", clienteRetorno.get("movtoVendas").getAsDouble());
                                            }
                                            if (clienteRetorno.has("despesas")) {
                                                dadosClifo.put("DESPESAS", clienteRetorno.get("despesas").getAsDouble());
                                            }
                                            if (clienteRetorno.has("empresaTrab")) {
                                                dadosClifo.put("EMPRESA_TRAB", clienteRetorno.get("empresaTrab").getAsString());
                                            }
                                            if (clienteRetorno.has("obs")) {
                                                dadosClifo.put("OBS", clienteRetorno.get("obs").getAsString());
                                            }
                                            if (clienteRetorno.has("pessoa")) {
                                                dadosClifo.put("PESSOA", clienteRetorno.get("pessoa").getAsString());
                                            }
                                            if (clienteRetorno.has("civil")) {
                                                dadosClifo.put("CIVIL", clienteRetorno.get("civil").getAsString());
                                            }
                                            if (clienteRetorno.has("conjuge")) {
                                                dadosClifo.put("CONJUGE", clienteRetorno.get("conjuge").getAsString());
                                            }
                                            if (clienteRetorno.has("cpfConjuge")) {
                                                dadosClifo.put("CPF_CONJUGE", clienteRetorno.get("cpfConjuge").getAsString());
                                            }
                                            if (clienteRetorno.has("dtNascConj")) {
                                                dadosClifo.put("DT_NAC_CONJ", clienteRetorno.get("dtNascConj").getAsString());
                                            }
                                            if (clienteRetorno.has("qtdeFuncionarios")) {
                                                dadosClifo.put("QTDE_FUNCIONARIOS", clienteRetorno.get("qtdeFuncionarios").getAsInt());
                                            }
                                            if (clienteRetorno.has("outrasRendas")) {
                                                dadosClifo.put("OUTRAS_RENDAS", clienteRetorno.get("outrasRendas").getAsDouble());
                                            }
                                            if (clienteRetorno.has("numeroDependenteMaior")) {
                                                dadosClifo.put("NUM_DEP_MAIOR", clienteRetorno.get("numeroDependenteMaior").getAsInt());
                                            }
                                            if (clienteRetorno.has("numDepMenor")) {
                                                dadosClifo.put("NUM_DEP_MENOR", clienteRetorno.get("numDepMenor").getAsInt());
                                            }
                                            if (clienteRetorno.has("complementoCargoConj")) {
                                                dadosClifo.put("COMPLEMENTO_CARGO_CONJ", clienteRetorno.get("complementoCargoConj").getAsString());
                                            }
                                            if (clienteRetorno.has("rgConjuge")) {
                                                dadosClifo.put("RG_CONJUGE", clienteRetorno.get("rgConjuge").getAsString());
                                            }
                                            if (clienteRetorno.has("orgaoEmissorConj")) {
                                                dadosClifo.put("ORGAO_EMISSOR_CONJ", clienteRetorno.get("orgaoEmissorConj").getAsString());
                                            }
                                            if (clienteRetorno.has("limiteConjuge")) {
                                                dadosClifo.put("LIMITE_CONJUGE", clienteRetorno.get("limiteConjuge").getAsDouble());
                                            }
                                            if (clienteRetorno.has("empresaConjuge")) {
                                                dadosClifo.put("EMPRESA_CONJUGE", clienteRetorno.get("empresaConjuge").getAsString());
                                            }
                                            if (clienteRetorno.has("admissaoConjuge")) {
                                                dadosClifo.put("ADMISSAO_CONJUGE", clienteRetorno.get("admissaoConjuge").getAsString());
                                            }
                                            if (clienteRetorno.has("rendaConjuge")) {
                                                dadosClifo.put("RENDA_CONJUGE", clienteRetorno.get("rendaConjuge").getAsDouble());
                                            }
                                            if (clienteRetorno.has("enviarExtrato")) {
                                                dadosClifo.put("ENVIAR_EXTRATO", clienteRetorno.get("enviarExtrato").getAsString());
                                            }
                                            if (clienteRetorno.has("tipoExtrato")) {
                                                dadosClifo.put("TIPO_EXTRATO", clienteRetorno.get("tipoExtrato").getAsString());
                                            }
                                            if (clienteRetorno.has("conjPodeComprar")) {
                                                dadosClifo.put("CONJ_PODE_COMPRAR", clienteRetorno.get("conjPodeComprar").getAsString());
                                            }
                                            if (clienteRetorno.has("dtUltCompra")) {
                                                dadosClifo.put("DT_ULT_COMPRA", clienteRetorno.get("dtUltCompra").getAsString());
                                            }
                                            if (clienteRetorno.has("dtRenovacao")) {
                                                dadosClifo.put("DT_RENOVACAO", clienteRetorno.get("dtRenovacao").getAsString());
                                            }
                                            if (clienteRetorno.has("idCfastatu")) {
                                                //JsonObject status = clienteRetorno.getAsJsonObject("statusPessoa");
                                                dadosClifo.put("ID_CFASTATU", clienteRetorno.get("idCfastatu").getAsInt());
                                            }
                                            if (clienteRetorno.has("idCfaativi")) {
                                                //JsonObject ramoAtividade = clienteRetorno.getAsJsonObject("ramoAtividade");
                                                dadosClifo.put("ID_CFAATIVI", clienteRetorno.get("idCfaativi").getAsInt());
                                            }
                                            if (clienteRetorno.has("idCfatpcli")) {
                                                //JsonObject tipoClientePessoa = clienteRetorno.getAsJsonObject("tipoClientePessoa");
                                                dadosClifo.put("ID_CFATPCLI", clienteRetorno.get("idCfatpcli").getAsInt());
                                            }
                                            if (clienteRetorno.has("idCfaprofi")) {
                                                //JsonObject tipoClientePessoa = clienteRetorno.getAsJsonObject("profissaoPessoa");
                                                dadosClifo.put("ID_CFAPROFI", clienteRetorno.get("idCfaprofi").getAsInt());
                                            }
                                            if (clienteRetorno.has("idCfaareas")) {
                                                dadosClifo.put("ID_CFAAREAS", clienteRetorno.get("idCfaareas").getAsInt());
                                            }
                                            listaDadosClifo.add(dadosClifo);
                                        }
                                        PessoaSql pessoaSql = new PessoaSql(context);

                                        todosSucesso = pessoaSql.insertList(listaDadosClifo);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACLIFO");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final JsonParseException e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Cliente e Fornecedor : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosClifo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Cliente e Fornecedor : " + e.getMessage());
                    }
                });
            }

            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosClifo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosRemoveClifo() {
        JsonObject statuRetorno;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Remover Cliente e Fornecedor");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Remover Cliente e Fornecedor");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            String filtraClientePorParametro =
                    "SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") AND \n" +
                            "(CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))  \n";

            String filtraClientePorVendedor =
                    "((CFACLIFO.ID_CFACLIFO IN \n" +
                            "(" + filtraClientePorParametro + " ) " +
                            ") \n" +
                            "OR (CFACLIFO.NOME_RAZAO LIKE '%CONSUM%FIN%'))";

            parametrosWebservice += "&where= " + filtraClientePorVendedor;

            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            Gson gson = new Gson();
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    //boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        List<ContentValues> listaTodosClifo = new ArrayList<ContentValues>();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClienteRetorno.size() > 0) {
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cliente));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaClienteRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaClienteRetorno.size());
                                                }
                                            });
                                        }

                                        for (int i = 0; i < listaClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cliente) + " a Remover - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClienteRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaClienteRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente) + " a Remover - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaClienteRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject clienteRetorno = listaClienteRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosClifo = new ContentValues();

                                            dadosClifo.put("ID_CFACLIFO", clienteRetorno.get("idCfaclifo").getAsInt());
                                            dadosClifo.put("ID_SMAEMPRE", clienteRetorno.get("idSmaempre").getAsInt());
                                            dadosClifo.put("CPF_CNPJ", clienteRetorno.has("cpfCgc") ? clienteRetorno.get("cpfCgc").getAsString() : "");
                                            dadosClifo.put("NOME_RAZAO", clienteRetorno.get("nomeRazao").getAsString());

                                            listaTodosClifo.add(dadosClifo);
                                        }
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.vamos_checar_cliente));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.vamos_checar_cliente));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo,null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        if ((listaTodosClifo != null) && (listaTodosClifo.size() > 0) ){
                            PessoaSql pessoaSql = new PessoaSql(context);

                            //Cursor listaClifoApp = pessoaSql.query("CLIENTE = '1' AND CODIGO_CLI IS NOT NULL");
                            Cursor listaClifoApp = pessoaSql.query(filtraClientePorVendedor);

                            if ((listaClifoApp != null) & (listaClifoApp.getCount() > 0)){

                                if (listaTodosClifo.size() != listaClifoApp.getCount()) {

                                    while (listaClifoApp.moveToNext()) {
                                        Integer idClifoTab = listaClifoApp.getInt(listaClifoApp.getColumnIndex("ID_CFACLIFO"));
                                        Boolean naoEstaLista = true;

                                        for (ContentValues v : listaTodosClifo) {
                                            if (v.getAsInteger("ID_CFACLIFO").equals(idClifoTab)) {
                                                naoEstaLista = false;
                                                break;
                                            } else {
                                                naoEstaLista = true;
                                            }
                                        }
                                        if (naoEstaLista) {
                                            pessoaSql.delete("ID_CFACLIFO = " + idClifoTab);
                                            ParametrosSql parametrosSql = new ParametrosSql(context);
                                            parametrosSql.delete("ID_CFACLIFO = " + idClifoTab);
                                        }
                                    }
                                }
                            }
                        }
                        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, true);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (JsonParseException e) {
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosClifo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        } catch (Exception e) {

            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosClifo - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosCotacao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.procurando_dados) + " Cotação");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Cotação");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACOTAC");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACOTAC, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaCotacaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaCotacaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cotacao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaCotacaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cotacao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaCotacaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosCotacao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaCotacaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_cotacao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaCotacaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaCotacaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cotacao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaCotacaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject cotacaoRetorno = listaCotacaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosCotacao = new ContentValues();

                                            dadosCotacao.put("ID_CFACOTAC", cotacaoRetorno.get("idCfaativi").getAsInt());
                                            dadosCotacao.put("ID_CFAMOEDA", cotacaoRetorno.get("idCfamoeda").getAsInt());
                                            dadosCotacao.put("GUID", cotacaoRetorno.get("guid").getAsString());
                                            dadosCotacao.put("US_CAD", cotacaoRetorno.get("usCad").getAsString());
                                            dadosCotacao.put("DT_CAD", cotacaoRetorno.get("dtCad").getAsString());
                                            dadosCotacao.put("DT_ALT", cotacaoRetorno.get("dtAlt").getAsString());
                                            dadosCotacao.put("DATA", cotacaoRetorno.get("data").getAsString());
                                            dadosCotacao.put("VALOR", cotacaoRetorno.get("valor").getAsDouble());

                                            listaDadosCotacao.add(dadosCotacao);
                                        } // FIm do for
                                        CotacaoSql cotacaoSql = new CotacaoSql(context);
                                        todosSucesso = cotacaoSql.insertList(listaDadosCotacao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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

                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACOTAC, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACOTAC");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Cotação : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosCotação - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosEndereco() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Endereço");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Endereço");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAENDER");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraEnder =
                    "(CFAENDER.ID_CFACLIFO IN " +
                            "(SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")\n" +
                            "AND (CFAPARAM.ID_CFACLIFO_VENDE = " +
                            "(SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (CFAENDER.DT_ALT >= '" + ultimaData + "') AND " + filtraEnder;
            } else {
                parametrosWebservice += "&where= " + filtraEnder;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEnderecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEnderecoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_endereco));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaEnderecoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_endereco));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaEnderecoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosEndereco = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaEnderecoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_endereco) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEnderecoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaEnderecoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_endereco) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaEnderecoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject enderecoRetorno = listaEnderecoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosEndereco = new ContentValues();

                                            dadosEndereco.put("ID_CFAENDER", enderecoRetorno.get("idCfaender").getAsInt());
                                            dadosEndereco.put("DT_ALT", enderecoRetorno.get("dtAlt").getAsString());
                                            dadosEndereco.put("TIPO", enderecoRetorno.get("tipo").getAsString());
                                            if (enderecoRetorno.has("idCfaclifo") && enderecoRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                dadosEndereco.put("ID_CFACLIFO", enderecoRetorno.get("idCfaclifo").getAsInt());
                                            }
                                            if (enderecoRetorno.has("idSmaempre") && enderecoRetorno.get("idSmaempre").getAsInt() > 0) {
                                                dadosEndereco.put("ID_SMAEMPRE", enderecoRetorno.get("idSmaempre").getAsInt());
                                            }
                                            if (enderecoRetorno.has("idCfaestad")) {
                                                //JsonObject estado = enderecoRetorno.getAsJsonObject("estadoEndereco");
                                                dadosEndereco.put("ID_CFAESTAD", enderecoRetorno.get("idCfaestad").getAsInt());
                                            }
                                            if (enderecoRetorno.has("idCfacidad")) {
                                                //JsonObject cidade = enderecoRetorno.getAsJsonObject("cidadeEndereco");
                                                dadosEndereco.put("ID_CFACIDAD", enderecoRetorno.get("idCfacidad").getAsInt());
                                            }
                                            if (enderecoRetorno.has("cep")) {
                                                dadosEndereco.put("CEP", enderecoRetorno.get("cep").getAsString());
                                            }
                                            if (enderecoRetorno.has("bairro")) {
                                                dadosEndereco.put("BAIRRO", enderecoRetorno.get("bairro").getAsString());
                                            }
                                            if (enderecoRetorno.has("logradouro")) {
                                                dadosEndereco.put("LOGRADOURO", enderecoRetorno.get("logradouro").getAsString());
                                            }
                                            if (enderecoRetorno.has("numero")) {
                                                dadosEndereco.put("NUMERO", enderecoRetorno.get("numero").getAsString());
                                            }
                                            if (enderecoRetorno.has("complemento")) {
                                                dadosEndereco.put("COMPLEMENTO", enderecoRetorno.get("complemento").getAsString());
                                            }
                                            if (enderecoRetorno.has("email")) {
                                                dadosEndereco.put("EMAIL", enderecoRetorno.get("email").getAsString());
                                            }
                                            listaDadosEndereco.add(dadosEndereco);
                                        }
                                        EnderecoSql enderecoSql = new EnderecoSql(context);

                                        todosSucesso = enderecoSql.insertList(listaDadosEndereco);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAENDER");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final JsonParseException e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Endereço : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosEndereco - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Endereço : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosEndereco - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        }
    }


    private void importarDadosParametros() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Parâmetros");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Parâmetros");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAPARAM");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraParam =
                    "(CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")\n" +
                            "AND (CFAPARAM.ID_CFACLIFO_VENDE = " +
                            "(SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraParam;
            } else {
                parametrosWebservice += "&where= " + filtraParam;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaParametroRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaParametroRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_parametro));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaParametroRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parametro));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaParametroRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosStatus = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaParametroRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_parametro) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaParametroRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaParametroRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parametro) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaParametroRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject parametroRetorno = listaParametroRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosParametros = new ContentValues();

                                            dadosParametros.put("ID_CFAPARAM", parametroRetorno.get("idCfaparam").getAsInt());
                                            dadosParametros.put("DT_ALT", parametroRetorno.get("dtAlt").getAsString());
                                            if (parametroRetorno.has("idCfaclifo") && parametroRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                dadosParametros.put("ID_CFACLIFO", parametroRetorno.get("idCfaclifo").getAsInt());
                                            }
                                            if (parametroRetorno.has("idSmaempre") && parametroRetorno.get("idSmaempre").getAsInt() > 0) {
                                                dadosParametros.put("ID_SMAEMPRE", parametroRetorno.get("idSmaempre").getAsInt());
                                            }
                                            if (parametroRetorno.has("idCfaclifoVende") && parametroRetorno.get("idCfaclifoVende").getAsInt() > 0) {
                                                dadosParametros.put("ID_CFACLIFO_VENDE", parametroRetorno.get("idCfaclifoVende").getAsInt());
                                            }
                                            if (parametroRetorno.has("idCfatpcob") && parametroRetorno.get("idCfatpcob").getAsInt() > 0) {
                                                dadosParametros.put("ID_CFATPCOB", parametroRetorno.get("idCfatpcob").getAsInt());
                                            }
                                            if (parametroRetorno.has("idCfaporta") && parametroRetorno.get("idCfaporta").getAsInt() > 0) {
                                                dadosParametros.put("ID_CFAPORTA", parametroRetorno.get("idCfaporta").getAsInt());
                                            }
                                            if (parametroRetorno.has("idCfatpdoc") && parametroRetorno.get("idCfatpdoc").getAsInt() > 0) {
                                                dadosParametros.put("ID_CFATPDOC", parametroRetorno.get("idCfatpdoc").getAsInt());
                                            }
                                            if (parametroRetorno.has("idAeaplpgt") && parametroRetorno.get("idAeaplpgt").getAsInt() > 0) {
                                                dadosParametros.put("ID_AEAPLPGT", parametroRetorno.get("idAeaplpgt").getAsInt());
                                            }
                                            if (parametroRetorno.has("roteiro")) {
                                                dadosParametros.put("ROTEIRO", parametroRetorno.get("roteiro").getAsString());
                                            }
                                            if (parametroRetorno.has("frequencia")) {
                                                dadosParametros.put("FREQUENCIA", parametroRetorno.get("frequencia").getAsString());
                                            }
                                            if (parametroRetorno.has("diasAtrazo")) {
                                                dadosParametros.put("DIAS_ATRAZO", parametroRetorno.get("diasAtrazo").getAsString());
                                            }
                                            if (parametroRetorno.has("diasCarencia")) {
                                                dadosParametros.put("DIAS_CARENCIA", parametroRetorno.get("diasCarencia").getAsString());
                                            }
                                            if (parametroRetorno.has("vendeAtrazado")) {
                                                dadosParametros.put("VENDE_ATRAZADO", parametroRetorno.get("vendeAtrazado").getAsString());
                                            }
                                            if (parametroRetorno.has("descPromocao")) {
                                                dadosParametros.put("DESC_PROMOCAO", parametroRetorno.get("descPromocao").getAsString());
                                            }
                                            if (parametroRetorno.has("dtUltVisita")) {
                                                dadosParametros.put("DT_ULT_VISITA", parametroRetorno.get("dtUltVisita").getAsString());
                                            }
                                            if (parametroRetorno.has("dtUltEnvio")) {
                                                dadosParametros.put("DT_ULT_ENVIO", parametroRetorno.get("dtUltEnvio").getAsString());
                                            }
                                            if (parametroRetorno.has("dataUltimoRecebimento")) {
                                                dadosParametros.put("DT_ULT_RECEBTO", parametroRetorno.get("dataUltimoRecebimento").getAsString());
                                            }
                                            if (parametroRetorno.has("dtProximoContato")) {
                                                dadosParametros.put("DT_PROXIMO_CONTATO", parametroRetorno.get("dtProximoContato").getAsString());
                                            }
                                            if (parametroRetorno.has("atacadoVarejo")) {
                                                dadosParametros.put("ATACADO_VAREJO", parametroRetorno.get("atacadoVarejo").getAsString());
                                            }
                                            if (parametroRetorno.has("vistaPrazo")) {
                                                dadosParametros.put("VISTA_PRAZO", parametroRetorno.get("vistaPrazo").getAsString());
                                            }
                                            if (parametroRetorno.has("faturaVlMin")) {
                                                dadosParametros.put("FATURA_VL_MIN", parametroRetorno.get("faturaVlMin").getAsString());
                                            }
                                            if (parametroRetorno.has("parcelaEmAberto")) {
                                                dadosParametros.put("PARCELA_EM_ABERTO", parametroRetorno.get("parcelaEmAberto").getAsString());
                                            }
                                            if (parametroRetorno.has("capitaliza")) { dadosParametros.put("CAPITALIZA", parametroRetorno.get("capitaliza").getAsString()); }
                                            if (parametroRetorno.has("limite")) {
                                                dadosParametros.put("LIMITE", parametroRetorno.get("limite").getAsDouble());
                                            }
                                            if (parametroRetorno.has("descontoAtacadoVista")) {
                                                dadosParametros.put("DESC_ATAC_VISTA", parametroRetorno.get("descAtacVista").getAsDouble());
                                            }
                                            if (parametroRetorno.has("descontoAtacadoPrazo")) {
                                                dadosParametros.put("DESC_ATAC_PRAZO", parametroRetorno.get("descAtacPrazo").getAsDouble());
                                            }
                                            if (parametroRetorno.has("descontoVarejoVista")) {
                                                dadosParametros.put("DESC_VARE_VISTA", parametroRetorno.get("descVareVista").getAsDouble());
                                            }
                                            if (parametroRetorno.has("descontoVarejoPrazo")) {
                                                dadosParametros.put("DESC_VARE_PRAZO", parametroRetorno.get("descVarePrazo").getAsDouble());
                                            }
                                            dadosParametros.put("DESC_SERV_VISTA", parametroRetorno.get("descServVista").getAsDouble());
                                            dadosParametros.put("DESC_SERV_PRAZO", parametroRetorno.get("descServPrazo").getAsDouble());
                                            if (parametroRetorno.has("jurosDiario")) {
                                                dadosParametros.put("JUROS_DIARIO", parametroRetorno.get("jurosDiario").getAsDouble());
                                            }
                                            listaDadosStatus.add(dadosParametros);
                                        }
                                        ParametrosSql parametrosSql = new ParametrosSql(context);

                                        todosSucesso = parametrosSql.insertList(listaDadosStatus);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPARAM");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Parametros : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosParametros - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosFotos() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Imagens");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Imagens");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Checa se a configuracao eh para busca as imagens
            if (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S")) {

                if (textStatusErro != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatusErro.append("\n*" +context.getResources().getString(R.string.demorar_baixar_fotos));
                        }
                    });
                }
                // Pega quando foi a ultima data que recebeu dados
                String ultimaData = pegaUltimaDataAtualizacao("CFAFOTOS");
                // Cria uma variavel para salvar todos os paramentros em json
                String parametrosWebservice = "";
                String filtraFotos =
                        "(CFAFOTOS.ID_AEAPRODU IN (SELECT AEAPRODU.ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1'))";

                Gson gson = new Gson();
                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraFotos + "&size=50";
                } else {
                    parametrosWebservice += "&where= " + filtraFotos + "&size=50";
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        boolean todosSucesso = true;

                        JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                        if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                            final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                            int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();
                            if (pageNumber == 15){
                                Integer valueInt = pageNumber;
                            }
                            for (int ia = pageNumber; ia < totalPages; ia++) {

                                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                                // Verifica se retornou com sucesso
                                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                            }
                                        });
                                    }
                                    // Checa se retornou alguma coisa
                                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                        final JsonArray listaFotosRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                        // Checa se retornou algum dados na lista
                                        if (listaFotosRetorno.size() > 0) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_fotos));
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaFotosRetorno.size(), 0, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fotos));
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setIndeterminate(false);
                                                        progressBarStatus.setMax(listaFotosRetorno.size());
                                                    }
                                                });
                                            }
                                            List<ContentValues> listaDadosFotos = new ArrayList<ContentValues>();
                                            FotosSql fotosSql = new FotosSql(context);
                                            for (int i = 0; i < listaFotosRetorno.size(); i++) {
                                                // Atualiza a notificacao
                                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_fotos) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaFotosRetorno.size());
                                                mBuilder.setStyle(bigTextStyle)
                                                        .setProgress(listaFotosRetorno.size(), i, false);
                                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                                // Checo se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    final int finalI1 = i;
                                                    final int finalPageNumber = pageNumber + 1;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fotos) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaFotosRetorno.size());
                                                        }
                                                    });
                                                }
                                                if (progressBarStatus != null) {
                                                    final int finalI = i;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            progressBarStatus.setProgress(finalI);
                                                        }
                                                    });
                                                }
                                                JsonObject fotosRetorno = listaFotosRetorno.get(i).getAsJsonObject();
                                                final ContentValues dadosFotos = new ContentValues();

                                                if (fotosRetorno.has("foto")) {

                                                    dadosFotos.put("ID_CFAFOTOS", fotosRetorno.get("idCfafotos").getAsInt());
                                                    dadosFotos.put("DT_ALT", fotosRetorno.get("dtAlt").getAsString());
                                                    if (fotosRetorno.has("idCfaclifo") && fotosRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                        dadosFotos.put("ID_CFACLIFO", fotosRetorno.get("idCfaclifo").getAsInt());
                                                    }
                                                    if (fotosRetorno.has("idAeaprodu") && fotosRetorno.get("idAeaprodu").getAsInt() > 0) {
                                                        dadosFotos.put("ID_AEAPRODU", fotosRetorno.get("idAeaprodu").getAsInt());
                                                    }
                                                    if (fotosRetorno.has("foto")) {
                                                        byte[] tmp = new byte[fotosRetorno.getAsJsonArray("foto").size()];
                                                        for (int j = 0; j < fotosRetorno.getAsJsonArray("foto").size(); j++) {
                                                            tmp[j] = (byte) (((int) fotosRetorno.getAsJsonArray("foto").get(j).getAsInt()) & 0xFF);
                                                        }
                                                        dadosFotos.put("FOTO", tmp);
                                                    }
                                                    //listaDadosFotos.add(dadosFotos);

                                                    if (fotosSql.insertOrReplace(dadosFotos) < 1){
                                                        if (textStatusErro != null) {
                                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    textStatusErro.append("\n*" + context.getResources().getText(R.string.nao_conseguimos_inserir_foto) + ": " + dadosFotos.getAsInteger("ID_CFAFOTOS"));
                                                                }
                                                            });
                                                        }
                                                        todosSucesso = false;
                                                    }
                                                    dadosFotos.clear();
                                                    fotosRetorno = null;
                                                }
                                            }
                                            //FotosSql fotosSql = new FotosSql(context);

                                            //todosSucesso = fotosSql.insertList(listaDadosFotos);
                                        }
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, true);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());


                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                    } else {
                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                                .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                    }
                                    // Esvazia memoria
                                    retornoWebservice = null;
                                    // Incrementa o total de paginas
                                    pageNumber++;
                                    if (pageNumber < totalPages) {
                                        retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                    }
                                } else {
                                    todosSucesso = false;

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                            } // Fim do for ia (page)
                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("CFAFOTOS");
                            }
                        }
                    } else {
                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                    }
                } else {
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_fotos) + "\n - As Fotos voltou vazia do Servidor.");
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final JsonParseException e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Fotos : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                        .bigText("ImportarDadosFotos - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Fotos : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                    .bigText("ImportarDadosFotos - \n" + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

        }
    }


    private void importarDadosPlanoPagamento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Plano de Pagamento");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Plano de Pagamento");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPLPGT");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraPlanoPagamento =
                    "(AEAPLPGT.ATIVO = '1') AND (AEAPLPGT.ENVIA_PALM = '1')";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraPlanoPagamento;
            } else {
                parametrosWebservice += "&where= " + filtraPlanoPagamento;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPlanoPagamentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPlanoPagamentoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaPlanoPagamentoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaPlanoPagamentoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosPagamento = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaPlanoPagamentoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPlanoPagamentoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaPlanoPagamentoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPlanoPagamentoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject pagamentoRetorno = listaPlanoPagamentoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosPagamento = new ContentValues();

                                            dadosPagamento.put("ID_AEAPLPGT", pagamentoRetorno.get("idAeaplpgt").getAsInt());
                                            dadosPagamento.put("ID_SMAEMPRE", pagamentoRetorno.get("idSmaempre").getAsInt());
                                            if (pagamentoRetorno.has("idCfamoeda")) {
                                                dadosPagamento.put("ID_CFAMOEDA", pagamentoRetorno.get("idCfamoeda").getAsString());
                                            }
                                            if (pagamentoRetorno.has("idCfatpdoc")) {
                                                dadosPagamento.put("ID_CFATPDOC", pagamentoRetorno.get("idCfatpdoc").getAsString());
                                            }
                                            if (pagamentoRetorno.has("idAeaplpgtEquivalente")) {
                                                dadosPagamento.put("ID_AEAPLPGT_EQUIVALENTE", pagamentoRetorno.get("idAeaplpgtEquivalente").getAsString());
                                            }
                                            if (pagamentoRetorno.has("idAeaforma")) {
                                                dadosPagamento.put("ID_AEAFORMA", pagamentoRetorno.get("idAeaforma").getAsString());
                                            }
                                            dadosPagamento.put("GUID", pagamentoRetorno.get("guid").getAsString());
                                            dadosPagamento.put("US_CAD", pagamentoRetorno.get("usCad").getAsString());
                                            dadosPagamento.put("DT_CAD", pagamentoRetorno.get("dtCad").getAsString());
                                            dadosPagamento.put("DT_ALT", pagamentoRetorno.get("dtAlt").getAsString());
                                            dadosPagamento.put("CODIGO", pagamentoRetorno.get("codigo").getAsInt());
                                            dadosPagamento.put("DESCRICAO", pagamentoRetorno.get("descricao").getAsString());
                                            dadosPagamento.put("ATIVO", pagamentoRetorno.get("ativo").getAsString());
                                            if (pagamentoRetorno.has("origemValor")) {
                                                dadosPagamento.put("ORIGEM_VALOR", pagamentoRetorno.get("origemValor").getAsString());
                                            }
                                            dadosPagamento.put("ATAC_VAREJO", pagamentoRetorno.get("atacVarejo").getAsString());
                                            dadosPagamento.put("VISTA_PRAZO", pagamentoRetorno.get("vistaPrazo").getAsString());
                                            dadosPagamento.put("COMISSAO", pagamentoRetorno.get("comissao").getAsString());
                                            dadosPagamento.put("PERC_DESC_COMISSAO", pagamentoRetorno.get("percDescComissao").getAsDouble());
                                            dadosPagamento.put("PERC_DESC_ATAC", pagamentoRetorno.get("percDescAtac").getAsDouble());
                                            dadosPagamento.put("PERC_DESC_VARE", pagamentoRetorno.get("percDescVare").getAsDouble());
                                            dadosPagamento.put("PERC_DESC_SERV", pagamentoRetorno.get("percDescServ").getAsDouble());
                                            if (pagamentoRetorno.has("descPromocao")) {
                                                dadosPagamento.put("DESC_PROMOCAO", pagamentoRetorno.get("descPromocao").getAsString());
                                            }
                                            dadosPagamento.put("PERC_ENTRADA", pagamentoRetorno.get("percEntrada").getAsDouble());
                                            dadosPagamento.put("QTDE_PARCELAS1", pagamentoRetorno.get("qtdeParcelas1").getAsInt());
                                            dadosPagamento.put("DIAS_ENTRADA", pagamentoRetorno.get("diasEntrada").getAsInt());
                                            dadosPagamento.put("QTDE_PARCELAS2", pagamentoRetorno.get("qtdeParcelas2").getAsInt());
                                            dadosPagamento.put("DIAS_PARCELAS2", pagamentoRetorno.get("diasParcelas2").getAsInt());
                                            dadosPagamento.put("QTDE_PARCELAS3", pagamentoRetorno.get("qtdeParcelas3").getAsInt());
                                            dadosPagamento.put("DIAS_PARCELAS3", pagamentoRetorno.get("diasParcelas3").getAsInt());
                                            if (pagamentoRetorno.has("juroMedioAtac")) {
                                                dadosPagamento.put("JURO_MEDIO_ATAC", pagamentoRetorno.get("juroMedioAtac").getAsDouble());
                                            }
                                            if (pagamentoRetorno.has("juroMedioVare")) {
                                                dadosPagamento.put("JURO_MEDIO_VARE", pagamentoRetorno.get("juroMedioVare").getAsDouble());
                                            }
                                            if (pagamentoRetorno.has("juroMedioServ")) {
                                                dadosPagamento.put("JURO_MEDIO_SERV", pagamentoRetorno.get("juroMedioServ").getAsDouble());
                                            }
                                            if (pagamentoRetorno.has("juroMedioLocal")) {
                                                dadosPagamento.put("JURO_MEDIO_LOCAL", pagamentoRetorno.get("juroMedioLocal").getAsString());
                                            }
                                            if (pagamentoRetorno.has("temRegras")) {
                                                dadosPagamento.put("TEM_REGRAS", pagamentoRetorno.get("temRegras").getAsInt());
                                            }
                                            if (pagamentoRetorno.has("enviaPalm")) {
                                                dadosPagamento.put("ENVIA_PALM", pagamentoRetorno.get("enviaPalm").getAsString());
                                            }
                                            listaDadosPagamento.add(dadosPagamento);
                                        }
                                        PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                                        todosSucesso = planoPagamentoSql.insertList(listaDadosPagamento);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } //Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLPGT");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Plano de Pagamento : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosPlanoPagamento - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosClasseProdutos() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Classe de Produto");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Classe de Produto");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEACLASE");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClasseRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClasseRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_classe_produto));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaClasseRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_classe_produto));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaClasseRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosClasse = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaClasseRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClasseRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaClasseRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaClasseRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject classeRetorno = listaClasseRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosClasse = new ContentValues();

                                            dadosClasse.put("ID_AEACLASE", classeRetorno.get("idAeaclase").getAsInt());
                                            dadosClasse.put("CODIGO", classeRetorno.get("codigo").getAsInt());
                                            dadosClasse.put("DT_ALT", classeRetorno.get("dtAlt").getAsString());
                                            dadosClasse.put("DESCRICAO", classeRetorno.get("descricao").getAsString());

                                            listaDadosClasse.add(dadosClasse);
                                        }
                                        ClasseSql classeSql = new ClasseSql(context);

                                        todosSucesso = classeSql.insertList(listaDadosClasse);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACLASE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Classe de Produtos : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosClasseProdutos - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosUnidadeVenda() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Unidade de Venda");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Unidade de Venda");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAUNVEN");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaUnidadeVendaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaUnidadeVendaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_unidade_venda));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaUnidadeVendaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_unidade_venda));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaUnidadeVendaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosUnidade = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaUnidadeVendaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaUnidadeVendaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaUnidadeVendaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaUnidadeVendaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject unidadeRetorno = listaUnidadeVendaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosUnidade = new ContentValues();

                                            dadosUnidade.put("ID_AEAUNVEN", unidadeRetorno.get("idAeaunven").getAsInt());
                                            dadosUnidade.put("DT_ALT", unidadeRetorno.get("dtAlt").getAsString());
                                            dadosUnidade.put("DESCRICAO_SINGULAR", unidadeRetorno.get("descricaoSingular").getAsString());
                                            dadosUnidade.put("SIGLA", unidadeRetorno.get("sigla").getAsString());
                                            dadosUnidade.put("DECIMAIS", unidadeRetorno.get("decimais").getAsInt());
                                            listaDadosUnidade.add(dadosUnidade);
                                        }
                                        UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);

                                        todosSucesso = unidadeVendaSql.insertList(listaDadosUnidade);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } //Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAUNVEN");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Unidade de Venda : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosUnidadeVenda - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosGrade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Grade de Produto");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Grade de Produto");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAGRADE");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaGradeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaGradeRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_grade));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaGradeRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_grade));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaGradeRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosGrade = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaGradeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_grade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaGradeRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaGradeRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_grade) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaGradeRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject gradeRetorno = listaGradeRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosGrade = new ContentValues();

                                            dadosGrade.put("ID_AEAGRADE", gradeRetorno.get("idAeagrade").getAsInt());
                                            dadosGrade.put("ID_AEATPGRD", gradeRetorno.get("idAeatpgrd").getAsInt());
                                            dadosGrade.put("DT_ALT", gradeRetorno.get("dtAlt").getAsString());
                                            dadosGrade.put("DESCRICAO", gradeRetorno.get("descricao").getAsString());
                                            listaDadosGrade.add(dadosGrade);
                                        }
                                        GradeSql gradeSql = new GradeSql(context);

                                        todosSucesso = gradeSql.insertList(listaDadosGrade);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAGRADE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Grade de Produtos : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosGrade - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosMarca() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Marca");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Marca");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAMARCA");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaMarcaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaMarcaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_marca));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaMarcaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_marca));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaMarcaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosMarca = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaMarcaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_marca) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaMarcaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaMarcaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_marca) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaMarcaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject marcaRetorno = listaMarcaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosMarca = new ContentValues();

                                            dadosMarca.put("ID_AEAMARCA", marcaRetorno.get("idAeamarca").getAsInt());
                                            dadosMarca.put("DT_ALT", marcaRetorno.get("dtAlt").getAsString());
                                            dadosMarca.put("DESCRICAO", marcaRetorno.get("descricao").getAsString());

                                            listaDadosMarca.add(dadosMarca);
                                        }
                                        MarcaSql marcaSql = new MarcaSql(context);

                                        todosSucesso = marcaSql.insertList(listaDadosMarca);


                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAMARCA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Marca : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosMarca - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosCodigoSituacaoTributaria() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Situação Tributária");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Situacao Tributaria");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEACODST");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTributariaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTributariaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaTributariaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaTributariaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosSituacao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaTributariaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTributariaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaTributariaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaTributariaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject situacaoRetorno = listaTributariaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosSituacao = new ContentValues();

                                            dadosSituacao.put("ID_AEACODST", situacaoRetorno.get("idAeacodst").getAsInt());
                                            dadosSituacao.put("DT_ALT", situacaoRetorno.get("dtAlt").getAsString());
                                            dadosSituacao.put("CODIGO", situacaoRetorno.get("codigo").getAsString());
                                            dadosSituacao.put("DESCRICAO", situacaoRetorno.get("descricao").getAsString());
                                            dadosSituacao.put("TIPO", situacaoRetorno.get("tipo").getAsString());
                                            dadosSituacao.put("ORIGEM", situacaoRetorno.get("origem").getAsString());

                                            listaDadosSituacao.add(dadosSituacao);
                                        }

                                        SituacaoTributariaSql situacaoTributariaSql = new SituacaoTributariaSql(context);

                                        todosSucesso = situacaoTributariaSql.insertList(listaDadosSituacao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // FIm do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACODST");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Situação Trib. : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosSituacaoTributaria - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosProduto() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Produto");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Produto");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPRODU");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            //String filtraProduto = "(AEAPRODU.ATIVO = '1')";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            } else {
                parametrosWebservice += "&where= (ATIVO = '1') ";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_produto));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaProdutoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaProdutoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosProduto = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaProdutoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_produto) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaProdutoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaProdutoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject produtoRetorno = listaProdutoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosProduto = new ContentValues();

                                            dadosProduto.put("ID_AEAPRODU", produtoRetorno.get("idAeaprodu").getAsInt());
                                            if (produtoRetorno.has("idAeafamil") && produtoRetorno.get("idAeafamil").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEAFAMIL", produtoRetorno.get("idAeafamil").getAsInt());
                                            }
                                            if (produtoRetorno.has("idAeaclase") && produtoRetorno.get("idAeaclase").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEACLASE", produtoRetorno.get("idAeaclase").getAsInt());
                                            }
                                            if (produtoRetorno.has("idAeagrupo") && produtoRetorno.get("idAeagrupo").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEAGRUPO", produtoRetorno.get("idAeagrupo").getAsInt());
                                            }
                                            if (produtoRetorno.has("idAeasgrup") && produtoRetorno.get("idAeasgrup").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEASGRUP", produtoRetorno.get("idAeasgrup").getAsInt());
                                            }
                                            if (produtoRetorno.has("idAeamarca") && produtoRetorno.get("idAeamarca").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEAMARCA", produtoRetorno.get("idAeamarca").getAsInt());
                                            }
                                            if (produtoRetorno.has("idAeaunven")) {
                                                //JsonObject unidade = produtoRetorno.getAsJsonObject("unidadeVendaProduto");
                                                dadosProduto.put("ID_AEAUNVEN", produtoRetorno.get("idAeaunven").getAsInt());
                                            }
                                            dadosProduto.put("DT_CAD", produtoRetorno.get("dtCad").getAsString());
                                            dadosProduto.put("DT_ALT", produtoRetorno.get("dtAlt").getAsString());
                                            dadosProduto.put("DESCRICAO", produtoRetorno.get("descricao").getAsString());
                                            if (produtoRetorno.has("descricaoAuxiliar")) {
                                                dadosProduto.put("DESCRICAO_AUXILIAR", produtoRetorno.get("descricaoAuxiliar").getAsString());
                                            }
                                            dadosProduto.put("CODIGO_ESTRUTURAL", produtoRetorno.get("codigoEstrutural").getAsString());
                                            if (produtoRetorno.has("referencia")) {
                                                dadosProduto.put("REFERENCIA", produtoRetorno.get("referencia").getAsString());
                                            }
                                            if (produtoRetorno.has("codigoBarras")) {
                                                dadosProduto.put("CODIGO_BARRAS", produtoRetorno.get("codigoBarras").getAsString());
                                            }
                                            if (produtoRetorno.has("pesoLiquido")) {
                                                dadosProduto.put("PESO_LIQUIDO", produtoRetorno.get("pesoLiquido").getAsDouble());
                                            }
                                            if (produtoRetorno.has("pesoBruto")) {
                                                dadosProduto.put("PESO_BRUTO", produtoRetorno.get("pesoBruto").getAsDouble());
                                            }
                                            dadosProduto.put("ATIVO", produtoRetorno.get("ativo").getAsString());
                                            dadosProduto.put("TIPO", produtoRetorno.get("tipo").getAsString());

                                            listaDadosProduto.add(dadosProduto);
                                        }
                                        ProdutoSql produtoSql = new ProdutoSql(context);

                                        todosSucesso = produtoSql.insertList(listaDadosProduto);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRODU");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Produto : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosProduto - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosPreco() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Preço");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Preço");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPRECO");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "(AEAPRECO.ID_AEAPRODU IN (SELECT ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1'))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPrecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPrecoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_preco));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaPrecoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_preco));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaPrecoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosPreco = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaPrecoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_preco) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPrecoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaPrecoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_preco) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPrecoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject precoRetorno = listaPrecoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosPreco = new ContentValues();

                                            dadosPreco.put("ID_AEAPRECO", precoRetorno.get("idAeapreco").getAsInt());
                                            dadosPreco.put("ID_AEAPRODU", precoRetorno.get("idAeaprodu").getAsInt());
                                            if (precoRetorno.has("idCfaclifo") && precoRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                dadosPreco.put("ID_CFACLIFO", precoRetorno.get("idCfaclifo").getAsInt());
                                            }
                                            if (precoRetorno.has("idAeaplpgt") && precoRetorno.get("idAeaplpgt").getAsInt() > 0) {
                                                dadosPreco.put("ID_AEAPLPGT", precoRetorno.get("idAeaplpgt").getAsInt());
                                            }
                                            if (precoRetorno.has("dtAlt")) {
                                                dadosPreco.put("DT_ALT", precoRetorno.get("dtAlt").getAsString());
                                            }
                                            dadosPreco.put("VENDA_ATAC", precoRetorno.get("vendaAtac").getAsString());
                                            dadosPreco.put("VENDA_VARE", precoRetorno.get("vendaVare").getAsString());

                                            listaDadosPreco.add(dadosPreco);
                                        }
                                        PrecoSql precoSql = new PrecoSql(context);

                                        todosSucesso = precoSql.insertList(listaDadosPreco);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100) + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_preco)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_preco))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRECO");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Preço : " + e.getMessage());
                    }
                });
            }
            // Cria uma notificacao para ser manipulado
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosPreco - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosEmbalagem() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Embalagem de Produto");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Embalagem de Produto");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAEMBAL");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "(AEAEMBAL.ATIVO = '1') AND " +
                            "(AEAEMBAL.ID_AEAPRODU IN (SELECT ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1'))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEmbalagemRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEmbalagemRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_embalagem));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaEmbalagemRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_embalagem));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaEmbalagemRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosEmbalagem = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaEmbalagemRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEmbalagemRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaEmbalagemRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaEmbalagemRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject embalagemRetorno = listaEmbalagemRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosEmbalagem = new ContentValues();

                                            dadosEmbalagem.put("ID_AEAEMBAL", embalagemRetorno.get("idAeaembal").getAsInt());
                                            if (embalagemRetorno.has("idAeaprodu") && embalagemRetorno.get("idAeaprodu").getAsInt() > 0) {
                                                dadosEmbalagem.put("ID_AEAPRODU", embalagemRetorno.get("idAeaprodu").getAsInt());
                                            }
                                            if (embalagemRetorno.has("idAeaunven") && embalagemRetorno.get("idAeaunven").getAsInt() > 0) {
                                                dadosEmbalagem.put("ID_AEAUNVEN", embalagemRetorno.get("idAeaunven").getAsInt());
                                            }
                                            dadosEmbalagem.put("DT_ALT", embalagemRetorno.get("dtAlt").getAsString());
                                            if (embalagemRetorno.has("principal")) {
                                                dadosEmbalagem.put("PRINCIPAL", embalagemRetorno.get("principal").getAsString());
                                            }
                                            if (embalagemRetorno.has("descricao")) {
                                                dadosEmbalagem.put("DESCRICAO", embalagemRetorno.get("descricao").getAsString());
                                            }
                                            if (embalagemRetorno.has("fatorConversao")) {
                                                dadosEmbalagem.put("FATOR_CONVERSAO", embalagemRetorno.get("fatorConversao").getAsDouble());
                                            }
                                            if (embalagemRetorno.has("fatorPreco")) {
                                                dadosEmbalagem.put("FATOR_PRECO", embalagemRetorno.get("fatorPreco").getAsDouble());
                                            }
                                            if (embalagemRetorno.has("modulo")) {
                                                dadosEmbalagem.put("MODULO", embalagemRetorno.get("modulo").getAsInt());
                                            }
                                            if (embalagemRetorno.has("decimais")) {
                                                dadosEmbalagem.put("DECIMAIS", embalagemRetorno.get("decimais").getAsInt());
                                            }
                                            if (embalagemRetorno.has("ativo")) {
                                                dadosEmbalagem.put("ATIVO", embalagemRetorno.get("ativo").getAsString());
                                            }
                                            listaDadosEmbalagem.add(dadosEmbalagem);
                                        }
                                        EmbalagemSql embalagemSql = new EmbalagemSql(context);

                                        todosSucesso = embalagemSql.insertList(listaDadosEmbalagem);


                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } //Fim for ia (pager)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAEMBAL");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Embalagem : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosEmbalagem - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosProdutosPorLoja() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Produto por Loja");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Produto por Loja");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPLOJA");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "(AEAPLOJA.ATIVO = '1') AND \n" +
                            "(AEAPLOJA.ID_AEAPRODU IN (SELECT ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1')) \n" +
                            "AND (AEAPLOJA.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") ";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {

                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {
                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoLojaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoLojaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - Parte " + (pageNumber + 1) + "/" + totalPages);
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaProdutoLojaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto_loja));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaProdutoLojaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosProdutoLoja = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaProdutoLojaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoLojaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaProdutoLojaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaProdutoLojaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject produtoLojaRetorno = listaProdutoLojaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosProdutoLoja = new ContentValues();

                                            dadosProdutoLoja.put("ID_AEAPLOJA", produtoLojaRetorno.get("idAeaploja").getAsInt());
                                            dadosProdutoLoja.put("ID_SMAEMPRE", produtoLojaRetorno.get("idSmaempre").getAsInt());
                                            dadosProdutoLoja.put("ID_AEAPRODU", produtoLojaRetorno.get("idAeaprodu").getAsInt());
                                            dadosProdutoLoja.put("ID_AEACODST", produtoLojaRetorno.get("idAeacodst").getAsInt());
                                            dadosProdutoLoja.put("DT_ALT", produtoLojaRetorno.get("dtAlt").getAsString());
                                            dadosProdutoLoja.put("ESTOQUE_F", produtoLojaRetorno.get("estoqueF").getAsDouble());
                                            dadosProdutoLoja.put("ESTOQUE_C", produtoLojaRetorno.get("estoqueC").getAsDouble());
                                            if (produtoLojaRetorno.has("retido")) {
                                                dadosProdutoLoja.put("RETIDO", produtoLojaRetorno.get("retido").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("pedido")) {
                                                dadosProdutoLoja.put("PEDIDO", produtoLojaRetorno.get("pedido").getAsDouble());
                                            }
                                            dadosProdutoLoja.put("ATIVO", produtoLojaRetorno.get("ativo").getAsString());
                                            if (produtoLojaRetorno.has("dtEntradaD")) {
                                                dadosProdutoLoja.put("DT_ENTRADA_D", produtoLojaRetorno.get("dtEntradaD").getAsString());
                                            }
                                            if (produtoLojaRetorno.has("dtEntradaN")) {
                                                dadosProdutoLoja.put("DT_ENTRADA_N", produtoLojaRetorno.get("dtEntradaN").getAsString());
                                            }
                                            if (produtoLojaRetorno.has("dtReajusteVare")) {
                                                dadosProdutoLoja.put("DT_REAJUSTE_VARE", produtoLojaRetorno.get("dtReajusteVare").getAsString());
                                            }
                                            if (produtoLojaRetorno.has("dtReajusteAtac")) {
                                                dadosProdutoLoja.put("DT_REAJUSTE_ATAC", produtoLojaRetorno.get("dtReajusteAtac").getAsString());
                                            }
                                            if (produtoLojaRetorno.has("ctReposicaoN")) {
                                                dadosProdutoLoja.put("CT_REPOSICAO_N", produtoLojaRetorno.get("ctReposicaoN").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("ctCompletoN")) {
                                                dadosProdutoLoja.put("CT_COMPLETO_N", produtoLojaRetorno.get("ctCompletoN").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("ctRealN")) {
                                                dadosProdutoLoja.put("CT_REAL_N", produtoLojaRetorno.get("ctRealN").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("ctMedioN")) {
                                                dadosProdutoLoja.put("CT_MEDIO_N", produtoLojaRetorno.get("ctMedioN").getAsDouble());
                                            }
                                            dadosProdutoLoja.put("VENDA_ATAC", produtoLojaRetorno.get("vendaAtac").getAsDouble());
                                            dadosProdutoLoja.put("VENDA_VARE", produtoLojaRetorno.get("vendaVare").getAsDouble());
                                            if (produtoLojaRetorno.has("promocaoAtacVista")) {
                                                dadosProdutoLoja.put("PROMOCAO_ATAC_VISTA", produtoLojaRetorno.get("promocaoAtacVista").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("promocaoAtacPrazo")) {
                                                dadosProdutoLoja.put("PROMOCAO_ATAC_PRAZO", produtoLojaRetorno.get("promocaoAtacPrazo").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("promocaoVareVista")) {
                                                dadosProdutoLoja.put("PROMOCAO_VARE_VISTA", produtoLojaRetorno.get("promocaoVareVista").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("promocaoVarePrazo")) {
                                                dadosProdutoLoja.put("PROMOCAO_VARE_PRAZO", produtoLojaRetorno.get("promocaoVarePrazo").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("precoMinimoAtacado")) {
                                                dadosProdutoLoja.put("PRECO_MINIMO_ATAC", produtoLojaRetorno.get("precoMinimoAtac").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("precoMinimoVarejo")) {
                                                dadosProdutoLoja.put("PRECO_MINIMO_VARE", produtoLojaRetorno.get("precoMinimoVare").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("precoMaximoAtacado")) {
                                                dadosProdutoLoja.put("PRECO_MAXIMO_ATAC", produtoLojaRetorno.get("precoMaximoAtac").getAsDouble());
                                            }
                                            if (produtoLojaRetorno.has("precoMaximoVarejo")) {
                                                dadosProdutoLoja.put("PRECO_MAXIMO_VARE", produtoLojaRetorno.get("precoMaximoVare").getAsDouble());
                                            }
                                            listaDadosProdutoLoja.add(dadosProdutoLoja);
                                        }
                                        ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                                        todosSucesso = produtoLojaSql.insertList(listaDadosProdutoLoja);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLOJA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Produtos por Loja : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosProdutoPorLoja - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosLocalEstoque() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Local de Estoque");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Local de Estoque");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEALOCES");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "(AEALOCES.ATIVO = '1') AND " +
                            "(AEALOCES.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") ";


            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaLocalEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaLocalEstoqueRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_local_estoque));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaLocalEstoqueRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_local_estoque));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaLocalEstoqueRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosLocal = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaLocalEstoqueRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaLocalEstoqueRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaLocalEstoqueRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaLocalEstoqueRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject localRetorno = listaLocalEstoqueRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosLocalEstoque = new ContentValues();

                                            dadosLocalEstoque.put("ID_AEALOCES", localRetorno.get("idAealoces").getAsInt());
                                            dadosLocalEstoque.put("ID_SMAEMPRE", localRetorno.get("idSmaempre").getAsInt());
                                            dadosLocalEstoque.put("DT_ALT", localRetorno.get("dtAlt").getAsString());
                                            dadosLocalEstoque.put("CODIGO", localRetorno.get("codigo").getAsInt());
                                            dadosLocalEstoque.put("DESCRICAO", localRetorno.get("descricao").getAsString());
                                            dadosLocalEstoque.put("ATIVO", localRetorno.get("ativo").getAsString());
                                            dadosLocalEstoque.put("TIPO_VENDA", localRetorno.get("tipoVenda").getAsString());
                                            listaDadosLocal.add(dadosLocalEstoque);
                                        }
                                        LocacaoSql locacaoSql = new LocacaoSql(context);

                                        todosSucesso = locacaoSql.insertList(listaDadosLocal);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEALOCES");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Local de Estoque : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosLocalEstoque - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosEstoque() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Estoque de Produto");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Estoque de Produto");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAESTOQ");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "(AEAESTOQ.ATIVO = '1') AND " +
                            "(AEAESTOQ.ID_AEALOCES IN (SELECT AEALOCES.ID_AEALOCES FROM AEALOCES WHERE (AEALOCES.ATIVO = '1') AND (AEALOCES.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEstoqueRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_estoque));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaEstoqueRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estoque));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaEstoqueRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosEstoque = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaEstoqueRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_estoque) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEstoqueRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaEstoqueRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estoque) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaEstoqueRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject estoqueRetorno = listaEstoqueRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosEstoque = new ContentValues();

                                            dadosEstoque.put("ID_AEAESTOQ", estoqueRetorno.get("idAeaestoq").getAsInt());
                                            dadosEstoque.put("ID_AEAPLOJA", estoqueRetorno.get("idAeaploja").getAsInt());
                                            if (estoqueRetorno.has("idAealoces") && estoqueRetorno.get("idAealoces").getAsInt() > 0) {
                                                dadosEstoque.put("ID_AEALOCES", estoqueRetorno.get("idAealoces").getAsInt());
                                            }
                                            dadosEstoque.put("DT_ALT", estoqueRetorno.get("dtAlt").getAsString());
                                            dadosEstoque.put("ESTOQUE", estoqueRetorno.get("estoque").getAsDouble());
                                            dadosEstoque.put("RETIDO", estoqueRetorno.get("retido").getAsDouble());
                                            dadosEstoque.put("ATIVO", estoqueRetorno.get("ativo").getAsString());

                                            listaDadosEstoque.add(dadosEstoque);
                                        }
                                        EstoqueSql estoqueSql = new EstoqueSql(context);

                                        todosSucesso = estoqueSql.insertList(listaDadosEstoque);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAESTOQ");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Estoque : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosEstoque - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosOrcamento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Orçamento");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Orçamento");
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
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEASAIDA_ORC");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();

            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);
            String [] listaTipo = new String[]{ OrcamentoRotinas.PEDIDO_ENVIADO,
                    OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                    OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO};

            listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, null, OrcamentoRotinas.ORDEM_DECRESCENTE);

            if ( (listaOrcamento != null) && (listaOrcamento.size() > 0) ) {
                String whereGuidOrcamento = "(GUID IN (";
                int controle = 0;
                for (OrcamentoBeans orcamento : listaOrcamento) {
                    controle++;
                    whereGuidOrcamento += "'" + orcamento.getGuid() + "'";
                    if (controle < listaOrcamento.size()) {
                        whereGuidOrcamento += ", ";
                    }
                }
                whereGuidOrcamento += ") )";

                if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                    parametrosWebservice += "&where= ( (DT_ALT >= '" + ultimaData + "') AND " + whereGuidOrcamento + ")";
                } else {
                    parametrosWebservice += "&where= " + whereGuidOrcamento;
                }
                if (!parametrosWebservice.isEmpty()) {
                    Gson gson = new Gson();
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                    JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                    if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                        statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                        if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                            boolean todosSucesso = true;

                            JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                            if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                                final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                                int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                                for (int ia = pageNumber; ia < totalPages; ia++) {

                                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                                    // Verifica se retornou com sucesso
                                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        mBuilder.setStyle(bigTextStyle);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                                }
                                            });
                                        }
                                        // Checa se retornou alguma coisa
                                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                            final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                            // Checa se retornou algum dados na lista
                                            if (listaPedidoRetorno.size() > 0) {
                                                // Atualiza a notificacao
                                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_orcamento));
                                                mBuilder.setStyle(bigTextStyle)
                                                        .setProgress(listaPedidoRetorno.size(), 0, false);
                                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                                // Checo se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento));
                                                        }
                                                    });
                                                }
                                                if (progressBarStatus != null) {
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            progressBarStatus.setIndeterminate(false);
                                                            progressBarStatus.setMax(listaPedidoRetorno.size());
                                                        }
                                                    });
                                                }

                                                for (int i = 0; i < listaPedidoRetorno.size(); i++) {
                                                    // Atualiza a notificacao
                                                    bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPedidoRetorno.size());
                                                    mBuilder.setStyle(bigTextStyle)
                                                            .setProgress(listaPedidoRetorno.size(), i, false);
                                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                                    // Checo se o texto de status foi passado pro parametro
                                                    if (textStatus != null) {
                                                        final int finalI1 = i;
                                                        final int finalPageNumber = pageNumber + 1;
                                                        ((Activity) context).runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPedidoRetorno.size());
                                                            }
                                                        });
                                                    }
                                                    if (progressBarStatus != null) {
                                                        final int finalI = i;
                                                        ((Activity) context).runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                progressBarStatus.setProgress(finalI);
                                                            }
                                                        });
                                                    }
                                                    JsonObject pedidoRetorno = listaPedidoRetorno.get(i).getAsJsonObject();
                                                    ContentValues dadosOrcamento = new ContentValues();
                                                    dadosOrcamento.put("ID_SMAEMPRE", pedidoRetorno.get("idSmaempre").getAsInt());
                                                    if (pedidoRetorno.has("idCfaclifo") && pedidoRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                        dadosOrcamento.put("ID_CFACLIFO", pedidoRetorno.get("idCfaclifo").getAsInt());
                                                    }
                                                    if (pedidoRetorno.has("idCfaestad") && pedidoRetorno.get("idCfaestad").getAsInt() > 0) {
                                                        dadosOrcamento.put("ID_CFAESTAD", pedidoRetorno.get("idCfaestad").getAsInt());
                                                    }
                                                    if (pedidoRetorno.has("idCfacidad") && pedidoRetorno.get("idCfacidad").getAsInt() > 0) {
                                                        dadosOrcamento.put("ID_CFACIDAD", pedidoRetorno.get("idCfacidad").getAsInt());
                                                    }
                                                    if (pedidoRetorno.has("idAearoman") && pedidoRetorno.get("idAearoman").getAsInt() > 0) {
                                                        dadosOrcamento.put("ID_AEAROMAN", pedidoRetorno.get("idAearoman").getAsInt());
                                                    }
                                                    if (pedidoRetorno.has("idCfatpdoc") && pedidoRetorno.get("idCfatpdoc").getAsInt() > 0) {
                                                        dadosOrcamento.put("ID_CFATPDOC", pedidoRetorno.get("idCfatpdoc").getAsInt());
                                                    }
                                                    //dadosOrcamento.put("GUID", pedidoRetorno.get("guid").getAsString());
                                                    //dadosOrcamento.put("NUMERO", pedidoRetorno.get("numero").getAsInt());
                                                    if (pedidoRetorno.has("vlFrete")) {
                                                        dadosOrcamento.put("VL_FRETE", pedidoRetorno.get("vlFrete").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlSeguro")) {
                                                        dadosOrcamento.put("VL_SEGURO", pedidoRetorno.get("vlSeguro").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlOutros")) {
                                                        dadosOrcamento.put("VL_OUTROS", pedidoRetorno.get("vlOutros").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlEncargosFinanceiros")) {
                                                        dadosOrcamento.put("VL_ENCARGOS_FINANCEIROS", pedidoRetorno.get("vlEncargosFinanceiros").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlTabelaFaturado")) {
                                                        dadosOrcamento.put("VL_TABELA_FATURADO", pedidoRetorno.get("vlTabelaFaturado").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("fcVlTotalFaturado")) {
                                                        dadosOrcamento.put("FC_VL_TOTAL_FATURADO", pedidoRetorno.get("fcVlTotalFaturado").getAsDouble());
                                                    }
                                                    dadosOrcamento.put("ATAC_VAREJO", pedidoRetorno.get("atacVarejo").getAsString());
                                                    if (pedidoRetorno.has("pessoaCliente")) {
                                                        dadosOrcamento.put("PESSOA_CLIENTE", pedidoRetorno.get("pessoaCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("nomeCliente")) {
                                                        dadosOrcamento.put("NOME_CLIENTE", pedidoRetorno.get("nomeCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("ieRgCliente")) {
                                                        dadosOrcamento.put("IE_RG_CLIENTE", pedidoRetorno.get("ieRgCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("cpfCgcCliente")) {
                                                        dadosOrcamento.put("CPF_CGC_CLIENTE", pedidoRetorno.get("cpfCgcCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("enderecoCliente")) {
                                                        dadosOrcamento.put("ENDERECO_CLIENTE", pedidoRetorno.get("enderecoCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("bairroCliente")) {
                                                        dadosOrcamento.put("BAIRRO_CLIENTE", pedidoRetorno.get("bairroCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("cepCliente")) {
                                                        dadosOrcamento.put("CEP_CLIENTE", pedidoRetorno.get("cepCliente").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("obs")) {
                                                        dadosOrcamento.put("OBS", pedidoRetorno.get("obs").getAsString());
                                                    }
                                                    if (pedidoRetorno.has("andamento")) {
                                                        String situacao = pedidoRetorno.get("andamento").getAsString();

                                                        if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("3")) {
                                                            // Marca o status como retorno liberado
                                                            dadosOrcamento.put("STATUS", "RL");

                                                        } else if (situacao.equalsIgnoreCase("1")) {
                                                            // Marca o peiddo como enviado
                                                            dadosOrcamento.put("STATUS", "N");

                                                        } else if (situacao.equalsIgnoreCase("X") || situacao.equalsIgnoreCase("2")) {
                                                            // Marca o status como retorno como excluido ou bloqueado
                                                            dadosOrcamento.put("STATUS", "RB");

                                                        } else if (situacao.equalsIgnoreCase("7")) {
                                                            // Marca o status como retorno como conferido
                                                            dadosOrcamento.put("STATUS", "C");

                                                        } else if (situacao.equalsIgnoreCase("8") || situacao.equalsIgnoreCase("9") ||
                                                                situacao.equalsIgnoreCase("A") || situacao.equalsIgnoreCase("B")) {
                                                            // Marca o status como retorno como faturado
                                                            dadosOrcamento.put("STATUS", "F");

                                                        } else if (situacao.equalsIgnoreCase("99")) {
                                                            // Marca o status como retorno como excluido
                                                            dadosOrcamento.put("STATUS", "RE");

                                                        } else {
                                                            dadosOrcamento.put("STATUS", "N");
                                                        }
                                                    }
                                                    if (pedidoRetorno.has("tipoEntrega")) {
                                                        dadosOrcamento.put("TIPO_ENTREGA", pedidoRetorno.get("tipoEntrega").getAsString());
                                                    }
                                                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                                    //if (orcamentoSql.updateFast(dadosOrcamento, "AEAORCAM.GUID = '" + dadosOrcamento.getAsString("GUID") + "'") == 0) {
                                                    if (orcamentoSql.updateFast(dadosOrcamento, "AEAORCAM.GUID = '" + pedidoRetorno.get("guid").getAsString() + "'") == 0) {

                                                        dadosOrcamento.put("GUID", pedidoRetorno.get("guid").getAsString());
                                                        dadosOrcamento.put("NUMERO", pedidoRetorno.get("numero").getAsInt());

                                                        if (pedidoRetorno.has("vlTabela")) {
                                                            dadosOrcamento.put("VL_TABELA", pedidoRetorno.get("vlTabela").getAsDouble());
                                                            dadosOrcamento.put("VL_MERC_BRUTO", pedidoRetorno.get("vlMercBruto").getAsDouble());
                                                        }
                                                        if (pedidoRetorno.has("vlMercCusto")) {
                                                            dadosOrcamento.put("VL_MERC_CUSTO", pedidoRetorno.get("vlMercCusto").getAsDouble());
                                                        }
                                                        if (pedidoRetorno.has("fcVlTotal")) {
                                                            dadosOrcamento.put("FC_VL_TOTAL", pedidoRetorno.get("fcVlTotal").getAsDouble());
                                                        }
                                                        dadosOrcamento.put("DT_CAD", pedidoRetorno.get("dtCad").getAsString());
                                                        dadosOrcamento.put("DT_ALT", pedidoRetorno.get("dtAlt").getAsString());

                                                        if (orcamentoSql.insertOrReplace(dadosOrcamento) <= 0) {
                                                            todosSucesso = false;
                                                        }
                                                    }
                                                    for (int j = 0; j < listaOrcamento.size(); j++) {
                                                        if (listaOrcamento.get(j).getGuid().equalsIgnoreCase(pedidoRetorno.get("guid").getAsString())){
                                                            listaOrcamento.remove(j);
                                                            break;
                                                        }
                                                    }
                                                } // Fim do for
                                            }
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(0, 0, true);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                        } else {
                                            // Cria uma notificacao para ser manipulado
                                            /*Load mLoad = PugNotification.with(context).load()
                                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                    .smallIcon(R.mipmap.ic_launcher)
                                                    .largeIcon(R.mipmap.ic_launcher)
                                                    .title(R.string.versao_savare_desatualizada)
                                                    .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                                    .flags(Notification.DEFAULT_LIGHTS);
                                            mLoad.simple().build();*/

                                            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                                    .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(0, 0, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                        }
                                        // Incrementa o total de paginas
                                        pageNumber++;
                                        if (pageNumber < totalPages) {
                                            retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                        }
                                    } else {
                                        todosSucesso = false;

                                        // Cria uma notificacao para ser manipulado
                                        /*Load mLoad = PugNotification.with(context).load()
                                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                .smallIcon(R.mipmap.ic_launcher)
                                                .largeIcon(R.mipmap.ic_launcher)
                                                .title(R.string.recebendo_dados)
                                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                                .flags(Notification.DEFAULT_LIGHTS);
                                        mLoad.simple().build();*/

                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                                .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                    }
                                } // Fim do for ia (page)
                                if (todosSucesso) {
                                    inserirUltimaAtualizacao("AEASAIDA_ORC");
                                }
                            }
                        } else {
                            // Cria uma notificacao para ser manipulado
                            /*Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.recebendo_dados)
                                    .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();*/

                            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                    .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO));
                            mBuilder.setStyle(bigTextStyle)
                                    .setProgress(0, 0, false);
                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                        }
                    }
                }
                importarDadosPedido(listaOrcamento);
            }
        } catch (Exception e) {
            // Cria uma notificacao para ser manipulado
            /*PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportarDadosOrcamento - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();*/

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                        .bigText("ImportaDadosOrcamento - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    /**
     * Pega os dados do pedido que foi enviado pelo dispositivo.
     * Ou seja, pelo numero do orcamento eh pego os dados depois que o orcamento eh transformado
     * em pedido.
     */
    private void importarDadosPedido(List<OrcamentoBeans> listaPedidosDispositivo) {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Pedido");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Pedido");
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
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEASAIDA");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();

            if ((listaPedidosDispositivo != null) && (listaPedidosDispositivo.size() > 0)){
                listaOrcamento = listaPedidosDispositivo;

            } else if(listaPedidosDispositivo == null) {
                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);
                String[] listaTipo = new String[]{  OrcamentoRotinas.PEDIDO_ENVIADO,
                        OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                        OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO};

                listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(listaTipo, null, OrcamentoRotinas.ORDEM_DECRESCENTE);
            }

            String whereGuidOrcamento = "";

            if ( (listaOrcamento != null) && (listaOrcamento.size() > 0) ) {
                whereGuidOrcamento = "( (SERIE_ORC = (SELECT AEASERIE.CODIGO FROM SMAEMPRE, AEASERIE WHERE (SMAEMPRE.ID_AEASERIE_ORC_PALM = AEASERIE.ID_AEASERIE) " +
                        "AND (SMAEMPRE.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + "))) AND " +
                        "(NUMERO_ORC IN (";
                int controle = 0;
                for (OrcamentoBeans orcamento : listaOrcamento) {
                    controle++;
                    whereGuidOrcamento += orcamento.getNumero();
                    if (controle < listaOrcamento.size()) {
                        whereGuidOrcamento += ", ";
                    }
                }
                whereGuidOrcamento += ")) )";
            }

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where=  ( (DT_ALT >= '" + ultimaData + "' ) AND " +
                        "(AEASAIDA.ID_CFACLIFO_VENDEDOR_INI = (SELECT ID_CFACLIFO FROM CFACLIFO WHERE CFACLIFO.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")) AND " +
                        "(AEASAIDA.ID_CFACLIFO IS NOT NULL) AND (AEASAIDA.ID_CFACIDAD IS NOT NULL) ) \n";;

                if (!whereGuidOrcamento.isEmpty()){
                    parametrosWebservice += " OR ( (DT_ALT >= '" + ultimaData + "') AND " + whereGuidOrcamento + ")";
                }
            } else if (!whereGuidOrcamento.isEmpty()) {
                parametrosWebservice += "&where= " + whereGuidOrcamento;
            } else {
                return;
            }

            // Verifica se teve algum parametro
            if (!parametrosWebservice.isEmpty()) {
                Gson gson = new Gson();
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEASAIDA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        boolean todosSucesso = true;

                        JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                        if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                            final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                            int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                            for (int ia = pageNumber; ia < totalPages; ia++) {

                                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                                // Verifica se retornou com sucesso
                                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                    mBuilder.setStyle(bigTextStyle);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                            }
                                        });
                                    }
                                    // Checa se retornou alguma coisa
                                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                        final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                        // Checa se retornou algum dados na lista
                                        if (listaPedidoRetorno.size() > 0) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_orcamento));
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaPedidoRetorno.size(), 0, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento));
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setIndeterminate(false);
                                                        progressBarStatus.setMax(listaPedidoRetorno.size());
                                                    }
                                                });
                                            }

                                            for (int i = 0; i < listaPedidoRetorno.size(); i++) {
                                                // Atualiza a notificacao
                                                bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPedidoRetorno.size());
                                                mBuilder.setStyle(bigTextStyle)
                                                        .setProgress(listaPedidoRetorno.size(), i, false);
                                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                                // Checo se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    final int finalI1 = i;
                                                    final int finalPageNumber = pageNumber + 1;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPedidoRetorno.size());
                                                        }
                                                    });
                                                }
                                                if (progressBarStatus != null) {
                                                    final int finalI = i;
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            progressBarStatus.setProgress(finalI);
                                                        }
                                                    });
                                                }
                                                JsonObject pedidoRetorno = listaPedidoRetorno.get(i).getAsJsonObject();
                                                ContentValues dadosOrcamento = new ContentValues();
                                                dadosOrcamento.put("ID_SMAEMPRE", pedidoRetorno.get("idSmaempre").getAsInt());
                                                if (pedidoRetorno.has("idCfaclifo") && pedidoRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                    dadosOrcamento.put("ID_CFACLIFO", pedidoRetorno.get("idCfaclifo").getAsInt());
                                                }
                                                if (pedidoRetorno.has("idCfaestad") && pedidoRetorno.get("idCfaestad").getAsInt() > 0) {
                                                    dadosOrcamento.put("ID_CFAESTAD", pedidoRetorno.get("idCfaestad").getAsInt());
                                                }
                                                if (pedidoRetorno.has("idCfacidad") && pedidoRetorno.get("idCfacidad").getAsInt() > 0) {
                                                    dadosOrcamento.put("ID_CFACIDAD", pedidoRetorno.get("idCfacidad").getAsInt());
                                                }
                                                if (pedidoRetorno.has("idAearoman") && pedidoRetorno.get("idAearoman").getAsInt() > 0) {
                                                    dadosOrcamento.put("ID_AEAROMAN", pedidoRetorno.get("idAearoman").getAsInt());
                                                }
                                                if (pedidoRetorno.has("idCfatpdoc") && pedidoRetorno.get("idCfatpdoc").getAsInt() > 0) {
                                                    dadosOrcamento.put("ID_CFATPDOC", pedidoRetorno.get("idCfatpdoc").getAsInt());
                                                }
                                                dadosOrcamento.put("GUID", pedidoRetorno.get("guid").getAsString());
                                                dadosOrcamento.put("NUMERO", pedidoRetorno.get("numero").getAsInt());
                                                if (pedidoRetorno.has("vlFrete")) {
                                                    dadosOrcamento.put("VL_FRETE", pedidoRetorno.get("vlFrete").getAsDouble());
                                                }
                                                if (pedidoRetorno.has("vlSeguro")) {
                                                    dadosOrcamento.put("VL_SEGURO", pedidoRetorno.get("vlSeguro").getAsDouble());
                                                }
                                                if (pedidoRetorno.has("vlOutros")) {
                                                    dadosOrcamento.put("VL_OUTROS", pedidoRetorno.get("vlOutros").getAsDouble());
                                                }
                                                if (pedidoRetorno.has("vlEncargosFinanceiros")) {
                                                    dadosOrcamento.put("VL_ENCARGOS_FINANCEIROS", pedidoRetorno.get("vlEncargosFinanceiros").getAsDouble());
                                                }
                                                if (pedidoRetorno.has("vlTabelaFaturado")) {
                                                    dadosOrcamento.put("VL_TABELA_FATURADO", pedidoRetorno.get("vlTabelaFaturado").getAsDouble());
                                                }
                                                if (pedidoRetorno.has("fcVlTotalFaturado")) {
                                                    dadosOrcamento.put("FC_VL_TOTAL_FATURADO", pedidoRetorno.get("fcVlTotalFaturado").getAsDouble());
                                                }
                                                dadosOrcamento.put("ATAC_VAREJO", pedidoRetorno.get("atacVarejo").getAsString());
                                                if (pedidoRetorno.has("pessoaCliente")) {
                                                    dadosOrcamento.put("PESSOA_CLIENTE", pedidoRetorno.get("pessoaCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("nomeCliente")) {
                                                    dadosOrcamento.put("NOME_CLIENTE", pedidoRetorno.get("nomeCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("ieRgCliente")) {
                                                    dadosOrcamento.put("IE_RG_CLIENTE", pedidoRetorno.get("ieRgCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("cpfCgcCliente")) {
                                                    dadosOrcamento.put("CPF_CGC_CLIENTE", pedidoRetorno.get("cpfCgcCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("enderecoCliente")) {
                                                    dadosOrcamento.put("ENDERECO_CLIENTE", pedidoRetorno.get("enderecoCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("bairroCliente")) {
                                                    dadosOrcamento.put("BAIRRO_CLIENTE", pedidoRetorno.get("bairroCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("cepCliente")) {
                                                    dadosOrcamento.put("CEP_CLIENTE", pedidoRetorno.get("cepCliente").getAsString());
                                                }
                                                if (pedidoRetorno.has("obs")) {
                                                    dadosOrcamento.put("OBS", pedidoRetorno.get("obs").getAsString());
                                                }
                                                if (pedidoRetorno.has("andamento")) {
                                                    String situacao = pedidoRetorno.get("andamento").getAsString();

                                                    if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("3")) {
                                                        // Marca o status como retorno liberado
                                                        dadosOrcamento.put("STATUS", "RL");

                                                    } else if (situacao.equalsIgnoreCase("1")) {
                                                        // Marca o peiddo como enviado
                                                        dadosOrcamento.put("STATUS", "N");

                                                    } else if (situacao.equalsIgnoreCase("X") || situacao.equalsIgnoreCase("2")) {
                                                        // Marca o status como retorno como excluido ou bloqueado
                                                        dadosOrcamento.put("STATUS", "RB");

                                                    } else if (situacao.equalsIgnoreCase("7")) {
                                                        // Marca o status como retorno como conferido
                                                        dadosOrcamento.put("STATUS", "C");

                                                    } else if (situacao.equalsIgnoreCase("8") || situacao.equalsIgnoreCase("9") ||
                                                            situacao.equalsIgnoreCase("A") || situacao.equalsIgnoreCase("B")) {
                                                        // Marca o status como retorno como faturado
                                                        dadosOrcamento.put("STATUS", "F");

                                                    } else if (situacao.equalsIgnoreCase("99")) {
                                                        // Marca o status como retorno como excluido
                                                        dadosOrcamento.put("STATUS", "RE");

                                                    } else {
                                                        dadosOrcamento.put("STATUS", "F");
                                                    }
                                                } else {
                                                    dadosOrcamento.put("STATUS", "F");
                                                }
                                                if (pedidoRetorno.has("tipoEntrega")) {
                                                    dadosOrcamento.put("TIPO_ENTREGA", pedidoRetorno.get("tipoEntrega").getAsString());
                                                }
                                                OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                                                if (orcamentoSql.updateFast(dadosOrcamento, "AEAORCAM.NUMERO = " + pedidoRetorno.get("numeroOrc") ) == 0) {

                                                    if (pedidoRetorno.has("vlMercTabela")) {
                                                        dadosOrcamento.put("VL_TABELA", pedidoRetorno.get("vlMercTabela").getAsDouble());
                                                        dadosOrcamento.put("VL_TABELA_FATURADO", pedidoRetorno.get("vlMercTabela").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlMercBruto")) {
                                                        dadosOrcamento.put("VL_MERC_BRUTO", pedidoRetorno.get("vlMercBruto").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("vlMercCusto")) {
                                                        dadosOrcamento.put("VL_MERC_CUSTO", pedidoRetorno.get("vlMercCusto").getAsDouble());
                                                    }
                                                    if (pedidoRetorno.has("fcVlTotal")) {
                                                        dadosOrcamento.put("FC_VL_TOTAL", pedidoRetorno.get("fcVlTotal").getAsDouble());
                                                        dadosOrcamento.put("FC_VL_TOTAL_FATURADO", pedidoRetorno.get("fcVlTotal").getAsDouble());
                                                    }
                                                    dadosOrcamento.put("DT_CAD", pedidoRetorno.get("dtCad").getAsString());
                                                    dadosOrcamento.put("DT_ALT", pedidoRetorno.get("dtAlt").getAsString());

                                                    if (orcamentoSql.insertOrReplace(dadosOrcamento) <= 0) {
                                                        todosSucesso = false;
                                                    }
                                                }
                                            } // Fim do for
                                        }
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, true);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                    } else {
                                        // Cria uma notificacao para ser manipulado
                                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                                .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(0, 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                    }
                                    // Incrementa o total de paginas
                                    pageNumber++;
                                    if (pageNumber < totalPages) {
                                        retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEASAIDA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                    }
                                } else {
                                    todosSucesso = false;

                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                            } // Fim do for ia (page)
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("AEASAIDA");
                            }
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        /*Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();*/

                        bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO));
                        mBuilder.setStyle(bigTextStyle)
                                .setProgress(0, 0, false);
                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                    }
                }
            }
        } catch (Exception e) {
            // Cria uma notificacao para ser manipulado
            /*PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportarDadosOrcamento - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();*/

            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.msg_error))
                    .bigText("ImportaDadosOrcamento - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }



    private void importarDadosItemOrcamento() {
        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Item de Orçamento");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Item de Orçamento");
                }
            });
        }
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaItemOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAITORC, criaPropriedadeDataAlteracaoWebservice("AEAITORC"));

            // Checa se retornou alguma coisa
            if ((listaItemOrcamentoObject != null) && (listaItemOrcamentoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mBuilder.setStyle(bigTextStyle);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

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
                            progressBarStatus.setMax(listaItemOrcamentoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaItemOrcamentoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaItemOrcamentoObject.size());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(listaItemOrcamentoObject.size(), 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaItemOrcamentoObject.size());
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
                    String guidOrcamento = null;

                    if (objetoIndividual.hasProperty("return")) {
                        objeto = (SoapObject) objetoIndividual.getProperty("return");
                    } else {
                        objeto = objetoIndividual;
                    }

                    final ContentValues dadosItemOrcamento = new ContentValues();

                    if (objeto.hasProperty("idEstoqueTemp")) {
                        //SoapObject estoque = (SoapObject) objeto.getProperty("estoqueVenda");
                        dadosItemOrcamento.put("ID_AEAESTOQ", Integer.parseInt(objeto.getProperty("idEstoqueTemp").toString()));
                    }
                    if (objeto.hasProperty("idPlanoPagamentoTemp")) {
                        //SoapObject planoPagamento = (SoapObject) objeto.getProperty("planoPagamento");
                        dadosItemOrcamento.put("ID_AEAPLPGT", Integer.parseInt(objeto.getProperty("idPlanoPagamentoTemp").toString()));
                    }
                    if (objeto.hasProperty("idUnidadeTemp")) {
                        //SoapObject unidade = (SoapObject) objeto.getProperty("unidadeVenda");
                        dadosItemOrcamento.put("ID_AEAUNVEN", Integer.parseInt(objeto.getProperty("idUnidadeTemp").toString()));
                    }
                    if (objeto.hasProperty("idVendedorTemp")) {
                        //SoapObject vendedor = (SoapObject) objeto.getProperty("pessoaVendedor");
                        dadosItemOrcamento.put("ID_CFACLIFO_VENDEDOR", Integer.parseInt(objeto.getProperty("idVendedorTemp").toString()));
                    }
                    guidOrcamento = objeto.getProperty("guidOrcamento").toString();

                    dadosItemOrcamento.put("GUID", objeto.getProperty("guid").toString());
                    dadosItemOrcamento.put("DT_CAD", objeto.getProperty("dataCadastro").toString());
                    dadosItemOrcamento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    //dadosItemOrcamento.put("DT_ULTIMA_ATUALIZACAO", objeto.getProperty("").toString());
                    dadosItemOrcamento.put("SEQUENCIA", Integer.parseInt(objeto.getProperty("sequencia").toString()));
                    dadosItemOrcamento.put("QUANTIDADE", Double.parseDouble(objeto.getProperty("quantidade").toString()));
                    if (objeto.hasProperty("quantidadeFaturada")) {
                        dadosItemOrcamento.put("QUANTIDADE_FATURADA", Double.parseDouble(objeto.getProperty("quantidadeFaturada").toString()));
                    }
                    if (objeto.hasProperty("valorCusto")) {
                        dadosItemOrcamento.put("VL_CUSTO", Double.parseDouble(objeto.getProperty("valorCusto").toString()));
                    }
                    if (objeto.hasProperty("valorBruto")) {
                        dadosItemOrcamento.put("VL_BRUTO", Double.parseDouble(objeto.getProperty("valorBruto").toString()));
                    }
                    if (objeto.hasProperty("ValorDesconto")) {
                        dadosItemOrcamento.put("VL_DESCONTO", Double.parseDouble(objeto.getProperty("ValorDesconto").toString()));
                    }
                    if (objeto.hasProperty("valorTabela")) {
                        dadosItemOrcamento.put("VL_TABELA", Double.parseDouble(objeto.getProperty("valorTabela").toString()));
                    }
                    if (objeto.hasProperty("valorTabelaUnitario")) {
                        dadosItemOrcamento.put("VL_TABELA_UN", Double.parseDouble(objeto.getProperty("valorTabelaUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorTabelaFaturado")) {
                        dadosItemOrcamento.put("VL_TABELA_FATURADO", Double.parseDouble(objeto.getProperty("valorTabelaFaturado").toString()));
                    }
                    if (objeto.hasProperty("valorTabelaFaturadoUnitario")) {
                        dadosItemOrcamento.put("VL_TABELA_UN_FATURADO", Double.parseDouble(objeto.getProperty("valorTabelaFaturadoUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorCustoUnitario")) {
                        dadosItemOrcamento.put("FC_CUSTO_UN", Double.parseDouble(objeto.getProperty("valorCustoUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorBrutoUnitario")) {
                        dadosItemOrcamento.put("FC_BRUTO_UN", Double.parseDouble(objeto.getProperty("valorBrutoUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorDescontoUnitario")) {
                        dadosItemOrcamento.put("FC_DESCONTO_UN", Double.parseDouble(objeto.getProperty("valorDescontoUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorLiquido")) {
                        dadosItemOrcamento.put("FC_LIQUIDO", Double.parseDouble(objeto.getProperty("valorLiquido").toString()));
                    }
                    if (objeto.hasProperty("valorLiquidoUnitario")) {
                        dadosItemOrcamento.put("FC_LIQUIDO_UN", Double.parseDouble(objeto.getProperty("valorLiquidoUnitario").toString()));
                    }
                    if (objeto.hasProperty("valorLiquidoFaturado")) {
                        dadosItemOrcamento.put("FC_LIQUIDO_FATURADO", Double.parseDouble(objeto.getProperty("valorLiquidoFaturado").toString()));
                    }
                    if (objeto.hasProperty("promocao")) {
                        dadosItemOrcamento.put("PROMOCAO", objeto.getProperty("promocao").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("tipoProduto")) {
                        dadosItemOrcamento.put("TIPO_PRODUTO", objeto.getProperty("tipoProduto").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("complemento")) {
                        dadosItemOrcamento.put("COMPLEMENTO", objeto.getProperty("complemento").toString().replace("anyType{}", ""));
                    }
                    //dadosItemOrcamento.put("SEQ_DESCONTO", objeto.getProperty("").toString());

                    if (objeto.hasProperty("statusRetorno")) {

                        String situacao = objeto.getProperty("statusRetorno").toString();

                        if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("1")) {
                            // Marca o status como retorno liberado
                            dadosItemOrcamento.put("STATUS", "RL");

                        } else if (situacao.equalsIgnoreCase("6")) {
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "RB");

                        } else if (situacao.equalsIgnoreCase("2")) {
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "C");

                        } else if (situacao.equalsIgnoreCase("3") || situacao.equalsIgnoreCase("4")) {
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "F");

                        } else if (situacao.equalsIgnoreCase("5")) {
                            // Marca o status como retorno como faturado
                            dadosItemOrcamento.put("STATUS", "RE");

                        } else {

                            dadosItemOrcamento.put("STATUS", "RB");
                        }
                    }

                    final ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                    // Busca o id do orcamento
                    Cursor cursor = orcamentoSql.sqlSelect("SELECT AEAORCAM.ID_AEAORCAM FROM AEAORCAM WHERE (AEAORCAM.GUID LIKE '" + guidOrcamento + "')");

                    if ((cursor != null) && (cursor.getCount() > 0) && (cursor.moveToFirst())) {
                        // Pega o id do orcamento/pedido
                        dadosItemOrcamento.put("ID_AEAORCAM", cursor.getInt(cursor.getColumnIndex("ID_AEAORCAM")));
                    }

                    if (itemOrcamentoSql.update(dadosItemOrcamento, "AEAITORC.GUID LIKE '" + dadosItemOrcamento.getAsString("GUID") + "'") == 0) {

                        if (itemOrcamentoSql.insertOrReplace(dadosItemOrcamento) <= 0) {
                            todosSucesso = false;
                        }
                    }
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAITORC");
                }
                // Atualiza a notificacao
                bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                mBuilder.setStyle(bigTextStyle)
                        .setProgress(0, 0, true);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Item Orçamento : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosItemOrcamento - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosPercentual() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Percentual");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Percentual");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPERCE");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPercentualRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPercentualRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_percentual));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaPercentualRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_percentual));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaPercentualRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosPercentual = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaPercentualRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_percentual) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPercentualRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaPercentualRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_percentual) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaPercentualRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject percentualRetorno = listaPercentualRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosPercentual = new ContentValues();

                                            if (percentualRetorno.has("idAeaperce") && percentualRetorno.get("idAeaperce").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAPERCE", percentualRetorno.get("idAeaperce").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeatbperTabela") && percentualRetorno.get("idAeatbperTabela").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEATBPER_TABELA", percentualRetorno.get("idAeatbperTabela").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeatbper") && percentualRetorno.get("idAeatbper").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEATBPER", percentualRetorno.get("idAeatbper").getAsInt());
                                            }
                                            if (percentualRetorno.has("idSmaempre") && percentualRetorno.get("idSmaempre").getAsInt() > 0) {
                                                dadosPercentual.put("ID_SMAEMPRE", percentualRetorno.get("idSmaempre").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeaclase") && percentualRetorno.get("idAeaclase").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEACLASE", percentualRetorno.get("idAeaclase").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeamarca") && percentualRetorno.get("idAeamarca").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAMARCA", percentualRetorno.get("idAeamarca").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeaprodu") && percentualRetorno.get("idAeaprodu").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAPRODU", percentualRetorno.get("idAeaprodu").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeaploja") && percentualRetorno.get("idAeaploja").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAPLOJA", percentualRetorno.get("idAeaploja").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeaagppr") && percentualRetorno.get("idAeaagppr").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAAGPPR", percentualRetorno.get("idAeaagppr").getAsInt());
                                            }
                                            if (percentualRetorno.has("idCfaparamVendedor") && percentualRetorno.get("idCfaparamVendedor").getAsInt() > 0) {
                                                dadosPercentual.put("ID_CFAPARAM_VENDEDOR", percentualRetorno.get("idCfaparamVendedor").getAsInt());
                                            }
                                            if (percentualRetorno.has("idAeafator") && percentualRetorno.get("idAeafator").getAsInt() > 0) {
                                                dadosPercentual.put("ID_AEAFATOR", percentualRetorno.get("idAeafator").getAsInt());
                                            }
                                            dadosPercentual.put("DT_ALT", percentualRetorno.get("dtAlt").getAsString());
                                            if (percentualRetorno.has("tipoIss")) {
                                                dadosPercentual.put("TIPO_ISS", percentualRetorno.get("tipoIss").getAsString());
                                            }
                                            if (percentualRetorno.has("iss")) {
                                                dadosPercentual.put("ISS", percentualRetorno.get("iss").getAsDouble());
                                            }
                                            if (percentualRetorno.has("custoFixo")) {
                                                dadosPercentual.put("CUSTO_FIXO", percentualRetorno.get("custoFixo").getAsDouble());
                                            }
                                            if (percentualRetorno.has("impostosFederais")) {
                                                dadosPercentual.put("IMPOSTOS_FEDERAIS", percentualRetorno.get("impostosFederais").getAsDouble());
                                            }
                                            if (percentualRetorno.has("markupVare")) {
                                                dadosPercentual.put("MARKUP_VARE", percentualRetorno.get("markupVare").getAsDouble());
                                            }
                                            if (percentualRetorno.has("markupAtac")) {
                                                dadosPercentual.put("MARKUP_ATAC", percentualRetorno.get("markupAtac").getAsDouble());
                                            }
                                            if (percentualRetorno.has("lucroVare")) {
                                                dadosPercentual.put("LUCRO_VARE", percentualRetorno.get("lucroVare").getAsDouble());
                                            }
                                            if (percentualRetorno.has("lucroAtac")) {
                                                dadosPercentual.put("LUCRO_ATAC", percentualRetorno.get("lucroAtac").getAsDouble());
                                            }
                                            if (percentualRetorno.has("descMercVistaVare")) {
                                                dadosPercentual.put("DESC_MERC_VISTA_VARE", percentualRetorno.get("descMercVistaVare").getAsDouble());
                                            }
                                            if (percentualRetorno.has("descMercVistaAtac")) {
                                                dadosPercentual.put("DESC_MERC_VISTA_ATAC", percentualRetorno.get("descMercVistaAtac").getAsDouble());
                                            }
                                            if (percentualRetorno.has("descMercPrazoVare")) {
                                                dadosPercentual.put("DESC_MERC_PRAZO_VARE", percentualRetorno.get("descMercPrazoVare").getAsDouble());
                                            }
                                            if (percentualRetorno.has("descMercPrazoAtac")) {
                                                dadosPercentual.put("DESC_MERC_PRAZO_ATAC", percentualRetorno.get("descMercPrazoAtac").getAsDouble());
                                            }
                                            if (percentualRetorno.has("aliquotaIpi")) {
                                                dadosPercentual.put("ALIQUOTA_IPI", percentualRetorno.get("aliquotaIpi").getAsDouble());
                                            }
                                            if (percentualRetorno.has("aliquotaPis")) {
                                                dadosPercentual.put("ALIQUOTA_PIS", percentualRetorno.get("aliquotaPis").getAsDouble());
                                            }
                                            if (percentualRetorno.has("aliquotaCofins")) {
                                                dadosPercentual.put("ALIQUOTA_COFINS", percentualRetorno.get("aliquotaCofins").getAsDouble());
                                            }
                                            listaDadosPercentual.add(dadosPercentual);
                                        } // Fim do for
                                        PercentualSql percentualSql = new PercentualSql(context);

                                        todosSucesso = percentualSql.insertList(listaDadosPercentual);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } //Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPERCE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO));
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Percentual : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosPercentual - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosFator() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Fator");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Fator");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAFATOR");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());


                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaFatorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaFatorRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_fator));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaFatorRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fator));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaFatorRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosFator = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaFatorRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_fator) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaFatorRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaFatorRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fator) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaFatorRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject fatorRetorno = listaFatorRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosFator = new ContentValues();

                                            dadosFator.put("ID_AEAFATOR", fatorRetorno.get("idAeafator").getAsInt());
                                            dadosFator.put("DT_ALT", fatorRetorno.get("dtAlt").getAsString());
                                            dadosFator.put("CODIGO", fatorRetorno.get("codigo").getAsInt());
                                            dadosFator.put("DESCRICAO", fatorRetorno.get("descricao").getAsString());
                                            if ((fatorRetorno.has("jurosMedioAtac")) && (!fatorRetorno.get("jurosMedioAtac").isJsonNull())) {
                                                dadosFator.put("JURO_MEDIO_ATAC", fatorRetorno.get("jurosMedioAtac").getAsDouble());
                                            }
                                            if ((fatorRetorno.has("jurosMedioVare")) && (!fatorRetorno.get("jurosMedioVare").isJsonNull())) {
                                                dadosFator.put("JURO_MEDIO_VARE", fatorRetorno.get("jurosMedioVare").getAsDouble());
                                            }
                                            if ((fatorRetorno.has("jurosMedioServ")) && (!fatorRetorno.get("jurosMedioServ").isJsonNull())) {
                                                dadosFator.put("JURO_MEDIO_SERV", fatorRetorno.get("jurosMedioServ").getAsDouble());
                                            }
                                            if ((fatorRetorno.has("descPgAntAtac")) && (!fatorRetorno.get("descPgAntAtac").isJsonNull())) {
                                                dadosFator.put("DESC_PG_ANT_ATAC", fatorRetorno.get("descPgAntAtac").getAsDouble());
                                            }
                                            if ((fatorRetorno.has("descPgAntVare")) && (!fatorRetorno.get("descPgAntVare").isJsonNull())) {
                                                dadosFator.put("DESC_PG_ANT_VARE", fatorRetorno.get("descPgAntVare").getAsDouble());
                                            }
                                            if ((fatorRetorno.has("descPgAntServ")) && (!fatorRetorno.get("descPgAntServ").isJsonNull())) {
                                                dadosFator.put("DESC_PG_ANT_SERV", fatorRetorno.get("descPgAntServ").getAsDouble());
                                            }
                                            dadosFator.put("DESC_MAX_PLANO_ATAC_VISTA", fatorRetorno.get("descMaxPlanoAtacVista").getAsDouble());
                                            dadosFator.put("DESC_MAX_PLANO_ATAC_PRAZO", fatorRetorno.get("descMaxPlanoAtacPrazo").getAsDouble());
                                            dadosFator.put("DESC_MAX_PLANO_VARE_VISTA", fatorRetorno.get("descMaxPlanoVareVista").getAsDouble());
                                            dadosFator.put("DESC_MAX_PLANO_VARE_PRAZO", fatorRetorno.get("descMaxPlanoVarePrazo").getAsDouble());
                                            dadosFator.put("DESC_MAX_PLANO_SERV_VISTA", fatorRetorno.get("descMaxPlanoServVista").getAsDouble());
                                            dadosFator.put("DESC_MAX_PLANO_SERV_PRAZO", fatorRetorno.get("descMaxPlanoServPrazo").getAsDouble());
                                            if (fatorRetorno.has("TIPO_BONUS")) {
                                                dadosFator.put("TIPO_BONUS", fatorRetorno.get("tipoBonus").getAsString());
                                            }
                                            dadosFator.put("DIAS_BONUS", fatorRetorno.get("diasBonus").getAsInt());

                                            listaDadosFator.add(dadosFator);
                                        }
                                        FatorSql fatorSql = new FatorSql(context);

                                        todosSucesso = fatorSql.insertList(listaDadosFator);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAFATOR");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Fator : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosFator - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosProdutoRecomendado() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Produto Recomendado");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Produto Recomendado");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPRREC");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') ";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoRecomendadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoRecomendadoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_recomendado));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaProdutoRecomendadoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_recomendado));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaProdutoRecomendadoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosRecomendado = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaProdutoRecomendadoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoRecomendadoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaProdutoRecomendadoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaProdutoRecomendadoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject recomendadoRetorno = listaProdutoRecomendadoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosRecomendado = new ContentValues();

                                            //dadosRecomendado.put("ID_AEAPRREC", recomendadoRetorno.get("idPrecoRecomendado").getAsInt());
                                            if (recomendadoRetorno.has("idProduto") && recomendadoRetorno.get("idProduto").getAsInt() > 0) {
                                                dadosRecomendado.put("ID_AEAPRODU", recomendadoRetorno.get("idProduto").getAsInt());
                                            }
                                            if (recomendadoRetorno.has("idAreas") && recomendadoRetorno.get("idAreas").getAsInt() > 0) {
                                                dadosRecomendado.put("ID_AEAAREAS", recomendadoRetorno.get("idAreas").getAsInt());
                                            }
                                            if (recomendadoRetorno.has("idCidade") && recomendadoRetorno.get("idCidade").getAsInt() > 0) {
                                                dadosRecomendado.put("ID_CFACIDAD", recomendadoRetorno.get("idCidade").getAsInt());
                                            }
                            /*if (recomendadoRetorno.has("idClifoVendedor") && recomendadoRetorno.get("idClifoVendedor").getAsInt() > 0) {
                                dadosRecomendado.put("ID_CFACLIFO_VENDEDOR", recomendadoRetorno.get("idClifoVendedor").getAsInt());
                            }*/
                                            if (recomendadoRetorno.has("idClifo") && recomendadoRetorno.get("idClifo").getAsInt() > 0) {
                                                dadosRecomendado.put("ID_CFACLIFO", recomendadoRetorno.get("idClifo").getAsInt());
                                            }
                                            if (recomendadoRetorno.has("idEmpresa") && recomendadoRetorno.get("idEmpresa").getAsInt() > 0) {
                                                dadosRecomendado.put("ID_SMAEMPRE", recomendadoRetorno.get("idEmpresa").getAsInt());
                                            }
                                            if (recomendadoRetorno.has("posicao")) {
                                                dadosRecomendado.put("POSICAO", recomendadoRetorno.get("posicao").getAsInt());
                                            }
                                            if (recomendadoRetorno.has("quantidadeVendida")) {
                                                dadosRecomendado.put("QUANTIDADE_VENDIDA", recomendadoRetorno.get("quantidadeVendida").getAsDouble());
                                            }
                                            if (recomendadoRetorno.has("valorTotalCusto")) {
                                                dadosRecomendado.put("VALOR_TOTAL_CUSTO", recomendadoRetorno.get("valorTotalCusto").getAsDouble());
                                            }
                                            if (recomendadoRetorno.has("valorTotalVenda")) {
                                                dadosRecomendado.put("VALOR_TOTAL_VENDA", recomendadoRetorno.get("valorTotalVenda").getAsDouble());
                                            }
                                            listaDadosRecomendado.add(dadosRecomendado);
                                        }
                                        ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(context);

                                        produtoRecomendadoSql.delete(null);

                                        todosSucesso = produtoRecomendadoSql.insertList(listaDadosRecomendado);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRREC");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Produto Recomendado : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosProdutoRecomendado - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosTabelaPromocao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Tabela de Promoção");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Tabela de Promoção");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEATBPRO");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            String parametrosWebservice = "";
            String filtroPromocao = "((DT_INICIO <= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)) AND (DT_FIM >= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtroPromocao;
            } else {
                parametrosWebservice += "&where= " +  filtroPromocao;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEATBPRO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTabelaPromocaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTabelaPromocaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaTabelaPromocaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaTabelaPromocaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaTabelaPromocao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaTabelaPromocaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTabelaPromocaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaTabelaPromocaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaTabelaPromocaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject recomendadoRetorno = listaTabelaPromocaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosTabelaPromocao = new ContentValues();

                                            dadosTabelaPromocao.put("ID_AEATBPRO", recomendadoRetorno.get("idAeatbpro").getAsInt());
                                            dadosTabelaPromocao.put("GUID", recomendadoRetorno.get("guid").getAsString());
                                            if (recomendadoRetorno.has("usCad")) {
                                                dadosTabelaPromocao.put("US_CAD", recomendadoRetorno.get("usCad").getAsString());
                                            }
                                            if (recomendadoRetorno.has("dtCad")) {
                                                dadosTabelaPromocao.put("DT_CAD", recomendadoRetorno.get("dtCad").getAsString());
                                            }
                                            if (recomendadoRetorno.has("dtAlt")) {
                                                dadosTabelaPromocao.put("DT_ALT", recomendadoRetorno.get("dtAlt").getAsString());
                                            }
                                            if (recomendadoRetorno.has("ctInteg") && recomendadoRetorno.get("ctInteg").getAsInt() > 0) {
                                                dadosTabelaPromocao.put("CT_INTEG", recomendadoRetorno.get("ctInteg").getAsInt());
                                            }
                                            dadosTabelaPromocao.put("CODIGO", recomendadoRetorno.get("codigo").getAsInt());
                                            dadosTabelaPromocao.put("DESCRICAO", recomendadoRetorno.get("descricao").getAsString());

                                            if (recomendadoRetorno.has("dtInicio")) {
                                                dadosTabelaPromocao.put("DT_INICIO", recomendadoRetorno.get("dtInicio").getAsString());
                                            }
                                            if (recomendadoRetorno.has("dtFim")) {
                                                dadosTabelaPromocao.put("DT_FIM", recomendadoRetorno.get("dtFim").getAsString());
                                            }
                                            if (recomendadoRetorno.has("dias")) {
                                                dadosTabelaPromocao.put("DIAS", recomendadoRetorno.get("dias").getAsString());
                                            }
                                            if (recomendadoRetorno.has("ativo")) {
                                                dadosTabelaPromocao.put("ATIVO", recomendadoRetorno.get("ativo").getAsString());
                                            }
                                            if (recomendadoRetorno.has("vistaPrazo")) {
                                                dadosTabelaPromocao.put("VISTA_PRAZO", recomendadoRetorno.get("vistaPrazo").getAsString());
                                            }
                                            listaTabelaPromocao.add(dadosTabelaPromocao);
                                        }
                                        TabelaPromocaoSql tabelaPromocaoSql = new TabelaPromocaoSql(context);

                                        tabelaPromocaoSql.delete(null);

                                        todosSucesso = tabelaPromocaoSql.insertList(listaTabelaPromocao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEATBPRO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEATBPRO");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Tabela Promoção : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosTabelaPromocao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void importarDadosItemPromocao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Item de Promoção");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Item de Promoção");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAITTBP");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            String parametrosWebservice = "";
            String filtroPromocao = "(ID_AEATBPRO IN (SELECT ID_AEATBPRO FROM AEATBPRO WHERE ((DT_INICIO <= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)) AND (DT_FIM >= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtroPromocao;
            } else {
                parametrosWebservice += "&where= " + filtroPromocao;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAITTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaItemPromocaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaItemPromocaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaItemPromocaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_promocao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaItemPromocaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaTabelaPromocao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaItemPromocaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_item_promocao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaItemPromocaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaItemPromocaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_promocao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaItemPromocaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject itemPromocaoRetorno = listaItemPromocaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosItemPromocao = new ContentValues();

                                            dadosItemPromocao.put("ID_AEAITTBP", itemPromocaoRetorno.get("idAeaittbp").getAsInt());
                                            dadosItemPromocao.put("ID_AEATBPRO", itemPromocaoRetorno.get("idAeatbpro").getAsInt());
                                            if (itemPromocaoRetorno.has("idAeaagppr") && itemPromocaoRetorno.get("idAeaagppr").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEAAGPPR", itemPromocaoRetorno.get("idAeaagppr").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeamarca") && itemPromocaoRetorno.get("idAeamarca").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEAMARCA", itemPromocaoRetorno.get("idAeamarca").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeafamil") && itemPromocaoRetorno.get("idAeafamil").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEAFAMIL", itemPromocaoRetorno.get("idAeafamil").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeaclase") && itemPromocaoRetorno.get("idAeaclase").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEACLASE", itemPromocaoRetorno.get("idAeaclase").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeagrupo") && itemPromocaoRetorno.get("idAeagrupo").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEAGRUPO", itemPromocaoRetorno.get("idAeagrupo").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeasgrup") && itemPromocaoRetorno.get("idAeasgrup").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEASGRUP", itemPromocaoRetorno.get("idAeasgrup").getAsInt());
                                            }
                                            if (itemPromocaoRetorno.has("idAeaprodu") && itemPromocaoRetorno.get("idAeaprodu").getAsInt() > 0) {
                                                dadosItemPromocao.put("ID_AEAPRODU", itemPromocaoRetorno.get("idAeaprodu").getAsInt());
                                            }
                                            dadosItemPromocao.put("GUID", itemPromocaoRetorno.get("guid").getAsString());
                                            if (itemPromocaoRetorno.has("usCad")) {
                                                dadosItemPromocao.put("US_CAD", itemPromocaoRetorno.get("usCad").getAsString());
                                            }
                                            if (itemPromocaoRetorno.has("dtCad")) {
                                                dadosItemPromocao.put("DT_CAD", itemPromocaoRetorno.get("dtCad").getAsString());
                                            }
                                            if (itemPromocaoRetorno.has("dtAlt")) {
                                                dadosItemPromocao.put("DT_ALT", itemPromocaoRetorno.get("dtAlt").getAsString());
                                            }
                                            if (itemPromocaoRetorno.has("ctInteg") && itemPromocaoRetorno.get("ctInteg").getAsInt() > 0) {
                                                dadosItemPromocao.put("CT_INTEG", itemPromocaoRetorno.get("ctInteg").getAsInt());
                                            }
                                            dadosItemPromocao.put("DESC_MERC_VISTA_VARE", itemPromocaoRetorno.get("descMercVistaVare").getAsDouble());
                                            dadosItemPromocao.put("DESC_MERC_VISTA_ATAC", itemPromocaoRetorno.get("descMercVistaAtac").getAsDouble());
                                            dadosItemPromocao.put("DESC_MERC_PRAZO_VARE", itemPromocaoRetorno.get("descMercPrazoVare").getAsDouble());
                                            dadosItemPromocao.put("DESC_MERC_PRAZO_ATAC", itemPromocaoRetorno.get("descMercPrazoAtac").getAsDouble());
                                            dadosItemPromocao.put("DESC_SERV_VISTA", itemPromocaoRetorno.get("descServVista").getAsDouble());
                                            dadosItemPromocao.put("DESC_SERV_PRAZO", itemPromocaoRetorno.get("descServPrazo").getAsDouble());
                                            dadosItemPromocao.put("COM_MERC_VISTA_VARE", itemPromocaoRetorno.get("comMercVistaVare").getAsDouble());
                                            dadosItemPromocao.put("COM_MERC_VISTA_ATAC", itemPromocaoRetorno.get("comMercVistaAtac").getAsDouble());
                                            dadosItemPromocao.put("COM_MERC_PRAZO_VARE", itemPromocaoRetorno.get("comMercPrazoVare").getAsDouble());
                                            dadosItemPromocao.put("COM_MERC_PRAZO_ATAC", itemPromocaoRetorno.get("comMercPrazoAtac").getAsDouble());
                                            dadosItemPromocao.put("COM_SERV_VISTA", itemPromocaoRetorno.get("comServVista").getAsDouble());
                                            dadosItemPromocao.put("COM_SERV_PRAZO", itemPromocaoRetorno.get("comServPrazo").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_MERC_VISTA_VARE", itemPromocaoRetorno.get("comExtMercVistaVare").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_MERC_VISTA_ATAC", itemPromocaoRetorno.get("comExtMercVistaAtac").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_MERC_PRAZO_VARE", itemPromocaoRetorno.get("comExtMercPrazoVare").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_MERC_PRAZO_ATAC", itemPromocaoRetorno.get("comExtMercPrazoAtac").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_SERV_VISTA", itemPromocaoRetorno.get("comExtServVista").getAsDouble());
                                            dadosItemPromocao.put("COM_EXT_SERV_Prazo", itemPromocaoRetorno.get("comExtServPrazo").getAsDouble());
                                            dadosItemPromocao.put("PRECO_VISTA_VARE", itemPromocaoRetorno.get("precoVistaVare").getAsDouble());
                                            dadosItemPromocao.put("PRECO_VISTA_ATAC", itemPromocaoRetorno.get("precoVistaAtac").getAsDouble());
                                            dadosItemPromocao.put("PRECO_PRAZO_VARE", itemPromocaoRetorno.get("precoPrazoVare").getAsDouble());
                                            dadosItemPromocao.put("PRECO_PRAZO_ATAC", itemPromocaoRetorno.get("precoPrazoAtac").getAsDouble());
                                            dadosItemPromocao.put("PRECO_VISTA_SERV", itemPromocaoRetorno.get("precoVistaServ").getAsDouble());
                                            dadosItemPromocao.put("PRECO_PRAZO_SERV", itemPromocaoRetorno.get("precoPrazoServ").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_MERC_VISTA_VARE", itemPromocaoRetorno.get("descMaxMercVistaVare").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_MERC_VISTA_ATAC", itemPromocaoRetorno.get("descMaxMercVistaAtac").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_MERC_PRAZO_VARE", itemPromocaoRetorno.get("descMaxMercPrazoVare").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_MERC_PRAZO_ATAC", itemPromocaoRetorno.get("descMaxMercPrazoAtac").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_SERV_VISTA", itemPromocaoRetorno.get("descMaxServVista").getAsDouble());
                                            dadosItemPromocao.put("DESC_MAX_SERV_PRAZO", itemPromocaoRetorno.get("descMaxServPrazo").getAsDouble());

                                            listaTabelaPromocao.add(dadosItemPromocao);
                                        }
                                        ItemPromocaoSql itemPromocaoSql = new ItemPromocaoSql(context);

                                        itemPromocaoSql.delete(null);

                                        todosSucesso = itemPromocaoSql.insertList(listaTabelaPromocao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAITTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAITTBP");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Item Promoção : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosItemPromocao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosExcaoPromocao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Exceção de Promoção");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Exceção de Promoção");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAEXTBP");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            String parametrosWebservice = "";
            String filtroPromocao = "(ID_AEAITTBP IN (SELECT ID_AEAITTBP FROM AEAITTBP WHERE ID_AEATBPRO IN (SELECT ID_AEATBPRO FROM AEATBPRO WHERE " +
                                    "((DT_INICIO <= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)) AND (DT_FIM >= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE))))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtroPromocao;
            } else {
                parametrosWebservice += "&where= " + filtroPromocao;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEXTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaExcecaoPromocaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaExcecaoPromocaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaExcecaoPromocaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_excao_promocao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaExcecaoPromocaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaExcecaoPromocao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaExcecaoPromocaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_excao_promocao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaExcecaoPromocaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaExcecaoPromocaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_excao_promocao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaExcecaoPromocaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject excecaoPromocaoRetorno = listaExcecaoPromocaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosExcecaoPromocao = new ContentValues();

                                            dadosExcecaoPromocao.put("ID_AEAEXTBP", excecaoPromocaoRetorno.get("idAeaextbp").getAsInt());
                                            dadosExcecaoPromocao.put("ID_AEAITTBP", excecaoPromocaoRetorno.get("idAeaittbp").getAsInt());
                                            if (excecaoPromocaoRetorno.has("idCfaareas") && excecaoPromocaoRetorno.get("idCfaareas").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("ID_CFAAREAS", excecaoPromocaoRetorno.get("idCfaareas").getAsInt());
                                            }
                                            if (excecaoPromocaoRetorno.has("idCfatpcli") && excecaoPromocaoRetorno.get("idCfatpcli").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("ID_CFATPCLI", excecaoPromocaoRetorno.get("idCfatpcli").getAsInt());
                                            }
                                            if (excecaoPromocaoRetorno.has("idCfastatu") && excecaoPromocaoRetorno.get("idCfastatu").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("ID_CFASTATU", excecaoPromocaoRetorno.get("idCfastatu").getAsInt());
                                            }
                                            if (excecaoPromocaoRetorno.has("idCfaprofi") && excecaoPromocaoRetorno.get("idCfaprofi").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("ID_CFAPROFI", excecaoPromocaoRetorno.get("idCfaprofi").getAsInt());
                                            }
                                            if (excecaoPromocaoRetorno.has("idCfaativi") && excecaoPromocaoRetorno.get("idCfaativi").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("ID_CFAATIVI", excecaoPromocaoRetorno.get("idCfaativi").getAsInt());
                                            }
                                            dadosExcecaoPromocao.put("GUID", excecaoPromocaoRetorno.get("guid").getAsString());
                                            if (excecaoPromocaoRetorno.has("usCad")) {
                                                dadosExcecaoPromocao.put("US_CAD", excecaoPromocaoRetorno.get("usCad").getAsString());
                                            }
                                            if (excecaoPromocaoRetorno.has("dtCad")) {
                                                dadosExcecaoPromocao.put("DT_CAD", excecaoPromocaoRetorno.get("dtCad").getAsString());
                                            }
                                            if (excecaoPromocaoRetorno.has("dtAlt")) {
                                                dadosExcecaoPromocao.put("DT_ALT", excecaoPromocaoRetorno.get("dtAlt").getAsString());
                                            }
                                            if (excecaoPromocaoRetorno.has("ctInteg") && excecaoPromocaoRetorno.get("ctInteg").getAsInt() > 0) {
                                                dadosExcecaoPromocao.put("CT_INTEG", excecaoPromocaoRetorno.get("ctInteg").getAsInt());
                                            }
                                            dadosExcecaoPromocao.put("DESC_MERC_VISTA_VARE", excecaoPromocaoRetorno.get("descMercVistaVare").getAsDouble());
                                            dadosExcecaoPromocao.put("DESC_MERC_VISTA_ATAC", excecaoPromocaoRetorno.get("descMercVistaAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("DESC_MERC_PRAZO_VARE", excecaoPromocaoRetorno.get("descMercPrazoVare").getAsDouble());
                                            dadosExcecaoPromocao.put("DESC_MERC_PRAZO_ATAC", excecaoPromocaoRetorno.get("descMercPrazoAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("DESC_SERV_VISTA", excecaoPromocaoRetorno.get("descServVista").getAsDouble());
                                            dadosExcecaoPromocao.put("DESC_SERV_PRAZO", excecaoPromocaoRetorno.get("descServPrazo").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_MERC_VISTA_VARE", excecaoPromocaoRetorno.get("comMercVistaVare").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_MERC_VISTA_ATAC", excecaoPromocaoRetorno.get("comMercVistaAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_MERC_PRAZO_VARE", excecaoPromocaoRetorno.get("comMercPrazoVare").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_MERC_PRAZO_ATAC", excecaoPromocaoRetorno.get("comMercPrazoAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_SERV_VISTA", excecaoPromocaoRetorno.get("comServVista").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_SERV_PRAZO", excecaoPromocaoRetorno.get("comServPrazo").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_MERC_VISTA_VARE", excecaoPromocaoRetorno.get("comExtMercVistaVare").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_MERC_VISTA_ATAC", excecaoPromocaoRetorno.get("comExtMercVistaAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_MERC_PRAZO_VARE", excecaoPromocaoRetorno.get("comExtMercPrazoVare").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_MERC_PRAZO_ATAC", excecaoPromocaoRetorno.get("comExtMercPrazoAtac").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_SERV_VISTA", excecaoPromocaoRetorno.get("comExtServVista").getAsDouble());
                                            dadosExcecaoPromocao.put("COM_EXT_SERV_Prazo", excecaoPromocaoRetorno.get("comExtServPrazo").getAsDouble());

                                            listaExcecaoPromocao.add(dadosExcecaoPromocao);
                                        }
                                        ItemExcaoPromocaoSql itemExcaoPromocaoSql = new ItemExcaoPromocaoSql(context);

                                        itemExcaoPromocaoSql.delete(null);

                                        todosSucesso = itemExcaoPromocaoSql.insertList(listaExcecaoPromocao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEXTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAEXTBP");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Exceção Promoção : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosExcecaoPromocao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosTabelaItemPromocao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Relacionamento da Promoção");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Relacionamento da Promoção");
                }
            });
        }
        try {
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAEMTBP");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            String parametrosWebservice = "";
            String filtroPromocao = "(ID_AEATBPRO IN (SELECT ID_AEATBPRO FROM AEATBPRO WHERE ((DT_INICIO <= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)) AND (DT_FIM >= (SELECT CAST('NOW' AS DATE) FROM RDB$DATABASE)))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtroPromocao;
            } else {
                parametrosWebservice += "&where= " + filtroPromocao;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaRelacaoPromocaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaRelacaoPromocaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_tabela_promocao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaRelacaoPromocaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_relacao_promocao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaRelacaoPromocaoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaRelacaoPromocao = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaRelacaoPromocaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_relacao_promocao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaRelacaoPromocaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaRelacaoPromocaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_relacao_promocao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaRelacaoPromocaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject relacaoPromocaoRetorno = listaRelacaoPromocaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosRelacaoPromocao = new ContentValues();

                                            dadosRelacaoPromocao.put("ID_AEAEMTBP", relacaoPromocaoRetorno.get("idAeaemtbp").getAsInt());
                                            dadosRelacaoPromocao.put("ID_AEATBPRO", relacaoPromocaoRetorno.get("idAeatbpro").getAsInt());
                                            dadosRelacaoPromocao.put("ID_SMAEMPRE", relacaoPromocaoRetorno.get("idSmaempre").getAsInt());
                                            dadosRelacaoPromocao.put("GUID", relacaoPromocaoRetorno.get("guid").getAsString());
                                            if (relacaoPromocaoRetorno.has("usCad")) {
                                                dadosRelacaoPromocao.put("US_CAD", relacaoPromocaoRetorno.get("usCad").getAsString());
                                            }
                                            if (relacaoPromocaoRetorno.has("dtCad")) {
                                                dadosRelacaoPromocao.put("DT_CAD", relacaoPromocaoRetorno.get("dtCad").getAsString());
                                            }
                                            if (relacaoPromocaoRetorno.has("dtAlt")) {
                                                dadosRelacaoPromocao.put("DT_ALT", relacaoPromocaoRetorno.get("dtAlt").getAsString());
                                            }
                                            if (relacaoPromocaoRetorno.has("ctInteg") && relacaoPromocaoRetorno.get("ctInteg").getAsInt() > 0) {
                                                dadosRelacaoPromocao.put("CT_INTEG", relacaoPromocaoRetorno.get("ctInteg").getAsInt());
                                            }

                                            listaRelacaoPromocao.add(dadosRelacaoPromocao);
                                        }
                                        TabelaItemPromocaoSql tabelaItemPromocaoSql = new TabelaItemPromocaoSql(context);

                                        tabelaItemPromocaoSql.delete(null);

                                        todosSucesso = tabelaItemPromocaoSql.insertList(listaRelacaoPromocao);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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
                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMTBP, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAEMTBP");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Tabela por Item da Promoção : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosRelacaoPromocao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosParcela() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Títulos à Receber/Pagar");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Títulos à Receber/Pagar");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("RPAPARCE");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraParcela =
                    "(RPAPARCE.ID_CFACLIFO IN (SELECT CFACLIFO.ID_CFACLIFO FROM CFACLIFO WHERE CFACLIFO.ID_CFACLIFO IN \n" +
                            "(SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO_VENDE = " +
                            "(SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraParcela;
            } else {
                parametrosWebservice += "&where= " + filtraParcela + " AND (RPAPARCE.DT_BAIXA IS NULL)";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {

                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaParcelaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaParcelaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_parcela));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaParcelaRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parcela));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaParcelaRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosParcela = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaParcelaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_parcela) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaParcelaRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaParcelaRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parcela) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaParcelaRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject parcelaRetorno = listaParcelaRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosParcela = new ContentValues();

                                            dadosParcela.put("ID_RPAPARCE", parcelaRetorno.get("idRpaparce").getAsInt());
                                            if (parcelaRetorno.has("idSmaempre") && parcelaRetorno.get("idSmaempre").getAsInt() > 0) {
                                                dadosParcela.put("ID_SMAEMPRE", parcelaRetorno.get("idSmaempre").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idRpafatur") && parcelaRetorno.get("idRpafatur").getAsInt() > 0) {
                                                dadosParcela.put("ID_RPAFATUR", parcelaRetorno.get("idRpafatur").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfaclifo") && parcelaRetorno.get("idCfaclifo").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFACLIFO", parcelaRetorno.get("idCfaclifo").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfatpdoc") && parcelaRetorno.get("idCfatpdoc").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFATPDOC", parcelaRetorno.get("idCfatpdoc").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfatpcob") && parcelaRetorno.get("idCfatpcob").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFATPCOB", parcelaRetorno.get("idCfatpcob").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfaporta") && parcelaRetorno.get("idCfaporta").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFAPORTA", parcelaRetorno.get("idCfaporta").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfaccred") && parcelaRetorno.get("idCfaccred").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFACCRED", parcelaRetorno.get("idCfaccred").getAsInt());
                                            }
                                            dadosParcela.put("DT_ALT", parcelaRetorno.get("dtAlt").getAsString());
                                            dadosParcela.put("TIPO", (parcelaRetorno.has("tipo") && parcelaRetorno.get("tipo") != null) ? parcelaRetorno.get("tipo").getAsString() : null);
                                            dadosParcela.put("DT_EMISSAO", (parcelaRetorno.has("tipo") && parcelaRetorno.get("tipo") != null) ? parcelaRetorno.get("dtEmissao").getAsString() : null);
                                            dadosParcela.put("DT_VENCIMENTO", (parcelaRetorno.has("tipo") && parcelaRetorno.get("tipo") != null) ? parcelaRetorno.get("dtVencimento").getAsString() : null);
                                            if (parcelaRetorno.has("dtPagamento")) {
                                                dadosParcela.put("DT_PAGAMENTO", parcelaRetorno.get("dtPagamento").getAsString());
                                            }
                                            if (parcelaRetorno.has("dtBaixa")) {
                                                dadosParcela.put("DT_BAIXA", parcelaRetorno.get("dtBaixa").getAsString());
                                            }
                                            if (parcelaRetorno.has("parcela")) {
                                                dadosParcela.put("PARCELA", parcelaRetorno.get("parcela").getAsInt());
                                            }
                                            dadosParcela.put("VL_PARCELA", parcelaRetorno.get("vlParcela").getAsDouble());
                                            dadosParcela.put("VL_JUROS_PRORROG", parcelaRetorno.get("vlJurosProrrog").getAsDouble());
                                            if (parcelaRetorno.has("fcVlTotalPago")) {
                                                dadosParcela.put("FC_VL_TOTAL_PAGO", parcelaRetorno.get("fcVlTotalPago").getAsDouble());
                                            }
                                            if (parcelaRetorno.has("fcVlRestante")) {
                                                dadosParcela.put("FC_VL_RESTANTE", parcelaRetorno.get("fcVlRestante").getAsDouble());
                                            }
                                            if (parcelaRetorno.has("fcVlRestanteSemProrrog")) {
                                                dadosParcela.put("FC_VL_RESTANTE_SEM_PRORROG", parcelaRetorno.get("fcVlRestanteSemProrrog").getAsDouble());
                                            }
                                            dadosParcela.put("VL_JUROS_DIARIO", parcelaRetorno.get("vlJurosDiario").getAsDouble());
                                            dadosParcela.put("TAXA_DIARIA", parcelaRetorno.get("taxaDiaria").getAsDouble());
                                            if (parcelaRetorno.has("capitaliza")) {
                                                dadosParcela.put("CAPITALIZA", parcelaRetorno.get("capitaliza").getAsString());
                                            }
                                            if (parcelaRetorno.has("prorrogado")) {
                                                dadosParcela.put("PRORROGADO", parcelaRetorno.get("prorrogado").getAsString());
                                            }
                                            if (parcelaRetorno.has("percDesconto")) {
                                                dadosParcela.put("PERC_DESCONTO", parcelaRetorno.get("percDesconto").getAsDouble());
                                            }
                                            dadosParcela.put("SEQUENCIAL", parcelaRetorno.get("sequencial").getAsString());
                                            if (parcelaRetorno.has("numero")) {
                                                dadosParcela.put("NUMERO", parcelaRetorno.get("numero").getAsString());
                                            }
                                            if (parcelaRetorno.has("obs")) {
                                                dadosParcela.put("OBS", parcelaRetorno.get("obs").getAsString());
                                            }
                                            listaDadosParcela.add(dadosParcela);
                                        }
                                        ParcelaSql parcelaSql = new ParcelaSql(context);

                                        todosSucesso = parcelaSql.insertList(listaDadosParcela);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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

                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_parcela)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_parcela))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for page
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("RPAPARCE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Parcela : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosParcela - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarDadosLancamentoParcela() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Laçamento de Títulos");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Laçamento de Títulos");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("RPALCPAR");
            String ultimaDataParcela = pegaUltimaDataAtualizacao("RPAPARCE");
            // Cria uma variavel para salvar todos os paramentros em json
            //String parametrosWebservice = "";
            StringBuilder filtroParcela = new StringBuilder();
            filtroParcela.append("SELECT RPAPARCE.ID_RPAPARCE FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO IN (SELECT CFACLIFO.ID_CFACLIFO FROM CFACLIFO WHERE CFACLIFO.ID_CFACLIFO IN \n");
            filtroParcela.append("(SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO_VENDE = \n" );
            filtroParcela.append("(SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))))");

            if ((ultimaDataParcela != null) && (!ultimaDataParcela.isEmpty())){
                filtroParcela.append(" AND (RPAPARCE.DT_ALT >= '").append(ultimaDataParcela).append("')");
            } else {
                filtroParcela.append(" AND (RPAPARCE.DT_BAIXA IS NULL)");
            }
            StringBuilder filtroLancamento = new StringBuilder();
            filtroLancamento.append("&where= ");
            filtroLancamento.append("RPALCPAR.ID_RPAPARCE IN (").append(filtroParcela.toString()).append(")");

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                filtroLancamento.append(" AND (RPALCPAR.DT_ALT >= '").append(ultimaData).append("')");
            }
            Gson gson = new Gson();
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPALCPAR, WSSisinfoWebservice.METODO_GET, filtroLancamento.toString(), null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {

                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaLancamentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaLancamentoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_lacamento_parcela));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaLancamentoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_lacamento_parcela));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaLancamentoRetorno.size());
                                                }
                                            });
                                        }
                                        List<ContentValues> listaDadosParcela = new ArrayList<ContentValues>();
                                        for (int i = 0; i < listaLancamentoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_lacamento_parcela) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaLancamentoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaLancamentoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_lacamento_parcela) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaLancamentoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject parcelaRetorno = listaLancamentoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosParcela = new ContentValues();

                                            dadosParcela.put("ID_RPALCPAR", parcelaRetorno.get("idRpalcpar").getAsInt());
                                            dadosParcela.put("ID_RPAPARCE", parcelaRetorno.get("idRpaparce").getAsInt());
                                            if (parcelaRetorno.has("idCbaitmov") && parcelaRetorno.get("idCbaitmov").getAsInt() > 0) {
                                                dadosParcela.put("ID_CBAITMOV", parcelaRetorno.get("idCbaitmov").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCbaplcta") && parcelaRetorno.get("idCbaplcta").getAsInt() > 0) {
                                                dadosParcela.put("ID_CBAPLCTA", parcelaRetorno.get("idCbaplcta").getAsInt());
                                            }
                                            if (parcelaRetorno.has("idCfatpdocAnt") && parcelaRetorno.get("idCfatpdocAnt").getAsInt() > 0) {
                                                dadosParcela.put("ID_CFATPDOC_ANT", parcelaRetorno.get("idCfatpdocAnt").getAsInt());
                                            }
                                            dadosParcela.put("GUID", parcelaRetorno.get("guid").getAsString());
                                            dadosParcela.put("US_CAD", parcelaRetorno.get("usCad").getAsString());
                                            dadosParcela.put("DT_CAD", parcelaRetorno.get("dtCad").getAsString());
                                            dadosParcela.put("DT_ALT", parcelaRetorno.get("dtAlt").getAsString());
                                            dadosParcela.put("TIPO", (parcelaRetorno.has("tipo") && parcelaRetorno.get("tipo") != null) ? parcelaRetorno.get("tipo").getAsString() : null);
                                            if (parcelaRetorno.has("dtImportacao")) {
                                                dadosParcela.put("DT_IMPORTACAO", parcelaRetorno.get("dtImportacao").getAsString());
                                            }
                                            if (parcelaRetorno.has("dtMovimento")) {
                                                dadosParcela.put("DT_MOVIMENTO", parcelaRetorno.get("dtMovimento").getAsString());
                                            }
                                            dadosParcela.put("SEQUENCIA", parcelaRetorno.get("sequencia").getAsInt());
                                            if (parcelaRetorno.has("dC")) {
                                                dadosParcela.put("D_C", parcelaRetorno.get("dC").getAsString());
                                            }
                                            dadosParcela.put("VL_PAGO", parcelaRetorno.get("vlPago").getAsDouble());
                                            dadosParcela.put("VL_JUROS_PRORROG", parcelaRetorno.get("vlJurosProrrog").getAsDouble());
                                            dadosParcela.put("VL_JUROS", parcelaRetorno.get("vlJuros").getAsDouble());
                                            dadosParcela.put("VL_DESCONTO", parcelaRetorno.get("vlDesconto").getAsDouble());
                                            if (parcelaRetorno.has("fcVlPagoTotal")) {
                                                dadosParcela.put("FC_VL_PAGO_TOTAL", parcelaRetorno.get("fcVlPagoTotal").getAsDouble());
                                            }
                                            if (parcelaRetorno.has("obs")) {
                                                dadosParcela.put("OBS", parcelaRetorno.get("obs").getAsString());
                                            }

                                            listaDadosParcela.add(dadosParcela);
                                        }
                                        LancamentoParcelaSql parcelaSql = new LancamentoParcelaSql(context);

                                        todosSucesso = parcelaSql.insertList(listaDadosParcela);
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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

                                } else {
                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_lacamento_parcela))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPALCPAR, WSSisinfoWebservice.METODO_GET, filtroLancamento.toString()+ "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for page
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("RPALCPAR");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Lancamento de Parcela : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosLancamentoParcela - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }


    private void importarNovaVersao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa) + " Nova Versão");
        mBuilder.setStyle(bigTextStyle)
                .setProgress(0, 0, true);
        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Nova Versão");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("SMAVERPR");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            //String filtraVersao = "";
            Gson gson = new Gson();
            /*if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') ";
            } else {
                parametrosWebservice += "&where= " + filtraVersao + " AND (RPAPARCE.DT_BAIXA IS NULL)";
            }*/
            ServidoresBeans servidorCentral = new ServidoresBeans();
            servidorCentral.setIpServidor(ServicosWeb.IP_SERVIDOR_WEBSERVICE);
            servidorCentral.setPorta(Integer.parseInt(ServicosWeb.PORTA_JSON));

            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorCentral, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAVERPR, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {

                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if ( (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) && (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_ELEMENTS).getAsInt() > 0) ) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

                            statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                            // Verifica se retornou com sucesso
                            if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                                // Atualiza a notificacao
                                bigTextStyle.bigText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                mBuilder.setStyle(bigTextStyle);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                        }
                                    });
                                }
                                // Checa se retornou alguma coisa
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaVersaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaVersaoRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_versao));
                                        mBuilder.setStyle(bigTextStyle)
                                                .setProgress(listaVersaoRetorno.size(), 0, false);
                                        notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                        // Checo se o texto de status foi passado pro parametro
                                        if (textStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_versao));
                                                }
                                            });
                                        }
                                        if (progressBarStatus != null) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressBarStatus.setIndeterminate(false);
                                                    progressBarStatus.setMax(listaVersaoRetorno.size());
                                                }
                                            });
                                        }
                                        for (int i = 0; i < listaVersaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            bigTextStyle.bigText(context.getResources().getString(R.string.recebendo_dados_versao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaVersaoRetorno.size());
                                            mBuilder.setStyle(bigTextStyle)
                                                    .setProgress(listaVersaoRetorno.size(), i, false);
                                            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_versao) + " - Parte " + finalPageNumber + "/" + totalPages + " - " + finalI1 + "/" + listaVersaoRetorno.size());
                                                    }
                                                });
                                            }
                                            if (progressBarStatus != null) {
                                                final int finalI = i;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressBarStatus.setProgress(finalI);
                                                    }
                                                });
                                            }
                                            JsonObject versaoRetorno = listaVersaoRetorno.get(i).getAsJsonObject();
                                            ContentValues dadosVersao = new ContentValues();

                                            dadosVersao.put("ID_RPAPARCE", versaoRetorno.get("idRpaparce").getAsInt());
                                            if (versaoRetorno.has("numeroVersao") && versaoRetorno.get("numeroVersao").getAsInt() > 0) {
                                                // Checa se tem alguma versao nova
                                                if (versaoRetorno.get("numeroVersao").getAsInt() > funcoes.getNumeroVersaoAplicacao() ){

                                                    //String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                                                    File pastaTemp = new File(Environment.getExternalStorageDirectory() + "/SAVARE/TEMP");
                                                    String nomeArquivo = "";

                                                    if (versaoRetorno.has("programa")){
                                                        nomeArquivo += versaoRetorno.get("programa").getAsString().replaceAll("[^a-zA-Z0-9]+","");
                                                    }else {
                                                        nomeArquivo += "SAVARE";
                                                    }
                                                    nomeArquivo += versaoRetorno.get("numeroVersao").getAsInt();

                                                    if(!pastaTemp.exists()){
                                                        pastaTemp.mkdirs();
                                                    }
                                                    URL url = new URL(versaoRetorno.get("urlArquivo").getAsString());
                                                    HttpURLConnection conection = (HttpURLConnection) url.openConnection();
                                                    conection.setRequestMethod("GET");
                                                    conection.connect();

                                                    File outputFile = new File(pastaTemp, nomeArquivo + ".apk");

                                                    if (!outputFile.exists()) {
                                                        outputFile.createNewFile();
                                                    }
                                                    FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                                                    InputStream is = conection.getInputStream();//Get InputStream for connection

                                                    byte[] buffer = new byte[1024];//Set buffer type
                                                    int len1 = 0;//init length
                                                    while ((len1 = is.read(buffer)) != -1) {
                                                        fos.write(buffer, 0, len1);//Write new file
                                                    }
                                                    //Close all connection after doing task
                                                    fos.close();
                                                    is.close();
                                                }
                                            }
                                        }
                                    }
                                    // Atualiza a notificacao
                                    bigTextStyle.bigText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, true);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR, mBuilder.build());

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
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

                                } else {
                                    // Cria uma notificacao para ser manipulado
                                    /*Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_parcela)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();*/

                                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados_parcela))
                                            .bigText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString());
                                    mBuilder.setStyle(bigTextStyle)
                                            .setProgress(0, 0, false);
                                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarWebserviceJson(servidorAtivo, null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAVERPR, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                                        .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                                mBuilder.setStyle(bigTextStyle)
                                        .setProgress(0, 0, false);
                                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                            }
                        } // Fim for page
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("SMAVERPR");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.recebendo_dados))
                            .bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString());
                    mBuilder.setStyle(bigTextStyle)
                            .setProgress(0, 0, false);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
                }
            }
        } catch (final Exception e) {
            if (textStatusErro != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatusErro.append("\n*" + context.getResources().getText(R.string.erro_inesperado) + " - Nova Versão : " + e.getMessage());
                    }
                });
            }
            bigTextStyle.setBigContentTitle(context.getResources().getString(R.string.importar_dados_recebidos))
                    .bigText("ImportaDadosVersao - " + e.getMessage());
            mBuilder.setStyle(bigTextStyle)
                    .setProgress(0, 0, false);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
        }
    }

    private void inserirUltimaAtualizacao(String tabela) {
        //TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String dataInicioAtualizacao = sdf.format(calendario.getTime());

        Calendar dtAlt = Calendar.getInstance();

        ContentValues dataAtualizacao = new ContentValues();
        //dataAtualizacao.put("ID_DISPOSITIVO", telephonyManager.getDeviceId());
        dataAtualizacao.put("TABELA", tabela);
        dataAtualizacao.put("DT_ALT", sdf.format(dtAlt.getTime()));
        dataAtualizacao.put("DATA_ULTIMA_ATUALIZACAO", dataInicioAtualizacao);

        UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);

        ultimaAtualizacaoSql.insertOrReplace(dataAtualizacao);
    }

    private List<PropertyInfo> criaPropriedadeDataAlteracaoWebservice(String tabela) {

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(tabela);

        if ((listaUltimaAtualizacaoDispositivo != null) && (listaUltimaAtualizacaoDispositivo.size() > 0) && (tabela != null) && (!tabela.isEmpty())) {

            // Passa pela lista de atualizacoes
            for (UltimaAtualizacaoBeans ultimaData : listaUltimaAtualizacaoDispositivo) {
                // Checa se a tabela da atualizacao eh a requerida por parametro
                if (ultimaData.getTabela().equalsIgnoreCase(tabela)) {

                    PropertyInfo propertyDataUltimaAtualizacao = new PropertyInfo();
                    propertyDataUltimaAtualizacao.setName("dataUltimaAtualizacao");
                    propertyDataUltimaAtualizacao.setValue(ultimaData.getDataUltimaAtualizacao());
                    propertyDataUltimaAtualizacao.setType(ultimaData.getDataUltimaAtualizacao().getClass());

                    // Cria uma lista para salvar todas as propriedades
                    List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                    // Adiciona a propriedade na lista
                    listaPropertyInfos.add(propertyDataUltimaAtualizacao);
                    return listaPropertyInfos;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    private String pegaUltimaDataAtualizacao(String tabela) {

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(tabela);

        if ((listaUltimaAtualizacaoDispositivo != null) && (listaUltimaAtualizacaoDispositivo.size() > 0) && (tabela != null) && (!tabela.isEmpty())) {

            // Passa pela lista de atualizacoes
            for (UltimaAtualizacaoBeans ultimaData : listaUltimaAtualizacaoDispositivo) {
                // Checa se a tabela da atualizacao eh a requerida por parametro
                if (ultimaData.getTabela().equalsIgnoreCase(tabela)) {

                    return ultimaData.getDataUltimaAtualizacao();
                }
            }
        } else {
            return null;
        }
        return null;
    }
}