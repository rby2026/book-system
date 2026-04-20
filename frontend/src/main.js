import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'

import 'ant-design-vue/dist/reset.css'
import './assets/css/tailwind.css'
import * as process from "echarts";

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(Antd)
app.config.globalProperties.$baseUrl = process.env.VUE_APP_BASEURL

app.mount('#app')
