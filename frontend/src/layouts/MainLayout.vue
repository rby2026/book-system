<template>
    <a-layout class="main-layout">
        <!-- 侧边栏 -->
        <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible class="sidebar">
            <div class="logo">
                <img src="@/assets/logo.png" alt="Logo" />
                <h1 v-if="!collapsed">图书管理系统</h1>
            </div>

            <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
                <a-menu-item key="dashboard">
                    <router-link to="/">
                        <dashboard-outlined />
                        <span>首页</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item key="books">
                    <router-link to="/books">
                        <book-outlined />
                        <span>图书管理</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item key="borrows">
                    <router-link to="/borrows">
                        <swap-outlined />
                        <span>借阅管理</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item v-if="userStore.isAdmin" key="users">
                    <router-link to="/users">
                        <team-outlined />
                        <span>用户管理</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item v-if="userStore.isAdmin" key="rules">
                    <router-link to="/rules">
                        <setting-outlined />
                        <span>规则管理</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item v-if="userStore.isAdmin" key="statistics">
                    <router-link to="/statistics">
                        <setting-outlined />
                        <span>统计分析</span>
                    </router-link>
                </a-menu-item>

                <a-menu-item v-if="userStore.isAdmin" key="logs">
                  <router-link to="/logs">
                    <setting-outlined />
                    <span>日志管理</span>
                  </router-link>
                </a-menu-item>
            </a-menu>
        </a-layout-sider>

        <a-layout>
            <!-- 顶部导航栏 -->
            <a-layout-header class="header">
                <div class="header-left">
                    <menu-unfold-outlined v-if="collapsed" class="trigger" @click="() => (collapsed = !collapsed)" />
                    <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />

                    <breadcrumb />
                </div>

                <div class="header-right">
                    <a-dropdown>
                        <a class="user-dropdown">
                            <a-avatar>
                                {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
                            </a-avatar>
                            <span class="username">{{ userStore.userInfo?.username }}</span>
                        </a>
                        <template #overlay>
                            <a-menu>
                                <a-menu-item key="profile">
                                    <router-link to="/profile">
                                        <user-outlined />
                                        <span>个人中心</span>
                                    </router-link>
                                </a-menu-item>
                                <a-menu-divider />
                                <a-menu-item key="logout" @click="handleLogout">
                                    <logout-outlined />
                                    <span>退出登录</span>
                                </a-menu-item>
                            </a-menu>
                        </template>
                    </a-dropdown>
                </div>
            </a-layout-header>

            <!-- 内容区域 -->
            <a-layout-content class="content">
                <router-view v-slot="{ Component }">
                    <transition name="fade" mode="out-in">
                        <component :is="Component" />
                    </transition>
                </router-view>
            </a-layout-content>

            <!-- 页脚 -->
            <a-layout-footer class="footer">
                图书管理系统 ©{{ new Date().getFullYear() }} Created by Z
            </a-layout-footer>
        </a-layout>
    </a-layout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import Breadcrumb from '@/components/Breadcrumb.vue'
import {
    MenuUnfoldOutlined,
    MenuFoldOutlined,
    DashboardOutlined,
    BookOutlined,
    SwapOutlined,
    TeamOutlined,
    SettingOutlined,
    UserOutlined,
    LogoutOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

// 根据当前路由设置选中的菜单项
const selectedKeys = computed(() => {
    const path = route.path
    if (path === '/') return ['dashboard']
    return [path.split('/')[1]]
})

// 监听路由变化，更新选中的菜单项
watch(
    () => route.path,
    (newPath) => {
        if (newPath === '/') {
            selectedKeys.value = ['dashboard']
        } else {
            selectedKeys.value = [newPath.split('/')[1]]
        }
    }
)

// 退出登录
const handleLogout = () => {
    userStore.logout()
    router.push('/login')
}
</script>

<style scoped>
.main-layout {
    min-height: 100vh;
}

.sidebar {
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 10;
    overflow-y: auto;
}

/* 主布局添加左边距避免侧边栏遮挡 */
.main-layout > .ant-layout {
  margin-left: 200px;
  transition: margin-left 0.2s;
}

/* 侧边栏折叠时的适配 */
.sidebar.ant-layout-sider-collapsed + .ant-layout {
  margin-left: 80px;
}

.logo {
    height: 64px;
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    margin-bottom: 8px;
}

.logo img {
    width: 32px;
    height: 32px;
}

.logo h1 {
    color: white;
    font-size: 18px;
    margin: 0 0 0 12px;
    font-weight: 600;
    white-space: nowrap;
}

.header {
    background: #fff;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    position: sticky;
    top: 0;
    z-index: 9;
}

.header-left {
    display: flex;
    align-items: center;
}

.header-right {
    display: flex;
    align-items: center;
}

.trigger {
    font-size: 18px;
    cursor: pointer;
    transition: color 0.3s;
    padding: 0 24px;
}

.trigger:hover {
    color: #1890ff;
}

.user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 12px;
}

.username {
    margin-left: 8px;
    color: rgba(0, 0, 0, 0.65);
}

.content {
  overflow-y: auto;
  height: calc(100vh - 64px - 70px);
  margin: 24px;
  padding: 24px;
  background: #fff;
  min-height: 280px;
}

.footer {
    text-align: center;
    color: rgba(0, 0, 0, 0.45);
    position: sticky;
    bottom: 0;
    z-index: 9;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>