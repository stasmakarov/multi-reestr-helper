package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class ContractTermUniqueTest {

    @Autowired
    DataManager dataManager;

    @Test
    void uniqueContractLegacyIdAndTermType() {
        ContractTerm first = dataManager.create(ContractTerm.class);
        first.setContractLegacyId(777001);
        first.setTermType(ContractTermType.PAYMENT);
        first.setFixedDate(java.time.LocalDate.of(2026, 1, 15));
        first = dataManager.save(first);
        try {
            ContractTerm duplicate = dataManager.create(ContractTerm.class);
            duplicate.setContractLegacyId(777001);
            duplicate.setTermType(ContractTermType.PAYMENT);
            duplicate.setFixedDate(java.time.LocalDate.of(2026, 2, 1));
            assertThatThrownBy(() -> dataManager.save(duplicate)).isInstanceOf(RuntimeException.class);
        } finally {
            dataManager.remove(first);
        }
    }
}
