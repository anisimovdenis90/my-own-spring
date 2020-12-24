package com.anisimovdenis;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ProductService implements InitializingBean {

    @Autowired
    private PromotionsService promotionsService;

    public PromotionsService getPromotionsService() {
        return promotionsService;
    }

    @PostConstruct
    public void init() {
        System.out.println("Post construct processing");
    }

    public void setPromotionsService(PromotionsService promotionsService) {
        this.promotionsService = promotionsService;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("After properties set processing");
    }
}
