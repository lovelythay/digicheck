import { Cliente } from "./services/Cliente";

async function main() {
  const cliente = new Cliente();
  try {
    const conectado = await cliente.conectar();

    if (!conectado) {
      cliente.desconectar();
      return;
    }

    await cliente.enviarRequisicao();
    await cliente.receberResposta();

    cliente.desconectar();
  } catch (err) {
    console.error("Erro ao conectar:", err);
  }
}

main();
