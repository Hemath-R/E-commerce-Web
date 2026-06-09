$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$rand = Get-Random -Maximum 999999
$email = "test$rand@example.com"
$pwd = 'password123'
Write-Output "Registering: $email"
$body = @{ fullName = 'Test User'; email = $email; password = $pwd } | ConvertTo-Json
try {
    $reg = Invoke-RestMethod -Uri "$base/auth/register" -Method Post -ContentType 'application/json' -Body $body
    Write-Output "Register response:`n$(ConvertTo-Json $reg -Depth 5)"
} catch { Write-Error "Register failed: $_" }

try {
    $loginBody = @{ email = $email; password = $pwd } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType 'application/json' -Body $loginBody
    Write-Output "Login response:`n$(ConvertTo-Json $login -Depth 5)"
    $token = $login.data.token
    Write-Output "Token: $token"
} catch { Write-Error "Login failed: $_"; exit 1 }

$headers = @{ Authorization = "Bearer $token" }
try {
    # Quick test: use Invoke-WebRequest to capture HTTP status when Authorization header present
    $resp = Invoke-WebRequest -Uri "$base/products" -Method Get -Headers $headers -UseBasicParsing -ErrorAction Stop
    Write-Output "Products fetch with Authorization status: $($resp.StatusCode)"
} catch {
    Write-Error "Products fetch with Authorization failed: $_"
}
try {
    $products = Invoke-RestMethod -Uri "$base/products" -Method Get -Headers $headers
    Write-Output "Products list:`n$(ConvertTo-Json $products -Depth 5)"
    $first = $products.data.content[0]
    Write-Output "First product id: $($first.id)"
} catch { Write-Error "Products fetch failed: $_"; exit 1 }

try {
    $prod = Invoke-RestMethod -Uri "$base/products/$($first.id)" -Method Get -Headers $headers
    Write-Output "Product detail:`n$(ConvertTo-Json $prod -Depth 6)"
} catch { Write-Error "Product detail failed: $_"; exit 1 }

try {
    $addBody = @{ productId = $first.id; quantity = 1; size = '9' } | ConvertTo-Json
    $add = Invoke-RestMethod -Uri "$base/cart" -Method Post -ContentType 'application/json' -Headers $headers -Body $addBody
    Write-Output "Add to cart response:`n$(ConvertTo-Json $add -Depth 6)"
} catch { Write-Error "Add to cart failed: $_"; exit 1 }

try {
    $cart = Invoke-RestMethod -Uri "$base/cart" -Method Get -Headers $headers
    Write-Output "Cart:`n$(ConvertTo-Json $cart -Depth 6)"
} catch { Write-Error "Get cart failed: $_"; exit 1 }

try {
    $orderBody = @{ shippingName='Test User'; shippingEmail=$email; shippingPhone='9999999999'; shippingAddress='123 Test St'; shippingCity='TestCity'; shippingState='TS'; shippingPincode='123456' } | ConvertTo-Json
    $order = Invoke-RestMethod -Uri "$base/orders" -Method Post -ContentType 'application/json' -Headers $headers -Body $orderBody
    Write-Output "Order creation response:`n$(ConvertTo-Json $order -Depth 6)"
} catch { Write-Error "Order creation failed: $_"; exit 1 }

Write-Output 'Flow complete.'
