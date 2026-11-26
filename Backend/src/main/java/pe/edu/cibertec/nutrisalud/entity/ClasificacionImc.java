package pe.edu.cibertec.nutrisalud.entity;

import java.math.BigDecimal;

public enum ClasificacionImc {
    BAJO_PESO, NORMAL, SOBREPESO, OBESIDAD;

    private static final BigDecimal MINIMO_NORMAL = new BigDecimal("18.50");
    private static final BigDecimal MINIMO_SOBREPESO = new BigDecimal("25");
    private static final BigDecimal MINIMO_OBESIDAD = new BigDecimal("30");

    public static ClasificacionImc segunImc(BigDecimal imc) {
        if (imc.compareTo(MINIMO_NORMAL) < 0) {
            return BAJO_PESO;
        }
        if (imc.compareTo(MINIMO_SOBREPESO) < 0) {
            return NORMAL;
        }
        if (imc.compareTo(MINIMO_OBESIDAD) < 0) {
            return SOBREPESO;
        }
        return OBESIDAD;
    }
}
