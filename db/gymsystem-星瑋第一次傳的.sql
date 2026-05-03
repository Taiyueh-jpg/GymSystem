CREATE DATABASE  IF NOT EXISTS `gymsystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gymsystem`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: gymsystem
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
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理員編號 (主鍵)',
  `email` varchar(255) NOT NULL COMMENT '登入帳號 (唯一值)',
  `password` varchar(255) NOT NULL COMMENT '登入密碼',
  `name` varchar(255) NOT NULL COMMENT '教練/管理員姓名',
  `role` varchar(50) NOT NULL DEFAULT 'coach' COMMENT '角色權限',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '狀態：1=在職, 0=離職/停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '帳號建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '資料最後更新時間',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `UK_admin_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'test@gym.com','123456','測試小編','admin',1,'2026-04-20 11:34:14','2026-04-20 11:34:14');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `article_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `content` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `is_pinned` tinyint(1) DEFAULT '0',
  `status` varchar(20) DEFAULT 'draft',
  `published_at` datetime DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`),
  KEY `fk_article_admin` (`admin_id`),
  CONSTRAINT `fk_article_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (1,'春季優惠活動','加入會員享有8折優惠！','優惠資訊','uploads/article/spring.jpg',1,'published','2026-04-09 00:30:46',1,'2026-04-09 00:30:46','2026-04-09 00:30:46'),(2,'新課程公告','全新瑜珈課程開放報名！','課程公告','uploads/article/yoga.jpg',0,'published','2026-04-09 00:30:46',1,'2026-04-09 00:30:46','2026-04-09 00:30:46');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_msg`
--

DROP TABLE IF EXISTS `contact_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_msg` (
  `msg_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `guest_name` varchar(50) DEFAULT NULL,
  `guest_email` varchar(100) DEFAULT NULL,
  `subject` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `content` text NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  `msg_status` varchar(20) DEFAULT 'new',
  `is_read` tinyint(1) DEFAULT '0',
  `read_at` datetime DEFAULT NULL,
  `reply_content` text,
  `replied_at` datetime DEFAULT NULL,
  `is_reply_read` tinyint(1) DEFAULT '0',
  `reply_read_at` datetime DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `flagged_keywords` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_flagged` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`msg_id`),
  KEY `fk_contact_msg_admin` (`admin_id`),
  KEY `fk_contact_msg_member` (`member_id`),
  KEY `fk_contact_msg_course` (`course_id`),
  KEY `fk_contact_msg_order` (`order_id`),
  CONSTRAINT `fk_contact_msg_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_contact_msg_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `fk_contact_msg_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_contact_msg_order` FOREIGN KEY (`order_id`) REFERENCES `porder` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_msg`
--

LOCK TABLES `contact_msg` WRITE;
/*!40000 ALTER TABLE `contact_msg` DISABLE KEYS */;
INSERT INTO `contact_msg` VALUES (1,1,NULL,NULL,'商品損壞','complaint_order','我收到的商品有破損',1,NULL,'replied',0,NULL,'您好！感謝詢問，目前有瑜珈、重訓、有氧等課程，歡迎來電洽詢！','2026-04-22 09:48:25',0,NULL,1,NULL,'2026-04-09 00:25:40','2026-04-22 09:48:25',0),(2,NULL,'Amy','amy@gmail.com','課程時間詢問','course','請問瑜珈課幾點開始？',NULL,1,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-09 00:25:40','2026-04-09 00:25:40',0),(5,NULL,'王小明','wang@example.com','想詢問課程費用','課程諮詢','請問健身課程的月費方案有哪些？',NULL,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-21 14:14:19','2026-04-21 14:14:19',0),(6,NULL,'李小華','lee@example.com','器材故障回報','設備問題','三樓的跑步機螢幕無法顯示。',NULL,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-21 14:14:19','2026-04-21 14:14:19',0),(10,NULL,'測試訪客','test@gmail.com','測試主旨','課程諮詢','這是一則測試留言',NULL,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-21 14:32:40','2026-04-21 14:32:40',0),(11,1,NULL,NULL,'測試圖片上傳','設備問題','附上圖片測試 Cloudinary 上傳功能',NULL,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-21 14:41:09','2026-04-21 14:41:20',0),(12,NULL,'Joyce Test','gymsystem2026@gmail.com','詢問課程','general','請問目前有哪些課程可以報名？',NULL,NULL,'replied',0,NULL,'您好！感謝詢問，目前有瑜珈、重訓、有氧等課程，歡迎來電洽詢！','2026-04-22 10:00:22',0,NULL,1,NULL,'2026-04-22 09:39:01','2026-04-22 10:00:22',0),(13,NULL,'Test','test@test.com','課程詢問','general','請問有折扣碼嗎？',NULL,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,'折扣碼','2026-04-22 09:43:27','2026-04-22 09:43:27',1);
/*!40000 ALTER TABLE `contact_msg` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_msg_attachment`
--

DROP TABLE IF EXISTS `contact_msg_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_msg_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `msg_id` bigint NOT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `file_size` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`),
  KEY `fk_attachement_msg` (`msg_id`),
  CONSTRAINT `fk_attachement_msg` FOREIGN KEY (`msg_id`) REFERENCES `contact_msg` (`msg_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_msg_attachment`
--

LOCK TABLES `contact_msg_attachment` WRITE;
/*!40000 ALTER TABLE `contact_msg_attachment` DISABLE KEYS */;
INSERT INTO `contact_msg_attachment` VALUES (3,11,'2-5-50kg-premium-rubber-dumbbell-set-horizontal-racks-p5779-75460_zoom.png','https://res.cloudinary.com/dqohqtbpy/image/upload/v1776753676/gymsystem/contact/2026/04/7f4b1309-b5fe-4cbf-bc8e-a7cdda0fe678.png','image/png',675428,'2026-04-21 14:41:20','2026-04-21 14:41:20'),(4,11,'69127-3-Tier-Pro-Hex-Rack.jpg','https://res.cloudinary.com/dqohqtbpy/image/upload/v1776753678/gymsystem/contact/2026/04/35b518c4-50bc-4884-ab5e-239ab24522bb.jpg','image/jpeg',60478,'2026-04-21 14:41:20','2026-04-21 14:41:20'),(5,11,'69129-HexProfessionalRack.jpg','https://res.cloudinary.com/dqohqtbpy/image/upload/v1776753679/gymsystem/contact/2026/04/4eefbeab-233e-453e-9ab1-b60ee5d87363.jpg','image/jpeg',184855,'2026-04-21 14:41:20','2026-04-21 14:41:20');
/*!40000 ALTER TABLE `contact_msg_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` bigint NOT NULL AUTO_INCREMENT COMMENT '課程編號 (主鍵)',
  `course_name` varchar(255) NOT NULL COMMENT '課程名稱',
  `course_type` varchar(20) NOT NULL COMMENT '課程類型：group / personal',
  `coach_id` bigint NOT NULL COMMENT '教練編號 (外鍵)',
  `description` varchar(500) DEFAULT NULL COMMENT '課程描述/備註',
  `course_date` date NOT NULL COMMENT '課程日期',
  `start_time` datetime NOT NULL COMMENT '開始時間',
  `end_time` datetime NOT NULL COMMENT '結束時間',
  `capacity` int NOT NULL DEFAULT '1' COMMENT '人數上限',
  `enrolled_count` int NOT NULL DEFAULT '0' COMMENT '已報名人數',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '狀態：1=開放/上架, 0=取消/下架, 2=已結束',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',
  PRIMARY KEY (`course_id`),
  KEY `FK_course_coach` (`coach_id`),
  CONSTRAINT `FK_course_coach` FOREIGN KEY (`coach_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_reservation`
--

DROP TABLE IF EXISTS `course_reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_reservation` (
  `reservation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '預約編號 (主鍵)',
  `course_id` bigint NOT NULL COMMENT '對應課程編號 (外鍵)',
  `member_id` bigint NOT NULL COMMENT '對應會員編號 (外鍵)',
  `reservation_status` varchar(20) NOT NULL DEFAULT 'reserved' COMMENT '預約狀態：reserved, cancelled, completed, no_show',
  `reserved_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '預約時間',
  `cancelled_at` datetime DEFAULT NULL COMMENT '取消時間',
  `remark` varchar(255) DEFAULT NULL COMMENT '備註',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `UK_course_member` (`course_id`,`member_id`) COMMENT '保證同一會員不能重複預約同一堂課',
  KEY `FK_res_member` (`member_id`),
  CONSTRAINT `FK_reservation_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `FK_reservation_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_reservation`
--

LOCK TABLES `course_reservation` WRITE;
/*!40000 ALTER TABLE `course_reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_log`
--

DROP TABLE IF EXISTS `email_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_log` (
  `email_id` bigint NOT NULL AUTO_INCREMENT,
  `email_type` varchar(30) NOT NULL,
  `ref_id` bigint DEFAULT NULL,
  `recipient_email` varchar(100) NOT NULL,
  `subject` varchar(150) NOT NULL,
  `body` text NOT NULL,
  `send_status` varchar(20) DEFAULT 'pending',
  `sent_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`email_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_log`
--

LOCK TABLES `email_log` WRITE;
/*!40000 ALTER TABLE `email_log` DISABLE KEYS */;
INSERT INTO `email_log` VALUES (3,'contact_reply',12,'gymsystem2026@gmail.com','【GymSystem】您的留言已收到回覆','<!DOCTYPE html>\n<html lang=\"zh-TW\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n</head>\n<body style=\"margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;\">\n  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f4f4;padding:40px 0;\">\n    <tr><td align=\"center\">\n      <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\"\n             style=\"background:#fff;border-radius:12px;overflow:hidden;\n                    box-shadow:0 2px 12px rgba(0,0,0,0.08);\">\n        <tr>\n          <td style=\"background:#1a1a1a;padding:32px 40px;text-align:center;\">\n            <div style=\"font-size:28px;font-weight:700;color:#fff;\">GymSystem</div>\n            <div style=\"color:#aaa;font-size:13px;margin-top:6px;\">健身房管理系統</div>\n          </td>\n        </tr>\n        <tr>\n          <td style=\"padding:40px;\">\n            <p style=\"color:#333;font-size:16px;margin:0 0 16px;\">\n              親愛的 <strong>Joyce Test</strong>，您好！\n            </p>\n            <p style=\"color:#555;font-size:14px;line-height:1.8;margin:0 0 24px;\">\n              您在 GymSystem 的留言已獲得客服回覆，以下為完整內容：\n            </p>\n            <div style=\"background:#f8f8f8;border-left:4px solid #ccc;\n                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:20px;\">\n              <div style=\"font-size:12px;color:#999;margin-bottom:8px;\">您的留言</div>\n              <div style=\"font-size:13px;color:#444;font-weight:600;margin-bottom:6px;\">\n                主旨：詢問課程\n              </div>\n              <div style=\"font-size:14px;color:#555;line-height:1.7;white-space:pre-wrap;\">請問目前有哪些課程可以報名？</div>\n            </div>\n            <div style=\"background:#eef4ff;border-left:4px solid #2979ff;\n                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:28px;\">\n              <div style=\"font-size:12px;color:#2979ff;margin-bottom:8px;\">客服回覆</div>\n              <div style=\"font-size:14px;color:#333;line-height:1.8;white-space:pre-wrap;\">您好！感謝詢問，目前有瑜珈、重訓、有氧等課程，歡迎來電洽詢！</div>\n              <div style=\"font-size:12px;color:#999;margin-top:10px;\">回覆時間：2026/04/22 10:00</div>\n            </div>\n            <p style=\"color:#888;font-size:13px;line-height:1.7;margin:0;\">\n              如有後續問題，歡迎再次前往\n              <a href=\"http://localhost:5500/contact.html\"\n                 style=\"color:#2979ff;text-decoration:none;font-weight:600;\">\n                聯絡我們\n              </a>\n              留言。\n            </p>\n          </td>\n        </tr>\n        <tr>\n          <td style=\"background:#f8f8f8;padding:24px 40px;text-align:center;\n                     border-top:1px solid #eee;\">\n            <p style=\"color:#aaa;font-size:12px;margin:0;\">\n              &copy; 2026 GymSystem. All Rights Reserved.<br>\n              此為系統自動發送郵件，請勿直接回覆。\n            </p>\n          </td>\n        </tr>\n      </table>\n    </td></tr>\n  </table>\n</body>\n</html>\n','sent','2026-04-22 10:00:08','2026-04-22 10:00:05'),(4,'contact_reply',12,'gymsystem2026@gmail.com','【GymSystem】您的留言已收到回覆','<!DOCTYPE html>\n<html lang=\"zh-TW\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n</head>\n<body style=\"margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;\">\n  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f4f4;padding:40px 0;\">\n    <tr><td align=\"center\">\n      <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\"\n             style=\"background:#fff;border-radius:12px;overflow:hidden;\n                    box-shadow:0 2px 12px rgba(0,0,0,0.08);\">\n        <tr>\n          <td style=\"background:#1a1a1a;padding:32px 40px;text-align:center;\">\n            <div style=\"font-size:28px;font-weight:700;color:#fff;\">GymSystem</div>\n            <div style=\"color:#aaa;font-size:13px;margin-top:6px;\">健身房管理系統</div>\n          </td>\n        </tr>\n        <tr>\n          <td style=\"padding:40px;\">\n            <p style=\"color:#333;font-size:16px;margin:0 0 16px;\">\n              親愛的 <strong>Joyce Test</strong>，您好！\n            </p>\n            <p style=\"color:#555;font-size:14px;line-height:1.8;margin:0 0 24px;\">\n              您在 GymSystem 的留言已獲得客服回覆，以下為完整內容：\n            </p>\n            <div style=\"background:#f8f8f8;border-left:4px solid #ccc;\n                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:20px;\">\n              <div style=\"font-size:12px;color:#999;margin-bottom:8px;\">您的留言</div>\n              <div style=\"font-size:13px;color:#444;font-weight:600;margin-bottom:6px;\">\n                主旨：詢問課程\n              </div>\n              <div style=\"font-size:14px;color:#555;line-height:1.7;white-space:pre-wrap;\">請問目前有哪些課程可以報名？</div>\n            </div>\n            <div style=\"background:#eef4ff;border-left:4px solid #2979ff;\n                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:28px;\">\n              <div style=\"font-size:12px;color:#2979ff;margin-bottom:8px;\">客服回覆</div>\n              <div style=\"font-size:14px;color:#333;line-height:1.8;white-space:pre-wrap;\">您好！感謝詢問，目前有瑜珈、重訓、有氧等課程，歡迎來電洽詢！</div>\n              <div style=\"font-size:12px;color:#999;margin-top:10px;\">回覆時間：2026/04/22 10:00</div>\n            </div>\n            <p style=\"color:#888;font-size:13px;line-height:1.7;margin:0;\">\n              如有後續問題，歡迎再次前往\n              <a href=\"http://localhost:5500/contact.html\"\n                 style=\"color:#2979ff;text-decoration:none;font-weight:600;\">\n                聯絡我們\n              </a>\n              留言。\n            </p>\n          </td>\n        </tr>\n        <tr>\n          <td style=\"background:#f8f8f8;padding:24px 40px;text-align:center;\n                     border-top:1px solid #eee;\">\n            <p style=\"color:#aaa;font-size:12px;margin:0;\">\n              &copy; 2026 GymSystem. All Rights Reserved.<br>\n              此為系統自動發送郵件，請勿直接回覆。\n            </p>\n          </td>\n        </tr>\n      </table>\n    </td></tr>\n  </table>\n</body>\n</html>\n','sent','2026-04-22 10:00:25','2026-04-22 10:00:22');
/*!40000 ALTER TABLE `email_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faq`
--

DROP TABLE IF EXISTS `faq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faq` (
  `faq_id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(255) NOT NULL,
  `answer` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `display_order` int DEFAULT '0',
  `status` varchar(20) DEFAULT 'active',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`faq_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faq`
--

LOCK TABLES `faq` WRITE;
/*!40000 ALTER TABLE `faq` DISABLE KEYS */;
INSERT INTO `faq` VALUES (3,'如何加入 GymSystem 會員？','您可以直接前往前台櫃檯填寫入會申請表，或在官方網站線上申請。申請時需攜帶身分證件，完成繳費後即可當日開始使用。','會員制度',1,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(4,'月費制與年費制有何不同？','月費制每月計費，彈性較高，適合初次嘗試的朋友；年費制一次繳清享有折扣優惠，長期健身更划算。詳細方案請洽詢前台或參閱官網。','會員制度',2,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(5,'如何暫停或取消會籍？','暫停會籍需提前 7 天至櫃檯提出申請，每月最多可申請暫停 30 天。取消會籍請依合約條款辦理，年約會員需注意違約規定。','會員制度',3,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(6,'忘記會員卡怎麼辦？','忘記攜帶會員卡時，可至前台出示身分證件進行人工核對，即可正常入場。建議下載會員 App，使用電子會員證更方便。','會員制度',4,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(7,'如何預約團體課程？','您可以透過官方 App、官網會員中心，或直接至前台預約。熱門課程建議提前 3 天預約，取消預約請於課程開始前 2 小時辦理，以免影響預約資格。','課程與教練',1,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(8,'私人教練課程怎麼安排？','私人教練課程需先進行免費體能評估（約 30 分鐘），由教練根據您的目標制定訓練計畫，再依需求購買課包。請洽詢前台或直接聯繫指定教練。','課程與教練',2,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(9,'課程可以補課嗎？','團體課程若提前取消可安排補課，補課期限為原課程日後 30 天內。私人教練課若需調整時間，請提前 24 小時通知教練。','課程與教練',3,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(10,'健身房的開放時間是？','週一至週五：06:00 – 23:00，週六、日及國定假日：08:00 – 21:00。特殊節假日開放時間將另行公告，請關注官方公告。','場館設施',1,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(11,'場館內有哪些設施？','本館設有自由重量區、有氧訓練區、功能性訓練區、游泳池、三溫暖、男女更衣室及置物櫃。部分設施使用需額外收費，詳情請洽詢前台。','場館設施',2,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(12,'置物櫃如何使用？','入場後可自由使用當日置物櫃，離場時請記得取回物品並歸還鑰匙。長期置物櫃租用方案請洽前台，月租費用另計。','場館設施',3,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(13,'停車場提供嗎？','本館地下一至二樓設有停車場，會員憑當日入場記錄可享 2 小時免費停車，超時依停車場計費標準收費。','場館設施',4,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(14,'有哪些付款方式？','支援現金、信用卡（Visa / MasterCard / JCB）、轉帳及行動支付（LINE Pay、Apple Pay）。年費方案亦可分期付款，詳情請洽前台。','付款與費用',1,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(15,'發票如何索取？','每次消費預設開立電子發票，雲端歸戶至您的手機條碼。若需紙本發票或統一編號抬頭，請於結帳時告知服務人員。','付款與費用',2,'active','2026-04-21 10:46:33','2026-04-21 10:46:33'),(16,'費用可以退款嗎？','依消費保護法規，購買後 7 天內且未使用，可申請全額退款。課包購買後若已使用，退款金額以已使用堂數計算。詳細退費規定請參閱會員合約。','付款與費用',3,'active','2026-04-21 10:46:33','2026-04-21 10:46:33');
/*!40000 ALTER TABLE `faq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keyword_filter`
--

DROP TABLE IF EXISTS `keyword_filter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `keyword_filter` (
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(100) NOT NULL,
  `type` varchar(20) DEFAULT 'block',
  `status` varchar(20) DEFAULT 'active',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyword_id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keyword_filter`
--

LOCK TABLES `keyword_filter` WRITE;
/*!40000 ALTER TABLE `keyword_filter` DISABLE KEYS */;
INSERT INTO `keyword_filter` VALUES (3,'系統管理員','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(4,'官方帳號','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(5,'LINE官方','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(6,'客服專線','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(7,'帳號異常','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(8,'帳號將被停用','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(9,'立即驗證','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(10,'點擊連結','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(11,'請點擊以下','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(12,'bit.ly','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(13,'tinyurl','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(14,'goo.gl','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(15,'http://','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(16,'轉帳','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(17,'匯款','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(19,'虛擬貨幣','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(20,'比特幣','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(21,'以太幣','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(22,'投資報酬','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(23,'保證獲利','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(24,'身分證字號','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(25,'信用卡號','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(26,'銀行帳號','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(27,'密碼驗證','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(28,'OTP驗證碼','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(29,'線上賭博','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(30,'百家樂','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(31,'老虎機','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(32,'運彩投注','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(33,'加LINE領','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(34,'免費送','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(35,'限時優惠領取','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(36,'私訊我','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(37,'加我LINE','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(38,'加我WeChat','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(39,'減肥藥保證','block','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(40,'Line ID','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(41,'WeChat ID','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(42,'Telegram','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(43,'加我好友','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(44,'限時優惠','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(45,'免費體驗','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(46,'折扣碼','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(47,'https://','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(48,'www.','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(49,'代辦','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(50,'兼職','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(51,'日賺','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(52,'在家工作','flag','active','2026-04-21 15:27:42','2026-04-21 15:27:42'),(53,'ATM','block','active','2026-04-22 10:46:00','2026-04-22 10:46:00');
/*!40000 ALTER TABLE `keyword_filter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '會員編號 (主鍵)',
  `email` varchar(255) NOT NULL COMMENT '登入帳號 (信箱, 唯一值)',
  `password` varchar(255) NOT NULL COMMENT '登入密碼',
  `name` varchar(255) NOT NULL COMMENT '會員姓名',
  `mobile` varchar(20) DEFAULT NULL COMMENT '聯絡電話',
  `address` varchar(255) DEFAULT NULL COMMENT '通訊地址',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '狀態：1=正常, 0=停權',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '註冊時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '資料最後更新時間',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `UK_member_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'test@gmail.com','test1234','測試會員',NULL,NULL,NULL,1,'2026-04-21 14:35:25','2026-04-21 14:35:25');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orderdetail`
--

DROP TABLE IF EXISTS `orderdetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderdetail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `fk_orderdetail_porder` (`order_id`),
  KEY `fk_orderdetail_product` (`product_id`),
  CONSTRAINT `fk_orderdetail_porder` FOREIGN KEY (`order_id`) REFERENCES `porder` (`order_id`),
  CONSTRAINT `fk_orderdetail_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderdetail`
--

LOCK TABLES `orderdetail` WRITE;
/*!40000 ALTER TABLE `orderdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `orderdetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `porder`
--

DROP TABLE IF EXISTS `porder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `porder` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_method` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  `order_date` datetime(6) NOT NULL,
  `payment_type` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `total_amount` decimal(38,2) NOT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `porder`
--

LOCK TABLES `porder` WRITE;
/*!40000 ALTER TABLE `porder` DISABLE KEYS */;
INSERT INTO `porder` VALUES (1,'home_delivery',1,'2026-04-09 00:13:19.000000','credit_card','paid',1500.00);
/*!40000 ALTER TABLE `porder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `image_base64` longtext,
  `pname` varchar(255) NOT NULL,
  `price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pt_order`
--

DROP TABLE IF EXISTS `pt_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pt_order` (
  `pt_order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '私教訂單編號 (主鍵)',
  `member_id` bigint NOT NULL COMMENT '購買會員 (外鍵)',
  `package_id` bigint NOT NULL COMMENT '對應私教方案 (外鍵)',
  `total_sessions` int NOT NULL COMMENT '購買總節數',
  `used_sessions` int NOT NULL DEFAULT '0' COMMENT '已使用節數',
  `remaining_sessions` int NOT NULL COMMENT '剩餘節數',
  `total_amount` decimal(10,2) NOT NULL COMMENT '訂單總金額',
  `order_status` varchar(20) NOT NULL DEFAULT 'paid' COMMENT '訂單狀態：paid、refunded、expired',
  `purchased_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '購買時間',
  `expired_at` datetime DEFAULT NULL COMMENT '到期時間',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',
  PRIMARY KEY (`pt_order_id`),
  KEY `FK_ptorder_member` (`member_id`),
  KEY `FK_ptorder_package` (`package_id`),
  CONSTRAINT `FK_ptorder_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK_ptorder_package` FOREIGN KEY (`package_id`) REFERENCES `pt_package` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pt_order`
--

LOCK TABLES `pt_order` WRITE;
/*!40000 ALTER TABLE `pt_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `pt_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pt_package`
--

DROP TABLE IF EXISTS `pt_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pt_package` (
  `package_id` bigint NOT NULL AUTO_INCREMENT COMMENT '方案編號 (主鍵)',
  `package_name` varchar(255) NOT NULL COMMENT '方案名稱',
  `session_count` int NOT NULL COMMENT '課程堂數',
  `price` decimal(10,2) NOT NULL COMMENT '方案價格',
  `description` varchar(500) DEFAULT NULL COMMENT '方案說明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '狀態：1=上架, 0=下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',
  PRIMARY KEY (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pt_package`
--

LOCK TABLES `pt_package` WRITE;
/*!40000 ALTER TABLE `pt_package` DISABLE KEYS */;
/*!40000 ALTER TABLE `pt_package` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-22 13:27:11
