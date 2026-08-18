import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    kotlin("jvm") version "2.2.21"
}

fun property(name: String): String = providers.gradleProperty(name).get()

val minecraftVersion = property("minecraft_version")
val forgeVersion = property("forge_version")
val mappingsChannelName = property("mapping_channel")
val mappingsVersionName = property("mapping_version")
val kffVersion = property("kff_version")
val curiosVersion = property("curios_version")
val modId = property("mod_id")
val modName = property("mod_name")
val modLicense = property("mod_license")
val modVersion = property("mod_version")
val modGroupId = property("mod_group_id")
val modAuthors = property("mod_authors")
val modDescription = property("mod_description")
val minecraftVersionRange = property("minecraft_version_range")
val forgeVersionRange = property("forge_version_range")
val loaderVersionRange = property("loader_version_range")
val kffVersionRange = property("kff_version_range")

version = modVersion
group = modGroupId

base {
    archivesName.set(modId)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    jvmToolchain(17)
}

configure<UserDevExtension> {
    mappings(mappingsChannelName, mappingsVersionName)
}

repositories {
    mavenCentral()
    maven {
        name = "KotlinForForge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top/")
    }
}

dependencies {
    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    implementation("thedarkcolour:kotlinforforge:$kffVersion")

    compileOnly(fg.deobf("top.theillusivec4.curios:curios-forge:$curiosVersion:api"))
    if (providers.gradleProperty("includeCurios").orNull.toBoolean()) {
        runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:$curiosVersion"))
    }
}

val replaceProperties = mapOf(
    "minecraft_version" to minecraftVersion,
    "minecraft_version_range" to minecraftVersionRange,
    "forge_version" to forgeVersion,
    "forge_version_range" to forgeVersionRange,
    "loader_version_range" to loaderVersionRange,
    "kff_version_range" to kffVersionRange,
    "mod_id" to modId,
    "mod_name" to modName,
    "mod_license" to modLicense,
    "mod_version" to modVersion,
    "mod_authors" to modAuthors,
    "mod_description" to modDescription
)

tasks.processResources {
    inputs.properties(replaceProperties)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(
            "Specification-Title" to modId,
            "Specification-Vendor" to modAuthors,
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to archiveVersion.get(),
            "Implementation-Vendor" to modAuthors,
            "MixinConfigs" to "jump_enchants.mixins.json"
        )
    }
    finalizedBy("reobfJar")
}
