package co.edu.udea.brujula.infraestructura.salida.correo;

public final class MensajeDeRecuperacion {

    private static final String ASUNTO = "Brújula · Restablece tu contraseña";

    public record Mensaje(String enlace, String asunto, String texto, String html) {
    }

    private MensajeDeRecuperacion() {
    }

    public static Mensaje paraEnlace(String frontendUrl, String token, int minutosDeVigencia) {
        String enlace = frontendUrl.replaceAll("/+$", "") + "/restablecer?token=" + token;
        String texto = """
                Hola,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en Brújula.
                Abre este enlace para definir una nueva contraseña (vigente por %d minutos y de un solo uso):

                %s

                Si no hiciste esta solicitud, ignora este mensaje: tu contraseña no cambiará.
                """.formatted(minutosDeVigencia, enlace);
        String html = """
                <p>Hola,</p>
                <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en Brújula.
                Abre este enlace para definir una nueva contraseña (vigente por %d minutos y de un solo uso):</p>
                <p><a href="%s">%s</a></p>
                <p>Si no hiciste esta solicitud, ignora este mensaje: tu contraseña no cambiará.</p>
                """.formatted(minutosDeVigencia, enlace, enlace);
        return new Mensaje(enlace, ASUNTO, texto, html);
    }
}
