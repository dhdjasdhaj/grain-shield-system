<template>
  <div class="glass-card crud-card">
    <div class="card-head">
      <div><div class="card-title">{{ title }}</div><div class="hint">共 {{ rows.length }} 条记录</div></div>
      <div class="toolbar"><el-input v-model="keyword" clearable prefix-icon="Search" placeholder="搜索当前表格" style="width:210px"/><el-button v-if="canCreate" type="primary" class="gs-gradient-button" @click="openCreate"><el-icon><Plus/></el-icon>新增</el-button><el-button @click="load"><el-icon><Refresh/></el-icon></el-button></div>
    </div>
    <el-table :data="filtered" v-loading="loading" height="calc(100vh - 286px)" stripe>
      <el-table-column type="index" label="#" width="58" fixed />
      <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" :min-width="col.width||120" show-overflow-tooltip>
        <template #default="scope">
          <StatusTag v-if="col.tag" :value="scope.row[col.prop]" :map="col.tagMap" />
          <span v-else>{{ col.formatter ? col.formatter(scope.row[col.prop], scope.row) : display(scope.row[col.prop]) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit||canDelete" label="操作" width="150" fixed="right">
        <template #default="scope"><el-button v-if="canEdit" link type="primary" @click="openEdit(scope.row)">编辑</el-button><el-button v-if="canDelete" link type="danger" @click="remove(scope.row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialog" :title="editingId?'编辑'+title:'新增'+title" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item v-for="field in fields" :key="field.prop" :label="field.label" :class="{wide:field.wide}" :required="field.required">
            <el-select v-if="field.type==='select'" v-model="form[field.prop]" :multiple="field.multiple" filterable clearable style="width:100%" :placeholder="'请选择'+field.label">
              <el-option v-for="opt in resolveOptions(field.options)" :key="String(opt.value)" :label="opt.label" :value="opt.value"/>
            </el-select>
            <el-input-number v-else-if="field.type==='number'" v-model="form[field.prop]" :min="field.min??0" :precision="field.precision??2" controls-position="right" style="width:100%"/>
            <el-switch v-else-if="field.type==='switch'" v-model="form[field.prop]"/>
            <el-date-picker v-else-if="field.type==='datetime'" v-model="form[field.prop]" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%"/>
            <el-input v-else-if="field.type==='textarea'" v-model="form[field.prop]" type="textarea" :rows="3" :placeholder="field.placeholder"/>
            <el-input v-else v-model="form[field.prop]" :type="field.type==='password'?'password':'text'" show-password :placeholder="field.placeholder"/>
          </el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/client'
import StatusTag from './StatusTag.vue'
const props=defineProps({title:String,endpoint:String,columns:Array,fields:Array,canCreate:{type:Boolean,default:true},canEdit:{type:Boolean,default:true},canDelete:{type:Boolean,default:true},defaults:{type:Object,default:()=>({})}})
const emit=defineEmits(['changed'])
const rows=ref([]),loading=ref(false),saving=ref(false),dialog=ref(false),editingId=ref(null),keyword=ref('');const form=reactive({})
const filtered=computed(()=>{const k=keyword.value.trim().toLowerCase();if(!k)return rows.value;return rows.value.filter(r=>JSON.stringify(r).toLowerCase().includes(k))})
const display=v=>v===null||v===undefined||v===''?'-':v
const resolveOptions=o=>typeof o==='function'?o():o||[]
async function load(){loading.value=true;try{rows.value=await api.get(props.endpoint)}catch(e){ElMessage.error(e.message)}finally{loading.value=false}}
function reset(){Object.keys(form).forEach(k=>delete form[k]);Object.assign(form,JSON.parse(JSON.stringify(props.defaults||{})))}
function openCreate(){editingId.value=null;reset();dialog.value=true}
function openEdit(row){editingId.value=row.id;reset();for(const f of props.fields)form[f.prop]=row[f.prop]??(f.multiple?[]:null);dialog.value=true}
async function save(){for(const f of props.fields){if(f.required&&(form[f.prop]===null||form[f.prop]===undefined||form[f.prop]===''))return ElMessage.warning(`请填写${f.label}`)}saving.value=true;try{editingId.value?await api.put(`${props.endpoint}/${editingId.value}`,form):await api.post(props.endpoint,form);ElMessage.success('保存成功');dialog.value=false;await load();emit('changed')}catch(e){ElMessage.error(e.message)}finally{saving.value=false}}
async function remove(row){try{await ElMessageBox.confirm('删除后不可恢复，确认继续？','删除确认',{type:'warning'});await api.delete(`${props.endpoint}/${row.id}`);ElMessage.success('删除成功');await load();emit('changed')}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(e.message)}}
onMounted(load);defineExpose({load})
</script>
<style scoped>.crud-card{overflow:hidden}.hint{font-size:11px;color:#8b9b95;margin-top:4px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}.wide{grid-column:1/-1}@media(max-width:650px){.form-grid{grid-template-columns:1fr}}</style>
