<template>
  <div class="policy-wrap">
    <el-page-header @back="$router.back()" content="用户协议与隐私政策" />
    <el-tabs v-model="active" style="margin-top:12px">
      <el-tab-pane label="用户协议" name="user">
        <el-card>
          <h3>匠脉锡雕平台用户协议（摘要）</h3>
          <p>1. 用户须提供真实注册信息并妥善保管账号。</p>
          <p>2. 平台提供课程学习、作品展示、文创交易、活动预约服务。</p>
          <p>3. 用户上传内容不得侵权、违法、违规；平台保留审核与下架权。</p>
          <p>4. 交易订单与售后按平台规则处理，恶意退款将限制账号权限。</p>
          <p>5. 平台可基于运营需要更新协议，重大变更将通过公告告知。</p>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="隐私政策" name="privacy">
        <el-card>
          <h3>隐私政策（摘要）</h3>
          <p>1. 收集信息：注册信息、学习记录、订单与活动报名信息。</p>
          <p>2. 使用目的：身份识别、课程服务、交易履约、运营分析与风控。</p>
          <p>3. 信息保护：采用访问控制、日志审计与备份机制。</p>
          <p>4. 用户权利：可在个人中心申请更正、导出或注销账号。</p>
          <p>5. 未成年人保护：建议监护人指导下使用平台并限制交易功能。</p>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const active = ref('user')
</script>

<style scoped>
.policy-wrap { padding: 20px; background: #fff; border-radius: 8px; }
</style>
