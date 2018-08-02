	<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">

		  <el-input placeholder="输入玩家ID,不包含zone" v-model="accid" class="input-with-select" size="small">
			<el-button slot="prepend" >玩家</el-button>
			<el-button slot="append" icon="el-icon-search" @click="findAccount">查询</el-button>
		  </el-input>
	</div>

	<div style="margin-top: 20px; margin-left: 0px;" v-if="acc">
	  <template>
		<el-tabs v-model="acctab" type="card">
		  <el-tab-pane label="基本信息" name="a1">

			<el-row>
		  		<el-input placeholder="" v-model="acc.id" class="input-with-select" size="small">
		  		<el-button slot="prepend">ID</el-button>
		  			<el-button slot="append" type="danger" plain @click="bindAccId">绑定</el-button>
		  		</el-input>
			</el-row>


			<el-row>
		  		<el-input placeholder="" v-model="acc.account" class="input-with-select" size="small">
		  			<el-button slot="prepend">帐号</el-button>
		  		</el-input>
			</el-row>

			<el-row>
		  		<el-input placeholder="" v-model="acc.passwd" class="input-with-select" size="small">
		  		<el-button slot="prepend">密码</el-button>
		  			<el-button slot="append" icon="el-icon-search" @click="modifyPassword">修改</el-button>
		  		</el-input>
			</el-row>
			<el-row>
		  		<el-input placeholder="" v-model="acc.role" class="input-with-select" size="small">
		  		<el-button slot="prepend">权限</el-button>
		  			<el-button slot="append" icon="el-icon-search" @click="modifyAdmin">修改</el-button>
		  		</el-input>
			</el-row>

			<el-row>
		  		<el-input placeholder="" v-model="acc.source" class="input-with-select" size="small">
		  		<el-button slot="prepend">来源</el-button>
		  		</el-input>
			</el-row>
			<el-row>
		  		<el-input placeholder="" v-model="acc.ip" class="input-with-select" size="small">
		  		<el-button slot="prepend">注册地址</el-button>
		  		</el-input>
			</el-row>
			<el-row>
		  		<el-input placeholder="" v-model="acc.charge" class="input-with-select" size="small">
		  		<el-button slot="prepend">充值</el-button>
		  		</el-input>
			</el-row>
			<el-row>
		  		<el-input placeholder="" v-model="acc.chargecount" class="input-with-select" size="small">
		  		<el-button slot="prepend">充值次数</el-button>
		  		</el-input>
			</el-row>
		  </el-tab-pane>

		  <el-tab-pane label="空白" name="a3">

		  </el-tab-pane>
		</el-tabs>
	  </template>
	</div>

