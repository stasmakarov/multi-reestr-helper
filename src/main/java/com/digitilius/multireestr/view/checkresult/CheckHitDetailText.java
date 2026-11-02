package com.digitilius.multireestr.view.checkresult;

import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CheckHitDetailText {

    private static final Set<String> KNOWN_KEYS = Set.of("pos", "id", "char");

    private CheckHitDetailText() {
    }

    public static String describe(String detail, NoticeDto notice, Phrases phrases) {
        if (detail == null || detail.isBlank()) {
            return "";
        }
        Map<String, String> pairs = pairs(detail);
        if (pairs == null) {
            return detail;
        }
        String pos = pairs.get("pos");
        SpecPositionDto item = findItem(notice, parseId(pairs.get("id")), pos);
        String charToken = pairs.get("char");
        KtruCharacteristicDto characteristic = findCharacteristic(item, charToken);
        String position = positionText(pos, item, phrases);
        String characteristicText = characteristicText(charToken, characteristic, phrases);
        if (characteristicText.isEmpty()) {
            return position.isEmpty() ? detail : position;
        }
        if (position.isEmpty()) {
            return characteristicText;
        }
        return phrases.join().formatted(characteristicText, position);
    }

    private static String positionText(String pos, SpecPositionDto item, Phrases phrases) {
        String number = pos == null ? "" : pos.trim();
        if (number.isEmpty() && item != null && item.getPos() != null) {
            number = item.getPos().toString();
        }
        if (number.isEmpty()) {
            return "";
        }
        String name = item == null ? "" : trim(item.getName());
        if (name.isEmpty()) {
            return phrases.position().formatted(number);
        }
        return phrases.positionNamed().formatted(number, name);
    }

    private static String characteristicText(
            String charToken,
            KtruCharacteristicDto characteristic,
            Phrases phrases
    ) {
        if ((charToken == null || charToken.isBlank()) && characteristic == null) {
            return "";
        }
        String name = characteristic == null ? "" : trim(characteristic.getName());
        String value = characteristic == null ? "" : trim(characteristic.getValue());
        if (!name.isEmpty() && !value.isEmpty()) {
            return phrases.charValue().formatted(name, value);
        }
        if (!name.isEmpty()) {
            return phrases.charName().formatted(name);
        }
        String token = charToken == null ? "" : charToken.trim();
        if (token.isEmpty() || "null".equals(token)) {
            return "";
        }
        return phrases.charNumber().formatted(token);
    }

    private static Map<String, String> pairs(String detail) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String part : detail.split(";", -1)) {
            if (part.isBlank()) {
                continue;
            }
            int eq = part.indexOf('=');
            if (eq <= 0) {
                return null;
            }
            String key = part.substring(0, eq).trim();
            if (!KNOWN_KEYS.contains(key)) {
                return null;
            }
            map.put(key, part.substring(eq + 1).trim());
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    private static SpecPositionDto findItem(NoticeDto notice, Integer id, String pos) {
        if (notice == null || notice.getItems() == null) {
            return null;
        }
        if (id != null) {
            for (SpecPositionDto item : notice.getItems()) {
                if (item != null && id.equals(item.getId())) {
                    return item;
                }
            }
        }
        String number = pos == null ? "" : pos.trim();
        if (number.isEmpty()) {
            return null;
        }
        for (SpecPositionDto item : notice.getItems()) {
            if (item != null && item.getPos() != null && number.equals(item.getPos().toString())) {
                return item;
            }
        }
        return null;
    }

    private static KtruCharacteristicDto findCharacteristic(SpecPositionDto item, String charToken) {
        if (item == null || item.getCharacteristics() == null) {
            return null;
        }
        Integer id = parseId(charToken);
        if (id == null) {
            return null;
        }
        for (KtruCharacteristicDto characteristic : item.getCharacteristics()) {
            if (characteristic != null && id.equals(characteristic.getId())) {
                return characteristic;
            }
        }
        return null;
    }

    private static Integer parseId(String raw) {
        if (raw == null || raw.isBlank() || "null".equals(raw.trim())) {
            return null;
        }
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public record Phrases(
            String positionNamed,
            String position,
            String charValue,
            String charName,
            String charNumber,
            String join
    ) {
    }
}
