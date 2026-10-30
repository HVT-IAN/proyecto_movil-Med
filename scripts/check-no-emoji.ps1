$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$extensions = @(
    "*.kt",
    "*.kts",
    "*.xml",
    "*.md",
    "*.json",
    "*.rules",
    "*.properties",
    "*.pro",
    "*.gradle",
    "*.toml"
)
$excludedDirectories = @("build", ".gradle", ".idea", ".git")

$files = Get-ChildItem -Path $root -Recurse -File -Include $extensions |
    Where-Object {
        $relative = $_.FullName.Substring($root.Length).TrimStart("\")
        $parts = $relative.Split("\")
        -not ($parts | Where-Object { $excludedDirectories -contains $_ })
    }

$pattern = "[\uD800-\uDBFF][\uDC00-\uDFFF]|[\u2600-\u27BF]|[\u2B00-\u2BFF]|[\u2300-\u23FF]|[\uFE00-\uFE0F]"
$violations = @()

foreach ($file in $files) {
    $lineNumber = 0
    foreach ($line in Get-Content -LiteralPath $file.FullName -Encoding UTF8) {
        $lineNumber++
        if ($line -match $pattern) {
            $violations += [PSCustomObject]@{
                File = $file.FullName.Substring($root.Length).TrimStart("\")
                Line = $lineNumber
                Text = $line.Trim()
            }
        }
    }
}

if ($violations.Count -gt 0) {
    $violations | Format-Table -AutoSize
    Write-Output "Se encontraron $($violations.Count) posibles emojis."
    exit 1
}

Write-Output "Sin emojis: $($files.Count) archivos revisados."
exit 0
