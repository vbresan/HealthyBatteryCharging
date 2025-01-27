param (
    [Parameter(Mandatory=$true,Position=0)]
    [int]$PortNumber
)

& $Env:LOCALAPPDATA\Android\sdk\platform-tools\adb.exe connect 192.168.2.162:$PortNumber
