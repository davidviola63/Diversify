
param(
    [Parameter(Mandatory = $true)]
    [string]$RunDirectory
)

$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

try {

    $config = Get-Content `
        "$RunDirectory\config.json" -Raw |
        ConvertFrom-Json

    $dependencies = (Get-Content `
        ".\target\benchmark-classpath.txt" -Raw).Trim()

    $classpath = "target\test-classes;target\classes;$dependencies"

    $java = Join-Path $env:JAVA_HOME "bin\java.exe"

    $benchmarkRegex = "^" +
        [regex]::Escape($config.Benchmark) + '$'

    $javaArgs = @(
        "-cp"
        $classpath
        "org.openjdk.jmh.Main"
        $benchmarkRegex
        "-wi"
        "5"
        "-w"
        "3s"
        "-i"
        "10"
        "-r"
        "3s"
        "-f"
        "3"
        "-t"
        "1"
        "-bm"
        "avgt"
        "-tu"
        "us"
        "-prof"
        "gc"
        "-foe"
        "true"
        "-rf"
        "json"
        "-rff"
        "$RunDirectory\performance.json"
    )

    if ($null -ne $config.Parameters) {

        foreach ($parameter in $config.Parameters.PSObject.Properties) {

            $javaArgs += "-p"
            $javaArgs += "$($parameter.Name)=$($parameter.Value)"
        }
    }

    & $java @javaArgs

    if ($LASTEXITCODE -ne 0) {
        throw "JMH terminato con codice $LASTEXITCODE"
    }

    if (-not (Test-Path "$RunDirectory\performance.json")) {
        throw "File JSON dei risultati mancante."
    }

    $data = @(Get-Content `
        "$RunDirectory\performance.json" -Raw |
        ConvertFrom-Json)

    if ($data.Count -ne 1) {
        throw "Atteso esattamente un risultato JMH."
    }

    if ($data[0].benchmark -ne $config.Benchmark) {
        throw "Il benchmark eseguito non corrisponde a quello richiesto."
    }

    foreach ($parameter in $config.Parameters.PSObject.Properties) {

        if ($data[0].params.($parameter.Name) -ne $parameter.Value) {
            throw "Parametro JMH non corrispondente."
        }
    }

    Set-Content "$RunDirectory\success.flag" "SUCCESS"

    exit 0

} catch {

    Write-Error $_ -ErrorAction Continue

    exit 1
}