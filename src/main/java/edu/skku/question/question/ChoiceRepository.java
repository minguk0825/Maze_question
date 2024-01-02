package edu.skku.question.question;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChoiceRepository {
    private final EntityManager entityManager;

    public Long save(Choice choice) {
        entityManager.persist(choice);
        return choice.getId();
    }

    public Choice find(Long id) {
        return entityManager.find(Choice.class, id);
    }

}
