package storages;

import models.Account;
import models.Manufacturer;
import models.Order;
import models.OrderStatus;
import models.Product;
import service.Settings;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Класс описывает работу магазина, где в качестве хранилища используется база
 * данных. В качестве СУБД используется PosgreSQL 10 (PgAdmin 4).
 * 
 * @author tolyara
 * @since 12.11.2017
 */

public class WebShopJDBC implements Storage {

	private Connection connection;
	private static final String QUERY_SELECT_ALL_PRODUCTS = "select * from products order by product_id;";
	private static final String QUERY_INSERT_PRODUCT = "insert into products (product_name, category_id_fk, manufacturer_name_fk, price, creation_date, colour, size, amount_in_storage) values (?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String QUERY_UPDATE_PRODUCT = "update products as products set product_name = ? where products.product_id = ?;"
			+ "update products as products set category_id_fk = ? where products.product_id = ?;"
			+ "update products as products set manufacturer_name_fk = ? where products.product_id = ?;"
			+ "update products as products set price = ? where products.product_id = ?;"
			+ "update products as products set creation_date = ? where products.product_id = ?;"
			+ "update products as products set colour = ? where products.product_id = ?;"
			+ "update products as products set size = ? where products.product_id = ?;"
			+ "update products as products set amount_in_storage = ? where products.product_id = ?;";
	private static final String QUERY_DELETE_PRODUCT = "delete from products as products where products.product_id = ?;";
	private static final String QUERY_SELECT_ALL_ROLES = "select * from account_roles;";
	private static final String QUERY_SELECT_ALL_ACCOUNTS = "select * from accounts;";
	private static final String QUERY_INSERT_ORDER = "insert into orders (account_name_fk, status, total_price) values (?, ?, ?);";
	private static final String QUERY_INSERT_INTO_ORDER_PRODUCT = "insert into order_product (order_id, product_id, product_name, category_id, manufacturer_name, price, creation_date, colour, size, ordered_amount) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String QUERY_SELECT_ALL_ORDERS = "select * from orders;";
	private static final String QUERY_SELECT_ALL_ORDER_PRODUCT = "select * from order_product;";
	private static final String QUERY_UPDATE_ACCOUNT_STATUS = "update accounts as accounts set is_active = ? where accounts.account_name = ?;";
	private static final String QUERY_UPDATE_ORDER_STATUS = "update orders as orders set status = ? where orders.order_id = ?;";
	private static final String QUERY_INSERT_ACCOUNT = "insert into accounts (account_name, account_pass, is_active) values (?, ?, ?);"
			+ "insert into account_roles (account_name_fk, role_name) values (?, ?);";
	private static final String QUERY_SELECT_ALL_MANUFACTURERS = "select * from manufacturers;";
	private static final String QUERY_FIND_PRODUCTS = "select * from products where manufacturer_name_fk like ? and price >= ? and price <= ? and (colour like ? or colour is null); ";

	/*
	 * Default constructor is used if we want to use JDBC connection through Tomcat
	 * connection pool. 
	 */
	public WebShopJDBC() {
		this.connection = ConnectionPool.getInstance().getConnection();
	}

	/*
	 * This constructor is used if we want to use classic JDBC without a connection
	 * pool. To use it you need to pass any string as argument (particularly, change
	 * WebShopJDBC constructor type in class StorageIdentifier).
	 */
	public WebShopJDBC(String oneConnection) {
		final Settings settings = Settings.getInstance();
		try {
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(settings.value("jdbc.url"), settings.value("jdbc.username"),
					settings.value("jdbc.password"));
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ConcurrentHashMap<Integer, Product> getProducts() {
		final ConcurrentHashMap<Integer, Product> products = new ConcurrentHashMap<>();
		/*
		 * Для автоматического закрытия соединения с БД используется конструкция
		 * try-with-resources, которую можно применять с любыми объектами, относящимися
		 * к интерфейсу AutoCloseable. В данном случае это объект Statement.
		 */
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_PRODUCTS)) {
			while (rs.next()) {
				products.put(rs.getInt("product_id"),
						new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("category_id_fk"),
								rs.getString("manufacturer_name_fk"), rs.getDouble("price"),
								rs.getDate("creation_date"), rs.getString("colour"), rs.getString("size"),
								rs.getInt("amount_in_storage")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return products;
	}

	// ------------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------

	@Override
	public int addProduct(Product product) {
		// if (product.getPrice() > 0 || product.getAmount() > 0) {
		int addedProductId = -1;
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_INSERT_PRODUCT,
				Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, product.getProductName());
			statement.setInt(2, product.getCategoryId());
			statement.setString(3, product.getManufacturerName());
			statement.setDouble(4, product.getPrice());
			statement.setDate(5, (java.sql.Date) product.getCreationDate());
			statement.setString(6, product.getColour());
			statement.setString(7, product.getSize());
			statement.setInt(8, product.getAmount());
			statement.executeUpdate();
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					addedProductId = generatedKeys.getInt(1);
				} else {
					throw new IllegalStateException("Could not add new product to DB!");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return addedProductId;
		// } else {
		// throw new NumberFormatException(ERROR_PRODUCT_CREATE_NEGATIVE_VALUE);
		// }
	}

	/*
	 * Создаем заглушку для метода generateProductId(), определенного в
	 * интерфейсе-предке.
	 */
	@Override
	public int generateProductId() {
		return 7;
	}

	/*
	 * TODO - данный метод переписать с помощью select... UPD - способ не найден
	 */
	@Override
	public Product getProductById(int id) {
		return this.getProducts().get(id);
	}

	@Override
	public void editProduct(int id, String newProductName, int newCategoryId, String newManufacturerName,
			Double newPrice, java.util.Date newDate, String newColour, String newSize, int newAmount) {
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_UPDATE_PRODUCT)) {
			statement.setString(1, newProductName);
			statement.setInt(2, id);
			statement.setInt(3, newCategoryId);
			statement.setInt(4, id);
			statement.setString(5, newManufacturerName);
			statement.setInt(6, id);
			statement.setDouble(7, newPrice);
			statement.setInt(8, id);
			statement.setDate(9, (java.sql.Date) newDate);
			statement.setInt(10, id);
			statement.setString(11, newColour);
			statement.setInt(12, id);
			statement.setString(13, newSize);
			statement.setInt(14, id);
			statement.setInt(15, newAmount);
			statement.setInt(16, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteProduct(int id) {
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_DELETE_PRODUCT)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * TODO - данный метод переписать с помощью select...
	 */
	@Override
	public Product getProductByProductName(String productName) {
		Product foundedProduct = new Product();
		try {
			for (Product product : this.getProducts().values()) {
				if (product.getProductName().equalsIgnoreCase(productName)) {
					foundedProduct = product;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foundedProduct;
	}

	// ------------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------

	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String checkAccountRole(String login) {
		String foundedRole = new String();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ROLES)) {
			while (rs.next()) {
				/*
				 * Сравниваем логины из таблицы БД account_roles с переданным через параметр
				 * логином аккаунта
				 */
				if (rs.getString("account_name_fk").equals(login)) {
					foundedRole = rs.getString("role_name");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundedRole;
	}

	@Override
	public boolean checkLoginPassword(String login, String password) {
		boolean authenticationResult = false;
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ACCOUNTS)) {
			while (rs.next()) {
				/*
				 * Сравниваем логин из таблицы БД accounts с переданным через параметр логином
				 * аккаунта
				 */
				if (rs.getString("account_name").equals(login)) {
					/*
					 * Сравниваем пароль данного логина из таблицы БД accounts с переданным через
					 * параметр паролем
					 */
					if (rs.getString("account_pass").equals(password)) {
						/*
						 * Также нужно, чтобы аккаунт не был заблокирован
						 */
						if (rs.getBoolean("is_active") == true) {
							/* разрешаем вход в систему */
							authenticationResult = true;
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return authenticationResult;
	}

	@Override
	public int makeOrder(Order order) {
		int addedOrderId = -1;
		/* Сначала добавим новый заказ в таблицу orders */
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_INSERT_ORDER,
				Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, order.getUserLogin());
			statement.setString(2, order.getStatus().toString());
			statement.setDouble(3, order.getTotalPrice());
			statement.executeUpdate();
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					addedOrderId = generatedKeys.getInt(1);
				} else {
					throw new IllegalStateException("Could not add new product to DB!");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		/* Затем добавляем информацию в таблицу order_product */
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_INSERT_INTO_ORDER_PRODUCT)) {
			for (Product product : order.getOrderedProducts().values()) {
				statement.setInt(1, addedOrderId);
				statement.setInt(2, product.getId());
				statement.setString(3, product.getProductName());
				statement.setInt(4, product.getCategoryId());
				statement.setString(5, product.getManufacturerName());
				statement.setDouble(6, product.getPrice());
				statement.setDate(7, (java.sql.Date) product.getCreationDate());
				statement.setString(8, product.getColour());
				statement.setString(9, product.getSize());
				statement.setInt(10, product.getAmount());
				statement.executeUpdate();
				statement.clearParameters();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return addedOrderId;
	}

	/*
	 * Создаем заглушку для метода generateOrderId(), определенного в
	 * интерфейсе-предке.
	 */
	@Override
	public int generateOrderId() {
		return 7;
	}

	@Override
	public ConcurrentHashMap<Integer, Order> getUserOrders(String login) {
		ConcurrentHashMap<Integer, Order> foundedOrders = new ConcurrentHashMap<Integer, Order>();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ORDERS)) {
			while (rs.next()) {
				/*
				 * Сравниваем логины из таблицы БД orders с переданным через параметр логином
				 * аккаунта
				 */
				int orderId = rs.getInt("order_id");
				if (rs.getString("account_name_fk").equals(login)) {
					foundedOrders.put(orderId,
							new Order(orderId, rs.getString("account_name_fk"),
									this.getOrderedProductsByOrderId(orderId),
									OrderStatus.recognizeOrderStatus(rs.getString("status"))));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundedOrders;
	}

	private ConcurrentHashMap<Integer, Product> getOrderedProductsByOrderId(int orderId) {
		ConcurrentHashMap<Integer, Product> foundedOrderedProducts = new ConcurrentHashMap<Integer, Product>();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ORDER_PRODUCT)) {
			while (rs.next()) {
				/*
				 * Сравниваем order_id из таблицы БД orders с переданным через параметр аккаунта
				 */
				if (rs.getInt("order_id") == orderId) {
					foundedOrderedProducts.put(rs.getInt("product_id"),
							new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("category_id"),
									rs.getString("manufacturer_name"), rs.getDouble("price"),
									rs.getDate("creation_date"), rs.getString("colour"), rs.getString("size"),
									rs.getInt("ordered_amount")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundedOrderedProducts;
	}

	@Override
	public ConcurrentHashMap<String, Account> getAccounts() {
		final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ACCOUNTS)) {
			while (rs.next()) {
				accounts.put(rs.getString("account_name"),
						new Account(rs.getString("account_name"), rs.getBoolean("is_active")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return accounts;
	}

	@Override
	public void changeAccountStatus(String login, Boolean currentStatus) {
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_UPDATE_ACCOUNT_STATUS)) {
			/* меняем статус аккаунта на противоположный */
			statement.setBoolean(1, !currentStatus);
			statement.setString(2, login);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ConcurrentHashMap<Integer, Order> getAllOrders() {
		ConcurrentHashMap<Integer, Order> foundedOrders = new ConcurrentHashMap<Integer, Order>();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_ORDERS)) {
			while (rs.next()) {
				int orderId = rs.getInt("order_id");
				foundedOrders.put(orderId,
						new Order(orderId, rs.getString("account_name_fk"), this.getOrderedProductsByOrderId(orderId),
								OrderStatus.recognizeOrderStatus(rs.getString("status"))));
			}
			// }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundedOrders;
	}

	@Override
	public void changeOrderStatus(int orderId, String newOrderStatus) {
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_UPDATE_ORDER_STATUS)) {
			/* меняем статус заказа */
			statement.setString(1, OrderStatus.recognizeOrderStatus(newOrderStatus).toString());
			statement.setInt(2, orderId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addAccount(String role, Account account) {
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_INSERT_ACCOUNT)) {
			/* для таблицы accounts */
			statement.setString(1, account.getLogin());
			statement.setString(2, account.getPassword());
			statement.setBoolean(3, account.getIsActive());
			/* для таблицы account_roles */
			statement.setString(4, account.getLogin());
			statement.setString(5, role);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ConcurrentMap<String, Manufacturer> getManufacturers() {
		final ConcurrentHashMap<String, Manufacturer> manufacturers = new ConcurrentHashMap<>();
		try (final Statement statement = this.connection.createStatement();
				final ResultSet rs = statement.executeQuery(QUERY_SELECT_ALL_MANUFACTURERS)) {
			while (rs.next()) {
				manufacturers.put(rs.getString("manufacturer_name"),
						new Manufacturer(rs.getString("manufacturer_name")));
				;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return manufacturers;
	}

	@Override
	public ConcurrentHashMap<Integer, Product> findProducts(String manufacturerName, String minPrice, String maxPrice,
			String colour) {
		final ConcurrentHashMap<Integer, Product> foundedProducts = new ConcurrentHashMap<>();
		/* Приводим полученные строки в необходимый для работы с БД вид */
		String manufacturerNameForDB;
		if (manufacturerName == null || manufacturerName.isEmpty()) {
			manufacturerNameForDB = "%";
		} else {
			manufacturerNameForDB = manufacturerName;
		}
		double minPriceForDB;
		if (minPrice == null || minPrice.isEmpty()) {
			/* Ставим нижний порог цены 0 */
			minPriceForDB = 0.0;
		} else {
			minPriceForDB = Double.valueOf(minPrice);
		}
		double maxPriceForDB;
		if (maxPrice == null || maxPrice.isEmpty()) {
			/* Ставим верхний порог цены очень большой, чтобы все товары попадали в выборку */
			maxPriceForDB = 100_000_000.0;
		} else {
			maxPriceForDB = Double.valueOf(maxPrice);
		}		
		String colourForDB;
		if (colour == null || colour.isEmpty()) {
			colourForDB = "%";
		}
		else {
			colourForDB = colour;
		}
		try (final PreparedStatement statement = this.connection.prepareStatement(QUERY_FIND_PRODUCTS)) {
			statement.setString(1, manufacturerNameForDB);
			statement.setDouble(2, minPriceForDB);
			statement.setDouble(3, maxPriceForDB);
			statement.setString(4, colourForDB);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				foundedProducts.put(rs.getInt("product_id"),
						new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("category_id_fk"),
								rs.getString("manufacturer_name_fk"), rs.getDouble("price"),
								rs.getDate("creation_date"), rs.getString("colour"), rs.getString("size"),
								rs.getInt("amount_in_storage")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundedProducts;
	}

}
