<html>
<head>
<title>Admin View</title>
<style>
span.tab {
	width: 200px;
	display: block;
	float: left;
}
</style>
</head>
<body>
	<form method="post">
		<p>
			<span class="tab">Command: </span><input type="text" name="command" size="60">
		</p>
		<p>
			<input type="submit" value="Execute">
		</p>		
	</form>
				
	<form method="get">			
      	<p> Generated Rules: </p>
      	<textarea rows="5" cols="200" >${rules}</textarea>
	</form>
</body>
</html>