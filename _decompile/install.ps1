$root = 'g:\Game\FurryNEL_Latest\FurryNEL_Pack_PCL2HMCL'
$jar = "$root\.minecraft\mods\4681704866889354274@3@0.jar"
$bak = "$jar.bak"
$out = "$root\_decompile\out"
$jarTool = "$root\Java\bin\jar.exe"

# backup
Copy-Item $jar $bak -Force
Write-Output "backup created: $bak"

# install FilterHelper + SkinHandler classes
Push-Location $out
$classes = @(
    'com\netease\mc\mod\filter\FilterHelper.class',
    'com\netease\mc\mod\filter\FilterHelper$1.class',
    'com\netease\mc\mod\filter\FilterHelper$2.class',
    'com\netease\mc\mod\filter\FilterHelper$ReviewCode.class',
    'com\netease\mc\mod\skin\SkinHandler.class',
    'com\netease\mc\mod\skin\SkinHandler$1.class',
    'com\netease\mc\mod\skin\SkinHandler$2.class',
    'com\netease\mc\mod\skin\SkinHandler$3.class',
    'com\netease\mc\mod\skin\SkinHandler$4.class'
)
& $jarTool uf $jar $classes
Write-Output "jar update exit=$LASTEXITCODE"
Pop-Location
