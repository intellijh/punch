package com.punch.shop.common.annotation;

import com.punch.shop.common.config.JpaAuditingConfig;
import com.punch.shop.common.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
public @interface RepositoryTest {
}
