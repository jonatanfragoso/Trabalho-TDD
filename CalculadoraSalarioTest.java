import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraSalarioTest {

    CalculadoraSalario calculadora = new CalculadoraSalario();

    // --- TESTES DE VALIDAÇÃO (Erros e Exceções) ---

    @Test
    @DisplayName("Deve lançar exceção para salário bruto inválido (Zero)")
    void deveLancarExcecaoSalarioZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculadora.calcularSalarioLiquido(BigDecimal.ZERO, 0, false);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção para número de dependentes negativo")
    void deveLancarExcecaoDependentesNegativos() {
        BigDecimal salario = new BigDecimal("1000.00");
        assertThrows(IllegalArgumentException.class, () -> {
            // Assinatura esperada: salario, numeroDependentes, usaValeTransporte
            calculadora.calcularSalarioLiquido(salario, -1, false);
        });
    }

    // --- TESTES DE REGRAS ISOLADAS (Novos Parâmetros) ---

    @Test
    @DisplayName("Vale Transporte: Deve descontar 6% quando ativo")
    void deveDescontarValeTransporte() {
        // Cenário:
        // Bruto: 1000.00
        // INSS (8%): 80.00
        // IRRF (< 2000): 0.00
        // VT (True - 6%): 60.00
        // Líquido: 1000 - 80 - 0 - 60 = 860.00
        
        BigDecimal salario = new BigDecimal("1000.00");
        BigDecimal esperado = new BigDecimal("860.00");
        
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 0, true);
        
        assertEquals(esperado, resultado);
    }
    
    @Test
    @DisplayName("Vale Transporte: Não deve descontar quando inativo")
    void naoDeveDescontarValeTransporte() {
        // Cenário idêntico ao anterior, mas sem VT
        // Líquido: 1000 - 80 - 0 - 0 = 920.00
        
        BigDecimal salario = new BigDecimal("1000.00");
        BigDecimal esperado = new BigDecimal("920.00");
        
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 0, false);
        
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("IRRF Progressivo: Faixa 20% (Acima de 4000) sem dependentes")
    void deveCalcularIRRF20Porcento() {
        // Cenário:
        // Bruto: 5000.00
        // INSS (8% de 5000 = 400): 400.00 (não bate no teto de 500)
        // IRRF (> 4000 - 20%): 1000.00
        // VT (False): 0.00
        // Líquido: 5000 - 400 - 1000 = 3600.00
        
        BigDecimal salario = new BigDecimal("5000.00");
        BigDecimal esperado = new BigDecimal("3600.00");
        
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 0, false);
        
        assertEquals(esperado, resultado);
    }

    // --- TESTES DE DEPENDENTES (Dedução de IR) ---

    @Test
    @DisplayName("Dependentes: Deve abater R$ 150,00 do IR por dependente")
    void deveAbaterIRPorDependente() {
        // Cenário:
        // Bruto: 3000.00
        // INSS (8%): 240.00
        // IRRF Base (Faixa 2000-4000 é 10%): 300.00
        // Dependentes (1): -150.00
        // IRRF Final: 150.00
        // Líquido: 3000 - 240 - 150 = 2610.00
        
        BigDecimal salario = new BigDecimal("3000.00");
        BigDecimal esperado = new BigDecimal("2610.00");
        
        // Passando 1 dependente
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 1, false);
        
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("Dependentes: IRRF não pode ser negativo (Dedução maior que o imposto)")
    void irrfNaoPodeSerNegativo() {
        // Cenário:
        // Bruto: 3000.00
        // INSS (8%): 240.00
        // IRRF Base (10%): 300.00
        // Dependentes (3 * 150): 450.00 de desconto
        // Cálculo IR: 300 - 450 = -150 -> DEVE ZERAR
        // Líquido: 3000 - 240 - 0 = 2760.00
        
        BigDecimal salario = new BigDecimal("3000.00");
        BigDecimal esperado = new BigDecimal("2760.00");
        
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 3, false);
        
        assertEquals(esperado, resultado);
    }

    // --- TESTE DE INTEGRAÇÃO COMPLEXO (Todas as regras) ---

    @Test
    @DisplayName("Integração: Salário Alto + Teto INSS + VT + Dependentes + IR 20%")
    void deveCalcularCenarioComplexo() {
        // Cenário:
        // Bruto: 7000.00
        // INSS (8% = 560): Teto aplicado -> 500.00
        // VT (True - 6%): 420.00
        // IRRF Base (> 4000 - 20%): 1400.00
        // Dependentes (2 * 150): -300.00
        // IRRF Final: 1100.00
        
        // Líquido: 7000 - 500(INSS) - 420(VT) - 1100(IR) = 4980.00
        
        BigDecimal salario = new BigDecimal("7000.00");
        BigDecimal esperado = new BigDecimal("4980.00");
        
        BigDecimal resultado = calculadora.calcularSalarioLiquido(salario, 2, true);
        
        assertEquals(esperado, resultado);
    }
}
