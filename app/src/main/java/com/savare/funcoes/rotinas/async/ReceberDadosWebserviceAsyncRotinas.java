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

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
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
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
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
import java.util.UUID;
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

        mLoad = PugNotification.with(context).load()
                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR)
                .smallIcon(R.mipmap.ic_launcher)
                .largeIcon(R.mipmap.ic_launcher)
                .title(R.string.importar_dados_recebidos)
                .flags(Notification.DEFAULT_LIGHTS);

        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_estamos_checando_se_existe_internet));
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getString(R.string.aguarde_estamos_checando_se_existe_internet));
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
        funcoes.setValorXml("RecebendoDados", "S");

        if (funcoes.existeConexaoInternet()) try {

            mLoad.bigTextStyle(R.string.estamos_checando_webservice_online);
            mLoad.progress().value(0, 0, true).build();

            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setVisibility(View.VISIBLE);
                        textStatus.setText(context.getResources().getText(R.string.estamos_checando_webservice_online));
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
            // Verifica se o servidor webservice esta online
            if (funcoes.pingWebserviceSisInfo()) {

                mLoad.bigTextStyle(R.string.checando_versao_savare);
                mLoad.progress().value(0, 0, true).build();

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
                if ((funcoes.getValorXml("AbriuAppPriveiraVez").equalsIgnoreCase("S")) || (funcoes.getValorXml("AbriuAppPriveiraVez").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                    cadastrarDispositivo();

                } else {
                    // Checa se a versao do savere eh compativel com o webservice
                    if ((checarEmpresaAdmin()) && (checaFuncionarioAtivo())) {

                        importaDadosEmpresa();

                        if (funcoes.checaVersao()) {
                            // Recebe os dados da tabela
                            if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))) ||
                                    (tabelaRecebeDados == null)) {

                                if (checaFuncionarioAtivo() == false) {
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

                            // Recebe os dados da tabela CFAATIVI
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

                            // Recebe os dados da tabela CFAPROFI
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
                                importarDadosRemoveClifo();
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
        }
        else {

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
            if (textStatus != null) {
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
        if (textStatus != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.terminamos));
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

        if (usuario.containsKey("CODIGO_FUN")) {
            funcoes.setValorXml("CodigoUsuario", usuario.getAsString("CODIGO_FUN"));
        }
        if (usuario.containsKey("ID_SMAEMPRE")) {
            funcoes.setValorXml("CodigoEmpresa", usuario.getAsString("ID_SMAEMPRE"));
        }
        if (usuario.containsKey("GUID")) {
            funcoes.setValorXml("ChaveFuncionario", usuario.getAsString("GUID"));
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
        if (usuario.containsKey("MODO_CONEXAO")) {
            funcoes.setValorXml("ModoConexao", usuario.getAsString("MODO_CONEXAO"));
        }
        if (usuario.containsKey("IP_SERVIDOR_WEBSERVICE")) {
            funcoes.setValorXml("IPServidorWebservice", usuario.getAsString("IP_SERVIDOR_WEBSERVICE"));
        }
        if (usuario.containsKey("IP_SERVIDOR_SISINFO")) {
            funcoes.setValorXml("IPServidor", usuario.getAsString("IP_SERVIDOR_SISINFO"));
        }
        if (usuario.containsKey("CAMINHO_BANCO_SISINFO")) {
            funcoes.setValorXml("CaminhoBancoDados", usuario.getAsString("CAMINHO_BANCO_SISINFO"));
        }
        if (usuario.containsKey("PORTA_BANCO_SISINFO")) {
            funcoes.setValorXml("PortaBancoDados", usuario.getAsString("PORTA_BANCO_SISINFO"));
        }
        if (usuario.containsKey("USUARIO_SISINFO_WEBSERVICE")) {
            funcoes.setValorXml("UsuarioServidor", usuario.getAsString("USUARIO_SISINFO_WEBSERVICE"));
        }
        if (usuario.containsKey("SENHA_SISINFO_WEBSERVICE")) {
            funcoes.setValorXml("SenhaServidor", funcoes.criptografaSenha(usuario.getAsString("SENHA_SISINFO_WEBSERVICE")));
        }
    }

    public void setProgressBarStatus(ProgressBar progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    public void setTextStatus(TextView textStatus) {
        this.textStatus = textStatus;
    }

    public void setIdOrcamento(String[] idOrcamentoSelecionado) {
        this.idOrcamentoSelecionado = idOrcamentoSelecionado;
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
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Funcionário");
        mLoad.progress().value(0, 0, true).build();

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
                ultimaData = pegaUltimaDataAtualizacao("CFACLIFO_FUNC");

                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (ID_CFACLIFO = (SELECT SMADISPO.ID_CFACLIFO_FUNC FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + funcoes.getValorXml("UuidDispositivo") + "') )";
                } else {
                    parametrosWebservice += "&where= (ID_CFACLIFO = (SELECT SMADISPO.ID_CFACLIFO_FUNC FROM SMADISPO WHERE SMADISPO.IDENTIFICACAO = '" + funcoes.getValorXml("UuidDispositivo") + "') )";
                }

                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

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
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
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
                            for (int i = 0; i < listaUsuarioRetorno.size(); i++) {

                                final JsonObject usuarioRetorno = listaUsuarioRetorno.get(i).getAsJsonObject();

                                // Atualiza a notificacao
                                mLoad.bigTextStyle(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Funcionário: " + usuarioRetorno.get("nomeRazao").toString());
                                //mLoad.progress().update(0, i, listaUsuarioRetorno.size(), false).build();
                                mLoad.progress().value(0, 0, true).build();

                                // Checo se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Funcionário: " + usuarioRetorno.get("nomeRazao").toString());
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

                                ContentValues dadosInativo = new ContentValues();
                                dadosInativo.put("ATIVO_USUA", usuarioRetorno.get("ativo").toString().replace("\"", ""));

                                UsuarioSQL usuarioSQL = new UsuarioSQL(context);
                                usuarioSQL.update(dadosInativo, "LOGIN_USUA = '" + funcoes.getValorXml("Usuario") + "'");

                                // Checa se o usuario esta ativo
                                if (usuarioRetorno.get("ativo").toString().equalsIgnoreCase("0")) {

                                    final ContentValues contentValues = new ContentValues();
                                    contentValues.put("comando", 0);
                                    contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                    contentValues.put("mensagem", "O funcionário dessa chave esta inativo, não podemos baixar os dados dele. Entre em contato com a sua empresa ou com o suporte SAVARE.");
                                    contentValues.put("dados", "");

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            funcoes.menssagem(contentValues);
                                        }
                                    });
                                    return false;
                                }
                            }

                            final PessoaSql pessoaSql = new PessoaSql(context);

                            todosSucesso = pessoaSql.insertList(listaDadosUsuario);

                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("CFACLIFO_FUNC");
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
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
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
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            } else {
                // Cria uma notificacao para ser manipulado
                Load mLoad = PugNotification.with(context).load()
                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(R.mipmap.ic_launcher)
                        .title(R.string.recebendo_dados)
                        .bigTextStyle(context.getResources().getString(R.string.nao_achamos_chave_dispositivo))
                        .flags(Notification.DEFAULT_LIGHTS);
                mLoad.simple().build();
            }
        } catch (Exception e) {
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

    private void cadastrarDispositivo() {
        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Vamos cadastrar o Dispositivo");
        mLoad.progress().value(0, 0, true).build();

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
            String cnpjEmpresa = funcoes.getValorXml("CnpjEmpresa");
            String parametrosWebservice = "";
            JsonObject statuRetorno = null;

            if ((cnpjEmpresa != null) && (!cnpjEmpresa.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                String abriuAppPrimeiraVez = funcoes.getValorXml("AbriuAppPriveiraVez");

                if (((abriuAppPrimeiraVez != null) && (abriuAppPrimeiraVez.equalsIgnoreCase("S"))) || (abriuAppPrimeiraVez.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {

                    parametrosWebservice += "&cnpjUrl=" + cnpjEmpresa;
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                    JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMADISPO, WSSisinfoWebservice.METODO_POST, null, parametrosWebservice), JsonObject.class);

                    if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                        statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                        // Verifica se retornou com sucesso
                        if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {

                            ContentValues dadosUsuario = new ContentValues();
                            dadosUsuario.put("LOGIN_USUA", funcoes.getValorXml("Usuario"));
                            dadosUsuario.put("SENHA_USUA", funcoes.criptografaSenha(funcoes.getValorXml("SenhaUsuario")));

                            UsuarioSQL usuarioSQL = new UsuarioSQL(context);
                            usuarioSQL.delete(null);

                            if (usuarioSQL.insert(dadosUsuario) > 0) {
                                funcoes.setValorXml("AbriuAppPriveiraVez", "N");
                            }
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.dispositivo_registrado));
                            mLoad.progress().value(0, 0, true).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.dispositivo_registrado));
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
                            Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.msg_error)
                                    .bigTextStyle(context.getResources().getString(R.string.nao_conseguimos_cadastrar_dispositivo) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString())
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();

                            final JsonObject finalStatuRetorno = statuRetorno;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(R.string.nao_conseguimos_cadastrar_dispositivo);

                                    new MaterialDialog.Builder(context)
                                            .title("ReceberDadosWebserviceAsyncRotinas")
                                            .content(context.getResources().getString(R.string.nao_conseguimos_cadastrar_dispositivo) + "\n" +  (finalStatuRetorno != null ? finalStatuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_MENSAGEM_RETORNO).getAsString() : ""))
                                            .positiveText(R.string.button_ok)
                                            .show();
                                }
                            });
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                }
            } else {
                // Cria uma notificacao para ser manipulado
                Load mLoad = PugNotification.with(context).load()
                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(R.mipmap.ic_launcher)
                        .title(R.string.recebendo_dados_usuario)
                        .bigTextStyle(context.getResources().getString(R.string.nao_achamos_cnpj) + "\n")
                        .flags(Notification.DEFAULT_LIGHTS);
                mLoad.simple().build();

                SuperToast.create(context, context.getResources().getString(R.string.nao_achamos_cnpj), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
            }

        } catch (final Exception e) {
            // Cria uma notificacao
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("CadastrarDispositivo - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

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
                        progressBarStatus.setIndeterminate(true);
                        progressBarStatus.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }


    private boolean checarEmpresaAdmin() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Dados de licença da empresa");
        mLoad.progress().value(0, 0, true).build();

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
                JsonObject retornoWebservice = new Gson().fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO_ADMIN, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    // Pega o status que retornou do webservice
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
                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                            final JsonArray listaUsuarioRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

                            if (listaUsuarioRetorno.size() > 0) {
                                boolean todosSucesso = true;

                                // Atualiza a notificacao
                                mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_licenca));
                                mLoad.progress().value(0, 0, true).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Empresa: " + usuarioRetorno.get("nomeRazao").toString());
                                    //mLoad.progress().update(0, i, listaUsuarioRetorno.size(), false).build();
                                    mLoad.progress().value(0, 0, true).build();

                                    // Checo se o texto de status foi passado pro parametro
                                    if (textStatus != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Empresa: " + usuarioRetorno.get("nomeRazao").toString());
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
                                        ContentValues dadosInativo = new ContentValues();
                                        dadosInativo.put("ATIVO_USUA", "0");

                                        usuarioSQL.update(dadosInativo, "LOGIN_USUA = '" + funcoes.getValorXml("Usuario") + "'");

                                        final ContentValues contentValues = new ContentValues();
                                        contentValues.put("comando", 0);
                                        contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                        contentValues.put("mensagem", "Está inativo, não podemos baixar os dados dele. Entre em contato com a sua empresa ou com o suporte SAVARE.");
                                        contentValues.put("dados", "");
                                        // Pega os dados do usuario

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                funcoes.menssagem(contentValues);
                                            }
                                        });
                                        return false;
                                    } else {
                                        dadosServidor.put("ATIVO_USUA", "1");
                                        dadosServidor.put("IP_SERVIDOR_USUA", (usuarioRetorno.has("ipServidorSisinfo")) ? usuarioRetorno.get("ipServidorSisinfo").getAsString() : "");
                                        dadosServidor.put("IP_SERVIDOR_WEBSERVICE_USUA", (usuarioRetorno.has("ipServidorWebservice")) ? usuarioRetorno.get("ipServidorWebservice").getAsString() : "");
                                        dadosServidor.put("USUARIO_SERVIDOR_USUA", (usuarioRetorno.has("usuSisinfoWebservice")) ? usuarioRetorno.get("usuSisinfoWebservice").getAsString() : "");
                                        dadosServidor.put("SENHA_SERVIDOR_USUA", (usuarioRetorno.has("senhaSisinfoWebservice")) ? usuarioRetorno.get("senhaSisinfoWebservice").getAsString() : "");
                                        dadosServidor.put("CAMINHO_BANCO_DADOS_USUA", (usuarioRetorno.has("caminhoBancoSisinfo")) ? usuarioRetorno.get("caminhoBancoSisinfo").getAsString() : "");
                                        dadosServidor.put("PORTA_BANCO_DADOS_USUA", (usuarioRetorno.has("portaBancoSisinfo")) ? usuarioRetorno.get("portaBancoSisinfo").getAsString() : "");
                                        dadosServidor.put("MODO_CONEXAO", (usuarioRetorno.has("modoConexaoWebservice")) ? usuarioRetorno.get("modoConexaoWebservice").getAsString() : "");

                                        salvarDadosXml(dadosServidor);
                                    }
                                }
                                // Atualiza os dados do usuario
                                if ((dadosServidor.size() > 0) && (usuarioSQL.update(dadosServidor, "LOGIN_USUA = '" + funcoes.getValorXml("Usuario") + "'") > 0)) {
                                    inserirUltimaAtualizacao("CFACLIFO_ADMIN");
                                    return true;
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
                            }
                        }
                        inserirUltimaAtualizacao("CFACLIFO_ADMIN");
                    } else if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        ContentValues dadosInativo = new ContentValues();
                        dadosInativo.put("ATIVO_USUA", "0");

                        UsuarioSQL usuarioSQL = new UsuarioSQL(context);
                        usuarioSQL.update(dadosInativo, "LOGIN_USUA = '" + funcoes.getValorXml("Usuario") + "'");

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.nao_autorizado));
                        mLoad.progress().value(0, 0, true).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nao_autorizado));
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
                            // Cria uma notificacao para ser manipulado
                            Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.recebendo_dados_usuario)
                                    .bigTextStyle(mensagem.get("mensagemRetorno").getAsString())
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();
                        }
                        return false;
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados_usuario)
                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();

                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa));
                                }
                            });
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();

                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + " Dados de licença da empresa");
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            // Cria uma notificacao
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosFuncionario- " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
        return true;
    }


    private void importaDadosEmpresa() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Empresa");
        mLoad.progress().value(0, 0, true).build();

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

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml("ChaveFuncionario") + "'))";
            } else {
                parametrosWebservice += "&where= (ID_SMAEMPRE = (SELECT CFACLIFO.ID_SMAEMPRE FROM CFACLIFO WHERE CFACLIFO.GUID = '" + funcoes.getValorXml("ChaveFuncionario") + "'))";
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            //JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEmpresaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEmpresaRetorno.size() > 0) {
                                        final List<ContentValues> listaDadosEmpresa = new ArrayList<ContentValues>();

                                        for (int i = 0; i < listaEmpresaRetorno.size(); i++) {

                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_empresa) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEmpresaRetorno.size());
                                            mLoad.progress().update(0, i, listaEmpresaRetorno.size(), false).build();

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

                                            salvarDadosXml(dadosEmpresa);
                                            listaDadosEmpresa.add(dadosEmpresa);
                                        }
                                        EmpresaSql empresaSql = new EmpresaSql(context);

                                        todosSucesso = empresaSql.insertList(listaDadosEmpresa);
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_empresa)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        }// Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("SMAEMPRE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosArea() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Área de Atuação");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaAreasRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaAreasRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaAreasRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_areas) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + "/" + listaAreasRetorno.size());
                                            mLoad.progress().update(0, i, listaAreasRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_areas)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAAREAS");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }

        } catch (Exception e) {
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


    private void importarDadosAtividade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Ramo de Atividade");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaAtividadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaAtividadeRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaAtividadeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_atividade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaAtividadeRetorno.size());
                                            mLoad.progress().update(0, i, listaAtividadeRetorno.size(), false).build();

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
                                            if ((atividadeRetorno.has("descPromocao"))) {
                                                dadosAtividade.put("DESC_PROMOCAO", atividadeRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosAtividade.add(dadosAtividade);
                                        } // FIm do for
                                        RamoAtividadeSql ramoAtividadeSql = new RamoAtividadeSql(context);
                                        todosSucesso = ramoAtividadeSql.insertList(listaDadosAtividade);
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_atividade)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAATIVI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }

        } catch (Exception e) {
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


    private void importarDadosStatus() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Status");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaStatusRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaStatusRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaStatusRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_status) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaStatusRetorno.size());
                                            mLoad.progress().update(0, i, listaStatusRetorno.size(), false).build();

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
                                            if ((statusObsRetorno.has("descPromocao"))) {
                                                dadosStatus.put("DESC_PROMOCAO", statusObsRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosStatus.add(dadosStatus);
                                        } // Fim for
                                        StatusSql statusSql = new StatusSql(context);

                                        todosSucesso = statusSql.insertList(listaDadosStatus);
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFASTATU");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosTipoDocumento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo Documento");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoDocumentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoDocumentoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaTipoDocumentoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoDocumentoRetorno.size());
                                            mLoad.progress().update(0, i, listaTipoDocumentoRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
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
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados_parcela)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                            // Incrementa o total de paginas
                            pageNumber++;
                            if (pageNumber < totalPages) {
                                retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPDOC");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosCartaoCredito() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cartão Credito");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaCartaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaCartaoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaCartaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cartao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaCartaoRetorno.size());
                                            mLoad.progress().update(0, i, listaCartaoRetorno.size(), false).build();

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
                                            if (cartaoRetorno.has("taxa1")  && cartaoRetorno.get("taxa1").getAsDouble() > 0) {
                                                dadosCartao.put("TAXA1", cartaoRetorno.get("taxa1").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxa2")  && cartaoRetorno.get("taxa2").getAsDouble() > 0) {
                                                dadosCartao.put("TAXA2", cartaoRetorno.get("taxa2").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxa3")  && cartaoRetorno.get("taxa3").getAsDouble() > 0) {
                                                dadosCartao.put("TAXA3", cartaoRetorno.get("taxa3").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("taxaDeb")  && cartaoRetorno.get("taxaDeb").getAsDouble() > 0) {
                                                dadosCartao.put("TAXA_DEB", cartaoRetorno.get("taxaDeb").getAsDouble());
                                            }
                                            if (cartaoRetorno.has("diasDeb")  && cartaoRetorno.get("diasDeb").getAsInt() > 0) {
                                                dadosCartao.put("DIAS_DEB", cartaoRetorno.get("diasDeb").getAsInt());
                                            }
                                            if (cartaoRetorno.has("diasCre")  && cartaoRetorno.get("diasCre").getAsInt() > 0) {
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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACCRED");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosPortador() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Portador (Banco)");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPortadorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPortadorRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaPortadorRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_portador) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPortadorRetorno.size());
                                            mLoad.progress().update(0, i, listaPortadorRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPORTA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosProfissao() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Profissão");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProfissaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProfissaoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaProfissaoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_profisao) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProfissaoRetorno.size());
                                            mLoad.progress().update(0, i, listaProfissaoRetorno.size(), false).build();

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
                                            if (profissaoRetorno.has("descPromocao")) {
                                                dadosProfissao.put("DESC_PROMOCAO", profissaoRetorno.get("descPromocao").getAsString());
                                            }
                                            listaDadosProfissao.add(dadosProfissao);
                                        } // Fim for
                                        ProfissaoSql profissaoSql = new ProfissaoSql(context);

                                        todosSucesso = profissaoSql.insertList(listaDadosProfissao);
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPROFI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosTipoCliente() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo Cliente");
        mLoad.progress().value(0, 0, true).build();


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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoClienteRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaTipoClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoClienteRetorno.size());
                                            mLoad.progress().update(0, i, listaTipoClienteRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCLI");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosTipoCobranca() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Tipo de Cobrança");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTipoCobrancaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTipoCobrancaRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaTipoCobrancaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTipoCobrancaRetorno.size());
                                            mLoad.progress().update(0, i, listaTipoCobrancaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFATPCOB");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosEstado() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Estado");
        mLoad.progress().value(0, 0, true).build();

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
                parametrosWebservice += "&where= ( " + whereDatas + " ) AND " + filtraEstadoPorFuncionario;
            } else {
                parametrosWebservice += "&where= " + filtraEstadoPorFuncionario;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEstadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEstadoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaEstadoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estado) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEstadoRetorno.size());
                                            mLoad.progress().update(0, i, listaEstadoRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAESTAD");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosCidade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cidade");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaCidadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaCidadeRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaCidadeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cidade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaCidadeRetorno.size());
                                            mLoad.progress().update(0, i, listaCidadeRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACIDAD");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosClifo() {
        JsonObject statuRetorno;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Cliente e Fornecedor");
        mLoad.progress().value(0, 0, true).build();

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

            String filtraClientePorVendedor =
                    "((CFACLIFO.ID_CFACLIFO IN \n" +
                            "(" + filtraClientePorParametro + " ) " +
                            ") \n" +
                            "OR ( (CFACLIFO.NOME_RAZAO LIKE '%CONSUM%FIN%') " + ((ultimaData != null && !ultimaData.isEmpty()) ? " AND (CFACLIFO.DT_ALT >= '" + ultimaData + "')" : "") + " ))";

            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (CFACLIFO.DT_ALT >= '" + ultimaData + "') AND " + filtraClientePorVendedor;
            } else {
                parametrosWebservice += "&where= " + filtraClientePorVendedor;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            Gson gson = new Gson();
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClienteRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClienteRetorno.size());
                                            mLoad.progress().update(0, i, listaClienteRetorno.size(), false).build();

                                            // Checo se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                final int finalI1 = i;
                                                final int finalPageNumber = pageNumber + 1;
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente) + " - Parte " + (finalPageNumber + 1) + "/" + totalPages + " - " + finalI1 + "/" + listaClienteRetorno.size());
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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFACLIFO");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (JsonParseException e) {

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
        } catch (Exception e) {

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


    private void importarDadosRemoveClifo() {
        JsonObject statuRetorno;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Remover Cliente e Fornecedor");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    //boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        List<ContentValues> listaTodosClifo = new ArrayList<ContentValues>();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClienteRetorno.size() > 0) {
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

                                        for (int i = 0; i < listaClienteRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClienteRetorno.size());
                                            mLoad.progress().update(0, i, listaClienteRetorno.size(), false).build();

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
                                            dadosClifo.put("NOME_RAZAO", clienteRetorno.get("nomeRazao").getAsString());

                                            listaTodosClifo.add(dadosClifo);
                                        }
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.vamos_checar_cliente));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                        mLoad.progress().value(0, 0, true).build();

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
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (JsonParseException e) {

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
        } catch (Exception e) {

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


    private void importarDadosEndereco() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Endereço");
        mLoad.progress().value(0, 0, true).build();

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
            String ultimaDataParam = pegaUltimaDataAtualizacao("CFAPARAM");
            // Cria uma variavel para salvar todos os paramentros em json
            String parametrosWebservice = "";
            String filtraClientePorParametro =
                    "SELECT CFAPARAM.ID_CFACLIFO FROM CFAPARAM WHERE (CFAPARAM.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") \n" +
                            "AND (CFAPARAM.ID_CFACLIFO_VENDE = (SELECT CLIFO_VENDE.ID_CFACLIFO FROM CFACLIFO CLIFO_VENDE WHERE CLIFO_VENDE.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")) \n";

            // CHECA SE TEVE ALGUMA ALTERACAO NA TABELA PARAM
            if ((ultimaDataParam != null) && (!ultimaDataParam.isEmpty())) {

                filtraClientePorParametro += " AND (CFAPARAM.DT_ALT >= '" + ultimaDataParam + "')";
            }
            String filtraEnder =
                    "(CFAENDER.ID_CFACLIFO IN (" + filtraClientePorParametro + " ) )";

            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                parametrosWebservice += "&where= (CFAENDER.DT_ALT >= '" + ultimaData + "') OR " + filtraEnder;
            } else {
                parametrosWebservice += "&where= " + filtraEnder;
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            Gson gson = new Gson();
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEnderecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEnderecoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaEnderecoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEnderecoRetorno.size());
                                            mLoad.progress().update(0, i, listaEnderecoRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAENDER");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (JsonParseException e) {
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

        } catch (Exception e) {
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


    private void importarDadosParametros() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Parâmetros");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaParametroRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaParametroRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaParametroRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parametro) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaParametroRetorno.size());
                                            mLoad.progress().update(0, i, listaParametroRetorno.size(), false).build();

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
                                            if (parametroRetorno.has("jurosDiario")) {
                                                dadosParametros.put("JUROS_DIARIO", parametroRetorno.get("jurosDiario").getAsDouble());
                                            }
                                            listaDadosStatus.add(dadosParametros);
                                        }
                                        ParametrosSql parametrosSql = new ParametrosSql(context);

                                        todosSucesso = parametrosSql.insertList(listaDadosStatus);
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("CFAPARAM");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosFotos() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Imagens");
        mLoad.progress().value(0, 0, true).build();
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

                // Pega quando foi a ultima data que recebeu dados
                String ultimaData = pegaUltimaDataAtualizacao("CFAFOTOS");
                // Cria uma variavel para salvar todos os paramentros em json
                String parametrosWebservice = "";
                String filtraFotos =
                        "(CFAFOTOS.ID_AEAPRODU IN (SELECT AEAPRODU.ID_AEAPRODU FROM AEAPRODU WHERE AEAPRODU.ATIVO = '1'))";

                Gson gson = new Gson();
                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    parametrosWebservice += "&where= (CFAFOTOS.DT_ALT >= '" + ultimaData + "') AND " + filtraFotos + "&size=50";
                } else {
                    parametrosWebservice += "&where= " + filtraFotos + "&size=50";
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                        boolean todosSucesso = true;

                        JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                        if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                            final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                            int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                            for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                        final JsonArray listaFotosRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                        // Checa se retornou algum dados na lista
                                        if (listaFotosRetorno.size() > 0) {
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
                                            for (int i = 0; i < listaFotosRetorno.size(); i++) {
                                                // Atualiza a notificacao
                                                mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fotos) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaFotosRetorno.size());
                                                mLoad.progress().update(0, i, listaFotosRetorno.size(), false).build();

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
                                                        //dadosFotos.put("FOTO", Base64.decode(fotosRetorno.get("foto").getAsString(), Base64.DEFAULT));

                                                        byte[] tmp = new byte[fotosRetorno.getAsJsonArray("foto").size()];
                                                        for(int j = 0; j < fotosRetorno.getAsJsonArray("foto").size(); j++){
                                                            tmp[j]=(byte)(((int)fotosRetorno.getAsJsonArray("foto").get(j).getAsInt()) & 0xFF);
                                                        }
                                                        dadosFotos.put("FOTO", tmp);
                                                    }
                                                    listaDadosFotos.add(dadosFotos);
                                                }
                                            }
                                            FotosSql fotosSql = new FotosSql(context);

                                            todosSucesso = fotosSql.insertList(listaDadosFotos);
                                        }
                                        // Atualiza a notificacao
                                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                        mLoad.progress().value(0, 0, true).build();

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
                                        Load mLoad = PugNotification.with(context).load()
                                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                .smallIcon(R.mipmap.ic_launcher)
                                                .largeIcon(R.mipmap.ic_launcher)
                                                .title(R.string.versao_savare_desatualizada)
                                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                                .flags(Notification.DEFAULT_LIGHTS);
                                        mLoad.simple().build();
                                    }
                                    // Incrementa o total de paginas
                                    pageNumber++;
                                    if (pageNumber < totalPages) {
                                        retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                    }
                                } else {
                                    todosSucesso = false;

                                    // Cria uma notificacao para ser manipulado
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                            } // Fim do for ia (page)
                            // Checa se todos foram inseridos/atualizados com sucesso
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("CFAFOTOS");
                            }
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_fotos) + "\n - As Fotos voltou vazia do Servidor.")
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (JsonParseException e) {
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
        } catch (Exception e) {
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode())
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosFotos - \n" + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();

            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .title(R.string.recebendo_dados)
                    .bigTextStyle("ImportaDadosFotos - \n" + e.getMessage())
                    .flags(Notification.DEFAULT_LIGHTS);
            mLoad.simple().build();
        }
    }


    private void importarDadosPlanoPagamento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Plano de Pagamento");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPlanoPagamentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPlanoPagamentoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaPlanoPagamentoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPlanoPagamentoRetorno.size());
                                            mLoad.progress().update(0, i, listaPlanoPagamentoRetorno.size(), false).build();

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
                                    }
                                    // Atualiza a notificacao
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } //Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLPGT");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosClasseProdutos() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Classe de Produto");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaClasseRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaClasseRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaClasseRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaClasseRetorno.size());
                                            mLoad.progress().update(0, i, listaClasseRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACLASE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosUnidadeVenda() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Unidade de Venda");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaUnidadeVendaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaUnidadeVendaRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaUnidadeVendaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaUnidadeVendaRetorno.size());
                                            mLoad.progress().update(0, i, listaUnidadeVendaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } //Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAUNVEN");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosGrade() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Grade de Produto");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaGradeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaGradeRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaGradeRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_grade) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaGradeRetorno.size());
                                            mLoad.progress().update(0, i, listaGradeRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAGRADE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosMarca() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Marca");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaMarcaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaMarcaRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaMarcaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_marca) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaMarcaRetorno.size());
                                            mLoad.progress().update(0, i, listaMarcaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAMARCA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void importarDadosCodigoSituacaoTributaria() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Situação Tributária");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaTributariaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaTributariaRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaTributariaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaTributariaRetorno.size());
                                            mLoad.progress().update(0, i, listaTributariaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // FIm do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEACODST");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosProduto() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaProdutoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoRetorno.size());
                                            mLoad.progress().update(0, i, listaProdutoRetorno.size(), false).build();

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
                                            if (produtoRetorno.has("idAeaclase") && produtoRetorno.get("idAeaclase").getAsInt() > 0) {
                                                dadosProduto.put("ID_AEACLASE", produtoRetorno.get("idAeaclase").getAsInt());
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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRODU");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosPreco() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Preço");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPrecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPrecoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaPrecoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_preco) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPrecoRetorno.size());
                                            mLoad.progress().update(0, i, listaPrecoRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_preco)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRECO");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
            // Cria uma notificacao para ser manipulado
            PugNotification.with(context)
                    .load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + e.hashCode() + new Random().nextInt(100))
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle("ImportaDadosPreco - " + e.getMessage())
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .simple()
                    .build();
        }
    }

    private void importarDadosEmbalagem() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Embalagem de Produto");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEmbalagemRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEmbalagemRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaEmbalagemRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEmbalagemRetorno.size());
                                            mLoad.progress().update(0, i, listaEmbalagemRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } //Fim for ia (pager)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAEMBAL");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosProdutosPorLoja() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto por Loja");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {

                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {
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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoLojaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoLojaRetorno.size() > 0) {
                                        // Atualiza a notificacao
                                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - Parte " + (pageNumber + 1) + "/" + totalPages);
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
                                        for (int i = 0; i < listaProdutoLojaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoLojaRetorno.size());
                                            mLoad.progress().update(0, i, listaProdutoLojaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPLOJA");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosLocalEstoque() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Local de Estoque");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaLocalEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaLocalEstoqueRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaLocalEstoqueRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaLocalEstoqueRetorno.size());
                                            mLoad.progress().update(0, i, listaLocalEstoqueRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEALOCES");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosEstoque() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Estoque de Produto");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaEstoqueRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaEstoqueRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estoque) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaEstoqueRetorno.size());
                                            mLoad.progress().update(0, i, listaEstoqueRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAESTOQ");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosOrcamento() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Orçamento");
        mLoad.progress().value(0, 0, true).build();

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

                            if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                                final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                                int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                                for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                        if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                            final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                            // Checa se retornou algum dados na lista
                                            if (listaPedidoRetorno.size() > 0) {
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

                                                for (int i = 0; i < listaPedidoRetorno.size(); i++) {
                                                    // Atualiza a notificacao
                                                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPedidoRetorno.size());
                                                    mLoad.progress().update(0, i, listaPedidoRetorno.size(), false).build();

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
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                            mLoad.progress().value(0, 0, true).build();

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
                                            Load mLoad = PugNotification.with(context).load()
                                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                    .smallIcon(R.mipmap.ic_launcher)
                                                    .largeIcon(R.mipmap.ic_launcher)
                                                    .title(R.string.versao_savare_desatualizada)
                                                    .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                                    .flags(Notification.DEFAULT_LIGHTS);
                                            mLoad.simple().build();
                                        }
                                        // Incrementa o total de paginas
                                        pageNumber++;
                                        if (pageNumber < totalPages) {
                                            retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                        }
                                    } else {
                                        todosSucesso = false;

                                        // Cria uma notificacao para ser manipulado
                                        Load mLoad = PugNotification.with(context).load()
                                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                .smallIcon(R.mipmap.ic_launcher)
                                                .largeIcon(R.mipmap.ic_launcher)
                                                .title(R.string.recebendo_dados)
                                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                                .flags(Notification.DEFAULT_LIGHTS);
                                        mLoad.simple().build();
                                    }
                                } // Fim do for ia (page)
                                if (todosSucesso) {
                                    inserirUltimaAtualizacao("AEASAIDA_ORC");
                                }
                            }
                        } else {
                            // Cria uma notificacao para ser manipulado
                            Load mLoad = PugNotification.with(context).load()
                                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                    .smallIcon(R.mipmap.ic_launcher)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .title(R.string.recebendo_dados)
                                    .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                                    .flags(Notification.DEFAULT_LIGHTS);
                            mLoad.simple().build();
                        }
                    }
                }
                importarDadosPedido(listaOrcamento);
            }
        } catch (Exception e) {
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


    /**
     * Pega os dados do pedido que foi enviado pelo dispositivo.
     * Ou seja, pelo numero do orcamento eh pego os dados depois que o orcamento eh transformado
     * em pedido.
     */
    private void importarDadosPedido(List<OrcamentoBeans> listaPedidosDispositivo) {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Pedido");
        mLoad.progress().value(0, 0, true).build();

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

            if ((ultimaData != null) && (!ultimaData.isEmpty()) && (!whereGuidOrcamento.isEmpty())) {
                /*parametrosWebservice += "&where=  ( (DT_ALT >= '" + ultimaData + "' ) AND " +
                                        // Pega todas as vendas feito para o vendedor a partir de uma data
                                        "(AEASAIDA.ID_CFACLIFO_VENDEDOR_INI = (SELECT ID_CFACLIFO FROM CFACLIFO WHERE CFACLIFO.CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario") + ")) AND " +
                                        "(AEASAIDA.ID_CFACLIFO IS NOT NULL) AND (AEASAIDA.ID_CFACIDAD IS NOT NULL) ) \n";;
                if (!whereGuidOrcamento.isEmpty()){
                    parametrosWebservice += " OR ( (DT_ALT >= '" + ultimaData + "') AND " + whereGuidOrcamento + ")";
                }*/
                //Cria where para pegar os pedidos que foi enviado pelo vendedor e que teve alteração a partir de uma data
                parametrosWebservice += "&where= (DT_ALT >= '" + ultimaData + "') AND " + whereGuidOrcamento;

            } else if (!whereGuidOrcamento.isEmpty()) {
                //Cria where para pegar os pedidos que foi enviado pelo vendedor
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

                        if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                            final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                            int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                            for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                    if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                        final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                        // Checa se retornou algum dados na lista
                                        if (listaPedidoRetorno.size() > 0) {
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

                                            for (int i = 0; i < listaPedidoRetorno.size(); i++) {
                                                // Atualiza a notificacao
                                                mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPedidoRetorno.size());
                                                mLoad.progress().update(0, i, listaPedidoRetorno.size(), false).build();

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
                                        mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                        mLoad.progress().value(0, 0, true).build();

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
                                        Load mLoad = PugNotification.with(context).load()
                                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                                .smallIcon(R.mipmap.ic_launcher)
                                                .largeIcon(R.mipmap.ic_launcher)
                                                .title(R.string.versao_savare_desatualizada)
                                                .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                                .flags(Notification.DEFAULT_LIGHTS);
                                        mLoad.simple().build();
                                    }
                                    // Incrementa o total de paginas
                                    pageNumber++;
                                    if (pageNumber < totalPages) {
                                        retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEASAIDA, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                    }
                                } else {
                                    todosSucesso = false;

                                    // Cria uma notificacao para ser manipulado
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                            } // Fim do for ia (page)
                            if (todosSucesso) {
                                inserirUltimaAtualizacao("AEASAIDA");
                            }
                        }
                    } else {
                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.mipmap.ic_launcher)
                                .title(R.string.recebendo_dados)
                                .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();
                    }
                }
            }
        } catch (Exception e) {
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


    private void importarDadosItemOrcamento() {
        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Item de Orçamento");
        mLoad.progress().value(0, 0, true).build();

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

            final Vector<SoapObject> listaItemOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC, criaPropriedadeDataAlteracaoWebservice("AEAITORC"));

            // Checa se retornou alguma coisa
            if ((listaItemOrcamentoObject != null) && (listaItemOrcamentoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaItemOrcamentoObject.size(), false).build();

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
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_item_orcamento) + " - " + (finalControle + 1) + "/" + listaItemOrcamentoObject.size());
                    mLoad.progress().update(0, controle, listaItemOrcamentoObject.size(), false).build();

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
                mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                mLoad.progress().value(0, 0, true).build();

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
        } catch (Exception e) {
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
                    .build();
            ;
        }
    }


    private void importarDadosPercentual() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Percentual");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaPercentualRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaPercentualRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaPercentualRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_percentual) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaPercentualRetorno.size());
                                            mLoad.progress().update(0, i, listaPercentualRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } //Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPERCE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.get(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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
                    .build();
            ;
        }
    }


    private void importarDadosFator() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Fator");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaFatorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaFatorRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaFatorRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fator) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaFatorRetorno.size());
                                            mLoad.progress().update(0, i, listaFatorRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.versao_savare_desatualizada)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim do for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAFATOR");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosProdutoRecomendado() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Produto Recomendado");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaProdutoRecomendadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaProdutoRecomendadoRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaProdutoRecomendadoRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaProdutoRecomendadoRetorno.size());
                                            mLoad.progress().update(0, i, listaProdutoRecomendadoRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim for ia (page)
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("AEAPRREC");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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


    private void importarDadosParcela() {
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Títulos à Receber/Pagar");
        mLoad.progress().value(0, 0, true).build();

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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice, null), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {

                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == HttpURLConnection.HTTP_OK) {
                    boolean todosSucesso = true;

                    JsonObject pageRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_PAGE_RETORNO);

                    if (pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt() >= 0) {
                        final int totalPages = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_TOTAL_PAGES_RETORNO).getAsInt();
                        int pageNumber = pageRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_PAGE_NUMBER_RETORNO).getAsInt();

                        for (int ia = pageNumber; ia < totalPages; ia++) {

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
                                if ((retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO))) {
                                    final JsonArray listaParcelaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                                    // Checa se retornou algum dados na lista
                                    if (listaParcelaRetorno.size() > 0) {
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
                                        for (int i = 0; i < listaParcelaRetorno.size(); i++) {
                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parcela) + " - Parte " + (pageNumber + 1) + "/" + totalPages + " - " + i + "/" + listaParcelaRetorno.size());
                                            mLoad.progress().update(0, i, listaParcelaRetorno.size(), false).build();

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
                                    mLoad.bigTextStyle(context.getResources().getString(R.string.aguarde_mais_um_pouco_proxima_etapa));
                                    mLoad.progress().value(0, 0, true).build();

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
                                    Load mLoad = PugNotification.with(context).load()
                                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                            .smallIcon(R.mipmap.ic_launcher)
                                            .largeIcon(R.mipmap.ic_launcher)
                                            .title(R.string.recebendo_dados_parcela)
                                            .bigTextStyle(context.getResources().getString(R.string.nao_chegou_dados_servidor_empresa) + "\n" + retornoWebservice.toString())
                                            .flags(Notification.DEFAULT_LIGHTS);
                                    mLoad.simple().build();
                                }
                                // Incrementa o total de paginas
                                pageNumber++;
                                if (pageNumber < totalPages) {
                                    retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, parametrosWebservice + "&pageNumber=" + pageNumber, null), JsonObject.class);
                                }
                            } else {
                                todosSucesso = false;

                                // Cria uma notificacao para ser manipulado
                                Load mLoad = PugNotification.with(context).load()
                                        .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                                        .smallIcon(R.mipmap.ic_launcher)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .title(R.string.recebendo_dados)
                                        .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                                        .flags(Notification.DEFAULT_LIGHTS);
                                mLoad.simple().build();
                            }
                        } // Fim for page
                        // Checa se todos foram inseridos/atualizados com sucesso
                        if (todosSucesso) {
                            inserirUltimaAtualizacao("RPAPARCE");
                        }
                    }
                } else {
                    // Cria uma notificacao para ser manipulado
                    Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100))
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.mipmap.ic_launcher)
                            .title(R.string.recebendo_dados)
                            .bigTextStyle(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice) + "\n" + statuRetorno.toString())
                            .flags(Notification.DEFAULT_LIGHTS);
                    mLoad.simple().build();
                }
            }
        } catch (Exception e) {
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

    private void inserirUltimaAtualizacao(String tabela) {
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
