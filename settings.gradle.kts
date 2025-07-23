rootProject.name = "JavaAssemblyLanguage"
include("core")
include("compiler")
project(":core").name = "langjal"
project(":compiler").name = "jalc"
