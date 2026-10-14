package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import com.fasterxml.jackson.databind.ObjectMapper;

import modeloMensagem.Requisicao;
import modeloMensagem.Resposta;

public class Servidor {

    private static final int PORTA = 5000;
    private static final int MAX_CONEXOES = 100;
    private static final int TIMEOUT = 30000;
    private static final int MAX_BYTES = 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Socket> conexoes = new CopyOnWriteArrayList<>(); // essa estrutura é thread-safe, ideal para
                                                                        // múltiplas conexões simultâneas
    private final Semaphore vagas = new Semaphore(MAX_CONEXOES);// controla o número máximo de conexões simultâneas

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) { // cria um ServerSocket para escutar na porta especificada

            System.out.println("Servidor iniciado na porta " + PORTA);
            while (true) {

                Socket socket = serverSocket.accept();

                if (!vagas.tryAcquire()) {

                    enviarErro(socket, "Conexão recusada - Servidor cheio",80,null);

                    continue; // volta ao início do loop para aceitar a próxima conexão
                }

                socket.setSoTimeout(TIMEOUT); // define o tempo máximo de inatividade para a conexão
                conexoes.add(socket);
                enviarMensagem(socket, "Conexão aceita", false, false,true,90,null);
                
                new Thread(() -> {
                    try {
                        processarRequisicao(socket);
                    } catch (IOException e) {
                        
                        e.printStackTrace();
                    }
                }).start(); // inicia uma nova thread para processar a requisição do cliente            
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    private void validarDocumento(Socket socket, Requisicao requisicao) throws IOException {
       
        String tipoDocumento = requisicao.getTipoDocumento();
        String documento = requisicao.getDocumento();
        boolean valido = false;

        String documentoLimpo = documento.replaceAll("\\D", "");

        if(documentoLimpo.isBlank()) {
           
            enviarMensagem(socket, "Documento vazio/nulo.", true, false, false,70, requisicao);
            return;
        }

        
        if ("CPF".equalsIgnoreCase(tipoDocumento)) {
         
            if(documentoLimpo.length() == 11) {
                valido = ValidadorDocumento.isCpfValido(documentoLimpo);
            } else {
                enviarMensagem(socket, "Quantidade de dígitos do CPF inválida", true, false, false,70, requisicao);
            }

        } else if ("CNPJ".equalsIgnoreCase(tipoDocumento)) {

            if(documentoLimpo.length() == 14) {
                valido = ValidadorDocumento.isCnpjValido(documentoLimpo);
            } else {
                enviarMensagem(socket, "Quantidade de dígitos do CNPJ inválida", true, false, false,70, requisicao);
            }

        } else {

            enviarMensagem(socket, "Tipo de documento inválido", true, false, false,70, requisicao);
            return;
        }

        enviarMensagem(socket, valido ? "Documento válido" : "Documento inválido", true, valido,true,100, requisicao);
    }

    public void processarRequisicao(Socket socket) throws IOException {
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); // cria um BufferedReader para ler a entrada do cliente

            String linha;
            

            while ((linha = lerMensagem(socket, in)) != null) { 
              
                if (linha.isBlank()) { // ignora linhas em branco
                    continue;
                }


                try {

                    Requisicao requisicao = MAPPER.readValue(
                            linha,
                            Requisicao.class);

                    
                    if (requisicao == null) {
                        enviarErro(socket, "Requisição nula", 60, null);
                        return;
                    }
                    if(requisicao.getVersao() == null) {
                        enviarErro(socket, "Versão do protocolo ausente", 60, requisicao);
                        return;
                    }
                    if (!requisicao.getVersao().equals("1.0")) {
                        enviarErro(socket, "Versão do protocolo incompatível", 60, requisicao);
                        return;
                    }

                    if(requisicao.getId() <= 0 || requisicao.getId() > 999999) {
                        enviarErro(socket, "ID da requisição inválido", 60, requisicao);
                        return;
                    }

                    if(requisicao.getId() == null) {
                        enviarErro(socket, "ID da requisição ausente", 60, requisicao);
                        return;
                    }

                    validarDocumento(socket, requisicao);

                    break; // uma requisição por conexão

                } catch (IOException e) {

                    e.printStackTrace();
                }
          }
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout: cliente ocioso");
        } finally {
            conexoes.remove(socket);
            vagas.release();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String lerMensagem(Socket socket, BufferedReader in) throws IOException {

        StringBuilder mensagem = new StringBuilder();
        int totalBytes = 0;

        int caractere;

        while ((caractere = in.read()) != -1) { // lê caractere por caractere até o final da mensagem

            char c = (char) caractere;

            if (c == '\n') {
                return mensagem.toString();
            }

            String caracteresString = String.valueOf(c);

            int bytes = caracteresString.getBytes(StandardCharsets.UTF_8).length;
            totalBytes += bytes;

            if (totalBytes > MAX_BYTES) {

                enviarErro(socket, "Requisição excede o tamanho máximo permitido",60,null);
                return null; // encerra a leitura da mensagem
            }

            mensagem.append(c);
        }

        if (mensagem.length() > 0) {
            return null;    
        }

        return mensagem.toString();
    }

    private void enviarErro(Socket socket, String mensagemErro, int codigo,Requisicao requisicao) {
        try {
            OutputStream out = socket.getOutputStream();

            Resposta resposta = new Resposta();
            resposta.setCodigo(codigo);
            resposta.setVersao(requisicao.getVersao());
            resposta.setId(requisicao.getId());
            resposta.setSucesso(false);
            resposta.setMensagem(mensagemErro);
            out.write((MAPPER.writeValueAsString(resposta) + "\n").getBytes(StandardCharsets.UTF_8));

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensagem(Socket socket, String mensagem, boolean haValidade, boolean statusValidade,boolean sucesso, int codigo, Requisicao requisicao)
            throws IOException {
        try {
            OutputStream out = socket.getOutputStream();

            Resposta resposta = new Resposta();
            resposta.setCodigo(codigo);

            if (requisicao != null) {
                resposta.setVersao(requisicao.getVersao());
                resposta.setId(requisicao.getId());
            }
            resposta.setSucesso(sucesso);
            if (haValidade) {
                resposta.setValido(statusValidade);
            }

            if (!mensagem.isEmpty()) {
                resposta.setMensagem(mensagem);
            }

            String json = MAPPER.writeValueAsString(resposta) + "\n";
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}