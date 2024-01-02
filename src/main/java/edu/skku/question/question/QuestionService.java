package edu.skku.question.question;

import edu.skku.question.question.dto.ChoiceDto;
import edu.skku.question.question.dto.CreateQuestionDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    @Transactional
    public void createQuestion(String text, QuestionType type, List<ChoiceDto> choiceDtos, Integer answer, QuestionLevel level) {

        Question question = new Question();
        question.setType(type);
        question.setText(text);
        question.setAnswer(answer);
        question.setLevel(level);

        List<Choice> choices = choiceDtos.stream().map((choiceDto -> {
            Choice c = new Choice();
            c.setText(choiceDto.getText());
            c.setNumber(choiceDto.getNumber());
            c.setIsAnswer(choiceDto.getIsAnswer());
            c.setQuestion(question);
            return c;
        })).toList();

        question.setChoices(choices);

        this.questionRepository.save(question);
    }

    public Question findRandom(QuestionType type, QuestionLevel level) {
        return questionRepository.findRandomByTypeAndLevel(type, level);
    }

    public Question findOne(Long questionId) {
        Optional<Question> question = questionRepository.findById(questionId);
        return question.orElseThrow(() -> new EntityNotFoundException("Question with id " + questionId + " not found"));
    }

    public void deleteQuestion(Long questionId) {
        this.questionRepository.deleteById(questionId);
    }

    @Transactional
    public void createQuestions(List<CreateQuestionDto> questionDtos) {
        List<Question> questions = new ArrayList<>();

        for (CreateQuestionDto dto : questionDtos) {
            Question question = new Question();
            question.setType(dto.getType());
            question.setText(dto.getText());
            question.setAnswer(dto.getAnswer());
            question.setLevel(dto.getLevel());

            List<Choice> choices = dto.getChoices().stream().map(choiceDto -> {
                Choice c = new Choice();
                c.setText(choiceDto.getText());
                c.setNumber(choiceDto.getNumber());
                c.setIsAnswer(choiceDto.getIsAnswer());
                c.setQuestion(question);
                return c;
            }).collect(Collectors.toList());

            question.setChoices(choices);
            questions.add(question);
        }
        this.questionRepository.saveAll(questions);
    }

    public void deleteAllQuestions() {
        questionRepository.deleteAll();
    }

    public void createQuestion(CreateQuestionDto questionDto) {
        Question question = new Question();
        question.setType(questionDto.getType());
        question.setText(questionDto.getText());
        question.setAnswer(questionDto.getAnswer());
        question.setLevel(questionDto.getLevel());

        List<Choice> choices = questionDto.getChoices().stream().map((choiceDto -> {
            Choice c = new Choice();
            c.setText(choiceDto.getText());
            c.setNumber(choiceDto.getNumber());
            c.setIsAnswer(choiceDto.getIsAnswer());
            c.setQuestion(question);
            return c;
        })).toList();

        question.setChoices(choices);

        this.questionRepository.save(question);
    }

    public List<Question> findRandomQuestions(QuestionType type, QuestionLevel level, int count) {
        // 주어진 타입과 레벨에 해당하는 모든 문제 조회
        List<Question> allMatchingQuestions = questionRepository.findByTypeAndLevel(type, level);

        // 랜덤으로 문제 선택
        Collections.shuffle(allMatchingQuestions);
        return allMatchingQuestions.stream().limit(count).collect(Collectors.toList());
    }

}
