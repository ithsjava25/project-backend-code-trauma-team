param(
    [switch]$NoDockerCheck,
    [switch]$Serious
)

$ErrorActionPreference = "Stop"

if ($args -contains "--serious") {
    $Serious = $true
}

function Assert-CommandExists {
    param([string]$CommandName)

    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        throw "Command '$CommandName' was not found. Install it and try again."
    }
}

function Invoke-CommandWithMode {
    param(
        [scriptblock]$Command
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $tempLog = $null

    try {
        $ErrorActionPreference = "Continue"

        if ($Serious) {
            & $Command 2>&1
        }
        else {
            $tempLog = [System.IO.Path]::GetTempFileName()
            & $Command *> $tempLog

            if ($LASTEXITCODE -ne 0) {
                Write-Host "Command failed in quiet mode (exit code: $LASTEXITCODE)." -ForegroundColor Red
                Write-Host "Recent output from ${tempLog}:" -ForegroundColor DarkRed
                Get-Content -Path $tempLog -Tail 25 | ForEach-Object { Write-Host $_ -ForegroundColor DarkGray }
            }
        }

        return $LASTEXITCODE
    }
    finally {
        if ($tempLog -and (Test-Path $tempLog)) {
            Remove-Item -Path $tempLog -Force -ErrorAction SilentlyContinue
        }
        $ErrorActionPreference = $previousErrorActionPreference
    }
}

function Write-FunStatus {
    param(
        [string[]]$Messages,
        [ConsoleColor]$Color = [ConsoleColor]::Cyan
    )

    if ($Messages -and $Messages.Count -gt 0) {
        $index = Get-Random -Minimum 0 -Maximum $Messages.Count
        Write-Host $Messages[$index] -ForegroundColor $Color
    }
}

function Pause-ForEnjoyment {
    if (-not $Serious) {
        Start-Sleep -Seconds 2
    }
}

function Open-BrowserUrl {
    param([string]$Url)

    try {
        if ($IsWindows -or $env:OS -eq "Windows_NT") {
            Start-Process $Url
            return
        }

        if ($IsMacOS) {
            & open $Url
            return
        }

        if ($IsLinux) {
            & xdg-open $Url
            return
        }

        # Fallback for uncommon PowerShell hosts.
        Start-Process $Url
    }
    catch {
        Write-Warning "Could not automatically open browser. Open this URL manually: $Url"
    }
}

if ($Serious) {
    Write-Host "Starting local development environment (serious mode)..." -ForegroundColor Cyan
}
else {
    Write-FunStatus -Messages @(
        "Waking up the dev servers..."
        "Turning caffeine into deployment energy..."
        "Calibrating keyboard clicks and backend magic..."
    ) -Color Cyan
}
Pause-ForEnjoyment

Assert-CommandExists -CommandName "docker"

if (-not $NoDockerCheck) {
    if ($Serious) {
        Write-Host "Checking Docker engine..." -ForegroundColor Yellow
    }
    else {
        Write-FunStatus -Messages @(
            "Knocking on Docker's front door..."
            "Asking Docker politely to wake up..."
            "Reading Docker's morning mood..."
        ) -Color Yellow
    }
    Pause-ForEnjoyment
    $engineReady = $false
    $maxAttempts = 12

    for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
        $dockerInfoExitCode = Invoke-CommandWithMode -Command { docker info }
        if ($dockerInfoExitCode -eq 0) {
            $engineReady = $true
            break
        }

        if ($attempt -lt $maxAttempts) {
            Write-FunStatus -Messages @(
                "Fetching coffee for the engineers... Docker not ready yet ($attempt/$maxAttempts). Retrying in 5 seconds..."
                "Asking the marketing department to leave... Docker still booting ($attempt/$maxAttempts). Retrying in 5 seconds..."
                "Negotiating with the container gods... still waiting ($attempt/$maxAttempts). Retrying in 5 seconds..."
            ) -Color DarkYellow
            Start-Sleep -Seconds 5
        }
    }

    if (-not $engineReady) {
        throw @"
Docker engine is not available yet.
Start Docker Desktop, wait until 'Engine running', then run this script again.
Tip: verify manually with 'docker info' (it should return without daemon errors).
"@
    }
}

if ($Serious) {
    Write-Host "Starting infrastructure services (PostgreSQL + MinIO)..." -ForegroundColor Yellow
}
else {
    Write-FunStatus -Messages @(
        "Summoning PostgreSQL and MinIO..."
        "Spinning up databases and object storage wizardry..."
        "Hiring tiny container elves for infrastructure..."
    ) -Color Yellow
}
Pause-ForEnjoyment
$composeExitCode = Invoke-CommandWithMode -Command { docker compose up -d }

if ($composeExitCode -ne 0) {
    throw "docker compose failed. Fix the error above and retry."
}

if ($Serious) {
    Write-Host "Starting Spring Boot with profile 'local'..." -ForegroundColor Yellow
}
else {
    Write-FunStatus -Messages @(
        "Launching Spring Boot thrusters..."
        "Convincing Java to do something useful..."
        "Whispering encouragement to the JVM..."
    ) -Color Yellow
}
Write-Host "App URL: http://localhost:8080" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the application." -ForegroundColor Green
Write-Host "Admin login (local profile):" -ForegroundColor Magenta
Write-Host "Username: admin@traumateam.com" -ForegroundColor Magenta
Write-Host "Password: password" -ForegroundColor Magenta
Write-Host "Starting web browser for your lazy a**" -ForegroundColor Green
Write-Host "Opening browser at http://localhost:8080 ..." -ForegroundColor Green
if (-not $Serious) {
    Write-Host "Quiet mode active: app logs are hidden while running." -ForegroundColor DarkGreen
    Write-Host "If the prompt does not return, the app is still running in this terminal." -ForegroundColor DarkGreen
    Write-Host "Use --serious to see full startup logs." -ForegroundColor DarkGreen
}
Pause-ForEnjoyment
Open-BrowserUrl -Url "http://localhost:8080"

$mavenExitCode = Invoke-CommandWithMode -Command { .\mvnw spring-boot:run "-Dspring-boot.run.profiles=local" }

if ($mavenExitCode -ne 0) {
    if ($mavenExitCode -in @(130, 1)) {
        Write-Warning "Spring Boot terminated by user (Ctrl+C)"
    }
    else {
        throw "Spring Boot failed with exit code $mavenExitCode. Re-run with --serious to see detailed logs."
    }
}
