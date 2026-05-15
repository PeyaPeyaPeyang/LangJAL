param(
    [string]$JarPath = "compiler/build/libs/jalc.jar",
    [string]$ExamplesDir = "examples",
    [string]$ExpectedDir = "examples/outs",
    [string[]]$ExampleNames = @()
)

$ErrorActionPreference = "Stop"

function Normalize-Output {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return $Text.Replace("`r`n", "`n").TrimEnd("`n")
}

function Get-ClassNameFromFile {
    param(
        [string]$RootDir,
        [string]$ClassFile
    )

    $relative = [System.IO.Path]::GetRelativePath($RootDir, $ClassFile)
    $withoutExtension = $relative.Substring(0, $relative.Length - [System.IO.Path]::GetExtension($relative).Length)
    return ($withoutExtension -replace "[\\/]", ".")
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$resolvedJarPath = (Resolve-Path (Join-Path $repoRoot $JarPath)).Path
$resolvedExamplesDir = (Resolve-Path (Join-Path $repoRoot $ExamplesDir)).Path
$resolvedExpectedDir = (Resolve-Path (Join-Path $repoRoot $ExpectedDir)).Path

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $resolvedExamplesDir "build\test-output\$timestamp"
New-Item -ItemType Directory -Path $outputRoot | Out-Null

if ($ExampleNames.Count -eq 0) {
    $exampleFiles = Get-ChildItem -Path $resolvedExamplesDir -Filter *.jal | Sort-Object Name
} else {
    $exampleFiles = foreach ($exampleName in $ExampleNames) {
        $fileName = if ($exampleName.EndsWith(".jal")) { $exampleName } else { "$exampleName.jal" }
        $fullPath = Join-Path $resolvedExamplesDir $fileName
        if (-not (Test-Path $fullPath)) {
            throw "Example file not found: $fileName"
        }
        Get-Item $fullPath
    }
}

$failures = New-Object System.Collections.Generic.List[string]

foreach ($exampleFile in $exampleFiles) {
    $exampleName = [System.IO.Path]::GetFileNameWithoutExtension($exampleFile.Name)
    $expectedFile = Join-Path $resolvedExpectedDir "$exampleName.out.txt"
    if (-not (Test-Path $expectedFile)) {
        $failures.Add("[$exampleName] Expected output file not found: $expectedFile")
        continue
    }

    $compileOutputDir = Join-Path $outputRoot $exampleName
    New-Item -ItemType Directory -Path $compileOutputDir | Out-Null

    Write-Host "Compiling $($exampleFile.Name)"
    & java -jar $resolvedJarPath $exampleFile.FullName --output $compileOutputDir
    if ($LASTEXITCODE -ne 0) {
        $failures.Add("[$exampleName] Compilation failed.")
        continue
    }

    $classFile = Get-ChildItem -Path $compileOutputDir -Recurse -Filter *.class | Select-Object -First 1
    if ($null -eq $classFile) {
        $failures.Add("[$exampleName] No class file was generated.")
        continue
    }

    $className = Get-ClassNameFromFile -RootDir $compileOutputDir -ClassFile $classFile.FullName

    Write-Host "Running $className"
    $actualOutput = & java -cp $compileOutputDir $className | Out-String
    if ($LASTEXITCODE -ne 0) {
        $failures.Add("[$exampleName] Execution failed.")
        continue
    }

    $expectedOutput = Get-Content -Raw -Path $expectedFile
    $normalizedActual = Normalize-Output -Text $actualOutput
    $normalizedExpected = Normalize-Output -Text $expectedOutput

    if ($normalizedActual -ne $normalizedExpected) {
        $failures.Add(@"
[$exampleName] Output mismatch.
Expected:
$normalizedExpected
Actual:
$normalizedActual
"@.Trim())
        continue
    }

    Write-Host "PASS $exampleName"
}

if ($failures.Count -gt 0) {
    Write-Host ""
    Write-Host "Failures:"
    foreach ($failure in $failures) {
        Write-Host $failure
        Write-Host ""
    }
    exit 1
}

Write-Host ""
Write-Host "All examples passed."
