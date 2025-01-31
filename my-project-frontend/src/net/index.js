import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";

// 存储认证信息的键名
const authItemName = "authorize"

// 获取请求头中Authorization字段的方法，携带访问令牌
// 比如如果我需要退出登录，我需要获得请求头中的jwt令牌，不然我无法知道应该使得哪一个用户失效
const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

// 默认的错误处理函数
const defaultError = (error) => {
    console.error(error)
    const status = error.response.status
    // 针对请求过多(429)的特殊处理
    if (status === 429) {
        ElMessage.error(error.response.data.message)
    } else {
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

// 默认的请求失败处理函数，记录警告信息
const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

// 获取存储在localStorage或sessionStorage中的访问令牌
// 如果localStorage或sessionStorage中没有authItemName对象，就说明没有登录，直接返回null
// 如果jwt过期了也返回null
// 如果都没问题，就返回token
function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str)
    // 检查令牌是否过期
    if(new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj.token
}

// 存储访问令牌到localStorage或sessionStorage
function storeAccessToken(remember, token, expire){
    const authObj = {
        token: token,
        expire: expire
    }
    const str = JSON.stringify(authObj)
    // 选择持久化方式，如果用户选择了remember me，那就存session，反之local
    if(remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

// 删除访问令牌，并可选择重定向至登录页面
// redirect = false 是函数参数的默认值，表示调用这个函数时，如果不传redirect，它默认为false。
function deleteAccessToken(redirect = false) {
    //localStorage：数据永久存储，即使页面关闭或浏览器重启，数据仍然存在。
    // sessionStorage：数据临时存储，只在当前标签页有效，关闭标签页后数据会被清除。
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    if(redirect) {
        router.push({ name: 'welcome-login' })
    }
}

// axios.post(url, data, config)
//url: 请求地址，指向要发送 POST 请求的接口路径。
// 例如：'/api/user/login' 或完整路径 https://example.com/api/login。

// data (object | string | FormData)
// 发送到服务器的数据，通常是一个对象或表单数据，作为请求体的内容。
// 例如：
// { username: 'admin', password: '123456' }

// config (object，可选)
// 配置对象，包含请求头、参数等额外信息。
// 常用配置项包括：
// {
//   headers: {
//     'Content-Type': 'application/json',
//     'Authorization': 'Bearer token'
//   },
//   timeout: 5000  // 请求超时时间（毫秒）
// }

// axios.post 返回一个 Promise，可以通过 .then 处理成功响应，通过 .catch 处理失败情况。
// response 包含服务器返回的数据，如 response.data。
// error.response 包含错误信息和状态码。
function internalPost(url, data, headers, success, failure, error = defaultError){
    axios.post(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            // keypoint 改动
            failure(data.message)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

// 封装GET请求，自动处理认证和错误

function internalGet(url, headers, success, failure, error = defaultError){
    //当axios执行成功后，执行then部分，data会接受所有返回的参数，可能是这样的：
    // {
    //     "code": 200,
    //     "message": "success",
    //     "data": {
    //     "token": "xyz789",
    //         "expire": "2025-06-01T12:00:00",
    //         "username": "张三"
    //      }
    // }
    axios.get(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            //data.data就是上面那个例子里的data属性部分，而这里的success实际上是传入的“成功函数”
            // 也就是说，如果code==200，那么我们就会把data.data作为参数，传入这个“成功函数”中
            success(data.data)
        } else if(data.code === 401) {
            // keypoint 有改动
            failure(data.message)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

// 登录函数，调用POST请求并保存访问令牌
function login(username, password, remember, success, failure = defaultFailure){
    // 这个接口定义咋spring security中：loginProcessingUrl("/api/auth/login")。
    // 这个方法执行的时候会自动调用loadUserByUsername方法进行登录验证，因为我们对这个方法进行了重写，所以会直接调用AccountServiceImpl类的loadUserByUsername
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {//这里整个函数都是一个参数，当internalPost方法的axios成功执行后，就会把axios返回的参数传入“成功函数”，
        // 而成功函数就是由这个参数传入的，所以实际上会把axios返回的参数传入这个函数
        storeAccessToken(remember,data.token,   data.expire)
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        //这个success是登录页面传进来的方法
        // @see loginPage.vue # userLogin
        success(data)
    }, failure)
}

// 封装POST请求对外暴露的方法
function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader() , success, failure)
}

// 登出函数，清除令牌并重定向
function logout(success, failure = defaultFailure){
    get('/api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功`)
        success()
    }, failure)
}

// 封装GET请求对外暴露的方法
function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

// 判断当前是否处于未授权状态
function unauthorized() {
    return !takeAccessToken()
}

// 导出所有API函数，供组件调用
export { post, get, login, logout, unauthorized }
