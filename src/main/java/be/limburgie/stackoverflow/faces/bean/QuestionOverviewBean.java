package be.limburgie.stackoverflow.faces.bean;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import be.limburgie.stackoverflow.domain.Question;
import be.limburgie.stackoverflow.service.QuestionService;

import com.liferay.faces.portal.context.LiferayFacesContext;

@Named @Scope("view")
public class QuestionOverviewBean implements Serializable {

	@Inject private QuestionService questionService;
	private List<Question> newestQuestions;
	
	public List<Question> getNewestQuestions() {
		if(newestQuestions == null) {
			long groupId = LiferayFacesContext.getInstance().getScopeGroupId();
			newestQuestions = questionService.getLatestQuestions(groupId, 0, 20);
		}
		return newestQuestions;
	}
	
}
