plugins {
    idea
    `maven-publish`
    jacoco
    id("net.minecraftforge.gradle") version "[6.0.24,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.2.0"
}

group = "com.bettercontent"
version = property("mod_version") as String
base {
    archivesName.set(property("artifact_name") as String)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("official", property("minecraft_version") as String)
    copyIdeResources = true
    jarJar.enable()

    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.console.level", "debug")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create(property("mod_id") as String) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client")
        create("server") { arg("--nogui") }
        create("gameTestServer") {
            workingDirectory(project.file("run-gametest"))
            property("forge.enableGameTest", "true")
            property("forge.gameTestServer", "true")
            property("forge.enabledGameTestNamespaces", property("mod_id") as String)
            arg("--nogui")
        }
    }
}

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://harleyoconnor.com/maven")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.llamalad7.mixinextras.org/releases/")
    maven("https://maven.valkyrienskies.org") { content { includeGroup("org.valkyrienskies.core") } }
    maven("https://www.cursemaven.com") { content { includeGroup("curse.maven") } }
    mavenCentral()
}

dependencies {
    minecraft("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")
    compileOnly(fg.deobf("curse.maven:hyle-609850:7736352"))
    compileOnly(fg.deobf("curse.maven:thirst-was-taken-679270:6660408"))
    compileOnly(fg.deobf("curse.maven:cold-sweat-506194:7893262"))
    compileOnly(fg.deobf("curse.maven:pollution-of-the-realms-269973:8554528"))
    compileOnly(fg.deobf("curse.maven:little-logistics-570050:4799459"))
    compileOnly(fg.deobf("curse.maven:weather-storms-tornadoes-237746:5244118"))
    compileOnly(fg.deobf("curse.maven:creativecore-257814:7649757"))
    compileOnly(fg.deobf("curse.maven:ambientsounds-254284:7550220"))
    compileOnly(fg.deobf("curse.maven:oculus-581495:6020952"))
    compileOnly(fg.deobf("curse.maven:sophisticated-core-618298:7916595"))
    compileOnly(fg.deobf("curse.maven:sophisticated-storage-619320:7973265"))
    compileOnly(fg.deobf("curse.maven:curios-api-309927:6418456"))
    compileOnly(fg.deobf("curse.maven:mantle-74924:7563777"))
    compileOnly(fg.deobf("curse.maven:tinkers-construct-74072:7449219"))
    compileOnly(fg.deobf("curse.maven:polymorph-388800:6450982"))
    compileOnly(fg.deobf("curse.maven:architectury-api-419699:5137938"))
    compileOnly(fg.deobf("curse.maven:ftb-library-forge-404465:7296748"))
    compileOnly(fg.deobf("curse.maven:ftb-teams-forge-404468:7499810"))
    compileOnly(fg.deobf("curse.maven:ftb-quests-forge-289412:7909594"))
    compileOnly(fg.deobf("curse.maven:epic-fight-mod-405076:8049910"))
    compileOnly(fg.deobf("curse.maven:valkyrien-skies-258371:7906689"))
    compileOnly(fg.deobf("curse.maven:realistic-block-physics-375616:6393411"))
    compileOnly(fg.deobf("curse.maven:realistic-physics-1030082:6026115"))
    compileOnly(fg.deobf("curse.maven:rehooked-1096531:6341096"))
    testRuntimeOnly(fg.deobf("curse.maven:rehooked-1096531:6341096"))
    compileOnly(fg.deobf("curse.maven:patchouli-306770:7731017"))
    compileOnly("org.valkyrienskies.core:api:1.1.0+cf208d8b56")
    runtimeOnly(fg.deobf("curse.maven:thirst-was-taken-679270:6660408"))
    runtimeOnly(fg.deobf("curse.maven:curios-api-309927:6418456"))
    runtimeOnly(fg.deobf("curse.maven:architectury-api-419699:5137938"))
    runtimeOnly(fg.deobf("curse.maven:ftb-library-forge-404465:7296748"))
    runtimeOnly(fg.deobf("curse.maven:ftb-teams-forge-404468:7499810"))
    runtimeOnly(fg.deobf("curse.maven:ftb-filter-system-943925:6466153"))
    runtimeOnly(fg.deobf("curse.maven:ftb-quests-forge-289412:7909594"))
    runtimeOnly(fg.deobf("curse.maven:mantle-74924:7563777"))
    runtimeOnly(fg.deobf("curse.maven:tinkers-construct-74072:7449219"))
    runtimeOnly(fg.deobf("curse.maven:polymorph-388800:6450982"))
    runtimeOnly(fg.deobf("curse.maven:ars-nouveau-401955:6688854"))
    runtimeOnly(fg.deobf("curse.maven:realistic-block-physics-375616:6393411"))
    runtimeOnly(fg.deobf("curse.maven:realistic-physics-1030082:6026115"))
    runtimeOnly(fg.deobf("curse.maven:geckolib-388172:7553267"))
    runtimeOnly(fg.deobf("curse.maven:sophisticated-core-618298:7916595"))
    runtimeOnly(fg.deobf("curse.maven:sophisticated-storage-619320:7973265"))
    runtimeOnly(fg.deobf("com.ferreusveritas.dynamictrees:DynamicTrees-1.20.1:1.4.9"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.google.code.gson:gson:2.10.1")
}

tasks.named<Jar>("jar") {
    finalizedBy("reobfJar")
}

val stageRuntimeJar by tasks.registering(Copy::class) {
    group = "build"
    description = "Stages the reobfuscated runtime jar into build/libs using the canonical release filename."
    dependsOn(tasks.named("reobfJar"))
    mustRunAfter(tasks.named("jarJar"))
    mustRunAfter(tasks.named("reobfJarJar"))
    from(layout.buildDirectory.file("reobfJar/output.jar"))
    into(layout.buildDirectory.dir("libs"))
    rename { "${base.archivesName.get()}-$version.jar" }
}

tasks.named("assemble") {
    dependsOn(stageRuntimeJar)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register("headlessGameTest") {
    group = "verification"
    description = "Runs Forge game tests in a headless dedicated server."
    dependsOn(tasks.named("runGameTestServer"))
}

tasks.register("verifyFast") {
    group = "verification"
    description = "Runs deterministic unit/resource checks without Forge game tests."
    dependsOn(tasks.named("check"))
}

tasks.register("verifyFull") {
    group = "verification"
    description = "Runs the full verification lane including headless Forge game tests."
    dependsOn(tasks.named("verifyFast"))
    dependsOn(tasks.named("headlessGameTest"))
}

val resetGameTestMods = tasks.register<Delete>("resetGameTestMods") {
    delete(layout.projectDirectory.dir("run-gametest/mods"))
}

val syncGameTestStructures = tasks.register<Sync>("syncGameTestStructures") {
    from(layout.projectDirectory.dir("src/main/resources/gameteststructures"))
    into(layout.projectDirectory.dir("run-gametest/gameteststructures"))
}

tasks.matching { it.name.startsWith("prepareRunGameTestServer") }.configureEach {
    dependsOn(resetGameTestMods)
    dependsOn(syncGameTestStructures)
}

tasks.processResources {
    val props = mapOf(
        "minecraft_version" to project.property("minecraft_version"),
        "forge_version" to project.property("forge_version"),
        "mod_id" to project.property("mod_id"),
        "mod_name" to project.property("mod_name"),
        "mod_version" to project.property("mod_version")
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(props)
    }
}


