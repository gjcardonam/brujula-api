#!/usr/bin/env python3
"""
Dibuja la vista de puertos y adaptadores de Brújula.

Mermaid no dibuja el hexágono clásico de esta arquitectura, así que este diagrama se arma a mano.
Para cambiarlo basta con editar las listas de PUERTOS_ENTRADA, PUERTOS_SALIDA y los adaptadores.

    python3 docs/generar_hexagono.py        -> docs/imagenes/hexagono.svg
"""

import math
from pathlib import Path

SALIDA = Path(__file__).resolve().parent / "imagenes" / "hexagono.svg"

ANCHO, ALTO = 1500, 930
CENTRO_X, CENTRO_Y = 750, 470
RADIO_APLICACION = 300
RADIO_DOMINIO = 190

AZUL = "#1e3a5f"
AMBAR = "#f59e0b"
GRIS_BORDE = "#9ca3af"
GRIS_TEXTO = "#4b5563"
FONDO_APP = "#dbeafe"
FONDO_DOMINIO = "#fef3c7"
FONDO_CAJA = "#ffffff"

PUERTOS_ENTRADA = [
    "RegistrarEstudiante",
    "IniciarSesion",
    "ValidarSesion",
    "ActualizarPerfil",
    "ListarEjercicios",
    "AbrirEjercicio",
    "RegistrarIntento",
    "CrearEjercicio",
]

PUERTOS_SALIDA = [
    "UsuarioRepositorio",
    "EjercicioRepositorio",
    "IntentoRepositorio",
    "SesionRepositorio",
    "ProveedorDeTokens",
    "CifradorDeContrasenas",
    "NotificadorDeCorreo",
    "Reloj",
]

ADAPTADORES_ENTRADA = [
    ("Controladores REST", "autenticación, recuperación, perfil,\nejercicios, intentos, catálogos, archivos"),
    ("Filtro de autenticación", "valida el token en cada petición"),
    ("Carga al arrancar", "administrador inicial\ny datos de ejemplo"),
]

ADAPTADORES_SALIDA = [
    ("PostgreSQL con JPA", "entidades, Spring Data\ny adaptadores de repositorio"),
    ("JWT y BCrypt", "tokens de sesión y de registro,\ncifrado de contraseñas"),
    ("Correo SMTP", "enlace de restablecimiento"),
    ("Google Identity Services", "verificación del ID token"),
    ("Disco y reloj", "imágenes de ejercicios\ny hora del servidor"),
]


def vertices(radio):
    """Hexágono con los vértices a izquierda y derecha, para que los lados queden verticales."""
    return [
        (CENTRO_X + radio * math.cos(math.radians(angulo)),
         CENTRO_Y + radio * math.sin(math.radians(angulo)))
        for angulo in range(0, 360, 60)
    ]


def puntos(radio):
    return " ".join(f"{x:.1f},{y:.1f}" for x, y in vertices(radio))


def sobre_el_borde(fraccion, lado):
    """
    Ubica un punto sobre el borde del hexágono de aplicación.
    El lado izquierdo recorre el tramo superior, el vértice y el tramo inferior.
    """
    v = vertices(RADIO_APLICACION)
    tramo = [v[2], v[3], v[4]] if lado == "izquierdo" else [v[5], v[0], v[1]]
    posicion = fraccion * 2
    indice = 0 if posicion < 1 else 1
    t = posicion - indice
    inicio, fin = tramo[indice], tramo[indice + 1]
    return inicio[0] + (fin[0] - inicio[0]) * t, inicio[1] + (fin[1] - inicio[1]) * t


def texto(x, y, contenido, tam=13, color="#1f2937", anclaje="middle", peso="normal"):
    return (f'<text x="{x:.1f}" y="{y:.1f}" font-size="{tam}" fill="{color}" '
            f'text-anchor="{anclaje}" font-weight="{peso}" '
            f'font-family="Liberation Sans, Arial, sans-serif">{contenido}</text>')


def caja(x, y, ancho, alto, titulo, detalle):
    partes = [f'<rect x="{x}" y="{y}" width="{ancho}" height="{alto}" rx="8" '
              f'fill="{FONDO_CAJA}" stroke="{GRIS_BORDE}"/>']
    partes.append(texto(x + ancho / 2, y + 22, titulo, 14, AZUL, peso="bold"))
    for i, linea in enumerate(detalle.split("\n")):
        partes.append(texto(x + ancho / 2, y + 42 + i * 15, linea, 11, GRIS_TEXTO))
    return partes


def generar():
    p = ['<?xml version="1.0" encoding="UTF-8"?>',
         f'<svg xmlns="http://www.w3.org/2000/svg" width="{ANCHO}" height="{ALTO}" '
         f'viewBox="0 0 {ANCHO} {ALTO}">',
         '<defs>',
         f'<marker id="flecha" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">'
         f'<path d="M0,0 L10,4 L0,8 z" fill="{AZUL}"/></marker>',
         f'<marker id="flechaGris" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">'
         f'<path d="M0,0 L10,4 L0,8 z" fill="{GRIS_TEXTO}"/></marker>',
         '</defs>',
         f'<rect width="{ANCHO}" height="{ALTO}" fill="#ffffff"/>']

    p.append(texto(CENTRO_X, 38, "Brújula · arquitectura hexagonal", 22, AZUL, peso="bold"))
    p.append(texto(CENTRO_X, 60, "las dependencias siempre apuntan hacia el dominio", 13, GRIS_TEXTO))

    # Hexagonos
    p.append(f'<polygon points="{puntos(RADIO_APLICACION)}" fill="{FONDO_APP}" '
             f'stroke="{AZUL}" stroke-width="2.5"/>')
    p.append(f'<polygon points="{puntos(RADIO_DOMINIO)}" fill="{FONDO_DOMINIO}" '
             f'stroke="{AMBAR}" stroke-width="2.5"/>')

    p.append(texto(CENTRO_X, CENTRO_Y - RADIO_DOMINIO - 32, "APLICACIÓN", 15, AZUL, peso="bold"))
    p.append(texto(CENTRO_X, CENTRO_Y - RADIO_DOMINIO - 14, "casos de uso", 12, GRIS_TEXTO))

    p.append(texto(CENTRO_X, CENTRO_Y - 62, "DOMINIO", 18, "#92400e", peso="bold"))
    for i, linea in enumerate(["modelo", "servicios de dominio",
                               "puertos de entrada y salida", "errores de negocio"]):
        p.append(texto(CENTRO_X, CENTRO_Y - 32 + i * 22, linea, 13, "#78350f"))
    p.append(texto(CENTRO_X, CENTRO_Y + 72, "sin Spring, sin JPA, sin HTTP", 11, "#92400e"))

    # Puertos sobre el borde
    lado_cuadro = 16
    for lado, lista, anclaje, desplazamiento in (
            ("izquierdo", PUERTOS_ENTRADA, "end", -14),
            ("derecho", PUERTOS_SALIDA, "start", 14)):
        for i, nombre in enumerate(lista):
            fraccion = (i + 1) / (len(lista) + 1)
            x, y = sobre_el_borde(fraccion, lado)
            p.append(f'<rect x="{x - lado_cuadro / 2:.1f}" y="{y - lado_cuadro / 2:.1f}" '
                     f'width="{lado_cuadro}" height="{lado_cuadro}" fill="#ffffff" '
                     f'stroke="{AZUL}" stroke-width="2"/>')
            p.append(texto(x + desplazamiento, y + 4, nombre, 11, AZUL, anclaje))

    p.append(texto(CENTRO_X - RADIO_APLICACION - 40, CENTRO_Y - RADIO_APLICACION + 38,
                   "puertos de entrada", 12, AZUL, "end", "bold"))
    p.append(texto(CENTRO_X + RADIO_APLICACION + 40, CENTRO_Y - RADIO_APLICACION + 38,
                   "puertos de salida", 12, AZUL, "start", "bold"))

    # Columnas de adaptadores
    ancho_caja, alto_caja, separacion = 250, 74, 16
    izquierda_x = 30
    total_izquierda = len(ADAPTADORES_ENTRADA) * (alto_caja + separacion) - separacion
    y = CENTRO_Y - total_izquierda / 2
    p.append(texto(izquierda_x + ancho_caja / 2, y - 46, "ADAPTADORES DE ENTRADA", 13, AZUL, peso="bold"))
    p.append(texto(izquierda_x + ancho_caja / 2, y - 28, "manejan la aplicación", 11, GRIS_TEXTO))
    for titulo, detalle in ADAPTADORES_ENTRADA:
        p += caja(izquierda_x, y, ancho_caja, alto_caja, titulo, detalle)
        destino_x, destino_y = sobre_el_borde(0.5, "izquierdo")
        p.append(f'<path d="M{izquierda_x + ancho_caja + 6},{y + alto_caja / 2:.1f} '
                 f'L{destino_x - 26:.1f},{destino_y:.1f}" stroke="{AZUL}" stroke-width="1.6" '
                 f'fill="none" marker-end="url(#flecha)" opacity="0.55"/>')
        y += alto_caja + separacion

    derecha_x = ANCHO - ancho_caja - 30
    total_derecha = len(ADAPTADORES_SALIDA) * (alto_caja + separacion) - separacion
    y = CENTRO_Y - total_derecha / 2
    p.append(texto(derecha_x + ancho_caja / 2, y - 46, "ADAPTADORES DE SALIDA", 13, AZUL, peso="bold"))
    p.append(texto(derecha_x + ancho_caja / 2, y - 28, "los conduce la aplicación", 11, GRIS_TEXTO))
    for titulo, detalle in ADAPTADORES_SALIDA:
        p += caja(derecha_x, y, ancho_caja, alto_caja, titulo, detalle)
        origen_x, origen_y = sobre_el_borde(0.5, "derecho")
        p.append(f'<path d="M{origen_x + 26:.1f},{origen_y:.1f} '
                 f'L{derecha_x - 6},{y + alto_caja / 2:.1f}" stroke="{AZUL}" stroke-width="1.6" '
                 f'fill="none" marker-end="url(#flecha)" opacity="0.55"/>')
        y += alto_caja + separacion

    # Pie
    pie_y = ALTO - 58
    p.append(f'<rect x="30" y="{pie_y - 26}" width="{ANCHO - 60}" height="66" rx="8" '
             f'fill="#f9fafb" stroke="#e5e7eb"/>')
    p.append(texto(CENTRO_X, pie_y - 4, "Los cuadros sobre el borde son los puertos: interfaces "
                   "declaradas en el dominio.", 12, GRIS_TEXTO))
    p.append(texto(CENTRO_X, pie_y + 16,
                   "Los adaptadores de entrada llaman a los puertos de entrada; los de salida "
                   "implementan los puertos de salida.", 12, GRIS_TEXTO))
    p.append(texto(CENTRO_X, pie_y + 34, "Cambiar de base de datos o de framework web es escribir "
                   "otro adaptador, sin tocar el dominio.", 12, GRIS_TEXTO))

    p.append('</svg>')
    SALIDA.parent.mkdir(parents=True, exist_ok=True)
    SALIDA.write_text("\n".join(p), encoding="utf-8")
    print(f"Escrito {SALIDA}")


if __name__ == "__main__":
    generar()
