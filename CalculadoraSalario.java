import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraSalario {

    public BigDecimal calcularSalarioLiquido(BigDecimal salarioBruto) {
        // Regra: Salários iguais ou inferiores a zero geram exceção
        if (salarioBruto == null || salarioBruto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O salário bruto deve ser maior que zero.");
        }

        // Regra INSS: 8% sobre o bruto
        BigDecimal descontoINSS = salarioBruto.multiply(new BigDecimal("0.08"));
        
        // Regra INSS: Teto máximo de R$ 500,00
        BigDecimal tetoINSS = new BigDecimal("500.00");
        if (descontoINSS.compareTo(tetoINSS) > 0) {
            descontoINSS = tetoINSS;
        }

        // Regra IRRF: Isento até 2000, 10% sobre o total se maior que 2000
        BigDecimal descontoIRRF = BigDecimal.ZERO;
        BigDecimal limiteIsencaoIR = new BigDecimal("2000.00");

        if (salarioBruto.compareTo(limiteIsencaoIR) > 0) {
            descontoIRRF = salarioBruto.multiply(new BigDecimal("0.10"));
        }

        // Cálculo Final: Bruto - INSS - IRRF
        BigDecimal salarioLiquido = salarioBruto
                .subtract(descontoINSS)
                .subtract(descontoIRRF);

        // Regra: Arredondamento para duas casas decimais
        return salarioLiquido.setScale(2, RoundingMode.HALF_UP);
    }
}
