# Induztek Terreno

**Estudiante:** Benjamin Hernandez  
**Sección:** 001D

Aplicación Android para apoyar el registro y consulta de visitas y protocolos de pruebas eléctricas realizados en terreno.

## Tecnologías

- Kotlin.
- Jetpack Compose y Material 3.
- Arquitectura MVVM con ViewModel y StateFlow.
- SQLite para el almacenamiento local.

## Estructura y funcionamiento

La interfaz está organizada en pantallas y componentes. Los modelos representan la información del proyecto y los ViewModels administran el estado, las validaciones y las operaciones de persistencia local. Las mediciones se evalúan mediante clases e interfaces que permiten aplicar reglas según el tipo de prueba.

## Ejecución

1. Abrir el proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo Android.
4. Ejecutar el módulo `app`.

## Pruebas

Pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Pruebas instrumentadas con un emulador o dispositivo conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas instrumentadas revisan el recorrido de acceso y la persistencia relacional en SQLite, incluidos los identificadores y la evidencia fotográfica BLOB.

## Enlaces

- [Repositorio](https://github.com/B3njvv23/App-moviles)
- [Tablero de trabajo](https://trello.com/b/DXXNw3KB/app-bases-de-datos)
