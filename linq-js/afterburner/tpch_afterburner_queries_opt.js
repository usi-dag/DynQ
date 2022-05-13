

const Q1 = `
select
	l_returnflag,
	l_linestatus,
	sum(l_quantity) as sum_qty,
	sum(l_extendedprice) as sum_base_price,
	sum(l_extendedprice * (1 - l_discount)) as sum_disc_price,
	sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge,
	avg(l_quantity) as avg_qty,
	avg(l_extendedprice) as avg_price,
	avg(l_discount) as avg_disc,
	count(*) as count_order
from
	lineitem
where
	l_shipdate <= date '1998-12-01' - interval '90' day
group by
	l_returnflag,
	l_linestatus
order by
	l_returnflag,
	l_linestatus
`



const Q2 = `
select
	s_acctbal,
	s_name,
	n_name,
	p_partkey,
	p_mfgr,
	s_address,
	s_phone,
	s_comment
from
	part,
	supplier,
	partsupp,
	nation,
	region
where
	p_partkey = ps_partkey
	and s_suppkey = ps_suppkey
	and p_size = 15
	and p_type like '%BRASS'
	and s_nationkey = n_nationkey
	and n_regionkey = r_regionkey
	and r_name = 'EUROPE'
	and ps_supplycost = (
		select
			min(ps_supplycost)
		from
			partsupp,
			supplier,
			nation,
			region
		where
			p_partkey = ps_partkey
			and s_suppkey = ps_suppkey
			and s_nationkey = n_nationkey
			and n_regionkey = r_regionkey
			and r_name = 'EUROPE'
	)
order by
	s_acctbal desc,
	n_name,
	s_name,
	p_partkey
limit 100
`

const Q2tmp = `
with 
reg_nat as (select * from region, nation where r_regionkey = n_regionkey and r_name='EUROPE'),
nat_sup as (select * from reg_nat, supplier where s_nationkey = n_nationkey),
sup_psup as (select * from nat_sup, partsupp where ps_suppkey = s_suppkey),
brass as (
    select * from sup_psup, part 
    where p_partkey = ps_partkey
    and p_size = 15 
    and p_type like '%BRASS'),
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
order by
	s_acctbal desc,
	n_name,
	s_name,
	p_partkey
limit 100
`



const Q3 = `
with cus_ord as (
    select * from orders, customer
    where c_custkey = o_custkey
    and c_mktsegment = 'BUILDING'
    and o_orderdate < date '1995-03-15'
)
select
	l_orderkey,
	sum(l_extendedprice * (1 - l_discount)) as revenue,
	o_orderdate,
	o_shippriority
from
	cus_ord,
	lineitem
where
	l_orderkey = o_orderkey
	and l_shipdate > date '1995-03-15'
group by
	l_orderkey,
	o_orderdate,
	o_shippriority
order by
	revenue desc,
	o_orderdate
limit 10
`



const Q4aft = `
with distinct_orders as (
    select l_orderkey from lineitem where l_commitdate < l_receiptdate group by l_orderkey)
select o_orderpriority, count(*) as order_count
from distinct_orders, orders
where o_orderkey = l_orderkey
and o_orderdate >= date '1993-07-01'
and o_orderdate < date '1993-07-01' + interval '3' month
group by o_orderpriority
order by o_orderpriority`

const Q4 = `
select o_orderpriority, count(*) as order_count
from orders
where
	o_orderdate >= date '1993-07-01'
	and o_orderdate < date '1993-07-01' + interval '3' month
	and exists (
		select
			*
		from
			lineitem
		where
			l_orderkey = o_orderkey
			and l_commitdate < l_receiptdate
	)
group by o_orderpriority
order by o_orderpriority`



const Q5aft = `
with
reg_nat as (select * from region, nation where r_regionkey = n_regionkey and r_name = 'ASIA'),
nat_sup as (select * from reg_nat, supplier where n_nationkey = s_nationkey),
sup_line as (select * from nat_sup, lineitem where s_suppkey = l_suppkey),
cus_ord as (
    select * from customer, orders 
    where c_custkey = o_custkey
    and o_orderdate >= date '1994-01-01'
    and o_orderdate < date '1995-01-01')

select n_name, sum(l_extendedprice * (1 - l_discount)) as revenue
from cus_ord, sup_line
where l_orderkey = o_orderkey and c_nationkey = s_nationkey
group by n_name
order by revenue desc
`

const Q5 = `
with
reg_nat as (select * from region, nation where r_regionkey = n_regionkey and r_name = 'ASIA'),
reg_nat_cust as (select * from reg_nat, customer where n_nationkey = c_nationkey),
reg_nat_cust_ord as (
    select * from reg_nat_cust, orders 
    where c_custkey = o_custkey
    and o_orderdate >= date '1994-01-01'
    and o_orderdate < date '1995-01-01'),
reg_nat_cust_ord_line as (select * from reg_nat_cust_ord, lineitem where o_orderkey = l_orderkey),
with_sup as (select * from reg_nat, supplier where n_nationkey = s_nationkey)


select n_name, sum(l_extendedprice * (1 - l_discount)) as revenue
from supplier, reg_nat_cust_ord_line 
where s_suppkey = l_suppkey
and c_nationkey = s_nationkey
and s_nationkey = n_nationkey 
group by n_name
order by revenue desc
`

const Q6 = `
select
	sum(l_extendedprice * l_discount) as revenue
from
	lineitem
where
	l_shipdate >= date '1994-01-01'
	and l_shipdate < date '1994-01-01' + interval '1' year
	and l_discount between .0499999 and .0700001
	and l_quantity < 24
`


// TODO try using table name instead of renaming (e.g., cus_nat.n_name instead of cust_nation)
const Q7 = `
with
cus_nat as (
    select c_custkey, n_name as cust_nation
    from nation, customer 
    where n_nationkey = c_nationkey
    and (n_name = 'FRANCE' or n_name = 'GERMANY')),
ord_cus as (select * from cus_nat, orders where c_custkey = o_custkey),
sup_nat as (
    select s_suppkey, n_name as supp_nation
    from nation, supplier
    where s_nationkey = n_nationkey
    and (n_name = 'FRANCE' or n_name = 'GERMANY')),
sup_line as (
    select 
        l_orderkey, 
        year(l_shipdate) as l_year, 
        l_extendedprice,
        l_discount,
        supp_nation
    from sup_nat, lineitem
    where l_suppkey = s_suppkey
    and l_shipdate between date '1995-01-01' and date '1996-12-31'
)

select
	supp_nation,
	cust_nation,
	l_year,
	sum(l_extendedprice * (1 - l_discount)) as revenue
from ord_cus, sup_line
where l_orderkey = o_orderkey
and (
    (supp_nation = 'FRANCE' and cust_nation = 'GERMANY')
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
    select n_nationkey as cust_nationkey 
    from region, nation 
    where r_regionkey = n_regionkey and r_name = 'AMERICA'),
cus_nat as (select * from nat_region, customer where cust_nationkey = c_nationkey),
ord_cus as (
    select o_orderkey, year(o_orderdate) as o_year
    from cus_nat, orders
    where c_custkey = o_custkey
    and o_orderdate between date '1995-01-01' and date '1996-12-31'),
par_line as (
    select * from part, lineitem
    where p_partkey = l_partkey
    and p_type = 'ECONOMY ANODIZED STEEL'),
sup_nat as (
    select s_suppkey, n_name as nation 
    from nation, supplier
    where n_nationkey = s_nationkey),
par_line_sup as(
    select * 
    from sup_nat, par_line 
    where s_suppkey = l_suppkey)

select
	o_year, 
	sum(case when nation = 'BRAZIL' then l_extendedprice * (1 - l_discount)  else 0 end) / sum(l_extendedprice * (1 - l_discount) ) as mkt_share
from par_line_sup, ord_cus 
where l_orderkey = o_orderkey
group by
	o_year
order by
	o_year
`


const Q8tmp = `
with
par_line as (
    select * from part, lineitem
    where p_partkey = l_partkey
    and p_type = 'ECONOMY ANODIZED STEEL'),

pl_ord as (
    select l_extendedprice, l_discount, l_suppkey, o_custkey, o_orderkey, year(o_orderdate) as o_year
    from par_line, orders
    where l_orderkey = o_orderkey
    and o_orderdate between date '1995-01-01' and date '1996-12-31'),

plo_cus as (
    select * from pl_ord, customer
    where o_custkey = c_custkey),
    
nat_reg as (
    select *
    from region, nation 
    where r_regionkey = n_regionkey and r_name = 'AMERICA'),

nr_ploc as (
    select * from nat_reg, plo_cus
    where n_nationkey = c_nationkey),

nrploc_supp as (
    select * from nr_ploc, supplier
    where l_suppkey = s_suppkey),
    
joined as (
    select n1.n_name, o_year, l_extendedprice * (1 - l_discount) as volume
    from nation n1, nrploc_supp
    where n1.n_nationkey = s_nationkey)

select
	o_year, 
	sum(case when n_name = 'BRAZIL' then volume else 0 end) / sum(volume) as mkt_share
from joined
group by
	o_year
order by
	o_year
`


const Q9current = `
with 
sup_nat as (
    select * from nation, supplier
    where s_nationkey = n_nationkey),
par_line as (
    select * from part, lineitem
    where l_partkey = p_partkey and p_name like '%green%'),
par_line_sup as (
    select * from sup_nat, par_line
    where l_suppkey = s_suppkey),
par_line_sup_ps as (
    select * from par_line_sup, partsupp
    where ps_partkey = l_partkey and ps_suppkey = l_suppkey),
profit as (
    select 
        n_name as nation, 
        year(o_orderdate) as o_year, 
        l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity as amount
    from par_line_sup_ps, orders
    where o_orderkey = l_orderkey)

select nation, o_year, sum(amount) as sum_profit from profit
group by nation, o_year order by nation, o_year DESC
`

const Q9explore = `
with 
sup_nat as (select * from nation, supplier where s_nationkey = n_nationkey),
par_psupp as (select * from part, partsupp where p_partkey = ps_partkey and p_name like '%green%'),     
sup_nat_par_psupp as (select * from sup_nat, par_psupp where s_suppkey = ps_suppkey),
sup_nat_par_psupp_line as (
    select * 
    from sup_nat_par_psupp, lineitem
    where ps_suppkey = l_suppkey
    and ps_partkey = l_partkey
--     and p_partkey = l_partkey
--     and s_suppkey = l_suppkey
),
profit as (
    select 
        n_name as nation, 
        year(o_orderdate) as o_year, 
        l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity as amount
    from sup_nat_par_psupp_line, orders
    where l_orderkey = o_orderkey)

select nation, o_year, sum(amount) as sum_profit from profit
group by nation, o_year order by nation, o_year DESC
`

// hypher
const Q9 = `
with 
sup_nat as (
    select * from nation, supplier
    where s_nationkey = n_nationkey),
    
par_supp as(
    select * from part, partsupp
    where p_partkey = ps_partkey
    and p_name like '%green%'),

sn_ps as (
    select * from sup_nat, par_supp
    where s_suppkey = ps_suppkey),

snps_line as (
    select * from sn_ps, lineitem
    where ps_partkey = l_partkey 
    and ps_suppkey = l_suppkey
    and s_suppkey = l_suppkey
    and p_partkey = l_partkey),

profit as (
    select 
        n_name as nation, 
        year(o_orderdate) as o_year, 
        l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity as amount
    from snps_line, orders
    where l_orderkey = o_orderkey)

select nation, o_year, sum(amount) as sum_profit from profit
group by nation, o_year order by nation, o_year DESC
`


const Q10 = `
with 
cus_nat as (select * from nation, customer where n_nationkey = c_nationkey),
ord_cus as (
    select * from cus_nat, orders
    where c_custkey = o_custkey
    and o_orderdate >= date '1993-10-01'
    and o_orderdate < date '1994-01-01'
)

select 
    c_custkey,
	c_name,
	sum(l_extendedprice * (1 - l_discount)) as revenue,
	c_acctbal,
	n_name,
	c_address,
	c_phone,
	c_comment
from ord_cus, lineitem
where o_orderkey = l_orderkey
and l_returnflag = 'R'
group by
	c_custkey,
	c_name,
	c_acctbal,
	c_phone,
	n_name,
	c_address,
	c_comment
order by
	revenue desc
limit 20
`


// TODO look at this -- get rid of 0+coalesce
const Q11 = `
with 
sup_nat as (
    select * from nation, supplier 
    where n_nationkey = s_nationkey and n_name = 'GERMANY'),
ps_sup as (
    select ps_partkey, 0+coalesce(sum(ps_supplycost * ps_availqty), 0) as value1
    from sup_nat, partsupp
    where s_suppkey = ps_suppkey
    group by ps_partkey),
value1_sum as (select sum(value1)*0.0001 as v from ps_sup)

select ps_partkey, value1 
from value1_sum, ps_sup
where value1 > v
order by value1 desc
`


// TODO look at this -- worst perf (projecting useless fields...)
const Q12 = `
select
	l_shipmode,
	sum(case
		when o_orderpriority = '1-URGENT'
			or o_orderpriority = '2-HIGH'
			then 1
		else 0
	end) as high_line_count,
	sum(case
		when o_orderpriority <> '1-URGENT'
			and o_orderpriority <> '2-HIGH'
			then 1
		else 0
	end) as low_line_count
from
	lineitem, orders
where
	o_orderkey = l_orderkey
	and l_shipmode in ('MAIL', 'SHIP')
	and l_commitdate < l_receiptdate
	and l_shipdate < l_commitdate
	and l_receiptdate >= date '1994-01-01'
	and l_receiptdate < date '1994-01-01' + interval '1' year
group by
	l_shipmode
order by
	l_shipmode
`



const Q13recheck = `
with c_orders as (
    select c_custkey, count(o_orderkey) as c_count
    from customer left outer join orders on c_custkey = o_custkey
    where not (o_comment like '%special%requests%')
    group by c_custkey
)
select c_count, count(*) as custdist
from c_orders
group by c_count
order by custdist desc, c_count desc
`

const Q13 = `
select
	c_count,
	count(*) as custdist
from
	(
		select
			c_custkey,
			count(o_orderkey) as c_count
		from
			customer left outer join orders on
				c_custkey = o_custkey
				and o_comment not like '%special%requests%'
		group by
			c_custkey
	) as c_orders
group by
	c_count
order by
	custdist desc,
	c_count desc
`



const Q14 = `
select
	100.00 * sum(case
		when p_type like 'PROMO%'
			then l_extendedprice * (1 - l_discount)
		else 0
	end) / sum(l_extendedprice * (1 - l_discount)) as promo_revenue
from
    lineitem, part
where
	l_partkey = p_partkey
	and l_shipdate >= date '1995-09-01'
	and l_shipdate < date '1995-09-01' + interval '1' month
`

const Q15 = `
with 
revenue0 as (
    select
		l_suppkey as supplier_no,
		sum(l_extendedprice * (1 - l_discount)) as total_revenue
	from
		lineitem
	where
		l_shipdate >= date '1996-01-01'
		and l_shipdate < date '1996-01-01' + interval '3' month
	group by
		l_suppkey), 
max_rev as (select max(total_revenue) as max_total_rev from revenue0)

select
	s_suppkey,
	s_name,
	s_address,
	s_phone,
	total_revenue
from max_rev, revenue0, supplier
where supplier_no = s_suppkey and total_revenue = max_total_rev
order by s_suppkey
`


const Q16 = `
with 
bad_sup as (select * from supplier where s_comment like '%Customer%Complaints%'),
ps_par as (
    select p_brand, p_type, p_size, ps_suppkey
    from part, partsupp
    where 
        p_partkey = ps_partkey 
        and p_brand <> 'Brand#45'
	    and p_type not like 'MEDIUM POLISHED%'
	    and p_size in (49, 14, 23, 45, 19, 3, 36, 9)
	    and ps_suppkey not in (select s_suppkey from bad_sup)
	group by p_brand, p_type, p_size, ps_suppkey
)

select
	p_brand,
	p_type,
	p_size,
	count(distinct ps_suppkey) as supplier_cnt
from ps_par
group by
	p_brand,
	p_type,
	p_size
order by
	supplier_cnt desc,
	p_brand,
	p_type,
	p_size
`



const Q17orig = `
select
	sum(l_extendedprice) / 7.0 as avg_yearly
from
	part, lineitem
where
	p_partkey = l_partkey 
	and p_brand = 'Brand#23'
	and p_container = 'MED BOX'
	and l_quantity < (
		select
			0.2 * avg(l_quantity)
		from
			lineitem
		where
			l_partkey = p_partkey
	)
`

const Q17a = `
with 
avg_q as (
    select l_partkey, avg(l_quantity) as avg_q
    from lineitem
    group by l_partkey),
par_line as (
    select l_extendedprice, l_quantity, p_partkey
    from part, lineitem
    where p_partkey = l_partkey
    and p_brand = 'Brand#23' and p_container = 'MED BOX')

select sum(l_extendedprice) / 7.0 as avg_yearly
from par_line, avg_q 
where l_partkey = p_partkey and l_quantity < (0.2 * avg_q)
`

const Q17 = `
with 
avg_q as (
    select l_partkey, avg(l_quantity) as avg_q
    from lineitem
    group by l_partkey),
part_scan as (select * from part where p_brand = 'Brand#23' and p_container = 'MED BOX'),
par_line_avg as (select p_partkey, avg_q from part_scan, avg_q where p_partkey = l_partkey)

select sum(l_extendedprice / 7.0) as avg_yearly
from par_line_avg, lineitem 
where p_partkey = l_partkey and l_quantity < (0.2 * avg_q)
`



const Q18 = `
with 
all_sums as (
    select l_orderkey, sum(l_quantity) as q_sums
    from lineitem
    group by l_orderkey),
-- having_sum as (select l_orderkey from all_sums where q_sums > 300),
ord_cus as (
    select c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice
    from customer, orders
    where c_custkey = o_custkey
    and o_orderkey in (select l_orderkey from all_sums where q_sums > 300))

select
	c_name,
	c_custkey,
	o_orderkey,
	o_orderdate,
	o_totalprice,
	sum(l_quantity) as sum_qty
from ord_cus, lineitem
where o_orderkey = l_orderkey
group by
	c_name,
	c_custkey,
	o_orderkey,
	o_orderdate,
	o_totalprice
order by
	o_totalprice desc,
	o_orderdate
limit 100
`



const Q19 = `
select
	sum(l_extendedprice* (1 - l_discount)) as revenue
from
	lineitem,
	part
where
	part.p_partkey = lineitem.l_partkey
    and l_shipmode in ('AIR', 'AIR REG')
	and l_shipinstruct = 'DELIVER IN PERSON'
	and
	(
        (
            p_brand = 'Brand#12'
            and p_container in ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG')
            and l_quantity >= 1 and l_quantity <= 1 + 10
            and p_size between 1 and 5
        )
        or
        (
            p_brand = 'Brand#23'
            and p_container in ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK')
            and l_quantity >= 10 and l_quantity <= 10 + 10
            and p_size between 1 and 10
        )
        or
        (
            p_brand = 'Brand#34'
            and p_container in ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG')
            and l_quantity >= 20 and l_quantity <= 20 + 10
            and p_size between 1 and 15
        )
    )
`



const Q20Orig = `
select
	s_name,
	s_address
from
	nation,
	supplier
where
	s_suppkey in (
		select
			ps_suppkey
		from
			partsupp
		where
			ps_partkey in (
				select
					p_partkey
				from
					part
				where
					p_name like 'forest%'
			)
			and ps_availqty > (
				select
					0.5 * sum(l_quantity)
				from
					lineitem
				where
					l_partkey = ps_partkey
					and l_suppkey = ps_suppkey
					and l_shipdate >= date '1994-01-01'
					and l_shipdate < date '1994-01-01' + interval '1' year
			)
	)
	and s_nationkey = n_nationkey
	and n_name = 'CANADA'
order by
	s_name
`

const Q20_1 = `
with 
ps_part as (
    select ps_availqty, ps_partkey, ps_suppkey
	from partsupp
	where ps_partkey in (select p_partkey from part where p_name like 'forest%'))
select
	s_name,
	s_address
from
	nation,
	supplier
where
	s_suppkey in (
		select ps_suppkey
		from ps_part
		where ps_availqty > (
			select 0.5 * sum(l_quantity)
			from lineitem
			where
					l_partkey = ps_partkey
					and l_suppkey = ps_suppkey
					and l_shipdate >= date '1994-01-01'
					and l_shipdate < date '1994-01-01' + interval '1' year
			)
	)
	and s_nationkey = n_nationkey
	and n_name = 'CANADA'
order by
	s_name
`


const Q20 = `
with 
ps_part as (
    select ps_availqty, ps_partkey, ps_suppkey
	from part, partsupp 
	where p_partkey = ps_partkey 
	and p_name like 'forest%'),
	
group_lineitem as (
    select l_partkey, l_suppkey, sum(l_quantity) as sumq
    from lineitem
    where l_shipdate >= date '1994-01-01'
    and l_shipdate < date '1994-01-01' + interval '1' year
    group by l_partkey, l_suppkey
),
ps_line as (
    select *
    from ps_part, group_lineitem
    where ps_partkey = l_partkey
    and ps_suppkey = l_suppkey 
    and ps_availqty > 0.5 * sumq
),
 
sup_nat as (
    select * 
    from nation, supplier
    where n_nationkey = s_nationkey
    and n_name = 'CANADA')
    
select s_name, s_address
from sup_nat
where s_suppkey in (select ps_suppkey from ps_line)
order by s_name 
`


const Q20tmp2 = `
with 
ps_part as (
    select ps_availqty, ps_partkey, ps_suppkey
	from partsupp
	where ps_partkey in (select p_partkey from part where p_name like 'forest%')),
group_lineitem as (
    select l_partkey, l_suppkey,  0.5 * sum(l_quantity) as sumq
    from lineitem
    where l_shipdate >= date '1994-01-01'
    and l_shipdate < date '1994-01-01' + interval '1' year
    group by l_partkey, l_suppkey
),
ps_line as (
    select *
    from ps_part, group_lineitem
    where ps_partkey = l_partkey
    and ps_suppkey = l_suppkey 
),
sup_nat as (
    select s_name, s_address, s_suppkey
    from nation, supplier
    where n_nationkey = s_nationkey and n_name = 'CANADA'
)

select s_name, s_address
from sup_nat left join ps_line on s_suppkey = ps_suppkey
where ps_availqty > sumq
order by s_name
`


const Q21 = `
select
	s_name,
	count(*) as numwait
from
	nation,
	supplier,
	lineitem l1,
	orders

where
	s_suppkey = l1.l_suppkey
	and o_orderkey = l1.l_orderkey
	and o_orderstatus = 'F'
	and l1.l_receiptdate > l1.l_commitdate
	and s_nationkey = n_nationkey
	and n_name = 'SAUDI ARABIA'
	and exists (
		select
			*
		from
			lineitem l2
		where
			l2.l_orderkey = l1.l_orderkey
			and l2.l_suppkey <> l1.l_suppkey
	)
	and not exists (
		select
			*
		from
			lineitem l3
		where
			l3.l_orderkey = l1.l_orderkey
			and l3.l_suppkey <> l1.l_suppkey
			and l3.l_receiptdate > l3.l_commitdate
	)
group by
	s_name
order by
	numwait desc,
	s_name
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
    and not exists (select * from orders where o_custkey = c_custkey))


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