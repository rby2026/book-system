<template>
    <div class="statistic-card">
        <div class="icon-wrapper" :style="{ backgroundColor: `${color}15` }">
            <component :is="iconComponent" :style="{ color }" />
        </div>
        <div class="content">
            <div class="title">{{ title }}</div>
            <div class="value" :style="{ color }">{{ value }}</div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import {
    BookOutlined,
    SwapOutlined,
    TeamOutlined,
    WarningOutlined,
    DollarOutlined,
    RiseOutlined,
    FallOutlined,
    UserOutlined,
    ClockCircleOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
    title: {
        type: String,
        required: true
    },
    value: {
        type: [Number, String],
        required: true
    },
    icon: {
        type: String,
        required: true
    },
    color: {
        type: String,
        default: '#1890ff'
    }
})

// 根据icon属性动态选择图标组件
const iconComponent = computed(() => {
    const iconMap = {
        'book': BookOutlined,
        'swap': SwapOutlined,
        'team': TeamOutlined,
        'warning': WarningOutlined,
        'dollar': DollarOutlined,
        'rise': RiseOutlined,
        'fall': FallOutlined,
        'user': UserOutlined,
        'clock-circle': ClockCircleOutlined
    }

    return iconMap[props.icon] || BookOutlined
})
</script>

<style scoped>
.statistic-card {
    display: flex;
    align-items: center;
    padding: 8px;
}

.icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    margin-right: 16px;
    font-size: 24px;
}

.content {
    flex: 1;
}

.title {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    margin-bottom: 4px;
}

.value {
    font-size: 16px;
    font-weight: 600;
    word-break: break-all;
}
</style>