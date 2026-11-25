# Induztek Terreno

Aplicación Android para registrar visitas y protocolos de pruebas eléctricas en terreno. La versión final conserva el trabajo local con SQLite y agrega dos servicios REST para sincronizar catálogo y protocolos, además de una consulta meteorológica externa.

**Estudiante:** Benjamín Hernández · **Sección:** 001D

## Funcionalidades

- Gestión de clientes, instalaciones, equipos, visitas y protocolos.
- Mediciones con validaciones de entrada y evaluación según el tipo de equipo.
- Registro fotográfico y generación de PDF del protocolo con evidencia.
- Edición del perfil y cambio de contraseña.
- Búsqueda de equipos y filtros de protocolos por texto, estado, cliente y autor.
- Contraseña con mínimo de 10 caracteres, mayúscula, número y símbolo.
- Persistencia en SQLite; los cambios se sincronizan cuando los servicios están disponibles.
- Consulta del clima actual de Santiago mediante Open-Meteo.

## Tecnologías

- Android: Kotlin, Jetpack Compose, MVVM, StateFlow, SQLite y Retrofit.
- Backend: Java 25, Spring Boot y dos servicios REST con H2 persistente.
- Pruebas Android: JUnit.

## Servicios y rutas

El servicio de catálogo corre en `http://localhost:8081` y conserva clientes, instalaciones, equipos y visitas.

- `GET /api/catalogo/{tipo}` y `GET /api/catalogo/{tipo}/{id}`
- `PUT /api/catalogo/{tipo}/{id}` y `DELETE /api/catalogo/{tipo}/{id}`
- `PUT /api/catalogo/sync` sincroniza el catálogo local

El servicio de protocolos corre en `http://localhost:8082` y conserva protocolos y su fotografía BLOB.

- `GET /api/protocolos` y `GET /api/protocolos/{id}`
- `PUT /api/protocolos/{id}` y `DELETE /api/protocolos/{id}`
- `PUT /api/protocolos/sync` sincroniza protocolos y evidencia

La app consulta el clima desde `https://api.open-meteo.com/`. Esta consulta requiere internet; el registro en SQLite se mantiene disponible sin conexión.

## Ejecución local

Se requiere Android Studio y Java 17 o superior (la instalación de Android Studio incluye Java).

1. Desde la carpeta raíz, inicia el catálogo: `.\gradlew.bat -p backend :catalogo:bootRun`.
2. En otra terminal desde la raíz, inicia protocolos: `.\gradlew.bat -p backend :protocolos:bootRun`.
3. Abre la carpeta raíz en Android Studio, inicia un emulador y ejecuta la configuración `app`.

Los servicios guardan los datos en archivos H2 locales. El emulador Android llega al computador por `10.0.2.2`; las URL están en `app/build.gradle.kts`. El tráfico HTTP está permitido solo en la compilación `debug` para desarrollo local. En una instalación real, configura URLs HTTPS y desactiva el tráfico sin cifrar.

La suite local de Android tiene 40 pruebas, incluidas seis que ejecutan los ViewModels con Robolectric y una base SQLite temporal. También hay dos pruebas REST del backend.

El informe de JaCoCo muestra 318 de 368 líneas cubiertas (86,4 %) en los archivos de reglas, validadores y evaluadores. Las seis pruebas de los ViewModels pasaron, pero el informe no registró su ejecución con Robolectric, así que ese porcentaje no acredita la cobertura de los ViewModels. El informe se genera en `app/build/reports/coverage/test/debug/index.html`.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:createDebugUnitTestCoverageReport
```

Para compilar los dos servicios:

```powershell
.\gradlew.bat -p backend :catalogo:bootJar :protocolos:bootJar
```

## APK firmado

La clave de firma y sus contraseñas se guardan localmente y no se suben al repositorio. Crea un archivo `keystore.properties` en la raíz a partir de `keystore.properties.example` y genera el `.jks` con `keytool -genkeypair -keystore induztek-release.jks -alias induztek -keyalg RSA -keysize 3072 -validity 10000`:

```properties
storeFile=induztek-release.jks
storePassword=CAMBIAR
keyAlias=induztek
keyPassword=CAMBIAR
```

Para la presentación local con backend en el mismo computador, ejecuta `.\gradlew.bat assembleVerificacion`. Este APK está firmado con el `.jks` local y usa HTTP solo para los servicios locales; queda en `app/build/outputs/apk/verificacion/`. La compilación `release` exige HTTPS. Pasa las URLs al generar ese APK, por ejemplo: `.\gradlew.bat assembleRelease -PcatalogoUrl=https://servidor/catalogo/ -PprotocolosUrl=https://servidor/protocolos/`. Entrega el APK, el archivo `.jks` y la configuración por el medio privado indicado por el curso; no publiques la clave en GitHub.

## Organización de archivos Kotlin

Cada archivo Kotlin comienza con un comentario corto sobre su propósito. Se mantienen las pantallas y modelos existentes para que cada flujo siga siendo fácil de ubicar; la integración de servicios quedó reunida en `data/ApiInduztek.kt`.

## Tablero del proyecto

[Trello: AppMoviles](https://trello.com/b/DXXNw3KB/appmoviles)





