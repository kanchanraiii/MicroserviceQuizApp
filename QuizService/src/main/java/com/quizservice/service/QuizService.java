package com.quizservice.service;

import com.quizservice.feign.QuizInterface;
import com.quizservice.model.QuestionWrapper;
import com.quizservice.model.Quiz;
import com.quizservice.model.Response;
import com.quizservice.repository.QuizRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

   
    @CircuitBreaker(
            name = "questionServiceCB",
            fallbackMethod = "createQuizFallback"
    )
    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {

        ResponseEntity<List<String>> response =
                quizInterface.getQuestionsForQuiz(category, numQ);

        if (response == null || response.getBody() == null) {
            throw new RuntimeException("No questions returned");
        }

        List<String> questionIds = response.getBody();

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestionIds(questionIds);

        quizRepo.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }


   
    @CircuitBreaker(
    		name="questionServiceCB",
    		fallbackMethod="getQuizQuestionsFallback"
    )
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(String quizId) {

        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        List<String> questionIds = quiz.getQuestionIds();

        return quizInterface.getQuestionsFromId(questionIds);
    }

   
    @CircuitBreaker(
    		name="questionServiceCB",
    		fallbackMethod="calculateResultFallback")
    public ResponseEntity<Integer> calculateResult(String id, List<Response> responses) {

        return quizInterface.getScore(responses);
    }
    
    
    // adding the fallback methods
    public ResponseEntity<String> createQuizFallback(String category, int numQ, String title, Throwable ex){
    	
    	System.out.println("Fallback - Create Quiz ");
    	
    	return new ResponseEntity<>(
    			"Question service unavailable. Try again later.",
                HttpStatus.SERVICE_UNAVAILABLE
    	);
    			
    }
    
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestionsFallback(String quizId, Throwable ex){
    	return new ResponseEntity<>(List.of(), HttpStatus.SERVICE_UNAVAILABLE);
    	
    }
    
    public ResponseEntity<Integer> calculateResultFallback(String id, List<Response> responses, Throwable ex){
    	return new ResponseEntity<>(
                0, HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
