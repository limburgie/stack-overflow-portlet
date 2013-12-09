package be.limburgie.stackoverflow.faces.common;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ViewScope implements Scope {

	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String,Object> viewMap = getViewRoot().getViewMap();
		if(viewMap.containsKey(name)) {
			return viewMap.get(name);
		} else {
			Object object = objectFactory.getObject();
			viewMap.put(name, object);
			return object;
		}
	}

	public Object remove(String name) {
		return getViewRoot().getViewMap().remove(name);
	}

	public String getConversationId() {
		return null;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		//Not supported
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	private UIViewRoot getViewRoot() {
		return FacesContext.getCurrentInstance().getViewRoot();
	}
	
}