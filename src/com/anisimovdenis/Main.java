package com.anisimovdenis;

import org.springframework.context.ApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext("com.anisimovdenis");

        ProductService productService = (ProductService) applicationContext.getBean("productService");
        System.out.println(productService);
        System.out.println(productService.getPromotionsService());
        System.out.println("Bean name: " + productService.getPromotionsService().getBeanName());
        applicationContext.close();
    }

}
