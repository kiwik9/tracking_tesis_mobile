import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("io.fabric")
    id("com.google.gms.google-services")
    id("kotlin-android")
}

android {
    compileSdkVersion(Config.SdkVersions.compileSdk)
    defaultConfig {
        applicationId = Config.Project.applicationId
        minSdkVersion(Config.SdkVersions.minSdk)
        targetSdkVersion(Config.SdkVersions.targetSdk)
        versionCode = Config.SdkVersions.versionCode
        versionName = Config.SdkVersions.versionName
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    // vectorDrawables.useSupportLibrary = true

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    flavorDimensions("default")
    productFlavors {
        create("dev") {
            setDimension("default")
            buildConfigField("String", "BASE_URL", getProjectConfig("DEV").apiUrl)
        }
        create("prod") {
            setDimension("default")
            buildConfigField("String", "BASE_URL", getProjectConfig("PROD").apiUrl)
        }
        create("cal") {
            setDimension("default")
            buildConfigField("String", "BASE_URL", getProjectConfig("CAL").apiUrl)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        val options = this as KotlinJvmOptions
        jvmTarget = "1.8"
        options.jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.navigation:navigation-fragment:2.3.1")
    implementation("androidx.navigation:navigation-ui:2.3.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlin_version"]}")
    implementation("com.google.firebase:firebase-messaging:21.0.0")
    implementation("com.google.android.gms:play-services-maps:17.0.0")

    kapt(Libs.kapt_room)
    kapt(Libs.kapt_glide)
    kapt(Libs.kapt_moshi_codegen)

    kapt("com.android.databinding:compiler:3.5.3")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libs.kotlin_stdlib_jdk7)
    implementation(Libs.appcompat)
    implementation(Libs.core_ktx)
    implementation(Libs.constraintlayout)
    implementation(Libs.material)
    implementation(Libs.datepickerrange)
    implementation(Libs.google_location)
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.slf4j:slf4j-android:1.7.30")
    implementation(Libs.glide)

    implementation("com.toptoche.searchablespinner:searchablespinnerlibrary:1.3.1")

    implementation(Libs.multidex)

    implementation("de.hdodenhof:circleimageview:3.1.0")

    // room
    implementation(Libs.room)
    // implementation(Libs.room_coroutines)
    implementation(Libs.room_ktx)

    implementation(Libs.timeline_view)

    // lifecycle
    implementation(Libs.lifecycle_extensions)
    implementation(Libs.lifecycle)
    implementation(Libs.livedata)
    implementation(Libs.lifecycle_viewmodel)

    implementation(Libs.fragment_ktx)

    implementation(Libs.work)

    // STETHO
    implementation(Libs.stetho)

    implementation(Libs.lottie)

    // RETROFIT
    implementation(Libs.retrofit)
    implementation(Libs.retrofit_converter_moshi)
    implementation(Libs.logging_interceptor)

    // moshi
    implementation(Libs.moshi)

    implementation(Libs.joda_time)

    implementation(Libs.material_progressbar)

    implementation(Libs.loading_button_android)

    implementation(Libs.material_dialog)
    implementation(Libs.material_dialog_input)
    implementation(Libs.material_dialog_lifecycle)

    implementation(Libs.material_datetimepicker)

    implementation(Libs.crashlytics)
    implementation(Libs.firebase_core)

    implementation(Libs.loading_button_android)

    implementation(Libs.flexbox)

    implementation ("br.com.simplepass:loading-button-android:2.2.0")
    implementation ("com.afollestad.material-dialogs:core:3.3.0")

    implementation ("com.google.code.gson:gson:2.8.6")

    implementation (platform("com.google.firebase:firebase-bom:26.0.0"))
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.android.gms:play-services-auth:18.1.0")

    implementation("com.google.firebase:firebase-bom:26.1.0")
    implementation( project(":slideDateTimePicker"))

    testImplementation(Libs.junit)

    implementation("id.zelory:compressor:3.0.0")

    androidTestImplementation(Libs.androidx_test_runner)
    androidTestImplementation(Libs.espresso_core)
}


fun getProjectConfig(type: String): Config.ProjectProperty {
    return Config.Project.config[type] ?: Config.ProjectProperty("")
}
