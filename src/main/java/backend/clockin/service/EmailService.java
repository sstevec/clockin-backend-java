package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

public interface EmailService {

    SysResult sendSimpleMail(String from, String to, String subject, String content);

    SysResult sendAttachmentMail(String from, String to, String subject, String content);

    SysResult sendInlineMail(String from, String to, String subject, String content);
}
