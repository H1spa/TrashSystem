package com.example.trash.service;

import com.example.trash.model.UtilizerProcess;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilizerServiceClient {
    private static final String BASE_URL = "http://localhost:5000/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Проверка состояния эмулятора
    public static boolean checkEmulatorStatus() {
        String url = BASE_URL + "/health";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                Map<String, Object> result = gson.fromJson(json,
                        new TypeToken<Map<String, Object>>(){}.getType());
                return result != null && result.containsKey("status");
            }
        } catch (IOException e) {
            System.err.println("Эмулятор не запущен: " + e.getMessage());
        }
        return false;
    }

    // Получение списка всех утилизаторов
    public static Map<String, Object> getAllUtilizers() {
        String url = BASE_URL + "/utilizers";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(json, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    // Запуск процесса утилизации
    public static String startUtilizerProcess(UtilizerProcess process) {
        String url = BASE_URL + "/utilizer/" + process.getUtilizerName();

        // Формируем запрос в формате из задания
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("client", String.valueOf(process.getOrderId()));
        requestData.put("orderId", process.getOrderId());
        requestData.put("orderNumber", process.getOrderNumber());
        requestData.put("researcherId", process.getResearcherId());
        requestData.put("researcherName", process.getResearcherName());

        // Добавляем услугу
        Map<String, Object> service = new HashMap<>();
        service.put("serviceId", process.getServiceId());
        service.put("serviceCode", process.getServiceCode());
        service.put("serviceName", process.getServiceName());

        requestData.put("services", List.of(service));

        String json = gson.toJson(requestData);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseJson = response.body().string();
                Map<String, Object> result = gson.fromJson(responseJson,
                        new TypeToken<Map<String, Object>>(){}.getType());

                // Получаем ID процесса из ответа
                if (result.containsKey("services")) {
                    List<Map<String, Object>> services = (List<Map<String, Object>>) result.get("services");
                    if (!services.isEmpty()) {
                        return (String) services.get(0).get("processId");
                    }
                }
            } else {
                System.err.println("Ошибка запуска процесса: " + response.code());
                String errorBody = response.body().string();
                System.err.println("Тело ошибки: " + errorBody);
            }
        } catch (IOException e) {
            System.err.println("Ошибка соединения: " + e.getMessage());
        }
        return null;
    }

    // Получение статуса процесса
    public static Map<String, Object> getProcessStatus(String processId, String utilizerName) {
        String url = BASE_URL + "/utilizer/" + utilizerName + "/status/" + processId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                return gson.fromJson(json,
                        new TypeToken<Map<String, Object>>(){}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получение результатов процесса
    public static Map<String, Object> getProcessResults(String processId, String utilizerName) {
        String url = BASE_URL + "/utilizer/" + utilizerName + "/results/" + processId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                return gson.fromJson(json,
                        new TypeToken<Map<String, Object>>(){}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получение информации о емкости утилизатора
    public static Map<String, Object> getUtilizerCapacity(String utilizerName) {
        String url = BASE_URL + "/utilizer/" + utilizerName + "/capacity";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                return gson.fromJson(json,
                        new TypeToken<Map<String, Object>>(){}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Одобрение результатов
    public static boolean approveResults(String processId, String utilizerName,
                                         String approverName, String notes) {
        String url = BASE_URL + "/utilizer/" + utilizerName + "/approve/" + processId;

        Map<String, String> requestData = new HashMap<>();
        requestData.put("approverName", approverName);
        requestData.put("notes", notes);

        String json = gson.toJson(requestData);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Отклонение результатов
    public static boolean rejectResults(String processId, String utilizerName,
                                        String reason, String reviewerName) {
        String url = BASE_URL + "/utilizer/" + utilizerName + "/reject/" + processId;

        Map<String, String> requestData = new HashMap<>();
        requestData.put("reason", reason);
        requestData.put("reviewerName", reviewerName);

        String json = gson.toJson(requestData);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkHealth() {
        try {
            String url = BASE_URL + "/health";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Map<String, Object> result = gson.fromJson(json,
                            new TypeToken<Map<String, Object>>(){}.getType());
                    return result != null && result.containsKey("status");
                }
            }
        } catch (IOException e) {
            System.err.println("Health check failed: " + e.getMessage());
        }
        return false;
    }

    // Проверка доступности утилизатора
    public static boolean isUtilizerAvailable(String utilizerName) {
        Map<String, Object> capacity = getUtilizerCapacity(utilizerName);
        if (capacity != null && capacity.containsKey("availableSlots")) {
            int availableSlots = ((Number) capacity.get("availableSlots")).intValue();
            return availableSlots > 0;
        }
        return false;
    }
}