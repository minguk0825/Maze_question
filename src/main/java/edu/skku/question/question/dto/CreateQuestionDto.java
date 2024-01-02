package edu.skku.question.question.dto;

import edu.skku.question.question.QuestionLevel;
import edu.skku.question.question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuestionDto {

    private String text;

    private List<ChoiceDto> choices;

    private QuestionType type;

    private Integer answer;

    private QuestionLevel level;

}
