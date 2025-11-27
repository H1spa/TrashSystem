CREATE DATABASE  IF NOT EXISTS `nenavredi` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `nenavredi`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: nenavredi
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `clients`
--

DROP TABLE IF EXISTS `clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `fio` varchar(150) NOT NULL,
  `birth_date` date DEFAULT NULL,
  `passport_series` varchar(10) DEFAULT NULL,
  `passport_number` varchar(10) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `company_id` int DEFAULT NULL,
  `type_client_id` int DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login` (`login`),
  KEY `company_id` (`company_id`),
  KEY `type_client_id` (`type_client_id`),
  CONSTRAINT `clients_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `compani` (`id`),
  CONSTRAINT `clients_ibfk_2` FOREIGN KEY (`type_client_id`) REFERENCES `type_clients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clients`
--

LOCK TABLES `clients` WRITE;
/*!40000 ALTER TABLE `clients` DISABLE KEYS */;
INSERT INTO `clients` VALUES (1,'client1','pass123','Иванов Иван Иванович','1985-05-15','4501','123456','+79991234567','ivanov@mail.ru',NULL,1,0),(2,'client2','pass456','Петров Петр Петрович','1978-12-20','4502','654321','+79997654321','petrov@company.ru',1,2,0),(3,'client3','pass789','Сидорова Анна Владимировна','1990-08-03','4503','789012','+79998887766','sidorova@firm.ru',2,2,0);
/*!40000 ALTER TABLE `clients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compani`
--

DROP TABLE IF EXISTS `compani`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compani` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `inn` varchar(12) DEFAULT NULL,
  `rs` varchar(20) DEFAULT NULL,
  `bik` varchar(10) DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compani`
--

LOCK TABLES `compani` WRITE;
/*!40000 ALTER TABLE `compani` DISABLE KEYS */;
INSERT INTO `compani` VALUES (1,'ООО \"Ромашка\"','г. Москва, ул. Ленина, 1','123456789012','40702810000000000001','044525225',0),(2,'АО \"Вектор\"','г. Санкт-Петербург, ул. Победы, 10','098765432109','40702810000000000002','044525226',0),(3,'ИП Иванов','г. Екатеринбург, ул. Мира, 5','112233445566','40802810000000000003','044525227',0);
/*!40000 ALTER TABLE `compani` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `okaz_uslyga`
--

DROP TABLE IF EXISTS `okaz_uslyga`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `okaz_uslyga` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `users_service_id` int NOT NULL,
  `utilizator_id` int DEFAULT NULL,
  `status` enum('Назначена','В работе','Завершена') DEFAULT 'Назначена',
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `users_service_id` (`users_service_id`),
  KEY `utilizator_id` (`utilizator_id`),
  CONSTRAINT `okaz_users_service_fk` FOREIGN KEY (`users_service_id`) REFERENCES `users_services` (`id`),
  CONSTRAINT `okaz_uslyga_fk1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `okaz_uslyga_fk3` FOREIGN KEY (`utilizator_id`) REFERENCES `utilizator` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `okaz_uslyga`
--

LOCK TABLES `okaz_uslyga` WRITE;
/*!40000 ALTER TABLE `okaz_uslyga` DISABLE KEYS */;
INSERT INTO `okaz_uslyga` VALUES (1,1,1,1,'В работе','2024-01-15 11:00:00','2024-01-20 16:00:00',0),(2,2,4,2,'Назначена','2024-01-17 10:00:00',NULL,0),(3,3,9,3,'Завершена','2024-01-17 10:00:00','2024-01-24 16:00:00',0);
/*!40000 ALTER TABLE `okaz_uslyga` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `client_id` int NOT NULL,
  `users_service_id` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('Создан','Выполняется','Завершен','Отменен') DEFAULT 'Создан',
  `duration_days` int DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `client_id` (`client_id`),
  KEY `users_service_id` (`users_service_id`),
  CONSTRAINT `fk_orders_users_service` FOREIGN KEY (`users_service_id`) REFERENCES `users_services` (`id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,1,'2024-01-15 10:00:00','Выполняется',5,0),(2,2,4,'2024-01-16 14:30:00','Создан',3,0),(3,3,9,'2024-01-17 09:15:00','Завершен',7,0);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `check_order_archive` BEFORE UPDATE ON `orders` FOR EACH ROW BEGIN
    IF NEW.archived = 1 THEN
        IF EXISTS (
            SELECT 1 FROM okaz_uslyga 
            WHERE order_id = NEW.id AND status != 'Завершена'
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Нельзя архивировать заказ, пока не завершены все услуги';
        END IF;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `cost` decimal(10,2) NOT NULL,
  `code` varchar(50) DEFAULT NULL,
  `duration_days` int DEFAULT NULL,
  `deviation_avg` decimal(5,2) DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
INSERT INTO `services` VALUES (1,'Утилизация батареек',262.71,'619',3,2.50,0),(2,'Утилизация аккумуляторов',361.88,'311',5,3.20,0),(3,'Утилизация ртутных градусников',234.09,'548',2,1.80,0),(4,'Утилизация лекарственных препаратов',143.22,'258',1,1.00,0),(5,'Утилизация сильно действующих химических препаратов',102.85,'176',7,4.10,0),(6,'Утилизация медицинских отходов',176.83,'501',2,1.50,0),(7,'Утилизация промышленных и производственных отходов',289.99,'543',10,5.20,0),(8,'Утилизация отходов химической промышленности',490.77,'557',14,6.80,0),(9,'Выезд курьера',341.78,'229',1,0.50,0),(10,'Хранение отходов',419.90,'415',30,2.10,0),(11,'Эко-паспорт',447.65,'323',7,3.50,0),(12,'Формирование карточки предприятия',209.78,'855',3,1.20,0),(13,'Формирование карточки частного лица',396.03,'346',2,0.80,0),(14,'Независимая экспертиза опасных отходов',105.32,'836',5,2.30,0),(15,'Исследования воздушной среды',443.66,'659',8,3.80,0),(16,'Исследования твердых отходов',370.62,'797',6,2.90,0),(17,'Исследования жидких отходов',290.11,'287',4,2.00,0);
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `type_clients`
--

DROP TABLE IF EXISTS `type_clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `type_clients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `type_clients`
--

LOCK TABLES `type_clients` WRITE;
/*!40000 ALTER TABLE `type_clients` DISABLE KEYS */;
INSERT INTO `type_clients` VALUES (1,'Физ лицо'),(2,'Юр лицо');
/*!40000 ALTER TABLE `type_clients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `type_users`
--

DROP TABLE IF EXISTS `type_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `type_users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `type_users`
--

LOCK TABLES `type_users` WRITE;
/*!40000 ALTER TABLE `type_users` DISABLE KEYS */;
INSERT INTO `type_users` VALUES (1,'лаборант'),(2,'админ'),(3,'исследователь'),(4,'бухгалтер');
/*!40000 ALTER TABLE `type_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `fio` varchar(150) NOT NULL,
  `last_login` datetime DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `type_user_id` int DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login` (`login`),
  KEY `type_user_id` (`type_user_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`type_user_id`) REFERENCES `type_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'chacking0','4tzqHdkqzo4','Clareta Hacking','2020-10-02 00:00:00','147.231.50.234',1,0),(2,'nmably1','ukM0e6','Northrop Mably','2020-06-20 00:00:00','22.32.15.211',2,0),(3,'frolf2','7QpCwac0yi','Fabian Rolf','2020-05-19 00:00:00','113.92.142.29',1,0),(4,'lraden3','5Ydp2mz','Lauree Raden','2020-06-22 00:00:00','39.24.146.52',1,0),(5,'bfollos4','ckmAJPQV','Barby Follos','2020-03-18 00:00:00','87.232.97.3',4,0),(6,'menterle5','0PRom6i','Mile Enterle','2020-07-04 00:00:00','85.121.209.6',2,0),(7,'mpeaker6','0Tc5oRc','Midge Peaker','2020-09-03 00:00:00','196.39.132.128',2,0),(8,'mrobichon7','LEwEjMlmE5X','Manon Robichon','2020-08-31 00:00:00','143.159.207.105',2,0),(9,'srobken8','Cbmj3Yi','Stavro Robken','2020-05-22 00:00:00','12.154.73.196',3,0),(10,'btidmas9','dYDHbBQfK','Bryan Tidmas','2020-06-06 00:00:00','24.42.134.21',4,0),(11,'jfussiea','EGxXuLQ9','Jeannette Fussie','2020-08-21 00:00:00','98.194.112.137',2,0),(12,'santonaccib','YcXAhY3Pja','Stephen Antonacci','2019-10-03 00:00:00','198.146.255.15',4,0),(13,'nbountiffc','5xfyjS9ZULGA','Niccolo Bountiff','2020-01-22 00:00:00','231.78.246.229',1,0),(14,'cbenjefieldd','tQOsP0ei9TuD','Clemente Benjefield','2020-07-09 00:00:00','88.126.93.246',4,0),(15,'ocorbyne','bG1ZIzwIoU','Orlan Corbyn','2020-04-24 00:00:00','232.175.48.179',3,0),(16,'cstickinsf','QRYADbgNj','Coreen Stickins','2020-04-20 00:00:00','64.30.175.158',1,0),(17,'dclarageg','Yp59ZIDnWe','Daveta Clarage','2020-06-09 00:00:00','139.88.229.111',3,0),(18,'jmccawleyh','g58zLcmCYON','Javier McCawley','2020-04-20 00:00:00','14.199.67.32',4,0),(19,'dbandi','yFAaYuVW','Daile Band','2019-12-02 00:00:00','206.105.148.56',3,0),(20,'abutteryj','ttOFbWWGtD','Angil Buttery','2020-06-21 00:00:00','192.158.7.138',2,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_services`
--

DROP TABLE IF EXISTS `users_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_services` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `service_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `fk_us_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
  CONSTRAINT `fk_us_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_services`
--

LOCK TABLES `users_services` WRITE;
/*!40000 ALTER TABLE `users_services` DISABLE KEYS */;
INSERT INTO `users_services` VALUES (1,1,8),(2,1,14),(3,1,17),(4,2,12),(5,2,1),(6,2,8),(7,2,14),(8,2,3),(9,3,7),(10,3,14),(11,4,12),(12,4,4),(13,5,7),(14,5,10),(15,5,1),(16,5,8),(17,6,8),(18,6,14),(19,6,9),(20,7,17),(21,7,1),(22,7,3),(23,7,13),(24,8,10),(25,8,2),(26,8,5),(27,8,12),(28,9,11),(29,9,3),(30,9,13),(31,10,9),(32,10,13),(33,10,6),(34,10,3);
/*!40000 ALTER TABLE `users_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilizator`
--

DROP TABLE IF EXISTS `utilizator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilizator` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `description` text,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilizator`
--

LOCK TABLES `utilizator` WRITE;
/*!40000 ALTER TABLE `utilizator` DISABLE KEYS */;
INSERT INTO `utilizator` VALUES (1,'Эко-Сервис','Специализируется на утилизации химических отходов. Опыт работы более 10 лет.',0),(2,'Мед-Утиль','Утилизация медицинских отходов категорий Б и В. Лицензия №ЛО-77-01-012345',0),(3,'Пром-Рециклинг','Переработка промышленных отходов 1-4 класса опасности. Современное оборудование.',0);
/*!40000 ALTER TABLE `utilizator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'nenavredi'
--

--
-- Dumping routines for database 'nenavredi'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 12:40:35
