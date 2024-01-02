package edu.skku.question.question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue
    private Long id;

    private String text;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Choice> choices = new ArrayList<>(); // 문제 선지들

    private Integer answer; // 정답 번호

    @Enumerated(EnumType.STRING)
    private QuestionType type; // 문제유형 [MATH,ENGLISH...]

    @Enumerated(EnumType.STRING)
    private QuestionLevel level; // 문제 난이도 [ONE,TWO,THREE,FOUR...]
}
