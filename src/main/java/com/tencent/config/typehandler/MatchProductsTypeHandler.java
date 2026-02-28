package com.tencent.config.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.vo.MatchRecords;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class MatchProductsTypeHandler extends BaseTypeHandler<List<MatchRecords.Product>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<MatchRecords.Product>> TYPE_REF = new TypeReference<>() {
    };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<MatchRecords.Product> parameter,
                                    JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize products as JSON", e);
        }
    }

    @Override
    public List<MatchRecords.Product> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseProducts(rs.getString(columnName));
    }

    @Override
    public List<MatchRecords.Product> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseProducts(rs.getString(columnIndex));
    }

    @Override
    public List<MatchRecords.Product> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseProducts(cs.getString(columnIndex));
    }

    private List<MatchRecords.Product> parseProducts(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize products JSON: " + json, e);
        }
    }
}
