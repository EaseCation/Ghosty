plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.lombok")
}

extra.set("copyTo", "{server}/plugins")

dependencies {
    compileOnly(projects.kotlinLib)
    compileOnly("cn.nukkit:nukkit")
    compileOnly(libs.apache.commons.compress) // from nukkit
    testImplementation(projects.kotlinLib)
    testImplementation("cn.nukkit:nukkit")
    testImplementation(kotlin("test"))
    val synapse = loadSynapase(project)
    compileOnly(synapse)
    testImplementation(synapse)
    testImplementation(libs.avro4k)
}

description = "Ghosty"
