# New 

Luồng voting 

Tổng cộng có 3 phiếu vote từ editor 

Khi tantor nộp submission lên cho editor (http://localhost:8386/api/workflow/name/3/submit-to-board?tantouId=202
) --> thì submission sẽ là ON-GOING 

Sau đó editor thực hiện vote (http://localhost:8386/api/workflow/name/review/board
) --> Khi tổng hợp hoàn thành đủ 3 phiếu thì mới thành công vote và submission sẽ là APPROVED
2/3 == APPROVED , 3/3 == APPROVED 
1/3 == REJECTED

FE Validate : Khi nộp lên board phải có 3 phiếu vote thì mới thành công hoặc cho out 
khi user đã vote 2 phiếu và còn lại không vote trong khoảng thời gian nhất định thì sẽ là rejected 

và các bản vote để sẽ được ghi nhận trong bảng SUBMISSION_REVIEW 