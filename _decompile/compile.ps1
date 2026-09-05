$ErrorActionPreference = 'Continue'
$root = 'g:\Game\FurryNEL_Latest\FurryNEL_Pack_PCL2HMCL'
$mc = "$root\.minecraft"
$javac = "$root\Java\bin\javac.exe"
$src = @(
    "$root\_decompile\src\com\netease\mc\mod\filter\FilterHelper.java",
    "$root\_decompile\src\com\netease\mc\mod\skin\SkinHandler.java"
)
$out = "$root\_decompile\out"
$errlog = "$root\_decompile\javac_err.log"
$outlog = "$root\_decompile\javac_out.log"
New-Item -ItemType Directory -Force -Path $out | Out-Null

$lines = Get-Content "$root\authproxy\gameargs.txt"
$cpidx = [Array]::IndexOf($lines, '-cp')
if ($cpidx -lt 0) { throw 'no -cp line in gameargs.txt' }
$cp = $lines[$cpidx + 1]
$cpAbs = (($cp -split ';' | ForEach-Object { if ($_) { Join-Path $mc $_ } }) -join ';')
$netease = "$mc\mods\4681704866889354274@3@0.jar"
$cpFull = "$cpAbs;$netease"

$allArgs = @('-encoding','UTF-8','-proc:none','-cp',"$cpFull",'-d',"$out") + $src
Write-Output "compiling FilterHelper.java + SkinHandler.java ..."
& $javac $allArgs 2>&1 | ForEach-Object { $_ }
$exit = $LASTEXITCODE
Write-Output "javac exit=$exit"
