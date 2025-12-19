import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraSalarioTest {

    // Instância da classe que será testada (ainda a ser implementada)
    CalculadoraSalario calculadora = new CalculadoraSalario();

    @Test
    @DisplayName("Deve lançar exceção para salário igual a zero")
    void deveLancarExcecaoParaSalarioZero() {
        BigDecimal salarioBruto = BigDecimal.ZERO;

        assertThrows(IllegalArgumentException.class, () -> {
            calculadora.calcularSalarioLiquido(salarioBruto);
        }, "Salário igual a zero deve gerar erro");
    }

    @Test
    @DisplayName("Deve lançar exceção para salário negativo")
    void deveLancarExcecaoParaSalarioNegativo() {
        BigDecimal salarioBruto = new BigDecimal("-100.00");

        assertThrows(IllegalArgumentException.class, () -> {
            calculadora.calcularSalarioLiquido(salarioBruto);
        }, "Salário negativo deve gerar erro");
    }

    @Test
    @DisplayName("Cenário 1: Salário baixo (Isento IRRF + INSS 8%)")
    void deveCalcularLiquidoParaSalarioBaixoSemImpostoDeRenda() {
        // Cenário:
        // Bruto: 1000.00
        // INSS (8%): 80.00 (Não atinge teto de 500)
        // IRRF: Isento (<= 2000)
        // Líquido esperado: 1000 - 80 - 0 = 920.00

        BigDecimal salarioBruto = new BigDecimal("1000.00");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("920.00");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto);

        assertEquals(salarioLiquidoEsperado, resultado);
    }

    @Test
    @DisplayName("Cenário 2: Salário no limite da isenção do IRRF")
    void deveCalcularLiquidoNoLimiteDeIsencaoDeIR() {
        // Cenário:
        // Bruto: 2000.00
        // INSS (8%): 160.00
        // IRRF: Isento (Até 2000 inclusive)
        // Líquido esperado: 2000 - 160 - 0 = 1840.00

        BigDecimal salarioBruto = new BigDecimal("2000.00");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("1840.00");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto);

        assertEquals(salarioLiquidoEsperado, resultado);
    }

    @Test
    @DisplayName("Cenário 3: Salário com IRRF e INSS sem teto")
    void deveCalcularLiquidoComImpostoDeRendaEINSSSemTeto() {
        // Cenário:
        // Bruto: 2500.00
        // INSS (8%): 200.00 (8% de 2500 é 200, menor que teto 500)
        // IRRF (> 2000): 10% sobre o total bruto = 250.00
        // Líquido esperado: 2500 - 200 - 250 = 2050.00

        BigDecimal salarioBruto = new BigDecimal("2500.00");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("2050.00");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto);

        assertEquals(salarioLiquidoEsperado, resultado);
    }

    @Test
    @DisplayName("Cenário 4: Salário alto (INSS no Teto + IRRF)")
    void deveCalcularLiquidoComTetoINSSEImpostoDeRenda() {
        // Cenário:
        // Bruto: 10000.00
        // INSS (8%): 800.00 -> TETO APLICADO -> 500.00
        // IRRF (> 2000): 10% sobre o total bruto = 1000.00
        // Líquido esperado: 10000 - 500 - 1000 = 8500.00

        BigDecimal salarioBruto = new BigDecimal("10000.00");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("8500.00");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto);

        assertEquals(salarioLiquidoEsperado, resultado);
    }

    @Test
    @DisplayName("Deve garantir arredondamento correto (2 casas decimais)")
    void deveArredondarCorretamente() {
        // Cenário para testar dízimas ou arredondamentos
        // Bruto: 1000.55
        // INSS (8%): 80.044 -> Arredonda -> 80.04
        // IRRF: 0
        // Líquido: 1000.55 - 80.04 = 920.51

        BigDecimal salarioBruto = new BigDecimal("1000.55");
        BigDecimal salarioLiquidoEsperado = new BigDecimal("920.51");

        BigDecimal resultado = calculadora.calcularSalarioLiquido(salarioBruto);

        assertEquals(salarioLiquidoEsperado, resultado);
    }
}
