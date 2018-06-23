
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>Личный кабинет</title>
<style>
<%@
include file ='/css/style.css' 
%>
</style>
</head>
<body>

	<h1>Личный кабинет</h1>
	<p>
		Здесь Вы можете просмотреть свои заказы <br><br> <a
			href="${pageContext.servletContext.contextPath}/user/view">
			Вернуться в магазин </a>
	</p>

	<%-- Список заказов --%>

	<div class="orders">
		<table border="1">
			<caption>Заказы</caption>
			<tr class="table_head">
				<td>- ID заказа -</td>
				<td>- Статус -</td>
				<td>- Итоговая <br> стоимость -</td>
				<td>- Данные о товарах -</td>
			</tr>
			<%-- В переменной userOrders передаются и ключи, и значения hashmap заказов --%>
			<c:forEach var="order" items="${userOrders}" varStatus="status">
				<tr valign="top">
					<td>${order.value.id}</td>
					<td>${order.value.status}</td>
					<td>${order.value.totalPrice}</td>
					<td>
						<table>
							<tr class="table_head">
								<td>- ID товара -</td>
								<td>- Наименование <br> товара -
								</td>
								<td>- Категория -</td>
								<td>- Производитель -</td>
								<td>- Цена -</td>
								<td>- Дата изготовления -</td>
								<td>- Цвет -</td>
								<td>- Размер -</td>
								<td>- Заказанное <br> количество -
								</td>
							</tr>
							<c:forEach var="product" items="${order.value.orderedProducts}"
								varStatus="status">
								<tr valign="top">
									<td>${product.value.id}</td>
									<td>${product.value.productName}</td>
									<td>${product.value.categoryId}</td>
									<td>${product.value.manufacturerName}</td>
									<td>${product.value.price}</td>
									<td>${product.value.creationDate}</td>
									<td>${product.value.colour}</td>
									<td>${product.value.size}</td>
									<td>${product.value.amount}</td>
								</tr>
							</c:forEach>
						</table>
					</td>
				</tr>
			</c:forEach>
		</table>
		<br>
		<form action="${pageContext.servletContext.contextPath}/logout"
			method="POST">
			<input type="submit" align="center" value="Выйти из системы">
		</form>
		<br>
	</div>

</body>
</html>
