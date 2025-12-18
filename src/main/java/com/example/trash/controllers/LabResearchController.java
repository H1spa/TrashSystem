package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import com.example.trash.dao.ExperimentDAO;
import com.example.trash.model.Experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LabResearchController extends BaseLabController {

    @FXML
    private TableView<Experiment> experimentsTable;
    @FXML
    private Button logoutButton;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button reportsButton; // Добавим новую кнопку для отчетов
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        welcomeLabel.setText("Добро пожаловать, Лаборант-исследователь!");

        // Загружаем эксперименты
        loadExperiments();
    }
    @FXML
    private void handleUtilizers() {
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/utilizer_dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            currentStage.setScene(scene);
            currentStage.setTitle("Панель управления утилизаторами");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть панель утилизаторов: " + e.getMessage());
        }
    }
    private void loadExperiments() {
        try {
            List<Experiment> experiments = ExperimentDAO.getAllExperiments();
            experimentsTable.getItems().setAll(experiments);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить эксперименты");
        }
    }

    @FXML
    private void handleAdd() {
        // TODO: Реализовать добавление эксперимента
        System.out.println("Добавление эксперимента...");
    }

    @FXML
    private void handleEdit() {
        // TODO: Реализовать редактирование эксперимента
        System.out.println("Редактирование эксперимента...");
    }

    @FXML
    private void handleDelete() {
        // TODO: Реализовать удаление эксперимента
        System.out.println("Удаление эксперимента...");
    }

    @FXML
    private void handleExport() {
        // Экспорт выбранных экспериментов
        try {
            List<Experiment> selectedExperiments = experimentsTable.getSelectionModel().getSelectedItems();

            if (selectedExperiments.isEmpty()) {
                showAlert("Предупреждение", "Выберите эксперименты для экспорта");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Экспорт экспериментов в Excel");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "эксперименты_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) experimentsTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("ID;Название;Дата начала;Дата окончания;Статус;Результат\n");

                for (Experiment exp : selectedExperiments) {
                    csv.append(String.format("%d;%s;%s;%s;%s;%s\n",
                            exp.getId(),
                            exp.getName(),
                            exp.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            exp.getEndDate() != null ?
                                    exp.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "",
                            exp.getStatus(),
                            exp.getResult() != null ? exp.getResult() : ""
                    ));
                }

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Эксперименты экспортированы:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось экспортировать: " + e.getMessage());
        }
    }

    @FXML
    private void handleReports() {
        // Меню отчетов для лаборанта-исследователя
        try {
            ContextMenu reportMenu = new ContextMenu();

            MenuItem experimentsReport = new MenuItem("Отчет по экспериментам (CSV)");
            MenuItem resultsReport = new MenuItem("Отчет по результатам (CSV)");
            MenuItem analysisReport = new MenuItem("Аналитический отчет (CSV)");
            MenuItem monthlyReport = new MenuItem("Ежемесячный отчет (PDF)");

            experimentsReport.setOnAction(e -> generateExperimentsReport());
            resultsReport.setOnAction(e -> generateResultsReport());
            analysisReport.setOnAction(e -> generateAnalysisReport());
            monthlyReport.setOnAction(e -> generateMonthlyPDFReport());

            reportMenu.getItems().addAll(experimentsReport, resultsReport,
                    analysisReport, new SeparatorMenuItem(),
                    monthlyReport);

            // Показываем меню рядом с кнопкой
            reportMenu.show(reportsButton,
                    reportsButton.localToScreen(reportsButton.getBoundsInLocal()).getMinX(),
                    reportsButton.localToScreen(reportsButton.getBoundsInLocal()).getMaxY());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть меню отчетов: " + e.getMessage());
        }
    }

    private void generateExperimentsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по экспериментам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_эксперименты_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) experimentsTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<Experiment> experiments = ExperimentDAO.getAllExperiments();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("=== ОТЧЕТ ПО ЭКСПЕРИМЕНТАМ ===\n");
                csv.append("Дата формирования: " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n");
                csv.append("Исследователь: " + currentUser.getName() + "\n\n");

                csv.append("ID;Название;Дата начала;Статус;Результат;Примечания\n");

                int activeCount = 0;
                int completedCount = 0;
                int failedCount = 0;

                for (Experiment exp : experiments) {
                    csv.append(String.format("%d;%s;%s;%s;%s;%s\n",
                            exp.getId(),
                            exp.getName(),
                            exp.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            exp.getStatus(),
                            exp.getResult() != null ? exp.getResult() : "",
                            exp.getNotes() != null ? exp.getNotes() : ""
                    ));

                    switch (exp.getStatus()) {
                        case "Активен": activeCount++; break;
                        case "Завершен": completedCount++; break;
                        case "Неудачен": failedCount++; break;
                    }
                }

                csv.append("\n=== СТАТИСТИКА ===\n");
                csv.append("Всего экспериментов: " + experiments.size() + "\n");
                csv.append("Активных: " + activeCount + "\n");
                csv.append("Завершенных: " + completedCount + "\n");
                csv.append("Неудачных: " + failedCount + "\n");

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по экспериментам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateResultsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по результатам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_результаты_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) experimentsTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<Experiment> experiments = ExperimentDAO.getCompletedExperiments();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("=== ОТЧЕТ ПО РЕЗУЛЬТАТАМ ЭКСПЕРИМЕНТОВ ===\n");
                csv.append("Дата формирования: " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n\n");

                csv.append("Название;Дата начала;Дата окончания;Результат;Исследователь;Выводы\n");

                for (Experiment exp : experiments) {
                    csv.append(String.format("%s;%s;%s;%s;%s;%s\n",
                            exp.getName(),
                            exp.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            exp.getEndDate() != null ?
                                    exp.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "",
                            exp.getResult(),
                            exp.getResearcherName(),
                            exp.getConclusions() != null ? exp.getConclusions() : ""
                    ));
                }

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по результатам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateAnalysisReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить аналитический отчет");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "аналитический_отчет_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) experimentsTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Получаем данные для анализа
                int totalExperiments = ExperimentDAO.getTotalExperimentsCount();
                int activeExperiments = ExperimentDAO.getActiveExperimentsCount();
                int completedExperiments = ExperimentDAO.getCompletedExperimentsCount();
                double successRate = ExperimentDAO.getSuccessRate();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("=== АНАЛИТИЧЕСКИЙ ОТЧЕТ ИССЛЕДОВАТЕЛЬСКОЙ ЛАБОРАТОРИИ ===\n");
                csv.append("Период: " +
                        LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                        " - " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n");
                csv.append("Исследователь: " + currentUser.getName() + "\n\n");

                csv.append("Показатель;Значение\n");
                csv.append("Всего экспериментов;" + totalExperiments + "\n");
                csv.append("Активных экспериментов;" + activeExperiments + "\n");
                csv.append("Завершенных экспериментов;" + completedExperiments + "\n");
                csv.append("Процент успеха;" + String.format("%.2f", successRate) + "%\n");
                csv.append("Средняя длительность эксперимента;" +
                        ExperimentDAO.getAverageDuration() + " дней\n");
                csv.append("Количество исследователей;" +
                        ExperimentDAO.getResearchersCount() + "\n");

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Аналитический отчет сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateMonthlyPDFReport() {
        try {
            // Здесь можно реализовать генерацию PDF отчета
            // Пока просто покажем сообщение
            showAlert("В разработке",
                    "Генерация ежемесячного PDF отчета будет доступна в следующем обновлении.\n" +
                            "Для получения PDF отчета обратитесь к администратору.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать PDF отчет: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
}