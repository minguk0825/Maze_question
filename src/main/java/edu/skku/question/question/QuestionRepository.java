package edu.skku.question.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Random;

@Repository

public interface QuestionRepository extends JpaRepository<Question, Long> {

    long countByTypeAndLevel(QuestionType type, QuestionLevel level); // 특정 타입의 질문 수를 세는 매서드

    List<Question> findByTypeAndLevel(QuestionType type, QuestionLevel level); // 특정 타입의 모든 질문을 가져오는 매서드

    default Question findRandomByTypeAndLevel(QuestionType type, QuestionLevel level) {
        long count = this.countByTypeAndLevel(type, level);

        if (count > 0) {
            Random random = new Random();
            int index = random.nextInt((int) count);
            List<Question> questions = this.findByTypeAndLevel(type, level);
            return questions.get(index);
        }
        return null; // 또는 적절한 예외 처리
    }


}
