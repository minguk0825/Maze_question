package edu.skku.question.question.dto;

import lombok.Getter;

@Getter
public class ChoiceDto {
    private Integer number; // 몇번 보기인지
    private String text; // 보기 내용
    private Boolean isAnswer; // 정답 여부
}
