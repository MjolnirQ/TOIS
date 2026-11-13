package ru.nsu.tois.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class StockService {
    private final JdbcTemplate jdbcTemplate;

    public boolean create(int tradePointId, int productId, int quantity) {
        return jdbcTemplate.update("""
                insert into trade_point_product_stock (trade_point_id, product_id, quantity)
                values (?, ?, ?)
                on conflict (trade_point_id, product_id) do nothing
                """, tradePointId, productId, quantity) == 1;
    }

    public void increase(int tradePointId, int productId, int quantity) {
        jdbcTemplate.update("""
                insert into trade_point_product_stock (trade_point_id, product_id, quantity)
                values (?, ?, ?)
                on conflict (trade_point_id, product_id) do update
                set quantity = trade_point_product_stock.quantity + excluded.quantity
                """, tradePointId, productId, quantity);
    }

    public void decrease(int tradePointId, int productId, int quantity) {
        int updatedRows = jdbcTemplate.update("""
                update trade_point_product_stock
                set quantity = quantity - ?
                where trade_point_id = ? and product_id = ? and quantity >= ?
                """, quantity, tradePointId, productId, quantity);

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Insufficient stock for product " + productId + " at trade point " + tradePointId
            );
        }
    }

    public void transfer(int senderId, int receiverId, int productId, int quantity) {
        // Take the two row locks in the same order regardless of transfer direction.
        if (senderId < receiverId) {
            decrease(senderId, productId, quantity);
            increase(receiverId, productId, quantity);
        } else {
            increase(receiverId, productId, quantity);
            decrease(senderId, productId, quantity);
        }
    }
}
