package be.limburgie.stackoverflow.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import be.limburgie.stackoverflow.domain.Answer;
import be.limburgie.stackoverflow.domain.Question;
import be.limburgie.stackoverflow.domain.UserVote;
import be.limburgie.stackoverflow.service.QuestionService;
import be.limburgie.stackoverflow.service.exception.CannotCreateQuestionException;
import be.limburgie.stackoverflow.service.exception.NoSuchQuestionException;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.messageboards.NoSuchThreadException;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;
import com.liferay.portlet.ratings.NoSuchEntryException;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

@Named
public class QuestionServiceLiferayImpl implements QuestionService {

	private static final Log LOGGER = LogFactoryUtil.getLog(QuestionServiceLiferayImpl.class);
	
	@Inject private AnswersByScoreComparator answersByScoreComparator;

	@SuppressWarnings("unchecked")
	public List<Question> getLatestQuestions(long groupId, int start, int end) {
		DynamicQuery dq = DynamicQueryFactoryUtil.forClass(MBThread.class, PortalClassLoaderUtil.getClassLoader());
		dq.add(PropertyFactoryUtil.forName("question").eq(true));
		dq.add(PropertyFactoryUtil.forName("groupId").eq(groupId));
		dq.addOrder(OrderFactoryUtil.desc("threadId"));
		dq.setLimit(start, end);
		try {
			List<MBThread> questionThreads = MBThreadLocalServiceUtil.dynamicQuery(dq);
			List<Question> questions = new ArrayList<Question>();
			for (MBThread thread : questionThreads) {
				questions.add(toQuestion(thread.getThreadId()));
			}
			return questions;
		} catch (SystemException e) {
			LOGGER.error(e);
		} catch (PortalException e) {
			LOGGER.error(e);
		}
		return Collections.emptyList();
	}

	public Question createQuestion(long groupId, long userId, Question question) throws CannotCreateQuestionException {
		long categoryId = 0;
		String subject = question.getSummary();
		String body = question.getContent();
		String format = "html";
		List<ObjectValuePair<String, InputStream>> inputStreamOVPs = Collections.emptyList();
		boolean anonymous = false;
		int priority = 0;
		boolean allowPingbacks = false;
		String userName = null;
		ServiceContext serviceContext = new ServiceContext();

		try {
			MBMessage message = MBMessageLocalServiceUtil.addMessage(userId, userName, groupId, categoryId, subject, body,
					format, inputStreamOVPs, anonymous, priority, allowPingbacks, serviceContext);
			MBThreadLocalServiceUtil.updateQuestion(message.getThreadId(), true);
			return toQuestion(message.getThreadId());
		} catch (PortalException e) {
			LOGGER.error(e);
			throw new CannotCreateQuestionException();
		} catch (SystemException e) {
			LOGGER.error(e);
			throw new CannotCreateQuestionException();
		}
	}

	private Question toQuestion(long threadId) throws PortalException, SystemException {
		Question q = new Question();
		MBThread thread = MBThreadLocalServiceUtil.getThread(threadId);
		MBMessage rootMessage = MBMessageLocalServiceUtil.getMessage(thread.getRootMessageId());

		DynamicQuery dq = DynamicQueryFactoryUtil.forClass(MBMessage.class, PortalClassLoaderUtil.getClassLoader());
		dq.add(PropertyFactoryUtil.forName("threadId").eq(threadId));
		dq.add(PropertyFactoryUtil.forName("status").eq(0));
		dq.add(PropertyFactoryUtil.forName("answer").eq(true));
		dq.setProjection(ProjectionFactoryUtil.rowCount());
		Long nbAccepted = (Long) MBMessageLocalServiceUtil.dynamicQuery(dq).get(0);
		if(nbAccepted > 0) {
			q.setAnswerAccepted(true);
		}
		
		RatingsStats ratings = RatingsStatsLocalServiceUtil.getStats(MBMessage.class.getName(), rootMessage.getMessageId());
		q.setId(thread.getThreadId());
		q.setSummary(rootMessage.getSubject());
		q.setContent(rootMessage.getBody());
		q.setNbViews(thread.getViewCount());
		q.setNbAnswers(thread.getMessageCount() - 1);
		q.setNbVotes((int)ratings.getTotalScore());
		return q;
	}
	
	private Question toExtendedQuestion(long threadId, long userId) throws PortalException, SystemException {
		Question q = toQuestion(threadId);
		MBThread thread = MBThreadLocalServiceUtil.getThread(threadId);
		q.setVote(new UserVote(getVoteScore(userId, thread.getRootMessageId())));
		
		List<Answer> answers = new ArrayList<Answer>();
		List<MBMessage> replies = MBMessageLocalServiceUtil.getThreadRepliesMessages(threadId,
				WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		for(MBMessage reply: replies) {
			Answer answer = toAnswer(reply);
			UserVote vote = new UserVote(getVoteScore(userId, reply.getMessageId()));
			answer.setVote(vote);
			answers.add(answer);
		}
		Collections.sort(answers, answersByScoreComparator);
		q.setAnswers(answers);
		return q;
	}
	
	private Answer toAnswer(MBMessage reply) throws SystemException {
		Answer answer = new Answer();
		answer.setId(reply.getMessageId());
		answer.setAccepted(reply.isAnswer());
		answer.setContent(reply.getBody());
		RatingsStats ratings = RatingsStatsLocalServiceUtil.getStats(MBMessage.class.getName(), reply.getMessageId());
		answer.setNbVotes((int)ratings.getTotalScore());
		return answer;
	}

	public Question getQuestion(long id, long userId, boolean incrementViewCount) throws NoSuchQuestionException {
		try {
			if(incrementViewCount) {
				MBThreadLocalServiceUtil.incrementViewCounter(id, 1);
			}
			return toExtendedQuestion(id, userId);
		} catch (NoSuchThreadException e) {
			throw new NoSuchQuestionException();
		} catch (PortalException e) {
			LOGGER.error(e);
		} catch (SystemException e) {
			LOGGER.error(e);
		}
		return null;
	}

	public Question setVote(long userId, Question question, int score) {
		try {
			MBThread thread = MBThreadLocalServiceUtil.getThread(question.getId());
			setVoteScore(userId, thread.getRootMessageId(), score);
			return toExtendedQuestion(question.getId(), userId);
		} catch (PortalException e) {
			LOGGER.error(e);
			return question;
		} catch (SystemException e) {
			LOGGER.error(e);
			return question;
		}
	}

	private int getVoteScore(long userId, long messageId) {
		try {
			String className = MBMessage.class.getName();
			return (int) RatingsEntryLocalServiceUtil.getEntry(userId, className, messageId).getScore();
		} catch (NoSuchEntryException e) {
			return 0;
		} catch (PortalException e) {
			LOGGER.error(e);
			return 0;
		} catch (SystemException e) {
			LOGGER.error(e);
			return 0;
		}
	}
	
	private void setVoteScore(long userId, long messageId, int score) {
		try {
			if(getVoteScore(userId, messageId) == score) {
				score = 0;
			}
			String className = MBMessage.class.getName();
			ServiceContext serviceContext = new ServiceContext();
			RatingsEntryLocalServiceUtil.updateEntry(userId, className, messageId, score, serviceContext);
		} catch (PortalException e) {
			LOGGER.error(e);
		} catch (SystemException e) {
			LOGGER.error(e);
		}
	}

	public Question setAnswerVote(long userId, long answerId, int score) {
		try {
			setVoteScore(userId, answerId, score);
			long threadId = MBMessageLocalServiceUtil.getMBMessage(answerId).getThreadId();
			return toExtendedQuestion(threadId, userId);
		} catch (PortalException e) {
			LOGGER.error(e);
		} catch (SystemException e) {
			LOGGER.error(e);
		}
		return null;
	}

	public Question postAnswer(long userId, Question question, String newAnswer) {
		try {
			String userName = null;
			long threadId = question.getId();
			MBThread thread = MBThreadLocalServiceUtil.getThread(threadId);
			long groupId = thread.getGroupId();
			String className = null;
			long classPK = 0;
			long parentMessageId = thread.getRootMessageId();
			String subject = null;
			String body = newAnswer;
			ServiceContext serviceContext = new ServiceContext();
			MBMessageLocalServiceUtil.addDiscussionMessage(userId, userName, groupId, className, classPK, threadId, parentMessageId, subject, body, serviceContext);
			return getQuestion(threadId, userId, false);
		} catch (PortalException e) {
			LOGGER.error(e);
		} catch (SystemException e) {
			LOGGER.error(e);
		} catch (NoSuchQuestionException e) {
			LOGGER.error(e);
		}
		return null;
	}

}
