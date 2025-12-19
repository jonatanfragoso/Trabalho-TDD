import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraSalario {

    // Constantes atualizadas conforme novas regras
    private static final BigDecimal TAXA_INSS = new BigDecimal("0.08");
    private static final BigDecimal TETO_INSS = new BigDecimal("500.00");
    
    private static final BigDecimal TAXA_VT = new BigDecimal("0.06");
    
    private static final BigDecimal LIMITE_ISENCAO_IR = new BigDecimal("2000.00");
    private static final BigDecimal LIMITE_FAIXA_1_IR = new BigDecimal("4000.00");
    private static final BigDecimal TAXA_IR_FAIXA_1 = new BigDecimal("0.10");
    private static final BigDecimal TAXA_IR_FAIXA_2 = new BigDecimal("0.20");
    
    private static final BigDecimal DEDUCAO_POR_DEPENDENTE = new BigDecimal("150.00");

    public BigDecimal calcularSalarioLiquido(BigDecimal salarioBruto, int numeroDependentes, boolean usaValeTransporte) {
        // Validações
        validarEntradas(salarioBruto, numeroDependentes);

        // Cálculos dos descontos
        BigDecimal descontoINSS = calcularINSS(salarioBruto);
        BigDecimal descontoVT = calcularValeTransporte(salarioBruto, usaValeTransporte);
        BigDecimal descontoIRRF = calcularIRRF(salarioBruto, numeroDependentes);

        // Cálculo Final
        BigDecimal salarioLiquido = salarioBruto
                .subtract(descontoINSS)
                .subtract(descontoVT)
                .subtract(descontoIRRF);

        return arredondar(salarioLiquido);
    }

    private void validarEntradas(BigDecimal salario, int dependentes) {
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O salário bruto deve ser maior que zero.");
        }
        if (dependentes < 0) {
            throw new IllegalArgumentException("O número de dependentes não pode ser negativo.");
        }
    }

    // Mantido (Regra não mudou, apenas constantes)
    protected BigDecimal calcularINSS(BigDecimal salarioBruto) {
        BigDecimal inssCalculado = salarioBruto.multiply(TAXA_INSS);

        if (inssCalculado.compareTo(TETO_INSS) > 0) {
            return TETO_INSS;
        }
        return arredondar(inssCalculado);
    }

    // Nova Lógica: Vale Transporte
    protected BigDecimal calcularValeTransporte(BigDecimal salarioBruto, boolean usaValeTransporte) {
        if (usaValeTransporte) {
            return arredondar(salarioBruto.multiply(TAXA_VT));
        }
        return BigDecimal.ZERO;
    }

    // Lógica Atualizada: IRRF Progressivo + Dependentes
    protected BigDecimal calcularIRRF(BigDecimal salarioBruto, int numeroDependentes) {
        BigDecimal valorBaseIR = BigDecimal.ZERO;

        // Definição da alíquota baseada nas faixas
        if (salarioBruto.compareTo(LIMITE_ISENCAO_IR) <= 0) {
            return BigDecimal.ZERO; // Isento
        } else if (salarioBruto.compareTo(LIMITE_FAIXA_1_IR) <= 0) {
            valorBaseIR = salarioBruto.multiply(TAXA_IR_FAIXA_1); // 10%
        } else {
            valorBaseIR = salarioBruto.multiply(TAXA_IR_FAIXA_2); // 20%
        }

        // Cálculo do abatimento por dependentes
        BigDecimal totalDeducao = DEDUCAO_POR_DEPENDENTE.multiply(new BigDecimal(numeroDependentes));
        
        // Aplicação do abatimento
        BigDecimal irFinal = valorBaseIR.subtract(totalDeducao);

        // O imposto não pode ser negativo
        if (irFinal.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return arredondar(irFinal);
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
