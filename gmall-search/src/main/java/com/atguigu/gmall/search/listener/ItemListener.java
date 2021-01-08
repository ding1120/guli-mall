package com.atguigu.gmall.search.listener;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemListener {

    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    private GoodsRepository goodsRepository;


    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = "SEARCH_ITEM_QUEUE",durable = "true"),
            exchange = @Exchange("PMS_ITEM_EXCHANGE"),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message){

        ResponseVo<SpuEntity> responseVo = this.pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = responseVo.getData();

        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if(!CollectionUtils.isEmpty(skuEntities)){
            // 3.把sku集合转化成goods集合
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();

                //(1).设置sku相关信息
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                //转化成double类型
                goods.setPrice(skuEntity.getPrice().doubleValue());

                //(2).设置spu相关信息

                goods.setCreateTime(spuEntity.getCreateTime());

                //(3).设置库存相关信息
                ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                }

                //(4).品牌
                ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                //(5).分类
                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                //(6).获取检索到的参数
                List<SearchAttrValueVo> searchAttrValueVos = new ArrayList<>();
                //(6.1)查询销售类型的检索参数
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = pmsClient.querySearchSkuAttrValuesByCidAndSkuId(skuEntity.getCatagoryId(), skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    searchAttrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                //(6.2)查询基本类型的检索参数
                ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = pmsClient.querySearchSpuAttrValuesByCidAndSpuId(skuEntity.getCatagoryId(), spuId);
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    searchAttrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }
                goods.setSearchAttrs(searchAttrValueVos);
                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }
    }
}
