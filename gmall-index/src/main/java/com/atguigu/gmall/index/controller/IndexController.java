package com.atguigu.gmall.index.controller;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;
    @GetMapping({"index.html", "/"})
    public String toIndex(Model model, HttpServletRequest request){
        System.out.println(request.getHeader("userId")+"===================");
        //三级分类数据

        //TODO :大广告,轮播广告
        //查询一级分类
        List<CategoryEntity> categoryEntities=this.indexService.queryLvl1Categories();

        //将数据返回给前端
        model.addAttribute("categories", categoryEntities);

        //返回到首页
        return "index";
    }

    //查询二级分类，三级分类
    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSubByPid(@PathVariable Long pid){
        List<CategoryEntity> categoryEntities= this.indexService.queryLvl2CategoriesWithSubByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/test/lock")
    @ResponseBody
    public  ResponseVo testLock(){
        this.indexService.testLock();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/read")
    @ResponseBody
    public ResponseVo testRead(){
        this.indexService.testRead();
        return ResponseVo.ok("读取成功！！！");
    }

    @GetMapping("index/test/write")
    @ResponseBody
    public ResponseVo testWrite(){
        this.indexService.testWrite();
        return ResponseVo.ok("写入成功！！！");
    }

    @GetMapping("index/test/latch")
    @ResponseBody
    public ResponseVo testLatch(){
        this.indexService.testLatch();
        return ResponseVo.ok("班长锁门了。。。。");
    }

    @GetMapping("index/test/countdown")
    @ResponseBody
    public ResponseVo testCountdown(){
        this.indexService.testCountdown();
        return ResponseVo.ok("出来了一位同学。。。。");
    }
}

