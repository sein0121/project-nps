package com.project.nps.ctl;

import com.project.nps.svc.AiocrSyncSvc;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Objects;

@Controller
@RequestMapping("/api/v1/aiocr")
@CrossOrigin
public class AiocrSyncCtl {
    private static final org.slf4j.Logger Logger = org.slf4j.LoggerFactory.getLogger(AiocrSyncCtl.class);

    @Resource(name = "aiocrSyncSvc")
    private AiocrSyncSvc aiocrSyncSvc;

    @Value("${twinreader.output.deleteYn}")
    String deleteYn;

    @RequestMapping(value = "/aiocrSyncLoad", method = RequestMethod.POST)
    @ResponseBody
    public HashMap<String, Object> aiocrSyncLoad(
            @RequestParam(value = "requestId") String requestId,
            @RequestParam(value = "format", required = false) String format,
            @RequestParam(value = "ocrFiles")MultipartFile[] ocrFiles,
            HttpServletRequest request)throws Exception {

        Logger.info("##### aiocrSyncLoad Start ##### \t requestId: "+ requestId);
        HashMap<String, Object> result = new HashMap<String, Object>();
        try{
            // 1. RequestID 중복 여부 체크
            aiocrSyncSvc.checkRequestId(requestId);

            // 2. 파일 INPUT 경로에 추가 후 분석 요청
            aiocrSyncSvc.setOcrProcess(requestId,format,ocrFiles, request);

        }catch (Exception e){
            Logger.error("##### aiocrSyncLoad error : " + e.getMessage());
            result.put("rsp_code", HttpStatus.BAD_REQUEST);
            result.put("rsp_msg", e.getMessage());
        }

        Logger.info("##### aiocrSyncLoad END #####" + result);
        return result;

    }
}
