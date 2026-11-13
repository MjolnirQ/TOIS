package ru.nsu.tois.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.response.CountedReportResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final JdbcTemplate jdbcTemplate;

    public CountedReportResponse suppliersForProduct(Integer productId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select s.id, s.organization_name, s.is_active, sp.supplier_price
                from supplier s
                join supplier_product sp on sp.supplier_id = s.id
                where sp.product_id = ?
                order by s.organization_name
                """, productId);

        return new CountedReportResponse(rows.size(), rows);
    }

    public CountedReportResponse suppliersDeliveredProduct(Integer productId, Integer minQuantity, LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        List<Object> params = new ArrayList<>(List.of(productId));
        String dateFilter = "";
        if (hasPeriod(from, to)) {
            dateFilter = " and d.delivery_date between ? and ? ";
            params.add(from);
            params.add(to);
        }
        params.add(minQuantity);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select s.id, s.organization_name, sum(di.quantity) as total_quantity
                from supplier s
                join delivery d on d.supplier_id = s.id
                join delivery_item di on di.delivery_id = d.id
                where di.product_id = ?
                """ + dateFilter + """
                group by s.id, s.organization_name
                having sum(di.quantity) >= ?
                order by total_quantity desc
                """, params.toArray());

        return new CountedReportResponse(rows.size(), rows);
    }

    public CountedReportResponse customersBoughtProduct(Integer productId, LocalDate from, LocalDate to) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select distinct c.id, c.full_name, c.phone_number
                from customer c
                join sale s on s.customer_id = c.id
                join sale_item si on si.sale_id = s.id
                where si.product_id = ?
                  and s.sale_date between ? and ?
                order by c.full_name
                """, productId, from, to);

        return new CountedReportResponse(rows.size(), rows);
    }

    public CountedReportResponse customersBoughtProductAtLeast(Integer productId, Integer minQuantity) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select c.id, c.full_name, c.phone_number, sum(si.quantity) as total_quantity
                from customer c
                join sale s on s.customer_id = c.id
                join sale_item si on si.sale_id = s.id
                where si.product_id = ?
                group by c.id, c.full_name, c.phone_number
                having sum(si.quantity) >= ?
                order by total_quantity desc
                """, productId, minQuantity);

        return new CountedReportResponse(rows.size(), rows);
    }

    public List<Map<String, Object>> tradePointAssortment(Integer tradePointId) {
        return jdbcTemplate.queryForList("""
                select p.name as product_name, stock.quantity, tp.address as trade_point_address
                from trade_point_product_stock stock
                join product p on p.id = stock.product_id
                join trade_point tp on tp.id = stock.trade_point_id
                where stock.trade_point_id = ?
                order by p.name
                """, tradePointId);
    }

    public List<Map<String, Object>> sellerTradePointStock(Integer tradePointId) {
        return jdbcTemplate.queryForList("""
                select p.id as product_id, p.name as product_name, stock.quantity
                from trade_point_product_stock stock
                join product p on p.id = stock.product_id
                where stock.trade_point_id = ?
                order by p.name
                """, tradePointId);
    }

    public List<Map<String, Object>> productStockAndPrices(Integer productId) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name, tp.type::text as trade_point_type,
                       stock.quantity, price.price
                from trade_point tp
                left join trade_point_product_stock stock
                    on stock.trade_point_id = tp.id and stock.product_id = ?
                left join trade_point_product_price price
                    on price.trade_point_id = tp.id and price.product_id = ?
                where stock.product_id is not null or price.product_id is not null
                order by tp.name
                """, productId, productId);
    }

    public List<Map<String, Object>> productStockAndPricesByType(Integer productId, String tradePointType) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name, tp.type::text as trade_point_type,
                       stock.quantity, price.price
                from trade_point tp
                left join trade_point_product_stock stock
                    on stock.trade_point_id = tp.id and stock.product_id = ?
                left join trade_point_product_price price
                    on price.trade_point_id = tp.id and price.product_id = ?
                where tp.type::text = ?
                  and (stock.product_id is not null or price.product_id is not null)
                order by tp.name
                """, productId, productId, tradePointType);
    }

    public List<Map<String, Object>> productStockAndPricesAtTradePoint(Integer productId, Integer tradePointId) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name, tp.type::text as trade_point_type,
                       stock.quantity, price.price
                from trade_point tp
                left join trade_point_product_stock stock
                    on stock.trade_point_id = tp.id and stock.product_id = ?
                left join trade_point_product_price price
                    on price.trade_point_id = tp.id and price.product_id = ?
                where tp.id = ?
                """, productId, productId, tradePointId);
    }

    public List<Map<String, Object>> salesPerSeller(LocalDate from, LocalDate to, String tradePointType) {
        List<Object> params = new ArrayList<>(List.of(from, to));
        String salesTypeFilter = "";
        String sellerJoin = "";
        String sellerTypeFilter = "";
        if (tradePointType != null && !tradePointType.isBlank()) {
            salesTypeFilter = " and tp.type::text = ? ";
            params.add(tradePointType);
            sellerJoin = """
                    join seller_assignment sa on sa.seller_id = sel.employee_id
                    join trade_point seller_tp on seller_tp.id = sa.trade_point_id
                    """;
            sellerTypeFilter = " and seller_tp.type::text = ? ";
            params.add(tradePointType);
        }

        return jdbcTemplate.queryForList("""
                with sales_total as (
                    select coalesce(sum(si.quantity * si.unit_price), 0) as total_sales
                    from sale s
                    join sale_item si on si.sale_id = s.id
                    join trade_point tp on tp.id = s.trade_point_id
                    where s.sale_date between ? and ?
                """ + salesTypeFilter + """
                ),
                seller_total as (
                    select count(distinct e.id) as seller_count
                    from employee e
                    join seller sel on sel.employee_id = e.id
                """ + sellerJoin + """
                    where e.position = 'SELLER'
                """ + sellerTypeFilter + """
                )
                select seller_total.seller_count,
                       sales_total.total_sales,
                       sales_total.total_sales / nullif(seller_total.seller_count, 0) as sales_per_seller
                from sales_total
                cross join seller_total
                """, params.toArray());
    }

    public List<Map<String, Object>> sellerOutput(Integer sellerId, Integer tradePointId, LocalDate from, LocalDate to) {
        return jdbcTemplate.queryForList("""
                select s.seller_id, e.full_name as seller_name, s.trade_point_id, tp.name as trade_point_name,
                       coalesce(sum(si.quantity * si.unit_price), 0) as output
                from sale s
                join sale_item si on si.sale_id = s.id
                join employee e on e.id = s.seller_id
                join trade_point tp on tp.id = s.trade_point_id
                where s.seller_id = ?
                  and s.trade_point_id = ?
                  and s.sale_date between ? and ?
                group by s.seller_id, e.full_name, s.trade_point_id, tp.name
                """, sellerId, tradePointId, from, to);
    }

    public List<Map<String, Object>> productSalesVolume(
            Integer productId,
            LocalDate from,
            LocalDate to,
            String tradePointType,
            Integer tradePointId
    ) {
        List<Object> params = new ArrayList<>(List.of(productId, from, to));
        String filter = "";
        if (tradePointType != null && !tradePointType.isBlank()) {
            filter += " and tp.type::text = ? ";
            params.add(tradePointType);
        }
        if (tradePointId != null) {
            filter += " and tp.id = ? ";
            params.add(tradePointId);
        }

        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name, tp.type::text as trade_point_type,
                       sum(si.quantity) as sold_quantity,
                       sum(si.quantity * si.unit_price) as sales_volume
                from sale s
                join sale_item si on si.sale_id = s.id
                join trade_point tp on tp.id = s.trade_point_id
                where si.product_id = ?
                  and s.sale_date between ? and ?
                """ + filter + """
                group by tp.id, tp.name, tp.type
                order by sales_volume desc
                """, params.toArray());
    }

    public List<Map<String, Object>> sellerSalaries(String tradePointType, Integer tradePointId) {
        List<Object> params = new ArrayList<>();
        String filter = "";
        if (tradePointType != null && !tradePointType.isBlank()) {
            filter += " and tp.type::text = ? ";
            params.add(tradePointType);
        }
        if (tradePointId != null) {
            filter += " and tp.id = ? ";
            params.add(tradePointId);
        }

        return jdbcTemplate.queryForList("""
                select distinct e.id as seller_id, e.full_name, e.salary, tp.id as trade_point_id, tp.name as trade_point_name
                from employee e
                join seller sel on sel.employee_id = e.id
                left join seller_assignment sa on sa.seller_id = sel.employee_id
                left join trade_point tp on tp.id = sa.trade_point_id
                where e.position = 'SELLER'
                """ + filter + " order by e.full_name", params.toArray());
    }

    public List<Map<String, Object>> supplierDeliveriesForProduct(Integer supplierId, Integer productId, LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        List<Object> params = new ArrayList<>(List.of(supplierId, productId));
        String dateFilter = "";
        if (hasPeriod(from, to)) {
            dateFilter = " and d.delivery_date between ? and ? ";
            params.add(from);
            params.add(to);
        }

        return jdbcTemplate.queryForList("""
                select d.id as delivery_id, d.delivery_date, di.product_id, p.name as product_name,
                       di.quantity, di.delivery_price
                from delivery d
                join delivery_item di on di.delivery_id = d.id
                join product p on p.id = di.product_id
                where d.supplier_id = ?
                  and di.product_id = ?
                """ + dateFilter + """
                order by d.delivery_date desc
                """, params.toArray());
    }

    public List<Map<String, Object>> deliveriesByOrderNumber(String orderNumber) {
        return jdbcTemplate.queryForList("""
                select so.order_number, d.id as delivery_id, d.delivery_date,
                       p.id as product_id, p.name as product_name, di.quantity, di.delivery_price
                from supplier_order so
                join delivery d on d.supplier_order_id = so.id
                join delivery_item di on di.delivery_id = d.id
                join product p on p.id = di.product_id
                where so.order_number = ?
                order by d.delivery_date desc, p.name
                """, orderNumber);
    }

    public List<Map<String, Object>> salesToTradeAreaRatio(String tradePointType, LocalDate from, LocalDate to) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name,
                       coalesce(sum(si.quantity * si.unit_price), 0) as total_sales,
                       tp.trade_area,
                       coalesce(sum(si.quantity * si.unit_price), 0) / nullif(tp.trade_area, 0) as sales_to_area
                from trade_point tp
                left join sale s on s.trade_point_id = tp.id and s.sale_date between ? and ?
                left join sale_item si on si.sale_id = s.id
                where (? is null or tp.type::text = ?)
                group by tp.id, tp.name, tp.trade_area
                order by tp.name
                """, from, to, tradePointType, tradePointType);
    }

    public List<Map<String, Object>> salesToHallCountRatio(String tradePointType, LocalDate from, LocalDate to) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name,
                       coalesce(sum(si.quantity * si.unit_price), 0) as total_sales,
                       coalesce(halls.hall_count, 0) as hall_count,
                       coalesce(sum(si.quantity * si.unit_price), 0) / nullif(coalesce(halls.hall_count, 0), 0) as sales_to_hall_count
                from trade_point tp
                left join (
                    select trade_point_id, count(*) as hall_count
                    from hall
                    group by trade_point_id
                ) halls on halls.trade_point_id = tp.id
                left join sale s on s.trade_point_id = tp.id and s.sale_date between ? and ?
                left join sale_item si on si.sale_id = s.id
                where (? is null or tp.type::text = ?)
                group by tp.id, tp.name, halls.hall_count
                order by tp.name
                """, from, to, tradePointType, tradePointType);
    }

    public List<Map<String, Object>> salesToCounterCountRatio(String tradePointType, LocalDate from, LocalDate to) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name,
                       coalesce(sum(si.quantity * si.unit_price), 0) as total_sales,
                       tp.counter_count,
                       coalesce(sum(si.quantity * si.unit_price), 0) / nullif(tp.counter_count, 0) as sales_to_counter_count
                from trade_point tp
                left join sale s on s.trade_point_id = tp.id and s.sale_date between ? and ?
                left join sale_item si on si.sale_id = s.id
                where (? is null or tp.type::text = ?)
                group by tp.id, tp.name, tp.counter_count
                order by tp.name
                """, from, to, tradePointType, tradePointType);
    }

    public List<Map<String, Object>> tradePointProfitability(Integer tradePointId, LocalDate from, LocalDate to) {
        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name,
                       coalesce(sum(si.quantity * si.unit_price), 0) as total_sales,
                       coalesce((
                           select sum(e.salary)
                           from seller_assignment sa
                           join employee e on e.id = sa.seller_id
                           where sa.trade_point_id = tp.id
                       ), 0) + coalesce(tp.rent_payment, 0) + coalesce(tp.utility_payments, 0) as overhead,
                       coalesce(sum(si.quantity * si.unit_price), 0)
                           / nullif(
                               coalesce((
                                   select sum(e.salary)
                                   from seller_assignment sa
                                   join employee e on e.id = sa.seller_id
                                   where sa.trade_point_id = tp.id
                               ), 0) + coalesce(tp.rent_payment, 0) + coalesce(tp.utility_payments, 0),
                               0
                           ) as profitability
                from trade_point tp
                left join sale s on s.trade_point_id = tp.id and s.sale_date between ? and ?
                left join sale_item si on si.sale_id = s.id
                where tp.id = ?
                group by tp.id, tp.name, tp.rent_payment, tp.utility_payments
                """, from, to, tradePointId);
    }

    public List<Map<String, Object>> productCustomers(Integer productId, LocalDate from, LocalDate to, String tradePointType, Integer tradePointId) {
        validatePeriod(from, to);
        List<Object> params = new ArrayList<>(List.of(productId));
        String dateFilter = "";
        if (hasPeriod(from, to)) {
            dateFilter = " and s.sale_date between ? and ? ";
            params.add(from);
            params.add(to);
        }
        String filter = "";
        if (tradePointType != null && !tradePointType.isBlank()) {
            filter += " and tp.type::text = ? ";
            params.add(tradePointType);
        }
        if (tradePointId != null) {
            filter += " and tp.id = ? ";
            params.add(tradePointId);
        }

        return jdbcTemplate.queryForList("""
                select distinct c.id, c.full_name, c.phone_number
                from customer c
                join sale s on s.customer_id = c.id
                join trade_point tp on tp.id = s.trade_point_id
                join sale_item si on si.sale_id = s.id
                where si.product_id = ?
                """ + dateFilter + filter + " order by c.full_name", params.toArray());
    }

    public List<Map<String, Object>> activeCustomers(LocalDate from, LocalDate to, String tradePointType, Integer tradePointId) {
        validatePeriod(from, to);
        List<Object> params = new ArrayList<>();
        String filter = "";
        if (hasPeriod(from, to)) {
            filter += " and s.sale_date between ? and ? ";
            params.add(from);
            params.add(to);
        }
        if (tradePointType != null && !tradePointType.isBlank()) {
            filter += " and tp.type::text = ? ";
            params.add(tradePointType);
        }
        if (tradePointId != null) {
            filter += " and tp.id = ? ";
            params.add(tradePointId);
        }

        return jdbcTemplate.queryForList("""
                select c.id, c.full_name, c.phone_number, sum(si.quantity * si.unit_price) as total_spent
                from customer c
                join sale s on s.customer_id = c.id
                join trade_point tp on tp.id = s.trade_point_id
                join sale_item si on si.sale_id = s.id
                where 1 = 1
                """ + filter + """
                group by c.id, c.full_name, c.phone_number
                order by total_spent desc
                """, params.toArray());
    }

    public List<Map<String, Object>> turnover(LocalDate from, LocalDate to, String tradePointType, Integer tradePointId) {
        List<Object> params = new ArrayList<>(List.of(from, to));
        String filter = "";
        if (tradePointType != null && !tradePointType.isBlank()) {
            filter += " and tp.type::text = ? ";
            params.add(tradePointType);
        }
        if (tradePointId != null) {
            filter += " and tp.id = ? ";
            params.add(tradePointId);
        }

        return jdbcTemplate.queryForList("""
                select tp.id as trade_point_id, tp.name as trade_point_name, tp.type::text as trade_point_type,
                       coalesce(sum(si.quantity * si.unit_price), 0) as turnover
                from trade_point tp
                left join sale s on s.trade_point_id = tp.id and s.sale_date between ? and ?
                left join sale_item si on si.sale_id = s.id
                where 1 = 1
                """ + filter + """
                group by tp.id, tp.name, tp.type
                order by turnover desc
                """, params.toArray());
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("from and to must be provided together");
        }
    }

    private boolean hasPeriod(LocalDate from, LocalDate to) {
        return from != null && to != null;
    }
}
