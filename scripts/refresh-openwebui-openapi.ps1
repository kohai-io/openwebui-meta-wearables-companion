param(
  [string] $BaseUrl = $env:OWUI_BASE_URL,
  [string] $OutputPath = "docs/openwebui/openapi.snapshot.json"
)

$ErrorActionPreference = "Stop"

if (Test-Path ".env") {
  Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -ne "" -and -not $line.StartsWith("#") -and $line.Contains("=")) {
      $key, $value = $line.Split("=", 2)
      $key = $key.Trim()
      $value = $value.Trim().Trim('"').Trim("'")
      if ($key -and -not [Environment]::GetEnvironmentVariable($key, "Process")) {
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
      }
    }
  }
  if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    $BaseUrl = $env:OWUI_BASE_URL
  }
}

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
  throw "OWUI_BASE_URL is required. Set it in your environment or pass -BaseUrl."
}

$normalizedBaseUrl = $BaseUrl.TrimEnd("/")
$openApiUrl = "$normalizedBaseUrl/openapi.json"
$outputDirectory = Split-Path -Parent $OutputPath

if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
  New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}

Write-Host "Fetching $openApiUrl"
Invoke-WebRequest -Uri $openApiUrl -OutFile $OutputPath
Write-Host "Wrote $OutputPath"
