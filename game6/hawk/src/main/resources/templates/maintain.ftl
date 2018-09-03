
	<div style="margin-top: 0px; margin-left: 0px;" v-if="logged">
		<h2>公会操作:</h2>
		<div style="margin-top: 10px; margin-left: 0px;" v-if="logged">
			<el-button size="mini" @click="flushGuild">刷新公会配置</el-button>

			<el-switch inline v-model="reloadable_guild" active-text="代码热更新" inactive-text="禁止热更新"
					   @change="checkGuildReloadable(1)"></el-switch>
		</div>

		<h2>公告</h2>
		<el-form label-width="80px">
                <el-form-item label="文件地址">
                    <el-input placeholder="..." v-model="bulletin_path" class="input-with-select" >
                    </el-input>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" icon="el-icon-menu" @click="getBulletin">查询</el-button>
                    <el-button type="primary" icon="el-icon-menu" @click="fixBulletin">修改</el-button>
                </el-form-item>

            </el-form>

        <el-input
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 20}"
          placeholder="公告内容"
          v-model="bulletin">
        </el-input>



		<h2>最近操作日志</h2>
		<el-form label-width="80px">
        			<el-form-item label="数量">
        				<el-input placeholder="..." v-model="topcount" class="input-with-select" >
        				</el-input>
        			</el-form-item>

        			<el-form-item>
        				<el-button type="primary" icon="el-icon-menu" @click="getAdminLogs">查询</el-button>
                    </el-form-item>
        		</el-form>

		<el-table
			  :data="toplogs"
			  stripe
			  border
			  max-height="800"
			  style="width: 100%">

			  <el-table-column
				prop="gm"
				label="管理员">
			  </el-table-column>


			  <el-table-column
				prop="data"
				label="日志">
			  </el-table-column>

			  <el-table-column label="充值日期">
                <template slot-scope="scope">
                  <el-date-picker
                        v-model="scope.row.time"
                        type="datetime"
                        :disabled="true">
                      </el-date-picker>
				</template>
			  </el-table-column>
			</el-table>
	</div>

