package com.plusProject.IQuiz.services;

import com.plusProject.IQuiz.dto.CategoriesDto;
import com.plusProject.IQuiz.dto.CategoryQuestionCountInfoDto;
import com.plusProject.IQuiz.dto.QuestionsDto;
import com.plusProject.IQuiz.frontEnd.Difficulty;
import com.plusProject.IQuiz.frontEnd.GameOptions;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
@Log
public class QuizDataService {

    public List<CategoriesDto.CategoryDto> getIQuizCategories() {
        RestTemplate restTemplate = new RestTemplate();
        CategoriesDto result = restTemplate.getForObject("https://opentdb.com/api_category.php", CategoriesDto.class);
        log.info("Quiz categories: " + result.getCategories());
        return result.getCategories();
    }

    public List<QuestionsDto.QuestionDto> getIQuizQuestions(GameOptions gameOptions) {
        CategoryQuestionCountInfoDto categoryQuestionCount = getCategoryQuestionCount(gameOptions.getCategoryId());
        int availableQuestionCount = categoryQuestionCount.getQuestionCountForDifficulty(gameOptions.getDifficulty());
        if (availableQuestionCount >= gameOptions.getNumberOfQuestions()) {
            return getIQuizQuestions(gameOptions.getNumberOfQuestions(), gameOptions.getCategoryId(), gameOptions.getDifficulty());
        } else {
            List<QuestionsDto.QuestionDto> questions = new ArrayList<>();
            Map<Difficulty, Integer> eachDifficultyQuestionCount = calculateEachDifficultyQuestionCount(gameOptions.getNumberOfQuestions(), gameOptions.getDifficulty(), categoryQuestionCount);
            for (Map.Entry<Difficulty, Integer> entry : eachDifficultyQuestionCount.entrySet()) {
                List<QuestionsDto.QuestionDto> originalDifficultyQuestions = getIQuizQuestions(entry.getValue(), gameOptions.getCategoryId(), entry.getKey());
                questions.addAll(originalDifficultyQuestions);
            }
            Collections.shuffle(questions);
            return questions;
        }
    }

    private List<QuestionsDto.QuestionDto> getIQuizQuestions(int numberOfQuestions, int categoryId, Difficulty difficulty) {
        if (numberOfQuestions <= 0) {
            return Collections.emptyList();
        }
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl("https://opentdb.com/api.php")
                .queryParam("amount", numberOfQuestions)
                .queryParam("category", categoryId)
                .queryParam("difficulty", difficulty.name().toLowerCase())
                .build().toUri();
        log.info("IQuiz question retrieve URL: " + uri);

        QuestionsDto result = restTemplate.getForObject(uri, QuestionsDto.class);
        log.info("IQuiz questions Open Trivia DB response code = " + result.getResponseCode() + ". Content: " + result.getResults());
        return result.getResults();
    }

    private CategoryQuestionCountInfoDto getCategoryQuestionCount(int categoryId) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl("https://opentdb.com/api_count.php")
                .queryParam("category", categoryId)
                .build().toUri();
        log.info("Quiz category question count retrieve URL: " + uri);
        CategoryQuestionCountInfoDto result = restTemplate.getForObject(uri, CategoryQuestionCountInfoDto.class);
        log.info("Quiz category question count content: " + result);
        return result;
    }

    static Map<Difficulty, Integer> calculateEachDifficultyQuestionCount(int numberOfQuestion, Difficulty difficulty, CategoryQuestionCountInfoDto categoryQuestionCount) {
        Map<Difficulty, Integer> eachDifficultyQuestionCount = new EnumMap<>(Difficulty.class);
        eachDifficultyQuestionCount.put(difficulty, Math.min(numberOfQuestion, categoryQuestionCount.getQuestionCountForDifficulty(difficulty)));

        int missingQuestions = numberOfQuestion - eachDifficultyQuestionCount.values().stream()
                .mapToInt(i ->i).sum();
        while (missingQuestions > 0) {
            Difficulty closestDifficulty = Difficulty.calculateNextDifficulty(eachDifficultyQuestionCount.keySet());
            if (closestDifficulty == null) {
                log.warning("Not enough question in given category!");
                break;
            }
            eachDifficultyQuestionCount.put(closestDifficulty, Math.min(missingQuestions, categoryQuestionCount.getQuestionCountForDifficulty(closestDifficulty)));
            missingQuestions = numberOfQuestion - eachDifficultyQuestionCount.values().stream()
                    .mapToInt(i -> i).sum();
        }
        if (difficulty == Difficulty.MEDIUM) {
            Difficulty otherDifficulty = Difficulty.calculateNextDifficulty(eachDifficultyQuestionCount.keySet());
            if (otherDifficulty != null) {
                Difficulty filledDifficulty = difficulty.getClosestDifficulty();
                final int numberToTransfer = Math.min(eachDifficultyQuestionCount.get(filledDifficulty) / 2, categoryQuestionCount.getQuestionCountForDifficulty(otherDifficulty));
                eachDifficultyQuestionCount.computeIfPresent(filledDifficulty, (d, count) -> count - numberToTransfer);
                eachDifficultyQuestionCount.put(otherDifficulty, numberToTransfer);
            }
        }
        return eachDifficultyQuestionCount;
    }

}
