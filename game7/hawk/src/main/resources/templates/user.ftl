<el-input v-model="serverhost" class="input-with-select" size="small">
    <el-select v-model="serverid" slot="prepend" placeholder="选择服务器">
        <el-option
                v-for="item in servers"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            <span style="float: left; color: #ff0000">[{{ item.id }}] </span>
            <span style="float: right; color: #8492a6; font-size: 11px">{{ item.name }} {{ item.host }}</span>
        </el-option>
    </el-select>
    <el-button slot="append" icon="el-icon-search" @click="onServerChange">查询</el-button>
</el-input>

<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">

    <el-input placeholder="输入玩家ID或昵称" v-model="uid" class="input-with-select" size="small">
        <el-button slot="prepend">玩家</el-button>
        <el-button slot="append" icon="el-icon-search" @click="search">查询</el-button>
    </el-input>
</div>


<div style="margin-top: 20px; margin-left: 0px;" v-if="user">
    <template>
        <el-tabs v-model="tab" type="card" @tab-click="changeTab">

            <el-tab-pane label="角色信息" name="a2">
                <el-row>
                    <el-button @click="flushCache">刷新缓存[慎重使用]</el-button>
                </el-row>

                <el-row>
                    <el-input placeholder="" v-model="user.name" class="input-with-select" size="small">
                        <el-button slot="prepend">昵称</el-button>
                    </el-input>
                </el-row>

                <el-row>
                    <el-input placeholder="" v-model="user.level" class="input-with-select" size="small">
                        <el-button slot="prepend">等级</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="fixLevel">修改</el-button>
                    </el-input>
                </el-row>

                <el-row>
                    <el-input placeholder="" v-model="user.vip" class="input-with-select" size="small">
                        <el-button slot="prepend">贵族</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="fixVipLevel">修改</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.vipExp" class="input-with-select" size="small">
                        <el-button slot="prepend">贵族经验</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.coin" class="input-with-select" size="small">
                        <el-button slot="prepend">金币</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="">修改</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.gem" class="input-with-select" size="small">
                        <el-button slot="prepend">钻石</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="">修改</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.charge" class="input-with-select" size="small">
                        <el-button slot="prepend">充值</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.fr" class="input-with-select" size="small">
                        <el-button slot="prepend">是否禁止登录</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="forbidRole">修改</el-button>
                    </el-input>
                </el-row>
                <el-row>
                    <el-input placeholder="" v-model="user.fs" class="input-with-select" size="small">
                        <el-button slot="prepend">是否禁止发言</el-button>
                        <el-button slot="append" icon="el-icon-search" @click="forbidSay">修改</el-button>
                    </el-input>
                </el-row>

            </el-tab-pane>
            <el-tab-pane label="英雄" name="a3">
                <el-table :data="herosTable" style="width: 100%" :row-class-name="tableRowClassName">
                    <el-table-column type="expand">
                        <template slot-scope="props">
                            <el-form label-position="left" inline class="demo-table-expand">
                                <el-form-item label="装备1"><span>{{ props.row.eqs[0] }}</span></el-form-item>
                                <el-form-item label="装备2"><span>{{ props.row.eqs[1] }}</span></el-form-item>
                                <el-form-item label="装备3"><span>{{ props.row.eqs[2] }}</span></el-form-item>
                                <el-form-item label="装备4"><span>{{ props.row.eqs[3] }}</span></el-form-item>
                                <el-form-item label="装备5"><span>{{ props.row.eqs[4] }}</span></el-form-item>
                                <el-form-item label="装备6"><span>{{ props.row.eqs[5] }}</span></el-form-item>
                            </el-form>
                        </template>
                    </el-table-column>

                    <el-table-column prop="name" label="名字" width="180"></el-table-column>
                    <el-table-column prop="level" label="等级" width="180"></el-table-column>
                    <el-table-column prop="grade" label="突破"></el-table-column>
                    <el-table-column prop="gift" label="觉醒"></el-table-column>
                    <el-table-column prop="fate" label="天命"></el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="背包" name="a4">

                <h3><font color="green">道具</font></h3>
                <hr/>
                <el-table :data="packItems" max-height="300" style="width: 100%" :row-class-name="tableRowClassName">
                    <el-table-column prop="name" label="名字" width="180"></el-table-column>
                    <el-table-column prop="count" label="数量"></el-table-column>
                </el-table>


                <h3><font color="red">英雄</font></h3>
                <hr/>
                <el-table :data="packHeros" max-height="300" style="width: 100%" :row-class-name="tableRowClassName">
                    <el-table-column prop="name" label="名字" width="180"></el-table-column>
                    <el-table-column prop="level" label="等级" width="180"></el-table-column>
                    <el-table-column prop="grade" label="突破"></el-table-column>
                    <el-table-column prop="gift" label="觉醒"></el-table-column>
                    <el-table-column prop="fate" label="天命"></el-table-column>
                </el-table>

                <h3><font color="blue">装备</font></h3>
                <hr/>
                <el-table :data="packEquips" max-height="300" style="width: 100%" :row-class-name="tableRowClassName">
                    <el-table-column prop="name" label="名字" width="180"></el-table-column>
                    <el-table-column prop="level" label="等级" width="180"></el-table-column>
                    <el-table-column prop="grade" label="突破"></el-table-column>
                    <el-table-column prop="wake" label="觉醒"></el-table-column>
                </el-table>

            </el-tab-pane>


            <el-tab-pane label="充值" name="a5">
                <el-row style="margin-top: 32px;">
                    <el-col :span="12">
                        <el-radio v-model="radio1" style="width:150px" label="6" border>6元(60钻)</el-radio>
                    </el-col>
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="30" style="width:150px" border>30元(300钻)</el-radio>
                    </el-col>
                </el-row>

                <el-row style="margin-top: 10px;">
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="68" style="width:150px" border>68元(680钻)</el-radio>
                    </el-col>
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="198" style="width:150px" border>198元(1980钻)</el-radio>
                    </el-col>
                </el-row>

                <el-row style="margin-top: 10px;">
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="328" style="width:150px" border>328元(3280钻)</el-radio>
                    </el-col>
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="648" style="width:150px" border>648元(6480钻)</el-radio>
                    </el-col>
                </el-row>

                <el-row style="margin-top: 10px;">
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="25" style="width:150px" border>25元(小月卡)</el-radio>
                    </el-col>
                    <el-col :span="12">
                        <el-radio v-model="radio1" label="50" style="width:150px" border>50元(大月卡)</el-radio>
                    </el-col>
                </el-row>

                <el-row style="margin-top: 10px;">
                    <el-col :span="12" :offset="0">
                        <el-radio v-model="radio1" label="98" style="width:150px" border>98元(年卡)</el-radio>
                    </el-col>
                </el-row>

                <el-button style="margin-top: 12px;" @click="charge" type="primary" icon="el-icon-success">充值
                </el-button>
            </el-tab-pane>

            <el-tab-pane label="邮件" name="a6">

                <el-form :model="mail" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="标题" prop="title">
                        <el-input v-model="mail.title"></el-input>
                    </el-form-item>
                    <el-form-item label="内容" prop="content">
                        <el-input v-model="mail.content"></el-input>
                    </el-form-item>
                    <el-form-item label="奖励" prop="awards">
                        <el-tag
                                v-for="award in awards"
                                closable
                                :disable-transitions="false"
                                @close="removeAward(award)">
                            {{award}}
                        </el-tag>
                    </el-form-item>
                    <el-form-item label="奖励" prop="awards">
                        <el-autocomplete placeholder="请输入内容" v-model="awd" clearable
                                         :fetch-suggestions="queryItemAsync"
                                         @select="handleSelect">
                            <template slot-scope="{ item }">
                                <div class="name">{{item.key}}:{{ item.good }}</div>
                            </template>
                        </el-autocomplete>
                        <el-input-number v-model="awdCount" :min="1"></el-input-number>
                        <el-button icon="el-icon-search" @click="addAward">增加</el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="sendmail()">发送</el-button>
                    </el-form-item>
                </el-form>

            </el-tab-pane>

            <el-tab-pane label="副本" name="a7">
                <el-input placeholder="填写关卡" class="input-with-select" size="small" v-model="scene">
                    <el-button slot="prepend" @click="setScene">修改</el-button>
                </el-input>
            </el-tab-pane>

        </el-tabs>
    </template>
</div>

