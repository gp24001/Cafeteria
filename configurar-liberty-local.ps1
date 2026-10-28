<#+.SYNOPSIS
Completa la configuración local de claves SSL y LTPA de cafefeServer.
.DESCRIPTION
Lee la instalación y el nombre del servidor desde pom.xml. Conserva server.xml
y los datos de PostgreSQL. Crea contraseñas aleatorias en server.env y un
override local que las referencia, sin imprimirlas ni guardarlas en el proyecto.
Puede ejecutarse nuevamente sin cambiar las contraseñas ya generadas.
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
[xml]$pomLocal = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'pom.xml') -Raw
$nsLocal = New-Object System.Xml.XmlNamespaceManager($pomLocal.NameTable)
$nsLocal.AddNamespace('m', 'http://maven.apache.org/POM/4.0.0')
$pluginLocal = $pomLocal.SelectSingleNode("//m:plugin[m:artifactId='liberty-maven-plugin']/m:configuration", $nsLocal)
if ($null -eq $pluginLocal) { throw 'No se encontró la configuración de Liberty en pom.xml.' }
$instalacionLocal = $pluginLocal.SelectSingleNode('m:installDirectory', $nsLocal).InnerText.Trim()
$nombreServidorLocal = $pluginLocal.SelectSingleNode('m:serverName', $nsLocal).InnerText.Trim()
$directorioServidorLocal = [System.IO.Path]::GetFullPath((Join-Path $instalacionLocal "usr\servers\$nombreServidorLocal"))
if (-not (Test-Path -LiteralPath (Join-Path $directorioServidorLocal 'server.xml'))) {
    throw "No existe server.xml en el servidor configurado: $directorioServidorLocal"
}
$archivoEntornoLocal = Join-Path $directorioServidorLocal 'server.env'
$archivoSeguridadLocal = Join-Path $directorioServidorLocal 'configDropins\overrides\cafefe-security.xml'
foreach ($rutaLocal in @($archivoEntornoLocal, $archivoSeguridadLocal)) {
    if (-not [System.IO.Path]::GetFullPath($rutaLocal).StartsWith(
            $directorioServidorLocal + [System.IO.Path]::DirectorySeparatorChar,
            [System.StringComparison]::OrdinalIgnoreCase)) {
        throw 'La configuración debe permanecer dentro del servidor indicado en pom.xml.'
    }
}
$textoEntornoLocal = if (Test-Path -LiteralPath $archivoEntornoLocal) {
    [System.IO.File]::ReadAllText($archivoEntornoLocal)
} else { '' }
$variablesNuevasLocal = @()
foreach ($variableLocal in @('CAFEFE_KEYSTORE_PASSWORD', 'CAFEFE_LTPA_PASSWORD')) {
    if ($textoEntornoLocal -notmatch "(?m)^$variableLocal=.+$") {
        $claveLocal = [Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
        $variablesNuevasLocal += "$variableLocal=$claveLocal"
    }
}
$fechaRespaldoLocal = Get-Date -Format 'yyyyMMdd-HHmmss'
if ($variablesNuevasLocal.Count -gt 0) {
    if (Test-Path -LiteralPath $archivoEntornoLocal) {
        Copy-Item -LiteralPath $archivoEntornoLocal -Destination "$archivoEntornoLocal.$fechaRespaldoLocal.bak"
    }
    $nuevoEntornoLocal = $textoEntornoLocal.TrimEnd() + "`r`n" + ($variablesNuevasLocal -join "`r`n") + "`r`n"
    [System.IO.File]::WriteAllText($archivoEntornoLocal, $nuevoEntornoLocal, [System.Text.UTF8Encoding]::new($false))
}
$xmlSeguridadLocal = @'
<?xml version="1.0" encoding="UTF-8"?>
<server>
    <!-- Claves locales independientes; no se sustituyen los archivos de claves anteriores. -->
    <keyStore id="defaultKeyStore"
              location="${server.config.dir}/resources/security/cafefe-local.p12"
              password="${env.CAFEFE_KEYSTORE_PASSWORD}"/>
    <ltpa keysFileName="${server.config.dir}/resources/security/cafefe-local-ltpa.keys"
          keysPassword="${env.CAFEFE_LTPA_PASSWORD}"/>
</server>
'@
if (Test-Path -LiteralPath $archivoSeguridadLocal) {
    $anteriorSeguridadLocal = [System.IO.File]::ReadAllText($archivoSeguridadLocal)
    if ($anteriorSeguridadLocal -ne $xmlSeguridadLocal) {
        Copy-Item -LiteralPath $archivoSeguridadLocal -Destination "$archivoSeguridadLocal.$fechaRespaldoLocal.bak"
    }
}
New-Item -ItemType Directory -Path (Split-Path -Parent $archivoSeguridadLocal) -Force | Out-Null
[System.IO.File]::WriteAllText($archivoSeguridadLocal, $xmlSeguridadLocal, [System.Text.UTF8Encoding]::new($false))
Write-Output "Configuración local lista para $nombreServidorLocal."
Write-Output 'server.xml y la configuración de PostgreSQL se conservaron.'
Write-Output 'Las contraseñas permanecen en server.env; reinicie el servidor para cargarlas.'
