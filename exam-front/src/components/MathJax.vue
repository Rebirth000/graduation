<template>
  <div class="mathjax-container">
    <slot></slot>
  </div>
</template>

<script>
import { onMounted, defineComponent } from 'vue'
import { MathJax } from 'mathjax-vue3'

export default defineComponent({
  name: 'MathJaxWrapper',
  components: {
    MathJax
  },
  props: {
    config: {
      type: Object,
      default: () => ({
        tex: {
          inlineMath: [['$', '$'], ['\\(', '\\)']],
          displayMath: [['$$', '$$'], ['\\[', '\\]']],
          processEscapes: true
        }
      })
    }
  },
  setup(props) {
    onMounted(() => {
      // 触发MathJax重新渲染
      if (window.MathJax) {
        window.MathJax.typesetPromise && window.MathJax.typesetPromise();
      }
    })
    
    return {}
  }
})
</script>

<style scoped>
.mathjax-container {
  width: 100%;
}
</style> 