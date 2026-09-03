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
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body {
  height: 100%;
}
</style>