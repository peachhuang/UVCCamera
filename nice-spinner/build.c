apply plugin: 'com.android.library'

buildscript {
    repositories {
        jcenter()
        //google()
        maven { url 'https://maven.google.com' }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.0-alpha04'
    }
}

android {
	compileSdkVersion versionCompiler
	buildToolsVersion versionBuildTool

	compileOptions {
		sourceCompatibility javaSourceCompatibility
		targetCompatibility javaTargetCompatibility
	}

    defaultConfig {
        minSdkVersion 14
		targetSdkVersion versionTarget
    }
}

dependencies {
    repositories {
        jcenter()
        google()
        maven { url 'https://maven.google.com' }
    }
    implementation 'com.android.support:appcompat-v7:27.0.1'
}
