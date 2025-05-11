<template>
  <div :class="['math-display', { 'display-block': isDisplayMode }]" v-html="renderedContent"></div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const props = defineProps({
  content: {
    type: String,
    default: '',
  },
});

// 判断是否为行间公式
const isDisplayMode = computed(() => {
  if (!props.content) return false;
  // 只要内容包含$$...$$，就认为有行间公式
  return /\$\$([\s\S]+?)\$\$/.test(props.content);
});

const renderedContent = computed(() => {
  if (!props.content) return '';
  
  try {
    let text = props.content;
    
    // 先处理行间公式：$$...$$
    text = text.replace(/\$\$([\s\S]+?)\$\$/g, (match, formula) => {
      try {
        return katex.renderToString(formula.trim(), { 
          displayMode: true,
          throwOnError: false,
          strict: false
        });
      } catch (e) {
        console.error('KaTeX行间公式渲染错误:', e);
        return match;
      }
    });
    
    // 再处理行内公式：$...$
    text = text.replace(/\$([^\$]+)\$/g, (match, formula) => {
      try {
        return katex.renderToString(formula.trim(), { 
          displayMode: false,
          throwOnError: false,
          strict: false
        });
      } catch (e) {
        console.error('KaTeX行内公式渲染错误:', e);
        return match;
      }
    });
    
    return text;
  } catch (e) {
    console.error('渲染错误:', e);
    return props.content;
  }
});

onMounted(() => {
  // 确保样式加载
  if (!document.getElementById('katex-css')) {
    const link = document.createElement('link');
    link.id = 'katex-css';
    link.rel = 'stylesheet';
    link.href = 'https://cdn.jsdelivr.net/npm/katex@0.16.0/dist/katex.min.css';
    document.head.appendChild(link);
  }
});
</script>

<style scoped>
.math-display {
  line-height: 1.5;
  display: inline-block;
}

.display-block {
  display: block;
  text-align: center;
  margin: 1em 0;
  font-size: 1.3em;
}

:deep(.katex) {
  font-size: 1em;
  font-family: KaTeX_Main, 'Times New Roman', serif;
}

:deep(.katex-display) {
  margin: 0.5em 0;
  overflow-x: auto;
  overflow-y: hidden;
}

:deep(.katex-html) {
  white-space: normal;
}
</style> 