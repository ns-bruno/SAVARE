package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 22/07/2015.
 */
public class GerarXmlCadastroClienteRotinas {

    private Context context;

    public GerarXmlCadastroClienteRotinas(Context context) {
        this.context = context;
    }

    public List<File> criarArquivoXml(List<PessoaBeans> listaPessoasCadastro){
        List<File> listaLocalArquivoXml = new ArrayList<File>();

        File pasta = new File(Environment.getExternalStorageDirectory() + "/SAVARE/XML");
        File arquivoXml;
        try {
            // Checa se a pasta xml existe
            if (!pasta.exists()) {
                //Caso nao exista as pasta entao eh criado as pastas
                pasta.mkdirs();
            }

            //List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();

            /*String where = "";
            // Checa se foi passado algum parametro
            if (idClienteTemporario != null){
                // Especifica uma pessoa
                where += "CFACLIFO.ID_CFACLIFO = " + idClienteTemporario;
            } else {
                // Pega todos os cadastro temporarios
                where += "CFACLIFO.ID_CFACLIFO < 0 ";
            }
            aqui
            PessoaRotinas pessoaRotinas = new PessoaRotinas(context);
            // Pega a lista de pessoa a serem enviadas os dados
            listaPessoasCadastro = pessoaRotinas.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);*/

            // Checa se retornou algum dados
            if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0){
                // Passa por todos os registros
                for (int i = 0; i < listaPessoasCadastro.size(); i++) {

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                    arquivoXml = new File(pasta, "/CD_" + funcoes.getValorXml("ChaveEmpresa") + "_" + listaPessoasCadastro.get(i).getCpfCnpj().replace(".", "").replace("-", "").replace("/", "") + ".xml");

                    Writer saida = new OutputStreamWriter(new FileOutputStream(arquivoXml));

                    XMLOutputter saidaXml = new XMLOutputter();

                    saidaXml.setFormat(Format.getCompactFormat().setEncoding("ISO-8859-1"));

                    saidaXml.output(gerarXml(listaPessoasCadastro.get(i)), saida);

                    if(arquivoXml.exists()){
                        listaLocalArquivoXml.add(new File(arquivoXml.getPath()));
                    }
                }
            }
        }catch (Exception e){
            ContentValues dadosMensagem = new ContentValues();

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            dadosMensagem.put("comando", 0);
            dadosMensagem.put("tela", "GerarXmlCadastroClienteRotinas");
            dadosMensagem.put("mensagem", "Não conseguimos criar o arquivo XML de cadastro do cliente. \n" + e.getMessage());
            dadosMensagem.put("dados", e.toString());
            dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
            dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
            dadosMensagem.put("email", funcoes.getValorXml("Email"));

            funcoes.menssagem(dadosMensagem);
        }

        return listaLocalArquivoXml;
    } // Fim criarArquivoXml


    private Document gerarXml(PessoaBeans pessoa){

        // Cria o elemento(tag) raiz
        Element tagCadastroProc = new Element("cadastroProc");

        // Cria a tag(elemento) que vai ficar dentro da raiz (cadastroProc)
        Element tagDadosPessoa = new Element("dadosPessoa");
        // Cria a tag que vai dentro do dadosPessoa
        Element tagIdentificacaoPessoa = new Element("identificacaoPessoa");

        // Cria os elementos que vai entro de identificacaoPessoa
        Element idPessoa = new Element("idPessoa");
        // Inseri os dados entro do elemento
        idPessoa.setText(""+pessoa.getIdPessoa());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(idPessoa);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element idAtividade = new Element("idAtividade");
        // Inseri os dados entro do elemento
        idAtividade.setText(""+pessoa.getRamoAtividade().getIdRamoAtividade());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(idAtividade);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element idTipoCliente = new Element("idTipoCliente");
        // Inseri os dados entro do elemento
        idTipoCliente.setText(""+pessoa.getTipoClientePessoa().getIdTipoCliente());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(idTipoCliente);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element razaoSocial = new Element("razaoSocial");
        // Inseri os dados entro do elemento
        razaoSocial.setText(pessoa.getNomeRazao());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(razaoSocial);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element fantasiaApelido = new Element("fantasiaApelido");
        // Inseri os dados entro do elemento
        fantasiaApelido.setText(pessoa.getNomeFantasia());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(fantasiaApelido);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element cpfCnpj = new Element("cpfCnpj");
        // Inseri os dados entro do elemento
        cpfCnpj.setText(""+pessoa.getCpfCnpj());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(cpfCnpj);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element ieRg = new Element("ieRg");
        // Inseri os dados entro do elemento
        ieRg.setText(""+pessoa.getIeRg());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(""+ieRg);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element capitalSocial = new Element("capitalSocial");
        // Inseri os dados entro do elemento
        capitalSocial.setText(""+pessoa.getCapitalSocial());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(capitalSocial);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element cliente = new Element("cliente");
        // Inseri os dados entro do elemento
        cliente.setText(""+pessoa.getCliente());
        // Adiciona o elemento a tag Pai
        tagIdentificacaoPessoa.addContent(cliente);

        tagDadosPessoa.addContent(tagIdentificacaoPessoa);

        // Cria a tag que vai dentro do dadosPessoa
        Element tagEndereco = new Element("endereco");

        // Cria os elementos que vai entro de identificacaoPessoa
        Element idEstado = new Element("idEstado");
        // Inseri os dados entro do elemento
        idEstado.setText(""+pessoa.getEstadoPessoa().getIdEstado());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(idEstado);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element idCidade = new Element("idCidade");
        // Inseri os dados entro do elemento
        idCidade.setText(""+pessoa.getCidadePessoa().getIdCidade());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(idCidade);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element tipoEndereco = new Element("tipoEndereco");
        // Inseri os dados entro do elemento
        tipoEndereco.setText(""+pessoa.getEnderecoPessoa().getTipoEndereco());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(tipoEndereco);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element bairro = new Element("bairro");
        // Inseri os dados entro do elemento
        bairro.setText(""+pessoa.getEnderecoPessoa().getBairro());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(bairro);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element logradouro = new Element("logradouro");
        // Inseri os dados entro do elemento
        logradouro.setText(""+pessoa.getEnderecoPessoa().getLogradouro());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(logradouro);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element numero = new Element("numero");
        // Inseri os dados entro do elemento
        numero.setText(""+pessoa.getEnderecoPessoa().getNumero());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(numero);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element complemento = new Element("complemento");
        // Inseri os dados entro do elemento
        complemento.setText(""+pessoa.getEnderecoPessoa().getComplemento());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(complemento);

        // Cria os elementos que vai entro de identificacaoPessoa
        Element email = new Element("email");
        // Inseri os dados entro do elemento
        email.setText("" + pessoa.getEnderecoPessoa().getEmail());
        // Adiciona o elemento a tag Pai
        tagEndereco.addContent(email);

        tagDadosPessoa.addContent(tagEndereco);

        Element tagParametros = new Element("parametros");

        // Cria os elementos que vai entro de tagParametros
        Element idPortadorBanco = new Element("idPortadorBanco");
        // Inseri os dados entro do elemento
        idPortadorBanco.setText("" + pessoa.getPortadorBancoPessoa().getIdPortadorBanco());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(idPortadorBanco);

        // Cria os elementos que vai entro de tagParametros
        Element idTipoDocumento = new Element("idTipoDocumento");
        // Inseri os dados entro do elemento
        idTipoDocumento.setText("" + pessoa.getTipoDocumentoPessoa().getIdTipoDocumento());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(idTipoDocumento);

        // Cria os elementos que vai entro de tagParametros
        Element idPlanoPagamento = new Element("idPlanoPagamento");
        // Inseri os dados entro do elemento
        idPlanoPagamento.setText("" + pessoa.getPlanoPagamentoPessoa().getIdPlanoPagamento());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(idPlanoPagamento);

        // Cria os elementos que vai entro de tagParametros
        Element limite = new Element("limite");
        // Inseri os dados entro do elemento
        limite.setText("" + pessoa.getLimiteCompra());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(limite);

        // Cria os elementos que vai entro de tagParametros
        Element descontoAtacadoVista = new Element("descontoAtacadoVista");
        // Inseri os dados entro do elemento
        descontoAtacadoVista.setText("" + pessoa.getDescontoAtacadoVista());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(descontoAtacadoVista);

        // Cria os elementos que vai entro de tagParametros
        Element descontoAtacadoPrazo = new Element("descontoAtacadoPrazo");
        // Inseri os dados entro do elemento
        descontoAtacadoPrazo.setText("" + pessoa.getDescontoAtacadoPrazo());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(descontoAtacadoPrazo);

        // Cria os elementos que vai entro de tagParametros
        Element descontoVarejoVista = new Element("descontoVarejoVista");
        // Inseri os dados entro do elemento
        descontoVarejoVista.setText("" + pessoa.getDescontoVarejoVista());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(descontoVarejoVista);

        // Cria os elementos que vai entro de tagParametros
        Element descontoVarejoPrazo = new Element("descontoVarejoPrazo");
        // Inseri os dados entro do elemento
        descontoVarejoPrazo.setText("" + pessoa.getDescontoVarejoPrazo());
        // Adiciona o elemento a tag Pai
        tagParametros.addContent(descontoVarejoPrazo);

        tagDadosPessoa.addContent(tagParametros);

        tagCadastroProc.addContent(tagDadosPessoa);

        Element tagIdentificacaoEmpresa = new Element("identificacaoEmpresa");

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        Element chaveEmpresa = new Element("chaveEmpresa");
        chaveEmpresa.setText(funcoes.getValorXml("ChaveEmpresa"));
        tagIdentificacaoEmpresa.addContent(chaveEmpresa);

        Element codigoEmpresa = new Element("codigoEmpresa");
        codigoEmpresa.setText(funcoes.getValorXml("CodigoEmpresa"));
        tagIdentificacaoEmpresa.addContent(codigoEmpresa);

        Element usuario = new Element("usuario");
        usuario.setText(funcoes.getValorXml("Usuario"));
        tagIdentificacaoEmpresa.addContent(usuario);

        Element codigoUsuario = new Element("codigoUsuario");
        codigoUsuario.setText(funcoes.getValorXml("CodigoUsuario"));
        tagIdentificacaoEmpresa.addContent(codigoUsuario);

        tagCadastroProc.addContent(tagIdentificacaoEmpresa);


        //Criamos um objeto Document
        Document doc = new Document();
        // Adiciona a tag raiz(root) no documento
        doc.setRootElement(tagCadastroProc);

        return doc;
    } // Fim gerarXml
}
