param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $MavenArgs
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$propertiesPath = Join-Path $scriptDir "maven-wrapper.properties"
$properties = @{}

Get-Content -LiteralPath $propertiesPath | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]+?)\s*=\s*(.+)\s*$") {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$distributionUrl = $properties["distributionUrl"]
$expectedSha256 = $properties["distributionSha256Sum"]

if (-not $distributionUrl) {
    throw "distributionUrl is missing in $propertiesPath"
}

$fileName = Split-Path -Leaf $distributionUrl
$mavenDirName = $fileName -replace "-bin\.zip$", ""
$zipPath = Join-Path $scriptDir $fileName
$mavenHome = Join-Path $scriptDir $mavenDirName
$mvnCmd = Join-Path $mavenHome "bin\mvn.cmd"

if (-not (Test-Path -LiteralPath $mvnCmd)) {
    New-Item -ItemType Directory -Force -Path $scriptDir | Out-Null

    if (-not (Test-Path -LiteralPath $zipPath)) {
        Write-Host "Downloading Maven $mavenDirName..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $distributionUrl -OutFile $zipPath
    }

    if ($expectedSha256) {
        $actualSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $zipPath).Hash.ToLowerInvariant()
        if ($actualSha256 -ne $expectedSha256.ToLowerInvariant()) {
            Remove-Item -LiteralPath $zipPath -Force
            throw "Maven distribution checksum mismatch. Expected $expectedSha256 but got $actualSha256."
        }
    }

    $tmpDir = Join-Path $scriptDir "$mavenDirName.tmp"
    if (Test-Path -LiteralPath $tmpDir) {
        Remove-Item -LiteralPath $tmpDir -Recurse -Force
    }

    Expand-Archive -LiteralPath $zipPath -DestinationPath $tmpDir -Force
    Move-Item -LiteralPath (Join-Path $tmpDir $mavenDirName) -Destination $scriptDir -Force
    Remove-Item -LiteralPath $tmpDir -Recurse -Force
}

& $mvnCmd @MavenArgs
exit $LASTEXITCODE
