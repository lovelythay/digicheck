package modeloMensagem;

public class Requisicao {

    private String versao;
    private Long id;
    private String tipoDocumento;
    private String documento;

    public String getVersao(){
        return this.versao;
    }

    public Long getId(){
        return this.id;
    }

    public String getTipoDocumento(){
        return this.tipoDocumento;
    }

    public String getDocumento(){
        return this.documento;
    }

    public void setVersao(String versao){
        this.versao = versao;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setTipoDocumento(String tipoDocumento){
        this.tipoDocumento = tipoDocumento;
    }

    public void setDocumento(String documento){
        this.documento = documento;
    }

}
