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
    compileOnly(project(":SynapseAPI"))
    implementation(libs.avro4k)
    testImplementation(projects.kotlinLib)
    testImplementation("cn.nukkit:nukkit")
    testImplementation(projects.synapseAPI)
    testImplementation(kotlin("test"))
}

description = "Ghosty"
