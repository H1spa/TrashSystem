package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Random;

public class CaptchaController {

    @FXML private ImageView staticPiece;
    @FXML private ImageView sliderPiece;
    @FXML private Slider captchaSlider;
    @FXML private Label statusLabel;
    @FXML private Button confirmButton;

    private boolean passed = false;

    private final double movementStart = 30;     // откуда начинается движение
    private final double movementWidth = 350;    // диапазон слайдера
    private final double offset = 64;         // <=== ВАЖНО: вручную подбираемый сдвиг !!!

    private double targetX;

    @FXML
    public void initialize() {
        Random random = new Random();

        // ставим статичную фигуру строго в пределах движения
        targetX = movementStart + random.nextDouble() * movementWidth;
        staticPiece.setLayoutX(targetX);

        // ставим движущую фигуру в начало
        sliderPiece.setLayoutX(movementStart);

        // настройка слайдера
        captchaSlider.setMin(0);
        captchaSlider.setMax(100);
        captchaSlider.setValue(0);

        // ПРАВИЛЬНОЕ МЕСТО ДЛЯ ЛИСТЕНЕРА С ПРОВЕРКОЙ
        // Один общий listener для движения и отладки
        captchaSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Двигаем фигуру
            double x = movementStart + (newVal.doubleValue() / 100.0) * movementWidth;
            sliderPiece.setLayoutX(x);

            // ПРОВЕРКА - считаем разницу с offset
            double correctedSliderX = sliderPiece.getLayoutX() + offset;
            double diff = Math.abs(correctedSliderX - staticPiece.getLayoutX());
            System.out.println("Diff: " + diff + " | Без offset: " + (sliderPiece.getLayoutX() - staticPiece.getLayoutX()));

            // УБРАНА ПОДСВЕТКА КНОПКИ ПРИ ПОПАДАНИИ В ЗОНУ
            // Только отладочный вывод, без изменения визуальных элементов
        });

        statusLabel.setText("Передвиньте фигуру, чтобы она совпала с другой.");
    }

    @FXML
    private void handleConfirm() {
        // Разница между реальными позициями с поправкой offset
        double correctedSliderX = sliderPiece.getLayoutX() + offset;
        double diff = Math.abs(correctedSliderX - staticPiece.getLayoutX());

        System.out.println("Проверка: diff = " + diff + ", correctedSliderX = " + correctedSliderX + ", staticX = " + staticPiece.getLayoutX());

        if (diff < 12) { // зона допуска ±12 px
            passed = true;
            statusLabel.setText("Успешно! Закрытие...");

            // Небольшая задержка для визуального подтверждения
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                Stage stage = (Stage) captchaSlider.getScene().getWindow();
                                stage.close();
                            });
                        }
                    },
                    500
            );
        } else {
            // Капча не пройдена - окно закрывается сразу
            passed = false;
            statusLabel.setText("Не совпало! Разница: " + (int)diff + "px");

            // Закрываем окно без задержки
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                Stage stage = (Stage) captchaSlider.getScene().getWindow();
                                stage.close();
                            });
                        }
                    },
                    0
            );
        }
    }

    public boolean isPassed() {
        return passed;
    }
}