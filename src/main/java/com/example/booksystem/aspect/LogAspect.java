package com.example.booksystem.aspect;

import com.example.booksystem.service.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 系统日志切面
 */
@Aspect
@Component
public class LogAspect {

    @Autowired
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点
     */
    @Pointcut("@annotation(io.swagger.annotations.ApiOperation)")
    public void logPointCut() {
    }

    /**
     * 环绕通知
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Integer status = 1;
        String errorMsg = null;

        try {
            // 执行方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 保存日志
            saveLog(point, beginTime, status, errorMsg);
        }
    }

    /**
     * 保存日志
     */
    private void saveLog(ProceedingJoinPoint joinPoint, long beginTime, Integer status, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 获取操作说明
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            String operation = apiOperation != null ? apiOperation.value() : "";

            // 请求方法
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = method.getName();
            String methodInfo = className + "." + methodName + "()";

            // 请求参数
            String params = getRequestParams(joinPoint);

            // 获取IP地址
            String ip = getIpAddress(request);

            // 执行时长
            long time = System.currentTimeMillis() - beginTime;

            // 保存日志
            logService.saveLog(operation, methodInfo, params, ip, status, errorMsg, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    params.append(paramNames[i]).append(": ");
                    // 如果是文件对象，则不转换为JSON
                    if (args[i] instanceof byte[] || args[i] instanceof java.io.InputStream) {
                        params.append("file");
                    } else {
                        params.append(objectMapper.writeValueAsString(args[i]));
                    }
                    params.append(", ");
                }
            }
            return params.toString();
        } catch (JsonProcessingException e) {
            return Arrays.toString(joinPoint.getArgs());
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}