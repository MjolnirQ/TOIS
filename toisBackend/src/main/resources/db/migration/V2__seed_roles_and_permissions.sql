insert into permission (code)
values
    ('admin.roles.read'),
    ('admin.roles.write'),
    ('admin.permissions.read'),
    ('admin.permissions.write'),
    ('admin.users.read'),
    ('admin.users.write'),
    ('seller.customers.write'),
    ('seller.catalog.read'),
    ('seller.sales.write'),
    ('seller.supply-requests.write'),
    ('seller.reports.read'),
    ('manager.suppliers.read'),
    ('manager.suppliers.write'),
    ('manager.supplier-products.read'),
    ('manager.supplier-products.write'),
    ('manager.supply-requests.read'),
    ('manager.supply-requests.write'),
    ('manager.supplier-orders.read'),
    ('manager.supplier-orders.write'),
    ('manager.deliveries.read'),
    ('manager.deliveries.write'),
    ('manager.transfers.read'),
    ('manager.transfers.write'),
    ('manager.catalog.read'),
    ('manager.customers.read'),
    ('manager.customers.write'),
    ('manager.sales.read'),
    ('manager.sales.write'),
    ('manager.stocks.read'),
    ('manager.stocks.write'),
    ('manager.products.read'),
    ('manager.products.write'),
    ('manager.prices.write'),
    ('manager.employees.read'),
    ('manager.employees.write'),
    ('manager.trade-points.read'),
    ('manager.trade-points.write'),
    ('manager.assignments.read'),
    ('manager.assignments.write'),
    ('manager.reports.read')
on conflict (code) do nothing;

insert into "role" (name)
values
    ('SELLER_ROLE'),
    ('TRADE_MANAGER_ROLE'),
    ('SUPERADMIN_ROLE')
on conflict (name) do nothing;

insert into role_permission (role_id, permission_id)
select r.id, p.id
from "role" r
join permission p on p.code in (
    'seller.customers.write',
    'seller.catalog.read',
    'seller.sales.write',
    'seller.supply-requests.write',
    'seller.reports.read'
)
where r.name = 'SELLER_ROLE'
on conflict do nothing;

insert into role_permission (role_id, permission_id)
select r.id, p.id
from "role" r
join permission p on p.code in (
    'manager.suppliers.read',
    'manager.suppliers.write',
    'manager.supplier-products.read',
    'manager.supplier-products.write',
    'manager.supply-requests.read',
    'manager.supply-requests.write',
    'manager.supplier-orders.read',
    'manager.supplier-orders.write',
    'manager.deliveries.read',
    'manager.deliveries.write',
    'manager.transfers.read',
    'manager.transfers.write',
    'manager.catalog.read',
    'manager.customers.read',
    'manager.customers.write',
    'manager.sales.read',
    'manager.sales.write',
    'manager.stocks.read',
    'manager.stocks.write',
    'manager.products.read',
    'manager.products.write',
    'manager.prices.write',
    'manager.employees.read',
    'manager.employees.write',
    'manager.trade-points.read',
    'manager.trade-points.write',
    'manager.assignments.read',
    'manager.assignments.write',
    'manager.reports.read'
)
where r.name = 'TRADE_MANAGER_ROLE'
