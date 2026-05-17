rootProject.name = "JavaAssemblyLanguage"
include("core")
include("compiler")
project(":core").name = "langjal"
include("jalc")
include("jalp")
