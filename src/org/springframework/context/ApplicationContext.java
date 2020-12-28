package org.springframework.context;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.event.ContextClosedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ApplicationContext {
    private BeanFactory beanFactory = new BeanFactory();

    public ApplicationContext(String basePackage) {
        System.out.println("----Application context constructor----");
        beanFactory.instantiate(basePackage);
        beanFactory.populateProperties();
        beanFactory.injectBeanNames();
        beanFactory.injectBeanFactory();
        beanFactory.initializeBeans();
    }

    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    public void close() {
        for (Object bean : beanFactory.getSingletons().values()) {
            for (Type type : bean.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type firstParameter = parameterizedType.getActualTypeArguments()[0];
                    if (firstParameter.equals(ContextClosedEvent.class)) {

                        Method method = null;
                        try {
                            method = bean.getClass().getMethod("onApplicationEvent", ContextClosedEvent.class);
                            method.invoke(bean, new ContextClosedEvent());
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        beanFactory.close();
    }
}
