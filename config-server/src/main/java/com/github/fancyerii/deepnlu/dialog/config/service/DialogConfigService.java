package com.github.fancyerii.deepnlu.dialog.config.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.github.fancyerii.deepnlu.dialog.config.data.RobotConfig;
@Service
public class DialogConfigService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT="insert into robot_config(robot_id, robot_type, robot_key, cfg_json, last_update) values(?,?,?,?,now())";    
    private static final String SQL_DELETE="delete from robot_config where robot_id=? and robot_type=? and robot_key=?";    
    private static final String SQL_QUERY_BY_ALL_KEY="select cfg_json from robot_config where robot_id=? and robot_type=? and robot_key=?";
    private static final String SQL_QUERY_BY_TYPE="select robot_key, cfg_json from robot_config where robot_id=? and robot_type=?";
    private static final String SQL_UPSERT="insert into robot_config(robot_id, robot_type, robot_key, cfg_json, last_update) values(?,?,?,?,now())"+
    		" on duplicate key update cfg_json=?, last_update=now()";    
    
    
    public void insertConfig(RobotConfig cfg) throws ConfigServerException{
    	try {
    		jdbcTemplate.update(SQL_INSERT, cfg.getRobotId(), cfg.getCfgType(), cfg.getCfgKey(), cfg.getCfgJson());
    	}catch(DataAccessException e) {
    		throw new ConfigServerException(e.getMessage(), e);
    	}
    }
    
    public void upsertConfig(RobotConfig cfg) throws ConfigServerException{
    	try {
    		jdbcTemplate.update(SQL_UPSERT, cfg.getRobotId(), cfg.getCfgType(), cfg.getCfgKey(), cfg.getCfgJson(), cfg.getCfgJson());
    	}catch(DataAccessException e) {
    		throw new ConfigServerException(e.getMessage(), e);
    	}
    }
    
    public void delConfig(String robotId, String type, String key)  throws ConfigServerException{
    	try {
    		jdbcTemplate.update(SQL_DELETE, robotId, type, key);
    	}catch(DataAccessException e) {
    		throw new ConfigServerException(e.getMessage(), e);
    	}
    	
    }
    
    public RobotConfig getConfig(String robotId, String type, String key) throws ConfigServerException{
    	try {
	    	return jdbcTemplate.query(SQL_QUERY_BY_ALL_KEY, new Object[] {robotId, type, key}, new ResultSetExtractor<RobotConfig>() {
				@Override
				public RobotConfig extractData(ResultSet rs) throws SQLException, DataAccessException {
					if(!rs.next()) return null;
					return new RobotConfig(robotId, type, key, rs.getString(1));
				}    		
	    	});
    	}catch(DataAccessException e) {
    		throw new ConfigServerException(e.getMessage(), e);
    	}
    }
    
    public List<RobotConfig> getConfigs(String robotId, String type) throws ConfigServerException{
    	try {
    		return jdbcTemplate.query(SQL_QUERY_BY_TYPE, new Object[] {robotId, type}, new RowMapper<RobotConfig>() {
				@Override
				public RobotConfig mapRow(ResultSet rs, int pos) throws SQLException {
					return new RobotConfig(robotId, type, rs.getString(1), rs.getString(2));
				}
    			
    		});
    	}catch(DataAccessException e) {
    		throw new ConfigServerException(e.getMessage(), e);
    	}
    }
}
