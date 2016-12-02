package com.pemc.crss.commons.web.dto;

import org.apache.commons.lang3.Validate;

public abstract class AbstractWebDto<T> implements WebDto<T> {

    private T target;

    public AbstractWebDto(final T target) {
        Validate.notNull(target, "Web dto target model cannot be null.");
        this.target = target;
    }

    @Override
    public T target() {
        return target;
    }

}
