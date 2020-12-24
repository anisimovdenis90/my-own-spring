package org.springframework.beans.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Resource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.stereotype.Component;
import org.springframework.beans.factory.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class BeanFactory {

    private Map<String, Object> singletons = new HashMap<>();
    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public BeanFactory() {
        postProcessors.add(new CommonAnnotationBeanPostProcessor());
    }

    public void addPostProcessor(BeanPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
    }

    public Object getBean(String beanName) {
        return singletons.get(beanName);
    }

    public void instantiate(String basePackage) {
        List<File> files = new LinkedList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String path = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                File file = new File(resource.toURI());

                for (File classFile : file.listFiles()) {
                    String fileName = classFile.getName();
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(0, fileName.lastIndexOf("."));
                        Class<?> classObject = Class.forName(basePackage + "." + className);
                        if (classObject.isAnnotationPresent(Component.class) || classObject.isAnnotationPresent(Service.class)) {
                            System.out.println("Component: " + classObject);
                            Object instance = classObject.getDeclaredConstructor().newInstance();
                            String beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
                            singletons.put(beanName, instance);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFilesProcessing(List<File> dst, File source) {
        if (source == null) {
            return;
        }
        if (source.isFile()) {
            dst.add(source);
            return;
        }
        if (source.isDirectory()) {
            File[] files;
            if ((files = source.listFiles()) != null) {
                for (File file : files)
                getFilesProcessing(dst, file);
            }
        }
    }

    public void populateProperties() {
        System.out.println("Populate properties");
        try {
            for (Object o : singletons.values()) {
                for (Field f : o.getClass().getDeclaredFields()) {
                    if (f.isAnnotationPresent(Autowired.class)) {
                        for (Object dependency : singletons.values()) {
                            if (dependency.getClass().equals(f.getType())) {
                                inject(o, f, dependency);
                            }
                        }
                    } else if (f.isAnnotationPresent(Resource.class)) {
                        for (String beanName : singletons.keySet()) {
                            if (beanName.equals(f.getName())) {
                                Object dependency = singletons.get(beanName);
                                inject(o, f, dependency);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void injectBeanNames() {
        for (String name : singletons.keySet()) {
            Object bean = singletons.get(name);
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(name);
            }
        }
    }

    public void injectBeanFactory() {
        for (Object bean : singletons.values()) {
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(this);
            }
        }
    }

    public void initializeBean() {
        for (String name : singletons.keySet()) {
            Object bean = singletons.get(name);
            for (BeanPostProcessor beforePostProcessor : postProcessors) {
                beforePostProcessor.postProcessBeforeInitialization(bean, name);
            }
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
            for (BeanPostProcessor afterPostProcessor : postProcessors) {
                afterPostProcessor.postProcessAfterInitialization(bean, name);
            }
        }
    }

    public void close() {
        for (Object bean : singletons.values()) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    try {
                        method.invoke(bean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bean instanceof DisposableBean) {
                ((DisposableBean) bean).destroy();
            }
        }
    }

    private void inject(Object o, Field f, Object dependency) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
        System.out.println("Setter name: " + setterName);
        Method setter = o.getClass().getMethod(setterName, dependency.getClass());
        setter.invoke(o, dependency);
    }

}
