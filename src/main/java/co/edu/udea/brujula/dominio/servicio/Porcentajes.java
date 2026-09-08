package co.edu.udea.brujula.dominio.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Las historias piden porcentajes con máximo dos decimales (HU-019 CA-11). */
public final class Porcentajes {

    private Porcentajes() {
    }

    public static BigDecimal de(long parte, long total) {
        if (total <= 0) return cero();
        return BigDecimal.valueOf(parte)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
