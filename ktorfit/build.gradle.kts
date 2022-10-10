plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(Libraries.kspApi)
    implementation(Libraries.kotlinPoet)
    implementation(Libraries.kotlinPoetKSP)
}