import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import router from './router'
import MathJaxVue from 'mathjax-vue3'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.use(MathJaxVue)

app.mount('#app')
