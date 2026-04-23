```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '16px', 'fontFamily': 'sans-serif'}}}%%
erDiagram
    %% ==========================================
    %% 核心人物實體
    %% ==========================================
    member {
        bigint member_id PK
        varchar email UK
        varchar password
        varchar name
        varchar mobile
        varchar address
        date birthday
        tinyint status
        datetime created_at
        datetime updated_at
    }
    
    admin {
        bigint admin_id PK
        varchar email UK
        varchar password
        varchar name
        varchar role
        tinyint status
        datetime created_at
        datetime updated_at
    }

    %% ==========================================
    %% 團體課程與預約模組
    %% ==========================================
    course {
        bigint course_id PK
        varchar course_name
        varchar course_type
        bigint coach_id FK
        varchar description
        date course_date
        datetime start_time
        datetime end_time
        int capacity
        int enrolled_count
        tinyint status
        datetime created_at
        datetime updated_at
    }
    
    course_reservation {
        bigint reservation_id PK
        bigint course_id FK
        bigint member_id FK
        varchar reservation_status
        datetime reserved_at
        datetime cancelled_at
        varchar remark
        datetime created_at
        datetime updated_at
    }

    %% ==========================================
    %% 私人教練模組 (PT)
    %% ==========================================
    pt_package {
        bigint package_id PK
        varchar package_name
        int session_count
        decimal price
        varchar description
        tinyint status
        datetime created_at
        datetime updated_at
    }

    pt_order {
        bigint pt_order_id PK
        bigint member_id FK
        bigint package_id FK
        int total_sessions
        int used_sessions
        int remaining_sessions
        decimal total_amount
        varchar order_status
        datetime purchased_at
        datetime expired_at
        datetime created_at
        datetime updated_at
    }

    %% ==========================================
    %% 實體商品與訂單模組
    %% ==========================================
    product {
        bigint product_id PK
        longtext image_base64
        varchar pname
        decimal price
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
    
    %% ==========================================
    %% 客服與內容模組
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
    %% 系統支援模組
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
    
    %% 會員業務關聯
    member ||--o{ porder : "下訂單(實體商品)"
    member ||--o{ course_reservation : "預約(團體課程)"
    member ||--o{ pt_order : "購買(私教方案)"
    member ||--o{ contact_msg : "發送(客服訊息)"

    %% 教練/管理員業務關聯
    admin ||--o{ course : "指導(課程)"
    admin ||--o{ article : "發布(文章)"
    admin ||--o{ contact_msg : "回覆(客服)"

    %% 課程與私教關聯
    course ||--o{ course_reservation : "被預約"
    course ||--o{ contact_msg : "課程提問"
    pt_package ||--o{ pt_order : "被購買"

    %% 訂單與商品關聯
    porder ||--|{ orderdetail : "包含(明細)"
    porder ||--o{ contact_msg : "訂單提問"
    product ||--o{ orderdetail : "被購買"

    %% 客服附件關聯
    contact_msg ||--o{ contact_msg_attachment : "附帶檔案"