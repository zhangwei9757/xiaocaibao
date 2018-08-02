package com.tumei.common;

import com.tumei.common.utils.RandomUtil;
import com.tumei.configs.IReadonly;
import com.tumei.game.GameUser;
import com.tumei.modelconf.*;
import com.tumei.modelconf.festival.*;
import com.tumei.modelconf.happy.SoulConf;
import com.tumei.modelconf.happy.SoulrankConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/1/19 0019.
 * <p>
 * 只读配置的预加载，可以在运行时异步加载
 */
@Service
public class Readonly extends IReadonly {
    private static Readonly _instance = null;

    public static Readonly getInstance() {
        return _instance;
    }

    private static Log log = LogFactory.getLog(Readonly.class);

    @Autowired
    @Qualifier("centerTemplate")
    private MongoTemplate centerTemplate;

    class Conf {
        // 品质英雄
        public HashMap<Integer, List<HeroConf>> qualityHeros = new HashMap<>();
        public HashMap<Integer, MaskcostConf> maskcosts = new HashMap<>();
        public List<MasksuitConf> masksuits;

        public HashMap<Integer, ChestConf> chests = new HashMap<>();
        public List<ScoreRewardConf> scorerewards;
        public HashMap<Integer, ItemConf> items = new HashMap<>();
        public List<GloryrewardConf> gloryrewardConfs;

        public List<RaidConf> raids;
        public List<FireraidConf> fireraids;
        public List<VipConf> vips;
        public List<HeroupConf> heroups;
        public List<HerobreakConf> herobks;
        public List<ChainConf> chains;
        public List<TeamExpConf> teamexps;
        public List<SummonConf> summons;
        public List<StarConf> stars;
        public List<EquipcostConf> equipcosts;
        public List<EquiprefcostConf> equiprefcosts;
        public List<EquaddConf> equaddConfs;
        public List<TreasurecostConf> treasurecosts;
        public List<TreasurerefcostConf> treasurerefcosts;
        public List<DailyConf> dailytasks;
        public List<AddtimeConf> addtimes;
        public TresackerConf tresackerConf;
        public List<NameConf> names;
        public List<ArenalistConf> arenalists;
        public List<ArenastoreConf> arenastores;
        public List<ArenarewardConf> arenarewards;
        public List<ItemstoreConf> itemstores;
        public List<HerostoreConf> herostores;
        public List<RaidstoreConf> raidstores;

        public List<DailyraidConf> dailyraids;


        public List<DailysupplyConf> dailysupplys;

        public HashMap<Integer, DonateConf> donates = new HashMap<>();
        public HashMap<Integer, DonaterewardConf> donaterewards = new HashMap<>();
        public HashMap<Integer, GuildraidConf> guildraids = new HashMap<>();
        public HashMap<Integer, FuwenraidConf> fuwens = new HashMap<>();

        public List<GroupstoreConf> groupstoreConfs;
        public List<Groupstore2Conf> groupstore2Confs;

        // 神秘宝藏
        public HashMap<Integer, DailytreasureConf> dailytreasureConfs = new HashMap<>();
        public List<DtrankConf> dtrankConfs;

        public List<MineRefreshConf> mineRefreshConfs;
        public List<MineStoneConf> mineStoneConfs;
        public List<MineChestConf> mineChestConfs;
        public List<MineMerchantConf> mineMerchantConfs;
        public List<MineMonsterConf> mineMonsterConfs;

        public HashSet<String> bundles = new HashSet<>();

        public List<SarenarewardConf> sarenarewardConfs;
        public List<BattlestoreConf> battlestoreConfs;

        public List<OpenraceConf> openraceConfs;

        public HashMap<Integer, ArtifactConf> artifactConfs = new HashMap<>();
        public HashMap<Integer, ArtpartConf> artpartConfs = new HashMap<>();
        public List<ArtpartstrConf> artpartstrConfs;
        public List<ArtpartstupConf> artpartstupConfs;
        public List<ArtadvancedConf> artadvancedConfs;
        public List<ArtstoreConf> artstoreConfs;
        public List<ArtsummonConf> artsummonConfs;

        // 只要第一条数据即可
        public List<BossConf> bossConfs;
        public List<BossrankConf> bossrankConfs;

        public List<HolyexpConf> holyexps = new ArrayList<>();
        public List<LeheupConf> leheups = new ArrayList<>();
        public List<LhwcostConf> lhwcosts = new ArrayList<>();
        public List<GlorychestConf> glorychests = new ArrayList<>();
        public List<MissionConf> missions = new ArrayList<>();

        public List<RdshopConf> rdshopConfs = new ArrayList<>();
        public List<GuildbagConf> guildbagConfs = new ArrayList<>();


        public void initialize() {
            bc.heros.values().forEach(hc -> {
                if (hc != null && (hc.key / 10000 != 9)) {
                    List<HeroConf> li = qualityHeros.getOrDefault(hc.quality, null);
                    if (li == null) {
                        li = new ArrayList<>();
                        qualityHeros.put(hc.quality, li);
                    }

                    li.add(hc);
                }
            });

            {
                List<ChestConf> bean = mongoTemplate.findAll(ChestConf.class);
                for (ChestConf ib : bean) {
                    chests.put(ib.key, ib);
                }
            }

            scorerewards = mongoTemplate.findAll(ScoreRewardConf.class);
            scorerewards.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            gloryrewardConfs = mongoTemplate.findAll(GloryrewardConf.class);
            gloryrewardConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            {
                List<ItemConf> bean = mongoTemplate.findAll(ItemConf.class);
                for (ItemConf ib : bean) {
                    items.put(ib.key, ib);
                }
            }

            {
                List<MaskcostConf> bean = mongoTemplate.findAll(MaskcostConf.class);
                for (MaskcostConf ib : bean) {
                    maskcosts.put(ib.key, ib);
                }
            }

            masksuits = mongoTemplate.findAll(MasksuitConf.class);
            masksuits.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            raids = mongoTemplate.findAll(RaidConf.class);
            raids.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            fireraids = mongoTemplate.findAll(FireraidConf.class);
            fireraids.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            vips = mongoTemplate.findAll(VipConf.class);
            vips.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            heroups = mongoTemplate.findAll(HeroupConf.class);
            heroups.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            herobks = mongoTemplate.findAll(HerobreakConf.class);
            herobks.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            chains = mongoTemplate.findAll(ChainConf.class);
            chains.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            teamexps = mongoTemplate.findAll(TeamExpConf.class);
            teamexps.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            summons = mongoTemplate.findAll(SummonConf.class);
            summons.sort((o1, o2) -> {
                if (o1.quality < o2.quality) {
                    return -1;
                } else if (o1.quality > o2.quality) {
                    return 1;
                }
                return 0;
            });

            stars = mongoTemplate.findAll(StarConf.class);
            stars.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            equipcosts = mongoTemplate.findAll(EquipcostConf.class);
            equipcosts.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            equiprefcosts = mongoTemplate.findAll(EquiprefcostConf.class);
            equiprefcosts.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            equaddConfs = mongoTemplate.findAll(EquaddConf.class);
            equaddConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            treasurecosts = mongoTemplate.findAll(TreasurecostConf.class);
            treasurecosts.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            treasurerefcosts = mongoTemplate.findAll(TreasurerefcostConf.class);
            treasurerefcosts.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            dailytasks = mongoTemplate.findAll(DailyConf.class);
            dailytasks.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            tresackerConf = mongoTemplate.findOne(new Query(Criteria.where("key").is(1L)), TresackerConf.class);

            addtimes = mongoTemplate.findAll(AddtimeConf.class);
            addtimes.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            names = mongoTemplate.findAll(NameConf.class);
            names.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            arenalists = mongoTemplate.findAll(ArenalistConf.class);
            arenalists.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            arenastores = mongoTemplate.findAll(ArenastoreConf.class);
            arenastores.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            arenarewards = mongoTemplate.findAll(ArenarewardConf.class);
            arenarewards.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            itemstores = mongoTemplate.findAll(ItemstoreConf.class);
            itemstores.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            herostores = mongoTemplate.findAll(HerostoreConf.class);
            herostores.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            raidstores = mongoTemplate.findAll(RaidstoreConf.class);
            raidstores.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            dailyraids = mongoTemplate.findAll(DailyraidConf.class);
            dailyraids.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            dailysupplys = mongoTemplate.findAll(DailysupplyConf.class);
            dailysupplys.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            {
                List<DonateConf> bean = mongoTemplate.findAll(DonateConf.class);
                for (DonateConf ib : bean) {
                    donates.put(ib.key, ib);
                }
            }
            {
                List<DonaterewardConf> bean = mongoTemplate.findAll(DonaterewardConf.class);
                for (DonaterewardConf ib : bean) {
                    donaterewards.put(ib.key, ib);
                }
            }
            {
                List<GuildraidConf> bean = mongoTemplate.findAll(GuildraidConf.class);
                for (GuildraidConf ib : bean) {
                    guildraids.put(ib.key, ib);
                }
            }
            {
                List<FuwenraidConf> bean = mongoTemplate.findAll(FuwenraidConf.class);
                for (FuwenraidConf ib : bean) {
                    fuwens.put(ib.key, ib);
                }
            }

            groupstoreConfs = mongoTemplate.findAll(GroupstoreConf.class);
            groupstoreConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            groupstore2Confs = mongoTemplate.findAll(Groupstore2Conf.class);
            groupstore2Confs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            {
                List<DailytreasureConf> bean = mongoTemplate.findAll(DailytreasureConf.class);
                for (DailytreasureConf ib : bean) {
                    dailytreasureConfs.put(ib.key, ib);
                }
            }

            dtrankConfs = mongoTemplate.findAll(DtrankConf.class);
            dtrankConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            mineRefreshConfs = mongoTemplate.findAll(MineRefreshConf.class);
            mineRefreshConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            mineStoneConfs = mongoTemplate.findAll(MineStoneConf.class);
            mineStoneConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            mineChestConfs = mongoTemplate.findAll(MineChestConf.class);
            mineChestConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            mineMerchantConfs = mongoTemplate.findAll(MineMerchantConf.class);
            mineMerchantConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            mineMonsterConfs = mongoTemplate.findAll(MineMonsterConf.class);
            mineMonsterConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });


            sarenarewardConfs = mongoTemplate.findAll(SarenarewardConf.class);
            sarenarewardConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            battlestoreConfs = mongoTemplate.findAll(BattlestoreConf.class);
            battlestoreConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            openraceConfs = mongoTemplate.findAll(OpenraceConf.class);
            openraceConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            {
                List<ArtifactConf> bean = mongoTemplate.findAll(ArtifactConf.class);
                for (ArtifactConf ib : bean) {
                    artifactConfs.put(ib.key, ib);
                }
            }
            {
                List<ArtpartConf> bean = mongoTemplate.findAll(ArtpartConf.class);
                for (ArtpartConf ib : bean) {
                    artpartConfs.put(ib.key, ib);
                }
            }
            artpartstrConfs = mongoTemplate.findAll(ArtpartstrConf.class);
            artpartstrConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            artpartstupConfs = mongoTemplate.findAll(ArtpartstupConf.class);
            artpartstupConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            artadvancedConfs = mongoTemplate.findAll(ArtadvancedConf.class);
            artadvancedConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            artstoreConfs = mongoTemplate.findAll(ArtstoreConf.class);
            artstoreConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            artsummonConfs = mongoTemplate.findAll(ArtsummonConf.class);
            artsummonConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            bossrankConfs = mongoTemplate.findAll(BossrankConf.class);
            bossrankConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            holyexps = mongoTemplate.findAll(HolyexpConf.class);
            holyexps.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            leheups = mongoTemplate.findAll(LeheupConf.class);
            leheups.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            lhwcosts = mongoTemplate.findAll(LhwcostConf.class);
            lhwcosts.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            glorychests = mongoTemplate.findAll(GlorychestConf.class);
            glorychests.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            missions = mongoTemplate.findAll(MissionConf.class);
            missions.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            rdshopConfs = mongoTemplate.findAll(RdshopConf.class);
            rdshopConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            guildbagConfs = mongoTemplate.findAll(GuildbagConf.class);
            guildbagConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

        }
    }

    private Conf conf;

    class ActConf {
        public List<SignConf> signConfs;
        public List<SevenConf> sevenConfs;
        public List<SinglerechargeConf> singleConfs;
        public List<CumrechargeConf> cumConfs;
        public List<SaleConf> saleConfs;
        public List<ExchangeConf> exchangeConfs;
        public List<FundConf> fundConfs;
        public List<CampaignactConf> campaignactConfs;
        public int campaignType = 0;
        public HashMap<Integer, GoldtreeConf> goldtrees = new HashMap<>();
        public List<ForheroConf> forheroConfs;
        public List<FirstsevenConf> firstsevenConfs;

        public List<DbscoreConf> dbscoreConfs;
        public List<DbriderConf> dbriderConfs;
        public List<DbspendConf> dbspendConfs;
        public List<DbstoreConf> dbstoreConfs;
        public List<DbsingleConf> dbsingleConfs;


        public void initialize() {
            signConfs = mongoTemplate.findAll(SignConf.class);
            signConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            sevenConfs = mongoTemplate.findAll(SevenConf.class);
            sevenConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            singleConfs = mongoTemplate.findAll(SinglerechargeConf.class);
            singleConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            cumConfs = mongoTemplate.findAll(CumrechargeConf.class);
            cumConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            saleConfs = mongoTemplate.findAll(SaleConf.class);
            saleConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            exchangeConfs = mongoTemplate.findAll(ExchangeConf.class);
            exchangeConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            fundConfs = mongoTemplate.findAll(FundConf.class);
            fundConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            campaignactConfs = mongoTemplate.findAll(CampaignactConf.class);
            campaignactConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            campaignType = campaignactConfs.stream().max((c1, c2) -> {
                if (c1.type > c2.type) {
                    return 1;
                } else if (c1.type < c2.type) {
                    return -1;
                }
                return 0;
            }).get().type;

            {
                List<GoldtreeConf> bean = mongoTemplate.findAll(GoldtreeConf.class);
                for (GoldtreeConf ib : bean) {
                    goldtrees.put(ib.key, ib);
                }
            }

            forheroConfs = mongoTemplate.findAll(ForheroConf.class);
            forheroConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            firstsevenConfs = mongoTemplate.findAll(FirstsevenConf.class);
            firstsevenConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            dbscoreConfs = mongoTemplate.findAll(DbscoreConf.class);
            dbscoreConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            dbspendConfs = mongoTemplate.findAll(DbspendConf.class);
            dbspendConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            dbstoreConfs = mongoTemplate.findAll(DbstoreConf.class);
            dbstoreConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            dbriderConfs = mongoTemplate.findAll(DbriderConf.class);
            dbriderConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
            dbsingleConfs = mongoTemplate.findAll(DbsingleConf.class);
            dbsingleConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
        }
    }

    private ActConf aconf;

    class FestConf {
        public List<FestivalConf> festivalConfs;
        public List<FestivalSingleConf> festivalSingleConfs;
        public List<FestivalLogonConf> festivalLogonConfs;
        public List<FestivalSpendConf> festivalSpendConfs;
        public List<FestivalSaleConf> festivalSaleConfs;
        public List<FestivalCumConf> festivalCumConfs;

        public List<SoulConf> soulConfs;
        public List<SoulrankConf> soulrankConfs;

        public void initialize() {
            festivalConfs = mongoTemplate.findAll(FestivalConf.class);
            festivalConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            festivalSingleConfs = mongoTemplate.findAll(FestivalSingleConf.class);
            festivalSingleConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            festivalCumConfs = mongoTemplate.findAll(FestivalCumConf.class);
            festivalCumConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });


            festivalLogonConfs = mongoTemplate.findAll(FestivalLogonConf.class);
            festivalLogonConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            festivalSaleConfs = mongoTemplate.findAll(FestivalSaleConf.class);
            festivalSaleConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            festivalSpendConfs = mongoTemplate.findAll(FestivalSpendConf.class);
            festivalSpendConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            soulConfs = mongoTemplate.findAll(SoulConf.class);
            soulConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });

            soulrankConfs = mongoTemplate.findAll(SoulrankConf.class);
            soulrankConfs.sort((o1, o2) -> {
                if (o1.key < o2.key) {
                    return -1;
                } else if (o1.key > o2.key) {
                    return 1;
                }
                return 0;
            });
        }
    }

    private FestConf festConf;

    @PostConstruct
    public void init() {
        refresh();
        refreshActivity();
        refreshFest();
        _instance = this;
    }

    /**
     * 带锁刷新只读表到内存中
     */
    @Override
    public void refresh() {
        super.refresh();
        log.warn("++++++++++++ 刷新只读配置 ++++++++++++");
        Conf _conf = new Conf();
        _conf.initialize();
        this.conf = _conf;
        log.warn("++++++++++++ 刷新只读配置 结束 ++++++++++++");
    }

    public void refreshFest() {
        log.warn("++++++++++++ 刷新节日活动配置 ++++++++++++");
        FestConf _conf = new FestConf();
        _conf.initialize();
        this.festConf = _conf;
        log.warn("++++++++++++ 刷新节日活动配置 结束 ++++++++++++");
    }

    public void refreshActivity() {
        log.warn("++++++++++++ 刷新活动所需配置 ++++++++++++");

        ActConf _aconf = new ActConf();
        _aconf.initialize();
        this.aconf = _aconf;
        log.warn("++++++++++++ 刷新活动所需配置结束 ++++++++++++");
    }


    /**
     * 战队等级对应的 摇钱树配置
     *
     * @param level
     * @return
     */
    public GoldtreeConf findGoldtree(int level) {
        return aconf.goldtrees.get(level);
    }

    /**
     * 在英雄列表中随机count个英雄出来
     *
     * @param count 随机个数
     * @return
     */
    public List<Integer> randHeros(int count) {
        List<Integer> objs = bc.heros.keySet().stream().filter(key -> (key < 90000)).collect(Collectors.toList());
        Collections.shuffle(objs);
        return objs.subList(0, count);
    }

    /**
     * 随机获取一个英雄
     *
     * @return
     */
    public synchronized int randHero() {
        List<Integer> objs = bc.heros.keySet().stream().filter(key -> (key < 90000)).collect(Collectors.toList());
        return objs.get(RandomUtil.getRandom() % objs.size());
    }

    public synchronized int randHero(int quality) {
        List<Integer> objs = new ArrayList<>();
        bc.heros.forEach((k, v) -> {
            if (k < 90000) {
                if (quality == 2) {
                    if (v.quality <= 2) {
                        objs.add(k);
                    }
                } else if (v.quality == quality) {
                    objs.add(k);
                }
            }
        });

        return objs.get(RandomUtil.getRandom() % objs.size());
    }


    public List<Integer> randHerosByList(int count, int[] grades) {
        List<Integer> objs = bc.heros.keySet().stream().filter(key -> {
            if (key >= 90000) {
                return false;
            }

            HeroConf hc = bc.heros.get(key);
            for (int g : grades) {
                if (g == hc.quality) {
                    return true;
                }
            }

            return false;
        }).collect(Collectors.toList());
        Collections.shuffle(objs);
        return objs.subList(0, count);
    }


    public List<HeroConf> findHeroByQuality(int quality) {
        return conf.qualityHeros.getOrDefault(quality, null);
    }


    /**
     * 查询副本关卡的信息
     *
     * @param raidKey 关卡id, -1后就是真实索引
     * @return
     */
    public RaidConf findRaid(int raidKey) {
        return conf.raids.get(raidKey - 1);
    }

    public FireraidConf findFireraid(int raidKey) {
        return conf.fireraids.get(raidKey - 1);
    }

    /**
     * 获取宝箱的信息
     *
     * @param chestid 宝箱的id
     * @return
     */
    public ChestConf findChest(int chestid) {
        return conf.chests.get(chestid);
    }

    /**
     * vip信息
     *
     * @param vip
     * @return
     */
    public VipConf findVip(int vip) {
        if (vip >= conf.vips.size()) {
            return null;
        }
        return conf.vips.get(vip);
    }

    /**
     * 每日限时领取奖励
     *
     * @param key 限时奖励的key
     * @return
     */
    public DailysupplyConf findDailysuppy(int key) {
        --key;
        if (key >= conf.dailysupplys.size()) {
            return null;
        }
        return conf.dailysupplys.get(key);
    }

    /**
     * 神秘商店
     *
     * @return
     */
    public List<RdshopConf> getRdshop() {
        return conf.rdshopConfs;
    }

    /**
     * 公会红包
     *
     * @param
     * @return
     */
    public List<GuildbagConf> getGuildbagConfs() {
        return conf.guildbagConfs;
    }

    /**
     * 英雄升级信息
     *
     * @param level 当前等级
     * @return
     */
    public HeroupConf findHeroup(int level) {
        --level;
        if (level >= conf.heroups.size()) {
            return null;
        }
        return conf.heroups.get(level);
    }

    public HerobreakConf findHerobreak(int level) {
        if (level >= conf.herobks.size()) {
            return null;
        }
        return conf.herobks.get(level);
    }

    public ChainConf findChain(int chain) {
        --chain;
        if (chain >= conf.chains.size()) {
            return null;
        }
        return conf.chains.get(chain);
    }

    /**
     * 战队经验升级
     *
     * @param level [1, ...)
     * @return
     */
    public TeamExpConf findTeamExp(int level) {
        --level;
        if (level >= conf.teamexps.size()) {
            return null;
        }

        return conf.teamexps.get(level);
    }

    /**
     * 时装的强化成本
     *
     * @param level
     * @return
     */
    public MaskcostConf findMaskcost(int level) {
        return conf.maskcosts.get(level);
    }

    /**
     * 时装的套装加成
     *
     * @return
     */
    public List<MasksuitConf> findMasksuits() {
        return conf.masksuits;
    }


    /**
     * 召唤
     *
     * @param quality [0, ...]
     * @return
     */
    public SummonConf findSummon(int quality) {
        if (quality >= conf.summons.size()) {
            return null;
        }

        return conf.summons.get(quality);
    }

    /**
     * 战斗力查询对应的奖励
     *
     * @return
     */
    public List<ScoreRewardConf> getScoreReward() {
        return conf.scorerewards;
    }


    /**
     * 占星台数据
     *
     * @return
     */
    public List<StarConf> findStars() {
        return conf.stars;
    }


    /**
     * 装备强化费用
     *
     * @param level
     * @return
     */
    public EquipcostConf findEquipCost(int level) {
        --level;
        if (level >= conf.equipcosts.size()) {
            return null;
        }

        return conf.equipcosts.get(level);
    }

    /**
     * 装备精炼费用
     *
     * @param level
     * @return
     */
    public EquiprefcostConf findEquipRefCost(int level) {
        if (level >= conf.equiprefcosts.size()) {
            return null;
        }

        return conf.equiprefcosts.get(level);
    }

    /**
     * 装备分解表
     *
     * @param level 觉醒等级,1开始,0不要查询
     * @return
     */
    public EquaddConf findEquipaddConf(int level) {
        if (level < 0 || level >= conf.equaddConfs.size()) {
            return null;
        }

        return conf.equaddConfs.get(level);
    }


    /**
     * 宝物强化费用
     *
     * @param level
     * @return
     */
    public TreasurecostConf findTreasureCost(int level) {
        --level;
        if (level >= conf.treasurecosts.size()) {
            return null;
        }

        return conf.treasurecosts.get(level);
    }


    public TreasurerefcostConf findTreasureRefCost(int level) {
        if (level >= conf.treasurerefcosts.size()) {
            return null;
        }

        return conf.treasurerefcosts.get(level);
    }


    /**
     * 查询物品
     *
     * @param id
     * @return
     */
    public ItemConf findItem(int id) {
        return conf.items.get(id);
    }

    /**
     * 查询日常任务
     *
     * @param index [1,...]
     * @return
     */
    public DailyConf findDailyTask(int index) {
        --index;
        if (index >= conf.dailytasks.size()) {
            return null;
        }

        return conf.dailytasks.get(index);
    }

    public List<DailyConf> getDailytasks() {
        return conf.dailytasks;
    }

    /**
     * id [0, ...]
     *
     * @param id
     * @return
     */
    public AddtimeConf findAddtime(int id) {
        if (id >= conf.addtimes.size()) {
            return null;
        }

        return conf.addtimes.get(id);
    }

    public TresackerConf findTresacker() {
        return conf.tresackerConf;
    }

    /**
     * 随机名字
     *
     * @return
     */
    public String randomName() {
        int a = RandomUtil.getRandom() % conf.names.size();
        int b = RandomUtil.getRandom() % conf.names.size();
        int c = RandomUtil.getRandom() % conf.names.size();

        return conf.names.get(a).part1 + conf.names.get(b).part2 + conf.names.get(c).part3;
    }

    /**
     * 根据排名获取竞技场的间隔
     *
     * @return
     */
    public int getArenaInterval(int rank) {
        ++rank;
        for (ArenalistConf ac : conf.arenalists) {
            if (rank >= ac.rank[0] && rank <= ac.rank[1]) {
                return ac.interval;
            }
        }

        return 40;
    }

    /**
     * 根据排名获取当前应该得到的竞技场奖励
     *
     * @param rank
     * @return
     */
    public ArenarewardConf getArenaRewards(int rank) {
        ++rank;
        for (ArenarewardConf ac : conf.arenarewards) {
            if (rank <= ac.rank) {
                return ac;
            }
        }
        return null;
    }

    /**
     * 跨服竞技场奖励
     *
     * @param rank
     * @return
     */
    public SarenarewardConf getSarenaRewards(int rank) {
        ++rank;
        for (SarenarewardConf ac : conf.sarenarewardConfs) {
            if (rank <= ac.rank) {
                return ac;
            }
        }
        return null;
    }

    /**
     * 竞技场商店所有奖励
     *
     * @return
     */
    public List<ArenastoreConf> findArenaStores() {
        return conf.arenastores;
    }


    /**
     * 跨服竞技场商店
     *
     * @return
     */
    public List<BattlestoreConf> findBattleStores() {
        return conf.battlestoreConfs;
    }

    public List<OpenraceConf> findOpenraces() {
        return conf.openraceConfs;
    }

    /**
     * 道具商店所有
     *
     * @return
     */
    public List<ItemstoreConf> findItemStores() {
        return conf.itemstores;
    }

    /**
     * 远征商店
     *
     * @return
     */
    public List<RaidstoreConf> findRaidStores() {
        return conf.raidstores;
    }

    public List<HerostoreConf> findHeroStores() {
        return conf.herostores;
    }

    public DailyraidConf findDailyRaids(int id) {
        --id;
        return conf.dailyraids.get(id);
    }

    public DonateConf findDonate(int level) {
        return conf.donates.get(level);
    }

    public DonaterewardConf findDonatereward(int level) {
        return conf.donaterewards.get(level);
    }

    /**
     * 公会副本
     *
     * @param level
     * @return
     */
    public GuildraidConf findGuildraid(int level) {
        return conf.guildraids.get(level);
    }

    public FuwenraidConf findFuwen(int level) {
        return conf.fuwens.get(level);
    }

    public List<GroupstoreConf> getGroupstoreConfs() {
        return conf.groupstoreConfs;
    }

    public List<Groupstore2Conf> getGroupstore2Confs() {
        return conf.groupstore2Confs;
    }

    public List<SignConf> getSignConfs() {
        return aconf.signConfs;
    }

    public List<SevenConf> getSevenConfs() {
        return aconf.sevenConfs;
    }

    /**
     * 单笔充值奖励
     *
     * @return
     */
    public List<SinglerechargeConf> getSingleConfs() {
        return aconf.singleConfs;
    }

    public List<CumrechargeConf> getCumConfs() {
        return aconf.cumConfs;
    }

    // 半价限购
    public List<SaleConf> getSaleConfs() {
        return aconf.saleConfs;
    }

    // 折扣兑换
    public List<ExchangeConf> getExchangeConfs() {
        return aconf.exchangeConfs;
    }

    /**
     * 首充奖励，灵活领取
     *
     * @return
     */
    public FirstrechargeConf getFirstrechargeConf() {
        return mongoTemplate.findOne(new Query(Criteria.where("key").is(1L)), FirstrechargeConf.class);
    }

    public List<CampaignactConf> getCampaignactConfs() {
        return aconf.campaignactConfs;
    }

    // 征战的种类
    public int getCampaignactTypes() {
        return aconf.campaignType;
    }

    /**
     * 开服基金
     *
     * @param idx
     * @return
     */
    public FundConf getFundConf(int idx) {
        return aconf.fundConfs.get(idx);
    }

    /**
     * 请求兑换码
     *
     * @param user 玩家
     * @param code 邀请码 8位
     * @return
     */
    public int[] pressCode(GameUser user, String code) {
        long uid = user.getUid();
        // 全大写存储在数据库
        code = code.toUpperCase();

        // 检测code是否全局唯一码，并且该玩家没有领取过
        if (mongoTemplate.exists(new Query(Criteria.where("id").is(code).and("uid").is(uid)), GlobalCRBean.class)) {
            return null;
        }

        GlobalCodeBean gcb = mongoTemplate.findOne(new Query(Criteria.where("id").is(code).and("flag").is(1)), GlobalCodeBean.class);
        if (gcb != null) {
            if (gcb.getLevel() != 0) {
                if (user.getLevel() < gcb.getLevel()) {
                    return null;
                }
            }

            if (gcb.getVip() != 0) {
                if (user.getVip() < gcb.getVip()) {
                    return null;
                }
            }

            GlobalCRBean gcrb = new GlobalCRBean();
            gcrb.setId(code);
            gcrb.setUid(uid);
            gcrb.setTime(new Date());
            mongoTemplate.insert(gcrb);

            int[] rtn = gcb.getAwards();
            return rtn;
        }


        // 以上不满足的情况下，检测是否单独发送的码, 仍旧使用GlobalCRBean记录

        CodeBean cb = mongoTemplate.findOne(new Query(Criteria.where("id").is(code)), CodeBean.class);
        if (cb != null) {
            if (cb.getLevel() != 0) {
                if (user.getLevel() < cb.getLevel()) {
                    return null;
                }
            }

            if (cb.getVip() != 0) {
                if (user.getVip() < cb.getVip()) {
                    return null;
                }
            }


            // 单独码需要满足，该玩家没有领取过相同类型的码,判断mode
            if (mongoTemplate.exists(new Query(Criteria.where("id").is(cb.getMode()).and("uid").is(uid)), GlobalCRBean.class)) {
                return null;
            }

            GlobalCRBean gcrb = new GlobalCRBean();
            gcrb.setId(cb.getMode());
            gcrb.setUid(uid);
            gcrb.setTime(new Date());
            mongoTemplate.insert(gcrb);

            int[] rtn = cb.getAwards();
            mongoTemplate.remove(cb);
            return rtn;
        }

        return null;
    }

    // 英雄福利
    public List<ForheroConf> getForheroConfs() {
        return aconf.forheroConfs;
    }

    /**
     * 开服七日的狂欢配置
     *
     * @param day 第一天是0
     * @return
     */
    public FirstsevenConf findFirstSevenConf(int day) {
        if (day < 0 || day > 6) {
            return null;
        }
        return aconf.firstsevenConfs.get(day);
    }

    /**
     * 夺宝
     *
     * @param level [1,4] 层
     * @return
     */
    public DbriderConf findDbRiderConf(int level) {
        return aconf.dbriderConfs.get(level - 1);
    }

    public List<DbsingleConf> getDbSingleConfs() {
        return aconf.dbsingleConfs;
    }

    public List<DbspendConf> getDbSpendConfs() {
        return aconf.dbspendConfs;
    }

    public List<DbstoreConf> getDbStoreConfs() {
        return aconf.dbstoreConfs;
    }

    public List<DbscoreConf> getDbScoreConfs() {
        return aconf.dbscoreConfs;
    }

    /**
     * 根据指定金银铜id,获取对应的 宝藏的信息
     *
     * @param id
     * @return
     */
    public DailytreasureConf findDailytreasureConf(int id) {
        return conf.dailytreasureConfs.getOrDefault(id, null);
    }

    public List<DailytreasureConf> getDailytreasureConf() {
        return new ArrayList<>(conf.dailytreasureConfs.values());
    }


    /**
     * 获取神秘宝藏排名奖励信息
     *
     * @return
     */
    public List<DtrankConf> getDtrankConfs() {
        return conf.dtrankConfs;
    }


    public List<MineRefreshConf> getMineRefreshConfs() {
        return conf.mineRefreshConfs;
    }

    public List<MineStoneConf> getMineStoneConfs() {
        return conf.mineStoneConfs;
    }

    public List<MineChestConf> getMineChestConfs() {
        return conf.mineChestConfs;
    }

    public List<MineMerchantConf> getMineMerchantConfs() {
        return conf.mineMerchantConfs;
    }

    public List<MineMonsterConf> getMineMonsterConfs() {
        return conf.mineMonsterConfs;
    }

    public List<SarenarewardConf> getSarenarewardConfs() {
        return conf.sarenarewardConfs;
    }

    /**
     * @param key [0, ...]
     * @return
     */
    public GloryrewardConf findGloryrewardConf(int key) {
        if (key < 0 || key >= conf.gloryrewardConfs.size()) {
            return null;
        }
        return conf.gloryrewardConfs.get(key);
    }

    /**
     * 是否包含指定的包
     *
     * @param bundle
     * @return
     */
    public boolean containsBundle(String bundle) {
        return conf.bundles.contains(bundle.trim());
    }

    public List<FestivalLogonConf> getFestivalLogonConfs() {
        return festConf.festivalLogonConfs;
    }

    public List<FestivalSpendConf> getFestivalSpendConfs() {
        return festConf.festivalSpendConfs;
    }

    public List<FestivalSaleConf> getFestivalSaleConfs() {
        return festConf.festivalSaleConfs;
    }

    public List<FestivalSingleConf> getFestivalSingleConfs() {
        return festConf.festivalSingleConfs;
    }

    public List<FestivalCumConf> getFestivalCumConfs() {
        return festConf.festivalCumConfs;
    }

    public List<FestivalConf> getFestivalConfs() {
        return festConf.festivalConfs;
    }

    public FestivalConf findFestivalConf(int mode) {
        if (mode == -1) {
            return null;
        }
        return festConf.festivalConfs.stream().filter(fc -> fc.mode == mode).findFirst().get();
    }

    public List<SoulConf> getSoulConfs() {
        return festConf.soulConfs;
    }

    public SoulConf findSoulConf(int key) {
        --key;
        return festConf.soulConfs.get(key);
    }

    public List<SoulrankConf> getSoulrankConfs() {
        return festConf.soulrankConfs;
    }

    /**
     * 获取所有神器商店的配置
     *
     * @return
     */
    public List<ArtstoreConf> getArtstoreConfs() {
        return conf.artstoreConfs;
    }

    /**
     * 获取所有神器召唤的配置
     *
     * @return
     */
    public List<ArtsummonConf> getArtsummonConfs() {
        return conf.artsummonConfs;
    }

    public List<ArtadvancedConf> getArtadvancedConfs() {
        return conf.artadvancedConfs;
    }

    public List<ArtpartstrConf> getArtpartstrConfs() {
        return conf.artpartstrConfs;
    }

    public List<ArtpartstupConf> getArtpartstupConfs() {
        return conf.artpartstupConfs;
    }

    public ArtifactConf findArtifact(int artid) {
        return conf.artifactConfs.getOrDefault(artid, null);
    }

    public ArtpartConf findArtpart(int comid) {
        return conf.artpartConfs.getOrDefault(comid, null);
    }

    public BossConf getBossConf(int level) {
        return conf.bossConfs.get(level - 1);
    }

    public List<BossrankConf> getBossrankConfs() {
        return conf.bossrankConfs;
    }

    /**
     * 根据等级,查询升级下一级所需的经验,如果是-1标识无限大
     *
     * @param level
     * @return
     */
    public int findHolyexp(int level) {
        --level;
        if (level < 0 || level >= conf.holyexps.size()) {
            return -2;
        }

        return conf.holyexps.get(level).cost1;
    }

    /**
     * 根据等级查询升级下一级所需的经验 如果是-1标识不能升级
     *
     * @param level
     * @return
     */
    public LeheupConf findLeheup(int level) {
        --level;
        if (level < 0 || level >= conf.leheups.size()) {
            return null;
        }

        return conf.leheups.get(level);
    }

    public LhwcostConf findLhwcost(int gift) {
        if (gift < 0 || gift >= conf.lhwcosts.size()) {
            return null;
        }

        return conf.lhwcosts.get(gift);
    }

    public List<GlorychestConf> getGlorychests() {
        return conf.glorychests;
    }


    public MissionConf findMission(int key) {
        --key;
        if (key < 0 || key >= conf.missions.size()) {
            return null;
        }

        return conf.missions.get(key);
    }

    public List<MissionConf> getMissions() {
        return conf.missions;
    }
}




