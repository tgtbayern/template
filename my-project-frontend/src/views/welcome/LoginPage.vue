<script setup>
// 引入 Element Plus 图标组件 User 和 Lock，用于表单输入框的前缀图标
import { User, Lock } from '@element-plus/icons-vue'
// 引入 Vue 的 reactive 函数，创建响应式对象
import { reactive } from "vue";
import { ref } from 'vue' // 引入 ref
import {login} from "@/net/index.js";

// 创建一个响应式表单对象 form，包含用户名、密码和记住我三个字段
const form = reactive({
  username: '',  // 用户名或邮箱字段，初始值为空
  password: '',  // 密码字段，初始值为空
  remember: false  // 记住我字段，初始值为 false
})
const formRef = ref()

// 定义表单验证规则
const rule = {
  username: [
    // 验证用户名为必填项，如果为空则提示“请输入用户名”
    { required: true, message: '请输入用户名' }
  ],
  password: [
    // 验证密码为必填项，如果为空则提示“请输入密码”
    { required: true, message: '请输入密码' }
  ]
};

// 用户登录方法
function userLogin() {
  // 调用 formRef 的 validate 方法进行表单校验
  formRef.value.validate((valid) => {
    // 如果表单验证通过 (valid 为 true)
    if (valid) {
      // 调用 login 方法(这个方法位于net/index.js里，被import导入)，传入表单的用户名、密码和记住登录的状态
      // success 是一个回调函数，表示登录成功后的逻辑
      login(form.username, form.password, form.remember,  () => {
        // 此处可以添加登录成功后的操作，比如跳转页面或弹出提示
      });
    }
  });
}


</script>

<template>
  <div style="text-align: center;margin: 0 20px"> <!-- 外层容器，水平居中，左右边距 20px -->
    <div style="margin-top: 150px"> <!-- 距离页面顶部 150px 的标题部分 -->
      <div style="font-size: 25px;font-weight: bold">登录</div> <!-- 标题文本“登录”，加粗字体，25px 大小 -->
      <div style="font-size: 14px;color: grey">在进入系统之前，请先输入用户名和密码进行登录</div> <!-- 提示文字，灰色，字体大小 14px -->
    </div>

    <div style="margin-top: 50px"> <!-- 表单距离标题 50px -->
      <!-- 使用 Element Plus 表单组件，绑定到响应式表单对象 form -->
<!--      绑定 form 数据对象，表单中的输入内容会同步更新。-->
<!--      通过 rules 定义验证规则，提供表单验证功能。-->
<!--      使用 ref 引用表单实例，方便在代码中操作表单（如提交、验证、重置）。-->
      <el-form :model="form" :rules="rule" ref="formRef">

        <!-- 用户名/邮箱输入框 -->
        <el-form-item prop="username"> <!-- 表单项容器 -->
          <el-input v-model="form.username" maxlength="30" type="text" placeholder="用户名/邮箱">
            <!-- 输入框绑定到 form.username，最大长度 30 字符，类型为文本框，显示占位符 -->
            <template #prefix> <!-- 自定义前缀插槽，添加用户图标 -->
              <el-icon><User/></el-icon> <!-- 用户名输入框前的用户图标 -->
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password"> <!-- 表单项容器 -->
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
            <!-- 输入框绑定到 form.password，最大长度 20 字符，显示“密码”占位符 -->
            <template #prefix> <!-- 自定义前缀插槽，添加锁头图标 -->
              <el-icon><Lock /></el-icon> <!-- 密码输入框前的锁图标 -->
            </template>
          </el-input>
        </el-form-item><el-row>
        <!-- 第一列：左对齐，包含一个“记住我”复选框 -->
        <el-col :span="12" style="text-align: left">
          <el-form-item>
            <!-- el-checkbox是Element UI的复选框组件
                 v-model="form.remember" 表示双向绑定form对象的remember字段
                 label="记住我" 是复选框右侧的文字 -->
            <el-checkbox v-model="form.remember" label="记住我"/>
          </el-form-item>
        </el-col>

        <!-- 第二列：右对齐，包含一个“忘记密码”链接 -->
        <el-col :span="12" style="text-align: right">
          <!-- el-link是Element UI的超链接组件，点击可触发跳转或其他操作 -->
          <el-link>忘记密码？</el-link>
        </el-col>
      </el-row>
      </el-form>
    </div>
    <div style="margin-top: 40px">
      <!-- 登录按钮，宽度设置为270px，按钮类型为主要(primary)，表示蓝色按钮 -->
      <el-button @click="userLogin" style="width: 270px" type="primary" >立即登录</el-button>
    </div>

    <!-- 分割线，Element UI的分割线组件，用于分隔内容 -->
    <el-divider>
      <!-- 分割线中嵌入文字，字体大小13px，颜色为灰色，提示用户没有账号 -->
      <span style="font-size: 13px; color: grey">没有账号?</span>
    </el-divider>

    <div>
      <!-- 注册按钮，宽度设置为270px -->
      <el-button style="width: 270px;background-color: #c493fa; border-color:#c493fa; color: #ffffff;" type="warning">
        立即注册
      </el-button>
    </div>
  </div>
</template>

<style scoped>
/* 作用域样式，仅在当前组件内生效，避免污染全局样式 */
</style>
