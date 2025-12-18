package com.example.trash.util;

import com.example.trash.dao.ServiceDAO;
import com.example.trash.model.Service;

public class ProcessTypeMapper {

    public static String getProcessTypeFromServiceCode(String serviceCode) {
        // Маппинг кодов услуг на типы процессов утилизации
        switch(serviceCode) {
            case "619": // Утилизация батареек
            case "311": // Утилизация аккумуляторов
            case "543": // Утилизация промышленных отходов
            case "229": // Выезд курьера
            case "415": // Хранение отходов
                return "PROCESS_1"; // Измельчение + сжигание

            case "258": // Утилизация лекарственных препаратов
            case "176": // Утилизация химических препаратов
            case "501": // Утилизация медицинских отходов
            case "323": // Эко-паспорт
            case "855": // Формирование карточки предприятия
            case "346": // Формирование карточки частного лица
                return "PROCESS_2"; // Ингибитор + снижение концентрации

            case "548": // Утилизация ртутных градусников
            case "557": // Утилизация отходов химической промышленности
            case "836": // Независимая экспертиза
            case "659": // Исследования воздушной среды
            case "797": // Исследования твердых отходов
            case "287": // Исследования жидких отходов
                return "PROCESS_3"; // Химические утилизаторы + исследования

            default:
                return "PROCESS_1"; // По умолчанию
        }
    }

    public static String getProcessDescription(String processType) {
        switch(processType) {
            case "PROCESS_1":
                return "Измельчение на бисерной мельнице с дальнейшим сжиганием в инсинераторе";
            case "PROCESS_2":
                return "Добавление ингибитора и снижение концентрации опасных веществ";
            case "PROCESS_3":
                return "Закачка химических утилизаторов с исследованиями остаточных веществ";
            default:
                return "Стандартный процесс утилизации";
        }
    }

    public static String getProcessSteps(String processType) {
        switch(processType) {
            case "PROCESS_1":
                return "1. Загрузка материала\n" +
                        "2. Измельчение на бисерной мельнице\n" +
                        "3. Розлив по емкостям\n" +
                        "4. Сжигание в инсинераторе\n" +
                        "5. Замер показателей";
            case "PROCESS_2":
                return "1. Загрузка материала\n" +
                        "2. Добавление ингибитора\n" +
                        "3. Мониторинг снижения концентрации\n" +
                        "4. Слив в емкости\n" +
                        "5. Контроль качества";
            case "PROCESS_3":
                return "1. Загрузка материала\n" +
                        "2. Закачка химических утилизаторов\n" +
                        "3. Исследование остаточных веществ\n" +
                        "4. Розлив по емкостям\n" +
                        "5. Анализ результатов";
            default:
                return "Стандартные этапы процесса утилизации";
        }
    }

    public static double getExpectedDuration(String processType) {
        // Возвращает ожидаемую продолжительность в секундах
        switch(processType) {
            case "PROCESS_1":
                return 30000; // 30 секунд
            case "PROCESS_2":
                return 25000; // 25 секунд
            case "PROCESS_3":
                return 35000; // 35 секунд
            default:
                return 30000; // 30 секунд по умолчанию
        }
    }
}