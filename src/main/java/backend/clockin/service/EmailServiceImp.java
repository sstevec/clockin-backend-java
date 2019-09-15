package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailServiceImp implements EmailService{
    @Autowired
    JavaMailSender javaMailSender;

    @Override
    public SysResult sendSimpleMail(String from, String to, String subject, String content) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText("你好啊: " + content + "仅在关闭该弹窗前有效");

        javaMailSender.send(simpleMailMessage);
        return SysResult.build(200,"验证码发送成功",content);
    }

    @Override
    public SysResult sendAttachmentMail(String from, String to, String subject, String content) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource fileSystemResource = new FileSystemResource(new File("C:\\Users\\su999\\Desktop\\old\\1.jpg"));
            String fileName = "1.jpg";
            helper.addAttachment(fileName, fileSystemResource);

            javaMailSender.send(message);
            return SysResult.build(200,"验证码发送成功",content);
        } catch (Exception e) {
            e.printStackTrace();
            return SysResult.build(201,"验证码发送失败");
        }
    }

    @Override
    public SysResult sendInlineMail(String from, String to, String subject, String content) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File("C:\\Users\\su999\\Desktop\\old\\1.jpg"));
            helper.addInline("1.jpg", file);
            javaMailSender.send(mimeMessage);
            return SysResult.build(200,"验证码发送成功",content);
        } catch (Exception e) {
            return SysResult.build(201,"验证码发送失败");
        }
    }

}
