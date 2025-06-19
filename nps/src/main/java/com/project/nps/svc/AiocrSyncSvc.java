package com.project.nps.svc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AiocrSyncSvc {
    public void checkRequestId(String requestId) throws Exception;
    public void setOcrProcess(String requestId, String format, MultipartFile[] ocrFiles, HttpServletRequest request) throws Exception;
}

