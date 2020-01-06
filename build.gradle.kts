plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    jcenter()
}

fun target(name: String, resources: String? = null) {
    val os = org.gradle.internal.os.OperatingSystem.current()!!
    kotlin {
        when {
            os.isWindows -> mingwX86(name)
            os.isMacOsX -> macosX64(name)
            os.isLinux -> linuxX64(name)
            else -> throw Error("Unknown host")
        }.binaries.executable {
            if (resources != null && os.isWindows) {
                windowsResources(resources)
                linkerOpts("-mwindows")
            }
        }

        sourceSets {
            all {
                dependencies {
                    implementation("com.github.msink:libui:0.1.6")
                }
            }
        }
    }
}

target("histogram")
target("control")
target("draw")

fun org.jetbrains.kotlin.gradle.plugin.mpp.Executable.windowsResources(rcFileName: String) {
    val taskName = linkTaskName.replaceFirst("link", "windres")
    val inFile = compilation.defaultSourceSet.resources.sourceDirectories.singleFile.resolve(rcFileName)
    val outFile = buildDir.resolve("processedResources/$taskName.res")

    val windresTask = tasks.create<Exec>(taskName) {
        val konanUserDir = System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan"
        val konanLlvmDir = "$konanUserDir/dependencies/msys2-mingw-w64-i686-clang-llvm-lld-compiler_rt-8.0.1/bin"

        inputs.file(inFile)
        outputs.file(outFile)
        commandLine("$konanLlvmDir/windres", inFile, "-D_${buildType.name}", "-O", "coff", "-o", outFile)
        environment("PATH", "$konanLlvmDir;${System.getenv("PATH")}")

        dependsOn(compilation.compileKotlinTask)
    }

    linkTask.dependsOn(windresTask)
    linkerOpts(outFile.toString())
}
