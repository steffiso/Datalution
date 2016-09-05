<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>User View</title>
<style>
span.tab {
	width: 200px;
	display: block;
	float: left;
}
}
</style>
</head>
<body>

	<form method="get">	
		<p>Show Character</p>
		<p>
			<span class="tab">Get command:</span><input type="text" name="getCommand">
		</p>
		<p>
			<input type="submit" value="Show">
		</p>
	</form>	
	
	<form method="post">
		<p>Put Character:</p>
		<p>
			<span class="tab">Put command:</span><input type="text" name="putCommand" size="50">
		</p>
		<p>
			<input type="submit" value="Create">
		</p>

	</form>
	
	<form method="get">			
      	<p> Result: </p>
      	<textarea rows="5" cols="200" >${result}</textarea>
	</form>
</body>
</html>