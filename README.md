# DigiCheck

O DigiCheck é uma aplicação cliente-servidor para validar números de CPF e CNPJ. O cliente envia uma requisição ao servidor por meio de uma conexão TCP, e o servidor responde com o resultado da validação.

> Projeto desenvolvido como atividade da disciplina de **Sistemas Distribuídos**.

## Tecnologias

- **Cliente:** TypeScript e Node.js
- **Servidor:** Java e Maven
- **Comunicação:** TCP com mensagens JSON delimitadas por quebra de linha
- **Porta do servidor:** `5000`


## Pré-requisitos

- JDK 24
- Maven
- Node.js e npm
- Visual Studio Code com a extensão **Extension Pack for Java** (opcional, caso utilize o VS Code para executar o servidor)

## Como executar

Inicie primeiro o servidor. Em um terminal, na raiz do projeto:

```bash
cd digiCheck-servidor
mvn clean install
```

Depois de compilado, inicie a classe `Principal` pela sua IDE ou pelo comando:

```bash
mvn exec:java -Dexec.mainClass=Principal
```

O servidor fica aguardando conexões na porta `5000`.

Em outro terminal, inicie o cliente:

```bash
cd digiCheck-client
npm install
npm run dev
```

Quando solicitado, informe o tipo de documento (`CPF` ou `CNPJ`) e o número que deseja validar. O cliente exibe a resposta recebida do servidor.

## Estrutura do projeto

```text
DigiCheck/
├── digiCheck-client/    # Cliente TypeScript
├── digiCheck-servidor/  # Servidor Java
└── README.md
```
