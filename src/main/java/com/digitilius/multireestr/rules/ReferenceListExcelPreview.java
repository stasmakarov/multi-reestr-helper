package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceListItem;

import java.util.List;

public record ReferenceListExcelPreview(
        int rowCount,
        int itemCount,
        int existingCount,
        List<ReferenceListItem> items
) {
}
