import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraSalario {

    // Constantes de Negócio (Facilita manutenção se as taxas mudarem)
    private static final BigDecimal TAXA_INSS = new BigDecimal("0.08");
    private static final BigDecimal TETO_INSS = new BigDecimal("500.00");
    private static final BigDecimal LIMITE_ISENCAO_IR = new BigDecimal("2000.00");
    private static final BigDecimal TAXA_IR = new BigDecimal("0.10");

    public BigDecimal calcularSalarioLiquido(BigDecimal salarioBruto) {
        validarSalario(salarioBruto);

        BigDecimal descontoINSS = calcularINSS(salarioBruto);
        BigDecimal descontoIRRF = calcularIRRF(salarioBruto);

        BigDecimal salarioLiquido = salarioBruto
                .subtract(descontoINSS)
                .subtract(descontoIRRF);

        return arredondar(salarioLiquido);
    }

    // Método isolado para validação
    private void validarSalario(BigDecimal salario) {
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O salário bruto deve ser maior que zero.");
        }
    }

    // Protected: Permite testes unitários diretos nesta regra (Testabilidade)
    protected BigDecimal calcularINSS(BigDecimal salarioBruto) {
        BigDecimal inssCalculado = salarioBruto.multiply(TAXA_INSS);

        if (inssCalculado.compareTo(TETO_INSS) > 0) {
            return TETO_INSS;
        }
        return arredondar(inssCalculado);
    }

    // Protected: Permite testes unitários diretos nesta regra (Testabilidade)
    protected BigDecimal calcularIRRF(BigDecimal salarioBruto) {
        if (salarioBruto.compareTo(LIMITE_ISENCAO_IR) <= 0) {
            return BigDecimal.ZERO;
        }

        // A regra diz: 10% sobre o valor TOTAL do salário bruto
        return arredondar(salarioBruto.multiply(TAXA_IR));
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
