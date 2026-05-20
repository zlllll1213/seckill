const { contextBridge } = require('electron')

// 当前不需要暴露 Node API 给渲染层，保持最小权限原则
// 如有需要（如文件系统），在此处通过 contextBridge.exposeInMainWorld 添加
contextBridge.exposeInMainWorld('electronAPI', {
  platform: process.platform
})
