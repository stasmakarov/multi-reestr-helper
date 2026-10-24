package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

@Service
public class NewViolationCountService {

    private final DataManager dataManager;

    public NewViolationCountService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public int countNew() {
        Long count = dataManager.loadValue(
                        "select count(e) from mtr_Violation e where e.status = :status", Long.class)
                .parameter("status", ViolationStatus.NEW)
                .one();
        return count == null ? 0 : count.intValue();
    }
}
