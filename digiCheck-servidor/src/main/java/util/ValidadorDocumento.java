package util;

import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;

public class ValidadorDocumento {
    
    public static boolean isCpfValido(String cpf) {
        CPFValidator validator = new CPFValidator();
        try {
            validator.assertValid(cpf);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCnpjValido(String cnpj) {
        CNPJValidator validator = new CNPJValidator();
        try {
            validator.assertValid(cnpj);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
