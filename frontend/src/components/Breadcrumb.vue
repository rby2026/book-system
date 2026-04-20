<template>
    <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
            <router-link v-if="item.path && index !== breadcrumbItems.length - 1" :to="item.path">
                {{ item.title }}
            </router-link>
            <span v-else>{{ item.title }}</span>
        </a-breadcrumb-item>
    </a-breadcrumb>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 路由映射表
const routeMap = {
    '/': { title: '首页', icon: 'dashboard' },
    '/books': { title: '图书管理', icon: 'book' },
    '/borrows': { title: '借阅管理', icon: 'swap' },
    '/users': { title: '用户管理', icon: 'team' },
    '/rules': { title: '规则管理', icon: 'setting' },
    '/profile': { title: '个人中心', icon: 'user' }
}

// 生成面包屑导航项
const breadcrumbItems = computed(() => {
    const { path, meta } = route
    const items = []

    // 首页
    items.push({
        title: '首页',
        path: '/'
    })

    if (path === '/') {
        return items
    }

    // 分割路径
    const pathArray = path.split('/').filter(Boolean)
    let currentPath = ''

    // 遍历路径生成面包屑
    for (let i = 0; i < pathArray.length; i++) {
        currentPath += `/${pathArray[i]}`

        // 如果是详情页或编辑页
        if (pathArray[i] === 'detail' || pathArray[i] === 'edit') {
            items.push({
                title: pathArray[i] === 'detail' ? '详情' : '编辑',
                path: ''
            })
            continue
        }

        // 查找路由映射
        const routeInfo = routeMap[currentPath]
        if (routeInfo) {
            items.push({
                title: routeInfo.title,
                path: currentPath
            })
        } else {
            // 使用路由元数据或路径名
            const title = meta.title || pathArray[i]
            items.push({
                title,
                path: currentPath
            })
        }
    }

    return items
})

// 监听路由变化
watch(
    () => route.path,
    () => {
        // 路由变化时，面包屑会自动更新（通过计算属性）
    }
)
</script>

<style scoped>
.breadcrumb {
    margin-left: 16px;
}
</style>