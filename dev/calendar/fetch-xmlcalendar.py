#!/usr/bin/env python3
"""Download xmlcalendar.ru XML and write Liquibase CSV for MTR_CALENDAR_DAY."""

from __future__ import annotations

import argparse
import csv
import sys
import urllib.request
import uuid
import xml.etree.ElementTree as ET
from datetime import date
from pathlib import Path

DEFAULT_YEARS = (2024, 2025, 2026, 2027)
URL_TEMPLATE = "https://xmlcalendar.ru/data/ru/{year}/calendar.xml"
USER_AGENT = "multi-reestr-calendar"
CREATED_BY = "system"
CREATED_DATE = "2026-01-01T00:00:00"
SOURCE = "XMLCALENDAR"
UUID_NAMESPACE = uuid.UUID("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
SHORTENED_MARK = "Предпраздничный день"
WORKING_TRANSFER_MARK = "Рабочий день (перенос)"
TRANSFER_FROM_PREFIX = "Перенос с "
TRANSFER_TO_PREFIX = "Перенос на "


def fetch(year: int) -> bytes:
    request = urllib.request.Request(
        URL_TEMPLATE.format(year=year),
        headers={"User-Agent": USER_AGENT},
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        if response.status != 200:
            raise RuntimeError(f"xmlcalendar.ru HTTP {response.status} for {year}")
        return response.read()


def parse(xml_bytes: bytes, expected_year: int) -> list[tuple[date, str, str]]:
    root = ET.fromstring(xml_bytes)
    if root.tag != "calendar":
        raise ValueError("Корневой элемент XML должен быть <calendar>")
    year = int(root.attrib["year"])
    if year != expected_year:
        raise ValueError(f"Год в XML {year} не совпадает с запрошенным {expected_year}")

    holidays_el = root.find("holidays")
    holidays = {
        holiday.attrib["id"]: holiday.attrib["title"]
        for holiday in (list(holidays_el) if holidays_el is not None else [])
    }
    days_el = root.find("days")
    days = list(days_el) if days_el is not None else []
    reverse_from: dict[str, list[str]] = {}
    for day in days:
        from_date = day.attrib.get("f") or None
        if from_date:
            reverse_from.setdefault(from_date, []).append(day.attrib["d"])

    parsed: list[tuple[date, str, str]] = []
    for day in days:
        xml_date = day.attrib["d"]
        month, day_of_month = (int(part) for part in xml_date.split("."))
        parsed_date = date(year, month, day_of_month)
        day_type_code = day.attrib["t"]
        holiday_id = day.attrib.get("h") or None
        from_date = day.attrib.get("f") or None
        parsed.append(
            (
                parsed_date,
                *map_day(day_type_code, xml_date, holiday_id, from_date, holidays, reverse_from),
            )
        )
    return parsed


def map_day(
    day_type_code: str,
    xml_date: str,
    holiday_id: str | None,
    from_date: str | None,
    holidays: dict[str, str],
    reverse_from: dict[str, list[str]],
) -> tuple[str, str]:
    if day_type_code == "1":
        return (
            "HOLIDAY" if holiday_id else "WEEKEND",
            named_or_transfer_mark(holiday_id, from_date, holidays),
        )
    if day_type_code == "2":
        mark = named_or_transfer_mark(holiday_id, from_date, holidays)
        return "SHORTENED", mark if mark else SHORTENED_MARK
    if day_type_code == "3":
        destinations = reverse_from.get(xml_date) or []
        if destinations:
            return "WORKING_TRANSFER", TRANSFER_TO_PREFIX + ", ".join(
                format_mark_date(item) for item in destinations
            )
        return "WORKING_TRANSFER", WORKING_TRANSFER_MARK
    raise ValueError(f"Неизвестный тип дня xmlcalendar t={day_type_code} для {xml_date}")


def named_or_transfer_mark(
    holiday_id: str | None,
    from_date: str | None,
    holidays: dict[str, str],
) -> str:
    if holiday_id:
        title = holidays.get(holiday_id)
        if not title:
            raise ValueError(f"В XML нет праздника h={holiday_id}")
        return title
    if from_date:
        return TRANSFER_FROM_PREFIX + format_mark_date(from_date)
    return ""


def format_mark_date(mm_dd: str) -> str:
    month, day_of_month = (int(part) for part in mm_dd.split("."))
    return f"{day_of_month:02d}.{month:02d}"


def row_id(parsed_date: date) -> str:
    return str(uuid.uuid5(UUID_NAMESPACE, f"mtr-calendar-day:{parsed_date.isoformat()}"))


def write_csv(path: Path, rows: list[tuple[date, str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(
            [
                "ID",
                "DATE_",
                "YEAR_",
                "DAY_TYPE",
                "SOURCE",
                "MARK",
                "CREATED_BY",
                "CREATED_DATE",
                "LAST_MODIFIED_BY",
                "LAST_MODIFIED_DATE",
            ]
        )
        for parsed_date, day_type, mark in rows:
            writer.writerow(
                [
                    row_id(parsed_date),
                    parsed_date.isoformat(),
                    parsed_date.year,
                    day_type,
                    SOURCE,
                    mark,
                    CREATED_BY,
                    CREATED_DATE,
                    "",
                    "",
                ]
            )


def main(argv: list[str]) -> int:
    repo_root = Path(__file__).resolve().parents[2]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "years",
        nargs="*",
        type=int,
        default=list(DEFAULT_YEARS),
        help="годы (по умолчанию 2024 2025 2026 2027)",
    )
    parser.add_argument(
        "--out",
        type=Path,
        default=repo_root
        / "src/main/resources/com/digitilius/multireestr/liquibase/data/mtr-calendar-day.csv",
    )
    args = parser.parse_args(argv)

    rows: list[tuple[date, str, str]] = []
    for year in args.years:
        xml_bytes = fetch(year)
        rows.extend(parse(xml_bytes, year))
    rows.sort(key=lambda item: item[0])
    write_csv(args.out, rows)
    print(f"Записано {len(rows)} строк в {args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
