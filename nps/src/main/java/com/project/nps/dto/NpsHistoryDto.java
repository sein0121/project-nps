package com.project.nps.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NpsHistoryDto {
  public String requestId;    // PK 리퀘스트ID

  // NPS0001
  public String callbackUrl;  // 콜백URL
  public String format;       // 포멧구조
  public String reqDt;        // 요청일시
  public String resDt;        // 처리일시

  // NPS0002
  public String fileNm;       // 파일명
  public int pageNum;         // 페이지번호
  public String category;     // 분류명

  // NPS0001, NPS0002
  public String proStatus;    // 처리상태
  public String proMsg;       // 처리메시지

  public String regDt;        // 등록일시
}
