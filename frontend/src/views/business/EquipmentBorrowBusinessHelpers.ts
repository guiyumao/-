export function formatDateTime(value?: string) {
    if (!value) {
        return '-'
    }
    return value.replace('T', ' ').slice(0, 16)
}

export function borrowStatusText(status: number) {
    if (status === 2) {
        return '借用中'
    }
    if (status === 3) {
        return '已归还'
    }
    if (status === 4) {
        return '已逾期'
    }
    if (status === 5) {
        return '已拒绝'
    }
    return '待处理'
}

export function borrowStatusType(status: number) {
    if (status === 2) {
        return 'warning'
    }
    if (status === 3) {
        return 'success'
    }
    if (status === 4) {
        return 'danger'
    }
    if (status === 5) {
        return 'info'
    }
    return ''
}
