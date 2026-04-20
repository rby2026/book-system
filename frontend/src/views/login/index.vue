<template>
    <div class="login-container">
        <div class="bg-gradient"></div>
        <div class="bg-blur"></div>
        <canvas ref="canvasRef" class="animated-bg"></canvas>
        <div class="login-card">
            <div class="login-header">
                <div class="logo-container">
                    <div class="logo-icon">
                        <svg viewBox="0 0 24 24" fill="currentColor">
                            <path d="M4 19.5C4 18.837 4.26339 18.2011 4.73223 17.7322C5.20107 17.2634 5.83696 17 6.5 17H20">
                            </path>
                            <path d="M6.5 2H20V22H6.5C5.83696 22 5.20107 21.7366 4.73223 21.2678C4.26339 20.7989 4 20.163 4 19.5V4.5C4 3.83696 4.26339 3.20107 4.73223 2.73223C5.20107 2.26339 5.83696 2 6.5 2Z">
                            </path>
                        </svg>
                    </div>
                </div>
                <h1 class="title">图书管理系统</h1>
                <p class="subtitle">欢迎回来，请登录您的账号</p>
            </div>

            <a-tabs v-model:activeKey="activeKey" centered class="login-tabs">
                <a-tab-pane key="login" tab="登录" class="tab-pane">
                    <a-form :model="loginForm" :rules="loginRules" ref="loginFormRef" layout="vertical" @submit.prevent>
                        <a-form-item name="username" label="用户名">
                            <a-input v-model:value="loginForm.username" placeholder="请输入用户名" size="large"
                                :prefix="h(UserOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item name="password" label="密码">
                            <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" size="large"
                                :prefix="h(LockOutlined)" class="input-field" />
                        </a-form-item>

                        <div class="form-actions">
                            <a-checkbox v-model:checked="rememberMe" class="remember-checkbox">
                                记住我
                            </a-checkbox>
                            <a href="#" class="forgot-password">忘记密码？</a>
                        </div>

                        <a-form-item>
                            <a-button type="primary" @click="handleLogin" :loading="loginLoading" block size="large" class="login-button">
                                登录
                            </a-button>
                        </a-form-item>
                    </a-form>
                </a-tab-pane>

                <a-tab-pane key="register" tab="注册" class="tab-pane">
                    <a-form :model="registerForm" :rules="registerRules" ref="registerFormRef" layout="vertical"
                        @finish="handleRegister">
                        <a-form-item name="username" label="用户名">
                            <a-input v-model:value="registerForm.username" placeholder="请输入用户名" size="large"
                                :prefix="h(UserOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item name="password" label="密码">
                            <a-input-password v-model:value="registerForm.password" placeholder="请输入密码" size="large"
                                :prefix="h(LockOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item name="confirmPassword" label="确认密码">
                            <a-input-password v-model:value="registerForm.confirmPassword" placeholder="请确认密码"
                                size="large" :prefix="h(LockOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item name="email" label="邮箱">
                            <a-input v-model:value="registerForm.email" placeholder="请输入邮箱" size="large"
                                :prefix="h(MailOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item name="phone" label="手机号">
                            <a-input v-model:value="registerForm.phone" placeholder="请输入手机号" size="large"
                                :prefix="h(PhoneOutlined)" class="input-field" />
                        </a-form-item>

                        <a-form-item>
                            <a-button type="primary" html-type="submit" :loading="registerLoading" block size="large" class="register-button">
                                注册
                            </a-button>
                        </a-form-item>
                    </a-form>
                </a-tab-pane>
            </a-tabs>

            <div class="login-footer">
                <p class="footer-text">© 2026 图书管理系统. 保留所有权利.</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
    UserOutlined,
    LockOutlined,
    MailOutlined,
    PhoneOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeKey = ref('login')
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loginLoading = ref(false)
const registerLoading = ref(false)
const canvasRef = ref(null)
const rememberMe = ref(false)
let animationFrameId = null

// 登录表单
const loginForm = reactive({
    username: '',
    password: ''
})

// 注册表单
const registerForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: ''
})

// 登录表单验证规则
const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
}

// 注册表单验证规则
const registerRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
    ],
    phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' }
    ]
}

// 验证确认密码
async function validateConfirmPassword(rule, value) {
    if (value !== registerForm.password) {
        return Promise.reject('两次输入的密码不一致')
    }
    return Promise.resolve()
}

// 登录
const handleLogin = async () => {
    try {
        // 先验证表单
        await loginFormRef.value.validate();

        loginLoading.value = true;
        const success = await userStore.login(loginForm);
        if (success) {
            // 获取重定向地址或默认跳转到首页
            const redirect = route.query.redirect || '/';
            await router.push(redirect);
        }
    } catch (error) {
        if (error.errorFields) {
            // 表单验证错误，不做处理（ant-design-vue会自动显示错误信息）
            return;
        }
        // 登录错误已经在store中处理了错误消息的显示
        console.error('登录失败:', error);
    } finally {
        loginLoading.value = false;
    }
}

// 注册
const handleRegister = async () => {
    registerLoading.value = true
    try {
        // 提取注册所需数据
        const { confirmPassword, ...registerData } = registerForm

        await userStore.register(registerData)

        // 注册成功后切换到登录页
        activeKey.value = 'login'

        // 填充登录表单
        loginForm.username = registerForm.username
        loginForm.password = registerForm.password
    } catch (error) {
        console.error('注册失败:', error)
    } finally {
        registerLoading.value = false
    }
}

// 流体动画参数
const config = {
  numPoints: 60,        // 粒子数量
  maxDistance: 120,     // 粒子连线最大距离
  particleSpeed: 0.3,   // 粒子运动速度
  lineColor: 'rgba(255, 255, 255, 0.15)',  // 连线颜色
  particleColor: 'rgba(255, 255, 255, 0.4)', // 粒子颜色
  particleSize: 2       // 粒子大小
}

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.vx = (Math.random() - 0.5) * config.particleSpeed
    this.vy = (Math.random() - 0.5) * config.particleSpeed
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    if (this.x < 0 || this.x > this.canvas.width) this.vx *= -1
    if (this.y < 0 || this.y > this.canvas.height) this.vy *= -1
  }

  draw() {
    this.ctx.fillStyle = config.particleColor
    this.ctx.beginPath()
    this.ctx.arc(this.x, this.y, config.particleSize, 0, Math.PI * 2)
    this.ctx.fill()
  }
}

// 初始化画布
const initCanvas = () => {
  const canvas = canvasRef.value
  const dpr = window.devicePixelRatio || 1
  const rect = canvas.getBoundingClientRect()

  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  canvas.style.width = `${rect.width}px`
  canvas.style.height = `${rect.height}px`

  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)
}

// 创建粒子系统
const createParticles = () => {
  const particles = []
  for(let i = 0; i < config.numPoints; i++) {
    particles.push(new Particle(canvasRef.value))
  }
  return particles
}

// 绘制动画
const animate = (particles) => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 绘制连线
  particles.forEach(p1 => {
    particles.forEach(p2 => {
      const distance = Math.sqrt(
          Math.pow(p1.x - p2.x, 2) +
          Math.pow(p1.y - p2.y, 2)
      )

      if(distance < config.maxDistance) {
        ctx.beginPath()
        ctx.strokeStyle = config.lineColor
        ctx.lineWidth = 0.5
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.stroke()
      }
    })
  })

  // 更新并绘制粒子
  particles.forEach(particle => {
    particle.update()
    particle.draw()
  })

  animationFrameId = requestAnimationFrame(() => animate(particles))
}

onMounted(() => {
  initCanvas()
  const particles = createParticles()
  animate(particles)

  // 窗口resize处理
  window.addEventListener('resize', initCanvas)
})

onUnmounted(() => {
  window.removeEventListener('resize', initCanvas)
  cancelAnimationFrame(animationFrameId)
})
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    min-height: 100vh;
    overflow: hidden;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    z-index: 0;
}

.bg-blur {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
    z-index: 0;
}

.animated-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
}

.login-card {
    position: relative;
    z-index: 1;
    width: 420px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(15px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.login-card:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.login-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 32px;
}

.logo-container {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.logo-icon {
    width: 50px;
    height: 50px;
    color: white;
}

.logo-icon svg {
    width: 100%;
    height: 100%;
}

.title {
    font-size: 28px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 8px 0;
}

.subtitle {
    font-size: 16px;
    color: #666;
    margin: 0;
}

.login-tabs {
    margin-bottom: 24px;
}

.tab-pane {
    padding-top: 20px;
}

.input-field {
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    transition: all 0.3s ease;
}

.input-field:hover {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.input-field:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.form-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.remember-checkbox {
    font-size: 14px;
    color: #666;
}

.forgot-password {
    font-size: 14px;
    color: #667eea;
    text-decoration: none;
    transition: color 0.3s ease;
}

.forgot-password:hover {
    color: #764ba2;
    text-decoration: underline;
}

.login-button,
.register-button {
    border-radius: 8px;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    transition: all 0.3s ease;
}

.login-button:hover,
.register-button:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-button:active,
.register-button:active {
    transform: translateY(0);
}

.login-footer {
    margin-top: 32px;
    text-align: center;
}

.footer-text {
    font-size: 14px;
    color: #999;
    margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .login-card {
        width: 90%;
        padding: 30px;
    }

    .title {
        font-size: 24px;
    }

    .logo-container {
        width: 80px;
        height: 80px;
    }

    .logo-icon {
        width: 40px;
        height: 40px;
    }
}
</style>