package com.jzh.springbootvuedemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class SpringbootVueDemoApplicationTests {

    @Test
    void contextLoads() {
        String test = "view,edit";
        List<String> strings = Arrays.asList(test.split(","));
        System.out.println(strings);
    }

}
