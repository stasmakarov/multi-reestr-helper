package com.digitilius.multireestr.legacy;

import org.springframework.dao.DataAccessResourceFailureException;

public class LegacyNotConfiguredException extends DataAccessResourceFailureException {

    public static final String MESSAGE = "БД MultiReestr не настроена";

    public LegacyNotConfiguredException() {
        super(MESSAGE);
    }
}
