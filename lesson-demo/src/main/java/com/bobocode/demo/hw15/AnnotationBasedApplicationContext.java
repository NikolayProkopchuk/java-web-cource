package com.bobocode.demo.hw15;

import com.bobocode.demo.hw15.annotation.Bean;
import com.bobocode.demo.hw15.exception.NoSuchBeanException;
import com.bobocode.demo.hw15.exception.NoUniqueBeanException;
import com.bobocode.demo.hw15.test.Test1;
import com.bobocode.demo.hw15.test.Test2;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class AnnotationBasedApplicationContext implements ApplicationContext {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationBasedApplicationContext("com.bobocode.demo.hw15");
        Test2 test2 = applicationContext.getBean(Test2.class);
        Test1 test1 = applicationContext.getBean("test", Test1.class);
        Map<String, Test2> allBeans = applicationContext.getAllBeans(Test2.class);
        System.out.println(test2);
        System.out.println(test1);
        System.out.println(allBeans);
    }

    private final Map<String, Object> beans;

    public AnnotationBasedApplicationContext(String packageName) {
        Reflections reflections = new Reflections(packageName);

        beans = reflections.getTypesAnnotatedWith(Bean.class).stream()
                .collect(Collectors.toMap(
                        clazz -> !clazz.getAnnotation(Bean.class).name().isEmpty()
                                ? clazz.getAnnotation(Bean.class).name()
                                : Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1),
                        this::initializeBean));
    }

    @SneakyThrows
    private <T> T initializeBean(Class<T> clazz) {
        return clazz.getConstructor().newInstance();
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        List<T> beansCurrentType = beans.values().stream()
                .filter(bean -> bean.getClass().isAssignableFrom(beanType))
                .map(beanType::cast)
                .collect(Collectors.toList());

        if (beansCurrentType.isEmpty()) {
            throw new NoSuchElementException();
        }

        if (beansCurrentType.size() > 1) {
            throw new NoUniqueBeanException();
        }

        return beansCurrentType.get(0);
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        Object objectBean = beans.get(name);
        if (objectBean == null || !objectBean.getClass().isAssignableFrom(beanType)) {
            throw new NoSuchBeanException();
        }
        return beanType.cast(objectBean);
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beans.entrySet().stream()
                .filter(entry -> entry.getValue().getClass().isAssignableFrom(beanType))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> beanType.cast(entry.getValue())));
    }
}
