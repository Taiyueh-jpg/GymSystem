-- ======================================================
-- GymSystem Database Master Script (Final Full Version)
-- 執行順序已根據 Foreign Key 依賴關係優化
-- ======================================================
CREATE DATABASE IF NOT EXISTS `gymsystem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `gymsystem`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

-- ------------------------------------------------------
-- 第一階段：建立「被參考」的 Parent Tables
-- ------------------------------------------------------

-- Table: admin (Joyce 標記)
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

INSERT INTO `admin` VALUES (1,'Admin','123456','manager');

-- Table: member (芳羽)
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `UK_member_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

INSERT INTO `member` VALUES (1,'joyce@gmail.com','0912345678','Joyce','123456');

-- Table: product (信穎)
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `image_base64` longtext,
  `pname` varchar(255) NOT NULL,
  `price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: course (星瑋)
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `course_id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int NOT NULL,
  `coach_name` varchar(255) DEFAULT NULL,
  `course_name` varchar(255) NOT NULL,
  `course_type` varchar(255) DEFAULT NULL,
  `enrolled_count` int NOT NULL,
  PRIMARY KEY (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

INSERT INTO `course` VALUES (1,20,'Coach Amy','瑜珈課','yoga',0);

-- Table: porder (滷蛋)
DROP TABLE IF EXISTS `porder`;
CREATE TABLE `porder` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_method` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  `order_date` datetime(6) NOT NULL,
  `payment_type` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `total_amount` decimal(38,2) NOT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

INSERT INTO `porder` VALUES (1,'home_delivery',1,'2026-04-09 00:13:19.000000','credit_card','paid',1500.00);

-- ------------------------------------------------------
-- 第二階段：建立「帶有外鍵」的 Child Tables
-- ------------------------------------------------------

-- Table: article (Joyce)
DROP TABLE IF EXISTS `article`;
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
  CONSTRAINT `fk_article_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

INSERT INTO `article` VALUES (1,'春季優惠活動','加入會員享有8折優惠！','優惠資訊','uploads/article/spring.jpg',1,'published','2026-04-09 00:30:46',1,'2026-04-09 00:30:46','2026-04-09 00:30:46'),(2,'新課程公告','全新瑜珈課程開放報名！','課程公告','uploads/article/yoga.jpg',0,'published','2026-04-09 00:30:46',1,'2026-04-09 00:30:46','2026-04-09 00:30:46');

-- Table: contact_msg (Joyce)
DROP TABLE IF EXISTS `contact_msg`;
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
  PRIMARY KEY (`msg_id`),
  CONSTRAINT `fk_contact_msg_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_contact_msg_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_contact_msg_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `fk_contact_msg_order` FOREIGN KEY (`order_id`) REFERENCES `porder` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

INSERT INTO `contact_msg` VALUES (1,1,NULL,NULL,'商品損壞','complaint_order','我收到的商品有破損',1,NULL,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-09 00:25:40','2026-04-09 00:25:40'),(2,NULL,'Amy','amy@gmail.com','課程時間詢問','course','請問瑜珈課幾點開始？',NULL,1,'new',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'2026-04-09 00:25:40','2026-04-09 00:25:40');

-- Table: orderdetail (滷蛋)
DROP TABLE IF EXISTS `orderdetail`;
CREATE TABLE `orderdetail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`detail_id`),
  CONSTRAINT `fk_orderdetail_porder` FOREIGN KEY (`order_id`) REFERENCES `porder` (`order_id`),
  CONSTRAINT `fk_orderdetail_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: reservation (星瑋)
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `reservation_id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `reservation_time` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`reservation_id`),
  CONSTRAINT `fk_res_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `fk_res_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------
-- 第三階段：其他 (Joyce)
-- ------------------------------------------------------

-- Table: contact_msg_attachment (Joyce)
DROP TABLE IF EXISTS `contact_msg_attachment`;
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
  CONSTRAINT `fk_attachement_msg` FOREIGN KEY (`msg_id`) REFERENCES `contact_msg` (`msg_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- Table: faq (Joyce)
DROP TABLE IF EXISTS `faq`;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- Table: keyword_filter (Joyce)
DROP TABLE IF EXISTS `keyword_filter`;
CREATE TABLE `keyword_filter` (
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(100) NOT NULL,
  `type` varchar(20) DEFAULT 'block',
  `status` varchar(20) DEFAULT 'active',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyword_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- Table: email_log (Joyce)
DROP TABLE IF EXISTS `email_log`;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- 恢復外鍵檢查
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
