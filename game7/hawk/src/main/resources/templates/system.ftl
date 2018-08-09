<div style="margin-top: 0px; margin-left: 0px;" v-if="logged">
    <h2>总体信息</h2>

    <el-form label-width="80px">
        <el-form-item label="选择日期">
            <el-date-picker
                    v-model="today"
                    type="date"
                    value-format="yyyy-MM-dd">
            </el-date-picker>
        </el-form-item>

        <el-form-item>
            <el-button type="primary" icon="el-icon-menu" @click="loadSysInfos">查询</el-button>
        </el-form-item>
    </el-form>


    <el-table
            :data="sysInfos"
            stripe
            border
            show-summary
            :summary-method="getSummaries"
            style="width: 100%">
        <el-table-column
                fixed
                prop="day"
                label="日期" width="90">
        </el-table-column>

        <el-table-column
                prop="charge"
                label="收入">
        </el-table-column>

        <el-table-column
                prop="ncharge"
                label="新人收入">
        </el-table-column>

        <el-table-column
                prop="dau"
                label="总玩家">
        </el-table-column>
        <el-table-column
                prop="danu"
                label="新增玩家">
        </el-table-column>
        <el-table-column
                prop="dacu"
                label="充值玩家">
        </el-table-column>

        <el-table-column
                prop="arpu"
                label="ARPU">
        </el-table-column>
        <el-table-column
                prop="arppu"
                label="ARPPU">
        </el-table-column>
        <el-table-column
                prop="arnpu"
                label="ARNPU">
        </el-table-column>


        <el-table-column label="留存信息">
            <el-table-column prop="rs.0" label="次日留存"></el-table-column>
            <el-table-column prop="rs.1" label="2日留存"></el-table-column>
            <el-table-column prop="rs.2" label="3日留存"></el-table-column>
            <el-table-column prop="rs.3" label="4日留存"></el-table-column>
            <el-table-column prop="rs.4" label="5日留存"></el-table-column>
            <el-table-column prop="rs.5" label="6日留存"></el-table-column>
            <el-table-column prop="rs.6" label="7日留存"></el-table-column>
        </el-table-column>
    </el-table>
</div>
<div style="margin-top: 20px; margin-left: 0px;" v-if="logged">
    <h2>收入查询</h2>
    <el-form label-width="80px">
        <el-form-item label="选择日期">
            <el-date-picker
                    v-model="from"
                    type="daterange"
                    range-separator="至"
                    value-format="yyyy-MM-dd"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期">
            </el-date-picker>
        </el-form-item>

        <el-form-item label="环境">
            <el-switch v-model="product"></el-switch>
        </el-form-item>

        <el-form-item label="区服">
            <el-input v-model="zone">
            </el-input>
        </el-form-item>

        <el-form-item label="来源">
            <el-input v-model="source">
            </el-input>
        </el-form-item>

        <el-form-item label="总收入">
            <el-input placeholder="查询结果..." v-model="income" class="input-with-select">
            </el-input>
        </el-form-item>


        <el-form-item>
            <el-button type="primary" icon="el-icon-menu" @click="queryIncome">查询</el-button>
        </el-form-item>
    </el-form>

</div>

<div style="margin-top: 0px; margin-left: 0px;" v-if="logged">
    <h2>最高充值玩家</h2>
    <el-form label-width="80px">
        <el-form-item label="帐号ID">
            <el-input placeholder="可选参数" v-model="topaccid" class="input-with-select">
            </el-input>
        </el-form-item>


        <el-form-item label="数量">
            <el-input placeholder="..." v-model="topcount" class="input-with-select">
            </el-input>
        </el-form-item>

        <el-form-item>
            <el-button type="primary" icon="el-icon-menu" @click="getTopAccount">查询</el-button>
        </el-form-item>
    </el-form>

    <el-table
            :data="topacc"
            stripe
            border
            max-height="300"
            style="width: 100%">

        <el-table-column type="expand">
            <template slot-scope="props">
                <el-form label-position="left" inline class="demo-table-expand" v-for="info in props.row.infos">
                    <el-form-item label="角色">
                        <span>{{ info }}</span>
                    </el-form-item>
                </el-form>
            </template>
        </el-table-column>

        <el-table-column
                fixed
                prop="id"
                label="ID" width="90">
        </el-table-column>
        <el-table-column
                prop="account"
                label="账号">
        </el-table-column>
        <el-table-column
                prop="charge"
                label="充值">
        </el-table-column>


        <el-table-column label="创建日期">
            <template slot-scope="scope">
                <el-date-picker
                        v-model="scope.row.createtime"
                        type="datetime"
                        :disabled="true">
                </el-date-picker>
            </template>
        </el-table-column>
    </el-table>
</div>

<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">
    <h2>全服广播</h2>
    <el-input placeholder="请输入内容" v-model="notify">
        <template slot="prepend">广播:</template>
        <el-button slot="append" icon="el-icon-search" @click="sendBroadcast">发送</el-button>
    </el-input>
</div>

<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">
	<el-button size="mini" @click="makeOpenRmb">刷新所有第一次登录奖励(不会用就不要点，错误，概不负责)</el-button>

</div>

