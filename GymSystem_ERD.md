```mermaid
erDiagram
    %% ==========================================
    %% 核心人物實體
    %% ==========================================
    member {
        bigint member_id PK
        varchar email UK
        varchar mobile
        varchar address
        date birthday
        varchar name
        varchar password
        tinyint status
        datetime created_at
        datetime updated_at
    }
    
    admin {
        bigint admin_id PK
        varchar name
        varchar password
        varchar role
    }
    
    %% ==========================================
    %% 業務核心實體 (商品、課程、訂單)
    %% ==========================================
    product {
        bigint product_id PK
        longtext image_base64
        varchar pname
        decimal price
    }
    
    course {
        bigint course_id PK
        int capacity
        varchar coach_name
        varchar course_name
        varchar course_type
        int enrolled_count
    }
    
    porder {
        bigint order_id PK
        varchar delivery_method
        bigint member_id FK
        datetime order_date
        varchar payment_type
        varchar status
        decimal total_amount
    }

    orderdetail {
        bigint detail_id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        decimal unit_price
    }
    
    reservation {
        bigint reservation_id PK
        bigint course_id FK
        bigint member_id FK
        datetime reservation_time
        varchar status
    }
    
    %% ==========================================
    %% 客服與內容實體
    %% ==========================================
    article {
        bigint article_id PK
        varchar title
        text content
        varchar category
        varchar image_url
        tinyint is_pinned
        varchar status
        datetime published_at
        bigint admin_id FK
        datetime created_at
        datetime updated_at
    }
    
    contact_msg {
        bigint msg_id PK
        bigint member_id FK
        varchar guest_name
        varchar guest_email
        varchar subject
        varchar category
        text content
        bigint order_id FK
        bigint course_id FK
        varchar msg_status
        tinyint is_read
        datetime read_at
        text reply_content
        datetime replied_at
        tinyint is_reply_read
        datetime reply_read_at
        bigint admin_id FK
        varchar flagged_keywords
        datetime created_at
        datetime updated_at
    }

    contact_msg_attachment {
        bigint attachment_id PK
        bigint msg_id FK
        varchar file_name
        varchar file_path
        varchar file_type
        int file_size
        datetime created_at
        datetime updated_at
    }

    %% ==========================================
    %% 系統支援實體
    %% ==========================================
    faq {
        bigint faq_id PK
        varchar question
        text answer
        varchar category
        int display_order
        varchar status
        datetime created_at
        datetime updated_at
    }
    
    keyword_filter {
        bigint keyword_id PK
        varchar keyword
        varchar type
        varchar status
        datetime created_at
        datetime updated_at
    }
    
    email_log {
        bigint email_id PK
        varchar email_type
        bigint ref_id
        varchar recipient_email
        varchar subject
        text body
        varchar send_status
        datetime sent_at
        datetime created_at
    }

    %% ==========================================
    %% 關聯線定義 (Relationships)
    %% ==========================================
    
    %% 會員關聯
    member ||--o{ porder : "下訂單"
    member ||--o{ reservation : "預約課程"
    member ||--o{ contact_msg : "發送客服訊息"

    %% 管理員關聯
    admin ||--o{ article : "發布文章"
    admin ||--o{ contact_msg : "回覆客服訊息"

    %% 訂單與商品關聯
    porder ||--|{ orderdetail : "包含明細"
    porder ||--o{ contact_msg : "相關訂單詢問"
    product ||--o{ orderdetail : "被購買"

    %% 課程關聯
    course ||--o{ reservation : "被預約"
    course ||--o{ contact_msg : "相關課程詢問"

    %% 客服附件關聯
    contact_msg ||--o{ contact_msg_attachment : "附帶檔案"