<!DOCTYPE html>
<html lang="zh-CN" xmlns:li="http://www.w3.org/1999/xhtml" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <title>超挂机英雄传官方支付页</title>

    <!-- 引入样式 -->
    <link href="https://cdn.bootcss.com/element-ui/2.0.7/theme-chalk/index.css" rel="stylesheet">

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
    <!-- 先引入 Vue -->
    <script src="https://cdn.bootcss.com/vue/2.5.9/vue.js"></script>
    <script src="https://cdn.bootcss.com/vue-resource/1.3.4/vue-resource.min.js"></script>
    <!-- 引入组件库 -->
    <script src="https://cdn.bootcss.com/element-ui/2.0.7/index.js"></script>
</head>
<body>
<div id="app" v-loading="loading">
	<el-button type="danger" style="margin-top: 10px">挂机英雄传官方充值(非云顶)</el-button>
	<div style="margin-top: 25px;">
	  <el-input placeholder="输入玩家ID" v-model="uid" class="input-with-select">
		<el-select v-model="value" slot="prepend" placeholder="选择服务器">
			  <el-option
				v-for="item in options"
				:key="item.server_id"
				:label="item.server_name"
				:value="item.server_id">
			  </el-option>
		</el-select>
		<el-button slot="append" icon="el-icon-search" @click="search"></el-button>
	  </el-input>
	</div>

	<el-table
          :data="roles"
          style="width: 100%;margin-top: 10px;" border>
          <el-table-column
            label="昵称"
            width="180">
            <template slot-scope="scope">
				  <div slot="reference" class="name-wrapper">
					<el-tag size="medium">{{ scope.row.player_name }}</el-tag>
				  </div>
			  </template>
          </el-table-column>
          <el-table-column
            prop="player_rank"
            label="等级" style="color:#67C23A">
          </el-table-column>
        </el-table>

	<el-row style="margin-top: 32px;">
		<el-col :span="12">
        	<el-radio v-model="radio1" style="width:150px" label="1" border>6元(60钻)</el-radio>
        </el-col>
		<el-col :span="12">
        	<el-radio v-model="radio1" label="2" style="width:150px"  border>30元(300钻)</el-radio>
        </el-col>
    </el-row>

	<el-row style="margin-top: 10px;">
		<el-col :span="12">
        	<el-radio v-model="radio1" label="3" style="width:150px"  border>68元(680钻)</el-radio>
        </el-col>
		<el-col :span="12">
        	<el-radio v-model="radio1" label="4" style="width:150px"  border>198元(1980钻)</el-radio>
        </el-col>
    </el-row>

	<el-row style="margin-top: 10px;">
		<el-col :span="12">
        	<el-radio v-model="radio1" label="5" style="width:150px"  border>328元(3280钻)</el-radio>
        </el-col>
		<el-col :span="12">
        	<el-radio v-model="radio1" label="6" style="width:150px"  border>648元(6480钻)</el-radio>
        </el-col>
    </el-row>

	<el-row style="margin-top: 10px;">
		<el-col :span="12">
        	<el-radio v-model="radio1" label="7" style="width:150px"  border>25元(小月卡)</el-radio>
        </el-col>
		<el-col :span="12">
        	<el-radio v-model="radio1" label="8" style="width:150px"  border>50元(大月卡)</el-radio>
        </el-col>
    </el-row>

	<el-row style="margin-top: 10px;">
		<el-col :span="12" :offset="0">
        	<el-radio v-model="radio1" label="9" style="width:150px"  border>98元(年卡)</el-radio>
        </el-col>
    </el-row>

	<el-steps finish-status="success" style="margin-top: 20px;">
	  <el-step title="请求订单"></el-step>
	  <el-step title="扫码支付"></el-step>
	</el-steps>

	<el-button style="margin-top: 12px;" @click="getOrder" type="primary" icon="el-icon-success">请求订单</el-button>

</div>

<script>
    new Vue({
      el: '#app',
      data: function() {
        return {
        	visible: false,
        	payUrl: '',
        	options: ${servers},
        	value: '',
        	uid: '',
        	loading: false,
        	name: '',
        	level: '',
        	roles: [],
        	radio1: '1',
        	}
      },
      methods: {
      	search() {
      		var vm = this;

      		if (vm.value == '' || vm.uid == '') {
      			vm.$notify({
                          title: '警告',
                          message: '请选择服务器并填写玩家ID!',
                          duration: "1000",
                          type: 'warning'
                        });
      			return;
      		}

      		this.loading = true;
      		this.$http.post("/service/validateUser", {
      			"server_id": vm.value,
      			"user_name": vm.uid,
      		}, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							console.log(response.data);
      							var data = response.data;
      							if (data.success != 1) {
      							} else {
      								vm.roles = [data]
      							}

                            	//vm.$message("ok:" + vm.value + "," + vm.uid);
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("查询错误!");
                            })
      	},
      	getOrder() {
      		var vm = this;
      		if (vm.uid == '') {
      			vm.$notify({
                          title: '警告',
                          message: '未指定充值玩家',
                          duration: "1000",
                          type: 'warning'
                        });
      			return;
      		}

      		this.loading = true;
      		this.$http.get("/abp/get?index=" + vm.radio1 + "&price=" + "6" + "&uid=" + vm.uid +
      			"&order=" + (new Date()).valueOf().toString() + "&bundle=com.tumei.guaji", {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							console.log(response.bodyText);
      							if (response.bodyText == null || response.bodyText == '') {
                                	vm.$message.error("无法生成订单!");
      							} else {
      								vm.loading = true;
      								window.location.href = response.bodyText;
      							}
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("无法生成订单!");
                            })
      	},
      	open() {
      	this.$confirm('本次支付是否成功?', '提示', {
                  confirmButtonText: '成功',
                  cancelButtonText: '失败',
                  type: 'warning'
                }).then(() => {
                }).catch(() => {
                });
        }
      }
    })
  </script>

</body>
<style>
  .el-select .el-input {
    width: 130px;
  }
  .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }
</style>
</html>
