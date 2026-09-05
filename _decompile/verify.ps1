$root = 'g:\Game\FurryNEL_Latest\FurryNEL_Pack_PCL2HMCL'
$jp = "$root\Java\bin\javap.exe"
$j = "$root\.minecraft\mods\4681704866889354274@3@0.jar"
$b = "$j.bak"
$c = 'com.netease.mc.mod.friendplay.GuiOpenEventHandler'
Write-Output '==== CURRENT jar ===='
& $jp -p -classpath $j $c | Select-String 'autoConnected|AUTO_SERVER'
Write-Output '==== BACKUP jar (original) ===='
& $jp -p -classpath $b $c | Select-String 'autoConnected|AUTO_SERVER'
Write-Output ("sizes -> current:{0}  backup:{1}" -f (Get-Item $j).Length, (Get-Item $b).Length)
