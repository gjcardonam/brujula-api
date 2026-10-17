package co.edu.udea.brujula.dominio.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Porcentajes {

    private static final int DECIMALES = 2;

    private Porcentajes() {
    }

    public static BigDecimal de(long parte, long total) {
        if (total <= 0) return cero();
        return BigDecimal.valueOf(parte)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), DECIMALES, RoundingMode.HALF_UP);
    }

    public static BigDecimal redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    public static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(DECIMALES, RoundingMode.HALF_UP);
    }
}
