consumer-group: mỗi instance đóng vai là consumer cần cấu hình consumer group (groupId) để giải quyết một số bài toán như sau. (Ví dụ trong hệ thống có 3 instance đều lắng nghe 1 kafka topic.)
- Nếu không có groupId, kafka sẽ xem 3 instance này là độc lập và 1 message trong topic này sẽ được xử lí tận 3 lần. (1 email notification sẽ được send 3 lần bởi 3 instance)
- Kafka có các partition và quản lý thứ tự cũng như đánh dấu các message nào đã được xử lí thông qua offset. Nếu không có groupId, thì kafka sẽ không biết instance đó đến từ đó để đưa ra vị trí chính xác message nào tiếp theo cần được xử lí. 
- 1 instance có thể xử lí nhiều partitions và các partition này sẽ bị giữ lại bởi chỉ riêng instance này. Nếu trong trường hợp instance này bị crash, việc sử dụng groupId giúp cho Kafka move toàn bộ partition đang bị chiếm giữ sang một instance cùng groupId khác để xử lí. tránh tình trạng các message bị hold lại.
...

Thông thường các instance cùng consumer-group sẽ có chung một logic code. Có một vài trường hợp khác logic code - Canary Deployment (các instance đang sử dụng logic code version1, ta cần test logic với version2, nên sẽ deploy version2 lên một pod - các pod còn lại vẫn sử dụng code version1.)
- Có nhiều cách để quyết định 1 message sẽ được xử lí bởi canary-pod (theo % hoặc theo cụ thể do producer quyết định)
- Thường partition cuối cùng sẽ được chọn làm canary-partition. Nên khi một message được quyết định được xử lí bởi canary-pod thì cần chọn partition cố định để gửi (Kafka có hỗ trợ lấy ra số lượng partition thông qua cluster)

Tăng partition thì dễ, nhưng giảm partition thì cực kì phức tạp
- bản chất các partitions là một chuỗi thư mục file logs ghi liên tục trên đĩa => gây mất mát data trên các partitions bị xoá
+ Nếu muốn xoá bắt buộc phải tìm cách đọc hết message trên các partitions kia => cũng phức tạp vì các message tới partition trong thời gian thực. 
- Phá vở tính chất thứ tự của message trong Kafka:
+ giả sử trong hệ thống có 5 partitions. các message có key = userId có nhu cầu được xử lí theo thứ tự. 
+ ban đầu các message có cùng userId (ví dụ: 107) <=> 107 mod 5 = 2 => các message này sẽ được đưa vào partition2. Sau đó hệ thống cắt giảm còn 2 partitions <=> 107 mod 2 = 1. các message tiếp theo có cùng key sẽ được đưa vào partition1. Điều này sẽ phá vở nhu cầu ban đầu, vì kafka chỉ đảm bảo việc xử lí tuần cho message trong cùng 1 partition.

Cách giảm partition
- Tạo topic mới với số lượng partition mong muốn.
- Hướng toàn bộ producer sang topic mới 
- Giữ lại một phần consumer trên topic cũ, 1 phần consumer trên topic mới. 
- Đến khi nào số lượng message trên topic cũ = 0 thì move toàn bộ sang topic mới.