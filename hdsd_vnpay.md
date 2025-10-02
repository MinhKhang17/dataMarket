post http://localhost:8080/api/payment/create (gửi cả auth token)

{
"amount": "100000",
"orderType": "topup",
"orderInfo": "Nap point",
"bankCode": "NCB",
"language": "vn"
}

nó hiện ra cái link -> crtl + nhấn vô link (nếu test trong postman) -------- còn fe code ra 1 trang form gửi các trường như trên là ok

Ngân hàng	NCB

Số thẻ	9704198526191432198

Tên chủ thẻ	NGUYEN VAN A

Ngày phát hành	07/15

Mật khẩu OTP	123456

http://localhost:8080/api/payment/return?... -> backend trả về json

{
"status": "success",
"message": "Payment completed successfully"
} ------- hoặc failed

fe gọi api này sang trang riêng -> render đẹp hơn -> setting cho nó hiện khoảng 5s thông báo thành công xong redirect về trang khác (fe tự quyết xem trả về trang nào)