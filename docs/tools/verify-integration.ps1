param([Parameter(Mandatory=$true)][string]$TestServerUrl)
$ErrorActionPreference='Stop'
# Use a dedicated local test server; never pass a shared or production server.
if ($TestServerUrl -notmatch '^jdbc:mysql://(127\.0\.0\.1|localhost):\d+/$') {
    throw 'Use jdbc:mysql://127.0.0.1:PUERTO/ para una instancia local de pruebas.'
}
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$database = 'erp_inventory_test_audit_' + [guid]::NewGuid().ToString('N')
$previousUrl = $env:ERP_DB_URL
$previousSchema = $env:ERP_TEST_SCHEMA_FILE
try {
    $env:ERP_DB_URL = $TestServerUrl + $database + '?createDatabaseIfNotExist=true&serverTimezone=America/Bogota'
    $env:ERP_TEST_SCHEMA_FILE = Join-Path $repo 'docs/database.sql'
    Write-Output ('Base de verificación: ' + $database)
    Push-Location -LiteralPath $repo
    try {
        & mvn -B test-compile
        if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas.' }
        $schemaArgument = '-Dexec.args="' + $env:ERP_TEST_SCHEMA_FILE.Replace('\','/') + '"'
        & mvn -B exec:java '-Dexec.mainClass=frescuratotal.erp.IntegratedWorkflowProbe' '-Dexec.classpathScope=test' $schemaArgument
        if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación del flujo integrado.' }
    } finally { Pop-Location }
} finally {
    $env:ERP_DB_URL = $previousUrl
    $env:ERP_TEST_SCHEMA_FILE = $previousSchema
}
