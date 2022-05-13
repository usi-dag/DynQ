
const Q1 = `
select
    lineitem.l_returnflag,
    lineitem.l_linestatus,
    sum(lineitem.l_quantity) as sum_qty,
    sum(lineitem.l_extendedprice) as sum_base_price,
    sum(lineitem.l_extendedprice * (1 - lineitem.l_discount)) as sum_disc_price,
    sum(lineitem.l_extendedprice * (1 - lineitem.l_discount) * (1 + l_tax)) as sum_charge,
    avg(lineitem.l_quantity) as avg_qty,
    avg(lineitem.l_extendedprice) as avg_price,
    avg(lineitem.l_discount) as avg_disc,
    count(*) as count_order
from lineitem
where lineitem.l_shipdate <= date '1998-12-01' - interval '90' day
group by
    lineitem.l_returnflag,
    lineitem.l_linestatus
order by
    lineitem.l_returnflag,
    lineitem.l_linestatus
`




const Q2 = `
with
reg_nat as (
    select nation.n_nationkey, nation.n_name 
    from region, nation 
    where region.r_regionkey = nation.n_regionkey 
      and region.r_name='EUROPE'),
nat_sup as (
    select supplier.s_suppkey, supplier.s_acctbal, supplier.s_name, 
           reg_nat.n_name, supplier.s_address, supplier.s_phone, supplier.s_comment
    from reg_nat, supplier 
    where supplier.s_nationkey = reg_nat.n_nationkey),
sup_psup as (
    select partsupp.ps_partkey, partsupp.ps_supplycost,
           nat_sup.s_acctbal, nat_sup.s_name, nat_sup.n_name, 
           nat_sup.s_address, nat_sup.s_phone, nat_sup.s_comment
    from nat_sup, partsupp
    where partsupp.ps_suppkey = nat_sup.s_suppkey),
brass as (
    select sup_psup.ps_partkey, sup_psup.ps_supplycost,
           sup_psup.s_acctbal, sup_psup.s_name, sup_psup.n_name,
           part.p_partkey, part.p_mfgr,
           sup_psup.s_address, sup_psup.s_phone, sup_psup.s_comment
    from sup_psup, part
    where sup_psup.ps_partkey = part.p_partkey
      and part.p_size = 15
      and part.p_type like '%BRASS'),
mincost as (
    select min(ps_supplycost) as min_ps_supplycost, ps_partkey as mc_partkey
    from brass
    group by ps_partkey)

select
    s_acctbal,
    s_name,
    n_name,
    p_partkey,
    p_mfgr,
    s_address,
    s_phone,
    s_comment
from mincost, brass
where min_ps_supplycost = ps_supplycost
and mc_partkey = ps_partkey -- don't get this, it should be already done
order by
    s_acctbal desc,
    n_name,
    s_name,
    p_partkey
limit 100
`



const Q3 = `
select
    lineitem.l_orderkey,
    sum(lineitem.l_extendedprice * (1 - lineitem.l_discount)) as revenue,
    orders.o_orderdate,
    orders.o_shippriority
from
    customer,
    orders,
    lineitem
where customer.c_mktsegment = 'BUILDING'
  and customer.c_custkey = orders.o_custkey
  and lineitem.l_orderkey = orders.o_orderkey
  and orders.o_orderdate < date '1995-03-15'
  and lineitem.l_shipdate > date '1995-03-15'
group by
    lineitem.l_orderkey,
    orders.o_orderdate,
    orders.o_shippriority
order by
    revenue desc,
    orders.o_orderdate
limit 10`



const Q4 = `
select orders.o_orderpriority, count(*) as order_count
from orders
where orders.o_orderdate >= date '1993-07-01'
  and orders.o_orderdate < date '1993-07-01' + interval '3' month
  and exists (
      select * 
      from lineitem
      where lineitem.l_orderkey = orders.o_orderkey
        and lineitem.l_commitdate < lineitem.l_receiptdate)
group by orders.o_orderpriority
order by orders.o_orderpriority
`

const Q5opt = `
with
reg_nat as (
    select nation.n_nationkey, nation.n_name
    from region, nation
    where region.r_regionkey = nation.n_regionkey
      and region.r_name = 'ASIA'),
reg_nat_cust as (
    select reg_nat.n_nationkey, reg_nat.n_name, customer.c_custkey
    from reg_nat, customer
    where reg_nat.n_nationkey = customer.c_nationkey),
reg_nat_cust_ord as (
    select reg_nat_cust.n_nationkey, reg_nat_cust.n_name, orders.o_orderkey
    from reg_nat_cust, orders
    where reg_nat_cust.c_custkey = orders.o_custkey
      and orders.o_orderdate >= date '1994-01-01'
      and orders.o_orderdate < date '1995-01-01'),
reg_nat_cust_ord_line as (
    select
    reg_nat_cust_ord.n_nationkey,
    reg_nat_cust_ord.n_name,
    lineitem.l_suppkey,
    lineitem.l_discount,
    lineitem.l_extendedprice
    from reg_nat_cust_ord, lineitem
    where reg_nat_cust_ord.o_orderkey = lineitem.l_orderkey)

select
    rncol.n_name,
    sum(rncol.l_extendedprice * (1 - rncol.l_discount)) as revenue
from supplier, reg_nat_cust_ord_line rncol
where supplier.s_suppkey = rncol.l_suppkey
and supplier.s_nationkey = rncol.n_nationkey
group by rncol.n_name
order by revenue desc
`

// aft
const Q5 = `
with
reg_nat as (
    select nation.n_nationkey, nation.n_name
    from region, nation
    where region.r_regionkey = nation.n_regionkey
      and region.r_name = 'ASIA'),
nat_sup as (
    select supplier.s_nationkey, supplier.s_suppkey, reg_nat.n_name
    from reg_nat, supplier
    where reg_nat.n_nationkey = supplier.s_nationkey),
sup_line as (
    select lineitem.l_orderkey, lineitem.l_extendedprice, lineitem.l_discount,
           nat_sup.s_nationkey, nat_sup.n_name
    from nat_sup, lineitem
    where nat_sup.s_suppkey = lineitem.l_suppkey),  
cus_ord as (
    select orders.o_orderkey, customer.c_nationkey
    from customer, orders
    where customer.c_custkey = orders.o_custkey
      and orders.o_orderdate >= date '1994-01-01'
      and orders.o_orderdate < date '1995-01-01')

select n_name, sum(l_extendedprice * (1 - l_discount)) as revenue
from cus_ord, sup_line
where o_orderkey = l_orderkey
  and c_nationkey = s_nationkey 
group by n_name
order by revenue desc
`

const Q6 = `
select sum(lineitem.l_extendedprice * lineitem.l_discount) as revenue
from lineitem
where lineitem.l_shipdate >= date '1994-01-01'
  and lineitem.l_shipdate < date '1994-01-01' + interval '1' year
  and lineitem.l_discount between .0499999 and .0700001
  and lineitem.l_quantity < 24
`



const Q7 = `
with
cus_nat as (
    select customer.c_custkey, nation.n_name as cust_nation
    from nation, customer
    where nation.n_nationkey = customer.c_nationkey
    and (nation.n_name = 'FRANCE' or nation.n_name = 'GERMANY')),
ord_cus as (
    select orders.o_orderkey, cus_nat.cust_nation
    from cus_nat, orders
    where cus_nat.c_custkey = orders.o_custkey),
sup_nat as (
    select supplier.s_suppkey, nation.n_name as supp_nation
    from nation, supplier
    where nation.n_nationkey = supplier.s_nationkey
    and (nation.n_name = 'FRANCE' or nation.n_name = 'GERMANY')),
sup_line as (
    select
        lineitem.l_orderkey,
        year(lineitem.l_shipdate) as l_year,
        lineitem.l_extendedprice,
        lineitem.l_discount,
        sup_nat.supp_nation
    from sup_nat, lineitem
    where lineitem.l_suppkey = sup_nat.s_suppkey
    and lineitem.l_shipdate between date '1995-01-01' and date '1996-12-31')

select
    supp_nation,
    cust_nation,
    l_year,
    sum(l_extendedprice * (1 - l_discount)) as revenue
from ord_cus, sup_line
where ord_cus.o_orderkey = sup_line.l_orderkey
and ((supp_nation = 'FRANCE' and cust_nation = 'GERMANY')
    or (supp_nation = 'GERMANY' and cust_nation = 'FRANCE'))
group by
    supp_nation,
    cust_nation,
    l_year
order by
    supp_nation,
    cust_nation,
    l_year
`



const Q8 = `
with
nat_region as (
    select nation.n_nationkey as cust_nationkey
    from region, nation
    where region.r_regionkey = nation.n_regionkey
    and region.r_name = 'AMERICA'),
cus_nat as (
    select customer.c_custkey
    from nat_region, customer
    where nat_region.cust_nationkey = customer.c_nationkey),
ord_cus as (
    select orders.o_orderkey, year(orders.o_orderdate) as o_year
    from cus_nat, orders
    where cus_nat.c_custkey = orders.o_custkey
    and orders.o_orderdate between date '1995-01-01' and date '1996-12-31'),
par_line as (
    select
        lineitem.l_discount,
        lineitem.l_extendedprice,
        lineitem.l_suppkey,
        lineitem.l_orderkey
    from part, lineitem
    where part.p_partkey = lineitem.l_partkey
    and part.p_type = 'ECONOMY ANODIZED STEEL'),
sup_nat as (
    select supplier.s_suppkey, nation.n_name as nation
    from nation, supplier
    where nation.n_nationkey = supplier.s_nationkey),
par_line_sup as(
    select *
    from sup_nat, par_line
    where s_suppkey = l_suppkey)

select
    o_year,
    sum(case when nation = 'BRAZIL' then l_extendedprice * (1 - l_discount)  else 0 end)
        / sum(l_extendedprice * (1 - l_discount) ) as mkt_share
from par_line_sup, ord_cus
where l_orderkey = o_orderkey
group by o_year
order by o_year
`



const Q9opt = `
with
    sup_nat as (
    select nation.n_name, supplier.s_suppkey
from nation, supplier
where nation.n_nationkey = supplier.s_nationkey),

par_supp as(
    select part.p_partkey, partsupp.ps_suppkey, partsupp.ps_supplycost, partsupp.ps_partkey
    from part, partsupp
    where part.p_partkey = partsupp.ps_partkey
    and part.p_name like '%green%'),

sn_ps as (
    select * from sup_nat, par_supp
    where s_suppkey = ps_suppkey),

snps_line as (
    select
        sn_ps.n_name,
        sn_ps.ps_supplycost,
        lineitem.l_extendedprice,
        lineitem.l_discount,
        lineitem.l_quantity,
        lineitem.l_orderkey
    from sn_ps, lineitem
    where sn_ps.ps_partkey = lineitem.l_partkey
    and sn_ps.ps_suppkey = lineitem.l_suppkey),

profit as (
    select
        snps_line.n_name as nation,
        year(orders.o_orderdate) as o_year,
        snps_line.l_extendedprice * (1 - snps_line.l_discount) -
        snps_line.ps_supplycost * snps_line.l_quantity as amount
    from snps_line, orders
    where snps_line.l_orderkey = orders.o_orderkey)

select nation, o_year, sum(amount) as sum_profit 
from profit
group by nation, o_year 
order by nation, o_year DESC
`

const Q9 = `
with 
sup_nat as (
    select nation.n_name as nation, supplier.s_suppkey
    from nation, supplier 
    where nation.n_nationkey = supplier.s_nationkey),
par_line as (
    select lineitem.l_suppkey, lineitem.l_orderkey, lineitem.l_extendedprice,
           lineitem.l_discount, lineitem.l_quantity, lineitem.l_partkey
    from part, lineitem
    where part.p_partkey = lineitem.l_partkey
      and part.p_name like '%green%'),
par_line_sup as (
    select nation, 
           l_suppkey, l_orderkey, l_extendedprice,
           l_discount, l_quantity, l_partkey
    from sup_nat, par_line
    where s_suppkey = l_suppkey),
     
par_line_sup_ps as (
    select par_line_sup.nation, par_line_sup.l_orderkey, par_line_sup.l_extendedprice,
           par_line_sup.l_discount, par_line_sup.l_quantity, partsupp.ps_supplycost
    from par_line_sup, partsupp
    where par_line_sup.l_partkey = partsupp.ps_partkey
    and par_line_sup.l_suppkey = partsupp.ps_suppkey
),
profit as (
    select
           par_line_sup_ps.nation,
           year(orders.o_orderdate) as o_year,
           par_line_sup_ps.l_extendedprice * (1 - par_line_sup_ps.l_discount) -
            par_line_sup_ps.ps_supplycost * par_line_sup_ps.l_quantity as amount
    from par_line_sup_ps, orders
    where par_line_sup_ps.l_orderkey = orders.o_orderkey
)

select nation, o_year, sum(amount) as sum_profit 
from profit
group by nation, o_year 
order by nation, o_year DESC`


const Q10 = `
select
    customer.c_custkey,
    customer.c_name,
    sum(lineitem.l_extendedprice * (1 - lineitem.l_discount)) as revenue,
    customer.c_acctbal,
    nation.n_name,
    customer.c_address,
    customer.c_phone,
    customer.c_comment
from
    customer,
    orders,
    lineitem,
    nation
where customer.c_custkey = orders.o_custkey
    and lineitem.l_orderkey = orders.o_orderkey
    and orders.o_orderdate >= date '1993-10-01'
    and orders.o_orderdate < date '1993-10-01' + interval '3' month
    and lineitem.l_returnflag = 'R'
    and customer.c_nationkey = nation.n_nationkey
group by
    customer.c_custkey,
    customer.c_name,
    customer.c_acctbal,
    customer.c_phone,
    nation.n_name,
    customer.c_address,
    customer.c_comment
order by revenue desc
limit 20
`


const Q11 = `
with
sup_nat as (
    select supplier.s_suppkey
    from nation, supplier
    where nation.n_nationkey = supplier.s_nationkey
    and nation.n_name = 'GERMANY'),
ps_sup as (
    select partsupp.ps_partkey, sum(partsupp.ps_supplycost * partsupp.ps_availqty) as value1
    from sup_nat, partsupp
    where sup_nat.s_suppkey = partsupp.ps_suppkey
    group by partsupp.ps_partkey),
value1_sum as (select sum(value1)*0.0001 as v from ps_sup)

select ps_partkey, value1
from value1_sum, ps_sup
where value1 > v
order by value1 desc
`

const Q12 = `
select
    lineitem.l_shipmode,
    sum(case
            when orders.o_orderpriority = '1-URGENT' or orders.o_orderpriority = '2-HIGH'
                then 1
            else 0
        end) as high_line_count,
    sum(case
            when orders.o_orderpriority <> '1-URGENT' and orders.o_orderpriority <> '2-HIGH'
                then 1
            else 0
        end) as low_line_count
from orders, lineitem
where lineitem.l_orderkey = orders.o_orderkey
  and lineitem.l_shipmode in ('MAIL', 'SHIP')
  and lineitem.l_commitdate < lineitem.l_receiptdate
  and lineitem.l_shipdate < lineitem.l_commitdate
  and lineitem.l_receiptdate >= date '1994-01-01'
  and lineitem.l_receiptdate < date '1994-01-01' + interval '1' year
group by lineitem.l_shipmode
order by lineitem.l_shipmode
`



const Q13 = `
select
    c_count,
    count(*) as custdist
from (
    select
        customer.c_custkey,
        count(orders.o_orderkey) as c_count
    from customer left outer join orders 
        on customer.c_custkey = orders.o_custkey
        and orders.o_comment not like '%special%requests%'
    group by customer.c_custkey
) as c_orders
group by c_count
order by custdist desc, c_count desc
`


const Q14 = `
select 
       100.00 *
       sum(case
        when part.p_type like 'PROMO%'
        then lineitem.l_extendedprice * (1 - lineitem.l_discount)
        else 0
       end) / 
       sum(lineitem.l_extendedprice * (1 - lineitem.l_discount)) as promo_revenue
from part, lineitem
where lineitem.l_partkey = part.p_partkey
and lineitem.l_shipdate >= date '1995-09-01'
and lineitem.l_shipdate < date '1995-09-01' + interval '1' month
`



const Q15 = `
with
    revenue0 as (
    select 
        l_suppkey as supplier_no,
        sum(l_extendedprice * (1 - l_discount)) as total_revenue
    from lineitem
    where l_shipdate >= date '1996-01-01'
    and l_shipdate < date '1996-01-01' + interval '3' month
group by l_suppkey),
     
max_rev as (select max(total_revenue) as max_total_rev from revenue0)

select
    supplier.s_suppkey,
    supplier.s_name,
    supplier.s_address,
    supplier.s_phone,
    revenue0.total_revenue
from max_rev, revenue0, supplier
where revenue0.supplier_no = supplier.s_suppkey
and revenue0.total_revenue = max_rev.max_total_rev
order by supplier.s_suppkey
`



const Q16 = `
select
    part.p_brand,
    part.p_type,
    part.p_size,
    count(distinct partsupp.ps_suppkey) as supplier_cnt
from part, partsupp
where part.p_partkey = partsupp.ps_partkey
and part.p_brand <> 'Brand#45'
and part.p_type not like 'MEDIUM POLISHED%'
and part.p_size in (49, 14, 23, 45, 19, 3, 36, 9)
and not exists (
    select *
    from supplier
    where supplier.s_comment like '%Customer%Complaints%'
    and partsupp.ps_suppkey = supplier.s_suppkey
)
group by
    part.p_brand,
    part.p_type,
    part.p_size
order by
    supplier_cnt desc,
    part.p_brand,
    part.p_type,
    part.p_size
`



const Q17 = `
with
avg_q as (
    select l_partkey, avg(l_quantity) as avg_q
    from lineitem
    group by l_partkey),
part_scan as (select p_partkey from part where p_brand = 'Brand#23' and p_container = 'MED BOX'),
par_line_avg as (
    select p_partkey, avg_q
    from part_scan, avg_q
    where p_partkey = l_partkey)

select sum(lineitem.l_extendedprice / 7.0) as avg_yearly
from par_line_avg, lineitem
where par_line_avg.p_partkey = lineitem.l_partkey
and lineitem.l_quantity < (0.2 * par_line_avg.avg_q)
`

const Q18orig = `
select
    customer.c_name,
    customer.c_custkey,
    orders.o_orderkey,
    orders.o_orderdate,
    orders.o_totalprice,
    sum(lineitem.l_quantity) as sum_qty
from
    customer,
    orders,
    lineitem
where orders.o_orderkey in (
    select lineitem.l_orderkey
    from lineitem
    group by lineitem.l_orderkey 
    having sum(lineitem.l_quantity) > 300)
and customer.c_custkey = orders.o_custkey
and orders.o_orderkey = lineitem.l_orderkey
group by
    customer.c_name,
    customer.c_custkey,
    orders.o_orderkey,
    orders.o_orderdate,
    orders.o_totalprice
order by
    orders.o_totalprice desc,
    orders.o_orderdate
limit 100
`

const Q18 = `
with 
all_sums as (
    select l_orderkey, sum(l_quantity) as q_sums
    from lineitem
    group by l_orderkey),
ord_cus as (
    select customer.c_name, customer.c_custkey,
           orders.o_orderkey, orders.o_orderdate, orders.o_totalprice
    from customer, orders
    where customer.c_custkey = orders.o_custkey
    and orders.o_orderkey in (select l_orderkey from all_sums where q_sums > 300))

select
    ord_cus.c_name,
    ord_cus.c_custkey,
    ord_cus.o_orderkey,
    ord_cus.o_orderdate,
    ord_cus.o_totalprice,
	sum(lineitem.l_quantity) as sum_qty
from ord_cus, lineitem
where ord_cus.o_orderkey = lineitem.l_orderkey
group by
    ord_cus.c_name,
    ord_cus.c_custkey,
    ord_cus.o_orderkey,
    ord_cus.o_orderdate,
    ord_cus.o_totalprice
order by
    ord_cus.o_totalprice desc,
    ord_cus.o_orderdate
limit 100
`


const Q19 = `
select sum(lineitem.l_extendedprice* (1 - lineitem.l_discount)) as revenue
from part, lineitem
where part.p_partkey = lineitem.l_partkey
and lineitem.l_shipmode in ('AIR', 'AIR REG')
and lineitem.l_shipinstruct = 'DELIVER IN PERSON'
and (
        (
        part.p_brand = 'Brand#12'
        and part.p_container in ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG')
        and lineitem.l_quantity >= 1 and lineitem.l_quantity <= 1 + 10
        and part.p_size between 1 and 5
        )
    or
        (
        part.p_brand = 'Brand#23'
        and part.p_container in ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK')
        and lineitem.l_quantity >= 10 and lineitem.l_quantity <= 10 + 10
        and part.p_size between 1 and 10
        )
    or
        (
        part.p_brand = 'Brand#34'
        and part.p_container in ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG')
        and lineitem.l_quantity >= 20 and lineitem.l_quantity <= 20 + 10
        and part.p_size between 1 and 15
        )
)
`



const Q20 = `
    with
        ps_part as (
            select partsupp.ps_availqty, partsupp.ps_partkey, partsupp.ps_suppkey
            from part, partsupp
            where part.p_partkey = partsupp.ps_partkey
              and part.p_name like 'forest%'),

        group_lineitem as (
            select l_partkey, l_suppkey, sum(l_quantity) as sumq
            from lineitem
            where l_shipdate >= date '1994-01-01'
              and l_shipdate < date '1994-01-01' + interval '1' year
            group by l_partkey, l_suppkey),

        ps_line as (
            select *
            from ps_part, group_lineitem
            where ps_partkey = l_partkey
              and ps_suppkey = l_suppkey
              and ps_availqty > 0.5 * sumq),

        sup_nat as (
            select supplier.s_name, supplier.s_address, supplier.s_suppkey
            from nation, supplier
            where nation.n_nationkey = supplier.s_nationkey
              and nation.n_name = 'CANADA')

    select s_name, s_address
    from sup_nat
    where s_suppkey in (select ps_suppkey from ps_line)
    order by s_name
`

// afterburner
const Q20tmp = `
with
ps_part as (
    select partsupp.ps_availqty, partsupp.ps_partkey, partsupp.ps_suppkey
    from part, partsupp
    where part.p_partkey = partsupp.ps_partkey
    and part.p_name like 'forest%'),

group_lineitem as (
    select l_partkey, l_suppkey, sum(l_quantity) as sumq
    from lineitem
    where l_shipdate >= date '1994-01-01'
    and l_shipdate < date '1994-01-01' + interval '1' year
    group by l_partkey, l_suppkey),

ps_line as (
    select lineitem.l_partkey, lineitem.l_suppkey, sum(lineitem.l_quantity) as sumq
    from ps_part, lineitem
    where ps_part.ps_partkey = lineitem.l_partkey
    and ps_part.ps_suppkey = lineitem.l_suppkey
    and lineitem.l_shipdate >= date '1994-01-01'
    and lineitem.l_shipdate < date '1994-01-01' + interval '1' year
    group by lineitem.l_partkey, ps_availqty > 0.5 * sumq),

sup_nat as (
    select supplier.s_name, supplier.s_address, supplier.s_suppkey
    from nation, supplier
    where nation.n_nationkey = supplier.s_nationkey
    and nation.n_name = 'CANADA')

select s_name, s_address
from sup_nat
where s_suppkey in (select ps_suppkey from ps_line)
order by s_name
`



const Q21 = `
select supplier.s_name, count(*) as numwait
from
    supplier,
    lineitem l1,
    orders,
    nation
where supplier.s_suppkey = l1.l_suppkey
  and orders.o_orderkey = l1.l_orderkey
  and orders.o_orderstatus = 'F'
  and l1.l_receiptdate > l1.l_commitdate
  and supplier.s_nationkey = nation.n_nationkey
  and nation.n_name = 'SAUDI ARABIA'
  and exists (
      select *
      from lineitem l2
      where l2.l_orderkey = l1.l_orderkey
        and l2.l_suppkey <> l1.l_suppkey
    )
    and not exists (
        select * 
        from lineitem l3 
        where l3.l_orderkey = l1.l_orderkey
          and l3.l_suppkey <> l1.l_suppkey
          and l3.l_receiptdate > l3.l_commitdate
    )
group by supplier.s_name
order by numwait desc, supplier.s_name
limit 100
`



const Q22 = `
with
min_bal as (
    select c_acctbal
    from customer
    where c_acctbal > 0.00
    and substring(c_phone, 1, 2) in ('13', '31', '23', '29', '30', '18', '17')),

cus_ord as (
    select substring(c_phone, 1, 2) as cntrycode, c_acctbal
    from customer
    where c_acctbal > (select avg(c_acctbal) from min_bal)
    and substring(c_phone, 1, 2) in ('13', '31', '23', '29', '30', '18', '17')
    and not exists (select * from orders where orders.o_custkey = customer.c_custkey))

select cntrycode, count(*) as numcust, sum(c_acctbal) as totacctbal
from cus_ord
group by cntrycode
order by cntrycode
`



module.exports.queries = [
    undefined,
    Q1,
    Q2,
    Q3,
    Q4,
    Q5,
    Q6,
    Q7,
    Q8,
    Q9,
    Q10,
    Q11,
    Q12,
    Q13,
    Q14,
    Q15,
    Q16,
    Q17,
    Q18,
    Q19,
    Q20,
    Q21,
    Q22,
]