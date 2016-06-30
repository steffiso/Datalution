<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Player</title>
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
			<span class="tab">UserID:</span><input type="text" name="username">
		</p>
		<p>
			<input type="submit" value="Show">
		</p>

	</form>
	<c:choose>
		<c:when test="${username != null}">
			<c:choose>
				<c:when test="${values == null}">
					<form method="post">

						<p>Create new Character</p>
						<p>Please fill "put" command and click "Create"</p>

						<p>
							<input type="hidden" name="newPlayer" value="true"> <span
								class="tab">Put:</span><input type="text" name="name">
						</p>
						<p>
							<input type="submit" value="Create">
						</p>
					</form>
				</c:when>
				<c:otherwise>
					<p>Show Character</p>
					<p>
						<span class="tab">Values:</span><input type="text" name="value"
							size="60" value="${values}">
					</p>
					<form method="post">

						<p>Change Character</p>
						<p>Please fill "put" command and click "Change"</p>

						<p>
							<input type="hidden" name="newPlayer" value="false"> <span
								class="tab">Put:</span><input type="text" name="name" size="60">
						</p>
						<p>
							<input type="submit" value="Change">
						</p>
					</form>
				</c:otherwise>
			</c:choose>
		</c:when>
	</c:choose>
</body>
</html>