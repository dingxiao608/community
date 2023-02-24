$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	// 隐藏提示框
	$("#sendModal").modal("hide");

	//从页面获取目标username和content
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	// 异步post请求
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			data = $.parseJSON(data);//服务器端sendLetter方法返回的json字符串是{"code":0或者1}
			if (data.code == 0){// 发送私信成功
				$("#hintBody").text("发送成功！");
			}else {				//发送私信失败
				$("#hintBody").text(data.msg);
			}

			//显示提示框
			$("#hintModal").modal("show");
			//2秒后，自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 不管插入成功还是失败，都刷新
				window.location.reload();
			}, 2000);
		}
	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}