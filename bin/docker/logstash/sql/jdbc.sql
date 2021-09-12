SELECT
    rpid, type_id, dynamic_id, mid, oid, ctime, m_name, content, like_num
FROM
reply  where ctime > :sql_last_value order by ctime
