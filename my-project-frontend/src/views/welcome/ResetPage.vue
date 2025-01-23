<template>
  <div>
    <!-- 步骤条组件，显示当前步骤 -->
    <div style="margin: 30px 20px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件" />
        <el-step title="重置密码" />
      </el-steps>
    </div>

    <!-- 步骤一：验证电子邮件 -->
    <transition name="el-fade-in-linear" mode="out-in">
<!--      active一开始是0,然后当验证邮箱和验证码通过后,变成1,变成1后,vue会动态刷新页面-->
      <div v-if="active === 0" style="text-align: center; margin: 0 20px; height: 100%">
        <!-- 标题 -->
        <div style="margin-top: 80px">
          <div style="font-size: 25px; font-weight: bold">重置密码</div>
          <div style="font-size: 14px; color: grey">请输入电子邮件地址</div>
        </div>
        <!-- 表单区域 -->
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <!-- 输入电子邮件 -->
            <el-form-item prop="email">
              <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <!-- 输入验证码 -->
            <el-form-item prop="code">
              <el-row :gutter="10" style="width: 100%">
                <el-col :span="17">
                  <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                    <template #prefix>
                      <el-icon><EditPen /></el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="5">
                  <!-- 获取验证码按钮 -->
                  <el-button
                      type="success"
                      @click="validateEmail"
                      :disabled="!isEmailValid || coldTime > 0">
                    {{ coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码' }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </div>
        <!-- 提交按钮 -->
        <div style="margin-top: 70px">
          <el-button @click="confirmReset()" style="width: 270px;" type="danger" plain>确定</el-button>
        </div>
      </div>
    </transition>

    <!-- 步骤二：重置密码 -->
    <transition name="el-fade-in-linear" mode="out-in">
      <!--      active一开始是0,然后当验证邮箱和验证码通过后,变成1,变成1后,vue会动态刷新页面-->
      <div v-if="active === 1" style="text-align: center; margin: 0 20px; height: 100%">
        <div style="margin-top: 80px">
          <div style="font-size: 25px; font-weight: bold">重置密码</div>
          <div style="font-size: 14px; color: grey">新密码</div>
        </div>
        <!-- 表单区域 -->
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <!-- 输入新密码 -->
            <el-form-item prop="password">
              <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <!-- 重复新密码 -->
            <el-form-item prop="password_repeat">
              <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <!-- 提交按钮 -->
        <div style="margin-top: 70px">
          <el-button @click="doReset()" style="width: 270px;" type="danger" plain>重置密码</el-button>
        </div>
      </div>
    </transition>
  </div>

</template>

<script setup>
import {reactive, ref} from "vue"; // Vue 的响应式 API
import {EditPen, Lock, Message} from "@element-plus/icons-vue"; // 引入图标组件
import {get, post} from "@/net"; // 自定义的网络请求封装方法
import {ElMessage} from "element-plus"; // 消息提示组件
import router from "@/router"; // Vue 路由

// 当前步骤索引，默认从第 0 步开始
const active = ref(0);

// 表单数据
const form = reactive({
  email: '', // 邮箱地址
  code: '', // 验证码
  password: '', // 新密码
  password_repeat: '' // 重复输入的新密码
});

// 自定义校验规则：确保两次密码一致
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

// 表单校验规则
const rules = {
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    { type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] }
  ]
};

const formRef = ref(); // 引用表单组件实例
const isEmailValid = ref(false); // 邮箱地址是否有效
const coldTime = ref(0); // 倒计时，用于发送验证码按钮

// 监听表单校验事件
const onValidate = (prop, isValid) => {
  if (prop === 'email') isEmailValid.value = isValid;
};

// 发送验证码
const validateEmail = () => {
  coldTime.value = 60; // 设置倒计时初始值
  get(`/api/auth/ask-code?email=${form.email}&type=reset`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`);
    const handle = setInterval(() => {
      coldTime.value--;
      if (coldTime.value === 0) {
        clearInterval(handle); // 清除定时器
      }
    }, 1000);
  }, (message) => {
    ElMessage.warning(message); // 显示警告消息
    coldTime.value = 0;
  });
};

// 验证验证码并进入下一步
const confirmReset = () => {
  formRef.value.validate((isValid) => {
    if (isValid) {
      post('/api/auth/reset-confirm', {
        email: form.email,
        code: form.code
      }, () => active.value++); //一开始是0,然后变成1,变成1后,vue会动态刷新页面
    }
  });
};

// 重置密码
const doReset = () => {
  formRef.value.validate((isValid) => {
    if (isValid) {
      post('/api/auth/reset-password', {
        // 当我们在第一个页面输入完email和验证码后,会动态渲染到第二个页面,在第二个页面的时候,第一个页面的上存储的表单内容依然是存在的
        email: form.email,
        code: form.code,
        password: form.password
      }, () => {
        ElMessage.success('密码重置成功，请重新登录');
        router.push('/'); // 重置成功后跳转到登录页
      });
    }
  });
};

</script>

<style scoped>

</style>