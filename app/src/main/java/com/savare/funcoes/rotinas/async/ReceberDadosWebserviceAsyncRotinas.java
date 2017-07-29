package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.EnderecoSql;
import com.savare.banco.funcoesSql.EstadoSql;
import com.savare.banco.funcoesSql.EstoqueSql;
import com.savare.banco.funcoesSql.FatorSql;
import com.savare.banco.funcoesSql.FotosSql;
import com.savare.banco.funcoesSql.GradeSql;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.LocacaoSql;
import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
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
import com.savare.banco.funcoesSql.TipoClienteSql;
import com.savare.banco.funcoesSql.TipoDocumentoSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;
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

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by Bruno Nogueira Silva on 29/06/2016.
 */
public class ReceberDadosWebserviceAsyncRotinas extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String[] tabelaRecebeDados = null;
    private ProgressBar progressBarStatus = null;
    private TextView textStatus = null;
    private OnTaskCompleted listenerTaskCompleted;
    private Calendar calendario;
    private String[] idOrcamentoSelecionado = null;
    private List<String> listaGuidOrcamento = null;
    // Cria uma notificacao para ser manipulado
    Load mLoad;

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

    public interface OnTaskCompleted{
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

        mLoad = PugNotification.with(context).load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR)
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .title(R.string.importar_dados_recebidos)
                .flags(Notification.DEFAULT_LIGHTS);

        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_estamos_checando_se_existe_internet));
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getString(R.string.aguarde_estamos_checando_se_existe_internet));
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
        funcoes.setValorXml("RecebendoDados", "S");

        if (funcoes.existeConexaoInternet()) try {

            mLoad.bigTextStyle(R.string.estamos_checando_webservice_online);
            mLoad.progress().value(0, 0, true).build();

            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setVisibility(View.VISIBLE);
                        textStatus.setText(context.getResources().getText(R.string.estamos_checando_webservice_online));
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
            // Verifica se o servidor webservice esta online
            if (funcoes.pingWebserviceSisInfo()) {

                mLoad.bigTextStyle(R.string.checando_versao_savare);
                mLoad.progress().value(0, 0, true).build();

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setVisibility(View.VISIBLE);
                            textStatus.setText(context.getResources().getText(R.string.checando_versao_savare));
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
                // Importa os dados da empresa pprimeiro para checar a versao
                importaDadosEmpresa();

                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()) {

                    // Recebe os dados da tabela
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))) ||
                            (tabelaRecebeDados == null)) {

                        if (checaFuncionarioAtivo() == false){
                            return null;
                        }
                    }

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS))) ||
                            (tabelaRecebeDados == null)) {

                        importarDadosArea();
                    }

                    // Recebe os dados da tabela SMAEMPRES
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados da empresa
                        //importaDadosEmpresa();
                    }

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados da empresa
                        importarDadosAtividade();
                    }

                    // Recebe os dados da tabela CFASTATU
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosStatus();
                    }


                    // Recebe os dados da tabela CFATPDOC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPDOC))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosTipoDocumento();
                    }

                    // Recebe os dados da tabela CFACCRED
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACCRED))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosCartaoCredito();
                    }

                    // Recebe os dados da tabela CFAPORTA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPORTA))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosPortador();
                    }

                    // Recebe os dados da tabela CFAPORTA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPROFI))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosProfissao();
                    }

                    // Recebe os dados da tabela CFATPCLI
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPCLI))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosTipoCliente();
                    }

                    // Recebe os dados da tabela CFATPCOB
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPCOB))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosTipoCobranca();
                    }

                    // Recebe os dados da tabela CFAESTAD
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAESTAD))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosEstado();
                    }

                    // Recebe os dados da tabela CFACIDAD
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACIDAD))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosCidade();
                    }

                    // Recebe os dados da tabela CFACLIFO
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACLIFO))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosClifo();
                    }

                    // Recebe os dados da tabela CFAENDER
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAENDER))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosEndereco();
                    }

                    // Recebe os dados da tabela CFAPARAM
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPARAM))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosParametros();
                    }

                    // Recebe os dados da tabela CFAFOTOS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAFOTOS))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosFotos();
                    }

                    // Recebe os dados da tabela AEAPLPGT
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPLPGT))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosPlanoPagamento();
                    }

                    // Recebe os dados da tabela AEACLASE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEACLASE))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosClasseProdutos();
                    }

                    // Recebe os dados da tabela AEAUNVEN
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAUNVEN))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosUnidadeVenda();
                    }

                    // Recebe os dados da tabela AEAUNVEN
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAGRADE))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosGrade();
                    }

                    // Recebe os dados da tabela AEAMARCA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAMARCA))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosMarca();
                    }

                    // Recebe os dados da tabela AEACODST
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEACODST))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosCodigoSituacaoTributaria();
                    }

                    // Recebe os dados da tabela AEAPRODU
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPRODU))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosProduto();
                    }

                    // Recebe os dados da tabela AEAPRECO
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPRECO))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosPreco();
                    }

                    // Recebe os dados da tabela AEAEMBAL
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAEMBAL))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosEmbalagem();
                    }

                    // Recebe os dados da tabela AEAPLOJA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPLOJA))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosProdutosPorLoja();
                    }

                    // Recebe os dados da tabela AEALOCES
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEALOCES))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosLocalEstoque();
                    }

                    // Recebe os dados da tabela AEAESTOQ
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAESTOQ))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosEstoque();
                    }

                    // Recebe os dados da tabela AEAORCAM
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAORCAM))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosOrcamento();
                    }

                    // Recebe os dados da tabela AEAITORC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        //importarDadosItemOrcamento();
                    }

                    // Recebe os dados da tabela AEAPERCE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPERCE))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosPercentual();
                    }

                    // Recebe os dados da tabela AEAFATOR
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAFATOR))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosFator();
                    }

                    // Recebe os dados da tabela AEAPRREC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPRREC))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosProdutoRecomendado();
                    }

                    // Recebe os dados da tabela RPAPARCE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_RPAPARCE))) ||
                            (tabelaRecebeDados == null)) {

                        // Importa os dados
                        importarDadosParcela();
                    }

                } else {
                    // Armazena as informacoes para para serem exibidas e enviadas
                    final ContentValues contentValues = new ContentValues();
                    contentValues.put("comando", 0);
                    contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                    contentValues.put("mensagem", context.getResources().getString(R.string.nao_conseguimos_validar_versao));
                    contentValues.put("dados", "");
                    // Pega os dados do usuario

                    contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                    contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    contentValues.put("email", funcoes.getValorXml("Email"));

                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            funcoes.menssagem(contentValues);
                        }
                    });
                }
            } else {
                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                contentValues.put("mensagem", context.getResources().getString(R.string.aparentemente_servidor_webservice_offline));
                contentValues.put("dados", "");
                // Pega os dados do usuario

                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                contentValues.put("email", funcoes.getValorXml("Email"));

                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        funcoes.menssagem(contentValues);
                    }
                });
            }
        } catch (final Exception e) {

            // Atualiza a notificacao
            mLoad.bigTextStyle(context.getResources().getString(R.string.msg_error) + ": " + e.getMessage());
            mLoad.simple().build();

            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText(context.getResources().getString(R.string.msg_error) + e.getMessage());
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

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());
            // Pega os dados do usuario

            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } else {

            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR)
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("Não conseguimos identificar uma conexão de internet")
                    .smallIcon(R.drawable.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .flags(Notification.DEFAULT_LIGHTS)
                    .simple()
                    .build();

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
            mensagem.put("tela", "ReceberDadosWebServiceAsyncRotinas");
            mensagem.put("mensagem", (context.getResources().getString((R.string.nao_existe_conexao_internet))));

            funcoes.menssagem(mensagem);
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

        // Cria uma notificacao para ser manipulado
        PugNotification.with(context)
                .load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR)
                .title(R.string.importar_dados_recebidos)
                .message(context.getResources().getString(R.string.terminamos_atualizacao))
                //.bigTextStyle(context.getResources().getString(R.string.atualizado_sucesso))
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .flags(Notification.DEFAULT_LIGHTS)
                .simple()
                .build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.terminamos));
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
    }

    private void salvarDadosXml(ContentValues usuario){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        if(usuario.containsKey("CODIGO_FUN")) {
            funcoes.setValorXml("CodigoUsuario", usuario.getAsString("CODIGO_FUN"));
        }
        if(usuario.containsKey("ID_SMAEMPRE")) {
            funcoes.setValorXml("CodigoEmpresa", usuario.getAsString("ID_SMAEMPRE"));
        }
        if(usuario.containsKey("EMAIL")) {
            funcoes.setValorXml("Email", usuario.getAsString("EMAIL"));
        }
        funcoes.setValorXml("EnviarAutomatico", "N");
        funcoes.setValorXml("ReceberAutomatico", "N");
        funcoes.setValorXml("ImagemProduto", "N");
        funcoes.setValorXml("AbriuAppPriveiraVez", "S");
        if(usuario.containsKey("MODO_CONEXAO")) {
            funcoes.setValorXml("ModoConexao", usuario.getAsString("MODO_CONEXAO"));
        }
        if(usuario.containsKey("IP_SERVIDOR_WEBSERVICE")) {
            funcoes.setValorXml("IPServidorWebservice", usuario.getAsString("IP_SERVIDOR_WEBSERVICE"));
        }
        if(usuario.containsKey("IP_SERVIDOR_SISINFO")) {
            funcoes.setValorXml("IPServidor", usuario.getAsString("IP_SERVIDOR_SISINFO"));
        }
        if(usuario.containsKey("CAMINHO_BANCO_SISINFO")) {
            funcoes.setValorXml("CaminhoBancoDados", usuario.getAsString("CAMINHO_BANCO_SISINFO"));
        }
        if(usuario.containsKey("PORTA_BANCO_SISINFO")) {
            funcoes.setValorXml("PortaBancoDados", usuario.getAsString("PORTA_BANCO_SISINFO"));
        }
        if(usuario.containsKey("USUARIO_SISINFO_WEBSERVICE")) {
            funcoes.setValorXml("UsuarioServidor", usuario.getAsString("USUARIO_SISINFO_WEBSERVICE"));
        }
        if(usuario.containsKey("SENHA_SISINFO_WEBSERVICE")) {
            funcoes.setValorXml("SenhaServidor", funcoes.criptografaSenha(usuario.getAsString("SENHA_SISINFO_WEBSERVICE")));
        }
    }

    public void setProgressBarStatus(ProgressBar progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    public void setTextStatus(TextView textStatus) {
        this.textStatus = textStatus;
    }

    public void setIdOrcamento(String[] idOrcamentoSelecionado) { this.idOrcamentoSelecionado = idOrcamentoSelecionado; }

    public List<String> getListaGuidOrcamento() {return listaGuidOrcamento; }

    public void setListaGuidOrcamento(List<String> listaGuidOrcamento) { this.listaGuidOrcamento = listaGuidOrcamento; }


    private boolean checaFuncionarioAtivo(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Funcionário");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Funcionário");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String ultimaData = null, chaveFuncionario = null;

            // Cria uma variavel para salvar todos os paramentros para ser passado na url
            String parametrosWebservice = "";
            chaveFuncionario = funcoes.getValorXml("ChaveFuncionario");

            if ( (chaveFuncionario != null) && (!chaveFuncionario.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                // Pega quando foi a ultima data que recebeu dados
                ultimaData = pegaUltimaDataAtualizacao("CFACLIFO_FUN");

                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (GUID = '" + chaveFuncionario + "')";
                } else {
                    parametrosWebservice += "&where= (GUID = '" + chaveFuncionario + "')";
                }
            }
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            //JsonObject objectParametros = new JsonObject();

            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaUsuarioRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        boolean todosSucesso = true;

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_usuario));
                        mLoad.progress().value(0, 0, true).build();

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
                        for(int i = 0; i < listaUsuarioRetorno.size(); i++) {

                            final JsonObject usuarioRetorno = listaUsuarioRetorno.get(i).getAsJsonObject();

                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Usuário: " + usuarioRetorno.get("nomeRazao").toString());
                            //mLoad.progress().update(0, i, listaUsuarioRetorno.size(), false).build();
                            mLoad.progress().value(0, 0, true).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Usuário: " + usuarioRetorno.get("nomeRazao").toString());
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

                                final ContentValues contentValues = new ContentValues();
                                contentValues.put("comando", 0);
                                contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                contentValues.put("mensagem", "O usuário dessa chave esta inativo, não podemos baixar os dados dele. Entre em contato com o suporte SAVARE.");
                                contentValues.put("dados", "");
                                // Pega os dados do usuario

                                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                                contentValues.put("empresa", funcoes.getValorXml("ChaveFuncionario"));
                                contentValues.put("email", funcoes.getValorXml("Email"));

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        funcoes.menssagem(contentValues);
                                    }
                                });
                                return false;
                            } else {
                                final ContentValues dadosUsuario = new ContentValues();
                                dadosUsuario.put("ID_CFACLIFO", usuarioRetorno.get("idCfaclifo").getAsInt());
                                dadosUsuario.put("ID_CFAPROFI", (usuarioRetorno.has("idCfaprofi")) ? usuarioRetorno.get("idCfaprofi").getAsInt() : null);
                                dadosUsuario.put("ID_CFAATIVI", (usuarioRetorno.has("idCfaativi")) ? usuarioRetorno.get("idCfaativi").getAsInt() : null);
                                dadosUsuario.put("ID_CFAAREAS", (usuarioRetorno.has("idCfaareas")) ? usuarioRetorno.get("idCfaareas").getAsInt() : null);
                                dadosUsuario.put("ID_CFATPCLI", (usuarioRetorno.has("idCfatpcli")) ? usuarioRetorno.get("idCfatpcli").getAsInt() : null);
                                dadosUsuario.put("ID_CFASTATU", (usuarioRetorno.has("idCfastatu")) ? usuarioRetorno.get("idCfastatu").getAsInt() : null);
                                dadosUsuario.put("ID_SMAEMPRE", usuarioRetorno.get("idSmaempre").getAsInt());
                                dadosUsuario.put("DT_ALT", usuarioRetorno.get("dtAlt").getAsString());
                                dadosUsuario.put("GUID", usuarioRetorno.get("guid").getAsString());
                                dadosUsuario.put("CPF_CNPJ", usuarioRetorno.get("cpfCgc").getAsString());
                                dadosUsuario.put("IE_RG", usuarioRetorno.get("ieRg").getAsString());
                                dadosUsuario.put("NOME_RAZAO", usuarioRetorno.get("nomeRazao").getAsString());
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
                                dadosUsuario.put("ATIVO", usuarioRetorno.get("ativo").getAsString());

                                salvarDadosXml(dadosUsuario);

                                listaDadosUsuario.add(dadosUsuario);
                            }
                        }
                        /*if (!funcoes.getValorXml("SenhaUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                            // Inclui a senha no registro do banco de dados
                            dadosUsuario.put("SENHA_USUA", funcoes.getValorXml("SenhaUsuario"));
                        }

                        if (!funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                            // Inclui a senha no registro do banco de dados
                            dadosUsuario.put("LOGIN_USUA", funcoes.getValorXml("Usuario"));
                        }
                        final UsuarioSQL usuarioSql = new UsuarioSQL(context); */

                        final PessoaSql pessoaSql = new PessoaSql(context);

                        todosSucesso = pessoaSql.insertList(listaDadosUsuario);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACLIFO_FUN");
                        } else {
                            PugNotification.with(context)
                                    .load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR)
                                    .title(R.string.importar_dados_recebidos)
                                    //.message(context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario))
                                    .bigTextStyle(context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .flags(Notification.DEFAULT_LIGHTS)
                                    .simple()
                                    .build();

                            // Checa se o texto de status foi passado pro parametro
                            if (textStatus != null) {

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.nao_conseguimos_atualizar_usuario));
                                    }
                                });
                            }
                        }

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados_usuario)
                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            } else {
                // Cria uma notificacao para ser manipulado
                Load mLoad = PugNotification.with(context).load()
                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(R.mipmap.ic_launcher)
                        .title(R.string.recebendo_dados)
                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                        .flags(Notification.DEFAULT_LIGHTS);
                mLoad.simple().build();
            }
        }catch (Exception e){
            // Cria uma notificacao
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEmpresa- " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
        return true;
    }


    private void importaDadosEmpresa(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Empresa");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml("ChaveFuncionario") + "'))";
            } else {
                parametrosWebservice += "&where= (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml("ChaveFuncionario") + "'))";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            //JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaEmpresaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        boolean todosSucesso = true;
                        final List<ContentValues> listaDadosEmpresa = new ArrayList<ContentValues>();

                        for(int i = 0; i < listaEmpresaRetorno.size(); i++) {

                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + i + "/" + listaEmpresaRetorno.size());
                            mLoad.progress().update(0, i, listaEmpresaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + finalI + "/" + listaEmpresaRetorno.size());
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
                            if (empresaRetorno.has("idAeaplpgtAtac")) {
                                dadosEmpresa.put("ID_AEAPLPGT_ATAC", empresaRetorno.get("idAeaplpgtAtac").getAsInt());
                            }
                            dadosEmpresa.put("DT_ALT", empresaRetorno.get("dtAlt").getAsString());
                            dadosEmpresa.put("NOME_RAZAO", empresaRetorno.get("nomeRazao").getAsString());
                            dadosEmpresa.put("NOME_FANTASIA", empresaRetorno.has("nomeFantasia") ? empresaRetorno.get("nomeFantasia").getAsString() : "");
                            dadosEmpresa.put("CPF_CGC", empresaRetorno.has("cpfCgc") ? empresaRetorno.get("cpfCgc").getAsString() : "");
                            dadosEmpresa.put("ORC_SEM_ESTOQUE", empresaRetorno.get("orcSemEstoque").getAsString());
                            dadosEmpresa.put("DIAS_ATRAZO", empresaRetorno.get("diasAtrazo").getAsInt());
                            dadosEmpresa.put("SEM_MOVIMENTO", empresaRetorno.get("semMovimento").getAsInt());
                            dadosEmpresa.put("JUROS_DIARIO", empresaRetorno.get("jurosDiario").getAsDouble());
                            dadosEmpresa.put("VENDE_BLOQUEADO_ORC", empresaRetorno.has("vendeBloqueadoOrc") ? empresaRetorno.get("vendeBloqueadoOrc").getAsString() : "");
                            dadosEmpresa.put("VENDE_BLOQUEADO_PED", empresaRetorno.has("vendeBloqueadoPed") ? empresaRetorno.get("vendeBloqueadoPed").getAsString() : "");
                            dadosEmpresa.put("VALIDADE_FICHA_CLIENTE", empresaRetorno.get("validadeFichaCliente").getAsInt());
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
                            dadosEmpresa.put("IP_SERVIDOR_SISINFO", (empresaRetorno.has("ipServidorSisinfo")) ? empresaRetorno.get("ipServidorSisinfo").getAsString() : "");
                            dadosEmpresa.put("IP_SERVIDOR_WEBSERVICE", (empresaRetorno.has("ipServidorWebservice")) ? empresaRetorno.get("ipServidorWebservice").getAsString() : "");
                            dadosEmpresa.put("USUARIO_SISINFO_WEBSERVICE", (empresaRetorno.has("usuSisinfoWebservice")) ? empresaRetorno.get("usuSisinfoWebservice").getAsString() : "");
                            dadosEmpresa.put("SENHA_SISINFO_WEBSERVICE", (empresaRetorno.has("senhaSisinfoWebservice")) ? funcoes.criptografaSenha(empresaRetorno.get("senhaSisinfoWebservice").getAsString()) : "");
                            dadosEmpresa.put("MODO_CONEXAO", (empresaRetorno.has("modoConexao")) ? empresaRetorno.get("modoConexao").getAsString() : "W");
                            dadosEmpresa.put("CAMINHO_BANCO_SISINFO", (empresaRetorno.has("caminhoBancoDados")) ? empresaRetorno.get("caminhoBancoDados").getAsString() : "");
                            dadosEmpresa.put("PORTA_BANCO_SISINFO", (empresaRetorno.has("portaBancoDados")) ? empresaRetorno.get("portaBancoDados").getAsString() : "");

                            salvarDadosXml(dadosEmpresa);

                            listaDadosEmpresa.add(dadosEmpresa);
                        }
                        EmpresaSql empresaSql = new EmpresaSql(context);

                        todosSucesso = empresaSql.insertList(listaDadosEmpresa);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("SMAEMPRE");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_empresa)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
            /*final Vector<SoapObject> listaEmpresaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE, criaPropriedadeDataAlteracaoWebservice("SMAEMPRE"));

            // Checa se retornou alguma coisa
            if (listaEmpresaObject != null && listaEmpresaObject.size() > 0) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaEmpresaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaEmpresaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaEmpresa = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEmpresaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + (finalControle + 1) + "/" + listaEmpresaObject.size());
                    mLoad.progress().update(0, controle, listaEmpresaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + (finalControle + 1) + "/" + listaEmpresaObject.size());
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

                    final SoapObject objeto;

                    if (objetoIndividual.hasProperty("return")) {
                        objeto = (SoapObject) objetoIndividual.getProperty("return");

                    } else {
                        objeto = objetoIndividual;
                    }

                    // Cria variavel para salvar os dados da empresa e enviar para o banco de dados
                    ContentValues dadosEmpresa = new ContentValues();

                    // Inseri os valores
                    dadosEmpresa.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosEmpresa.put("DT_ALT", objeto.getProperty("dataAlt").toString());
                    dadosEmpresa.put("NOME_RAZAO", objeto.getProperty("nomeRazao").toString());
                    dadosEmpresa.put("NOME_FANTASIA", objeto.hasProperty("nomeFantasia") ? objeto.getProperty("nomeFantasia").toString() : "");
                    dadosEmpresa.put("CPF_CGC", objeto.hasProperty("cpfCnpj") ? objeto.getProperty("cpfCnpj").toString() : "");
                    dadosEmpresa.put("ORC_SEM_ESTOQUE", objeto.getProperty("orcamentoSemEstoque").toString());
                    dadosEmpresa.put("DIAS_ATRAZO", Integer.parseInt(objeto.getProperty("diasAtrazo").toString()));
                    dadosEmpresa.put("SEM_MOVIMENTO", Integer.parseInt(objeto.getProperty("semMovimento").toString()));
                    dadosEmpresa.put("JUROS_DIARIO", Double.parseDouble(objeto.getProperty("jurosDiario").toString()));
                    dadosEmpresa.put("VENDE_BLOQUEADO_ORC", objeto.hasProperty("vendeBloqueadoOrcamento") ? objeto.getProperty("vendeBloqueadoOrcamento").toString() : "");
                    dadosEmpresa.put("VENDE_BLOQUEADO_PED", objeto.hasProperty("vendeBloqueadoPedido") ? objeto.getProperty("vendeBloqueadoPedido").toString() : "");
                    dadosEmpresa.put("VALIDADE_FICHA_CLIENTE", Integer.parseInt(objeto.getProperty("validadeFichaCliente").toString()));
                    dadosEmpresa.put("VL_MIN_PRAZO_VAREJO", Double.parseDouble(objeto.getProperty("valorMinimoPrazoVarejo").toString()));
                    dadosEmpresa.put("VL_MIN_PRAZO_ATACADO", Double.parseDouble(objeto.getProperty("valorMinimoPrazoAtacado").toString()));
                    dadosEmpresa.put("VL_MIN_VISTA_VAREJO", Double.parseDouble(objeto.getProperty("valorMinimoVistaVarejo").toString()));
                    dadosEmpresa.put("VL_MIN_VISTA_ATACADO", Double.parseDouble(objeto.getProperty("valorMinimoVistaAtacado").toString()));
                    dadosEmpresa.put("MULTIPLOS_PLANOS", objeto.hasProperty("multiplosPlanos") ? objeto.getProperty("multiplosPlanos").toString() : "");
                    dadosEmpresa.put("QTD_DIAS_DESTACA_PRODUTO", Integer.parseInt(objeto.getProperty("quantidadeDiasDestacaProduto").toString()));
                    dadosEmpresa.put("QTD_CASAS_DECIMAIS", objeto.hasProperty("quantidadeCasasDecimais") ? Integer.parseInt(objeto.getProperty("quantidadeCasasDecimais").toString()) : 3);
                    dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_ATACADO", objeto.hasProperty("fechaVendaCreditoNegativoAtacado") ? objeto.getProperty("fechaVendaCreditoNegativoAtacado").toString() : "");
                    dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_VAREJO", objeto.hasProperty("fechaVendaCreditoNegativoVarejo") ? objeto.getProperty("fechaVendaCreditoNegativoVarejo").toString() : "");
                    dadosEmpresa.put("TIPO_ACUMULO_CREDITO_ATACADO", objeto.hasProperty("titpoAcumuloCreditoAtacado") ? objeto.getProperty("titpoAcumuloCreditoAtacado").toString() : "");
                    dadosEmpresa.put("TIPO_ACUMULO_CREDITO_VAREJO", objeto.hasProperty("titpoAcumuloCreditoVarejo") ? objeto.getProperty("titpoAcumuloCreditoVarejo").toString() : "");
                    dadosEmpresa.put("PERIODO_CREDITO_ATACADO", objeto.hasProperty("periodocrceditoAtacado") ? objeto.getProperty("periodocrceditoAtacado").toString() : "");
                    dadosEmpresa.put("PERIODO_CREDITO_VAREJO", objeto.hasProperty("periodocrceditoVarejo") ? objeto.getProperty("periodocrceditoVarejo").toString() : "");

                    listaEmpresa.add(dadosEmpresa);
                    //EmpresaSql empresaSql = new EmpresaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = empresaSql.construirSqlStatement(dadosEmpresa);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = empresaSql.argumentoStatement(dadosEmpresa);

                    //Log.i("SAVARE", "ReceberDadosWebserviceAsyncRotinas");

                    *//*if (empresaSql.insertOrReplace(dadosEmpresa) <= 0){
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //areasSql.insertOrReplace(dadosAreas);
                            empresaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                EmpresaSql empresaSql = new EmpresaSql(context);

                todosSucesso = empresaSql.insertList(listaEmpresa);

                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso){
                    inserirUltimaAtualizacao("SMAEMPRE");
                }

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                mLoad.progress().value(0, 0, true).build();

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
        }catch (Exception e){
            // Cria uma notificacao
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEmpresa- " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosArea(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Área de Atuação");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaAreasRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_areas));
                        mLoad.progress().value(0, listaAreasRetorno.size(), false).build();

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
                        for(int i = 0; i < listaAreasRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_areas) + " - " + i + "/" + listaAreasRetorno.size());
                            mLoad.progress().update(0, i, listaAreasRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_areas) + " - " + finalI1 + "/" + listaAreasRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAAREAS");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_areas)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }

        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosArea - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosAtividade(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Ramo de Atividade");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaAtividadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_atividade));
                        mLoad.progress().value(0, listaAtividadeRetorno.size(), false).build();

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
                        for(int i = 0; i < listaAtividadeRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_atividade) + " - " + i + "/" + listaAtividadeRetorno.size());
                            mLoad.progress().update(0, i, listaAtividadeRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_atividade) + " - " + finalI1 + "/" + listaAtividadeRetorno.size());
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
                            if ((atividadeRetorno.has("descPromocao"))) {
                                dadosAtividade.put("DESC_PROMOCAO", atividadeRetorno.get("descPromocao").getAsString());
                            }
                            listaDadosAtividade.add(dadosAtividade);
                        } // FIm do for
                        RamoAtividadeSql ramoAtividadeSql = new RamoAtividadeSql(context);
                        todosSucesso = ramoAtividadeSql.insertList(listaDadosAtividade);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAATIVI");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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

                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_atividade)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }

        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosAtividade - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosStatus(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Status");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaStatusRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_status));
                        mLoad.progress().value(0, listaStatusRetorno.size(), false).build();

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
                        for(int i = 0; i < listaStatusRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_status) + " - " + i + "/" + listaStatusRetorno.size());
                            mLoad.progress().update(0, i, listaStatusRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_status) + " - " + finalI1 + "/" + listaStatusRetorno.size());
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
                            if ((statusObsRetorno.has("mensagem"))){
                                dadosStatus.put("MENSAGEM", statusObsRetorno.get("mensagem").getAsString());
                            }
                            if ((statusObsRetorno.has("bloqueia"))){
                                dadosStatus.put("BLOQUEIA", statusObsRetorno.get("bloqueia").getAsString());
                            }
                            if ((statusObsRetorno.has("parcelaEmAberto"))){
                                dadosStatus.put("PARCELA_EM_ABERTO", statusObsRetorno.get("parcelaEmAberto").getAsString());
                            }
                            if ((statusObsRetorno.has("vistaPrazo"))){
                                dadosStatus.put("VISTA_PRAZO", statusObsRetorno.get("vistaPrazo").getAsString());
                            }
                            dadosStatus.put("DESC_ATAC_VISTA", statusObsRetorno.get("descAtacVista").getAsDouble());
                            dadosStatus.put("DESC_ATAC_PRAZO", statusObsRetorno.get("descAtacPrazo").getAsDouble());
                            dadosStatus.put("DESC_VARE_VISTA", statusObsRetorno.get("descVareVista").getAsDouble());
                            dadosStatus.put("DESC_VARE_PRAZO", statusObsRetorno.get("descVarePrazo").getAsDouble());
                            if ((statusObsRetorno.has("descPromocao"))) {
                                dadosStatus.put("DESC_PROMOCAO", statusObsRetorno.get("descPromocao").getAsString());
                            }
                            listaDadosStatus.add(dadosStatus);
                        } // Fim for
                        StatusSql statusSql = new StatusSql(context);

                        todosSucesso = statusSql.insertList(listaDadosStatus);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFASTATU");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosStatus - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosTipoDocumento(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo Documento");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaTipoDocumentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_documento));
                        mLoad.progress().value(0, listaTipoDocumentoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaTipoDocumentoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - " + i + "/" + listaTipoDocumentoRetorno.size());
                            mLoad.progress().update(0, i, listaTipoDocumentoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - " + finalI1 + "/" + listaTipoDocumentoRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPDOC");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosTipoDocumento - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosCartaoCredito(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cartão Credito");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaCartaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cartao));
                        mLoad.progress().value(0, listaCartaoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaCartaoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cartao) + " - " + i + "/" + listaCartaoRetorno.size());
                            mLoad.progress().update(0, i, listaCartaoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cartao) + " - " + finalI1 + "/" + listaCartaoRetorno.size());
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
                            dadosCartao.put("DT_ALT", cartaoRetorno.get("dtAlt").getAsString());
                            dadosCartao.put("CODIGO", cartaoRetorno.get("codigo").getAsInt());
                            dadosCartao.put("DESCRICAO", cartaoRetorno.get("descricao").getAsString());

                            listaDadosCartao.add(dadosCartao);
                        }
                        CartaoSql cartaoSql = new CartaoSql(context);

                        todosSucesso = cartaoSql.insertList(listaDadosCartao);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACCRED");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosCartaoCredito - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosPortador(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Portador (Banco)");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();


                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaPortadorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_portador));
                        mLoad.progress().value(0, listaPortadorRetorno.size(), false).build();

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
                        for(int i = 0; i < listaPortadorRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_portador) + " - " + i + "/" + listaPortadorRetorno.size());
                            mLoad.progress().update(0, i, listaPortadorRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_portador) + " - " + finalI1 + "/" + listaPortadorRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPORTA");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosPortador - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosProfissao(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Profissão");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaProfissaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_profisao));
                        mLoad.progress().value(0, listaProfissaoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaProfissaoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_profisao) + " - " + i + "/" + listaProfissaoRetorno.size());
                            mLoad.progress().update(0, i, listaProfissaoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_profisao) + " - " + finalI1 + "/" + listaProfissaoRetorno.size());
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
                            if (profissaoRetorno.has("descPromocao")){
                                dadosProfissao.put("DESC_PROMOCAO", profissaoRetorno.get("descPromocao").getAsString());
                            }
                            listaDadosProfissao.add(dadosProfissao);
                        } // Fim for
                        ProfissaoSql profissaoSql = new ProfissaoSql(context);

                        todosSucesso = profissaoSql.insertList(listaDadosProfissao);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPROFI");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosProfissao - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosTipoCliente(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo Cliente");
        mLoad.progress().value(0, 0, true).build();


        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaTipoClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cliente));
                        mLoad.progress().value(0, listaTipoClienteRetorno.size(), false).build();

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
                        for(int i = 0; i < listaTipoClienteRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - " + i + "/" + listaTipoClienteRetorno.size());
                            mLoad.progress().update(0, i, listaTipoClienteRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - " + finalI1 + "/" + listaTipoClienteRetorno.size());
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
                            if (tipoClienteRetorno.has("descPromocao")){
                                dadosTipoCliente.put("DESC_PROMOCAO", tipoClienteRetorno.get("descPromocao").getAsString());
                            }
                            if (tipoClienteRetorno.has("vendeAtacVarejo")){
                                dadosTipoCliente.put("VENDE_ATAC_VAREJO", tipoClienteRetorno.get("vendeAtacVarejo").getAsString());
                            }
                            listaDadosTipoCliente.add(dadosTipoCliente);
                        }

                        TipoClienteSql tipoClienteSql = new TipoClienteSql(context);

                        todosSucesso = tipoClienteSql.insertList(listaDadosTipoCliente);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCLI");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosTipoCliente - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosTipoCobranca(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo de Cobrança");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaTipoCobrancaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca));
                        mLoad.progress().value(0, listaTipoCobrancaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaTipoCobrancaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - " + i + "/" + listaTipoCobrancaRetorno.size());
                            mLoad.progress().update(0, i, listaTipoCobrancaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - " + finalI1 + "/" + listaTipoCobrancaRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCOB");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosTipoCobranca - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosEstado(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Estado");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraEstadoPorFuncionario =
                    "(CFAESTAD.ID_CFAESTAD IN " +
                            "(SELECT DISTINCT CFAENDER.ID_CFAESTAD FROM CFAENDER WHERE CFAENDER.ID_CFACLIFO IN (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                    "AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraEstadoPorFuncionario;
            } else {
                parametrosWebservice += "&where= " + filtraEstadoPorFuncionario;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaEstadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estado));
                        mLoad.progress().value(0, listaEstadoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaEstadoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estado) + " - " + i + "/" + listaEstadoRetorno.size());
                            mLoad.progress().update(0, i, listaEstadoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estado) + " - " + finalI1 + "/" + listaEstadoRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAESTAD");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEstado - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosCidade(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cidade");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraCidadePorFuncionario =
                    "(CFACIDAD.ID_CFAESTAD IN " +
                            "(SELECT DISTINCT CFAENDER.ID_CFAESTAD FROM CFAENDER WHERE CFAENDER.ID_CFACLIFO IN (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                    "AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")))))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraCidadePorFuncionario;
            } else {
                parametrosWebservice += "&where= " + filtraCidadePorFuncionario;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaCidadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cidade));
                        mLoad.progress().value(0, listaCidadeRetorno.size(), false).build();

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
                        for(int i = 0; i < listaCidadeRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cidade) + " - " + i + "/" + listaCidadeRetorno.size());
                            mLoad.progress().update(0, i, listaCidadeRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cidade) + " - " + finalI1 + "/" + listaCidadeRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACIDAD");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosCidade - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosClifo(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cliente e Fornecedor");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filraClientePorVendedor =
                    "((CFACLIFO.ID_CFACLIFO IN \n" +
                    "(SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + " AND \n" +
                    "CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + "))) \n" +
                    "OR (CFACLIFO.NOME_RAZAO LIKE '%CONSUMIDOR%FINAL%'))";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filraClientePorVendedor;
            } else {
                parametrosWebservice += "&where= " + filraClientePorVendedor;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente));
                        mLoad.progress().value(0, listaClienteRetorno.size(), false).build();

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
                        for(int i = 0; i < listaClienteRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente) + " - " + i + "/" + listaClienteRetorno.size());
                            mLoad.progress().update(0, i, listaClienteRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente) + " - " + finalI1 + "/" + listaClienteRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACLIFO");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (JsonParseException e){

            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosClifo - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        } catch (Exception e){

            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosClifo - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosBairro(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Bairro");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Bairro");
                }
            });
        }
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFABAIRO");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtro =
                    "CFABAIRO.ID_CFABAIRO IN \n" +
                    "    (SELECT DISTINCT CFAENDER.ID_CFABAIRO FROM CFAENDER WHERE \n" +
                    "        CFAENDER.ID_CFACLIFO IN \n" +
                    "            (SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO_VENDE = \n" +
                    "                (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")) AND CFAENDER.ID_CFABAIRO IS NOT NULL )";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtro;
            } else {
                parametrosWebservice += "&where= " + filtro;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFABAIRO, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaBairroRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco));
                        mLoad.progress().value(0, listaBairroRetorno.size(), false).build();

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
                                    progressBarStatus.setMax(listaBairroRetorno.size());
                                }
                            });
                        }
                        List<ContentValues> listaDadosBairro = new ArrayList<ContentValues>();
                        for(int i = 0; i < listaBairroRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + i + "/" + listaBairroRetorno.size());
                            mLoad.progress().update(0, i, listaBairroRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + finalI1 + "/" + listaBairroRetorno.size());
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
                            JsonObject bairroRetorno = listaBairroRetorno.get(i).getAsJsonObject();
                            ContentValues dadosBairro = new ContentValues();

                            dadosBairro.put("ID_CFABAIRO", bairroRetorno.get("idCfabairo").getAsInt());
                            dadosBairro.put("DT_ALT", bairroRetorno.get("dtAlt").getAsString());
                            dadosBairro.put("GUID", bairroRetorno.get("guid").getAsString());
                            if (bairroRetorno.has("descricao")){
                                dadosBairro.put("DESCRICAO", bairroRetorno.get("descricao").getAsString());
                            }
                            listaDadosBairro.add(dadosBairro);
                        }
                        EnderecoSql enderecoSql = new EnderecoSql(context);

                        todosSucesso = enderecoSql.insertList(listaDadosBairro);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFABAIRO");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (JsonParseException e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosBairro - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("" +
                            "" +
                            "ImportaDadosBairro - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

        }
    }


    private void importarDadosEndereco(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Endereço");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaEnderecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco));
                        mLoad.progress().value(0, listaEnderecoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaEnderecoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + i + "/" + listaEnderecoRetorno.size());
                            mLoad.progress().update(0, i, listaEnderecoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + finalI1 + "/" + listaEnderecoRetorno.size());
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
                            if (enderecoRetorno.has("idCfaclifo") && enderecoRetorno.get("idCfaclifo").getAsInt() > 0){
                                dadosEndereco.put("ID_CFACLIFO", enderecoRetorno.get("idCfaclifo").getAsInt());
                            }
                            if (enderecoRetorno.has("idSmaempre") && enderecoRetorno.get("idSmaempre").getAsInt() > 0){
                                dadosEndereco.put("ID_SMAEMPRE", enderecoRetorno.get("idSmaempre").getAsInt());
                            }
                            if (enderecoRetorno.has("idCfaestad")){
                                //JsonObject estado = enderecoRetorno.getAsJsonObject("estadoEndereco");
                                dadosEndereco.put("ID_CFAESTAD", enderecoRetorno.get("idCfaestad").getAsInt());
                            }
                            if (enderecoRetorno.has("idCfacidad")){
                                //JsonObject cidade = enderecoRetorno.getAsJsonObject("cidadeEndereco");
                                dadosEndereco.put("ID_CFACIDAD", enderecoRetorno.get("idCfacidad").getAsInt());
                            }
                            if (enderecoRetorno.has("cep")){
                                dadosEndereco.put("CEP", enderecoRetorno.get("cep").getAsString());
                            }
                            if (enderecoRetorno.has("bairro")){
                                dadosEndereco.put("BAIRRO", enderecoRetorno.get("bairro").getAsString());
                            }
                            if (enderecoRetorno.has("logradouro")){
                                dadosEndereco.put("LOGRADOURO", enderecoRetorno.get("logradouro").getAsString());
                            }
                            if (enderecoRetorno.has("numero")){
                                dadosEndereco.put("NUMERO", enderecoRetorno.get("numero").getAsString());
                            }
                            if (enderecoRetorno.has("complemento")){
                                dadosEndereco.put("COMPLEMENTO", enderecoRetorno.get("complemento").getAsString());
                            }
                            if (enderecoRetorno.has("email")){
                                dadosEndereco.put("EMAIL", enderecoRetorno.get("email").getAsString());
                            }
                            listaDadosEndereco.add(dadosEndereco);
                        }
                        EnderecoSql enderecoSql = new EnderecoSql(context);

                        todosSucesso = enderecoSql.insertList(listaDadosEndereco);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAENDER");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (JsonParseException e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEndereco - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEndereco - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

        }
    }


    private void importarDadosParametros(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Parâmetros");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            }else {
                parametrosWebservice += "&where= " + filtraParam;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaParametroRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parametro));
                        mLoad.progress().value(0, listaParametroRetorno.size(), false).build();

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
                        for(int i = 0; i < listaParametroRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parametro) + " - " + i + "/" + listaParametroRetorno.size());
                            mLoad.progress().update(0, i, listaParametroRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parametro) + " - " + finalI1 + "/" + listaParametroRetorno.size());
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
                            if (parametroRetorno.has("idCfaclifo") && parametroRetorno.get("idCfaclifo").getAsInt() > 0){
                                dadosParametros.put("ID_CFACLIFO", parametroRetorno.get("idCfaclifo").getAsInt());
                            }
                            if (parametroRetorno.has("idSmaempre") && parametroRetorno.get("idSmaempre").getAsInt() > 0){
                                dadosParametros.put("ID_SMAEMPRE", parametroRetorno.get("idSmaempre").getAsInt());
                            }
                            if (parametroRetorno.has("idCfaclifoVende") && parametroRetorno.get("idCfaclifoVende").getAsInt() > 0){
                                dadosParametros.put("ID_CFACLIFO_VENDE", parametroRetorno.get("idCfaclifoVende").getAsInt());
                            }
                            if (parametroRetorno.has("idCfatpcob") && parametroRetorno.get("idCfatpcob").getAsInt() > 0){
                                dadosParametros.put("ID_CFATPCOB", parametroRetorno.get("idCfatpcob").getAsInt());
                            }
                            if (parametroRetorno.has("idCfaporta") && parametroRetorno.get("idCfaporta").getAsInt() > 0){
                                dadosParametros.put("ID_CFAPORTA", parametroRetorno.get("idCfaporta").getAsInt());
                            }
                            if (parametroRetorno.has("idCfatpdoc") && parametroRetorno.get("idCfatpdoc").getAsInt() > 0){
                                dadosParametros.put("ID_CFATPDOC", parametroRetorno.get("idCfatpdoc").getAsInt());
                            }
                            if (parametroRetorno.has("idAeaplpgt") && parametroRetorno.get("idAeaplpgt").getAsInt() > 0){
                                dadosParametros.put("ID_AEAPLPGT", parametroRetorno.get("idAeaplpgt").getAsInt());
                            }
                            if (parametroRetorno.has("roteiro")){
                                dadosParametros.put("ROTEIRO", parametroRetorno.get("roteiro").getAsString());
                            }
                            if (parametroRetorno.has("frequencia")){
                                dadosParametros.put("FREQUENCIA", parametroRetorno.get("frequencia").getAsString());
                            }
                            if (parametroRetorno.has("diasAtrazo")){
                                dadosParametros.put("DIAS_ATRAZO", parametroRetorno.get("diasAtrazo").getAsString());
                            }
                            if (parametroRetorno.has("diasCarencia")){
                                dadosParametros.put("DIAS_CARENCIA", parametroRetorno.get("diasCarencia").getAsString());
                            }
                            if (parametroRetorno.has("vendeAtrazado")){
                                dadosParametros.put("VENDE_ATRAZADO", parametroRetorno.get("vendeAtrazado").getAsString());
                            }
                            if (parametroRetorno.has("descPromocao")){
                                dadosParametros.put("DESC_PROMOCAO", parametroRetorno.get("descPromocao").getAsString());
                            }
                            if (parametroRetorno.has("dtUltVisita")){
                                dadosParametros.put("DT_ULT_VISITA", parametroRetorno.get("dtUltVisita").getAsString());
                            }
                            if (parametroRetorno.has("dtUltEnvio")){
                                dadosParametros.put("DT_ULT_ENVIO", parametroRetorno.get("dtUltEnvio").getAsString());
                            }
                            if (parametroRetorno.has("dataUltimoRecebimento")){
                                dadosParametros.put("DT_ULT_RECEBTO", parametroRetorno.get("dataUltimoRecebimento").getAsString());
                            }
                            if (parametroRetorno.has("dtProximoContato")){
                                dadosParametros.put("DT_PROXIMO_CONTATO", parametroRetorno.get("dtProximoContato").getAsString());
                            }
                            if (parametroRetorno.has("atacadoVarejo")){
                                dadosParametros.put("ATACADO_VEREJO", parametroRetorno.get("atacadoVarejo").getAsString());
                            }
                            if (parametroRetorno.has("vistaPrazo")){
                                dadosParametros.put("VISTA_PRAZO", parametroRetorno.get("vistaPrazo").getAsString());
                            }
                            if (parametroRetorno.has("faturaVlMin")){
                                dadosParametros.put("FATURA_VL_MIN", parametroRetorno.get("faturaVlMin").getAsString());
                            }
                            if (parametroRetorno.has("parcelaEmAberto")){
                                dadosParametros.put("PARCELA_EM_ABERTO", parametroRetorno.get("parcelaEmAberto").getAsString());
                            }
                            if (parametroRetorno.has("limite")){
                                dadosParametros.put("LIMITE", parametroRetorno.get("limite").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoAtacadoVista")){
                                dadosParametros.put("DESC_ATAC_VISTA", parametroRetorno.get("descAtacVista").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoAtacadoPrazo")){
                                dadosParametros.put("DESC_ATAC_PRAZO", parametroRetorno.get("descAtacPrazo").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoVarejoVista")){
                                dadosParametros.put("DESC_VARE_VISTA", parametroRetorno.get("descVareVista").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoVarejoPrazo")){
                                dadosParametros.put("DESC_VARE_PRAZO", parametroRetorno.get("descVarePrazo").getAsDouble());
                            }
                            if (parametroRetorno.has("jurosDiario")){
                                dadosParametros.put("JUROS_DIARIO", parametroRetorno.get("jurosDiario").getAsDouble());
                            }
                            listaDadosStatus.add(dadosParametros);
                        }
                        ParametrosSql parametrosSql = new ParametrosSql(context);

                        todosSucesso = parametrosSql.insertList(listaDadosStatus);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPARAM");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosParametros - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosFotos(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Imagens");
        mLoad.progress().value(0, 0, true).build();
        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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

                // Pega quando foi a ultima data que recebeu dados
                String ultimaData = pegaUltimaDataAtualizacao("CFAFOTOS");
                // Cria uma variavel para salvar todos os paramentros em json
                String parametrosWebservice = "";
                String filtraFotos =
                        "(CFAFOTOS.ID_AEAPRODU IN (SELECT AEAPRODU.ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1'))";

                Gson gson = new Gson();
                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + filtraFotos;
                } else {
                    parametrosWebservice += "&where= " + filtraFotos;
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        boolean todosSucesso = true;

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mLoad.progress().value(0, 0, true).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                                }
                            });
                        }
                        // Checa se retornou alguma coisa
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                            final JsonArray listaFotosRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fotos));
                            mLoad.progress().value(0, listaFotosRetorno.size(), false).build();

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
                            for(int i = 0; i < listaFotosRetorno.size(); i++){
                                // Atualiza a notificacao
                                mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fotos) + " - " + i + "/" + listaFotosRetorno.size());
                                mLoad.progress().update(0, i, listaFotosRetorno.size(), false).build();

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    final int finalI1 = i;
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fotos) + " - " + finalI1 + "/" + listaFotosRetorno.size());
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
                                ContentValues dadosFotos = new ContentValues();

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
                                        dadosFotos.put("FOTO", Base64.decode(fotosRetorno.get("foto").getAsString(), Base64.DEFAULT));
                                    }
                                    listaDadosFotos.add(dadosFotos);
                                }
                            }
                            FotosSql fotosSql = new FotosSql(context);

                            todosSucesso = fotosSql.insertList(listaDadosFotos);

                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("CFAFOTOS");
                            }
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                            mLoad.progress().value(0, 0, true).build();

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
                        } else {
                            // Cria uma notificacao para ser manipulado
                            Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.versao_savare_desatualizada)
                                    .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                }
            }
        } catch (JsonParseException e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosFotos - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        } catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosFotos - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosPlanoPagamento(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Plano de Pagamento");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaPlanoPagamentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_plano_pagamento));
                        mLoad.progress().value(0, listaPlanoPagamentoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaPlanoPagamentoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - " + i + "/" + listaPlanoPagamentoRetorno.size());
                            mLoad.progress().update(0, i, listaPlanoPagamentoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - " + finalI1 + "/" + listaPlanoPagamentoRetorno.size());
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
                            dadosPagamento.put("DT_ALT", pagamentoRetorno.get("dtAlt").getAsString());
                            dadosPagamento.put("CODIGO", pagamentoRetorno.get("codigo").getAsInt());
                            dadosPagamento.put("DESCRICAO", pagamentoRetorno.get("descricao").getAsString());
                            dadosPagamento.put("ATIVO", pagamentoRetorno.get("ativo").getAsString());
                            if (pagamentoRetorno.has("origemValor")) {
                                dadosPagamento.put("ORIGEM_VALOR", pagamentoRetorno.get("origemValor").getAsString());
                            }
                            dadosPagamento.put("ATAC_VAREJO", pagamentoRetorno.get("atacVarejo").getAsString());
                            dadosPagamento.put("VISTA_PRAZO", pagamentoRetorno.get("vistaPrazo").getAsString());
                            dadosPagamento.put("PERC_DESC_ATAC", pagamentoRetorno.get("percDescAtac").getAsDouble());
                            dadosPagamento.put("PERC_DESC_VARE", pagamentoRetorno.get("percDescVare").getAsDouble());
                            if (pagamentoRetorno.has("descPromocao")) {
                                dadosPagamento.put("DESC_PROMOCAO", pagamentoRetorno.get("descPromocao").getAsString());
                            }
                            if (pagamentoRetorno.has("juroMedioAtac")) {
                                dadosPagamento.put("JURO_MEDIO_ATAC", pagamentoRetorno.get("juroMedioAtac").getAsDouble());
                            }
                            dadosPagamento.put("JURO_MEDIO_VARE", pagamentoRetorno.get("juroMedioVare").getAsDouble());
                            //dadosPagamento.put("DIAS_MEDIOS", pagamentoRetorno.get("diasMedios").getAsInt());
                            listaDadosPagamento.add(dadosPagamento);
                        }
                        PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                        todosSucesso = planoPagamentoSql.insertList(listaDadosPagamento);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLPGT");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosPlanoPagamento - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosClasseProdutos(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Classe de Produto");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaClasseRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_classe_produto));
                        mLoad.progress().value(0, listaClasseRetorno.size(), false).build();

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
                        for(int i = 0; i < listaClasseRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - " + i + "/" + listaClasseRetorno.size());
                            mLoad.progress().update(0, i, listaClasseRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - " + finalI1 + "/" + listaClasseRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACLASE");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosClasseProdutos - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosUnidadeVenda(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Unidade de Venda");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaUnidadeVendaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_unidade_venda));
                        mLoad.progress().value(0, listaUnidadeVendaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaUnidadeVendaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - " + i + "/" + listaUnidadeVendaRetorno.size());
                            mLoad.progress().update(0, i, listaUnidadeVendaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - " + finalI1 + "/" + listaUnidadeVendaRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAUNVEN");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosUnidadeVenda - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosGrade(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Grade de Produto");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaGradeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_grade));
                        mLoad.progress().value(0, listaGradeRetorno.size(), false).build();

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
                        for(int i = 0; i < listaGradeRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_grade) + " - " + i + "/" + listaGradeRetorno.size());
                            mLoad.progress().update(0, i, listaGradeRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_grade) + " - " + finalI1 + "/" + listaGradeRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAGRADE");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    }  else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosGrade - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosMarca(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Marca");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaMarcaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_marca));
                        mLoad.progress().value(0, listaMarcaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaMarcaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_marca) + " - " + i + "/" + listaMarcaRetorno.size());
                            mLoad.progress().update(0, i, listaMarcaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_marca) + " - " + finalI1 + "/" + listaMarcaRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAMARCA");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosMarca - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosCodigoSituacaoTributaria(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Situação Tributária");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaTributariaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria));
                        mLoad.progress().value(0, listaTributariaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaTributariaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - " + i + "/" + listaTributariaRetorno.size());
                            mLoad.progress().update(0, i, listaTributariaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - " + finalI1 + "/" + listaTributariaRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACODST");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosSituacaoTributaria - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosProduto(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaProdutoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto));
                        mLoad.progress().value(0, listaProdutoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaProdutoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto) + " - " + i + "/" + listaProdutoRetorno.size());
                            mLoad.progress().update(0, i, listaProdutoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto) + " - " + finalI1 + "/" + listaProdutoRetorno.size());
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
                            if (produtoRetorno.has("idAeaclase") && produtoRetorno.get("idAeaclase").getAsInt() > 0) {
                                dadosProduto.put("ID_AEACLASE", produtoRetorno.get("idAeaclase").getAsInt());
                            }
                            if (produtoRetorno.has("idAeamarca") && produtoRetorno.get("idAeamarca").getAsInt() > 0) {
                                dadosProduto.put("ID_AEAMARCA", produtoRetorno.get("idAeamarca").getAsInt());
                            }
                            if (produtoRetorno.has("idAeaunven")){
                                //JsonObject unidade = produtoRetorno.getAsJsonObject("unidadeVendaProduto");
                                dadosProduto.put("ID_AEAUNVEN", produtoRetorno.get("idAeaunven").getAsInt());
                            }
                            dadosProduto.put("DT_CAD",produtoRetorno.get("dtCad").getAsString());
                            dadosProduto.put("DT_ALT",produtoRetorno.get("dtAlt").getAsString());
                            dadosProduto.put("DESCRICAO",produtoRetorno.get("descricao").getAsString());
                            if (produtoRetorno.has("descricaoAuxiliar")) {
                                dadosProduto.put("DESCRICAO_AUXILIAR",produtoRetorno.get("descricaoAuxiliar").getAsString());
                            }
                            dadosProduto.put("CODIGO_ESTRUTURAL",produtoRetorno.get("codigoEstrutural").getAsString());
                            if (produtoRetorno.has("referencia")) {
                                dadosProduto.put("REFERENCIA",produtoRetorno.get("referencia").getAsString());
                            }
                            if (produtoRetorno.has("codigoBarras")) {
                                dadosProduto.put("CODIGO_BARRAS",produtoRetorno.get("codigoBarras").getAsString());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRODU");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosProduto - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosPreco(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Preço");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaPrecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_preco));
                        mLoad.progress().value(0, listaPrecoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaPrecoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_preco) + " - " + i + "/" + listaPrecoRetorno.size());
                            mLoad.progress().update(0, i, listaPrecoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_preco) + " - " + finalI1 + "/" + listaPrecoRetorno.size());
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
                            if (precoRetorno.has("dtAlt")){
                                dadosPreco.put("DT_ALT", precoRetorno.get("dtAlt").getAsString());
                            }
                            dadosPreco.put("VENDA_ATAC",precoRetorno.get("vendaAtac").getAsString());
                            dadosPreco.put("VENDA_VARE",precoRetorno.get("vendaVare").getAsString());

                            listaDadosPreco.add(dadosPreco);
                        }
                        PrecoSql precoSql = new PrecoSql(context);

                        todosSucesso = precoSql.insertList(listaDadosPreco);

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRECO");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode() + new Random().nextInt())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_preco)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode() + new Random().nextInt())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosPreco - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosEmbalagem(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Embalagem de Produto");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaEmbalagemRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_embalagem));
                        mLoad.progress().value(0, listaEmbalagemRetorno.size(), false).build();

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
                        for(int i = 0; i < listaEmbalagemRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - " + i + "/" + listaEmbalagemRetorno.size());
                            mLoad.progress().update(0, i, listaEmbalagemRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - " + finalI1 + "/" + listaEmbalagemRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAEMBAL");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEmbalagem - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosProdutosPorLoja(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto por Loja");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaProdutoLojaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja));
                        mLoad.progress().value(0, listaProdutoLojaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaProdutoLojaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - " + i + "/" + listaProdutoLojaRetorno.size());
                            mLoad.progress().update(0, i, listaProdutoLojaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - " + finalI1 + "/" + listaProdutoLojaRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLOJA");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosProdutoPorLoja - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosLocalEstoque(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Local de Estoque");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaLocalEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_local_estoque));
                        mLoad.progress().value(0, listaLocalEstoqueRetorno.size(), false).build();

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
                        for(int i = 0; i < listaLocalEstoqueRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - " + i + "/" + listaLocalEstoqueRetorno.size());
                            mLoad.progress().update(0, i, listaLocalEstoqueRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - " + finalI1 + "/" + listaLocalEstoqueRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEALOCES");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosLocalEstoque - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosEstoque(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Estoque de Produto");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estoque));
                        mLoad.progress().value(0, listaEstoqueRetorno.size(), false).build();

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
                        for(int i = 0; i < listaEstoqueRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estoque) + " - " + i + "/" + listaEstoqueRetorno.size());
                            mLoad.progress().update(0, i, listaEstoqueRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estoque) + " - " + finalI1 + "/" + listaEstoqueRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAESTOQ");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosEstoque - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosOrcamento(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Orçamento");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAORCAM");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "')";
            }
            if ((listaGuidOrcamento != null) && (listaGuidOrcamento.size() > 0)){
                parametrosWebservice += " AND (GUID IN(";
                int controle = 0;
                for (String guid : listaGuidOrcamento) {
                    controle++;
                    parametrosWebservice += "'" + guid + "'";
                    if (controle < listaGuidOrcamento.size()){
                        parametrosWebservice += ", ";
                    }
                }
                parametrosWebservice += " ))";
            }

            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    //JsonObject objectRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento));
                        mLoad.progress().value(0, listaPedidoRetorno.size(), false).build();

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

                        for(int i = 0; i < listaPedidoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - " + i + "/" + listaPedidoRetorno.size());
                            mLoad.progress().update(0, i, listaPedidoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - " + finalI1 + "/" + listaPedidoRetorno.size());
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
                            if (pedidoRetorno.has("nomeRazao")) {
                                dadosOrcamento.put("NOME_CLIENTE", pedidoRetorno.get("nomeRazao").getAsString());
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
                            if (orcamentoSql.updateFast(dadosOrcamento, "AEAORCAM.GUID = '" + dadosOrcamento.getAsString("GUID") + "'") == 0) {

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
                        } // Fim do for
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAORCAM");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportarDadosOrcamento - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosItemOrcamento(){
        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Item de Orçamento");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Item de Orçamento");
                }
            });
        }
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaItemOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC, criaPropriedadeDataAlteracaoWebservice("AEAITORC"));

            // Checa se retornou alguma coisa
            if ((listaItemOrcamentoObject != null) && (listaItemOrcamentoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaItemOrcamentoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaItemOrcamentoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaItemOrcamentoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaItemOrcamentoObject.size());
                    mLoad.progress().update(0, controle, listaItemOrcamentoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaItemOrcamentoObject.size());
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

                        if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("1")){
                            // Marca o status como retorno liberado
                            dadosItemOrcamento.put("STATUS", "RL");

                        } else if (situacao.equalsIgnoreCase("6")){
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "RB");

                        } else if (situacao.equalsIgnoreCase("2")){
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "C");

                        } else if (situacao.equalsIgnoreCase("3") || situacao.equalsIgnoreCase("4")){
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosItemOrcamento.put("STATUS", "F");

                        } else if (situacao.equalsIgnoreCase("5")){
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

                    if ((cursor != null) && (cursor.getCount() > 0) && (cursor.moveToFirst())){
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
                mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                mLoad.progress().value(0, 0, true).build();

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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosItemOrcamento" + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();;
        }
    }


    private void importarDadosPercentual(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Percentual");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaPercentualRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_percentual));
                        mLoad.progress().value(0, listaPercentualRetorno.size(), false).build();

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
                        for(int i = 0; i < listaPercentualRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_percentual) + " - " + i + "/" + listaPercentualRetorno.size());
                            mLoad.progress().update(0, i, listaPercentualRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_percentual) + " - " + finalI1 + "/" + listaPercentualRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPERCE");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                }  else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosPercentual" + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();;
        }
    }


    private void importarDadosFator(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Fator");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();


                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaFatorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fator));
                        mLoad.progress().value(0, listaFatorRetorno.size(), false).build();

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
                        for(int i = 0; i < listaFatorRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fator) + " - " + i + "/" + listaFatorRetorno.size());
                            mLoad.progress().update(0, i, listaFatorRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fator) + " - " + finalI1 + "/" + listaFatorRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAFATOR");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosFator" + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosProdutoRecomendado(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto Recomendado");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaProdutoRecomendadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_recomendado));
                        mLoad.progress().value(0, listaProdutoRecomendadoRetorno.size(), false).build();

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
                        for(int i = 0; i < listaProdutoRecomendadoRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - " + i + "/" + listaProdutoRecomendadoRetorno.size());
                            mLoad.progress().update(0, i, listaProdutoRecomendadoRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - " + finalI1 + "/" + listaProdutoRecomendadoRetorno.size());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRREC");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosProdutoRecomendado" + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }


    private void importarDadosParcela(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Títulos à Receber/Pagar");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
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
                parametrosWebservice +="&where= " + filtraParcela;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, 0, true).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                            }
                        });
                    }
                    // Checa se retornou alguma coisa
                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))){
                        final JsonArray listaParcelaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parcela));
                        mLoad.progress().value(0, listaParcelaRetorno.size(), false).build();

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
                        for(int i = 0; i < listaParcelaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parcela) + " - " + i + "/" + listaParcelaRetorno.size());
                            mLoad.progress().update(0, i, listaParcelaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parcela) + " - " + finalI1 + "/" + listaParcelaRetorno.size());
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
                            dadosParcela.put("DT_ALT", parcelaRetorno.get("dtAlt").getAsString());
                            dadosParcela.put("TIPO", parcelaRetorno.get("tipo").getAsString());
                            dadosParcela.put("DT_EMISSAO", parcelaRetorno.get("dtEmissao").getAsString());
                            dadosParcela.put("DT_VENCIMENTO", parcelaRetorno.get("dtVencimento").getAsString());
                            if (parcelaRetorno.has("dtBaixa")) {
                                dadosParcela.put("DT_BAIXA", parcelaRetorno.get("dtBaixa").getAsString());
                            }
                            if (parcelaRetorno.has("parcela")) {
                                dadosParcela.put("PARCELA", parcelaRetorno.get("parcela").getAsInt());
                            }
                            dadosParcela.put("VL_PARCELA", parcelaRetorno.get("vlParcela").getAsDouble());
                            if (parcelaRetorno.has("fcVlTotalPago")) {
                                dadosParcela.put("FC_VL_TOTAL_PAGO", parcelaRetorno.get("fcVlTotalPago").getAsDouble());
                            }
                            if (parcelaRetorno.has("fcVlRestante")) {
                                dadosParcela.put("FC_VL_RESTANTE", parcelaRetorno.get("fcVlRestante").getAsDouble());
                            }
                            if (parcelaRetorno.has("vlJurosDiario")) {
                                dadosParcela.put("VL_JUROS_DIARIO", parcelaRetorno.get("vlJurosDiario").getAsDouble());
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

                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("RPAPARCE");
                        }
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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

                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_parcela)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + context.hashCode())
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosParcela - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
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

    private List<PropertyInfo> criaPropriedadeDataAlteracaoWebservice(String tabela){

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(tabela);

        if ((listaUltimaAtualizacaoDispositivo != null) && (listaUltimaAtualizacaoDispositivo.size() > 0) && (tabela != null) && (!tabela.isEmpty())) {

            // Passa pela lista de atualizacoes
            for (UltimaAtualizacaoBeans ultimaData : listaUltimaAtualizacaoDispositivo) {
                // Checa se a tabela da atualizacao eh a requerida por parametro
                if (ultimaData.getTabela().equalsIgnoreCase(tabela)){

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

    private String pegaUltimaDataAtualizacao(String tabela){

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(tabela);

        if ((listaUltimaAtualizacaoDispositivo != null) && (listaUltimaAtualizacaoDispositivo.size() > 0) && (tabela != null) && (!tabela.isEmpty())) {

            // Passa pela lista de atualizacoes
            for (UltimaAtualizacaoBeans ultimaData : listaUltimaAtualizacaoDispositivo) {
                // Checa se a tabela da atualizacao eh a requerida por parametro
                if (ultimaData.getTabela().equalsIgnoreCase(tabela)){

                    return ultimaData.getDataUltimaAtualizacao();
                }
            }
        } else {
            return null;
        }
        return null;
    }
}
