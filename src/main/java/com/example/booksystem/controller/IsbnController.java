package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * ISBN API代理控制器
 * 用于解决前端直接调用第三方API的CORS问题
 */
@Api(tags = "ISBN API代理")
@RestController
@RequestMapping("/api/isbn")
public class IsbnController {

    private final RestTemplate restTemplate = new RestTemplate();

    // 聚合数据API密钥，可以从配置文件中读取
    @Value("${isbn.key}")
    private String apiKey;

    @ApiOperation("查询ISBN信息")
    @GetMapping("/query")
    public Result<Object> queryIsbn(@RequestParam String isbn) {
        try {
            String url = "http://apis.juhe.cn/isbn/query?key=" + apiKey + "&isbn=" + isbn;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Result.success(response.getBody());
            } else {
                return Result.error("查询ISBN信息失败");
            }
        } catch (Exception e) {
            return Result.error("查询ISBN信息失败: " + e.getMessage());
        }
    }
}