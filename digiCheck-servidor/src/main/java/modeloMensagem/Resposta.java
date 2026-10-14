package modeloMensagem;

public class Resposta {
    private String versao;
    private Long id;
    private boolean sucesso;
    private boolean valido;
    private int codigo;
    private String mensagem;

    public String getVersao() {
        return this.versao;
    }
    
    public Long getId() {
        return id;
    }

    public boolean isSucesso() {
        return this.sucesso;
    }

    public boolean isValido() {
        return this.valido;
    }

    public int getCodigo() {
        return this.codigo;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String toString() {
        return "Resposta{" +
                "versao='" + versao + '\'' +
                ", id=" + id +
                ", sucesso=" + sucesso +
                ", valido=" + valido +
                ", mensagem='" + mensagem + '\'' +
                '}';
    }
}
