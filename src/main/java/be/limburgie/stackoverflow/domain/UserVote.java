package be.limburgie.stackoverflow.domain;

import java.io.Serializable;

public class UserVote implements Serializable {

	private int score;
	
	public UserVote(int score) {
		this.score = score;
	}
	
	/* Helpers */
	
	public boolean isUp() {
		return score == 1;
	}
	
	public boolean isDown() {
		return score == -1;
	}
	
	public String getUpCssClass() {
		return isUp() ? "vote-up-on" : "vote-up-off";
	}
	
	public String getDownCssClass() {
		return isDown() ? "vote-down-on" : "vote-down-off";
	}
	
	/* Getters and setters */
	
	public int getScore() {
		return score;
	}
	
}
