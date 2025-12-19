import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraSalario {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;

    // =====================
    // Constantes de negócio
    // =====================
    private static final BigDecimal INSS_PERCENTUAL = new BigDecimal("0.08");
    private static final BigDecimal INSS_TETO = new BigDecimal("500.00");

    private static final BigDecimal VT_PERCENTUAL = new BigDecimal("0.06");

    private static final BigDecimal IR_10 = new BigDecimal("0.10");
    private static final BigDecimal IR_20 = new BigDecimal("0.20");

    private static final BigDecimal LIMITE_FAIXA_1 = new BigDecimal("2000.00");
    private static final BigDecimal LIMITE_FAIXA_2 = new BigDecimal("4000.00");

    private static final BigDecimal DEDUCAO_DEPENDENTE = new BigDecimal("150.00");

    // =====================
    // API pública
    // =====================

    public BigDecimal calcularSalarioLiquido(BigDecimal salarioBruto, int dependentes, boolean optouVT) {
        validarSalario(salarioBruto);

        BigDecimal inss = calcularINSS(salarioBruto);
        BigDecimal vt = calcularValeTransporte(salarioBruto, optouVT);
        BigDecimal irLiquido = calcularIRComDependentes(salarioBruto, dependentes);

        return salarioBruto
                .subtract(inss)
                .subtract(vt)
                .subtract(irLiquido)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularINSS(BigDecimal salarioBruto) {
        return aplicarTeto(
                salarioBruto.multiply(INSS_PERCENTUAL),
                INSS_TETO
        );
    }

    public BigDecimal calcularValeTransporte(BigDecimal salarioBruto, boolean optouVT) {
        return optouVT
                ? calcularPercentual(salarioBruto, VT_PERCENTUAL)
                : ZERO;
    }

    public BigDecimal calcularImpostoRendaBruto(BigDecimal salarioBruto) {
        return calcularPercentual(salarioBruto, percentualIR(salarioBruto));
    }

    public BigDecimal aplicarDeducaoDependentes(BigDecimal impostoBruto, int dependentes) {
        BigDecimal deducao = DEDUCAO_DEPENDENTE.multiply(BigDecimal.valueOf(dependentes));
        return maxZero(impostoBruto.subtract(deducao));
    }

    // =====================
    // Métodos privados (baixo acoplamento e baixa complexidade)
    // =====================

    private BigDecimal calcularIRComDependentes(BigDecimal salarioBruto, int dependentes) {
        BigDecimal irBruto = calcularImpostoRendaBruto(salarioBruto);
        return aplicarDeducaoDependentes(irBruto, dependentes);
    }

    private BigDecimal percentualIR(BigDecimal salarioBruto) {
        if (salarioBruto.compareTo(LIMITE_FAIXA_2) > 0) return IR_20;
        if (salarioBruto.compareTo(LIMITE_FAIXA_1) > 0) return IR_10;
        return ZERO;
    }

    private BigDecimal calcularPercentual(BigDecimal valor, BigDecimal percentual) {
        return valor
                .multiply(percentual)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal aplicarTeto(BigDecimal valor, BigDecimal teto) {
        return min(valor, teto).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal validarSalario(BigDecimal salario) {
        if (salario.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Salário inválido");
        }
        return salario;
    }

    private BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    private BigDecimal maxZero(BigDecimal valor) {
        return valor.compareTo(ZERO) < 0 ? ZERO : valor.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
