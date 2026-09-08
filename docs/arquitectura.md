# Arquitectura de Brújula

Documento de arquitectura de la API. Los diagramas se ven directamente en GitHub y también están
exportados en [`docs/imagenes/`](imagenes/), en PNG para leerlos aquí y en SVG para el informe o la
presentación, donde conviene que no se pixelen.

La API está construida en **arquitectura hexagonal**, también llamada de puertos y adaptadores. La
idea de fondo es sencilla: las reglas del negocio no deben depender de Spring, de la base de datos ni
de HTTP. Al revés, esas tecnologías se enchufan al dominio a través de interfaces.

---

## 1. Vista general: puertos y adaptadores

![Arquitectura hexagonal de Brújula](imagenes/hexagono.png)

En el centro está el **dominio**, que no importa una sola clase del framework: ahí viven el modelo
(Usuario, Ejercicio, Intento, Simulacro), las reglas puras y los **puertos**, que son interfaces.

Alrededor está la **aplicación**, con un caso de uso por historia de usuario. Un caso de uso
implementa un puerto de entrada y, para lo que necesita del mundo exterior, llama a puertos de salida
sin saber quién los implementa.

Por fuera quedan los **adaptadores**:

- Los de la izquierda **manejan** la aplicación: llegan de afuera y llaman a un puerto de entrada. Son
  los controladores REST, el filtro que valida el token y la carga inicial al arrancar.
- Los de la derecha son **conducidos** por la aplicación: implementan un puerto de salida. Son la
  persistencia con JPA, el emisor de tokens, el envío de correo, la verificación con Google, el
  almacenamiento de imágenes y el reloj.

---

## 2. Las tres capas y la regla de dependencias

```mermaid
flowchart TB
    subgraph INFRA["INFRAESTRUCTURA · adaptadores"]
        direction LR
        REST["Controladores REST<br/>y DTO"]
        ARRANQUE["Carga inicial<br/>administrador y datos de ejemplo"]
        JPA["Persistencia JPA<br/>PostgreSQL"]
        SEG["Seguridad<br/>JWT y BCrypt"]
        EXT["Correo SMTP · Google<br/>Imágenes en disco · Reloj"]
    end

    subgraph APP["APLICACIÓN · casos de uso"]
        CASOS["Implementan los puertos de entrada<br/>y orquestan los de salida"]
    end

    subgraph DOM["DOMINIO · el centro"]
        MODELO["Modelo<br/>Usuario, Ejercicio, Intento, Simulacro"]
        SERV["Servicios de dominio<br/>clasificación de errores, recomendaciones,<br/>estadísticas, política de contraseñas"]
        PUERTOS["Puertos<br/>entrada y salida"]
    end

    REST --> CASOS
    ARRANQUE --> CASOS
    CASOS --> MODELO
    CASOS --> SERV
    CASOS --> PUERTOS
    JPA -.implementa.-> PUERTOS
    SEG -.implementa.-> PUERTOS
    EXT -.implementa.-> PUERTOS

    classDef dominio fill:#fef3c7,stroke:#b45309,stroke-width:2px,color:#1f2937
    classDef aplicacion fill:#dbeafe,stroke:#1e3a5f,stroke-width:2px,color:#1f2937
    classDef infra fill:#f3f4f6,stroke:#6b7280,stroke-width:1px,color:#1f2937
    class MODELO,SERV,PUERTOS dominio
    class CASOS aplicacion
    class REST,ARRANQUE,JPA,SEG,EXT infra
```

Las flechas siempre apuntan hacia adentro: `infraestructura → aplicación → dominio`. El dominio no
conoce a nadie. Que la persistencia aparezca apuntando a los puertos y no al revés es lo que se llama
inversión de dependencias, y es lo que permite cambiar de base de datos escribiendo otro adaptador.

Esta regla no se queda en el papel: `ArquitecturaTest` lee los imports de cada archivo y falla la
compilación si el dominio empieza a depender del framework o si un caso de uso llama directamente a
un adaptador.

---

## 3. Puertos de entrada: un caso de uso por historia

![Puertos de entrada](imagenes/puertos-de-entrada.png)

Cada historia de usuario del alcance de PI1 entra por su propio puerto. Los controladores no
contienen lógica: reciben la petición, la convierten en un comando y llaman al puerto.

| Puerto de entrada | Caso de uso que lo implementa | Historias |
| :-- | :-- | :-- |
| `RegistrarEstudiante` | `RegistroDeEstudiantes` | HU-001 |
| `AutenticarUsuario` | `AutenticacionDeUsuarios` | HU-002 |
| `RecuperarContrasena` | `RecuperacionDeContrasena` | HU-003 |
| `GestionarSesion`, `ValidarSesion` | `SesionesDeUsuario` | HU-004 |
| `GestionarPerfil` | `PerfilDeUsuario` | HU-005 |
| `ConsultarBanco`, `ConsultarEjercicio`, `BuscarSiguienteEjercicio` | `BancoDeEjercicios` | HU-006 a HU-010 |
| `ResponderEjercicio`, `ConsultarHistorialDeIntentos` | `RegistroDeIntentos` | HU-010 a HU-012, HU-014, HU-016, HU-025, HU-027, HU-028 |
| `GestionarSimulacro` | `Simulacros` | HU-013 a HU-018, HU-026 |
| `ConsultarEstadisticas` | `EstadisticasDelEstudiante` | HU-019, HU-029 |
| `AdministrarEjercicios`, `GuardarImagen` | `AdministracionDeEjercicios` | HU-020 a HU-024 |
| `ConsultarCatalogos` | `ConsultaDeCatalogos` | catálogos de apoyo |

---

## 4. Puertos de salida y sus adaptadores

![Puertos de salida](imagenes/puertos-de-salida.png)

Son quince interfaces declaradas en el dominio, cada una con un adaptador en la infraestructura. Vale
la pena señalar tres que no son repositorios:

- **`Reloj`** existe para no llamar a `Instant.now()` dentro de las reglas. Gracias a eso el bloqueo
  de diez minutos tras cinco intentos fallidos se prueba adelantando un reloj falso, sin esperar.
- **`ParametrosDelSistema`** lee los umbrales de la tabla `parametros_sistema`, porque las historias
  piden que sean configurables y no queden fijos en el código.
- **`VerificadorDeGoogle`** aísla la verificación del ID token; en desarrollo hay una variante
  simulada que permite probar el registro sin credenciales reales de Google.

---

## 5. Cómo fluye una petición: responder un ejercicio

Este es el recorrido completo de HU-010 a HU-012, con la clasificación del error de HU-027 y HU-028.

```mermaid
sequenceDiagram
    autonumber
    actor E as Estudiante
    participant C as IntentoControlador<br/>adaptador de entrada
    participant P as ResponderEjercicio<br/>puerto de entrada
    participant U as RegistroDeIntentos<br/>caso de uso
    participant M as ClasificadorDeError<br/>servicio de dominio
    participant R as IntentoRepositorio<br/>puerto de salida
    participant A as IntentoAdaptador<br/>JPA

    E->>C: POST /api/intentos<br/>opción, confianza, token
    C->>P: responder(idEstudiante, respuesta)
    P->>U: (implementación)

    U->>U: valida opción y nivel de confianza
    U->>R: porToken(tokenIdempotencia)
    R->>A: consulta
    A-->>U: vacío, no es un reenvío

    Note over U: si la respuesta es incorrecta,<br/>se clasifica el error una sola vez
    U->>R: ultimosResultados(ventana)
    R->>A: consulta
    A-->>U: resultados previos del área
    U->>M: clasificar(confianza, opción, ventana)
    M-->>U: Error cognitivo, de hábito o de ansiedad

    U->>R: guardar(intento)
    R->>A: insert
    A-->>U: intento con id
    U-->>C: ResultadoDeIntento
    C-->>E: 201 con la retroalimentación de su opción

    Note over C,E: el tipo de error no viaja en la respuesta,<br/>solo se ve agregado en las estadísticas
```

Lo que hay que notar: el caso de uso nunca toca JPA. Habla con `IntentoRepositorio`, que es una
interfaz del dominio, y quien responde del otro lado es el adaptador. En las pruebas, del otro lado
responde una implementación en memoria y el flujo es exactamente el mismo.

---

## 6. Una regla de negocio en el dominio: el ingreso

El bloqueo tras cinco intentos fallidos no está en el controlador ni en la base de datos: está en la
entidad `Usuario`, que es quien sabe contar sus propios intentos.

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant C as AutenticacionControlador
    participant P as AutenticarUsuario<br/>puerto de entrada
    participant S as AutenticacionDeUsuarios<br/>caso de uso
    participant E as Usuario<br/>entidad de dominio
    participant R as UsuarioRepositorio<br/>puerto de salida
    participant T as ProveedorDeTokens<br/>puerto de salida

    U->>C: POST /api/auth/login
    C->>P: autenticar(correo, contraseña)
    P->>S: (implementación)
    S->>R: porEmail(correo)
    R-->>S: usuario

    alt cuenta bloqueada y el castigo sigue vigente
        S->>E: estaBloqueado(ahora, minutos)
        E-->>S: sí
        S-->>C: CuentaBloqueada
        C-->>U: 423 con los minutos que faltan
    else contraseña incorrecta
        S->>E: registrarIngresoFallido(ahora, máximo)
        E-->>S: quedó bloqueada o no
        S->>R: guardar(usuario)
        S-->>C: CredencialesInvalidas
        C-->>U: 401 con el mismo mensaje de siempre
    else credenciales correctas
        S->>E: registrarIngresoExitoso(ahora)
        S->>R: guardar(usuario)
        S->>T: emitirSesion(usuario)
        T-->>S: token, jti y vencimiento
        S-->>C: SesionIniciada
        C-->>U: 200 con el token y su rol
    end
```

El mensaje de credenciales inválidas es el mismo cuando el correo no existe y cuando la contraseña
está mal, para que nadie pueda averiguar qué correos están registrados. Esa decisión también es del
dominio: la excepción se llama `CredencialesInvalidas` y trae un único mensaje.

---

## 7. Despliegue

```mermaid
flowchart LR
    NAV["Navegador<br/>del estudiante"]

    subgraph HOST["Docker Compose"]
        direction TB
        WEB["brujula-web<br/>nginx + React compilado<br/>puerto 3000"]
        API["brujula-api<br/>Spring Boot<br/>puerto 8080"]
        DB[("brujula-db<br/>PostgreSQL 16")]
        MAIL["brujula-mail<br/>Mailpit, correo de prueba<br/>puerto 8025"]
        VOL[("volumen<br/>imágenes de ejercicios")]
    end

    GOOGLE["Google Identity Services<br/>verificación del ID token"]

    NAV -->|HTTP| WEB
    WEB -->|proxy /api| API
    API -->|JDBC| DB
    API -->|SMTP| MAIL
    API --> VOL
    API -->|HTTPS, llaves públicas| GOOGLE

    classDef contenedor fill:#dbeafe,stroke:#1e3a5f,color:#1f2937
    classDef externo fill:#f3f4f6,stroke:#6b7280,color:#1f2937
    class WEB,API,DB,MAIL,VOL contenedor
    class NAV,GOOGLE externo
```

El front no habla directamente con la API: nginx sirve el React compilado y reenvía `/api` al
contenedor de la API, así que ambos quedan en el mismo origen y no hace falta configurar CORS en
producción.

---

## 8. Qué ganamos con esta arquitectura

**Se puede probar el negocio sin levantar nada.** Los casos de uso solo hablan con interfaces, así que
en las pruebas se les pasan implementaciones en memoria. Las 36 pruebas del proyecto corren en
segundos, sin base de datos y sin contexto de Spring.

**Las reglas están en un solo lugar.** Si mañana cambia el umbral de dominio previo o el mensaje de
una recomendación, se toca un servicio de dominio y nada más.

**Se puede cambiar la tecnología por partes.** Pasar de PostgreSQL a otra base, o de correo SMTP a un
servicio externo, es escribir otro adaptador que implemente el mismo puerto.

**El costo.** Hay más archivos y más interfaces que en una aplicación en capas, y para operaciones
muy simples se siente ceremonioso. Es el precio de mantener el dominio aislado, y para un proyecto
que va a seguir creciendo en PI II nos pareció que vale la pena.

---

## Cómo regenerar los diagramas

Las fuentes están en [`docs/diagramas/`](diagramas/) como archivos de Mermaid, salvo el hexágono, que
lo dibuja un script de Python porque Mermaid no tiene esa forma.

```bash
./docs/generar-diagramas.sh
```
