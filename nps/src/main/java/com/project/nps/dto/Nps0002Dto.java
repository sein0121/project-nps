package com.project.nps.dto;

import lombok.Data;

@Data
public class Nps0002Dto {
  public String requestId;    // 리퀘스트ID
  public String fileNm;       // 파일명
  public int pageNum;         // 페이지번호
  public String category;     // 분류명
  public String proStatus;    // 처리상태
  public String proMsg;       // 처리메시지
  public String regDt;        // 등록일시
}
