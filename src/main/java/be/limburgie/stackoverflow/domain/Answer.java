package be.limburgie.stackoverflow.domain;

public class Answer {

	private long id;
	private String content;
	private int nbVotes;
	private boolean accepted;
	private UserVote vote;

	/* Getters and setters */
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public UserVote getVote() {
		return vote;
	}

	public void setVote(UserVote vote) {
		this.vote = vote;
	}

}
