package edu.skku.question.question.dto;

import edu.skku.question.question.Choice;
import edu.skku.question.question.QuestionLevel;
import edu.skku.question.question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDto {
    private Long id;

    private String text;

    //    private List<ChoiceDto> choices;
    private List<Choice> choices;

    private QuestionType type;

    private Integer answer;

    private QuestionLevel level;
}
