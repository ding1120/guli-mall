package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    GmallSmsClient gmallSmsClient;
    @Autowired
    GmallWmsClient gmallWmsClient;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        //1.查询sku信息
        CompletableFuture<SkuEntity> skuFutrue = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.gmallPmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new ItemException("该skuId对应的商品不存在");
            }

            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubtitle(skuEntity.getSubtitle());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);
        //2.查询分类信息
        CompletableFuture<Void> catesFuture = skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> categoryResponseVo = this.gmallPmsClient.queryLvl123CategoriesByCid3(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        //3.查询品牌信息

        CompletableFuture<Void> brandFuture=skuFutrue.thenAcceptAsync(skuEntity -> {
        ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(skuEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResponseVo.getData();
        if(brandEntity!=null){

            itemVo.setBrandId(brandEntity.getId());
            itemVo.setBrandName(brandEntity.getName());
        }
        },threadPoolExecutor);

        //4.查询spu信息
        CompletableFuture<Void> spuFuture=skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.gmallPmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity!=null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        },threadPoolExecutor);

        //5.查询sku的图片列表
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = this.gmallPmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imagesResponseVo.getData();
            itemVo.setSkuImages(skuImagesEntities);
        }, threadPoolExecutor);


        //6.查询营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.gmallSmsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);


        //7.库存信息
        CompletableFuture<Void> waresFuture=CompletableFuture.runAsync(()->{
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

        }, threadPoolExecutor);

        //8.查询spu的所有销售属性
        CompletableFuture<Void> spuSalesFuture = skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.gmallPmsClient.querySaleAttrValueBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> attrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(attrValueVos);
        }, threadPoolExecutor);


        //9.查询sku的销售属性
        CompletableFuture<Void> skuSalesFuture= CompletableFuture.runAsync(()->{
            ResponseVo<List<SkuAttrValueEntity>> skuAttrsResponseVo = this.gmallPmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrsResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId,SkuAttrValueEntity::getAttrValue)));
            }
        }, threadPoolExecutor);


        //10.销售属性组合和skuId的映射关系
        CompletableFuture<Void> salesGroupFuture = skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> skuMappingResponseVo = this.gmallPmsClient.querySaleAttrValuesMappingSkuIdBySpuId(skuEntity.getSpuId());
            String json = skuMappingResponseVo.getData();
            itemVo.setSkuJson(json);
        }, threadPoolExecutor);

        //11.查询商品详情
        CompletableFuture<Void> descFuture=skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.gmallPmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if(spuDescEntity!=null) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(),",")));
            }
        },threadPoolExecutor);

        //12.查询规格参数组及组下规格参数信息
        CompletableFuture<Void> groupFuture = skuFutrue.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = this.gmallPmsClient.queryGroupsWithAttrsAndValuesByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuId, skuEntity.getSpuId());
            List<ItemGroupVo> groupVos = groupResponseVo.getData();
            itemVo.setGroups(groupVos);

        }, threadPoolExecutor);

        //加上join进行阻塞
        CompletableFuture.allOf(catesFuture,brandFuture,imagesFuture,spuFuture,salesFuture,waresFuture,spuSalesFuture,skuSalesFuture,salesGroupFuture,descFuture,groupFuture).join();
        return itemVo;

    }
    private  void createHtml(Long skuId){
        ItemVo itemVo=this.loadData(skuId);
        //上下文初始化对象
        Context context = new Context();
        //页面静态化所需的数据模型
        context.setVariable("itemVo",itemVo);

        //jdk1.8新语法
        try ( PrintWriter printWriter = new PrintWriter(new File("F:\\guli-front\\html\\" + skuId + ".html"))){
            //通过thymeleaf提供的模板引擎进行模板静态化
            //1-模板的视图名称 2-thymeleaf的上下文对象 3-文件流
            templateEngine.process("item",context, printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    public void asyncExecute(Long skuId){
        threadPoolExecutor.execute(()->createHtml(skuId));
    }
}


class CompletableFutureDemo{
    public static  void main(String[] args) throws IOException {
//        CompletableFuture.runAsync(()->{
//            System.out.println("这是通过CompletableFuture的runAsync初始化一个子任务!"+Thread.currentThread().getName());
//        });

        //供给型（无参数，有返回值）
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("这是通过CompletableFuture的supplyAsync初始化了一个子任务！" + Thread.currentThread().getName());
//            int i = 1/0;
            return "hello completableFuture!";
        });
        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
            System.out.println("=================thenApplyAsync==================");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
            return "thenApplyAsync";
        });
        CompletableFuture<String> future2 = future.thenApplyAsync(t -> {
            System.out.println("=================thenApplyAsync2==================");
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
            return "thenApplyAsync";
        });
        CompletableFuture<Void> future3 = future.thenAcceptAsync(t -> {
            System.out.println("=================thenAcceptAsync==================");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
        });
        CompletableFuture<Void> future4 = future.thenRunAsync(() -> {
            System.out.println("=================thenRunAsync===================");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("没有上一任务的返回结果，也没有自己的返回结果");
        });
    /*.whenComplete((t,u)->{
            System.out.println("这是whenCompletable的supplyAsync执行的结果=============");
            System.out.println("上一个任务的返回结果t:"+t);
            System.out.println("上一个任务的异常信息u:"+u);
        }).exceptionally(t->{
            System.out.println("上一个任务的异常信息t = " + t);
            return "exceptionally";
        });*/

    //所有任务都执行完在返回结果
        CompletableFuture.allOf(future1,future2,future3,future4);
        System.out.println("这是主线程");
        System.in.read();

    }
}