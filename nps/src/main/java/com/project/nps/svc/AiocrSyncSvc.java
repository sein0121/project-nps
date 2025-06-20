package com.project.nps.svc;

import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface AiocrSyncSvc {
    public void checkRequestId(String requestId) throws Exception;
    public void setOcrProcess(String requestId, String format, MultipartFile[] ocrFiles, HttpServletRequest request) throws Exception;
    public void setProStatus(String requestId, JSONObject reqBody, HttpServletRequest request) throws Exception;
    public void getProStatus(String requestId) throws Exception;
    public JSONArray getOcrResult(String requestId, HttpServletRequest request) throws Exception;

    public void deleteDir(String requestId)throws Exception;
}

