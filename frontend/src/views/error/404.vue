<template>
    <div class="not-found-container">
        <div class="not-found-content">
            <h1 class="error-code">404</h1>
            <h2 class="error-title">页面不存在</h2>
            <p class="error-description">抱歉，您访问的页面不存在或已被删除</p>
            <div class="actions">
                <a-button type="primary" @click="goHome">
                    返回首页
                </a-button>
                <a-button @click="goBack" class="ml-4">
                    返回上一页
                </a-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
    router.push('/')
}

const goBack = () => {
    router.go(-1)
}
</script>

<style scoped>
.not-found-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f0f2f5;
}

.not-found-content {
    text-align: center;
    padding: 48px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    max-width: 500px;
}

.error-code {
    font-size: 72px;
    font-weight: bold;
    color: #1890ff;
    margin: 0 0 16px;
}

.error-title {
    font-size: 24px;
    color: rgba(0, 0, 0, 0.85);
    margin: 0 0 16px;
}

.error-description {
    font-size: 16px;
    color: rgba(0, 0, 0, 0.45);
    margin: 0 0 24px;
}

.actions {
    display: flex;
    justify-content: center;
}

.ml-4 {
    margin-left: 16px;
}
</style>