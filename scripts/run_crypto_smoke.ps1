$deps = Get-Content -Raw ai-middleware/target/classpath.txt
$cp = "ai-middleware/target/classes;$deps"

$keyBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$keyBase64 = [Convert]::ToBase64String($keyBytes)

java "-Dfile.encoding=UTF-8" "-Dai.crypto.enabled=true" "-Dai.crypto.key.base64=$keyBase64" "-Dai.crypto.key.id=dev-key" "-Dai.crypto.key.version=v1" -cp $cp com.mes.ai.tools.CryptoServiceSmokeRunner
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" "-Dai.crypto.enabled=true" "-Dai.crypto.key.base64=$keyBase64" "-Dai.crypto.key.id=dev-key" "-Dai.crypto.key.version=v1" "-Dai.http.port=18080" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
