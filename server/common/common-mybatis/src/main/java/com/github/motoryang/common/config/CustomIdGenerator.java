package com.github.motoryang.common.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import org.springframework.stereotype.Component;

@Component
public class CustomIdGenerator implements IdentifierGenerator {

    // UUID v7 生成器
    private final NoArgGenerator generator = Generators.timeBasedEpochGenerator();

    @Override
    public Number nextId(Object entity) {
        return null;
    }

    @Override
    public String nextUUID(Object entity) {
        return generator.generate().toString();
    }
}

