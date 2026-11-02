package com.digitilius.multireestr.view.checkresult;

import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckHitDetailTextTest {

    private static final CheckHitDetailText.Phrases RU = new CheckHitDetailText.Phrases(
            "Позиция %s «%s»",
            "Позиция %s",
            "%s: %s",
            "%s",
            "Характеристика № %s",
            "%s. %s"
    );

    @Test
    void namesPositionAndCharacteristic() {
        NoticeDto notice = notice(position(1, 3, "Папка картонная", characteristic(6, "Цвет", "белый"),
                characteristic(7, "Ширина корешка", "25")));

        assertThat(CheckHitDetailText.describe("pos=1;id=3;char=6", notice, RU))
                .isEqualTo("Цвет: белый. Позиция 1 «Папка картонная»");
        assertThat(CheckHitDetailText.describe("pos=1;id=3;char=7", notice, RU))
                .isEqualTo("Ширина корешка: 25. Позиция 1 «Папка картонная»");
    }

    @Test
    void positionWithoutCharacteristicUsesItemName() {
        NoticeDto notice = notice(position(1, 10, "Батареи"));

        assertThat(CheckHitDetailText.describe("pos=1;id=10", notice, RU))
                .isEqualTo("Позиция 1 «Батареи»");
    }

    @Test
    void fallsBackWhenNoticeHasNoName() {
        assertThat(CheckHitDetailText.describe("pos=1;id=3;char=6", null, RU))
                .isEqualTo("Характеристика № 6. Позиция 1");
        assertThat(CheckHitDetailText.describe("pos=2;id=11", null, RU))
                .isEqualTo("Позиция 2");
    }

    @Test
    void leavesUnknownDetailUnchanged() {
        assertThat(CheckHitDetailText.describe("term=DELIVERY", null, RU)).isEqualTo("term=DELIVERY");
        assertThat(CheckHitDetailText.describe("", null, RU)).isEmpty();
    }

    private static NoticeDto notice(SpecPositionDto item) {
        NoticeDto notice = new NoticeDto();
        notice.setItems(List.of(item));
        return notice;
    }

    private static SpecPositionDto position(int pos, int id, String name, KtruCharacteristicDto... characteristics) {
        SpecPositionDto item = new SpecPositionDto();
        item.setPos(pos);
        item.setId(id);
        item.setName(name);
        item.setCharacteristics(List.of(characteristics));
        return item;
    }

    private static KtruCharacteristicDto characteristic(int id, String name, String value) {
        KtruCharacteristicDto characteristic = new KtruCharacteristicDto();
        characteristic.setId(id);
        characteristic.setName(name);
        characteristic.setValue(value);
        return characteristic;
    }
}
