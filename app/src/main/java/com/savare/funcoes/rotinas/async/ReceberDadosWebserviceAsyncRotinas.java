package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import com.savare.beans.RetornoWebServiceBeans;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.VersionUtils;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import br.com.goncalves.pugnotification.interfaces.PendingIntentNotification;
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
                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()) {
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))) ||
                            (tabelaRecebeDados == null)) {

                        if (importaDadosUsuario() == false){
                            return null;
                        }

                        /*// Indica que essa notificacao eh do tipo progress
                        mLoad.bigTextStyle(context.getResources().getString(R.string.estamos_conectanto_servidor_nuvem));
                        mLoad.progress().value(0, 0, true).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.estamos_conectanto_servidor_nuvem));
                                }
                            });
                        }

                        List<PropertyInfo> listaPropriedade = null;

                        if (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {

                            listaPropriedade = criaPropriedadeDataAlteracaoWebservice("USUARIO_USUA");
                        }

                        // Busca no servidor Webservice
                        Vector<SoapObject> listaUsuarioObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA, ((listaPropriedade != null) ? listaPropriedade : null));

                        // Checa se retornou alguma coisa
                        if (listaUsuarioObject != null && listaUsuarioObject.size() > 0) {

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
                            // Passa por toda a lista
                            for (final SoapObject objetoIndividual : listaUsuarioObject) {

                                final SoapObject objeto;

                                if (objetoIndividual.hasProperty("return")) {
                                    objeto = (SoapObject) objetoIndividual.getProperty("return");

                                } else if (objetoIndividual.hasProperty("anyType")) {
                                    objeto = (SoapObject) objetoIndividual.getProperty("anyType");

                                } else {
                                    objeto = objetoIndividual;
                                }

                                // Atualiza a notificacao
                                mLoad.bigTextStyle(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Usuário: " + objeto.getProperty("nomeUsuario").toString());
                                mLoad.progress().value(0, 0, true).build();

                                // Checa se o texto de status foi passado pro parametro
                                if (textStatus != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - " + objeto.getProperty("nomeUsuario").toString());
                                        }
                                    });
                                }
                                // Checa se o usuario esta ativo
                                if (objeto.getProperty("ativoUsuario").toString().equalsIgnoreCase("0")) {

                                    final ContentValues contentValues = new ContentValues();
                                    contentValues.put("comando", 0);
                                    contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                    contentValues.put("mensagem", "O usuário dessa chave esta inativo, não podemos baixar os dados dele. Entre em contato com o suporte SAVARE.");
                                    contentValues.put("dados", "");
                                    // Pega os dados do usuario

                                    contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                                    contentValues.put("empresa", funcoes.getValorXml("ChaveUsuario"));
                                    contentValues.put("email", funcoes.getValorXml("Email"));

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            funcoes.menssagem(contentValues);
                                        }
                                    });

                                    return null;

                                } else {
                                    final ContentValues dadosUsuario = new ContentValues();
                                    dadosUsuario.put("ID_USUA", Integer.parseInt(objeto.getProperty("idUsuario").toString()));
                                    dadosUsuario.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                                    dadosUsuario.put("NOME_USUA", objeto.getProperty("nomeUsuario").toString());
                                    dadosUsuario.put("EMAIL_USUA", ((objeto.hasProperty("email")) ? objeto.getProperty("email").toString().replace("anyType{}", "") : ""));
                                    dadosUsuario.put("EMPRESA_USUA", objeto.getProperty("empresaUsuario").toString());
                                    dadosUsuario.put("VENDE_ATACADO_USUA", objeto.getProperty("vendeAtacadoUsuario").toString());
                                    dadosUsuario.put("VENDE_VAREJO_USUA", objeto.getProperty("vendeVarejoUsuario").toString());
                                    dadosUsuario.put("ATIVO_USUA", objeto.getProperty("ativoUsuario").toString());
                                    dadosUsuario.put("IP_SERVIDOR_USUA", (objeto.hasProperty("ipServidor")) ? objeto.getProperty("ipServidor").toString() : "");
                                    dadosUsuario.put("IP_SERVIDOR_WEBSERVICE_USUA", (objeto.hasProperty("ipServidorWebservice")) ? objeto.getProperty("ipServidorWebservice").toString() : "");
                                    dadosUsuario.put("USUARIO_SERVIDOR_USUA", (objeto.hasProperty("usuarioServidor")) ? objeto.getProperty("usuarioServidor").toString() : "");
                                    dadosUsuario.put("SENHA_SERVIDOR_USUA", (objeto.hasProperty("senhaServidor")) ? funcoes.criptografaSenha(objeto.getProperty("senhaServidor").toString()) : "");
                                    dadosUsuario.put("PASTA_SERVIDOR_USUA", (objeto.hasProperty("pastaServidor")) ? objeto.getProperty("pastaServidor").toString() : "/");
                                    dadosUsuario.put("MODO_CONEXAO", (objeto.hasProperty("modoConexao")) ? objeto.getProperty("modoConexao").toString() : "W");
                                    dadosUsuario.put("CAMINHO_BANCO_DADOS_USUA", (objeto.hasProperty("caminhoBancoDados")) ? objeto.getProperty("caminhoBancoDados").toString() : "");
                                    dadosUsuario.put("PORTA_BANCO_DADOS_USUA", (objeto.hasProperty("portaBancoDados")) ? objeto.getProperty("portaBancoDados").toString() : "");
                                    dadosUsuario.put("QTDE_CASAS_DECIMAIS", (objeto.hasProperty("quantidadeCasasDecimais")) ? objeto.getProperty("quantidadeCasasDecimais").toString() : "3");

                                    if (!funcoes.getValorXml("SenhaUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                                        // Inclui a senha no registro do banco de dados
                                        dadosUsuario.put("SENHA_USUA", funcoes.getValorXml("SenhaUsuario"));
                                    }

                                    if (!funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                                        // Inclui a senha no registro do banco de dados
                                        dadosUsuario.put("LOGIN_USUA", funcoes.getValorXml("Usuario"));
                                    }

                                    final UsuarioSQL usuarioSql = new UsuarioSQL(context);

                                    // Pega o sql para passar para o statement
                                    //final String sql = usuarioSql.construirSqlStatement(dadosUsuario);
                                    // Pega o argumento para o statement
                                    //final String[] argumentoSql = usuarioSql.argumentoStatement(dadosUsuario);

                                    //usuarioSql.insertOrReplace(dadosUsuario);

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {

                                            // Inseri os dados do usuario no banco de dados interno
                                            //if (usuarioSql.insertOrReplaceFast(sql, argumentoSql) > 0) {
                                            if (usuarioSql.insertOrReplace(dadosUsuario) > 0) {

                                                // Salva os dados necessarios no xml de configuracao da app
                                                salvarDadosXml(dadosUsuario);

                                                inserirUltimaAtualizacao("USUARIO_USUA");

                                                // Atualiza a notificacao
                                                mLoad.bigTextStyle(context.getResources().getString(R.string.usuario_atualizado_sucesso) + " Vamos para o próximo passo, Aguarde...");
                                                mLoad.progress().value(0, 0, true).build();

                                                // Checa se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.usuario_atualizado_sucesso));
                                                        }
                                                    });
                                                }
                                            } else {
                                                //mLoad.dismiss((PendingIntentNotification) context);

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
                                    });
                                }
                            }
                        }*/
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
                        importaDadosEmpresa();
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
                .message(context.getResources().getString(R.string.atualizado_sucesso))
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

        funcoes.setValorXml("CodigoUsuario", usuario.getAsString("ID_USUA"));
        funcoes.setValorXml("CodigoEmpresa", usuario.getAsString("ID_SMAEMPRE"));
        funcoes.setValorXml("Email", usuario.getAsString("EMAIL_USUA"));
        funcoes.setValorXml("EnviarAutomatico", "N");
        funcoes.setValorXml("ReceberAutomatico", "N");
        funcoes.setValorXml("ImagemProduto", "N");
        funcoes.setValorXml("AbriuAppPriveiraVez", "S");
        funcoes.setValorXml("ModoConexao", usuario.getAsString("MODO_CONEXAO"));
        funcoes.setValorXml("IPServidorWebservice", usuario.getAsString("IP_SERVIDOR_WEBSERVICE_USUA"));
        funcoes.setValorXml("IPServidor", usuario.getAsString("IP_SERVIDOR_USUA"));
        funcoes.setValorXml("CaminhoBancoDados", usuario.getAsString("CAMINHO_BANCO_DADOS_USUA"));
        funcoes.setValorXml("PortaBancoDados", usuario.getAsString("PORTA_BANCO_DADOS_USUA"));
        funcoes.setValorXml("UsuarioServidor", usuario.getAsString("ID_USUA"));
        funcoes.setValorXml("SenhaServidor", funcoes.criptografaSenha(usuario.getAsString("SENHA_SERVIDOR_USUA")));
        funcoes.setValorXml("CasasDecimais", usuario.getAsString("QTDE_CASAS_DECIMAIS"));
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


    private boolean importaDadosUsuario(){
        JsonObject statuRetorno = null;

        // Atualiza a notificacao
        mLoad.bigTextStyle(context.getResources().getString(R.string.procurando_dados) + " Uusuário");
        mLoad.progress().value(0, 0, true).build();

        // Checo se o texto de status foi passado pro parametro
        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setText(context.getResources().getString(R.string.procurando_dados) + " Usuário");
                }
            });
        }
        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            String ultimaData = null;

            if (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                // Pega quando foi a ultima data que recebeu dados
                ultimaData = pegaUltimaDataAtualizacao("USUARIO_USUA");
            }
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectParametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectParametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_USUARIO_USUA, WSSisinfoWebservice.METODO_GET, objectParametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonObject usuarioRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

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
                        //for(int i = 0; i < listaUsuarioRetorno.size(); i++){
                            //final JsonObject usuarioRetorno = listaUsuarioRetorno.get(i).getAsJsonObject();
                            
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Usuário: " + usuarioRetorno.get("nomeUsuario").toString());
                            //mLoad.progress().update(0, i, listaUsuarioRetorno.size(), false).build();
                            mLoad.progress().value(0, 0, true).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - Usuário: " + usuarioRetorno.get("nomeUsuario").toString());
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
                            if (usuarioRetorno.get("ativoUsuario").toString().equalsIgnoreCase("0")) {

                                final ContentValues contentValues = new ContentValues();
                                contentValues.put("comando", 0);
                                contentValues.put("tela", "ReceberDadosWebserviceAsyncRotinas");
                                contentValues.put("mensagem", "O usuário dessa chave esta inativo, não podemos baixar os dados dele. Entre em contato com o suporte SAVARE.");
                                contentValues.put("dados", "");
                                // Pega os dados do usuario

                                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                                contentValues.put("empresa", funcoes.getValorXml("ChaveUsuario"));
                                contentValues.put("email", funcoes.getValorXml("Email"));

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        funcoes.menssagem(contentValues);
                                    }
                                });
                                return false;
                            } else {
                                final ContentValues dadosUsuario = new ContentValues();
                                dadosUsuario.put("ID_USUA", usuarioRetorno.get("idUsuario").getAsString());
                                dadosUsuario.put("ID_SMAEMPRE",  usuarioRetorno.get("idEmpresa").getAsInt());
                                dadosUsuario.put("NOME_USUA", usuarioRetorno.get("nomeUsuario").getAsString());
                                dadosUsuario.put("EMAIL_USUA", ((usuarioRetorno.has("email")) ?  usuarioRetorno.get("").getAsString() : ""));
                                dadosUsuario.put("EMPRESA_USUA", usuarioRetorno.get("empresaUsuario").getAsString());
                                dadosUsuario.put("VENDE_ATACADO_USUA", usuarioRetorno.get("vendeAtacadoUsuario").getAsString());
                                dadosUsuario.put("VENDE_VAREJO_USUA", usuarioRetorno.get("vendeVarejoUsuario").getAsString());
                                dadosUsuario.put("ATIVO_USUA", usuarioRetorno.get("ativoUsuario").getAsString());
                                dadosUsuario.put("IP_SERVIDOR_USUA", (usuarioRetorno.has("ipServidor")) ?  usuarioRetorno.get("ipServidor").getAsString() : "");
                                dadosUsuario.put("IP_SERVIDOR_WEBSERVICE_USUA", (usuarioRetorno.has("ipServidorWebservice")) ?  usuarioRetorno.get("ipServidorWebservice").getAsString() : "");
                                dadosUsuario.put("USUARIO_SERVIDOR_USUA", (usuarioRetorno.has("usuarioServidor")) ?  usuarioRetorno.get("usuarioServidor").getAsString() : "");
                                dadosUsuario.put("SENHA_SERVIDOR_USUA", (usuarioRetorno.has("senhaServidor")) ? funcoes.criptografaSenha( usuarioRetorno.get("senhaServidor").getAsString()) : "");
                                dadosUsuario.put("PASTA_SERVIDOR_USUA", (usuarioRetorno.has("pastaServidor")) ?  usuarioRetorno.get("pastaServidor").getAsString() : "/");
                                dadosUsuario.put("MODO_CONEXAO", (usuarioRetorno.has("modoConexao")) ?  usuarioRetorno.get("modoConexao").getAsString() : "W");
                                dadosUsuario.put("CAMINHO_BANCO_DADOS_USUA", (usuarioRetorno.has("caminhoBancoDados")) ?  usuarioRetorno.get("caminhoBancoDados").getAsString() : "");
                                dadosUsuario.put("PORTA_BANCO_DADOS_USUA", (usuarioRetorno.has("portaBancoDados")) ? usuarioRetorno.get("portaBancoDados").getAsString() : "");
                                dadosUsuario.put("QTDE_CASAS_DECIMAIS", (usuarioRetorno.has("quantidadeCasasDecimais")) ?  usuarioRetorno.get("quantidadeCasasDecimais").getAsString() : "3");

                                if (!funcoes.getValorXml("SenhaUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                                    // Inclui a senha no registro do banco de dados
                                    dadosUsuario.put("SENHA_USUA", funcoes.getValorXml("SenhaUsuario"));
                                }

                                if (!funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
                                    // Inclui a senha no registro do banco de dados
                                    dadosUsuario.put("LOGIN_USUA", funcoes.getValorXml("Usuario"));
                                }
                                final UsuarioSQL usuarioSql = new UsuarioSQL(context);

                                // Pega o sql para passar para o statement
                                //final String sql = usuarioSql.construirSqlStatement(dadosUsuario);
                                // Pega o argumento para o statement
                                //final String[] argumentoSql = usuarioSql.argumentoStatement(dadosUsuario);

                                //usuarioSql.insertOrReplace(dadosUsuario);

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {

                                        // Inseri os dados do usuario no banco de dados interno
                                        //if (usuarioSql.insertOrReplaceFast(sql, argumentoSql) > 0) {
                                        if (usuarioSql.insertOrReplace(dadosUsuario) > 0) {

                                            // Salva os dados necessarios no xml de configuracao da app
                                            salvarDadosXml(dadosUsuario);

                                            inserirUltimaAtualizacao("USUARIO_USUA");

                                            // Atualiza a notificacao
                                            mLoad.bigTextStyle(context.getResources().getString(R.string.usuario_atualizado_sucesso) + " Vamos para o próximo passo, Aguarde...");
                                            mLoad.progress().value(0, 0, true).build();

                                            // Checa se o texto de status foi passado pro parametro
                                            if (textStatus != null) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        textStatus.setText(context.getResources().getString(R.string.usuario_atualizado_sucesso));
                                                    }
                                                });
                                            }
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
                                });
                            }
                        //}
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_SMAEMPRE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaEmrpesaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaEmrpesaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_empresa));
                        mLoad.progress().value(0, 0, true).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_empresa));
                                }
                            });
                        }
                        if (progressBarStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setIndeterminate(false);
                                    progressBarStatus.setMax(listaEmrpesaRetorno.size());
                                }
                            });
                        }
                        List<ContentValues> listaDadosEmpresa = new ArrayList<ContentValues>();
                        for(int i = 0; i < listaEmrpesaRetorno.size(); i++){
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + i + "/" + listaEmrpesaRetorno.size());
                            mLoad.progress().update(0, i, listaEmrpesaRetorno.size(), false).build();

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null) {
                                final int finalI1 = i;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        textStatus.setText(context.getResources().getString(R.string.recebendo_dados_empresa) + " - " + finalI1 + "/" + listaEmrpesaRetorno.size());
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
                            JsonObject empresaRetorno = listaEmrpesaRetorno.get(i).getAsJsonObject();

                            ContentValues dadosEmpresa = new ContentValues();
                            // Inseri os valores
                            dadosEmpresa.put("ID_SMAEMPRE", empresaRetorno.get("idEmpresa").getAsInt());
                            if (empresaRetorno.has("idPlanoPagamentoVarejo")) {
                                dadosEmpresa.put("ID_AEAPLPGT_VARE", empresaRetorno.get("idPlanoPagamentoVarejo").getAsInt());
                            }
                            if (empresaRetorno.has("idPlanoPagamentoAtacado")) {
                                dadosEmpresa.put("ID_AEAPLPGT_ATAC", empresaRetorno.get("idPlanoPagamentoAtacado").getAsInt());
                            }
                            dadosEmpresa.put("DT_ALT", empresaRetorno.get("dataAlt").getAsString());
                            dadosEmpresa.put("NOME_RAZAO", empresaRetorno.get("nomeRazao").getAsString());
                            dadosEmpresa.put("NOME_FANTASIA", empresaRetorno.has("nomeFantasia") ? empresaRetorno.get("nomeFantasia").getAsString() : "");
                            dadosEmpresa.put("CPF_CGC", empresaRetorno.has("cpfCnpj") ? empresaRetorno.get("cpfCnpj").getAsString() : "");
                            dadosEmpresa.put("ORC_SEM_ESTOQUE", empresaRetorno.get("orcamentoSemEstoque").getAsString());
                            dadosEmpresa.put("DIAS_ATRAZO", empresaRetorno.get("diasAtrazo").getAsInt());
                            dadosEmpresa.put("SEM_MOVIMENTO", empresaRetorno.get("semMovimento").getAsInt());
                            dadosEmpresa.put("JUROS_DIARIO", empresaRetorno.get("jurosDiario").getAsDouble());
                            dadosEmpresa.put("VENDE_BLOQUEADO_ORC", empresaRetorno.has("vendeBloqueadoOrcamento") ? empresaRetorno.get("vendeBloqueadoOrcamento").getAsString() : "");
                            dadosEmpresa.put("VENDE_BLOQUEADO_PED", empresaRetorno.has("vendeBloqueadoPedido") ? empresaRetorno.get("vendeBloqueadoPedido").getAsString() : "");
                            dadosEmpresa.put("VALIDADE_FICHA_CLIENTE", empresaRetorno.get("validadeFichaCliente").getAsInt());
                            dadosEmpresa.put("VL_MIN_PRAZO_VAREJO", empresaRetorno.get("valorMinimoPrazoVarejo").getAsDouble());
                            dadosEmpresa.put("VL_MIN_PRAZO_ATACADO", empresaRetorno.get("valorMinimoPrazoAtacado").getAsDouble());
                            dadosEmpresa.put("VL_MIN_VISTA_VAREJO", empresaRetorno.get("valorMinimoVistaVarejo").getAsDouble());
                            dadosEmpresa.put("VL_MIN_VISTA_ATACADO", empresaRetorno.get("valorMinimoVistaAtacado").getAsDouble());
                            dadosEmpresa.put("MULTIPLOS_PLANOS", empresaRetorno.has("multiplosPlanos") ? empresaRetorno.get("multiplosPlanos").getAsString() : "");
                            dadosEmpresa.put("QTD_DIAS_DESTACA_PRODUTO", empresaRetorno.get("quantidadeDiasDestacaProduto").getAsInt());
                            dadosEmpresa.put("QTD_CASAS_DECIMAIS", empresaRetorno.has("quantidadeCasasDecimais") ?  empresaRetorno.get("quantidadeCasasDecimais").getAsInt() : 3);
                            dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_ATACADO", empresaRetorno.has("fechaVendaCreditoNegativoAtacado") ? empresaRetorno.get("fechaVendaCreditoNegativoAtacado").getAsString() : "");
                            dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_VAREJO", empresaRetorno.has("fechaVendaCreditoNegativoVarejo") ? empresaRetorno.get("fechaVendaCreditoNegativoVarejo").getAsString() : "");
                            dadosEmpresa.put("TIPO_ACUMULO_CREDITO_ATACADO", empresaRetorno.has("titpoAcumuloCreditoAtacado") ? empresaRetorno.get("titpoAcumuloCreditoAtacado").getAsString() : "");
                            dadosEmpresa.put("TIPO_ACUMULO_CREDITO_VAREJO", empresaRetorno.has("titpoAcumuloCreditoVarejo") ? empresaRetorno.get("titpoAcumuloCreditoVarejo").getAsString() : "");
                            dadosEmpresa.put("PERIODO_CREDITO_ATACADO", empresaRetorno.has("periodocrceditoAtacado") ? empresaRetorno.get("periodocrceditoAtacado").getAsString() : "");
                            dadosEmpresa.put("PERIODO_CREDITO_VAREJO", empresaRetorno.has("periodocrceditoVarejo") ? empresaRetorno.get("periodocrceditoVarejo").getAsString() : "");

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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAAREAS, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaAreasRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaAreasRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_areas));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosAreas.put("ID_CFAAREAS", areasRetorno.get("idAreas").getAsInt());
                            dadosAreas.put("DT_ALT", areasRetorno.get("dataAlt").getAsString());
                            dadosAreas.put("CODIGO", areasRetorno.get("codigoAreas").getAsInt());
                            dadosAreas.put("DESCRICAO", areasRetorno.get("descricaoAreas").getAsString());
                            dadosAreas.put("DESC_ATAC_VISTA", areasRetorno.get("descontoAtacadoVista").getAsDouble());
                            dadosAreas.put("DESC_ATAC_PRAZO", areasRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            dadosAreas.put("DESC_VARE_VISTA", areasRetorno.get("descontoVarejoVista").getAsDouble());
                            dadosAreas.put("DESC_VARE_PRAZO", areasRetorno.get("descontoVarejoPrazo").getAsDouble());
                            dadosAreas.put("DESC_SERV_VISTA", areasRetorno.get("descontoServicoVista").getAsDouble());
                            dadosAreas.put("DESC_SERV_PRAZO", areasRetorno.get("descontoServicoPrazo").getAsDouble());
                            if ((areasRetorno.has("descontoPromocao"))) {
                                dadosAreas.put("DESC_PROMOCAO", areasRetorno.get("descontoPromocao").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaAreasObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS, criaPropriedadeDataAlteracaoWebservice("CFAAREAS"));

            // Checa se retornou alguma coisa
            if ((listaAreasObject != null) && (listaAreasObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaAreasObject.size(), false).build();

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
                            progressBarStatus.setMax(listaAreasObject.size());
                        }
                    });
                }
                int controle = 0;
                List<ContentValues> listaAreas = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAreasObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_areas) + " - " + (finalControle + 1) + "/" + listaAreasObject.size());
                    mLoad.progress().update(0, controle, listaAreasObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_areas) + " - " + (finalControle + 1) + "/" + listaAreasObject.size());
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
                    final SoapObject objeto;

                    if (objetoIndividual.hasProperty("return")) {
                        objeto = (SoapObject) objetoIndividual.getProperty("return");

                    } else {
                        objeto = objetoIndividual;
                    }
                    controle ++;

                    ContentValues dadosAreas = new ContentValues();
                    dadosAreas.put("ID_CFAAREAS", Integer.parseInt(objeto.getProperty("idAreas").toString()));
                    dadosAreas.put("DT_ALT", objeto.getProperty("dataAlt").toString());
                    dadosAreas.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoAreas").toString()));
                    dadosAreas.put("DESCRICAO", objeto.getProperty("descricaoAreas").toString());
                    dadosAreas.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosAreas.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosAreas.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    dadosAreas.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    dadosAreas.put("DESC_SERV_VISTA", Double.parseDouble(objeto.getProperty("descontoServicoVista").toString()));
                    dadosAreas.put("DESC_SERV_PRAZO", Double.parseDouble(objeto.getProperty("descontoServicoPrazo").toString()));
                    if ((objeto.hasProperty("descontoPromocao"))) {
                        dadosAreas.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    listaAreas.add(dadosAreas);
                } // Fim do for
                AreasSql areasSql = new AreasSql(context);

                todosSucesso = areasSql.insertList(listaAreas);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAATIVI, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaAtividadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaAtividadeRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_atividade));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosAtividade.put("ID_CFAATIVI", atividadeRetorno.get("idRamoAtividade").getAsInt());
                            dadosAtividade.put("DT_ALT", atividadeRetorno.get("dataAlteracao").getAsString());
                            dadosAtividade.put("CODIGO", atividadeRetorno.get("codigo").getAsInt());
                            dadosAtividade.put("DESCRICAO", atividadeRetorno.get("descricaoRamoAtividade").getAsString());
                            dadosAtividade.put("DESC_ATAC_VISTA", atividadeRetorno.get("descontoAtacadoVista").getAsDouble());
                            dadosAtividade.put("DESC_ATAC_PRAZO", atividadeRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            dadosAtividade.put("DESC_VARE_VISTA", atividadeRetorno.get("descontoVarejoVista").getAsDouble());
                            dadosAtividade.put("DESC_VARE_PRAZO", atividadeRetorno.get("descontoVarejoPrazo").getAsDouble());
                            if ((atividadeRetorno.has("descontoPromocao"))) {
                                dadosAtividade.put("DESC_PROMOCAO", atividadeRetorno.get("descontoPromocao").getAsString());
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

            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaAtividadeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI, criaPropriedadeDataAlteracaoWebservice("CFAATIVI"));

            // Checa se retornou alguma coisa
            if ((listaAtividadeObject != null) && (listaAtividadeObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaAtividadeObject.size(), false).build();

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
                            progressBarStatus.setMax(listaAtividadeObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaAtividade = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAtividadeObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_atividade) + " - " + (finalControle + 1) + "/" + listaAtividadeObject.size());
                    mLoad.progress().update(0, controle, listaAtividadeObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_atividade) + " - " + (finalControle + 1) + "/" + listaAtividadeObject.size());
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
                    ContentValues dadosAtividade = new ContentValues();
                    dadosAtividade.put("ID_CFAATIVI", Integer.parseInt(objeto.getProperty("idRamoAtividade").toString()));
                    dadosAtividade.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosAtividade.put("CODIGO", Integer.parseInt(objeto.getProperty("codigo").toString()));
                    dadosAtividade.put("DESCRICAO", objeto.getProperty("descricaoRamoAtividade").toString());
                    dadosAtividade.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosAtividade.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosAtividade.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    dadosAtividade.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    if ((objeto.hasProperty("descontoPromocao"))) {
                        dadosAtividade.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    listaAtividade.add(dadosAtividade);
                    //RamoAtividadeSql atividadeSql = new RamoAtividadeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = atividadeSql.construirSqlStatement(dadosAtividade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = atividadeSql.argumentoStatement(dadosAtividade);

                    *//*if (atividadeSql.insertOrReplace(dadosAtividade) <= 0) {
                        todosSucesso = false;
                    }*//*
                } // Fim do for
                RamoAtividadeSql atividadeSql = new RamoAtividadeSql(context);

                todosSucesso = atividadeSql.insertList(listaAtividade);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFASTATU, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaStatusRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaStatusRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_status));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosStatus.put("ID_CFASTATU", statusObsRetorno.get("idStatus").getAsInt());
                            dadosStatus.put("DT_ALT", statusObsRetorno.get("dataAlteracao").getAsString());
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
                            dadosStatus.put("DESC_ATAC_VISTA", statusObsRetorno.get("descontoAtacadoVista").getAsDouble());
                            dadosStatus.put("DESC_ATAC_PRAZO", statusObsRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            dadosStatus.put("DESC_VARE_VISTA", statusObsRetorno.get("descontoVarejoVista").getAsDouble());
                            dadosStatus.put("DESC_VARE_PRAZO", statusObsRetorno.get("descontoVarejoPrazo").getAsDouble());
                            if ((statusObsRetorno.has("descontoPromocao"))) {
                                dadosStatus.put("DESC_PROMOCAO", statusObsRetorno.get("descontoPromocao").getAsString());
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


            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaStatusObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU, criaPropriedadeDataAlteracaoWebservice("CFASTATU"));

            // Checa se retornou alguma coisa
            if ((listaStatusObject != null) && (listaStatusObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaStatusObject.size(), false).build();

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
                            progressBarStatus.setMax(listaStatusObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaStatus = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaStatusObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_status) + " - " + (finalControle + 1) + "/" + listaStatusObject.size());
                    mLoad.progress().update(0, controle, listaStatusObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_status) + " - " + (finalControle + 1) + "/" + listaStatusObject.size());
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

                    ContentValues dadosStatus = new ContentValues();
                    dadosStatus.put("ID_CFASTATU", Integer.parseInt(objeto.getProperty("idStatus").toString()));
                    dadosStatus.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosStatus.put("CODIGO", Integer.parseInt(objeto.getProperty("codigo").toString()));
                    dadosStatus.put("DESCRICAO", objeto.getProperty("descricao").toString());
                    if ((objeto.hasProperty("mensagem"))){
                        dadosStatus.put("MENSAGEM", objeto.getProperty("mensagem").toString());
                    }
                    if ((objeto.hasProperty("bloqueia"))){
                        dadosStatus.put("BLOQUEIA", objeto.getProperty("bloqueia").toString());
                    }
                    if ((objeto.hasProperty("parcelaEmAberto"))){
                        dadosStatus.put("PARCELA_EM_ABERTO", objeto.getProperty("parcelaEmAberto").toString());
                    }
                    if ((objeto.hasProperty("vistaPrazo"))){
                        dadosStatus.put("VISTA_PRAZO", objeto.getProperty("vistaPrazo").toString());
                    }
                    dadosStatus.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosStatus.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosStatus.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    dadosStatus.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    if ((objeto.hasProperty("descontoPromocao"))) {
                        dadosStatus.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }

                    listaStatus.add(dadosStatus);
                    //StatusSql statusSql = new StatusSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = statusSql.construirSqlStatement(dadosStatus);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = statusSql.argumentoStatement(dadosStatus);

                    *//*if (statusSql.insertOrReplace(dadosStatus) <= 0) {
                        todosSucesso = false;
                    }*//*

                } // Fim do for
                StatusSql statusSql = new StatusSql(context);

                todosSucesso = statusSql.insertList(listaStatus);

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
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFASTATU");
                }
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFATPDOC, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaTipoDocumentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaTipoDocumentoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_documento));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosDocumento.put("ID_CFATPDOC", tipoDocumentoRetorno.get("idTipoDocumento").getAsInt());
                            dadosDocumento.put("ID_SMAEMPRE", tipoDocumentoRetorno.get("idEmpresa").getAsInt());
                            dadosDocumento.put("DT_ALT", tipoDocumentoRetorno.get("dataAlteracao").getAsString());
                            dadosDocumento.put("CODIGO", tipoDocumentoRetorno.get("codigoTipoDocumento").getAsInt());
                            if (tipoDocumentoRetorno.has("descricaoTipoDocumento")) {
                                dadosDocumento.put("DESCRICAO", tipoDocumentoRetorno.get("descricaoTipoDocumento").getAsString());
                            }
                            if (tipoDocumentoRetorno.has("siglaTipoDocumento")) {
                                dadosDocumento.put("SIGLA", tipoDocumentoRetorno.get("siglaTipoDocumento").getAsString());
                            }
                            if (tipoDocumentoRetorno.has("tipoVenda")) {
                                dadosDocumento.put("TIPO", tipoDocumentoRetorno.get("tipoVenda").getAsString());
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

            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaDocumentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPDOC, criaPropriedadeDataAlteracaoWebservice("CFATPDOC"));

            // Checa se retornou alguma coisa
            if ((listaDocumentoObject != null) && (listaDocumentoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;
                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaDocumentoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaDocumentoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaTipoDocumento = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaDocumentoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - " + (finalControle + 1) + "/" + listaDocumentoObject.size());
                    mLoad.progress().update(0, controle, listaDocumentoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_documento) + " - " + (finalControle + 1) + "/" + listaDocumentoObject.size());
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

                    ContentValues dadosDocumento = new ContentValues();
                    dadosDocumento.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    dadosDocumento.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosDocumento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosDocumento.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoTipoDocumento").toString()));
                    if (objeto.hasProperty("descricaoTipoDocumento")) {
                        dadosDocumento.put("DESCRICAO", objeto.getProperty("descricaoTipoDocumento").toString());
                    }
                    if (objeto.hasProperty("siglaTipoDocumento")) {
                        dadosDocumento.put("SIGLA", objeto.getProperty("siglaTipoDocumento").toString());
                    }
                    if (objeto.hasProperty("tipoVenda")) {
                        dadosDocumento.put("TIPO", objeto.getProperty("tipoVenda").toString());
                    }

                    listaTipoDocumento.add(dadosDocumento);
                } // Fim do for
                TipoDocumentoSql tipoDocumentoSql = new TipoDocumentoSql(context);

                todosSucesso = tipoDocumentoSql.insertList(listaTipoDocumento);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACCRED, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaCartaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaCartaoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cartao));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosCartao.put("ID_CFACCRED", cartaoRetorno.get("idCartao").getAsInt());
                            dadosCartao.put("DT_ALT", cartaoRetorno.get("dataAlteracao").getAsString());
                            dadosCartao.put("CODIGO", cartaoRetorno.get("codigoCartao").getAsInt());
                            dadosCartao.put("DESCRICAO", cartaoRetorno.get("descricaoCartao").getAsString());

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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaCartaoCreditoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACCRED, criaPropriedadeDataAlteracaoWebservice("CFACCRED"));

            // Checa se retornou alguma coisa
            if ((listaCartaoCreditoObject != null) && (listaCartaoCreditoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaCartaoCreditoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaCartaoCreditoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaCartao = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaCartaoCreditoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cartao) + " - " + (finalControle + 1) + "/" + listaCartaoCreditoObject.size());
                    mLoad.progress().update(0, controle, listaCartaoCreditoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cartao) + " - " + (finalControle + 1) + "/" + listaCartaoCreditoObject.size());
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

                    ContentValues dadosCartao = new ContentValues();
                    dadosCartao.put("ID_CFACCRED", Integer.parseInt(objeto.getProperty("idCartao").toString()));
                    dadosCartao.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosCartao.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoCartao").toString()));
                    dadosCartao.put("DESCRICAO", objeto.getProperty("descricaoCartao").toString());

                    listaCartao.add(dadosCartao);

                    //CartaoSql cartaoSql = new CartaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cartaoSql.construirSqlStatement(dadosCartao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cartaoSql.argumentoStatement(dadosCartao);

                    *//*if (cartaoSql.insertOrReplace(dadosCartao) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cartaoSql.insertOrReplace(dadosCartao);
                            cartaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                CartaoSql cartaoSql = new CartaoSql(context);

                todosSucesso = cartaoSql.insertList(listaCartao);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAPORTA, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaPortadorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaPortadorRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_portador));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosPortador.put("ID_CFAPORTA", portadorRetorno.get("idPortadorBanco").getAsInt());
                            dadosPortador.put("DT_ALT", portadorRetorno.get("dataAlteracao").getAsString());
                            dadosPortador.put("CODIGO", portadorRetorno.get("codigoPortadorBanco").getAsInt());
                            if (portadorRetorno.has("digitoPortador")) {
                                dadosPortador.put("DG", portadorRetorno.get("digitoPortador").getAsInt());
                            }
                            if (portadorRetorno.has("descricaoPortador")) {
                                dadosPortador.put("DESCRICAO", portadorRetorno.get("descricaoPortador").getAsString());
                            }
                            if (portadorRetorno.has("siglaPortador")) {
                                dadosPortador.put("SIGLA", portadorRetorno.get("siglaPortador").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPortadorObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPORTA, criaPropriedadeDataAlteracaoWebservice("CFAPORTA"));

            // Checa se retornou alguma coisa
            if ((listaPortadorObject != null) && (listaPortadorObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaPortadorObject.size(), false).build();

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
                            progressBarStatus.setMax(listaPortadorObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaPortador = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPortadorObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_portador) + " - " + (finalControle + 1) + "/" + listaPortadorObject.size());
                    mLoad.progress().update(0, controle, listaPortadorObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_portador) + " - " + (finalControle + 1) + "/" + listaPortadorObject.size());
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

                    ContentValues dadosPortador = new ContentValues();
                    dadosPortador.put("ID_CFAPORTA", Integer.parseInt(objeto.getProperty("idPortadorBanco").toString()));
                    dadosPortador.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosPortador.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoPortadorBanco").toString()));
                    if (objeto.hasProperty("digitoPortador")) {
                        dadosPortador.put("DG", Integer.parseInt(objeto.getProperty("digitoPortador").toString()));
                    }
                    if (objeto.hasProperty("descricaoPortador")) {
                        dadosPortador.put("DESCRICAO", objeto.getProperty("descricaoPortador").toString());
                    }
                    if (objeto.hasProperty("siglaPortador")) {
                        dadosPortador.put("SIGLA", objeto.getProperty("siglaPortador").toString());
                    }
                    if (objeto.hasProperty("tipo")) {
                        dadosPortador.put("TIPO", objeto.getProperty("tipo").toString());
                    }
                    listaPortador.add(dadosPortador);

                    // Pega o sql para passar para o statement
                    //final String sql = portadorBancoSql.construirSqlStatement(dadosPortador);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = portadorBancoSql.argumentoStatement(dadosPortador);

                    *//*if (portadorBancoSql.insertOrReplace(dadosPortador) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //portadorBancoSql.insertOrReplace(dadosPortador);
                            portadorBancoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                PortadorBancoSql portadorBancoSql = new PortadorBancoSql(context);

                todosSucesso = portadorBancoSql.insertList(listaPortador);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAPROFI, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaProfissaoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaProfissaoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_profisao));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosProfissao.put("ID_CFAPROFI", profissaoRetorno.get("idProfissao").getAsInt());
                            dadosProfissao.put("DT_ALT", profissaoRetorno.get("dataAlteracao").getAsString());
                            dadosProfissao.put("CODIGO", profissaoRetorno.get("codigoProfissao").getAsInt());
                            if (profissaoRetorno.has("cbo")) {
                                dadosProfissao.put("CBO", profissaoRetorno.get("cbo").getAsInt());
                            }
                            dadosProfissao.put("DESCRICAO", profissaoRetorno.get("descricaoProfissao").getAsString());
                            dadosProfissao.put("DESC_ATAC_VISTA", profissaoRetorno.get("descontoAtacadoVista").getAsDouble());
                            dadosProfissao.put("DESC_ATAC_PRAZO", profissaoRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            dadosProfissao.put("DESC_VARE_PRAZO", profissaoRetorno.get("descontoVarejoPrazo").getAsDouble());
                            dadosProfissao.put("DESC_VARE_VISTA", profissaoRetorno.get("descontoVarejoVista").getAsDouble());
                            if (profissaoRetorno.has("promocao")){
                                dadosProfissao.put("DESC_PROMOCAO", profissaoRetorno.get("promocao").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProfissaoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPROFI, criaPropriedadeDataAlteracaoWebservice("CFAPROFI"));

            // Checa se retornou alguma coisa
            if ((listaProfissaoObject != null) && (listaProfissaoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaProfissaoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaProfissaoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaProfissao = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProfissaoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_profisao) + " - " + (finalControle + 1) + "/" + listaProfissaoObject.size());
                    mLoad.progress().update(0, controle, listaProfissaoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_profisao) + " - " + (finalControle + 1) + "/" + listaProfissaoObject.size());
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

                    ContentValues dadosProfissao = new ContentValues();
                    dadosProfissao.put("ID_CFAPROFI", Integer.parseInt(objeto.getProperty("idProfissao").toString()));
                    dadosProfissao.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosProfissao.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoProfissao").toString()));
                    if (objeto.hasProperty("cbo")) {
                        dadosProfissao.put("CBO", Integer.parseInt(objeto.getProperty("cbo").toString()));
                    }
                    dadosProfissao.put("DESCRICAO", objeto.getProperty("descricaoProfissao").toString());
                    dadosProfissao.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosProfissao.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosProfissao.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    dadosProfissao.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    if (objeto.hasProperty("promocao")){
                        dadosProfissao.put("DESC_PROMOCAO", objeto.getProperty("promocao").toString());
                    }
                    listaProfissao.add(dadosProfissao);
                    //ProfissaoSql profissaoSql = new ProfissaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = profissaoSql.construirSqlStatement(dadosProfissao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = profissaoSql.argumentoStatement(dadosProfissao);

                    *//*if (profissaoSql.insertOrReplace(dadosProfissao) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //profissaoSql.insertOrReplace(dadosProfissao);
                            profissaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for

                ProfissaoSql profissaoSql = new ProfissaoSql(context);

                todosSucesso = profissaoSql.insertList(listaProfissao);
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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFATPCLI, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaTipoClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaTipoClienteRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cliente));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosTipoCliente.put("ID_CFATPCLI", tipoClienteRetorno.get("idTipoCliente").getAsInt());
                            dadosTipoCliente.put("DT_ALT", tipoClienteRetorno.get("dataAlteracao").getAsString());
                            dadosTipoCliente.put("CODIGO", tipoClienteRetorno.get("codigoTipoCliente").getAsInt());
                            dadosTipoCliente.put("DESCRICAO", tipoClienteRetorno.get("descricaoTipoCliente").getAsString());
                            dadosTipoCliente.put("DESC_ATAC_VISTA", tipoClienteRetorno.get("descontoAtacadoVista").getAsDouble());
                            dadosTipoCliente.put("DESC_ATAC_PRAZO", tipoClienteRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            dadosTipoCliente.put("DESC_VARE_PRAZO", tipoClienteRetorno.get("descontoVarejoPrazo").getAsDouble());
                            dadosTipoCliente.put("DESC_VARE_VISTA", tipoClienteRetorno.get("descontoVarejoVista").getAsDouble());
                            if (tipoClienteRetorno.has("descontoPromocao")){
                                dadosTipoCliente.put("DESC_PROMOCAO", tipoClienteRetorno.get("descontoPromocao").getAsString());
                            }
                            if (tipoClienteRetorno.has("vendeAtacadoVarejo")){
                                dadosTipoCliente.put("VENDE_ATAC_VAREJO", tipoClienteRetorno.get("vendeAtacadoVarejo").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaTipoClienteObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPCLI, criaPropriedadeDataAlteracaoWebservice("CFATPCLI"));

            // Checa se retornou alguma coisa
            if ((listaTipoClienteObject != null) && (listaTipoClienteObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaTipoClienteObject.size(), false).build();

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
                            progressBarStatus.setMax(listaTipoClienteObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaTipoCliente = new ArrayList<ContentValues>();
                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaTipoClienteObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - " + (finalControle + 1) + "/" + listaTipoClienteObject.size());
                    mLoad.progress().update(0, controle, listaTipoClienteObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cliente) + " - " + (finalControle + 1) + "/" + listaTipoClienteObject.size());
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

                    ContentValues dadosTipoCliente = new ContentValues();
                    dadosTipoCliente.put("ID_CFATPCLI", Integer.parseInt(objeto.getProperty("idTipoCliente").toString()));
                    dadosTipoCliente.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosTipoCliente.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoTipoCliente").toString()));
                    dadosTipoCliente.put("DESCRICAO", objeto.getProperty("descricaoTipoCliente").toString());
                    dadosTipoCliente.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosTipoCliente.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosTipoCliente.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    dadosTipoCliente.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    if (objeto.hasProperty("descontoPromocao")){
                        dadosTipoCliente.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    if (objeto.hasProperty("vendeAtacadoVarejo")){
                        dadosTipoCliente.put("VENDE_ATAC_VAREJO", objeto.getProperty("vendeAtacadoVarejo").toString());
                    }
                    listaTipoCliente.add(dadosTipoCliente);

                    //TipoClienteSql tipoClienteSql = new TipoClienteSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = tipoClienteSql.construirSqlStatement(dadosTipoCliente);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = tipoClienteSql.argumentoStatement(dadosTipoCliente);

                    *//*if (tipoClienteSql.insertOrReplace(dadosTipoCliente) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //tipoClienteSql.insertOrReplace(dadosTipoCliente);
                            tipoClienteSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                TipoClienteSql tipoClienteSql = new TipoClienteSql(context);

                todosSucesso = tipoClienteSql.insertList(listaTipoCliente);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFATPCOB, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaTipoCobrancaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaTipoCobrancaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosTipoCobranca.put("ID_CFATPCOB", tipoCobrancaRetorno.get("idTipoCobranca").getAsInt());
                            dadosTipoCobranca.put("DT_ALT", tipoCobrancaRetorno.get("dataAlteracao").getAsString());
                            dadosTipoCobranca.put("CODIGO", tipoCobrancaRetorno.get("codigo").getAsInt());
                            dadosTipoCobranca.put("DESCRICAO", tipoCobrancaRetorno.get("descricaoTipoCobranca").getAsString());
                            dadosTipoCobranca.put("SIGLA", tipoCobrancaRetorno.get("siglaTipoCobranca").getAsString());

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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaTipoCobrancaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPCOB, criaPropriedadeDataAlteracaoWebservice("CFATPCOB"));

            // Checa se retornou alguma coisa
            if ((listaTipoCobrancaObject != null) && (listaTipoCobrancaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaTipoCobrancaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaTipoCobrancaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaTipoCobranca = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaTipoCobrancaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - " + (finalControle + 1) + "/" + listaTipoCobrancaObject.size());
                    mLoad.progress().update(0, controle, listaTipoCobrancaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_tipo_cobranca) + " - " + (finalControle + 1) + "/" + listaTipoCobrancaObject.size());
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

                    ContentValues dadosTipoCobranca = new ContentValues();
                    dadosTipoCobranca.put("ID_CFATPCOB", Integer.parseInt(objeto.getProperty("idTipoCobranca").toString()));
                    dadosTipoCobranca.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosTipoCobranca.put("CODIGO", Integer.parseInt(objeto.getProperty("codigo").toString()));
                    dadosTipoCobranca.put("DESCRICAO", objeto.getProperty("descricaoTipoCobranca").toString());
                    dadosTipoCobranca.put("SIGLA", objeto.getProperty("siglaTipoCobranca").toString());

                    listaTipoCobranca.add(dadosTipoCobranca);

                    //CobrancaSql cobrancaSql = new CobrancaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cobrancaSql.construirSqlStatement(dadosTipoCobranca);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cobrancaSql.argumentoStatement(dadosTipoCobranca);

                    *//*if (cobrancaSql.insertOrReplace(dadosTipoCobranca) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cobrancaSql.insertOrReplace(dadosTipoCobranca);
                            cobrancaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                CobrancaSql cobrancaSql = new CobrancaSql(context);

                todosSucesso = cobrancaSql.insertList(listaTipoCobranca);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAESTAD");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAESTAD, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaEstadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaEstadoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estado));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosEstado.put("ID_CFAESTAD", estadoRetorno.get("idEstado").getAsInt());
                            dadosEstado.put("DT_ALT", estadoRetorno.get("dataAlteracao").getAsString());
                            dadosEstado.put("DESCRICAO", estadoRetorno.get("descricaoEstado").getAsString());
                            dadosEstado.put("UF", estadoRetorno.get("siglaEstado").getAsString());
                            if (estadoRetorno.has("icmsSaida")) {
                                dadosEstado.put("ICMS_SAI", estadoRetorno.get("icmsSaida").getAsDouble());
                            }
                            if (estadoRetorno.has("ipiSaida")) {
                                dadosEstado.put("IPI_SAI", estadoRetorno.get("ipiSaida").getAsDouble());
                            }
                            if (estadoRetorno.has("tipoIpiSaida")) {
                                dadosEstado.put("TIPO_IPI_SAI", estadoRetorno.get("tipoIpiSaida").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEstadoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAESTAD, criaPropriedadeDataAlteracaoWebservice("CFAESTAD"));

            // Checa se retornou alguma coisa
            if ((listaEstadoObject != null) && (listaEstadoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaEstadoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaEstadoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaEstado = new ArrayList<ContentValues>();
                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEstadoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estado) + " - " + (finalControle + 1) + "/" + listaEstadoObject.size());
                    mLoad.progress().update(0, controle, listaEstadoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estado) + " - " + (finalControle + 1) + "/" + listaEstadoObject.size());
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

                    ContentValues dadosEstado = new ContentValues();
                    dadosEstado.put("ID_CFAESTAD", Integer.parseInt(objeto.getProperty("idEstado").toString()));
                    dadosEstado.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosEstado.put("DESCRICAO", objeto.getProperty("descricaoEstado").toString());
                    dadosEstado.put("UF", objeto.getProperty("siglaEstado").toString());
                    if (objeto.hasProperty("icmsSaida")) {
                        dadosEstado.put("ICMS_SAI", Double.parseDouble(objeto.getProperty("icmsSaida").toString()));
                    }
                    if (objeto.hasProperty("ipiSaida")) {
                        dadosEstado.put("IPI_SAI", Double.parseDouble(objeto.getProperty("ipiSaida").toString()));
                    }
                    if (objeto.hasProperty("tipoIpiSaida")) {
                        dadosEstado.put("TIPO_IPI_SAI", objeto.getProperty("tipoIpiSaida").toString());
                    }
                    listaEstado.add(dadosEstado);

                    // Pega o sql para passar para o statement
                    //final String sql = estadoSql.construirSqlStatement(dadosEstado);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = estadoSql.argumentoStatement(dadosEstado);

                    *//*if (estadoSql.insertOrReplace(dadosEstado) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //estadoSql.insertOrReplace(dadosEstado);
                            estadoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                EstadoSql estadoSql = new EstadoSql(context);

                todosSucesso = estadoSql.insertList(listaEstado);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACIDAD");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACIDAD, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaCidadeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaCidadeRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cidade));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosCidade.put("ID_CFACIDAD", cidadeRetorno.get("idCidade").getAsInt());
                            dadosCidade.put("ID_CFAESTAD", cidadeRetorno.get("idEstado").getAsInt());
                            dadosCidade.put("DT_ALT", cidadeRetorno.get("dataAlteracao").getAsString());
                            dadosCidade.put("COD_IBGE", cidadeRetorno.get("codigoIbge").getAsInt());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaCidadeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACIDAD, criaPropriedadeDataAlteracaoWebservice("CFACIDAD"));

            // Checa se retornou alguma coisa
            if ((listaCidadeObject != null) && (listaCidadeObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaCidadeObject.size(), false).build();

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
                            progressBarStatus.setMax(listaCidadeObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaCidade = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaCidadeObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cidade) + " - " + (finalControle + 1) + "/" + listaCidadeObject.size());
                    mLoad.progress().update(0, controle, listaCidadeObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cidade) + " - " + (finalControle + 1) + "/" + listaCidadeObject.size());
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

                    ContentValues dadosCidade = new ContentValues();
                    dadosCidade.put("ID_CFACIDAD", Integer.parseInt(objeto.getProperty("idCidade").toString()));
                    dadosCidade.put("ID_CFAESTAD", Integer.parseInt(objeto.getProperty("idEstado").toString()));
                    dadosCidade.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosCidade.put("COD_IBGE", Integer.parseInt(objeto.getProperty("codigoIbge").toString()));
                    dadosCidade.put("DESCRICAO", objeto.getProperty("descricao").toString());

                    listaCidade.add(dadosCidade);
                    //CidadeSql cidadeSql = new CidadeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cidadeSql.construirSqlStatement(dadosCidade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cidadeSql.argumentoStatement(dadosCidade);

                    *//*if (cidadeSql.insertOrReplace(dadosCidade) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cidadeSql.insertOrReplace(dadosCidade);
                            cidadeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                CidadeSql cidadeSql = new CidadeSql(context);

                todosSucesso = cidadeSql.insertList(listaCidade);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFACLIFO");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFACLIFO, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaClienteRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaClienteRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosClifo.put("ID_CFACLIFO", clienteRetorno.get("idPessoa").getAsInt());
                            dadosClifo.put("ID_SMAEMPRE", clienteRetorno.get("idEmpresa").getAsInt());
                            dadosClifo.put("CPF_CNPJ", clienteRetorno.has("cpfCnpj") ? clienteRetorno.get("cpfCnpj").getAsString() : "");
                            dadosClifo.put("DT_ALT", clienteRetorno.get("dataAlteracao").getAsString());
                            if (clienteRetorno.has("ieRg")) {
                                dadosClifo.put("IE_RG", clienteRetorno.get("ieRg").getAsString());
                            }
                            dadosClifo.put("NOME_RAZAO", clienteRetorno.get("nomeRazao").getAsString());
                            if (clienteRetorno.has("nomeFantasia")) {
                                dadosClifo.put("NOME_FANTASIA", clienteRetorno.get("nomeFantasia").getAsString());
                            }
                            if (clienteRetorno.has("dataNascimento")) {
                                dadosClifo.put("DT_NASCIMENTO", clienteRetorno.get("dataNascimento").getAsString());
                            }
                            if (clienteRetorno.has("codigoCliente")) {
                                dadosClifo.put("CODIGO_CLI", clienteRetorno.get("codigoCliente").getAsInt());
                            }
                            if (clienteRetorno.has("codigoFuncionario")) {
                                dadosClifo.put("CODIGO_FUN", clienteRetorno.get("codigoFuncionario").getAsInt());
                            }
                            if (clienteRetorno.has("codigoUsuario")) {
                                dadosClifo.put("CODIGO_USU", clienteRetorno.get("codigoUsuario").getAsInt());
                            }
                            if (clienteRetorno.has("codigoTransportadora")) {
                                dadosClifo.put("CODIGO_TRA", clienteRetorno.get("codigoTransportadora").getAsInt());
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
                            if (clienteRetorno.has("inscricaoSuframa")) {
                                dadosClifo.put("INSC_SUFRAMA", clienteRetorno.get("inscricaoSuframa").getAsString());
                            }
                            if (clienteRetorno.has("inscricaoJunta")) {
                                dadosClifo.put("INSC_JUNTA", clienteRetorno.get("inscricaoJunta").getAsString());
                            }
                            if (clienteRetorno.has("inscricaoMunicipal")) {
                                dadosClifo.put("INSC_MUNICIPAL", clienteRetorno.get("inscricaoMunicipal").getAsString());
                            }
                            if (clienteRetorno.has("inscricaoProdutor")) {
                                dadosClifo.put("INSC_PRODUTOR", clienteRetorno.get("inscricaoProdutor").getAsString());
                            }
                            if (clienteRetorno.has("rendaMesGiro")) {
                                dadosClifo.put("RENDA_MES_GIRO", clienteRetorno.get("rendaMesGiro").getAsDouble());
                            }
                            if (clienteRetorno.has("capitalSocial")) {
                                dadosClifo.put("CAPITAL_SOCIAL", clienteRetorno.get("capitalSocial").getAsDouble());
                            }
                            if (clienteRetorno.has("estoqueMercadorias")) {
                                dadosClifo.put("EST_MERCADORIAS", clienteRetorno.get("estoqueMercadorias").getAsDouble());
                            }
                            if (clienteRetorno.has("estoqueMateriaPrima")) {
                                dadosClifo.put("EST_MAT_PRIMA", clienteRetorno.get("estoqueMateriaPrima").getAsDouble());
                            }
                            if (clienteRetorno.has("movimentoVendas")) {
                                dadosClifo.put("MOVTO_VENDAS", clienteRetorno.get("movimentoVendas").getAsDouble());
                            }
                            if (clienteRetorno.has("despesas")) {
                                dadosClifo.put("DESPESAS", clienteRetorno.get("despesas").getAsDouble());
                            }
                            if (clienteRetorno.has("empresaTrabalho")) {
                                dadosClifo.put("EMPRESA_TRAB", clienteRetorno.get("empresaTrabalho").getAsString());
                            }
                            if (clienteRetorno.has("observacao")) {
                                dadosClifo.put("OBS", clienteRetorno.get("observacao").getAsString());
                            }
                            if (clienteRetorno.has("pessoa")) {
                                dadosClifo.put("PESSOA", clienteRetorno.get("pessoa").getAsString());
                            }
                            if (clienteRetorno.has("civil")) {
                                dadosClifo.put("CIVIL", clienteRetorno.get("civil").getAsString());
                            }
                            if (clienteRetorno.has("Conjuge")) {
                                dadosClifo.put("CONJUGE", clienteRetorno.get("Conjuge").getAsString());
                            }
                            if (clienteRetorno.has("cpfConjuge")) {
                                dadosClifo.put("CPF_CONJUGE", clienteRetorno.get("cpfConjuge").getAsString());
                            }
                            if (clienteRetorno.has("dataNascimentoConjuge")) {
                                dadosClifo.put("DT_NAC_CONJ", clienteRetorno.get("dataNascimentoConjuge").getAsString());
                            }
                            if (clienteRetorno.has("quantidadeFuncionarios")) {
                                dadosClifo.put("QTDE_FUNCIONARIOS", clienteRetorno.get("quantidadeFuncionarios").getAsInt());
                            }
                            if (clienteRetorno.has("outrasRendas")) {
                                dadosClifo.put("OUTRAS_RENDAS", clienteRetorno.get("outrasRendas").getAsDouble());
                            }
                            if (clienteRetorno.has("numeroDependenteMaior")) {
                                dadosClifo.put("NUM_DEP_MAIOR", clienteRetorno.get("numeroDependenteMaior").getAsInt());
                            }
                            if (clienteRetorno.has("numeroDependenteMenor")) {
                                dadosClifo.put("NUM_DEP_MENOR", clienteRetorno.get("numeroDependenteMenor").getAsInt());
                            }
                            if (clienteRetorno.has("complementoCargoConjuge")) {
                                dadosClifo.put("COMPLEMENTO_CARGO_CONJ", clienteRetorno.get("complementoCargoConjuge").getAsString());
                            }
                            if (clienteRetorno.has("rgConjuge")) {
                                dadosClifo.put("RG_CONJUGE", clienteRetorno.get("rgConjuge").getAsString());
                            }
                            if (clienteRetorno.has("orgaoEmissorConjuge")) {
                                dadosClifo.put("ORGAO_EMISSOR_CONJ", clienteRetorno.get("orgaoEmissorConjuge").getAsString());
                            }
                            if (clienteRetorno.has("limiteConjuge")) {
                                dadosClifo.put("LIMITE_CONJUGE", clienteRetorno.get("limiteConjuge").getAsDouble());
                            }
                            if (clienteRetorno.has("empresaConjuge")) {
                                dadosClifo.put("EMPRESA_CONJUGE", clienteRetorno.get("empresaConjuge").getAsString());
                            }
                            if (clienteRetorno.has("dataAdmissaoConjuge")) {
                                dadosClifo.put("ADMISSAO_CONJUGE", clienteRetorno.get("dataAdmissaoConjuge").getAsString());
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
                            if (clienteRetorno.has("conjugePodeComprar")) {
                                dadosClifo.put("CONJ_PODE_COMPRAR", clienteRetorno.get("conjugePodeComprar").getAsString());
                            }
                            if (clienteRetorno.has("dataUltimaCompra")) {
                                dadosClifo.put("DT_ULT_COMPRA", clienteRetorno.get("dataUltimaCompra").getAsString());
                            }
                            if (clienteRetorno.has("dataRenovacao")) {
                                dadosClifo.put("DT_RENOVACAO", clienteRetorno.get("dataRenovacao").getAsString());
                            }
                            if (clienteRetorno.has("statusPessoa")) {
                                JsonObject status = clienteRetorno.getAsJsonObject("statusPessoa");
                                dadosClifo.put("ID_CFASTATU", status.get("idStatus").getAsInt());
                            }
                            if (clienteRetorno.has("ramoAtividade")) {
                                JsonObject ramoAtividade = clienteRetorno.getAsJsonObject("ramoAtividade");
                                dadosClifo.put("ID_CFAATIVI", ramoAtividade.get("idRamoAtividade").getAsInt());
                            }
                            if (clienteRetorno.has("tipoClientePessoa")) {
                                JsonObject tipoClientePessoa = clienteRetorno.getAsJsonObject("tipoClientePessoa");
                                dadosClifo.put("ID_CFATPCLI", tipoClientePessoa.get("idTipoCliente").getAsInt());
                            }
                            if (clienteRetorno.has("tipoClientePessoa")) {
                                JsonObject tipoClientePessoa = clienteRetorno.getAsJsonObject("tipoClientePessoa");
                                dadosClifo.put("ID_CFATPCLI", tipoClientePessoa.get("idTipoCliente").getAsInt());
                            }
                            if (clienteRetorno.has("profissaoPessoa")) {
                                JsonObject tipoClientePessoa = clienteRetorno.getAsJsonObject("profissaoPessoa");
                                dadosClifo.put("ID_CFAPROFI", tipoClientePessoa.get("idProfissao").getAsInt());
                            }
                            if (clienteRetorno.has("areaPessoa")) {
                                JsonObject areaPessoa = clienteRetorno.getAsJsonObject("areaPessoa");
                                if (areaPessoa.has("idAreas") && areaPessoa.get("idAreas").getAsInt() > 0) {
                                    dadosClifo.put("ID_CFAAREAS", areaPessoa.get("idAreas").getAsInt());
                                }
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaClifoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACLIFO, criaPropriedadeDataAlteracaoWebservice("CFACLIFO"));

            // Checa se retornou alguma coisa
            if ((listaClifoObject != null) && (listaClifoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaClifoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaClifoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaCliente = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaClifoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_cliente) + " - " + (finalControle + 1) + "/" + listaClifoObject.size());
                    mLoad.progress().update(0, controle, listaClifoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_cliente) + " - " + (finalControle + 1) + "/" + listaClifoObject.size());
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

                    ContentValues dadosClifo = new ContentValues();
                    dadosClifo.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idPessoa").toString()));
                    dadosClifo.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosClifo.put("CPF_CNPJ", objeto.hasProperty("cpfCnpj") ? objeto.getProperty("cpfCnpj").toString() : "");
                    dadosClifo.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    if (objeto.hasProperty("ieRg")) {
                        dadosClifo.put("IE_RG", objeto.getProperty("ieRg").toString());
                    }
                    dadosClifo.put("NOME_RAZAO", objeto.getProperty("nomeRazao").toString());
                    if (objeto.hasProperty("nomeFantasia")) {
                        dadosClifo.put("NOME_FANTASIA", objeto.getProperty("nomeFantasia").toString());
                    }
                    if (objeto.hasProperty("dataNascimento")) {
                        dadosClifo.put("DT_NASCIMENTO", objeto.getProperty("dataNascimento").toString());
                    }
                    if (objeto.hasProperty("codigoCliente")) {
                        dadosClifo.put("CODIGO_CLI", objeto.getProperty("codigoCliente").toString());
                    }
                    if (objeto.hasProperty("codigoFuncionario")) {
                        dadosClifo.put("CODIGO_FUN", objeto.getProperty("codigoFuncionario").toString());
                    }
                    if (objeto.hasProperty("codigoUsuario")) {
                        dadosClifo.put("CODIGO_USU", objeto.getProperty("codigoUsuario").toString());
                    }
                    if (objeto.hasProperty("codigoTransportadora")) {
                        dadosClifo.put("CODIGO_TRA", objeto.getProperty("codigoTransportadora").toString());
                    }
                    if (objeto.hasProperty("cliente")) {
                        dadosClifo.put("CLIENTE", objeto.getProperty("cliente").toString());
                    }
                    if (objeto.hasProperty("funcionario")) {
                        dadosClifo.put("FUNCIONARIO", objeto.getProperty("funcionario").toString());
                    }
                    if (objeto.hasProperty("usuario")) {
                        dadosClifo.put("USUARIO", objeto.getProperty("usuario").toString());
                    }
                    if (objeto.hasProperty("transportadora")) {
                        dadosClifo.put("TRANSPORTADORA", objeto.getProperty("transportadora").toString());
                    }
                    if (objeto.hasProperty("sexo")) {
                        dadosClifo.put("SEXO", objeto.getProperty("sexo").toString());
                    }
                    if (objeto.hasProperty("inscricaoSuframa")) {
                        dadosClifo.put("INSC_SUFRAMA", objeto.getProperty("inscricaoSuframa").toString());
                    }
                    if (objeto.hasProperty("inscricaoJunta")) {
                        dadosClifo.put("INSC_JUNTA", objeto.getProperty("inscricaoJunta").toString());
                    }
                    if (objeto.hasProperty("inscricaoMunicipal")) {
                        dadosClifo.put("INSC_MUNICIPAL", objeto.getProperty("inscricaoMunicipal").toString());
                    }
                    if (objeto.hasProperty("inscricaoProdutor")) {
                        dadosClifo.put("INSC_PRODUTOR", objeto.getProperty("inscricaoProdutor").toString());
                    }
                    if (objeto.hasProperty("rendaMesGiro")) {
                        dadosClifo.put("RENDA_MES_GIRO", Double.parseDouble(objeto.getProperty("rendaMesGiro").toString()));
                    }
                    if (objeto.hasProperty("capitalSocial")) {
                        dadosClifo.put("CAPITAL_SOCIAL", Double.parseDouble(objeto.getProperty("capitalSocial").toString()));
                    }
                    if (objeto.hasProperty("estoqueMercadorias")) {
                        dadosClifo.put("EST_MERCADORIAS", Double.parseDouble(objeto.getProperty("estoqueMercadorias").toString()));
                    }
                    if (objeto.hasProperty("estoqueMateriaPrima")) {
                        dadosClifo.put("EST_MAT_PRIMA", Double.parseDouble(objeto.getProperty("estoqueMateriaPrima").toString()));
                    }
                    if (objeto.hasProperty("movimentoVendas")) {
                        dadosClifo.put("MOVTO_VENDAS", Double.parseDouble(objeto.getProperty("movimentoVendas").toString()));
                    }
                    if (objeto.hasProperty("despesas")) {
                        dadosClifo.put("DESPESAS", Double.parseDouble(objeto.getProperty("despesas").toString()));
                    }
                    if (objeto.hasProperty("empresaTrabalho")) {
                        dadosClifo.put("EMPRESA_TRAB", objeto.getProperty("empresaTrabalho").toString());
                    }
                    if (objeto.hasProperty("observacao")) {
                        dadosClifo.put("OBS", objeto.getProperty("observacao").toString());
                    }
                    if (objeto.hasProperty("pessoa")) {
                        dadosClifo.put("PESSOA", objeto.getProperty("pessoa").toString());
                    }
                    if (objeto.hasProperty("civil")) {
                        dadosClifo.put("CIVIL", objeto.getProperty("civil").toString());
                    }
                    if (objeto.hasProperty("Conjuge")) {
                        dadosClifo.put("CONJUGE", objeto.getProperty("Conjuge").toString());
                    }
                    if (objeto.hasProperty("cpfConjuge")) {
                        dadosClifo.put("CPF_CONJUGE", objeto.getProperty("cpfConjuge").toString());
                    }
                    if (objeto.hasProperty("dataNascimentoConjuge")) {
                        dadosClifo.put("DT_NAC_CONJ", objeto.getProperty("dataNascimentoConjuge").toString());
                    }
                    if (objeto.hasProperty("quantidadeFuncionarios")) {
                        dadosClifo.put("QTDE_FUNCIONARIOS", Integer.parseInt(objeto.getProperty("quantidadeFuncionarios").toString()));
                    }
                    if (objeto.hasProperty("outrasRendas")) {
                        dadosClifo.put("OUTRAS_RENDAS", Double.parseDouble(objeto.getProperty("outrasRendas").toString()));
                    }
                    if (objeto.hasProperty("numeroDependenteMaior")) {
                        dadosClifo.put("NUM_DEP_MAIOR", Integer.parseInt(objeto.getProperty("numeroDependenteMaior").toString()));
                    }
                    if (objeto.hasProperty("numeroDependenteMenor")) {
                        dadosClifo.put("NUM_DEP_MENOR", Integer.parseInt(objeto.getProperty("numeroDependenteMenor").toString()));
                    }
                    if (objeto.hasProperty("complementoCargoConjuge")) {
                        dadosClifo.put("COMPLEMENTO_CARGO_CONJ", objeto.getProperty("complementoCargoConjuge").toString());
                    }
                    if (objeto.hasProperty("rgConjuge")) {
                        dadosClifo.put("RG_CONJUGE", objeto.getProperty("rgConjuge").toString());
                    }
                    if (objeto.hasProperty("orgaoEmissorConjuge")) {
                        dadosClifo.put("ORGAO_EMISSOR_CONJ", objeto.getProperty("orgaoEmissorConjuge").toString());
                    }
                    if (objeto.hasProperty("limiteConjuge")) {
                        dadosClifo.put("LIMITE_CONJUGE", Double.parseDouble(objeto.getProperty("limiteConjuge").toString()));
                    }
                    if (objeto.hasProperty("empresaConjuge")) {
                        dadosClifo.put("EMPRESA_CONJUGE", objeto.getProperty("empresaConjuge").toString());
                    }
                    if (objeto.hasProperty("dataAdmissaoConjuge")) {
                        dadosClifo.put("ADMISSAO_CONJUGE", objeto.getProperty("dataAdmissaoConjuge").toString());
                    }
                    if (objeto.hasProperty("rendaConjuge")) {
                        dadosClifo.put("RENDA_CONJUGE", Double.parseDouble(objeto.getProperty("rendaConjuge").toString()));
                    }
                    if (objeto.hasProperty("enviarExtrato")) {
                        dadosClifo.put("ENVIAR_EXTRATO", objeto.getProperty("enviarExtrato").toString());
                    }
                    if (objeto.hasProperty("tipoExtrato")) {
                        dadosClifo.put("TIPO_EXTRATO", objeto.getProperty("tipoExtrato").toString());
                    }
                    if (objeto.hasProperty("conjugePodeComprar")) {
                        dadosClifo.put("CONJ_PODE_COMPRAR", objeto.getProperty("conjugePodeComprar").toString());
                    }
                    if (objeto.hasProperty("dataUltimaCompra")) {
                        dadosClifo.put("DT_ULT_COMPRA", objeto.getProperty("dataUltimaCompra").toString());
                    }
                    if (objeto.hasProperty("dataRenovacao")) {
                        dadosClifo.put("DT_RENOVACAO", objeto.getProperty("dataRenovacao").toString());
                    }
                    if (objeto.hasProperty("statusPessoa")) {
                        SoapObject status = (SoapObject) objeto.getProperty("statusPessoa");
                        dadosClifo.put("ID_CFASTATU", Integer.parseInt(status.getProperty("idStatus").toString()));
                    }
                    if (objeto.hasProperty("ramoAtividade")) {
                        SoapObject ramoAtividade = (SoapObject) objeto.getProperty("ramoAtividade");
                        dadosClifo.put("ID_CFAATIVI", Integer.parseInt(ramoAtividade.getProperty("idRamoAtividade").toString()));
                    }
                    if (objeto.hasProperty("tipoClientePessoa")) {
                        SoapObject tipoClientePessoa = (SoapObject) objeto.getProperty("tipoClientePessoa");
                        dadosClifo.put("ID_CFATPCLI", Integer.parseInt(tipoClientePessoa.getProperty("idTipoCliente").toString()));
                    }
                    if (objeto.hasProperty("tipoClientePessoa")) {
                        SoapObject tipoClientePessoa = (SoapObject) objeto.getProperty("tipoClientePessoa");
                        dadosClifo.put("ID_CFATPCLI", Integer.parseInt(tipoClientePessoa.getProperty("idTipoCliente").toString()));
                    }
                    if (objeto.hasProperty("profissaoPessoa")) {
                        SoapObject tipoClientePessoa = (SoapObject) objeto.getProperty("profissaoPessoa");
                        dadosClifo.put("ID_CFAPROFI", Integer.parseInt(tipoClientePessoa.getProperty("idProfissao").toString()));
                    }
                    if (objeto.hasProperty("areaPessoa")) {
                        SoapObject areaPessoa = (SoapObject) objeto.getProperty("areaPessoa");
                        if (areaPessoa.hasProperty("idAreas")) {
                            dadosClifo.put("ID_CFAAREAS", Integer.parseInt(areaPessoa.getProperty("idAreas").toString()));
                        }
                    }
                    listaCliente.add(dadosClifo);

                    //PessoaSql pessoaSql = new PessoaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = pessoaSql.construirSqlStatement(dadosClifo);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = pessoaSql.argumentoStatement(dadosClifo);

                    *//*if (pessoaSql.insertOrReplace(dadosClifo) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //pessoaSql.insertOrReplace(dadosClifo);
                            pessoaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                PessoaSql pessoaSql = new PessoaSql(context);

                todosSucesso = pessoaSql.insertList(listaCliente);

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
            }*/
        }catch (Exception e){

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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAENDER");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAENDER, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaEnderecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaEnderecoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosEndereco.put("ID_CFAENDER", enderecoRetorno.get("idEndereco").getAsInt());
                            dadosEndereco.put("DT_ALT", enderecoRetorno.get("dataAlteracao").getAsString());
                            dadosEndereco.put("TIPO", enderecoRetorno.get("tipoEndereco").getAsString());
                            if (enderecoRetorno.has("idClifoEndereco") && enderecoRetorno.get("idClifoEndereco").getAsInt() > 0){
                                dadosEndereco.put("ID_CFACLIFO", enderecoRetorno.get("idClifoEndereco").getAsInt());
                            }
                            if (enderecoRetorno.has("idEmrpesa") && enderecoRetorno.get("idEmrpesa").getAsInt() > 0){
                                dadosEndereco.put("ID_SMAEMPRE", enderecoRetorno.get("idEmrpesa").getAsInt());
                            }
                            if (enderecoRetorno.has("estadoEndereco")){
                                JsonObject estado = enderecoRetorno.getAsJsonObject("estadoEndereco");
                                dadosEndereco.put("ID_CFAESTAD", estado.get("idEstado").getAsInt());
                            }
                            if (enderecoRetorno.has("cidadeEndereco")){
                                JsonObject cidade = enderecoRetorno.getAsJsonObject("cidadeEndereco");
                                dadosEndereco.put("ID_CFACIDAD", cidade.get("idCidade").getAsInt());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEnderecoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAENDER, criaPropriedadeDataAlteracaoWebservice("CFAENDER"));

            // Checa se retornou alguma coisa
            if ((listaEnderecoObject != null) && (listaEnderecoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaEnderecoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaEnderecoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaEndereco = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEnderecoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + (finalControle + 1) + "/" + listaEnderecoObject.size());
                    mLoad.progress().update(0, controle, listaEnderecoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_endereco) + " - " + (finalControle + 1) + "/" + listaEnderecoObject.size());
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

                    ContentValues dadosEndereco = new ContentValues();
                    dadosEndereco.put("ID_CFAENDER", Integer.parseInt(objeto.getProperty("idEndereco").toString()));
                    dadosEndereco.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosEndereco.put("TIPO", objeto.getProperty("tipoEndereco").toString());
                    if (objeto.hasProperty("idClifoEndereco")){
                        dadosEndereco.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idClifoEndereco").toString()));
                    }
                    if (objeto.hasProperty("idEmrpesa")){
                        dadosEndereco.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmrpesa").toString()));
                    }
                    if (objeto.hasProperty("estadoEndereco")){
                        SoapObject estado = (SoapObject) objeto.getProperty("estadoEndereco");
                        dadosEndereco.put("ID_CFAESTAD", Integer.parseInt(estado.getProperty("idEstado").toString()));
                    }
                    if (objeto.hasProperty("cidadeEndereco")){
                        SoapObject estado = (SoapObject) objeto.getProperty("cidadeEndereco");
                        dadosEndereco.put("ID_CFACIDAD", Integer.parseInt(estado.getProperty("idCidade").toString()));
                    }
                    if (objeto.hasProperty("cep")){
                        dadosEndereco.put("CEP", objeto.getProperty("cep").toString());
                    }
                    if (objeto.hasProperty("bairro")){
                        dadosEndereco.put("BAIRRO", objeto.getProperty("bairro").toString());
                    }
                    if (objeto.hasProperty("logradouro")){
                        dadosEndereco.put("LOGRADOURO", objeto.getProperty("logradouro").toString());
                    }
                    if (objeto.hasProperty("numero")){
                        dadosEndereco.put("NUMERO", objeto.getProperty("numero").toString());
                    }
                    if (objeto.hasProperty("complemento")){
                        dadosEndereco.put("COMPLEMENTO", objeto.getProperty("complemento").toString());
                    }
                    if (objeto.hasProperty("email")){
                        dadosEndereco.put("EMAIL", objeto.getProperty("email").toString());
                    }
                    listaEndereco.add(dadosEndereco);

                    //EnderecoSql enderecoSql = new EnderecoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = enderecoSql.construirSqlStatement(dadosEndereco);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = enderecoSql.argumentoStatement(dadosEndereco);

                    *//*if (enderecoSql.insertOrReplace(dadosEndereco) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //enderecoSql.insertOrReplace(dadosEndereco);
                            enderecoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                EnderecoSql enderecoSql = new EnderecoSql(context);

                todosSucesso = enderecoSql.insertList(listaEndereco);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("CFAPARAM");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAPARAM, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaParametroRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaParametroRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parametro));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosParametros.put("ID_CFAPARAM", parametroRetorno.get("idParametro").getAsInt());
                            dadosParametros.put("DT_ALT", parametroRetorno.get("dataAlteracao").getAsString());
                            if (parametroRetorno.has("idClifo") && parametroRetorno.get("idClifo").getAsInt() > 0){
                                dadosParametros.put("ID_CFACLIFO", parametroRetorno.get("idClifo").getAsInt());
                            }
                            if (parametroRetorno.has("idEmpresa") && parametroRetorno.get("idEmpresa").getAsInt() > 0){
                                dadosParametros.put("ID_SMAEMPRE", parametroRetorno.get("idEmpresa").getAsInt());
                            }
                            if (parametroRetorno.has("idVendedor") && parametroRetorno.get("idVendedor").getAsInt() > 0){
                                dadosParametros.put("ID_CFACLIFO_VENDE", parametroRetorno.get("idVendedor").getAsInt());
                            }
                            if (parametroRetorno.has("idTipoCobranca") && parametroRetorno.get("idTipoCobranca").getAsInt() > 0){
                                dadosParametros.put("ID_CFATPCOB", parametroRetorno.get("idTipoCobranca").getAsInt());
                            }
                            if (parametroRetorno.has("idPortadorBanco") && parametroRetorno.get("idPortadorBanco").getAsInt() > 0){
                                dadosParametros.put("ID_CFAPORTA", parametroRetorno.get("idPortadorBanco").getAsInt());
                            }
                            if (parametroRetorno.has("idTipoDocumento") && parametroRetorno.get("idTipoDocumento").getAsInt() > 0){
                                dadosParametros.put("ID_CFATPDOC", parametroRetorno.get("idTipoDocumento").getAsInt());
                            }
                            if (parametroRetorno.has("idPlanoPagamento") && parametroRetorno.get("idPlanoPagamento").getAsInt() > 0){
                                dadosParametros.put("ID_AEAPLPGT", parametroRetorno.get("idPlanoPagamento").getAsInt());
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
                            if (parametroRetorno.has("descontoPromocao")){
                                dadosParametros.put("DESC_PROMOCAO", parametroRetorno.get("descontoPromocao").getAsString());
                            }
                            if (parametroRetorno.has("dataUltimaVisita")){
                                dadosParametros.put("DT_ULT_VISITA", parametroRetorno.get("dataUltimaVisita").getAsString());
                            }
                            if (parametroRetorno.has("dataUltimoEnvio")){
                                dadosParametros.put("DT_ULT_ENVIO", parametroRetorno.get("dataUltimoEnvio").getAsString());
                            }
                            if (parametroRetorno.has("dataUltimoRecebimento")){
                                dadosParametros.put("DT_ULT_RECEBTO", parametroRetorno.get("dataUltimoRecebimento").getAsString());
                            }
                            if (parametroRetorno.has("dataProximoContato")){
                                dadosParametros.put("DT_PROXIMO_CONTATO", parametroRetorno.get("dataProximoContato").getAsString());
                            }
                            if (parametroRetorno.has("atacadoVarejo")){
                                dadosParametros.put("ATACADO_VEREJO", parametroRetorno.get("atacadoVarejo").getAsString());
                            }
                            if (parametroRetorno.has("vistaPrazo")){
                                dadosParametros.put("VISTA_PRAZO", parametroRetorno.get("vistaPrazo").getAsString());
                            }
                            if (parametroRetorno.has("faturaValorMinimo")){
                                dadosParametros.put("FATURA_VL_MIN", parametroRetorno.get("faturaValorMinimo").getAsString());
                            }
                            if (parametroRetorno.has("parcelaEmAberto")){
                                dadosParametros.put("PARCELA_EM_ABERTO", parametroRetorno.get("parcelaEmAberto").getAsString());
                            }
                            if (parametroRetorno.has("limite")){
                                dadosParametros.put("LIMITE", parametroRetorno.get("limite").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoAtacadoVista")){
                                dadosParametros.put("DESC_ATAC_VISTA", parametroRetorno.get("descontoAtacadoVista").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoAtacadoPrazo")){
                                dadosParametros.put("DESC_ATAC_PRAZO", parametroRetorno.get("descontoAtacadoPrazo").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoVarejoVista")){
                                dadosParametros.put("DESC_VARE_VISTA", parametroRetorno.get("descontoVarejoVista").getAsDouble());
                            }
                            if (parametroRetorno.has("descontoVarejoPrazo")){
                                dadosParametros.put("DESC_VARE_PRAZO", parametroRetorno.get("descontoVarejoPrazo").getAsDouble());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaParametroObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPARAM, criaPropriedadeDataAlteracaoWebservice("CFAPARAM"));

            // Checa se retornou alguma coisa
            if ((listaParametroObject != null) && (listaParametroObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaParametroObject.size(), false).build();

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
                            progressBarStatus.setMax(listaParametroObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaParametros = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaParametroObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parametro) + " - " + (finalControle + 1) + "/" + listaParametroObject.size());
                    mLoad.progress().update(0, controle, listaParametroObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parametro) + " - " + (finalControle + 1) + "/" + listaParametroObject.size());
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

                    ContentValues dadosParametros = new ContentValues();
                    dadosParametros.put("ID_CFAPARAM", Integer.parseInt(objeto.getProperty("idParametro").toString()));
                    dadosParametros.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    if (objeto.hasProperty("idClifo")){
                        dadosParametros.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idClifo").toString()));
                    }
                    if (objeto.hasProperty("idEmpresa")){
                        dadosParametros.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("idVendedor")){
                        dadosParametros.put("ID_CFACLIFO_VENDE", Integer.parseInt(objeto.getProperty("idVendedor").toString()));
                    }
                    if (objeto.hasProperty("idTipoCobranca")){
                        dadosParametros.put("ID_CFATPCOB", Integer.parseInt(objeto.getProperty("idTipoCobranca").toString()));
                    }
                    if (objeto.hasProperty("idPortadorBanco")){
                        dadosParametros.put("ID_CFAPORTA", Integer.parseInt(objeto.getProperty("idPortadorBanco").toString()));
                    }
                    if (objeto.hasProperty("idTipoDocumento")){
                        dadosParametros.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    }
                    if (objeto.hasProperty("idPlanoPagamento")){
                        dadosParametros.put("ID_AEAPLPGT", Integer.parseInt(objeto.getProperty("idPlanoPagamento").toString()));
                    }
                    if (objeto.hasProperty("roteiro")){
                        dadosParametros.put("ROTEIRO", Integer.parseInt(objeto.getProperty("roteiro").toString()));
                    }
                    if (objeto.hasProperty("frequencia")){
                        dadosParametros.put("FREQUENCIA", Integer.parseInt(objeto.getProperty("frequencia").toString()));
                    }
                    if (objeto.hasProperty("diasAtrazo")){
                        dadosParametros.put("DIAS_ATRAZO", Integer.parseInt(objeto.getProperty("diasAtrazo").toString()));
                    }
                    if (objeto.hasProperty("diasCarencia")){
                        dadosParametros.put("DIAS_CARENCIA", Integer.parseInt(objeto.getProperty("diasCarencia").toString()));
                    }
                    if (objeto.hasProperty("vendeAtrazado")){
                        dadosParametros.put("VENDE_ATRAZADO", objeto.getProperty("vendeAtrazado").toString());
                    }
                    if (objeto.hasProperty("descontoPromocao")){
                        dadosParametros.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    if (objeto.hasProperty("dataUltimaVisita")){
                        dadosParametros.put("DT_ULT_VISITA", objeto.getProperty("dataUltimaVisita").toString());
                    }
                    if (objeto.hasProperty("dataUltimoEnvio")){
                        dadosParametros.put("DT_ULT_ENVIO", objeto.getProperty("dataUltimoEnvio").toString());
                    }
                    if (objeto.hasProperty("dataUltimoRecebimento")){
                        dadosParametros.put("DT_ULT_RECEBTO", objeto.getProperty("dataUltimoRecebimento").toString());
                    }
                    if (objeto.hasProperty("dataProximoContato")){
                        dadosParametros.put("DT_PROXIMO_CONTATO", objeto.getProperty("dataProximoContato").toString());
                    }
                    if (objeto.hasProperty("atacadoVarejo")){
                        dadosParametros.put("ATACADO_VEREJO", objeto.getProperty("atacadoVarejo").toString());
                    }
                    if (objeto.hasProperty("vistaPrazo")){
                        dadosParametros.put("VISTA_PRAZO", objeto.getProperty("vistaPrazo").toString());
                    }
                    if (objeto.hasProperty("faturaValorMinimo")){
                        dadosParametros.put("FATURA_VL_MIN", objeto.getProperty("faturaValorMinimo").toString());
                    }
                    if (objeto.hasProperty("parcelaEmAberto")){
                        dadosParametros.put("PARCELA_EM_ABERTO", objeto.getProperty("parcelaEmAberto").toString());
                    }
                    if (objeto.hasProperty("limite")){
                        dadosParametros.put("LIMITE", Double.parseDouble(objeto.getProperty("limite").toString()));
                    }
                    if (objeto.hasProperty("descontoAtacadoVista")){
                        dadosParametros.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    }
                    if (objeto.hasProperty("descontoAtacadoPrazo")){
                        dadosParametros.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    }
                    if (objeto.hasProperty("descontoVarejoVista")){
                        dadosParametros.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    }
                    if (objeto.hasProperty("descontoVarejoPrazo")){
                        dadosParametros.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    }
                    if (objeto.hasProperty("jurosDiario")){
                        dadosParametros.put("JUROS_DIARIO", Double.parseDouble(objeto.getProperty("jurosDiario").toString()));
                    }
                    listaParametros.add(dadosParametros);

                    //ParametrosSql parametrosSql = new ParametrosSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = parametrosSql.construirSqlStatement(dadosParametros);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = parametrosSql.argumentoStatement(dadosParametros);

                    *//*if (parametrosSql.insertOrReplace(dadosParametros) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //parametrosSql.insertOrReplace(dadosParametros);
                            parametrosSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ParametrosSql parametrosSql = new ParametrosSql(context);

                todosSucesso = parametrosSql.insertList(listaParametros);

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
            }*/
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
                //JsonArray parametros = new JsonArray();
                JsonObject objectparametros = new JsonObject();

                Gson gson = new Gson();
                if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                    objectparametros.addProperty("ultimaData", ultimaData);
                    //parametros.add(gson.toJsonTree(ultimaData));
                }
                WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
                JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_CFAFOTOS, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

                if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                    statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                    // Verifica se retornou com sucesso
                    if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                        final JsonArray listaFotosRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                        boolean todosSucesso = true;

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                        mLoad.progress().value(0, listaFotosRetorno.size(), false).build();

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
                            // Atualiza a notificacao
                            mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fotos));
                            mLoad.progress().value(0, 0, true).build();

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

                                if (fotosRetorno.has("fotos")) {

                                    dadosFotos.put("ID_CFAFOTOS", fotosRetorno.get("").getAsInt());
                                    dadosFotos.put("DT_ALT", fotosRetorno.get("").getAsString());
                                    if (fotosRetorno.has("idClifo") && fotosRetorno.get("idClifo").getAsInt() > 0) {
                                        dadosFotos.put("ID_CFACLIFO", fotosRetorno.get("idClifo").getAsInt());
                                    }
                                    if (fotosRetorno.has("idProduto") && fotosRetorno.get("idProduto").getAsInt() > 0) {
                                        dadosFotos.put("ID_AEAPRODU", fotosRetorno.get("idProduto").getAsInt());
                                    }
                                    if (fotosRetorno.has("fotos")) {
                                        dadosFotos.put("FOTO", Base64.decode(fotosRetorno.get("fotos").getAsString(), Base64.DEFAULT));
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
                /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                final Vector<SoapObject> listaFotosObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAFOTOS, criaPropriedadeDataAlteracaoWebservice("CFAFOTOS"));

                // Checa se retornou alguma coisa
                if ((listaFotosObject != null) && (listaFotosObject.size() > 0)) {
                    // Vareavel para saber se todos os dados foram inseridos com sucesso
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaFotosObject.size(), false).build();

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
                                progressBarStatus.setMax(listaFotosObject.size());
                            }
                        });
                    }
                    int controle = 0;

                    List<ContentValues> listaImagem = new ArrayList<ContentValues>();

                    // Passa por toda a lista
                    for (SoapObject objetoIndividual : listaFotosObject) {
                        final int finalControle = controle;

                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fotos) + " - " + (finalControle + 1) + "/" + listaFotosObject.size());
                        mLoad.progress().update(0, controle, listaFotosObject.size(), false).build();

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fotos) + " - " + (finalControle + 1) + "/" + listaFotosObject.size());
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

                        if (objeto.hasProperty("fotos")) {

                            ContentValues dadosFotos = new ContentValues();
                            dadosFotos.put("ID_CFAFOTOS", Integer.parseInt(objeto.getProperty("idFotos").toString()));
                            dadosFotos.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                            if (objeto.hasProperty("idClifo")) {
                                dadosFotos.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idClifo").toString()));
                            }
                            if (objeto.hasProperty("idProduto")) {
                                dadosFotos.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                            }
                            if (objeto.hasProperty("fotos")) {
                                dadosFotos.put("FOTO", Base64.decode(objeto.getProperty("fotos").toString(), Base64.DEFAULT));
                            }
                            listaImagem.add(dadosFotos);

                            //FotosSql fotosSql = new FotosSql(context);

                            // Pega o sql para passar para o statement
                            //final String sql = fotosSql.construirSqlStatement(dadosFotos);
                            // Pega o argumento para o statement
                            //final String[] argumentoSql = fotosSql.argumentoStatement(dadosFotos);

                            *//*if (fotosSql.insertOrReplace(dadosFotos) <= 0) {
                                todosSucesso = false;
                            }*//*
                        }
                        *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //fotosSql.insertOrReplace(dadosFotos);
                            fotosSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                    } // Fim do for
                    if ((listaFotosObject != null) && (listaFotosObject.size() > 0)){
                        listaFotosObject.clear();
                    }
                    FotosSql fotosSql = new FotosSql(context);

                    todosSucesso = fotosSql.insertList(listaImagem);

                    // Checa se todos foram inseridos/atualizados com sucesso
                    if (todosSucesso) {
                        inserirUltimaAtualizacao("CFAFOTOS");
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
                }*/
            }
        }catch (Exception e){
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPLPGT");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPLPGT, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaPlanoPagamentoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaPlanoPagamentoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_plano_pagamento));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosPagamento.put("ID_AEAPLPGT", pagamentoRetorno.get("idPlanoPagamento").getAsInt());
                            dadosPagamento.put("ID_SMAEMPRE", pagamentoRetorno.get("idEmpresa").getAsInt());
                            dadosPagamento.put("DT_ALT", pagamentoRetorno.get("dataAlteracao").getAsString());
                            dadosPagamento.put("CODIGO", pagamentoRetorno.get("codigoPlanoPagamento").getAsInt());
                            dadosPagamento.put("DESCRICAO", pagamentoRetorno.get("descricaoPlanoPagamento").getAsString());
                            dadosPagamento.put("ATIVO", pagamentoRetorno.get("ativo").getAsString());
                            if (pagamentoRetorno.has("origemValor")) {
                                dadosPagamento.put("ORIGEM_VALOR", pagamentoRetorno.get("origemValor").getAsString());
                            }
                            dadosPagamento.put("ATAC_VAREJO", pagamentoRetorno.get("atacadoVarejo").getAsString());
                            dadosPagamento.put("VISTA_PRAZO", pagamentoRetorno.get("vistaPrazo").getAsString());
                            dadosPagamento.put("PERC_DESC_ATAC", pagamentoRetorno.get("descontoAtacado").getAsDouble());
                            dadosPagamento.put("PERC_DESC_VARE", pagamentoRetorno.get("descontoVarejo").getAsDouble());
                            if (pagamentoRetorno.has("descontoPromocao")) {
                                dadosPagamento.put("DESC_PROMOCAO", pagamentoRetorno.get("descontoPromocao").getAsString());
                            }
                            if (pagamentoRetorno.has("jurosAtacado")) {
                                dadosPagamento.put("JURO_MEDIO_ATAC", pagamentoRetorno.get("jurosAtacado").getAsDouble());
                            }
                            dadosPagamento.put("JURO_MEDIO_VARE", pagamentoRetorno.get("jurosVarejo").getAsDouble());
                            dadosPagamento.put("DIAS_MEDIOS", pagamentoRetorno.get("diasMedios").getAsInt());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPlanoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPLPGT, criaPropriedadeDataAlteracaoWebservice("AEAPLPGT"));

            // Checa se retornou alguma coisa
            if ((listaPlanoObject != null) && (listaPlanoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaPlanoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaPlanoObject.size());
                        }
                    });
                }
                int controle = 0;
                List<ContentValues> listaPlanoPagamento = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPlanoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - " + (finalControle + 1) + "/" + listaPlanoObject.size());
                    mLoad.progress().update(0, controle, listaPlanoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_plano_pagamento) + " - " + (finalControle + 1) + "/" + listaPlanoObject.size());
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

                    ContentValues dadosPagamento = new ContentValues();
                    dadosPagamento.put("ID_AEAPLPGT", Integer.parseInt(objeto.getProperty("idPlanoPagamento").toString()));
                    dadosPagamento.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosPagamento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosPagamento.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoPlanoPagamento").toString()));
                    dadosPagamento.put("DESCRICAO", objeto.getProperty("descricaoPlanoPagamento").toString());
                    dadosPagamento.put("ATIVO", objeto.getProperty("ativo").toString());
                    dadosPagamento.put("ATAC_VAREJO", objeto.getProperty("atacadoVarejo").toString());
                    dadosPagamento.put("VISTA_PRAZO", objeto.getProperty("vistaPrazo").toString());
                    dadosPagamento.put("PERC_DESC_ATAC", objeto.getProperty("descontoAtacado").toString());
                    dadosPagamento.put("PERC_DESC_VARE", objeto.getProperty("descontoVarejo").toString());
                    if (objeto.hasProperty("descontoPromocao")) {
                        dadosPagamento.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    dadosPagamento.put("JURO_MEDIO_ATAC", Double.parseDouble(objeto.getProperty("jurosAtacado").toString()));
                    dadosPagamento.put("JURO_MEDIO_VARE", Double.parseDouble(objeto.getProperty("jurosVarejo").toString()));
                    dadosPagamento.put("DIAS_MEDIOS", Integer.parseInt(objeto.getProperty("diasMedios").toString()));
                    listaPlanoPagamento.add(dadosPagamento);

                    //PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = planoPagamentoSql.construirSqlStatement(dadosPagamento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = planoPagamentoSql.argumentoStatement(dadosPagamento);

                    *//*if (planoPagamentoSql.insertOrReplace(dadosPagamento) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //planoPagamentoSql.insertOrReplace(dadosPagamento);
                            planoPagamentoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                todosSucesso = planoPagamentoSql.insertList(listaPlanoPagamento);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEACLASE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaClasseRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaClasseRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_classe_produto));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosClasse.put("ID_AEACLASE", classeRetorno.get("idClasse").getAsInt());
                            dadosClasse.put("CODIGO", classeRetorno.get("codigoClasse").getAsInt());
                            dadosClasse.put("DT_ALT", classeRetorno.get("dataAlteracao").getAsString());
                            dadosClasse.put("DESCRICAO", classeRetorno.get("descricaoClasse").getAsString());

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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaClasseObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEACLASE, criaPropriedadeDataAlteracaoWebservice("AEACLASE"));

            // Checa se retornou alguma coisa
            if ((listaClasseObject != null) && (listaClasseObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaClasseObject.size(), false).build();

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
                            progressBarStatus.setMax(listaClasseObject.size());
                        }
                    });
                }
                int controle = 0;
                List<ContentValues> listaClasseProduto = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaClasseObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - " + (finalControle + 1) + "/" + listaClasseObject.size());
                    mLoad.progress().update(0, controle, listaClasseObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_classe_produto) + " - " + (finalControle + 1) + "/" + listaClasseObject.size());
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

                    ContentValues dadosClasse = new ContentValues();
                    dadosClasse.put("ID_AEACLASE", Integer.parseInt(objeto.getProperty("idClasse").toString()));
                    dadosClasse.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoClasse").toString()));
                    dadosClasse.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosClasse.put("DESCRICAO", objeto.getProperty("descricaoClasse").toString());
                    listaClasseProduto.add(dadosClasse);

                    //ClasseSql classeSql = new ClasseSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = classeSql.construirSqlStatement(dadosClasse);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = classeSql.argumentoStatement(dadosClasse);

                    *//*if (classeSql.insertOrReplace(dadosClasse) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //classeSql.insertOrReplace(dadosClasse);
                            classeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ClasseSql classeSql = new ClasseSql(context);

                todosSucesso = classeSql.insertList(listaClasseProduto);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAUNVEN, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaUnidadeVendaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaUnidadeVendaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_unidade_venda));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosUnidade.put("ID_AEAUNVEN", unidadeRetorno.get("idUnidadeVenda").getAsInt());
                            dadosUnidade.put("DT_ALT", unidadeRetorno.get("dataAlteracao").getAsString());
                            dadosUnidade.put("DESCRICAO_SINGULAR", unidadeRetorno.get("descricaoUnidadeVenda").getAsString());
                            dadosUnidade.put("SIGLA", unidadeRetorno.get("siglaUnidadeVenda").getAsString());
                            dadosUnidade.put("DECIMAIS", unidadeRetorno.get("casasDecimais").getAsInt());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaUnidadeVendaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAUNVEN, criaPropriedadeDataAlteracaoWebservice("AEAUNVEN"));

            // Checa se retornou alguma coisa
            if ((listaUnidadeVendaObject != null) && (listaUnidadeVendaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaUnidadeVendaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaUnidadeVendaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaUnidadeVenda = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaUnidadeVendaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - " + (finalControle + 1) + "/" + listaUnidadeVendaObject.size());
                    mLoad.progress().update(0, controle, listaUnidadeVendaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_unidade_venda) + " - " + (finalControle + 1) + "/" + listaUnidadeVendaObject.size());
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

                    ContentValues dadosUnidade = new ContentValues();
                    dadosUnidade.put("ID_AEAUNVEN", Integer.parseInt(objeto.getProperty("idUnidadeVenda").toString()));
                    dadosUnidade.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosUnidade.put("DESCRICAO_SINGULAR", objeto.getProperty("descricaoUnidadeVenda").toString());
                    dadosUnidade.put("SIGLA", objeto.getProperty("siglaUnidadeVenda").toString());
                    dadosUnidade.put("DECIMAIS", Integer.parseInt(objeto.getProperty("casasDecimais").toString()));
                    listaUnidadeVenda.add(dadosUnidade);

                    //UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = unidadeVendaSql.construirSqlStatement(dadosUnidade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = unidadeVendaSql.argumentoStatement(dadosUnidade);

                    *//*if (unidadeVendaSql.insertOrReplace(dadosUnidade) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //unidadeVendaSql.insertOrReplace(dadosUnidade);
                            unidadeVendaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);

                todosSucesso = unidadeVendaSql.insertList(listaUnidadeVenda);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAGRADE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaGradeRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaGradeRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_grade));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosGrade.put("ID_AEAGRADE", gradeRetorno.get("idGrade").getAsInt());
                            dadosGrade.put("ID_AEATPGRD", gradeRetorno.get("idTipoGrade").getAsInt());
                            dadosGrade.put("DT_ALT", gradeRetorno.get("dataAlteracao").getAsString());
                            dadosGrade.put("DESCRICAO", gradeRetorno.get("descricaoGrade").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaGradeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAGRADE, criaPropriedadeDataAlteracaoWebservice("AEAGRADE"));

            // Checa se retornou alguma coisa
            if ((listaGradeObject != null) && (listaGradeObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaGradeObject.size(), false).build();

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
                            progressBarStatus.setMax(listaGradeObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaGrade = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaGradeObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_grade) + " - " + (finalControle + 1) + "/" + listaGradeObject.size());
                    mLoad.progress().update(0, controle, listaGradeObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_grade) + " - " + (finalControle + 1) + "/" + listaGradeObject.size());
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

                    ContentValues dadosGrade = new ContentValues();
                    dadosGrade.put("ID_AEAGRADE", Integer.parseInt(objeto.getProperty("idGrade").toString()));
                    dadosGrade.put("ID_AEATPGRD", Integer.parseInt(objeto.getProperty("idTipoGrade").toString()));
                    dadosGrade.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosGrade.put("DESCRICAO", objeto.getProperty("descricaoGrade").toString());
                    listaGrade.add(dadosGrade);

                    //GradeSql gradeSql = new GradeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = gradeSql.construirSqlStatement(dadosGrade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = gradeSql.argumentoStatement(dadosGrade);

                    *//*if (gradeSql.insertOrReplace(dadosGrade) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //gradeSql.insertOrReplace(dadosGrade);
                            gradeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                GradeSql gradeSql = new GradeSql(context);

                todosSucesso = gradeSql.insertList(listaGrade);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAMARCA, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaMarcaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaMarcaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_marca));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosMarca.put("ID_AEAMARCA", marcaRetorno.get("idMarca").getAsInt());
                            dadosMarca.put("DT_ALT", marcaRetorno.get("dataAlteracao").getAsString());
                            dadosMarca.put("DESCRICAO", marcaRetorno.get("descricaoMarca").getAsString());

                            listaDadosMarca.add(dadosMarca);
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaMarcaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAMARCA, criaPropriedadeDataAlteracaoWebservice("AEAMARCA"));

            // Checa se retornou alguma coisa
            if ((listaMarcaObject != null) && (listaMarcaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaMarcaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaMarcaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaMarca = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaMarcaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_marca) + " - " + (finalControle + 1) + "/" + listaMarcaObject.size());
                    mLoad.progress().update(0, controle, listaMarcaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_marca) + " - " + (finalControle + 1) + "/" + listaMarcaObject.size());
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

                    ContentValues dadosMarca = new ContentValues();
                    dadosMarca.put("ID_AEAMARCA", Integer.parseInt(objeto.getProperty("idMarca").toString()));
                    dadosMarca.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosMarca.put("DESCRICAO", objeto.getProperty("descricaoMarca").toString());

                    listaMarca.add(dadosMarca);

                    //MarcaSql marcaSql = new MarcaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = marcaSql.construirSqlStatement(dadosMarca);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = marcaSql.argumentoStatement(dadosMarca);

                    *//*if (marcaSql.insertOrReplace(dadosMarca) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //marcaSql.insertOrReplace(dadosMarca);
                            marcaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                MarcaSql marcaSql = new MarcaSql(context);

                todosSucesso = marcaSql.insertList(listaMarca);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEACODST, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaTributariaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaTributariaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosSituacao.put("ID_AEACODST", situacaoRetorno.get("idSituacaoTributaria").getAsInt());
                            dadosSituacao.put("DT_ALT", situacaoRetorno.get("dataAlteracao").getAsString());
                            dadosSituacao.put("CODIGO", situacaoRetorno.get("codigoSituacaoTributaria").getAsInt());
                            dadosSituacao.put("DESCRICAO", situacaoRetorno.get("descricaoSituacaoTributaria").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaSituacaoTributariaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEACODST, criaPropriedadeDataAlteracaoWebservice("AEACODST"));

            // Checa se retornou alguma coisa
            if ((listaSituacaoTributariaObject != null) && (listaSituacaoTributariaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaSituacaoTributariaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaSituacaoTributariaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaSituacaoTributaria = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaSituacaoTributariaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - " + (finalControle + 1) + "/" + listaSituacaoTributariaObject.size());
                    mLoad.progress().update(0, controle, listaSituacaoTributariaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_situacao_tributaria) + " - " + (finalControle + 1) + "/" + listaSituacaoTributariaObject.size());
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

                    ContentValues dadosSituacao = new ContentValues();
                    dadosSituacao.put("ID_AEACODST", Integer.parseInt(objeto.getProperty("idSituacaoTributaria").toString()));
                    dadosSituacao.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosSituacao.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoSituacaoTributaria").toString()));
                    dadosSituacao.put("DESCRICAO", objeto.getProperty("descricaoSituacaoTributaria").toString());
                    dadosSituacao.put("TIPO", objeto.getProperty("tipo").toString());
                    dadosSituacao.put("ORIGEM", objeto.getProperty("origem").toString());

                    listaSituacaoTributaria.add(dadosSituacao);

                    //SituacaoTributariaSql situacaoTributariaSql = new SituacaoTributariaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = situacaoTributariaSql.construirSqlStatement(dadosSituacao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = situacaoTributariaSql.argumentoStatement(dadosSituacao);

                    *//*if (situacaoTributariaSql.insertOrReplace(dadosSituacao) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //situacaoTributariaSql.insertOrReplace(dadosSituacao);
                            situacaoTributariaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                SituacaoTributariaSql situacaoTributariaSql = new SituacaoTributariaSql(context);

                todosSucesso = situacaoTributariaSql.insertList(listaSituacaoTributaria);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPRODU");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPRODU, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaProdutoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaProdutoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosProduto.put("ID_AEAPRODU", produtoRetorno.get("idProduto").getAsInt());
                            if (produtoRetorno.has("idClasse") && produtoRetorno.get("idClasse").getAsInt() > 0) {
                                dadosProduto.put("ID_AEACLASE", produtoRetorno.get("idClasse").getAsInt());
                            }
                            if (produtoRetorno.has("idMarca") && produtoRetorno.get("idMarca").getAsInt() > 0) {
                                dadosProduto.put("ID_AEAMARCA", produtoRetorno.get("idMarca").getAsInt());
                            }
                            if (produtoRetorno.has("unidadeVendaProduto")){
                                JsonObject unidade = produtoRetorno.getAsJsonObject("unidadeVendaProduto");
                                //SoapObject unidade = (SoapObject) objeto.getProperty("unidadeVendaProduto");
                                dadosProduto.put("ID_AEAUNVEN", unidade.get("idUnidadeVenda").getAsInt());
                            }
                            dadosProduto.put("DT_CAD",produtoRetorno.get("dataCadastro").getAsString());
                            dadosProduto.put("DT_ALT",produtoRetorno.get("dataAlteracao").getAsString());
                            dadosProduto.put("DESCRICAO",produtoRetorno.get("descricaoProduto").getAsString());
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
                            dadosProduto.put("TIPO", produtoRetorno.get("tipoProduto").getAsString());

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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPRODU, criaPropriedadeDataAlteracaoWebservice("AEAPRODU"));

            // Checa se retornou alguma coisa
            if ((listaProdutoObject != null) && (listaProdutoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaProdutoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaProdutoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaProduto = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto) + " - " + (finalControle + 1) + "/" + listaProdutoObject.size());
                    mLoad.progress().update(0, controle, listaProdutoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto) + " - " + (finalControle + 1) + "/" + listaProdutoObject.size());
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

                    ContentValues dadosProduto = new ContentValues();
                    dadosProduto.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                    if (objeto.hasProperty("idClasse")) {
                        dadosProduto.put("ID_AEACLASE", Integer.parseInt(objeto.getProperty("idClasse").toString()));
                    }
                    if (objeto.hasProperty("idMarca")) {
                        dadosProduto.put("ID_AEAMARCA", Integer.parseInt(objeto.getProperty("idMarca").toString()));
                    }
                    if (objeto.hasProperty("unidadeVendaProduto")){
                        SoapObject unidade = (SoapObject) objeto.getProperty("unidadeVendaProduto");
                        dadosProduto.put("ID_AEAUNVEN", Integer.parseInt(unidade.getProperty("idUnidadeVenda").toString()));
                    }
                    dadosProduto.put("DT_CAD", objeto.getProperty("dataCadastro").toString());
                    dadosProduto.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosProduto.put("DESCRICAO", objeto.getProperty("descricaoProduto").toString());
                    if (objeto.hasProperty("descricaoAuxiliar")) {
                        dadosProduto.put("DESCRICAO_AUXILIAR", objeto.getProperty("descricaoAuxiliar").toString());
                    }
                    dadosProduto.put("CODIGO_ESTRUTURAL", objeto.getProperty("codigoEstrutural").toString());
                    if (objeto.hasProperty("referencia")) {
                        dadosProduto.put("REFERENCIA", objeto.getProperty("referencia").toString());
                    }
                    if (objeto.hasProperty("codigoBarras")) {
                        dadosProduto.put("CODIGO_BARRAS", objeto.getProperty("codigoBarras").toString());
                    }
                    if (objeto.hasProperty("pesoLiquido")) {
                        dadosProduto.put("PESO_LIQUIDO", Double.parseDouble(objeto.getProperty("pesoLiquido").toString()));
                    }
                    if (objeto.hasProperty("pesoBruto")) {
                        dadosProduto.put("PESO_BRUTO", Double.parseDouble(objeto.getProperty("pesoBruto").toString()));
                    }
                    dadosProduto.put("ATIVO", objeto.getProperty("ativo").toString());
                    dadosProduto.put("TIPO", objeto.getProperty("tipoProduto").toString());

                    listaProduto.add(dadosProduto);

                    //ProdutoSql produtoSql = new ProdutoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoSql.construirSqlStatement(dadosProduto);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoSql.argumentoStatement(dadosProduto);

                    *//*if (produtoSql.insertOrReplace(dadosProduto) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoSql.insertOrReplace(dadosProduto);
                            produtoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ProdutoSql produtoSql = new ProdutoSql(context);

                todosSucesso = produtoSql.insertList(listaProduto);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPRECO, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaPrecoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaPrecoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_preco));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosPreco.put("ID_AEAPRECO", precoRetorno.get("idPreco").getAsInt());
                            dadosPreco.put("ID_AEAPRODU", precoRetorno.get("idProduto").getAsInt());
                            if (precoRetorno.has("idClifo") && precoRetorno.get("idClifo").getAsInt() > 0) {
                                dadosPreco.put("ID_CFACLIFO", precoRetorno.get("idClifo").getAsInt());
                            }
                            if (precoRetorno.has("idPlanoPagamento") && precoRetorno.get("idPlanoPagamento").getAsInt() > 0) {
                                dadosPreco.put("ID_AEAPLPGT", precoRetorno.get("idPlanoPagamento").getAsInt());
                            }
                            if (precoRetorno.has("dataAlteracao")){
                                dadosPreco.put("DT_ALT", precoRetorno.get("dataAlteracao").getAsString());
                            }
                            dadosPreco.put("VENDA_ATAC",precoRetorno.get("vendaAtacado").getAsString());
                            dadosPreco.put("VENDA_VARE",precoRetorno.get("vendaVarejo").getAsString());

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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAEMBAL, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaEmbalagemRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaEmbalagemRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_embalagem));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosEmbalagem.put("ID_AEAEMBAL", embalagemRetorno.get("idEmbalagem").getAsInt());
                            if (embalagemRetorno.has("idProduto") && embalagemRetorno.get("idProduto").getAsInt() > 0) {
                                dadosEmbalagem.put("ID_AEAPRODU", embalagemRetorno.get("idProduto").getAsInt());
                            }
                            if (embalagemRetorno.has("idUnidadeVenda") && embalagemRetorno.get("idUnidadeVenda").getAsInt() > 0) {
                                dadosEmbalagem.put("ID_AEAUNVEN", embalagemRetorno.get("idUnidadeVenda").getAsInt());
                            }
                            dadosEmbalagem.put("DT_ALT", embalagemRetorno.get("dataAlteracao").getAsString());
                            if (embalagemRetorno.has("principal")) {
                                dadosEmbalagem.put("PRINCIPAL", embalagemRetorno.get("principal").getAsString());
                            }
                            if (embalagemRetorno.has("descricaoEmbalagem")) {
                                dadosEmbalagem.put("DESCRICAO", embalagemRetorno.get("descricaoEmbalagem").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEmbalagemObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAEMBAL, criaPropriedadeDataAlteracaoWebservice("AEAEMBAL"));

            // Checa se retornou alguma coisa
            if ((listaEmbalagemObject != null) && (listaEmbalagemObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaEmbalagemObject.size(), false).build();

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
                            progressBarStatus.setMax(listaEmbalagemObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaEmbalagem = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEmbalagemObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - " + (finalControle + 1) + "/" + listaEmbalagemObject.size());
                    mLoad.progress().update(0, controle, listaEmbalagemObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_embalagem) + " - " + (finalControle + 1) + "/" + listaEmbalagemObject.size());
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

                    ContentValues dadosEmbalagem = new ContentValues();
                    dadosEmbalagem.put("ID_AEAEMBAL", Integer.parseInt(objeto.getProperty("idEmbalagem").toString()));
                    if (objeto.hasProperty("idProduto")) {
                        dadosEmbalagem.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                    }
                    if (objeto.hasProperty("idUnidadeVenda")) {
                        dadosEmbalagem.put("ID_AEAUNVEN", Integer.parseInt(objeto.getProperty("idUnidadeVenda").toString()));
                    }
                    dadosEmbalagem.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    if (objeto.hasProperty("principal")) {
                        dadosEmbalagem.put("PRINCIPAL", objeto.getProperty("principal").toString());
                    }
                    if (objeto.hasProperty("descricaoEmbalagem")) {
                        dadosEmbalagem.put("DESCRICAO", objeto.getProperty("descricaoEmbalagem").toString());
                    }
                    if (objeto.hasProperty("fatorConversao")) {
                        dadosEmbalagem.put("FATOR_CONVERSAO", Double.parseDouble(objeto.getProperty("fatorConversao").toString()));
                    }
                    if (objeto.hasProperty("fatorPreco")) {
                        dadosEmbalagem.put("FATOR_PRECO", Double.parseDouble(objeto.getProperty("fatorPreco").toString()));
                    }
                    if (objeto.hasProperty("modulo")) {
                        dadosEmbalagem.put("MODULO", Integer.parseInt(objeto.getProperty("modulo").toString()));
                    }
                    if (objeto.hasProperty("decimais")) {
                        dadosEmbalagem.put("DECIMAIS", Integer.parseInt(objeto.getProperty("decimais").toString()));
                    }
                    if (objeto.hasProperty("ativo")) {
                        dadosEmbalagem.put("ATIVO", objeto.getProperty("ativo").toString());
                    }
                    listaEmbalagem.add(dadosEmbalagem);

                    //EmbalagemSql embalagemSql = new EmbalagemSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = embalagemSql.construirSqlStatement(dadosEmbalagem);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = embalagemSql.argumentoStatement(dadosEmbalagem);

                    *//*if (embalagemSql.insertOrReplace(dadosEmbalagem) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //embalagemSql.insertOrReplace(dadosEmbalagem);
                            embalagemSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                EmbalagemSql embalagemSql = new EmbalagemSql(context);

                todosSucesso = embalagemSql.insertList(listaEmbalagem);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAPLOJA");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPLOJA, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaProdutoLojaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaProdutoLojaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosProdutoLoja.put("ID_AEAPLOJA", produtoLojaRetorno.get("idProdutoLoja").getAsInt());
                            dadosProdutoLoja.put("ID_SMAEMPRE", produtoLojaRetorno.get("idEmpresa").getAsInt());
                            dadosProdutoLoja.put("ID_AEAPRODU", produtoLojaRetorno.get("idProduto").getAsInt());
                            dadosProdutoLoja.put("ID_AEACODST", produtoLojaRetorno.get("idSituacaoTributaria").getAsInt());
                            dadosProdutoLoja.put("DT_ALT", produtoLojaRetorno.get("dataAlteracao").getAsString());
                            dadosProdutoLoja.put("ESTOQUE_F", produtoLojaRetorno.get("estoqueFisico").getAsDouble());
                            dadosProdutoLoja.put("ESTOQUE_C", produtoLojaRetorno.get("estoqueContabil").getAsDouble());
                            if (produtoLojaRetorno.has("retido")) {
                                dadosProdutoLoja.put("RETIDO", produtoLojaRetorno.get("retido").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("pedido")) {
                                dadosProdutoLoja.put("PEDIDO", produtoLojaRetorno.get("pedido").getAsDouble());
                            }
                            dadosProdutoLoja.put("ATIVO", produtoLojaRetorno.get("ativo").getAsString());
                            if (produtoLojaRetorno.has("dataEntradaDireta")) {
                                dadosProdutoLoja.put("DT_ENTRADA_D", produtoLojaRetorno.get("dataEntradaDireta").getAsString());
                            }
                            if (produtoLojaRetorno.has("dataEntradaNota")) {
                                dadosProdutoLoja.put("DT_ENTRADA_N", produtoLojaRetorno.get("dataEntradaNota").getAsString());
                            }
                            if (produtoLojaRetorno.has("custoReposicaoNota")) {
                                dadosProdutoLoja.put("CT_REPOSICAO_N", produtoLojaRetorno.get("cutoReposicaoNota").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("custoCompletoNota")) {
                                dadosProdutoLoja.put("CT_COMPLETO_N", produtoLojaRetorno.get("custoCompletoNota").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("custoRealNota")) {
                                dadosProdutoLoja.put("CT_REAL_N", produtoLojaRetorno.get("custoRealNota").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("custoMedioNota")) {
                                dadosProdutoLoja.put("CT_MEDIO_N", produtoLojaRetorno.get("custoMedioNota").getAsDouble());
                            }
                            dadosProdutoLoja.put("VENDA_ATAC", produtoLojaRetorno.get("vendaAtacado").getAsDouble());
                            dadosProdutoLoja.put("VENDA_VARE", produtoLojaRetorno.get("vendaVarejo").getAsDouble());
                            if (produtoLojaRetorno.has("promocaoAtacadoVista")) {
                                dadosProdutoLoja.put("PROMOCAO_ATAC_VISTA", produtoLojaRetorno.get("promocaoAtacadoVIsta").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("promocaoAtacadoPrazo")) {
                                dadosProdutoLoja.put("PROMOCAO_ATAC_PRAZO", produtoLojaRetorno.get("promocaoAtacadoPrazo").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("promocaoVarejoVista")) {
                                dadosProdutoLoja.put("PROMOCAO_VARE_VISTA", produtoLojaRetorno.get("promocaoVarejoVista").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("promocaoVarejoPrazo")) {
                                dadosProdutoLoja.put("PROMOCAO_VARE_PRAZO", produtoLojaRetorno.get("promocaoVarejoPrazo").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("precoMinimoAtacado")) {
                                dadosProdutoLoja.put("PRECO_MINIMO_ATAC", produtoLojaRetorno.get("precoMinimoAtacado").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("precoMinimoVarejo")) {
                                dadosProdutoLoja.put("PRECO_MINIMO_VARE", produtoLojaRetorno.get("precoMinimoVarejo").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("precoMaximoAtacado")) {
                                dadosProdutoLoja.put("PRECO_MAXIMO_ATAC", produtoLojaRetorno.get("precoMaximoAtacado").getAsDouble());
                            }
                            if (produtoLojaRetorno.has("precoMaximoVarejo")) {
                                dadosProdutoLoja.put("PRECO_MAXIMO_VARE", produtoLojaRetorno.get("precoMaximoVarejo").getAsDouble());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoLojaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPLOJA, criaPropriedadeDataAlteracaoWebservice("AEAPLOJA"));

            // Checa se retornou alguma coisa
            if ((listaProdutoLojaObject != null) && (listaProdutoLojaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaProdutoLojaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaProdutoLojaObject.size());
                        }
                    });
                }
                int controle = 0;
                List<ContentValues> listaProdutoLoja = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoLojaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - " + (finalControle + 1) + "/" + listaProdutoLojaObject.size());
                    mLoad.progress().update(0, controle, listaProdutoLojaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_produto_loja) + " - " + (finalControle + 1) + "/" + listaProdutoLojaObject.size());
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

                    ContentValues dadosProdutoLoja = new ContentValues();
                    dadosProdutoLoja.put("ID_AEAPLOJA", Integer.parseInt(objeto.getProperty("idProdutoLoja").toString()));
                    dadosProdutoLoja.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosProdutoLoja.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                    dadosProdutoLoja.put("ID_AEACODST", Integer.parseInt(objeto.getProperty("idSituacaoTributaria").toString()));
                    dadosProdutoLoja.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosProdutoLoja.put("ESTOQUE_F", Double.parseDouble(objeto.getProperty("estoqueFisico").toString()));
                    dadosProdutoLoja.put("ESTOQUE_C", Double.parseDouble(objeto.getProperty("estoqueContabil").toString()));
                    if (objeto.hasProperty("retido")) {
                        dadosProdutoLoja.put("RETIDO", Double.parseDouble(objeto.getProperty("retido").toString()));
                    }
                    if (objeto.hasProperty("pedido")) {
                        dadosProdutoLoja.put("PEDIDO", Double.parseDouble(objeto.getProperty("pedido").toString()));
                    }
                    dadosProdutoLoja.put("ATIVO", objeto.getProperty("ativo").toString());
                    if (objeto.hasProperty("dataEntradaDireta")) {
                        dadosProdutoLoja.put("DT_ENTRADA_D", objeto.getProperty("dataEntradaDireta").toString());
                    }
                    if (objeto.hasProperty("dataEntradaNota")) {
                        dadosProdutoLoja.put("DT_ENTRADA_N", objeto.getProperty("dataEntradaNota").toString());
                    }
                    if (objeto.hasProperty("custoReposicaoNota")) {
                        dadosProdutoLoja.put("CT_REPOSICAO_N", Double.parseDouble(objeto.getProperty("cutoReposicaoNota").toString()));
                    }
                    if (objeto.hasProperty("custoCompletoNota")) {
                        dadosProdutoLoja.put("CT_COMPLETO_N", Double.parseDouble(objeto.getProperty("custoCompletoNota").toString()));
                    }
                    dadosProdutoLoja.put("VENDA_ATAC", Double.parseDouble(objeto.getProperty("vendaAtacado").toString()));
                    dadosProdutoLoja.put("VENDA_VARE", Double.parseDouble(objeto.getProperty("vendaVarejo").toString()));
                    if (objeto.hasProperty("promocaoAtacado")) {
                        dadosProdutoLoja.put("PROMOCAO_ATAC", Double.parseDouble(objeto.getProperty("promocaoAtacado").toString()));
                    }
                    if (objeto.hasProperty("promocaoVarejo")) {
                        dadosProdutoLoja.put("PROMOCAO_VARE", Double.parseDouble(objeto.getProperty("promocaoVarejo").toString()));
                    }
                    if (objeto.hasProperty("precoMinimoAtacado")) {
                        dadosProdutoLoja.put("PRECO_MINIMO_ATAC", Double.parseDouble(objeto.getProperty("precoMinimoAtacado").toString()));
                    }
                    if (objeto.hasProperty("precoMinimoVarejo")) {
                        dadosProdutoLoja.put("PRECO_MINIMO_VARE", Double.parseDouble(objeto.getProperty("precoMinimoVarejo").toString()));
                    }
                    if (objeto.hasProperty("precoMaximoAtacado")) {
                        dadosProdutoLoja.put("PRECO_MAXIMO_ATAC", Double.parseDouble(objeto.getProperty("precoMaximoAtacado").toString()));
                    }
                    if (objeto.hasProperty("precoMaximoVarejo")) {
                        dadosProdutoLoja.put("PRECO_MAXIMO_VARE", Double.parseDouble(objeto.getProperty("precoMaximoVarejo").toString()));
                    }
                    listaProdutoLoja.add(dadosProdutoLoja);
                    //ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoLojaSql.construirSqlStatement(dadosProdutoLoja);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoLojaSql.argumentoStatement(dadosProdutoLoja);

                    *//*if (produtoLojaSql.insertOrReplace(dadosProdutoLoja) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoLojaSql.insertOrReplace(dadosProdutoLoja);
                            produtoLojaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                todosSucesso = produtoLojaSql.insertList(listaProdutoLoja);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEALOCES");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEALOCES, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaLocalEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaLocalEstoqueRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_local_estoque));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosLocalEstoque.put("ID_AEALOCES", localRetorno.get("idLocalEstoque").getAsInt());
                            dadosLocalEstoque.put("ID_SMAEMPRE", localRetorno.get("idEmpresa").getAsInt());
                            dadosLocalEstoque.put("DT_ALT", localRetorno.get("dataAlteracao").getAsString());
                            dadosLocalEstoque.put("CODIGO", localRetorno.get("codigoLocalEstoque").getAsInt());
                            dadosLocalEstoque.put("DESCRICAO", localRetorno.get("descricaoLocalEstoque").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaLocalEstoqueObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEALOCES, criaPropriedadeDataAlteracaoWebservice("AEALOCES"));

            // Checa se retornou alguma coisa
            if ((listaLocalEstoqueObject != null) && (listaLocalEstoqueObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaLocalEstoqueObject.size(), false).build();

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
                            progressBarStatus.setMax(listaLocalEstoqueObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaLocalEstoque = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaLocalEstoqueObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_local_estoque) + " - " + (finalControle + 1) + "/" + listaLocalEstoqueObject.size());
                    mLoad.progress().update(0, controle, listaLocalEstoqueObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estoque) + " - " + (finalControle + 1) + "/" + listaLocalEstoqueObject.size());
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

                    ContentValues dadosLocalEstoque = new ContentValues();
                    dadosLocalEstoque.put("ID_AEALOCES", Integer.parseInt(objeto.getProperty("idLocalEstoque").toString()));
                    dadosLocalEstoque.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosLocalEstoque.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosLocalEstoque.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoLocalEstoque").toString()));
                    dadosLocalEstoque.put("DESCRICAO", objeto.getProperty("descricaoLocalEstoque").toString());
                    dadosLocalEstoque.put("ATIVO", objeto.getProperty("ativo").toString());
                    dadosLocalEstoque.put("TIPO_VENDA", objeto.getProperty("tipoVenda").toString());
                    listaLocalEstoque.add(dadosLocalEstoque);

                    //LocacaoSql locacaoSql = new LocacaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = locacaoSql.construirSqlStatement(dadosLocalEstoque);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = locacaoSql.argumentoStatement(dadosLocalEstoque);

                    *//*if (locacaoSql.insertOrReplace(dadosLocalEstoque) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //locacaoSql.insertOrReplace(dadosLocalEstoque);
                            locacaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                LocacaoSql locacaoSql = new LocacaoSql(context);

                todosSucesso = locacaoSql.insertList(listaLocalEstoque);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("AEAESTOQ");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAESTOQ, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaEstoqueRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaEstoqueRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estoque));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosEstoque.put("ID_AEAESTOQ", estoqueRetorno.get("idEstoque").getAsInt());
                            dadosEstoque.put("ID_AEAPLOJA", estoqueRetorno.get("idProdutoLoja").getAsInt());
                            if (estoqueRetorno.has("idLocacao") && estoqueRetorno.get("idLocacao").getAsInt() > 0) {
                                dadosEstoque.put("ID_AEALOCES", estoqueRetorno.get("idLocacao").getAsInt());
                            }
                            dadosEstoque.put("DT_ALT", estoqueRetorno.get("dataAlteracao").getAsString());
                            dadosEstoque.put("ESTOQUE", estoqueRetorno.get("estoqueLocacao").getAsDouble());
                            dadosEstoque.put("RETIDO", estoqueRetorno.get("retidoLocacao").getAsDouble());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEstoqueObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAESTOQ, criaPropriedadeDataAlteracaoWebservice("AEAESTOQ"));

            // Checa se retornou alguma coisa
            if ((listaEstoqueObject != null) && (listaEstoqueObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaEstoqueObject.size(), false).build();

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
                            progressBarStatus.setMax(listaEstoqueObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaEstoque = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEstoqueObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_estoque) + " - " + (finalControle + 1) + "/" + listaEstoqueObject.size());
                    mLoad.progress().update(0, controle, listaEstoqueObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_estoque) + " - " + (finalControle + 1) + "/" + listaEstoqueObject.size());
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

                    ContentValues dadosEstoque = new ContentValues();
                    dadosEstoque.put("ID_AEAESTOQ", Integer.parseInt(objeto.getProperty("idEstoque").toString()));
                    dadosEstoque.put("ID_AEAPLOJA", Integer.parseInt(objeto.getProperty("idProdutoLoja").toString()));
                    if (objeto.hasProperty("idLocacao")) {
                        dadosEstoque.put("ID_AEALOCES", Integer.parseInt(objeto.getProperty("idLocacao").toString()));
                    }
                    dadosEstoque.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosEstoque.put("ESTOQUE", Double.parseDouble(objeto.getProperty("estoqueLocacao").toString()));
                    dadosEstoque.put("RETIDO", Double.parseDouble(objeto.getProperty("retidoLocacao").toString()));
                    dadosEstoque.put("ATIVO", objeto.getProperty("ativo").toString());

                    listaEstoque.add(dadosEstoque);
                    //EstoqueSql estoqueSql = new EstoqueSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = estoqueSql.construirSqlStatement(dadosEstoque);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = estoqueSql.argumentoStatement(dadosEstoque);

                    *//*if (estoqueSql.insertOrReplace(dadosEstoque) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //estoqueSql.insertOrReplace(dadosEstoque);
                            estoqueSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                EstoqueSql estoqueSql = new EstoqueSql(context);

                todosSucesso = estoqueSql.insertList(listaEstoque);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            if ((listaGuidOrcamento != null) && (listaGuidOrcamento.size() > 0)){
                //parametros.add(gson.toJsonTree(listaGuidOrcamento));
                objectparametros.add("listaGuidOrcamento", gson.toJsonTree(listaGuidOrcamento, listaGuidOrcamento.getClass()));
            }

            String sTeste = objectparametros.toString();

            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAORCAM, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);

                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    //JsonObject objectRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);

                    final JsonArray listaPedidoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaPedidoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento));
                        mLoad.progress().value(0, 0, true).build();

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
                            dadosOrcamento.put("ID_SMAEMPRE", pedidoRetorno.get("idEmpresa").getAsInt());
                            if (pedidoRetorno.has("idPessoa") && pedidoRetorno.get("idPessoa").getAsInt() > 0) {
                                dadosOrcamento.put("ID_CFACLIFO", pedidoRetorno.get("idPessoa").getAsInt());
                            }
                            if (pedidoRetorno.has("idEstado") && pedidoRetorno.get("idEstado").getAsInt() > 0) {
                                dadosOrcamento.put("ID_CFAESTAD", pedidoRetorno.get("idEstado").getAsInt());
                            }
                            if (pedidoRetorno.has("idCidade") && pedidoRetorno.get("idCidade").getAsInt() > 0) {
                                dadosOrcamento.put("ID_CFACIDAD", pedidoRetorno.get("idCidade").getAsInt());
                            }
                            if (pedidoRetorno.has("idRomaneio") && pedidoRetorno.get("idRomaneio").getAsInt() > 0) {
                                dadosOrcamento.put("ID_AEAROMAN", pedidoRetorno.get("idRomaneio").getAsInt());
                            }
                            if (pedidoRetorno.has("idTipoDocumento") && pedidoRetorno.get("idTipoDocumento").getAsInt() > 0) {
                                dadosOrcamento.put("ID_CFATPDOC", pedidoRetorno.get("idTipoDocumento").getAsInt());
                            }
                            dadosOrcamento.put("GUID", pedidoRetorno.get("guid").getAsString());
                            dadosOrcamento.put("NUMERO", pedidoRetorno.get("numero").getAsInt());
                            if (pedidoRetorno.has("totalFrete")) {
                                dadosOrcamento.put("VL_FRETE", pedidoRetorno.get("totalFrete").getAsDouble());
                            }
                            if (pedidoRetorno.has("totalSeguro")) {
                                dadosOrcamento.put("VL_SEGURO", pedidoRetorno.get("totalSeguro").getAsDouble());
                            }
                            if (pedidoRetorno.has("totalOutros")) {
                                dadosOrcamento.put("VL_OUTROS", pedidoRetorno.get("totalOutros").getAsDouble());
                            }
                            if (pedidoRetorno.has("totalEncargoFinanceiros")) {
                                dadosOrcamento.put("VL_ENCARGOS_FINANCEIROS", pedidoRetorno.get("totalEncargosFinanceiros").getAsDouble());
                            }
                            if (pedidoRetorno.has("totalTabelaFaturado")) {
                                dadosOrcamento.put("VL_TABELA_FATURADO", pedidoRetorno.get("totalTabelaFaturado").getAsDouble());
                            }
                            if (pedidoRetorno.has("totalOrcamentoFaturado")) {
                                dadosOrcamento.put("FC_VL_TOTAL_FATURADO", pedidoRetorno.get("totalOrcamentoFaturado").getAsDouble());
                            }
                            dadosOrcamento.put("ATAC_VAREJO", pedidoRetorno.get("tipoVenda").getAsString());
                            if (pedidoRetorno.has("pessoaCliente")) {
                                dadosOrcamento.put("PESSOA_CLIENTE", pedidoRetorno.get("pessoaCliente").getAsString());
                            }
                            if (pedidoRetorno.has("nomeRazao")) {
                                dadosOrcamento.put("NOME_CLIENTE", pedidoRetorno.get("nomeRazao").getAsString());
                            }
                            if (pedidoRetorno.has("rgIe")) {
                                dadosOrcamento.put("IE_RG_CLIENTE", pedidoRetorno.get("rgIe").getAsString());
                            }
                            if (pedidoRetorno.has("cpfCnpj")) {
                                dadosOrcamento.put("CPF_CGC_CLIENTE", pedidoRetorno.get("cpfCnpj").getAsString());
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
                            if (pedidoRetorno.has("observacao")) {
                                dadosOrcamento.put("OBS", pedidoRetorno.get("observacao").getAsString());
                            }
                            if (pedidoRetorno.has("status")) {
                                String situacao = pedidoRetorno.get("status").getAsString();

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

                                if (pedidoRetorno.has("totalTabela")) {
                                    dadosOrcamento.put("VL_TABELA", pedidoRetorno.get("totalTabela").getAsDouble());
                                    dadosOrcamento.put("VL_MERC_BRUTO", pedidoRetorno.get("totalTabela").getAsDouble());
                                }
                                if (pedidoRetorno.has("totalOrcamentoCusto")) {
                                    dadosOrcamento.put("VL_MERC_CUSTO", pedidoRetorno.get("totalOrcamentoCusto").getAsDouble());
                                }
                                if (pedidoRetorno.has("totalOrcamento")) {
                                    dadosOrcamento.put("FC_VL_TOTAL", pedidoRetorno.get("totalOrcamento").getAsDouble());
                                }
                                dadosOrcamento.put("DT_CAD", pedidoRetorno.get("dataCadastro").getAsString());
                                dadosOrcamento.put("DT_ALT", pedidoRetorno.get("dataAlteracao").getAsString());

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

            /*final Vector<SoapObject> listaOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAORCAM, criaPropriedadeDataAlteracaoWebservice("AEAORCAM"));

            // Checa se retornou alguma coisa
            if ((listaOrcamentoObject != null) && (listaOrcamentoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaOrcamentoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaOrcamentoObject.size());
                        }
                    });
                }
                int controle = 0;
                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaOrcamentoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - " + (finalControle + 1) + "/" + listaOrcamentoObject.size());
                    mLoad.progress().update(0, controle, listaOrcamentoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - " + (finalControle + 1) + "/" + listaOrcamentoObject.size());
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

                    ContentValues dadosOrcamento = new ContentValues();
                    //dadosOrcamento.put("ID_AEAORCAM", Integer.parseInt(objeto.getProperty("idOrcamento").toString()));
                    dadosOrcamento.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    if (objeto.hasProperty("idPessoa")) {
                        dadosOrcamento.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idPessoa").toString()));
                    }
                    if (objeto.hasProperty("idEstado")) {
                        dadosOrcamento.put("ID_CFAESTAD", Integer.parseInt(objeto.getProperty("idEstado").toString()));
                    }
                    if (objeto.hasProperty("idCidade")) {
                        dadosOrcamento.put("ID_CFACIDAD", Integer.parseInt(objeto.getProperty("idCidade").toString()));
                    }
                    if (objeto.hasProperty("idRomaneio")) {
                        dadosOrcamento.put("ID_AEAROMAN", Integer.parseInt(objeto.getProperty("idRomaneio").toString()));
                    }
                    if (objeto.hasProperty("idTipoDocumento")) {
                        dadosOrcamento.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    }
                    dadosOrcamento.put("GUID", objeto.getProperty("guid").toString());

                    dadosOrcamento.put("NUMERO", Integer.parseInt(objeto.getProperty("numero").toString()));
                *//*if (objeto.hasProperty("totalOrcamentoCusto")) {
                    dadosOrcamento.put("VL_MERC_CUSTO", Double.parseDouble(objeto.getProperty("totalOrcamentoCusto").toString()));
                }
                dadosOrcamento.put("VL_MERC_BRUTO", Double.parseDouble(objeto.getProperty("totalOrcamentoBruto").toString()));
                if (objeto.hasProperty("totalDesconto")) {
                    dadosOrcamento.put("VL_MERC_DESCONTO", Double.parseDouble(objeto.getProperty("totalDesconto").toString()));
                }*//*
                    if (objeto.hasProperty("totalFrete")) {
                        dadosOrcamento.put("VL_FRETE", Double.parseDouble(objeto.getProperty("totalFrete").toString()));
                    }
                    if (objeto.hasProperty("totalSeguro")) {
                        dadosOrcamento.put("VL_SEGURO", Double.parseDouble(objeto.getProperty("totalSeguro").toString()));
                    }
                    if (objeto.hasProperty("totalOutros")) {
                        dadosOrcamento.put("VL_OUTROS", Double.parseDouble(objeto.getProperty("totalOutros").toString()));
                    }
                    if (objeto.hasProperty("totalEncargoFinanceiros")) {
                        dadosOrcamento.put("VL_ENCARGOS_FINANCEIROS", Double.parseDouble(objeto.getProperty("totalEncargosFinanceiros").toString()));
                    }
                    if (objeto.hasProperty("totalTabelaFaturado")) {
                        dadosOrcamento.put("VL_TABELA_FATURADO", Double.parseDouble(objeto.getProperty("totalTabelaFaturado").toString()));
                    }
                    if (objeto.hasProperty("totalOrcamentoFaturado")) {
                        dadosOrcamento.put("FC_VL_TOTAL_FATURADO", Double.parseDouble(objeto.getProperty("totalOrcamentoFaturado").toString()));
                    }
                    dadosOrcamento.put("ATAC_VAREJO", objeto.getProperty("tipoVenda").toString());
                    if (objeto.hasProperty("pessoaCliente")) {
                        dadosOrcamento.put("PESSOA_CLIENTE", objeto.getProperty("pessoaCliente").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("nomeRazao")) {
                        dadosOrcamento.put("NOME_CLIENTE", objeto.getProperty("nomeRazao").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("rgIe")) {
                        dadosOrcamento.put("IE_RG_CLIENTE", objeto.getProperty("rgIe").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("cpfCnpj")) {
                        dadosOrcamento.put("CPF_CGC_CLIENTE", objeto.getProperty("cpfCnpj").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("enderecoCliente")) {
                        dadosOrcamento.put("ENDERECO_CLIENTE", objeto.getProperty("enderecoCliente").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("bairroCliente")) {
                        dadosOrcamento.put("BAIRRO_CLIENTE", objeto.getProperty("bairroCliente").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("cepCliente")) {
                        dadosOrcamento.put("CEP_CLIENTE", objeto.getProperty("cepCliente").toString().replace("anyType{}", ""));
                    }
                    //dadosOrcamento.put("FONE_CLIENTE", objeto.getProperty("ativo").toString());
                    if (objeto.hasProperty("observacao")) {
                        dadosOrcamento.put("OBS", objeto.getProperty("observacao").toString().replace("anyType{}", ""));
                    }
                    if (objeto.hasProperty("status")) {
                        String situacao = objeto.getProperty("status").toString();

                        if (situacao.equalsIgnoreCase("0") || situacao.equalsIgnoreCase("3")) {
                            // Marca o status como retorno liberado
                            dadosOrcamento.put("STATUS", "RL");

                        } else if (situacao.equalsIgnoreCase("-1") || situacao.equalsIgnoreCase("2")) {
                            // Marca o status como retorno como excluido ou bloqueado
                            dadosOrcamento.put("STATUS", "RB");

                        } else if (situacao.equalsIgnoreCase("7")) {
                            // Marca o status como retorno como conferido
                            dadosOrcamento.put("STATUS", "C");

                        } else if (situacao.equalsIgnoreCase("8") || situacao.equalsIgnoreCase("9") ||
                                situacao.equalsIgnoreCase("10") || situacao.equalsIgnoreCase("11")) {
                            // Marca o status como retorno como faturado
                            dadosOrcamento.put("STATUS", "F");

                        } else if (situacao.equalsIgnoreCase("99")) {
                            // Marca o status como retorno como excluido
                            dadosOrcamento.put("STATUS", "RE");

                        } else {
                            dadosOrcamento.put("STATUS", "RB");
                        }
                    }
                    if (objeto.hasProperty("tipoEntrega")) {
                        dadosOrcamento.put("TIPO_ENTREGA", objeto.getProperty("tipoEntrega").toString().replace("anyType{}", ""));
                    }

                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = orcamentoSql.construirSqlStatement(dadosOrcamento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = orcamentoSql.argumentoStatement(dadosOrcamento);

                    if (orcamentoSql.updateFast(dadosOrcamento, "AEAORCAM.NUMERO = " + dadosOrcamento.getAsInteger("NUMERO")) == 0) {

                        if (objeto.hasProperty("totalTabela")) {
                            dadosOrcamento.put("VL_TABELA", Double.parseDouble(objeto.getProperty("totalTabela").toString()));
                        }
                        if (objeto.hasProperty("totalOrcamento")) {
                            dadosOrcamento.put("FC_VL_TOTAL", Double.parseDouble(objeto.getProperty("totalOrcamento").toString()));
                        }
                        dadosOrcamento.put("DT_CAD", objeto.getProperty("dataCadastro").toString());
                        dadosOrcamento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());

                        if (orcamentoSql.insertOrReplace(dadosOrcamento) <= 0) {
                            todosSucesso = false;
                        }
                    }
                *//*((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        //orcamentoSql.insertOrReplace(dadosOrcamento);
                        orcamentoSql.insertOrReplaceFast(sql, argumentoSql);
                    }
                });*//*
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAORCAM");
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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPERCE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaPercentualRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaPercentualRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_percentual));
                        mLoad.progress().value(0, 0, true).build();

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

                            if (percentualRetorno.has("idPercentual") && percentualRetorno.get("idPercentual").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEAPERCE", percentualRetorno.get("idPercentual").getAsInt());
                            }
                            if (percentualRetorno.has("idTabelaPercentualTabela") && percentualRetorno.get("idTabelaPercentualTabela").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEATBPER_TABELA", percentualRetorno.get("idTabelaPercentualTabela").getAsInt());
                            }
                            if (percentualRetorno.has("idTabelaPercentual") && percentualRetorno.get("idTabelaPercentual").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEATBPER", percentualRetorno.get("idTabelaPercentual").getAsInt());
                            }
                            if (percentualRetorno.has("idEmpresa") && percentualRetorno.get("idEmpresa").getAsInt() > 0) {
                                dadosPercentual.put("ID_SMAEMPRE", percentualRetorno.get("idEmpresa").getAsInt());
                            }
                            if (percentualRetorno.has("idClasse") && percentualRetorno.get("idClasse").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEACLASE", percentualRetorno.get("idClasse").getAsInt());
                            }
                            if (percentualRetorno.has("idMarca") && percentualRetorno.get("idMarca").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEAMARCA", percentualRetorno.get("idMarca").getAsInt());
                            }
                            if (percentualRetorno.has("idProduto") && percentualRetorno.get("idProduto").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEAPRODU", percentualRetorno.get("idProduto").getAsInt());
                            }
                            if (percentualRetorno.has("idProdutoLoja") && percentualRetorno.get("idProdutoLoja").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEAPLOJA", percentualRetorno.get("idProdutoLoja").getAsInt());
                            }
                            if (percentualRetorno.has("idAgrupamentoProduto") && percentualRetorno.get("idAgrupamentoProduto").getAsInt() > 0) {
                                dadosPercentual.put("ID_AEAAGPPR", percentualRetorno.get("idAgrupamentoProduto").getAsInt());
                            }
                            if (percentualRetorno.has("idParametroVendedor") && percentualRetorno.get("idParametroVendedor").getAsInt() > 0) {
                                dadosPercentual.put("ID_CFAPARAM_VENDEDOR", percentualRetorno.get("idParametroVendedor").getAsInt());
                            }
                            dadosPercentual.put("DT_ALT", percentualRetorno.get("dataAlteracao").getAsString());
                            if (percentualRetorno.has("tipoIss")) {
                                dadosPercentual.put("TIPO_ISS", percentualRetorno.get("tipoIss").getAsString());
                            }
                            if (percentualRetorno.has("percentualIss")) {
                                dadosPercentual.put("ISS", percentualRetorno.get("percentualIss").getAsDouble());
                            }
                            if (percentualRetorno.has("custoFixo")) {
                                dadosPercentual.put("CUSTO_FIXO", percentualRetorno.get("custoFixo").getAsDouble());
                            }
                            if (percentualRetorno.has("impostosFederais")) {
                                dadosPercentual.put("IMPOSTOS_FEDERAIS", percentualRetorno.get("impostosFederais").getAsDouble());
                            }
                            if (percentualRetorno.has("markupVarejo")) {
                                dadosPercentual.put("MARKUP_VARE", percentualRetorno.get("markupVarejo").getAsDouble());
                            }
                            if (percentualRetorno.has("markupAtacado")) {
                                dadosPercentual.put("MARKUP_ATAC", percentualRetorno.get("markupAtacado").getAsDouble());
                            }
                            if (percentualRetorno.has("lucroVarejo")) {
                                dadosPercentual.put("LUCRO_VARE", percentualRetorno.get("lucroVarejo").getAsDouble());
                            }
                            if (percentualRetorno.has("lucroAtacado")) {
                                dadosPercentual.put("LUCRO_ATAC", percentualRetorno.get("lucroAtacado").getAsDouble());
                            }
                            if (percentualRetorno.has("descontoMercadoriaVistaVarejo")) {
                                dadosPercentual.put("DESC_MERC_VISTA_VARE", percentualRetorno.get("descontoMercadoriaVistaVarejo").getAsDouble());
                            }
                            if (percentualRetorno.has("descontoMercadoriaVistaAtacado")) {
                                dadosPercentual.put("DESC_MERC_VISTA_ATAC", percentualRetorno.get("descontoMercadoriaVistaAtacado").getAsDouble());
                            }
                            if (percentualRetorno.has("descontoMercadoriaPrazoVarejo")) {
                                dadosPercentual.put("DESC_MERC_PRAZO_VARE", percentualRetorno.get("descontoMercadoriaPrazoVarejo").getAsDouble());
                            }
                            if (percentualRetorno.has("descontoMercadoriaPrazoAtacado")) {
                                dadosPercentual.put("DESC_MERC_PRAZO_ATAC", percentualRetorno.get("descontoMercadoriaPrazoAtacado").getAsDouble());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPercentualObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPERCE, criaPropriedadeDataAlteracaoWebservice("AEAPERCE"));

            // Checa se retornou alguma coisa
            if ((listaPercentualObject != null) && (listaPercentualObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaPercentualObject.size(), false).build();

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
                            progressBarStatus.setMax(listaPercentualObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaPercentual = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPercentualObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_percentual) + " - " + (finalControle + 1) + "/" + listaPercentualObject.size());
                    mLoad.progress().update(0, controle, listaPercentualObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_percentual) + " - " + (finalControle + 1) + "/" + listaPercentualObject.size());
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

                    ContentValues dadosPercentual = new ContentValues();
                    dadosPercentual.put("ID_AEAPERCE", Integer.parseInt(objeto.getProperty("idPercentual").toString()));
                    dadosPercentual.put("ID_AEATBPER_TABELA", Integer.parseInt(objeto.getProperty("idPercentualTabela").toString()));
                    if (objeto.hasProperty("idEmpresa")) {
                        dadosPercentual.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("idClasse")) {
                        dadosPercentual.put("ID_AEACLASE", Integer.parseInt(objeto.getProperty("idClasse").toString()));
                    }
                    if (objeto.hasProperty("idMarca")) {
                        dadosPercentual.put("ID_AEAMARCA", Integer.parseInt(objeto.getProperty("idMarca").toString()));
                    }
                    if (objeto.hasProperty("idProduto")) {
                        dadosPercentual.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                    }
                    if (objeto.hasProperty("idProdutoLoja")) {
                        dadosPercentual.put("ID_AEAPLOJA", Integer.parseInt(objeto.getProperty("idProdutoLoja").toString()));
                    }
                    if (objeto.hasProperty("idParametroVendedor")) {
                        dadosPercentual.put("ID_CFAPARAM_VENDEDOR", Integer.parseInt(objeto.getProperty("idParametroVendedor").toString()));
                    }
                    dadosPercentual.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    if (objeto.hasProperty("tipoIss")) {
                        dadosPercentual.put("TIPO_ISS", objeto.getProperty("tipoIss").toString());
                    }
                    if (objeto.hasProperty("percentualIss")) {
                        dadosPercentual.put("ISS", Double.parseDouble(objeto.getProperty("percentualIss").toString()));
                    }
                    if (objeto.hasProperty("custoFixo")) {
                        dadosPercentual.put("CUSTO_FIXO", Double.parseDouble(objeto.getProperty("custoFixo").toString()));
                    }
                    if (objeto.hasProperty("impostosFederais")) {
                        dadosPercentual.put("IMPOSTOS_FEDERAIS", Double.parseDouble(objeto.getProperty("impostosFederais").toString()));
                    }
                    if (objeto.hasProperty("markupVarejo")) {
                        dadosPercentual.put("MARKUP_VARE", Double.parseDouble(objeto.getProperty("markupVarejo").toString()));
                    }
                    if (objeto.hasProperty("markupAtacado")) {
                        dadosPercentual.put("MARKUP_ATAC", Double.parseDouble(objeto.getProperty("markupAtacado").toString()));
                    }
                    if (objeto.hasProperty("lucroVarejo")) {
                        dadosPercentual.put("LUCRO_VARE", Double.parseDouble(objeto.getProperty("lucroVarejo").toString()));
                    }
                    if (objeto.hasProperty("lucroAtacado")) {
                        dadosPercentual.put("LUCRO_ATAC", Double.parseDouble(objeto.getProperty("lucroAtacado").toString()));
                    }
                    if (objeto.hasProperty("descontoMercadoriaVistaVarejo")) {
                        dadosPercentual.put("DESC_MERC_VISTA_VARE", Double.parseDouble(objeto.getProperty("descontoMercadoriaVistaVarejo").toString()));
                    }
                    if (objeto.hasProperty("descontoMercadoriaVistaAtacado")) {
                        dadosPercentual.put("DESC_MERC_VISTA_ATAC", Double.parseDouble(objeto.getProperty("descontoMercadoriaVistaAtacado").toString()));
                    }
                    if (objeto.hasProperty("descontoMercadoriaPrazoVarejo")) {
                        dadosPercentual.put("DESC_MERC_PRAZO_VARE", Double.parseDouble(objeto.getProperty("descontoMercadoriaPrazoVarejo").toString()));
                    }
                    if (objeto.hasProperty("descontoMercadoriaPrazoAtacado")) {
                        dadosPercentual.put("DESC_MERC_PRAZO_ATAC", Double.parseDouble(objeto.getProperty("descontoMercadoriaPrazoAtacado").toString()));
                    }
                    if (objeto.hasProperty("aliquotaIpi")) {
                        dadosPercentual.put("ALIQUOTA_IPI", Double.parseDouble(objeto.getProperty("aliquotaIpi").toString()));
                    }
                    if (objeto.hasProperty("aliquotaPis")) {
                        dadosPercentual.put("ALIQUOTA_PIS", Double.parseDouble(objeto.getProperty("aliquotaPis").toString()));
                    }
                    if (objeto.hasProperty("aliquotaCofins")) {
                        dadosPercentual.put("ALIQUOTA_COFINS", Double.parseDouble(objeto.getProperty("aliquotaCofins").toString()));
                    }
                    listaPercentual.add(dadosPercentual);

                    //PercentualSql percentualSql = new PercentualSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = percentualSql.construirSqlStatement(dadosPercentual);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = percentualSql.argumentoStatement(dadosPercentual);

                    *//*if (percentualSql.insertOrReplace(dadosPercentual) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //percentualSql.insertOrReplace(dadosPercentual);
                            percentualSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                PercentualSql percentualSql = new PercentualSql(context);

                todosSucesso = percentualSql.insertList(listaPercentual);

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
            }*/
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
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAFATOR, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaFatorRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaFatorRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fator));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosFator.put("ID_AEAFATOR", fatorRetorno.get("idFator").getAsInt());
                            dadosFator.put("DT_ALT", fatorRetorno.get("dataAlteracao").getAsString());
                            dadosFator.put("CODIGO", fatorRetorno.get("codigoFator").getAsInt());
                            dadosFator.put("DESCRICAO", fatorRetorno.get("descricaoFator").getAsString());
                            dadosFator.put("JURO_MEDIO_ATAC", fatorRetorno.get("jurosMedioAtacado").getAsDouble());
                            dadosFator.put("JURO_MEDIO_VARE", fatorRetorno.get("jurosMedioVarejo").getAsDouble());
                            dadosFator.put("JURO_MEDIO_SERV", fatorRetorno.get("jurosMedioServico").getAsDouble());
                            dadosFator.put("DESC_PG_ANT_ATAC", fatorRetorno.get("descontoPagamentoAntecipadoAtacado").getAsDouble());
                            dadosFator.put("DESC_PG_ANT_VARE", fatorRetorno.get("descontoPagamentoAntecipadoVarejo").getAsDouble());
                            dadosFator.put("DESC_PG_ANT_SERV", fatorRetorno.get("descontoPagamentoAntecipadoServico").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_ATAC_VISTA", fatorRetorno.get("descontoMaximoPlanoAtacadoVista").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_ATAC_PRAZO", fatorRetorno.get("descontoMaximoPlanoAtacadoPrazo").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_VARE_VISTA", fatorRetorno.get("descontoMaximoPlanoVarejoVista").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_VARE_PRAZO", fatorRetorno.get("descontoMaximoPlanoVarejoPrazo").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_SERV_VISTA", fatorRetorno.get("descontoMaximoPlanoServicoVista").getAsDouble());
                            dadosFator.put("DESC_MAX_PLANO_SERV_PRAZO", fatorRetorno.get("descontoMaximoPlanoServicoPrazo").getAsDouble());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaFatorObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAFATOR, criaPropriedadeDataAlteracaoWebservice("AEAFATOR"));

            // Checa se retornou alguma coisa
            if ((listaFatorObject != null) && (listaFatorObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaFatorObject.size(), false).build();

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
                            progressBarStatus.setMax(listaFatorObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaFator = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaFatorObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_fator) + " - " + (finalControle + 1) + "/" + listaFatorObject.size());
                    mLoad.progress().update(0, controle, listaFatorObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fator) + " - " + (finalControle + 1) + "/" + listaFatorObject.size());
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

                    ContentValues dadosFator = new ContentValues();
                    dadosFator.put("ID_AEAFATOR", Integer.parseInt(objeto.getProperty("idFator").toString()));
                    dadosFator.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosFator.put("CODIGO", Integer.parseInt(objeto.getProperty("codigoFator").toString()));
                    dadosFator.put("DESCRICAO", objeto.getProperty("descricaoFator").toString());
                    dadosFator.put("JURO_MEDIO_ATAC", Double.parseDouble(objeto.getProperty("jurosMedioAtacado").toString()));
                    dadosFator.put("JURO_MEDIO_VARE", Double.parseDouble(objeto.getProperty("jurosMedioVarejo").toString()));
                    dadosFator.put("JURO_MEDIO_SERV", Double.parseDouble(objeto.getProperty("jurosMedioServico").toString()));
                    dadosFator.put("DESC_PG_ANT_ATAC", Double.parseDouble(objeto.getProperty("descontoPagamentoAntecipadoAtacado").toString()));
                    dadosFator.put("DESC_PG_ANT_VARE", Double.parseDouble(objeto.getProperty("descontoPagamentoAntecipadoVarejo").toString()));
                    dadosFator.put("DESC_PG_ANT_SERV", Double.parseDouble(objeto.getProperty("descontoPagamentoAntecipadoServico").toString()));
                    dadosFator.put("DESC_MAX_PLANO_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoAtacadoVista").toString()));
                    dadosFator.put("DESC_MAX_PLANO_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoAtacadoPrazo").toString()));
                    dadosFator.put("DESC_MAX_PLANO_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoVarejoVista").toString()));
                    dadosFator.put("DESC_MAX_PLANO_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoVarejoPrazo").toString()));
                    dadosFator.put("DESC_MAX_PLANO_SERV_VISTA", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoServicoVista").toString()));
                    dadosFator.put("DESC_MAX_PLANO_SERV_PRAZO", Double.parseDouble(objeto.getProperty("descontoMaximoPlanoServicoPrazo").toString()));
                    if (objeto.hasProperty("TIPO_BONUS")) {
                        dadosFator.put("TIPO_BONUS", objeto.getProperty("tipoBonus").toString());
                    }
                    dadosFator.put("DIAS_BONUS", Integer.parseInt(objeto.getProperty("diasBonus").toString()));

                    listaFator.add(dadosFator);

                    //FatorSql fatorSql = new FatorSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = fatorSql.construirSqlStatement(dadosFator);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = fatorSql.argumentoStatement(dadosFator);

                    *//*if (fatorSql.insertOrReplace(dadosFator) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //fatorSql.insertOrReplace(dadosFator);
                            fatorSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                FatorSql fatorSql = new FatorSql(context);

                todosSucesso = fatorSql.insertList(listaFator);

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
            }*/
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
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_AEAPRREC, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaProdutoRecomendadoRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaProdutoRecomendadoRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_recomendado));
                        mLoad.progress().value(0, 0, true).build();

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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoRecomendadoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPRREC, criaPropriedadeDataAlteracaoWebservice("AEAPRREC"));

            // Checa se retornou alguma coisa
            if ((listaProdutoRecomendadoObject != null) && (listaProdutoRecomendadoObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaProdutoRecomendadoObject.size(), false).build();

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
                            progressBarStatus.setMax(listaProdutoRecomendadoObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaProdutoRecomend = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoRecomendadoObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - " + (finalControle + 1) + "/" + listaProdutoRecomendadoObject.size());
                    mLoad.progress().update(0, controle, listaProdutoRecomendadoObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_recomendado) + " - " + (finalControle + 1) + "/" + listaProdutoRecomendadoObject.size());
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

                    ContentValues dadosRecomendado = new ContentValues();
                    dadosRecomendado.put("ID_AEAPRREC", Integer.parseInt(objeto.getProperty("idPrecoRecomendado").toString()));
                    if (objeto.hasProperty("idProduto")) {
                        dadosRecomendado.put("ID_AEAPRODU", Integer.parseInt(objeto.getProperty("idProduto").toString()));
                    }
                    if (objeto.hasProperty("idAreas")) {
                        dadosRecomendado.put("ID_AEAAREAS", Integer.parseInt(objeto.getProperty("idAreas").toString()));
                    }
                    if (objeto.hasProperty("idCidade")) {
                        dadosRecomendado.put("ID_CFACIDAD", Integer.parseInt(objeto.getProperty("idCidade").toString()));
                    }
                    if (objeto.hasProperty("idClifoVendedor")) {
                        dadosRecomendado.put("ID_CFACLIFO_VENDEDOR", Integer.parseInt(objeto.getProperty("idClifoVendedor").toString()));
                    }
                    if (objeto.hasProperty("idClifo")) {
                        dadosRecomendado.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idClifo").toString()));
                    }
                    if (objeto.hasProperty("idEmpresa")) {
                        dadosRecomendado.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("posicao")) {
                        dadosRecomendado.put("POSICAO", Integer.parseInt(objeto.getProperty("posicao").toString()));
                    }
                    listaProdutoRecomend.add(dadosRecomendado);

                    //ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoRecomendadoSql.construirSqlStatement(dadosRecomendado);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoRecomendadoSql.argumentoStatement(dadosRecomendado);

                    *//*if (produtoRecomendadoSql.insertOrReplace(dadosRecomendado) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoRecomendadoSql.insertOrReplace(dadosRecomendado);
                            produtoRecomendadoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(context);

                todosSucesso = produtoRecomendadoSql.insertList(listaProdutoRecomend);

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
            }*/
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
            // Pega quando foi a ultima data que recebeu dados
            String ultimaData = pegaUltimaDataAtualizacao("RPAPARCE");
            // Cria uma variavel para salvar todos os paramentros em json
            //JsonArray parametros = new JsonArray();
            JsonObject objectparametros = new JsonObject();

            Gson gson = new Gson();
            if ((ultimaData != null) && (!ultimaData.isEmpty())) {

                objectparametros.addProperty("ultimaData", ultimaData);
                //parametros.add(gson.toJsonTree(ultimaData));
            }
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            JsonObject retornoWebservice = gson.fromJson(webserviceSisInfo.executarSelectWebserviceJson(null, WSSisinfoWebservice.FUNCTION_JSON_SELECT_RPAPARCE, WSSisinfoWebservice.METODO_GET, objectparametros.toString()), JsonObject.class);

            if ((retornoWebservice != null) && (retornoWebservice.has(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO))) {
                statuRetorno = retornoWebservice.getAsJsonObject(WSSisinfoWebservice.KEY_OBJECT_STATUS_RETORNO);
                // Verifica se retornou com sucesso
                if (statuRetorno.get(WSSisinfoWebservice.KEY_ELEMENT_CODIGO_RETORNO).getAsInt() == 300) {
                    final JsonArray listaParcelaRetorno = retornoWebservice.getAsJsonArray(WSSisinfoWebservice.KEY_OBJECT_OBJECT_RETORNO);
                    boolean todosSucesso = true;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                    mLoad.progress().value(0, listaParcelaRetorno.size(), false).build();

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
                        // Atualiza a notificacao
                        mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parcela));
                        mLoad.progress().value(0, 0, true).build();

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

                            dadosParcela.put("ID_RPAPARCE", parcelaRetorno.get("idParcela").getAsInt());
                            if (parcelaRetorno.has("idEmpresa") && parcelaRetorno.get("idEmpresa").getAsInt() > 0) {
                                dadosParcela.put("ID_SMAEMPRE", parcelaRetorno.get("idEmpresa").getAsInt());
                            }
                            if (parcelaRetorno.has("idFatura") && parcelaRetorno.get("idFatura").getAsInt() > 0) {
                                dadosParcela.put("ID_RPAFATUR", parcelaRetorno.get("idFatura").getAsInt());
                            }
                            if (parcelaRetorno.has("idPessoa") && parcelaRetorno.get("idPessoa").getAsInt() > 0) {
                                dadosParcela.put("ID_CFACLIFO", parcelaRetorno.get("idPessoa").getAsInt());
                            }
                            if (parcelaRetorno.has("idTipoDocumento") && parcelaRetorno.get("idTipoDocumento").getAsInt() > 0) {
                                dadosParcela.put("ID_CFATPDOC", parcelaRetorno.get("idTipoDocumento").getAsInt());
                            }
                            if (parcelaRetorno.has("idTipoCobranca") && parcelaRetorno.get("idTipoCobranca").getAsInt() > 0) {
                                dadosParcela.put("ID_CFATPCOB", parcelaRetorno.get("idTipoCobranca").getAsInt());
                            }
                            if (parcelaRetorno.has("idPortadorBanco") && parcelaRetorno.get("idPortadorBanco").getAsInt() > 0) {
                                dadosParcela.put("ID_CFAPORTA", parcelaRetorno.get("idPortadorBanco").getAsInt());
                            }
                            dadosParcela.put("DT_ALT", parcelaRetorno.get("dataAlteracao").getAsString());
                            dadosParcela.put("TIPO", parcelaRetorno.get("tipo").getAsString());
                            dadosParcela.put("DT_EMISSAO", parcelaRetorno.get("dataEmissao").getAsString());
                            dadosParcela.put("DT_VENCIMENTO", parcelaRetorno.get("dataVencimento").getAsString());
                            if (parcelaRetorno.has("dataBaixa")) {
                                dadosParcela.put("DT_BAIXA", parcelaRetorno.get("dataBaixa").getAsString());
                            }
                            if (parcelaRetorno.has("parcela")) {
                                dadosParcela.put("PARCELA", parcelaRetorno.get("parcela").getAsInt());
                            }
                            dadosParcela.put("VL_PARCELA", parcelaRetorno.get("valorParcela").getAsDouble());
                            if (parcelaRetorno.has("totalPago")) {
                                dadosParcela.put("FC_VL_TOTAL_PAGO", parcelaRetorno.get("totalPago").getAsDouble());
                            }
                            if (parcelaRetorno.has("totalRestante")) {
                                dadosParcela.put("FC_VL_RESTANTE", parcelaRetorno.get("totalRestante").getAsDouble());
                            }
                            if (parcelaRetorno.has("jurosDiario")) {
                                dadosParcela.put("VL_JUROS_DIARIO", parcelaRetorno.get("jurosDiario").getAsDouble());
                            }
                            if (parcelaRetorno.has("percentualDesconto")) {
                                dadosParcela.put("PERC_DESCONTO", parcelaRetorno.get("percentualDesconto").getAsDouble());
                            }
                            dadosParcela.put("SEQUENCIAL", parcelaRetorno.get("sequencial").getAsString());
                            if (parcelaRetorno.has("numero")) {
                                dadosParcela.put("NUMERO", parcelaRetorno.get("numero").getAsString());
                            }
                            if (parcelaRetorno.has("observasao")) {
                                dadosParcela.put("OBS", parcelaRetorno.get("observasao").getAsString());
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
            /*WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            List<PropertyInfo> listaPropriedadeParcela = criaPropriedadeDataAlteracaoWebservice("RPAPARCE");

            if (listaPropriedadeParcela == null){
                listaPropriedadeParcela = new ArrayList<PropertyInfo>();
            }

            UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);
            ArrayList<UltimaAtualizacaoBeans> lista = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas("RPAPARCE_BAIXA");

            if (lista != null && lista.size() > 0){
                PropertyInfo propertyDataUltimaAtualizacao = new PropertyInfo();
                propertyDataUltimaAtualizacao.setName("dataInicioBaixa");
                propertyDataUltimaAtualizacao.setValue(lista.get(0).getDataUltimaAtualizacao());
                propertyDataUltimaAtualizacao.setType(lista.get(0).getDataUltimaAtualizacao().getClass());

                listaPropriedadeParcela.add(propertyDataUltimaAtualizacao);
            }

            final Vector<SoapObject> listaParcelaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_RPAPARCE, listaPropriedadeParcela);

            // Checa se retornou alguma coisa
            if ((listaParcelaObject != null) && (listaParcelaObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Atualiza a notificacao
                mLoad.bigTextStyle(context.getResources().getString(R.string.servidor_nuvem_retornou_alguma_coisa));
                mLoad.progress().value(0, listaParcelaObject.size(), false).build();

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
                            progressBarStatus.setMax(listaParcelaObject.size());
                        }
                    });
                }
                int controle = 0;

                List<ContentValues> listaParcela = new ArrayList<ContentValues>();

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaParcelaObject) {
                    final int finalControle = controle;

                    // Atualiza a notificacao
                    mLoad.bigTextStyle(context.getResources().getString(R.string.recebendo_dados_parcela) + " - " + (finalControle + 1) + "/" + listaParcelaObject.size());
                    mLoad.progress().update(0, controle, listaParcelaObject.size(), false).build();

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_parcela) + " - " + (finalControle + 1) + "/" + listaParcelaObject.size());
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

                    ContentValues dadosParcela = new ContentValues();
                    dadosParcela.put("ID_RPAPARCE", Integer.parseInt(objeto.getProperty("idParcela").toString()));
                    if (objeto.hasProperty("idEmpresa")) {
                        dadosParcela.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("idFatura")) {
                        dadosParcela.put("ID_RPAFATUR", Integer.parseInt(objeto.getProperty("idFatura").toString()));
                    }
                    if (objeto.hasProperty("idPessoa")) {
                        dadosParcela.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idPessoa").toString()));
                    }
                    if (objeto.hasProperty("idTipoDocumento")) {
                        dadosParcela.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    }
                    if (objeto.hasProperty("idTipoCobranca")) {
                        dadosParcela.put("ID_CFATPCOB", Integer.parseInt(objeto.getProperty("idTipoCobranca").toString()));
                    }
                    if (objeto.hasProperty("idPortadorBanco")) {
                        dadosParcela.put("ID_CFAPORTA", Integer.parseInt(objeto.getProperty("idPortadorBanco").toString()));
                    }
                    dadosParcela.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosParcela.put("TIPO", objeto.getProperty("tipo").toString());
                    dadosParcela.put("DT_EMISSAO", objeto.getProperty("dataEmissao").toString());
                    dadosParcela.put("DT_VENCIMENTO", objeto.getProperty("dataVencimento").toString());
                    if (objeto.hasProperty("dataBaixa")) {
                        dadosParcela.put("DT_BAIXA", objeto.getProperty("dataBaixa").toString());
                    }
                    if (objeto.hasProperty("parcela")) {
                        dadosParcela.put("PARCELA", Integer.parseInt(objeto.getProperty("parcela").toString()));
                    }
                    dadosParcela.put("VL_PARCELA", Double.parseDouble(objeto.getProperty("valorParcela").toString()));
                    if (objeto.hasProperty("totalPago")) {
                        dadosParcela.put("FC_VL_TOTAL_PAGO", Double.parseDouble(objeto.getProperty("totalPago").toString()));
                    }
                    if (objeto.hasProperty("totalRestante")) {
                        dadosParcela.put("FC_VL_RESTANTE", Double.parseDouble(objeto.getProperty("totalRestante").toString()));
                    }
                    if (objeto.hasProperty("jurosDiario")) {
                        dadosParcela.put("VL_JUROS_DIARIO", Double.parseDouble(objeto.getProperty("jurosDiario").toString()));
                    }
                    if (objeto.hasProperty("percentualDesconto")) {
                        dadosParcela.put("PERC_DESCONTO", Double.parseDouble(objeto.getProperty("percentualDesconto").toString()));
                    }
                    dadosParcela.put("SEQUENCIAL", objeto.getProperty("sequencial").toString());
                    if (objeto.hasProperty("numero")) {
                        dadosParcela.put("NUMERO", objeto.getProperty("numero").toString());
                    }
                    if (objeto.hasProperty("observasao")) {
                        dadosParcela.put("OBS", objeto.getProperty("observasao").toString());
                    }
                    listaParcela.add(dadosParcela);

                    //ParcelaSql parcelaSql = new ParcelaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = parcelaSql.construirSqlStatement(dadosParcela);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = parcelaSql.argumentoStatement(dadosParcela);

                    *//*if (parcelaSql.insertOrReplace(dadosParcela) <= 0) {
                        todosSucesso = false;
                    }*//*
                    *//*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //parcelaSql.insertOrReplace(dadosParcela);
                            parcelaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*//*
                } // Fim do for
                ParcelaSql parcelaSql = new ParcelaSql(context);

                todosSucesso = parcelaSql.insertList(listaParcela);

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
            }*/
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
