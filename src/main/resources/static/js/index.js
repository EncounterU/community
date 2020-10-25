$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val();//#对应的是id
	var content = $("#message-text").val();
	// 发送异步请求(POST)
	$.post(
	    CONTEXT_PATH + "/discuss/add",//访问路径，居然还是完整的
	    {"title":title,"content":content},//传入的值 ，注意格式
	    function(data) {//回调函数，处理返回的结果
	        data = $.parseJSON(data);//转为json对象
	        // 在提示框中显示返回消息
	        $("#hintBody").text(data.msg);//装进modal，下面再一起显示吗？
	        // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后,自动隐藏提示框
            setTimeout(function(){
                $("#hintModal").modal("hide");
                // 刷新页面
                if(data.code == 0) {
                    window.location.reload();//重新加载当前页面
                }
            }, 2000);
	    }
	);

}