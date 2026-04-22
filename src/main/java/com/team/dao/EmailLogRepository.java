package com.team.dao;

import com.team.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    List<EmailLog> findByEmailType(String emailType);

    List<EmailLog> findBySendStatus(String sendStatus);

    List<EmailLog> findByRefIdAndEmailType(Long refId, String emailType);

    List<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
}
