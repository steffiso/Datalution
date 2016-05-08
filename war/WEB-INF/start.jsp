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
			<span class="tab">User:</span><input type="text" name="username">
			<input type="hidden" name="insert" value="true">
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
						<p>Please fill out the form and click "Create"</p>

						<p>
							<span class="tab">Name:</span><input type="text" name="name">
						</p>
						<p>
							<span class="tab">Character class:</span> <select id="character"
								name="character">
								<option value="hobbit">hobbit</option>
								<option value="dwarf">dwarf</option>
								<option value="mage">mage</option>
								<option value="elf">elf</option>
							</select>
						</p>
						<p>
							<input type="submit" value="Create">
						</p>
					</form>
				</c:when>
				<c:otherwise>
					<p>Show Character</p>
					<p>
						<span class="tab">Values:</span><input type="text" name="name" size="60"
							value="${values}">
					</p>

				</c:otherwise>
			</c:choose>
		</c:when>
	</c:choose>
</body>
</html>