<template>
  <div class="ai-container">
    <el-tabs v-model="activeTab" class="ai-tabs">
      
      <!-- AI Tutor / Chat Tab -->
      <el-tab-pane label="AI 工艺问答助手" name="chat">
        <div class="chat-wrapper">
          <div class="chat-history" ref="chatHistoryRef">
            <div 
              v-for="(msg, index) in chatList" 
              :key="index"
              :class="['chat-bubble-container', msg.role === 'user' ? 'is-user' : 'is-ai']"
            >
              <el-avatar v-if="msg.role === 'ai'" :size="40" class="ai-avatar" style="background-color: var(--el-color-primary)"><el-icon><Cpu /></el-icon></el-avatar>
              <div class="chat-bubble">
                <div v-if="msg.loading" class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <div v-else class="chat-text" v-html="msg.text"></div>
              </div>
              <el-avatar v-if="msg.role === 'user'" :size="40" class="user-avatar">{{ username.charAt(0).toUpperCase() }}</el-avatar>
            </div>
          </div>
          
          <div class="chat-input-area">
            <el-input 
              v-model="inputMsg" 
              type="textarea" 
              :rows="3" 
              placeholder="向 AI 助教提问，例如：永康锡雕的打錾技法有哪些核心要领？"
              resize="none"
              @keyup.enter.exact.prevent="sendMessage"
            />
            <button class="send-btn" @click="sendMessage" :disabled="loading || !inputMsg.trim()" title="发送指令">
              <el-icon size="22"><Position /></el-icon>
            </button>
          </div>
        </div>
      </el-tab-pane>

      <!-- AI Pattern Generation Tab -->
      <el-tab-pane label="AI 纹样灵感生成" name="generator">
        <div class="gen-wrapper">
          <div class="gen-form">
            <h3 class="gen-title">智能锡雕纹理生成</h3>
            <p class="gen-desc">输入关键字或描述，AI将为您生成独一无二的非遗锡雕底纹与器型设计图稿。</p>
            
            <el-form label-position="top">
              <el-form-item label="设计描述词 (Prompt)">
                <el-input 
                  v-model="prompt" 
                  type="textarea" 
                  :rows="4" 
                  placeholder="例如：一件传统的锡制茶壶，表面雕有精致的梅花和喜鹊图案，光泽细腻，展现永康锡雕工艺，8k分辨率..." 
                />
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="生成风格">
                    <el-select v-model="styleParam" style="width:100%">
                      <el-option label="传统写实" value="realistic" />
                      <el-option label="现代几何" value="modern" />
                      <el-option label="水墨意境" value="ink" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="图片尺寸">
                    <el-select v-model="sizeParam" style="width:100%">
                      <el-option label="1:1 方形图" value="1024x1024" />
                      <el-option label="16:9 横屏图" value="1920x1080" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item>
                <el-button type="success" size="large" class="gen-btn" @click="generateImage" :loading="genLoading" icon="MagicStick">
                  开始生成灵感
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="gen-result">
            <div class="result-placeholder" v-if="!currentImage && !genLoading">
              <el-icon class="placeholder-icon"><Picture /></el-icon>
              <p>生成的灵感图纸将展示在这里</p>
            </div>
            
            <div class="generating-box" v-if="genLoading">
              <el-progress type="dashboard" :percentage="genProgress" :color="colors" />
              <p class="anim-text">AI 正在深度演算中...</p>
            </div>
            
            <div class="result-image-box" v-if="currentImage && !genLoading">
              <el-image 
                style="width: 100%; height: 100%; border-radius: 8px;" 
                :src="currentImage" 
                :preview-src-list="[currentImage]"
                fit="contain"
              />
              <div class="result-actions">
                <el-button type="primary" icon="Download" circle title="下载原图" />
                <el-button type="warning" icon="Star" circle title="收藏灵感" />
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAiReply, generateAiImage } from '../api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const isLoggedIn = ref(!!localStorage.getItem('token'))
const username = ref(localStorage.getItem('nickname') || localStorage.getItem('username') || '游客')
const activeTab = ref('chat')

// --- Guest Guard ---
const handleGuestAction = (actionName = '使用 AI 功能') => {
  ElMessageBox.confirm(
    `您当前处于游客模式，登录后即可${actionName}。是否现在去登录？`,
    '提示',
    {
      confirmButtonText: '去登录',
      cancelButtonText: '先看看',
      type: 'info'
    }
  ).then(() => {
    router.push('/login')
  }).catch(() => {})
}

// --- Chat Logic ---
const inputMsg = ref('')
const loading = ref(false)
const chatHistoryRef = ref(null)

const chatList = ref([
  { role: 'ai', text: '你好！我是你的非遗锡雕智能助教。遇到不懂的工艺问题、历史渊源或者材料配比，都可以随时问我哦！' }
])

const scrollToBottom = async () => {
  await nextTick()
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
  }
}

const sendMessage = () => {
  if (!isLoggedIn.value) {
    handleGuestAction('提问并获得 AI 解答')
    return
  }
  if (!inputMsg.value.trim() || loading.value) return
  
  const userText = inputMsg.value
  chatList.value.push({ role: 'user', text: userText })
  inputMsg.value = ''
  loading.value = true
  
  scrollToBottom()
  
  // Add loading bubble 
  chatList.value.push({ role: 'ai', loading: true })
  scrollToBottom()
  
  const userId = Number(localStorage.getItem('userId') || 1)
  getAiReply({ message: userText, user_id: userId }).then(res => {
    chatList.value.pop() // Remove loading bubble
    // Compatible with both wrapped backend response and ai-service direct response.
    const replyText = res?.data || res?.reply || res?.message || "API 异常，返回值为空"
    const formattedReply = String(replyText).replace(/\n/g, '<br>')
    chatList.value.push({ role: 'ai', text: formattedReply })
    loading.value = false
    scrollToBottom()
  }).catch(err => {
    chatList.value.pop()
    const isTimeout =
      err?.code === 'ECONNABORTED' ||
      String(err?.message || '').toLowerCase().includes('timeout')
    const apiError =
      (isTimeout ? 'AI 响应超时，请稍后重试（已将超时阈值提高到 60 秒）。' : '') ||
      err?.response?.data?.message ||
      err?.response?.data?.reply ||
      err?.message ||
      'AI 服务调用失败，请稍后重试。'
    chatList.value.push({ role: 'ai', text: String(apiError).replace(/\n/g, '<br>') })
    loading.value = false
    scrollToBottom()
  })
}

// --- Image Generation Logic ---
const prompt = ref('')
const styleParam = ref('realistic')
const sizeParam = ref('1024x1024')
const genLoading = ref(false)
const genProgress = ref(0)
const currentImage = ref('')

const colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

const generateImage = async () => {
  if (!isLoggedIn.value) {
    handleGuestAction('生成 AI 纹样灵感图')
    return
  }
  if (!prompt.value.trim() || genLoading.value) return

  genLoading.value = true
  genProgress.value = 0
  currentImage.value = ''

  const timer = setInterval(() => {
    if (genProgress.value < 90) {
      genProgress.value += Math.floor(Math.random() * 10) + 3
    }
  }, 350)

  try {
    const res = await generateAiImage({
      prompt: prompt.value.trim(),
      style: styleParam.value,
      size: sizeParam.value
    })
    if (!res?.image_url) {
      throw new Error(res?.error || '图片生成失败')
    }
    currentImage.value = res.image_url
    genProgress.value = 100
    ElMessage.success(`图片生成完成（${res.provider || 'ai'}）`)
  } catch (err) {
    const msg =
      err?.response?.data?.error ||
      err?.response?.data?.message ||
      err?.message ||
      '图片生成失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    clearInterval(timer)
    genLoading.value = false
  }
}

onMounted(() => {
  // 锁死 body 滚动，仅在本页面生效全屏沉浸
  document.body.style.overflow = 'hidden'
  const pubMain = document.querySelector('.pub-main')
  if (pubMain) {
    // 动态调整 padding
    pubMain.style.paddingBottom = '0'
  }
})

onUnmounted(() => {
  // 离开此页时恢复滚动
  document.body.style.overflow = ''
  const pubMain = document.querySelector('.pub-main')
  if (pubMain) {
    pubMain.style.paddingBottom = ''
  }
})
</script>

<style scoped>
.ai-container {
  height: calc(100vh - 64px); /* 减去顶部导航 */
  width: 100%;
  margin: 0;
  /* 取消圆角及外边框限制，完全贴合浏览器 */
  border-radius: 0;
  border: none;
  background: rgba(11, 12, 16, 1); /* 全屏后直接用极夜纯色做底垫，避免过度透视 */
  background-image: radial-gradient(circle at 50% 0%, rgba(102, 252, 241, 0.08) 0%, transparent 60%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #c5c6c7;
}

:deep(.ai-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.ai-tabs .el-tabs__content) {
  flex: 1;
  padding: 0;
  height: 100%;
}

:deep(.ai-tabs .el-tab-pane) {
  height: 100%;
}

:deep(.el-tabs__header) {
  margin: 0;
  padding: 16px 30px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  background: transparent;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: transparent;
}

:deep(.el-tabs__item) {
  color: #8892b0;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s;
}

:deep(.el-tabs__item.is-active), :deep(.el-tabs__item:hover) {
  color: #66fcf1;
  text-shadow: 0 0 10px rgba(102, 252, 241, 0.5);
}

:deep(.el-tabs__active-bar) {
  background-color: #66fcf1;
  height: 3px;
  border-radius: 3px;
  box-shadow: 0 0 10px #66fcf1;
}

/* Chat Wrapper (超级暗黑黑客风) */
.chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: transparent;
  position: relative;
}

.chat-history {
  flex: 1;
  padding: 40px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
  scroll-behavior: smooth;
  /* 顶部和底部羽化透明遮罩，使内容有消失在玻璃内的深邃感 */
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
  mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
}

/* 隐藏历史滚动条但保留功能 */
.chat-history::-webkit-scrollbar {
  width: 4px;
}
.chat-history::-webkit-scrollbar-thumb {
  background: rgba(102, 252, 241, 0.15);
  border-radius: 2px;
}

.chat-bubble-container {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

@keyframes slideInUp {
  0% { opacity: 0; transform: translateY(20px) scale(0.98); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

.chat-bubble-container.is-ai {
  align-self: flex-start;
}

.chat-bubble-container.is-user {
  align-self: flex-end;
  justify-content: flex-end;
}

.ai-avatar {
  margin-right: 20px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #0b0c10 0%, #1f2833 100%) !important;
  color: #66fcf1;
  border: 1px solid rgba(102, 252, 241, 0.5);
  box-shadow: 0 0 20px rgba(102, 252, 241, 0.3), inset 0 0 10px rgba(102, 252, 241, 0.1);
}

.user-avatar {
  margin-left: 20px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #1f2833 0%, #0b0c10 100%);
  color: #c5c6c7;
  border: 1px solid rgba(197, 198, 199, 0.3);
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
}

.chat-bubble {
  padding: 18px 24px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.8;
  letter-spacing: 0.5px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  font-weight: 300;
  position: relative;
}

/* 机器人的神秘感 */
.is-ai .chat-bubble {
  background: linear-gradient(135deg, rgba(31, 40, 51, 0.8), rgba(11, 12, 16, 0.6));
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(102, 252, 241, 0.2);
  border-top-left-radius: 4px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.5), inset 1px 1px 0 rgba(255,255,255,0.05);
}

/* 机器人气泡专属呼吸边框底光 */
.is-ai .chat-bubble::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: inset 0 0 20px rgba(102, 252, 241, 0.05);
  pointer-events: none;
}

/* 用户青色系 */
.is-user .chat-bubble {
  background: linear-gradient(135deg, rgba(69, 162, 158, 0.25), rgba(31, 40, 51, 0.4));
  color: #66fcf1;
  border: 1px solid rgba(102, 252, 241, 0.4);
  border-top-right-radius: 4px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.4), inset 1px 1px 0 rgba(255,255,255,0.05);
}

.chat-input-area {
  padding: 20px 40px 30px;
  background: transparent;
  display: flex;
  gap: 20px;
  align-items: flex-end;
  position: relative;
}

/* 使用渐变底托代替生硬边线 */
.chat-input-area::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 252, 241, 0.2), transparent);
}

:deep(.el-textarea__inner) {
  background: rgba(11, 12, 16, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.08);
  color: #fff;
  border-radius: 20px;
  font-size: 15px;
  padding: 16px 20px;
  box-shadow: inset 0 4px 10px rgba(0,0,0,0.4);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  line-height: 1.6;
}

:deep(.el-textarea__inner:focus) {
  border-color: rgba(102, 252, 241, 0.6);
  box-shadow: 0 0 25px rgba(102, 252, 241, 0.15), inset 0 2px 5px rgba(0,0,0,0.2);
  background: rgba(31, 40, 51, 0.8);
}

:deep(.el-textarea__inner::placeholder) {
  color: rgba(136, 146, 176, 0.6);
}

.send-btn {
  height: 56px;
  width: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: linear-gradient(135deg, #45a29e, #66fcf1);
  border: none;
  color: #0b0c10;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 0 15px rgba(102, 252, 241, 0.3);
  flex-shrink: 0;
  margin-bottom: 2px;
}

.send-btn:not(:disabled):hover {
  background: #fff;
  box-shadow: 0 0 30px rgba(102, 252, 241, 0.6);
  transform: translateY(-4px) scale(1.05);
}

.send-btn:disabled {
  background: rgba(31, 40, 51, 0.8);
  color: #6b7280;
  box-shadow: none;
  border: 1px solid rgba(255,255,255,0.05);
}

/* Typing Indicator Animation */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 10px 8px;
  align-items: center;
}
.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #66fcf1;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
  box-shadow: 0 0 8px #66fcf1;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.3); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* --- Generation Wrapper (赛博实验室) --- */
.gen-wrapper {
  display: flex;
  height: 100%;
}

.gen-form {
  width: 440px;
  padding: 40px 30px;
  background: rgba(11, 12, 16, 0.6);
  border-right: 1px solid rgba(255,255,255,0.05);
  overflow-y: auto;
}

.gen-title {
  font-size: 24px;
  color: #fff;
  margin-top: 0;
  letter-spacing: 1px;
}

.gen-desc {
  font-size: 14px;
  color: #8892b0;
  margin-bottom: 40px;
  line-height: 1.6;
}

:deep(.el-form-item__label) {
  color: #c5c6c7;
  font-size: 14px;
  padding-bottom: 8px;
}

:deep(.el-input__inner), :deep(.el-select .el-input__wrapper) {
  background: rgba(31, 40, 51, 0.6);
  border: 1px solid rgba(255,255,255,0.1);
  color: #fff;
  box-shadow: none;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #66fcf1 inset !important;
}

.gen-btn {
  width: 100%;
  margin-top: 20px;
  font-size: 16px;
  letter-spacing: 2px;
  font-weight: 700;
  background: linear-gradient(90deg, #45a29e, #66fcf1);
  border: none;
  color: #0b0c10;
  height: 50px;
  border-radius: 25px;
  transition: all 0.3s;
}

.gen-btn:hover {
  box-shadow: 0 0 20px rgba(102, 252, 241, 0.4);
  transform: translateY(-2px);
}

.gen-result {
  flex: 1;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
}

.result-placeholder {
  text-align: center;
  color: #45a29e;
  opacity: 0.5;
}

.placeholder-icon {
  font-size: 100px;
  margin-bottom: 20px;
  text-shadow: 0 0 30px rgba(69, 162, 158, 0.3);
}

.generating-box {
  text-align: center;
}

:deep(.el-progress-circle__track) {
  stroke: rgba(255,255,255,0.05);
}

.anim-text {
  margin-top: 24px;
  color: #66fcf1;
  font-weight: 500;
  letter-spacing: 2px;
  animation: pulse 2s infinite;
  text-shadow: 0 0 10px rgba(102, 252, 241, 0.3);
}

@keyframes pulse {
  0% { opacity: 0.4; }
  50% { opacity: 1; }
  100% { opacity: 0.4; }
}

.result-image-box {
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255,255,255,0.05);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-actions {
  position: absolute;
  top: 40px;
  right: 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-actions .el-button {
  background: rgba(11, 12, 16, 0.8);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  transition: all 0.3s;
}

.result-actions .el-button:hover {
  background: #66fcf1;
  color: #0b0c10;
  border-color: #66fcf1;
  box-shadow: 0 0 15px rgba(102, 252, 241, 0.5);
}
</style>
