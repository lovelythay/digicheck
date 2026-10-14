import * as net from "node:net";
import * as readLine from "node:readline";
import { Requisicao } from "../modeloMensagem/Requisicao";
import { Resposta } from "../modeloMensagem/Resposta";

export class Cliente {
  private socket: net.Socket;

  constructor() {
    this.socket = new net.Socket();
  }

  conectar(): Promise<boolean> {

    return new Promise((resolve, reject) => { 

        let buffer = "";

        const aoReceberDados = (dados: Buffer) => {

            buffer += dados.toString("utf8");

            const indice = buffer.indexOf("\n");

            if (indice === -1) {
                return;
            }

            const mensagemJson = buffer
                .slice(0, indice)
                .trim();

            try {

                const resposta = JSON.parse(mensagemJson);

                if (resposta.sucesso === false) {

                    console.error(
                        "Servidor recusou a conexão:",
                        resposta.mensagem
                    );

                    this.socket.removeListener("data", aoReceberDados);
                    resolve(false);
                    return;
                }

                console.log("Conexão aceita pelo servidor.");

                this.socket.removeListener("data", aoReceberDados);
                resolve(true);

            } catch (erro) {

                reject(erro);
            }
        };

        this.socket.once("error", reject);

        this.socket.on("data", aoReceberDados);

        this.socket.connect(5000, "localhost", () => {
            console.log("Conexão TCP estabelecida.");
        });
    });
}

  async enviarRequisicao(): Promise<void> {
    // Interface para leitura de entrada do usuário
    const r1 = readLine.createInterface({ 
      input: process.stdin,
      output: process.stdout,
    });


    const tipoDocumento = await this.perguntar(
      r1,
      "Digite o tipo de documento (CPF/CNPJ): ",
    );

    const documento = await this.perguntar(
      r1,
      "Digite o número do documento: ",
    );

    r1.close();

    const id = Math.floor(Math.random() * 999999) + 1;

    const requisicao: Requisicao = {
      versao: "1.0",
      id: id,
      tipoDocumento: tipoDocumento,
      documento: documento,
    };

    const mensagem = JSON.stringify(requisicao) + "\n";

    const tamanhoBytes = Buffer.byteLength(JSON.stringify(requisicao), "utf-8"); // calcula o tamanho da mensagem em bytes, sem considerar o caractere de quebra de linha

    if (tamanhoBytes > 1024) {
      console.log("Erro: mensagem excede 1024 bytes.");
      return;
    }

    this.socket.write(mensagem, "utf-8");
  }

  private perguntar(rl: readLine.Interface, pergunta: string): Promise<string> { 
    return new Promise((resolve) => {
      rl.question(pergunta, (resposta) => {
        resolve(resposta);
      });
    });
  }

  desconectar(): void {
    this.socket.end();
    console.log("Desconectado do servidor.");
  }

  receberResposta(): Promise<void> {
    return new Promise((resolve, reject) => {
      let buffer = "";
      const aoReceberDados = (dados: Buffer) => {
        buffer += dados.toString("utf-8");

        let index;
        while ((index = buffer.indexOf("\n")) !== -1) {
          const mensagem = buffer.slice(0, index).trim();
          
          buffer = buffer.slice(index + 1);

          if (mensagem.length === 0){
            continue;
          }

          try{
            const resposta: Resposta = JSON.parse(mensagem); 

            console.log("Resposta recebida do servidor:");
            console.log("===================================");
            if(resposta.versao !== undefined){
              console.log("Versão:",resposta.versao);
            }
            if(resposta.id !== undefined){
              console.log("ID:",resposta.id);
            }
            console.log("Sucesso:",resposta.sucesso);
            console.log("Código:", resposta.codigo);

            if (resposta.sucesso){
              if (resposta.valido !== undefined){
                console.log("Valido:", resposta.valido);
              }
            }


            if(resposta.mensagem !== undefined){
              console.log("Mensagem:", resposta.mensagem);
            }

            console.log("===================================");

            this.socket.removeListener("data", aoReceberDados);
            resolve();

            
          } catch (erro) {
            console.log("Erro ao processar a resposta:", erro);
            reject(erro);
          }
        }
      };
      this.socket.on("data", aoReceberDados);
      this.socket.on("error", reject);
    });
  }
}
