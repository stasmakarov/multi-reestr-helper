package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.security.MultiReestrSpecifics;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReferenceListExcelService {

    public static final String[] HEADERS = {
            "value", "prefixMatch", "excluded", "groupCode", "caption", "validFrom", "validTo"
    };

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter RU = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DataFormatter FORMATTER = new DataFormatter();

    private final Metadata metadata;
    private final DataManager dataManager;
    private final AccessManager accessManager;

    @PersistenceContext
    private EntityManager entityManager;

    public ReferenceListExcelService(Metadata metadata, DataManager dataManager, AccessManager accessManager) {
        this.metadata = metadata;
        this.dataManager = dataManager;
        this.accessManager = accessManager;
    }

    public ReferenceListExcelPreview preview(ReferenceList list, InputStream excel) {
        List<ReferenceListItem> items = parse(excel);
        int existing = list.getId() == null ? 0 : countExisting(list.getId());
        return new ReferenceListExcelPreview(items.size(), items.size(), existing, items);
    }

    @Transactional
    public void replaceItems(ReferenceList list, ReferenceListExcelPreview preview) {
        requireImportPermission();
        entityManager.createQuery("delete from mtr_ReferenceListItem e where e.list = :list")
                .setParameter("list", list)
                .executeUpdate();
        entityManager.flush();
        SaveContext saveContext = new SaveContext();
        for (ReferenceListItem item : preview.items()) {
            ReferenceListItem entity = metadata.create(ReferenceListItem.class);
            entity.setList(list);
            entity.setValue(item.getValue());
            entity.setPrefixMatch(item.getPrefixMatch());
            entity.setExcluded(item.getExcluded());
            entity.setGroupCode(item.getGroupCode());
            entity.setCaption(item.getCaption());
            entity.setValidFrom(item.getValidFrom());
            entity.setValidTo(item.getValidTo());
            saveContext.saving(entity);
        }
        dataManager.save(saveContext);
    }

    public byte[] export(ReferenceList list) {
        List<ReferenceListItem> items = list.getItems() == null ? List.of() : list.getItems();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("items");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            int rowNum = 1;
            for (ReferenceListItem item : items) {
                Row row = sheet.createRow(rowNum++);
                set(row, 0, item.getValue());
                set(row, 1, bool(item.getPrefixMatch()));
                set(row, 2, bool(item.getExcluded()));
                set(row, 3, item.getGroupCode());
                set(row, 4, item.getCaption());
                set(row, 5, date(item.getValidFrom()));
                set(row, 6, date(item.getValidTo()));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<ReferenceListItem> parse(InputStream excel) {
        try (Workbook workbook = new XSSFWorkbook(excel)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("Пустой файл Excel");
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columns = headerMap(headerRow);
            if (!columns.containsKey("value")) {
                throw new IllegalArgumentException("Нет колонки value");
            }
            List<ReferenceListItem> items = new ArrayList<>();
            for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String rawValue = cell(row, columns.get("value"));
                if (rawValue.isBlank()) {
                    continue;
                }
                Boolean prefixMatch = parseBool(cell(row, columns.get("prefixmatch")));
                Boolean excluded = parseBool(cell(row, columns.get("excluded")));
                String groupCode = cell(row, columns.get("groupcode"));
                String caption = cell(row, columns.get("caption"));
                LocalDate validFrom = parseDate(cell(row, columns.get("validfrom")));
                LocalDate validTo = parseDate(cell(row, columns.get("validto")));
                for (String value : splitValues(rawValue)) {
                    ReferenceListItem item = new ReferenceListItem();
                    item.setValue(value);
                    item.setPrefixMatch(prefixMatch);
                    item.setExcluded(excluded);
                    item.setGroupCode(blankToNull(groupCode));
                    item.setCaption(blankToNull(caption));
                    item.setValidFrom(validFrom);
                    item.setValidTo(validTo);
                    items.add(item);
                }
            }
            return items;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<String> splitValues(String rawValue) {
        String[] parts = rawValue.split(",");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String value = part.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private void requireImportPermission() {
        SpecificOperationAccessContext context =
                new SpecificOperationAccessContext(MultiReestrSpecifics.REFERENCE_LIST_IMPORT);
        accessManager.applyRegisteredConstraints(context);
        if (!context.isPermitted()) {
            throw new IllegalStateException("Нет права на импорт справочного списка");
        }
    }

    private int countExisting(java.util.UUID listId) {
        Long count = dataManager.loadValue(
                        "select count(e) from mtr_ReferenceListItem e where e.list.id = :id", Long.class)
                .parameter("id", listId)
                .one();
        return count.intValue();
    }

    private static Map<String, Integer> headerMap(Row headerRow) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        if (headerRow == null) {
            return columns;
        }
        for (Cell cell : headerRow) {
            String name = FORMATTER.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
            name = switch (name) {
                case "значение" -> "value";
                case "префикс" -> "prefixmatch";
                case "исключение" -> "excluded";
                case "пункт" -> "groupcode";
                case "наименование", "примечание" -> "caption";
                case "с" -> "validfrom";
                case "по" -> "validto";
                default -> name;
            };
            if (!name.isEmpty()) {
                columns.put(name, cell.getColumnIndex());
            }
        }
        return columns;
    }

    private static String cell(Row row, Integer index) {
        if (index == null) {
            return "";
        }
        Cell cell = row.getCell(index);
        return cell == null ? "" : FORMATTER.formatCellValue(cell).trim();
    }

    private static Boolean parseBool(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "1", "true", "yes", "да" -> true;
            case "0", "false", "no", "нет" -> false;
            default -> throw new IllegalArgumentException("Некорректный признак: " + raw);
        };
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw, ISO);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(raw, RU);
        }
    }

    private static void set(Row row, int column, String value) {
        if (value != null) {
            row.createCell(column).setCellValue(value);
        }
    }

    private static String bool(Boolean value) {
        if (value == null) {
            return "";
        }
        return value ? "true" : "false";
    }

    private static String date(LocalDate value) {
        return value == null ? "" : value.format(ISO);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
