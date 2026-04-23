package com.team.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.model.ContactMsgAttachment;

@Repository
public interface ContactMsgAttachmentRepository extends JpaRepository<ContactMsgAttachment, Long> {

    List<ContactMsgAttachment> findByContactMsg_MsgId(Long msgId);

    void deleteByContactMsg_MsgId(Long msgId);
}
