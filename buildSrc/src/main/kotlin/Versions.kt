import kotlin.String
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {

  const val google_location = "17.0.0"
  const val material_datetimepicker = "4.2.3"
  const val timeline_view = "1.1.3"
  const val stepper = "1.0.2"
  const val support_version = "29.0.0"
  const val loading_button_android = "2.2.0"
  const val arc_layout = "1.0.3"
  const val material_progressbar: String = "1.6.1"
  const val preference: String = "1.1.0-alpha05"
  const val youtube_player: String = "10.0.4"
  const val lottie_version: String = "3.0.7"
  const val joda_time: String = "2.10.2"
  const val klockVersion = "1.6.0"
  const val android_networking = "1.0.2"
  const val fragment_ktx = "1.2.0-alpha01"
  const val legacy_support = "1.0.0"
  const val google_material: String = "1.0.0"

  const val glide: String = "4.9.0"
  
  const val appcompat: String = "1.1.0"

  const val constraintlayout: String = "1.1.3"

  const val core_ktx: String = "1.1.0"

  const val espresso_core: String = "3.2.0"

  const val androidx_test_runner: String = "1.2.0"

  const val aapt2: String = "3.5.0-5435860"

  const val com_android_tools_build_gradle: String = "3.5.0"

  const val lint_gradle: String = "26.5.0"

  const val de_fayard_buildsrcversions_gradle_plugin: String = "0.4.2" // available: "0.5.0"

  const val junit: String = "4.12"

  const val org_jetbrains_kotlin: String = "1.3.50" // available: "1.3.50"

  /**
   *
   * See issue 19: How to update Gradle itself?
   * https://github.com/jmfayard/buildSrcVersions/issues/19
   */
  const val gradleLatestVersion: String = "5.6.2"

  const val gradleCurrentVersion: String = "5.4.1"

  const val multidex: String = "2.0.1"
  /**
   *
   * Project libraries
   */
  const val materialDialog: String = "3.1.0"
  const val circleimageview: String = "2.1.0"
  const val retrofit: String = "2.6.1"
  const val logging_interceptor: String = "3.8.0"
  const val stetho: String = "1.5.1"//1.4.2
  const val rxandroid: String = "2.1.1"
  const val firebase_core: String = "17.0.1"
  const val firebase_alt: String = "16.0.1" //11.0.2
  const val firebase: String = "17.3.3" //11.0.2
  const val google: String = "17.0.0" //10.2.6
  const val crashlytics: String = "2.10.1"
  const val firebase_messaging = "20.0.0"
  const val flexbox: String = "2.0.0"
  const val signature_pad: String = "1.2.1"
  const val ahbottomnavigation: String = "2.3.4"
  const val dagger: String = "2.11"
  const val android_job: String = "1.2.5"
  const val circleprogress: String = "1.2.1"
  const val sectionedrecyclerv: String = "1.0.4"
  const val work_version: String = "2.3.0-alpha03"
  const val nav_version: String = "2.1.0-rc01"
  const val grpcVersion: String = "1.21.0"
  const val dateRangePicker: String = "2.0"
  const val moshi: String = "1.8.0"

  // ARCHITECTURE
  const val room: String = "2.2.0-rc01"
  const val lifecycle: String = "2.2.0-alpha02"
  const val paging: String = "2.1.0"

  const val carousel_view = "0.1.5"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
  inline get() =
      id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
