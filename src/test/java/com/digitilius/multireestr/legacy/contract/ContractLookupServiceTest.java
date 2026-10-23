package com.digitilius.multireestr.legacy.contract;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
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
@RequireDocker
class ContractLookupServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ContractLookupService contractLookupService;

    @Test
    void findsGoldenContractsByIdNumberAndPurchaseNumber() {
        assertThat(contractLookupService.findById(30)).get().extracting(ContractDto::getId).isEqualTo(30);
        assertThat(contractLookupService.findById(19)).get().extracting(ContractDto::getId).isEqualTo(19);
        assertThat(contractLookupService.findById(3)).get().extracting(ContractDto::getId).isEqualTo(3);

        List<ContractDto> byNotice = contractLookupService.search("0173100017026000015");
        assertThat(byNotice).extracting(ContractDto::getId).contains(30);

        List<ContractDto> byIdText = contractLookupService.search("30");
        assertThat(byIdText).extracting(ContractDto::getId).contains(30);

        assertThat(contractLookupService.findById(38)).isEmpty();
    }
}
