plugins { id("com.android.application") }
android {
 namespace = "io.github.kamihicouki.wifidebugguard"
 compileSdk = 36
 defaultConfig {
  applicationId = providers.gradleProperty("guardApplicationId").orElse("io.github.kamihicouki.wifidebugguard").get()
  minSdk = 30
  targetSdk = 36
  versionCode = 7
  versionName = "1.4.0"
 }
 val uploadStore = providers.environmentVariable("WDG_UPLOAD_STORE").orNull
 signingConfigs {
  if (uploadStore != null) create("upload") {
   storeFile = file(uploadStore)
   storePassword = providers.environmentVariable("WDG_UPLOAD_PASSWORD").get()
   keyAlias = providers.environmentVariable("WDG_UPLOAD_ALIAS").get()
   keyPassword = providers.environmentVariable("WDG_UPLOAD_PASSWORD").get()
  }
 }
 buildTypes { getByName("release") {
  isMinifyEnabled = false
  if (uploadStore != null) signingConfig = signingConfigs.getByName("upload")
 } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies {
 compileOnly("androidx.annotation:annotation:1.3.0")
 implementation("dev.rikka.shizuku:api:13.1.5")
 implementation("dev.rikka.shizuku:provider:13.1.5")
}
