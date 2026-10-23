plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "cl.duocuc.induztek"
 compileSdk = 37
 defaultConfig { applicationId = "cl.duocuc.induztek"; minSdk = 26; targetSdk = 37; versionCode = 3; versionName = "1.2"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; manifestPlaceholders["appLabel"] = "Induztek Terreno" }
 buildTypes {
  create("verificacion") {
   initWith(getByName("debug"))
   applicationIdSuffix = ".qa"
   manifestPlaceholders["appLabel"] = "Induztek Terreno QA"
   matchingFallbacks += listOf("debug")
  }
 }
 testBuildType = if (providers.gradleProperty("verificacionNativa").isPresent) "verificacion" else "debug"
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildFeatures { compose = true; buildConfig = true }
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
 testImplementation("junit:junit:4.13.2")
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
