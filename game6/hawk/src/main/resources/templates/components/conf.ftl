Vue.component('comp_conf', {
    props: ['logged'],
	template: '<div v-if="logged">冻结账号配置</div>'
});
