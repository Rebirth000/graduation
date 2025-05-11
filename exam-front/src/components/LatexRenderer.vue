<template>
  <span v-html="renderedContent"></span>
</template>

<script setup>
import { computed } from 'vue'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

const renderedContent = computed(() => {
  let text = props.content
  // 匹配所有 $...$ 和 $$...$$ 格式的LaTeX公式
  const inlineMathRegex = /\$([^\$]+)\$/g
  const displayMathRegex = /\$\$([^\$]+)\$\$/g

  // 替换行内公式
  text = text.replace(inlineMathRegex, (match, formula) => {
    try {
      return katex.renderToString(formula, {
        throwOnError: false,
        displayMode: false
      })
    } catch (e) {
      console.error('LaTeX渲染错误:', e)
      return match
    }
  })

  // 替换显示公式
  text = text.replace(displayMathRegex, (match, formula) => {
    try {
      return katex.renderToString(formula, {
        throwOnError: false,
        displayMode: true
      })
    } catch (e) {
      console.error('LaTeX渲染错误:', e)
      return match
    }
  })

  return text
})
</script>

<style>
/* 可以添加自定义样式 */
.katex-display {
  margin: 1em 0;
}
</style> 