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

		<c:choose>
			<c:when test="${username != null}">
				<c:choose>
					<c:when test="${values == null}">
						<p>Show Character</p>
							<p>
								<span class="tab">Get command:</span><input type="text" name="getCommand">
							</p>
							
							<p>No entity found. Please fill "put command" and click "Create".</p>
							
							<p>
								<input type="submit" value="Show">
							</p>
					</c:when>
					<c:otherwise>
							<p>Show Character</p>
							<p>
								<span class="tab">Get command:</span><input type="text" name="getCommand">
							</p>

							<span class="tab">Values:</span><input type="text" name="value"
								size="60" value="${values}">
							<p>
								<input type="submit" value="Show">
							</p>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<p>Show Character</p>
							<p>
								<span class="tab">Get command:</span><input type="text" name="getCommand">
							</p>
							<p>
								<input type="submit" value="Show">
							</p>
			</c:otherwise>
		</c:choose>
	</form>
	
	
	<form method="post">
		<p>Put Character:</p>
		<p>
			<span class="tab">Put command:</span><input type="text" name="putCommand">
		</p>
		<p>
			<input type="submit" value="Create">
		</p>

	</form>
</body>
</html>