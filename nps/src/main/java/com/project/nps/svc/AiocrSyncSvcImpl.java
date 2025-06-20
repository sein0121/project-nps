package com.project.nps.svc;

import com.project.nps.config.WebClientUtil;
import com.project.nps.dto.Nps0001Dto;

import com.project.nps.dto.Nps0002Dto;
import com.project.nps.dto.NpsHistoryDto;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("aiocrSyncSvc")
public class AiocrSyncSvcImpl implements AiocrSyncSvc {

    private static final Logger logger = LoggerFactory.getLogger(AiocrSyncSvcImpl.class);

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Value("${twinreader.input.path}")
    private String inputPath;

    @Value("${server.ip}")
    private String serverIp;

    @Value("${server.port}")
    private String serverPort;

    @Value("${twinreader.callback-url}")
    private String callbackUrl;

    @Value("${twinreader.callback-status}")
    private String callbackStatus;

    @Value("${twinreader.analysis.pipeline.name}")
    private String pipelineName;

    @Value("${twinreader.analysis.clsfGroupID}")
    private String groupID;

    @Value("${twinreader.url}")
    private String twrdUrl;

    @Value("${twinreader.api.inference}")
    private String inferenceApi;

    @Value("${twinreader.api.category}")
    private String apiCategory;

    @Value("${aipct.pension.thread.timeout}")
    private int threadTimeout;

    @Value("${aipct.pension.thread.sleep}")
    private int threadSleep;

  @Autowired
  private WebClientUtil webClientUtil;

    public void checkRequestId(String requestId) throws Exception {
        // 1. DB NPSPEN0001 PARAM SET
        logger.info("1. DB Nps0001Dto PARAM SET");
        Nps0001Dto nps0001Dto = new Nps0001Dto();
        nps0001Dto.setRequestId(requestId);

        // 2. NPSPEN0001 테이블 RequestID 개수 조회
        logger.info("2. NPSPEN0001 테이블 RequestID 개수 조회");
        int reqIdCnt = sqlSessionTemplate.selectOne("Nps0001Sql.selectReqIdCnt", nps0001Dto);

        // 3. 이 전에 사용 된 RequestID 인 경우 오류 처리
        if (reqIdCnt > 0) {
            throw new Exception("중복되는 Request ID 입니다.");
        }
    }

    public void setOcrProcess(
        String requestId,
        String format,
        MultipartFile[] ocrFiles,
        HttpServletRequest request) throws Exception {

        //현재 시간
        LocalDateTime now = LocalDateTime.now();
        String formatNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        //1.DB 001, 002 set
        logger.info("1. DB 001, 002 PARAN SET");
        NpsHistoryDto npsHistoryDto = new NpsHistoryDto();
        npsHistoryDto.setRequestId(requestId);
        npsHistoryDto.setFormat(format);
        npsHistoryDto.setReqDt(formatNow);
        npsHistoryDto.setResDt(formatNow);
        npsHistoryDto.setRegDt(formatNow);

        //Sync 의 경우 callbackUrl 불필요
        npsHistoryDto.setCallbackUrl(null);

        npsHistoryDto.setPageNum(0);
        npsHistoryDto.setCategory(null);
        npsHistoryDto.setProStatus("start");
        npsHistoryDto.setProMsg(null);

        //2. 분석 요청에 대한 DB 001 INSERT
        logger.info("2. 분석 요청에 대한 DB 001 INSERT");
        sqlSessionTemplate.insert("Nps0001Sql.insertReq", npsHistoryDto);

        //3. 전달 받은 파일 서버 INPUT 에 저장
        logger.info("3. 전달받은 파일 INPUT 경로에 저장");
        String saveDirPath = inputPath + "/"+requestId+"/";
        File saveDir = new File(saveDirPath);
        if (!saveDir.exists()) {
            logger.info("3-1. INPUT 디렉토리 생성" + saveDirPath);
            saveDir.mkdirs();
        }

        // 2. 파일 저장 루프
        for (MultipartFile ocrFile : ocrFiles) {
            String fileName = ocrFile.getOriginalFilename();
            File savedFile = new File(saveDir, fileName);

            // 파일 저장
            ocrFile.transferTo(savedFile);
            logger.info("4. INPUT 파일 저장" + saveDirPath);

            // DB INSERT
            logger.info("5. 요청에 대한 DB 002 INSERT");
            npsHistoryDto.setFileNm(fileName);
            sqlSessionTemplate.insert("Nps0002Sql.insert0002", npsHistoryDto);
        }

        //6. 트윈리더 분석 API 호출
        logger.info("6. 트윈리더 이미지 분석 요청");
        try{
            JSONObject analysisObj = new JSONObject();
            JSONArray analysisArr = new JSONArray();

            logger.info("6-1. 트윈리더 2.3버전 처리");
            analysisArr.add("/"+requestId+"/");
            analysisObj.put("pathList", analysisArr);
            analysisObj.put("requestID", requestId);
//            analysisObj.put("callbackUrl", "");
//            analysisObj.put("callbackUrl", "http://"+serverIp+":"+serverPort+callbackUrl);
            analysisObj.put("callbackUrl", "http://"+serverIp+":"+serverPort+callbackStatus);
            analysisObj.put("pipelineName", pipelineName);
            analysisObj.put("clsfGroupID", groupID);

//            logger.info("🍙🍙🍙 {}",analysisObj);
            JSONObject loadAnalysis = webClientUtil.post(
                twrdUrl+inferenceApi,
                analysisObj,
                JSONObject.class
            );

            if(!(boolean)loadAnalysis.get("success")) throw new Exception("Twinreader ANALYSIS FAILED");
        }catch(Exception error) {
            logger.error("##### AIOCR Analysis PROCESS FAILED " + error.getMessage());
            throw new Exception("AIOCR Analysis PROCESS FAILED");
        }


    }

    //분석 완료 후 prostatus 값 변경
    public void setProStatus(String requestId, JSONObject reqBody, HttpServletRequest request) throws Exception {

        LocalDateTime now = LocalDateTime.now();
        String formatNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        //1. 0001,002 set
        logger.info("7. ProStatus DB 001, 002 PARAN SET");
         Nps0001Dto nps0001Dto = new Nps0001Dto();
        nps0001Dto.setRequestId(requestId);
        nps0001Dto.setProStatus("analysis");
        nps0001Dto.setResDt(formatNow);

        Nps0002Dto nps0002Dto = new Nps0002Dto();
        nps0002Dto.setRequestId(requestId);

        try {
            // 2. DB NPSPEN0001 PRO_STATUS UPDATE
            logger.info("7-1. DB 0001 PRO_STATUS UPDATE");
            sqlSessionTemplate.update("Nps0001Sql.updateProStatus", nps0001Dto);
        } catch(Exception error) {
            logger.error("##### PRO_STATUS UPDATE FAILED " + error.getMessage());
            logger.error(nps0001Dto.toString());
            throw new Exception("PRO_STATUS UPDATE FAILED");
        }
    }

    //분석 완료 후 prostatus 값 가져오기
    public void getProStatus(String requestId) throws Exception {

        LocalDateTime now = LocalDateTime.now();
        String formatNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

//        1. DB 0001 Param SET
        Nps0001Dto nps0001Dto = new Nps0001Dto();
        nps0001Dto.setRequestId(requestId);

        int idx =1;
        String proStatus = "";

        //2. DB 0002 ProStatus 조회 (10초에 한번, 2분동안)
        while (!"analysis".equals(proStatus) && idx<=threadTimeout) {
            logger.info("7-2. DB NPS0001 PRO_STATUS 조회 "+idx);
            Thread.sleep(threadSleep);
            proStatus = sqlSessionTemplate.selectOne("Nps0001Sql.selectProStatus", nps0001Dto);
            idx++;
        }
        if(!"analysis".equals(proStatus)) throw new Exception("PRO_STATUS UPDATE FAILED");
    }

    //분석완료 후 결과 가공
    public JSONArray getOcrResult(String requestId, HttpServletRequest request) throws Exception {
        //현재 시간
        LocalDateTime now = LocalDateTime.now();
        String formatNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        JSONArray ocrResult = new JSONArray();

        logger.info("8. 분류 조회 - DB 001, 002 PARAN GET");
        Nps0001Dto nps0001Dto = new Nps0001Dto();
        nps0001Dto.setRequestId(requestId);
        nps0001Dto.setProStatus("finish");
        nps0001Dto.setResDt(formatNow);

        Nps0002Dto nps0002Dto = new Nps0002Dto();
        nps0002Dto.setRequestId(requestId);
        nps0002Dto.setRegDt(formatNow);

//       문서 분류결과 조회
        logger.info("8-1. 문서 분류 결과 조회");
        JSONArray analysisArr = new JSONArray();
        try{
            JSONObject analysisObj = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            jsonArr.add("/"+requestId+"/");
            analysisObj.put("images", jsonArr);

            analysisArr = webClientUtil.postJson(
                twrdUrl + apiCategory,
                analysisObj,
                JSONArray.class
            );

        }catch(Exception error) {
            logger.error(" ##### Twinreader ANALYSIS RESULT SEARCH FAILED " + error.getMessage());
            throw new Exception("Twinreader ANALYSIS RESULT SEARCH FAILED");
        }
        logger.info("Category 🍙🍙🍙🍙 {}", analysisArr);


        return ocrResult;
    }
}
