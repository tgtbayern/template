<template>
  <!-- 主体容器，居中对齐，左右添加边距 -->
  <div style="text-align: center;margin: 0 20px">
    <!-- 标题部分 -->
    <div style="margin-top: 100px">
      <!-- 注册新用户标题 -->
      <div style="font-size: 25px;font-weight: bold">注册新用户</div>
      <!-- 说明文字，提供注册表单的提示信息 -->
      <div style="font-size: 14px;color: grey">欢迎注册猫猫监控平台，请在下方填写相关信息</div>
    </div>
    <!-- 表单区域 -->
    <div style="margin-top: 50px">
      <!-- 表单绑定数据对象 form 和校验规则 rules，并注册校验事件
      rules 中的每个字段与 form 对象中的字段一一对应。验证规则会在指定的触发器（如 blur 或 change）执行。
      绑定校验事件 validate ，该事件会在表单中某个字段的验证规则被触发时触发。而每个字段对应的验证规则写在每个字段的rule里,比如blur,change-->
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <!-- 绑定用户名字段，限制最大长度为20，提示 "用户名" -->
          <el-input v-model="form.username" :maxlength="20" type="text" placeholder="用户名">
            <!-- 输入框前缀图标 -->
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <!-- 绑定密码字段，限制最大长度为16，提示 "密码" -->
          <el-input v-model="form.password" :maxlength="20" type="password" placeholder="密码">
            <!-- 输入框前缀图标 -->
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 重复密码输入框 -->
        <el-form-item prop="password_repeat">
          <!-- 绑定重复密码字段，限制最大长度为16，提示 "重复密码" -->
          <el-input v-model="form.password_repeat" :maxlength="20" type="password" placeholder="重复密码">
            <!-- 输入框前缀图标 -->
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 邮箱输入框 -->
        <el-form-item prop="email">
          <!-- 绑定邮箱字段，提示 "电子邮件地址" -->
          <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
            <!-- 输入框前缀图标 -->
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 验证码输入框 -->
        <el-form-item prop="code">
          <el-row :gutter="10" style="width: 100%">
            <!-- 验证码输入框部分 -->
            <el-col :span="17">
              <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-col>
            <!-- 验证码按钮部分 -->
            <el-col :span="5">
<!--              如果邮箱正确且冷却时间已经到了(没有太频繁地请求验证码)-->
              <el-button type="success" @click="validateEmail" :disabled="!isEmailValid || coldTime > 0">
                <!-- 按钮内容：冷却倒计时或 "获取验证码" -->
                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <!-- 注册按钮 -->
    <div style="margin-top: 80px">
      <el-button style="width: 270px" type="warning" @click="register" plain>立即注册</el-button>
    </div>
    <!-- 登录链接 -->
    <div style="margin-top: 20px">
      <span style="font-size: 14px;line-height: 15px;color: grey">已有账号? </span>
<!--      当用户点击绑定了 @click 的按钮或链接时，事件触发。执行 router.push('/')，Vue Router 会将当前页面导航到路径 '/'，即根路径。-->
      <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')" >立即登录</el-link>
    </div>
  </div>
</template>

<script setup>
/* 引入必要的图标组件、路由对象、响应式方法、消息弹框组件、网络请求方法 */
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import router from "@/router";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";

/* 定义表单的响应式对象，包含用户名、密码、重复密码、邮箱和验证码字段 */
// const form = reactive({...}) 是 Vue 3 中的一个写法，它使用了 Vue 3 的 reactive API 来定义一个响应式对象。
// reactive 是 Vue 3 提供的响应式核心函数之一，用于创建一个深层响应式的对象。当这个对象中的数据发生变化时，所有依赖该数据的视图会自动更新。
// 比如表单中有个密码对象,密码不能有空格,在用户输入的时候,一旦检测到空格就会直接提醒用户,而不是等到用户输入完点击提交的时候再提醒用户

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

/* 用户名校验规则，判断是否为空或包含非法字符 */
// callback是验证结束的收尾动作,如果验证成功就直接callback,如果不成功就callback(error)
const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}

/* 密码校验规则，确保两次输入一致 */
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

/* 定义表单的校验规则 */
const rules = {
  username: [
    //   validator指定一个自定义的合法规则 trigger指定验证的触发时机(何适调用validator)。['blur', 'change'] 表示当用户输入内容发生变化或失去焦点时都会触发验证。

    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 20, message: '用户名的长度必须在2-20个字符之间', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码的长度必须在6-20个字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ]
}

/* 引用表单组件实例，用于后续操作 */
const formRef = ref()
/* 是否通过邮箱校验的状态 */
const isEmailValid = ref(false)
/* 验证码冷却倒计时 */
const coldTime = ref(0)

/* 校验事件回调(当表单里的任意字段校验通过后,再调用这个方法)，用于更新邮箱验证状态 */
const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

/* 注册事件，发送表单数据到后端 */
const register = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        code: form.code
      }, () => {
        ElMessage.success('注册成功，欢迎加入我们')
        router.push("/")
      })
    } else {
      ElMessage.warning('请正确填写注册表单内容！')
    }
  })
}

const validateEmail = () => {
  // 定义一个名为 validateEmail 的函数，用于处理邮箱验证逻辑
  coldTime.value = 60;
  // 设置冷却时间为 60 秒，通常用于倒计时显示或按钮禁用控制
  get(
      `/api/auth/ask-code?email=${form.email}&type=register`,
      // 调用封装的 GET 请求方法，后端目标地址是 `/api/auth/ask-code`
      // 通过查询参数将邮箱（form.email）和操作类型（type=register）发送给后端
      () => {
        // 请求成功的回调函数
        ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`);
        // 弹出成功消息，提示用户验证码已发送到指定邮箱
        const handle = setInterval(() => {
          // 启动一个定时器，每隔 1 秒执行一次回调
          coldTime.value--;
          // 每次定时器触发时，将冷却时间减 1
          if (coldTime.value === 0) {
            // 如果冷却时间归零
            clearInterval(handle);
            // 停止定时器，防止继续执行
          }
        }, 1000); // 定时器的间隔时间为 1 秒
      },
      undefined,
      // 第三个参数为空，可能是占位符，表示没有提供额外的选项
      (message) => {
        // 请求失败的回调函数
        ElMessage.warning(message);
        // 弹出警告消息，提示用户请求失败的原因
        coldTime.value = 0;
        // 将冷却时间重置为 0，取消倒计时功能
      }
  );
};

</script>

<style scoped>
/* 目前无自定义样式 */
</style>
