Set WshShell = CreateObject("WScript.Shell")

' 获取当前脚本所在目录
strScriptPath = WScript.ScriptFullName
strScriptDir = Left(strScriptPath, InStrRev(strScriptPath, "\"))

' 快捷方式目标路径 - PowerShell脚本
strTargetPath = "powershell.exe"
strArguments = "-ExecutionPolicy Bypass -File """ & strScriptDir & "start-all-services.ps1"""

' 桌面路径
strDesktopPath = WshShell.SpecialFolders("Desktop")

' 快捷方式路径
strShortcutPath = strDesktopPath & "\智能家居系统.lnk"

' 创建快捷方式对象
Set objShortcut = WshShell.CreateShortcut(strShortcutPath)

' 设置快捷方式属性
objShortcut.TargetPath = strTargetPath
objShortcut.Arguments = strArguments
objShortcut.WorkingDirectory = strScriptDir
objShortcut.Description = "启动智能家居系统 - 包含前端(3000)和后端所有服务"
objShortcut.WindowStyle = 1 ' 普通窗口
objShortcut.IconLocation = "shell32.dll,15" ' 使用文件夹图标

' 保存快捷方式
objShortcut.Save

' 提示用户
MsgBox "桌面快捷方式已创建成功！" & vbCrLf & vbCrLf & _
       "快捷方式位置: " & strShortcutPath & vbCrLf & _
       "目标文件: " & strScriptDir & "start-all-services.ps1" & vbCrLf & vbCrLf & _
       "访问地址: http://localhost:3000", vbInformation, "快捷方式创建成功"