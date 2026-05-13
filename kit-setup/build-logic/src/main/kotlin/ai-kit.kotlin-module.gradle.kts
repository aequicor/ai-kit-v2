plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
