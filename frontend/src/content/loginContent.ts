export const loginContent = {
    platformEyebrow: '实验室一体化平台',
    title: '把设备、耗材和安全管理收拢到一个清晰界面里',
    description: '登录后查看真实统计、处理待办和切换角色，不放示例数据，不写空话。',
    highlights: [
        {
            title: '统一看板',
            value: '设备 / 耗材 / 危化品',
            description: '把分散业务放在同一套导航和统计里，减少来回切换。',
        },
        {
            title: '真实权限',
            value: '按角色切换',
            description: '只显示当前账号可见的菜单与数据，避免无关内容干扰。',
        },
    ],
    valuePoints: [
        '只读取后端数据，不在前端硬塞演示用例。',
        '菜单标题异常时自动回退成中文。',
        '仪表盘、提醒和业务单据保持同一套主题。',
    ],
    roleGuideTitle: '权限说明',
    roleGuideDescription: '登录时选择对应角色，系统会按当前权限加载菜单。',
    formBadge: '安全登录',
    loginModeEyebrow: '账号登录',
    registerModeEyebrow: '账号注册',
    loginHeading: '进入系统',
    registerHeading: '创建账号',
    loginDescription: '使用账号登录，继续处理实验室业务。',
    registerDescription: '填写基础信息，创建教师或学生账号。',
} as const
