<!DOCTYPE html>
<html lang="zh-CN" xmlns:li="http://www.w3.org/1999/xhtml" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <title>英雄无敌后台</title>

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
    <el-button type="danger" style="margin-top: 10px">英雄无敌管理页面</el-button>
    <div style="margin-top: 25px;" v-if="!logged">
        <el-row>
            <el-input placeholder="管理员账号" v-model="account" class="input-with-select">
                <el-button slot="prepend">帐号:</el-button>
            </el-input>
        </el-row>
        <el-row>
            <el-input placeholder="管理员密码" v-model="password" class="input-with-select">
                <el-button slot="prepend">密码:</el-button>
                <el-button type="primary" slot="append" icon="el-icon-menu" @click="logon">登录</el-button>
            </el-input>
        </el-row>
    </div>

    <div style="margin-top: 25px;" v-if="logged">
        <el-tabs :tab-position="tab_position" v-if="logged">
            <el-tab-pane label="信息">
                <#include "system.ftl" />
            </el-tab-pane>
            <el-tab-pane label="帐号管理">
                <#include "account.ftl" />
            </el-tab-pane>
            <el-tab-pane label="服务器">
                <#include "server.ftl" />
            </el-tab-pane>
            <el-tab-pane label="玩家">
                <#include "user.ftl" />
            </el-tab-pane>
            <el-tab-pane label="维护">
                <#include "maintain.ftl" />
            </el-tab-pane>
            <el-tab-pane label="配置">
                <comp_conf></comp_conf>
            </el-tab-pane>
        </el-tabs>
    </div>
</div>

<script>
	Date.prototype.Format = function (fmt) { //author: meizz
		var o = {
			"M+": this.getMonth() + 1, //月份
			"d+": this.getDate(), //日
			"h+": this.getHours(), //小时
			"m+": this.getMinutes(), //分
			"s+": this.getSeconds(), //秒
			"q+": Math.floor((this.getMonth() + 3) / 3), //季度
			"S": this.getMilliseconds() //毫秒
		};
		if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
		for (var k in o)
		if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
		return fmt;
	}

    <#include "./components/conf.ftl" />

    new Vue({
      el: '#app',
      data: function() {
        return {
        	items: [], // 只读的物品列表
        	heros: [], // 只读的英雄列表
        	tab_position: 'top',
        	loading: false,
        	serveredit: false,  // 标识服务器表格是否可以编辑
        	product: false, // 是否沙盒支付
        	zone: 0, // 查询支付的参数 服务器 0 标识全部
        	source: '', // 查询服务器的来源
        	account: localStorage.getItem('name'),
        	password: localStorage.getItem('passwd'),
        	jwt: '',
        	bulletin_path: 'uc',
        	bulletin: '',
        	reloadable: false,
        	reloadable_guild: false,
        	servers: null,	// 服务器列表
        	topaccid: "",	// 查询具体帐号的充值情况,可选
        	topcount: 3, 	// 查询最高几个人
        	topmode: 1,		// 查询的模式
        	topacc: [], 	// 查询最高充值
        	toplogs: [],	// 最近操作日志
        	topscene: [],   // 最高关卡

        	logged: false,
        	today: null,    // 总体信息查询日期
        	from: null,		// 开始查询日期
        	income: null,		// 收入
        	serverid: null,	// 当前选择的服务器id
        	serverhost: null,	// 当前选择服务器的host
        	serverinfo: null,	// 当前选择的服务器的状态
        	uid: 1097001,		// 当前搜索的玩家的id

			accid: null,		// 需要查询的帐号id
			acc: null,		// 查询的帐号信息
			acctab: "a1",		// 帐号选项卡

			user: null,			// 玩家基本信息
			tab: "a2",	 		// 选项卡

			tabServer: "a1",	// 服务器选项卡

			heros: null,	// 英雄信息
			herosTable: null,
			packHeros: null, // 背包中的英雄
			packEquips: null, // 背包中的装备
			packItems: null, // 背包中的物品

			sysInfos: null, // 多日的充值查询

			radio1: '6',	// 充值选项
			scene: null,    // 关卡

			mail: {
				title: '系统奖励',
				content: '这是一封来自管理员发送的奖励邮件.',
				awards: '',
			},
			awd: '',		// 奖励的临时单个记录的内容
			awdCount: 1,	// 奖励的临时单个记录的数量
			awards: [],		// 奖励的完整记录
			notify: '' 	// 全局通知
        }
      },
      methods: {
      	logon() {
      		var today = new Date().Format('yyyy-MM-dd')
      		var nextday = new Date()
      		nextday.setTime(nextday.getTime() + 3600*24*1000)
      		nextday = nextday.Format('yyyy-MM-dd')
      		this.from = [today, nextday]
      		this.today = today
      		var vm = this;

      		if (vm.account == '' || vm.password == '') {
      			vm.$notify({
                          title: '警告',
                          message: '账号或者密码不能为空.',
                          duration: "1000",
                          type: 'warning'
                        });
      			return;
      		}

      		this.loading = true;
      		this.$http.get("/logon_gm?account=" + vm.account + "&password=" + vm.password + "&version=1",
	      		{}, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							var ret = response.data;
      							if (ret.err != 0) {
                            		vm.$message.error(ret.result);
      							} else {
      								vm.jwt = ret.jwt
      								vm.servers = ret.servers
                            		vm.$notify({
	                            		message: "认证成功",
	                            		type: 'success'
                            		})
                            		localStorage.setItem('name', vm.account)
                            		localStorage.setItem('passwd', vm.password)
                            		//vm.onServerChange()
                            		vm.logged = true
                            		vm.loadItems()
                            		vm.loadHeros()
                            		vm.loadSysInfos()
                            		vm.checkGuildReloadable(0)
      							}
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("查询错误!");
                            })
      	},

      	// 服务器切换选择,刷新当前状态
      	onServerChange() {
      		for (var i = 0; i < this.servers.length; ++i) {
      			if (this.servers[i].id == this.serverid) {
      				this.serverhost = this.servers[i].host;
                    if (this.$refs.st != null) {
                    	this.$refs.st.setCurrentRow(i)
                    }
      				break;
      			}
      		}

			var url = "http://" + this.serverhost + "/cmd/getInfo?xtkn=" + this.jwt
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.serverinfo = response.data
                            })
                            .catch(function(response) {
      							this.loading = false;
                                this.$message.error("服务器:" + this.serverhost + "没有启动!")
                            })
            this.checkReloadable(0)
      	},
      	onServerChangeTable(val) {
      		this.serverid = val.id
      		this.serverhost = val.host

			var url = "http://" + this.serverhost + "/cmd/getInfo?xtkn=" + this.jwt
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.serverinfo = response.data
                            })
                            .catch(function(response) {
      							this.loading = false;
                                this.$message.error("服务器:" + this.serverhost + "没有启动!")
                            })
            this.checkReloadable(0)
      	},
      	getSummaries(param) {
            const { columns, data } = param;
            const sums = [];
            columns.forEach((column, index) => {
              if (index === 0) {
                sums[index] = '统计';
                return;
              } else if (index <= 2) {
                  const values = data.map(item => Number(item[column.property]));
                  if (!values.every(value => isNaN(value))) {
                    sums[index] = values.reduce((prev, curr) => {
                      const value = Number(curr);
                      if (!isNaN(value)) {
                        return prev + curr;
                      } else {
                        return prev;
                      }
                    }, 0);
                    sums[index] += ' 分';
                  } else {
                    sums[index] = 'N/A';
                  }
              }
            });
            return sums;
        },

      	getBulletin() {
			var url = "/cmd/getBulletin?xtkn=" + this.jwt + "&file=" + this.bulletin_path
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.bulletin = response.bodyText
                            })
                            .catch(function(response) {
      							this.loading = false;
                            })
      	},
      	fixBulletin() {
			var url = "/cmd/fixBulletin?xtkn=" + this.jwt + "&data=" + encodeURI(this.bulletin) + "&file=" + this.bulletin_path
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.bulletin = response.bodyText
                            })
                            .catch(function(response) {
      							this.loading = false;
                            })
      	},

      	checkGuildReloadable(mode) {
			var url = "/cmd/isGuildReloadable?xtkn=" + this.jwt + "&mode=" + mode
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.reloadable_guild = (response.bodyText == "true") ? true : false
                            })
                            .catch(function(response) {
      							this.loading = false;
                            })
      	},

      	checkReloadable(mode) {
			var url = "http://" + this.serverhost + "/cmd/isReloadable?xtkn=" + this.jwt + "&mode=" + mode
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.reloadable = (response.bodyText == "true") ? true : false
                            })
                            .catch(function(response) {
      							this.loading = false;
                            })
      	},
      	search() {
      		var vm = this;
      		if (vm.uid == null) {
      			vm.$notify({
                          title: '警告',
                          message: '未指定搜索的玩家',
                          duration: "1000",
                          type: 'warning'
                        });
      			return;
      		}

      		// 1. 根据当前的serverid,找到对应的serverhost
      		for (var i = 0; i < vm.servers.length; ++i) {
      			if (vm.servers[i].id == vm.serverid) {
      				vm.serverhost = vm.servers[i].host;
      				break;
      			}
      		}

			var url = ""
      		if (isNaN(vm.uid)) { // 0 或者非数字
				url = "http://" + vm.serverhost + "/role/searchByName?xtkn=" + vm.jwt + "&name=" + vm.uid
      		} else {
				url = "http://" + vm.serverhost + "/role/search?xtkn=" + vm.jwt + "&id=" + vm.uid
      		}

      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.user = response.data
      							if (isNaN(vm.uid)) {
      								vm.uid = vm.user.id
      							}
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},

      	queryIncome() {
			var url = "/cmd/getIncome?xtkn=" + this.jwt + "&begin=" + this.from[0] + "&end=" + this.from[1] + "&sandbox=" + (this.product ? 0 : 1) + "&zone=" + this.zone + "&source=" + this.source
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							this.loading = false;
      							this.income = parseInt(response.data) / 100 + "元"
                            })
                            .catch(function(response) {
      							this.loading = false;
                                this.$message.error("错误:" + response);
                            })
      	},

		// 修改等级
      	fixLevel() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/fixLevel?xtkn=" + vm.jwt + "&id=" + vm.uid + "&level=" + vm.user.level
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},
      	// vip等级经验
      	fixVipLevel() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/fixVipLevel?xtkn=" + vm.jwt + "&id=" + vm.uid + "&level=" + vm.user.vip + "&exp=" + vm.user.vipExp
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},

      	getTopScene() {
      		if (this.topcount > 100) {
      			this.topcount = 100
      		}
			var url = "http://" + this.serverhost + "/role/getTopScene?xtkn=" + this.jwt + "&mode=" + this.topmode
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.topscene = response.data
								//console.log(JSON.stringify(this.topscene))
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},

      	forbidRole() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/forbidRole?xtkn=" + vm.jwt + "&id=" + vm.uid + "&val=" + vm.user.fr
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},

      	forbidSay() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/forbidSay?xtkn=" + vm.jwt + "&id=" + vm.uid + "&val=" + vm.user.fs
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},

        flushGuild() {
      		var vm = this;
			var url = "/cmd/flushGuild?xtkn=" + vm.jwt
      		this.loading = true;
      		this.$http.get(url, {})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message.info(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},

      	flushReadonly() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/cmd/flushReadonly?xtkn=" + vm.jwt
      		this.loading = true;
      		this.$http.get(url, {})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message.info(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},
      	flushFestival() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/cmd/flushFestival?xtkn=" + vm.jwt
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message.info(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},
      	flushActivity() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/cmd/flushActivity?xtkn=" + vm.jwt
      		this.loading = true;
      		this.$http.get(url, {emulateJSON: true})
                            .then((response) => {
      							vm.loading = false;
      							vm.$message.info(response.bodyText)
                            })
                            .catch(function(response) {
      							vm.loading = false;
                                vm.$message.error("错误:" + response);
                            })
      	},


      	changeTab(_tab, _event) {
      		if (this.tab == "a3") {
      			this.infoHeros()
      		} else if (this.tab == "a4") {
      			this.infoPacks()
      		}
      	},

		// 查看英雄
      	infoHeros() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/heros?xtkn=" + vm.jwt + "&id=" + vm.uid;
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
						   .then((response) => {
								vm.loading = false;
								var heros = response.data;
								vm.herosTable = []
								for (var i = 0; i < heros.heros.length; ++i) {
									var h = heros.heros[i]
									if (h != null) {
										var hi = this.findHero(h.hero)
										var ht = {
											name: hi.name,
											level: h.level,
											grade: h.grade,
											fate: h.fate,
											gift: h.gift,
											eqs: []
										}
										vm.herosTable[i] = ht
										for (var j = 0; j < h.equipStructs.length; ++j) {
											var eq = h.equipStructs[j]
											if (eq != null) {
												var ei = this.findItem(eq.id)
												ht.eqs[j] = "编号:" + ei.good + ",强化:" + eq.level + ",精炼:" + eq.grade + ",觉醒:" + eq.wake
											}
										}
									}
								}
						   })
						   .catch(function(response) {
								vm.loading = false;
							    vm.$message.error("错误:" + response);
						   })
      	},

		// 查看背包
      	infoPacks() {
      		var vm = this;
			var url = "http://" + vm.serverhost + "/role/packs?xtkn=" + vm.jwt + "&id=" + vm.uid;
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
						   .then((response) => {
								vm.loading = false;
								var packs = response.data;
								//console.log(response.bodyText)
								vm.packHeros = []
								for (var i = 0; i < packs.heros.length; ++i) {
									var h = packs.heros[i]
									if (h != null) {
										var hi = this.findHero(h.hero)
										var ht = {
											name: hi.name,
											level: h.level,
											grade: h.grade,
											fate: h.fate,
											gift: h.gift,
											eqs: []
										}
										vm.packHeros[i] = ht
										for (var j = 0; j < h.equipStructs.length; ++j) {
											var eq = h.equipStructs[j]
											if (eq != null) {
												var ei = this.findItem(eq.id)
												ht.eqs[j] = "编号:" + ei.good + ",强化:" + eq.level + ",精炼:" + eq.grade + ",觉醒:" + eq.wake
											}
										}
									}
								}

								vm.packEquips = []
								for (var i = 0; i < packs.equips.length; ++i) {
									var h = packs.equips[i]
									if (h != null) {
                                        var ei = this.findItem(h.id)
										var ht = {
											name: ei.good,
											level: h.level,
											grade: h.grade,
											wake: h.wake,
										}
										vm.packEquips[i] = ht
									}
								}

								vm.packItems = []
								for (var key in packs.items) {
									var c = packs.items[parseInt(key)]
                                    var ei = this.findItem(key)
                                    var ht = {
                                        name: ei.good,
                                        count: c,
                                    }
									//console.log("key:" + ht.name + "," + ht.count)
                                    vm.packItems.push(ht)
								}
						   })
						   .catch(function(response) {
								vm.loading = false;
							    vm.$message.error("错误:" + response);
						   })
      	},


      	charge() {
			this.$confirm('是否准备充值' + this.radio1 + '元?', '提示', {
					  confirmButtonText: '确定',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
						var url = "http://" + this.serverhost + "/role/charge?xtkn=" + this.jwt + "&id=" + this.uid + "&rmb=" + parseInt(this.radio1) * 100
						this.loading = true;
						this.$http.get(url, {emulateJSON: false})
										.then((response) => {
											this.loading = false;
											this.$message.info(response.bodyText)
										})
										.catch(function(response) {
											this.loading = false;
											this.$message.error("错误:" + response);
										})
					}).catch(() => {

					});

      	},
      	flushCache() {
            var url = "http://" + this.serverhost + "/cmd/flushCache?xtkn=" + this.jwt + "&uid=" + this.uid
            this.loading = true;
            this.$http.get(url, {emulateJSON: false})
                        .then((response) => {
                            this.loading = false;
                            this.$message.info(response.bodyText)
                        })
                        .catch(function(response) {
                            this.loading = false;
                            this.$message.error("错误:" + response);
                        })
      	},
      	setScene() {
            var url = "http://" + this.serverhost + "/role/setScene?xtkn=" + this.jwt + "&id=" + this.uid + "&scene=" + this.scene
            this.loading = true;
            this.$http.get(url, {emulateJSON: false})
                        .then((response) => {
                            this.loading = false;
                            this.$message.info(response.bodyText)
                        })
                        .catch(function(response) {
                            this.loading = false;
                            this.$message.error("错误:" + response);
                        })
      	},
      	sendmail() {
      		this.mail.awards = ''
      		for (var i = 0; i < this.awards.length; ++i) {
      			var awd = this.awards[i].split(':')[1]
      			if (awd == null || awd.split(',').length % 2 != 0) {
      				this.$message.error('邮件格式错误!')
      				return
      			}

      			if (this.mail.awards.length > 0) {
      				this.mail.awards += ','
      			}
      			this.mail.awards += awd
      		}

			this.$confirm('是否准备发送邮件?', '提示', {
					  confirmButtonText: '确定',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
						var url = "http://" + this.serverhost + "/cmd/addawardmail?xtkn=" + this.jwt + "&id=" + this.uid + "&title=" + this.mail.title + "&content=" + this.mail.content + "&awards=" + this.mail.awards
						this.loading = true;
						this.$http.get(url, {emulateJSON: false})
										.then((response) => {
											this.loading = false;
											this.$message.info(response.bodyText)
										})
										.catch(function(response) {
											this.loading = false;
											this.$message.error("错误:" + response);
										})
					}).catch(() => {

					});

      	},
      	sendmailserver() {
      		this.mail.awards = ''
      		for (var i = 0; i < this.awards.length; ++i) {
      			var awd = this.awards[i].split(':')[1]
      			if (awd == null || awd.split(',').length % 2 != 0) {
      				this.$message.error('邮件格式错误!')
      				return
      			}

      			if (this.mail.awards.length > 0) {
      				this.mail.awards += ','
      			}
      			this.mail.awards += awd
      		}

			this.$confirm('是否准备[[全服]]发送邮件?', '提示', {
					  confirmButtonText: '确定',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
						var url = "http://" + this.serverhost + "/cmd/addawardmailAll?xtkn=" + this.jwt + "&title=" + this.mail.title + "&content=" + this.mail.content + "&awards=" + this.mail.awards
						this.loading = true;
						this.$http.get(url, {emulateJSON: false})
										.then((response) => {
											this.loading = false;
											this.$message.info(response.bodyText)
										})
										.catch(function(response) {
											this.loading = false;
											this.$message.error("错误:" + response);
										})
					}).catch(() => {

					});

      	},
      	sendmailserverall() {
      		this.mail.awards = ''
      		for (var i = 0; i < this.awards.length; ++i) {
      			var awd = this.awards[i].split(':')[1]
      			if (awd == null || awd.split(',').length % 2 != 0 || awd.split(',').length <= 0) {
      				this.$message.error('邮件格式错误!')
      				return
      			}

      			if (this.mail.awards.length > 0) {
      				this.mail.awards += ','
      			}
      			this.mail.awards += awd
      		}

			this.$confirm('是否准备!!!!全区全服!!!!发送邮件?', '提示', {
					  confirmButtonText: '确定',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
                        this.loading = true;

                        var num = this.servers.length

                        for (var i = 0; i < this.servers.length; ++i) {
                        	var host = this.servers[i].host;
                        	console.log("server:" + host);
                        	var url = "http://" + host + "/cmd/addawardmailAll?xtkn=" + this.jwt + "&title=" + this.mail.title + "&content=" + this.mail.content + "&awards=" + this.mail.awards

                            this.$http.get(url, {emulateJSON: false})
                                            .then((response) => {
                                                console.log("服务器[" + response.url + "] 邮件发送完毕.");
                                                var h = response.url.indexOf('/', 8)
                                                h = response.url.substring(0, h)

                                                this.$notify.info({
                                                	title: '成功',
                                                	message: "邮件:" + h,
                                                });

                                                --num
                                                if (num <= 0) {
                                                	this.loading = false
                                                }
                                            })
                                            .catch(function(response) {
                                                console.log("服务器[" + response.url + "] 邮件发送失败.");
                                                var h = response.url.indexOf('/', 8)
                                                h = response.url.substring(0, h)
                                                this.$notify.error({
                                                	title: '错误',
                                                	message: "邮件:" + h,
                                                	duration: 0
                                                });

                                                --num
                                                if (num <= 0) {
                                                	this.loading = false
                                                }
                                            })
                        }
                    }).catch(() => {
                        this.loading = false;
                    });
        },
      	flushServerAll(cmd) {
            this.loading = true;
            var num = this.servers.length

            for (var i = 0; i < this.servers.length; ++i) {
                var host = this.servers[i].host;
                var url = "http://" + host + "/cmd/" + cmd + "?xtkn=" + this.jwt

                this.$http.get(url, {emulateJSON: false})
                                .then((response) => {
                                    console.log("服务器[" + response.url + "] 刷新.");
                                    var h = response.url.indexOf('/', 8)
                                    h = response.url.substring(0, h)

                                    this.$notify({
                                        title: '成功',
                                        message: "刷新:" + h + " 命令" + cmd,
                                    });

                                    --num
                                    if (num <= 0) {
                                        this.loading = false
                                    }
                                })
                                .catch(function(response) {
                                    var h = response.url.indexOf('/', 8)
                                    h = response.url.substring(0, h)
                                    this.$notify.error({
                                        title: '错误',
                                        message: "刷新:" + h + " 命令" + cmd,
                                        duration: 0
                                    });

                                    --num
                                    if (num <= 0) {
                                        this.loading = false
                                    }
                                })
            }
        },


      	loadItems() {
      		var url = "/cmd/getItems?xtkn=" + this.jwt
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.items = response.data
							})
							.catch(function(response) {
							})
      	},
      	findItem(id) {
      		for (var i = 0; i < this.items.length; ++i) {
      			var item = this.items[i]
      			if (item.key == id) {
      				return item;
      			}
      		}
      		return null;
      	},

      	loadHeros() {
      		var url = "/cmd/getHeros?xtkn=" + this.jwt
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.heros = response.data
							})
							.catch(function(response) {
							})
      	},
      	findHero(id) {
      		id = parseInt(id)
      		for (var i = 0; i < this.heros.length; ++i) {
      			var item = this.heros[i]
      			if (item.key === id) {
      				return item;
      			}
      		}
      		return null;
      	},

      	// 系统界面的收入,活跃,留存
      	loadSysInfos() {
              		var url = "/cmd/recentInfos?xtkn=" + this.jwt + "&date=" + this.today
        			this.$http.get(url, {emulateJSON: true})
        							.then((response) => {
        								this.sysInfos = response.data
        							})
        							.catch(function(response) {
        							})
              	},

      	createStateFilter(queryString) {
                return (state) => {
                  var rtn = (state.good.toLowerCase().indexOf(queryString.toLowerCase()) === 0) || ((state.key + "").indexOf(queryString) === 0);
                  return rtn;
                };
              },
      	queryItemAsync(queryString, cb) {
			var results = queryString ? this.items.filter(this.createStateFilter(queryString)) : this.items;
			cb(results);
		},
		handleSelect(item) {
			this.awd = item.good + ":" + item.key
		},
      	sendBroadcast() {
			var url = "/cmd/sendNotify?xtkn=" + this.jwt + "&zone=0&msg=" + this.notify
			this.loading = true;
			this.$http.get(url, {emulateJSON: false})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})

      	},
      	// 删除其中一个奖励
      	removeAward(awd) {
      		this.awards.splice(this.awards.indexOf(awd), 1);
      	},
      	addAward() {
      		var awd = this.awd + "," + this.awdCount
      		this.awards.push(awd);
      	},

      	getTopAccount() {
      		if (this.topcount > 100) {
      			this.topcount = 100
      		}
			var url = "/cmd/getTopAccount?xtkn=" + this.jwt + "&count=" + this.topcount + "&id=" + this.topaccid
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.topacc = response.data
								//console.log(this.topacc)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	getAdminLogs() {
      		if (this.topcount > 100) {
      			this.topcount = 100
      		}
			var url = "/cmd/getAdminLogs?xtkn=" + this.jwt + "&count=" + this.topcount
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.toplogs = response.data
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	findAccount() {
			var url = "/cmd/infoAccount?xtkn=" + this.jwt + "&uid=" + this.accid
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.acc = response.data
								this.accid = this.acc.id
								this.$message.info(response.bodyText)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	bindAccId() {
			var url = "/cmd/bindAccount?xtkn=" + this.jwt + "&acc=" + this.acc.account + "&oid=" + this.acc.id
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	modifyPassword() {
			var url = "/cmd/modifyPassword?xtkn=" + this.jwt + "&uid=" + this.accid + "&password=" + this.acc.passwd
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	modifyAdmin() {
			var url = "/cmd/modifyAdmin?xtkn=" + this.jwt + "&uid=" + this.accid + "&admin=" + this.acc.role
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	loadServers() {
      		var url = "/cmd/getServers?xtkn=" + this.jwt
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.servers = response.data
							})
							.catch(function(response) {
							})
      	},
      	addServer() {
      		this.servers.push({id:999, name: '填写区名字', status: '新服', host:'', start: new Date()})
      	},
      	saveServer(s) {
			var url = "/cmd/saveServer?xtkn=" + this.jwt
			this.loading = true;
			this.$http.post(url, s, {})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
								this.loadServers()
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},
      	delServer(s) {
			var url = "/cmd/delServer?xtkn=" + this.jwt + "&oid=" + s.objectId
			this.loading = true;
			this.$http.get(url, {emulateJSON: true})
							.then((response) => {
								this.loading = false;
								this.$message.info(response.bodyText)
								this.loadServers()
							})
							.catch(function(response) {
								this.loading = false;
								this.$message.error("错误:" + response);
							})
      	},

		tableRowClassName({row, rowIndex}) {
            if (rowIndex % 2 == 0) {
              return 'warning-row';
            }
            return '';
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
.el-tag { margin-left: 5px; margin-right: 5px; }

.el-row {
	margin-bottom: 20px;
}
.el-col {
	border-radius: 4px;
}

.el-select .el-input {
	width: 130px;
}

.input-with-select .el-input-group__prepend {
	background-color: #fff;
}

.demo-table-expand {
	font-size: 0;
}
.demo-table-expand label {
  width: 120px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
.el-table .warning-row {
	background: oldlace;
}
.el-table .success-row {
	background: #f0f9eb;
}




</style>
</html>
