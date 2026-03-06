import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 样式加载顺序很重要
import './assets/styles/variables.css'
import './assets/styles/element-override.css'
import 'element-plus/dist/index.css'
import './assets/styles/global.css'
import './assets/styles/animations.css'

import App from './App.vue'
import router from './router/index'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
