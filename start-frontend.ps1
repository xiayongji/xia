param(
    [string]$ProjectPath = "c:\Users\user\Desktop\毕业设计"
)

Write-Host "启动前端开发服务器..." -ForegroundColor Cyan
Set-Location -Path $ProjectPath
& "C:\Program Files\nodejs\node.exe" node_modules\vite\bin\vite.js