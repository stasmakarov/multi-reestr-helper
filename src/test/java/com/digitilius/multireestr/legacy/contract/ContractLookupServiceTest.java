package com.digitilius.multireestr.legacy.contract;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class ContractLookupServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ContractLookupService contractLookupService;

    @Test
    void findsContractsOfGoldenNotices3_17_40() {
        assertThat(contractLookupService.findById(30).orElseThrow().getPurchaseNumber())
                .isEqualTo("0173100017026000015");
        assertThat(contractLookupService.findById(19).orElseThrow().getPurchaseNumber())
                .isEqualTo("0173100017026000001");
        assertThat(contractLookupService.findById(3).orElseThrow().getPurchaseNumber())
                .isEqualTo("0173100017024000008");
    }

    @Test
    void notice38HasNoContractByPurchaseNumber() {
        List<ContractDto> found = contractLookupService.search("0173100017024000010");
        assertThat(found).isEmpty();
    }

    @Test
    void searchByContractNumber() {
        List<ContractDto> found = contractLookupService.search("0173100017026000015");
        assertThat(found).extracting(ContractDto::getId).contains(30);
    }
}
