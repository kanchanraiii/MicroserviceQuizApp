package com.quizservice.service;

import com.quizservice.feign.QuizInterface;
import com.quizservice.model.QuestionWrapper;
import com.quizservice.model.Quiz;
import com.quizservice.model.Response;
import com.quizservice.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private QuizInterface quizInterface;

   
    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {

        List<String> questionIds =
                quizInterface.getQuestionsForQuiz(category, numQ).getBody();

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestionIds(questionIds);

        quizRepo.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

   
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(String quizId) {

        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        List<String> questionIds = quiz.getQuestionIds();

        return quizInterface.getQuestionsFromId(questionIds);
    }

   
    public ResponseEntity<Integer> calculateResult(String id, List<Response> responses) {

        return quizInterface.getScore(responses);
    }
}
