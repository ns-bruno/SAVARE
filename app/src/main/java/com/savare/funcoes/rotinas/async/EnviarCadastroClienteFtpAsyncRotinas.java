package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.savare.R;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarXmlCadastroClienteRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 22/07/2015.
 */
public class EnviarCadastroClienteFtpAsyncRotinas extends AsyncTask<List<PessoaBeans>, String, Integer> {

    public static final int TELA_RECEPTOR_ALARME = 0,
                            TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT = 1,
                            TELA_CLIENTE_DETALHES = 2;
    private Context context;
    private ProgressDialog progress;
    private FTPClient conexaoFtp = new FTPClient();
    private CopyStreamAdapter streamListener;
    private String mensagemErro;
    private int telaChamada;
    private String idPessoaTemporario;


    public EnviarCadastroClienteFtpAsyncRotinas(Context context, int telaChamada) {
        this.context = context;
        this.telaChamada = telaChamada;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Inicializa a variavel de mensagem
        mensagemErro = " ";

        if (telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
            // Instancia a classe para montar uma tela de progresso
            progress = new ProgressDialog(context);
            progress.setMessage("Aguarde, estamos gerando os arquivos necessários à ser enviados...");
            progress.setCancelable(false);
            progress.show();
        }
    }

    @Override
    protected Integer doInBackground(List<PessoaBeans>... params) {

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Checa se nao foi cancelada o progresso
        if(!isCancelled()){
            // Checa se quem chamou esta operacao foi alguma tela
            if (telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                publishProgress("Estamos verificando se existe alguma conexão com a internet, aguarde...");
            }
            // Checa se tem internet
            if (funcoes.existeConexaoInternet()) {

                // Checa se foi passado alguma coisa por parametro
                if (params.length > 0){
                    // Passa por todos os registro do parametro
                    for (int i = 0; i < params.length; i++) {

                        // Envia o arquivo de acordo com o passado por parametro
                        enviarArquivoXmlFtp(params[i]);
                    }
                }/* else {
                    // Envia todos os cadastro novos salvo no banco de dados
                    enviarArquivoXmlFtp(null);
                }*/
            } else {
                mensagemErro += context.getResources().getString(R.string.nao_existe_conexao_internet) + "\n";
            }

        } else {
            mensagemErro += context.getResources().getString(R.string.operacao_cancelada) + "\n";
        }
        return null;
    } // Fim doInBackground

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        // Atualiza mensagem
        if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
            progress.setMessage(values[0]);
        }
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        funcoes.setValorXml("EnviandoDados", "N");

        // Atualiza mensagem
        if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES) {
            // Fecha progressDialogo
            if (progress.isShowing()) {
                progress.dismiss();
            }

            ContentValues dadosMensagem = new ContentValues();

            dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
            dadosMensagem.put("dados", mensagemErro);
            dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
            dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
            dadosMensagem.put("email", funcoes.getValorXml("Email"));

            if (result != null && result > 0) {
                dadosMensagem.put("comando", 1);
                dadosMensagem.put("mensagem", "Foram enviados " + result + " arquivos.");
                dadosMensagem.put("comando", 0);
            } else if (mensagemErro.length() > 5){
                dadosMensagem.put("mensagem", mensagemErro);
                dadosMensagem.put("comando", 0);
            } else {
                dadosMensagem.put("mensagem", "Pronto.");
                dadosMensagem.put("comando", 1);
            }

            funcoes.menssagem(dadosMensagem);
        }
    }

    private void enviarArquivoXmlFtp(List<PessoaBeans> listaPessoasCadastro){

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        UsuarioSQL usuarioSQL = new UsuarioSQL(context);
        // Pega os dados do usuario e conexoes
        Cursor dadosUsuario = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));
        // Move para o primeiro registro
        dadosUsuario.moveToFirst();

        // Pega o host(endereco) do servidor FTP
        String hostFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("IP_SERVIDOR_USUA"));
        String usuarioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("USUARIO_SERVIDOR_USUA"));
        String senhaFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_SERVIDOR_USUA"));
        String nomeDiretorioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("PASTA_SERVIDOR_USUA"));

        // Passa por paramento o id do cliente temporario a ser gerado XML
        GerarXmlCadastroClienteRotinas gerarXml = new GerarXmlCadastroClienteRotinas(context);

        // Checa se quem chamou esta operacao foi alguma tela
        if (telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
            publishProgress("Vamor gerar os arquivos para ser enviado, aguarde...");
        }

        final List<File> listaLocalXml = new ArrayList<File>(gerarXml.criarArquivoXml(listaPessoasCadastro));

        // Checa se retornou alguma coisa
        if (listaLocalXml != null && listaLocalXml.size() > 0){

            // Passa por todos os arquivos
            for (int j = 0; j < listaLocalXml.size(); j++) {

                idPessoaTemporario = "" + listaPessoasCadastro.get(j).getIdPessoa();

                // Checa se quem chamou esta operacao foi alguma tela
                if (telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                    publishProgress("Foi gerado " + listaLocalXml.size() + " arquivo(s). Estamos conectando com o servidor em nuvem, aguarde mais um pouco...");
                }
                try{
                    conexaoFtp.setConnectTimeout(10 * 1000);
                    conexaoFtp.setDefaultTimeout(30 * 1000);

                    // Conecta com o servidor FTP usando a porta 21
                    conexaoFtp.connect(hostFtp);

                    boolean status = false;

                    // Checa o codigo de resposta, se for positivo, a conexao foi feita
                    if (FTPReply.isPositiveCompletion(conexaoFtp.getReplyCode())) {

                        // Autenticacao com usuario e senha
                        status = conexaoFtp.login(usuarioFtp, funcoes.descriptografaSenha(senhaFtp));
                    } else {
                        // Atualiza a mensagem do progresso
                        if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                            publishProgress("Não foi possível conectar no servidor em nuvem. Vamos para próxima etapa, aguarde... \n");
                        }
                    }
                    if (status) {
                        // Atualiza a mensagem do progresso
                        if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                            publishProgress("Conectou com sucesso no servidor em nuvem. Estamos enviado o arquivo " + (j + 1) + " de " + listaLocalXml.size() + "\n");
                        }
                        // Checa se o arquivo xml existe
                        if (listaLocalXml.get(j).exists()) {
                            // Setando para o modo de transferencia de Arquivos
                            // conexaoFtp.setFileType(FTP.BINARY_FILE_TYPE);
                            conexaoFtp.enterLocalPassiveMode();
                            conexaoFtp.setFileTransferMode(FTPClient.ASCII_FILE_TYPE);
                            conexaoFtp.setFileType(FTPClient.ASCII_FILE_TYPE);

                            // Vai para uma pasta especifica no servidor FTP
                            if (!conexaoFtp.changeWorkingDirectory(nomeDiretorioFtp)) {
                                criarDiretorio(nomeDiretorioFtp);
                            }
                            // Pega o Arquivo a ser enviado e transforma em um formato proprio para transferir
                            FileInputStream arqEnviar = new FileInputStream(listaLocalXml.get(j));
                            // Pega o nome do arquivo XML
                            String nome = listaLocalXml.get(j).getName();
                            // Pega o tamanho total do arquivo XML em bytes
                            final double totalByteArquivo = (double) listaLocalXml.get(j).length();
                            final int arquivoAtual = j;

                            // Cria um formato para o formatar numeros reais (double)
                            final NumberFormat format = NumberFormat.getInstance();
                            format.setMaximumFractionDigits(4);
                            format.setMinimumFractionDigits(4);
                            format.setMaximumIntegerDigits(3);
                            format.setMinimumIntegerDigits(1);
                            // format.setRoundingMode(RoundingMode.HALF_UP);

                            // Pega em tempo real o que esta acontecendo na transferencia
                            streamListener = new CopyStreamAdapter() {
                                @Override
                                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {

                                    if(telaChamada != TELA_RECEPTOR_ALARME){
                                        double totalArquivo = (double) totalByteArquivo;
                                        // Converte o total transferido em double
                                        double totalTransferido = (double) totalBytesTransferred;
                                        // Calcula em percentual do total transferido do arquivo XML
                                        double percentual = ((totalTransferido / totalArquivo) * 100);

                                        publishProgress("Foi transferido " + format.format(percentual) + "%, " + totalBytesTransferred + " de " + totalByteArquivo
                                                + " bytes do arquivo " + (arquivoAtual + 1) + " de " + listaLocalXml.size() + ", aguarde...");
                                    }
                                }

                                @Override
                                public void bytesTransferred(CopyStreamEvent arg0) {

                                }
                            };
                            // Associa um ouvite de transferencia de byte
                            conexaoFtp.setCopyStreamListener(streamListener);
                            // Incrementa o tamanho da transferencia do arquivo
                            conexaoFtp.setBufferSize(1024000);

                            // Envia o arquivo
                            if (conexaoFtp.storeFile(funcoes.getValorXml("ChaveEmpresa") + "_arquivoDeEnvio.txt", arqEnviar)) {
                                // Atualiza a mensagem do progresso
                                if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                                    publishProgress("Confirmando o envio do arquivo " + (j + 1) + " de " + listaLocalXml.size() + ", aguarde...\n");
                                }
                                // Renomeia o arquivo enviado para checar se chegou no servidor FTP com  sucesso
                                if (renomeaArquivoFtp(funcoes.getValorXml("ChaveEmpresa") + "_arquivoDeEnvio.txt", nome)) {

                                    // Atualiza a mensagem do progresso
                                    if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                                        publishProgress("Arquvio " + (j + 1) + " de " + listaLocalXml.size() + " enviado com sucesso, eliminando os arquivos temporarios, aguarde...");
                                    }
                                    // Desconecta do servidor FTP
                                    conexaoFtp.disconnect();
                                    // Cria um formato de data
                                    DateFormat dataFormatada = new SimpleDateFormat("MM_yyyy");

                                    File localNovoDiretorio = new File(Environment.getExternalStorageDirectory()+ "/SAVARE/XML/CADASTRO/"+ dataFormatada.format(new Date()));
                                    // Checa se a pasta xml existe
                                    if (!localNovoDiretorio.exists()) {
                                        localNovoDiretorio.mkdirs();
                                    }
                                    // Move o arquivo XML para a pasta  de arquivos enviados
                                    if (listaLocalXml.get(j).renameTo(new File(localNovoDiretorio, listaLocalXml.get(j).getName()))) {
                                        // Atualiza a mensagem do progresso
                                        if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                                            publishProgress("Arquvio temporario eliminado com sucesso, vamos passar para a próxima etapa.");
                                        }
                                        /*//Criamos uma classe SAXBuilder que vai processar o XML4
                                        SAXBuilder builder = new SAXBuilder();
                                        //Este documento agora possui toda a estrutura do arquivo.
                                        Document documento = builder.build(localNovoDiretorio.getPath() + "/" + listaLocalXml.get(j).getName());
                                        //Recuperamos o elemento root
                                        // Cria o elemento(tag) raiz
                                        Element tagCadastroProc = (Element) documento.getRootElement();

                                        //Recuperamos todos elementos filhos (children)
                                        List elements = tagCadastroProc.getChildren();
                                        Iterator iterator = elements.iterator();
                                        //Iteramos com os elementos filhos, e filhos do dos filhos
                                        Element pessoa = (Element) iterator.next();
                                        // Pega o id do elemento
                                        String idPessoa = pessoa.getChildText("idPessoa");*/

                                        final PessoaSql pessoaSql = new PessoaSql(context);

                                        final ContentValues atualizaPessoa = new ContentValues();
                                        atualizaPessoa.put("STATUS_CADASTRO_NOVO", "E");

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            public void run() {
                                                // Atualiza os dados da pessoa
                                                if ((pessoaSql.update(atualizaPessoa, "CFACLIFO.ID_CFACLIFO = " + idPessoaTemporario)) > 0){
                                                    // Atualiza a mensagem do progresso
                                                    if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                                                        publishProgress("Pronto! Enviamos e marcamos o cadastro como enviado. Só aguardar o retorno da empresa. Vamos para próxima faze.");
                                                    }
                                                } else {
                                                    // Atualiza a mensagem do progresso
                                                    if(telaChamada == TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT || telaChamada == TELA_CLIENTE_DETALHES){
                                                        publishProgress("Não conseguimos marca o cadastro como enviado.");
                                                    }
                                                    mensagemErro += "Não conseguimos marca o cadastro como enviado.";
                                                }
                                            }
                                        });

                                    } else {
                                        mensagemErro += "Erro ao mover o arquivo enviado para a pasta ENVIADOS. " + listaLocalXml.get(j).getPath() + "\n";
                                    }
                                }else { // Fim if renomeArquivoFtp
                                    mensagemErro += "Erro ao checar se o arquivo foi enviado. \n";
                                }
                                // Fecha o arquivo a ser enviado
                                arqEnviar.close();
                            } else {// Fim if storeFile
                                mensagemErro += "Erro ao fazer upload do arquivo. \n";
                            }

                        } else {
                            mensagemErro += "Não foi encontrado o arquivo para envio. \n";
                        }

                    } else { // Fim if status
                        mensagemErro += "Usuário ou Senha estão errados. Favor verificar \n";
                    }
                }catch (final IOException e) {
                    // Deleta o arquivo XML a ser enviado
                    listaLocalXml.get(j).delete();
                    // Salva os dados da mensagem
                    mensagemErro = mensagemErro + "Não foi possível enviar para a nuvem nenhum arquivo. \n" + e.getMessage() +  "\n";

                } catch (final Exception e) {
                    // Deleta o arquivo XML a ser enviado
                    listaLocalXml.get(j).delete();
                    // Salva os dados da mensagem
                    mensagemErro = mensagemErro + "Não foi possível enviar para a nuvem nenhum arquivo. \n" + e.getMessage() +  "\n";

                }
            } // For j
        } else {
            mensagemErro += "Não existe novos cadastro a serem enviados.\n";
        }
    }

    public boolean criarDiretorio(final String nomeDiretorio) {
        try {
            boolean status = conexaoFtp.makeDirectory(nomeDiretorio);
            return status;

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    ContentValues dadosMensagem = new ContentValues();

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                    dadosMensagem.put("comando", 0);
                    dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
                    dadosMensagem.put("mensagem", "Não conseguimos criar o diretorio " + nomeDiretorio + "\n" + e.getMessage());
                    dadosMensagem.put("dados", e.toString());
                    dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
                    dadosMensagem.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(dadosMensagem);
                }
            });
        }
        return false;
    } // Fim criarDiretorio


    /**
     * Renomea um arquivo especifico dentro da pasta do servidor FTP.
     *
     * @param nomeAntigo
     * @param nomeNovo
     * @return
     */
    public boolean renomeaArquivoFtp(final String nomeAntigo, final String nomeNovo) {
        try {
            boolean status = conexaoFtp.rename(nomeAntigo, nomeNovo);
            return status;

        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    ContentValues dadosMensagem = new ContentValues();

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(
                            context);

                    dadosMensagem.put("comando", 0);
                    dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
                    dadosMensagem.put("mensagem", "Não foi possível renomear o arquivo xml de " + nomeAntigo + " para " + nomeNovo + "\n" + e.getMessage());
                    dadosMensagem.put("dados", e.toString());
                    dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
                    dadosMensagem.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(dadosMensagem);
                }
            });
        }
        return false;
    } // Fim renomeaArquivoFtp
}
