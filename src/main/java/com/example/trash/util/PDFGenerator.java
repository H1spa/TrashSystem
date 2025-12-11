package com.example.trash.util;

import com.example.trash.model.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    public static void generateOrderPDF(Order order, Client client, List<Service> services) throws Exception {
        String fileName = "order_" + order.getId() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Заголовок
        Paragraph title = new Paragraph("ЗАКАЗ НА ОКАЗАНИЕ УСЛУГ")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16);
        document.add(title);

        // Используем ID вместо номера заказа, если метода getOrderNumber нет
        document.add(new Paragraph("№ " + order.getOrderNumber())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14));

        // Форматируем дату вручную, если метода getFormattedDate нет
        String formattedDate = order.getCreatedAt() != null
                ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        document.add(new Paragraph("Дата: " + formattedDate)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Информация о клиенте
        Paragraph clientTitle = new Paragraph("ДАННЫЕ КЛИЕНТА:")
                .setBold()
                .setFontSize(12);
        document.add(clientTitle);

        Table clientTable = new Table(2);
        clientTable.setWidth(UnitValue.createPercentValue(100));

        addTableCell(clientTable, "ФИО:", client.getFio());
        addTableCell(clientTable, "Телефон:", client.getPhone());

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            addTableCell(clientTable, "Email:", client.getEmail());
        }

        if (client.getCompanyName() != null && !client.getCompanyName().isEmpty()) {
            addTableCell(clientTable, "Компания:", client.getCompanyName());
        }

        if (client.getPassportSeries() != null && client.getPassportNumber() != null) {
            addTableCell(clientTable, "Паспорт:",
                    client.getPassportSeries() + " " + client.getPassportNumber());
        }

        document.add(clientTable);
        document.add(new Paragraph("\n"));

        // Информация о кейсе
        Paragraph caseTitle = new Paragraph("ИНФОРМАЦИЯ О КЕЙСЕ:")
                .setBold()
                .setFontSize(12);
        document.add(caseTitle);

        // Используем getCaseCode() вместо getCase()
        String caseCode = order.getCaseCode();
        document.add(new Paragraph("Код кейса: " + caseCode)
                .setFontSize(11));
        document.add(new Paragraph("\n"));

        // Услуги
        Paragraph servicesTitle = new Paragraph("ПЕРЕЧЕНЬ УСЛУГ:")
                .setBold()
                .setFontSize(12);
        document.add(servicesTitle);

        Table servicesTable = new Table(4);
        servicesTable.setWidth(UnitValue.createPercentValue(100));

        // Заголовки таблицы
        servicesTable.addHeaderCell(new Cell().add(new Paragraph("№").setBold()));
        servicesTable.addHeaderCell(new Cell().add(new Paragraph("Наименование услуги").setBold()));
        servicesTable.addHeaderCell(new Cell().add(new Paragraph("Код").setBold()));
        servicesTable.addHeaderCell(new Cell().add(new Paragraph("Стоимость, руб.").setBold()));

        // Данные услуг
        double total = 0;
        int i = 1;
        for (Service service : services) {
            servicesTable.addCell(new Cell().add(new Paragraph(String.valueOf(i))));
            servicesTable.addCell(new Cell().add(new Paragraph(service.getName())));
            servicesTable.addCell(new Cell().add(new Paragraph(service.getCode() != null ? service.getCode() : "")));
            servicesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", service.getCost()))));
            total += service.getCost();
            i++;
        }

        document.add(servicesTable);
        document.add(new Paragraph("\n"));

        // Итого
        Paragraph totalParagraph = new Paragraph("ИТОГО К ОПЛАТЕ: " + String.format("%.2f", total) + " руб.")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(totalParagraph);

        document.add(new Paragraph("\n\n"));

        // Подписи
        Table signatureTable = new Table(2);
        signatureTable.setWidth(UnitValue.createPercentValue(100));

        signatureTable.addCell(new Cell().add(new Paragraph("___________________\nИсполнитель"))
                .setTextAlignment(TextAlignment.CENTER));
        signatureTable.addCell(new Cell().add(new Paragraph("___________________\nКлиент"))
                .setTextAlignment(TextAlignment.CENTER));

        document.add(signatureTable);

        // Примечание
        Paragraph note = new Paragraph("\nПримечание: Документ сформирован автоматически. " +
                "Для получения дополнительной информации обратитесь в службу поддержки.")
                .setFontSize(8)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(note);

        document.close();

        System.out.println("PDF документ создан: " + fileName);
    }

    private static void addTableCell(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")));
    }
}