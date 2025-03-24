package com.gjyy.ai.dao;

import com.gjyy.ai.vo.CallRecordsVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class CallRecordsDao {

    private final JdbcTemplate jdbcTemplate;

    public CallRecordsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CallRecordsVO getCallRecordById(int id) {
        String sql = "SELECT id, call_time, call_count, call_input,call_ip, call_user_code FROM gjyy_ai_api_call_records WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new CallRecordRowMapper(), id);
    }

    private static final class CallRecordRowMapper implements RowMapper<CallRecordsVO> {

        @Override
        public CallRecordsVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            CallRecordsVO callRecordsVO = new CallRecordsVO();
            callRecordsVO.setId(rs.getInt("id"));
            callRecordsVO.setCall_time(rs.getTimestamp("call_time")==null?null:rs.getTimestamp("call_time").toLocalDateTime());
            callRecordsVO.setCall_count(rs.getInt("call_count"));
            callRecordsVO.setCall_input(rs.getString("call_input"));
            callRecordsVO.setCall_ip(rs.getString("call_ip"));
            callRecordsVO.setCall_user_code(rs.getString("call_user_code"));
            return callRecordsVO;
        }
    }
}