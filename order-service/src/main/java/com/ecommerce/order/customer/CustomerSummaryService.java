package com.ecommerce.order.customer;

import com.ecommerce.order.common.NotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

/**
 * Ringkasan dihitung langsung dengan SQL agregat lewat JDBC, tanpa
 * memuat entity: satu query untuk seluruh riwayat pelanggan.
 *
 * Setiap sumber daya JDBC ditutup lewat try-with-resources, termasuk
 * ketika method keluar dengan exception (mis. 404 pelanggan tidak ada).
 * Koneksi yang tidak dikembalikan tidak pernah kembali ke pool.
 */
@Service
public class CustomerSummaryService {

    private static final String SUMMARY_SQL = """
            select count(*), coalesce(sum(total), 0)
            from orders where customer_id = ?""";
    private static final String EXISTS_SQL =
            "select 1 from customers where id = ?";

    private final DataSource dataSource;

    public CustomerSummaryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CustomerSummary summary(Long customerId) throws SQLException {
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(SUMMARY_SQL)) {
            statement.setLong(1, customerId);
            long orderCount;
            BigDecimal totalSpent;
            try (var rows = statement.executeQuery()) {
                rows.next();
                orderCount = rows.getLong(1);
                totalSpent = rows.getBigDecimal(2);
            }
            if (orderCount == 0 && !exists(connection, customerId)) {
                throw new NotFoundException("Customer", customerId);
            }
            return new CustomerSummary(customerId, orderCount, totalSpent);
        }
    }

    private static boolean exists(Connection connection, Long customerId)
            throws SQLException {
        try (var statement = connection.prepareStatement(EXISTS_SQL)) {
            statement.setLong(1, customerId);
            try (var rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }
}
