package org.mifosplatform.provisioning.provisioning.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.billing.paymode.data.McodeData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningCommandParameterData;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningData;
import org.mifosplatform.provisioning.provisioning.data.ServiceParameterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProvisioningReadPlatformServiceImpl implements ProvisioningReadPlatformService {
	
	   private final JdbcTemplate jdbcTemplate;
	   private final PlatformSecurityContext context;
	   
	   @Autowired
	    public ProvisioningReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
	        this.context = context;
	        this.jdbcTemplate = new JdbcTemplate(dataSource);

	    }
	   
	   @Override
		public ProvisioningData retrieveIdData(Long id) {	
			try {
				context.authenticatedUser();
				
				ProvisioningMapper rm = new ProvisioningMapper();
				
				final String sql = "select "+rm.schema()+" and p.id=?";
				 
				return jdbcTemplate.queryForObject(sql, rm, new Object[] {id});
				} catch (EmptyResultDataAccessException e) {
				return null;
				}
		}

	@Override
	public List<ProvisioningData> getProvisioningData() {						
		try {
			
			 ProvisioningMapper rm = new ProvisioningMapper();
			 final String sql = "select "+rm.schema();
			return jdbcTemplate.query(sql, rm, new Object[] {});
			} catch (EmptyResultDataAccessException e) {
			return null;
			}
	}
	 private static final class ProvisioningMapper implements RowMapper<ProvisioningData> {

		    public String schema() {
				return " p.id as id,p.provisioning_system as ProvisioningSystem,p.command_name as CommandName,p.status as status from b_command p where p.is_deleted='N' ";
			}
		    
	        @Override
	        public ProvisioningData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			  Long id = rs.getLong("id");
			  String ProvisioningSystem=rs.getString("ProvisioningSystem");
			  String CommandName=rs.getString("CommandName");
			  String status=rs.getString("status");
			  return new ProvisioningData(id,ProvisioningSystem,CommandName,status);
	       }
	}
	 
	 @Override
		public List<McodeData> retrieveProvisioningCategory() {
			context.authenticatedUser();

			SystemDataMapper mapper = new SystemDataMapper();

			String sql = "select " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] { "Provisioning" });
		}

		@Override
		public List<McodeData> retrievecommands() {
			context.authenticatedUser();

			SystemDataMapper mapper = new SystemDataMapper();

			String sql = "select " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] { "Command" });
		}

		private static final class SystemDataMapper implements RowMapper<McodeData> {

			public String schema() {


				return " mc.id as id,mc.code_value as codeValue from m_code m,m_code_value mc where m.id = mc.code_id and m.code_name=? ";

			}

			@Override
			public McodeData mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				Long id=rs.getLong("id");
				String codeValue = rs.getString("codeValue");
				return new McodeData(id,codeValue);
				
			}

		}

		@Override
		public List<ProvisioningCommandParameterData> retrieveCommandParams(Long id) {
			try {
				
				 ProvisioningCommandMapper rm = new ProvisioningCommandMapper();
				 final String sql = "select "+rm.schema();
				 return jdbcTemplate.query(sql, rm, new Object[] {id});
				} catch (EmptyResultDataAccessException e) {
				return null;
				}
		}

		 private static final class ProvisioningCommandMapper implements RowMapper<ProvisioningCommandParameterData> {

			    public String schema() {
					return " c.id as id, c.command_param as commandParam,c.param_type as paramType from b_command_parameters c where c.command_id=? ";
				}
			    
		        @Override
		        public ProvisioningCommandParameterData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				  Long id = rs.getLong("id");
				  String commandParam=rs.getString("commandParam");
				  String paramType=rs.getString("paramType");
				  return new ProvisioningCommandParameterData(id,commandParam,paramType);
		       }
		}

		 @Transactional
		 @Override
		public List<ServiceParameterData> getSerivceParameters(Long orderId) {
			
			try{
				this.context.authenticatedUser();
				ServiceParameterMapper mapper=new ServiceParameterMapper();
				final String sql="select "+mapper.schema();
				return this.jdbcTemplate.query(sql, mapper,new Object[] {orderId});
				
			}catch(EmptyResultDataAccessException exception){
				return null;
			}
			
		}
		
		 private static final class ServiceParameterMapper implements RowMapper<ServiceParameterData> {

			    public String schema() {
					return " sd.id as id,sd.service_identification as paramName,sd.image as paramValue FROM b_orders o, b_plan_master p,b_service s," +
							" b_plan_detail pd,b_prov_service_details sd WHERE  o.id= ? AND p.id = o.plan_id AND pd.plan_id = p.id AND " +
							" pd.service_code = s.service_code and sd.service_id=s.id";
				}
			    
			    
			    public String provisionedschema() {
					return "  s.id AS id,s.parameter_name AS paramName,s.parameter_value AS paramValue  FROM b_service_parameters s " +
							"  WHERE s.order_id = ?";
				}
			    
		        @Override
		        public ServiceParameterData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		        	
				  Long id = rs.getLong("id");
				  String paramName=rs.getString("paramName");
				  String paramValue=rs.getString("paramValue");
				  return new ServiceParameterData(id,paramName,paramValue);
		       }
		}
		 
		 
		 @Transactional
		 @Override
		public List<ServiceParameterData> getProvisionedSerivceParameters(Long orderId) {
			
			try{
				this.context.authenticatedUser();
				ServiceParameterMapper mapper=new ServiceParameterMapper();
				final String sql="select "+mapper.provisionedschema();
				return this.jdbcTemplate.query(sql, mapper,new Object[] {orderId});
				
			}catch(EmptyResultDataAccessException exception){
				return null;
			}
			
		}

	


}