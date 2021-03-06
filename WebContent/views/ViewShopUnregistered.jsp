
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title> Обзор магазина </title>
    <style>
        <%@include file='/css/style.css' %>
    </style>
</head>
<body class="user">

<h1>Магазин</h1>
<p>Незарегистрированный пользователь, доступные опции ограничены</p>

                <%--Каталог товаров--%>

<div class="products">
<table border="1" id="grid">
    <caption> Каталог товаров </caption>
    <thead>
    <tr class="table_head">
        <th> - ID - </th>
        <th class="th_productName" data-type="string"> - Наименование - </th>
        <th> - Категория - </th>
        <th> - Производитель - </th>
        <th class="th_price" data-type="number"> - Цена - </th>
        <th class="th_creation_date" data-type="string"> - Дата изготовления - </th>
        <th> - Цвет - </th>
        <th> - Размер - </th>
        <th> - Количество <br> на складе - </th>
        <th> - Действия - </th>
    </tr>
    </thead>
    <%-- В переменной products передаются только значения hashmap товаров --%>
    <tbody>
    <c:forEach var="product" items="${products}" varStatus="status">
        <tr valign="top">
            <td>${product.id}</td>
            <td>${product.productName}</td>   
            <td>${product.categoryId}</td>
            <td>${product.manufacturerName}</td>
            <td>${product.price}</td>
            <td>${product.creationDate}</td>
            <td>${product.colour}</td>
            <td>${product.size}</td>  
            <td>${product.amount}</td>   
            <td>
                <a href="${pageContext.servletContext.contextPath}/add-to-basket?productId=${product.id}"> Добавить в корзину </a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table> <br>
<a href="${pageContext.servletContext.contextPath}/"> На главную </a> <p> </p>
</div>

                    <%--Корзина--%>

<div class="basket">
    <table border="1">
        <caption> Корзина </caption>
        <tr class="table_head">
            <td> - ID - </td>
            <td> - Наименование - </td>
            <td> - Количество - </td>
            <td> - Действия - </td>
        </tr>
        <c:forEach var="bufferProduct" items="${bufferProducts}" varStatus="status">
            <tr valign="top">
                <td>${bufferProduct.id}</td>
                <td>${bufferProduct.productName}</td>
                <td>${bufferProduct.amount}</td>
                <td>
                    <a href="${pageContext.servletContext.contextPath}/remove-from-basket?productId=${bufferProduct.id}"> Удалить </a><br>
                </td>
            </tr>
        </c:forEach>
    </table> <br>
    <p style="margin-left: 50px;"> Незарегистрированный пользователь, <br> оформление заказа невозможно. </p> 
    <p>  </p>
    <p style="margin-left: 50px;"> Внимание! Удаление товара(-ов) из <br> корзины происходит автоматически, без <br> подтверждения.
        </p>
</div>

<%--Данный скрипт взят и "допилен" под нужды проекта с ресурса http://plnkr.co/edit/im9GDgRDAHMGORMXeSvU?p=preview--%>
<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/jquery-3.2.1.min.js"></script>
<script type="text/javascript">
    // сортировка таблицы
    // использовать делегирование!
    // должно быть масштабируемо:
    // код работает без изменений при добавлении новых столбцов и строк

    var grid = document.getElementById('grid');

    grid.onclick = function(e) {
        if (e.target.tagName != 'TH') return;

        // Если TH -- сортируем
        sortGrid(e.target.cellIndex, e.target.getAttribute('data-type'));
    };

    function sortGrid(colNum, type) {
        var tbody = grid.getElementsByTagName('tbody')[0];

        // Составить массив из TR
        var rowsArray = [].slice.call(tbody.rows);

        // определить функцию сравнения, в зависимости от типа
        var compare;

        switch (type) {
            case 'number':
                compare = function(rowA, rowB) {
                    return rowA.cells[colNum].innerHTML - rowB.cells[colNum].innerHTML;
                };
                break;
            case 'string':
                compare = function(rowA, rowB) {
                    return rowA.cells[colNum].innerHTML > rowB.cells[colNum].innerHTML;
                };
                break;
        }

        // сортировать
        rowsArray.sort(compare);

        // Убрать tbody из большого DOM документа для лучшей производительности
        grid.removeChild(tbody);

        // добавить результат в нужном порядке в TBODY
        // они автоматически будут убраны со старых мест и вставлены в правильном порядке
        for (var i = 0; i < rowsArray.length; i++) {
            tbody.appendChild(rowsArray[i]);
        }

        grid.appendChild(tbody);

    }
</script>

</body>
</html>
