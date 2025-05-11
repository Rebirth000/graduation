<template>
  <div class="math-editor">
    <div class="editor-toolbar">
      <el-tooltip content="行内公式: $x^2$">
        <el-button size="small" @click="insertInlineMath" round type="info">插入行内公式</el-button>
      </el-tooltip>
      <el-tooltip content="行间公式: $$\\frac{x}{y}$$">
        <el-button size="small" @click="insertDisplayMath" round type="primary">插入行间公式</el-button>
      </el-tooltip>
      <el-tooltip content="常用数学符号参考">
        <el-popover placement="bottom" trigger="click" width="400">
          <template #reference>
            <el-button size="small" round type="success">符号参考</el-button>
          </template>
          <div class="symbols-reference">
            <div>上标：x^2</div>
            <div>下标：x_n</div>
            <div>分数：\frac{x}{y}</div>
            <div>根号：\sqrt{x}</div>
            <div>求和：\sum_{i=1}^n</div>
            <div>积分：\int_{a}^b</div>
            <div>希腊字母：\alpha, \beta, \gamma, \pi</div>
          </div>
        </el-popover>
      </el-tooltip>
    </div>
    <div class="editor-container">
      <el-input
        v-model="localValue"
        type="textarea"
        :rows="rows"
        :placeholder="placeholder"
        @input="handleInput"
        class="editor-textarea"
      />
    </div>
    <div v-if="showPreview" class="preview-container">
      <div class="preview-title">
        <span class="preview-title-bar"></span>
        LaTeX预览:
      </div>
      <div class="preview-content" v-html="renderedMath"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  rows: {
    type: Number,
    default: 3,
  },
  placeholder: {
    type: String,
    default: '输入内容，支持LaTeX数学公式，例如: $x^2$ 或 $$\\frac{x}{y}$$',
  },
  showPreview: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['update:modelValue']);
const localValue = ref(props.modelValue);

watch(() => props.modelValue, (newValue) => {
  localValue.value = newValue;
});

const handleInput = () => {
  emit('update:modelValue', localValue.value);
};

const insertInlineMath = () => {
  const textarea = document.querySelector('.editor-container textarea');
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const text = localValue.value;
  const before = text.substring(0, start);
  const after = text.substring(end);
  localValue.value = before + '$ $' + after;
  emit('update:modelValue', localValue.value);
  // 设置光标位置
  setTimeout(() => {
    textarea.focus();
    textarea.setSelectionRange(start + 2, start + 2);
  }, 0);
};

const insertDisplayMath = () => {
  const textarea = document.querySelector('.editor-container textarea');
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const text = localValue.value;
  const before = text.substring(0, start);
  const after = text.substring(end);
  localValue.value = before + '$$ $$' + after;
  emit('update:modelValue', localValue.value);
  // 设置光标位置
  setTimeout(() => {
    textarea.focus();
    textarea.setSelectionRange(start + 3, start + 3);
  }, 0);
};

const renderedMath = computed(() => {
  if (!localValue.value) return '';
  
  try {
    let text = localValue.value;
    
    // 处理行内公式：$...$
    text = text.replace(/\$([^\$]+)\$/g, (match, formula) => {
      try {
        return katex.renderToString(formula.trim(), { 
          displayMode: false,
          throwOnError: false
        });
      } catch (e) {
        console.error('KaTeX行内公式渲染错误:', e);
        return match;
      }
    });
    
    // 处理行间公式：$$...$$
    text = text.replace(/\$\$([^\$]+)\$\$/g, (match, formula) => {
      try {
        return katex.renderToString(formula.trim(), { 
          displayMode: true,
          throwOnError: false
        });
      } catch (e) {
        console.error('KaTeX行间公式渲染错误:', e);
        return match;
      }
    });
    
    return text;
  } catch (e) {
    console.error('渲染错误:', e);
    return localValue.value;
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
.math-editor {
  width: 100%;
}

.editor-toolbar {
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
  background: #f6f8fa;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 1px 4px rgba(64,158,255,0.06);
}

.editor-container {
  margin-bottom: 8px;
}

.editor-textarea >>> textarea {
  border-radius: 10px !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  font-size: 1.1em;
  min-height: 80px;
}

.preview-container {
  margin-top: 14px;
  padding: 14px 16px 10px 16px;
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.06);
}

.preview-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #409EFF;
  display: flex;
  align-items: center;
  font-size: 1.08em;
}

.preview-title-bar {
  display: inline-block;
  width: 4px;
  height: 18px;
  background: #409EFF;
  border-radius: 2px;
  margin-right: 8px;
}

.preview-content {
  min-height: 40px;
  padding: 10px 8px;
  background-color: white;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
  font-size: 1.08em;
}

.preview-content :deep(.katex-display) {
  margin: 0.5em 0;
  overflow-x: auto;
}

.symbols-reference {
  font-size: 14px;
}
</style> 