plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Если вы используете Kotlin Kapt для обработки аннотаций (например, с Dagger/Hilt, Room), раскомментируйте эту строку:
    // id("org.jetbrains.kotlin.kapt")
}

android {
    // Убедитесь, что namespace соответствует вашему базовому пакету
    namespace = "com.st84582.dreamcatcher"

    // compileSdk всегда должна быть последней стабильной версией Android SDK
    // На данный момент Android 14 (API 34) является широко используемой.
    // Если требуется доступ к новейшим функциям, обновите до 35, когда она станет стабильной.
    compileSdk = 34

    defaultConfig {
        applicationId = "com.st84582.dreamcatcher"
        // minSdk: Ваш текущий minSdk 24 (Android 7.0 Nougat) подходит для большинства устройств.
        // Это требует desugaring для java.time, который будет добавлен ниже.
        minSdk = 26
        targetSdk = 34 // Должен совпадать с compileSdk

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Добавьте поддержку векторных изображений для старых версий Android
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false // В релизе обычно true для уменьшения размера APK
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // НАСТРОЙКИ КОМПИЛЯЦИИ JAVA И KOTLIN
    compileOptions {
        // Установите Java 17, так как Android Studio и Gradle теперь полностью поддерживают ее.
        // Это хорошая практика для современных Android-проектов.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // Установите JVM Target на 17 для соответствия Java 17
        jvmTarget = "17"
    }

    // ВАЖНО: ИСПРАВЛЕННЫЙ СИНТАКСИС ДЛЯ Kotlin DSL
    // Добавьте этот блок для поддержки java.time.LocalDate на minSdk 24 и ниже
    var isCoreLibraryDesugaringEnabled =
        true // Использование 'is' префикса и '=' для присваивания
    // Это соответствует Kotlin DSL
    // Закомментируйте или удалите строку: coreLibraryDesugaringEnabled = true

    // Если у вас не используется Jetpack Compose, этот блок вам не нужен
    // buildFeatures {
    //     compose = false
    // }
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.1" // Укажите свою версию
    // }

    packaging {
        resources {
            // Исключения, которые иногда вызывают конфликты при сборке.
            // Убедитесь, что вы не исключаете ресурсы, которые вам действительно нужны.
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,NOTICE.md,LICENSE,NOTICE,ASL2.0}"
        }
    }
}

dependencies {
    // ДОБАВЛЕНИЕ ЗАВИСИМОСТИ ДЛЯ API DESUGARING
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.2.5") // Синтаксис Kotlin DSL: круглые скобки

    // --- AndroidX Базовые Компоненты ---
    // Core KTX - базовые Kotlin-расширения
    implementation("androidx.core:core-ktx:1.13.1")
    // AppCompat - обратная совместимость для Material Design
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Material Design Components (для UI-элементов, таких как кнопки, поля ввода и т.д.)
    implementation("com.google.android.material:material:1.12.0")

    // --- Layouts ---
    // ConstraintLayout для гибких макетов (очень рекомендуется)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // RecyclerView для эффективных списков
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // CardView для карточек
    implementation("androidx.cardview:cardview:1.0.0")

    // --- Image Loading (если нужно, например для аватаров из сети) ---
    // CircleImageView для аватаров (это сторонняя библиотека, убедитесь, что она вам нужна)
    // Если вам нужна более универсальная библиотека для загрузки изображений (например, с URL), рассмотрите Coil, Glide или Picasso.
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // --- Networking (Retrofit & OkHttp) ---
    // Retrofit для выполнения сетевых запросов
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter-Gson для автоматической сериализации/десериализации JSON с помощью Gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp - HTTP-клиент, используемый Retrofit (версия 4.x требует Kotlin)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // OkHttp Logging Interceptor - для логирования HTTP-запросов и ответов в Logcat (ОЧЕНЬ ПОЛЕЗНО ДЛЯ ОТЛАДКИ)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Kotlin Coroutines (для асинхронных операций, очень рекомендуется) ---
    // Обратите внимание на версию, она должна быть совместима с вашей версией Kotlin.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // --- Lifecycle Components (ViewModel, LiveData, Activity KTX) ---
    // Lifecycle-runtime-ktx - расширения для работы с жизненным циклом компонентов
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    // Activity KTX - Kotlin-расширения для Activity
    implementation("androidx.activity:activity-ktx:1.9.0")
    // ViewModel KTX - если вы планируете использовать ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    // LiveData KTX - если вы планируете использовать LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("com.google.android.material:material:1.12.0")
}


