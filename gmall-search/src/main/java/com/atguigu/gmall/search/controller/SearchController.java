package com.atguigu.gmall.search.controller;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;


import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 12-21:使用thymeleaf,进行服务端渲染
 */
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    //响应前端页面
    @GetMapping
    public String search(SearchParamVo paramVo, Model model){

        SearchResponseVo responseVo= this.searchService.search(paramVo);
        //将响应数据放入model中，进行响应
        model.addAttribute("response",responseVo);
        model.addAttribute("searchParam",paramVo);
        //返回到search页面(search.html)
        return "search";
    }

}
