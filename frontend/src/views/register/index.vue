<template>
    <div class="register-container min-h-screen flex items-center justify-center bg-gray-100">
        <a-card class="register-card w-96 shadow-lg">
            <div class="text-center mb-6">
                <h2 class="text-2xl font-bold text-gray-800">图书管理系统</h2>
                <p class="text-gray-600 mt-2">用户注册</p>
            </div>

            <a-form :model="registerForm" @finish="handleRegister" layout="vertical">
                <a-form-item name="username" label="用户名" :rules="[{ required: true, message: '请输入用户名' }]">
                    <a-input v-model:value="registerForm.username" placeholder="请输入用户名" />
                </a-form-item>

                <a-form-item name="password" label="密码" :rules="[{ required: true, message: '请输入密码' }]">
                    <a-input-password v-model:value="registerForm.password" placeholder="请输入密码" />
                </a-form-item>

                <a-form-item name="confirmPassword" label="确认密码" :rules="[
                    { required: true, message: '请确认密码' },
                    { validator: validatePassword }
                ]">
                    <a-input-password v-model:value="registerForm.confirmPassword" placeholder="请确认密码" />
                </a-form-item>

                <a-form-item name="email" label="邮箱" :rules="[
                    { required: true, message: '请输入邮箱' },
                    { type: 'email', message: '请输入有效的邮箱地址' }
                ]">
                    <a-input v-model:value="registerForm.email" placeholder="请输入邮箱" />
                </a-form-item>

                <a-form-item name="phone" label="手机号" :rules="[{ required: true, message: '请输入手机号' }]">
                    <a-input v-model:value="registerForm.phone" placeholder="请输入手机号" />
                </a-form-item>

                <a-form-item name="userType" label="用户类型" :rules="[{ required: true, message: '请选择用户类型' }]">
                    <a-radio-group v-model:value="registerForm.userType">
                        <a-radio :value="0">学生</a-radio>
                        <a-radio :value="1">教师</a-radio>
                    </a-radio-group>
                </a-form-item>

                <a-form-item>
                    <a-button type="primary" html-type="submit" class="w-full" :loading="loading">
                        注册
                    </a-button>
                </a-form-item>

                <div class="text-center">
                    <router-link to="/login" class="text-blue-500 hover:text-blue-700">
                        已有账号？立即登录
                    </router-link>
                </div>
            </a-form>
        </a-card>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const registerForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    userType: 0
})

const validatePassword = async (rule, value) => {
    if (value !== registerForm.password) {
        return Promise.reject('两次输入的密码不一致')
    }
    return Promise.resolve()
}

const handleRegister = async (values) => {
    loading.value = true
    try {
        // 移除确认密码字段，后端不需要
        const { confirmPassword, ...registerData } = values
        const success = await userStore.register(registerData)
        if (success) {
            message.success('注册成功，请登录')
            router.push('/login')
        }
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.register-card {
    border-radius: 8px;
}
</style>