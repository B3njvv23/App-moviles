import java.util.Properties

// Aquí configuro la app, las pruebas y la firma local.
plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
val firmaLocal = rootProject.file("keystore.properties")
val datosFirma = Properties().apply { if (firmaLocal.exists()) firmaLocal.inputStream().use { load(it) } }
val urlCatalogo = providers.gradleProperty("catalogoUrl").getOrElse("http://10.0.2.2:8081/")
val urlProtocolos = providers.gradleProperty("protocolosUrl").getOrElse("http://10.0.2.2:8082/")
android {
 namespace = "cl.duocuc.induztek"
 compileSdk = 37
 signingConfigs {
  if (firmaLocal.exists()) create("localRelease") {
   storeFile = rootProject.file(datosFirma.getProperty("storeFile"))
   storePassword = datosFirma.getProperty("storePassword")
   keyAlias = datosFirma.getProperty("keyAlias")
   keyPassword = datosFirma.getProperty("keyPassword")
  }
 }
 defaultConfig { applicationId = "cl.duocuc.induztek"; minSdk = 26; targetSdk = 37; versionCode = 4; versionName = "2.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; manifestPlaceholders["appLabel"] = "Induztek Terreno"; manifestPlaceholders["allowCleartext"] = "false"; buildConfigField("String", "CATALOGO_URL", "\"$urlCatalogo\""); buildConfigField("String", "PROTOCOLOS_URL", "\"$urlProtocolos\"") }
 buildTypes {
  debug {
   manifestPlaceholders["allowCleartext"] = "true"
   enableUnitTestCoverage = true
   enableAndroidTestCoverage = true
  }
  create("verificacion") {
   initWith(getByName("debug"))
   applicationIdSuffix = ".qa"
   manifestPlaceholders["appLabel"] = "Induztek Terreno"
   manifestPlaceholders["allowCleartext"] = "true"
   matchingFallbacks += listOf("debug")
   if (firmaLocal.exists()) signingConfig = signingConfigs.getByName("localRelease")
  }
  release {
   if (firmaLocal.exists()) signingConfig = signingConfigs.getByName("localRelease")
  }
 }
 testBuildType = if (providers.gradleProperty("verificacionNativa").isPresent) "verificacion" else "debug"
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildFeatures { compose = true; buildConfig = true }
 testOptions {
  unitTests.all {
   it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
   it.jvmArgs(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-opens=java.base/java.net=ALL-UNNAMED",
    "--add-opens=java.base/java.security=ALL-UNNAMED",
    "--add-opens=java.base/java.text=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--enable-native-access=ALL-UNNAMED",
   )
  }
 }
 testCoverage { jacocoVersion = "0.8.13" }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2026.02.01"))
 implementation("androidx.activity:activity-compose:1.13.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.core:core-ktx:1.17.0")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
 implementation("androidx.navigation:navigation-compose:2.10.2")
 implementation("com.squareup.retrofit2:retrofit:3.0.0")
 implementation("com.squareup.retrofit2:converter-gson:3.0.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.robolectric:robolectric:4.18")
 androidTestImplementation(platform("androidx.compose:compose-bom:2026.02.01"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 androidTestImplementation("androidx.test:runner:1.7.0")
 androidTestImplementation("androidx.test.ext:junit:1.3.0")
 androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
 androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
 add("verificacionImplementation", "androidx.compose.ui:ui-test-manifest")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
