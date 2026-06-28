# 批量替换后端英文消息为中文的 PowerShell 脚本

$projectRoot = "c:\Users\wdx\Desktop\实验室设备与耗材管理系统"
$backendSrc = "$projectRoot\backend\src\main\java"

# 定义替换规则（英文 -> 中文常量）
$replacements = @(
    @{Pattern = '"created"'; Replacement = 'Messages.CREATED'}
    @{Pattern = '"updated"'; Replacement = 'Messages.UPDATED'}
    @{Pattern = '"deleted"'; Replacement = 'Messages.DELETED'}
    @{Pattern = '"returned"'; Replacement = 'Messages.BORROW_RETURNED'}
    @{Pattern = '"sent"'; Replacement = 'Messages.BORROW_REMINDER_SENT'}
    @{Pattern = '"password reset"'; Replacement = 'Messages.PASSWORD_RESET_SUCCESS'}
)

# 遍历所有 Controller 文件
Get-ChildItem -Path $backendSrc -Filter "*Controller.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $content = Get-Content $file -Raw -Encoding UTF8
    $modified = $false

    # 执行替换
    foreach ($rule in $replacements) {
        if ($content -match $rule.Pattern) {
            $content = $content -replace $rule.Pattern, $rule.Replacement
            $modified = $true
        }
    }

    # 如果修改了，检查是否需要添加 import
    if ($modified) {
        if ($content -notmatch 'import edu\.university\.lab\.common\.constant\.Messages;') {
            # 找到最后一个 import 语句的位置
            if ($content -match '(?s)(import .+?;)\s*\n\s*@') {
                $lastImport = $matches[1]
                $content = $content -replace [regex]::Escape($lastImport), "$lastImport`nimport edu.university.lab.common.constant.Messages;"
            }
        }

        # 写回文件
        Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
        Write-Host "已更新: $file"
    }
}

Write-Host "批量替换完成！"
