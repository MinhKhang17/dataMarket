package com.example.datasetapi.service.feature;

import com.example.datasetapi.repository.WithdrawVerifyTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailServiceImpl implements  EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private WithdrawVerifyTokenRepository withdrawOtpRepository;
    @Value("${app.backend-url}")
    private String backendUrl;
    @Override
    public void sendAccountInfoEmail(String to, String username, String password, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Thông tin tài khoản của bạn");

            // HTML + CSS
            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f7f8fa;">
                        <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                            <h2 style="color: #333;">Xin chào, """ + username + """
                            !</h2>
                            <p style="font-size: 15px; color: #555;">
                                Dưới đây là thông tin tài khoản của bạn:
                            </p>
                            <div style="background-color: #f0f3f8; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
                                <p style="margin: 0; color: #333;"><strong>Tên đăng nhập:</strong> """ + username + """
                                </p>
                                <p style="margin: 0; color: #333;"><strong>Mật khẩu:</strong> """ + password + """
                            </p>
                            </div>
                            <p style="font-size: 15px; color: #444;">
                                Vì lý do bảo mật, vui lòng <strong>đổi mật khẩu ngay sau khi đăng nhập lần đầu</strong>.
                            </p>
                            <a href=\"""" + resetLink + """
                                \" style="display: inline-block; background-color: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 6px; margin-top: 10px;">
                                Cập nhật mật khẩu ngay
                            </a>
                            <p style="margin-top: 25px; font-size: 13px; color: #777;">
                                Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email.
                            </p>
                        </div>
                    </div>
                    """;

            helper.setText(htmlContent, true);
            helper.setFrom("itsportfpt@gmail.com");

            mailSender.send(message);
            System.out.println("✅ Email tài khoản đã được gửi đến " + to);

        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    @Override
    public void sendVerfiMail(String token, String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Xác thực tài khoản của bạn");

            String verifyLink = backendUrl + "/api/auth/register/verify-email?token=" + token;

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; background: #f5f6fa; padding: 25px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 20px;
                                border-radius: 10px; box-shadow: 0 3px 10px rgba(0,0,0,0.1);">

                        <h2 style="text-align:center; color:#333;">Xác thực Email</h2>

                        <p style="font-size: 15px; color:#444;">
                            Xin chào, chúng tôi đã nhận được yêu cầu đăng ký từ email:
                            <strong>""" + email + """
                        </strong>
                        </p>

                        <p style="font-size: 15px; color:#444;">
                            Vui lòng nhấn vào nút bên dưới để hoàn tất quá trình xác thực email của bạn.
                        </p>

                        <div style="text-align:center; margin: 30px 0;">
                            <a href='""" + verifyLink + """
                               '
                               style="background:#007bff; color:white; padding:12px 22px; 
                                      text-decoration:none; border-radius:6px; font-weight:bold;">
                                Xác nhận Email
                            </a>
                        </div>

                        <p style="font-size: 13px; color:#666;">
                            Nếu nút không hoạt động, hãy sao chép liên kết bên dưới và dán vào trình duyệt:
                        </p>

                        <p style="font-size: 13px; word-break:break-all; color:#1a73e8;">
                            """ + verifyLink + """
                        </p>

                        <hr style="margin-top:25px;">
                        <p style="font-size: 12px; color:#999; text-align:center;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </div>
                """;

            helper.setText(htmlContent, true);
            helper.setFrom("itsportfpt@gmail.com");

            mailSender.send(message);
            System.out.println("📨 Email xác thực đã được gửi tới: " + email);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    @Override
    public void sendWithdrawVerifyLink(Long userId, String email, String token,
                                       Long amount, String bankName, String accountNumber,
                                       String bankHolderName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Confirm Your Withdrawal Request");

            String verifyLink = backendUrl + "/api/payment/withdraw/verify?token=" + token;

            String html = """
            <div style="font-family: Arial, sans-serif; background: #f5f6fa; padding: 25px;">
                <div style="max-width: 650px; margin: auto; background: white; padding: 25px;
                            border-radius: 12px; box-shadow: 0 3px 12px rgba(0,0,0,0.1);">

                    <h2 style="text-align:center; color:#333; margin-bottom: 20px;">
                        Confirm Your Withdrawal Request
                    </h2>

                    <p style="font-size: 15px; color:#444; line-height: 1.5;">
                        A withdrawal request has been submitted from your account.  
                        Please review the transaction details below:
                    </p>

                    <div style="background:#f0f3f8; padding: 18px; border-radius: 10px; margin: 20px 0;">
                        <p style="margin: 6px 0; font-size:15px;">
                            <strong>Amount:</strong> """ + String.format("%,d", amount) + """ 
                        VND
                        </p>
                        <p style="margin: 6px 0; font-size:15px;">
                            <strong>Bank:</strong> """ + bankName + """
                        </p>
                        <p style="margin: 6px 0; font-size:15px;">
                            <strong>Account Number:</strong> """ + accountNumber + """
                        </p>
                        <p style="margin: 6px 0; font-size:15px;">
                            <strong>Account Holder:</strong> """ + bankHolderName + """
                        </p>
                        <p style="margin: 6px 0; font-size:15px;">
                            <strong>Requested At:</strong> """ + Instant.now().toString() + """
                        </p>
                    </div>

                    <p style="font-size: 15px; color:#444; line-height:1.5;">
                        If this information is correct, click the button below to confirm and complete your withdrawal.
                    </p>

                    <div style="text-align:center; margin: 30px 0;">
                        <a href='""" + verifyLink + """
                           '
                           style="background:#007bff; color:white; padding:14px 28px;
                                  text-decoration:none; border-radius:8px;
                                  font-size:16px; font-weight:bold; display:inline-block;">
                            ✔ Confirm Withdrawal
                        </a>
                    </div>

                    <p style="font-size: 13px; color:#666;">
                        If the button doesn't work, use this link:
                    </p>

                    <p style="font-size: 13px; word-break:break-all; color:#1a73e8;">
                        """ + verifyLink + """
                    </p>

                    <hr style="margin-top:25px;">
                    <p style="font-size: 12px; color:#999; text-align:center;">
                        If you didn't make this request, please contact support immediately.<br>
                        This is an automated email — do not reply.
                    </p>
                </div>
            </div>
        """;

            helper.setText(html, true);
            helper.setFrom("itsportfpt@gmail.com");
            mailSender.send(message);

        } catch (Exception e){
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

}
