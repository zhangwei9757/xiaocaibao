<div style="margin-top: 0px; margin-left: 0px;" v-if="logged">
    <h2>服务器列表
        <el-switch inline v-model="serveredit" inactive-text="编辑"></el-switch>

        <el-button
                type="info"
                size="mini"
                @click="loadServers()">刷新
        </el-button>

        <el-button
                type="success"
                size="mini"
                @click="addServer()">新增
        </el-button>

    </h2>

    <el-table
            ref="st"
            :data="servers"
            border
            stripe
            highlight-current-row
            max-height=300
            style="width: 100%">

        <el-table-column
                label="区号"
                width="80">
            <template slot-scope="scope">
                <el-input v-if="serveredit" v-model="scope.row.id"></el-input>
                <span v-else>{{scope.row.id}}</span>
            </template>
        </el-table-column>

        <el-table-column
                label="名字"
                width="180">
            <template slot-scope="scope">
                <el-input v-if="serveredit" v-model="scope.row.name"></el-input>
                <span v-else>{{scope.row.name}}</span>
            </template>
        </el-table-column>

        <el-table-column
                label="地址"
                width="240">
            <template slot-scope="scope">
                <el-input v-if="serveredit" v-model="scope.row.host"></el-input>
                <span v-else>{{scope.row.host}}</span>
            </template>
        </el-table-column>

        <el-table-column
                label="状态"
                width="100">
            <template slot-scope="scope">
                <el-input v-if="serveredit" v-model="scope.row.status"></el-input>
                <span v-else>{{scope.row.status}}</span>
            </template>
        </el-table-column>

        <el-table-column
                label="开服时间" width="240">
            <template slot-scope="scope">
                <el-date-picker
                        v-model="scope.row.start"
                        type="datetime"
                        v-if="serveredit"
                        placeholder="开服时间">
                </el-date-picker>
                <el-date-picker
                        v-model="scope.row.start"
                        type="datetime"
                        :disabled="true"
                        v-else
                        placeholder="开服时间">
                </el-date-picker>
            </template>
        </el-table-column>

        <el-table-column label="操作"> min-width="240">
            <template slot-scope="scope">
                <el-button
                        size="mini"
                        @click="onServerChangeTable(scope.row)">切换
                </el-button>

                <el-button
                        size="mini"
                        @click="saveServer(scope.row)">保存
                </el-button>

                <el-button
                  size="mini"
                  type="danger"
                  @click="delServer(scope.row)">删除</el-button>
            </template>
        </el-table-column>

    </el-table>
</div>


<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">
</div>

<br/>
<br/>
<br/>

<font color="red" size="32">{{serverid}}区 - {{serverhost}}</font>
<hr/>

<el-tabs v-model="tabServer" type="card">
    <el-tab-pane label="信息" name="a1">

        <h2>服务器操作:</h2>
        <div style="margin-top: 10px; margin-left: 0px;" v-if="logged">
            <el-button size="mini" @click="flushReadonly">刷新只读配置</el-button>
            <el-button size="mini" @click="flushFestival">刷新节日配置</el-button>
            <el-button size="mini" @click="flushActivity">刷新活动配置</el-button>

            <el-switch inline v-model="reloadable" active-text="代码热更新" inactive-text="禁止热更新"
                @change="checkReloadable(1)"></el-switch>

            <h2>基本信息</h2>
            <hr/>
            <el-form v-if="serverinfo" :model="serverinfo" label-position="center" inline class="demo-table-expand">
                <el-form-item label="当前在线">
                    <span>{{ serverinfo.users }}</span>
                </el-form-item>
                <el-form-item label="今日活跃">
                    <span>{{ serverinfo.dau }}</span>
                </el-form-item>
                <el-form-item label="今日新增活跃">
                    <span>{{ serverinfo.danu }}</span>
                </el-form-item>
                <el-form-item label="今日充值">
                    <span>{{ serverinfo.charge / 100 }}元</span>
                </el-form-item>
                <el-form-item label="今日新增充值">
                    <span>{{ serverinfo.newCharge / 100 }}元</span>
                </el-form-item>

                <el-form-item label="运行时间">
                    <span>{{ serverinfo.upTime}}秒</span>
                </el-form-item>
                <el-form-item label="使用内存">
                    <span>{{ serverinfo.totalMemory}}MB</span>
                </el-form-item>
                <el-form-item label="最大内存">
                    <span>{{ serverinfo.maxMemory}}MB</span>
                </el-form-item>
                <el-form-item label="空闲内存">
                    <span>{{ serverinfo.freeMemory}}MB</span>
                </el-form-item>
            </el-form>
        </div>

        <div style="margin-top: 0px; margin-left: 0px;" v-if="logged">
            <h2>排行榜</h2>
            <el-form label-width="80px">
                <el-form-item>
                    <div style="margin-top: 20px">
                        <el-radio-group v-model="topmode" size="medium">
                            <el-radio-button label="1">充值</el-radio-button>
                            <el-radio-button label="2">消费</el-radio-button>
                            <el-radio-button label="3">等级</el-radio-button>
                            <el-radio-button label="4">星星</el-radio-button>
                            <el-radio-button label="5">副本</el-radio-button>
                            <el-radio-button label="6">战力</el-radio-button>
                        </el-radio-group>
                    </div>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" icon="el-icon-menu" @click="getTopScene">查询</el-button>
                </el-form-item>
            </el-form>

            <el-table
                    :data="topscene"
                    stripe
                    border
                    max-height="600"
                    style="width: 100%">
                <el-table-column
                        type="index">
                </el-table-column>

                <el-table-column
                        prop="name"
                        label="角色ID" width="240">
                </el-table-column>

                <el-table-column
                        prop="value"
                        label="数值">
                </el-table-column>
            </el-table>

            <br/>
            <br/>

            <h2>聊天</h2>
            <el-form label-width="80px">
                <el-form-item>
                    <el-button type="primary" icon="el-icon-menu" @click="viewChat">刷新</el-button>
                </el-form-item>
            </el-form>

            <el-table
                    :data="chats"
                    stripe
                    border
                    max-height="600"
                    style="width: 100%">
                <el-table-column
                        type="index">
                </el-table-column>

                <el-table-column
                        prop="id"
                        label="角色ID" width="140">
                </el-table-column>
                <el-table-column
                        prop="name"
                        label="昵称" width="150">
                </el-table-column>
                <el-table-column
                        prop="vip"
                        label="VIP" width="80">
                </el-table-column>

                <el-table-column
                        prop="msg"
                        label="聊天">
                </el-table-column>

                <el-table-column label="操作"> min-width="240">
                    <template slot-scope="scope">
                        <el-button
                                size="mini"
                                @click="forbidSay(null, scope.row.id, 1)">禁言
                        </el-button>
                        <el-button
                                size="mini"
                                @click="forbidSay(null, scope.row.id, 0)">解禁
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <br/>
            <br/>
            <br/>
            <br/>
        </div>
    </el-tab-pane>

    <el-tab-pane label="邮件" name="a2">
        <el-form :model="mail" label-width="100px" class="demo-ruleForm">
            <el-form-item label="标题" prop="title">
                <el-input v-model="mail.title"></el-input>
            </el-form-item>
            <el-form-item label="内容" prop="content">
                <el-input v-model="mail.content"></el-input>
            </el-form-item>
            <el-form-item label="奖励" prop="awards">
                <el-tag v-for="award in awards" closable :disable-transitions="false" @close="removeAward(award)">
                    {{award}}
                </el-tag>
            </el-form-item>
            <el-form-item label="奖励" prop="awards">
                <el-autocomplete placeholder="请输入内容" v-model="awd" clearable :fetch-suggestions="queryItemAsync"
                                 @select="handleSelect">
                    <template slot-scope="{ item }">
                        <div class="name">{{item.key}}:{{ item.good }}</div>
                    </template>
                </el-autocomplete>
                <el-input-number v-model="awdCount" :min="1"></el-input-number>
                <el-button icon="el-icon-search" @click="addAward">增加</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="sendmailserver()">发送</el-button>
            </el-form-item>
        </el-form>
    </el-tab-pane>


    <el-tab-pane label="一键全邮件" name="a3">
        <el-form :model="mail" label-width="100px" class="demo-ruleForm">
            <el-form-item label="标题" prop="title">
                <el-input v-model="mail.title"></el-input>
            </el-form-item>
            <el-form-item label="内容" prop="content">
                <el-input v-model="mail.content"></el-input>
            </el-form-item>
            <el-form-item label="奖励" prop="awards">
                <el-tag v-for="award in awards" closable :disable-transitions="false" @close="removeAward(award)">
                    {{award}}
                </el-tag>
            </el-form-item>
            <el-form-item label="奖励" prop="awards">
                <el-autocomplete placeholder="请输入内容" v-model="awd" clearable :fetch-suggestions="queryItemAsync"
                                 @select="handleSelect">
                    <template slot-scope="{ item }">
                        <div class="name">{{item.key}}:{{ item.good }}</div>
                    </template>
                </el-autocomplete>
                <el-input-number v-model="awdCount" :min="1"></el-input-number>
                <el-button icon="el-icon-search" @click="addAward">增加</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="sendmailserverall()">发送</el-button>
            </el-form-item>
        </el-form>
    </el-tab-pane>

    <el-tab-pane label="一键所有刷新" name="a4">
        <el-button size="mini" @click="flushServerAll('flushReadonly')">刷新只读配置</el-button>
        <el-button size="mini" @click="flushServerAll('flushFestival')">刷新节日配置</el-button>
        <el-button size="mini" @click="flushServerAll('flushActivity')">刷新活动配置</el-button>
    </el-tab-pane>


</el-tabs>
