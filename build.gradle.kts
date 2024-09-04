import com.diffplug.gradle.spotless.YamlExtension.JacksonYamlGradleConfig

plugins {
  id("fabric-loom") version "1.7-SNAPSHOT"
  id("com.diffplug.spotless") version "latest.release"
  id("org.jetbrains.changelog") version "latest.release"
  id("com.modrinth.minotaur") version "latest.release"
  id("maven-publish")
}

version = providers.gradleProperty("minecraft_version").get() + "-" + providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
  maven("https://maven.parchmentmc.org") {
    name = "ParchmentMC"
    content {
      includeGroup("org.parchmentmc.data")
    }
  }
}

dependencies {
  minecraft("com.mojang", "minecraft", providers.gradleProperty("minecraft_version").get())
  mappings(
    loom.layered {
      officialMojangMappings()
      parchment("org.parchmentmc.data:parchment-${providers.gradleProperty("parchment_mappings").get()}@zip")
    },
  )
  modImplementation("net.fabricmc", "fabric-loader", providers.gradleProperty("loader_version").get())
  modImplementation("net.fabricmc.fabric-api", "fabric-api", providers.gradleProperty("fabric_version").get())
}

base {
  archivesName.set(providers.gradleProperty("archives_base_name").get())
}

tasks {
  processResources {
    val templateContext =
      mapOf(
        "version" to version,
        "mc_compatibility" to
          providers.gradleProperty("compatible_minecraft_versions").getOrElse(providers.gradleProperty("minecraft_version").get()),
      )

    inputs.properties(templateContext)
    filesMatching("fabric.mod.json") {
      expand(templateContext)
    }
  }

  withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
  }

  java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  jar {
    // Embed license in output jar
    from("LICENSE") {
      rename { "${it}_${base.archivesName.get()}" }
    }
  }

  withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {
        artifactId = providers.gradleProperty("archives_base_name").get()
        afterEvaluate {
          from(components["java"])
        }
        pom {
          name = "DataBrokers"
          description = "Make Trades Data-Driven"
          url = "https://github.com/jsorrell/DataBrokers"
          licenses {
            license {
              name = "MIT"
            }
          }
        }
      }
    }

    repositories {
      maven {
        val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
        val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
        url = if (version.toString().endsWith("dev")) snapshotsRepoUrl else releasesRepoUrl
      }
    }
  }
}

loom {
  accessWidenerPath.set(file("src/main/resources/databrokers.accesswidener"))
}

fabricApi {
  configureDataGeneration {
    strictValidation = true
  }
}

spotless {
  format("misc") {
    target("*.md", ".gitignore")
    indentWithSpaces(2)
    endWithNewline()
  }

  java {
    target("src/*/java/**/*.java")
    palantirJavaFormat()
    toggleOffOn()
    removeUnusedImports()
    importOrder()
  }

  kotlinGradle {
    target("*.gradle.kts")
    ktlint()
  }

  json {
    target("**/*.json")
    targetExclude("${layout.buildDirectory}/**", "run/**")
    gson().indentWithSpaces(2)
  }

  yaml {
    target("**/*.yml", "**/*.yaml")
    (jackson() as JacksonYamlGradleConfig).yamlFeature("WRITE_DOC_START_MARKER", false)
  }
}

changelog {
  version.set(providers.gradleProperty("mod_version").get())
  repositoryUrl.set(project.extra["repository"] as String)
  introduction.set(
    """
    All notable changes to this project will be documented in this file.

    The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
    """.trimIndent(),
  )
  groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed"))

  // Curseforge markdown doesn't recognize "-"
  itemPrefix.set("*")
}

// modrinth {
//  token.set(providers.environmentVariable("MODRINTH_TOKEN"))
//  projectId.set("carpet-sky-additions")
//  versionNumber.set(providers.gradleProperty("mod_version"))
//  versionName.set("${versions.mod} for Minecraft ${versions.minecraft}")
//  versionType.set("release")
//  uploadFile.set(tasks.remapJar)
//  gameVersions.add(versions.minecraft)
//  loaders.add("fabric")
//  loaders.add("quilt")
//  changelog.set(
//    provider {
//      project.changelog.renderItem(project.changelog.get(versions.mod).withHeader(false).withEmptySections(false).withLinks(false))
//    },
//  )
//  dependencies {
//    required.project("fabric-api")
//    required.project("carpet")
//    required.project("cloth-config")
//    optional.project("modmenu")
//  }
// }
// tasks.modrinth.get().dependsOn(tasks.patchChangelog)
