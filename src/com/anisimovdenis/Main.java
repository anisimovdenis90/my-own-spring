package com.anisimovdenis;

import org.springframework.beans.factory.BeanFactory;

public class Main {

    public static void main(String[] args) {
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.addPostProcessor(new CustomPostProcessor());
        beanFactory.instantiate("com.anisimovdenis");
        beanFactory.populateProperties();
        beanFactory.injectBeanNames();
        beanFactory.injectBeanFactory();
        beanFactory.initializeBean();

        ProductService productService = (ProductService) beanFactory.getBean("productService");
        System.out.println(productService);
        System.out.println(productService.getPromotionsService());
        System.out.println("Bean name: " + productService.getPromotionsService().getBeanName());
    }

}
