package com.project.nps.ctl;

import com.project.nps.dto.Nps0001Dto;
import com.project.nps.svc.AiocrSyncSvc;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

            //3. 분석 처리 완료 여부 조회 (ProStatus)

            aiocrSyncSvc.getProStatus(requestId);

            // 4. 항목 추출 결과 OUTPUT 경로에서 가져와 수정
            JSONArray ocrResult = aiocrSyncSvc.getOcrResult(requestId,request);

        }catch (Exception e){
            Logger.error("##### aiocrSyncLoad error : " + e.getMessage());
            result.put("rsp_code", HttpStatus.BAD_REQUEST);
            result.put("rsp_msg", e.getMessage());
        }

        Logger.info("##### aiocrSyncLoad END #####" + result);
        return result;

    }

    @RequestMapping(value = "/setProStatus", method = RequestMethod.POST)
    @ResponseBody
    public void setProStatus(
        @RequestBody(required = false) JSONObject reqBody
        , HttpServletRequest request) throws Exception {

        // 트윈리더 분석/추출 결과 PARAM
        String requestId = (String) reqBody.get("requestId");

        Logger.info("##### setProStatus START ##### \t requestId : " + requestId);

        try {
            // 1. DB NPSPEN0001 PRO_STATUS UPDATE (analysis)
            Logger.info("##### reqBody : " + reqBody);
//            Nps0001Dto nps0001Dto = new Nps0001Dto();
//            Logger.info("🔍 update 직전 row 존재 여부 확인: {}", sqlSessionTemplate.selectOne("Nps0001Sql.selectProStatus", nps0001Dto));
            aiocrSyncSvc.setProStatus(requestId, reqBody, request);
        } catch(Exception error) {
            Logger.error("##### setProStatus error : " + error.getMessage());
        }

        Logger.info("##### setProStatus END #####");
    }

//    @PostMapping("/getOcrResult")
//    @ResponseBody
//    public ResponseEntity<String> callbackHandler(@RequestBody JSONObject body, HttpServletRequest request) {
//        String requestId = (String) body.get("requestId");
//        try {
//            aiocrSyncSvc.setProStatus(requestId, body, request);
//            return ResponseEntity.ok("callback received");
//        } catch (Exception e) {
//            Logger.error("### Callback processing failed: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("callback failed");
//        }
//    }
}
