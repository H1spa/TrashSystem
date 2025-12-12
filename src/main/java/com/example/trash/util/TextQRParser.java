package com.example.trash.util;

import com.example.trash.model.QRData;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Парсер для текстового формата QR-кодов лаборатории.
 * Обрабатывает многострочный текст с полями вида "Ключ: значение".
 */
public class TextQRParser {

    /**
     * Основной метод парсинга текстового QR-кода
     */
    public static QRData parseTextFormat(String textData) {
        System.out.println("=== ПАРСИНГ ТЕКСТОВОГО QR ===");
        System.out.println("Исходный текст:\n" + textData);

        QRData qrData = new QRData();
        QRData.ClientData clientData = new QRData.ClientData();
        qrData.setClientData(clientData);

        // Инициализируем список услуг пустым списком, а не null
        qrData.setServices(new ArrayList<>());

        if (textData == null || textData.trim().isEmpty()) {
            System.out.println("Внимание: получены пустые данные!");
            return qrData;
        }

        // Разбиваем текст на строки
        String[] lines = textData.split("\n");
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue; // Пропускаем пустые строки
            }

            // Ищем разделитель ":"
            int colonIndex = trimmedLine.indexOf(':');
            if (colonIndex == -1) {
                System.out.println("Строка " + lineNumber + ": не содержит ':' - '" + trimmedLine + "'");
                continue;
            }

            String key = trimmedLine.substring(0, colonIndex).trim();
            String value = trimmedLine.substring(colonIndex + 1).trim();

            System.out.println("Строка " + lineNumber + ": ключ='" + key + "', значение='" + value + "'");

            // Обрабатываем известные ключи (регистронезависимо)
            switch (key.toLowerCase()) {
                case "фио":
                case "ф.и.о":
                case "ф.и.о.":
                case "fio":
                    clientData.setFio(value);
                    System.out.println("  -> Установлено ФИО: " + value);
                    break;

                case "телефон":
                case "тел":
                case "phone":
                case "т.":
                    clientData.setPhone(value);
                    System.out.println("  -> Установлен телефон: " + value);
                    break;

                case "email":
                case "e-mail":
                case "почта":
                    clientData.setEmail(value);
                    System.out.println("  -> Установлен email: " + value);
                    break;

                case "паспорт":
                case "паспортные данные":
                case "passport":
                    // Можно добавить дополнительную обработку серии/номера
                    clientData.setPassport(value);
                    System.out.println("  -> Установлены паспортные данные: " + value);
                    break;

                case "код кейса":
                case "кейс":
                case "код":
                case "case":
                case "case code":
                case "casecode":
                    qrData.setCaseCode(value);
                    System.out.println("  -> Установлен код кейса: " + value);
                    break;

                case "услуги":
                case "service":
                case "services":
                case "коды услуг":
                    parseServices(qrData, value);
                    System.out.println("  -> Установлены услуги: " + value);
                    break;

                case "компания":
                case "организация":
                case "company":
                case "enterprise":
                    clientData.setCompanyName(value);
                    System.out.println("  -> Установлено название компании: " + value);
                    break;

                default:
                    System.out.println("  -> Неизвестный ключ: " + key);
                    // Попробуем угадать по содержимому
                    if (key.toLowerCase().contains("фио") || key.toLowerCase().contains("fio")) {
                        clientData.setFio(value);
                    } else if (key.toLowerCase().contains("тел")) {
                        clientData.setPhone(value);
                    }
                    break;
            }
        }

        // Логируем результат парсинга
        System.out.println("\n=== РЕЗУЛЬТАТ ПАРСИНГА ===");
        System.out.println("Код кейса: " + qrData.getCaseCode());
        System.out.println("ФИО клиента: " + (clientData.getFio() != null ? clientData.getFio() : "не указано"));
        System.out.println("Телефон: " + (clientData.getPhone() != null ? clientData.getPhone() : "не указано"));
        System.out.println("Email: " + (clientData.getEmail() != null ? clientData.getEmail() : "не указано"));
        System.out.println("Услуги: " + (qrData.getServices() != null ? qrData.getServices() : "не указаны"));
        System.out.println("=================================\n");

        return qrData;
    }

    /**
     * Парсинг списка услуг (могут быть через запятую, точку с запятой или пробел)
     */
    private static void parseServices(QRData qrData, String servicesString) {
        if (servicesString == null || servicesString.trim().isEmpty()) {
            return;
        }

        // Разделяем строку разными разделителями
        String[] servicesArray = servicesString.split("[,;\\s]+");
        List<String> servicesList = new ArrayList<>();

        for (String service : servicesArray) {
            String trimmedService = service.trim();
            if (!trimmedService.isEmpty()) {
                servicesList.add(trimmedService);
            }
        }

        qrData.setServices(servicesList);
    }
}