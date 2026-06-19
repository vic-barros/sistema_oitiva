package agenda_oitiva.model;
import java.util.Objects;

public abstract class Pessoa {

    private int idPessoa; // 1. ADICIONADO: Atributo para bater com a PK do seu DER
    private String nome;
    private String cpf;
    
    public abstract TipoPessoa getTipoDePessoa();
    
    // 2. ADICIONADO: Construtor vazio necessário para o DAO instanciar as classes filhas
    public Pessoa() {
    }
    
    public Pessoa(String nome, String cpf) {
        this.nome = nome;
        setCpf(cpf);
    }

    public int getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(int idPessoa) {
        this.idPessoa = idPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if(nome == null || nome.strip().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido!");
        } else {
            this.nome = nome;
        }
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpfSujo) {
    	
    	if(cpfSujo == null) {
    		this.cpf = null;
    		return;
    	}
        String cpfLimpo = cpfSujo.replaceAll("\\D", "");
        
        if(cpfLimpo.isEmpty()){
        	this.cpf = null;
        	return;
        }
        
        if(cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("Tamanho de CPF Inválido!");
        }
        //DV1
        int somaDv1 = 0;
        for(int i = 0; i < 9; i++) {
            int num = Character.getNumericValue(cpfLimpo.charAt(i));
            somaDv1 = somaDv1 + num * (10 - i);
        }
        
        int resto1 = somaDv1 % 11;
        int dv1;
        if(resto1 < 2) {
            dv1 = 0;
        } else {
            dv1 = 11 - resto1;
        }
        
        //DV2
        int somaDv2 = 0;
        for(int i = 0; i < 10; i++) {
            int num = Character.getNumericValue(cpfLimpo.charAt(i));
            somaDv2 = somaDv2 + num * (11 - i);
        }
        
        int resto2 = somaDv2 % 11;
        int dv2;
        if(resto2 < 2) {
            dv2 = 0;
        } else {
            dv2 = 11 - resto2;
        }
        
        //Verificação Final
        if(dv1 == Character.getNumericValue(cpfLimpo.charAt(9))
                && dv2 == Character.getNumericValue(cpfLimpo.charAt(10))) {
            this.cpf = cpfLimpo;
        } else {
            throw new IllegalArgumentException("CPF Inválido");
        }
    }

    @Override
    public String toString() {
        return "ID = " + idPessoa + " NOME = " + nome + " CPF = " + cpf;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf, nome, idPessoa);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pessoa other = (Pessoa) obj;
        return Objects.equals(cpf, other.cpf) && Objects.equals(nome, other.nome) && idPessoa == other.idPessoa;
    }
}