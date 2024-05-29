import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "StackUnderflow"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()

}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.commone
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test"))

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //implementation("com.google.code.gson:gson:2.8.8")
    implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.0")
    implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.0")
    implementation("br.com.devsrsouza.compose.icons:line-awesome:1.1.0")
    implementation("it.skrape:skrapeit:1.1.5")
    implementation("it.skrape:skrapeit-http-fetcher:1.1.5")
    implementation("org.seleniumhq.selenium:selenium-java:4.21.0")
    implementation("org.mongodb:mongodb-driver-sync:5.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0-alpha04")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.1.0")
    implementation("org.fusesource.jansi:jansi:1.18")



}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ComposeVmesnik"
            packageVersion = "1.0.0"
        }
    }

}