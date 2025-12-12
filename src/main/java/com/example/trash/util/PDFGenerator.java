package com.example.trash.util;

import com.example.trash.model.*;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    // Цветовая схема (зеленая тематика лаборатории)
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(73, 140, 81);    // #498C51
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(245, 248, 246);       // #F5F8F6
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(230, 230, 230);   // #E6E6E6

    // Шрифты с поддержкой кириллицы
    private static PdfFont regularFont;
    private static PdfFont boldFont;
    private static PdfFont headerFont;

    static {
        try {
            // Определяем пути к обычному и жирному шрифту
            String regularPath;
            String boldPath;

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows")) {
                regularPath = "C:/Windows/Fonts/arial.ttf";
                boldPath = "C:/Windows/Fonts/arialbd.ttf";
            } else if (os.contains("mac")) {
                regularPath = "/System/Library/Fonts/Arial.ttf";
                boldPath = "/System/Library/Fonts/Arial Bold.ttf";
            } else {
                regularPath = "/usr/share/fonts/truetype/freefont/FreeSans.ttf";
                boldPath = "/usr/share/fonts/truetype/freefont/FreeSansBold.ttf";
            }

            regularFont = PdfFontFactory.createFont(regularPath, PdfEncodings.IDENTITY_H);
            boldFont = PdfFontFactory.createFont(boldPath, PdfEncodings.IDENTITY_H);
            headerFont = boldFont;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                regularFont = PdfFontFactory.createFont();
                boldFont = PdfFontFactory.createFont();
                headerFont = boldFont;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static File generateOrderPDF(Order order, Client client, List<Service> services, Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить заказ как PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF файлы", "*.pdf"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        String defaultFileName = String.format("Заказ_%d_от_%s.pdf",
                order.getOrderNumber(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")));
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            generateProfessionalPDF(file, order, client, services);
            return file;
        }
        return null;
    }

    private static void generateProfessionalPDF(File file, Order order, Client client, List<Service> services) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(file));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // Настраиваем отступы (увеличиваем для A4)
        document.setMargins(40, 35, 40, 35);

        // 1. ШАПКА ДОКУМЕНТА
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 60, 20}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Логотип (левая колонка)
        Cell logoCell = new Cell();
        logoCell.setBorder(null);
        logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        try {
            URL logoUrl = PDFGenerator.class.getResource("/import/photo/Логотип.png");
            if (logoUrl != null) {
                Image logo = new Image(ImageDataFactory.create(logoUrl))
                        .setWidth(70)
                        .setHeight(70)
                        .setAutoScale(true);
                logoCell.add(logo);
            } else {
                // Запасной текст если лого нет
                Paragraph logoText = new Paragraph("Лого")
                        .setFont(regularFont)
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY);
                logoCell.add(logoText);
            }
        } catch (Exception e) {
            // Если лого нет, оставляем пустым
        }

        // Центральная колонка с заголовком
        Cell titleCell = new Cell();
        titleCell.setBorder(null);
        titleCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        titleCell.setTextAlignment(TextAlignment.CENTER);

        Paragraph title = new Paragraph("ЗАКАЗ НА ОКАЗАНИЕ УСЛУГ")
                .setFont(headerFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);

        Paragraph subTitle = new Paragraph("Лаборатория анализа опасных отходов")
                .setFont(regularFont)
                .setFontSize(11)
                .setFontColor(ColorConstants.GRAY);

        titleCell.add(title).add(subTitle);

        // Правая колонка с номером и датой
        Cell infoCell = new Cell();
        infoCell.setBorder(null);
        infoCell.setTextAlignment(TextAlignment.RIGHT);
        infoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);

        Paragraph orderNum = new Paragraph("№ " + order.getOrderNumber())
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(5);

        Paragraph date = new Paragraph("Дата: " + order.getFormattedDate())
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY);

        infoCell.add(orderNum).add(date);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        headerTable.addCell(infoCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));

        // 2. ИНФОРМАЦИЯ О ЗАКАЗЕ (карточка)
        Paragraph sectionTitle1 = new Paragraph("ИНФОРМАЦИЯ О ЗАКАЗЕ")
                .setFont(headerFont)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(10)
                .setMarginBottom(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(sectionTitle1);

        Table orderInfoTable = createCardTable();

        addCardRow(orderInfoTable, "Номер заказа:", String.valueOf(order.getOrderNumber()), boldFont, regularFont);
        addCardRow(orderInfoTable, "Код кейса:", order.getCaseCode() != null ? order.getCaseCode() : "не указан", boldFont, regularFont);
        addCardRow(orderInfoTable, "Дата создания:", order.getFormattedDate(), boldFont, regularFont);
        addCardRow(orderInfoTable, "Статус:", order.getStatus(), boldFont, regularFont);

        document.add(orderInfoTable);
        document.add(new Paragraph("\n"));

        // 3. ИНФОРМАЦИЯ О КЛИЕНТЕ (карточка)
        Paragraph sectionTitle2 = new Paragraph("ИНФОРМАЦИЯ О КЛИЕНТЕ")
                .setFont(headerFont)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(10)
                .setMarginBottom(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(sectionTitle2);

        Table clientInfoTable = createCardTable();

        addCardRow(clientInfoTable, "ФИО:", client.getFio(), boldFont, regularFont);

        if (client.getBirthDate() != null) {
            addCardRow(clientInfoTable, "Дата рождения:",
                    client.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    boldFont, regularFont);
        }

        addCardRow(clientInfoTable, "Телефон:", client.getPhone(), boldFont, regularFont);

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            addCardRow(clientInfoTable, "Email:", client.getEmail(), boldFont, regularFont);
        }

        if (client.getPassportSeries() != null && client.getPassportNumber() != null) {
            addCardRow(clientInfoTable, "Паспорт:",
                    client.getPassportSeries() + " №" + client.getPassportNumber(),
                    boldFont, regularFont);
        }

        if (client.getCompanyName() != null && !client.getCompanyName().isEmpty()) {
            addCardRow(clientInfoTable, "Компания:", client.getCompanyName(), boldFont, regularFont);
        }



        document.add(clientInfoTable);
        document.add(new Paragraph("\n"));

        // 4. ПЕРЕЧЕНЬ УСЛУГ (таблица)
        Paragraph sectionTitle3 = new Paragraph("ПЕРЕЧЕНЬ УСЛУГ")
                .setFont(headerFont)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(10)
                .setMarginBottom(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(sectionTitle3);

        if (services != null && !services.isEmpty()) {
            // Создаем таблицу с пропорциональными колонками
            Table servicesTable = new Table(UnitValue.createPercentArray(new float[]{5, 55, 15, 25}));
            servicesTable.setWidth(UnitValue.createPercentValue(100));

            // Заголовки таблицы
            String[] headers = {"№", "Наименование услуги", "Код", "Стоимость, руб."};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .setBackgroundColor(LIGHT_BG)
                        .setBorder(new SolidBorder(BORDER_COLOR, 1))
                        .setPadding(8)
                        .add(new Paragraph(header)
                                .setFont(boldFont)
                                .setFontSize(11)
                                .setFontColor(PRIMARY_COLOR)
                                .setTextAlignment(TextAlignment.CENTER));
                servicesTable.addHeaderCell(headerCell);
            }

            // Данные услуг
            double total = 0;
            int index = 1;

            for (Service service : services) {
                // Номер
                servicesTable.addCell(createTableCell(String.valueOf(index), regularFont, 8, TextAlignment.CENTER));

                // Наименование
                servicesTable.addCell(createTableCell(service.getName(), regularFont, 8, TextAlignment.LEFT));

                // Код
                String code = service.getCode() != null && !service.getCode().isEmpty() ? service.getCode() : "—";
                servicesTable.addCell(createTableCell(code, regularFont, 8, TextAlignment.CENTER));

                // Стоимость
                String cost = String.format("%,.2f", service.getCost());
                servicesTable.addCell(createTableCell(cost, regularFont, 8, TextAlignment.RIGHT));

                total += service.getCost();
                index++;
            }

            // Итоговая строка
            Cell totalLabelCell = new Cell(1, 3)
                    .setBorder(new SolidBorder(BORDER_COLOR, 1))
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(LIGHT_BG)
                    .add(new Paragraph("ИТОГО:")
                            .setFont(boldFont)
                            .setFontSize(12)
                            .setFontColor(PRIMARY_COLOR));

            Cell totalValueCell = new Cell()
                    .setBorder(new SolidBorder(BORDER_COLOR, 1))
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(LIGHT_BG)
                    .add(new Paragraph(String.format("%,.2f руб.", total))
                            .setFont(boldFont)
                            .setFontSize(12)
                            .setFontColor(PRIMARY_COLOR));

            servicesTable.addCell(totalLabelCell);
            servicesTable.addCell(totalValueCell);

            document.add(servicesTable);
        } else {
            Paragraph noServices = new Paragraph("Услуги не указаны")
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(30);
            document.add(noServices);
        }

        document.add(new Paragraph("\n\n"));

        // 5. ПОДПИСИ И ПРИМЕЧАНИЯ
        Paragraph sectionTitle4 = new Paragraph("ПОДПИСИ СТОРОН")
                .setFont(headerFont)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(10)
                .setMarginBottom(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(sectionTitle4);

        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        footerTable.setWidth(UnitValue.createPercentValue(100));

        // Исполнитель
        Cell executorCell = new Cell();
        executorCell.setBorder(null);
        executorCell.setPaddingTop(20);
        executorCell.setTextAlignment(TextAlignment.CENTER);

        Paragraph executorLabel = new Paragraph("Исполнитель:")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(25);

        Paragraph executorSign = new Paragraph("_________________________")
                .setFont(regularFont)
                .setFontSize(11)
                .setMarginBottom(5);

        Paragraph executorName = new Paragraph("(подпись, ФИО, должность)")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(5);

        executorCell.add(executorLabel)
                .add(executorSign)
                .add(executorName);

        // Клиент
        Cell clientCell = new Cell();
        clientCell.setBorder(null);
        clientCell.setPaddingTop(20);
        clientCell.setTextAlignment(TextAlignment.CENTER);

        Paragraph clientLabel = new Paragraph("Клиент:")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(25);

        Paragraph clientSign = new Paragraph("_________________________")
                .setFont(regularFont)
                .setFontSize(11)
                .setMarginBottom(5);

        Paragraph clientName = new Paragraph("(подпись, ФИО)")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(5);

        clientCell.add(clientLabel)
                .add(clientSign)
                .add(clientName);

        footerTable.addCell(executorCell);
        footerTable.addCell(clientCell);
        document.add(footerTable);

        document.add(new Paragraph("\n"));

        // 6. ПРИМЕЧАНИЯ
        Paragraph notesTitle = new Paragraph("Примечания:")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginBottom(10);

        Paragraph note1 = new Paragraph("• Документ сформирован автоматически в системе \"Не навреди\"")
                .setFont(regularFont)
                .setFontSize(10)
                .setMarginBottom(5);

        Paragraph note2 = new Paragraph("• Стоимость указана в российских рублях")
                .setFont(regularFont)
                .setFontSize(10)
                .setMarginBottom(5);

        Paragraph note3 = new Paragraph("• Для получения дополнительной информации обращайтесь в службу поддержки")
                .setFont(regularFont)
                .setFontSize(10)
                .setMarginBottom(15);

        Paragraph contactInfo = new Paragraph("📞 +7 (800) 123-45-67 | 📧 lab@nenavredi.ru | 🌐 www.nenavredi.ru")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);

        document.add(notesTitle)
                .add(note1)
                .add(note2)
                .add(note3)
                .add(contactInfo);

        // 7. ФУТЕР (нижний колонтитул)
        Paragraph footer = new Paragraph(
                "Страница 1 из 1 | " +
                        "Дата формирования: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " | " +
                        "Система \"Не навреди\"")
                .setFont(regularFont)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setPaddingTop(10)
                .setBorderTop(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        document.add(footer);

        document.close();

        System.out.println("✅ PDF документ успешно создан: " + file.getAbsolutePath());
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Создает стилизованную карточку для информации
     */
    private static Table createCardTable() {
        Table table = new Table(1);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(15);
        table.setBorder(new SolidBorder(BORDER_COLOR, 1));
        table.setBackgroundColor(LIGHT_BG);
        return table;
    }

    /**
     * Добавляет строку в карточку
     */
    private static void addCardRow(Table table, String label, String value,
                                   PdfFont labelFont, PdfFont valueFont) {
        if (value == null || value.trim().isEmpty()) {
            value = "не указано";
        }

        Paragraph row = new Paragraph();

        // Метка (жирный)
        Text labelText = new Text(label + " ")
                .setFont(labelFont)
                .setFontSize(11)
                .setFontColor(PRIMARY_COLOR);

        // Значение
        Text valueText = new Text(value)
                .setFont(valueFont)
                .setFontSize(11)
                .setFontColor(ColorConstants.BLACK);

        row.add(labelText);
        row.add(valueText);

        Cell cell = new Cell()
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(10)
                .add(row);

        table.addCell(cell);
    }

    /**
     * Создает ячейку таблицы со стандартными стилями
     */
    private static Cell createTableCell(String content, PdfFont font, float padding, TextAlignment alignment) {
        Paragraph paragraph = new Paragraph(content)
                .setFont(font)
                .setFontSize(10);

        if (alignment != null) {
            paragraph.setTextAlignment(alignment);
        }

        return new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(padding)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(paragraph);
    }

}