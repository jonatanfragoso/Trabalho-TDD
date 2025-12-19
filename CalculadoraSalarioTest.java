import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraSalarioTest {

    CalculadoraSalario calculadora = new CalculadoraSalario();

    // ==========================================
    // TESTES UNITÁRIOS: REGRAS ISOLADAS
    // ==========================================

    @Test
    @DisplayName("Regra INSS: Deve respeitar o teto de R$ 500,00")
    void testeUnitarioINSSComTeto() {
        // Bruto 10k * 8% = 800 -> Teto 500
        BigDecimal inss = calculadora.calcularINSS(new BigDecimal("10000.00"));
        assertEquals(new BigDecimal("500.00"), inss);
    }

    @Test
    @DisplayName("Regra VT: Deve ser zero se funcionário não optar")
    void testeUnitarioVTSemOpcao() {
        BigDecimal vt = calculadora.calcularValeTransporte(new BigDecimal("1000.00"), false);
        assertEquals(BigDecimal.ZERO, vt);
    }

    @Test
    @DisplayName("Regra IR (Faixas): Deve aplicar 10% para salários entre 2k e 4k")
    void testeUnitarioFaixa1IR() {
        // 2500 * 10% = 250.00
        BigDecimal irBruto = calculadora.calcularImpostoRendaBruto(new BigDecimal("2500.00"));
        assertEquals(new BigDecimal("250.00"), irBruto);
    }

    @Test
    @DisplayName("Regra IR (Faixas): Deve aplicar 20% para salários acima de 4k")
    void testeUnitarioFaixa2IR() {
        // 5000 * 20% = 1000.00
        BigDecimal irBruto = calculadora.calcularImpostoRendaBruto(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("1000.00"), irBruto);
    }

    @Test
    @DisplayName("Regra Dependentes: Imposto não pode ficar negativo")
    void testeUnitarioDeducaoNegativa() {
        // Imposto Devido: 100.00 | 1 Filho (150.00 dedução) -> Resultado deve ser 0.00, não -50.00
        BigDecimal irBruto = new BigDecimal("100.00");
        BigDecimal irFinal = calculadora.aplicarDeducaoDependentes(irBruto, 1);
        
        assertEquals(BigDecimal.ZERO, irFinal);
    }

    // ==========================================
    // TESTES DE INTEGRAÇÃO: CÁLCULO FINAL
    // ==========================================

    @Test
    @DisplayName("Integração: Cenário Completo (Teto INSS + IR 20% + VT + Dependentes)")
    void testeIntegracaoCenarioComplexo() {
        // Cenário:
        // Salário: 5000.00
        // 1. INSS (8% de 5000 = 400.00). (Menor que teto 500) -> Desconto: 400.00
        // 2. VT (Sim = 6% de 5000) -> Desconto: 300.00
        // 3. IR Bruto (> 4000 = 20% de 5000) -> 1000.00
        // 4. Dependentes (2 * 150 = 300 dedução) -> IR Liq: 700.00
        // ---------------------------------------------------
        // Líquido = 5000 - 400 - 300 - 700 = 3600.00
        
        BigDecimal salarioBruto = new BigDecimal("5000.00");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("3600.00");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto, 2, true);

        assertEquals(salarioLiquidoEsperado, resultado);
    }

    @Test
    @DisplayName("Validação: Salário zero deve lançar exceção")
    void testeValidacaoSalario() {
        assertThrows(IllegalArgumentException.class, 
            () -> calculadora.calcularSalarioLiquido(BigDecimal.ZERO, 0, false));
    }
}
