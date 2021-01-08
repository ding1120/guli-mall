package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;



import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;

import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    SpuMapper spuMapper;
    @Autowired
    SpuDescMapper descMapper;
    @Autowired
    SpuAttrValueService attrValueService;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuImagesService  skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    RabbitTemplate rabbitTemplate;




    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    //放在业务中进行查询，得到的是spuEntity
    //请求参数包括分页查询，和分类id，将分页查询条件封装为vo类
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        //1.判断categoryId是否为0，不为0根据分类id查，否则差全部
        if(categoryId!=0){
            queryWrapper.eq("category_id",categoryId);
        }
        //2.如果用户输入的是检索条件，根据检索条件(商品名或者id)进行查询
        String key = pageParamVo.getKey();
        //3.判断关键字是否为空，同时Blank能够判断空格，但Empty不能
        if(StringUtils.isNotBlank(key)){
            //检索条件不为空&(name=key||id=key)
            /**
             * 箭头函数copy小括号，为参数，
             * 落地大括号(里面为方法体)
             */
            queryWrapper.and(t->t.like("name",key)).or().like("id",key);
        }
        PageResultVo pageResultVo = new PageResultVo(this.page(pageParamVo.getPage(), queryWrapper));
        return pageResultVo;
    }

    @GlobalTransactional //代表一个全局的事务方法，其他事务是分支事务（本地事务）@Transaction
    @Override
    public void bigSave(SpuVo spuVo) {
        /**
         * 1.保存spu相关
         */
        //1.1保存spu的基本信息
        Long spuId = saveSpu(spuVo);

        //1.2 保存spu的描述信息
        saveSpuDesc(spuVo, spuId);


        //1.3 保存spu的规格参数信息
        //List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        //将vo集合转换成Entity集合
       /* if(!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                spuAttrValueVo.setSpuId(spuId);
                spuAttrValueVo.setSort(0);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            //批量保存
            attrValueService.saveBatch(spuAttrValueEntities);
        }*/
        saveBaseAttr(spuVo, spuId);


        /**
         * 2.保存sku信息
         */saveSku(spuVo, spuId);

        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE", "item.insert",spuId);

    }
    private void saveSku(SpuVo spuVo, Long spuId) {
        //2.1 保存sku的基本信息
        List<SkuVo> skuVos = spuVo.getSkus();
        if(CollectionUtils.isEmpty(skuVos)){
            return;
        }
        skuVos.forEach(skuVo -> {
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo,skuEntity);
            // 品牌和分类的id需要从spu中获取
            skuEntity.setBrandId(spuVo.getBrandId());
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            // 如果图片列表不为null，则设置默认图片
            if(!CollectionUtils.isEmpty(images)){
                // 设置第一张图片作为默认图片
                skuEntity.setDefaultImage(skuEntity.getDefaultImage()==null?
                        images.get(0):skuEntity.getDefaultImage());

            }
            skuEntity.setSpuId(spuId);
            skuMapper.insert(skuEntity);

            //获取保存后的skuId
            Long skuId = skuEntity.getId();

            //2.2保存sku的图片信息
            if(!CollectionUtils.isEmpty(images)){
                String defaultImage=images.get(0);
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);
            }

            //2.3 保存sku的规格参数（销售属性）
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(saleAttr->{
                // 设置属性名，需要根据id查询AttrEntity
                saleAttr.setSort(0);
                saleAttr.setSkuId(skuId);
            });
            skuAttrValueService.saveBatch(saleAttrs);
        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();

        if(CollectionUtils.isEmpty(baseAttrs)){
           List<SpuAttrValueEntity> attrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
               SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
               BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
               spuAttrValueEntity.setSpuId(spuId);
               return spuAttrValueEntity;
           }).collect(Collectors.toList());
           attrValueService.saveBatch(attrValueEntities);
       }
    }



    private void saveSpuDesc(SpuVo spuVo, Long spuId) {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
        spuDescEntity.setSpuId(spuId);
        // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(),","));
        this.descMapper.insert(spuDescEntity);
    }

    private Long saveSpu(SpuVo spuVo) {
        spuVo.setPublishStatus(1);//默认是已上架
        spuVo.setCreateTime(new Date());
        spuMapper.insert(spuVo);
        return spuVo.getId();
    }


}