package be.limburgie.stackoverflow.service;

import java.io.Serializable;
import java.util.List;

import be.limburgie.stackoverflow.domain.Question;
import be.limburgie.stackoverflow.service.exception.CannotCreateQuestionException;
import be.limburgie.stackoverflow.service.exception.NoSuchQuestionException;


public interface QuestionService extends Serializable {

	List<Question> getLatestQuestions(long repositoryId, int start, int end);

	Question createQuestion(long repositoryId, long userId, Question question) throws CannotCreateQuestionException;

	Question getQuestion(long id, long userId, boolean incrementViewCount) throws NoSuchQuestionException;

	Question setVote(long userId, Question question, int score);

	Question setAnswerVote(long userId, long answerId, int i);

	Question postAnswer(long userId, Question question, String newAnswer);

}
