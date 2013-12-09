package be.limburgie.stackoverflow.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;

public class Question implements Serializable {

	private List<Answer> answers = new ArrayList<Answer>();
	private long id;
	private String summary;
	private String content;
	private int nbVotes;
	private int nbViews;
	private int nbAnswers;
	private boolean answerAccepted;
	private UserVote vote;

	// Helpers
	
	public boolean isAnswered() {
		return nbAnswers > 0;
	}

	public String getExcerpt() {
		return StringUtil.shorten(HtmlUtil.stripHtml(content), 200);
	}
	
	public String getIdString() {
		return String.valueOf(id);
	}
	
	public String getAnswerLabel() {
		return nbAnswers == 1 ? "answer" : "answers";
	}
	
	public String getVoteLabel() {
		return nbVotes == 1 ? "vote" : "votes";
	}
	
	public String getViewLabel() {
		return nbViews == 1 ? "view" : "views";
	}
	
	public String getAnswersCssClass() {
		if(!isAnswered()) {
			return "unanswered";
		}
		if(answerAccepted) {
			return "answered-accepted";
		}
		return "answered";
	}

	// Getters and setters

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getNbVotes() {
		return nbVotes;
	}

	public void setNbVotes(int nbVotes) {
		this.nbVotes = nbVotes;
	}

	public int getNbViews() {
		return nbViews;
	}

	public void setNbViews(int nbViews) {
		this.nbViews = nbViews;
	}

	public int getNbAnswers() {
		return nbAnswers;
	}

	public void setNbAnswers(int nbAnswers) {
		this.nbAnswers = nbAnswers;
	}

	public boolean isAnswerAccepted() {
		return answerAccepted;
	}

	public void setAnswerAccepted(boolean answerAccepted) {
		this.answerAccepted = answerAccepted;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<Answer> answers) {
		this.answers = answers;
	}

	public UserVote getVote() {
		return vote;
	}

	public void setVote(UserVote vote) {
		this.vote = vote;
	}

}
