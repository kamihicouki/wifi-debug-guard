plugins { id("com.android.application") }
android {
 namespace = "io.github.kamihicouki.wifidebugguard"
 compileSdk = 34
 defaultConfig {
  applicationId = "io.github.kamihicouki.wifidebugguard"
  minSdk = 30
  targetSdk = 33
  versionCode = 4
  versionName = "1.1.1"
 }
 buildTypes { getByName("release") { isMinifyEnabled = false } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
