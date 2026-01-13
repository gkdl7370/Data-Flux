package com.flux.batch.reader;

import com.flux.dto.DataPacket;
import org.apache.poi.ss.usermodel.*;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ExcelItemReader implements ItemReader<DataPacket> {
    private Iterator<Row> rowIterator;
    // [핵심] 엑셀의 숫자/날짜 포맷을 문자로 안전하게 변환해주는 도구
    private final DataFormatter dataFormatter = new DataFormatter();

    public ExcelItemReader() {}

    public void setFilePath(String filePath) throws Exception {
        Workbook workbook = WorkbookFactory.create(new File(filePath));
        Sheet sheet = workbook.getSheetAt(0);
        this.rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) rowIterator.next(); // 헤더 스킵
    }

    @Override
    public DataPacket read() {
        if (rowIterator != null && rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // [수정] getStringCellValue() -> dataFormatter.formatCellValue() 사용
            // 셀이 비어있거나 숫자여도 안전하게 String으로 가져옵니다.
            String targetId = dataFormatter.formatCellValue(row.getCell(0));
            String referenceDate = dataFormatter.formatCellValue(row.getCell(1));

            List<Double> values = new ArrayList<>();
            // Payload (데이터 값) 처리 (3번 열부터 26번 열까지)
            for (int i = 3; i < 27; i++) {
                Cell cell = row.getCell(i);
                // 셀이 비어있으면 0.0, 아니면 숫자값 읽기
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    values.add(0.0);
                } else {
                    values.add(cell.getNumericCellValue());
                }
            }

            return DataPacket.builder()
                    .targetId(targetId)
                    .referenceDate(referenceDate)
                    .type("STANDARD")
                    .payload(values)
                    .build();
        }
        return null;
    }
}