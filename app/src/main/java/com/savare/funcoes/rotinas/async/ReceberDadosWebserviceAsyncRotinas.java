package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.banco.funcoesSql.AreasSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.RamoAtividadeSql;
import com.savare.banco.funcoesSql.StatusSql;
import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.RetornoWebServiceBeans;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.VersionUtils;
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
                if (checaVersao()){
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
                                    dadosUsuario.put("SENHA_SERVIDOR_USUA", (objeto.hasProperty("senhaServidor")) ? objeto.getProperty("senhaServidor").toString() : "");
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
                                            if (usuarioSql.insertOrReplaceFast(sql, argumentoSql) > 0) {

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

                        // Importa os dados da empresa
                        importarDadosStatus();
                    }
                }
            } catch (Exception e){

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
    }

    /**
     * Funcao para checar a versao do savare com o servidor webservice.
     *
     * @return
     */
    private boolean checaVersao(){
        boolean valido = false;
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
            Vector<SoapObject> listaVersao = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_VERSAO_SAVARE, null);

            // Checa se retornou alguma coisa
            if (listaVersao != null && listaVersao.size() > 0) {

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaVersao) {
                    // Cria uma vareavel para receber os dados retornado do webservice
                    //SoapObject objeto;

                    /*if (objetoIndividual.hasProperty("return")) {
                        objeto = (SoapObject) objetoIndividual.getProperty("return");
                    } else {
                        objeto = objetoIndividual;
                    }*/

                    int versaoLocal = VersionUtils.getVersionCode(context);
                    int versaoWebservice = Integer.parseInt(objetoIndividual.getProperty("return").toString());

                    // Checa se o SAVARE esta desatualizado
                    if (versaoLocal < versaoWebservice){

                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                            .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                            .smallIcon(R.mipmap.ic_launcher)
                            .largeIcon(R.drawable.ic_launcher)
                            .title(R.string.versao_savare_desatualizada)
                            .bigTextStyle(R.string.savare_desatualizado_favor_atualize)
                            .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();

                    // Checa se o SAVARE esta mais atualizado que o webservice
                    } else if (versaoLocal > versaoWebservice){

                        // Cria uma notificacao para ser manipulado
                        Load mLoad = PugNotification.with(context).load()
                                .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                                .smallIcon(R.mipmap.ic_launcher)
                                .largeIcon(R.drawable.ic_launcher)
                                .title(R.string.versao_savare_desatualizada)
                                .bigTextStyle(R.string.savare_mais_atualizado_que_webservice)
                                .flags(Notification.DEFAULT_LIGHTS);
                        mLoad.simple().build();

                        valido = true;

                    // Checa se o SAVARE esta na mesma versao que o webservice
                    } else if (versaoLocal == versaoWebservice){
                        valido = true;
                    }
                }
            }
        }catch (Exception e){
            // Cria uma notificacao para ser manipulado
            Load mLoad = PugNotification.with(context).load()
                    .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.drawable.ic_launcher)
                    .title(R.string.versao_savare_desatualizada)
                    .bigTextStyle(context.getResources().getString(R.string.erro_validar_versao) + " \n " + e.getMessage())
                    .flags(Notification.DEFAULT_LIGHTS);
            mLoad.simple().build();
        }

        return valido;
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

    private void importaDadosEmpresa(){
        WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);
        try {
            Vector<SoapObject> listaEmpresaObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE, criaPropriedadeDataAlteracaoWebservice("SMAEMPRE"));

            // Checa se retornou alguma coisa
            if (listaEmpresaObject != null && listaEmpresaObject.size() > 0) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaEmpresaObject) {

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
                    .message(e.getMessage())
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

            Vector<SoapObject> listaAreasObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS, criaPropriedadeDataAlteracaoWebservice("CFAAREAS"));

            // Checa se retornou alguma coisa
            if ((listaAreasObject != null) && (listaAreasObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAreasObject) {

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


    private void importarDadosAtividade(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            Vector<SoapObject> listaAtividadeObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI, criaPropriedadeDataAlteracaoWebservice("CFAATIVI"));

            // Checa se retornou alguma coisa
            if ((listaAtividadeObject != null) && (listaAtividadeObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaAtividadeObject) {

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


    private void importarDadosStatus(){
        try {
            WSSisinfoWebservice webserviceSisInfo = new WSSisinfoWebservice(context);

            Vector<SoapObject> listaStatusObject = webserviceSisInfo.executarSelectWebservice(null, WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU, criaPropriedadeDataAlteracaoWebservice("CFASTATU"));

            // Checa se retornou alguma coisa
            if ((listaStatusObject != null) && (listaStatusObject.size() > 0)) {
                // Vareavel para saber se todos os dados foram inseridos com sucesso
                boolean todosSucesso = true;

                // Passa por toda a lista
                for (SoapObject objetoIndividual : listaStatusObject) {

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
                    /*((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            //statusSql.insertOrReplace(dadosStatus);
                            statusSql.insertOrReplaceFast(sql, argumentoSql);
                        }
                    });*/
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

        if ((listaUltimaAtualizacaoDispositivo != null) && (listaUltimaAtualizacaoDispositivo.size() > 0) && (listaUltimaAtualizacaoDispositivo.contains(tabela)) &&
            (tabela != null) && (!tabela.isEmpty())) {

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
