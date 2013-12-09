package be.limburgie.stackoverflow.service.impl;

import java.util.Comparator;

import javax.inject.Named;

import be.limburgie.stackoverflow.domain.Answer;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

@Named
public class AnswersByScoreComparator implements Comparator<Answer> {

	public int compare(Answer answer1, Answer answer2) {
		String className = MBMessage.class.getName();
		try {
			double score1 = RatingsStatsLocalServiceUtil.getStats(className, answer1.getId()).getTotalScore();
			double score2 = RatingsStatsLocalServiceUtil.getStats(className, answer2.getId()).getTotalScore();
			return (int) (score1 - score2);
		} catch (SystemException e) {
			return 0;
		}
	}

}
