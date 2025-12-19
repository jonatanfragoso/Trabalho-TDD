import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraSalarioTest {

    CalculadoraSalario calculadora = new CalculadoraSalario();

    // --- TESTES DE INTEGRAÇÃO (O fluxo completo) ---

    @Test
    @DisplayName("Integração: Deve calcular líquido corretamente (Cenário Padrão)")
    void deveCalcularLiquidoCorretamente() {
        BigDecimal salarioBruto = new BigDecimal("2500.00");
        // INSS: 200.00 | IRRF: 250.00 | Liq: 2050.00
        BigDecimal esperado = new BigDecimal("2050.00");
        assertEquals(esperado, calculadora.calcularSalarioLiquido(salarioBruto));
    }

    @Test
    @DisplayName("Validação: Deve lançar exceção para salário zero")
    void deveValidarSalarioZero() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularSalarioLiquido(BigDecimal.ZERO));
    }

    // --- TESTES UNITÁRIOS ISOLADOS (Novos, graças à refatoração) ---

    @Test
    @DisplayName("Unidade INSS: Deve aplicar teto de 500,00")
    void deveAplicarTetoINSS() {
        BigDecimal salarioAlto = new BigDecimal("10000.00");
        BigDecimal inss = calculadora.calcularINSS(salarioAlto);

        // Verifica APENAS a regra do INSS, sem ruído do IRRF
        assertEquals(new BigDecimal("500.00"), inss);
    }

    @Test
    @DisplayName("Unidade INSS: Deve calcular 8% exato")
    void deveCalcularPorcentagemINSS() {
        BigDecimal salario = new BigDecimal("1000.00");
        BigDecimal inss = calculadora.calcularINSS(salario);

        assertEquals(new BigDecimal("80.00"), inss);
    }

    @Test
    @DisplayName("Unidade IRRF: Deve ser isento até 2000")
    void deveIsentarIRRF() {
        BigDecimal salario = new BigDecimal("2000.00");
        BigDecimal irrf = calculadora.calcularIRRF(salario);

        assertEquals(BigDecimal.ZERO, irrf);
    }

    @Test
    @DisplayName("Unidade IRRF: Deve taxar 10% sobre total bruto acima de 2000")
    void deveTaxarIRRF() {
        BigDecimal salario = new BigDecimal("2500.00");
        BigDecimal irrf = calculadora.calcularIRRF(salario);

        assertEquals(new BigDecimal("250.00"), irrf);
    }
}
