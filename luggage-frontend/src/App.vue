<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from './store/user'

// 恢复登录状态
const userStore = useUserStore()
userStore.restore()
</script>

<style>
* {
  box-sizing: border-box;
}
html, body {
  margin: 0;
  height: 100%;
}
</style>
