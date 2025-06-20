package com.project.nps.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Nps0001Dto {
    public String requestId;    // 리퀘스트ID
    public String callbackUrl;  // 콜백URL
    public String format;       // 포멧구조
    public String proStatus;    // 처리상태
    public String proMsg;       // 처리메시지
    public String reqDt;        // 요청일시
    public String resDt;        // 처리일시
    public String regDt;        // 등록일시
}
