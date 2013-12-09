package be.limburgie.stackoverflow.faces.bean;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.portlet.PortletURL;
import javax.portlet.ResourceResponse;

import org.springframework.context.annotation.Scope;

import be.limburgie.stackoverflow.domain.Question;
import be.limburgie.stackoverflow.service.QuestionService;
import be.limburgie.stackoverflow.service.exception.CannotCreateQuestionException;

import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;

@Named @Scope("view")
public class CreateQuestionBean implements Serializable {

	@Inject private QuestionService questionService;
	private Question question;

	public void prepareNew() {
		question = new Question();
	}
	
	public void post() throws IOException, PortalException, SystemException {
		long userId = LiferayFacesContext.getInstance().getUserId();
		long groupId = LiferayFacesContext.getInstance().getScopeGroupId();
		try {
			question = questionService.createQuestion(groupId, userId, question);
			PortletURL renderUrl = ((ResourceResponse) LiferayFacesContext.getInstance().getPortletResponse()).createRenderURL();
			renderUrl.setParameter("questionId", question.getIdString());
			LiferayFacesContext.getInstance().getExternalContext().redirect(renderUrl.toString());
		} catch (CannotCreateQuestionException e) {
			LiferayFacesContext.getInstance().addGlobalErrorMessage("error!");
		}
//		String currentUrl = PortalUtil.getLayoutFullURL(LiferayFacesContext.getInstance().getThemeDisplay());
//		LiferayFacesContext.getInstance().getExternalContext().redirect(currentUrl);
	}
	
	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}
	
}
