import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Responsável pelo cálculo de salário líquido seguindo regras tributárias estritas.
 * Foco: Precisão Decimal (BigDecimal) e Isolamento de Regras (Métodos Protected).
 */
public class CalculadoraSalario {

    // --- Constantes de Configuração (Facilita manutenção de alíquotas) ---
    private static final BigDecimal TAXA_INSS = new BigDecimal("0.08");
    private static final BigDecimal TETO_INSS = new BigDecimal("500.00");

    private static final BigDecimal TAXA_VT = new BigDecimal("0.06");

    private static final BigDecimal IR_LIMITE_ISENCAO = new BigDecimal("2000.00");
    private static final BigDecimal IR_LIMITE_FAIXA_1 = new BigDecimal("4000.00");
    private static final BigDecimal IR_TAXA_FAIXA_1 = new BigDecimal("0.10");
    private static final BigDecimal IR_TAXA_FAIXA_2 = new BigDecimal("0.20");
    private static final BigDecimal IR_DEDUCAO_DEPENDENTE = new BigDecimal("150.00");

    /**
     * Método principal (Facade) que orquestra o fluxo de cálculo.
     */
    public BigDecimal calcularSalarioLiquido(BigDecimal salarioBruto, int dependentes, boolean usaValeTransporte) {
        validarEntradas(salarioBruto, dependentes);

        BigDecimal descontoINSS = calcularINSS(salarioBruto);
        BigDecimal descontoVT = calcularValeTransporte(salarioBruto, usaValeTransporte);
        
        // O cálculo do IR é composto (Bruto - Deduções), orquestrado aqui ou em método próprio
        BigDecimal irBruto = calcularImpostoRendaBruto(salarioBruto);
        BigDecimal descontoIRFinal = aplicarDeducaoDependentes(irBruto, dependentes);

        BigDecimal salarioLiquido = salarioBruto
                .subtract(descontoINSS)
                .subtract(descontoVT)
                .subtract(descontoIRFinal);

        return arredondar(salarioLiquido);
    }

    private void validarEntradas(BigDecimal salario, int dependentes) {
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Erro: O salário bruto deve ser positivo.");
        }
        if (dependentes < 0) {
            throw new IllegalArgumentException("Erro: O número de dependentes não pode ser negativo.");
        }
    }

    // --- Regras de Negócio Isoladas (Protected para Testabilidade) ---

    protected BigDecimal calcularINSS(BigDecimal salarioBruto) {
        BigDecimal calculado = salarioBruto.multiply(TAXA_INSS);
        
        if (calculado.compareTo(TETO_INSS) > 0) {
            return TETO_INSS;
        }
        return arredondar(calculado);
    }

    protected BigDecimal calcularValeTransporte(BigDecimal salarioBruto, boolean optouPorVT) {
        if (!optouPorVT) {
            return BigDecimal.ZERO;
        }
        return arredondar(salarioBruto.multiply(TAXA_VT));
    }

    /**
     * Calcula apenas a taxa base do IR antes das deduções.
     * Isso permite testar as faixas (10% vs 20%) isoladamente.
     */
    protected BigDecimal calcularImpostoRendaBruto(BigDecimal salarioBruto) {
        if (salarioBruto.compareTo(IR_LIMITE_ISENCAO) <= 0) {
            return BigDecimal.ZERO;
        } 
        
        if (salarioBruto.compareTo(IR_LIMITE_FAIXA_1) <= 0) {
            return arredondar(salarioBruto.multiply(IR_TAXA_FAIXA_1));
        } 
        
        return arredondar(salarioBruto.multiply(IR_TAXA_FAIXA_2));
    }

    /**
     * Aplica a dedução de dependentes sobre o imposto bruto.
     * Garante que o imposto nunca fique negativo.
     */
    protected BigDecimal aplicarDeducaoDependentes(BigDecimal irBruto, int dependentes) {
        BigDecimal totalDeducao = IR_DEDUCAO_DEPENDENTE.multiply(new BigDecimal(dependentes));
        BigDecimal irLiquido = irBruto.subtract(totalDeducao);

        if (irLiquido.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return irLiquido;
    }

    // Centraliza a regra de arredondamento para garantir consistência no sistema todo
    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
