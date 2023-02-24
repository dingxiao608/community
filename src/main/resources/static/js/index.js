$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//隐藏发布框
	$("#publishModal").modal("hide");

	//获取页面输入的标题和内容
	//var变量；$("#recipient-name")用jQuery的id选择器选中id为"recipient-name"的文本框；.val()表示获取框里面的值
	var title = $("#recipient-name").val()
	var content = $("#message-text").val()

	//发送异步请求（post）
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);//服务器端addDiscussPost方法返回的json字符串是{"code":0,"msg":xxx}
			//在提示框中显示返回的消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后，自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//如果添加成功就刷新页面（重新从数据库获取帖子数据）
				if (data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}