package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
        funcoes.setValorXml("RecebendoDados", "S");

        if (funcoes.existeConexaoInternet()){

            // Cria uma notificacao para ser manipulado
            /*Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message("Importando os dados, aguarde...")
                    .flags(Notification.DEFAULT_SOUND);
                mLoad.simple().build();*/

            try {
                // Checa se a versao do savere eh compativel com o webservice
                if (funcoes.checaVersao()){
                    WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))) ||
                            (tabelaRecebeDados == null) ){

                        // Checo se o texto de status foi passado pro parametro
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setText(context.getResources().getString(R.string.estamos_conectanto_servidor_nuvem));
                                }
                            });
                        }
                        // Busca no servidor Webservice
                        Vector<SoapObject> listaUsuarioObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA, criaPropriedadeDataAlteracaoWebservice("USUARIO_USUA"));

                        // Checa se retornou alguma coisa
                        if (listaUsuarioObject != null && listaUsuarioObject.size() > 0) {

                            // Checo se o texto de status foi passado pro parametro
                            if (textStatus != null){
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

                                // Checa se o texto de status foi passado pro parametro
                                if (textStatus != null){
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            textStatus.setText(context.getResources().getString(R.string.achamos_usuario_servidor_nuvem) + " - " + objeto.getProperty("nomeUsuario").toString());
                                        }
                                    });
                                }
                                // Checa se o usuario esta ativo
                                if (objeto.getProperty("ativoUsuario").toString().equalsIgnoreCase("0")){

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
                                    dadosUsuario.put("EMAIL_USUA", objeto.getProperty("email").toString());
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
                                    final String sql = usuarioSql.construirSqlStatement(dadosUsuario);
                                    // Pega o argumento para o statement
                                    final String[] argumentoSql = usuarioSql.argumentoStatement(dadosUsuario);

                                    //usuarioSql.insertOrReplace(dadosUsuario);

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {

                                            // Inseri os dados do usuario no banco de dados interno
                                            //if (usuarioSql.insertOrReplaceFast(sql, argumentoSql) > 0) {
                                            if (usuarioSql.insert(dadosUsuario) > 0) {

                                                // Salva os dados necessarios no xml de configuracao da app
                                                salvarDadosXml(dadosUsuario);

                                                inserirUltimaAtualizacao("USUARIO_USUA");

                                                // Checa se o texto de status foi passado pro parametro
                                                if (textStatus != null) {
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            textStatus.setText(context.getResources().getString(R.string.usuario_atualizado_sucesso));
                                                        }
                                                    });
                                                }
                                            } else {
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
                        }
                    }

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS))) ||
                            (tabelaRecebeDados == null) ){

                        importarDadosArea();
                    }

                    // Recebe os dados da tabela SMAEMPRES
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados da empresa
                        importaDadosEmpresa();
                    }

                    // Recebe os dados da tabela CFAAREAS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados da empresa
                        importarDadosAtividade();
                    }

                    // Recebe os dados da tabela CFASTATU
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosStatus();
                    }


                    // Recebe os dados da tabela CFATPDOC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPDOC))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosTipoDocumento();
                    }

                    // Recebe os dados da tabela CFACCRED
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACCRED))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosCartaoCredito();
                    }

                    // Recebe os dados da tabela CFAPORTA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPORTA))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosPortador();
                    }

                    // Recebe os dados da tabela CFAPORTA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPROFI))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosProfissao();
                    }

                    // Recebe os dados da tabela CFATPCLI
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPCLI))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosTipoCliente();
                    }

                    // Recebe os dados da tabela CFATPCOB
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFATPCOB))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosTipoCobranca();
                    }

                    // Recebe os dados da tabela CFAESTAD
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAESTAD))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosEstado();
                    }

                    // Recebe os dados da tabela CFACIDAD
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACIDAD))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosCidade();
                    }

                    // Recebe os dados da tabela CFACLIFO
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFACLIFO))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosClifo();
                    }

                    // Recebe os dados da tabela CFAENDER
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAENDER))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosEndereco();
                    }

                    // Recebe os dados da tabela CFAPARAM
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAPARAM))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosParametros();
                    }

                    // Recebe os dados da tabela CFAFOTOS
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_CFAFOTOS))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosFotos();
                    }

                    // Recebe os dados da tabela AEAPLPGT
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPLPGT))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosPlanoPagamento();
                    }

                    // Recebe os dados da tabela AEACLASE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEACLASE))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosClasseProdutos();
                    }

                    // Recebe os dados da tabela AEAUNVEN
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAUNVEN))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosUnidadeVenda();
                    }

                    // Recebe os dados da tabela AEAUNVEN
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAGRADE))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosGrade();
                    }

                    // Recebe os dados da tabela AEAMARCA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAMARCA))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosMarca();
                    }

                    // Recebe os dados da tabela AEACODST
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEACODST))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosCodigoSituacaoTributaria();
                    }

                    // Recebe os dados da tabela AEAPRODU
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPRODU))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosProduto();
                    }

                    // Recebe os dados da tabela AEAEMBAL
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAEMBAL))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosEmbalagem();
                    }

                    // Recebe os dados da tabela AEAPLOJA
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPLOJA))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosProdutosPorLoja();
                    }

                    // Recebe os dados da tabela AEALOCES
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEALOCES))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosLocalEstoque();
                    }

                    // Recebe os dados da tabela AEAESTOQ
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAESTOQ))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosEstoque();
                    }

                    // Recebe os dados da tabela AEAORCAM
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAORCAM))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosOrcamento();
                    }

                    // Recebe os dados da tabela AEAITORC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosItemOrcamento();
                    }

                    // Recebe os dados da tabela AEAPERCE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPERCE))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosPercentual();
                    }

                    // Recebe os dados da tabela AEAFATOR
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAFATOR))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosFator();
                    }

                    // Recebe os dados da tabela AEAPRREC
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_AEAPRREC))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosProdutoRecomendado();
                    }

                    // Recebe os dados da tabela RPAPARCE
                    if (((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_RPAPARCE))) ||
                            (tabelaRecebeDados == null) ){

                        // Importa os dados
                        importarDadosParcela();
                    }

                }else {
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
            } catch (final Exception e){

                // Checo se o texto de status foi passado pro parametro
                if (textStatus != null){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            textStatus.setText(context.getResources().getString(R.string.msg_error) + e.getMessage());
                        }
                    });
                }
                if (progressBarStatus != null){
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
        } else {
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

        //if ((tabelaRecebeDados != null) && (tabelaRecebeDados.length > 0) && (!Arrays.asList(tabelaRecebeDados).contains(WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA))){
            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setText(context.getResources().getString(R.string.terminamos_receber_dados));
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
        //}

    }

    private void salvarDadosXml(ContentValues usuario){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        funcoes.setValorXml("CodigoUsuario", usuario.getAsString("ID_USUA"));
        funcoes.setValorXml("CodigoEmpresa", usuario.getAsString("ID_SMAEMPRE"));
        funcoes.setValorXml("Email", usuario.getAsString("EMAIL_USUA"));
        funcoes.setValorXml("EnviarAutomatico", "S");
        funcoes.setValorXml("ReceberAutomatico", "S");
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

    private void importaDadosEmpresa(){
        WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
        try {
            final Vector<SoapObject> listaEmpresaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE, criaPropriedadeDataAlteracaoWebservice("SMAEMPRE"));

            // Checa se retornou alguma coisa
            if (listaEmpresaObject != null && listaEmpresaObject.size() > 0) {
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
                            progressBarStatus.setMax(listaEmpresaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEmpresaObject) {
                    final int finalControle = controle;

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
                    final ContentValues dadosEmpresa = new ContentValues();

                    // Inseri os valores
                    dadosEmpresa.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    dadosEmpresa.put("DT_ALT", objeto.getProperty("dataAlt").toString());
                    dadosEmpresa.put("NOME_RAZAO", objeto.getProperty("nomeRazao").toString());
                    dadosEmpresa.put("NOME_FANTASIA", objeto.hasProperty("nomeFantasia") ? objeto.getProperty("nomeFantasia").toString() : "");
                    dadosEmpresa.put("CPF_CGC", objeto.getProperty("cpfCnpj").toString());
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

                    final EmpresaSql empresaSql = new EmpresaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = empresaSql.construirSqlStatement(dadosEmpresa);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = empresaSql.argumentoStatement(dadosEmpresa);

                    Log.i("SAVARE", "ReceberDadosWebserviceAsyncRotinas");

                    if (empresaSql.insertOrReplace(dadosEmpresa) <= 0){
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //areasSql.insertOrReplace(dadosAreas);
                            empresaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso){
                    inserirUltimaAtualizacao("SMAEMPRE");
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
                mLoad.simple().build();
        } finally {
            if (webserviceSisInfo != null) {
                webserviceSisInfo = null;
            }
        }
    }

    private void importarDadosArea(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaAreasObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS, criaPropriedadeDataAlteracaoWebservice("CFAAREAS"));

            // Checa se retornou alguma coisa
            if ((listaAreasObject != null) && (listaAreasObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaAreasObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAreasObject) {
                    final int finalControle = controle;

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
                    controle ++;

                    ContentValues dadosAreas = new ContentValues();
                    dadosAreas.put("ID_CFAAREAS", Integer.parseInt(objetoIndividual.getProperty("idAreas").toString()));
                    dadosAreas.put("DT_ALT", objetoIndividual.getProperty("dataAlt").toString());
                    dadosAreas.put("CODIGO", Integer.parseInt(objetoIndividual.getProperty("codigoAreas").toString()));
                    dadosAreas.put("DESCRICAO", objetoIndividual.getProperty("descricaoAreas").toString());
                    dadosAreas.put("DESC_ATAC_VISTA", Double.parseDouble(objetoIndividual.getProperty("descontoAtacadoVista").toString()));
                    dadosAreas.put("DESC_ATAC_PRAZO", Double.parseDouble(objetoIndividual.getProperty("descontoAtacadoPrazo").toString()));
                    dadosAreas.put("DESC_VARE_VISTA", Double.parseDouble(objetoIndividual.getProperty("descontoVarejoVista").toString()));
                    dadosAreas.put("DESC_VARE_PRAZO", Double.parseDouble(objetoIndividual.getProperty("descontoVarejoPrazo").toString()));
                    dadosAreas.put("DESC_SERV_VISTA", Double.parseDouble(objetoIndividual.getProperty("descontoServicoVista").toString()));
                    dadosAreas.put("DESC_SERV_PRAZO", Double.parseDouble(objetoIndividual.getProperty("descontoServicoPrazo").toString()));
                    if ((objetoIndividual.hasProperty("descontoPromocao"))) {
                        dadosAreas.put("DESC_PROMOCAO", objetoIndividual.getProperty("descontoPromocao").toString());
                    }

                    AreasSql areasSql = new AreasSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = areasSql.construirSqlStatement(dadosAreas);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = areasSql.argumentoStatement(dadosAreas);

                    if (areasSql.insertOrReplace(dadosAreas) <= 0) {
                        todosSucesso = false;
                    }
                                /*((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        //areasSql.insertOrReplace(dadosAreas);
                                        areasSql.insertOrReplaceFast(sql, argumentoSql);
                                    }
                                });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAAREAS");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosAtividade(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaAtividadeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI, criaPropriedadeDataAlteracaoWebservice("CFAATIVI"));

            // Checa se retornou alguma coisa
            if ((listaAtividadeObject != null) && (listaAtividadeObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaAtividadeObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAtividadeObject) {
                    final int finalControle = controle;
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

                    ContentValues dadosAreas = new ContentValues();
                    dadosAreas.put("ID_CFAATIVI", Integer.parseInt(objeto.getProperty("idRamoAtividade").toString()));
                    dadosAreas.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosAreas.put("CODIGO", Integer.parseInt(objeto.getProperty("codigo").toString()));
                    dadosAreas.put("DESCRICAO", objeto.getProperty("descricaoRamoAtividade").toString());
                    dadosAreas.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    dadosAreas.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    dadosAreas.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    dadosAreas.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    if ((objeto.hasProperty("descontoPromocao"))) {
                        dadosAreas.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }

                    RamoAtividadeSql atividadeSql = new RamoAtividadeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = atividadeSql.construirSqlStatement(dadosAreas);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = atividadeSql.argumentoStatement(dadosAreas);

                    if (atividadeSql.insertOrReplace(dadosAreas) <= 0) {
                        todosSucesso = false;
                    }
                                /*((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        //atividadeSql.insertOrReplace(dadosAreas);
                                        atividadeSql.insertOrReplaceFast(sql, argumentoSql);
                                    }
                                });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAATIVI");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .bigTextStyle(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosStatus(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaStatusObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU, criaPropriedadeDataAlteracaoWebservice("CFASTATU"));

            // Checa se retornou alguma coisa
            if ((listaStatusObject != null) && (listaStatusObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaStatusObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaStatusObject) {
                    final int finalControle = controle;

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

                    StatusSql statusSql = new StatusSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = statusSql.construirSqlStatement(dadosStatus);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = statusSql.argumentoStatement(dadosStatus);

                    if (statusSql.insertOrReplace(dadosStatus) <= 0) {
                        todosSucesso = false;
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
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFASTATU");
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosTipoDocumento(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaDocumentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPDOC, criaPropriedadeDataAlteracaoWebservice("CFATPDOC"));

            // Checa se retornou alguma coisa
            if ((listaDocumentoObject != null) && (listaDocumentoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaDocumentoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaDocumentoObject) {
                    final int finalControle = controle;

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

                    TipoDocumentoSql tipoDocumentoSql = new TipoDocumentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = tipoDocumentoSql.construirSqlStatement(dadosDocumento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = tipoDocumentoSql.argumentoStatement(dadosDocumento);

                    if (tipoDocumentoSql.insertOrReplace(dadosDocumento) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //tipoDocumentoSql.insertOrReplace(dadosDocumento);
                            tipoDocumentoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFATPDOC");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosCartaoCredito(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaCartaoCreditoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACCRED, criaPropriedadeDataAlteracaoWebservice("CFACCRED"));

            // Checa se retornou alguma coisa
            if ((listaCartaoCreditoObject != null) && (listaCartaoCreditoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaCartaoCreditoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaCartaoCreditoObject) {
                    final int finalControle = controle;

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

                    CartaoSql cartaoSql = new CartaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cartaoSql.construirSqlStatement(dadosCartao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cartaoSql.argumentoStatement(dadosCartao);

                    if (cartaoSql.insertOrReplace(dadosCartao) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cartaoSql.insertOrReplace(dadosCartao);
                            cartaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFACCRED");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosPortador(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPortadorObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPORTA, criaPropriedadeDataAlteracaoWebservice("CFAPORTA"));

            // Checa se retornou alguma coisa
            if ((listaPortadorObject != null) && (listaPortadorObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaPortadorObject.size());
                        }
                    });
                }
                int controle = 0;


                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPortadorObject) {
                    final int finalControle = controle;

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

                    PortadorBancoSql portadorBancoSql = new PortadorBancoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = portadorBancoSql.construirSqlStatement(dadosPortador);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = portadorBancoSql.argumentoStatement(dadosPortador);

                    if (portadorBancoSql.insertOrReplace(dadosPortador) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //portadorBancoSql.insertOrReplace(dadosPortador);
                            portadorBancoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAPORTA");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosProfissao(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProfissaoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPROFI, criaPropriedadeDataAlteracaoWebservice("CFAPROFI"));

            // Checa se retornou alguma coisa
            if ((listaProfissaoObject != null) && (listaProfissaoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaProfissaoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProfissaoObject) {
                    final int finalControle = controle;

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

                    ProfissaoSql profissaoSql = new ProfissaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = profissaoSql.construirSqlStatement(dadosProfissao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = profissaoSql.argumentoStatement(dadosProfissao);

                    if (profissaoSql.insertOrReplace(dadosProfissao) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //profissaoSql.insertOrReplace(dadosProfissao);
                            profissaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAPROFI");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosTipoCliente(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaTipoClienteObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPCLI, criaPropriedadeDataAlteracaoWebservice("CFATPCLI"));

            // Checa se retornou alguma coisa
            if ((listaTipoClienteObject != null) && (listaTipoClienteObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaTipoClienteObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaTipoClienteObject) {
                    final int finalControle = controle;

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

                    TipoClienteSql tipoClienteSql = new TipoClienteSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = tipoClienteSql.construirSqlStatement(dadosTipoCliente);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = tipoClienteSql.argumentoStatement(dadosTipoCliente);

                    if (tipoClienteSql.insertOrReplace(dadosTipoCliente) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //tipoClienteSql.insertOrReplace(dadosTipoCliente);
                            tipoClienteSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFATPCLI");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosTipoCobranca(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaTipoCobrancaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFATPCOB, criaPropriedadeDataAlteracaoWebservice("CFATPCOB"));

            // Checa se retornou alguma coisa
            if ((listaTipoCobrancaObject != null) && (listaTipoCobrancaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaTipoCobrancaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaTipoCobrancaObject) {
                    final int finalControle = controle;

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

                    CobrancaSql cobrancaSql = new CobrancaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cobrancaSql.construirSqlStatement(dadosTipoCobranca);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cobrancaSql.argumentoStatement(dadosTipoCobranca);

                    if (cobrancaSql.insertOrReplace(dadosTipoCobranca) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cobrancaSql.insertOrReplace(dadosTipoCobranca);
                            cobrancaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFATPCOB");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosEstado(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEstadoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAESTAD, criaPropriedadeDataAlteracaoWebservice("CFAESTAD"));

            // Checa se retornou alguma coisa
            if ((listaEstadoObject != null) && (listaEstadoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaEstadoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEstadoObject) {
                    final int finalControle = controle;

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

                    EstadoSql estadoSql = new EstadoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = estadoSql.construirSqlStatement(dadosEstado);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = estadoSql.argumentoStatement(dadosEstado);

                    if (estadoSql.insertOrReplace(dadosEstado) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //estadoSql.insertOrReplace(dadosEstado);
                            estadoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAESTAD");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosCidade(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaCidadeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACIDAD, criaPropriedadeDataAlteracaoWebservice("CFACIDAD"));

            // Checa se retornou alguma coisa
            if ((listaCidadeObject != null) && (listaCidadeObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaCidadeObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaCidadeObject) {
                    final int finalControle = controle;

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

                    CidadeSql cidadeSql = new CidadeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = cidadeSql.construirSqlStatement(dadosCidade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = cidadeSql.argumentoStatement(dadosCidade);

                    if (cidadeSql.insertOrReplace(dadosCidade) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //cidadeSql.insertOrReplace(dadosCidade);
                            cidadeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFACIDAD");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosClifo(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaClifoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFACLIFO, criaPropriedadeDataAlteracaoWebservice("CFACLIFO"));

            // Checa se retornou alguma coisa
            if ((listaClifoObject != null) && (listaClifoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaClifoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaClifoObject) {
                    final int finalControle = controle;

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
                    dadosClifo.put("CPF_CNPJ", objeto.getProperty("cpfCnpj").toString());
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

                    PessoaSql pessoaSql = new PessoaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = pessoaSql.construirSqlStatement(dadosClifo);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = pessoaSql.argumentoStatement(dadosClifo);

                    if (pessoaSql.insertOrReplace(dadosClifo) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //pessoaSql.insertOrReplace(dadosClifo);
                            pessoaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFACLIFO");
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
        }catch (Exception e){

            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosEndereco(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEnderecoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAENDER, criaPropriedadeDataAlteracaoWebservice("CFAENDER"));

            // Checa se retornou alguma coisa
            if ((listaEnderecoObject != null) && (listaEnderecoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaEnderecoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEnderecoObject) {
                    final int finalControle = controle;

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


                    EnderecoSql enderecoSql = new EnderecoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = enderecoSql.construirSqlStatement(dadosEndereco);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = enderecoSql.argumentoStatement(dadosEndereco);

                    if (enderecoSql.insertOrReplace(dadosEndereco) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //enderecoSql.insertOrReplace(dadosEndereco);
                            enderecoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAENDER");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosParametros(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaParametroObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAPARAM, criaPropriedadeDataAlteracaoWebservice("CFAPARAM"));

            // Checa se retornou alguma coisa
            if ((listaParametroObject != null) && (listaParametroObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaParametroObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaParametroObject) {
                    final int finalControle = controle;

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

                    ContentValues dadosEndereco = new ContentValues();
                    dadosEndereco.put("ID_CFAPARAM", Integer.parseInt(objeto.getProperty("idParametro").toString()));
                    dadosEndereco.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    if (objeto.hasProperty("idClifo")){
                        dadosEndereco.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idClifo").toString()));
                    }
                    if (objeto.hasProperty("idEmpresa")){
                        dadosEndereco.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("idVendedor")){
                        dadosEndereco.put("ID_CFACLIFO_VENDE", Integer.parseInt(objeto.getProperty("idVendedor").toString()));
                    }
                    if (objeto.hasProperty("idTipoCobranca")){
                        dadosEndereco.put("ID_CFATPCOB", Integer.parseInt(objeto.getProperty("idTipoCobranca").toString()));
                    }
                    if (objeto.hasProperty("idPortadorBanco")){
                        dadosEndereco.put("ID_CFAPORTA", Integer.parseInt(objeto.getProperty("idPortadorBanco").toString()));
                    }
                    if (objeto.hasProperty("idTipoDocumento")){
                        dadosEndereco.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    }
                    if (objeto.hasProperty("idPlanoPagamento")){
                        dadosEndereco.put("ID_AEAPLPGT", Integer.parseInt(objeto.getProperty("idPlanoPagamento").toString()));
                    }
                    if (objeto.hasProperty("roteiro")){
                        dadosEndereco.put("ROTEIRO", Integer.parseInt(objeto.getProperty("roteiro").toString()));
                    }
                    if (objeto.hasProperty("frequencia")){
                        dadosEndereco.put("FREQUENCIA", Integer.parseInt(objeto.getProperty("frequencia").toString()));
                    }
                    if (objeto.hasProperty("diasAtrazo")){
                        dadosEndereco.put("DIAS_ATRAZO", Integer.parseInt(objeto.getProperty("diasAtrazo").toString()));
                    }
                    if (objeto.hasProperty("diasCarencia")){
                        dadosEndereco.put("DIAS_CARENCIA", Integer.parseInt(objeto.getProperty("diasCarencia").toString()));
                    }
                    if (objeto.hasProperty("vendeAtrazado")){
                        dadosEndereco.put("VENDE_ATRAZADO", objeto.getProperty("vendeAtrazado").toString());
                    }
                    if (objeto.hasProperty("descontoPromocao")){
                        dadosEndereco.put("DESC_PROMOCAO", objeto.getProperty("descontoPromocao").toString());
                    }
                    if (objeto.hasProperty("dataUltimaVisita")){
                        dadosEndereco.put("DT_ULT_VISITA", objeto.getProperty("dataUltimaVisita").toString());
                    }
                    if (objeto.hasProperty("dataUltimoEnvio")){
                        dadosEndereco.put("DT_ULT_ENVIO", objeto.getProperty("dataUltimoEnvio").toString());
                    }
                    if (objeto.hasProperty("dataUltimoRecebimento")){
                        dadosEndereco.put("DT_ULT_RECEBTO", objeto.getProperty("dataUltimoRecebimento").toString());
                    }
                    if (objeto.hasProperty("dataProximoContato")){
                        dadosEndereco.put("DT_PROXIMO_CONTATO", objeto.getProperty("dataProximoContato").toString());
                    }
                    if (objeto.hasProperty("atacadoVarejo")){
                        dadosEndereco.put("ATACADO_VEREJO", objeto.getProperty("atacadoVarejo").toString());
                    }
                    if (objeto.hasProperty("vistaPrazo")){
                        dadosEndereco.put("VISTA_PRAZO", objeto.getProperty("vistaPrazo").toString());
                    }
                    if (objeto.hasProperty("faturaValorMinimo")){
                        dadosEndereco.put("FATURA_VL_MIN", objeto.getProperty("faturaValorMinimo").toString());
                    }
                    if (objeto.hasProperty("parcelaEmAberto")){
                        dadosEndereco.put("PARCELA_EM_ABERTO", objeto.getProperty("parcelaEmAberto").toString());
                    }
                    if (objeto.hasProperty("limite")){
                        dadosEndereco.put("LIMITE", Double.parseDouble(objeto.getProperty("limite").toString()));
                    }
                    if (objeto.hasProperty("descontoAtacadoVista")){
                        dadosEndereco.put("DESC_ATAC_VISTA", Double.parseDouble(objeto.getProperty("descontoAtacadoVista").toString()));
                    }
                    if (objeto.hasProperty("descontoAtacadoPrazo")){
                        dadosEndereco.put("DESC_ATAC_PRAZO", Double.parseDouble(objeto.getProperty("descontoAtacadoPrazo").toString()));
                    }
                    if (objeto.hasProperty("descontoVarejoVista")){
                        dadosEndereco.put("DESC_VARE_VISTA", Double.parseDouble(objeto.getProperty("descontoVarejoVista").toString()));
                    }
                    if (objeto.hasProperty("descontoVarejoPrazo")){
                        dadosEndereco.put("DESC_VARE_PRAZO", Double.parseDouble(objeto.getProperty("descontoVarejoPrazo").toString()));
                    }
                    if (objeto.hasProperty("jurosDiario")){
                        dadosEndereco.put("JUROS_DIARIO", Double.parseDouble(objeto.getProperty("jurosDiario").toString()));
                    }

                    ParametrosSql parametrosSql = new ParametrosSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = parametrosSql.construirSqlStatement(dadosEndereco);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = parametrosSql.argumentoStatement(dadosEndereco);

                    if (parametrosSql.insertOrReplace(dadosEndereco) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //parametrosSql.insertOrReplace(dadosEndereco);
                            parametrosSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAPARAM");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosFotos(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaFotosObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAFOTOS, criaPropriedadeDataAlteracaoWebservice("CFAFOTOS"));

            // Checa se retornou alguma coisa
            if ((listaFotosObject != null) && (listaFotosObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaFotosObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaFotosObject) {
                    final int finalControle = controle;

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_fotos) + " - " + (finalControle + 1) + "/" + listaFotosObject.size());
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

                        FotosSql fotosSql = new FotosSql(context);

                        // Pega o sql para passar para o statement
                        //final String sql = fotosSql.construirSqlStatement(dadosFotos);
                        // Pega o argumento para o statement
                        //final String[] argumentoSql = fotosSql.argumentoStatement(dadosFotos);

                        if (fotosSql.insertOrReplace(dadosFotos) <= 0) {
                            todosSucesso = false;
                        }
                    }
                        /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //fotosSql.insertOrReplace(dadosFotos);
                            fotosSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("CFAFOTOS");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosPlanoPagamento(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPlanoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPLPGT, criaPropriedadeDataAlteracaoWebservice("AEAPLPGT"));

            // Checa se retornou alguma coisa
            if ((listaPlanoObject != null) && (listaPlanoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaPlanoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPlanoObject) {
                    final int finalControle = controle;

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

                    PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = planoPagamentoSql.construirSqlStatement(dadosPagamento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = planoPagamentoSql.argumentoStatement(dadosPagamento);

                    if (planoPagamentoSql.insertOrReplace(dadosPagamento) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //planoPagamentoSql.insertOrReplace(dadosPagamento);
                            planoPagamentoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAPLPGT");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private boolean sincronizaUltimaAtualizacao(){
        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = null;
        //ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoWebService = null;
        try {
            UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

            listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(null);

            WSSisinfoWebservice webserviceSisInfo;

            if (listaUltimaAtualizacaoDispositivo == null) {
                return true;
            } else {
                webserviceSisInfo = new WSSisinfoWebservice(context);

                Vector<SoapObject> listaAtualizacaoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_ULTIMA_ATUALIZACAO, null);

                // Checa se retornou alguma coisa
                if (listaAtualizacaoObject != null && listaAtualizacaoObject.size() > 0) {

                    // Instancia a lista do webservice
                    //listaUltimaAtualizacaoWebService = new ArrayList<UltimaAtualizacaoBeans>();

                    // Passa por toda a lista
                    for (SoapObject objetoIndividual : listaAtualizacaoObject) {

                        final SoapObject objeto;

                        if (objetoIndividual.hasProperty("return")) {
                            objeto = (SoapObject) objetoIndividual.getProperty("return");

                        } else {
                            objeto = objetoIndividual;
                        }
                        if (objeto.hasProperty("tabela")) {
                            // Passa por todas as datas do dispositivo
                            for (UltimaAtualizacaoBeans ultima : listaUltimaAtualizacaoDispositivo) {

                                // Checa se a tabela tem na lista do dispositivo
                                if (ultima.getTabela().equalsIgnoreCase(objeto.getProperty("tabela").toString())){

                                    // Checa se tem salvo no banco local do dispositivo o id da ultima atualizacao
                                    if ((objeto.hasProperty("idUltimaAtualizacao")) && (ultima.getIdUltimaAtualizacao() == 0)){

                                        ultima.setIdUltimaAtualizacao(Integer.parseInt(objeto.getProperty("idUltimaAtualizacao").toString()));

                                        ContentValues dataAtualizacao = new ContentValues();
                                        dataAtualizacao.put("ID_ULTIMA_ATUALIZACAO_DISPOSITIVO", Integer.parseInt(objeto.getProperty("idUltimaAtualizacao").toString()));

                                        UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);
                                        // Salva o id da ultima atualizacao que veio do webservice
                                        ultimaAtualizacaoSql.updateFast(dataAtualizacao, "TABELA = '" + ultima.getTabela() + "'");
                                    }

                                    if (objeto.hasProperty("dataUltimaAtualizacao")){
                                        // Checa se o webservice eh diferente das datas do dispositivo local
                                        if (!ultima.getDataUltimaAtualizacao().equalsIgnoreCase(objeto.getProperty("dataUltimaAtualizacao").toString())){

                                            PropertyInfo propertyDataUltimaAtualizacao = new PropertyInfo();
                                            propertyDataUltimaAtualizacao.setName("dadosUltimaAtualizacao");
                                            propertyDataUltimaAtualizacao.setValue(ultima);
                                            propertyDataUltimaAtualizacao.setType(ultima.getClass());

                                            // Cria uma lista para salvar todas as propriedades
                                            List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                                            // Adiciona a propriedade na lista
                                            listaPropertyInfos.add(propertyDataUltimaAtualizacao);

                                            webserviceSisInfo = new WSSisinfoWebservice(context);

                                            // Executa o webservice
                                            RetornoWebServiceBeans retorno = webserviceSisInfo.executarWebservice(listaPropertyInfos, WSSisinfoWebservice.FUNCTION_INSERT_ULTIMA_ATUALIZACAO);

                                            // Checa se retornou alguma coisa
                                            if (retorno != null){
                                                // Checa se o retorno teve insercao com sucesso
                                                if (retorno.getCodigoRetorno() == 100){

                                                    // Checa se tem o id no banco local
                                                    if (ultima.getIdUltimaAtualizacao() <= 0) {
                                                        ContentValues dataAtualizacao = new ContentValues();
                                                        dataAtualizacao.put("ID_ULTIMA_ATUALIZACAO_DISPOSITIVO", Integer.parseInt(retorno.getExtra().toString()));

                                                        UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);
                                                        // Salva o id da ultima atualizacao que veio do webservice
                                                        ultimaAtualizacaoSql.updateFast(dataAtualizacao, "TABELA = '" + ultima.getTabela() + "'");
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return true;
                } else {
                    // Passa por todas as datas do dispositivo
                    for (UltimaAtualizacaoBeans ultima : listaUltimaAtualizacaoDispositivo) {

                        PropertyInfo propertyDataUltimaAtualizacao = new PropertyInfo();
                        propertyDataUltimaAtualizacao.setName("dadosUltimaAtualizacao");
                        propertyDataUltimaAtualizacao.setValue(ultima);
                        propertyDataUltimaAtualizacao.setType(ultima.getClass());

                        // Cria uma lista para salvar todas as propriedades
                        List<PropertyInfo> listaPropertyInfos = new ArrayList<PropertyInfo>();

                        // Adiciona a propriedade na lista
                        listaPropertyInfos.add(propertyDataUltimaAtualizacao);

                        webserviceSisInfo = new WSSisinfoWebservice(context);

                        // Executa o webservice
                        RetornoWebServiceBeans retorno = webserviceSisInfo.executarWebservice(listaPropertyInfos, WSSisinfoWebservice.FUNCTION_INSERT_ULTIMA_ATUALIZACAO);

                        // Checa se retornou alguma coisa
                        if (retorno != null){
                            // Checa se o retorno teve insercao com sucesso
                            if (retorno.getCodigoRetorno() == 100){

                                // Checa se tem o id no banco local
                                if (ultima.getIdUltimaAtualizacao() <= 0) {

                                    ContentValues dataAtualizacao = new ContentValues();
                                    dataAtualizacao.put("ID_ULTIMA_ATUALIZACAO_DISPOSITIVO", Integer.parseInt(retorno.getExtra().toString()));

                                    UltimaAtualizacaoSql ultimaAtualizacaoSql = new UltimaAtualizacaoSql(context);
                                    // Salva o id da ultima atualizacao que veio do webservice
                                    ultimaAtualizacaoSql.updateFast(dataAtualizacao, "TABELA = '" + ultima.getTabela() + "'");
                                }
                            }
                        }
                    }
                    return true;
                }
            }
        } catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
        return false;
    }

    private void importarDadosClasseProdutos(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaClasseObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEACLASE, criaPropriedadeDataAlteracaoWebservice("AEACLASE"));

            // Checa se retornou alguma coisa
            if ((listaClasseObject != null) && (listaClasseObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaClasseObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaClasseObject) {
                    final int finalControle = controle;

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

                    ClasseSql classeSql = new ClasseSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = classeSql.construirSqlStatement(dadosClasse);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = classeSql.argumentoStatement(dadosClasse);

                    if (classeSql.insertOrReplace(dadosClasse) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //classeSql.insertOrReplace(dadosClasse);
                            classeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEACLASE");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosUnidadeVenda(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaUnidadeVendaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAUNVEN, criaPropriedadeDataAlteracaoWebservice("AEAUNVEN"));

            // Checa se retornou alguma coisa
            if ((listaUnidadeVendaObject != null) && (listaUnidadeVendaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaUnidadeVendaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaUnidadeVendaObject) {
                    final int finalControle = controle;

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

                    UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = unidadeVendaSql.construirSqlStatement(dadosUnidade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = unidadeVendaSql.argumentoStatement(dadosUnidade);

                    if (unidadeVendaSql.insertOrReplace(dadosUnidade) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //unidadeVendaSql.insertOrReplace(dadosUnidade);
                            unidadeVendaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAUNVEN");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosGrade(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaGradeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAGRADE, criaPropriedadeDataAlteracaoWebservice("AEAGRADE"));

            // Checa se retornou alguma coisa
            if ((listaGradeObject != null) && (listaGradeObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaGradeObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaGradeObject) {
                    final int finalControle = controle;

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

                    GradeSql gradeSql = new GradeSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = gradeSql.construirSqlStatement(dadosGrade);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = gradeSql.argumentoStatement(dadosGrade);

                    if (gradeSql.insertOrReplace(dadosGrade) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //gradeSql.insertOrReplace(dadosGrade);
                            gradeSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAGRADE");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosMarca(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaMarcaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAMARCA, criaPropriedadeDataAlteracaoWebservice("AEAMARCA"));

            // Checa se retornou alguma coisa
            if ((listaMarcaObject != null) && (listaMarcaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaMarcaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaMarcaObject) {
                    final int finalControle = controle;

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

                    MarcaSql marcaSql = new MarcaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = marcaSql.construirSqlStatement(dadosMarca);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = marcaSql.argumentoStatement(dadosMarca);

                    if (marcaSql.insertOrReplace(dadosMarca) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //marcaSql.insertOrReplace(dadosMarca);
                            marcaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAMARCA");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosCodigoSituacaoTributaria(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaSituacaoTributariaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEACODST, criaPropriedadeDataAlteracaoWebservice("AEACODST"));

            // Checa se retornou alguma coisa
            if ((listaSituacaoTributariaObject != null) && (listaSituacaoTributariaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaSituacaoTributariaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaSituacaoTributariaObject) {
                    final int finalControle = controle;

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

                    SituacaoTributariaSql situacaoTributariaSql = new SituacaoTributariaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = situacaoTributariaSql.construirSqlStatement(dadosSituacao);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = situacaoTributariaSql.argumentoStatement(dadosSituacao);

                    if (situacaoTributariaSql.insertOrReplace(dadosSituacao) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //situacaoTributariaSql.insertOrReplace(dadosSituacao);
                            situacaoTributariaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEACODST");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosProduto(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPRODU, criaPropriedadeDataAlteracaoWebservice("AEAPRODU"));

            // Checa se retornou alguma coisa
            if ((listaProdutoObject != null) && (listaProdutoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaProdutoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoObject) {
                    final int finalControle = controle;

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

                    ProdutoSql produtoSql = new ProdutoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoSql.construirSqlStatement(dadosProduto);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoSql.argumentoStatement(dadosProduto);

                    if (produtoSql.insertOrReplace(dadosProduto) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoSql.insertOrReplace(dadosProduto);
                            produtoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAPRODU");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }

    private void importarDadosEmbalagem(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEmbalagemObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAEMBAL, criaPropriedadeDataAlteracaoWebservice("AEAEMBAL"));

            // Checa se retornou alguma coisa
            if ((listaEmbalagemObject != null) && (listaEmbalagemObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaEmbalagemObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEmbalagemObject) {
                    final int finalControle = controle;

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

                    EmbalagemSql embalagemSql = new EmbalagemSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = embalagemSql.construirSqlStatement(dadosEmbalagem);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = embalagemSql.argumentoStatement(dadosEmbalagem);

                    if (embalagemSql.insertOrReplace(dadosEmbalagem) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //embalagemSql.insertOrReplace(dadosEmbalagem);
                            embalagemSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAEMBAL");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosProdutosPorLoja(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoLojaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPLOJA, criaPropriedadeDataAlteracaoWebservice("AEAPLOJA"));

            // Checa se retornou alguma coisa
            if ((listaProdutoLojaObject != null) && (listaProdutoLojaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaProdutoLojaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoLojaObject) {
                    final int finalControle = controle;

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

                    ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoLojaSql.construirSqlStatement(dadosProdutoLoja);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoLojaSql.argumentoStatement(dadosProdutoLoja);

                    if (produtoLojaSql.insertOrReplace(dadosProdutoLoja) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoLojaSql.insertOrReplace(dadosProdutoLoja);
                            produtoLojaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAPLOJA");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosLocalEstoque(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaLocalEstoqueObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEALOCES, criaPropriedadeDataAlteracaoWebservice("AEALOCES"));

            // Checa se retornou alguma coisa
            if ((listaLocalEstoqueObject != null) && (listaLocalEstoqueObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaLocalEstoqueObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaLocalEstoqueObject) {
                    final int finalControle = controle;

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

                    LocacaoSql locacaoSql = new LocacaoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = locacaoSql.construirSqlStatement(dadosLocalEstoque);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = locacaoSql.argumentoStatement(dadosLocalEstoque);

                    if (locacaoSql.insertOrReplace(dadosLocalEstoque) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //locacaoSql.insertOrReplace(dadosLocalEstoque);
                            locacaoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEALOCES");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosEstoque(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaEstoqueObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAESTOQ, criaPropriedadeDataAlteracaoWebservice("AEAESTOQ"));

            // Checa se retornou alguma coisa
            if ((listaEstoqueObject != null) && (listaEstoqueObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaEstoqueObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEstoqueObject) {
                    final int finalControle = controle;

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

                    EstoqueSql estoqueSql = new EstoqueSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = estoqueSql.construirSqlStatement(dadosEstoque);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = estoqueSql.argumentoStatement(dadosEstoque);

                    if (estoqueSql.insertOrReplace(dadosEstoque) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //estoqueSql.insertOrReplace(dadosEstoque);
                            estoqueSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAESTOQ");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosOrcamento(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAORCAM, criaPropriedadeDataAlteracaoWebservice("AEAORCAM"));

            // Checa se retornou alguma coisa
            if ((listaOrcamentoObject != null) && (listaOrcamentoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaOrcamentoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaOrcamentoObject) {
                    final int finalControle = controle;

                    // Checo se o texto de status foi passado pro parametro
                    if (textStatus != null){
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                textStatus.setText(context.getResources().getString(R.string.recebendo_dados_orcamento) + " - " + (finalControle + 1) + "/" + listaOrcamentoObject.size());
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

                    ContentValues dadosOrcamento = new ContentValues();
                    dadosOrcamento.put("ID_AEAORCAM", Integer.parseInt(objeto.getProperty("idOrcamento").toString()));
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
                    dadosOrcamento.put("DT_CAD", objeto.getProperty("dataCadastro").toString());
                    dadosOrcamento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosOrcamento.put("NUMERO", Integer.parseInt(objeto.getProperty("numero").toString()));
                    if (objeto.hasProperty("totalOrcamentoCusto")) {
                        dadosOrcamento.put("VL_MERC_CUSTO", Double.parseDouble(objeto.getProperty("totalOrcamentoCusto").toString()));
                    }
                    dadosOrcamento.put("VL_MERC_BRUTO", Double.parseDouble(objeto.getProperty("totalOrcamentoBruto").toString()));
                    if (objeto.hasProperty("totalDesconto")) {
                        dadosOrcamento.put("VL_MERC_DESCONTO", Double.parseDouble(objeto.getProperty("totalDesconto").toString()));
                    }
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
                    if (objeto.hasProperty("totalTabela")) {
                        dadosOrcamento.put("VL_TABELA", Double.parseDouble(objeto.getProperty("totalTabela").toString()));
                    }
                    if (objeto.hasProperty("totalTabelaFaturado")) {
                        dadosOrcamento.put("VL_TABELA_FATURADO", Double.parseDouble(objeto.getProperty("totalTabelaFaturado").toString()));
                    }
                    if (objeto.hasProperty("totalOrcamento")) {
                        dadosOrcamento.put("FC_VL_TOTAL", Double.parseDouble(objeto.getProperty("totalOrcamento").toString()));
                    }
                    if (objeto.hasProperty("totalOrcamentoFaturado")) {
                        dadosOrcamento.put("FC_VL_TOTAL_FATURADO", Double.parseDouble(objeto.getProperty("totalOrcamentoFaturado").toString()));
                    }
                    dadosOrcamento.put("ATAC_VAREJO", objeto.getProperty("tipoVenda").toString());
                    if (objeto.hasProperty("pessoaCliente")) {
                        dadosOrcamento.put("PESSOA_CLIENTE", objeto.getProperty("pessoaCliente").toString());
                    }
                    if (objeto.hasProperty("nomeRazao")) {
                        dadosOrcamento.put("NOME_CLIENTE", objeto.getProperty("nomeRazao").toString());
                    }
                    if (objeto.hasProperty("rgIe")) {
                        dadosOrcamento.put("IE_RG_CLIENTE", objeto.getProperty("rgIe").toString());
                    }
                    if (objeto.hasProperty("cpfCnpj")) {
                        dadosOrcamento.put("CPF_CGC_CLIENTE", objeto.getProperty("cpfCnpj").toString());
                    }
                    if (objeto.hasProperty("enderecoCliente")) {
                        dadosOrcamento.put("ENDERECO_CLIENTE", objeto.getProperty("enderecoCliente").toString());
                    }
                    if (objeto.hasProperty("bairroCliente")) {
                        dadosOrcamento.put("BAIRRO_CLIENTE", objeto.getProperty("bairroCliente").toString());
                    }
                    if (objeto.hasProperty("cepCliente")) {
                        dadosOrcamento.put("CEP_CLIENTE", objeto.getProperty("cepCliente").toString());
                    }
                    //dadosOrcamento.put("FONE_CLIENTE", objeto.getProperty("ativo").toString());
                    if (objeto.hasProperty("observacao")) {
                        dadosOrcamento.put("OBS", objeto.getProperty("observacao").toString());
                    }
                    if (objeto.hasProperty("status")) {
                        dadosOrcamento.put("STATUS", objeto.getProperty("status").toString());
                    }
                    if (objeto.hasProperty("tipoEntrega")) {
                        dadosOrcamento.put("TIPO_ENTREGA", objeto.getProperty("tipoEntrega").toString());
                    }
                    if (objeto.hasProperty("pesoLiquido")) {
                        dadosOrcamento.put("PESO_LIQUISO", Double.parseDouble(objeto.getProperty("pesoLiquido").toString()));
                    }
                    if (objeto.hasProperty("pesoBruto")) {
                        dadosOrcamento.put("PESO_BRUTO", Double.parseDouble(objeto.getProperty("pesoBruto").toString()));
                    }

                    OrcamentoSql orcamentoSql = new OrcamentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = orcamentoSql.construirSqlStatement(dadosOrcamento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = orcamentoSql.argumentoStatement(dadosOrcamento);

                    if (orcamentoSql.insertOrReplace(dadosOrcamento) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //orcamentoSql.insertOrReplace(dadosOrcamento);
                            orcamentoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAORCAM");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosItemOrcamento(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaItemOrcamentoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC, criaPropriedadeDataAlteracaoWebservice("AEAITORC"));

            // Checa se retornou alguma coisa
            if ((listaItemOrcamentoObject != null) && (listaItemOrcamentoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaItemOrcamentoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaItemOrcamentoObject) {
                    final int finalControle = controle;

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

                    if (objetoIndividual.hasProperty("return")) {
                        objeto = (SoapObject) objetoIndividual.getProperty("return");
                    } else {
                        objeto = objetoIndividual;
                    }

                    ContentValues dadosItemOrcamento = new ContentValues();
                    dadosItemOrcamento.put("ID_AEAITORC", Integer.parseInt(objeto.getProperty("idItemOrcamento").toString()));
                    dadosItemOrcamento.put("ID_AEAORCAM", Integer.parseInt(objeto.getProperty("idOrcamento").toString()));
                    if (objeto.hasProperty("estoqueVenda")) {
                        SoapObject estoque = (SoapObject) objeto.getProperty("estoqueVenda");
                        dadosItemOrcamento.put("ID_AEAESTOQ", Integer.parseInt(estoque.getProperty("idEstoque").toString()));
                    }
                    if (objeto.hasProperty("planoPagamento")) {
                        SoapObject planoPagamento = (SoapObject) objeto.getProperty("planoPagamento");
                        dadosItemOrcamento.put("ID_AEAPLPGT", Integer.parseInt(planoPagamento.getProperty("idPlanoPagamento").toString()));
                    }
                    if (objeto.hasProperty("unidadeVenda")) {
                        SoapObject unidade = (SoapObject) objeto.getProperty("unidadeVenda");
                        dadosItemOrcamento.put("ID_AEAUNVEN", Integer.parseInt(unidade.getProperty("idUnidadeVenda").toString()));
                    }
                    if (objeto.hasProperty("pessoaVendedor")) {
                        SoapObject vendedor = (SoapObject) objeto.getProperty("pessoaVendedor");
                        dadosItemOrcamento.put("ID_CFACLIFO_VENDEDOR", Integer.parseInt(vendedor.getProperty("idPessoa").toString()));
                    }
                    dadosItemOrcamento.put("GUID", objeto.getProperty("dataAlteracao").toString());
                    dadosItemOrcamento.put("DT_CAD", objeto.getProperty("dataCadastro").toString());
                    dadosItemOrcamento.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    //dadosItemOrcamento.put("DT_ULTIMA_ATUALIZACAO", objeto.getProperty("").toString());
                    dadosItemOrcamento.put("SEQUENCIA", Integer.parseInt(objeto.getProperty("seguencia").toString()));
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
                        dadosItemOrcamento.put("PROMOCAO", objeto.getProperty("promocao").toString());
                    }
                    if (objeto.hasProperty("tipoProduto")) {
                        dadosItemOrcamento.put("TIPO_PRODUTO", objeto.getProperty("tipoProduto").toString());
                    }
                    if (objeto.hasProperty("complemento")) {
                        dadosItemOrcamento.put("COMPLEMENTO", objeto.getProperty("complemento").toString());
                    }
                    //dadosItemOrcamento.put("SEQ_DESCONTO", objeto.getProperty("").toString());
                    if (objeto.hasProperty("pesoLiquido")) {
                        dadosItemOrcamento.put("PESO_LIQUIDO", Double.parseDouble(objeto.getProperty("pesoLiquido").toString()));
                    }
                    if (objeto.hasProperty("pesoBruto")) {
                        dadosItemOrcamento.put("PESO_BRUTO", Double.parseDouble(objeto.getProperty("pesoBruto").toString()));
                    }
                    //dadosItemOrcamento.put("STATUS", objeto.getProperty("").toString());
                    if (objeto.hasProperty("statusRetorno")) {
                        dadosItemOrcamento.put("STATUS_RETORNO", objeto.getProperty("statusRetorno").toString());
                    }


                    ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = itemOrcamentoSql.construirSqlStatement(dadosItemOrcamento);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = itemOrcamentoSql.argumentoStatement(dadosItemOrcamento);

                    if (itemOrcamentoSql.insertOrReplace(dadosItemOrcamento) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //itemOrcamentoSql.insertOrReplace(dadosItemOrcamento);
                            itemOrcamentoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAITORC");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosPercentual(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaPercentualObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPERCE, criaPropriedadeDataAlteracaoWebservice("AEAPERCE"));

            // Checa se retornou alguma coisa
            if ((listaPercentualObject != null) && (listaPercentualObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaPercentualObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaPercentualObject) {
                    final int finalControle = controle;

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
                    dadosPercentual.put("ID_AEAPERCE_TABELA", Integer.parseInt(objeto.getProperty("idPercentualTabela").toString()));
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

                    PercentualSql percentualSql = new PercentualSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = percentualSql.construirSqlStatement(dadosPercentual);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = percentualSql.argumentoStatement(dadosPercentual);

                    if (percentualSql.insertOrReplace(dadosPercentual) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //percentualSql.insertOrReplace(dadosPercentual);
                            percentualSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAPERCE");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosFator(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaFatorObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAFATOR, criaPropriedadeDataAlteracaoWebservice("AEAFATOR"));

            // Checa se retornou alguma coisa
            if ((listaFatorObject != null) && (listaFatorObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaFatorObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaFatorObject) {
                    final int finalControle = controle;

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

                    FatorSql fatorSql = new FatorSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = fatorSql.construirSqlStatement(dadosFator);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = fatorSql.argumentoStatement(dadosFator);

                    if (fatorSql.insertOrReplace(dadosFator) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //fatorSql.insertOrReplace(dadosFator);
                            fatorSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAFATOR");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosProdutoRecomendado(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaProdutoRecomendadoObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_AEAPRREC, criaPropriedadeDataAlteracaoWebservice("AEAPRREC"));

            // Checa se retornou alguma coisa
            if ((listaProdutoRecomendadoObject != null) && (listaProdutoRecomendadoObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaProdutoRecomendadoObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaProdutoRecomendadoObject) {
                    final int finalControle = controle;

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

                    ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = produtoRecomendadoSql.construirSqlStatement(dadosRecomendado);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = produtoRecomendadoSql.argumentoStatement(dadosRecomendado);

                    if (produtoRecomendadoSql.insertOrReplace(dadosRecomendado) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //produtoRecomendadoSql.insertOrReplace(dadosRecomendado);
                            produtoRecomendadoSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("AEAPRREC");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.importar_dados_recebidos)
                    .message(e.getMessage())
                    .flags(Notification.DEFAULT_SOUND);
            mLoad.simple().build();
        }
    }


    private void importarDadosParcela(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            final Vector<SoapObject> listaParcelaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_RPAPARCE, criaPropriedadeDataAlteracaoWebservice("RPAPARCE"));

            // Checa se retornou alguma coisa
            if ((listaParcelaObject != null) && (listaParcelaObject.size() > 0)) {
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
                            progressBarStatus.setMax(listaParcelaObject.size());
                        }
                    });
                }
                int controle = 0;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaParcelaObject) {
                    final int finalControle = controle;

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

                    ContentValues dadosMarca = new ContentValues();
                    dadosMarca.put("ID_RPAPARCE", Integer.parseInt(objeto.getProperty("idParcela").toString()));
                    if (objeto.hasProperty("idEmpresa")) {
                        dadosMarca.put("ID_SMAEMPRE", Integer.parseInt(objeto.getProperty("idEmpresa").toString()));
                    }
                    if (objeto.hasProperty("idFatura")) {
                        dadosMarca.put("ID_RPAFATUR", Integer.parseInt(objeto.getProperty("idFatura").toString()));
                    }
                    if (objeto.hasProperty("idPessoa")) {
                        dadosMarca.put("ID_CFACLIFO", Integer.parseInt(objeto.getProperty("idPessoa").toString()));
                    }
                    if (objeto.hasProperty("idTipoDocumento")) {
                        dadosMarca.put("ID_CFATPDOC", Integer.parseInt(objeto.getProperty("idTipoDocumento").toString()));
                    }
                    if (objeto.hasProperty("idTipoCobranca")) {
                        dadosMarca.put("ID_CFATPCOB", Integer.parseInt(objeto.getProperty("idTipoCobranca").toString()));
                    }
                    if (objeto.hasProperty("idPortadorBanco")) {
                        dadosMarca.put("ID_CFAPORTA", Integer.parseInt(objeto.getProperty("idPortadorBanco").toString()));
                    }
                    dadosMarca.put("DT_ALT", objeto.getProperty("dataAlteracao").toString());
                    dadosMarca.put("TIPO", objeto.getProperty("tipo").toString());
                    dadosMarca.put("DT_EMISSAO", objeto.getProperty("dataEmissao").toString());
                    dadosMarca.put("DT_VENCIMENTO", objeto.getProperty("dataVencimento").toString());
                    if (objeto.hasProperty("dataBaixa")) {
                        dadosMarca.put("DT_BAIXA", objeto.getProperty("dataBaixa").toString());
                    }
                    if (objeto.hasProperty("parcela")) {
                        dadosMarca.put("PARCELA", Integer.parseInt(objeto.getProperty("parcela").toString()));
                    }
                    dadosMarca.put("VL_PARCELA", Double.parseDouble(objeto.getProperty("valorParcela").toString()));
                    if (objeto.hasProperty("totalPago")) {
                        dadosMarca.put("FC_VL_TOTAL_PAGO", Double.parseDouble(objeto.getProperty("totalPago").toString()));
                    }
                    if (objeto.hasProperty("totalRestante")) {
                        dadosMarca.put("FC_VL_RESTANTE", Double.parseDouble(objeto.getProperty("totalRestante").toString()));
                    }
                    if (objeto.hasProperty("jurosDiario")) {
                        dadosMarca.put("VL_JUROS_DIARIO", Double.parseDouble(objeto.getProperty("jurosDiario").toString()));
                    }
                    if (objeto.hasProperty("percentualDesconto")) {
                        dadosMarca.put("PERC_DESCONTO", Double.parseDouble(objeto.getProperty("percentualDesconto").toString()));
                    }
                    dadosMarca.put("SEQUENCIAL", objeto.getProperty("sequencial").toString());
                    if (objeto.hasProperty("numero")) {
                        dadosMarca.put("NUMERO", objeto.getProperty("numero").toString());
                    }
                    if (objeto.hasProperty("observasao")) {
                        dadosMarca.put("OBS", objeto.getProperty("observasao").toString());
                    }

                    ParcelaSql parcelaSql = new ParcelaSql(context);

                    // Pega o sql para passar para o statement
                    //final String sql = parcelaSql.construirSqlStatement(dadosMarca);
                    // Pega o argumento para o statement
                    //final String[] argumentoSql = parcelaSql.argumentoStatement(dadosMarca);

                    if (parcelaSql.insertOrReplace(dadosMarca) <= 0) {
                        todosSucesso = false;
                    }
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //parcelaSql.insertOrReplace(dadosMarca);
                            parcelaSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
                } // Fim do for
                // Checa se todos foram inseridos/atualizados com sucesso
                if (todosSucesso) {
                    inserirUltimaAtualizacao("RPAPARCE");
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
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
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

    private List<PropertyInfo> criaPropriedadeDataAlteracaoWebservice(String tabela){

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(context);

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoDispositivo = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(null);

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
}
