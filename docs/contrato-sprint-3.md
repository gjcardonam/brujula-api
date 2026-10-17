# Contrato de la API · Sprint 3

Simulacros, resultados, recomendaciones y estadísticas (HU-013 a HU-019). Las rutas y los
nombres de los campos son los mismos que expone la rama `sprint-2`, para que el front que se
escribió contra esa rama funcione sin cambios. Las diferencias, que son pocas y casi todas
aditivas, están al final.

## Reglas generales

- Todas las rutas van bajo `/api` y exigen `Authorization: Bearer <token>`.
- Las de simulacros y estadísticas exigen además el rol **Estudiante**: un administrador recibe
  `403 ACCESO_DENEGADO`. Sin token o con token vencido: `401 SESION_INVALIDA`.
- Cada estudiante solo ve lo suyo. Un simulacro de otro estudiante responde `404 NO_ENCONTRADO`,
  igual que uno que no existe, para no revelar que existe.
- Las fechas son ISO-8601 en UTC con precisión de microsegundos, por ejemplo
  `"2026-10-17T19:53:05.740808Z"`.
- Los porcentajes son números con máximo dos decimales (`66.67`, `50.0`, `0.0`).
- Los errores llevan siempre la misma forma; cuando hay datos extra, van en la raíz del cuerpo:

```json
{ "estado": 409, "codigo": "SIMULACRO_EN_CURSO", "mensaje": "Ya tienes un simulacro en curso. …",
  "detalles": [], "idSimulacro": 12 }
```

## El tiempo lo lleva el servidor

El navegador solo pinta el temporizador. La hora de inicio queda guardada y **cada llamada**
sobre un simulacro revisa si ya se venció; si es así, lo finaliza en ese momento con
`fechaFin = fechaInicio + duración` y genera sus recomendaciones. Por eso una desconexión no da
tiempo extra (HU-016 CA-03) y, al reconectar, el simulacro vencido aparece ya finalizado
(HU-016 CA-05). Cada respuesta trae `ahora`, la hora del servidor, para que el front corrija el
desfase de su reloj.

## Catálogo de duraciones

### `GET /api/catalogos`

Se agregó la lista `duraciones`. La pantalla de configurar el simulacro toma de aquí el `id` que
envía al iniciar.

```json
{
  "componentes":  [{ "id": 1, "nombre": "Álgebra y cálculo" }],
  "competencias": [{ "id": 1, "nombre": "Interpretación y representación" }],
  "niveles":      [{ "id": 1, "nombre": "Básico" }],
  "duraciones":   [{ "id": 1, "minutos": 30 }, { "id": 2, "minutos": 45 }, { "id": 3, "minutos": 60 },
                   { "id": 4, "minutos": 90 }, { "id": 5, "minutos": 120 }]
}
```

## Simulacros

### Objeto `EstadoSimulacro`

Lo devuelven `POST /simulacros`, `GET /simulacros/en-curso`, `GET /simulacros/{id}` y va dentro
de `siguiente-ejercicio`.

| Campo | Tipo | |
| :-- | :-- | :-- |
| `id` | number | |
| `estado` | `"En curso"` \| `"Finalizado"` | |
| `duracionMinutos` | number | 30, 45, 60, 90 o 120; no cambia después de iniciar |
| `fechaInicio` | string | |
| `finPrevisto` | string | `fechaInicio + duracionMinutos` |
| `ahora` | string | hora del servidor en esa respuesta |
| `tiempoRestanteSeg` | number | 0 si ya finalizó |
| `respondidos` | number | intentos confirmados en el simulacro |
| `correctas` | number | |

### `POST /api/simulacros` · HU-013

Petición: `{ "idDuracion": 1 }`

| Respuesta | Cuándo |
| :-- | :-- |
| `201` + `EstadoSimulacro` | Se creó y el tiempo empezó a correr |
| `400 VALIDACION` | Falta `idDuracion` (`detalles: ["Debes seleccionar la duración del simulacro."]`) |
| `400 DURACION_INVALIDA` | El `idDuracion` no está en el catálogo |
| `409 SIMULACRO_EN_CURSO` | Ya tiene uno abierto y con tiempo; trae `idSimulacro` para continuarlo. Si el abierto ya se había vencido, se cierra y se crea el nuevo |
| `409 SIMULACRO_EN_CURSO` sin `idSimulacro` | Dos inicios llegaron al mismo tiempo y ganó el otro |
| `409 SIN_EJERCICIOS` | No hay ejercicios activos |

No existe ninguna ruta para cambiar la duración ni reiniciar el tiempo (HU-013 CA-06).

### `GET /api/simulacros/en-curso` · HU-016

`200` + `EstadoSimulacro` si tiene uno abierto con tiempo, o `204` sin cuerpo si no. Es la
llamada para retomar después de recargar o reconectar.

### `GET /api/simulacros/{id}`

`200` + `EstadoSimulacro`. Si el tiempo ya se acabó, lo devuelve con `estado: "Finalizado"`. El
front la usa cuando su temporizador llega a cero. `404` si no es suyo.

### `GET /api/simulacros/{id}/siguiente-ejercicio` · HU-014, HU-015 CA-03

```json
{
  "finalizado": false,
  "motivo": null,
  "ejercicio": {
    "id": 20, "numero": 20, "enunciado": "…", "imagenEnunciado": null,
    "componente": { "id": 1, "nombre": "Álgebra y cálculo" },
    "competencia": { "id": 2, "nombre": "Formulación y ejecución" },
    "nivel": "Intermedio", "estado": "Activo",
    "opciones": [{ "id": 77, "letra": "A", "descripcion": "$480.000", "imagen": null, "orden": 1 }],
    "intentosPrevios": 0
  },
  "estado": { "…": "EstadoSimulacro" }
}
```

- Entrega un ejercicio activo que no se haya respondido en este simulacro. Mientras no se
  responda, repite **el mismo**: recargar la página no cambia la pregunta ni la salta.
- El ejercicio no trae cuál opción es la correcta ni las retroalimentaciones.
- Cuando no hay ejercicio que dar: `finalizado: true`, `ejercicio: null` y `motivo`:

| `motivo` | |
| :-- | :-- |
| `"TIEMPO"` | Se acabó el tiempo; queda finalizado en esta llamada |
| `"SIN_EJERCICIOS"` | Ya respondió todos los activos; queda finalizado en esta llamada |
| `"FINALIZADO"` | Ya estaba finalizado |

### `POST /api/intentos` · HU-014, HU-016

Es la misma ruta de la práctica libre. Dentro de un simulacro se envía además `idSimulacro`.

```json
{ "idEjercicio": 20, "idOpcion": 78, "nivelConfianza": 4,
  "tokenIdempotencia": "8c1d3c4e-6a55-4a3e-9f4e-1f0b6d1f2a10", "idSimulacro": 1 }
```

Respuesta `201`:

```json
{
  "idIntento": 2, "esCorrecto": true, "idOpcionSeleccionada": 78, "idOpcionCorrecta": 78,
  "retroalimentacion": "…", "retroalimentacionDisponible": true, "nivelConfianza": 4,
  "respondidoEn": "2026-10-17T19:53:06.333997Z", "fechaHora": "2026-10-17T19:53:06.333997Z",
  "numeroIntento": 1, "repetido": false, "idSimulacro": 1
}
```

- `tokenIdempotencia` lo genera el navegador **una vez por respuesta** y lo reutiliza si tiene
  que reenviarla. Reenviar el mismo token devuelve el intento que ya quedó, con
  `repetido: true`, aunque el simulacro haya finalizado entretanto. Nunca se duplica.
- Un ejercicio se responde una sola vez por simulacro; lo garantiza también un índice único en
  la base.

| Error | Cuándo |
| :-- | :-- |
| `400 TOKEN_IDEMPOTENCIA_REQUERIDO` | Falta `tokenIdempotencia` |
| `400 VALIDACION` | Falta ejercicio u opción, o `nivelConfianza` no está entre 1 y 5 |
| `400 OPCION_INVALIDA` | La opción no pertenece al ejercicio |
| `404 NO_ENCONTRADO` | El ejercicio no existe, o el simulacro no existe o no es suyo |
| `410 NO_DISPONIBLE` | El ejercicio está desactivado |
| `409 SIMULACRO_FINALIZADO` | El simulacro ya finalizó o se le acabó el tiempo; trae `idSimulacro`. El front debe ir al resultado |
| `409 EJERCICIO_YA_RESPONDIDO` | Ese ejercicio ya tiene respuesta en el simulacro con otro token. El front debe pedir el siguiente |

### `POST /api/simulacros/{id}/finalizar` · HU-015 CA-02

Sin cuerpo. Finaliza el simulacro y devuelve `200` + `ResultadoSimulacro`. Es idempotente:
sobre uno ya finalizado devuelve el mismo resultado sin cambiar la hora de fin. `404` si no es
suyo.

### `GET /api/simulacros/{id}/resultado` · HU-017, HU-018

`200` + `ResultadoSimulacro`. `409 SIMULACRO_EN_CURSO` (con `idSimulacro`) si todavía tiene
tiempo. Si el tiempo ya pasó, primero lo finaliza. `404` si no es suyo.

```json
{
  "id": 1,
  "fechaInicio": "2026-10-17T19:51:10.587694Z",
  "fechaFin": "2026-10-17T19:51:13.610587Z",
  "duracionMinutos": 30,
  "tiempoUtilizadoSeg": 3,
  "respondidos": 6, "correctas": 3, "incorrectas": 3, "porcentajeAciertos": 50.0,
  "porComponente": [{ "id": 2, "nombre": "Geometría", "intentos": 1, "correctas": 0, "porcentaje": 0.0 }],
  "porCompetencia": [{ "id": 3, "nombre": "Argumentación", "intentos": 2, "correctas": 1, "porcentaje": 50.0 }],
  "recomendaciones": [
    { "orden": 1, "area": "Geometría", "tipo": "Componente", "idComponente": 2, "idCompetencia": null,
      "porcentaje": 0.0, "mensaje": "Te recomendamos practicar ejercicios de Geometría." }
  ],
  "mensajeRecomendacion": null,
  "detalle": [
    {
      "id": 2, "idEjercicio": 20, "numeroEjercicio": 20, "enunciado": "…", "imagenEnunciado": null,
      "componente": "Álgebra y cálculo", "competencia": "Formulación y ejecución", "nivel": "Intermedio",
      "estadoEjercicio": "Activo",
      "opciones": [{ "id": 78, "letra": "B", "descripcion": "$540.000", "imagen": null,
                     "seleccionada": true, "esCorrecta": true }],
      "esCorrecto": true, "nivelConfianza": 5, "fechaHora": "2026-10-17T19:51:11.432732Z",
      "retroalimentacion": "…", "idSimulacro": 1
    }
  ]
}
```

- Todo se calcula solo con los intentos de ese simulacro (HU-017 CA-09).
- `porComponente` y `porCompetencia` solo incluyen las áreas con al menos un intento, en orden
  de `id`.
- `recomendaciones` se generan **una vez, al finalizar**, y quedan guardadas con el simulacro:
  un simulacro nuevo no cambia las de los anteriores (HU-018 CA-06, CA-07). Son las áreas con
  menos del 60 % de aciertos, de menor a mayor porcentaje y, a igual porcentaje, en orden
  alfabético. El umbral vive en `parametros_sistema.umbral_recomendacion_pct`.
- `mensajeRecomendacion`: `null` si hay recomendaciones;
  `"Tienes un buen desempeño general. Sigue practicando para mantenerlo."` si todas las áreas
  quedaron en 60 % o más; `"No hay resultados suficientes: el simulacro finalizó sin respuestas
  registradas."` si no respondió ninguno.
- `detalle` va en el orden en que respondió; `retroalimentacion` es la de la opción que eligió.

### `GET /api/simulacros/mios?pagina=0`

Simulacros **finalizados** del estudiante, del más reciente al más antiguo, de 20 en 20. Antes
de listar cierra los que se hayan vencido. Sirve para volver a abrir un resultado.

```json
{
  "contenido": [{ "id": 1, "fechaInicio": "…", "fechaFin": "…", "duracionMinutos": 30,
                  "tiempoUtilizadoSeg": 3, "respondidos": 6, "correctas": 3, "incorrectas": 3,
                  "porcentajeAciertos": 50.0 }],
  "pagina": 0, "tamano": 20, "totalElementos": 1, "totalPaginas": 1
}
```

## Estadísticas

### `GET /api/estadisticas/mias` · HU-019

Junta la práctica libre y los simulacros del estudiante. Antes de calcular cierra los simulacros
vencidos, para que cuenten.

```json
{
  "sinDatos": false,
  "mensaje": null,
  "resumen": { "intentados": 8, "correctas": 4, "incorrectas": 4,
               "porcentajeAciertos": 50.0, "porcentajeDesaciertos": 50.0 },
  "porComponente": [{ "id": 1, "nombre": "Álgebra y cálculo", "intentados": 3, "correctas": 2,
                      "incorrectas": 1, "porcentajeAciertos": 66.67, "porcentajeDesaciertos": 33.33 }],
  "porCompetencia": [{ "id": 3, "nombre": "Argumentación", "intentados": 2, "correctas": 1,
                       "incorrectas": 1, "porcentajeAciertos": 50.0, "porcentajeDesaciertos": 50.0 }],
  "porConfianza": [{ "nivel": 1, "intentados": 1, "correctas": 0, "incorrectas": 1,
                     "porcentajeCorrectas": 0.0, "porcentajeIncorrectas": 100.0 }],
  "simulacros": { "cantidad": 2, "porcentajePromedioAciertos": 75.0, "promedioEjercicios": 3.5,
                  "tiempoPromedioSeg": 901.5, "ejerciciosPorHora": 13.98 }
}
```

- `porComponente`, `porCompetencia` y `porConfianza` solo traen lo que tiene al menos un
  intento: un área sin intentos no aparece, en lugar de aparecer con 0 % (HU-019 CA-07).
- `simulacros` cuenta solo los finalizados. `porcentajePromedioAciertos` promedia el porcentaje
  de cada simulacro que tuvo respuestas; `ejerciciosPorHora` divide todos los ejercicios
  respondidos en simulacros entre las horas usadas.
- Sin ningún intento ni simulacro: `sinDatos: true` y `mensaje` con el texto para mostrar en
  lugar de los porcentajes (HU-019 CA-09):
  `"Aún no has resuelto ejercicios ni simulacros. Cuando practiques, aquí verás tu desempeño."`

## Diferencias con la rama `sprint-2`

| Dónde | Cambio | Qué hacer en el front |
| :-- | :-- | :-- |
| `POST /intentos`, respuesta | Trae `fechaHora` **y** `respondidoEn` con el mismo valor. `main` usa `respondidoEn` y `sprint-2` usa `fechaHora`; quedan los dos para no romper ninguno | Nada |
| `POST /intentos`, petición | `tokenIdempotencia` es obligatorio, como ya lo es en `main`. En `sprint-2` era opcional | El front de `sprint-2` ya lo envía siempre |
| `POST /intentos` | Con `idSimulacro` de otro estudiante responde `404`, revisado antes que el token | Nada |
| `GET /catalogos` | No trae `tiposError`: la clasificación del tipo de error (HU-027 a HU-029) no entra en este sprint | No leer `tiposError` |
| `GET /estadisticas/mias` | No trae `errores` ni `componentesConIntentos` y no usa el parámetro `componente`, que eran de la distribución de tipos de error (HU-029). Se agregó `mensaje` | Quitar el panel "Distribución de tus errores" y el filtro por componente |
| Recomendaciones | A igual porcentaje se ordenan alfabéticamente en español ("Álgebra" antes que "Interpretación") | Nada |
| Fechas | Llegan con microsegundos y no con nanosegundos, para que lo que devuelve `finalizar` sea idéntico a lo que devuelve después `resultado` | Nada |

Lo que pide HU-016 CA-04, avisar al estudiante cuando se pierde y se recupera la conexión, es
solo del front: la API no tiene nada que ver ahí.
