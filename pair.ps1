param (
    [Parameter(Mandatory=$true,Position=0)]
    [string]$LastIPOctetPort
)

# Split the input string into the last octet and port
$lastIPOctet, $portNumber = $LastIPOctetPort.Split(':')

$IPAddress = "192.168.2.$lastIPOctet"
& $Env:LOCALAPPDATA\Android\sdk\platform-tools\adb.exe connect "$IPAddress`:$portNumber"
