package be.limburgie.stackoverflow.faces.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import com.liferay.faces.portal.context.LiferayFacesContext;

import be.limburgie.stackoverflow.domain.Question;
import be.limburgie.stackoverflow.service.QuestionService;
import be.limburgie.stackoverflow.service.exception.NoSuchQuestionException;

@Named @Scope("view")
public class QuestionDetailBean implements Serializable {

	@Inject private QuestionService questionService;
	private Question question;
	private long userId;
	private String newAnswer;
	private boolean posted;
	
	@PostConstruct
	public void initQuestion() {
		long id = LiferayFacesContext.getInstance().getRequestParameterAsLong("questionId", 0);
		userId = LiferayFacesContext.getInstance().getUserId();
		if(id != 0) {
			try {
				question = questionService.getQuestion(id, userId, true);
			} catch (NoSuchQuestionException e) {
				LiferayFacesContext.getInstance().addGlobalUnexpectedErrorMessage();
			}
		}
	}
	
	public void postAnswer() {
		question = questionService.postAnswer(userId, question, newAnswer);
		newAnswer = null;
		posted = true;
		LiferayFacesContext.getInstance().addGlobalInfoMessage("answer_posted_successfully");
	}
	
	public void answerUpVote(long answerId) {
		question = questionService.setAnswerVote(userId, answerId, 1);
	}
	
	public void answerDownVote(long answerId) {
		question = questionService.setAnswerVote(userId, answerId, -1);
	}
	
	public void setQuestionVote(int score) {
		long userId = LiferayFacesContext.getInstance().getUserId();
		question = questionService.setVote(userId, question, score);
	}
	
	public Question getQuestion() {
		return question;
	}

	public String getNewAnswer() {
		return newAnswer;
	}

	public void setNewAnswer(String newAnswer) {
		this.newAnswer = newAnswer;
	}
	
	public boolean isPosted() {
		return posted;
	}
	
}
